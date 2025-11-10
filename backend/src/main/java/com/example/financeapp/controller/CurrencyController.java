package com.example.financeapp.controller;

import com.example.financeapp.entity.Currency;
import com.example.financeapp.repository.CurrencyRepository;
import com.example.financeapp.service.ExchangeRateUpdater;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/currencies")
public class CurrencyController {

    @Autowired
    private CurrencyRepository currencyRepository;

    @Autowired
    private ExchangeRateUpdater exchangeRateUpdater;

    /**
     * 🔹 Lấy danh sách tất cả các loại tiền tệ
     */
    @GetMapping
    public ResponseEntity<List<Currency>> getAllCurrencies() {
        List<Currency> currencies = currencyRepository.findAll();
        return ResponseEntity.ok(currencies);
    }

    /**
     * ⚡ Cập nhật tỷ giá ngay lập tức (admin gọi thủ công)
     */
    @PostMapping("/updateRates")
    public ResponseEntity<String> updateRatesManually() {
        exchangeRateUpdater.updateExchangeRatesNow();
        return ResponseEntity.ok("Đã cập nhật tỷ giá thành công!");
    }
}
