package com.example.financeapp.dto;

import com.example.financeapp.entity.WalletType;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public class CreateWalletRequest {

    @NotBlank(message = "Tên ví không được để trống")
    @Size(min = 2, max = 100, message = "Tên ví phải từ 2-100 ký tự")
    private String walletName;

    @NotNull(message = "Loại ví không được để trống")
    private WalletType walletType;

    @NotNull(message = "Số dư ban đầu không được để trống")
    @DecimalMin(value = "0.0", inclusive = true, message = "Số dư không được âm")
    private BigDecimal balance;

    @Size(max = 3, message = "Mã tiền tệ tối đa 3 ký tự")
    private String currency; // Optional, default: VND

    @Size(max = 500, message = "Mô tả tối đa 500 ký tự")
    private String description;

    @Size(max = 50, message = "Icon tối đa 50 ký tự")
    private String icon;

    // Getters & Setters

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
}

