package com.example.financeapp.service;

import com.example.financeapp.dto.CreateWalletRequest;
import com.example.financeapp.entity.Wallet;
import java.util.List;

public interface WalletService {

    /**
     * Tạo ví mới cho người dùng đã đăng nhập.
     *
     * @param userId  ID của người dùng (lấy từ token đăng nhập)
     * @param request Dữ liệu ví mới (tên ví, loại tiền, mô tả, số dư ban đầu)
     * @return Wallet đã được lưu vào database
     */
    Wallet createWallet(Long userId, CreateWalletRequest request);

    /**
     * Cập nhật thông tin ví (tên, loại tiền tệ, mô tả...).
     *
     * @param walletId     ID của ví cần cập nhật
     * @param name         Tên ví mới
     * @param currencyCode Mã tiền tệ mới (VD: VND, USD)
     * @return Wallet sau khi đã cập nhật
     */
    Wallet updateWallet(Long walletId, String name, String currencyCode);

    /**
     * Lấy danh sách tất cả ví của người dùng.
     *
     * @param userId ID của người dùng
     * @return Danh sách ví thuộc về người dùng
     */
    List<Wallet> getWalletsByUserId(Long userId);
}
