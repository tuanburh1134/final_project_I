package com.example.financeapp.ai.service;

import com.example.financeapp.budget.entity.Budget;
import com.example.financeapp.budget.repository.BudgetRepository;
import com.example.financeapp.category.repository.CategoryRepository;
import com.example.financeapp.transaction.entity.Transaction;
import com.example.financeapp.transaction.repository.TransactionRepository;
import com.example.financeapp.wallet.entity.Wallet;
import com.example.financeapp.wallet.repository.WalletRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AiContextService {

    private final TransactionRepository transactionRepository;
    private final BudgetRepository budgetRepository;
    private final CategoryRepository categoryRepository;
    private final WalletRepository walletRepository;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATETIME_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public AiContextService(TransactionRepository transactionRepository,
                            BudgetRepository budgetRepository,
                            CategoryRepository categoryRepository,
                            WalletRepository walletRepository) {
        this.transactionRepository = transactionRepository;
        this.budgetRepository = budgetRepository;
        this.categoryRepository = categoryRepository;
        this.walletRepository = walletRepository;
    }

    public String buildUserContext(Long userId) {
        if (userId == null) return "";

        String wallets = summarizeWallets(userId);
        String transactions = summarizeRecentTransactions(userId);
        String budgets = summarizeBudgets(userId);
        String categories = summarizeCategories(userId);
        String monthSummary = summarizeMonthlyTotals(userId);
        String monthOverMonth = summarizeMonthOverMonth(userId);
        String topCategories = summarizeTopCategories30d(userId);
        String budgetsHealth = summarizeBudgetsHealth(userId);
        String lowBalances = summarizeLowBalanceWallets(userId);
        String largeSpend = summarizeLargestExpense30d(userId);

        return List.of(wallets, transactions, budgets, categories)
                .stream()
                .filter(s -> s != null && !s.isBlank())
                .collect(Collectors.joining(" | "))
                + (monthSummary.isBlank() ? "" : " | " + monthSummary)
                + (monthOverMonth.isBlank() ? "" : " | " + monthOverMonth)
                + (topCategories.isBlank() ? "" : " | " + topCategories)
            + (budgetsHealth.isBlank() ? "" : " | " + budgetsHealth)
                    + (lowBalances.isBlank() ? "" : " | " + lowBalances)
                    + (largeSpend.isBlank() ? "" : " | " + largeSpend);
    }

    private String summarizeWallets(Long userId) {
        List<Wallet> list = walletRepository.findByUser_UserId(userId);
        if (list.isEmpty()) return "Wallets: none";
        return "Wallets (top): " + list.stream()
                .limit(3)
                .map(w -> w.getWalletName() + "=" + safeAmount(w.getBalance()) + " " + (w.getCurrencyCode() == null ? "" : w.getCurrencyCode()))
                .collect(Collectors.joining(", "));
    }

    private String summarizeRecentTransactions(Long userId) {
        List<Transaction> list = transactionRepository.findByUser_UserIdOrderByTransactionDateDesc(userId);
        if (list.isEmpty()) return "Transactions: none";
        return "Recent tx (5): " + list.stream()
                .limit(5)
                .map(t -> {
                    String type = t.getTransactionType() != null ? t.getTransactionType().getTypeName() : "";
                    String cat = t.getCategory() != null ? t.getCategory().getCategoryName() : "";
                    String wal = t.getWallet() != null ? t.getWallet().getWalletName() : "";
                    return type + " " + safeAmount(t.getAmount()) + " cat=" + cat + " wal=" + wal + " on " + DATETIME_FMT.format(t.getTransactionDate());
                })
                .collect(Collectors.joining("; "));
    }

    private String summarizeBudgets(Long userId) {
        List<Budget> list = budgetRepository.findByUser_UserIdOrderByCreatedAtDesc(userId);
        if (list.isEmpty()) return "Budgets: none";
        return "Budgets (3): " + list.stream()
                .limit(3)
                .map(b -> {
                    String cat = b.getCategory() != null ? b.getCategory().getCategoryName() : "";
                    String wal = b.getWallet() != null ? b.getWallet().getWalletName() : "all wallets";
                    return cat + " limit=" + safeAmount(b.getAmountLimit()) + " wal=" + wal + " range=" + DATE_FMT.format(b.getStartDate()) + "~" + DATE_FMT.format(b.getEndDate());
                })
                .collect(Collectors.joining("; "));
    }

    private String summarizeCategories(Long userId) {
        long count = categoryRepository.findByUser_UserId(userId).size();
        long systemCount = categoryRepository.findByUserIsNullAndIsSystemTrue().size();
        return "Categories: user=" + count + ", system=" + systemCount;
    }

    private String summarizeMonthlyTotals(Long userId) {
        List<Transaction> list = transactionRepository.findByUser_UserIdOrderByTransactionDateDesc(userId);
        if (list.isEmpty()) return "";
        LocalDateTime from = LocalDate.now().minusDays(30).atStartOfDay();
        BigDecimal income = BigDecimal.ZERO;
        BigDecimal expense = BigDecimal.ZERO;
        for (Transaction t : list) {
            if (t.getTransactionDate() == null || t.getTransactionDate().isBefore(from)) continue;
            String type = t.getTransactionType() != null ? t.getTransactionType().getTypeName() : "";
            if ("Thu nhập".equalsIgnoreCase(type) || "Income".equalsIgnoreCase(type)) {
                income = income.add(zeroSafe(t.getAmount()));
            } else if ("Chi tiêu".equalsIgnoreCase(type) || "Expense".equalsIgnoreCase(type) || "Expense".equals(type)) {
                expense = expense.add(zeroSafe(t.getAmount()));
            }
        }
        if (income.compareTo(BigDecimal.ZERO) == 0 && expense.compareTo(BigDecimal.ZERO) == 0) return "";
        return "Last30d: income=" + fmt(income) + ", expense=" + fmt(expense);
    }

    private String summarizeMonthOverMonth(Long userId) {
        List<Transaction> list = transactionRepository.findByUser_UserIdOrderByTransactionDateDesc(userId);
        if (list.isEmpty()) return "";

        LocalDateTime currentFrom = LocalDate.now().minusDays(30).atStartOfDay();
        LocalDateTime previousFrom = LocalDate.now().minusDays(60).atStartOfDay();

        BigDecimal currentIncome = BigDecimal.ZERO;
        BigDecimal currentExpense = BigDecimal.ZERO;
        BigDecimal previousIncome = BigDecimal.ZERO;
        BigDecimal previousExpense = BigDecimal.ZERO;

        for (Transaction t : list) {
            if (t.getTransactionDate() == null) continue;
            LocalDateTime date = t.getTransactionDate();
            String type = t.getTransactionType() != null ? t.getTransactionType().getTypeName() : "";
            boolean isIncome = "Thu nhập".equalsIgnoreCase(type) || "Income".equalsIgnoreCase(type);
            boolean isExpense = "Chi tiêu".equalsIgnoreCase(type) || "Expense".equalsIgnoreCase(type) || "Expense".equals(type);

            if (date.isAfter(previousFrom.minusNanos(1)) && date.isBefore(currentFrom)) {
                if (isIncome) {
                    previousIncome = previousIncome.add(zeroSafe(t.getAmount()));
                } else if (isExpense) {
                    previousExpense = previousExpense.add(zeroSafe(t.getAmount()));
                }
            } else if (!date.isBefore(currentFrom)) {
                if (isIncome) {
                    currentIncome = currentIncome.add(zeroSafe(t.getAmount()));
                } else if (isExpense) {
                    currentExpense = currentExpense.add(zeroSafe(t.getAmount()));
                }
            }
        }

        if (currentIncome.compareTo(BigDecimal.ZERO) == 0 && currentExpense.compareTo(BigDecimal.ZERO) == 0
                && previousIncome.compareTo(BigDecimal.ZERO) == 0 && previousExpense.compareTo(BigDecimal.ZERO) == 0) {
            return "";
        }

        String incomeChange = percentChange(currentIncome, previousIncome);
        String expenseChange = percentChange(currentExpense, previousExpense);

        return "MoM: income " + fmt(currentIncome) + "/" + fmt(previousIncome) + " (" + incomeChange + ")"
                + ", expense " + fmt(currentExpense) + "/" + fmt(previousExpense) + " (" + expenseChange + ")";
    }

    private String summarizeTopCategories30d(Long userId) {
        List<Transaction> list = transactionRepository.findByUser_UserIdOrderByTransactionDateDesc(userId);
        if (list.isEmpty()) return "";
        LocalDateTime from = LocalDate.now().minusDays(30).atStartOfDay();
        Map<String, BigDecimal> sumByCat = list.stream()
                .filter(t -> t.getTransactionDate() != null && !t.getTransactionDate().isBefore(from))
                .filter(t -> t.getTransactionType() != null && "Chi tiêu".equalsIgnoreCase(t.getTransactionType().getTypeName()))
                .collect(Collectors.groupingBy(
                        t -> t.getCategory() != null ? t.getCategory().getCategoryName() : "(no category)",
                        Collectors.mapping(
                                t -> zeroSafe(t.getAmount()),
                                Collectors.reducing(BigDecimal.ZERO, BigDecimal::add)
                        )));

        if (sumByCat.isEmpty()) return "";

        String top = sumByCat.entrySet().stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .limit(3)
                .map(e -> e.getKey() + "=" + fmt(e.getValue()))
                .collect(Collectors.joining(", "));
        return top.isBlank() ? "" : "Top spend cat 30d: " + top;
    }

    private String summarizeLargestExpense30d(Long userId) {
        List<Transaction> list = transactionRepository.findByUser_UserIdOrderByTransactionDateDesc(userId);
        if (list.isEmpty()) return "";

        LocalDateTime from = LocalDate.now().minusDays(30).atStartOfDay();
        BigDecimal totalExpense = BigDecimal.ZERO;
        long expenseCount = 0;

        Transaction maxTx = null;
        BigDecimal maxAmount = BigDecimal.ZERO;

        for (Transaction t : list) {
            if (t.getTransactionDate() == null || t.getTransactionDate().isBefore(from)) continue;
            String type = t.getTransactionType() != null ? t.getTransactionType().getTypeName() : "";
            boolean isExpense = "Chi tiêu".equalsIgnoreCase(type) || "Expense".equalsIgnoreCase(type) || "Expense".equals(type);
            if (!isExpense) continue;

            BigDecimal amt = zeroSafe(t.getAmount());
            totalExpense = totalExpense.add(amt);
            expenseCount++;

            if (amt.compareTo(maxAmount) > 0) {
                maxAmount = amt;
                maxTx = t;
            }
        }

        if (expenseCount == 0) return "";

        BigDecimal avg = totalExpense.divide(BigDecimal.valueOf(expenseCount), 2, java.math.RoundingMode.HALF_UP);
        String avgText = fmt(avg);

        if (maxTx == null) return "";

        String cat = maxTx.getCategory() != null ? maxTx.getCategory().getCategoryName() : "";
        String wal = maxTx.getWallet() != null ? maxTx.getWallet().getWalletName() : "";
        String date = maxTx.getTransactionDate() != null ? DATETIME_FMT.format(maxTx.getTransactionDate()) : "";
        String maxText = fmt(maxAmount);

        return "Largest expense 30d: " + maxText + " cat=" + cat + " wal=" + wal + " on " + date + " | Avg expense 30d=" + avgText;
    }

    private String summarizeBudgetsHealth(Long userId) {
        List<Budget> list = budgetRepository.findByUser_UserIdOrderByCreatedAtDesc(userId);
        if (list.isEmpty()) return "";
        String summary = list.stream()
                .limit(3)
                .map(b -> budgetUsage(userId, b))
                .filter(s -> s != null && !s.isBlank())
                .collect(Collectors.joining("; "));
        String alerts = list.stream()
            .limit(3)
            .map(b -> budgetAlert(userId, b))
            .filter(s -> s != null && !s.isBlank())
            .collect(Collectors.joining("; "));

        String base = summary.isBlank() ? "" : "Budget health: " + summary;
        String alertPart = alerts.isBlank() ? "" : " | Budget alerts: " + alerts;
        return (base + alertPart).trim();
    }

    private String summarizeLowBalanceWallets(Long userId) {
        List<Wallet> list = walletRepository.findByUser_UserId(userId);
        if (list.isEmpty()) return "";

        BigDecimal thresholdVnd = new BigDecimal("200000");
        BigDecimal thresholdDefault = new BigDecimal("20");

        String low = list.stream()
                .map(w -> {
                    BigDecimal bal = zeroSafe(w.getBalance());
                    String code = w.getCurrencyCode() != null ? w.getCurrencyCode().toUpperCase(Locale.ROOT) : "";
                    BigDecimal threshold = "VND".equals(code) || "VNĐ".equals(code) ? thresholdVnd : thresholdDefault;
                    if (bal.compareTo(threshold) < 0) {
                        return w.getWalletName() + "=" + fmt(bal) + (code.isBlank() ? "" : " " + code);
                    }
                    return null;
                })
                .filter(s -> s != null && !s.isBlank())
                .collect(Collectors.joining(", "));

        return low.isBlank() ? "" : "Low balance (<threshold): " + low;
    }

    private String budgetUsage(Long userId, Budget b) {
        BigDecimal spent = transactionRepository.findTransactionsByBudget(
                        userId,
                        b.getCategory().getCategoryId(),
                        b.getWallet() != null ? b.getWallet().getWalletId() : null,
                        b.getStartDate(),
                        b.getEndDate())
                .stream()
                .map(t -> zeroSafe(t.getAmount()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal limit = zeroSafe(b.getAmountLimit());
        if (limit.compareTo(BigDecimal.ZERO) <= 0) return "";
        BigDecimal percent = spent.multiply(BigDecimal.valueOf(100)).divide(limit, 2, java.math.RoundingMode.HALF_UP);
        String cat = b.getCategory() != null ? b.getCategory().getCategoryName() : "";
        String wal = b.getWallet() != null ? b.getWallet().getWalletName() : "all wallets";
        return cat + " (" + wal + "): " + fmt(spent) + "/" + fmt(limit) + " (" + percent.toPlainString() + "%)";
    }

    private String budgetAlert(Long userId, Budget b) {
        BigDecimal spent = transactionRepository.findTransactionsByBudget(
                        userId,
                        b.getCategory().getCategoryId(),
                        b.getWallet() != null ? b.getWallet().getWalletId() : null,
                        b.getStartDate(),
                        b.getEndDate())
                .stream()
                .map(t -> zeroSafe(t.getAmount()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal limit = zeroSafe(b.getAmountLimit());
        if (limit.compareTo(BigDecimal.ZERO) <= 0) return "";
        BigDecimal percent = spent.multiply(BigDecimal.valueOf(100)).divide(limit, 2, java.math.RoundingMode.HALF_UP);
        if (percent.compareTo(BigDecimal.valueOf(80)) < 0) return ""; // alert near/over 80%
        String cat = b.getCategory() != null ? b.getCategory().getCategoryName() : "";
        String wal = b.getWallet() != null ? b.getWallet().getWalletName() : "all wallets";
        return cat + " (" + wal + ") at " + percent.toPlainString() + "%";
    }

    private BigDecimal zeroSafe(BigDecimal v) {
        return v == null ? BigDecimal.ZERO : v;
    }

    private String fmt(BigDecimal v) {
        return zeroSafe(v).setScale(2, java.math.RoundingMode.HALF_UP).toPlainString();
    }

    private String percentChange(BigDecimal current, BigDecimal previous) {
        BigDecimal safeCurrent = zeroSafe(current);
        BigDecimal safePrevious = zeroSafe(previous);

        if (safePrevious.compareTo(BigDecimal.ZERO) == 0) {
            if (safeCurrent.compareTo(BigDecimal.ZERO) == 0) {
                return "0%";
            }
            return "new";
        }

        BigDecimal diff = safeCurrent.subtract(safePrevious);
        BigDecimal change = diff.multiply(BigDecimal.valueOf(100)).divide(safePrevious, 2, java.math.RoundingMode.HALF_UP);
        return change.toPlainString() + "%";
    }

    private String safeAmount(java.math.BigDecimal amount) {
        if (amount == null) return "0";
        return amount.setScale(2, java.math.RoundingMode.HALF_UP).toPlainString();
    }
}
