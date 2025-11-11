package com.example.financeapp.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.List;

@Entity
@Table(name = "currencies")
public class Currency {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long currencyId;

    @Column(name = "currency_code", length = 3, nullable = false, unique = true)
    private String currencyCode; // Ví dụ: "VND", "USD", "EUR"

    @Column(name = "currency_name", nullable = false)
    private String currencyName; // Ví dụ: "Việt Nam Đồng", "Đô la Mỹ"

    @Column(name = "symbol", length = 10)
    private String symbol; // Ký hiệu: ₫, $, €, ...

    @Column(name = "rate_to_vnd", nullable = false, precision = 18, scale = 4)
    private BigDecimal rateToVnd; // Ví dụ: 1 USD = 24000, 1 EUR = 26000, 1 VND = 1

    // ✅ Thêm liên kết 1-nhiều để JPA có thể map ngược lại với Wallet
    @OneToMany(mappedBy = "currency", cascade = CascadeType.ALL, orphanRemoval = false)
    private List<Wallet> wallets;

    // ===== Constructors =====
    public Currency() {}

    public Currency(String currencyCode, String currencyName, String symbol, BigDecimal rateToVnd) {
        this.currencyCode = currencyCode;
        this.currencyName = currencyName;
        this.symbol = symbol;
        this.rateToVnd = rateToVnd;
    }

    // ===== Getters & Setters =====
    public Long getCurrencyId() {
        return currencyId;
    }

    public void setCurrencyId(Long currencyId) {
        this.currencyId = currencyId;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public void setCurrencyCode(String currencyCode) {
        this.currencyCode = currencyCode;
    }

    public String getCurrencyName() {
        return currencyName;
    }

    public void setCurrencyName(String currencyName) {
        this.currencyName = currencyName;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public BigDecimal getRateToVnd() {
        return rateToVnd;
    }

    public void setRateToVnd(BigDecimal rateToVnd) {
        this.rateToVnd = rateToVnd;
    }

    public List<Wallet> getWallets() {
        return wallets;
    }

    public void setWallets(List<Wallet> wallets) {
        this.wallets = wallets;
    }
}
