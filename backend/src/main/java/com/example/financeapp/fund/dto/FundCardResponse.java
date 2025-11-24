package com.example.financeapp.fund.dto;

import com.example.financeapp.fund.entity.Fund;
import com.example.financeapp.fund.entity.FundTermType;
import com.example.financeapp.fund.entity.FundType;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

public class FundCardResponse {

    private Long fundId;
    private String fundName;
    private FundType fundType;
    private FundTermType termType;
    private BigDecimal currentAmount;
    private BigDecimal targetAmount;
    private double progressPercent;
    private LocalDate startDate;
    private LocalDate endDate;
    private int memberCount;
    private String currencyCode;

    public static FundCardResponse from(Fund fund, BigDecimal currentBalance, int memberCount) {
        FundCardResponse response = new FundCardResponse();
        response.setFundId(fund.getFundId());
        response.setFundName(fund.getFundName());
        response.setFundType(fund.getFundType());
        response.setTermType(fund.getTermType());
        response.setCurrentAmount(currentBalance);
        response.setTargetAmount(fund.getTargetAmount());
        response.setStartDate(fund.getStartDate());
        response.setEndDate(fund.getEndDate());
        response.setMemberCount(memberCount);
        response.setCurrencyCode(fund.getCurrencyCode());
        response.setProgressPercent(calculateProgress(currentBalance, fund.getTargetAmount()));
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

    public int getMemberCount() {
        return memberCount;
    }

    public void setMemberCount(int memberCount) {
        this.memberCount = memberCount;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public void setCurrencyCode(String currencyCode) {
        this.currencyCode = currencyCode;
    }
}

