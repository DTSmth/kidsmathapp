package org.example.kidsmathapp.dto.progress;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.kidsmathapp.dto.inventory.InventoryItemDto;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LessonCompletionResult {

    private int starsEarned;
    private int bonusStars;
    private int totalStars;

    @Builder.Default
    private List<AchievementDto> newAchievements = new ArrayList<>();

    private boolean streakUpdated;
    private int currentStreak;
    private InventoryItemDto newItem; // nullable — set when an item drops
}
