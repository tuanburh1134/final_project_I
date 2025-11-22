package com.example.financeapp.budget.repository;

import com.example.financeapp.budget.entity.Budget;
import com.example.financeapp.category.entity.Category;
import com.example.financeapp.user.entity.User;
import com.example.financeapp.wallet.entity.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface BudgetRepository extends JpaRepository<Budget, Long> {

    List<Budget> findByUser_UserIdOrderByStartDateDesc(Long userId);

    // ← METHOD MỚI: kiểm tra trùng chính xác 100%
    @Query("""
        SELECT CASE WHEN COUNT(b) > 0 THEN true ELSE false END
        FROM Budget b
        WHERE b.user = :user
          AND b.category = :category
          AND (
                (b.wallet IS NULL AND :wallet IS NULL) 
             OR (b.wallet IS NOT NULL AND :wallet IS NOT NULL AND b.wallet.walletId = :walletId)
              )
          AND b.startDate = :startDate
          AND b.endDate = :endDate
        """)
    boolean existsExactlySameBudget(
            @Param("user") User user,
            @Param("category") Category category,
            @Param("wallet") Wallet wallet,
            @Param("walletId") Long walletId,   // để xử lý null an toàn
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    @Query("""
    SELECT CASE WHEN COUNT(b) > 0 THEN true ELSE false END
    FROM Budget b
    WHERE b.user = :user
      AND b.category.categoryId = :categoryId
      AND (
            (b.wallet IS NULL AND :walletId IS NULL)
         OR (b.wallet IS NOT NULL AND :walletId IS NOT NULL AND b.wallet.walletId = :walletId)
         OR (b.wallet IS NULL AND :walletId IS NOT NULL)
         OR (b.wallet IS NOT NULL AND :walletId IS NULL)
          )
      AND b.startDate <= :newEndDate
      AND b.endDate >= :newStartDate
    """)
    boolean existsOverlappingBudget(
            @Param("user") User user,
            @Param("categoryId") Long categoryId,
            @Param("walletId") Long walletId,
            @Param("newStartDate") LocalDate newStartDate,
            @Param("newEndDate") LocalDate newEndDate
    );

    @Query("""
            SELECT b FROM Budget b
            WHERE b.user.userId = :userId
              AND b.category.categoryId = :categoryId
              AND b.startDate <= :targetDate
              AND b.endDate >= :targetDate
              AND (:walletId IS NULL OR b.wallet IS NULL OR b.wallet.walletId = :walletId)
            ORDER BY CASE WHEN b.wallet IS NULL THEN 1 ELSE 0 END
            """)
    List<Budget> findApplicableBudgets(@Param("userId") Long userId,
                                       @Param("categoryId") Long categoryId,
                                       @Param("walletId") Long walletId,
                                       @Param("targetDate") LocalDate targetDate);
}