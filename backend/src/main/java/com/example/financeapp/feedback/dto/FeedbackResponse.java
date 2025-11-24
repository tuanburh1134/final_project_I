package com.example.financeapp.feedback.dto;

import com.example.financeapp.feedback.FeedbackStatus;
import com.example.financeapp.feedback.FeedbackType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class FeedbackResponse {
    private Long feedbackId;
    private String title;
    private String content;
    private FeedbackType type;
    private FeedbackStatus status;
    private String screenshotUrl;
    private String contactEmail;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

