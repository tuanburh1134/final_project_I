package com.example.financeapp.dto;

import com.example.financeapp.entity.Wallet;
import com.example.financeapp.entity.WalletType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class WalletResponse {

    private Long walletId;
    private String walletName;
    private WalletType walletType;
    private String walletTypeDisplay;
    private BigDecimal balance;
    private String currency;
    private String description;
    private String icon;
    private boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Constructor từ Entity
    public WalletResponse(Wallet wallet) {
        this.walletId = wallet.getWalletId();
        this.walletName = wallet.getWalletName();
        this.walletType = wallet.getWalletType();
        this.walletTypeDisplay = wallet.getWalletType().getDisplayName();
        this.balance = wallet.getBalance();
        this.currency = wallet.getCurrency();
        this.description = wallet.getDescription();
        this.icon = wallet.getIcon();
        this.isActive = wallet.isActive();
        this.createdAt = wallet.getCreatedAt();
        this.updatedAt = wallet.getUpdatedAt();
    }

    // Getters & Setters

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

    public WalletType getWalletType() {
        return walletType;
    }

    public void setWalletType(WalletType walletType) {
        this.walletType = walletType;
    }

    public String getWalletTypeDisplay() {
        return walletTypeDisplay;
    }

    public void setWalletTypeDisplay(String walletTypeDisplay) {
        this.walletTypeDisplay = walletTypeDisplay;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
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
}

