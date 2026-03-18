package org.example.kidsmathapp.dto.content;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnswerResultDto {

    private Long questionId;
    private boolean correct;
    private String correctAnswer;
    private String message;
}
