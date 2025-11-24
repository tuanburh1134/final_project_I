package com.example.financeapp.feedback.service;

import com.example.financeapp.feedback.dto.FeedbackResponse;
import com.example.financeapp.feedback.dto.SubmitFeedbackRequest;

import java.util.List;

public interface FeedbackService {
    FeedbackResponse submitFeedback(Long userId, SubmitFeedbackRequest request);
    List<FeedbackResponse> getMyFeedbacks(Long userId);
}

