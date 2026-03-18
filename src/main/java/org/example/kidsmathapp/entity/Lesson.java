package org.example.kidsmathapp.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Entity
@Table(name = "lessons")
public class Lesson extends BaseEntity {

    @Column(nullable = false)
    private String title;

    @Column
    private String description;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(name = "order_index")
    private Integer orderIndex;

    @Builder.Default
    @Column(name = "stars_reward", nullable = false)
    private Integer starsReward = 10;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "topic_id", nullable = false)
    private Topic topic;

    @Builder.Default
    @OneToMany(mappedBy = "lesson", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Question> questions = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "lesson", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Progress> progressList = new ArrayList<>();
}
