package com.example.financeapp.ai.controller;

import com.example.financeapp.ai.dto.ChatRequest;
import com.example.financeapp.ai.dto.ChatResponse;
import com.example.financeapp.ai.service.GeminiService;
import com.example.financeapp.ai.service.AiRateLimiter;
import com.example.financeapp.security.CustomUserDetails;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/ai")
@CrossOrigin(origins = "*")
public class AiController {

    private final GeminiService geminiService;
    private final AiRateLimiter rateLimiter;

    public AiController(GeminiService geminiService, AiRateLimiter rateLimiter) {
        this.geminiService = geminiService;
        this.rateLimiter = rateLimiter;
    }

    @PostMapping("/chat")
    public ResponseEntity<Map<String, Object>> chat(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody ChatRequest request
    ) {
        Map<String, Object> res = new HashMap<>();
        try {
            String key = userDetails != null ? "user:" + userDetails.getUser().getUserId() : "anon";
            if (!rateLimiter.allow(key)) {
                res.put("error", "Bạn gửi yêu cầu quá nhanh. Vui lòng thử lại sau ít phút.");
                return ResponseEntity.status(429).body(res);
            }

            if (request.getMessages() == null || request.getMessages().isEmpty()) {
                res.put("error", "messages must not be empty");
                return ResponseEntity.badRequest().body(res);
            }

            Long userId = userDetails != null ? userDetails.getUser().getUserId() : null;

            ChatResponse reply = geminiService.generate(request, userId);
            res.put("reply", reply.getReply());
            res.put("usage", reply.getUsage());
            if (userDetails != null) {
                res.put("userId", userDetails.getUser().getUserId());
            }
            return ResponseEntity.ok(res);
        } catch (IllegalStateException ex) {
            res.put("error", ex.getMessage());
            return ResponseEntity.badRequest().body(res);
        } catch (Exception ex) {
            res.put("error", "AI service error: " + ex.getMessage());
            return ResponseEntity.status(500).body(res);
        }
    }
}
