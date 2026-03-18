package org.example.kidsmathapp.service;

import lombok.RequiredArgsConstructor;
import org.example.kidsmathapp.dto.content.AnswerResultDto;
import org.example.kidsmathapp.dto.content.AnswerSubmissionDto;
import org.example.kidsmathapp.dto.content.LessonSubmissionResult;
import org.example.kidsmathapp.dto.progress.LessonCompletionResult;
import org.example.kidsmathapp.entity.Lesson;
import org.example.kidsmathapp.entity.Question;
import org.example.kidsmathapp.exception.ApiException;
import org.example.kidsmathapp.repository.LessonRepository;
import org.example.kidsmathapp.repository.QuestionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class QuestionService {

    private final QuestionRepository questionRepository;
    private final LessonRepository lessonRepository;
    private final ProgressService progressService;

    private static final int PASSING_SCORE = 70;

    @Transactional(readOnly = true)
    public AnswerResultDto checkAnswer(Long questionId, String answer) {
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> ApiException.notFound("Question not found"));

        boolean isCorrect = question.getCorrectAnswer().equalsIgnoreCase(answer.trim());
        
        return AnswerResultDto.builder()
                .questionId(questionId)
                .correct(isCorrect)
                .correctAnswer(question.getCorrectAnswer())
                .message(isCorrect ? "Great job!" : "Try again!")
                .build();
    }

    @Transactional
    public LessonSubmissionResult submitLessonAnswers(Long lessonId, Long childId, List<AnswerSubmissionDto> answers) {
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> ApiException.notFound("Lesson not found"));

        List<AnswerResultDto> results = new ArrayList<>();
        int correctCount = 0;

        for (AnswerSubmissionDto submission : answers) {
            AnswerResultDto result = checkAnswer(submission.getQuestionId(), submission.getAnswer());
            results.add(result);
            if (result.isCorrect()) {
                correctCount++;
            }
        }

        int totalQuestions = answers.size();
        int score = totalQuestions > 0 
                ? (int) Math.round((correctCount * 100.0) / totalQuestions) 
                : 0;
        
        boolean passed = score >= PASSING_SCORE;
        String message = generateCompletionMessage(score, passed);

        // Record progress using the existing comprehensive ProgressService
        LessonCompletionResult completionResult = null;
        if (passed) {
            completionResult = progressService.recordLessonCompletion(childId, lessonId, score);
        } else {
            // Just record score without marking as complete
            progressService.recordProgress(childId, lessonId, score, false);
        }

        LessonSubmissionResult.LessonSubmissionResultBuilder resultBuilder = LessonSubmissionResult.builder()
                .lessonId(lessonId)
                .totalQuestions(totalQuestions)
                .correctAnswers(correctCount)
                .score(score)
                .passed(passed)
                .message(message)
                .results(results);

        if (completionResult != null) {
            resultBuilder
                    .starsEarned(completionResult.getStarsEarned())
                    .bonusStars(completionResult.getBonusStars())
                    .totalStars(completionResult.getTotalStars())
                    .newAchievements(completionResult.getNewAchievements())
                    .streakUpdated(completionResult.isStreakUpdated())
                    .currentStreak(completionResult.getCurrentStreak());
        } else {
            resultBuilder
                    .starsEarned(0)
                    .bonusStars(0);
        }

        return resultBuilder.build();
    }

    private String generateCompletionMessage(int score, boolean passed) {
        if (score == 100) {
            return "Perfect score! You're a math superstar! ⭐";
        } else if (score >= 90) {
            return "Excellent work! Almost perfect! 🌟";
        } else if (score >= 80) {
            return "Great job! Keep it up! 👏";
        } else if (passed) {
            return "Good work! You passed! 👍";
        } else {
            return "Keep practicing! You can do it! 💪";
        }
    }
}
