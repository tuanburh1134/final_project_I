package com.example.financeapp.budget.controller;

import com.example.financeapp.budget.dto.BudgetAlertResponse;
import com.example.financeapp.budget.dto.BudgetSummaryResponse;
import com.example.financeapp.budget.dto.BudgetTransactionResponse;
import com.example.financeapp.budget.dto.CreateBudgetRequest;
import com.example.financeapp.budget.entity.Budget;
import com.example.financeapp.user.entity.User;
import com.example.financeapp.security.CustomUserDetails;
import com.example.financeapp.budget.service.BudgetService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/budgets")
public class BudgetController {

    @Autowired
    private BudgetService budgetService;

    @GetMapping
    public ResponseEntity<Map<String, Object>> getBudgets(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Map<String, Object> res = new HashMap<>();
        try {
            User user = userDetails.getUser();
            List<BudgetSummaryResponse> budgets = budgetService.getBudgets(user.getUserId());
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
            @PathVariable Long budgetId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Map<String, Object> res = new HashMap<>();
        try {
            User user = userDetails.getUser();
            List<BudgetTransactionResponse> transactions =
                    budgetService.getBudgetTransactions(user.getUserId(), budgetId);
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

    @GetMapping("/alerts")
    public ResponseEntity<Map<String, Object>> getBudgetAlerts(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Map<String, Object> res = new HashMap<>();
        try {
            User user = userDetails.getUser();
            List<BudgetAlertResponse> alerts = budgetService.getBudgetAlerts(user.getUserId());
            res.put("alerts", alerts);
            res.put("total", alerts.size());
            return ResponseEntity.ok(res);
        } catch (RuntimeException e) {
            res.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(res);
        } catch (Exception e) {
            res.put("error", "Lỗi hệ thống: " + e.getMessage());
            return ResponseEntity.status(500).body(res);
        }
    }

    @PostMapping("/alerts/{alertId}/resolve")
    public ResponseEntity<Map<String, Object>> resolveBudgetAlert(
            @PathVariable Long alertId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Map<String, Object> res = new HashMap<>();
        try {
            User user = userDetails.getUser();
            budgetService.resolveBudgetAlert(user.getUserId(), alertId);
            res.put("message", "Đã đánh dấu cảnh báo là đã đọc");
            return ResponseEntity.ok(res);
        } catch (RuntimeException e) {
            res.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(res);
        } catch (Exception e) {
            res.put("error", "Lỗi hệ thống: " + e.getMessage());
            return ResponseEntity.status(500).body(res);
        }
    }
    @PostMapping("/create")
    public ResponseEntity<Map<String, Object>> createBudget(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody CreateBudgetRequest request
    ) {
        Map<String, Object> res = new HashMap<>();
        try {
            User user = userDetails.getUser();
            Budget budget = budgetService.createBudget(user.getUserId(), request);

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
}
