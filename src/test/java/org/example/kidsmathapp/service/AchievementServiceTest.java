package org.example.kidsmathapp.service;

import org.example.kidsmathapp.dto.progress.AchievementDto;
import org.example.kidsmathapp.entity.Achievement;
import org.example.kidsmathapp.entity.Child;
import org.example.kidsmathapp.entity.ChildAchievement;
import org.example.kidsmathapp.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AchievementServiceTest {

    @Mock private AchievementRepository achievementRepository;
    @Mock private ChildAchievementRepository childAchievementRepository;
    @Mock private ChildRepository childRepository;
    @Mock private ProgressRepository progressRepository;
    @Mock private LessonRepository lessonRepository;
    @Mock private UnlockConditionEvaluator unlockConditionEvaluator;

    @InjectMocks private AchievementService achievementService;

    private Child child;

    @BeforeEach
    void setUp() {
        child = Child.builder()
                .totalStars(5)
                .currentStreak(1)
                .build();
    }

    @Test
    void checkAndAward_unlocks_achievement_when_condition_met() {
        Achievement achievement = Achievement.builder()
                .name("Star Collector")
                .unlockCondition("{\"type\":\"stars\",\"count\":5}")
                .starsBonus(0)
                .build();

        when(childRepository.findById(anyLong())).thenReturn(Optional.of(child));
        when(achievementRepository.findAll()).thenReturn(List.of(achievement));
        when(childAchievementRepository.findByChildId(anyLong())).thenReturn(List.of());
        when(childAchievementRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(unlockConditionEvaluator.evaluate(any(), any())).thenReturn(true);

        List<AchievementDto> result = achievementService.checkAndAwardAchievements(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Star Collector");
    }

    @Test
    void checkAndAward_does_not_duplicate_already_earned_achievement() {
        Achievement achievement = Achievement.builder()
                .name("Star Collector")
                .unlockCondition("{\"type\":\"stars\",\"count\":5}")
                .starsBonus(0)
                .build();

        ChildAchievement alreadyEarned = ChildAchievement.builder()
                .achievement(achievement)
                .child(child)
                .build();

        when(childRepository.findById(anyLong())).thenReturn(Optional.of(child));
        when(achievementRepository.findAll()).thenReturn(List.of(achievement));
        when(childAchievementRepository.findByChildId(anyLong())).thenReturn(List.of(alreadyEarned));

        List<AchievementDto> result = achievementService.checkAndAwardAchievements(1L);

        assertThat(result).isEmpty();
        verify(childAchievementRepository, never()).save(any());
    }

    @Test
    void checkAndAward_skips_achievement_when_condition_not_met() {
        Achievement achievement = Achievement.builder()
                .name("Legend")
                .unlockCondition("{\"type\":\"stars\",\"count\":1000}")
                .starsBonus(0)
                .build();

        when(childRepository.findById(anyLong())).thenReturn(Optional.of(child));
        when(achievementRepository.findAll()).thenReturn(List.of(achievement));
        when(childAchievementRepository.findByChildId(anyLong())).thenReturn(List.of());
        when(unlockConditionEvaluator.evaluate(any(), any())).thenReturn(false);

        List<AchievementDto> result = achievementService.checkAndAwardAchievements(1L);

        assertThat(result).isEmpty();
    }
}
