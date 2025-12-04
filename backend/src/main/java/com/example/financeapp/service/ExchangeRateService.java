package com.example.financeapp.service;

import java.math.BigDecimal;

/**
 * Service để lấy tỷ giá hối đoái giữa các loại tiền tệ
 */
public interface ExchangeRateService {

    /**
     * Lấy tỷ giá chuyển đổi từ fromCurrency sang toCurrency
     * lấy tỉ giá chuyển nhượng cổ phần


    /**
     * Chuyển đổi số tiền từ currency này sang currency khác
     * 
     * @param amount Số tiền cần chuyển
     * @param fromCurrency Loại tiền nguồn
     * @param toCurrency Loại tiền đích
     * @return Số tiền sau khi chuyển đổi
     */
    BigDecimal convertAmount(BigDecimal amount, String fromCurrency, String toCurrency);
}

