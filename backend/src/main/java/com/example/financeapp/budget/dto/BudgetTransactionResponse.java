package com.example.financeapp.budget.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO đại diện giao dịch thuộc một ngân sách.
 */
public class BudgetTransactionResponse {

    private Long transactionId;
    private Long walletId;
    private String walletName;
    private String walletCurrency;
    private BigDecimal amount;
    private LocalDateTime transactionDate;
    private String note;
    private String imageUrl;
    private Long categoryId;
    private String categoryName;
    private boolean overBudget;
    private BigDecimal overBudgetAmount = BigDecimal.ZERO;

    public Long getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(Long transactionId) {
        this.transactionId = transactionId;
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

    public String getWalletCurrency() {
        return walletCurrency;
    }

    public void setWalletCurrency(String walletCurrency) {
        this.walletCurrency = walletCurrency;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public LocalDateTime getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(LocalDateTime transactionDate) {
        this.transactionDate = transactionDate;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
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

    public boolean isOverBudget() {
        return overBudget;
    }

    public void setOverBudget(boolean overBudget) {
        this.overBudget = overBudget;
    }

    public BigDecimal getOverBudgetAmount() {
        return overBudgetAmount;
    }

    public void setOverBudgetAmount(BigDecimal overBudgetAmount) {
        this.overBudgetAmount = overBudgetAmount;
    }
}

