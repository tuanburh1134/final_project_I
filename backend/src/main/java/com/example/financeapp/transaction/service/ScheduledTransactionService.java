package com.example.financeapp.transaction.service;

import com.example.financeapp.transaction.dto.ScheduleLogResponse;
import com.example.financeapp.transaction.dto.ScheduledTransactionRequest;
import com.example.financeapp.transaction.dto.ScheduledTransactionResponse;

import java.util.List;

public interface ScheduledTransactionService {

    ScheduledTransactionResponse createSchedule(Long userId, ScheduledTransactionRequest request);

    List<ScheduledTransactionResponse> getSchedules(Long userId);

    List<ScheduleLogResponse> getScheduleLogs(Long userId, Long scheduleId);

    void cancelSchedule(Long userId, Long scheduleId);

    void processDueSchedules();
}

