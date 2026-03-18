package org.example.kidsmathapp.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.example.kidsmathapp.entity.enums.GradeLevel;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Entity
@Table(name = "children")
public class Child extends BaseEntity {

    @Column(nullable = false)
    private String name;

    @Column(name = "avatar_id")
    private String avatarId;

    @Column(name = "birth_date")
    private LocalDate birthDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "grade_level")
    private GradeLevel gradeLevel;

    @Builder.Default
    @Column(name = "total_stars", nullable = false)
    private Integer totalStars = 0;

    @Builder.Default
    @Column(name = "current_streak", nullable = false)
    private Integer currentStreak = 0;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id", nullable = false)
    private User parent;

    @Builder.Default
    @OneToMany(mappedBy = "child", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Progress> progressList = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "child", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<ChildAchievement> achievements = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "child", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<PointsLog> pointsLogs = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "child", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Streak> streaks = new ArrayList<>();
}
