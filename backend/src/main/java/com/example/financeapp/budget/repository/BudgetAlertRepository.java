package com.example.financeapp.budget.repository;

import com.example.financeapp.budget.entity.Budget;
import com.example.financeapp.budget.entity.BudgetAlert;
import com.example.financeapp.budget.entity.BudgetAlertType;
import com.example.financeapp.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BudgetAlertRepository extends JpaRepository<BudgetAlert, Long> {

    boolean existsByBudgetAndResolvedFalse(Budget budget);

    boolean existsByBudgetAndAlertTypeAndResolvedFalse(Budget budget, BudgetAlertType alertType);

    List<BudgetAlert> findByUserAndResolvedFalseOrderByTriggeredAtDesc(User user);

    List<BudgetAlert> findByUserOrderByTriggeredAtDesc(User user);

    List<BudgetAlert> findByBudgetAndAlertTypeAndResolvedFalse(Budget budget, BudgetAlertType alertType);
}

