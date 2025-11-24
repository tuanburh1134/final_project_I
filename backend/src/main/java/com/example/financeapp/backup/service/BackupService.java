package com.example.financeapp.backup.service;

import com.example.financeapp.backup.dto.BackupStatusResponse;

public interface BackupService {

    void backupAllUsers();

    void backupUser(Long userId);

    BackupStatusResponse getStatus(Long userId);
}

