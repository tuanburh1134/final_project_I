package com.example.financeapp.budget.service.impl;

import com.example.financeapp.budget.dto.BudgetAlert;
import com.example.financeapp.budget.dto.BudgetSummaryResponse;
import com.example.financeapp.budget.entity.Budget;
import com.example.financeapp.budget.entity.Budget.BudgetStatus;
import com.example.financeapp.budget.repository.BudgetRepository;
import com.example.financeapp.category.entity.Category;
import com.example.financeapp.category.repository.CategoryRepository;
import com.example.financeapp.common.service.EmailService;
import com.example.financeapp.transaction.entity.Transaction;
import com.example.financeapp.transaction.entity.TransactionType;
import com.example.financeapp.transaction.repository.TransactionRepository;
import com.example.financeapp.user.entity.User;
import com.example.financeapp.user.repository.UserRepository;
import com.example.financeapp.wallet.entity.Wallet;
import com.example.financeapp.wallet.repository.WalletRepository;
import com.example.financeapp.wallet.service.WalletService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SuppressWarnings({"null", "NullAway"})
@ExtendWith(MockitoExtension.class)
class BudgetServiceImplTest {

    @Mock private BudgetRepository budgetRepository;
    @Mock private UserRepository userRepository;
    @Mock private WalletRepository walletRepository;
    @Mock private CategoryRepository categoryRepository;
    @Mock private WalletService walletService;
    @Mock private TransactionRepository transactionRepository;
    @Mock private EmailService emailService;

    @InjectMocks
    private BudgetServiceImpl budgetService;

    private User user;
    private Wallet wallet;
    private Category category;
    private Budget budget;

    @BeforeEach
    void setup() {
        user = new User();
        user.setUserId(1L);
        user.setEmail("user@example.com");

        wallet = new Wallet();
        wallet.setWalletId(10L);
        wallet.setWalletName("Ví chính");

        category = new Category();
        category.setCategoryId(5L);
        category.setCategoryName("Ăn uống");

        budget = new Budget();
        budget.setBudgetId(100L);
        budget.setUser(user);
        budget.setWallet(wallet);
        budget.setCategory(category);
        budget.setAmountLimit(BigDecimal.valueOf(5_000_000));
        budget.setStartDate(LocalDate.now().minusDays(5));
        budget.setEndDate(LocalDate.now().plusDays(5));
        budget.setStatus(BudgetStatus.ACTIVE);

        when(budgetRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    void handleExpenseTransaction_shouldMarkTransactionOverBudget_andUpdateBudget() {
        Transaction transaction = buildTransaction(BigDecimal.valueOf(1_500_000));

        when(budgetRepository.findApplicableBudgets(eq(1L), eq(5L), eq(10L), any()))
                .thenReturn(List.of(budget));
        when(transactionRepository.sumExpensesForBudget(eq(1L), eq(5L), eq(10L),
                any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(BigDecimal.valueOf(5_500_000));
        when(transactionRepository.save(any(Transaction.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        BudgetAlert alert = budgetService.handleExpenseTransaction(transaction);

        ArgumentCaptor<Transaction> txCaptor = ArgumentCaptor.forClass(Transaction.class);
        verify(transactionRepository, atLeastOnce()).save(txCaptor.capture());
        Transaction savedTx = txCaptor.getValue();
        assertThat(savedTx.isOverBudget()).isTrue();
        assertThat(savedTx.getOverBudgetAmount()).isEqualByComparingTo("500000");
        assertThat(savedTx.getBudget()).isEqualTo(budget);

        assertThat(budget.getStatus()).isEqualTo(BudgetStatus.OVER_LIMIT);
        assertThat(budget.getOverBudgetAmount()).isEqualByComparingTo("500000");
        verify(emailService).sendBudgetExceededEmail(eq("user@example.com"), eq("Ăn uống"),
                eq(BigDecimal.valueOf(5_500_000)), eq(BigDecimal.valueOf(5_000_000)));
        assertThat(alert).isNotNull();
        assertThat(alert.getLevel()).isEqualTo("OVER_LIMIT");
        assertThat(alert.getOverBudgetAmount()).isEqualByComparingTo("500000");
    }

    @Test
    void getBudgets_shouldReturnBudgetWithStatusAndOverBudgetFields() {
        budget.setWallet(null); // áp dụng tất cả ví
        when(budgetRepository.findByUser_UserIdOrderByStartDateDesc(1L))
                .thenReturn(List.of(budget));
        when(transactionRepository.sumExpensesForBudget(eq(1L), eq(5L), isNull(),
                any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(BigDecimal.valueOf(4_000_000));

        List<BudgetSummaryResponse> summaries = budgetService.getBudgets(1L);

        assertThat(summaries).hasSize(1);
        BudgetSummaryResponse resp = summaries.get(0);
        assertThat(resp.getAmountLimit()).isEqualByComparingTo("5000000");
        assertThat(resp.getSpentAmount()).isEqualByComparingTo("4000000");
        assertThat(resp.getRemainingAmount()).isEqualByComparingTo("1000000");
        assertThat(resp.getBudgetStatus()).isEqualTo(BudgetStatus.ACTIVE.name());
        assertThat(resp.getOverBudgetAmount()).isEqualByComparingTo("0");
        assertThat(resp.isHasExceededBudget()).isFalse();
    }

    private Transaction buildTransaction(BigDecimal amount) {
        Transaction tx = new Transaction();
        tx.setTransactionId(999L);
        tx.setUser(user);
        tx.setWallet(wallet);
        tx.setCategory(category);
        TransactionType type = new TransactionType();
        type.setTypeId(1L);
        type.setTypeName("Chi tiêu");
        tx.setTransactionType(type);
        tx.setAmount(amount);
        tx.setTransactionDate(LocalDateTime.now());
        return tx;
    }
}

