package org.example.kidsmathapp.service;

import org.example.kidsmathapp.dto.content.QuestionDto;
import org.example.kidsmathapp.entity.enums.Difficulty;
import org.example.kidsmathapp.entity.enums.GradeLevel;
import org.example.kidsmathapp.entity.enums.QuestionType;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class QuestionGeneratorService {

    private final Random random = new Random();

    /**
     * Generate N procedural math questions for a given grade and topic type.
     * Returns List<QuestionDto> with no DB storage.
     */
    public List<QuestionDto> generate(GradeLevel grade, String topicType, int count) {
        List<QuestionDto> questions = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            questions.add(generateOne(grade, topicType, i));
        }
        return questions;
    }

    private QuestionDto generateOne(GradeLevel grade, String topicType, int index) {
        return switch (grade) {
            case KINDERGARTEN -> generateKindergartenQ(topicType, index);
            case GRADE_1 -> generateGrade1Q(topicType, index);
            case GRADE_2 -> generateGrade2Q(topicType, index);
            case GRADE_3 -> generateGrade3Q(topicType, index);
            default -> generateGrade1Q(topicType, index);
        };
    }

    private QuestionDto generateKindergartenQ(String topicType, int index) {
        int a = random.nextInt(5) + 1;
        int b = random.nextInt(5) + 1;
        String correct = String.valueOf(a + b);
        List<String> options = generateOptions(a + b, 1, 10);
        return QuestionDto.builder()
                .id((long) -(index + 1)) // negative IDs = generated (not DB)
                .questionText("What is " + a + " + " + b + "? 🌟")
                .questionType(QuestionType.MULTIPLE_CHOICE)
                .options(options)
                .correctAnswer(correct)
                .difficulty(Difficulty.EASY)
                .imageUrl(null)
                .build();
    }

    private QuestionDto generateGrade1Q(String topicType, int index) {
        int a = random.nextInt(10) + 5;
        int b = random.nextInt(10) + 1;
        String correct = String.valueOf(a + b);
        List<String> options = generateOptions(a + b, 1, 20);
        return QuestionDto.builder()
                .id((long) -(index + 1))
                .questionText("What is " + a + " + " + b + "? 🎯")
                .questionType(QuestionType.MULTIPLE_CHOICE)
                .options(options)
                .correctAnswer(correct)
                .difficulty(Difficulty.EASY)
                .imageUrl(null)
                .build();
    }

    private QuestionDto generateGrade2Q(String topicType, int index) {
        int a = random.nextInt(50) + 10;
        int b = random.nextInt(30) + 10;
        String correct = String.valueOf(a + b);
        List<String> options = generateOptions(a + b, 5, 100);
        return QuestionDto.builder()
                .id((long) -(index + 1))
                .questionText("What is " + a + " + " + b + "? 🚀")
                .questionType(QuestionType.MULTIPLE_CHOICE)
                .options(options)
                .correctAnswer(correct)
                .difficulty(Difficulty.MEDIUM)
                .imageUrl(null)
                .build();
    }

    private QuestionDto generateGrade3Q(String topicType, int index) {
        int a = random.nextInt(10) + 2;
        int b = random.nextInt(10) + 2;
        String correct = String.valueOf(a * b);
        List<String> options = generateOptions(a * b, 2, 100);
        return QuestionDto.builder()
                .id((long) -(index + 1))
                .questionText("What is " + a + " × " + b + "? ⭐")
                .questionType(QuestionType.MULTIPLE_CHOICE)
                .options(options)
                .correctAnswer(correct)
                .difficulty(Difficulty.MEDIUM)
                .imageUrl(null)
                .build();
    }

    /**
     * Generate 4 options including the correct answer, no duplicates,
     * all within [min, max]. Shuffled.
     */
    public List<String> generateOptions(int correct, int min, int max) {
        Set<Integer> opts = new LinkedHashSet<>();
        opts.add(correct);
        int attempts = 0;
        while (opts.size() < 4 && attempts < 50) {
            int delta = random.nextInt(5) + 1;
            int candidate = random.nextBoolean() ? correct + delta : correct - delta;
            if (candidate >= min && candidate <= max && candidate != correct) {
                opts.add(candidate);
            }
            attempts++;
        }
        // fill if needed
        int v = min;
        while (opts.size() < 4) {
            if (!opts.contains(v)) opts.add(v);
            v++;
        }
        List<String> result = opts.stream().map(String::valueOf).collect(Collectors.toList());
        Collections.shuffle(result);
        return result;
    }
}
