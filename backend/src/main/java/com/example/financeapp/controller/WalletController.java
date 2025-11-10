package com.example.financeapp.controller;

import com.example.financeapp.dto.CreateWalletRequest;
import com.example.financeapp.entity.User;
import com.example.financeapp.entity.Wallet;
import com.example.financeapp.repository.UserRepository;
import com.example.financeapp.repository.WalletRepository;
import com.example.financeapp.service.WalletService;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/wallets")
@CrossOrigin(origins = "*")
public class WalletController {

    @Autowired
    private WalletService walletService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WalletRepository walletRepository;

    @PostMapping("/create")
    public ResponseEntity<Map<String, Object>> createWallet(@Valid @RequestBody CreateWalletRequest request) {
        Map<String, Object> res = new HashMap<>();
        try {
            String email = SecurityContextHolder.getContext().getAuthentication().getName();
            Optional<User> userOpt = userRepository.findByEmail(email);

            if (userOpt.isEmpty()) {
                res.put("error", "Không tìm thấy user với email: " + email);
                return ResponseEntity.status(401).body(res);
            }

            Long userId = userOpt.get().getUserId();
            Wallet wallet = walletService.createWallet(userId, request);

            res.put("message", "Tạo ví thành công");
            res.put("wallet", wallet);
            return ResponseEntity.ok(res);
        } catch (RuntimeException e) {
            res.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(res);
        } catch (Exception e) {
            res.put("error", "Lỗi máy chủ nội bộ: " + e.getMessage());
            return ResponseEntity.status(500).body(res);
        }
    }

    @GetMapping
    public ResponseEntity<?> getMyWallets() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Optional<User> userOpt = userRepository.findByEmail(email);

        if (userOpt.isEmpty()) {
            Map<String, Object> res = new HashMap<>();
            res.put("error", "Không tìm thấy thông tin user từ token");
            return ResponseEntity.status(401).body(res);
        }

        Long userId = userOpt.get().getUserId();
        return ResponseEntity.ok(walletService.getWalletsByUserId(userId));
    }

    @GetMapping("/{walletId}")
    public ResponseEntity<Map<String, Object>> getWalletDetails(@PathVariable Long walletId) {
        Map<String, Object> res = new HashMap<>();
        try {
            String email = SecurityContextHolder.getContext().getAuthentication().getName();
            Optional<User> userOpt = userRepository.findByEmail(email);

            if (userOpt.isEmpty()) {
                res.put("error", "Không tìm thấy thông tin user từ token");
                return ResponseEntity.status(401).body(res);
            }
            Long userId = userOpt.get().getUserId();

            Wallet wallet = walletService.getWalletDetails(userId, walletId);
            res.put("wallet", wallet);
            return ResponseEntity.ok(res);
        } catch (RuntimeException e) {
            res.put("error", e.getMessage());
            return ResponseEntity.status(404).body(res);
        } catch (Exception e) {
            res.put("error", "Lỗi máy chủ nội bộ: " + e.getMessage());
            return ResponseEntity.status(500).body(res);
        }
    }

    // ✅ API sửa ví
    @PutMapping("/{walletId}/update")
    public ResponseEntity<Map<String, Object>> updateWallet(
            @PathVariable Long walletId,
            @Valid @RequestBody CreateWalletRequest request) {

        Map<String, Object> res = new HashMap<>();
        try {
            String email = SecurityContextHolder.getContext().getAuthentication().getName();
            Long userId = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User không tồn tại"))
                    .getUserId();

            Wallet updatedWallet = walletService.updateWallet(userId, walletId, request);
            res.put("message", "Cập nhật ví thành công");
            res.put("wallet", updatedWallet);
            return ResponseEntity.ok(res);
        } catch (RuntimeException e) {
            res.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(res);
        } catch (Exception e) {
            res.put("error", "Lỗi máy chủ: " + e.getMessage());
            return ResponseEntity.status(500).body(res);
        }
    }

    @PatchMapping("/{walletId}/set-default")
    @Transactional
    public ResponseEntity<Map<String, Object>> setDefaultWallet(@PathVariable Long walletId) {
        Map<String, Object> res = new HashMap<>();
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Long userId = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User không tồn tại")).getUserId();

        walletRepository.findByWalletIdAndUser_UserId(walletId, userId)
                .orElseThrow(() -> new RuntimeException("Ví không tồn tại hoặc không thuộc về bạn"));

        walletRepository.unsetDefaultWallet(userId, walletId);
        walletRepository.setDefaultWallet(userId, walletId);

        res.put("message", "Đặt ví mặc định thành công");
        return ResponseEntity.ok(res);
    }
}
