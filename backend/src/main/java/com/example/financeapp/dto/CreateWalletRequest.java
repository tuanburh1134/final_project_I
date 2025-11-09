package com.example.financeapp.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public class CreateWalletRequest {

    @NotBlank(message = "Tên ví không được để trống")
    @Size(max = 100, message = "Tên ví không quá 100 ký tự")
    private String walletName;

    @NotBlank(message = "Loại tiền không được để trống")
    @Pattern(regexp = "^[A-Z]{3}$", message = "Mã tiền tệ phải đúng định dạng ISO (VD: VND, USD, EUR)")
    private String currencyCode;

    @NotNull(message = "Số dư ban đầu không được để trống")
    @DecimalMin(value = "0.00", inclusive = true, message = "Số dư ban đầu phải ≥ 0")
    @Digits(integer = 15, fraction = 2, message = "Số dư chỉ được có tối đa 2 chữ số thập phân")
    private BigDecimal initialBalance = BigDecimal.ZERO;

    @Size(max = 255, message = "Mô tả không quá 255 ký tự")
    private String description;

    // Getters and Setters
    public String getWalletName() {
        return walletName;
    }

    public void setWalletName(String walletName) {
        this.walletName = walletName;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public void setCurrencyCode(String currencyCode) {
        this.currencyCode = currencyCode;
    }

    public BigDecimal getInitialBalance() {
        return initialBalance;
    }

    public void setInitialBalance(BigDecimal initialBalance) {
        this.initialBalance = initialBalance;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
