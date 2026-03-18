package org.example.kidsmathapp.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.kidsmathapp.dto.progress.AchievementDto;
import org.example.kidsmathapp.entity.Achievement;
import org.example.kidsmathapp.entity.Child;
import org.example.kidsmathapp.entity.ChildAchievement;
import org.example.kidsmathapp.exception.ApiException;
import org.example.kidsmathapp.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
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
    @Spy  private ObjectMapper objectMapper;

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
    void checkAndAward_unlocks_stars_achievement_when_threshold_met() {
        Achievement starsAchievement = Achievement.builder()
                .name("Star Collector")
                .unlockCondition("{\"type\":\"stars\",\"count\":5}")
                .starsBonus(0)
                .build();

        when(childRepository.findById(anyLong())).thenReturn(Optional.of(child));
        when(achievementRepository.findAll()).thenReturn(List.of(starsAchievement));
        when(childAchievementRepository.findByChildId(anyLong())).thenReturn(List.of());
        when(childAchievementRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        List<AchievementDto> result = achievementService.checkAndAwardAchievements(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Star Collector");
    }

    @Test
    void checkAndAward_does_not_duplicate_already_earned_achievement() {
        Achievement starsAchievement = Achievement.builder()
                .name("Star Collector")
                .unlockCondition("{\"type\":\"stars\",\"count\":5}")
                .starsBonus(0)
                .build();

        ChildAchievement alreadyEarned = ChildAchievement.builder()
                .achievement(starsAchievement)
                .child(child)
                .build();

        when(childRepository.findById(anyLong())).thenReturn(Optional.of(child));
        when(achievementRepository.findAll()).thenReturn(List.of(starsAchievement));
        when(childAchievementRepository.findByChildId(anyLong())).thenReturn(List.of(alreadyEarned));

        List<AchievementDto> result = achievementService.checkAndAwardAchievements(1L);

        assertThat(result).isEmpty();
        verify(childAchievementRepository, never()).save(any());
    }

    @Test
    void evaluateCondition_returns_false_for_unknown_type() {
        boolean result = achievementService.evaluateCondition(child, "{\"type\":\"unknown\",\"count\":1}");
        assertThat(result).isFalse();
    }

    @Test
    void evaluateCondition_returns_false_for_malformed_json() {
        boolean result = achievementService.evaluateCondition(child, "not-json");
        assertThat(result).isFalse();
    }

    @Test
    void evaluateCondition_returns_false_for_null() {
        boolean result = achievementService.evaluateCondition(child, null);
        assertThat(result).isFalse();
    }

    @Test
    void evaluateCondition_streak_threshold_met() {
        child = Child.builder().currentStreak(7).totalStars(0).build();
        boolean result = achievementService.evaluateCondition(child, "{\"type\":\"streak\",\"days\":7}");
        assertThat(result).isTrue();
    }

    @Test
    void evaluateCondition_streak_threshold_not_met() {
        child = Child.builder().currentStreak(3).totalStars(0).build();
        boolean result = achievementService.evaluateCondition(child, "{\"type\":\"streak\",\"days\":7}");
        assertThat(result).isFalse();
    }
}
