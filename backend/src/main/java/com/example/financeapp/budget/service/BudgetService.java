package com.example.financeapp.budget.service;

import com.example.financeapp.budget.dto.CreateBudgetRequest;
import com.example.financeapp.budget.dto.BudgetAlertResponse;
import com.example.financeapp.budget.dto.BudgetSummaryResponse;
import com.example.financeapp.budget.dto.BudgetTransactionResponse;
import com.example.financeapp.budget.entity.Budget;
import com.example.financeapp.transaction.entity.Transaction;

import java.util.List;

public interface BudgetService {
    Budget createBudget(Long userId, CreateBudgetRequest request);
    List<BudgetSummaryResponse> getBudgets(Long userId);
    List<BudgetTransactionResponse> getBudgetTransactions(Long userId, Long budgetId);
    List<BudgetAlertResponse> getBudgetAlerts(Long userId);
    void resolveBudgetAlert(Long userId, Long alertId);
    void evaluateBudgetAfterTransaction(Long userId, Transaction transaction);
}