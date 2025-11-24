package com.example.financeapp.transaction.repository;

import com.example.financeapp.transaction.schedule.ScheduleStatus;
import com.example.financeapp.transaction.schedule.ScheduledTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface ScheduledTransactionRepository extends JpaRepository<ScheduledTransaction, Long> {

    List<ScheduledTransaction> findByUser_UserIdOrderByCreatedAtDesc(Long userId);

    @Query("""
            SELECT s FROM ScheduledTransaction s
            WHERE s.status = :status
              AND s.nextRunAt <= :now
            """)
    List<ScheduledTransaction> findDueSchedules(
            @Param("status") ScheduleStatus status,
            @Param("now") LocalDateTime now
    );

    java.util.Optional<ScheduledTransaction> findByScheduleIdAndUser_UserId(Long scheduleId, Long userId);
}

