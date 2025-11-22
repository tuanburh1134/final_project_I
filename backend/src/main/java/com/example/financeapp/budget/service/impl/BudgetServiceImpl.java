package com.example.financeapp.budget.service.impl;

import com.example.financeapp.budget.dto.BudgetAlert;
import com.example.financeapp.budget.dto.BudgetSummaryResponse;
import com.example.financeapp.budget.dto.CreateBudgetRequest;
import com.example.financeapp.budget.entity.Budget;
import com.example.financeapp.budget.entity.Budget.BudgetStatus;
import com.example.financeapp.budget.repository.BudgetRepository;
import com.example.financeapp.budget.service.BudgetService;
import com.example.financeapp.category.entity.Category;
import com.example.financeapp.category.repository.CategoryRepository;
import com.example.financeapp.common.service.EmailService;
import com.example.financeapp.fund.repository.FundRepository;
import com.example.financeapp.transaction.entity.Transaction;
import com.example.financeapp.transaction.repository.TransactionRepository;
import com.example.financeapp.user.entity.User;
import com.example.financeapp.user.repository.UserRepository;
import com.example.financeapp.wallet.entity.Wallet;
import com.example.financeapp.wallet.repository.WalletRepository;
import com.example.financeapp.wallet.service.WalletService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class BudgetServiceImpl implements BudgetService {

    @Autowired
    private BudgetRepository budgetRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    private WalletRepository walletRepository;
    @Autowired
    private WalletService walletService;
    @Autowired
    private FundRepository fundRepository;
    @Autowired
    private TransactionRepository transactionRepository;
    @Autowired
    private EmailService emailService;

    @Override
    @Transactional
    public Budget createBudget(@NonNull Long userId, CreateBudgetRequest request) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));

        if (request.getStartDate().isAfter(request.getEndDate())) {
            throw new RuntimeException("Ngày bắt đầu phải trước hoặc bằng ngày kết thúc");
        }

        Long categoryId = request.getCategoryId();
        if (categoryId == null) {
            throw new RuntimeException("Danh mục không hợp lệ");
        }

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Danh mục không tồn tại"));

        if (!"Chi tiêu".equals(category.getTransactionType().getTypeName())) {
            throw new RuntimeException("Chỉ được tạo ngân sách cho danh mục Chi tiêu");
        }

        Long requestedWalletId = request.getWalletId();
        Long walletIdForCheck = null;
        if (requestedWalletId != null) {
            Wallet wallet = walletRepository.findById(requestedWalletId)
                    .orElseThrow(() -> new RuntimeException("Ví không tồn tại"));

            if (!walletService.hasAccess(wallet.getWalletId(), userId)) {
                throw new RuntimeException("Bạn không có quyền truy cập ví này");
            }
            walletIdForCheck = wallet.getWalletId();

            if (fundRepository.existsByWallet_WalletId(walletIdForCheck)) {
                throw new RuntimeException("Ví này đang được sử dụng cho một quỹ tiết kiệm. Vui lòng chọn ví khác.");
            }
        }

        // KIỂM TRA GIAO NHAU (OVERLAP) – CHẶN HOÀN TOÀN
        boolean hasOverlap = budgetRepository.existsOverlappingBudget(
                user,
                categoryId,
                walletIdForCheck,
                request.getStartDate(),
                request.getEndDate()
        );

        if (hasOverlap) {
            String walletInfo = walletIdForCheck == null ? "tất cả ví" : "ví đã chọn";
            throw new RuntimeException(
                    "Không thể tạo ngân sách mới!\n" +
                            "Danh mục \"" + category.getCategoryName() + "\" trong " + walletInfo +
                            " đã có ngân sách đang áp dụng trong khoảng thời gian này.\n" +
                            "Vui lòng chọn khoảng thời gian không giao nhau hoặc chỉnh sửa ngân sách cũ."
            );
        }

        // Nếu không trùng → tạo bình thường
        Wallet wallet = null;
        if (walletIdForCheck != null) {
            wallet = walletRepository.findById(walletIdForCheck).orElse(null);
        }

        Budget budget = new Budget();
        budget.setUser(user);
        budget.setCategory(category);
        budget.setWallet(wallet);
        budget.setAmountLimit(request.getAmountLimit());
        budget.setStartDate(request.getStartDate());
        budget.setEndDate(request.getEndDate());
        budget.setNote(request.getNote() != null && !request.getNote().trim().isEmpty()
                ? request.getNote().trim() : null);

        return budgetRepository.save(budget);
    }

    @Override
    @Transactional
    public List<BudgetSummaryResponse> getBudgets(@NonNull Long userId) {
        List<Budget> budgets = budgetRepository.findByUser_UserIdOrderByStartDateDesc(userId);
        return budgets.stream()
                .map(budget -> buildBudgetSummary(budget, userId))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Transaction> getBudgetTransactions(@NonNull Long userId, Long budgetId) {
        if (budgetId == null) {
            throw new RuntimeException("Ngân sách không hợp lệ");
        }

        Budget budget = budgetRepository.findById(budgetId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy ngân sách"));

        if (!budget.getUser().getUserId().equals(userId)) {
            throw new RuntimeException("Bạn không có quyền xem ngân sách này");
        }

        Long walletId = budget.getWallet() != null ? budget.getWallet().getWalletId() : null;
        LocalDateTime start = budget.getStartDate().atStartOfDay();
        LocalDateTime end = budget.getEndDate().atTime(LocalTime.MAX);

        return transactionRepository.findTransactionsForBudget(
                userId,
                budget.getCategory().getCategoryId(),
                walletId,
                start,
                end
        );
    }

    @Override
    @Transactional
    public BudgetAlert handleExpenseTransaction(Transaction transaction) {
        if (transaction == null || transaction.getTransactionDate() == null || transaction.getCategory() == null) {
            return null;
        }

        Long userId = transaction.getUser() != null ? transaction.getUser().getUserId() : null;
        if (userId == null) {
            return null;
        }

        Long walletId = transaction.getWallet() != null ? transaction.getWallet().getWalletId() : null;
        Long categoryId = transaction.getCategory().getCategoryId();
        if (categoryId == null) {
            return null;
        }

        LocalDate targetDate = transaction.getTransactionDate().toLocalDate();

        List<Budget> budgets = budgetRepository.findApplicableBudgets(userId, categoryId, walletId, targetDate);
        if (budgets.isEmpty()) {
            return null;
        }

        Budget budget = budgets.get(0);
        Long budgetWalletId = budget.getWallet() != null ? budget.getWallet().getWalletId() : null;

        BigDecimal spent = transactionRepository.sumExpensesForBudget(
                userId,
                budget.getCategory().getCategoryId(),
                budgetWalletId,
                budget.getStartDate().atStartOfDay(),
                budget.getEndDate().atTime(LocalTime.MAX)
        );

        if (spent == null) {
            spent = BigDecimal.ZERO;
        }

        BigDecimal amountLimit = budget.getAmountLimit() != null ? budget.getAmountLimit() : BigDecimal.ZERO;
        BigDecimal remaining = amountLimit.subtract(spent);
        BigDecimal overBudgetAmount = spent.subtract(amountLimit);

        boolean exceeded = overBudgetAmount.compareTo(BigDecimal.ZERO) > 0;
        BigDecimal normalizedOverAmount = exceeded ? overBudgetAmount : BigDecimal.ZERO;

        transaction.setBudget(budget);
        transaction.setOverBudget(exceeded);
        transaction.setOverBudgetAmount(normalizedOverAmount);
        transactionRepository.save(transaction);

        budget.setOverBudgetAmount(normalizedOverAmount);
        BudgetStatus status = determineBudgetStatus(budget, normalizedOverAmount);
        if (budget.getStatus() != status) {
            budget.setStatus(status);
        }

        AlertInfo alertInfo = handleBudgetAlerts(budget, amountLimit, spent, remaining);
        budgetRepository.save(budget);

        return buildBudgetAlert(budget, alertInfo, normalizedOverAmount, remaining);
    }

    private BudgetSummaryResponse buildBudgetSummary(Budget budget, Long userId) {
        Long walletId = budget.getWallet() != null ? budget.getWallet().getWalletId() : null;

        LocalDateTime start = budget.getStartDate().atStartOfDay();
        LocalDateTime end = budget.getEndDate().atTime(LocalTime.MAX);

        BigDecimal spent = transactionRepository.sumExpensesForBudget(
                userId,
                budget.getCategory().getCategoryId(),
                walletId,
                start,
                end
        );

        if (spent == null) {
            spent = BigDecimal.ZERO;
        }

        BigDecimal amountLimit = budget.getAmountLimit() != null ? budget.getAmountLimit() : BigDecimal.ZERO;
        BigDecimal remaining = amountLimit.subtract(spent);

        BigDecimal progress = BigDecimal.ZERO;
        if (amountLimit.compareTo(BigDecimal.ZERO) > 0) {
            progress = spent
                    .multiply(BigDecimal.valueOf(100))
                    .divide(amountLimit, 2, RoundingMode.HALF_UP);
        }

        BigDecimal overBudgetAmount = spent.subtract(amountLimit);
        if (overBudgetAmount.compareTo(BigDecimal.ZERO) < 0) {
            overBudgetAmount = BigDecimal.ZERO;
        }

        BudgetStatus status = determineBudgetStatus(budget, overBudgetAmount);
        if (budget.getStatus() != status) {
            budget.setStatus(status);
            budgetRepository.save(budget);
        }

        AlertInfo alertInfo = handleBudgetAlerts(budget, amountLimit, spent, remaining);

        BudgetSummaryResponse response = new BudgetSummaryResponse();
        response.setBudgetId(budget.getBudgetId());
        response.setCategoryId(budget.getCategory().getCategoryId());
        response.setCategoryName(budget.getCategory().getCategoryName());
        response.setAmountLimit(amountLimit);
        response.setSpentAmount(spent);
        response.setRemainingAmount(remaining);
        response.setProgressPercentage(progress);
        response.setOverLimit(remaining.compareTo(BigDecimal.ZERO) < 0);
        response.setOverBudgetAmount(overBudgetAmount);
        response.setHasExceededBudget(overBudgetAmount.compareTo(BigDecimal.ZERO) > 0);
        response.setBudgetStatus(status.name());
        response.setWarningTriggered(alertInfo.warningTriggered());
        response.setOverLimitAlertTriggered(alertInfo.overLimitTriggered());
        response.setWarningThresholdPercent(alertInfo.thresholdPercent());
        response.setStartDate(budget.getStartDate());
        response.setEndDate(budget.getEndDate());
        response.setNote(budget.getNote());
        response.setAppliesToAllWallets(walletId == null);

        if (walletId != null) {
            response.setWalletId(walletId);
            response.setWalletName(budget.getWallet().getWalletName());
        }

        return response;
    }

    private AlertInfo handleBudgetAlerts(Budget budget,
                                         BigDecimal amountLimit,
                                         BigDecimal spent,
                                         BigDecimal remaining) {
        Objects.requireNonNull(budget, "Budget is required");
        if (amountLimit == null || amountLimit.compareTo(BigDecimal.ZERO) <= 0) {
            return new AlertInfo(false, remaining.compareTo(BigDecimal.ZERO) <= 0,
                    defaultThresholdPercent(budget));
        }

        BigDecimal thresholdPercent = defaultThresholdPercent(budget);
        BigDecimal thresholdValue = amountLimit
                .multiply(thresholdPercent)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

        boolean warningTriggered = remaining.compareTo(BigDecimal.ZERO) > 0
                && remaining.compareTo(thresholdValue) <= 0;
        boolean overLimitTriggered = remaining.compareTo(BigDecimal.ZERO) <= 0;

        boolean budgetUpdated = false;

        if (warningTriggered && !budget.isWarningAlertSent()) {
            sendWarningEmail(budget, remaining, amountLimit);
            budget.setWarningAlertSent(true);
            budgetUpdated = true;
        }

        if (overLimitTriggered && !budget.isOverLimitAlertSent()) {
            sendOverLimitEmail(budget, spent, amountLimit);
            budget.setOverLimitAlertSent(true);
            budgetUpdated = true;
        }

        if (budgetUpdated) {
            budgetRepository.save(budget);
        }

        return new AlertInfo(warningTriggered, overLimitTriggered, thresholdPercent);
    }

    private BigDecimal defaultThresholdPercent(Budget budget) {
        BigDecimal threshold = budget.getWarningThresholdPercent();
        if (threshold == null || threshold.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.valueOf(20);
        }
        return threshold;
    }

    private void sendWarningEmail(Budget budget, BigDecimal remaining, BigDecimal amountLimit) {
        emailService.sendBudgetWarningEmail(
                budget.getUser().getEmail(),
                budget.getCategory().getCategoryName(),
                remaining,
                amountLimit
        );
    }

    private void sendOverLimitEmail(Budget budget, BigDecimal spent, BigDecimal amountLimit) {
        emailService.sendBudgetExceededEmail(
                budget.getUser().getEmail(),
                budget.getCategory().getCategoryName(),
                spent,
                amountLimit
        );
    }

    private BudgetStatus determineBudgetStatus(Budget budget, BigDecimal overBudgetAmount) {
        if (overBudgetAmount != null && overBudgetAmount.compareTo(BigDecimal.ZERO) > 0) {
            return BudgetStatus.OVER_LIMIT;
        }
        if (budget.getEndDate() != null && budget.getEndDate().isBefore(LocalDate.now())) {
            return BudgetStatus.COMPLETED;
        }
        return BudgetStatus.ACTIVE;
    }

    private BudgetAlert buildBudgetAlert(Budget budget,
                                                                           AlertInfo alertInfo,
                                                                           BigDecimal overBudgetAmount,
                                                                           BigDecimal remaining) {
        if (alertInfo.overLimitTriggered()) {
            String message = String.format("Ngân sách %s đã vượt hạn mức %s",
                    budget.getCategory().getCategoryName(),
                    overBudgetAmount.stripTrailingZeros().toPlainString());
            return new BudgetAlert(true, "OVER_LIMIT", message, overBudgetAmount);
        }

        if (alertInfo.warningTriggered() && remaining != null && remaining.compareTo(BigDecimal.ZERO) >= 0) {
            String message = String.format("Ngân sách %s chỉ còn %s (<= %s%%)",
                    budget.getCategory().getCategoryName(),
                    remaining.stripTrailingZeros().toPlainString(),
                    defaultThresholdPercent(budget).stripTrailingZeros().toPlainString());
            return new BudgetAlert(
                    true,
                    "WARNING",
                    message,
                    BigDecimal.ZERO
            );
        }

        return null;
    }

    private record AlertInfo(boolean warningTriggered, boolean overLimitTriggered,
                             BigDecimal thresholdPercent) {}
}