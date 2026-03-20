package org.example.kidsmathapp.controller;

import lombok.RequiredArgsConstructor;
import org.example.kidsmathapp.dto.ApiResponse;
import org.example.kidsmathapp.dto.inventory.*;
import org.example.kidsmathapp.service.InventoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;
    private final ControllerHelper controllerHelper;

    @GetMapping("/{childId}")
    public ResponseEntity<ApiResponse<InventoryDto>> getInventory(
            @PathVariable Long childId,
            @AuthenticationPrincipal UserDetails userDetails) {
        controllerHelper.validateChildOwnership(userDetails, childId);
        return ResponseEntity.ok(ApiResponse.success(inventoryService.getInventory(childId)));
    }

    @PostMapping("/{childId}/equip")
    public ResponseEntity<ApiResponse<InventoryItemDto>> equipItem(
            @PathVariable Long childId,
            @RequestBody EquipItemRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        controllerHelper.validateChildOwnership(userDetails, childId);
        InventoryItemDto result = inventoryService.equipItem(childId, request.getItemId(), request.getSlot());
        return ResponseEntity.ok(ApiResponse.success(result, "Item equipped"));
    }

    @PostMapping("/{childId}/daily-bonus")
    public ResponseEntity<ApiResponse<DailyBonusResponse>> claimDailyBonus(
            @PathVariable Long childId,
            @AuthenticationPrincipal UserDetails userDetails) {
        controllerHelper.validateChildOwnership(userDetails, childId);
        DailyBonusResponse response = inventoryService.claimDailyBonus(childId);
        return ResponseEntity.ok(ApiResponse.success(response, "Daily bonus processed"));
    }
}
