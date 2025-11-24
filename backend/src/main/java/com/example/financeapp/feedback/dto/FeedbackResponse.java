package com.example.financeapp.feedback.dto;

import com.example.financeapp.feedback.entity.FeedbackPriority;
import com.example.financeapp.feedback.entity.FeedbackStatus;
import com.example.financeapp.feedback.entity.FeedbackType;
import com.example.financeapp.feedback.entity.UserFeedback;

import java.time.LocalDateTime;

public class FeedbackResponse {

    private Long feedbackId;
    private FeedbackType feedbackType;
    private FeedbackPriority priority;
    private FeedbackStatus status;
    private String title;
    private String description;
    private String module;
    private String platform;
    private String appVersion;
    private String screenshotUrl;
    private String adminReply;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static FeedbackResponse from(UserFeedback feedback) {
        FeedbackResponse response = new FeedbackResponse();
        response.setFeedbackId(feedback.getFeedbackId());
        response.setFeedbackType(feedback.getFeedbackType());
        response.setPriority(feedback.getPriority());
        response.setStatus(feedback.getStatus());
        response.setTitle(feedback.getTitle());
        response.setDescription(feedback.getDescription());
        response.setModule(feedback.getModule());
        response.setPlatform(feedback.getPlatform());
        response.setAppVersion(feedback.getAppVersion());
        response.setScreenshotUrl(feedback.getScreenshotUrl());
        response.setAdminReply(feedback.getAdminReply());
        response.setCreatedAt(feedback.getCreatedAt());
        response.setUpdatedAt(feedback.getUpdatedAt());
        return response;
    }

    public Long getFeedbackId() {
        return feedbackId;
    }

    public void setFeedbackId(Long feedbackId) {
        this.feedbackId = feedbackId;
    }

    public FeedbackType getFeedbackType() {
        return feedbackType;
    }

    public void setFeedbackType(FeedbackType feedbackType) {
        this.feedbackType = feedbackType;
    }

    public FeedbackPriority getPriority() {
        return priority;
    }

    public void setPriority(FeedbackPriority priority) {
        this.priority = priority;
    }

    public FeedbackStatus getStatus() {
        return status;
    }

    public void setStatus(FeedbackStatus status) {
        this.status = status;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getModule() {
        return module;
    }

    public void setModule(String module) {
        this.module = module;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public String getAppVersion() {
        return appVersion;
    }

    public void setAppVersion(String appVersion) {
        this.appVersion = appVersion;
    }

    public String getScreenshotUrl() {
        return screenshotUrl;
    }

    public void setScreenshotUrl(String screenshotUrl) {
        this.screenshotUrl = screenshotUrl;
    }

    public String getAdminReply() {
        return adminReply;
    }

    public void setAdminReply(String adminReply) {
        this.adminReply = adminReply;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}

