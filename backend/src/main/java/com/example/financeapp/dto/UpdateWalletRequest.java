package com.example.financeapp.dto;

import com.example.financeapp.entity.WalletType;
import jakarta.validation.constraints.Size;

public class UpdateWalletRequest {

    @Size(min = 2, max = 100, message = "Tên ví phải từ 2-100 ký tự")
    private String walletName;

    private WalletType walletType; // Cho phép thay đổi loại ví

    @Size(max = 3, message = "Mã tiền tệ tối đa 3 ký tự")
    private String currency; // Cho phép thay đổi đơn vị tiền tệ

    @Size(max = 500, message = "Mô tả tối đa 500 ký tự")
    private String description;

    @Size(max = 50, message = "Icon tối đa 50 ký tự")
    private String icon;

    private Boolean isActive;

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

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }
}

