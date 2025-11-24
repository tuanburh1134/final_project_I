package com.example.financeapp.backup.entity;

import com.example.financeapp.user.entity.User;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_backup_status")
public class UserBackupStatus {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "last_backup_at")
    private LocalDateTime lastBackupAt;

    @Column(name = "last_backup_location", length = 500)
    private String lastBackupLocation;

    @Column(name = "last_status", length = 50)
    private String lastStatus;

    @Column(name = "last_error", length = 2000)
    private String lastError;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public LocalDateTime getLastBackupAt() {
        return lastBackupAt;
    }

    public void setLastBackupAt(LocalDateTime lastBackupAt) {
        this.lastBackupAt = lastBackupAt;
    }

    public String getLastBackupLocation() {
        return lastBackupLocation;
    }

    public void setLastBackupLocation(String lastBackupLocation) {
        this.lastBackupLocation = lastBackupLocation;
    }

    public String getLastStatus() {
        return lastStatus;
    }

    public void setLastStatus(String lastStatus) {
        this.lastStatus = lastStatus;
    }

    public String getLastError() {
        return lastError;
    }

    public void setLastError(String lastError) {
        this.lastError = lastError;
    }
}

