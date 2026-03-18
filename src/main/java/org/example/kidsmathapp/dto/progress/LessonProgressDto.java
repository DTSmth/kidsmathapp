package org.example.kidsmathapp.dto.progress;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LessonProgressDto {

    private Long lessonId;
    private String lessonTitle;
    private Integer orderIndex;
    private boolean completed;
    private Integer score;
    private LocalDateTime completedAt;
}
