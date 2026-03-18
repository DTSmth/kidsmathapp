package org.example.kidsmathapp.dto.content;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class LessonDto {

    private Long id;
    private String title;
    private String description;
    private Integer orderIndex;
    private Integer starsReward;
    private Long topicId;
    private String topicName;
}
