package com.example.financeapp.transaction.repository;

import com.example.financeapp.transaction.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    // Lấy giao dịch theo user
    List<Transaction> findByUser_UserIdOrderByTransactionDateDesc(Long userId);

    // Kiểm tra wallet có giao dịch không
    boolean existsByWallet_WalletId(Long walletId);

    // ===== MERGE WALLET METHODS =====

    /**
     * Lấy tất cả transactions của một wallet
     */
    List<Transaction> findByWallet_WalletId(Long walletId);

    /**
     * Đếm số lượng transactions trong wallet
     */
    long countByWallet_WalletId(Long walletId);

    /**
     * Update wallet_id cho tất cả transactions (khi merge)
     * Chuyển tất cả transactions từ sourceWalletId sang targetWalletId
     */
    @Modifying
    @Query("UPDATE Transaction t SET t.wallet.walletId = :targetWalletId " +
            "WHERE t.wallet.walletId = :sourceWalletId")
    int updateWalletIdForAllTransactions(
            @Param("sourceWalletId") Long sourceWalletId,
            @Param("targetWalletId") Long targetWalletId
    );

    /**
     * Kiểm tra category có trong giao dịch không
     */
    boolean existsByCategory_CategoryId(Long categoryId);

    @Query("""
            SELECT COALESCE(SUM(t.amount), 0)
            FROM Transaction t
            WHERE t.user.userId = :userId
              AND t.category.categoryId = :categoryId
              AND t.transactionDate BETWEEN :startDate AND :endDate
              AND (:walletId IS NULL OR t.wallet.walletId = :walletId)
            """)
    BigDecimal sumExpensesForBudget(@Param("userId") Long userId,
                                    @Param("categoryId") Long categoryId,
                                    @Param("walletId") Long walletId,
                                    @Param("startDate") LocalDateTime startDate,
                                    @Param("endDate") LocalDateTime endDate);

    @Query("""
            SELECT t
            FROM Transaction t
            WHERE t.user.userId = :userId
              AND t.category.categoryId = :categoryId
              AND t.transactionDate BETWEEN :startDate AND :endDate
              AND (:walletId IS NULL OR t.wallet.walletId = :walletId)
            ORDER BY t.transactionDate DESC
            """)
    List<Transaction> findTransactionsForBudget(@Param("userId") Long userId,
                                                @Param("categoryId") Long categoryId,
                                                @Param("walletId") Long walletId,
                                                @Param("startDate") LocalDateTime startDate,
                                                @Param("endDate") LocalDateTime endDate);

    @Query("""
            SELECT t
            FROM Transaction t
            WHERE t.user.userId = :userId
              AND (:startDate IS NULL OR t.transactionDate >= :startDate)
              AND (:endDate IS NULL OR t.transactionDate <= :endDate)
              AND (:walletId IS NULL OR t.wallet.walletId = :walletId)
            ORDER BY t.transactionDate DESC
            """)
    List<Transaction> findTransactionsForReport(@Param("userId") Long userId,
                                                @Param("startDate") LocalDateTime startDate,
                                                @Param("endDate") LocalDateTime endDate,
                                                @Param("walletId") Long walletId);
}
