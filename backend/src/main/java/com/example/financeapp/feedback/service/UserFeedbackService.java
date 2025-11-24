package com.example.financeapp.feedback.service;

import com.example.financeapp.feedback.dto.FeedbackResponse;
import com.example.financeapp.feedback.dto.SubmitFeedbackRequest;

import java.util.List;

public interface UserFeedbackService {

    FeedbackResponse submitFeedback(Long userId, SubmitFeedbackRequest request);

    List<FeedbackResponse> getMyFeedback(Long userId);
}

