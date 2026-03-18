package org.example.kidsmathapp.dto.content;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LessonDetailDto {

    private Long id;
    private String title;
    private String description;
    private Integer orderIndex;
    private Integer starsReward;
    private Long topicId;
    private String topicName;
    private String content;
    private List<QuestionDto> questions;
}
