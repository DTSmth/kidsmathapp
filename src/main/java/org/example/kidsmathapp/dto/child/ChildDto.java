package org.example.kidsmathapp.dto.child;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.kidsmathapp.entity.enums.GradeLevel;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChildDto {

    private Long id;
    private String name;
    private String avatarId;
    private LocalDate birthDate;
    private GradeLevel gradeLevel;
    private Integer totalStars;
    private Integer currentStreak;
    private LocalDateTime createdAt;
}
