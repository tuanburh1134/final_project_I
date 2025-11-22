package com.example.financeapp.backup.dto;

import java.time.LocalDateTime;

public class BackupStatusResponse {

    private LocalDateTime lastBackupAt;
    private String lastBackupLocation;
    private String lastStatus;
    private String lastError;

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

