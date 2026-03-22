package org.example.kidsmathapp.controller;

import lombok.RequiredArgsConstructor;
import org.example.kidsmathapp.dto.ApiResponse;
import org.example.kidsmathapp.dto.child.ChildSummaryDto;
import org.example.kidsmathapp.dto.progress.ParentDashboardDto;
import org.example.kidsmathapp.entity.Child;
import org.example.kidsmathapp.mapper.ChildMapper;
import org.example.kidsmathapp.repository.ChildRepository;
import org.example.kidsmathapp.service.ParentDashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/parent")
@RequiredArgsConstructor
public class ParentDashboardController {

    private final ParentDashboardService parentDashboardService;
    private final ChildRepository childRepository;
    private final ChildMapper childMapper;
    private final ControllerHelper controllerHelper;

    @GetMapping("/children")
    public ResponseEntity<ApiResponse<List<ChildSummaryDto>>> getChildren(
            @AuthenticationPrincipal UserDetails userDetails) {
        Long parentId = controllerHelper.getParentId(userDetails);
        List<Child> children = childRepository.findByParentId(parentId);
        List<ChildSummaryDto> dtos = children.stream()
                .map(childMapper::toSummaryDto)
                .toList();
        return ResponseEntity.ok(ApiResponse.success(dtos));
    }

    @GetMapping("/children/{childId}/dashboard")
    public ResponseEntity<ApiResponse<ParentDashboardDto>> getChildDashboard(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long childId) {
        Long parentId = controllerHelper.getParentId(userDetails);
        ParentDashboardDto dashboard = parentDashboardService.getDashboard(parentId, childId);
        return ResponseEntity.ok(ApiResponse.success(dashboard));
    }
}
