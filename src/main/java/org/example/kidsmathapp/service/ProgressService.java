package org.example.kidsmathapp.service;

import lombok.RequiredArgsConstructor;
import org.example.kidsmathapp.dto.progress.AchievementDto;
import org.example.kidsmathapp.dto.progress.DashboardDto;
import org.example.kidsmathapp.dto.progress.LessonCompletionResult;
import org.example.kidsmathapp.dto.progress.LessonProgressDto;
import org.example.kidsmathapp.dto.progress.TopicProgressDto;
import org.example.kidsmathapp.entity.Child;
import org.example.kidsmathapp.entity.Lesson;
import org.example.kidsmathapp.entity.Progress;
import org.example.kidsmathapp.entity.Topic;
import org.example.kidsmathapp.entity.enums.RankLevel;
import org.example.kidsmathapp.exception.ApiException;
import org.example.kidsmathapp.repository.ChildRepository;
import org.example.kidsmathapp.repository.LessonRepository;
import org.example.kidsmathapp.repository.ProgressRepository;
import org.example.kidsmathapp.repository.TopicRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * GAMIFICATION PIPELINE
 * ProgressService ──► GamificationOrchestrator ──► PointsService
 *                                              ├──► StreakService
 *                                              └──► AchievementService
 *
 * All three are called within ProgressService's @Transactional boundary.
 * Failure in any step rolls back the entire lesson completion.
 */
@Service
@RequiredArgsConstructor
public class ProgressService {

    private final ProgressRepository progressRepository;
    private final ChildRepository childRepository;
    private final LessonRepository lessonRepository;
    private final TopicRepository topicRepository;
    private final GamificationOrchestrator gamificationOrchestrator;
    private final AchievementService achievementService;
    private final StreakService streakService;

    private static final int HIGH_SCORE_THRESHOLD = 90;
    private static final double HIGH_SCORE_BONUS_MULTIPLIER = 0.5;

    @Transactional(readOnly = true)
    public Optional<Progress> getProgress(Long childId, Long lessonId) {
        return progressRepository.findByChildIdAndLessonId(childId, lessonId);
    }

    @Transactional(readOnly = true)
    public List<Progress> getProgressForChild(Long childId) {
        return progressRepository.findByChildId(childId);
    }

    @Transactional(readOnly = true)
    public List<Progress> getCompletedProgressForChild(Long childId) {
        return progressRepository.findByChildIdAndCompleted(childId, true);
    }

    @Transactional(readOnly = true)
    public int countCompletedLessonsForTopic(Long childId, Long topicId) {
        List<Lesson> lessons = lessonRepository.findByTopicId(topicId);
        int count = 0;
        for (Lesson lesson : lessons) {
            Optional<Progress> progress = progressRepository.findByChildIdAndLessonId(childId, lesson.getId());
            if (progress.isPresent() && Boolean.TRUE.equals(progress.get().getCompleted())) {
                count++;
            }
        }
        return count;
    }

    @Transactional
    public Progress recordProgress(Long childId, Long lessonId, int score, boolean completed) {
        Child child = childRepository.findById(childId)
                .orElseThrow(() -> ApiException.notFound("Child not found"));

        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> ApiException.notFound("Lesson not found"));

        return createOrUpdateProgressRecord(child, lesson, score, completed);
    }

    @Transactional(readOnly = true)
    public boolean isLessonCompleted(Long childId, Long lessonId) {
        return progressRepository.findByChildIdAndLessonId(childId, lessonId)
                .map(Progress::getCompleted)
                .orElse(false);
    }

    /**
     * Records lesson completion with full gamification features:
     * - Creates/updates Progress record
     * - Awards stars based on lesson.starsReward (with bonus for high scores)
     * - Logs points to PointsLog
     * - Updates child's totalStars
     * - Updates streak if first activity today
     * - Checks and awards any unlocked achievements
     */
    @Transactional
    public LessonCompletionResult recordLessonCompletion(Long childId, Long lessonId, int score) {
        Child child = childRepository.findById(childId)
                .orElseThrow(() -> ApiException.notFound("Child not found with id: " + childId));
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> ApiException.notFound("Lesson not found with id: " + lessonId));

        createOrUpdateProgressRecord(child, lesson, score, true);

        int baseStars = lesson.getStarsReward();
        int bonusStars = score >= HIGH_SCORE_THRESHOLD
                ? (int) Math.round(baseStars * HIGH_SCORE_BONUS_MULTIPLIER)
                : 0;
        int totalStarsEarned = baseStars + bonusStars;

        String reason = String.format("Completed lesson: %s (Score: %d%%)", lesson.getTitle(), score);
        GamificationOrchestrator.OrchestratorResult gamification =
                gamificationOrchestrator.orchestrate(childId, totalStarsEarned, reason);

        child = childRepository.findById(childId).orElse(child);

        org.example.kidsmathapp.dto.inventory.InventoryItemDto droppedItem =
                gamification.newItems().isEmpty() ? null : gamification.newItems().get(0);

        return LessonCompletionResult.builder()
                .starsEarned(baseStars)
                .bonusStars(bonusStars)
                .totalStars(child.getTotalStars())
                .newAchievements(gamification.newAchievements())
                .streakUpdated(gamification.streakUpdated())
                .currentStreak(child.getCurrentStreak())
                .newItem(droppedItem)
                .build();
    }

    private Progress createOrUpdateProgressRecord(Child child, Lesson lesson, int score, boolean markCompleted) {
        Progress progress = progressRepository.findByChildIdAndLessonId(child.getId(), lesson.getId())
                .orElse(Progress.builder()
                        .child(child)
                        .lesson(lesson)
                        .completed(false)
                        .build());

        if (score > (progress.getScore() != null ? progress.getScore() : 0)) {
            progress.setScore(score);
        }

        if (markCompleted && !Boolean.TRUE.equals(progress.getCompleted())) {
            progress.setCompleted(true);
            progress.setCompletedAt(LocalDateTime.now());
        }

        return progressRepository.save(progress);
    }

    /**
     * Gets detailed progress for a specific topic.
     * Returns percentage of lessons completed and list of lessons with completion status.
     */
    @Transactional(readOnly = true)
    public TopicProgressDto getTopicProgress(Long childId, Long topicId) {
        if (!childRepository.existsById(childId)) {
            throw ApiException.notFound("Child not found with id: " + childId);
        }

        Topic topic = topicRepository.findById(topicId)
                .orElseThrow(() -> ApiException.notFound("Topic not found with id: " + topicId));

        List<Lesson> lessons = lessonRepository.findByTopicIdOrderByOrderIndexAsc(topicId);
        List<Progress> progressList = progressRepository.findByChildIdAndLessonTopicId(childId, topicId);

        // Map lesson id to progress for quick lookup
        Map<Long, Progress> progressMap = progressList.stream()
                .collect(Collectors.toMap(p -> p.getLesson().getId(), Function.identity(), (a, b) -> a));

        List<LessonProgressDto> lessonProgressDtos = new ArrayList<>();
        int completedCount = 0;

        for (Lesson lesson : lessons) {
            Progress progress = progressMap.get(lesson.getId());
            boolean completed = progress != null && Boolean.TRUE.equals(progress.getCompleted());

            if (completed) {
                completedCount++;
            }

            lessonProgressDtos.add(LessonProgressDto.builder()
                    .lessonId(lesson.getId())
                    .lessonTitle(lesson.getTitle())
                    .orderIndex(lesson.getOrderIndex())
                    .completed(completed)
                    .score(progress != null ? progress.getScore() : null)
                    .completedAt(progress != null ? progress.getCompletedAt() : null)
                    .build());
        }

        int totalLessons = lessons.size();
        double percentComplete = totalLessons > 0 ? (completedCount * 100.0 / totalLessons) : 0;

        return TopicProgressDto.builder()
                .topicId(topic.getId())
                .topicName(topic.getName())
                .lessonsCompleted(completedCount)
                .totalLessons(totalLessons)
                .percentComplete(percentComplete)
                .lessons(lessonProgressDtos)
                .build();
    }

    /**
     * Gets a comprehensive dashboard for a child including:
     * - Total stars
     * - Current streak
     * - Topics with progress percentages
     * - Recent achievements
     * - Daily challenge status
     */
    @Transactional(readOnly = true)
    public DashboardDto getChildDashboard(Long childId) {
        Child child = childRepository.findById(childId)
                .orElseThrow(() -> ApiException.notFound("Child not found with id: " + childId));

        // Get all topics and calculate progress for each
        List<Topic> topics = topicRepository.findAllByOrderByOrderIndexAsc();
        List<TopicProgressDto> topicProgressList = new ArrayList<>();

        for (Topic topic : topics) {
            List<Lesson> lessons = lessonRepository.findByTopicIdOrderByOrderIndexAsc(topic.getId());
            if (lessons.isEmpty()) {
                continue;
            }

            List<Progress> progressList = progressRepository.findByChildIdAndLessonTopicId(childId, topic.getId());
            long completedCount = progressList.stream()
                    .filter(p -> Boolean.TRUE.equals(p.getCompleted()))
                    .count();

            double percentComplete = (completedCount * 100.0 / lessons.size());

            topicProgressList.add(TopicProgressDto.builder()
                    .topicId(topic.getId())
                    .topicName(topic.getName())
                    .lessonsCompleted((int) completedCount)
                    .totalLessons(lessons.size())
                    .percentComplete(percentComplete)
                    .build());
        }

        // Get recent achievements (last 5)
        List<AchievementDto> recentAchievements = achievementService.getRecentAchievements(childId, 5);

        // Check if daily challenge is complete (had activity today)
        boolean dailyChallengeComplete = streakService.hasActivityToday(childId);

        RankLevel rankLevel = RankLevel.fromStars(child.getTotalStars());
        int starsToNextRank = rankLevel.getNextLevelStars() - child.getTotalStars();

        return DashboardDto.builder()
                .childId(child.getId())
                .childName(child.getName())
                .totalStars(child.getTotalStars())
                .currentStreak(child.getCurrentStreak())
                .topics(topicProgressList)
                .recentAchievements(recentAchievements)
                .dailyChallengeComplete(dailyChallengeComplete)
                .rankLevel(rankLevel.name())
                .rankLevelEmoji(rankLevel.getEmoji())
                .starsToNextRank(Math.max(0, starsToNextRank))
                .build();
    }
}
