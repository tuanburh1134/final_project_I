package com.example.financeapp.feedback.service.impl;

import com.example.financeapp.feedback.dto.FeedbackResponse;
import com.example.financeapp.feedback.dto.SubmitFeedbackRequest;
import com.example.financeapp.feedback.entity.Feedback;
import com.example.financeapp.feedback.repository.FeedbackRepository;
import com.example.financeapp.feedback.service.FeedbackService;
import com.example.financeapp.user.entity.User;
import com.example.financeapp.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FeedbackServiceImpl implements FeedbackService {

    private final FeedbackRepository feedbackRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public FeedbackResponse submitFeedback(Long userId, SubmitFeedbackRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));

        Feedback feedback = new Feedback();
        feedback.setUser(user);
        feedback.setTitle(request.getTitle().trim());
        feedback.setContent(request.getContent().trim());
        feedback.setType(request.getType());
        feedback.setScreenshotUrl(request.getScreenshotUrl());
        feedback.setContactEmail(request.getContactEmail() != null
                ? request.getContactEmail().trim()
                : user.getEmail());

        Feedback saved = feedbackRepository.save(feedback);
        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FeedbackResponse> getMyFeedbacks(Long userId) {
        return feedbackRepository.findByUser_UserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private FeedbackResponse toResponse(Feedback feedback) {
        return FeedbackResponse.builder()
                .feedbackId(feedback.getFeedbackId())
                .title(feedback.getTitle())
                .content(feedback.getContent())
                .type(feedback.getType())
                .status(feedback.getStatus())
                .screenshotUrl(feedback.getScreenshotUrl())
                .contactEmail(feedback.getContactEmail())
                .createdAt(feedback.getCreatedAt())
                .updatedAt(feedback.getUpdatedAt())
                .build();
    }
}

