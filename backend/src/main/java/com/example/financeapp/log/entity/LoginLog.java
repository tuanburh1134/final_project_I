package com.example.financeapp.log.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity
@Table(name = "login_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * ID của user đăng nhập
     */
    @Column(name = "user_id", nullable = false)
    private Long userId;

    /**
     * Địa chỉ IP khi đăng nhập
     */
    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    /**
     * Thông tin thiết bị / trình duyệt
     */
    @Column(name = "user_agent", columnDefinition = "TEXT")
    private String userAgent;

    /**
     * Thời gian đăng nhập
     */
    @Column(name = "login_time", nullable = false, updatable = false)
    private Instant loginTime;

    @PrePersist
    public void prePersist() {
        if (loginTime == null) {
            loginTime = Instant.now();
        }
    }
}