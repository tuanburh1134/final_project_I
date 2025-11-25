package com.example.financeapp.budget.service;

import com.example.financeapp.budget.dto.CreateBudgetRequest;
import com.example.financeapp.budget.entity.Budget;

public interface BudgetService {
    Budget createBudget(Long userId, CreateBudgetRequest request);
}