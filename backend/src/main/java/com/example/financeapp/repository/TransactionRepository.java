package com.example.financeapp.repository;

import com.example.financeapp.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findByUser_UserIdOrderByTransactionDateDesc(Long userId);
}
import org.springframework.stereotype.Repository;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    // Kiểm tra xem ví có giao dịch nào hay chưa
    boolean existsByWallet_WalletId(Long walletId);
}
