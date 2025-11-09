package com.example.financeapp.service.impl;

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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class WalletServiceImpl implements WalletService {

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CurrencyRepository currencyRepository;

    @Override
    @Transactional
    public Wallet createWallet(Long userId, CreateWalletRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User không tồn tại"));

        if (!currencyRepository.existsById(request.getCurrencyCode())) {
            throw new RuntimeException("Loại tiền tệ không hợp lệ: " + request.getCurrencyCode());
        }

        if (walletRepository.existsByWalletNameAndUser_UserId(request.getWalletName(), userId)) {
            throw new RuntimeException("Bạn đã có ví tên \"" + request.getWalletName() + "\"");
        }

        Wallet wallet = new Wallet();
        wallet.setUser(user);
        wallet.setWalletName(request.getWalletName().trim());
        wallet.setCurrencyCode(request.getCurrencyCode().toUpperCase());
        wallet.setBalance(BigDecimal.ZERO);
        wallet.setDescription(request.getDescription());

        return walletRepository.save(wallet);
    }

    @Override
    public List<Wallet> getWalletsByUserId(Long userId) {
        return walletRepository.findByUser_UserId(userId);
    }

    @Override
    public BigDecimal calculateWalletBalance(Long walletId) {
        List<Transaction> transactions = transactionRepository
                .findByWallet_WalletIdOrderByCreatedAtDesc(walletId);

        return transactions.stream()
                .map(t -> "INCOME".equalsIgnoreCase(t.getType()) ? t.getAmount() : t.getAmount().negate())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    public Map<String, Object> getWalletDetail(Long walletId) {
        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new RuntimeException("Ví không được tìm thấy"));

        List<Transaction> transactions = transactionRepository
                .findByWallet_WalletIdOrderByCreatedAtDesc(walletId);

        BigDecimal totalIncome = BigDecimal.ZERO;
        BigDecimal totalExpense = BigDecimal.ZERO;

        for (Transaction t : transactions) {
            if ("INCOME".equalsIgnoreCase(t.getType())) {
                totalIncome = totalIncome.add(t.getAmount());
            } else {
                totalExpense = totalExpense.add(t.getAmount());
            }
        }

        BigDecimal currentBalance = totalIncome.subtract(totalExpense);

        Map<String, Object> detail = new HashMap<>();
        detail.put("walletId", wallet.getWalletId());
        detail.put("walletName", wallet.getWalletName());
        detail.put("currencyCode", wallet.getCurrencyCode());
        detail.put("description", wallet.getDescription());
        detail.put("currentBalance", currentBalance);
        detail.put("totalIncome", totalIncome);
        detail.put("totalExpense", totalExpense);
        detail.put("transactions", transactions);

        Optional<Currency> currencyOpt = currencyRepository.findById(wallet.getCurrencyCode());
        detail.put("symbol", currencyOpt.map(Currency::getSymbol).orElse(wallet.getCurrencyCode()));

        return detail;
    }
}