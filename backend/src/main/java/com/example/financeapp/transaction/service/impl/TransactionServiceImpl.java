package com.example.financeapp.transaction.service.impl;

import com.example.financeapp.category.entity.Category;
import com.example.financeapp.category.repository.CategoryRepository;
import com.example.financeapp.exception.ApiErrorCode;
import com.example.financeapp.exception.ApiException;
import com.example.financeapp.transaction.dto.CreateTransactionRequest;
import com.example.financeapp.transaction.dto.UpdateTransactionRequest;
import com.example.financeapp.transaction.entity.Transaction;
import com.example.financeapp.transaction.entity.TransactionType;
import com.example.financeapp.transaction.repository.TransactionRepository;
import com.example.financeapp.transaction.repository.TransactionTypeRepository;
import com.example.financeapp.transaction.service.TransactionService;
import com.example.financeapp.user.entity.User;
import com.example.financeapp.user.repository.UserRepository;
import com.example.financeapp.wallet.entity.Wallet;
import com.example.financeapp.wallet.entity.WalletMember;
import com.example.financeapp.wallet.entity.WalletMember.WalletRole;
import com.example.financeapp.wallet.repository.WalletMemberRepository;
import com.example.financeapp.wallet.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor // Sử dụng Lombok để inject dependencies sạch hơn
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final WalletRepository walletRepository;
    private final TransactionTypeRepository typeRepository;
    private final CategoryRepository categoryRepository;
    private final WalletMemberRepository walletMemberRepository;

    // =================================================================================
    // PRIVATE HELPER METHODS (CLEAN CODE & RBAC)
    // =================================================================================

    /**
     * Validate quyền tạo giao dịch:
     * - Phải là thành viên của ví.
     * - Không phải là VIEWER.
     */
    private void validateCreateAccess(Long walletId, Long userId) {
        WalletMember member = walletMemberRepository.findByWallet_WalletIdAndUser_UserId(walletId, userId)
                .orElseThrow(() -> new ApiException("Bạn không có quyền truy cập ví này", ApiErrorCode.FORBIDDEN));

        if (member.getRole() == WalletRole.VIEWER) {
            throw new ApiException("Viewer chỉ được xem, không được tạo giao dịch", ApiErrorCode.FORBIDDEN);
        }
    }

    /**
     * Validate quyền sửa/xóa giao dịch:
     * - OWNER/ADMIN: Có quyền sửa/xóa tất cả.
     * - EDITOR: Chỉ được sửa/xóa giao dịch của chính mình.
     * - VIEWER: Không được phép.
     */
    private void validateModifyAccess(Transaction transaction, Long userId) {
        Long walletId = transaction.getWallet().getWalletId();

        WalletMember member = walletMemberRepository.findByWallet_WalletIdAndUser_UserId(walletId, userId)
                .orElseThrow(() -> new ApiException("Bạn không có quyền truy cập ví này", ApiErrorCode.FORBIDDEN));

        // 1. Chặn Viewer tuyệt đối
        if (member.getRole() == WalletRole.VIEWER) {
            throw new ApiException("Viewer không có quyền chỉnh sửa dữ liệu", ApiErrorCode.FORBIDDEN);
        }

        // 2. Logic cho Editor: Chỉ được sửa bài của mình
        if (member.getRole() == WalletRole.EDITOR) {
            if (!transaction.getUser().getUserId().equals(userId)) {
                throw new ApiException("Thành viên chỉ được chỉnh sửa giao dịch do mình tạo ra", ApiErrorCode.FORBIDDEN);
            }
        }

        // 3. OWNER và ADMIN được phép đi tiếp (quyền quản lý)
    }

    private Transaction createTransactionLogic(Long userId, CreateTransactionRequest req, String typeName) {
        // 1. Validate quyền trước khi xử lý logic nặng
        validateCreateAccess(req.getWalletId(), userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException("User không tồn tại", ApiErrorCode.USER_NOT_FOUND));

        // 2. Lấy wallet với PESSIMISTIC LOCK để tránh race condition tính tiền
        Wallet wallet = walletRepository.findByIdWithLock(req.getWalletId())
                .orElseThrow(() -> new ApiException("Ví không tồn tại", ApiErrorCode.VALIDATION_ERROR));

        // 3. Validate Type
        TransactionType type = typeRepository.findByTypeName(typeName)
                .orElseThrow(() -> new ApiException("Loại giao dịch không tồn tại", ApiErrorCode.VALIDATION_ERROR));

        // 4. Validate Category
        Category category = categoryRepository.findById(req.getCategoryId())
                .orElseThrow(() -> new ApiException("Danh mục không tồn tại", ApiErrorCode.VALIDATION_ERROR));

        if (!category.getTransactionType().getTypeId().equals(type.getTypeId())) {
            throw new ApiException("Danh mục không khớp với loại giao dịch", ApiErrorCode.VALIDATION_ERROR);
        }

        // 5. Validate Amount
        if (req.getAmount() == null || req.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ApiException("Số tiền phải lớn hơn 0", ApiErrorCode.VALIDATION_ERROR);
        }

        // 6. Tính toán số dư
        if ("Chi tiêu".equals(typeName)) {
            BigDecimal newBalance = wallet.getBalance().subtract(req.getAmount());
            // Tùy chọn: Cho phép âm ví hay không? Ở đây logic cũ là chặn âm.
            if (newBalance.compareTo(BigDecimal.ZERO) < 0) {
                throw new ApiException("Số dư ví không đủ để thực hiện chi tiêu", ApiErrorCode.VALIDATION_ERROR);
            }
            wallet.setBalance(newBalance);
        } else {
            // Thu nhập
            wallet.setBalance(wallet.getBalance().add(req.getAmount()));
        }

        walletRepository.save(wallet);

        // 7. Tạo Transaction
        Transaction tx = new Transaction();
        tx.setUser(user);
        tx.setWallet(wallet);
        tx.setTransactionType(type);
        tx.setCategory(category);
        tx.setAmount(req.getAmount());
        tx.setTransactionDate(req.getTransactionDate());
        tx.setNote(req.getNote());
        tx.setImageUrl(req.getImageUrl());

        return transactionRepository.save(tx);
    }

    // =================================================================================
    // PUBLIC SERVICE METHODS
    // =================================================================================

    @Override
    @Transactional
    public Transaction createExpense(Long userId, CreateTransactionRequest request) {
        return createTransactionLogic(userId, request, "Chi tiêu");
    }

    @Override
    @Transactional
    public Transaction createIncome(Long userId, CreateTransactionRequest request) {
        return createTransactionLogic(userId, request, "Thu nhập");
    }

    @Override
    @Transactional
    public Transaction updateTransaction(Long userId, Long transactionId, UpdateTransactionRequest request) {
        // 1. Tìm giao dịch
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new ApiException("Giao dịch không tồn tại", ApiErrorCode.VALIDATION_ERROR));

        // 2. Kiểm tra phân quyền (RBAC)
        validateModifyAccess(transaction, userId);

        // 3. Validate Category mới
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ApiException("Danh mục không tồn tại", ApiErrorCode.VALIDATION_ERROR));

        // Category mới phải cùng loại (Chi tiêu/Thu nhập) với transaction cũ
        if (!category.getTransactionType().getTypeId().equals(transaction.getTransactionType().getTypeId())) {
            throw new ApiException("Không thể đổi loại giao dịch (Chi tiêu <-> Thu nhập) khi cập nhật", ApiErrorCode.VALIDATION_ERROR);
        }

        // 4. Cập nhật thông tin
        transaction.setCategory(category);
        transaction.setNote(request.getNote());
        transaction.setImageUrl(request.getImageUrl());

        // Lưu ý: Logic hiện tại chưa hỗ trợ sửa số tiền (Amount).
        // Nếu sửa amount thì phải tính toán lại balance của Wallet (cộng cũ, trừ mới), khá phức tạp.
        // Tốt nhất nên khuyên user xóa đi tạo lại nếu sai số tiền.

        return transactionRepository.save(transaction);
    }

    @Override
    @Transactional
    public void deleteTransaction(Long userId, Long transactionId) {
        // 1. Tìm giao dịch
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new ApiException("Giao dịch không tồn tại", ApiErrorCode.VALIDATION_ERROR));

        // 2. Kiểm tra phân quyền (RBAC)
        validateModifyAccess(transaction, userId);

        // 3. Lock ví để revert tiền
        Wallet wallet = walletRepository.findByIdWithLock(transaction.getWallet().getWalletId())
                .orElseThrow(() -> new ApiException("Ví không tồn tại", ApiErrorCode.VALIDATION_ERROR));

        // 4. Revert số dư
        String typeName = transaction.getTransactionType().getTypeName();
        BigDecimal amount = transaction.getAmount();
        BigDecimal newBalance;

        if ("Chi tiêu".equals(typeName)) {
            // Xóa chi tiêu -> Tiền quay về ví (Cộng)
            newBalance = wallet.getBalance().add(amount);
        } else {
            // Xóa thu nhập -> Tiền mất đi (Trừ)
            newBalance = wallet.getBalance().subtract(amount);
        }

        // Check âm ví khi xóa thu nhập
        if (newBalance.compareTo(BigDecimal.ZERO) < 0) {
            throw new ApiException("Không thể xóa khoản thu nhập này vì số dư ví sẽ bị âm", ApiErrorCode.VALIDATION_ERROR);
        }

        wallet.setBalance(newBalance);
        walletRepository.save(wallet);

        // 5. Xóa
        transactionRepository.delete(transaction);
    }

    @Override
    public List<Transaction> getAllTransactions(Long userId) {
        // Có thể thêm logic filter chỉ lấy transaction của các ví mà user có quyền truy cập
        return transactionRepository.findByUser_UserIdOrderByTransactionDateDesc(userId);
    }
}