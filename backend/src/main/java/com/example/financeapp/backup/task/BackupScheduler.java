package com.example.financeapp.backup.task;

import com.example.financeapp.backup.service.BackupService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class BackupScheduler {

    private static final Logger log = LoggerFactory.getLogger(BackupScheduler.class);

    private final BackupService backupService;

    public BackupScheduler(BackupService backupService) {
        this.backupService = backupService;
    }

    /**
     * Auto-backup every night at 2AM server time.
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void nightlyBackup() {
        log.info("Starting nightly backup job...");
        backupService.backupAllUsers();
        log.info("Nightly backup job finished.");
    }
}

