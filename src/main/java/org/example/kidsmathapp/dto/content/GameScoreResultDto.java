package org.example.kidsmathapp.dto.content;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.kidsmathapp.dto.inventory.InventoryItemDto;
import org.example.kidsmathapp.dto.progress.AchievementDto;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GameScoreResultDto {
    private Integer score;
    private Integer starsEarned;
    private Integer personalBestScore;
    @JsonProperty("isNewPersonalBest")
    private boolean isNewPersonalBest;
    private boolean streakUpdated;
    private List<AchievementDto> newAchievements;
    private boolean gamificationApplied;  // false when orchestrator failed
    private InventoryItemDto newItem;     // nullable — set when an item drops
}
