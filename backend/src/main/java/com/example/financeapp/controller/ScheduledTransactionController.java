package com.example.financeapp.controller;

import com.example.financeapp.dto.CreateScheduledTransactionRequest;
import com.example.financeapp.entity.ScheduledTransaction;
import com.example.financeapp.entity.User;
import com.example.financeapp.repository.UserRepository;
import com.example.financeapp.service.ScheduledTransactionService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/scheduled-transactions")
@CrossOrigin(origins = "*")
public class ScheduledTransactionController {

    @Autowired
    private ScheduledTransactionService scheduledTransactionService;
    
    @Autowired
    private UserRepository userRepository;

    private Long getCurrentUserId() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .map(User::getUserId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy user"));
    }

    // ========================== CREATE SCHEDULED TRANSACTION ==========================

    @PostMapping("/create")
    public ResponseEntity<Map<String, Object>> createScheduledTransaction(
            @Valid @RequestBody CreateScheduledTransactionRequest request) {
        Map<String, Object> res = new HashMap<>();
        try {
            Long userId = getCurrentUserId();
            ScheduledTransaction scheduled = scheduledTransactionService.createScheduledTransaction(userId, request);
            
            res.put("message", "Tạo giao dịch đặt lịch thành công");
            res.put("scheduledTransaction", scheduled);
            return ResponseEntity.ok(res);
        } catch (RuntimeException e) {
            res.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(res);
        } catch (Exception e) {
            res.put("error", "Lỗi máy chủ nội bộ: " + e.getMessage());
            return ResponseEntity.status(500).body(res);
        }
    }

    // ========================== GET SCHEDULED TRANSACTIONS ==========================

    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllScheduledTransactions() {
        Map<String, Object> res = new HashMap<>();
        try {
            Long userId = getCurrentUserId();
            List<ScheduledTransaction> scheduled = scheduledTransactionService.getAllScheduledTransactions(userId);
            
            res.put("scheduledTransactions", scheduled);
            res.put("total", scheduled.size());
            return ResponseEntity.ok(res);
        } catch (Exception e) {
            res.put("error", "Lỗi máy chủ nội bộ: " + e.getMessage());
            return ResponseEntity.status(500).body(res);
        }
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<Map<String, Object>> getScheduledTransactionsByStatus(
            @PathVariable String status) {
        Map<String, Object> res = new HashMap<>();
        try {
            Long userId = getCurrentUserId();
            ScheduledTransaction.ScheduledStatus statusEnum;
            try {
                statusEnum = ScheduledTransaction.ScheduledStatus.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException e) {
                res.put("error", "Trạng thái không hợp lệ. Chấp nhận: PENDING, EXECUTED, CANCELLED");
                return ResponseEntity.badRequest().body(res);
            }
            
            List<ScheduledTransaction> scheduled = scheduledTransactionService.getScheduledTransactionsByStatus(userId, statusEnum);
            
            res.put("scheduledTransactions", scheduled);
            res.put("total", scheduled.size());
            res.put("status", status);
            return ResponseEntity.ok(res);
        } catch (Exception e) {
            res.put("error", "Lỗi máy chủ nội bộ: " + e.getMessage());
            return ResponseEntity.status(500).body(res);
        }
    }

    @GetMapping("/wallet/{walletId}")
    public ResponseEntity<Map<String, Object>> getScheduledTransactionsByWallet(
            @PathVariable Long walletId) {
        Map<String, Object> res = new HashMap<>();
        try {
            Long userId = getCurrentUserId();
            List<ScheduledTransaction> scheduled = scheduledTransactionService.getScheduledTransactionsByWallet(userId, walletId);
            
            res.put("scheduledTransactions", scheduled);
            res.put("total", scheduled.size());
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

    @GetMapping("/{scheduledId}")
    public ResponseEntity<Map<String, Object>> getScheduledTransactionById(
            @PathVariable Long scheduledId) {
        Map<String, Object> res = new HashMap<>();
        try {
            Long userId = getCurrentUserId();
            ScheduledTransaction scheduled = scheduledTransactionService.getScheduledTransactionById(userId, scheduledId);
            
            res.put("scheduledTransaction", scheduled);
            return ResponseEntity.ok(res);
        } catch (RuntimeException e) {
            res.put("error", e.getMessage());
            return ResponseEntity.status(404).body(res);
        } catch (Exception e) {
            res.put("error", "Lỗi máy chủ nội bộ: " + e.getMessage());
            return ResponseEntity.status(500).body(res);
        }
    }

    // ========================== CANCEL SCHEDULED TRANSACTION ==========================

    @PostMapping("/{scheduledId}/cancel")
    public ResponseEntity<Map<String, Object>> cancelScheduledTransaction(
            @PathVariable Long scheduledId) {
        Map<String, Object> res = new HashMap<>();
        try {
            Long userId = getCurrentUserId();
            scheduledTransactionService.cancelScheduledTransaction(userId, scheduledId);
            
            res.put("message", "Hủy giao dịch đặt lịch thành công");
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

