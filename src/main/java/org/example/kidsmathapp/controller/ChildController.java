package org.example.kidsmathapp.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.kidsmathapp.dto.ApiResponse;
import org.example.kidsmathapp.dto.child.ChildDto;
import org.example.kidsmathapp.dto.child.ChildSummaryDto;
import org.example.kidsmathapp.dto.child.CreateChildRequest;
import org.example.kidsmathapp.dto.child.UpdateChildRequest;
import org.example.kidsmathapp.service.ChildService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/children")
@RequiredArgsConstructor
public class ChildController {

    private final ChildService childService;
    private final ControllerHelper controllerHelper;

    @GetMapping
    public ResponseEntity<ApiResponse<List<ChildSummaryDto>>> listChildren(
            @AuthenticationPrincipal UserDetails userDetails) {
        Long parentId = controllerHelper.getParentId(userDetails);
        List<ChildSummaryDto> children = childService.getChildrenForParent(parentId);
        return ResponseEntity.ok(ApiResponse.success(children));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ChildDto>> createChild(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody CreateChildRequest request) {
        Long parentId = controllerHelper.getParentId(userDetails);
        ChildDto child = childService.createChild(parentId, request);
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(ApiResponse.success(child, "Child profile created successfully"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ChildDto>> getChild(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {
        Long parentId = controllerHelper.getParentId(userDetails);
        ChildDto child = childService.getChild(id, parentId);
        return ResponseEntity.ok(ApiResponse.success(child));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ChildDto>> updateChild(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id,
            @Valid @RequestBody UpdateChildRequest request) {
        Long parentId = controllerHelper.getParentId(userDetails);
        ChildDto child = childService.updateChild(id, parentId, request);
        return ResponseEntity.ok(ApiResponse.success(child, "Child profile updated successfully"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteChild(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {
        Long parentId = controllerHelper.getParentId(userDetails);
        childService.deleteChild(id, parentId);
        return ResponseEntity.ok(ApiResponse.success("Child profile deleted successfully"));
    }

    @PostMapping("/{id}/advance-grade")
    public ResponseEntity<ApiResponse<Map<String, Object>>> advanceGrade(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {
        Long parentId = controllerHelper.getParentId(userDetails);
        Map<String, Object> result = childService.advanceGrade(id, parentId);
        return ResponseEntity.ok(ApiResponse.success(result, (String) result.get("message")));
    }
}
