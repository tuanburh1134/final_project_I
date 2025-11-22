package com.example.financeapp.backup.repository;

import com.example.financeapp.backup.entity.UserBackupStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserBackupStatusRepository extends JpaRepository<UserBackupStatus, Long> {
    Optional<UserBackupStatus> findByUser_UserId(Long userId);
}

