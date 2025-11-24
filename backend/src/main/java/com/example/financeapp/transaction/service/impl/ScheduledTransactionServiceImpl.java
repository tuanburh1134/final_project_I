package com.example.financeapp.transaction.service.impl;

import com.example.financeapp.category.entity.Category;
import com.example.financeapp.category.repository.CategoryRepository;
import com.example.financeapp.transaction.dto.ScheduleLogResponse;
import com.example.financeapp.transaction.dto.ScheduledTransactionRequest;
import com.example.financeapp.transaction.dto.ScheduledTransactionResponse;
import com.example.financeapp.transaction.repository.ScheduledTransactionLogRepository;
import com.example.financeapp.transaction.repository.ScheduledTransactionRepository;
import com.example.financeapp.transaction.schedule.*;
import com.example.financeapp.transaction.service.ScheduledTransactionService;
import com.example.financeapp.transaction.service.TransactionService;
import com.example.financeapp.user.entity.User;
import com.example.financeapp.user.repository.UserRepository;
import com.example.financeapp.wallet.entity.Wallet;
import com.example.financeapp.wallet.repository.WalletRepository;
import com.example.financeapp.wallet.service.WalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ScheduledTransactionServiceImpl implements ScheduledTransactionService {

    private final ScheduledTransactionRepository scheduledTransactionRepository;
    private final ScheduledTransactionLogRepository logRepository;
    private final TransactionService transactionService;
    private final UserRepository userRepository;
    private final WalletRepository walletRepository;
    private final CategoryRepository categoryRepository;
    private final WalletService walletService;

    @Override
    @Transactional
    public ScheduledTransactionResponse createSchedule(Long userId, ScheduledTransactionRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));
        Wallet wallet = walletRepository.findById(request.getWalletId())
                .orElseThrow(() -> new RuntimeException("Ví không tồn tại"));
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Danh mục không tồn tại"));

        if (!walletService.hasAccess(wallet.getWalletId(), userId)) {
            throw new RuntimeException("Bạn không có quyền với ví này");
        }

        if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Số tiền phải lớn hơn 0");
        }

        if (request.getTransactionType() == ScheduledTransactionType.EXPENSE &&
                !"Chi tiêu".equals(category.getTransactionType().getTypeName())) {
            throw new RuntimeException("Danh mục phải là chi tiêu");
        }
        if (request.getTransactionType() == ScheduledTransactionType.INCOME &&
                !"Thu nhập".equals(category.getTransactionType().getTypeName())) {
            throw new RuntimeException("Danh mục phải là thu nhập");
        }

        LocalDateTime firstRun = request.getFirstRunAt();
        if (firstRun.isBefore(LocalDateTime.now())) {
            firstRun = adjustToFuture(firstRun, request.getFrequency());
            if (firstRun == null) {
                throw new RuntimeException("Thời gian thực hiện phải ở tương lai");
            }
        }

        if (request.getEndDate() != null && firstRun.toLocalDate().isAfter(request.getEndDate())) {
            throw new RuntimeException("Ngày kết thúc phải sau lần chạy đầu tiên");
        }

        ScheduledTransaction schedule = new ScheduledTransaction();
        schedule.setUser(user);
        schedule.setWallet(wallet);
        schedule.setCategory(category);
        schedule.setTransactionType(request.getTransactionType());
        schedule.setFrequency(request.getFrequency());
        schedule.setAmount(request.getAmount());
        schedule.setNote(request.getNote());
        schedule.setFirstRunAt(firstRun);
        schedule.setNextRunAt(firstRun);
        schedule.setEndDate(request.getEndDate());

        scheduledTransactionRepository.save(schedule);

        return toResponse(schedule);
    }

    @Override
    public List<ScheduledTransactionResponse> getSchedules(Long userId) {
        return scheduledTransactionRepository.findByUser_UserIdOrderByCreatedAtDesc(userId)
                .stream().map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<ScheduleLogResponse> getScheduleLogs(Long userId, Long scheduleId) {
        ScheduledTransaction schedule = scheduledTransactionRepository
                .findByScheduleIdAndUser_UserId(scheduleId, userId)
                .orElseThrow(() -> new RuntimeException("Lịch không tồn tại"));

        return logRepository.findByScheduledTransactionOrderByExecutedAtDesc(schedule)
                .stream().map(log -> {
                    ScheduleLogResponse dto = new ScheduleLogResponse();
                    dto.setLogId(log.getLogId());
                    dto.setStatus(log.getStatus());
                    dto.setMessage(log.getMessage());
                    dto.setExecutedAt(log.getExecutedAt());
                    return dto;
                }).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void cancelSchedule(Long userId, Long scheduleId) {
        ScheduledTransaction schedule = scheduledTransactionRepository
                .findByScheduleIdAndUser_UserId(scheduleId, userId)
                .orElseThrow(() -> new RuntimeException("Lịch không tồn tại"));

        schedule.setStatus(ScheduleStatus.CANCELLED);
        schedule.setNextRunAt(null);
        scheduledTransactionRepository.save(schedule);
    }

    @Override
    @Transactional
    public void processDueSchedules() {
        List<ScheduledTransaction> dueSchedules = scheduledTransactionRepository
                .findDueSchedules(ScheduleStatus.PENDING, LocalDateTime.now());

        for (ScheduledTransaction schedule : dueSchedules) {
            executeSchedule(schedule);
        }
    }

    private void executeSchedule(ScheduledTransaction schedule) {
        LocalDateTime runAt = schedule.getNextRunAt();
        boolean success = false;
        String message;

        try {
            com.example.financeapp.transaction.dto.CreateTransactionRequest request =
                    new com.example.financeapp.transaction.dto.CreateTransactionRequest();
            request.setAmount(schedule.getAmount());
            request.setWalletId(schedule.getWallet().getWalletId());
            request.setCategoryId(schedule.getCategory().getCategoryId());
            request.setNote(schedule.getNote());
            request.setTransactionDate(runAt);

            if (schedule.getTransactionType() == ScheduledTransactionType.EXPENSE) {
                transactionService.createExpense(schedule.getUser().getUserId(), request);
            } else {
                transactionService.createIncome(schedule.getUser().getUserId(), request);
            }

            success = true;
            message = "Thực hiện giao dịch thành công";
            schedule.setCompletedOccurrences(schedule.getCompletedOccurrences() + 1);
        } catch (Exception ex) {
            message = "Thực hiện giao dịch thất bại: " + ex.getMessage();
        }

        schedule.setLastRunAt(LocalDateTime.now());
        schedule.setNextRunAt(calculateNextRun(schedule));

        if (schedule.getNextRunAt() == null) {
            schedule.setStatus(success ? ScheduleStatus.COMPLETED : ScheduleStatus.FAILED);
        } else if (schedule.getStatus() != ScheduleStatus.CANCELLED) {
            schedule.setStatus(ScheduleStatus.PENDING);
        }

        scheduledTransactionRepository.save(schedule);

        ScheduledTransactionLog log = new ScheduledTransactionLog();
        log.setScheduledTransaction(schedule);
        log.setStatus(success ? ScheduleStatus.COMPLETED : ScheduleStatus.FAILED);
        log.setMessage(message);
        logRepository.save(log);
    }

    private LocalDateTime calculateNextRun(ScheduledTransaction schedule) {
        if (schedule.getFrequency() == ScheduleFrequency.ONE_TIME) {
            return null;
        }

        LocalDateTime next = schedule.getNextRunAt();
        if (next == null) {
            next = schedule.getFirstRunAt();
        }

        switch (schedule.getFrequency()) {
            case DAILY -> next = next.plusDays(1);
            case WEEKLY -> next = next.plusWeeks(1);
            case MONTHLY -> next = next.plusMonths(1);
            case YEARLY -> next = next.plusYears(1);
            default -> {
            }
        }

        if (schedule.getEndDate() != null && next.toLocalDate().isAfter(schedule.getEndDate())) {
            return null;
        }

        return next;
    }

    private LocalDateTime adjustToFuture(LocalDateTime base, ScheduleFrequency frequency) {
        LocalDateTime candidate = base;
        LocalDateTime now = LocalDateTime.now();

        if (frequency == ScheduleFrequency.ONE_TIME) {
            return candidate.isAfter(now) ? candidate : null;
        }

        while (!candidate.isAfter(now)) {
            switch (frequency) {
                case DAILY -> candidate = candidate.plusDays(1);
                case WEEKLY -> candidate = candidate.plusWeeks(1);
                case MONTHLY -> candidate = candidate.plusMonths(1);
                case YEARLY -> candidate = candidate.plusYears(1);
                default -> {
                }
            }
        }
        return candidate;
    }

    private ScheduledTransactionResponse toResponse(ScheduledTransaction schedule) {
        ScheduledTransactionResponse dto = new ScheduledTransactionResponse();
        dto.setScheduleId(schedule.getScheduleId());
        dto.setTransactionType(schedule.getTransactionType());
        dto.setFrequency(schedule.getFrequency());
        dto.setStatus(schedule.getStatus());
        dto.setAmount(schedule.getAmount());
        dto.setNote(schedule.getNote());
        dto.setWalletId(schedule.getWallet().getWalletId());
        dto.setWalletName(schedule.getWallet().getWalletName());
        dto.setCategoryId(schedule.getCategory().getCategoryId());
        dto.setCategoryName(schedule.getCategory().getCategoryName());
        dto.setNextRunAt(schedule.getNextRunAt());
        dto.setEndDate(schedule.getEndDate());
        dto.setCompletedOccurrences(schedule.getCompletedOccurrences());
        dto.setLastRunAt(schedule.getLastRunAt());
        dto.setCreatedAt(schedule.getCreatedAt());
        return dto;
    }
}

