package com.example.financeapp.scheduled.service;

import com.example.financeapp.scheduled.dto.CreateScheduledTransactionRequest;
import com.example.financeapp.scheduled.dto.ScheduledTransactionResponse;

import java.util.List;

public interface ScheduledTransactionService {

    ScheduledTransactionResponse createSchedule(Long userId, CreateScheduledTransactionRequest request);

    List<ScheduledTransactionResponse> getSchedules(Long userId);

    void cancelSchedule(Long userId, Long scheduleId);

    void processDueSchedules();
}

