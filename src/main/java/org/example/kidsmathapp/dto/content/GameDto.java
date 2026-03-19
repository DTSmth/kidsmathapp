package org.example.kidsmathapp.dto.content;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.kidsmathapp.entity.enums.GameType;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GameDto {
    private Long id;
    private String name;
    private String description;
    private GameType gameType;
    private String iconName;
    private Integer baseStarsReward;
    private Integer timeLimit;
    private Integer personalBestScore;   // null if never played
    private Integer personalBestStars;   // null if never played
}
