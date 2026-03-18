package org.example.kidsmathapp.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.kidsmathapp.dto.ApiResponse;
import org.example.kidsmathapp.dto.content.LessonDetailDto;
import org.example.kidsmathapp.dto.content.LessonSubmissionResult;
import org.example.kidsmathapp.dto.content.LessonSubmissionRequest;
import org.example.kidsmathapp.service.ContentService;
import org.example.kidsmathapp.service.QuestionService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/lessons")
@RequiredArgsConstructor
public class LessonController {

    private final ContentService contentService;
    private final QuestionService questionService;
    private final ControllerHelper controllerHelper;

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<LessonDetailDto>> getLessonDetail(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {
        // Authentication verified, lesson details don't require specific child context
        LessonDetailDto lesson = contentService.getLessonDetail(id);
        return ResponseEntity.ok(ApiResponse.success(lesson));
    }

    @PostMapping("/{id}/submit")
    public ResponseEntity<ApiResponse<LessonSubmissionResult>> submitAnswers(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id,
            @Valid @RequestBody LessonSubmissionRequest request) {
        controllerHelper.validateChildOwnership(userDetails, request.getChildId());

        LessonSubmissionResult result = questionService.submitLessonAnswers(
                id,
                request.getChildId(),
                request.getAnswers()
        );

        return ResponseEntity.ok(ApiResponse.success(result, result.getMessage()));
    }
}
