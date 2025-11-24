package com.example.financeapp.feedback.controller;

import com.example.financeapp.feedback.dto.FeedbackResponse;
import com.example.financeapp.feedback.dto.SubmitFeedbackRequest;
import com.example.financeapp.feedback.service.UserFeedbackService;
import com.example.financeapp.security.CustomUserDetails;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/feedback")
public class FeedbackController {

    private final UserFeedbackService feedbackService;

    public FeedbackController(UserFeedbackService feedbackService) {
        this.feedbackService = feedbackService;
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> submitFeedback(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody SubmitFeedbackRequest request
    ) {
        Long userId = requireUserId(userDetails);
        FeedbackResponse response = feedbackService.submitFeedback(userId, request);
        Map<String, Object> result = new HashMap<>();
        result.put("message", "Cảm ơn bạn! Phản hồi đã được ghi nhận.");
        result.put("feedback", response);
        return ResponseEntity.ok(result);
    }

    @GetMapping
    public ResponseEntity<List<FeedbackResponse>> getMyFeedback(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long userId = requireUserId(userDetails);
        return ResponseEntity.ok(feedbackService.getMyFeedback(userId));
    }

    private Long requireUserId(CustomUserDetails userDetails) {
        if (userDetails == null || userDetails.getUser() == null || userDetails.getUser().getUserId() == null) {
            throw new RuntimeException("Không tìm thấy thông tin người dùng");
        }
        return userDetails.getUser().getUserId();
    }
}

