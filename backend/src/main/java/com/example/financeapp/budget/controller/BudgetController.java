package com.example.financeapp.budget.controller;

import com.example.financeapp.budget.dto.BudgetSummaryResponse;
import com.example.financeapp.budget.dto.CreateBudgetRequest;
import com.example.financeapp.budget.entity.Budget;
import com.example.financeapp.security.CustomUserDetails;
import com.example.financeapp.transaction.entity.Transaction;
import com.example.financeapp.user.entity.User;
import com.example.financeapp.budget.service.BudgetService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@RestController
@RequestMapping("/budgets")
public class BudgetController {

    @Autowired
    private BudgetService budgetService;

    @PostMapping("/create")
    public ResponseEntity<Map<String, Object>> createBudget(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody CreateBudgetRequest request
    ) {
        Map<String, Object> res = new HashMap<>();
        try {
            User user = requireUser(userDetails);
            long userId = requireUserId(user);
            Budget budget = budgetService.createBudget(userId, request);

            res.put("message", "Tạo hạn mức chi tiêu thành công");
            res.put("budget", budget);
            return ResponseEntity.ok(res);

        } catch (RuntimeException e) {
            res.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(res);
        } catch (Exception e) {
            res.put("error", "Lỗi hệ thống: " + e.getMessage());
            return ResponseEntity.status(500).body(res);
        }
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> getBudgets(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Map<String, Object> res = new HashMap<>();
        try {
            User user = requireUser(userDetails);
            long userId = requireUserId(user);
            List<BudgetSummaryResponse> budgets = budgetService.getBudgets(userId);

            res.put("budgets", budgets);
            res.put("total", budgets.size());
            return ResponseEntity.ok(res);
        } catch (RuntimeException e) {
            res.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(res);
        } catch (Exception e) {
            res.put("error", "Lỗi hệ thống: " + e.getMessage());
            return ResponseEntity.status(500).body(res);
        }
    }

    @GetMapping("/{budgetId}/transactions")
    public ResponseEntity<Map<String, Object>> getBudgetTransactions(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long budgetId
    ) {
        Map<String, Object> res = new HashMap<>();
        try {
            User user = requireUser(userDetails);
            long userId = requireUserId(user);
            List<Transaction> transactions = budgetService.getBudgetTransactions(userId, budgetId);

            res.put("transactions", transactions);
            res.put("total", transactions.size());
            return ResponseEntity.ok(res);
        } catch (RuntimeException e) {
            res.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(res);
        } catch (Exception e) {
            res.put("error", "Lỗi hệ thống: " + e.getMessage());
            return ResponseEntity.status(500).body(res);
        }
    }

    private @NonNull User requireUser(CustomUserDetails userDetails) {
        if (userDetails == null || userDetails.getUser() == null) {
            throw new RuntimeException("Không tìm thấy thông tin người dùng");
        }
        return Objects.requireNonNull(userDetails.getUser(), "User not found");
    }

    private @NonNull Long requireUserId(User user) {
        return Objects.requireNonNull(user.getUserId(), "Không tìm thấy mã người dùng");
    }
}
