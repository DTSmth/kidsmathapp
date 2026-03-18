package org.example.kidsmathapp.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Entity
@Table(name = "child_achievements", uniqueConstraints = {
        @UniqueConstraint(name = "uq_child_achievement", columnNames = {"child_id", "achievement_id"})
})
public class ChildAchievement extends BaseEntity {

    @Column(name = "unlocked_at", nullable = false)
    private LocalDateTime unlockedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "child_id", nullable = false)
    private Child child;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "achievement_id", nullable = false)
    private Achievement achievement;
}
