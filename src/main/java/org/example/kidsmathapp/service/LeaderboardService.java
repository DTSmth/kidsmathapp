package org.example.kidsmathapp.service;

import lombok.RequiredArgsConstructor;
import org.example.kidsmathapp.dto.leaderboard.*;
import org.example.kidsmathapp.entity.Child;
import org.example.kidsmathapp.entity.GameScore;
import org.example.kidsmathapp.entity.enums.GameMode;
import org.example.kidsmathapp.exception.ApiException;
import org.example.kidsmathapp.repository.ChildRepository;
import org.example.kidsmathapp.repository.GameScoreRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@RequiredArgsConstructor
public class LeaderboardService {

    private final ChildRepository childRepository;
    private final GameScoreRepository gameScoreRepository;

    @Transactional(readOnly = true)
    public FamilyLeaderboardDto getFamilyLeaderboard(Long childId) {
        Child child = childRepository.findById(childId)
                .orElseThrow(() -> ApiException.notFound("Child not found"));
        Long parentId = child.getParent().getId();

        List<Child> siblings = childRepository.findByParentIdOrderByTotalStarsDescCurrentStreakDesc(parentId);

        AtomicInteger rank = new AtomicInteger(1);
        List<LeaderboardEntryDto> starRankings = siblings.stream().map(c ->
                LeaderboardEntryDto.builder()
                        .rank(rank.getAndIncrement())
                        .childId(c.getId())
                        .childName(c.getName())
                        .avatarId(c.getAvatarId())
                        .value(c.getTotalStars())
                        .currentStreak(c.getCurrentStreak())
                        .isCurrentChild(c.getId().equals(childId))
                        .build()
        ).toList();

        // Streak rankings (re-sort by streak)
        List<Child> byStreak = new ArrayList<>(siblings);
        byStreak.sort((a, b) -> Integer.compare(b.getCurrentStreak(), a.getCurrentStreak()));
        AtomicInteger streakRank = new AtomicInteger(1);
        List<LeaderboardEntryDto> streakRankings = byStreak.stream().map(c ->
                LeaderboardEntryDto.builder()
                        .rank(streakRank.getAndIncrement())
                        .childId(c.getId())
                        .childName(c.getName())
                        .avatarId(c.getAvatarId())
                        .value(c.getCurrentStreak())
                        .currentStreak(c.getCurrentStreak())
                        .isCurrentChild(c.getId().equals(childId))
                        .build()
        ).toList();

        int myStarRank = starRankings.stream().filter(LeaderboardEntryDto::isCurrentChild).findFirst().map(LeaderboardEntryDto::getRank).orElse(1);
        int myStreakRank = streakRankings.stream().filter(LeaderboardEntryDto::isCurrentChild).findFirst().map(LeaderboardEntryDto::getRank).orElse(1);

        return FamilyLeaderboardDto.builder()
                .starRankings(starRankings)
                .streakRankings(streakRankings)
                .currentChildRankByStars(myStarRank)
                .currentChildRankByStreak(myStreakRank)
                .build();
    }

    @Transactional(readOnly = true)
    public GameLeaderboardDto getGameLeaderboard(Long gameId, String gameMode, Long childId) {
        Child child = childRepository.findById(childId)
                .orElseThrow(() -> ApiException.notFound("Child not found"));
        Long parentId = child.getParent().getId();

        GameMode gameModeEnum = GameMode.valueOf(gameMode.toUpperCase());
        List<GameScore> scores = gameScoreRepository.findFamilyLeaderboard(
                gameId, gameModeEnum, parentId, PageRequest.of(0, 10));

        AtomicInteger rank = new AtomicInteger(1);
        List<LeaderboardEntryDto> entries = scores.stream().map(gs ->
                LeaderboardEntryDto.builder()
                        .rank(rank.getAndIncrement())
                        .childId(gs.getChild().getId())
                        .childName(gs.getChild().getName())
                        .avatarId(gs.getChild().getAvatarId())
                        .value(gs.getScore())
                        .currentStreak(gs.getChild().getCurrentStreak())
                        .isCurrentChild(gs.getChild().getId().equals(childId))
                        .build()
        ).toList();

        return GameLeaderboardDto.builder()
                .gameId(gameId)
                .gameMode(gameMode)
                .entries(entries)
                .build();
    }
}
