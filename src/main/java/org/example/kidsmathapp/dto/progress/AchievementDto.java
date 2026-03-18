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
public class AchievementDto {

    private Long id;
    private String name;
    private String description;
    private String badgeImageUrl;
    private LocalDateTime unlockedAt;
    private boolean earned;
}
