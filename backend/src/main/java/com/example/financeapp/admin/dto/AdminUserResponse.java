package com.example.financeapp.admin.dto;

import com.example.financeapp.security.Role;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class AdminUserResponse {
    private Long id;
    private String fullName;
    private String email;
    private String avatar;
    private Role role;
    private boolean locked;
    private boolean googleAccount;
    private boolean firstLogin;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
