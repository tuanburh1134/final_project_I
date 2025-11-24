package com.example.financeapp.budget.service.impl;

import com.example.financeapp.budget.dto.BudgetAlertResponse;
import com.example.financeapp.budget.dto.BudgetSummaryResponse;
import com.example.financeapp.budget.dto.BudgetTransactionResponse;
import com.example.financeapp.budget.dto.CreateBudgetRequest;
import com.example.financeapp.budget.entity.Budget;
import com.example.financeapp.budget.entity.BudgetAlert;
import com.example.financeapp.budget.entity.BudgetAlertType;
import com.example.financeapp.budget.entity.BudgetStatus;
import com.example.financeapp.budget.repository.BudgetAlertRepository;
import com.example.financeapp.budget.repository.BudgetRepository;
import com.example.financeapp.budget.service.BudgetService;
import com.example.financeapp.category.entity.Category;
import com.example.financeapp.category.repository.CategoryRepository;
import com.example.financeapp.transaction.entity.Transaction;
import com.example.financeapp.transaction.repository.TransactionRepository;
import com.example.financeapp.user.entity.User;
import com.example.financeapp.user.repository.UserRepository;
import com.example.financeapp.wallet.entity.Wallet;
import com.example.financeapp.wallet.repository.WalletMemberRepository;
import com.example.financeapp.wallet.repository.WalletRepository;
import com.example.financeapp.wallet.service.WalletService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class BudgetServiceImpl implements BudgetService {

    private static final BigDecimal NEAR_LIMIT_THRESHOLD = new BigDecimal("0.8");

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
    private TransactionRepository transactionRepository;
    @Autowired
    private WalletMemberRepository walletMemberRepository;
    @Autowired
    private BudgetAlertRepository budgetAlertRepository;

    @Override
    @Transactional
    public Budget createBudget(Long userId, CreateBudgetRequest request) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));

        if (request.getStartDate().isAfter(request.getEndDate())) {
            throw new RuntimeException("Ngày bắt đầu phải trước hoặc bằng ngày kết thúc");
        }

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Danh mục không tồn tại"));

        if (!"Chi tiêu".equals(category.getTransactionType().getTypeName())) {
            throw new RuntimeException("Chỉ được tạo ngân sách cho danh mục Chi tiêu");
        }

        Long walletIdForCheck = null;
        if (request.getWalletId() != null) {
            Wallet wallet = walletRepository.findById(request.getWalletId())
                    .orElseThrow(() -> new RuntimeException("Ví không tồn tại"));

            if (!walletService.hasAccess(wallet.getWalletId(), userId)) {
                throw new RuntimeException("Bạn không có quyền truy cập ví này");
            }
            walletIdForCheck = wallet.getWalletId();
        }

        // KIỂM TRA GIAO NHAU (OVERLAP) – CHẶN HOÀN TOÀN
        boolean hasOverlap = budgetRepository.existsOverlappingBudget(
                user,
                request.getCategoryId(),
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
        Wallet wallet = walletIdForCheck != null
                ? walletRepository.findById(walletIdForCheck).orElse(null)
                : null;

        Budget budget = new Budget();
        budget.setUser(user);
        budget.setCategory(category);
        budget.setWallet(wallet);
        budget.setAmountLimit(request.getAmountLimit());
        budget.setStartDate(request.getStartDate());
        budget.setEndDate(request.getEndDate());
        budget.setNote(request.getNote() != null && !request.getNote().trim().isEmpty()
                ? request.getNote().trim() : null);
        budget.setStatus(determineStatus(budget));
        budget.setOverBudget(false);
        budget.setOverspentAmount(BigDecimal.ZERO);

        return budgetRepository.save(budget);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BudgetSummaryResponse> getBudgets(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));

        List<Budget> budgets = budgetRepository.findByUserOrderByStartDateDesc(user);

        List<Long> accessibleWalletIds = getAccessibleWalletIds(userId);

        return budgets.stream()
                .map(budget -> toSummaryResponse(budget, accessibleWalletIds))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<BudgetTransactionResponse> getBudgetTransactions(Long userId, Long budgetId) {
        Budget budget = budgetRepository.findById(budgetId)
                .orElseThrow(() -> new RuntimeException("Ngân sách không tồn tại"));

        if (!budget.getUser().getUserId().equals(userId)) {
            throw new RuntimeException("Bạn không có quyền xem ngân sách này");
        }

        List<Long> walletScope;
        if (budget.getWallet() != null) {
            walletScope = Collections.singletonList(budget.getWallet().getWalletId());
        } else {
            walletScope = walletMemberRepository.findByUser_UserId(userId)
                    .stream()
                    .map(member -> member.getWallet().getWalletId())
                    .distinct()
                    .collect(Collectors.toList());
        }

        if (walletScope == null || walletScope.isEmpty()) {
            return Collections.emptyList();
        }

        LocalDateTime start = budget.getStartDate().atStartOfDay();
        LocalDateTime endExclusive = budget.getEndDate().plusDays(1).atStartOfDay();

        return transactionRepository.findBudgetTransactions(
                        budget.getCategory().getCategoryId(),
                        walletScope,
                        start,
                        endExclusive
                ).stream()
                .map(tx -> {
                    BudgetTransactionResponse dto = new BudgetTransactionResponse();
                    dto.setTransactionId(tx.getTransactionId());
                    dto.setWalletId(tx.getWallet().getWalletId());
                    dto.setWalletName(tx.getWallet().getWalletName());
                    dto.setWalletCurrency(tx.getWallet().getCurrencyCode());
                    dto.setAmount(tx.getAmount());
                    dto.setTransactionDate(tx.getTransactionDate());
                    dto.setNote(tx.getNote());
                    dto.setImageUrl(tx.getImageUrl());
                    dto.setCategoryId(tx.getCategory().getCategoryId());
                    dto.setCategoryName(tx.getCategory().getCategoryName());
                    dto.setOverBudget(tx.isOverBudget());
                    dto.setOverBudgetAmount(tx.getOverBudgetAmount());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<BudgetAlertResponse> getBudgetAlerts(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));

        return budgetAlertRepository.findByUserAndResolvedFalseOrderByTriggeredAtDesc(user)
                .stream()
                .map(this::toAlertResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void resolveBudgetAlert(Long userId, Long alertId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));

        BudgetAlert alert = budgetAlertRepository.findById(alertId)
                .orElseThrow(() -> new RuntimeException("Cảnh báo không tồn tại"));

        if (!alert.getUser().getUserId().equals(user.getUserId())) {
            throw new RuntimeException("Bạn không có quyền thao tác cảnh báo này");
        }

        if (!alert.isResolved()) {
            alert.setResolved(true);
            alert.setResolvedAt(LocalDateTime.now());
            budgetAlertRepository.save(alert);
        }
    }

    @Override
    @Transactional
    public void evaluateBudgetAfterTransaction(Long userId, Transaction transaction) {
        if (!"Chi tiêu".equals(transaction.getTransactionType().getTypeName())) {
            return;
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));

        List<Budget> budgets = budgetRepository.findActiveBudgets(
                user,
                transaction.getCategory().getCategoryId(),
                transaction.getTransactionDate().toLocalDate()
        );

        if (budgets.isEmpty()) {
            transaction.setOverBudget(false);
            transaction.setOverBudgetAmount(BigDecimal.ZERO);
            transactionRepository.save(transaction);
            return;
        }

        List<Long> accessibleWalletIds = getAccessibleWalletIds(userId);
        boolean transactionOverBudget = false;
        BigDecimal maxOverspent = BigDecimal.ZERO;

        for (Budget budget : budgets) {
            List<Long> walletScope;
            if (budget.getWallet() != null) {
                if (!budget.getWallet().getWalletId().equals(transaction.getWallet().getWalletId())) {
                    continue;
                }
                walletScope = Collections.singletonList(transaction.getWallet().getWalletId());
            } else {
                walletScope = accessibleWalletIds;
            }

            if (walletScope == null || walletScope.isEmpty()) {
                continue;
            }

            LocalDateTime start = budget.getStartDate().atStartOfDay();
            LocalDateTime endExclusive = budget.getEndDate().plusDays(1).atStartOfDay();
            BigDecimal spentAmount = transactionRepository.sumBudgetSpending(
                    transaction.getCategory().getCategoryId(),
                    walletScope,
                    start,
                    endExclusive
            );
            if (spentAmount == null) {
                spentAmount = BigDecimal.ZERO;
            }

            BigDecimal limit = budget.getAmountLimit();
            boolean exceeded = spentAmount.compareTo(limit) > 0;
            boolean nearLimit = !exceeded && spentAmount.compareTo(limit.multiply(NEAR_LIMIT_THRESHOLD)) >= 0;

            if (exceeded) {
                BigDecimal overspent = spentAmount.subtract(limit);
                budget.setOverBudget(true);
                budget.setOverspentAmount(overspent);
                budget.setStatus(BudgetStatus.EXCEEDED);
                transactionOverBudget = true;
                if (overspent.compareTo(maxOverspent) > 0) {
                    maxOverspent = overspent;
                }
                createAlertIfNeeded(budget, user, BudgetAlertType.EXCEEDED, spentAmount, limit,
                        String.format("Ngân sách %s đã vượt hạn mức %s", budget.getCategory().getCategoryName(), formatCurrency(limit)));
                resolveAlerts(budget, BudgetAlertType.NEAR_LIMIT);
            } else {
                budget.setOverBudget(false);
                budget.setOverspentAmount(BigDecimal.ZERO);
                budget.setStatus(determineStatus(budget));
                resolveAlerts(budget, BudgetAlertType.EXCEEDED);

                if (nearLimit) {
                    createAlertIfNeeded(budget, user, BudgetAlertType.NEAR_LIMIT, spentAmount, limit,
                            String.format("Ngân sách %s đã sử dụng %.0f%% hạn mức", budget.getCategory().getCategoryName(),
                                    spentAmount.multiply(BigDecimal.valueOf(100)).divide(limit, 0, RoundingMode.HALF_UP)));
                } else {
                    resolveAlerts(budget, BudgetAlertType.NEAR_LIMIT);
                }
            }

            budgetRepository.save(budget);
        }

        transaction.setOverBudget(transactionOverBudget);
        transaction.setOverBudgetAmount(transactionOverBudget ? maxOverspent : BigDecimal.ZERO);
        transactionRepository.save(transaction);
    }

    private BudgetSummaryResponse toSummaryResponse(Budget budget, List<Long> accessibleWalletIds) {
        List<Long> walletScope;
        if (budget.getWallet() != null) {
            walletScope = Collections.singletonList(budget.getWallet().getWalletId());
        } else {
            walletScope = accessibleWalletIds;
        }

        BigDecimal spentAmount = BigDecimal.ZERO;
        if (walletScope != null && !walletScope.isEmpty()) {
            LocalDateTime start = budget.getStartDate().atStartOfDay();
            LocalDateTime endExclusive = budget.getEndDate().plusDays(1).atStartOfDay();
            spentAmount = transactionRepository.sumBudgetSpending(
                    budget.getCategory().getCategoryId(),
                    walletScope,
                    start,
                    endExclusive
            );
            if (spentAmount == null) {
                spentAmount = BigDecimal.ZERO;
            }
        }

        BigDecimal remaining = budget.getAmountLimit().subtract(spentAmount);
        if (remaining.compareTo(BigDecimal.ZERO) < 0) {
            remaining = BigDecimal.ZERO;
        }

        double usagePercent = budget.getAmountLimit().compareTo(BigDecimal.ZERO) == 0
                ? 0.0
                : spentAmount.multiply(BigDecimal.valueOf(100))
                .divide(budget.getAmountLimit(), 2, RoundingMode.HALF_UP)
                .doubleValue();

        BudgetSummaryResponse response = new BudgetSummaryResponse();
        response.setBudgetId(budget.getBudgetId());
        response.setCategoryId(budget.getCategory().getCategoryId());
        response.setCategoryName(budget.getCategory().getCategoryName());
        response.setWalletId(budget.getWallet() != null ? budget.getWallet().getWalletId() : null);
        response.setWalletName(budget.getWallet() != null ? budget.getWallet().getWalletName() : null);
        response.setCurrencyCode(budget.getWallet() != null ? budget.getWallet().getCurrencyCode() : null);
        response.setAmountLimit(budget.getAmountLimit());
        response.setSpentAmount(spentAmount);
        response.setRemainingAmount(remaining);
        response.setOverspentAmount(budget.getOverspentAmount() != null ? budget.getOverspentAmount() : BigDecimal.ZERO);
        response.setUsagePercent(usagePercent);
        response.setOverBudget(budget.isOverBudget());
        BudgetStatus status = budget.getStatus() != null ? budget.getStatus() : determineStatus(budget);
        response.setStatus(status.name());
        response.setStartDate(budget.getStartDate());
        response.setEndDate(budget.getEndDate());
        response.setNote(budget.getNote());
        return response;
    }

    private BudgetAlertResponse toAlertResponse(BudgetAlert alert) {
        BudgetAlertResponse dto = new BudgetAlertResponse();
        dto.setAlertId(alert.getAlertId());
        dto.setBudgetId(alert.getBudget().getBudgetId());
        dto.setCategoryName(alert.getBudget().getCategory().getCategoryName());
        dto.setLimitAmount(alert.getLimitAmount());
        dto.setSpentAmount(alert.getSpentAmount());
        BigDecimal overspent = alert.getSpentAmount().subtract(alert.getLimitAmount());
        if (overspent.compareTo(BigDecimal.ZERO) < 0) {
            overspent = BigDecimal.ZERO;
        }
        dto.setOverspentAmount(overspent);
        dto.setAlertType(alert.getAlertType().name());
        dto.setMessage(alert.getMessage());
        dto.setStartDate(alert.getBudget().getStartDate());
        dto.setEndDate(alert.getBudget().getEndDate());
        dto.setTriggeredAt(alert.getTriggeredAt());
        dto.setResolved(alert.isResolved());
        return dto;
    }

    private List<Long> getAccessibleWalletIds(Long userId) {
        return walletMemberRepository.findByUser_UserId(userId)
                .stream()
                .map(member -> member.getWallet().getWalletId())
                .distinct()
                .collect(Collectors.toList());
    }

    private BudgetStatus determineStatus(Budget budget) {
        if (budget.isOverBudget()) {
            return BudgetStatus.EXCEEDED;
        }
        LocalDate today = LocalDate.now();
        if (today.isBefore(budget.getStartDate())) {
            return BudgetStatus.UPCOMING;
        }
        if (today.isAfter(budget.getEndDate())) {
            return BudgetStatus.COMPLETED;
        }
        return BudgetStatus.ACTIVE;
    }

    private void createAlertIfNeeded(Budget budget, User user, BudgetAlertType type,
                                     BigDecimal spentAmount, BigDecimal limitAmount, String message) {
        if (budgetAlertRepository.existsByBudgetAndAlertTypeAndResolvedFalse(budget, type)) {
            return;
        }
        BudgetAlert alert = new BudgetAlert();
        alert.setBudget(budget);
        alert.setUser(user);
        alert.setAlertType(type);
        alert.setLimitAmount(limitAmount);
        alert.setSpentAmount(spentAmount);
        alert.setMessage(message);
        budgetAlertRepository.save(alert);
    }

    private void resolveAlerts(Budget budget, BudgetAlertType type) {
        List<BudgetAlert> alerts = budgetAlertRepository.findByBudgetAndAlertTypeAndResolvedFalse(budget, type);
        if (alerts.isEmpty()) {
            return;
        }
        LocalDateTime now = LocalDateTime.now();
        alerts.forEach(alert -> {
            alert.setResolved(true);
            alert.setResolvedAt(now);
        });
        budgetAlertRepository.saveAll(alerts);
    }

    private String formatCurrency(BigDecimal amount) {
        return amount.stripTrailingZeros().toPlainString();
    }
}