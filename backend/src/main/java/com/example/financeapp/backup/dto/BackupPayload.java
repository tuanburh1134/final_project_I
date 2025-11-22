package com.example.financeapp.backup.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class BackupPayload {

    private UserInfo user;
    private List<WalletInfo> wallets;
    private List<TransactionInfo> transactions;
    private List<BudgetInfo> budgets;
    private List<ScheduledTransactionInfo> scheduledTransactions;
    private List<FundInfo> funds;
    private LocalDateTime generatedAt;

    public UserInfo getUser() {
        return user;
    }

    public void setUser(UserInfo user) {
        this.user = user;
    }

    public List<WalletInfo> getWallets() {
        return wallets;
    }

    public void setWallets(List<WalletInfo> wallets) {
        this.wallets = wallets;
    }

    public List<TransactionInfo> getTransactions() {
        return transactions;
    }

    public void setTransactions(List<TransactionInfo> transactions) {
        this.transactions = transactions;
    }

    public List<BudgetInfo> getBudgets() {
        return budgets;
    }

    public void setBudgets(List<BudgetInfo> budgets) {
        this.budgets = budgets;
    }

    public List<ScheduledTransactionInfo> getScheduledTransactions() {
        return scheduledTransactions;
    }

    public void setScheduledTransactions(List<ScheduledTransactionInfo> scheduledTransactions) {
        this.scheduledTransactions = scheduledTransactions;
    }

    public List<FundInfo> getFunds() {
        return funds;
    }

    public void setFunds(List<FundInfo> funds) {
        this.funds = funds;
    }

    public LocalDateTime getGeneratedAt() {
        return generatedAt;
    }

    public void setGeneratedAt(LocalDateTime generatedAt) {
        this.generatedAt = generatedAt;
    }

    // Nested DTOs
    public static class UserInfo {
        private Long userId;
        private String fullName;
        private String email;

        public Long getUserId() {
            return userId;
        }

        public void setUserId(Long userId) {
            this.userId = userId;
        }

        public String getFullName() {
            return fullName;
        }

        public void setFullName(String fullName) {
            this.fullName = fullName;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }
    }

    public static class WalletInfo {
        private Long walletId;
        private String walletName;
        private String currency;
        private BigDecimal balance;
        private String walletType;
        private LocalDateTime createdAt;

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

        public String getCurrency() {
            return currency;
        }

        public void setCurrency(String currency) {
            this.currency = currency;
        }

        public BigDecimal getBalance() {
            return balance;
        }

        public void setBalance(BigDecimal balance) {
            this.balance = balance;
        }

        public String getWalletType() {
            return walletType;
        }

        public void setWalletType(String walletType) {
            this.walletType = walletType;
        }

        public LocalDateTime getCreatedAt() {
            return createdAt;
        }

        public void setCreatedAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
        }
    }

    public static class TransactionInfo {
        private Long transactionId;
        private String walletName;
        private String categoryName;
        private String transactionType;
        private BigDecimal amount;
        private LocalDateTime transactionDate;
        private String note;

        public Long getTransactionId() {
            return transactionId;
        }

        public void setTransactionId(Long transactionId) {
            this.transactionId = transactionId;
        }

        public String getWalletName() {
            return walletName;
        }

        public void setWalletName(String walletName) {
            this.walletName = walletName;
        }

        public String getCategoryName() {
            return categoryName;
        }

        public void setCategoryName(String categoryName) {
            this.categoryName = categoryName;
        }

        public String getTransactionType() {
            return transactionType;
        }

        public void setTransactionType(String transactionType) {
            this.transactionType = transactionType;
        }

        public BigDecimal getAmount() {
            return amount;
        }

        public void setAmount(BigDecimal amount) {
            this.amount = amount;
        }

        public LocalDateTime getTransactionDate() {
            return transactionDate;
        }

        public void setTransactionDate(LocalDateTime transactionDate) {
            this.transactionDate = transactionDate;
        }

        public String getNote() {
            return note;
        }

        public void setNote(String note) {
            this.note = note;
        }
    }

    public static class BudgetInfo {
        private Long budgetId;
        private String categoryName;
        private String walletName;
        private BigDecimal amountLimit;
        private LocalDate startDate;
        private LocalDate endDate;
        private String status;

        public Long getBudgetId() {
            return budgetId;
        }

        public void setBudgetId(Long budgetId) {
            this.budgetId = budgetId;
        }

        public String getCategoryName() {
            return categoryName;
        }

        public void setCategoryName(String categoryName) {
            this.categoryName = categoryName;
        }

        public String getWalletName() {
            return walletName;
        }

        public void setWalletName(String walletName) {
            this.walletName = walletName;
        }

        public BigDecimal getAmountLimit() {
            return amountLimit;
        }

        public void setAmountLimit(BigDecimal amountLimit) {
            this.amountLimit = amountLimit;
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

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }
    }

    public static class ScheduledTransactionInfo {
        private Long scheduleId;
        private String walletName;
        private String categoryName;
        private String transactionType;
        private BigDecimal amount;
        private String scheduleType;
        private LocalDateTime scheduleTime;
        private LocalDateTime nextRunAt;
        private String status;

        public Long getScheduleId() {
            return scheduleId;
        }

        public void setScheduleId(Long scheduleId) {
            this.scheduleId = scheduleId;
        }

        public String getWalletName() {
            return walletName;
        }

        public void setWalletName(String walletName) {
            this.walletName = walletName;
        }

        public String getCategoryName() {
            return categoryName;
        }

        public void setCategoryName(String categoryName) {
            this.categoryName = categoryName;
        }

        public String getTransactionType() {
            return transactionType;
        }

        public void setTransactionType(String transactionType) {
            this.transactionType = transactionType;
        }

        public BigDecimal getAmount() {
            return amount;
        }

        public void setAmount(BigDecimal amount) {
            this.amount = amount;
        }

        public String getScheduleType() {
            return scheduleType;
        }

        public void setScheduleType(String scheduleType) {
            this.scheduleType = scheduleType;
        }

        public LocalDateTime getScheduleTime() {
            return scheduleTime;
        }

        public void setScheduleTime(LocalDateTime scheduleTime) {
            this.scheduleTime = scheduleTime;
        }

        public LocalDateTime getNextRunAt() {
            return nextRunAt;
        }

        public void setNextRunAt(LocalDateTime nextRunAt) {
            this.nextRunAt = nextRunAt;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }
    }

    public static class FundInfo {
        private Long fundId;
        private String fundName;
        private String fundType;
        private String termType;
        private BigDecimal targetAmount;
        private BigDecimal currentBalance;
        private double progressPercent;
        private LocalDate startDate;
        private LocalDate endDate;
        private List<FundMemberInfo> members;

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

        public String getFundType() {
            return fundType;
        }

        public void setFundType(String fundType) {
            this.fundType = fundType;
        }

        public String getTermType() {
            return termType;
        }

        public void setTermType(String termType) {
            this.termType = termType;
        }

        public BigDecimal getTargetAmount() {
            return targetAmount;
        }

        public void setTargetAmount(BigDecimal targetAmount) {
            this.targetAmount = targetAmount;
        }

        public BigDecimal getCurrentBalance() {
            return currentBalance;
        }

        public void setCurrentBalance(BigDecimal currentBalance) {
            this.currentBalance = currentBalance;
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

        public List<FundMemberInfo> getMembers() {
            return members;
        }

        public void setMembers(List<FundMemberInfo> members) {
            this.members = members;
        }
    }

    public static class FundMemberInfo {
        private String fullName;
        private String email;
        private String role;

        public String getFullName() {
            return fullName;
        }

        public void setFullName(String fullName) {
            this.fullName = fullName;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getRole() {
            return role;
        }

        public void setRole(String role) {
            this.role = role;
        }
    }
}

