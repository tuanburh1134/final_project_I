package com.example.financeapp.reminder.dto;

import com.example.financeapp.reminder.entity.DailyReminderSetting;

import java.time.LocalDateTime;
import java.time.LocalTime;

public class DailyReminderResponse {
    private Long reminderId;
    private boolean enabled;
    private LocalTime reminderTime;
    private boolean sendEmail;
    private boolean sendPush;
    private LocalDateTime lastSentAt;

    public static DailyReminderResponse fromEntity(DailyReminderSetting entity) {
        DailyReminderResponse resp = new DailyReminderResponse();
        resp.setReminderId(entity.getReminderId());
        resp.setEnabled(entity.isEnabled());
        resp.setReminderTime(entity.getReminderTime());
        resp.setSendEmail(entity.isSendEmail());
        resp.setSendPush(entity.isSendPush());
        resp.setLastSentAt(entity.getLastSentAt());
        return resp;
    }

    public Long getReminderId() {
        return reminderId;
    }

    public void setReminderId(Long reminderId) {
        this.reminderId = reminderId;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public LocalTime getReminderTime() {
        return reminderTime;
    }

    public void setReminderTime(LocalTime reminderTime) {
        this.reminderTime = reminderTime;
    }

    public boolean isSendEmail() {
        return sendEmail;
    }

    public void setSendEmail(boolean sendEmail) {
        this.sendEmail = sendEmail;
    }

    public boolean isSendPush() {
        return sendPush;
    }

    public void setSendPush(boolean sendPush) {
        this.sendPush = sendPush;
    }

    public LocalDateTime getLastSentAt() {
        return lastSentAt;
    }

    public void setLastSentAt(LocalDateTime lastSentAt) {
        this.lastSentAt = lastSentAt;
    }
}

