package com.example.financeapp.service;

import com.example.financeapp.dto.RegisterRequest;

public interface AuthService {
    void register(RegisterRequest request);
    void sendActivationEmail(String toEmail, String token);
    boolean verifyAccount(String token);
    void forgotPassword(String email);
    void resetPassword(String token, String newPassword, String confirmPassword);
}