package com.example.financeapp.scheduled.task;

import com.example.financeapp.scheduled.service.ScheduledTransactionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ScheduledTransactionRunner {

    private static final Logger log = LoggerFactory.getLogger(ScheduledTransactionRunner.class);

    private final ScheduledTransactionService scheduledTransactionService;

    public ScheduledTransactionRunner(ScheduledTransactionService scheduledTransactionService) {
        this.scheduledTransactionService = scheduledTransactionService;
    }

    @Scheduled(fixedDelay = 60000)
    public void runDueSchedules() {
        try {
            scheduledTransactionService.processDueSchedules();
        } catch (Exception e) {
            log.error("Lỗi xử lý giao dịch đặt lịch: {}", e.getMessage(), e);
        }
    }
}

