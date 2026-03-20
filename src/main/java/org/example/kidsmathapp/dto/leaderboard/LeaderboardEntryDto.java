package org.example.kidsmathapp.dto.leaderboard;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class LeaderboardEntryDto {
    private int rank;
    private Long childId;
    private String childName;
    private String avatarId;
    private int value; // stars or score
    private int currentStreak;
    @JsonProperty("isCurrentChild")
    private boolean isCurrentChild;
}
