package org.example.kidsmathapp.dto.inventory;

import lombok.*;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class DailyBonusResponse {
    private boolean itemGranted;
    private InventoryItemDto item; // null if not granted
    private boolean alreadyClaimed;
}
