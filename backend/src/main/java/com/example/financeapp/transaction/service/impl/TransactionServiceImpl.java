package com.example.financeapp.transaction.service.impl;

import com.example.financeapp.budget.service.BudgetService;
import com.example.financeapp.category.entity.Category;
import com.example.financeapp.category.repository.CategoryRepository;
import com.example.financeapp.transaction.dto.CreateTransactionRequest;
import com.example.financeapp.transaction.dto.DailyReminderResponse;
import com.example.financeapp.transaction.dto.UpdateTransactionRequest;
import com.example.financeapp.transaction.entity.Transaction;
import com.example.financeapp.transaction.entity.TransactionType;
import com.example.financeapp.transaction.repository.TransactionRepository;
import com.example.financeapp.transaction.repository.TransactionTypeRepository;
import com.example.financeapp.transaction.service.TransactionService;
import com.example.financeapp.user.entity.User;
import com.example.financeapp.user.repository.UserRepository;
import com.example.financeapp.wallet.entity.Wallet;
import com.example.financeapp.wallet.repository.WalletMemberRepository;
import com.example.financeapp.wallet.repository.WalletRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class TransactionServiceImpl implements TransactionService {

    @Autowired private TransactionRepository transactionRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private WalletRepository walletRepository;
    @Autowired private TransactionTypeRepository typeRepository;
    @Autowired private CategoryRepository categoryRepository;
    @Autowired private WalletMemberRepository walletMemberRepository;
    @Autowired private BudgetService budgetService;

    private Transaction createTransaction(Long userId, CreateTransactionRequest req, String typeName) {
        // 1. Kiểm tra user tồn tại
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User không tồn tại"));

        // 2. ✅ Kiểm tra wallet tồn tại với PESSIMISTIC LOCK
        // Tránh race condition khi nhiều transactions đồng thời
        Wallet wallet = walletRepository.findByIdWithLock(req.getWalletId())
                .orElseThrow(() -> new RuntimeException("Ví không tồn tại"));

        // 3. Kiểm tra quyền truy cập (hỗ trợ shared wallet)
        // User phải là OWNER hoặc MEMBER của ví mới được tạo transaction
        boolean hasAccess = walletMemberRepository.existsByWallet_WalletIdAndUser_UserId(
                req.getWalletId(),
                userId
        );

        if (!hasAccess) {
            throw new RuntimeException("Bạn không có quyền truy cập ví này");
        }

        // 4. Lấy transaction type
        TransactionType type = typeRepository.findByTypeName(typeName)
                .orElseThrow(() -> new RuntimeException("Loại giao dịch không tồn tại"));

        // 5. Lấy category và validate
        Category category = categoryRepository.findById(req.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Danh mục không tồn tại"));

        if (!category.getTransactionType().getTypeId().equals(type.getTypeId())) {
            throw new RuntimeException("Danh mục không thuộc loại giao dịch này");
        }

        // 6. Validate amount
        if (req.getAmount() == null || req.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Số tiền phải lớn hơn 0");
        }

        // 7. Kiểm tra số dư đủ cho chi tiêu
        if ("Chi tiêu".equals(typeName)) {
            BigDecimal newBalance = wallet.getBalance().subtract(req.getAmount());
            if (newBalance.compareTo(BigDecimal.ZERO) < 0) {
                throw new RuntimeException(
                        "Số dư không đủ. Số dư hiện tại: " + wallet.getBalance() +
                                " " + wallet.getCurrencyCode() +
                                ", Số tiền chi tiêu: " + req.getAmount() +
                                " " + wallet.getCurrencyCode()
                );
            }
            wallet.setBalance(newBalance);
        } else {
            // Thu nhập
            wallet.setBalance(wallet.getBalance().add(req.getAmount()));
        }

        // 8. Save wallet với balance mới
        walletRepository.save(wallet);

        // 9. Tạo transaction
        Transaction tx = new Transaction();
        tx.setUser(user);
        tx.setWallet(wallet);
        tx.setTransactionType(type);
        tx.setCategory(category);
        tx.setAmount(req.getAmount());
        tx.setTransactionDate(req.getTransactionDate());
        tx.setNote(req.getNote());
        tx.setImageUrl(req.getImageUrl());

        Transaction savedTx = transactionRepository.save(tx);
        budgetService.evaluateBudgetAfterTransaction(userId, savedTx);
        return savedTx;
    }

    @Override
    @Transactional
    public Transaction createExpense(Long userId, CreateTransactionRequest request) {
        return createTransaction(userId, request, "Chi tiêu");
    }

    @Override
    @Transactional
    public Transaction createIncome(Long userId, CreateTransactionRequest request) {
        return createTransaction(userId, request, "Thu nhập");
    }

    @Override
    @Transactional
    public Transaction updateTransaction(Long userId, Long transactionId, UpdateTransactionRequest request) {
        // 1. Kiểm tra transaction tồn tại
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new RuntimeException("Giao dịch không tồn tại"));

        // 2. Kiểm tra quyền: user phải là owner của transaction
        if (!transaction.getUser().getUserId().equals(userId)) {
            throw new RuntimeException("Bạn không có quyền sửa giao dịch này");
        }

        // 3. Lấy category mới và validate
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Danh mục không tồn tại"));

        // 4. Validate category phải cùng loại với transaction type hiện tại
        if (!category.getTransactionType().getTypeId().equals(transaction.getTransactionType().getTypeId())) {
            throw new RuntimeException("Danh mục không thuộc loại giao dịch này");
        }

        // 5. Cập nhật các field được phép sửa
        transaction.setCategory(category);
        transaction.setNote(request.getNote());
        transaction.setImageUrl(request.getImageUrl());

        Transaction saved = transactionRepository.save(transaction);
        budgetService.evaluateBudgetAfterTransaction(userId, saved);
        return saved;
    }

    @Override
    @Transactional
    public void deleteTransaction(Long userId, Long transactionId) {
        // 1. Kiểm tra transaction tồn tại
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new RuntimeException("Giao dịch không tồn tại"));

        // 2. Kiểm tra quyền: user phải là owner của transaction
        if (!transaction.getUser().getUserId().equals(userId)) {
            throw new RuntimeException("Bạn không có quyền xóa giao dịch này");
        }

        // 3. Lấy wallet với PESSIMISTIC LOCK để tránh race condition
        Wallet wallet = walletRepository.findByIdWithLock(transaction.getWallet().getWalletId())
                .orElseThrow(() -> new RuntimeException("Ví không tồn tại"));

        // 4. Kiểm tra loại giao dịch và tính toán số dư mới
        String typeName = transaction.getTransactionType().getTypeName();
        BigDecimal amount = transaction.getAmount();
        BigDecimal newBalance;

        if ("Chi tiêu".equals(typeName)) {
            // Xóa chi tiêu: cộng lại số tiền vào ví
            newBalance = wallet.getBalance().add(amount);
        } else {
            // Xóa thu nhập: trừ lại số tiền từ ví
            newBalance = wallet.getBalance().subtract(amount);
        }

        // 5. Kiểm tra số dư không được âm
        if (newBalance.compareTo(BigDecimal.ZERO) < 0) {
            throw new RuntimeException("Không thể xóa giao dịch vì ví không được âm tiền");
        }

        // 6. Cập nhật số dư ví
        wallet.setBalance(newBalance);
        walletRepository.save(wallet);

        // 7. Xóa transaction
        transactionRepository.delete(transaction);
    }

    @Override
    public List<Transaction> getAllTransactions(Long userId) {
        return transactionRepository.findByUser_UserIdOrderByTransactionDateDesc(userId);
    }

    @Override
    public DailyReminderResponse getDailyReminder(Long userId) {
        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.plusDays(1).atStartOfDay();

        boolean hasTransactionToday = transactionRepository
                .existsByUser_UserIdAndTransactionDateBetween(userId, startOfDay, endOfDay);

        Transaction lastTransaction = transactionRepository.findTopByUser_UserIdOrderByTransactionDateDesc(userId);

        String message;
        if (hasTransactionToday) {
            message = "Bạn đã ghi giao dịch hôm nay. Tiếp tục duy trì nhé!";
        } else if (lastTransaction == null) {
            message = "Bạn chưa ghi nhận giao dịch nào. Bắt đầu ngay để quản lý tài chính tốt hơn.";
        } else {
            long days = java.time.Duration.between(lastTransaction.getTransactionDate(), LocalDateTime.now()).toDays();
            if (days >= 1) {
                message = "Đã " + days + " ngày bạn chưa ghi giao dịch. Hãy cập nhật thu chi hôm nay!";
            } else {
                message = "Hãy ghi lại các khoản thu chi hôm nay để không bỏ lỡ.";
            }
        }

        return new DailyReminderResponse(!hasTransactionToday, message,
                lastTransaction != null ? lastTransaction.getTransactionDate() : null);
    }
}