package com.example.financeapp.budget.dto;

import java.math.BigDecimal;

public class BudgetAlert {
    private final boolean triggered;
    private final String level; // WARNING | OVER_LIMIT
    private final String message;
    private final BigDecimal overBudgetAmount;

    public BudgetAlert(boolean triggered, String level, String message, BigDecimal overBudgetAmount) {
        this.triggered = triggered;
        this.level = level;
        this.message = message;
        this.overBudgetAmount = overBudgetAmount;
    }

    public boolean isTriggered() {
        return triggered;
    }

    public String getLevel() {
        return level;
    }

    public String getMessage() {
        return message;
    }

    public BigDecimal getOverBudgetAmount() {
        return overBudgetAmount;
    }
}

