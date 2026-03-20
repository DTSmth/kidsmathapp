package org.example.kidsmathapp.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.kidsmathapp.dto.inventory.InventoryItemDto;
import org.example.kidsmathapp.dto.progress.AchievementDto;
import org.example.kidsmathapp.entity.enums.ItemDropSource;
import org.example.kidsmathapp.entity.enums.RankLevel;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * GAMIFICATION PIPELINE v2
 *
 * LessonService / GameService
 *   └─► GamificationOrchestrator.orchestrate()  [@Transactional]
 *         │
 *         ├─1─► PointsService.awardPoints()
 *         │
 *         ├─2─► StreakService.recordActivity() → StreakResult
 *         │       └─ if wouldHaveBroken:
 *         │            InventoryService.consumeShield()
 *         │            StreakService.preserveStreak()
 *         │
 *         ├─3─► AchievementService.checkAndAward()
 *         │       └─► UnlockConditionEvaluator.evaluate()
 *         │
 *         ├─4─► InventoryService.dispatchItemDrop() → Optional<item>
 *         │       (20% chance on game/lesson, 100% on milestone)
 *         │
 *         └─5─► RankLevel.fromStars() → detect rank change
 *
 * All steps share one transaction. Any exception rolls back everything.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GamificationOrchestrator {

    private final PointsService pointsService;
    private final StreakService streakService;
    private final AchievementService achievementService;
    private final InventoryService inventoryService;

    @Transactional
    public OrchestratorResult orchestrate(Long childId, int starsEarned, String pointsReason) {
        return orchestrate(childId, starsEarned, pointsReason, ItemDropSource.LESSON_COMPLETION);
    }

    @Transactional
    public OrchestratorResult orchestrate(Long childId, int starsEarned, String pointsReason, ItemDropSource dropSource) {
        // Capture rank before awarding points
        int previousStars = pointsService.getCurrentStars(childId);
        RankLevel previousRankLevel = RankLevel.fromStars(previousStars);

        // 1. Award points
        int newTotalStars = pointsService.awardPoints(childId, starsEarned, pointsReason);
        RankLevel currentRankLevel = RankLevel.fromStars(newTotalStars);

        // 2. Update streak
        StreakService.StreakResult streakResult = streakService.recordActivity(childId);
        boolean shieldConsumed = false;
        if (streakResult.wouldHaveBroken()) {
            shieldConsumed = inventoryService.consumeShield(childId);
            if (shieldConsumed) {
                streakService.preserveStreak(childId, streakResult.previousStreak());
                log.info("Streak shield absorbed a miss for child {}", childId);
            }
        }

        // 3. Check achievements
        List<AchievementDto> newAchievements = achievementService.checkAndAwardAchievements(childId);

        // 4. Dispatch item drop (random 20% chance on normal completion, higher on milestones)
        Optional<InventoryItemDto> itemDrop = Optional.empty();
        if (shouldDropItem(dropSource, streakResult, newAchievements)) {
            itemDrop = inventoryService.dispatchItemDrop(childId, dropSource);
        }
        List<InventoryItemDto> newItems = itemDrop.map(List::of).orElse(Collections.emptyList());

        log.info("Gamification orchestrated: child={} stars=+{} streak={} shieldConsumed={} achievements={} items={} rankChange={}→{}",
                childId, starsEarned, streakResult.updated(), shieldConsumed,
                newAchievements.size(), newItems.size(), previousRankLevel, currentRankLevel);

        return new OrchestratorResult(
                streakResult.updated(),
                shieldConsumed,
                newAchievements,
                newItems,
                previousRankLevel,
                currentRankLevel
        );
    }

    private boolean shouldDropItem(ItemDropSource source, StreakService.StreakResult streak, List<AchievementDto> achievements) {
        if (source == ItemDropSource.MILESTONE) return true;
        if (!achievements.isEmpty()) return true; // always drop on achievement unlock
        return Math.random() < 0.20; // 20% chance on normal completion
    }

    public record OrchestratorResult(
            boolean streakUpdated,
            boolean shieldConsumed,
            List<AchievementDto> newAchievements,
            List<InventoryItemDto> newItems,
            RankLevel previousRankLevel,
            RankLevel currentRankLevel
    ) {}
}
