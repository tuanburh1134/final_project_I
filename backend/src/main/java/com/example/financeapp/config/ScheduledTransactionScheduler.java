package com.example.financeapp.config;

import com.example.financeapp.entity.ScheduledTransaction;
import com.example.financeapp.service.ScheduledTransactionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Scheduler để tự động thực hiện các scheduled transactions
 * Chạy mỗi phút để kiểm tra và thực hiện các giao dịch đã đến hạn
 */
@Component
public class ScheduledTransactionScheduler {

    private static final Logger logger = LoggerFactory.getLogger(ScheduledTransactionScheduler.class);

    @Autowired
    private ScheduledTransactionService scheduledTransactionService;

    /**
     * Chạy mỗi phút để kiểm tra và thực hiện các scheduled transactions đã đến hạn
     */
    @Scheduled(fixedRate = 60000) // 60000ms = 1 phút
    public void executeScheduledTransactions() {
        try {
            List<ScheduledTransaction> pendingTransactions = scheduledTransactionService.getPendingTransactionsToExecute();
            
            if (pendingTransactions.isEmpty()) {
                return; // Không có giao dịch nào cần thực hiện
            }

            logger.info("Tìm thấy {} giao dịch đặt lịch cần thực hiện", pendingTransactions.size());

            for (ScheduledTransaction scheduled : pendingTransactions) {
                try {
                    scheduledTransactionService.executeScheduledTransaction(scheduled.getScheduledId());
                    logger.info("Đã thực hiện giao dịch đặt lịch ID: {}", scheduled.getScheduledId());
                } catch (Exception e) {
                    logger.error("Lỗi khi thực hiện giao dịch đặt lịch ID: {} - {}", 
                            scheduled.getScheduledId(), e.getMessage());
                    // Tiếp tục với các giao dịch khác
                }
            }
        } catch (Exception e) {
            logger.error("Lỗi trong scheduler: {}", e.getMessage());
        }
    }
}

