package org.example.kidsmathapp.dto.progress;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import java.time.LocalDate;
import java.util.List;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class StreakCalendarDto {
    private int currentStreak;
    private int longestStreak;
    private List<StreakDayDto> days; // last 30 days

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class StreakDayDto {
        private LocalDate date;
        private boolean practiced;
        private int practiceCount;
        private boolean dailyBonusClaimed;
        @JsonProperty("isToday")
        private boolean isToday;
    }
}
