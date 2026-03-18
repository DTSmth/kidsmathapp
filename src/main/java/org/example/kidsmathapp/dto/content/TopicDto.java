package org.example.kidsmathapp.dto.content;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.kidsmathapp.entity.enums.GradeLevel;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TopicDto {

    private Long id;
    private String name;
    private String description;
    private String iconName;
    private Integer orderIndex;
    private GradeLevel gradeLevel;
}
