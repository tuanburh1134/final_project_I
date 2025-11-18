package com.example.financeapp.dto;

import com.example.financeapp.entity.FundAutoTopupType;
import com.example.financeapp.entity.FundReminderType;
import com.example.financeapp.entity.FundTermType;
import com.example.financeapp.entity.FundType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class FundDetailResponse {
    private Long fundId;
    private String fundName;
    private FundType fundType;
    private FundTermType termType;
    private BigDecimal currentAmount;
    private BigDecimal targetAmount;
    private String currencyCode;
    private Long walletId;
    private double progress;
    private String description;
    private LocalDate startDate;
    private LocalDate endDate;
    private String frequency;
    private BigDecimal amountPerCycle;
    private boolean closed;
    private LocalDateTime closedAt;
    private String notes;
    private FundReminderType reminderType;
    private String reminderTime;
    private FundAutoTopupType autoTopupType;
    private String autoTopupConfig;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<FundMemberSummaryDTO> members = new ArrayList<>();

    public Long getFundId() {
        return fundId;
    }

    public void setFundId(Long fundId) {
        this.fundId = fundId;
    }

    public String getFundName() {
        return fundName;
    }

    public void setFundName(String fundName) {
        this.fundName = fundName;
    }

    public FundType getFundType() {
        return fundType;
    }

    public void setFundType(FundType fundType) {
        this.fundType = fundType;
    }

    public FundTermType getTermType() {
        return termType;
    }

    public void setTermType(FundTermType termType) {
        this.termType = termType;
    }

    public BigDecimal getCurrentAmount() {
        return currentAmount;
    }

    public void setCurrentAmount(BigDecimal currentAmount) {
        this.currentAmount = currentAmount;
    }

    public BigDecimal getTargetAmount() {
        return targetAmount;
    }

    public void setTargetAmount(BigDecimal targetAmount) {
        this.targetAmount = targetAmount;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public void setCurrencyCode(String currencyCode) {
        this.currencyCode = currencyCode;
    }

    public double getProgress() {
        return progress;
    }

    public void setProgress(double progress) {
        this.progress = progress;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public String getFrequency() {
        return frequency;
    }

    public void setFrequency(String frequency) {
        this.frequency = frequency;
    }

    public BigDecimal getAmountPerCycle() {
        return amountPerCycle;
    }

    public void setAmountPerCycle(BigDecimal amountPerCycle) {
        this.amountPerCycle = amountPerCycle;
    }

    public boolean isClosed() {
        return closed;
    }

    public void setClosed(boolean closed) {
        this.closed = closed;
    }

    public LocalDateTime getClosedAt() {
        return closedAt;
    }

    public void setClosedAt(LocalDateTime closedAt) {
        this.closedAt = closedAt;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public FundReminderType getReminderType() {
        return reminderType;
    }

    public void setReminderType(FundReminderType reminderType) {
        this.reminderType = reminderType;
    }

    public String getReminderTime() {
        return reminderTime;
    }

    public void setReminderTime(String reminderTime) {
        this.reminderTime = reminderTime;
    }

    public FundAutoTopupType getAutoTopupType() {
        return autoTopupType;
    }

    public void setAutoTopupType(FundAutoTopupType autoTopupType) {
        this.autoTopupType = autoTopupType;
    }

    public String getAutoTopupConfig() {
        return autoTopupConfig;
    }

    public void setAutoTopupConfig(String autoTopupConfig) {
        this.autoTopupConfig = autoTopupConfig;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public List<FundMemberSummaryDTO> getMembers() {
        return members;
    }

    public void setMembers(List<FundMemberSummaryDTO> members) {
        this.members = members;
    }

    public Long getWalletId() {
        return walletId;
    }

    public void setWalletId(Long walletId) {
        this.walletId = walletId;
    }
}

