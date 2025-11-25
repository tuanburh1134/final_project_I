package com.example.financeapp.admin.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class AdminActionLogResponse {
    private Long id;
    private Long adminId;
    private String adminEmail;
    private Long targetUserId;
    private String action;
    private String detail;
    private LocalDateTime createdAt;
}


