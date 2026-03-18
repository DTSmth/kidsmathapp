package org.example.kidsmathapp.service;

import org.example.kidsmathapp.dto.progress.AchievementDto;
import org.example.kidsmathapp.dto.progress.LessonCompletionResult;
import org.example.kidsmathapp.entity.Child;
import org.example.kidsmathapp.entity.Lesson;
import org.example.kidsmathapp.entity.Progress;
import org.example.kidsmathapp.exception.ApiException;
import org.example.kidsmathapp.repository.ChildRepository;
import org.example.kidsmathapp.repository.LessonRepository;
import org.example.kidsmathapp.repository.ProgressRepository;
import org.example.kidsmathapp.repository.TopicRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.*;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ProgressServiceTest {

    @Mock private ProgressRepository progressRepository;
    @Mock private ChildRepository childRepository;
    @Mock private LessonRepository lessonRepository;
    @Mock private TopicRepository topicRepository;
    @Mock private GamificationOrchestrator gamificationOrchestrator;
    @Mock private AchievementService achievementService;
    @Mock private StreakService streakService;

    @InjectMocks private ProgressService progressService;

    private Child child;
    private Lesson lesson;

    @BeforeEach
    void setUp() {
        child = Child.builder()
                .totalStars(0)
                .currentStreak(0)
                .build();
        lesson = Lesson.builder()
                .starsReward(10)
                .title("Test Lesson")
                .build();
    }

    @Test
    void recordLessonCompletion_awards_base_stars() {
        when(childRepository.findById(anyLong())).thenReturn(Optional.of(child));
        when(lessonRepository.findById(anyLong())).thenReturn(Optional.of(lesson));
        when(progressRepository.findByChildIdAndLessonId(anyLong(), anyLong())).thenReturn(Optional.empty());
        when(progressRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(childRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(gamificationOrchestrator.orchestrate(anyLong(), anyInt(), anyString()))
                .thenReturn(new GamificationOrchestrator.OrchestratorResult(false, List.of()));

        LessonCompletionResult result = progressService.recordLessonCompletion(1L, 1L, 80);

        assertThat(result.getStarsEarned()).isEqualTo(10);
        assertThat(result.getBonusStars()).isEqualTo(0);
    }

    @Test
    void recordLessonCompletion_awards_bonus_stars_at_90_percent() {
        when(childRepository.findById(anyLong())).thenReturn(Optional.of(child));
        when(lessonRepository.findById(anyLong())).thenReturn(Optional.of(lesson));
        when(progressRepository.findByChildIdAndLessonId(anyLong(), anyLong())).thenReturn(Optional.empty());
        when(progressRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(childRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(gamificationOrchestrator.orchestrate(anyLong(), anyInt(), anyString()))
                .thenReturn(new GamificationOrchestrator.OrchestratorResult(false, List.of()));

        LessonCompletionResult result = progressService.recordLessonCompletion(1L, 1L, 90);

        assertThat(result.getBonusStars()).isEqualTo(5); // 10 * 0.5 = 5
        assertThat(result.getStarsEarned()).isEqualTo(10);
    }

    @Test
    void recordLessonCompletion_no_bonus_below_90_percent() {
        when(childRepository.findById(anyLong())).thenReturn(Optional.of(child));
        when(lessonRepository.findById(anyLong())).thenReturn(Optional.of(lesson));
        when(progressRepository.findByChildIdAndLessonId(anyLong(), anyLong())).thenReturn(Optional.empty());
        when(progressRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(childRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(gamificationOrchestrator.orchestrate(anyLong(), anyInt(), anyString()))
                .thenReturn(new GamificationOrchestrator.OrchestratorResult(false, List.of()));

        LessonCompletionResult result = progressService.recordLessonCompletion(1L, 1L, 89);

        assertThat(result.getBonusStars()).isEqualTo(0);
    }

    @Test
    void recordLessonCompletion_throws_when_child_not_found() {
        when(childRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> progressService.recordLessonCompletion(99L, 1L, 80))
                .isInstanceOf(ApiException.class);
    }

    @Test
    void recordLessonCompletion_throws_when_lesson_not_found() {
        when(childRepository.findById(anyLong())).thenReturn(Optional.of(child));
        when(lessonRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> progressService.recordLessonCompletion(1L, 99L, 80))
                .isInstanceOf(ApiException.class);
    }

    @Test
    void recordLessonCompletion_does_not_downgrade_existing_score() {
        Progress existingProgress = Progress.builder()
                .score(95)
                .completed(true)
                .child(child)
                .lesson(lesson)
                .build();

        when(childRepository.findById(anyLong())).thenReturn(Optional.of(child));
        when(lessonRepository.findById(anyLong())).thenReturn(Optional.of(lesson));
        when(progressRepository.findByChildIdAndLessonId(anyLong(), anyLong()))
                .thenReturn(Optional.of(existingProgress));
        when(progressRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(gamificationOrchestrator.orchestrate(anyLong(), anyInt(), anyString()))
                .thenReturn(new GamificationOrchestrator.OrchestratorResult(false, List.of()));

        progressService.recordLessonCompletion(1L, 1L, 70);

        // Score should remain 95, not downgrade to 70
        assertThat(existingProgress.getScore()).isEqualTo(95);
    }
}
