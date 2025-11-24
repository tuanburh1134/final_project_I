package com.example.financeapp.transaction.repository;

import com.example.financeapp.transaction.entity.Transaction;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;

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
            WHERE t.category.categoryId = :categoryId
              AND t.transactionDate >= :startDate
              AND t.transactionDate < :endDate
              AND t.wallet.walletId IN :walletIds
            """)
    BigDecimal sumBudgetSpending(
            @Param("categoryId") Long categoryId,
            @Param("walletIds") List<Long> walletIds,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    @Query("""
            SELECT t FROM Transaction t
            JOIN FETCH t.wallet w
            JOIN FETCH t.category c
            WHERE t.category.categoryId = :categoryId
              AND t.transactionDate >= :startDate
              AND t.transactionDate < :endDate
              AND w.walletId IN :walletIds
            ORDER BY t.transactionDate DESC
            """)
    List<Transaction> findBudgetTransactions(
            @Param("categoryId") Long categoryId,
            @Param("walletIds") List<Long> walletIds,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    boolean existsByUser_UserIdAndTransactionDateBetween(
            Long userId,
            LocalDateTime startDate,
            LocalDateTime endDate
    );

    Transaction findTopByUser_UserIdOrderByTransactionDateDesc(Long userId);
}
