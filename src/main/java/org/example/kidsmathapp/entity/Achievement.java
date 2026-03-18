package org.example.kidsmathapp.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Entity
@Table(name = "achievements")
public class Achievement extends BaseEntity {

    @Column(nullable = false)
    private String name;

    @Column
    private String description;

    @Column(name = "badge_image_url")
    private String badgeImageUrl;

    @Column(name = "unlock_condition", columnDefinition = "TEXT")
    private String unlockCondition;

    @Builder.Default
    @Column(name = "stars_bonus", nullable = false)
    private Integer starsBonus = 0;
}
