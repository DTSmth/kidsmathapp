package org.example.kidsmathapp.controller;

import lombok.RequiredArgsConstructor;
import org.example.kidsmathapp.dto.ApiResponse;
import org.example.kidsmathapp.service.ParentReportEmailService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/internal/jobs")
@RequiredArgsConstructor
public class InternalJobController {

    @Value("${internal.job-secret:change-me-in-prod}")
    private String jobSecret;

    private final ParentReportEmailService emailService;

    @PostMapping("/weekly-email")
    public ResponseEntity<ApiResponse<String>> triggerWeeklyEmail(
            @RequestHeader("X-Internal-Secret") String secret) {
        if (!jobSecret.equals(secret)) {
            return ResponseEntity.status(403).body(ApiResponse.error("Unauthorized"));
        }
        emailService.sendWeeklyDigests();
        return ResponseEntity.ok(ApiResponse.success("Weekly emails dispatched", "OK"));
    }
}
