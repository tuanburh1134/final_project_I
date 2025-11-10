package com.example.financeapp.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public class CreateWalletRequest {

    @NotBlank(message = "Tên ví không được để trống")
    @Size(max = 100, message = "Tên ví không quá 100 ký tự")
    private String walletName;

    @NotBlank(message = "Loại tiền không được để trống")
    @Pattern(regexp = "^[A-Z]{3}$", message = "Mã tiền tệ phải đúng định dạng ISO (VD: VND, USD)")
    private String currencyCode;

    @DecimalMin(value = "0.0", inclusive = true, message = "Số dư phải ≥ 0")
    private BigDecimal balance = BigDecimal.ZERO; // ✅ dùng BigDecimal để đồng bộ với entity Wallet

    @Size(max = 255, message = "Mô tả không quá 255 ký tự")
    private String description;

    private Boolean setAsDefault = false; // ✅ đổi sang Boolean để tránh lỗi primitive khi null

    // ===== Getters & Setters =====
    public String getWalletName() { return walletName; }
    public void setWalletName(String walletName) { this.walletName = walletName; }

    public String getCurrencyCode() { return currencyCode; }
    public void setCurrencyCode(String currencyCode) { this.currencyCode = currencyCode; }

    public BigDecimal getBalance() { return balance; }
    public void setBalance(BigDecimal balance) { this.balance = balance; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Boolean getSetAsDefault() { return setAsDefault; }
    public void setSetAsDefault(Boolean setAsDefault) { this.setAsDefault = setAsDefault; }
}
