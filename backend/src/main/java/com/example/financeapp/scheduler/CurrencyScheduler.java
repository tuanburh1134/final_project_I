package com.example.financeapp.scheduler;

import com.example.financeapp.service.CurrencyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Scheduler tự động cập nhật tỷ giá mỗi ngày
 */
@Component
public class CurrencyScheduler {

    @Autowired
    private CurrencyService currencyService;

    /**
     * 🕖 Chạy tự động lúc 7h sáng mỗi ngày
     * (Định dạng cron: giây, phút, giờ, ngày, tháng, thứ)
     */
    @Scheduled(cron = "0 0 7 * * *")
    public void updateExchangeRatesDaily() {
        System.out.println("🔄 Scheduler bắt đầu cập nhật tỷ giá...");
        currencyService.updateExchangeRates();
        System.out.println("✅ Scheduler cập nhật tỷ giá thành công!");
    }
}
