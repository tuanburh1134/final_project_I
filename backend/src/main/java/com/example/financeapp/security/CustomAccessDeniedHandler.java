package com.example.financeapp.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.example.financeapp.exception.ApiErrorCode;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.*;

@Component
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void handle(
            HttpServletRequest request,
            HttpServletResponse response,
            AccessDeniedException accessDeniedException
    ) throws IOException, ServletException {

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("success", false);
        body.put("code", ApiErrorCode.ACCESS_DENIED.name());
        body.put("message", "Bạn không có quyền truy cập tài nguyên này");
        body.put("timestamp", new Date());

        response.setStatus(HttpStatus.FORBIDDEN.value());
        response.setContentType("application/json;charset=UTF-8");
        objectMapper.writeValue(response.getWriter(), body);
    }
}
