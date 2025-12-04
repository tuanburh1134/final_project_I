package com.example.financeapp.service;

import com.example.financeapp.dto.LoginRequest;
import com.example.financeapp.dto.RegisterRequest;
// smart checkout 
public interface AuthService {
    String login(LoginRequest request);
    String register(RegisterRequest request);
}
