package org.example.kidsmathapp.dto.content;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class LessonWithProgressDto extends LessonDto {

    private boolean completed;
    private Integer score;
    private LocalDateTime completedAt;
}
