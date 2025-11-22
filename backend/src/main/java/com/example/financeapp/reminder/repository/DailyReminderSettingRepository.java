package com.example.financeapp.reminder.repository;

import com.example.financeapp.reminder.entity.DailyReminderSetting;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DailyReminderSettingRepository extends JpaRepository<DailyReminderSetting, Long> {
    Optional<DailyReminderSetting> findByUser_UserId(Long userId);

    List<DailyReminderSetting> findByEnabledTrue();
}

