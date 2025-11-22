package com.example.financeapp.scheduled.dto;

import com.example.financeapp.scheduled.entity.ScheduledTransaction;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class ScheduledTransactionResponse {
    private Long scheduleId;
    private Long walletId;
    private String walletName;
    private Long categoryId;
    private String categoryName;
    private Long transactionTypeId;
    private String transactionTypeName;
    private BigDecimal amount;
    private String note;
    private LocalDateTime scheduleTime;
    private String status;
    private LocalDateTime executedAt;
    private String failureReason;

    public static ScheduledTransactionResponse fromEntity(ScheduledTransaction st) {
        ScheduledTransactionResponse resp = new ScheduledTransactionResponse();
        resp.setScheduleId(st.getScheduleId());
        if (st.getWallet() != null) {
            resp.setWalletId(st.getWallet().getWalletId());
            resp.setWalletName(st.getWallet().getWalletName());
        }
        if (st.getCategory() != null) {
            resp.setCategoryId(st.getCategory().getCategoryId());
            resp.setCategoryName(st.getCategory().getCategoryName());
        }
        if (st.getTransactionType() != null) {
            resp.setTransactionTypeId(st.getTransactionType().getTypeId());
            resp.setTransactionTypeName(st.getTransactionType().getTypeName());
        }
        resp.setAmount(st.getAmount());
        resp.setNote(st.getNote());
        resp.setScheduleTime(st.getScheduleTime());
        resp.setStatus(st.getStatus().name());
        resp.setExecutedAt(st.getExecutedAt());
        resp.setFailureReason(st.getFailureReason());
        return resp;
    }

    // getters/setters

    public Long getScheduleId() {
        return scheduleId;
    }

    public void setScheduleId(Long scheduleId) {
        this.scheduleId = scheduleId;
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

    public Long getTransactionTypeId() {
        return transactionTypeId;
    }

    public void setTransactionTypeId(Long transactionTypeId) {
        this.transactionTypeId = transactionTypeId;
    }

    public String getTransactionTypeName() {
        return transactionTypeName;
    }

    public void setTransactionTypeName(String transactionTypeName) {
        this.transactionTypeName = transactionTypeName;
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

    public LocalDateTime getScheduleTime() {
        return scheduleTime;
    }

    public void setScheduleTime(LocalDateTime scheduleTime) {
        this.scheduleTime = scheduleTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getExecutedAt() {
        return executedAt;
    }

    public void setExecutedAt(LocalDateTime executedAt) {
        this.executedAt = executedAt;
    }

    public String getFailureReason() {
        return failureReason;
    }

    public void setFailureReason(String failureReason) {
        this.failureReason = failureReason;
    }
}

