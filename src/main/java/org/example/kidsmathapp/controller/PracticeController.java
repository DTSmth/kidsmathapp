package org.example.kidsmathapp.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.kidsmathapp.dto.ApiResponse;
import org.example.kidsmathapp.dto.content.*;
import org.example.kidsmathapp.entity.Child;
import org.example.kidsmathapp.entity.enums.ItemDropSource;
import org.example.kidsmathapp.exception.ApiException;
import org.example.kidsmathapp.repository.ChildRepository;
import org.example.kidsmathapp.service.GamificationOrchestrator;
import org.example.kidsmathapp.service.QuestionGeneratorService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/practice")
@RequiredArgsConstructor
@Slf4j
public class PracticeController {

    private final QuestionGeneratorService questionGeneratorService;
    private final ChildRepository childRepository;
    private final ControllerHelper controllerHelper;
    private final GamificationOrchestrator gamificationOrchestrator;
    private final ObjectMapper objectMapper;

    @Value("${app.jwt.secret}")
    private String jwtSecret;

    private static final long SESSION_EXPIRY_SECONDS = 30 * 60; // 30 minutes

    @PostMapping("/generate")
    public ResponseEntity<ApiResponse<PracticeSessionDto>> generatePractice(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody PracticeGenerateRequest request) {
        controllerHelper.validateChildOwnership(userDetails, request.getChildId());

        Child child = childRepository.findById(request.getChildId())
                .orElseThrow(() -> ApiException.notFound("Child not found"));

        int count = request.getCount() > 0 ? request.getCount() : 10;
        List<QuestionDto> questions = questionGeneratorService.generate(
                child.getGradeLevel(), request.getTopicType(), count);

        String sessionToken = createSessionToken(request.getChildId(), questions);

        PracticeSessionDto session = PracticeSessionDto.builder()
                .sessionToken(sessionToken)
                .questions(questions)
                .build();

        return ResponseEntity.ok(ApiResponse.success(session));
    }

    @PostMapping("/submit")
    public ResponseEntity<ApiResponse<PracticeResultDto>> submitPractice(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody PracticeSubmitRequest request) {
        // Validate and extract session
        SessionData sessionData = validateSessionToken(request.getSessionToken());

        controllerHelper.validateChildOwnership(userDetails, sessionData.childId());

        // Score the answers using server-side correct answers (never trust client)
        List<QuestionDto> questions = sessionData.questions();
        List<PracticeSubmitRequest.PracticeAnswerDto> answers = request.getAnswers();

        int correctCount = 0;
        for (PracticeSubmitRequest.PracticeAnswerDto answer : answers) {
            int idx = answer.getQuestionIndex();
            if (idx >= 0 && idx < questions.size()) {
                QuestionDto q = questions.get(idx);
                if (q.getCorrectAnswer() != null && q.getCorrectAnswer().equals(answer.getAnswer())) {
                    correctCount++;
                }
            }
        }

        int totalCount = questions.size();
        int score = totalCount > 0 ? (int) Math.round(correctCount * 100.0 / totalCount) : 0;

        // Award stars: 1 star per 20% score
        int starsEarned = score / 20;

        GamificationOrchestrator.OrchestratorResult gamification = null;
        if (starsEarned > 0) {
            try {
                gamification = gamificationOrchestrator.orchestrate(
                        sessionData.childId(),
                        starsEarned,
                        "Practice session completed (Score: " + score + "%)",
                        ItemDropSource.LESSON_COMPLETION
                );
            } catch (Exception e) {
                log.warn("Gamification failed for practice session: {}", e.getMessage());
            }
        }

        org.example.kidsmathapp.dto.inventory.InventoryItemDto droppedItem = null;
        if (gamification != null && !gamification.newItems().isEmpty()) {
            droppedItem = gamification.newItems().get(0);
        }

        PracticeResultDto result = PracticeResultDto.builder()
                .score(score)
                .correctCount(correctCount)
                .totalCount(totalCount)
                .starsEarned(starsEarned)
                .newItem(droppedItem)
                .build();

        return ResponseEntity.ok(ApiResponse.success(result));
    }

    private String createSessionToken(Long childId, List<QuestionDto> questions) {
        try {
            Map<String, Object> payload = Map.of(
                    "sessionId", UUID.randomUUID().toString(),
                    "childId", childId,
                    "questions", questions,
                    "generatedAt", Instant.now().getEpochSecond(),
                    "expiresAt", Instant.now().getEpochSecond() + SESSION_EXPIRY_SECONDS
            );
            String jsonPayload = objectMapper.writeValueAsString(payload);
            String base64Payload = Base64.getUrlEncoder().withoutPadding()
                    .encodeToString(jsonPayload.getBytes(StandardCharsets.UTF_8));
            String hmac = computeHmac(base64Payload);
            return base64Payload + "." + hmac;
        } catch (Exception e) {
            throw new RuntimeException("Failed to create session token", e);
        }
    }

    private SessionData validateSessionToken(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length != 2) {
                throw ApiException.badRequest("Invalid session token format");
            }
            String base64Payload = parts[0];
            String providedHmac = parts[1];

            // Verify HMAC
            String expectedHmac = computeHmac(base64Payload);
            if (!expectedHmac.equals(providedHmac)) {
                throw ApiException.badRequest("Invalid session token signature");
            }

            // Decode payload
            String jsonPayload = new String(Base64.getUrlDecoder().decode(base64Payload), StandardCharsets.UTF_8);
            Map<String, Object> payload = objectMapper.readValue(jsonPayload, new TypeReference<>() {});

            // Check expiry
            long expiresAt = ((Number) payload.get("expiresAt")).longValue();
            if (Instant.now().getEpochSecond() > expiresAt) {
                throw ApiException.badRequest("Session token has expired");
            }

            Long childId = ((Number) payload.get("childId")).longValue();
            List<QuestionDto> questions = objectMapper.convertValue(
                    payload.get("questions"), new TypeReference<List<QuestionDto>>() {});

            return new SessionData(childId, questions);
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw ApiException.badRequest("Invalid session token: " + e.getMessage());
        }
    }

    private String computeHmac(String data) throws Exception {
        byte[] keyBytes = Base64.getDecoder().decode(jwtSecret);
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(keyBytes, "HmacSHA256"));
        byte[] hmacBytes = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        return Base64.getUrlEncoder().withoutPadding().encodeToString(hmacBytes);
    }

    private record SessionData(Long childId, List<QuestionDto> questions) {}
}
