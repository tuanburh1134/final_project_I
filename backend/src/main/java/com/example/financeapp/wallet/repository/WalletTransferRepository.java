package com.example.financeapp.wallet.repository;

import com.example.financeapp.wallet.entity.WalletTransfer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface WalletTransferRepository extends JpaRepository<WalletTransfer, Long> {

    /**
     * Lấy tất cả transfers của user (theo thời gian giảm dần)
     */
    @Query("SELECT t FROM WalletTransfer t " +
            "LEFT JOIN FETCH t.fromWallet " +
            "LEFT JOIN FETCH t.toWallet " +
            "LEFT JOIN FETCH t.user " +
            "WHERE t.user.id = :userId " +
            "ORDER BY t.transferDate DESC")
    List<WalletTransfer> findByUser_IdOrderByTransferDateDesc(@Param("userId") Long userId);

    /**
     * Lấy transfers của một ví cụ thể
     */
    @Query("SELECT t FROM WalletTransfer t " +
            "LEFT JOIN FETCH t.fromWallet " +
            "LEFT JOIN FETCH t.toWallet " +
            "LEFT JOIN FETCH t.user " +
            "WHERE t.fromWallet.walletId = :walletId OR t.toWallet.walletId = :walletId " +
            "ORDER BY t.transferDate DESC")
    List<WalletTransfer> findByWalletId(@Param("walletId") Long walletId);

    List<WalletTransfer> findByFromWallet_WalletIdOrderByTransferDateDesc(Long walletId);

    List<WalletTransfer> findByToWallet_WalletIdOrderByTransferDateDesc(Long walletId);

    /**
     * Lấy transfers trong khoảng thời gian
     */
    @Query("SELECT t FROM WalletTransfer t " +
            "WHERE t.user.id = :userId " +
            "AND t.transferDate BETWEEN :startDate AND :endDate " +
            "ORDER BY t.transferDate DESC")
    List<WalletTransfer> findByUserAndDateRange(
            @Param("userId") Long userId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    @Query("SELECT COUNT(t) FROM WalletTransfer t " +
            "WHERE (t.fromWallet.walletId = :wallet1Id AND t.toWallet.walletId = :wallet2Id) " +
            "OR (t.fromWallet.walletId = :wallet2Id AND t.toWallet.walletId = :wallet1Id)")
    long countTransfersBetweenWallets(
            @Param("wallet1Id") Long wallet1Id,
            @Param("wallet2Id") Long wallet2Id
    );

    void deleteByFromWallet_WalletIdOrToWallet_WalletId(Long fromWalletId, Long toWalletId);

    long countByUser_Id(Long userId);  // <--- FIXED

    @Query("SELECT t FROM WalletTransfer t " +
            "LEFT JOIN FETCH t.user " +
            "LEFT JOIN FETCH t.fromWallet " +
            "LEFT JOIN FETCH t.toWallet " +
            "WHERE t.transferId = :transferId")
    Optional<WalletTransfer> findByIdWithUser(@Param("transferId") Long transferId);

    @Query("SELECT t FROM WalletTransfer t " +
            "LEFT JOIN FETCH t.user " +
            "LEFT JOIN FETCH t.fromWallet " +
            "LEFT JOIN FETCH t.toWallet " +
            "WHERE t.transferId = :transferId")
    Optional<WalletTransfer> findByIdForDelete(@Param("transferId") Long transferId);
}
