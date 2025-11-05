package com.example.financeapp.entity;

public enum WalletType {
    CASH("Tiền mặt"),
    BANK("Ngân hàng"),
    EWALLET("Ví điện tử");

    private final String displayName;

    WalletType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}

