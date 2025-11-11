package com.example.financeapp.controller;

import com.example.financeapp.entity.Currency;
import com.example.financeapp.entity.CurrencyHistory;
import com.example.financeapp.repository.CurrencyHistoryRepository;
import com.example.financeapp.repository.CurrencyRepository;
import com.example.financeapp.service.CurrencyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/currencies")
@CrossOrigin(origins = "*")
public class CurrencyController {

    @Autowired
    private CurrencyService currencyService;

    @Autowired
    private CurrencyRepository currencyRepository;

    @Autowired(required = false)
    private CurrencyHistoryRepository currencyHistoryRepository;

    /**
     * 🟢 1. API cập nhật tỷ giá tiền tệ (thủ công)
     * POST /currencies/update-rates
     */
    @PostMapping("/update-rates")
    public ResponseEntity<Map<String, Object>> updateRates() {
        Map<String, Object> res = new HashMap<>();
        try {
            currencyService.updateExchangeRates();
            res.put("message", "✅ Cập nhật tỷ giá thành công");
            res.put("timestamp", new Date());
            return ResponseEntity.ok(res);
        } catch (Exception e) {
            res.put("error", e.getMessage());
            return ResponseEntity.status(500).body(res);
        }
    }

    /**
     * 🟢 2. API lấy danh sách tỷ giá hiện tại
     * GET /currencies
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllCurrencies() {
        Map<String, Object> res = new HashMap<>();
        try {
            List<Currency> currencies = currencyRepository.findAll();
            res.put("currencies", currencies);
            res.put("total", currencies.size());
            return ResponseEntity.ok(res);
        } catch (Exception e) {
            res.put("error", e.getMessage());
            return ResponseEntity.status(500).body(res);
        }
    }

    /**
     * 🟢 3. API xem chi tiết 1 loại tiền tệ
     * GET /currencies/{code}
     */
    @GetMapping("/{code}")
    public ResponseEntity<Map<String, Object>> getCurrencyByCode(@PathVariable String code) {
        Map<String, Object> res = new HashMap<>();
        try {
            Currency currency = currencyRepository.findByCurrencyCode(code.toUpperCase())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy mã tiền tệ: " + code));

            res.put("currency", currency);
            return ResponseEntity.ok(res);
        } catch (RuntimeException e) {
            res.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(res);
        } catch (Exception e) {
            res.put("error", "Lỗi máy chủ nội bộ: " + e.getMessage());
            return ResponseEntity.status(500).body(res);
        }
    }

    /**
     * 🟢 4. API xem lịch sử thay đổi tỷ giá (nếu có bảng currency_history)
     * GET /currencies/{code}/history
     */
    @GetMapping("/{code}/history")
    public ResponseEntity<Map<String, Object>> getCurrencyHistory(@PathVariable String code) {
        Map<String, Object> res = new HashMap<>();
        try {
            if (currencyHistoryRepository == null) {
                res.put("error", "Chức năng lưu lịch sử chưa được kích hoạt");
                return ResponseEntity.status(501).body(res);
            }

            List<CurrencyHistory> history = currencyHistoryRepository
                    .findByCurrencyCodeOrderByChangedAtDesc(code.toUpperCase());

            res.put("history", history);
            res.put("total", history.size());
            return ResponseEntity.ok(res);
        } catch (Exception e) {
            res.put("error", e.getMessage());
            return ResponseEntity.status(500).body(res);
        }
    }
}
