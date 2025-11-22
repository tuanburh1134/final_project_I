package com.example.financeapp.feedback.service.impl;

import com.example.financeapp.common.service.EmailService;
import com.example.financeapp.feedback.dto.FeedbackResponse;
import com.example.financeapp.feedback.dto.SubmitFeedbackRequest;
import com.example.financeapp.feedback.entity.UserFeedback;
import com.example.financeapp.feedback.repository.UserFeedbackRepository;
import com.example.financeapp.feedback.service.UserFeedbackService;
import com.example.financeapp.user.entity.User;
import com.example.financeapp.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class UserFeedbackServiceImpl implements UserFeedbackService {

    private final UserFeedbackRepository feedbackRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;

    public UserFeedbackServiceImpl(UserFeedbackRepository feedbackRepository,
                                   UserRepository userRepository,
                                   EmailService emailService) {
        this.feedbackRepository = feedbackRepository;
        this.userRepository = userRepository;
        this.emailService = emailService;
    }

    @Override
    @Transactional
    public FeedbackResponse submitFeedback(Long userId, SubmitFeedbackRequest request) {
        User user = userRepository.findById(Objects.requireNonNull(userId, "User không hợp lệ"))
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));

        UserFeedback feedback = new UserFeedback();
        feedback.setUser(user);
        feedback.setFeedbackType(request.getFeedbackType());
        feedback.setPriority(request.getPriority());
        feedback.setTitle(request.getTitle().trim());
        feedback.setDescription(request.getDescription().trim());
        feedback.setModule(request.getModule());
        feedback.setPlatform(request.getPlatform());
        feedback.setAppVersion(request.getAppVersion());
        feedback.setScreenshotUrl(request.getScreenshotUrl());
        String contact = StringUtils.hasText(request.getContactEmail())
                ? request.getContactEmail().trim()
                : user.getEmail();
        feedback.setContactEmail(contact);

        UserFeedback saved = feedbackRepository.save(feedback);

        emailService.sendFeedbackNotification(
                user.getFullName(),
                contact,
                saved.getTitle(),
                saved.getDescription(),
                saved.getModule(),
                saved.getPlatform(),
                saved.getPriority().name()
        );

        return FeedbackResponse.from(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FeedbackResponse> getMyFeedback(Long userId) {
        return feedbackRepository.findByUser_UserIdOrderByCreatedAtDesc(Objects.requireNonNull(userId))
                .stream()
                .map(FeedbackResponse::from)
                .collect(Collectors.toList());
    }
}

