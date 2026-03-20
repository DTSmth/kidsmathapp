package org.example.kidsmathapp.dto.leaderboard;

import lombok.*;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class LeaderboardEntryDto {
    private int rank;
    private Long childId;
    private String childName;
    private String avatarId;
    private int value; // stars or score
    private int currentStreak;
    private boolean isCurrentChild;
}
