package com.example.financeapp.dto;

import com.example.financeapp.entity.FundAutoTopupType;
import com.example.financeapp.entity.FundReminderType;
import com.example.financeapp.entity.FundTermType;
import com.example.financeapp.entity.FundType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class CreateFundRequest {

    @NotBlank(message = "Tên quỹ không được để trống")
    @Size(max = 150)
    private String fundName;

    @NotNull(message = "Loại quỹ không được để trống")
    private FundType fundType;

    @NotNull(message = "Loại kỳ hạn không được để trống")
    private FundTermType termType;

    @NotNull(message = "Vui lòng chọn ví để gắn với quỹ")
    private Long walletId;

    @DecimalMin(value = "0.0", inclusive = false, message = "Số tiền mục tiêu phải lớn hơn 0")
    private BigDecimal targetAmount;

    @Size(max = 500)
    private String description;

    private LocalDate startDate;

    private LocalDate endDate;

    private String frequency;

    @DecimalMin(value = "0.0", inclusive = false, message = "Số tiền gửi mỗi kỳ phải lớn hơn 0")
    private BigDecimal amountPerCycle;

    private FundReminderType reminderType = FundReminderType.NONE;

    private String reminderTime;

    private FundAutoTopupType autoTopupType = FundAutoTopupType.NONE;

    private String autoTopupConfig;

    private String notes;

    private List<String> memberEmails;

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

    public Long getWalletId() {
        return walletId;
    }

    public void setWalletId(Long walletId) {
        this.walletId = walletId;
    }

    public BigDecimal getTargetAmount() {
        return targetAmount;
    }

    public void setTargetAmount(BigDecimal targetAmount) {
        this.targetAmount = targetAmount;
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

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public List<String> getMemberEmails() {
        return memberEmails;
    }

    public void setMemberEmails(List<String> memberEmails) {
        this.memberEmails = memberEmails;
    }
}

