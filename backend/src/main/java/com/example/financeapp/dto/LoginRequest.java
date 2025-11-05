package com.example.financeapp.dto;

public class LoginRequest {
    
    @NotBlank(message = "Email là bắt buộc.")
    @Email(message = "Email không hợp lệ.")
    private String email;
    
    @NotBlank(message = "Mật khẩu là bắt buộc.")
    private String password;
    
    @NotBlank(message = "CAPTCHA là bắt buộc.")
    private String captchaToken;
}
