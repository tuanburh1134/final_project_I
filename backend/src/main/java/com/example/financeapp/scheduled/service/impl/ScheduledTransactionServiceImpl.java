package com.example.financeapp.scheduled.service.impl;

import com.example.financeapp.category.entity.Category;
import com.example.financeapp.category.repository.CategoryRepository;
import com.example.financeapp.scheduled.dto.CreateScheduledTransactionRequest;
import com.example.financeapp.scheduled.dto.ScheduledTransactionResponse;
import com.example.financeapp.scheduled.entity.ScheduledTransaction;
import com.example.financeapp.scheduled.entity.ScheduledTransaction.ScheduleStatus;
import com.example.financeapp.scheduled.repository.ScheduledTransactionRepository;
import com.example.financeapp.scheduled.service.ScheduledTransactionService;
import com.example.financeapp.transaction.dto.CreateTransactionRequest;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class ScheduledTransactionServiceImpl implements ScheduledTransactionService {

    @Autowired private ScheduledTransactionRepository scheduledTransactionRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private WalletRepository walletRepository;
    @Autowired private CategoryRepository categoryRepository;
    @Autowired private TransactionTypeRepository transactionTypeRepository;
    @Autowired private WalletService walletService;
    @Autowired private TransactionService transactionService;

    @Override
    @Transactional
    public ScheduledTransactionResponse createSchedule(Long userId, CreateScheduledTransactionRequest request) {
        long safeUserId = requireUserId(userId);

        User user = userRepository.findById(safeUserId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));

        long walletId = requireId(request.getWalletId(), "Ví không hợp lệ");
        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new RuntimeException("Ví không tồn tại"));

        if (!walletService.hasAccess(walletId, safeUserId)) {
            throw new RuntimeException("Bạn không có quyền truy cập ví này");
        }

        long categoryId = requireId(request.getCategoryId(), "Danh mục không hợp lệ");
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Danh mục không tồn tại"));

        long typeId = requireId(request.getTransactionTypeId(), "Loại giao dịch không hợp lệ");
        TransactionType transactionType = transactionTypeRepository.findById(typeId)
                .orElseThrow(() -> new RuntimeException("Loại giao dịch không tồn tại"));

        ScheduledTransaction scheduledTransaction = new ScheduledTransaction();
        scheduledTransaction.setUser(user);
        scheduledTransaction.setWallet(wallet);
        scheduledTransaction.setCategory(category);
        scheduledTransaction.setTransactionType(transactionType);
        scheduledTransaction.setAmount(request.getAmount());
        scheduledTransaction.setNote(request.getNote());
        scheduledTransaction.setScheduleTime(request.getScheduleTime());
        scheduledTransaction.setStatus(ScheduleStatus.PENDING);

        ScheduledTransaction saved = scheduledTransactionRepository.save(scheduledTransaction);
        return ScheduledTransactionResponse.fromEntity(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ScheduledTransactionResponse> getSchedules(Long userId) {
        long safeUserId = requireUserId(userId);
        return scheduledTransactionRepository.findByUser_UserIdOrderByScheduleTimeDesc(safeUserId)
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

        if (schedule.getStatus() == ScheduleStatus.COMPLETED) {
            throw new RuntimeException("Lịch đã thực thi, không thể hủy");
        }

        schedule.setStatus(ScheduleStatus.CANCELLED);
        scheduledTransactionRepository.save(schedule);
    }

    @Override
    @Transactional
    public void processDueSchedules() {
        List<ScheduledTransaction> dueSchedules = scheduledTransactionRepository
                .findTop50ByStatusAndScheduleTimeBeforeOrderByScheduleTimeAsc(
                        ScheduleStatus.PENDING, LocalDateTime.now());

        for (ScheduledTransaction schedule : dueSchedules) {
            processSingleSchedule(schedule);
        }
    }

    private void processSingleSchedule(ScheduledTransaction schedule) {
        Objects.requireNonNull(schedule, "Schedule không hợp lệ");
        try {
            schedule.setStatus(ScheduleStatus.PROCESSING);
            scheduledTransactionRepository.save(schedule);

            CreateTransactionRequest request = new CreateTransactionRequest();
            request.setWalletId(schedule.getWallet().getWalletId());
            request.setCategoryId(schedule.getCategory().getCategoryId());
            request.setAmount(schedule.getAmount());
            request.setTransactionDate(schedule.getScheduleTime());
            request.setNote(schedule.getNote());

            boolean isExpense = "Chi tiêu".equalsIgnoreCase(schedule.getTransactionType().getTypeName());
            if (isExpense) {
                transactionService.createExpense(schedule.getUser().getUserId(), request);
            } else {
                transactionService.createIncome(schedule.getUser().getUserId(), request);
            }

            schedule.setStatus(ScheduleStatus.COMPLETED);
            schedule.setExecutedAt(LocalDateTime.now());
            schedule.setFailureReason(null);
        } catch (Exception e) {
            schedule.setStatus(ScheduleStatus.FAILED);
            schedule.setFailureReason(e.getMessage());
        } finally {
            scheduledTransactionRepository.save(schedule);
        }
    }
    private long requireUserId(Long userId) {
        return Objects.requireNonNull(userId, "User không hợp lệ");
    }

    private long requireId(Long id, String message) {
        return Objects.requireNonNull(id, message);
    }
}

