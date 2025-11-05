package com.example.financeapp.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class RegisterRequest {

    @Email(message = "Email không hợp lệ.")
    @NotBlank(message = "Email là bắt buộc.")
    private String email;

    @NotBlank(message = "Mật khẩu là bắt buộc.")
    @Size(min = 8, message = "Mật khẩu phải có ít nhất 8 ký tự.")
    @Pattern(regexp = "^(?=.*[A-Z])(?=.*[!@#$&*])(?=\\S+$).{8,}$",
            message = "Mật khẩu phải chứa ít nhất 1 chữ hoa và 1 ký tự đặc biệt.")
    private String password;

    // Getters & Setters
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    @NotBlank(message = "Họ và tên là bắt buộc.")
    private String fullName;

    @NotBlank(message = "Tên đăng nhập là bắt buộc.")
    @Size(min = 3, message = "Tên đăng nhập phải có ít nhất 3 ký tự.")
    private String userName;
}