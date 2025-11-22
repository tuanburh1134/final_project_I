package com.example.financeapp.fund.dto;

import com.example.financeapp.fund.entity.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public class CreateFundRequest {

    @NotBlank(message = "Tên quỹ không được để trống")
    @Size(max = 150, message = "Tên quỹ tối đa 150 ký tự")
    private String fundName;

    @NotNull(message = "Loại quỹ không hợp lệ")
    private FundType fundType;

    @NotNull(message = "Loại kỳ hạn không hợp lệ")
    private FundTermType termType;

    @NotNull(message = "Vui lòng chọn ví đích của quỹ")
    private Long walletId;

    @DecimalMin(value = "0.01", message = "Số tiền mục tiêu phải lớn hơn 0")
    private BigDecimal targetAmount;

    private ContributionFrequency contributionFrequency = ContributionFrequency.NONE;

    @DecimalMin(value = "0.00", message = "Số tiền mỗi kỳ phải lớn hơn hoặc bằng 0")
    private BigDecimal contributionAmount;

    @NotNull(message = "Ngày bắt đầu quỹ không được để trống")
    private LocalDate startDate;

    private LocalDate endDate;

    private Boolean reminderEnabled = Boolean.FALSE;
    private FundReminderType reminderType;
    private LocalTime reminderTime;
    private DayOfWeek reminderDayOfWeek;
    private Integer reminderDayOfMonth;
    private Integer reminderMonthOfYear;

    private Boolean autoTopUpEnabled = Boolean.FALSE;
    private AutoTopUpMode autoTopUpMode;
    private AutoTopUpScheduleType autoTopUpScheduleType;
    private LocalTime autoTopUpTime;
    private DayOfWeek autoTopUpDayOfWeek;
    private Integer autoTopUpDayOfMonth;
    private Integer autoTopUpMonthOfYear;
    private Long autoTopUpSourceWalletId;

    @DecimalMin(value = "0.01", message = "Số tiền tự động nạp phải lớn hơn 0")
    private BigDecimal autoTopUpAmount;

    private String note;

    @Valid
    private List<FundMemberRequest> members;

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

    public ContributionFrequency getContributionFrequency() {
        return contributionFrequency;
    }

    public void setContributionFrequency(ContributionFrequency contributionFrequency) {
        this.contributionFrequency = contributionFrequency;
    }

    public BigDecimal getContributionAmount() {
        return contributionAmount;
    }

    public void setContributionAmount(BigDecimal contributionAmount) {
        this.contributionAmount = contributionAmount;
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

    public Boolean getReminderEnabled() {
        return reminderEnabled;
    }

    public void setReminderEnabled(Boolean reminderEnabled) {
        this.reminderEnabled = reminderEnabled;
    }

    public FundReminderType getReminderType() {
        return reminderType;
    }

    public void setReminderType(FundReminderType reminderType) {
        this.reminderType = reminderType;
    }

    public LocalTime getReminderTime() {
        return reminderTime;
    }

    public void setReminderTime(LocalTime reminderTime) {
        this.reminderTime = reminderTime;
    }

    public DayOfWeek getReminderDayOfWeek() {
        return reminderDayOfWeek;
    }

    public void setReminderDayOfWeek(DayOfWeek reminderDayOfWeek) {
        this.reminderDayOfWeek = reminderDayOfWeek;
    }

    public Integer getReminderDayOfMonth() {
        return reminderDayOfMonth;
    }

    public void setReminderDayOfMonth(Integer reminderDayOfMonth) {
        this.reminderDayOfMonth = reminderDayOfMonth;
    }

    public Integer getReminderMonthOfYear() {
        return reminderMonthOfYear;
    }

    public void setReminderMonthOfYear(Integer reminderMonthOfYear) {
        this.reminderMonthOfYear = reminderMonthOfYear;
    }

    public Boolean getAutoTopUpEnabled() {
        return autoTopUpEnabled;
    }

    public void setAutoTopUpEnabled(Boolean autoTopUpEnabled) {
        this.autoTopUpEnabled = autoTopUpEnabled;
    }

    public AutoTopUpMode getAutoTopUpMode() {
        return autoTopUpMode;
    }

    public void setAutoTopUpMode(AutoTopUpMode autoTopUpMode) {
        this.autoTopUpMode = autoTopUpMode;
    }

    public AutoTopUpScheduleType getAutoTopUpScheduleType() {
        return autoTopUpScheduleType;
    }

    public void setAutoTopUpScheduleType(AutoTopUpScheduleType autoTopUpScheduleType) {
        this.autoTopUpScheduleType = autoTopUpScheduleType;
    }

    public LocalTime getAutoTopUpTime() {
        return autoTopUpTime;
    }

    public void setAutoTopUpTime(LocalTime autoTopUpTime) {
        this.autoTopUpTime = autoTopUpTime;
    }

    public DayOfWeek getAutoTopUpDayOfWeek() {
        return autoTopUpDayOfWeek;
    }

    public void setAutoTopUpDayOfWeek(DayOfWeek autoTopUpDayOfWeek) {
        this.autoTopUpDayOfWeek = autoTopUpDayOfWeek;
    }

    public Integer getAutoTopUpDayOfMonth() {
        return autoTopUpDayOfMonth;
    }

    public void setAutoTopUpDayOfMonth(Integer autoTopUpDayOfMonth) {
        this.autoTopUpDayOfMonth = autoTopUpDayOfMonth;
    }

    public Integer getAutoTopUpMonthOfYear() {
        return autoTopUpMonthOfYear;
    }

    public void setAutoTopUpMonthOfYear(Integer autoTopUpMonthOfYear) {
        this.autoTopUpMonthOfYear = autoTopUpMonthOfYear;
    }

    public Long getAutoTopUpSourceWalletId() {
        return autoTopUpSourceWalletId;
    }

    public void setAutoTopUpSourceWalletId(Long autoTopUpSourceWalletId) {
        this.autoTopUpSourceWalletId = autoTopUpSourceWalletId;
    }

    public BigDecimal getAutoTopUpAmount() {
        return autoTopUpAmount;
    }

    public void setAutoTopUpAmount(BigDecimal autoTopUpAmount) {
        this.autoTopUpAmount = autoTopUpAmount;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public List<FundMemberRequest> getMembers() {
        return members;
    }

    public void setMembers(List<FundMemberRequest> members) {
        this.members = members;
    }
}

