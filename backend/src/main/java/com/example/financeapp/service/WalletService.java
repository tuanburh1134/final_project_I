package com.example.financeapp.service;

import com.example.financeapp.dto.CreateWalletRequest;
import com.example.financeapp.dto.UpdateWalletRequest;
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
     * Cập nhật thông tin ví (tên, loại ví, đơn vị tiền tệ)
     */
    WalletResponse updateWallet(String userEmail, Long walletId, UpdateWalletRequest request);
    
    /**
     * Xóa ví (soft delete - set isActive = false)
     */
    void deleteWallet(String userEmail, Long walletId);
    
    /**
     * Đặt ví làm mặc định
     */
    WalletResponse setDefaultWallet(String userEmail, Long walletId);
    
    /**
     * Lấy ví mặc định của user
     */
    WalletResponse getDefaultWallet(String userEmail);
    
    /**
     * Lấy tổng số dư tất cả ví
     */
    String getTotalBalance(String userEmail);
}

