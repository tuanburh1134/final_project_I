package com.example.financeapp.service;

import com.example.financeapp.dto.CreateTransactionRequest;
import com.example.financeapp.entity.Transaction;

import java.time.LocalDateTime;
import java.util.List;

public interface TransactionService {
    Transaction createExpense(Long userId, CreateTransactionRequest request);
    Transaction createIncome(Long userId, CreateTransactionRequest request);
    
    // GET methods
    List<Transaction> getAllTransactions(Long userId, Long walletId, Long typeId, LocalDateTime startDate, LocalDateTime endDate);
    Transaction getTransactionById(Long userId, Long transactionId);
    List<Transaction> getTransactionsByWallet(Long userId, Long walletId);
}