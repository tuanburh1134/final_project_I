package com.example.financeapp.transaction.schedule;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "scheduled_transaction_logs")
public class ScheduledTransactionLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long logId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "schedule_id", nullable = false)
    private ScheduledTransaction scheduledTransaction;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ScheduleStatus status;

    @Column(name = "message", length = 500)
    private String message;

    @Column(name = "executed_at", nullable = false)
    private LocalDateTime executedAt = LocalDateTime.now();

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

