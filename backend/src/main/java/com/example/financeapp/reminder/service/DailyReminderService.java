package com.example.financeapp.reminder.service;

import com.example.financeapp.reminder.dto.DailyReminderRequest;
import com.example.financeapp.reminder.dto.DailyReminderResponse;

public interface DailyReminderService {
    DailyReminderResponse getCurrentSetting(Long userId);
    DailyReminderResponse upsertSetting(Long userId, DailyReminderRequest request);
    void sendDailyReminders();
}

