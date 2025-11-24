package com.example.financeapp.transaction.dto;

import com.example.financeapp.budget.dto.BudgetAlert;
import com.example.financeapp.transaction.entity.Transaction;

public class TransactionResult {
    private final Transaction transaction;
    private final BudgetAlert budgetAlert;

    public TransactionResult(Transaction transaction, BudgetAlert budgetAlert) {
        this.transaction = transaction;
        this.budgetAlert = budgetAlert;
    }

    public Transaction getTransaction() {
        return transaction;
    }

    public BudgetAlert getBudgetAlert() {
        return budgetAlert;
    }
}

