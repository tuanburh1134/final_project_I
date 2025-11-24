package com.example.financeapp.feedback.dto;

import com.example.financeapp.feedback.entity.FeedbackPriority;
import com.example.financeapp.feedback.entity.FeedbackType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class SubmitFeedbackRequest {

    @NotNull(message = "Loại phản hồi không được để trống")
    private FeedbackType feedbackType;

    @NotNull(message = "Mức độ ưu tiên không hợp lệ")
    private FeedbackPriority priority = FeedbackPriority.MEDIUM;

    @NotBlank(message = "Tiêu đề không được để trống")
    @Size(max = 200, message = "Tiêu đề tối đa 200 ký tự")
    private String title;

    @NotBlank(message = "Mô tả chi tiết là bắt buộc")
    @Size(max = 4000, message = "Mô tả tối đa 4000 ký tự")
    private String description;

    @Size(max = 100, message = "Tên màn hình/module tối đa 100 ký tự")
    private String module;

    @Size(max = 50, message = "Nền tảng tối đa 50 ký tự")
    private String platform;

    @Size(max = 50, message = "Phiên bản ứng dụng tối đa 50 ký tự")
    private String appVersion;

    @Size(max = 500, message = "URL ảnh tối đa 500 ký tự")
    private String screenshotUrl;

    @Email(message = "Email liên hệ không hợp lệ")
    @Size(max = 150, message = "Email liên hệ tối đa 150 ký tự")
    private String contactEmail;

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

    public String getContactEmail() {
        return contactEmail;
    }

    public void setContactEmail(String contactEmail) {
        this.contactEmail = contactEmail;
    }
}

