package com.example.financeapp.service;

import com.example.financeapp.dto.CreateScheduledTransactionRequest;
import com.example.financeapp.entity.ScheduledTransaction;

import java.util.List;

public interface ScheduledTransactionService {
    ScheduledTransaction createScheduledTransaction(Long userId, CreateScheduledTransactionRequest request);
    List<ScheduledTransaction> getAllScheduledTransactions(Long userId);
    List<ScheduledTransaction> getScheduledTransactionsByStatus(Long userId, ScheduledTransaction.ScheduledStatus status);
    List<ScheduledTransaction> getScheduledTransactionsByWallet(Long userId, Long walletId);
    ScheduledTransaction getScheduledTransactionById(Long userId, Long scheduledId);
    void cancelScheduledTransaction(Long userId, Long scheduledId);
    void executeScheduledTransaction(Long scheduledId); // Internal method - được gọi bởi scheduler
    List<ScheduledTransaction> getPendingTransactionsToExecute(); // Internal method - lấy các transaction cần thực hiện
}

