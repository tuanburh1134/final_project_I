package com.example.financeapp.service.impl;

import com.example.financeapp.dto.CreateWalletRequest;
import com.example.financeapp.entity.Currency;
import com.example.financeapp.entity.User;
import com.example.financeapp.entity.Wallet;
import com.example.financeapp.repository.CurrencyRepository;
import com.example.financeapp.repository.TransactionRepository;
import com.example.financeapp.repository.UserRepository;
import com.example.financeapp.repository.WalletRepository;
import com.example.financeapp.service.WalletService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
public class WalletServiceImpl implements WalletService {

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CurrencyRepository currencyRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    // 🔁 Hàm đổi tiền giữa hai loại tiền
    private BigDecimal getExchangeRate(Currency from, Currency to) {
        if (from.getCurrencyCode().equals(to.getCurrencyCode())) {
            return BigDecimal.ONE;
        }
        return to.getRateToVnd().divide(from.getRateToVnd(), 4, RoundingMode.HALF_UP);
    }

    @Override
    @Transactional
    public Wallet createWallet(Long userId, CreateWalletRequest request) {
        // 1️⃣ Kiểm tra user tồn tại
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User không tồn tại"));

        // 2️⃣ Kiểm tra loại tiền có hợp lệ
        Currency currency = currencyRepository.findByCurrencyCode(request.getCurrencyCode())
                .orElseThrow(() -> new RuntimeException("Loại tiền không hợp lệ: " + request.getCurrencyCode()));

        // 3️⃣ Kiểm tra trùng tên ví trong cùng user
        if (walletRepository.existsByWalletNameAndUser_UserId(request.getWalletName(), userId)) {
            throw new RuntimeException("Bạn đã có ví tên \"" + request.getWalletName() + "\"");
        }

        // 4️⃣ Tạo ví mới
        Wallet wallet = new Wallet();
        wallet.setUser(user);
        wallet.setWalletName(request.getWalletName().trim());
        wallet.setCurrency(currency);
        wallet.setBalance(request.getBalance() != null ? request.getBalance() : BigDecimal.ZERO);
        wallet.setDescription(request.getDescription());
        wallet.setDefault(Boolean.TRUE.equals(request.getSetAsDefault()));

        if (wallet.isDefault()) {
            walletRepository.unsetDefaultWallet(userId, null);
        }

        return walletRepository.save(wallet);
    }

    @Override
    public List<Wallet> getWalletsByUserId(Long userId) {
        return walletRepository.findByUser_UserId(userId);
    }

    @Override
    public Wallet getWalletDetails(Long userId, Long walletId) {
        return walletRepository.findByWalletIdAndUser_UserId(walletId, userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy ví hoặc ví không thuộc quyền sở hữu của bạn."));
    }

    @Override
    @Transactional
    public Wallet updateWallet(Long userId, Long walletId, CreateWalletRequest request) {
        // 1️⃣ Tìm ví thuộc user
        Wallet wallet = walletRepository.findByWalletIdAndUser_UserId(walletId, userId)
                .orElseThrow(() -> new RuntimeException("Ví không tồn tại hoặc không thuộc về bạn"));

        // 2️⃣ Kiểm tra xem ví có giao dịch chưa
        boolean hasTransactions = transactionRepository.existsByWallet_WalletId(walletId);

        // 3️⃣ Nếu chưa có giao dịch → cho phép đổi loại tiền và số dư
        if (!hasTransactions) {
            if (!wallet.getCurrency().getCurrencyCode().equals(request.getCurrencyCode())) {
                Currency oldCurrency = wallet.getCurrency();
                Currency newCurrency = currencyRepository.findByCurrencyCode(request.getCurrencyCode())
                        .orElseThrow(() -> new RuntimeException("Không tìm thấy loại tiền tệ mới"));

                BigDecimal exchangeRate = getExchangeRate(oldCurrency, newCurrency);
                BigDecimal convertedBalance = wallet.getBalance().multiply(exchangeRate);

                wallet.setCurrency(newCurrency);
                wallet.setBalance(convertedBalance);
            }

            if (request.getBalance() != null) {
                wallet.setBalance(request.getBalance());
            }
        } else {
            // ❌ Đã có giao dịch → Không cho đổi loại tiền hoặc số dư
            if (!wallet.getCurrency().getCurrencyCode().equals(request.getCurrencyCode())) {
                throw new RuntimeException("Không thể đổi loại tiền khi ví đã có giao dịch");
            }

            if (request.getBalance() != null &&
                    request.getBalance().compareTo(wallet.getBalance()) != 0) {
                throw new RuntimeException("Không thể chỉnh sửa số dư khi ví đã có giao dịch");
            }
        }

        // ✅ Luôn cho sửa tên và mô tả
        wallet.setWalletName(request.getWalletName());
        wallet.setDescription(request.getDescription());

        return walletRepository.save(wallet);
    }

    @Override
    public void setDefaultWallet(Long userId, Long walletId) {
        walletRepository.unsetDefaultWallet(userId, walletId);
        walletRepository.setDefaultWallet(userId, walletId);
    }
}
