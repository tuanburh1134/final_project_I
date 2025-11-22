package com.example.financeapp.budget.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class BudgetSummaryResponse {
    private Long budgetId;
    private Long categoryId;
    private String categoryName;
    private Long walletId;
    private String walletName;
    private boolean appliesToAllWallets;
    private BigDecimal amountLimit;
    private BigDecimal spentAmount;
    private BigDecimal remainingAmount;
    private BigDecimal progressPercentage;
    private boolean overLimit;
    private BigDecimal overBudgetAmount;
    private boolean hasExceededBudget;
    private String budgetStatus;
    private boolean warningTriggered;
    private boolean overLimitAlertTriggered;
    private BigDecimal warningThresholdPercent;
    private LocalDate startDate;
    private LocalDate endDate;
    private String note;

    public Long getBudgetId() {
        return budgetId;
    }

    public void setBudgetId(Long budgetId) {
        this.budgetId = budgetId;
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

    public boolean isAppliesToAllWallets() {
        return appliesToAllWallets;
    }

    public void setAppliesToAllWallets(boolean appliesToAllWallets) {
        this.appliesToAllWallets = appliesToAllWallets;
    }

    public BigDecimal getAmountLimit() {
        return amountLimit;
    }

    public void setAmountLimit(BigDecimal amountLimit) {
        this.amountLimit = amountLimit;
    }

    public BigDecimal getSpentAmount() {
        return spentAmount;
    }

    public void setSpentAmount(BigDecimal spentAmount) {
        this.spentAmount = spentAmount;
    }

    public BigDecimal getRemainingAmount() {
        return remainingAmount;
    }

    public void setRemainingAmount(BigDecimal remainingAmount) {
        this.remainingAmount = remainingAmount;
    }

    public BigDecimal getProgressPercentage() {
        return progressPercentage;
    }

    public void setProgressPercentage(BigDecimal progressPercentage) {
        this.progressPercentage = progressPercentage;
    }

    public boolean isOverLimit() {
        return overLimit;
    }

    public void setOverLimit(boolean overLimit) {
        this.overLimit = overLimit;
    }

    public BigDecimal getOverBudgetAmount() {
        return overBudgetAmount;
    }

    public void setOverBudgetAmount(BigDecimal overBudgetAmount) {
        this.overBudgetAmount = overBudgetAmount;
    }

    public boolean isHasExceededBudget() {
        return hasExceededBudget;
    }

    public void setHasExceededBudget(boolean hasExceededBudget) {
        this.hasExceededBudget = hasExceededBudget;
    }

    public String getBudgetStatus() {
        return budgetStatus;
    }

    public void setBudgetStatus(String budgetStatus) {
        this.budgetStatus = budgetStatus;
    }

    public boolean isWarningTriggered() {
        return warningTriggered;
    }

    public void setWarningTriggered(boolean warningTriggered) {
        this.warningTriggered = warningTriggered;
    }

    public boolean isOverLimitAlertTriggered() {
        return overLimitAlertTriggered;
    }

    public void setOverLimitAlertTriggered(boolean overLimitAlertTriggered) {
        this.overLimitAlertTriggered = overLimitAlertTriggered;
    }

    public BigDecimal getWarningThresholdPercent() {
        return warningThresholdPercent;
    }

    public void setWarningThresholdPercent(BigDecimal warningThresholdPercent) {
        this.warningThresholdPercent = warningThresholdPercent;
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
}

