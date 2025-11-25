package com.example.financeapp.admin.dto;

import com.example.financeapp.security.Role;
import com.example.financeapp.user.entity.User;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AdminUserDetailResponse {

    private Long id;
    private String email;
    private String fullName;
    private Role role;
    private boolean locked;
    private boolean googleAccount;
    private boolean firstLogin;
    private LocalDateTime createdAt;   // ngày tạo tài khoản

    public static AdminUserDetailResponse fromEntity(User user) {
        AdminUserDetailResponse dto = new AdminUserDetailResponse();
        dto.setId(user.getUserId());                 // ✔ Sửa tại đây
        dto.setEmail(user.getEmail());
        dto.setFullName(user.getFullName());
        dto.setRole(user.getRole());
        dto.setLocked(user.isLocked());
        dto.setGoogleAccount(user.isGoogleAccount());
        dto.setFirstLogin(user.isFirstLogin());
        dto.setCreatedAt(user.getCreatedAt());
        return dto;
    }
}