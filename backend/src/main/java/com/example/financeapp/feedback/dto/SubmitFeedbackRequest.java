package com.example.financeapp.feedback.dto;

import com.example.financeapp.feedback.FeedbackType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SubmitFeedbackRequest {

    @NotBlank(message = "Tiêu đề không được để trống")
    @Size(max = 255)
    private String title;

    @NotBlank(message = "Nội dung không được để trống")
    @Size(max = 2000)
    private String content;

    @NotNull(message = "Loại phản hồi không được để trống")
    private FeedbackType type;

    @Size(max = 500)
    private String screenshotUrl;

    @Email(message = "Email không hợp lệ")
    private String contactEmail;
}

