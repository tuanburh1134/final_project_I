package com.example.financeapp.budget.entity;

import com.example.financeapp.category.entity.Category;
import com.example.financeapp.user.entity.User;
import com.example.financeapp.wallet.entity.Wallet;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "budgets")
public class Budget {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "budget_id")
    private Long budgetId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "wallet_id")
    private Wallet wallet; // null = áp dụng cho tất cả ví

    @Column(name = "amount_limit", nullable = false, precision = 15, scale = 2)
    private BigDecimal amountLimit;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "note", length = 255)
    private String note;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    @Column(name = "warning_threshold_percent", precision = 5, scale = 2)
    private BigDecimal warningThresholdPercent = BigDecimal.valueOf(20);

    @Column(name = "warning_alert_sent")
    private boolean warningAlertSent = false;

    @Column(name = "over_limit_alert_sent")
    private boolean overLimitAlertSent = false;

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // Getters & Setters
    public Long getBudgetId() { return budgetId; }
    public void setBudgetId(Long budgetId) { this.budgetId = budgetId; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public Category getCategory() { return category; }
    public void setCategory(Category category) { this.category = category; }

    public Wallet getWallet() { return wallet; }
    public void setWallet(Wallet wallet) { this.wallet = wallet; }

    public BigDecimal getAmountLimit() { return amountLimit; }
    public void setAmountLimit(BigDecimal amountLimit) { this.amountLimit = amountLimit; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public BigDecimal getWarningThresholdPercent() {
        return warningThresholdPercent;
    }

    public void setWarningThresholdPercent(BigDecimal warningThresholdPercent) {
        this.warningThresholdPercent = warningThresholdPercent;
    }

    public boolean isWarningAlertSent() {
        return warningAlertSent;
    }

    public void setWarningAlertSent(boolean warningAlertSent) {
        this.warningAlertSent = warningAlertSent;
    }

    public boolean isOverLimitAlertSent() {
        return overLimitAlertSent;
    }

    public void setOverLimitAlertSent(boolean overLimitAlertSent) {
        this.overLimitAlertSent = overLimitAlertSent;
    }
}