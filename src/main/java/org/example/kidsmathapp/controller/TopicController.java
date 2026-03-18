package org.example.kidsmathapp.controller;

import lombok.RequiredArgsConstructor;
import org.example.kidsmathapp.dto.ApiResponse;
import org.example.kidsmathapp.dto.content.LessonWithProgressDto;
import org.example.kidsmathapp.dto.content.TopicDto;
import org.example.kidsmathapp.dto.content.TopicWithProgressDto;
import org.example.kidsmathapp.service.ContentService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/topics")
@RequiredArgsConstructor
public class TopicController {

    private final ContentService contentService;
    private final ControllerHelper controllerHelper;

    @GetMapping
    public ResponseEntity<ApiResponse<List<TopicWithProgressDto>>> getTopicsWithProgress(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam Long childId) {
        controllerHelper.validateChildOwnership(userDetails, childId);
        List<TopicWithProgressDto> topics = contentService.getTopicsWithProgress(childId);
        return ResponseEntity.ok(ApiResponse.success(topics));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TopicDto>> getTopic(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {
        // Just verify authentication - topic details don't require child context
        TopicDto topic = contentService.getTopicById(id);
        return ResponseEntity.ok(ApiResponse.success(topic));
    }

    @GetMapping("/{id}/lessons")
    public ResponseEntity<ApiResponse<List<LessonWithProgressDto>>> getLessonsForTopic(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id,
            @RequestParam Long childId) {
        controllerHelper.validateChildOwnership(userDetails, childId);
        List<LessonWithProgressDto> lessons = contentService.getLessonsForTopic(id, childId);
        return ResponseEntity.ok(ApiResponse.success(lessons));
    }
}
