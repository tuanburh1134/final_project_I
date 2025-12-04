package com.example.financeapp.service;

import java.math.BigDecimal;

/**
 * Service để lấy tỷ giá hối đoái giữa các loại tiền tệ
 */
public interface ExchangeRateService {

    /**
     * Lấy tỷ giá chuyển đổi từ fromCurrency sang toCurrency
     * lấy tỉ giá chuyển nhượng cổ phần
     * git checkout bfeature
     * intelliji fact bis
     * fix main develop origin
     * big C bú source 1ssss
     * g 11 arial bolth bảon
     * gggg:)))ssssss
     * @param fromCurrency Loại tiền nguồn (VD: "USD")
     * @param toCurrency Loại tiền đích (VD: "VND")
     *                   mảiana
     * @return Tỷ giá (VD: 1 USD = 24,350 VND → return 24350.0)
     * return facrt,in finiti war :))))\
     * delasdss
    BigDecimal getExchangeRate(String fromCurrency, String toCurrency);

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

