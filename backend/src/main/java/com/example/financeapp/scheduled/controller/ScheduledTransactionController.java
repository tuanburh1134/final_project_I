package com.example.financeapp.scheduled.controller;

import com.example.financeapp.scheduled.dto.CreateScheduledTransactionRequest;
import com.example.financeapp.scheduled.dto.ScheduledTransactionResponse;
import com.example.financeapp.scheduled.service.ScheduledTransactionService;
import com.example.financeapp.security.CustomUserDetails;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/scheduled-transactions")
public class ScheduledTransactionController {

    @Autowired
    private ScheduledTransactionService scheduledTransactionService;

    @PostMapping
    public ResponseEntity<Map<String, Object>> createSchedule(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody CreateScheduledTransactionRequest request
    ) {
        Map<String, Object> res = new HashMap<>();
        try {
            Long userId = requireUserId(userDetails);
            ScheduledTransactionResponse schedule = scheduledTransactionService.createSchedule(userId, request);
            res.put("message", "Tạo lịch giao dịch thành công");
            res.put("schedule", schedule);
            return ResponseEntity.ok(res);
        } catch (RuntimeException e) {
            res.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(res);
        } catch (Exception e) {
            res.put("error", "Lỗi hệ thống: " + e.getMessage());
            return ResponseEntity.status(500).body(res);
        }
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> getSchedules(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Map<String, Object> res = new HashMap<>();
        try {
            Long userId = requireUserId(userDetails);
            List<ScheduledTransactionResponse> schedules = scheduledTransactionService.getSchedules(userId);
            res.put("schedules", schedules);
            res.put("total", schedules.size());
            return ResponseEntity.ok(res);
        } catch (RuntimeException e) {
            res.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(res);
        } catch (Exception e) {
            res.put("error", "Lỗi hệ thống: " + e.getMessage());
            return ResponseEntity.status(500).body(res);
        }
    }

    @DeleteMapping("/{scheduleId}")
    public ResponseEntity<Map<String, Object>> cancelSchedule(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long scheduleId
    ) {
        Map<String, Object> res = new HashMap<>();
        try {
            Long userId = requireUserId(userDetails);
            scheduledTransactionService.cancelSchedule(userId, scheduleId);
            res.put("message", "Đã hủy lịch giao dịch");
            return ResponseEntity.ok(res);
        } catch (RuntimeException e) {
            res.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(res);
        } catch (Exception e) {
            res.put("error", "Lỗi hệ thống: " + e.getMessage());
            return ResponseEntity.status(500).body(res);
        }
    }

    private Long requireUserId(CustomUserDetails userDetails) {
        if (userDetails == null || userDetails.getUser() == null) {
            throw new RuntimeException("Không tìm thấy thông tin người dùng");
        }
        if (userDetails.getUser().getUserId() == null) {
            throw new RuntimeException("Không tìm thấy mã người dùng");
        }
        return userDetails.getUser().getUserId();
    }
}

