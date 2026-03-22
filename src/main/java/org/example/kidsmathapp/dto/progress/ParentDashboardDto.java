package org.example.kidsmathapp.dto.progress;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ParentDashboardDto {
    private Long childId;
    private String childName;
    private String gradeLevel;
    private int daysActiveThisWeek;       // 0-7
    private int totalMinutesThisWeek;
    private int currentStreak;
    private List<TopicAccuracyDto> topicAccuracies;
    private List<HeatmapDayDto> heatmap;  // last 7 days
    private List<TrajectoryPointDto> trajectory; // last 30 days, weekly avg score
    private boolean isPremium;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TopicAccuracyDto {
        private Long topicId;
        private String topicName;
        private String topicEmoji;
        private double accuracy;          // 0-100
        private int lessonsCompleted;
        private int totalLessons;
        private boolean needsPractice;    // accuracy < 70%
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HeatmapDayDto {
        private String date;              // YYYY-MM-DD
        private boolean practiced;
        private int minutesPracticed;
        private boolean isToday;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TrajectoryPointDto {
        private String weekLabel;         // e.g. "Mar 1"
        private double avgScore;          // 0-100
    }
}
