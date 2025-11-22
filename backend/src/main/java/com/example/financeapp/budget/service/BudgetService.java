package com.example.financeapp.budget.service;

import com.example.financeapp.budget.dto.BudgetSummaryResponse;
import com.example.financeapp.budget.dto.CreateBudgetRequest;
import com.example.financeapp.budget.entity.Budget;
import org.springframework.lang.NonNull;

import com.example.financeapp.transaction.entity.Transaction;
import java.util.List;

public interface BudgetService {
    Budget createBudget(@NonNull Long userId, CreateBudgetRequest request);
    List<BudgetSummaryResponse> getBudgets(@NonNull Long userId);
    List<Transaction> getBudgetTransactions(@NonNull Long userId, Long budgetId);
}