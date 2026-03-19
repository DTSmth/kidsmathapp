package org.example.kidsmathapp.dto.content;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.kidsmathapp.entity.enums.Difficulty;
import org.example.kidsmathapp.entity.enums.QuestionType;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestionDto {

    private Long id;
    private String questionText;
    private QuestionType questionType;
    private List<String> options;
    private Difficulty difficulty;
    private String imageUrl;
    private String correctAnswer;
}
