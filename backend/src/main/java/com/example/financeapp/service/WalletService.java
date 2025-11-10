package com.example.financeapp.service;

import com.example.financeapp.dto.CreateWalletRequest;
import com.example.financeapp.entity.Wallet;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface WalletService {
    Wallet createWallet(Long userId, CreateWalletRequest request);
    List<Wallet> getWalletsByUserId(Long userId);
    Wallet updateWallet(Long userId, Long walletId, Map<String, Object> updates);
    BigDecimal calculateWalletBalance(Long walletId);
    Map<String, Object> getWalletDetail(Long walletId);
}