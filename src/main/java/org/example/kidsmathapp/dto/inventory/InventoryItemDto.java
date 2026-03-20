package org.example.kidsmathapp.dto.inventory;

import lombok.*;
import org.example.kidsmathapp.entity.enums.ItemTier;
import org.example.kidsmathapp.entity.enums.ItemType;
import java.time.LocalDateTime;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class InventoryItemDto {
    private Long inventoryId;
    private Long itemId;
    private String name;
    private String emoji;
    private ItemTier tier;
    private ItemType itemType;
    private boolean equipped;
    private ItemType equippedSlot;
    private LocalDateTime earnedAt;
}
