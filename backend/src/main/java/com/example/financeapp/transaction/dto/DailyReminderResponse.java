package com.example.financeapp.transaction.dto;

import java.time.LocalDateTime;

public class DailyReminderResponse {

    private boolean needsReminder;
    private String message;
    private LocalDateTime lastTransactionDate;

    public DailyReminderResponse() {
    }

    public DailyReminderResponse(boolean needsReminder, String message, LocalDateTime lastTransactionDate) {
        this.needsReminder = needsReminder;
        this.message = message;
        this.lastTransactionDate = lastTransactionDate;
    }

    public boolean isNeedsReminder() {
        return needsReminder;
    }

    public void setNeedsReminder(boolean needsReminder) {
        this.needsReminder = needsReminder;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public LocalDateTime getLastTransactionDate() {
        return lastTransactionDate;
    }

    public void setLastTransactionDate(LocalDateTime lastTransactionDate) {
        this.lastTransactionDate = lastTransactionDate;
    }
}

