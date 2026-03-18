package org.example.kidsmathapp.dto.progress;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardDto {

    private Long childId;
    private String childName;
    private int totalStars;
    private int currentStreak;
    
    @Builder.Default
    private List<TopicProgressDto> topics = new ArrayList<>();
    
    @Builder.Default
    private List<AchievementDto> recentAchievements = new ArrayList<>();
    
    private boolean dailyChallengeComplete;
}
