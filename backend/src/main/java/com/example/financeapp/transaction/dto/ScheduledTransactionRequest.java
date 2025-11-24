package com.example.financeapp.transaction.dto;

import com.example.financeapp.transaction.schedule.ScheduleFrequency;
import com.example.financeapp.transaction.schedule.ScheduledTransactionType;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class ScheduledTransactionRequest {

    @NotNull
    private ScheduledTransactionType transactionType;

    @NotNull
    private ScheduleFrequency frequency;

    @NotNull
    private Long walletId;

    @NotNull
    private Long categoryId;

    @Positive
    private BigDecimal amount;

    private String note;

    @NotNull
    @Future
    private LocalDateTime firstRunAt;

    private LocalDate endDate;

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

    public Long getWalletId() {
        return walletId;
    }

    public void setWalletId(Long walletId) {
        this.walletId = walletId;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
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

    public LocalDateTime getFirstRunAt() {
        return firstRunAt;
    }

    public void setFirstRunAt(LocalDateTime firstRunAt) {
        this.firstRunAt = firstRunAt;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }
}

