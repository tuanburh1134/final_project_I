package com.example.financeapp.transaction.controller;

import com.example.financeapp.budget.dto.BudgetAlert;
import com.example.financeapp.transaction.dto.CreateTransactionRequest;
import com.example.financeapp.transaction.dto.UpdateTransactionRequest;
import com.example.financeapp.transaction.entity.Transaction;
import com.example.financeapp.transaction.service.TransactionService;
import com.example.financeapp.user.entity.User;
import com.example.financeapp.user.repository.UserRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/transactions")
@CrossOrigin(origins = "*")
public class TransactionController {

    @Autowired private TransactionService transactionService;
    @Autowired private UserRepository userRepository;

    private Long getCurrentUserId() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .map(User::getUserId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy user"));
    }

    @PostMapping("/expense")
    public ResponseEntity<Map<String, Object>> addExpense(@Valid @RequestBody CreateTransactionRequest request) {
        Map<String, Object> res = new HashMap<>();
        try {
            var result = transactionService.createExpense(getCurrentUserId(), request);
            Transaction tx = result.getTransaction();
            res.put("message", "Thêm chi tiêu thành công");
            res.put("transaction", tx);
            attachBudgetAlert(res, result.getBudgetAlert());
            return ResponseEntity.ok(res);
        } catch (Exception e) {
            res.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(res);
        }
    }

    @PostMapping("/income")
    public ResponseEntity<Map<String, Object>> addIncome(@Valid @RequestBody CreateTransactionRequest request) {
        Map<String, Object> res = new HashMap<>();
        try {
            var result = transactionService.createIncome(getCurrentUserId(), request);
            Transaction tx = result.getTransaction();
            res.put("message", "Thêm thu nhập thành công");
            res.put("transaction", tx);
            attachBudgetAlert(res, result.getBudgetAlert());
            return ResponseEntity.ok(res);
        } catch (Exception e) {
            res.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(res);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateTransaction(
            @PathVariable("id") Long id,
            @Valid @RequestBody UpdateTransactionRequest request) {
        Map<String, Object> res = new HashMap<>();
        try {
            Long userId = getCurrentUserId();
            System.out.println("Update transaction request - ID: " + id + ", UserId: " + userId);
            Transaction tx = transactionService.updateTransaction(userId, id, request);
            res.put("message", "Cập nhật giao dịch thành công");
            res.put("transaction", tx);
            return ResponseEntity.ok(res);
        } catch (Exception e) {
            System.out.println("Error updating transaction: " + e.getMessage());
            e.printStackTrace();
            res.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(res);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteTransaction(@PathVariable("id") Long id) {
        Map<String, Object> res = new HashMap<>();
        try {
            Long userId = getCurrentUserId();
            transactionService.deleteTransaction(userId, id);
            res.put("message", "Xóa giao dịch thành công");
            return ResponseEntity.ok(res);
        } catch (Exception e) {
            System.out.println("Error deleting transaction: " + e.getMessage());
            e.printStackTrace();
            res.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(res);
        }
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllTransactions() {
        Map<String, Object> res = new HashMap<>();
        try {
            Long userId = getCurrentUserId();
            List<Transaction> transactions = transactionService.getAllTransactions(userId);
            res.put("transactions", transactions);
            res.put("total", transactions.size());
            return ResponseEntity.ok(res);
        } catch (Exception e) {
            res.put("error", e.getMessage());
            return ResponseEntity.status(500).body(res);
        }
    }

    private void attachBudgetAlert(Map<String, Object> res, BudgetAlert alert) {
        if (alert == null || !alert.isTriggered()) {
            return;
        }
        res.put("budgetAlert", alert.getMessage());
        res.put("budgetAlertLevel", alert.getLevel());
        res.put("overBudgetAmount", alert.getOverBudgetAmount());
    }
}