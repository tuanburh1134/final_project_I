package com.example.financeapp.transaction.controller;

import com.example.financeapp.transaction.dto.ScheduleLogResponse;
import com.example.financeapp.transaction.dto.ScheduledTransactionRequest;
import com.example.financeapp.transaction.dto.ScheduledTransactionResponse;
import com.example.financeapp.transaction.service.ScheduledTransactionService;
import com.example.financeapp.user.entity.User;
import com.example.financeapp.user.repository.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/transactions/schedules")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class ScheduledTransactionController {

    private final ScheduledTransactionService scheduledTransactionService;
    private final UserRepository userRepository;

    private Long getCurrentUserId() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .map(User::getUserId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy user"));
    }

    @PostMapping
    public ResponseEntity<ScheduledTransactionResponse> createSchedule(
            @Valid @RequestBody ScheduledTransactionRequest request) {
        Long userId = getCurrentUserId();
        ScheduledTransactionResponse response = scheduledTransactionService.createSchedule(userId, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<ScheduledTransactionResponse>> getSchedules() {
        Long userId = getCurrentUserId();
        return ResponseEntity.ok(scheduledTransactionService.getSchedules(userId));
    }

    @GetMapping("/{scheduleId}/logs")
    public ResponseEntity<List<ScheduleLogResponse>> getLogs(@PathVariable Long scheduleId) {
        Long userId = getCurrentUserId();
        return ResponseEntity.ok(scheduledTransactionService.getScheduleLogs(userId, scheduleId));
    }

    @PostMapping("/{scheduleId}/cancel")
    public ResponseEntity<Map<String, Object>> cancelSchedule(@PathVariable Long scheduleId) {
        Long userId = getCurrentUserId();
        scheduledTransactionService.cancelSchedule(userId, scheduleId);
        Map<String, Object> res = new HashMap<>();
        res.put("message", "Đã hủy lịch giao dịch");
        return ResponseEntity.ok(res);
    }
}

