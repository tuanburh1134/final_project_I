package com.example.financeapp.transaction.service;

import com.example.financeapp.transaction.dto.CreateTransactionRequest;
import com.example.financeapp.transaction.dto.TransactionResult;
import com.example.financeapp.transaction.dto.UpdateTransactionRequest;
import com.example.financeapp.transaction.entity.Transaction;

import java.time.LocalDate;
import java.util.List;

public interface TransactionService {
    TransactionResult createExpense(Long userId, CreateTransactionRequest request);
    TransactionResult createIncome(Long userId, CreateTransactionRequest request);
    Transaction updateTransaction(Long userId, Long transactionId, UpdateTransactionRequest request);
    void deleteTransaction(Long userId, Long transactionId);
    List<Transaction> getAllTransactions(Long userId);
    List<Transaction> getTransactionsForReport(Long userId, LocalDate startDate, LocalDate endDate, Long walletId);
}