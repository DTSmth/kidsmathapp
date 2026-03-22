package org.example.kidsmathapp.service;

import org.example.kidsmathapp.dto.progress.ParentDashboardDto;
import org.example.kidsmathapp.entity.Child;
import org.example.kidsmathapp.entity.Lesson;
import org.example.kidsmathapp.entity.Progress;
import org.example.kidsmathapp.entity.Subscription;
import org.example.kidsmathapp.entity.Topic;
import org.example.kidsmathapp.entity.enums.GradeLevel;
import org.example.kidsmathapp.exception.ApiException;
import org.example.kidsmathapp.repository.ChildRepository;
import org.example.kidsmathapp.repository.LessonRepository;
import org.example.kidsmathapp.repository.ProgressRepository;
import org.example.kidsmathapp.repository.SubscriptionRepository;
import org.example.kidsmathapp.repository.TopicRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ParentDashboardServiceTest {

    @Mock private ChildRepository childRepository;
    @Mock private ProgressRepository progressRepository;
    @Mock private TopicRepository topicRepository;
    @Mock private LessonRepository lessonRepository;
    @Mock private SubscriptionRepository subscriptionRepository;

    @InjectMocks private ParentDashboardService service;

    private Child child;

    @BeforeEach
    void setUp() {
        child = Child.builder()
                .name("Alice")
                .gradeLevel(GradeLevel.KINDERGARTEN)
                .totalStars(50)
                .currentStreak(3)
                .build();
        // Manually set ID via reflection workaround — use mock child with known ID
        when(childRepository.findByIdAndParentId(anyLong(), anyLong())).thenReturn(Optional.of(child));
        when(subscriptionRepository.findByUserId(anyLong())).thenReturn(Optional.empty());
        when(topicRepository.findByGradeLevelOrderByOrderIndexAsc(any())).thenReturn(Collections.emptyList());
        when(progressRepository.findByChildId(anyLong())).thenReturn(Collections.emptyList());
    }

    @Test
    void days_active_this_week_counts_distinct_days() {
        LocalDateTime now = LocalDateTime.now();

        // Create progress entries on 3 different days this week
        Progress p1 = makeCompletedProgress(now.minusDays(1));
        Progress p2 = makeCompletedProgress(now.minusDays(1)); // same day as p1
        Progress p3 = makeCompletedProgress(now.minusDays(2));
        Progress p4 = makeCompletedProgress(now.minusDays(5));

        when(progressRepository.findByChildId(anyLong())).thenReturn(List.of(p1, p2, p3, p4));

        ParentDashboardDto dashboard = service.getDashboard(1L, 1L);

        // Should count 3 distinct days, not 4 progress records
        assertThat(dashboard.getDaysActiveThisWeek()).isEqualTo(3);
    }

    @Test
    void needs_practice_flag_set_when_accuracy_below_70() {
        // Create a topic with a lesson
        Topic topic = Topic.builder()
                .name("Addition")
                .iconName("addition")
                .gradeLevel(GradeLevel.KINDERGARTEN)
                .build();

        Lesson lesson = Lesson.builder()
                .topic(topic)
                .title("Adding to 5")
                .build();

        Progress lowScoreProgress = Progress.builder()
                .lesson(lesson)
                .completed(true)
                .score(50) // below 70%
                .completedAt(LocalDateTime.now().minusDays(1))
                .build();

        when(topicRepository.findByGradeLevelOrderByOrderIndexAsc(any())).thenReturn(List.of(topic));
        when(lessonRepository.findByTopicIdOrderByOrderIndexAsc(any())).thenReturn(List.of(lesson));
        when(progressRepository.findByChildId(anyLong())).thenReturn(List.of(lowScoreProgress));

        ParentDashboardDto dashboard = service.getDashboard(1L, 1L);

        assertThat(dashboard.getTopicAccuracies()).hasSize(1);
        assertThat(dashboard.getTopicAccuracies().get(0).isNeedsPractice()).isTrue();
        assertThat(dashboard.getTopicAccuracies().get(0).getAccuracy()).isEqualTo(50.0);
    }

    @Test
    void needs_practice_flag_not_set_when_accuracy_above_70() {
        Topic topic = Topic.builder()
                .name("Counting")
                .iconName("counting")
                .gradeLevel(GradeLevel.KINDERGARTEN)
                .build();

        Lesson lesson = Lesson.builder()
                .topic(topic)
                .title("Count to 5")
                .build();

        Progress highScoreProgress = Progress.builder()
                .lesson(lesson)
                .completed(true)
                .score(90)
                .completedAt(LocalDateTime.now().minusDays(1))
                .build();

        when(topicRepository.findByGradeLevelOrderByOrderIndexAsc(any())).thenReturn(List.of(topic));
        when(lessonRepository.findByTopicIdOrderByOrderIndexAsc(any())).thenReturn(List.of(lesson));
        when(progressRepository.findByChildId(anyLong())).thenReturn(List.of(highScoreProgress));

        ParentDashboardDto dashboard = service.getDashboard(1L, 1L);

        assertThat(dashboard.getTopicAccuracies().get(0).isNeedsPractice()).isFalse();
    }

    @Test
    void empty_progress_returns_safe_defaults_no_npe() {
        when(progressRepository.findByChildId(anyLong())).thenReturn(Collections.emptyList());

        // Should not throw NPE
        ParentDashboardDto dashboard = service.getDashboard(1L, 1L);

        assertThat(dashboard).isNotNull();
        assertThat(dashboard.getDaysActiveThisWeek()).isEqualTo(0);
        assertThat(dashboard.getTotalMinutesThisWeek()).isEqualTo(0);
        assertThat(dashboard.getHeatmap()).hasSize(7);
        assertThat(dashboard.getTrajectory()).hasSize(4);
    }

    @Test
    void throws_forbidden_when_child_does_not_belong_to_parent() {
        when(childRepository.findByIdAndParentId(anyLong(), anyLong())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getDashboard(99L, 1L))
                .isInstanceOf(ApiException.class);
    }

    private Progress makeCompletedProgress(LocalDateTime completedAt) {
        return Progress.builder()
                .completed(true)
                .score(80)
                .completedAt(completedAt)
                .build();
    }
}
