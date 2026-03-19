package org.example.kidsmathapp.dto.content;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TopicWithProgressDto {

    private Long id;
    private String name;
    private String description;
    private String iconName;
    private int lessonsCompleted;
    private int totalLessons;
    private int progressPercent;
    @JsonProperty("isUnlocked")
    private boolean isUnlocked;
}
