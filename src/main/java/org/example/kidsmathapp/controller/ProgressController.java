package org.example.kidsmathapp.controller;

import lombok.RequiredArgsConstructor;
import org.example.kidsmathapp.dto.ApiResponse;
import org.example.kidsmathapp.dto.progress.DashboardDto;
import org.example.kidsmathapp.service.ProgressService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/progress")
@RequiredArgsConstructor
public class ProgressController {

    private final ProgressService progressService;
    private final ControllerHelper controllerHelper;

    @GetMapping("/dashboard/{childId}")
    public ResponseEntity<ApiResponse<DashboardDto>> getChildDashboard(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long childId) {
        controllerHelper.validateChildOwnership(userDetails, childId);
        DashboardDto dashboard = progressService.getChildDashboard(childId);
        return ResponseEntity.ok(ApiResponse.success(dashboard));
    }
}
