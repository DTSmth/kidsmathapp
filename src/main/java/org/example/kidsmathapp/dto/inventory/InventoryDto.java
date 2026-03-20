package org.example.kidsmathapp.dto.inventory;

import lombok.*;
import java.util.List;
import java.util.Map;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class InventoryDto {
    private Long childId;
    private List<InventoryItemDto> items;
    private Map<String, InventoryItemDto> equipped; // slot name → item
    private int totalItems;
}
