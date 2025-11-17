package com.example.financeapp.controller;

import com.example.financeapp.dto.CreateTransactionRequest;
import com.example.financeapp.entity.Transaction;
import com.example.financeapp.entity.User;
import com.example.financeapp.repository.UserRepository;
import com.example.financeapp.service.TransactionService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
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
            Transaction tx = transactionService.createExpense(getCurrentUserId(), request);
            res.put("message", "Thêm chi tiêu thành công");
            res.put("transaction", tx);
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
            Transaction tx = transactionService.createIncome(getCurrentUserId(), request);
            res.put("message", "Thêm thu nhập thành công");
            res.put("transaction", tx);
            return ResponseEntity.ok(res);
        } catch (Exception e) {
            res.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(res);
        }
    }

    // ========================== GET TRANSACTIONS ==========================

    /**
     * Lấy tất cả giao dịch của user
     * Có thể filter theo walletId, typeId, startDate, endDate
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllTransactions(
            @RequestParam(required = false) Long walletId,
            @RequestParam(required = false) Long typeId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        
        Map<String, Object> res = new HashMap<>();
        try {
            Long userId = getCurrentUserId();
            List<Transaction> transactions = transactionService.getAllTransactions(userId, walletId, typeId, startDate, endDate);
            
            res.put("transactions", transactions);
            res.put("total", transactions.size());
            return ResponseEntity.ok(res);
        } catch (RuntimeException e) {
            res.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(res);
        } catch (Exception e) {
            res.put("error", "Lỗi máy chủ nội bộ: " + e.getMessage());
            return ResponseEntity.status(500).body(res);
        }
    }

    /**
     * Lấy chi tiết 1 giao dịch
     */
    @GetMapping("/{transactionId}")
    public ResponseEntity<Map<String, Object>> getTransactionById(@PathVariable Long transactionId) {
        Map<String, Object> res = new HashMap<>();
        try {
            Long userId = getCurrentUserId();
            Transaction transaction = transactionService.getTransactionById(userId, transactionId);
            
            res.put("transaction", transaction);
            return ResponseEntity.ok(res);
        } catch (RuntimeException e) {
            res.put("error", e.getMessage());
            return ResponseEntity.status(404).body(res);
        } catch (Exception e) {
            res.put("error", "Lỗi máy chủ nội bộ: " + e.getMessage());
            return ResponseEntity.status(500).body(res);
        }
    }

    /**
     * Lấy tất cả giao dịch của 1 ví cụ thể
     */
    @GetMapping("/wallet/{walletId}")
    public ResponseEntity<Map<String, Object>> getTransactionsByWallet(@PathVariable Long walletId) {
        Map<String, Object> res = new HashMap<>();
        try {
            Long userId = getCurrentUserId();
            List<Transaction> transactions = transactionService.getTransactionsByWallet(userId, walletId);
            
            res.put("transactions", transactions);
            res.put("total", transactions.size());
            res.put("walletId", walletId);
            return ResponseEntity.ok(res);
        } catch (RuntimeException e) {
            res.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(res);
        } catch (Exception e) {
            res.put("error", "Lỗi máy chủ nội bộ: " + e.getMessage());
            return ResponseEntity.status(500).body(res);
        }
    }
}