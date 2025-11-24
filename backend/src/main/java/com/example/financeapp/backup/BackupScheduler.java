package com.example.financeapp.backup;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BackupScheduler {

    private static final Logger log = LoggerFactory.getLogger(BackupScheduler.class);

    private final DataBackupService dataBackupService;

    @Scheduled(cron = "0 0 3 * * *")
    public void runNightlyBackup() {
        log.info("Bắt đầu backup dữ liệu người dùng");
        dataBackupService.backupAllUsers();
    }
}

