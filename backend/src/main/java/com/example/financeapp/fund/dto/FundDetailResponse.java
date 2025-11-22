package com.example.financeapp.fund.dto;

import com.example.financeapp.fund.entity.Fund;
import com.example.financeapp.fund.entity.FundStatus;
import com.example.financeapp.fund.entity.FundTermType;
import com.example.financeapp.fund.entity.FundType;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

public class FundDetailResponse {

    private Long fundId;
    private String fundName;
    private FundType fundType;
    private FundTermType termType;
    private FundStatus status;
    private Long walletId;
    private String walletName;
    private String currencyCode;
    private BigDecimal currentAmount;
    private BigDecimal targetAmount;
    private double progressPercent;
    private LocalDate startDate;
    private LocalDate endDate;
    private String note;
    private String ownerName;
    private FundReminderConfig reminderConfig;
    private FundAutoTopUpConfig autoTopUpConfig;
    private List<FundMemberResponse> members;
    private String contributionFrequency;
    private BigDecimal contributionAmount;

    public static FundDetailResponse from(Fund fund,
                                          BigDecimal currentBalance,
                                          String walletName,
                                          FundReminderConfig reminderConfig,
                                          FundAutoTopUpConfig autoTopUpConfig,
                                          List<FundMemberResponse> members) {
        FundDetailResponse response = new FundDetailResponse();
        response.setFundId(fund.getFundId());
        response.setFundName(fund.getFundName());
        response.setFundType(fund.getFundType());
        response.setTermType(fund.getTermType());
        response.setStatus(fund.getStatus());
        response.setWalletId(fund.getWallet().getWalletId());
        response.setWalletName(walletName);
        response.setCurrencyCode(fund.getCurrencyCode());
        response.setCurrentAmount(currentBalance);
        response.setTargetAmount(fund.getTargetAmount());
        response.setProgressPercent(calculateProgress(currentBalance, fund.getTargetAmount()));
        response.setStartDate(fund.getStartDate());
        response.setEndDate(fund.getEndDate());
        response.setNote(fund.getNote());
        response.setOwnerName(fund.getOwner().getFullName());
        response.setReminderConfig(reminderConfig);
        response.setAutoTopUpConfig(autoTopUpConfig);
        response.setMembers(members);
        response.setContributionFrequency(fund.getContributionFrequency().name());
        response.setContributionAmount(fund.getContributionAmount());
        return response;
    }

    private static double calculateProgress(BigDecimal current, BigDecimal target) {
        if (current == null || target == null || target.compareTo(BigDecimal.ZERO) <= 0) {
            return 0;
        }
        return current.divide(target, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .min(BigDecimal.valueOf(100))
                .doubleValue();
    }

    public Long getFundId() {
        return fundId;
    }

    public void setFundId(Long fundId) {
        this.fundId = fundId;
    }

    public String getFundName() {
        return fundName;
    }

    public void setFundName(String fundName) {
        this.fundName = fundName;
    }

    public FundType getFundType() {
        return fundType;
    }

    public void setFundType(FundType fundType) {
        this.fundType = fundType;
    }

    public FundTermType getTermType() {
        return termType;
    }

    public void setTermType(FundTermType termType) {
        this.termType = termType;
    }

    public FundStatus getStatus() {
        return status;
    }

    public void setStatus(FundStatus status) {
        this.status = status;
    }

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

    public String getCurrencyCode() {
        return currencyCode;
    }

    public void setCurrencyCode(String currencyCode) {
        this.currencyCode = currencyCode;
    }

    public BigDecimal getCurrentAmount() {
        return currentAmount;
    }

    public void setCurrentAmount(BigDecimal currentAmount) {
        this.currentAmount = currentAmount;
    }

    public BigDecimal getTargetAmount() {
        return targetAmount;
    }

    public void setTargetAmount(BigDecimal targetAmount) {
        this.targetAmount = targetAmount;
    }

    public double getProgressPercent() {
        return progressPercent;
    }

    public void setProgressPercent(double progressPercent) {
        this.progressPercent = progressPercent;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

    public FundReminderConfig getReminderConfig() {
        return reminderConfig;
    }

    public void setReminderConfig(FundReminderConfig reminderConfig) {
        this.reminderConfig = reminderConfig;
    }

    public FundAutoTopUpConfig getAutoTopUpConfig() {
        return autoTopUpConfig;
    }

    public void setAutoTopUpConfig(FundAutoTopUpConfig autoTopUpConfig) {
        this.autoTopUpConfig = autoTopUpConfig;
    }

    public List<FundMemberResponse> getMembers() {
        return members;
    }

    public void setMembers(List<FundMemberResponse> members) {
        this.members = members;
    }

    public String getContributionFrequency() {
        return contributionFrequency;
    }

    public void setContributionFrequency(String contributionFrequency) {
        this.contributionFrequency = contributionFrequency;
    }

    public BigDecimal getContributionAmount() {
        return contributionAmount;
    }

    public void setContributionAmount(BigDecimal contributionAmount) {
        this.contributionAmount = contributionAmount;
    }
}

