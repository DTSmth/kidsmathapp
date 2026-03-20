package org.example.kidsmathapp.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.example.kidsmathapp.entity.enums.ItemTier;
import org.example.kidsmathapp.entity.enums.ItemType;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Entity
@Table(name = "avatar_item")
public class AvatarItem extends BaseEntity {

    @Column(nullable = false)
    private String name;

    @Column
    private String emoji;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ItemTier tier;

    @Enumerated(EnumType.STRING)
    @Column(name = "item_type", nullable = false)
    private ItemType itemType;

    @Column(name = "unlock_condition", columnDefinition = "TEXT")
    private String unlockCondition;
}
