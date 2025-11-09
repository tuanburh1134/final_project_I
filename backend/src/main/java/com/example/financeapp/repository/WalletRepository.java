package com.example.financeapp.repository;

import com.example.financeapp.entity.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WalletRepository extends JpaRepository<Wallet, Long> {

    /**
     * Lấy danh sách tất cả ví thuộc về một người dùng.
     *
     * @param userId ID của người dùng
     * @return Danh sách ví
     */
    List<Wallet> findByUser_UserId(Long userId);

    /**
     * Kiểm tra xem người dùng đã có ví với tên cụ thể hay chưa.
     *
     * @param walletName tên ví
     * @param userId ID người dùng
     * @return true nếu tồn tại, false nếu chưa
     */
    boolean existsByWalletNameAndUser_UserId(String walletName, Long userId);

    /**
     * Tìm ví theo ID và ID người dùng — dùng để đảm bảo người dùng chỉ có thể truy cập ví của chính họ.
     *
     * @param walletId ID ví
     * @param userId ID người dùng
     * @return Optional<Wallet>
     */
    Optional<Wallet> findByWalletIdAndUser_UserId(Long walletId, Long userId);

    /**
     * Xóa ví dựa theo ID và người dùng — giúp bảo mật, tránh xóa nhầm ví của người khác.
     *
     * @param walletId ID ví
     * @param userId ID người dùng
     */
    void deleteByWalletIdAndUser_UserId(Long walletId, Long userId);
}
