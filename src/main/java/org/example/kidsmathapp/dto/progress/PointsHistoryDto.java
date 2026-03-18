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
public class PointsHistoryDto {

    private Long id;
    private int points;
    private String reason;
    private LocalDateTime createdAt;
}
