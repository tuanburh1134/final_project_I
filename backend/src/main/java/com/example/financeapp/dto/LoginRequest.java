package com.example.financeapp.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequest {
    
    @NotBlank(message = "Email là bắt buộc.")
    @Email(message = "Email không hợp lệ.")
    private String email;
    
    @NotBlank(message = "Mật khẩu là bắt buộc.")
    private String password;
    
    @NotBlank(message = "CAPTCHA là bắt buộc.")
    private String captchaToken;
}
