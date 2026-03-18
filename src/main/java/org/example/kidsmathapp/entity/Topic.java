package org.example.kidsmathapp.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.example.kidsmathapp.entity.enums.GradeLevel;

import java.util.ArrayList;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Entity
@Table(name = "topics")
public class Topic extends BaseEntity {

    @Column(nullable = false)
    private String name;

    @Column
    private String description;

    @Column(name = "icon_name")
    private String iconName;

    @Column(name = "order_index")
    private Integer orderIndex;

    @Enumerated(EnumType.STRING)
    @Column(name = "grade_level")
    private GradeLevel gradeLevel;

    @Builder.Default
    @OneToMany(mappedBy = "topic", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Lesson> lessons = new ArrayList<>();

    @Builder.Default
    @ManyToMany(mappedBy = "topics", fetch = FetchType.LAZY)
    private List<Game> games = new ArrayList<>();
}
