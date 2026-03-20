package org.example.kidsmathapp.dto.leaderboard;

import lombok.*;
import java.util.List;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class FamilyLeaderboardDto {
    private List<LeaderboardEntryDto> starRankings;
    private List<LeaderboardEntryDto> streakRankings;
    private int currentChildRankByStars;
    private int currentChildRankByStreak;
}
