package org.example.kidsmathapp.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.kidsmathapp.dto.progress.ParentDashboardDto;
import org.example.kidsmathapp.entity.Child;
import org.example.kidsmathapp.entity.Lesson;
import org.example.kidsmathapp.entity.Progress;
import org.example.kidsmathapp.entity.Subscription;
import org.example.kidsmathapp.entity.Topic;
import org.example.kidsmathapp.exception.ApiException;
import org.example.kidsmathapp.repository.ChildRepository;
import org.example.kidsmathapp.repository.LessonRepository;
import org.example.kidsmathapp.repository.ProgressRepository;
import org.example.kidsmathapp.repository.SubscriptionRepository;
import org.example.kidsmathapp.repository.TopicRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ParentDashboardService {

    private final ChildRepository childRepository;
    private final ProgressRepository progressRepository;
    private final TopicRepository topicRepository;
    private final LessonRepository lessonRepository;
    private final SubscriptionRepository subscriptionRepository;

    @Transactional(readOnly = true)
    public ParentDashboardDto getDashboard(Long parentId, Long childId) {
        // Verify ownership
        Child child = childRepository.findByIdAndParentId(childId, parentId)
                .orElseThrow(() -> ApiException.forbidden("Access denied to this child"));

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime weekAgo = now.minusDays(7);
        LocalDateTime monthAgo = now.minusDays(30);

        List<Progress> allProgress = progressRepository.findByChildId(childId);
        List<Progress> weekProgress = allProgress.stream()
                .filter(p -> p.getCompletedAt() != null && p.getCompletedAt().isAfter(weekAgo))
                .collect(Collectors.toList());

        // 1. Days active this week
        int daysActiveThisWeek = (int) weekProgress.stream()
                .map(p -> p.getCompletedAt().toLocalDate())
                .distinct().count();

        // 2. Total minutes this week
        int totalMinutesThisWeek = weekProgress.stream()
                .mapToInt(p -> p.getTimeSpentSeconds() != null ? p.getTimeSpentSeconds() / 60 : 0)
                .sum();

        // 3. Topic accuracies
        List<Topic> topics = topicRepository.findByGradeLevelOrderByOrderIndexAsc(child.getGradeLevel());
        List<ParentDashboardDto.TopicAccuracyDto> topicAccuracies = new ArrayList<>();

        for (Topic topic : topics) {
            List<Lesson> lessons = lessonRepository.findByTopicIdOrderByOrderIndexAsc(topic.getId());
            List<Progress> topicProgress = allProgress.stream()
                    .filter(p -> p.getLesson() != null && p.getLesson().getTopic() != null
                            && p.getLesson().getTopic().getId().equals(topic.getId()))
                    .collect(Collectors.toList());

            long completedCount = topicProgress.stream()
                    .filter(p -> Boolean.TRUE.equals(p.getCompleted()))
                    .count();
            double avgScore = topicProgress.stream()
                    .filter(p -> Boolean.TRUE.equals(p.getCompleted()) && p.getScore() != null)
                    .mapToInt(Progress::getScore)
                    .average()
                    .orElse(0.0);

            topicAccuracies.add(ParentDashboardDto.TopicAccuracyDto.builder()
                    .topicId(topic.getId())
                    .topicName(topic.getName())
                    .topicEmoji(getTopicEmoji(topic.getIconName()))
                    .accuracy(avgScore)
                    .lessonsCompleted((int) completedCount)
                    .totalLessons(lessons.size())
                    .needsPractice(avgScore < 70 && completedCount > 0)
                    .build());
        }

        // 4. 7-day heatmap
        DateTimeFormatter fmt = DateTimeFormatter.ISO_LOCAL_DATE;
        LocalDate today = LocalDate.now();
        List<ParentDashboardDto.HeatmapDayDto> heatmap = new ArrayList<>();
        Map<LocalDate, List<Progress>> progressByDay = weekProgress.stream()
                .filter(p -> p.getCompletedAt() != null)
                .collect(Collectors.groupingBy(p -> p.getCompletedAt().toLocalDate()));

        for (int i = 6; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            List<Progress> dayProgress = progressByDay.getOrDefault(date, Collections.emptyList());
            int minutes = dayProgress.stream()
                    .mapToInt(p -> p.getTimeSpentSeconds() != null ? p.getTimeSpentSeconds() / 60 : 0)
                    .sum();
            heatmap.add(ParentDashboardDto.HeatmapDayDto.builder()
                    .date(date.format(fmt))
                    .practiced(!dayProgress.isEmpty())
                    .minutesPracticed(minutes)
                    .isToday(date.equals(today))
                    .build());
        }

        // 5. 30-day trajectory (4 weekly data points)
        List<Progress> monthProgress = allProgress.stream()
                .filter(p -> p.getCompletedAt() != null && p.getCompletedAt().isAfter(monthAgo)
                        && p.getScore() != null)
                .collect(Collectors.toList());

        List<ParentDashboardDto.TrajectoryPointDto> trajectory = new ArrayList<>();
        DateTimeFormatter weekFmt = DateTimeFormatter.ofPattern("MMM d");
        for (int weekIdx = 3; weekIdx >= 0; weekIdx--) {
            LocalDateTime weekStart = now.minusDays((weekIdx + 1) * 7L);
            LocalDateTime weekEnd = now.minusDays(weekIdx * 7L);
            OptionalDouble avgScore = monthProgress.stream()
                    .filter(p -> p.getCompletedAt().isAfter(weekStart) && p.getCompletedAt().isBefore(weekEnd))
                    .mapToInt(Progress::getScore)
                    .average();
            trajectory.add(ParentDashboardDto.TrajectoryPointDto.builder()
                    .weekLabel(weekStart.toLocalDate().format(weekFmt))
                    .avgScore(avgScore.orElse(0.0))
                    .build());
        }

        // 6. Premium check
        boolean isPremium = subscriptionRepository.findByUserId(parentId)
                .map(Subscription::isPremium)
                .orElse(false);

        return ParentDashboardDto.builder()
                .childId(childId)
                .childName(child.getName())
                .gradeLevel(child.getGradeLevel() != null ? child.getGradeLevel().name() : "KINDERGARTEN")
                .daysActiveThisWeek(daysActiveThisWeek)
                .totalMinutesThisWeek(totalMinutesThisWeek)
                .currentStreak(child.getCurrentStreak())
                .topicAccuracies(topicAccuracies)
                .heatmap(heatmap)
                .trajectory(trajectory)
                .isPremium(isPremium)
                .build();
    }

    private String getTopicEmoji(String iconName) {
        if (iconName == null) return "📚";
        return switch (iconName) {
            case "counting" -> "🔢";
            case "numbers" -> "🔢";
            case "addition" -> "➕";
            case "subtraction" -> "➖";
            case "shapes" -> "🔷";
            case "compare" -> "⚖️";
            case "place_value" -> "🏗️";
            case "fractions" -> "🍕";
            case "time" -> "⏰";
            case "measurement" -> "📏";
            case "multiplication" -> "✖️";
            case "division" -> "➗";
            case "money" -> "💰";
            case "geometry" -> "📐";
            case "problems" -> "🧩";
            default -> "📚";
        };
    }
}
