package com.example.financeapp.controller;

import com.example.financeapp.dto.CreateWalletRequest;
import com.example.financeapp.dto.UpdateWalletRequest;
import com.example.financeapp.dto.WalletResponse;
import com.example.financeapp.entity.WalletType;
import com.example.financeapp.service.WalletService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/wallet")
@CrossOrigin(origins = "*")
public class WalletController {

    @Autowired
    private WalletService walletService;

    /**
     * Tạo ví mới
     * POST /wallet/create
     */
    @PostMapping("/create")
    public ResponseEntity<Map<String, Object>> createWallet(
            @Valid @RequestBody CreateWalletRequest request,
            Authentication authentication) {

        Map<String, Object> response = new HashMap<>();

        try {
            String email = authentication.getName();
            WalletResponse wallet = walletService.createWallet(email, request);

            response.put("success", true);
            response.put("message", "Tạo ví thành công");
            response.put("wallet", wallet);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("error", "Lỗi hệ thống: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Lấy danh sách tất cả ví
     * GET /wallet/list
     */
    @GetMapping("/list")
    public ResponseEntity<Map<String, Object>> getAllWallets(Authentication authentication) {
        Map<String, Object> response = new HashMap<>();

        try {
            String email = authentication.getName();
            List<WalletResponse> wallets = walletService.getAllWallets(email);

            response.put("success", true);
            response.put("wallets", wallets);
            response.put("total", wallets.size());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    /**
     * Lấy danh sách ví đang hoạt động
     * GET /wallet/active
     */
    @GetMapping("/active")
    public ResponseEntity<Map<String, Object>> getActiveWallets(Authentication authentication) {
        Map<String, Object> response = new HashMap<>();

        try {
            String email = authentication.getName();
            List<WalletResponse> wallets = walletService.getActiveWallets(email);

            response.put("success", true);
            response.put("wallets", wallets);
            response.put("total", wallets.size());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    /**
     * Lấy danh sách ví theo loại
     * GET /wallet/type/{walletType}
     */
    @GetMapping("/type/{walletType}")
    public ResponseEntity<Map<String, Object>> getWalletsByType(
            @PathVariable String walletType,
            Authentication authentication) {

        Map<String, Object> response = new HashMap<>();

        try {
            String email = authentication.getName();
            WalletType type = WalletType.valueOf(walletType.toUpperCase());
            List<WalletResponse> wallets = walletService.getWalletsByType(email, type);

            response.put("success", true);
            response.put("wallets", wallets);
            response.put("walletType", type.getDisplayName());
            response.put("total", wallets.size());
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("error", "Loại ví không hợp lệ. Chọn: CASH, BANK, hoặc EWALLET");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    /**
     * Lấy chi tiết 1 ví
     * GET /wallet/{walletId}
     */
    @GetMapping("/{walletId}")
    public ResponseEntity<Map<String, Object>> getWalletById(
            @PathVariable Long walletId,
            Authentication authentication) {

        Map<String, Object> response = new HashMap<>();

        try {
            String email = authentication.getName();
            WalletResponse wallet = walletService.getWalletById(email, walletId);

            response.put("success", true);
            response.put("wallet", wallet);
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("error", "Lỗi hệ thống: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Cập nhật thông tin ví (tên, loại ví, đơn vị tiền tệ)
     * PUT /wallet/{walletId}
     */
    @PutMapping("/{walletId}")
    public ResponseEntity<Map<String, Object>> updateWallet(
            @PathVariable Long walletId,
            @Valid @RequestBody UpdateWalletRequest request,
            Authentication authentication) {

        Map<String, Object> response = new HashMap<>();

        try {
            String email = authentication.getName();
            WalletResponse wallet = walletService.updateWallet(email, walletId, request);

            response.put("success", true);
            response.put("message", "Cập nhật ví thành công");
            response.put("wallet", wallet);
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("error", "Lỗi hệ thống: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Lấy tổng số dư tất cả ví
     * GET /wallet/summary
     */
    @GetMapping("/summary")
    public ResponseEntity<Map<String, Object>> getWalletSummary(Authentication authentication) {
        Map<String, Object> response = new HashMap<>();

        try {
            String email = authentication.getName();
            String totalBalance = walletService.getTotalBalance(email);
            List<WalletResponse> activeWallets = walletService.getActiveWallets(email);

            response.put("success", true);
            response.put("totalBalance", totalBalance);
            response.put("totalWallets", activeWallets.size());
            response.put("wallets", activeWallets);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }
}

