package org.example.kidsmathapp.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.kidsmathapp.dto.progress.AchievementDto;
import org.example.kidsmathapp.entity.Achievement;
import org.example.kidsmathapp.entity.Child;
import org.example.kidsmathapp.entity.ChildAchievement;
import org.example.kidsmathapp.exception.ApiException;
import org.example.kidsmathapp.repository.AchievementRepository;
import org.example.kidsmathapp.repository.ChildAchievementRepository;
import org.example.kidsmathapp.repository.ChildRepository;
import org.example.kidsmathapp.repository.LessonRepository;
import org.example.kidsmathapp.repository.ProgressRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AchievementService {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final AchievementRepository achievementRepository;
    private final ChildAchievementRepository childAchievementRepository;
    private final ChildRepository childRepository;
    private final ProgressRepository progressRepository;
    private final LessonRepository lessonRepository;

    @Transactional
    public List<AchievementDto> checkAndAwardAchievements(Long childId) {
        Child child = childRepository.findById(childId)
                .orElseThrow(() -> ApiException.notFound("Child not found with id: " + childId));

        // Get all achievements the child hasn't earned yet
        Set<Long> earnedAchievementIds = childAchievementRepository.findByChildId(childId)
                .stream()
                .map(ca -> ca.getAchievement().getId())
                .collect(Collectors.toSet());

        List<Achievement> allAchievements = achievementRepository.findAll();
        List<Achievement> unearnedAchievements = allAchievements.stream()
                .filter(a -> !earnedAchievementIds.contains(a.getId()))
                .collect(Collectors.toList());

        List<AchievementDto> newlyUnlocked = new ArrayList<>();

        for (Achievement achievement : unearnedAchievements) {
            if (evaluateCondition(child, achievement.getUnlockCondition())) {
                // Award the achievement
                ChildAchievement childAchievement = ChildAchievement.builder()
                        .child(child)
                        .achievement(achievement)
                        .unlockedAt(LocalDateTime.now())
                        .build();
                childAchievementRepository.save(childAchievement);

                // Award bonus stars if any
                if (achievement.getStarsBonus() != null && achievement.getStarsBonus() > 0) {
                    child.setTotalStars(child.getTotalStars() + achievement.getStarsBonus());
                    childRepository.save(child);
                }

                newlyUnlocked.add(toAchievementDto(achievement, childAchievement.getUnlockedAt()));
            }
        }

        return newlyUnlocked;
    }

    @Transactional(readOnly = true)
    public List<AchievementDto> getChildAchievements(Long childId) {
        if (!childRepository.existsById(childId)) {
            throw ApiException.notFound("Child not found with id: " + childId);
        }

        List<ChildAchievement> earnedAchievements = childAchievementRepository.findByChildIdOrderByUnlockedAtDesc(childId);

        return earnedAchievements.stream()
                .map(ca -> toAchievementDto(ca.getAchievement(), ca.getUnlockedAt()))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AchievementDto> getRecentAchievements(Long childId, int limit) {
        if (!childRepository.existsById(childId)) {
            throw ApiException.notFound("Child not found with id: " + childId);
        }

        List<ChildAchievement> recentAchievements = childAchievementRepository
                .findByChildIdOrderByUnlockedAtDesc(childId, PageRequest.of(0, limit));

        return recentAchievements.stream()
                .map(ca -> toAchievementDto(ca.getAchievement(), ca.getUnlockedAt()))
                .collect(Collectors.toList());
    }

    /**
     * Evaluates an achievement unlock condition JSON.
     * 
     * Supported condition types:
     * - lessons_completed: {"type": "lessons_completed", "count": 10}
     * - topic_completed: {"type": "topic_completed", "topicId": 1}
     * - streak: {"type": "streak", "days": 7}
     * - stars: {"type": "stars", "count": 100}
     * - topics_started: {"type": "topics_started", "count": 3}
     */
    public boolean evaluateCondition(Child child, String conditionJson) {
        if (conditionJson == null || conditionJson.trim().isEmpty()) {
            return false;
        }

        try {
            JsonNode condition = OBJECT_MAPPER.readTree(conditionJson);
            String type = condition.has("type") ? condition.get("type").asText() : "";

            switch (type) {
                case "lessons_completed":
                    return evaluateLessonsCompleted(child, condition);
                case "topic_completed":
                    return evaluateTopicCompleted(child, condition);
                case "streak":
                    return evaluateStreak(child, condition);
                case "stars":
                    return evaluateStars(child, condition);
                case "topics_started":
                    return evaluateTopicsStarted(child, condition);
                default:
                    log.warn("Unknown achievement condition type: {}", type);
                    return false;
            }
        } catch (JsonProcessingException e) {
            log.error("Failed to parse achievement condition JSON: {}", conditionJson, e);
            return false;
        }
    }

    private boolean evaluateLessonsCompleted(Child child, JsonNode condition) {
        int requiredCount = condition.has("count") ? condition.get("count").asInt() : 0;
        long completedCount = progressRepository.countByChildIdAndCompletedTrue(child.getId());
        return completedCount >= requiredCount;
    }

    private boolean evaluateTopicCompleted(Child child, JsonNode condition) {
        if (!condition.has("topicId")) {
            return false;
        }
        Long topicId = condition.get("topicId").asLong();

        // Get all lessons in the topic
        int totalLessons = lessonRepository.findByTopicId(topicId).size();
        if (totalLessons == 0) {
            return false;
        }

        // Count completed lessons in the topic
        long completedInTopic = progressRepository.findByChildIdAndLessonTopicId(child.getId(), topicId)
                .stream()
                .filter(p -> Boolean.TRUE.equals(p.getCompleted()))
                .count();

        return completedInTopic >= totalLessons;
    }

    private boolean evaluateStreak(Child child, JsonNode condition) {
        int requiredDays = condition.has("days") ? condition.get("days").asInt() : 0;
        return child.getCurrentStreak() >= requiredDays;
    }

    private boolean evaluateStars(Child child, JsonNode condition) {
        int requiredStars = condition.has("count") ? condition.get("count").asInt() : 0;
        return child.getTotalStars() >= requiredStars;
    }

    private boolean evaluateTopicsStarted(Child child, JsonNode condition) {
        int requiredCount = condition.has("count") ? condition.get("count").asInt() : 0;
        long topicsStarted = progressRepository.countDistinctStartedTopicsByChildId(child.getId());
        return topicsStarted >= requiredCount;
    }

    private AchievementDto toAchievementDto(Achievement achievement, LocalDateTime unlockedAt) {
        return AchievementDto.builder()
                .id(achievement.getId())
                .name(achievement.getName())
                .description(achievement.getDescription())
                .badgeImageUrl(achievement.getBadgeImageUrl())
                .unlockedAt(unlockedAt)
                .earned(unlockedAt != null)
                .build();
    }
}
