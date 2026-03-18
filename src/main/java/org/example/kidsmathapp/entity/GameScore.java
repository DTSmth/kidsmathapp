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
@Table(name = "game_scores")
public class GameScore extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "child_id", nullable = false)
    private Child child;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_id", nullable = false)
    private Game game;

    @Column(nullable = false)
    private Integer score;

    @Column(name = "stars_earned", nullable = false)
    private Integer starsEarned;

    @Column(name = "time_spent")
    private Integer timeSpent;

    @Column(name = "played_at", nullable = false)
    private LocalDateTime playedAt;

    @PrePersist
    @Override
    protected void onCreate() {
        super.onCreate();
        if (playedAt == null) {
            playedAt = LocalDateTime.now();
        }
    }
}
