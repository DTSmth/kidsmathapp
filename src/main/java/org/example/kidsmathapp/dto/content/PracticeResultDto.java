package org.example.kidsmathapp.dto.content;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.kidsmathapp.dto.inventory.InventoryItemDto;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PracticeResultDto {
    private int score;
    private int correctCount;
    private int totalCount;
    private int starsEarned;
    private InventoryItemDto newItem;
}
