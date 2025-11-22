package com.example.financeapp.scheduled.repository;

import com.example.financeapp.scheduled.entity.ScheduledTransactionLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ScheduledTransactionLogRepository extends JpaRepository<ScheduledTransactionLog, Long> {

    List<ScheduledTransactionLog> findByScheduledTransaction_ScheduleIdOrderByRunAtDesc(Long scheduleId);
}

