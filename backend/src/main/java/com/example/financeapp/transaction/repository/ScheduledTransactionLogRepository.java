package com.example.financeapp.transaction.repository;

import com.example.financeapp.transaction.schedule.ScheduledTransaction;
import com.example.financeapp.transaction.schedule.ScheduledTransactionLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ScheduledTransactionLogRepository extends JpaRepository<ScheduledTransactionLog, Long> {

    List<ScheduledTransactionLog> findByScheduledTransactionOrderByExecutedAtDesc(ScheduledTransaction scheduledTransaction);
}

