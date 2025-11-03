package com.example.financeapp.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@Table(name = "PasswordResetTokens")
public class PasswordResetToken {
    private static final int EXPIRATION_MINUTES = 15;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true, nullable = false, length = 500)
    private String token;
    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(nullable = false, name = "user_id")
    private User user;
    private LocalDateTime expiryDate = LocalDateTime.now().plusMinutes(EXPIRATION_MINUTES);

    public PasswordResetToken(String token, User user) {
        this.token = token;
        this.user = user;
    }
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiryDate);
    }
}