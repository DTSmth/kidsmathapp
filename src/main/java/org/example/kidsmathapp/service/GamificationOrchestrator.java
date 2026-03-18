package org.example.kidsmathapp.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.kidsmathapp.dto.progress.AchievementDto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * GAMIFICATION PIPELINE
 * ProgressService ──► GamificationOrchestrator ──► PointsService
 *                                              ├──► StreakService
 *                                              └──► AchievementService
 *
 * All three are called within ProgressService's @Transactional boundary.
 * Failure in any step rolls back the entire lesson completion.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GamificationOrchestrator {

    private final PointsService pointsService;
    private final StreakService streakService;
    private final AchievementService achievementService;

    /**
     * Orchestrates all post-lesson gamification:
     * 1. Awards points and logs them
     * 2. Records daily activity (streak)
     * 3. Checks and awards any newly unlocked achievements
     *
     * @return OrchestratorResult containing streak update status and new achievements
     */
    @Transactional
    public OrchestratorResult orchestrate(Long childId, int starsEarned, String pointsReason) {
        // Award points and log
        pointsService.awardPoints(childId, starsEarned, pointsReason);

        // Update streak if first activity today
        boolean streakUpdated = streakService.recordActivity(childId);

        // Check and award any achievements
        List<AchievementDto> newAchievements = achievementService.checkAndAwardAchievements(childId);

        log.info("Gamification orchestrated for child {}: {} stars, streakUpdated={}, achievements={}",
                childId, starsEarned, streakUpdated, newAchievements.size());

        return new OrchestratorResult(streakUpdated, newAchievements);
    }

    public record OrchestratorResult(boolean streakUpdated, List<AchievementDto> newAchievements) {}
}
