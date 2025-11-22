package com.example.financeapp.scheduled.service.impl;

import com.example.financeapp.category.entity.Category;
import com.example.financeapp.category.repository.CategoryRepository;
import com.example.financeapp.common.service.EmailService;
import com.example.financeapp.scheduled.dto.CreateScheduledTransactionRequest;
import com.example.financeapp.scheduled.dto.ScheduledTransactionLogResponse;
import com.example.financeapp.scheduled.dto.ScheduledTransactionResponse;
import com.example.financeapp.scheduled.entity.ScheduledTransaction;
import com.example.financeapp.scheduled.entity.ScheduledTransaction.ScheduleStatus;
import com.example.financeapp.scheduled.entity.ScheduledTransaction.ScheduleType;
import com.example.financeapp.scheduled.entity.ScheduledTransactionLog;
import com.example.financeapp.scheduled.repository.ScheduledTransactionLogRepository;
import com.example.financeapp.scheduled.repository.ScheduledTransactionRepository;
import com.example.financeapp.scheduled.service.ScheduledTransactionService;
import com.example.financeapp.transaction.dto.CreateTransactionRequest;
import com.example.financeapp.transaction.dto.TransactionResult;
import com.example.financeapp.transaction.entity.TransactionType;
import com.example.financeapp.transaction.repository.TransactionTypeRepository;
import com.example.financeapp.transaction.service.TransactionService;
import com.example.financeapp.user.entity.User;
import com.example.financeapp.user.repository.UserRepository;
import com.example.financeapp.wallet.entity.Wallet;
import com.example.financeapp.wallet.repository.WalletRepository;
import com.example.financeapp.wallet.service.WalletService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class ScheduledTransactionServiceImpl implements ScheduledTransactionService {

    @Autowired private ScheduledTransactionRepository scheduledTransactionRepository;
    @Autowired private ScheduledTransactionLogRepository logRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private WalletRepository walletRepository;
    @Autowired private CategoryRepository categoryRepository;
    @Autowired private TransactionTypeRepository transactionTypeRepository;
    @Autowired private WalletService walletService;
    @Autowired private TransactionService transactionService;
    @Autowired private EmailService emailService;

    @Override
    @Transactional
    public ScheduledTransactionResponse createSchedule(Long userId, CreateScheduledTransactionRequest request) {
        long safeUserId = requireUserId(userId);

        User user = userRepository.findById(safeUserId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));

        Wallet wallet = walletRepository.findById(requireId(request.getWalletId(), "Ví không hợp lệ"))
                .orElseThrow(() -> new RuntimeException("Ví không tồn tại"));

        if (!walletService.hasAccess(wallet.getWalletId(), safeUserId)) {
            throw new RuntimeException("Bạn không có quyền truy cập ví này");
        }

        Category category = categoryRepository.findById(requireId(request.getCategoryId(), "Danh mục không hợp lệ"))
                .orElseThrow(() -> new RuntimeException("Danh mục không tồn tại"));

        TransactionType transactionType = transactionTypeRepository
                .findById(requireId(request.getTransactionTypeId(), "Loại giao dịch không hợp lệ"))
                .orElseThrow(() -> new RuntimeException("Loại giao dịch không tồn tại"));

        ScheduleType scheduleType = Objects.requireNonNull(request.getScheduleType(), "Kiểu lịch không hợp lệ");
        LocalDateTime firstRun = requireFutureDateTime(request.getScheduleTime());
        LocalDate resolvedEnd = resolveEndDate(scheduleType, request.getEndDate(), firstRun.toLocalDate());

        ScheduledTransaction schedule = new ScheduledTransaction();
        schedule.setUser(user);
        schedule.setWallet(wallet);
        schedule.setCategory(category);
        schedule.setTransactionType(transactionType);
        schedule.setAmount(request.getAmount());
        schedule.setNote(request.getNote());
        schedule.setScheduleType(scheduleType);
        schedule.setScheduleTime(firstRun);
        schedule.setNextRunAt(firstRun);
        schedule.setEndDate(resolvedEnd);
        schedule.setStatus(ScheduleStatus.PENDING);
        schedule.setLastRunStatus(null);
        schedule.setFailureReason(null);
        schedule.setExecutedAt(null);

        ScheduledTransaction saved = scheduledTransactionRepository.save(schedule);
        return ScheduledTransactionResponse.fromEntity(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ScheduledTransactionResponse> getSchedules(Long userId) {
        long safeUserId = requireUserId(userId);
        return scheduledTransactionRepository.findByUser_UserIdOrderByCreatedAtDesc(safeUserId)
                .stream()
                .map(ScheduledTransactionResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void cancelSchedule(Long userId, Long scheduleId) {
        long safeUserId = requireUserId(userId);
        long safeScheduleId = requireId(scheduleId, "ScheduleId không hợp lệ");

        ScheduledTransaction schedule = scheduledTransactionRepository.findById(safeScheduleId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy lịch giao dịch"));

        if (!schedule.getUser().getUserId().equals(safeUserId)) {
            throw new RuntimeException("Bạn không có quyền hủy lịch này");
        }

        if (schedule.getStatus() == ScheduleStatus.COMPLETED
                || schedule.getStatus() == ScheduleStatus.CANCELLED) {
            throw new RuntimeException("Lịch đã kết thúc, không thể hủy");
        }

        schedule.setStatus(ScheduleStatus.CANCELLED);
        schedule.setNextRunAt(null);
        scheduledTransactionRepository.save(schedule);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ScheduledTransactionLogResponse> getScheduleLogs(Long userId, Long scheduleId) {
        long safeUserId = requireUserId(userId);
        long safeScheduleId = requireId(scheduleId, "ScheduleId không hợp lệ");

        ScheduledTransaction schedule = scheduledTransactionRepository.findById(safeScheduleId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy lịch giao dịch"));

        if (!schedule.getUser().getUserId().equals(safeUserId)) {
            throw new RuntimeException("Bạn không có quyền xem lịch này");
        }

        return logRepository.findByScheduledTransaction_ScheduleIdOrderByRunAtDesc(safeScheduleId)
                .stream()
                .map(ScheduledTransactionLogResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void processDueSchedules() {
        List<ScheduledTransaction> dueSchedules = scheduledTransactionRepository
                .findTop50ByStatusAndNextRunAtLessThanEqualOrderByNextRunAtAsc(
                        ScheduleStatus.PENDING, LocalDateTime.now());

        for (ScheduledTransaction schedule : dueSchedules) {
            processSingleSchedule(schedule);
        }
    }

    private void processSingleSchedule(ScheduledTransaction schedule) {
        Objects.requireNonNull(schedule, "Schedule không hợp lệ");

        LocalDateTime runAt = schedule.getNextRunAt();
        if (runAt == null) {
            schedule.setStatus(ScheduleStatus.COMPLETED);
            scheduledTransactionRepository.save(schedule);
            return;
        }

        schedule.setStatus(ScheduleStatus.PROCESSING);
        scheduledTransactionRepository.save(schedule);

        try {
            executeTransaction(schedule, runAt);
            schedule.setLastRunStatus(ScheduleStatus.COMPLETED);
            schedule.setTotalSuccess(schedule.getTotalSuccess() + 1);
            schedule.setFailureReason(null);
            schedule.setExecutedAt(runAt);
            appendLog(schedule, ScheduleStatus.COMPLETED, runAt, "Thực hiện giao dịch tự động thành công");
        } catch (Exception ex) {
            schedule.setLastRunStatus(ScheduleStatus.FAILED);
            schedule.setTotalFailed(schedule.getTotalFailed() + 1);
            schedule.setFailureReason(ex.getMessage());
            schedule.setExecutedAt(runAt);
            appendLog(schedule, ScheduleStatus.FAILED, runAt,
                    ex.getMessage() != null ? ex.getMessage() : "Không xác định lý do thất bại");
            notifyFailure(schedule, ex.getMessage());
        }

        LocalDateTime nextRun = computeNextRun(schedule, runAt);
        schedule.setNextRunAt(nextRun);

        if (nextRun == null) {
            schedule.setStatus(schedule.getLastRunStatus() == ScheduleStatus.COMPLETED
                    ? ScheduleStatus.COMPLETED
                    : ScheduleStatus.FAILED);
        } else {
            schedule.setStatus(ScheduleStatus.PENDING);
        }

        scheduledTransactionRepository.save(schedule);
    }

    private void executeTransaction(ScheduledTransaction schedule, LocalDateTime runAt) {
        CreateTransactionRequest request = new CreateTransactionRequest();
        request.setWalletId(schedule.getWallet().getWalletId());
        request.setCategoryId(schedule.getCategory().getCategoryId());
        request.setAmount(schedule.getAmount());
        request.setTransactionDate(runAt);
        request.setNote(schedule.getNote());

        boolean isExpense = isExpense(schedule.getTransactionType());
        Long userId = schedule.getUser().getUserId();

        TransactionResult result = isExpense
                ? transactionService.createExpense(userId, request)
                : transactionService.createIncome(userId, request);

        if (result == null || result.getTransaction() == null) {
            throw new RuntimeException("Không thể tạo giao dịch tự động");
        }
    }

    private void appendLog(ScheduledTransaction schedule, ScheduleStatus status,
                           LocalDateTime runAt, String message) {
        ScheduledTransactionLog log = new ScheduledTransactionLog();
        log.setScheduledTransaction(schedule);
        log.setRunAt(runAt);
        log.setStatus(status);
        log.setAmount(schedule.getAmount());
        log.setMessage(message);
        logRepository.save(log);
    }

    private void notifyFailure(ScheduledTransaction schedule, String reason) {
        if (reason == null) {
            return;
        }
        String normalized = reason.toLowerCase();
        if (!normalized.contains("không đủ") && !normalized.contains("insufficient")) {
            return;
        }
        String email = schedule.getUser().getEmail();
        if (email == null || email.isBlank()) {
            return;
        }
        String walletName = schedule.getWallet() != null ? schedule.getWallet().getWalletName() : "Ví";
        emailService.sendScheduledTransactionFailureEmail(
                email,
                walletName,
                schedule.getAmount(),
                "Không đủ số dư để thực hiện giao dịch định kỳ."
        );
    }

    private LocalDateTime computeNextRun(ScheduledTransaction schedule, LocalDateTime lastRun) {
        if (schedule.getScheduleType() == ScheduleType.ONE_TIME) {
            return null;
        }

        LocalDateTime candidate;
        switch (schedule.getScheduleType()) {
            case DAILY -> candidate = lastRun.plusDays(1);
            case WEEKLY -> candidate = lastRun.plusWeeks(1);
            case MONTHLY -> candidate = lastRun.plusMonths(1);
            case YEARLY -> candidate = lastRun.plusYears(1);
            default -> throw new IllegalStateException("Kiểu lịch không hỗ trợ");
        }

        LocalDate endDate = schedule.getEndDate();
        if (endDate != null && candidate.toLocalDate().isAfter(endDate)) {
            return null;
        }
        return candidate;
    }

    private LocalDate resolveEndDate(ScheduleType scheduleType, LocalDate requestEnd, LocalDate firstRunDate) {
        if (scheduleType == ScheduleType.ONE_TIME) {
            return firstRunDate;
        }
        LocalDate endDate = Objects.requireNonNull(requestEnd, "Ngày kết thúc bắt buộc với lịch định kỳ");
        if (endDate.isBefore(firstRunDate)) {
            throw new RuntimeException("Ngày kết thúc phải sau thời điểm thực hiện đầu tiên");
        }
        return endDate;
    }

    private LocalDateTime requireFutureDateTime(LocalDateTime time) {
        LocalDateTime target = Objects.requireNonNull(time, "Thời gian hẹn không hợp lệ");
        if (!target.isAfter(LocalDateTime.now())) {
            throw new RuntimeException("Thời gian thực hiện phải ở tương lai");
        }
        return target;
    }

    private boolean isExpense(TransactionType transactionType) {
        if (transactionType == null || transactionType.getTypeName() == null) {
            return true;
        }
        return "Chi tiêu".equalsIgnoreCase(transactionType.getTypeName());
    }

    private long requireUserId(Long userId) {
        return Objects.requireNonNull(userId, "User không hợp lệ");
    }

    private long requireId(Long id, String message) {
        return Objects.requireNonNull(id, message);
    }
}

