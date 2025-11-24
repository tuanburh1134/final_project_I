package com.example.financeapp.user.entity;

import com.example.financeapp.security.Role;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Column(name = "email", unique = true, nullable = false)
    private String email;

    @Column(name = "password_hash")
    private String passwordHash; // null nếu là OAuth2 user

    @Column(name = "provider")
    private String provider; // local / google / facebook

    // ===== Xác minh email =====
    @Column(name = "enabled")
    private boolean enabled = false;

    // ===== Avatar người dùng =====
    @Column(name = "avatar", columnDefinition = "MEDIUMTEXT")
    private String avatar;

    // ===== Quyền hệ thống =====
    @Enumerated(EnumType.STRING)
    @Column(name = "role")
    private Role role = Role.USER;

    @Column(name = "locked")
    private boolean locked = false;

    // ===== Google Login =====
    @Column(name = "google_account")
    private boolean googleAccount = false;

    @Column(name = "first_login")
    private boolean firstLogin = false;

    // ===== Quên mật khẩu =====
    @Column(name = "reset_token")
    private String resetToken;

    @Column(name = "reset_token_expired_at")
    private LocalDateTime resetTokenExpiredAt;

    // ===== Soft delete + hoạt động gần nhất =====
    @Column(name = "deleted", nullable = false)
    private boolean deleted = false;

    @Column(name = "last_active_at")
    private LocalDateTime lastActiveAt;

    // ===== Auditing =====
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    public void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.googleAccount) {
            this.firstLogin = true;
        }
    }

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // ===========================
    // GETTERS & SETTERS
    // ===========================
    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    // Compatibility getters/setters for legacy code
    public Long getId() {
        return userId;
    }

    public void setId(Long id) {
        this.userId = id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getPassword() {
        return passwordHash;
    }

    public void setPassword(String password) {
        this.passwordHash = password;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public boolean isLocked() {
        return locked;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    public boolean isGoogleAccount() {
        return googleAccount;
    }

    public void setGoogleAccount(boolean googleAccount) {
        this.googleAccount = googleAccount;
    }

    public boolean isFirstLogin() {
        return firstLogin;
    }

    public void setFirstLogin(boolean firstLogin) {
        this.firstLogin = firstLogin;
    }

    public String getResetToken() {
        return resetToken;
    }

    public void setResetToken(String resetToken) {
        this.resetToken = resetToken;
    }

    public LocalDateTime getResetTokenExpiredAt() {
        return resetTokenExpiredAt;
    }

    public void setResetTokenExpiredAt(LocalDateTime resetTokenExpiredAt) {
        this.resetTokenExpiredAt = resetTokenExpiredAt;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public LocalDateTime getLastActiveAt() {
        return lastActiveAt;
    }

    public void setLastActiveAt(LocalDateTime lastActiveAt) {
        this.lastActiveAt = lastActiveAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    // ===== Builder (for backward compatibility) =====
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long userId;
        private String fullName;
        private String email;
        private String password;
        private String provider;
        private boolean enabled;
        private String avatar;
        private Role role = Role.USER;
        private boolean locked;
        private boolean googleAccount;
        private boolean firstLogin;
        private String resetToken;
        private LocalDateTime resetTokenExpiredAt;
        private boolean deleted;
        private LocalDateTime lastActiveAt;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public Builder userId(Long userId) { this.userId = userId; return this; }
        public Builder fullName(String fullName) { this.fullName = fullName; return this; }
        public Builder email(String email) { this.email = email; return this; }
        public Builder password(String password) { this.password = password; return this; }
        public Builder provider(String provider) { this.provider = provider; return this; }
        public Builder enabled(boolean enabled) { this.enabled = enabled; return this; }
        public Builder avatar(String avatar) { this.avatar = avatar; return this; }
        public Builder role(Role role) { this.role = role; return this; }
        public Builder locked(boolean locked) { this.locked = locked; return this; }
        public Builder googleAccount(boolean googleAccount) { this.googleAccount = googleAccount; return this; }
        public Builder firstLogin(boolean firstLogin) { this.firstLogin = firstLogin; return this; }
        public Builder resetToken(String resetToken) { this.resetToken = resetToken; return this; }
        public Builder resetTokenExpiredAt(LocalDateTime resetTokenExpiredAt) { this.resetTokenExpiredAt = resetTokenExpiredAt; return this; }
        public Builder deleted(boolean deleted) { this.deleted = deleted; return this; }
        public Builder lastActiveAt(LocalDateTime lastActiveAt) { this.lastActiveAt = lastActiveAt; return this; }
        public Builder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }
        public Builder updatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; return this; }

        public User build() {
            User user = new User();
            user.setUserId(userId);
            user.setFullName(fullName);
            user.setEmail(email);
            if (password != null) {
                user.setPassword(password);
            }
            user.setProvider(provider);
            user.setEnabled(enabled);
            user.setAvatar(avatar);
            user.setRole(role);
            user.setLocked(locked);
            user.setGoogleAccount(googleAccount);
            user.setFirstLogin(firstLogin);
            user.setResetToken(resetToken);
            user.setResetTokenExpiredAt(resetTokenExpiredAt);
            user.setDeleted(deleted);
            user.setLastActiveAt(lastActiveAt);
            user.setCreatedAt(createdAt);
            user.setUpdatedAt(updatedAt);
            return user;
        }
    }
}
