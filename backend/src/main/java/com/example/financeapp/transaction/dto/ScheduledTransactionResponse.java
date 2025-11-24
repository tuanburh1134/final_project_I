package com.example.financeapp.transaction.dto;

import com.example.financeapp.transaction.schedule.ScheduleFrequency;
import com.example.financeapp.transaction.schedule.ScheduleStatus;
import com.example.financeapp.transaction.schedule.ScheduledTransactionType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class ScheduledTransactionResponse {

    private Long scheduleId;
    private ScheduledTransactionType transactionType;
    private ScheduleFrequency frequency;
    private ScheduleStatus status;
    private BigDecimal amount;
    private String note;
    private Long walletId;
    private String walletName;
    private Long categoryId;
    private String categoryName;
    private LocalDateTime nextRunAt;
    private LocalDate endDate;
    private int completedOccurrences;
    private LocalDateTime lastRunAt;
    private LocalDateTime createdAt;

    public Long getScheduleId() {
        return scheduleId;
    }

    public void setScheduleId(Long scheduleId) {
        this.scheduleId = scheduleId;
    }

    public ScheduledTransactionType getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(ScheduledTransactionType transactionType) {
        this.transactionType = transactionType;
    }

    public ScheduleFrequency getFrequency() {
        return frequency;
    }

    public void setFrequency(ScheduleFrequency frequency) {
        this.frequency = frequency;
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

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public Long getWalletId() {
        return walletId;
    }

    public void setWalletId(Long walletId) {
        this.walletId = walletId;
    }

    public String getWalletName() {
        return walletName;
    }

    public void setWalletName(String walletName) {
        this.walletName = walletName;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public LocalDateTime getNextRunAt() {
        return nextRunAt;
    }

    public void setNextRunAt(LocalDateTime nextRunAt) {
        this.nextRunAt = nextRunAt;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public int getCompletedOccurrences() {
        return completedOccurrences;
    }

    public void setCompletedOccurrences(int completedOccurrences) {
        this.completedOccurrences = completedOccurrences;
    }

    public LocalDateTime getLastRunAt() {
        return lastRunAt;
    }

    public void setLastRunAt(LocalDateTime lastRunAt) {
        this.lastRunAt = lastRunAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}

