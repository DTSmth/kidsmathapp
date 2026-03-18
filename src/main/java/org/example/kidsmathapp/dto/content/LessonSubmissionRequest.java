package org.example.kidsmathapp.dto.content;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LessonSubmissionRequest {

    @NotNull(message = "Child ID is required")
    private Long childId;

    @NotNull(message = "Answers cannot be null")
    @NotEmpty(message = "Answers cannot be empty")
    @Valid
    private List<AnswerSubmissionDto> answers;
}
