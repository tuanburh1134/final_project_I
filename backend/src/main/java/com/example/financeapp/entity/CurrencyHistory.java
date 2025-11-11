package com.example.financeapp.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "currency_history")
public class CurrencyHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String currencyCode;

    @Column(precision = 18, scale = 6)
    private BigDecimal oldRate;

    @Column(precision = 18, scale = 6)
    private BigDecimal newRate;

    private LocalDateTime changedAt;

    public CurrencyHistory() {}

    public CurrencyHistory(String currencyCode, BigDecimal oldRate, BigDecimal newRate, LocalDateTime changedAt) {
        this.currencyCode = currencyCode;
        this.oldRate = oldRate;
        this.newRate = newRate;
        this.changedAt = changedAt;
    }

    // Getters & Setters
    public Long getId() { return id; }
    public String getCurrencyCode() { return currencyCode; }
    public void setCurrencyCode(String currencyCode) { this.currencyCode = currencyCode; }
    public BigDecimal getOldRate() { return oldRate; }
    public void setOldRate(BigDecimal oldRate) { this.oldRate = oldRate; }
    public BigDecimal getNewRate() { return newRate; }
    public void setNewRate(BigDecimal newRate) { this.newRate = newRate; }
    public LocalDateTime getChangedAt() { return changedAt; }
    public void setChangedAt(LocalDateTime changedAt) { this.changedAt = changedAt; }
}
