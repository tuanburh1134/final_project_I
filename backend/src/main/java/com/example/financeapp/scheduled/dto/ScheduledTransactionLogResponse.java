package com.example.financeapp.scheduled.dto;

import com.example.financeapp.scheduled.entity.ScheduledTransactionLog;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class ScheduledTransactionLogResponse {

    private Long logId;
    private LocalDateTime runAt;
    private String status;
    private BigDecimal amount;
    private String message;

    public static ScheduledTransactionLogResponse fromEntity(ScheduledTransactionLog log) {
        ScheduledTransactionLogResponse resp = new ScheduledTransactionLogResponse();
        resp.setLogId(log.getLogId());
        resp.setRunAt(log.getRunAt());
        resp.setAmount(log.getAmount());
        resp.setMessage(log.getMessage());
        resp.setStatus(log.getStatus().name());
        return resp;
    }

    public Long getLogId() {
        return logId;
    }

    public void setLogId(Long logId) {
        this.logId = logId;
    }

    public LocalDateTime getRunAt() {
        return runAt;
    }

    public void setRunAt(LocalDateTime runAt) {
        this.runAt = runAt;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}

