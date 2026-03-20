package org.example.kidsmathapp.controller;

import lombok.RequiredArgsConstructor;
import org.example.kidsmathapp.dto.ApiResponse;
import org.example.kidsmathapp.dto.progress.StreakCalendarDto;
import org.example.kidsmathapp.service.StreakService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/streaks")
@RequiredArgsConstructor
public class StreakController {

    private final StreakService streakService;
    private final ControllerHelper controllerHelper;

    @GetMapping("/{childId}/calendar")
    public ResponseEntity<ApiResponse<StreakCalendarDto>> getStreakCalendar(
            @PathVariable Long childId,
            @AuthenticationPrincipal UserDetails userDetails) {
        controllerHelper.validateChildOwnership(userDetails, childId);
        return ResponseEntity.ok(ApiResponse.success(streakService.getStreakCalendar(childId)));
    }
}
