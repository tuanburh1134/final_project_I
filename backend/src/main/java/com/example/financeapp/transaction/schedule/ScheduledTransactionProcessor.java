package com.example.financeapp.transaction.schedule;

import com.example.financeapp.transaction.service.ScheduledTransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ScheduledTransactionProcessor {

    private final ScheduledTransactionService scheduledTransactionService;

    @Scheduled(fixedDelay = 60000)
    public void process() {
        scheduledTransactionService.processDueSchedules();
    }
}

