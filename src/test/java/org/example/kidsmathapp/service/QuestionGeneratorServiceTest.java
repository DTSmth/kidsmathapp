package org.example.kidsmathapp.service;

import org.example.kidsmathapp.dto.content.QuestionDto;
import org.example.kidsmathapp.entity.enums.GradeLevel;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

class QuestionGeneratorServiceTest {

    private final QuestionGeneratorService service = new QuestionGeneratorService();

    @Test
    void generate_returns_exactly_n_questions() {
        List<QuestionDto> questions = service.generate(GradeLevel.KINDERGARTEN, "addition", 10);
        assertThat(questions).hasSize(10);
    }

    @Test
    void generate_returns_fewer_questions_when_count_is_small() {
        List<QuestionDto> questions = service.generate(GradeLevel.GRADE_1, "addition", 3);
        assertThat(questions).hasSize(3);
    }

    @Test
    void all_questions_have_correct_answer_in_options() {
        List<QuestionDto> questions = service.generate(GradeLevel.KINDERGARTEN, "addition", 20);
        for (QuestionDto q : questions) {
            assertThat(q.getOptions())
                    .as("Options should contain correct answer for question: %s", q.getQuestionText())
                    .contains(q.getCorrectAnswer());
        }
    }

    @Test
    void options_have_no_duplicates() {
        List<QuestionDto> questions = service.generate(GradeLevel.GRADE_2, "addition", 20);
        for (QuestionDto q : questions) {
            Set<String> uniqueOptions = Set.copyOf(q.getOptions());
            assertThat(uniqueOptions.size())
                    .as("Options should have no duplicates for question: %s", q.getQuestionText())
                    .isEqualTo(q.getOptions().size());
        }
    }

    @Test
    void each_question_has_four_options() {
        List<QuestionDto> questions = service.generate(GradeLevel.GRADE_3, "multiplication", 15);
        for (QuestionDto q : questions) {
            assertThat(q.getOptions())
                    .as("Each question should have exactly 4 options")
                    .hasSize(4);
        }
    }

    @Test
    void correct_answer_is_always_in_options_list() {
        for (GradeLevel grade : GradeLevel.values()) {
            if (grade == GradeLevel.GRADE_4 || grade == GradeLevel.GRADE_5) continue;
            List<QuestionDto> questions = service.generate(grade, "mixed", 10);
            for (QuestionDto q : questions) {
                assertThat(q.getOptions())
                        .as("Correct answer must be in options for grade %s", grade)
                        .contains(q.getCorrectAnswer());
            }
        }
    }

    @ParameterizedTest
    @EnumSource(value = GradeLevel.class, names = {"KINDERGARTEN", "GRADE_1", "GRADE_2", "GRADE_3"})
    void works_for_all_main_grade_levels(GradeLevel grade) {
        List<QuestionDto> questions = service.generate(grade, "addition", 5);
        assertThat(questions)
                .hasSize(5)
                .allMatch(q -> q.getQuestionText() != null)
                .allMatch(q -> q.getCorrectAnswer() != null)
                .allMatch(q -> q.getOptions() != null && !q.getOptions().isEmpty());
    }

    @Test
    void questions_have_negative_ids_for_generated_content() {
        List<QuestionDto> questions = service.generate(GradeLevel.KINDERGARTEN, "addition", 5);
        for (QuestionDto q : questions) {
            assertThat(q.getId())
                    .as("Generated questions should have negative IDs")
                    .isNegative();
        }
    }

    @Test
    void generate_options_produces_valid_options() {
        List<String> options = service.generateOptions(15, 1, 20);
        assertThat(options)
                .hasSize(4)
                .contains("15"); // correct answer should be in options
        // No duplicates
        assertThat(Set.copyOf(options)).hasSize(4);
    }
}
