package com.example.financeapp.config;

import com.example.financeapp.service.WalletService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ScheduledTasks {

    private final WalletService walletService;

    public ScheduledTasks(WalletService walletService) {
        this.walletService = walletService;
    }

    // Chạy mỗi ngày lúc 3:00 sáng
    @Scheduled(cron = "0 0 3 * * ?")
    public void cleanUpOldWallets() {
        walletService.deleteExpiredWallets();
    }
}
