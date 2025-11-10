package com.example.financeapp.controller;

import com.example.financeapp.dto.CreateWalletRequest;
import com.example.financeapp.entity.Currency;
import com.example.financeapp.entity.Transaction;
import com.example.financeapp.entity.User;
import com.example.financeapp.entity.Wallet;
import com.example.financeapp.repository.CurrencyRepository;
import com.example.financeapp.repository.TransactionRepository;
import com.example.financeapp.repository.UserRepository;
import com.example.financeapp.repository.WalletRepository;
import com.example.financeapp.service.WalletService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.*;

@RestController
@RequestMapping("/wallets")
@CrossOrigin(origins = "*")
public class WalletController {

    @Autowired
    private WalletService walletService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CurrencyRepository currencyRepository;

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    // TẠO VÍ
    @PostMapping("/create")
    public ResponseEntity<Map<String, Object>> createWallet(@Valid @RequestBody CreateWalletRequest request) {
        Map<String, Object> res = new HashMap<>();
        try {
            String email = SecurityContextHolder.getContext().getAuthentication().getName();
            Optional<User> userOpt = userRepository.findByEmail(email);
            if (userOpt.isEmpty()) {
                res.put("error", "Không tìm thấy user");
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
        }
    }

    // XEM TẤT CẢ VÍ
    @GetMapping
    public ResponseEntity<?> getMyWallets() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) return ResponseEntity.status(401).build();

        Long userId = userOpt.get().getUserId();
        return ResponseEntity.ok(walletService.getWalletsByUserId(userId));
    }

    // THÊM GIAO DỊCH (THU / CHI)
    @PostMapping("/{walletId}/transactions")
    @Transactional
    public ResponseEntity<Map<String, Object>> addTransaction(
            @PathVariable Long walletId,
            @RequestBody Map<String, Object> request) {

        Map<String, Object> res = new HashMap<>();

        try {
            String email = SecurityContextHolder.getContext().getAuthentication().getName();
            Optional<User> userOpt = userRepository.findByEmail(email);
            if (userOpt.isEmpty()) {
                res.put("error", "Không tìm thấy user");
                return ResponseEntity.status(401).body(res);
            }

            BigDecimal amount = new BigDecimal(request.get("amount").toString()).abs();
            String type = ((String) request.get("type")).trim().toUpperCase();
            String description = (String) request.getOrDefault("description", "");

            if (!"INCOME".equals(type) && !"EXPENSE".equals(type)) {
                res.put("error", "Type phải là INCOME hoặc EXPENSE");
                return ResponseEntity.badRequest().body(res);
            }

            Optional<Wallet> walletOpt = walletRepository.findById(walletId);
            if (walletOpt.isEmpty() || !walletOpt.get().getUser().getUserId().equals(userOpt.get().getUserId())) {
                res.put("error", "Ví không tồn tại hoặc không thuộc bạn");
                return ResponseEntity.status(403).body(res);
            }

            Wallet wallet = walletOpt.get();

            // TẠO GIAO DỊCH
            Transaction transaction = new Transaction();
            transaction.setWallet(wallet);
            transaction.setAmount(amount);
            transaction.setType(type);
            transaction.setDescription(description);
            transactionRepository.save(transaction);

            // CẬP NHẬT SỐ DƯ TRONG VÍ
            if ("INCOME".equals(type)) {
                wallet.setBalance(wallet.getBalance().add(amount));
            } else {
                wallet.setBalance(wallet.getBalance().subtract(amount));
            }
            walletRepository.save(wallet);

            res.put("message", "Giao dịch thành công");
            res.put("transaction", transaction);
            res.put("newBalance", wallet.getBalance());
            return ResponseEntity.ok(res);

        } catch (Exception e) {
            res.put("error", "Lỗi: " + e.getMessage());
            return ResponseEntity.status(500).body(res);
        }
    }

    // XEM SỐ DƯ CÁC VÍ (TỔNG QUAN)
    @GetMapping("/balances")
    public ResponseEntity<List<Map<String, Object>>> getWalletBalances() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) return ResponseEntity.status(401).build();

        Long userId = userOpt.get().getUserId();
        List<Wallet> wallets = walletService.getWalletsByUserId(userId);

        List<Map<String, Object>> balances = new ArrayList<>();
        for (Wallet wallet : wallets) {
            Map<String, Object> info = new HashMap<>();
            info.put("walletId", wallet.getWalletId());
            info.put("walletName", wallet.getWalletName());
            info.put("balance", wallet.getBalance()); // DÙNG TRỰC TIẾP TỪ DB
            info.put("currencyCode", wallet.getCurrencyCode());

            Optional<Currency> currencyOpt = currencyRepository.findById(wallet.getCurrencyCode());
            info.put("symbol", currencyOpt.map(Currency::getSymbol).orElse(wallet.getCurrencyCode()));
            info.put("description", wallet.getDescription());

            balances.add(info);
        }
        return ResponseEntity.ok(balances);
    }

    // XEM CHI TIẾT VÍ
    @GetMapping("/{walletId}/detail")
    public ResponseEntity<Map<String, Object>> getWalletDetail(@PathVariable Long walletId) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) return ResponseEntity.status(401).build();

        Long userId = userOpt.get().getUserId();
        Optional<Wallet> walletOpt = walletRepository.findById(walletId);
        if (walletOpt.isEmpty() || !walletOpt.get().getUser().getUserId().equals(userId)) {
            return ResponseEntity.status(403).body(Map.of("error", "Không có quyền"));
        }

        return ResponseEntity.ok(walletService.getWalletDetail(walletId));
    }
    // CHỈNH SỬA VÍ
    @PutMapping("/{walletId}/update")
    @Transactional
    public ResponseEntity<Map<String, Object>> updateWallet(
            @PathVariable Long walletId,
            @RequestBody Map<String, Object> request) {

        Map<String, Object> res = new HashMap<>();

        try {
            String email = SecurityContextHolder.getContext().getAuthentication().getName();
            Optional<User> userOpt = userRepository.findByEmail(email);
            if (userOpt.isEmpty()) {
                res.put("error", "Không tìm thấy user");
                return ResponseEntity.status(401).body(res);
            }

            Optional<Wallet> walletOpt = walletRepository.findById(walletId);
            if (walletOpt.isEmpty()) {
                res.put("error", "Ví không tồn tại");
                return ResponseEntity.badRequest().body(res);
            }

            Wallet wallet = walletOpt.get();
            if (!wallet.getUser().getUserId().equals(userOpt.get().getUserId())) {
                res.put("error", "Bạn không có quyền chỉnh sửa ví này");
                return ResponseEntity.status(403).body(res);
            }

            // Cập nhật thông tin ví
            if (request.containsKey("walletName")) {
                wallet.setWalletName(request.get("walletName").toString());
            }
            if (request.containsKey("type")) {
                wallet.setType(request.get("type").toString());
            }
            if (request.containsKey("currencyCode")) {
                String currencyCode = request.get("currencyCode").toString();
                Optional<Currency> currencyOpt = currencyRepository.findById(currencyCode);
                if (currencyOpt.isEmpty()) {
                    res.put("error", "Đơn vị tiền tệ không hợp lệ");
                    return ResponseEntity.badRequest().body(res);
                }
                wallet.setCurrencyCode(currencyCode);
            }
            if (request.containsKey("description")) {
                wallet.setDescription(request.get("description").toString());
            }

            walletRepository.save(wallet);

            res.put("message", "Cập nhật ví thành công");
            res.put("wallet", wallet);
            return ResponseEntity.ok(res);

        } catch (Exception e) {
            res.put("error", "Lỗi: " + e.getMessage());
            return ResponseEntity.status(500).body(res);
        }
    }

}