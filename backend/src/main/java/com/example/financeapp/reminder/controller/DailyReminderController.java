package com.example.financeapp.reminder.controller;

import com.example.financeapp.reminder.dto.DailyReminderRequest;
import com.example.financeapp.reminder.dto.DailyReminderResponse;
import com.example.financeapp.reminder.service.DailyReminderService;
import com.example.financeapp.security.CustomUserDetails;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/reminders")
public class DailyReminderController {

    @Autowired
    private DailyReminderService reminderService;

    @GetMapping
    public ResponseEntity<DailyReminderResponse> getReminder(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long userId = requireUserId(userDetails);
        return ResponseEntity.ok(reminderService.getCurrentSetting(userId));
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> upsertReminder(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody DailyReminderRequest request
    ) {
        Long userId = requireUserId(userDetails);
        DailyReminderResponse response = reminderService.upsertSetting(userId, request);
        Map<String, Object> res = new HashMap<>();
        res.put("message", "Cập nhật nhắc nhở thành công");
        res.put("reminder", response);
        return ResponseEntity.ok(res);
    }

    private Long requireUserId(CustomUserDetails userDetails) {
        if (userDetails == null || userDetails.getUser() == null || userDetails.getUser().getUserId() == null) {
            throw new RuntimeException("Không tìm thấy thông tin người dùng");
        }
        return userDetails.getUser().getUserId();
    }
}

