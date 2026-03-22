package org.example.kidsmathapp.controller;

import lombok.RequiredArgsConstructor;
import org.example.kidsmathapp.dto.ApiResponse;
import org.example.kidsmathapp.entity.Subscription;
import org.example.kidsmathapp.service.SubscriptionService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/subscription")
@RequiredArgsConstructor
public class SubscriptionController {

    private final SubscriptionService subscriptionService;
    private final ControllerHelper controllerHelper;

    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> getSubscription(
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = controllerHelper.getParentId(userDetails);
        Subscription sub = subscriptionService.getSubscriptionForUser(userId);
        Map<String, Object> result = Map.of(
                "status", sub.getStatus(),
                "isPremium", sub.isPremium(),
                "periodEnd", sub.getPeriodEnd() != null ? sub.getPeriodEnd().toString() : null
        );
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @PostMapping("/create-checkout")
    public ResponseEntity<ApiResponse<Map<String, String>>> createCheckout(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody(required = false) Map<String, String> body) {
        Long userId = controllerHelper.getParentId(userDetails);
        String successUrl = body != null ? body.get("successUrl") : null;
        String cancelUrl = body != null ? body.get("cancelUrl") : null;
        String checkoutUrl = subscriptionService.createCheckoutSession(userId, successUrl, cancelUrl);
        return ResponseEntity.ok(ApiResponse.success(Map.of("checkoutUrl", checkoutUrl)));
    }
}
