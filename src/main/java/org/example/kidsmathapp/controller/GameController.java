package org.example.kidsmathapp.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.kidsmathapp.dto.ApiResponse;
import org.example.kidsmathapp.dto.content.*;
import org.example.kidsmathapp.service.GameService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/games")
@RequiredArgsConstructor
public class GameController {

    private final GameService gameService;
    private final ControllerHelper controllerHelper;

    /**
     * List all games with personal best for the given child.
     * GET /api/v1/games?childId={id}
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<GameDto>>> getGames(
            @RequestParam Long childId,
            @AuthenticationPrincipal UserDetails userDetails) {
        controllerHelper.validateChildOwnership(userDetails, childId);
        List<GameDto> games = gameService.getGames(childId);
        return ResponseEntity.ok(ApiResponse.success(games, "Games retrieved"));
    }

    /**
     * Get game detail with questions (adaptive difficulty applied).
     * GET /api/v1/games/{id}?childId={id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<GameDetailDto>> getGameDetail(
            @PathVariable Long id,
            @RequestParam Long childId,
            @AuthenticationPrincipal UserDetails userDetails) {
        controllerHelper.validateChildOwnership(userDetails, childId);
        GameDetailDto detail = gameService.getGameDetail(id, childId);
        return ResponseEntity.ok(ApiResponse.success(detail, "Game detail retrieved"));
    }

    /**
     * Submit game score, award stars, update streak/achievements.
     * POST /api/v1/games/{id}/score
     */
    @PostMapping("/{id}/score")
    public ResponseEntity<ApiResponse<GameScoreResultDto>> submitScore(
            @PathVariable Long id,
            @Valid @RequestBody GameScoreRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        controllerHelper.validateChildOwnership(userDetails, request.getChildId());
        GameScoreResultDto result = gameService.recordScore(id, request);
        return ResponseEntity.ok(ApiResponse.success(result, "Score recorded"));
    }
}
