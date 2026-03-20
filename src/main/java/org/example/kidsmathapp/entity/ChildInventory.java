package org.example.kidsmathapp.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.example.kidsmathapp.entity.enums.ItemType;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Entity
@Table(name = "child_inventory", uniqueConstraints = {
        @UniqueConstraint(name = "uq_child_item", columnNames = {"child_id", "item_id"})
})
public class ChildInventory extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "child_id", nullable = false)
    private Child child;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", nullable = false)
    private AvatarItem item;

    @Enumerated(EnumType.STRING)
    @Column(name = "equipped_slot")
    private ItemType equippedSlot;

    @Column(name = "earned_at", nullable = false)
    private LocalDateTime earnedAt;
}
