package org.example.kidsmathapp.controller;

import lombok.RequiredArgsConstructor;
import org.example.kidsmathapp.dto.ApiResponse;
import org.example.kidsmathapp.dto.leaderboard.*;
import org.example.kidsmathapp.service.LeaderboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/leaderboard")
@RequiredArgsConstructor
public class LeaderboardController {

    private final LeaderboardService leaderboardService;
    private final ControllerHelper controllerHelper;

    @GetMapping("/family")
    public ResponseEntity<ApiResponse<FamilyLeaderboardDto>> getFamilyLeaderboard(
            @RequestParam Long childId,
            @AuthenticationPrincipal UserDetails userDetails) {
        controllerHelper.validateChildOwnership(userDetails, childId);
        return ResponseEntity.ok(ApiResponse.success(leaderboardService.getFamilyLeaderboard(childId)));
    }

    @GetMapping("/games/{gameId}")
    public ResponseEntity<ApiResponse<GameLeaderboardDto>> getGameLeaderboard(
            @PathVariable Long gameId,
            @RequestParam Long childId,
            @RequestParam(defaultValue = "NORMAL") String mode,
            @AuthenticationPrincipal UserDetails userDetails) {
        controllerHelper.validateChildOwnership(userDetails, childId);
        return ResponseEntity.ok(ApiResponse.success(
                leaderboardService.getGameLeaderboard(gameId, mode, childId)));
    }
}
