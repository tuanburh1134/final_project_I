package com.example.financeapp.service.impl;

import com.example.financeapp.dto.LoginRequest;
import com.example.financeapp.dto.RegisterRequest;
import com.example.financeapp.service.AuthService;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements AuthService {

    @Override
    public String login(LoginRequest request) {
        // TODO: implement login logic
        // Note: AuthController đã xử lý login logic trực tiếp
        return "login from service";
    }

    @Override
    public String register(RegisterRequest request) {
        // TODO: implement register logic
        // Note: AuthController đã xử lý register logic trực tiếp
        return "register from service";
    }
}

