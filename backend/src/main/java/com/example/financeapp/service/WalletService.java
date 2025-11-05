package com.example.financeapp.service;

import com.example.financeapp.dto.CreateWalletRequest;
import com.example.financeapp.dto.WalletResponse;
import com.example.financeapp.entity.WalletType;

import java.util.List;

public interface WalletService {
    
    /**
     * Tạo ví mới
     */
    WalletResponse createWallet(String userEmail, CreateWalletRequest request);
    
    /**
     * Lấy danh sách tất cả ví của user
     */
    List<WalletResponse> getAllWallets(String userEmail);
    
    /**
     * Lấy danh sách ví active của user
     */
    List<WalletResponse> getActiveWallets(String userEmail);
    
    /**
     * Lấy danh sách ví theo loại
     */
    List<WalletResponse> getWalletsByType(String userEmail, WalletType walletType);
    
    /**
     * Lấy thông tin chi tiết 1 ví
     */
    WalletResponse getWalletById(String userEmail, Long walletId);
    
    /**
     * Lấy tổng số dư tất cả ví
     */
    String getTotalBalance(String userEmail);
}

