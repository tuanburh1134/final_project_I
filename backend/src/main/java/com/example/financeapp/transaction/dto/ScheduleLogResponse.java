package com.example.financeapp.transaction.dto;

import com.example.financeapp.transaction.schedule.ScheduleStatus;

import java.time.LocalDateTime;

public class ScheduleLogResponse {

    private Long logId;
    private ScheduleStatus status;
    private String message;
    private LocalDateTime executedAt;

    public Long getLogId() {
        return logId;
    }

    public void setLogId(Long logId) {
        this.logId = logId;
    }

    public ScheduleStatus getStatus() {
        return status;
    }

    public void setStatus(ScheduleStatus status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public LocalDateTime getExecutedAt() {
        return executedAt;
    }

    public void setExecutedAt(LocalDateTime executedAt) {
        this.executedAt = executedAt;
    }
}

