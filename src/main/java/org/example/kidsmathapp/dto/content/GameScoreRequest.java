package org.example.kidsmathapp.dto.content;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GameScoreRequest {

    @NotNull
    private Long childId;

    @NotNull
    @Min(0) @Max(100)
    private Integer score;

    @Min(0)
    private Integer timeSpent;

    @Min(0)
    private Integer comboBonus;

    // JSON array [{questionId, answeredAt}] for ghost race replay
    private String answersLog;
}
