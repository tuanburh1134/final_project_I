package com.example.financeapp.security;



import com.fasterxml.jackson.databind.ObjectMapper;
import com.example.financeapp.exception.ApiErrorCode;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.*;

@Component
public class JwtAuthEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException
    ) throws IOException, ServletException {

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("success", false);
        body.put("code", ApiErrorCode.ACCESS_DENIED.name());
        body.put("message", "Bạn cần đăng nhập để sử dụng chức năng này");
        body.put("timestamp", new Date());

        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType("application/json;charset=UTF-8");
        objectMapper.writeValue(response.getWriter(), body);
    }
}
