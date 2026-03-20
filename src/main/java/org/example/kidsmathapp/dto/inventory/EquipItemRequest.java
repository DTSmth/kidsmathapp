package org.example.kidsmathapp.dto.inventory;

import lombok.*;
import org.example.kidsmathapp.entity.enums.ItemType;

@Data @NoArgsConstructor @AllArgsConstructor
public class EquipItemRequest {
    private Long itemId;
    private ItemType slot; // null = unequip
}
