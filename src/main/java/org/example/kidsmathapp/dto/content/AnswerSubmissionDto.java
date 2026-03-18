package org.example.kidsmathapp.dto.content;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnswerSubmissionDto {

    @NotNull(message = "Question ID is required")
    private Long questionId;

    @NotBlank(message = "Answer is required")
    private String answer;
}
