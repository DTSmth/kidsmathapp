package org.example.kidsmathapp.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.kidsmathapp.dto.ApiResponse;
import org.example.kidsmathapp.dto.content.AnswerResultDto;
import org.example.kidsmathapp.dto.content.AnswerSubmissionDto;
import org.example.kidsmathapp.service.QuestionService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/questions")
@RequiredArgsConstructor
public class QuestionController {

    private final QuestionService questionService;

    @PostMapping("/{id}/check")
    public ResponseEntity<ApiResponse<AnswerResultDto>> checkAnswer(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id,
            @Valid @RequestBody AnswerSubmissionDto request) {
        AnswerResultDto result = questionService.checkAnswer(id, request.getAnswer());
        return ResponseEntity.ok(ApiResponse.success(result));
    }
}
