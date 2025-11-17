package com.example.financeapp.repository;

import com.example.financeapp.entity.ScheduledTransaction;
import com.example.financeapp.entity.ScheduledTransaction.ScheduledStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface ScheduledTransactionRepository extends JpaRepository<ScheduledTransaction, Long> {

    /**
     * Lấy tất cả scheduled transactions của user
     */
    List<ScheduledTransaction> findByUser_UserIdOrderByScheduledDateAsc(Long userId);

    /**
     * Lấy scheduled transactions theo status
     */
    List<ScheduledTransaction> findByUser_UserIdAndStatusOrderByScheduledDateAsc(Long userId, ScheduledStatus status);

    /**
     * Lấy scheduled transactions theo wallet
     */
    List<ScheduledTransaction> findByWallet_WalletIdAndUser_UserIdOrderByScheduledDateAsc(Long walletId, Long userId);

    /**
     * Lấy tất cả scheduled transactions cần thực hiện (PENDING và scheduledDate <= now)
     */
    @Query("SELECT st FROM ScheduledTransaction st " +
           "WHERE st.status = :status " +
           "AND st.scheduledDate <= :now " +
           "ORDER BY st.scheduledDate ASC")
    List<ScheduledTransaction> findPendingTransactionsToExecute(
            @Param("status") ScheduledStatus status,
            @Param("now") LocalDateTime now
    );
}

