package com.example.financeapp.wallet.repository;

import com.example.financeapp.wallet.entity.Currency;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CurrencyRepository extends JpaRepository<Currency, String> {

    // Vì id = currencyCode -> existsById là đủ
    boolean existsById(String currencyCode);

    // Tìm theo ID luôn -> không cần method riêng
    Optional<Currency> findById(String currencyCode);
}
