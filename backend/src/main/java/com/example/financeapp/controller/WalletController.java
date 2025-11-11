package com.example.financeapp.controller;

import com.example.financeapp.dto.*;
import com.example.financeapp.entity.User;
import com.example.financeapp.entity.Wallet;
import com.example.financeapp.repository.UserRepository;
import com.example.financeapp.service.CurrencyService;
import com.example.financeapp.service.WalletService;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
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

    /**
     * Helper method để lấy userId từ JWT token
     */
    private Long getCurrentUserId() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            throw new RuntimeException("Không tìm thấy thông tin user từ token");
        }
        return userOpt.get().getUserId();
    }

    // ============ EXISTING ENDPOINTS ============

    @PostMapping("/create")
    public ResponseEntity<Map<String, Object>> createWallet(@Valid @RequestBody CreateWalletRequest request) {
        Map<String, Object> res = new HashMap<>();
        try {
            Long userId = getCurrentUserId();
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
    public ResponseEntity<Map<String, Object>> getMyWallets() {
        Map<String, Object> res = new HashMap<>();
        try {
            Long userId = getCurrentUserId();

            // Lấy tất cả wallets có quyền truy cập (bao gồm owned và shared)
            List<SharedWalletDTO> wallets = walletService.getAllAccessibleWallets(userId);

            res.put("wallets", wallets);
            res.put("total", wallets.size());
            return ResponseEntity.ok(res);

        } catch (Exception e) {
            res.put("error", e.getMessage());
            return ResponseEntity.status(500).body(res);
        }
    }

    @GetMapping("/{walletId}")
    public ResponseEntity<Map<String, Object>> getWalletDetails(@PathVariable Long walletId) {
        Map<String, Object> res = new HashMap<>();
        try {
            Long userId = getCurrentUserId();
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

    @PatchMapping("/{walletId}/set-default")
    @Transactional
    public ResponseEntity<Map<String, Object>> setDefaultWallet(@PathVariable Long walletId) {
        Map<String, Object> res = new HashMap<>();
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Long userId = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User không tồn tại")).getUserId();

        walletService.setDefaultWallet(userId, walletId);

        res.put("message", "Đặt ví mặc định thành công");
        return ResponseEntity.ok(res);
    }
    // ============ SHARED WALLET ENDPOINTS ============

    /**
     * Chia sẻ ví với người dùng khác qua email
     * POST /wallets/{walletId}/share
     */
    @PostMapping("/{walletId}/share")
    public ResponseEntity<Map<String, Object>> shareWallet(
            @PathVariable Long walletId,
            @Valid @RequestBody ShareWalletRequest request) {
        Map<String, Object> res = new HashMap<>();
        try {
            Long ownerId = getCurrentUserId();

            WalletMemberDTO member = walletService.shareWallet(walletId, ownerId, request.getEmail());

            res.put("message", "Chia sẻ ví thành công");
            res.put("member", member);
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
     * Lấy danh sách thành viên của ví
     * GET /wallets/{walletId}/members
     */
    @GetMapping("/{walletId}/members")
    public ResponseEntity<Map<String, Object>> getWalletMembers(@PathVariable Long walletId) {
        Map<String, Object> res = new HashMap<>();
        try {
            Long userId = getCurrentUserId();

            List<WalletMemberDTO> members = walletService.getWalletMembers(walletId, userId);

            res.put("members", members);
            res.put("total", members.size());
            return ResponseEntity.ok(res);

        } catch (RuntimeException e) {
            res.put("error", e.getMessage());
            return ResponseEntity.status(403).body(res);
        } catch (Exception e) {
            res.put("error", "Lỗi máy chủ nội bộ: " + e.getMessage());
            return ResponseEntity.status(500).body(res);
        }
    }

    /**
     * Xóa thành viên khỏi ví (chỉ owner)
     * DELETE /wallets/{walletId}/members/{memberUserId}
     */
    @DeleteMapping("/{walletId}/members/{memberUserId}")
    public ResponseEntity<Map<String, Object>> removeMember(
            @PathVariable Long walletId,
            @PathVariable Long memberUserId) {
        Map<String, Object> res = new HashMap<>();
        try {
            Long ownerId = getCurrentUserId();

            walletService.removeMember(walletId, ownerId, memberUserId);

            res.put("message", "Xóa thành viên thành công");
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
     * Rời khỏi ví (member tự rời)
     * POST /wallets/{walletId}/leave
     */
    @PostMapping("/{walletId}/leave")
    public ResponseEntity<Map<String, Object>> leaveWallet(@PathVariable Long walletId) {
        Map<String, Object> res = new HashMap<>();
        try {
            Long userId = getCurrentUserId();

            walletService.leaveWallet(walletId, userId);

            res.put("message", "Bạn đã rời khỏi ví thành công");
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
     * Kiểm tra quyền truy cập của user đối với wallet
     * GET /wallets/{walletId}/access
     */
    @GetMapping("/{walletId}/access")
    public ResponseEntity<Map<String, Object>> checkAccess(@PathVariable Long walletId) {
        Map<String, Object> res = new HashMap<>();
        try {
            Long userId = getCurrentUserId();

            boolean hasAccess = walletService.hasAccess(walletId, userId);
            boolean isOwner = walletService.isOwner(walletId, userId);

            res.put("hasAccess", hasAccess);
            res.put("isOwner", isOwner);
            res.put("role", isOwner ? "OWNER" : (hasAccess ? "MEMBER" : "NONE"));

            return ResponseEntity.ok(res);

        } catch (Exception e) {
            res.put("error", "Lỗi máy chủ nội bộ: " + e.getMessage());
            return ResponseEntity.status(500).body(res);
        }
    }
    // * Cập nhật thông tin ví (chỉ owner)
   //  * PUT /wallets/{walletId}/update
    // */
    @PutMapping("/{walletId}/update")
    public ResponseEntity<Map<String, Object>> updateWallet(
            @PathVariable Long walletId,
            @Valid @RequestBody UpdateWalletRequest request) {
        Map<String, Object> res = new HashMap<>();
        try {
            Long userId = getCurrentUserId();

            Wallet updatedWallet = walletService.updateWallet(walletId, userId, request);

            res.put("message", "Cập nhật ví thành công");
            res.put("wallet", updatedWallet);
            return ResponseEntity.ok(res);

        } catch (RuntimeException e) {
            res.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(res);
        } catch (Exception e) {
            res.put("error", "Lỗi máy chủ nội bộ: " + e.getMessage());
            return ResponseEntity.status(500).body(res);
        }
    }
    @DeleteMapping("/{walletId}")
    public ResponseEntity<Map<String, Object>> deleteWallet(@PathVariable Long walletId) {
        Map<String, Object> res = new HashMap<>();
        try {
            Long userId = getCurrentUserId();
            walletService.softDeleteWallet(walletId, userId);
            res.put("message", "Ví đã được chuyển vào kho lưu trữ. Sẽ bị xoá vĩnh viễn sau 30 ngày.");
            return ResponseEntity.ok(res);
        } catch (RuntimeException e) {
            res.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(res);
        } catch (Exception e) {
            res.put("error", "Lỗi máy chủ: " + e.getMessage());
            return ResponseEntity.status(500).body(res);
        }
    }
    @GetMapping("/archived")
    public ResponseEntity<Map<String, Object>> getArchivedWallets() {
        Map<String, Object> res = new HashMap<>();
        try {
            Long userId = getCurrentUserId();
            List<Wallet> archivedWallets = walletService.getArchivedWallets(userId);

            res.put("wallets", archivedWallets);
            res.put("total", archivedWallets.size());
            return ResponseEntity.ok(res);
        } catch (Exception e) {
            res.put("error", "Lỗi máy chủ: " + e.getMessage());
            return ResponseEntity.status(500).body(res);
        }
    }

    /**
     * 🔄 Khôi phục ví từ kho lưu trữ
     */
    @PostMapping("/{walletId}/restore")
    public ResponseEntity<Map<String, Object>> restoreWallet(@PathVariable Long walletId) {
        Map<String, Object> res = new HashMap<>();
        try {
            Long userId = getCurrentUserId();
            walletService.restoreWallet(walletId, userId);
            res.put("message", "Khôi phục ví thành công");
            return ResponseEntity.ok(res);
        } catch (RuntimeException e) {
            res.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(res);
        } catch (Exception e) {
            res.put("error", "Lỗi máy chủ: " + e.getMessage());
            return ResponseEntity.status(500).body(res);
        }
    }


}
