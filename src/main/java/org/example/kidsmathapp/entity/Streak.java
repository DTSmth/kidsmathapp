package org.example.kidsmathapp.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Entity
@Table(name = "streaks", uniqueConstraints = {
        @UniqueConstraint(name = "uq_child_streak_date", columnNames = {"child_id", "date"})
})
public class Streak extends BaseEntity {

    @Column(nullable = false)
    private LocalDate date;

    @Column(name = "practice_count", nullable = false)
    private Integer practiceCount;

    @Builder.Default
    @Column(name = "daily_bonus_claimed", nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean dailyBonusClaimed = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "child_id", nullable = false)
    private Child child;
}
