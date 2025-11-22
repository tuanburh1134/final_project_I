package com.example.financeapp.fund.entity;

import com.example.financeapp.user.entity.User;
import com.example.financeapp.wallet.entity.Wallet;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "funds")
public class Fund {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "fund_id")
    private Long fundId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "wallet_id", nullable = false)
    private Wallet wallet;

    @Column(name = "fund_name", nullable = false, length = 150)
    private String fundName;

    @Enumerated(EnumType.STRING)
    @Column(name = "fund_type", nullable = false, length = 20)
    private FundType fundType;

    @Enumerated(EnumType.STRING)
    @Column(name = "term_type", nullable = false, length = 20)
    private FundTermType termType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private FundStatus status = FundStatus.ACTIVE;

    @Column(name = "currency_code", length = 3, nullable = false)
    private String currencyCode;

    @Column(name = "target_amount", precision = 20, scale = 8)
    private BigDecimal targetAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "contribution_frequency", length = 20)
    private ContributionFrequency contributionFrequency = ContributionFrequency.NONE;

    @Column(name = "contribution_amount", precision = 20, scale = 8)
    private BigDecimal contributionAmount;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "note", length = 2000)
    private String note;

    @Column(name = "reminder_enabled", nullable = false)
    private boolean reminderEnabled = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "reminder_type", length = 20)
    private FundReminderType reminderType;

    @Column(name = "reminder_time")
    private LocalTime reminderTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "reminder_day_of_week", length = 10)
    private DayOfWeek reminderDayOfWeek;

    @Column(name = "reminder_day_of_month")
    private Integer reminderDayOfMonth;

    @Column(name = "reminder_month_of_year")
    private Integer reminderMonthOfYear;

    @Column(name = "auto_top_up_enabled", nullable = false)
    private boolean autoTopUpEnabled = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "auto_top_up_mode", length = 30)
    private AutoTopUpMode autoTopUpMode;

    @Enumerated(EnumType.STRING)
    @Column(name = "auto_top_up_schedule", length = 20)
    private AutoTopUpScheduleType autoTopUpScheduleType;

    @Column(name = "auto_top_up_time")
    private LocalTime autoTopUpTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "auto_top_up_day_of_week", length = 10)
    private DayOfWeek autoTopUpDayOfWeek;

    @Column(name = "auto_top_up_day_of_month")
    private Integer autoTopUpDayOfMonth;

    @Column(name = "auto_top_up_month_of_year")
    private Integer autoTopUpMonthOfYear;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "auto_top_up_wallet_id")
    private Wallet autoTopUpSourceWallet;

    @Column(name = "auto_top_up_amount", precision = 20, scale = 8)
    private BigDecimal autoTopUpAmount;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    @Column(name = "closed_at")
    private LocalDateTime closedAt;

    @OneToMany(mappedBy = "fund", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FundMember> members = new ArrayList<>();

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public void addMember(FundMember member) {
        this.members.add(member);
        member.setFund(this);
    }

    public void removeMember(FundMember member) {
        this.members.remove(member);
        member.setFund(null);
    }

    // Getters & Setters
    public Long getFundId() {
        return fundId;
    }

    public void setFundId(Long fundId) {
        this.fundId = fundId;
    }

    public User getOwner() {
        return owner;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }

    public Wallet getWallet() {
        return wallet;
    }

    public void setWallet(Wallet wallet) {
        this.wallet = wallet;
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

    public FundStatus getStatus() {
        return status;
    }

    public void setStatus(FundStatus status) {
        this.status = status;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public void setCurrencyCode(String currencyCode) {
        this.currencyCode = currencyCode;
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

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public boolean isReminderEnabled() {
        return reminderEnabled;
    }

    public void setReminderEnabled(boolean reminderEnabled) {
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

    public boolean isAutoTopUpEnabled() {
        return autoTopUpEnabled;
    }

    public void setAutoTopUpEnabled(boolean autoTopUpEnabled) {
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

    public Wallet getAutoTopUpSourceWallet() {
        return autoTopUpSourceWallet;
    }

    public void setAutoTopUpSourceWallet(Wallet autoTopUpSourceWallet) {
        this.autoTopUpSourceWallet = autoTopUpSourceWallet;
    }

    public BigDecimal getAutoTopUpAmount() {
        return autoTopUpAmount;
    }

    public void setAutoTopUpAmount(BigDecimal autoTopUpAmount) {
        this.autoTopUpAmount = autoTopUpAmount;
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

    public LocalDateTime getClosedAt() {
        return closedAt;
    }

    public void setClosedAt(LocalDateTime closedAt) {
        this.closedAt = closedAt;
    }

    public List<FundMember> getMembers() {
        return members;
    }

    public void setMembers(List<FundMember> members) {
        this.members = members;
    }
}

