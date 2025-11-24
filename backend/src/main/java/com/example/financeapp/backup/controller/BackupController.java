package com.example.financeapp.backup.controller;

import com.example.financeapp.backup.dto.BackupStatusResponse;
import com.example.financeapp.backup.service.BackupService;
import com.example.financeapp.security.CustomUserDetails;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/backup")
public class BackupController {

    private final BackupService backupService;

    public BackupController(BackupService backupService) {
        this.backupService = backupService;
    }

    @PostMapping("/me")
    public ResponseEntity<Map<String, Object>> backupCurrentUser(@AuthenticationPrincipal CustomUserDetails userDetails) {
        Long userId = requireUserId(userDetails);
        Map<String, Object> response = new HashMap<>();
        backupService.backupUser(userId);
        response.put("message", "Đã tạo bản sao lưu mới cho tài khoản của bạn.");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/status")
    public ResponseEntity<BackupStatusResponse> getStatus(@AuthenticationPrincipal CustomUserDetails userDetails) {
        Long userId = requireUserId(userDetails);
        return ResponseEntity.ok(backupService.getStatus(userId));
    }

    private Long requireUserId(CustomUserDetails userDetails) {
        if (userDetails == null || userDetails.getUser() == null || userDetails.getUser().getUserId() == null) {
            throw new RuntimeException("Không tìm thấy thông tin người dùng");
        }
        return userDetails.getUser().getUserId();
    }
}

