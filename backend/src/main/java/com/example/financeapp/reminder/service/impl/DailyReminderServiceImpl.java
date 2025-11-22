package com.example.financeapp.reminder.service.impl;

import com.example.financeapp.common.service.EmailService;
import com.example.financeapp.reminder.dto.DailyReminderRequest;
import com.example.financeapp.reminder.dto.DailyReminderResponse;
import com.example.financeapp.reminder.entity.DailyReminderSetting;
import com.example.financeapp.reminder.repository.DailyReminderSettingRepository;
import com.example.financeapp.reminder.service.DailyReminderService;
import com.example.financeapp.user.entity.User;
import com.example.financeapp.user.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Service
public class DailyReminderServiceImpl implements DailyReminderService {

    private static final Logger log = LoggerFactory.getLogger(DailyReminderServiceImpl.class);

    @Autowired private DailyReminderSettingRepository reminderRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private EmailService emailService;

    @Override
    @Transactional(readOnly = true)
    public DailyReminderResponse getCurrentSetting(Long userId) {
        return reminderRepository.findByUser_UserId(requireUserId(userId))
                .map(DailyReminderResponse::fromEntity)
                .orElseGet(() -> {
                    DailyReminderSetting setting = new DailyReminderSetting();
                    setting.setReminderTime(LocalTime.of(21, 0));
                    setting.setSendEmail(true);
                    setting.setSendPush(false);
                    setting.setEnabled(true);
                    return DailyReminderResponse.fromEntity(setting);
                });
    }

    @Override
    @Transactional
    public DailyReminderResponse upsertSetting(Long userId, DailyReminderRequest request) {
        long safeUserId = requireUserId(userId);
        User user = userRepository.findById(safeUserId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));

        DailyReminderSetting setting = reminderRepository.findByUser_UserId(safeUserId)
                .orElseGet(() -> {
                    DailyReminderSetting newSetting = new DailyReminderSetting();
                    newSetting.setUser(user);
                    return newSetting;
                });

        setting.setReminderTime(request.getReminderTime());
        setting.setSendEmail(request.isSendEmail());
        setting.setSendPush(request.isSendPush());
        setting.setEnabled(request.isEnabled());

        DailyReminderSetting saved = reminderRepository.save(setting);
        return DailyReminderResponse.fromEntity(saved);
    }

    @Override
    @Scheduled(fixedDelay = 300000) // 5 phút
    @Transactional
    public void sendDailyReminders() {
        LocalDateTime now = LocalDateTime.now();
        for (DailyReminderSetting setting : reminderRepository.findByEnabledTrue()) {
            if (shouldSendReminder(setting, now)) {
                sendReminder(setting);
            }
        }
    }

    private boolean shouldSendReminder(DailyReminderSetting setting, LocalDateTime now) {
        if (!setting.isEnabled()) return false;
        LocalDateTime targetTime = LocalDateTime.of(LocalDate.now(), setting.getReminderTime());
        if (now.isBefore(targetTime) || now.isAfter(targetTime.plusMinutes(5))) {
            return false;
        }
        if (setting.getLastSentAt() != null &&
                setting.getLastSentAt().toLocalDate().isEqual(LocalDate.now())) {
            return false;
        }
        return setting.isSendEmail(); // hiện chỉ gửi email; push có thể bổ sung sau
    }

    private void sendReminder(DailyReminderSetting setting) {
        try {
            if (setting.isSendEmail()) {
                emailService.sendDailyReminderEmail(
                        setting.getUser().getEmail(),
                        setting.getReminderTime()
                );
            }
            setting.setLastSentAt(LocalDateTime.now());
            reminderRepository.save(setting);
        } catch (Exception e) {
            log.error("Không thể gửi nhắc nhở cho user {}", setting.getUser().getEmail(), e);
        }
    }

    private long requireUserId(Long userId) {
        if (userId == null) {
            throw new RuntimeException("User không hợp lệ");
        }
        return userId;
    }
}

