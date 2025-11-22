package com.example.financeapp.reminder.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalTime;

public class DailyReminderRequest {

    @NotNull(message = "Giờ nhắc nhở không được để trống")
    private LocalTime reminderTime;

    private boolean sendEmail = true;
    private boolean sendPush = false;

    private boolean enabled = true;

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

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}

