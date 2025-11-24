package com.example.financeapp.fund.dto;

import com.example.financeapp.fund.entity.FundReminderType;

import java.time.DayOfWeek;
import java.time.LocalTime;

public class FundReminderConfig {

    private boolean enabled;
    private FundReminderType reminderType;
    private LocalTime reminderTime;
    private DayOfWeek reminderDayOfWeek;
    private Integer reminderDayOfMonth;
    private Integer reminderMonthOfYear;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public FundReminderType getReminderType() {
        return reminderType;
    }

    public void setReminderType(FundReminderType reminderType) {
        this.reminderType = reminderType;
    }

    public LocalTime getReminderTime() {
        return reminderTime;
    }

    public void setReminderTime(LocalTime reminderTime) {
        this.reminderTime = reminderTime;
    }

    public DayOfWeek getReminderDayOfWeek() {
        return reminderDayOfWeek;
    }

    public void setReminderDayOfWeek(DayOfWeek reminderDayOfWeek) {
        this.reminderDayOfWeek = reminderDayOfWeek;
    }

    public Integer getReminderDayOfMonth() {
        return reminderDayOfMonth;
    }

    public void setReminderDayOfMonth(Integer reminderDayOfMonth) {
        this.reminderDayOfMonth = reminderDayOfMonth;
    }

    public Integer getReminderMonthOfYear() {
        return reminderMonthOfYear;
    }

    public void setReminderMonthOfYear(Integer reminderMonthOfYear) {
        this.reminderMonthOfYear = reminderMonthOfYear;
    }
}

