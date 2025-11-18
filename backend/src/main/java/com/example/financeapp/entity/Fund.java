package com.example.financeapp.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "funds")
public class Fund {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "fund_id")
    private Long fundId;

    @Column(name = "fund_name", nullable = false, length = 150)
    private String fundName;

    @Column(name = "description", length = 500)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "fund_type", nullable = false, length = 20)
    private FundType fundType;

    @Enumerated(EnumType.STRING)
    @Column(name = "term_type", nullable = false, length = 20)
    private FundTermType termType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "wallet_id", nullable = false, unique = true)
    private Wallet wallet;

    @Column(name = "currency_code", nullable = false, length = 3)
    private String currencyCode;

    @Column(name = "target_amount", precision = 15, scale = 2)
    private BigDecimal targetAmount;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "frequency", length = 50)
    private String frequency;

    @Column(name = "amount_per_cycle", precision = 15, scale = 2)
    private BigDecimal amountPerCycle;

    @Enumerated(EnumType.STRING)
    @Column(name = "reminder_type", length = 30)
    private FundReminderType reminderType = FundReminderType.NONE;

    @Column(name = "reminder_time", length = 10)
    private String reminderTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "auto_topup_type", length = 30)
    private FundAutoTopupType autoTopupType = FundAutoTopupType.NONE;

    @Column(name = "auto_topup_config", length = 255)
    private String autoTopupConfig;

    @Column(name = "notes", length = 1000)
    private String notes;

    @Column(name = "is_closed", nullable = false)
    private boolean closed = false;

    @Column(name = "closed_at")
    private LocalDateTime closedAt;

    @Column(name = "is_deleted", nullable = false)
    private boolean deleted = false;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    @OneToMany(mappedBy = "fund", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<FundMember> fundMembers = new HashSet<>();

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (reminderType == null) {
            reminderType = FundReminderType.NONE;
        }
        if (autoTopupType == null) {
            autoTopupType = FundAutoTopupType.NONE;
        }
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
        if (deleted && deletedAt == null) {
            deletedAt = LocalDateTime.now();
        }
    }

    public LocalDateTime getDeletedAt() {
        return deletedAt;
    }

    public void setDeletedAt(LocalDateTime deletedAt) {
        this.deletedAt = deletedAt;
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

    public Set<FundMember> getFundMembers() {
        return fundMembers;
    }

    public void setFundMembers(Set<FundMember> fundMembers) {
        this.fundMembers = fundMembers;
    }
}

