package com.example.financeapp.service;

import com.example.financeapp.entity.Currency;
import com.example.financeapp.repository.CurrencyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.Map;

@Service
public class ExchangeRateUpdater {

    private static final String API_URL = "https://api.exchangerate-api.com/v4/latest/USD";

    @Autowired
    private CurrencyRepository currencyRepository;

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * 🕒 Tự động cập nhật mỗi ngày lúc 12:00 (giờ Việt Nam)
     */
    @Scheduled(cron = "0 0 12 * * *", zone = "Asia/Ho_Chi_Minh")
    public void updateExchangeRatesDaily() {
        System.out.println(">>> [Scheduler] Bắt đầu cập nhật tỷ giá tự động...");
        updateExchangeRatesNow(); // tái sử dụng logic
    }

    /**
     * ⚡ Cập nhật tỷ giá ngay lập tức (cho API hoặc admin gọi thủ công)
     */
    public void updateExchangeRatesNow() {
        try {
            Map<String, Object> response = restTemplate.getForObject(API_URL, Map.class);
            if (response == null || !response.containsKey("rates")) {
                throw new RuntimeException("Không nhận được dữ liệu tỷ giá từ API");
            }

            Map<String, Double> rates = (Map<String, Double>) response.get("rates");
            double usdToVnd = rates.getOrDefault("VND", 25000.0);

            for (Currency currency : currencyRepository.findAll()) {
                if (currency.getCurrencyCode().equals("VND")) {
                    currency.setRateToVnd(BigDecimal.ONE);
                } else {
                    Double rateToUsd = rates.get(currency.getCurrencyCode());
                    if (rateToUsd != null && rateToUsd > 0) {
                        double rateToVnd = usdToVnd / rateToUsd;
                        currency.setRateToVnd(BigDecimal.valueOf(rateToVnd));
                    }
                }
            }

            currencyRepository.flush();
            System.out.println(">>> [ExchangeRateUpdater] Cập nhật tỷ giá thành công ✅");

        } catch (Exception e) {
            System.err.println(">>> [ExchangeRateUpdater] Lỗi cập nhật tỷ giá: " + e.getMessage());
        }
    }
}
