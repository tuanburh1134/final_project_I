package com.example.financeapp.dto;

import com.example.financeapp.entity.FundAutoTopupType;
import com.example.financeapp.entity.FundReminderType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO dùng cho chỉnh sửa quỹ (chỉ cho phép một số trường).
 */
public class UpdateFundRequest {

    @Size(max = 150)
    private String fundName;

    private String frequency;

    @DecimalMin(value = "0.0", inclusive = false)
    private BigDecimal amountPerCycle;

    private LocalDate startDate;

    private LocalDate endDate;

    private String notes;

    private FundReminderType reminderType;

    private String reminderTime;

    private FundAutoTopupType autoTopupType;

    private String autoTopupConfig;

    private java.util.List<String> memberEmailsToAdd;

    private java.util.List<Long> memberIdsToRemove;

    public String getFundName() {
        return fundName;
    }

    public void setFundName(String fundName) {
        this.fundName = fundName;
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

    public java.util.List<String> getMemberEmailsToAdd() {
        return memberEmailsToAdd;
    }

    public void setMemberEmailsToAdd(java.util.List<String> memberEmailsToAdd) {
        this.memberEmailsToAdd = memberEmailsToAdd;
    }

    public java.util.List<Long> getMemberIdsToRemove() {
        return memberIdsToRemove;
    }

    public void setMemberIdsToRemove(java.util.List<Long> memberIdsToRemove) {
        this.memberIdsToRemove = memberIdsToRemove;
    }
}

