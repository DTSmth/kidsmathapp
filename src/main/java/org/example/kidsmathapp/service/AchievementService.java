package org.example.kidsmathapp.service;

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

    private final AchievementRepository achievementRepository;
    private final ChildAchievementRepository childAchievementRepository;
    private final ChildRepository childRepository;
    private final UnlockConditionEvaluator unlockConditionEvaluator;

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
            if (unlockConditionEvaluator.evaluate(child, achievement.getUnlockCondition())) {
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
