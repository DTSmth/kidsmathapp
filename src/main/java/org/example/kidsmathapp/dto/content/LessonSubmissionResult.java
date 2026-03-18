package org.example.kidsmathapp.dto.content;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.kidsmathapp.dto.progress.AchievementDto;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LessonSubmissionResult {

    private Long lessonId;
    private int totalQuestions;
    private int correctAnswers;
    private int score;
    private int starsEarned;
    private int bonusStars;
    private int totalStars;
    private boolean passed;
    private String message;
    private List<AnswerResultDto> results;
    
    @Builder.Default
    private List<AchievementDto> newAchievements = new ArrayList<>();
    
    private boolean streakUpdated;
    private int currentStreak;
}
