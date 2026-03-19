package org.example.kidsmathapp.dto.content;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.kidsmathapp.entity.enums.GameType;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GameDetailDto {
    private Long id;
    private String name;
    private String description;
    private GameType gameType;
    private Integer baseStarsReward;
    private Integer timeLimit;
    private List<QuestionDto> questions;
}
