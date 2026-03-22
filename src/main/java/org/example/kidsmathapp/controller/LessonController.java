package org.example.kidsmathapp.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.kidsmathapp.dto.ApiResponse;
import org.example.kidsmathapp.dto.content.LessonDetailDto;
import org.example.kidsmathapp.dto.content.LessonSubmissionResult;
import org.example.kidsmathapp.dto.content.LessonSubmissionRequest;
import org.example.kidsmathapp.entity.Progress;
import org.example.kidsmathapp.service.ContentService;
import org.example.kidsmathapp.service.PaywallEnforcer;
import org.example.kidsmathapp.service.ProgressService;
import org.example.kidsmathapp.service.QuestionService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/lessons")
@RequiredArgsConstructor
public class LessonController {

    private final ContentService contentService;
    private final QuestionService questionService;
    private final ProgressService progressService;
    private final ControllerHelper controllerHelper;
    private final PaywallEnforcer paywallEnforcer;

    @PostMapping("/{id}/start")
    public ResponseEntity<ApiResponse<Map<String, Object>>> startLesson(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id,
            @RequestParam Long childId) {
        controllerHelper.validateChildOwnership(userDetails, childId);

        Long parentId = controllerHelper.getParentId(userDetails);
        if (!paywallEnforcer.canStartLesson(parentId, childId)) {
            throw org.example.kidsmathapp.exception.ApiException.forbidden(
                    "Daily lesson limit reached. Upgrade to Premium for unlimited lessons.");
        }

        Progress progress = progressService.startLesson(childId, id);
        Map<String, Object> result = Map.of(
                "lessonId", id,
                "startedAt", progress.getLessonStartedAt().toString()
        );
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<LessonDetailDto>> getLessonDetail(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id,
            @RequestParam(required = false) Long childId) {
        LessonDetailDto lesson = contentService.getLessonDetail(id, childId);
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
