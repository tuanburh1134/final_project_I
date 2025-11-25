package com.example.financeapp.user.entity;

import com.example.financeapp.security.Role;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")               // ✅ Rõ ràng: map với cột user_id trong DB
    private Long id;

    // ========================
    // Thông tin cơ bản
    // ========================
    @Column(name = "full_name", length = 100)
    private String fullName;

    @Column(unique = true, nullable = false, length = 150)
    private String email;

    private String password;

    // ========================
    // Avatar — BASE64 → LONGTEXT
    // ========================
    @Column(name = "avatar", columnDefinition = "LONGTEXT")
    private String avatar;

    // ========================
    // Quyền hệ thống
    // ========================
    @Enumerated(EnumType.STRING)
    private Role role;

    private boolean locked;

    // ========================
    // Google Login
    // ========================
    @Column(name = "google_account")
    private boolean googleAccount;

    @Column(name = "first_login")
    private boolean firstLogin;

    // ========================
    // Quên mật khẩu
    // ========================
    private String resetToken;
    private LocalDateTime resetTokenExpiredAt;

    // ========================
    // Soft delete + hoạt động gần nhất
    // ========================
    @Builder.Default
    @Column(name = "deleted", nullable = false)
    private boolean deleted = false;

    @Column(name = "last_active_at")
    private LocalDateTime lastActiveAt;

    // ========================
    // Auditing
    // ========================
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    public void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.firstLogin = googleAccount;
    }

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // ========================
    // Hỗ trợ tạm cho code cũ dùng userId
    // ========================
    public Long getUserId() {
        return this.id;
    }

    public void setUserId(Long userId) {
        this.id = userId;
    }
}
