package org.example.kidsmathapp.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.kidsmathapp.dto.content.*;
import org.example.kidsmathapp.entity.Child;
import org.example.kidsmathapp.entity.Lesson;
import org.example.kidsmathapp.entity.Progress;
import org.example.kidsmathapp.entity.Question;
import org.example.kidsmathapp.entity.Topic;
import org.example.kidsmathapp.exception.ApiException;
import org.example.kidsmathapp.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ContentService {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final TopicRepository topicRepository;
    private final LessonRepository lessonRepository;
    private final QuestionRepository questionRepository;
    private final ChildRepository childRepository;
    private final ProgressService progressService;
    private final ProgressRepository progressRepository;

    @Transactional(readOnly = true)
    public List<TopicWithProgressDto> getTopicsWithProgress(Long childId) {
        Child child = childRepository.findById(childId)
                .orElseThrow(() -> ApiException.notFound("Child not found"));

        List<Topic> topics = topicRepository.findByGradeLevelOrderByOrderIndexAsc(child.getGradeLevel());
        List<TopicWithProgressDto> result = new ArrayList<>();

        // N+1 FIX: Load all completion counts in one query before the loop.
        Map<Long, Integer> completionCounts = new HashMap<>();
        List<Object[]> rawCounts = progressRepository.countCompletedGroupByTopicId(childId);
        for (Object[] row : rawCounts) {
            Long topicId = (Long) row[0];
            Long count = (Long) row[1];
            completionCounts.put(topicId, count.intValue());
        }

        boolean previousTopicCompleted = true;

        for (int i = 0; i < topics.size(); i++) {
            Topic topic = topics.get(i);
            int totalLessons = topic.getLessons().size();
            int completedLessons = completionCounts.getOrDefault(topic.getId(), 0);

            int progressPercent = totalLessons > 0
                    ? (int) Math.round((completedLessons * 100.0) / totalLessons)
                    : 0;

            // First topic is always unlocked, others unlock after completing any 1 lesson in the previous topic
            boolean isUnlocked = (i == 0) || previousTopicCompleted;

            result.add(TopicWithProgressDto.builder()
                    .id(topic.getId())
                    .name(topic.getName())
                    .description(topic.getDescription())
                    .iconName(topic.getIconName())
                    .lessonsCompleted(completedLessons)
                    .totalLessons(totalLessons)
                    .progressPercent(progressPercent)
                    .isUnlocked(isUnlocked)
                    .build());

            // Unlock next topic after completing at least 1 lesson in the current topic
            previousTopicCompleted = completedLessons >= 1;
        }

        return result;
    }

    @Transactional(readOnly = true)
    public List<LessonWithProgressDto> getLessonsForTopic(Long topicId, Long childId) {
        Topic topic = topicRepository.findById(topicId)
                .orElseThrow(() -> ApiException.notFound("Topic not found"));

        List<Lesson> lessons = lessonRepository.findByTopicIdOrderByOrderIndexAsc(topicId);

        return lessons.stream()
                .map(lesson -> {
                    Optional<Progress> progress = progressService.getProgress(childId, lesson.getId());

                    return LessonWithProgressDto.builder()
                            .id(lesson.getId())
                            .title(lesson.getTitle())
                            .description(lesson.getDescription())
                            .orderIndex(lesson.getOrderIndex())
                            .starsReward(lesson.getStarsReward())
                            .topicId(topic.getId())
                            .topicName(topic.getName())
                            .completed(progress.map(Progress::getCompleted).orElse(false))
                            .score(progress.map(Progress::getScore).orElse(null))
                            .completedAt(progress.map(Progress::getCompletedAt).orElse(null))
                            .build();
                })
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public LessonDetailDto getLessonDetail(Long lessonId) {
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> ApiException.notFound("Lesson not found"));

        Topic topic = lesson.getTopic();
        List<QuestionDto> questions = getQuestionsForLesson(lessonId);

        return LessonDetailDto.builder()
                .id(lesson.getId())
                .title(lesson.getTitle())
                .description(lesson.getDescription())
                .orderIndex(lesson.getOrderIndex())
                .starsReward(lesson.getStarsReward())
                .topicId(topic.getId())
                .topicName(topic.getName())
                .content(lesson.getContent())
                .lessonMode(lesson.getLessonMode())
                .questions(questions)
                .build();
    }

    @Transactional(readOnly = true)
    public List<QuestionDto> getQuestionsForLesson(Long lessonId) {
        List<Question> questions = questionRepository.findByLessonId(lessonId);

        return questions.stream()
                .map(this::toQuestionDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public TopicDto getTopicById(Long topicId) {
        Topic topic = topicRepository.findById(topicId)
                .orElseThrow(() -> ApiException.notFound("Topic not found"));

        return TopicDto.builder()
                .id(topic.getId())
                .name(topic.getName())
                .description(topic.getDescription())
                .iconName(topic.getIconName())
                .orderIndex(topic.getOrderIndex())
                .gradeLevel(topic.getGradeLevel())
                .build();
    }

    private QuestionDto toQuestionDto(Question question) {
        List<String> options = parseOptions(question.getOptions());

        return QuestionDto.builder()
                .id(question.getId())
                .questionText(question.getQuestionText())
                .questionType(question.getQuestionType())
                .options(options)
                .difficulty(question.getDifficulty())
                .imageUrl(question.getImageUrl())
                .build();
    }

    private List<String> parseOptions(String optionsJson) {
        if (optionsJson == null || optionsJson.isBlank()) {
            return Collections.emptyList();
        }

        try {
            return OBJECT_MAPPER.readValue(optionsJson, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            log.warn("Failed to parse options JSON: {}", optionsJson, e);
            return Collections.emptyList();
        }
    }
}
