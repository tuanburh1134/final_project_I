package com.example.financeapp.fund.dto;

import com.example.financeapp.fund.entity.AutoTopUpMode;
import com.example.financeapp.fund.entity.AutoTopUpScheduleType;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalTime;

public class FundAutoTopUpConfig {

    private boolean enabled;
    private AutoTopUpMode mode;
    private AutoTopUpScheduleType scheduleType;
    private LocalTime time;
    private DayOfWeek dayOfWeek;
    private Integer dayOfMonth;
    private Integer monthOfYear;
    private Long sourceWalletId;
    private String sourceWalletName;
    private BigDecimal amount;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public AutoTopUpMode getMode() {
        return mode;
    }

    public void setMode(AutoTopUpMode mode) {
        this.mode = mode;
    }

    public AutoTopUpScheduleType getScheduleType() {
        return scheduleType;
    }

    public void setScheduleType(AutoTopUpScheduleType scheduleType) {
        this.scheduleType = scheduleType;
    }

    public LocalTime getTime() {
        return time;
    }

    public void setTime(LocalTime time) {
        this.time = time;
    }

    public DayOfWeek getDayOfWeek() {
        return dayOfWeek;
    }

    public void setDayOfWeek(DayOfWeek dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }

    public Integer getDayOfMonth() {
        return dayOfMonth;
    }

    public void setDayOfMonth(Integer dayOfMonth) {
        this.dayOfMonth = dayOfMonth;
    }

    public Integer getMonthOfYear() {
        return monthOfYear;
    }

    public void setMonthOfYear(Integer monthOfYear) {
        this.monthOfYear = monthOfYear;
    }

    public Long getSourceWalletId() {
        return sourceWalletId;
    }

    public void setSourceWalletId(Long sourceWalletId) {
        this.sourceWalletId = sourceWalletId;
    }

    public String getSourceWalletName() {
        return sourceWalletName;
    }

    public void setSourceWalletName(String sourceWalletName) {
        this.sourceWalletName = sourceWalletName;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
}

