package com.example.financeapp.scheduled.entity;

import com.example.financeapp.scheduled.entity.ScheduledTransaction.ScheduleStatus;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "scheduled_transaction_logs")
public class ScheduledTransactionLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "log_id")
    private Long logId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "schedule_id", nullable = false)
    private ScheduledTransaction scheduledTransaction;

    @Column(name = "run_at", nullable = false)
    private LocalDateTime runAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private ScheduleStatus status;

    @Column(name = "amount", precision = 20, scale = 8)
    private BigDecimal amount;

    @Column(name = "message", length = 500)
    private String message;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public Long getLogId() {
        return logId;
    }

    public void setLogId(Long logId) {
        this.logId = logId;
    }

    public ScheduledTransaction getScheduledTransaction() {
        return scheduledTransaction;
    }

    public void setScheduledTransaction(ScheduledTransaction scheduledTransaction) {
        this.scheduledTransaction = scheduledTransaction;
    }

    public LocalDateTime getRunAt() {
        return runAt;
    }

    public void setRunAt(LocalDateTime runAt) {
        this.runAt = runAt;
    }

    public ScheduleStatus getStatus() {
        return status;
    }

    public void setStatus(ScheduleStatus status) {
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}

