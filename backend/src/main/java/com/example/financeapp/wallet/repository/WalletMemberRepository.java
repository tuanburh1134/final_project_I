package com.example.financeapp.wallet.repository;

import com.example.financeapp.wallet.entity.WalletMember;
import com.example.financeapp.wallet.entity.WalletMember.WalletRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WalletMemberRepository extends JpaRepository<WalletMember, Long> {

    // Tìm tất cả members của 1 wallet
    List<WalletMember> findByWallet_WalletId(Long walletId);

    // Tìm tất cả wallets mà user tham gia
    List<WalletMember> findByUser_Id(Long userId);

    // Tìm tất cả wallets mà user là owner
    List<WalletMember> findByUser_IdAndRole(Long userId, WalletRole role);

    // Kiểm tra user có phải member không
    boolean existsByWallet_WalletIdAndUser_Id(Long walletId, Long userId);

    // Tìm 1 member cụ thể
    Optional<WalletMember> findByWallet_WalletIdAndUser_Id(Long walletId, Long userId);

    // Xóa member khỏi wallet
    void deleteByWallet_WalletIdAndUser_Id(Long walletId, Long userId);

    // Kiểm tra user có phải owner không
    @Query("""
        SELECT CASE WHEN COUNT(wm) > 0 THEN TRUE ELSE FALSE END
        FROM WalletMember wm
        WHERE wm.wallet.walletId = :walletId
          AND wm.user.id = :userId
          AND wm.role = com.example.financeapp.wallet.entity.WalletMember.WalletRole.OWNER
    """)
    boolean isOwner(@Param("walletId") Long walletId, @Param("userId") Long userId);

    // Đếm số lượng member
    long countByWallet_WalletId(Long walletId);

    // Lấy owner
    Optional<WalletMember> findByWallet_WalletIdAndRole(Long walletId, WalletRole role);
}
