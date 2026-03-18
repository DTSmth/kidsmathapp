package org.example.kidsmathapp.service;

import org.example.kidsmathapp.dto.content.AnswerSubmissionDto;
import org.example.kidsmathapp.dto.content.LessonSubmissionResult;
import org.example.kidsmathapp.dto.progress.LessonCompletionResult;
import org.example.kidsmathapp.entity.Lesson;
import org.example.kidsmathapp.entity.Question;
import org.example.kidsmathapp.exception.ApiException;
import org.example.kidsmathapp.repository.LessonRepository;
import org.example.kidsmathapp.repository.QuestionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class QuestionServiceTest {

    @Mock private QuestionRepository questionRepository;
    @Mock private LessonRepository lessonRepository;
    @Mock private ProgressService progressService;

    @InjectMocks private QuestionService questionService;

    private Lesson lesson;
    private Question q1, q2, q3;

    @BeforeEach
    void setUp() {
        lesson = Lesson.builder().starsReward(10).title("Math Lesson").build();

        q1 = Question.builder().correctAnswer("7").build();
        q2 = Question.builder().correctAnswer("4").build();
        q3 = Question.builder().correctAnswer("9").build();
    }

    @Test
    void submitLessonAnswers_calculates_score_correctly() {
        when(lessonRepository.findById(anyLong())).thenReturn(Optional.of(lesson));
        when(questionRepository.findById(1L)).thenReturn(Optional.of(q1));
        when(questionRepository.findById(2L)).thenReturn(Optional.of(q2));
        when(questionRepository.findById(3L)).thenReturn(Optional.of(q3));
        when(progressService.recordProgress(anyLong(), anyLong(), anyInt(), anyBoolean()))
                .thenReturn(null);

        List<AnswerSubmissionDto> answers = List.of(
                AnswerSubmissionDto.builder().questionId(1L).answer("7").build(),  // correct
                AnswerSubmissionDto.builder().questionId(2L).answer("5").build(),  // wrong
                AnswerSubmissionDto.builder().questionId(3L).answer("9").build()   // correct
        );

        LessonSubmissionResult result = questionService.submitLessonAnswers(1L, 1L, answers);

        assertThat(result.getCorrectAnswers()).isEqualTo(2);
        assertThat(result.getTotalQuestions()).isEqualTo(3);
        assertThat(result.getScore()).isEqualTo(67); // Math.round(2/3 * 100)
    }

    @Test
    void submitLessonAnswers_passes_at_70_percent() {
        when(lessonRepository.findById(anyLong())).thenReturn(Optional.of(lesson));
        // 7 correct out of 10
        for (int i = 1; i <= 10; i++) {
            Question q = Question.builder().correctAnswer("correct").build();
            when(questionRepository.findById((long) i)).thenReturn(Optional.of(q));
        }
        LessonCompletionResult completionResult = LessonCompletionResult.builder()
                .starsEarned(10).bonusStars(0).totalStars(10).currentStreak(0).streakUpdated(false)
                .build();
        when(progressService.recordLessonCompletion(anyLong(), anyLong(), anyInt()))
                .thenReturn(completionResult);

        List<AnswerSubmissionDto> answers = new ArrayList<>();
        for (int i = 1; i <= 7; i++) {
            answers.add(AnswerSubmissionDto.builder().questionId((long) i).answer("correct").build());
        }
        for (int i = 8; i <= 10; i++) {
            answers.add(AnswerSubmissionDto.builder().questionId((long) i).answer("wrong").build());
        }

        LessonSubmissionResult result = questionService.submitLessonAnswers(1L, 1L, answers);

        assertThat(result.isPassed()).isTrue();
        assertThat(result.getScore()).isEqualTo(70);
    }

    @Test
    void submitLessonAnswers_fails_below_70_percent() {
        when(lessonRepository.findById(anyLong())).thenReturn(Optional.of(lesson));
        for (int i = 1; i <= 10; i++) {
            Question q = Question.builder().correctAnswer("correct").build();
            when(questionRepository.findById((long) i)).thenReturn(Optional.of(q));
        }
        when(progressService.recordProgress(anyLong(), anyLong(), anyInt(), anyBoolean()))
                .thenReturn(null);

        List<AnswerSubmissionDto> answers = new ArrayList<>();
        for (int i = 1; i <= 6; i++) {
            answers.add(AnswerSubmissionDto.builder().questionId((long) i).answer("correct").build());
        }
        for (int i = 7; i <= 10; i++) {
            answers.add(AnswerSubmissionDto.builder().questionId((long) i).answer("wrong").build());
        }

        LessonSubmissionResult result = questionService.submitLessonAnswers(1L, 1L, answers);

        assertThat(result.isPassed()).isFalse();
        assertThat(result.getScore()).isEqualTo(60);
    }

    @Test
    void submitLessonAnswers_returns_zero_score_for_empty_answers() {
        when(lessonRepository.findById(anyLong())).thenReturn(Optional.of(lesson));
        when(progressService.recordProgress(anyLong(), anyLong(), anyInt(), anyBoolean()))
                .thenReturn(null);

        LessonSubmissionResult result = questionService.submitLessonAnswers(1L, 1L, List.of());

        assertThat(result.getScore()).isEqualTo(0);
        assertThat(result.isPassed()).isFalse();
    }
}
