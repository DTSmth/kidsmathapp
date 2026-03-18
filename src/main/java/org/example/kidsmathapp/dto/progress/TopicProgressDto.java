package org.example.kidsmathapp.dto.progress;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TopicProgressDto {

    private Long topicId;
    private String topicName;
    private int lessonsCompleted;
    private int totalLessons;
    private double percentComplete;
    
    @Builder.Default
    private List<LessonProgressDto> lessons = new ArrayList<>();
}
