package com.example.financeapp.service.impl;

import com.example.financeapp.dto.CreateScheduledTransactionRequest;
import com.example.financeapp.dto.CreateTransactionRequest;
import com.example.financeapp.entity.*;
import com.example.financeapp.repository.*;
import com.example.financeapp.service.ScheduledTransactionService;
import com.example.financeapp.service.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ScheduledTransactionServiceImpl implements ScheduledTransactionService {

    @Autowired
    private ScheduledTransactionRepository scheduledTransactionRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private WalletRepository walletRepository;
    
    @Autowired
    private CategoryRepository categoryRepository;
    
    @Autowired
    private WalletMemberRepository walletMemberRepository;
    
    @Autowired
    private TransactionService transactionService;

    @Override
    @Transactional
    public ScheduledTransaction createScheduledTransaction(Long userId, CreateScheduledTransactionRequest request) {
        // 1. Kiểm tra user tồn tại
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User không tồn tại"));

        // 2. Kiểm tra wallet tồn tại và quyền truy cập
        Wallet wallet = walletRepository.findById(request.getWalletId())
                .orElseThrow(() -> new RuntimeException("Ví không tồn tại"));

        if (!walletMemberRepository.existsByWallet_WalletIdAndUser_UserId(request.getWalletId(), userId)) {
            throw new RuntimeException("Bạn không có quyền truy cập ví này");
        }

        // 3. Kiểm tra category tồn tại
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Danh mục không tồn tại"));

        // 4. Xác định transaction type từ category
        TransactionType transactionType = category.getTransactionType();

        // 5. Validate amount
        if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Số tiền phải lớn hơn 0");
        }

        // 6. Validate scheduled date (phải trong tương lai)
        if (request.getScheduledDate() == null) {
            throw new RuntimeException("Ngày hẹn không được để trống");
        }
        if (request.getScheduledDate().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Ngày hẹn phải trong tương lai");
        }

        // 7. Kiểm tra số dư đủ cho chi tiêu (nếu là chi tiêu)
        if ("Chi tiêu".equals(transactionType.getTypeName())) {
            if (wallet.getBalance().compareTo(request.getAmount()) < 0) {
                throw new RuntimeException(
                    "Số dư không đủ. Số dư hiện tại: " + wallet.getBalance() + 
                    " " + wallet.getCurrencyCode() + 
                    ", Số tiền chi tiêu: " + request.getAmount() + 
                    " " + wallet.getCurrencyCode()
                );
            }
        }

        // 8. Tạo scheduled transaction
        ScheduledTransaction scheduled = new ScheduledTransaction();
        scheduled.setUser(user);
        scheduled.setWallet(wallet);
        scheduled.setTransactionType(transactionType);
        scheduled.setCategory(category);
        scheduled.setAmount(request.getAmount());
        scheduled.setScheduledDate(request.getScheduledDate());
        scheduled.setNote(request.getNote());
        scheduled.setImageUrl(request.getImageUrl());
        scheduled.setStatus(ScheduledTransaction.ScheduledStatus.PENDING);

        return scheduledTransactionRepository.save(scheduled);
    }

    @Override
    public List<ScheduledTransaction> getAllScheduledTransactions(Long userId) {
        return scheduledTransactionRepository.findByUser_UserIdOrderByScheduledDateAsc(userId);
    }

    @Override
    public List<ScheduledTransaction> getScheduledTransactionsByStatus(Long userId, ScheduledTransaction.ScheduledStatus status) {
        return scheduledTransactionRepository.findByUser_UserIdAndStatusOrderByScheduledDateAsc(userId, status);
    }

    @Override
    public List<ScheduledTransaction> getScheduledTransactionsByWallet(Long userId, Long walletId) {
        // Kiểm tra quyền truy cập wallet
        if (!walletMemberRepository.existsByWallet_WalletIdAndUser_UserId(walletId, userId)) {
            throw new RuntimeException("Bạn không có quyền truy cập ví này");
        }
        return scheduledTransactionRepository.findByWallet_WalletIdAndUser_UserIdOrderByScheduledDateAsc(walletId, userId);
    }

    @Override
    public ScheduledTransaction getScheduledTransactionById(Long userId, Long scheduledId) {
        ScheduledTransaction scheduled = scheduledTransactionRepository.findById(scheduledId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy giao dịch đặt lịch"));

        // Kiểm tra quyền truy cập
        if (!scheduled.getUser().getUserId().equals(userId)) {
            throw new RuntimeException("Bạn không có quyền xem giao dịch đặt lịch này");
        }

        return scheduled;
    }

    @Override
    @Transactional
    public void cancelScheduledTransaction(Long userId, Long scheduledId) {
        ScheduledTransaction scheduled = scheduledTransactionRepository.findById(scheduledId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy giao dịch đặt lịch"));

        // Kiểm tra quyền
        if (!scheduled.getUser().getUserId().equals(userId)) {
            throw new RuntimeException("Bạn không có quyền hủy giao dịch đặt lịch này");
        }

        // Chỉ có thể hủy nếu đang PENDING
        if (scheduled.getStatus() != ScheduledTransaction.ScheduledStatus.PENDING) {
            throw new RuntimeException("Chỉ có thể hủy giao dịch đặt lịch đang chờ thực hiện");
        }

        scheduled.setStatus(ScheduledTransaction.ScheduledStatus.CANCELLED);
        scheduledTransactionRepository.save(scheduled);
    }

    @Override
    @Transactional
    public void executeScheduledTransaction(Long scheduledId) {
        ScheduledTransaction scheduled = scheduledTransactionRepository.findById(scheduledId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy giao dịch đặt lịch"));

        // Chỉ thực hiện nếu đang PENDING
        if (scheduled.getStatus() != ScheduledTransaction.ScheduledStatus.PENDING) {
            return; // Đã thực hiện hoặc đã hủy
        }

        try {
            // Tạo transaction thực tế
            CreateTransactionRequest transactionRequest = new CreateTransactionRequest();
            transactionRequest.setWalletId(scheduled.getWallet().getWalletId());
            transactionRequest.setCategoryId(scheduled.getCategory().getCategoryId());
            transactionRequest.setAmount(scheduled.getAmount());
            transactionRequest.setTransactionDate(scheduled.getScheduledDate());
            transactionRequest.setNote(scheduled.getNote());
            transactionRequest.setImageUrl(scheduled.getImageUrl());

            Transaction createdTransaction;
            if ("Chi tiêu".equals(scheduled.getTransactionType().getTypeName())) {
                createdTransaction = transactionService.createExpense(scheduled.getUser().getUserId(), transactionRequest);
            } else {
                createdTransaction = transactionService.createIncome(scheduled.getUser().getUserId(), transactionRequest);
            }

            // Cập nhật scheduled transaction
            scheduled.setStatus(ScheduledTransaction.ScheduledStatus.EXECUTED);
            scheduled.setExecutedAt(LocalDateTime.now());
            scheduled.setCreatedTransactionId(createdTransaction.getTransactionId());
            scheduledTransactionRepository.save(scheduled);

        } catch (Exception e) {
            // Nếu có lỗi (ví dụ: số dư không đủ), giữ nguyên status PENDING để thử lại sau
            // Có thể log lỗi hoặc gửi thông báo cho user
            throw new RuntimeException("Không thể thực hiện giao dịch đặt lịch: " + e.getMessage());
        }
    }

    @Override
    public List<ScheduledTransaction> getPendingTransactionsToExecute() {
        return scheduledTransactionRepository.findPendingTransactionsToExecute(
                ScheduledTransaction.ScheduledStatus.PENDING,
                LocalDateTime.now()
        );
    }
}

