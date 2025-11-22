package com.example.financeapp.scheduled.repository;

import com.example.financeapp.scheduled.entity.ScheduledTransaction;
import com.example.financeapp.scheduled.entity.ScheduledTransaction.ScheduleStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import jakarta.persistence.LockModeType;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ScheduledTransactionRepository extends JpaRepository<ScheduledTransaction, Long> {

    List<ScheduledTransaction> findByUser_UserIdOrderByScheduleTimeDesc(Long userId);

    List<ScheduledTransaction> findTop50ByStatusAndScheduleTimeBeforeOrderByScheduleTimeAsc(
            ScheduleStatus status, LocalDateTime scheduleTime);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<ScheduledTransaction> findByScheduleId(Long scheduleId);
}

