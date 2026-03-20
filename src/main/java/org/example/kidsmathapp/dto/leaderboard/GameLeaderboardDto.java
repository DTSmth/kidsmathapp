package org.example.kidsmathapp.dto.leaderboard;

import lombok.*;
import java.util.List;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class GameLeaderboardDto {
    private Long gameId;
    private String gameMode; // NORMAL or ENDLESS
    private List<LeaderboardEntryDto> entries;
}
