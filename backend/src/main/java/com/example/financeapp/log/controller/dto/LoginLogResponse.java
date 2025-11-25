package com.example.financeapp.log.controller.dto;

import com.example.financeapp.log.entity.LoginLog;
import lombok.Data;

import java.time.Instant;

@Data
public class LoginLogResponse {

    private Long id;
    private String ipAddress;
    private String userAgent;
    private Instant loginTime;

    public static LoginLogResponse fromEntity(LoginLog entity) {
        LoginLogResponse dto = new LoginLogResponse();
        dto.setId(entity.getId());
        dto.setIpAddress(entity.getIpAddress());
        dto.setUserAgent(entity.getUserAgent());
        dto.setLoginTime(entity.getLoginTime());
        return dto;
    }
}

