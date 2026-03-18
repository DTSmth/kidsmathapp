package org.example.kidsmathapp.dto.child;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.kidsmathapp.entity.enums.GradeLevel;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateChildRequest {

    @NotBlank(message = "Name is required")
    @Size(min = 1, max = 50, message = "Name must be between 1 and 50 characters")
    private String name;

    @NotBlank(message = "Avatar ID is required")
    private String avatarId;

    @Past(message = "Birth date must be in the past")
    private LocalDate birthDate;

    @NotNull(message = "Grade level is required")
    private GradeLevel gradeLevel;
}
