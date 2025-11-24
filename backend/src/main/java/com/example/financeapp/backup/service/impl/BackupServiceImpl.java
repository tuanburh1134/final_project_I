package com.example.financeapp.backup.service.impl;

import com.example.financeapp.backup.dto.BackupPayload;
import com.example.financeapp.backup.dto.BackupStatusResponse;
import com.example.financeapp.backup.entity.UserBackupStatus;
import com.example.financeapp.backup.repository.UserBackupStatusRepository;
import com.example.financeapp.backup.service.BackupService;
import com.example.financeapp.backup.service.CloudStorageService;
import com.example.financeapp.budget.entity.Budget;
import com.example.financeapp.budget.repository.BudgetRepository;
import com.example.financeapp.fund.entity.Fund;
import com.example.financeapp.fund.entity.FundMember;
import com.example.financeapp.fund.repository.FundMemberRepository;
import com.example.financeapp.fund.repository.FundRepository;
import com.example.financeapp.scheduled.entity.ScheduledTransaction;
import com.example.financeapp.scheduled.repository.ScheduledTransactionRepository;
import com.example.financeapp.transaction.entity.Transaction;
import com.example.financeapp.transaction.repository.TransactionRepository;
import com.example.financeapp.user.entity.User;
import com.example.financeapp.user.repository.UserRepository;
import com.example.financeapp.wallet.entity.Wallet;
import com.example.financeapp.wallet.repository.WalletRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.Objects;
import java.util.zip.GZIPOutputStream;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BackupServiceImpl implements BackupService {

    private static final Logger log = LoggerFactory.getLogger(BackupServiceImpl.class);

    private final UserRepository userRepository;
    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;
    private final BudgetRepository budgetRepository;
    private final ScheduledTransactionRepository scheduledTransactionRepository;
    private final FundRepository fundRepository;
    private final FundMemberRepository fundMemberRepository;
    private final UserBackupStatusRepository backupStatusRepository;
    private final CloudStorageService cloudStorageService;
    private final ObjectMapper objectMapper;

    public BackupServiceImpl(UserRepository userRepository,
                             WalletRepository walletRepository,
                             TransactionRepository transactionRepository,
                             BudgetRepository budgetRepository,
                             ScheduledTransactionRepository scheduledTransactionRepository,
                             FundRepository fundRepository,
                             FundMemberRepository fundMemberRepository,
                             UserBackupStatusRepository backupStatusRepository,
                             CloudStorageService cloudStorageService,
                             ObjectMapper objectMapper) {
        this.userRepository = userRepository;
        this.walletRepository = walletRepository;
        this.transactionRepository = transactionRepository;
        this.budgetRepository = budgetRepository;
        this.scheduledTransactionRepository = scheduledTransactionRepository;
        this.fundRepository = fundRepository;
        this.fundMemberRepository = fundMemberRepository;
        this.backupStatusRepository = backupStatusRepository;
        this.cloudStorageService = cloudStorageService;
        this.objectMapper = objectMapper;
    }

    @Override
    @Transactional
    public void backupAllUsers() {
        userRepository.findAll().forEach(user -> {
            try {
                backupUserInternal(user);
            } catch (Exception ex) {
                log.error("Failed to backup user {}: {}", user.getUserId(), ex.getMessage(), ex);
                updateStatus(user, null, "FAILED", ex.getMessage());
            }
        });
    }

    @Override
    @Transactional
    public void backupUser(Long userId) {
        User user = userRepository.findById(Objects.requireNonNull(userId, "User không hợp lệ"))
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));
        try {
            backupUserInternal(user);
        } catch (Exception ex) {
            log.error("Failed to backup user {}: {}", user.getUserId(), ex.getMessage(), ex);
            updateStatus(user, null, "FAILED", ex.getMessage());
            throw new RuntimeException("Sao lưu thất bại: " + ex.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public BackupStatusResponse getStatus(Long userId) {
        BackupStatusResponse response = new BackupStatusResponse();
        backupStatusRepository.findByUser_UserId(userId).ifPresent(status -> {
            response.setLastBackupAt(status.getLastBackupAt());
            response.setLastBackupLocation(status.getLastBackupLocation());
            response.setLastStatus(status.getLastStatus());
            response.setLastError(status.getLastError());
        });
        return response;
    }

    private void backupUserInternal(User user) throws IOException {
        BackupPayload payload = buildPayload(user);
        byte[] json = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsBytes(payload);
        byte[] compressed = gzip(json);
        String key = String.format("backups/user-%d/%s.json.gz",
                user.getUserId(), Base64.getUrlEncoder().withoutPadding().encodeToString(
                        (LocalDateTime.now() + "-" + user.getUserId()).getBytes()
                ));
        String location = cloudStorageService.upload(key, compressed, "application/gzip");
        updateStatus(user, location, "SUCCESS", null);
        log.info("Backup created for user {} at {}", user.getUserId(), location);
    }

    private BackupPayload buildPayload(User user) {
        BackupPayload payload = new BackupPayload();

        BackupPayload.UserInfo userInfo = new BackupPayload.UserInfo();
        userInfo.setUserId(user.getUserId());
        userInfo.setFullName(user.getFullName());
        userInfo.setEmail(user.getEmail());
        payload.setUser(userInfo);

        List<Wallet> wallets = walletRepository.findByUser_UserId(user.getUserId());
        payload.setWallets(wallets.stream().map(wallet -> {
            BackupPayload.WalletInfo info = new BackupPayload.WalletInfo();
            info.setWalletId(wallet.getWalletId());
            info.setWalletName(wallet.getWalletName());
            info.setCurrency(wallet.getCurrencyCode());
            info.setBalance(wallet.getBalance());
            info.setWalletType(wallet.getWalletType());
            info.setCreatedAt(wallet.getCreatedAt());
            return info;
        }).toList());

        List<Transaction> transactions = transactionRepository.findByUser_UserIdOrderByTransactionDateDesc(user.getUserId());
        payload.setTransactions(transactions.stream().map(tx -> {
            BackupPayload.TransactionInfo info = new BackupPayload.TransactionInfo();
            info.setTransactionId(tx.getTransactionId());
            info.setWalletName(tx.getWallet() != null ? tx.getWallet().getWalletName() : null);
            info.setCategoryName(tx.getCategory() != null ? tx.getCategory().getCategoryName() : null);
            info.setTransactionType(tx.getTransactionType() != null ? tx.getTransactionType().getTypeName() : null);
            info.setAmount(tx.getAmount());
            info.setTransactionDate(tx.getTransactionDate());
            info.setNote(tx.getNote());
            return info;
        }).toList());

        List<Budget> budgets = budgetRepository.findByUser_UserIdOrderByStartDateDesc(user.getUserId());
        payload.setBudgets(budgets.stream().map(budget -> {
            BackupPayload.BudgetInfo info = new BackupPayload.BudgetInfo();
            info.setBudgetId(budget.getBudgetId());
            info.setCategoryName(budget.getCategory() != null ? budget.getCategory().getCategoryName() : null);
            info.setWalletName(budget.getWallet() != null ? budget.getWallet().getWalletName() : "Tất cả ví");
            info.setAmountLimit(budget.getAmountLimit());
            info.setStartDate(budget.getStartDate());
            info.setEndDate(budget.getEndDate());
            info.setStatus(budget.getStatus() != null ? budget.getStatus().name() : null);
            return info;
        }).toList());

        List<ScheduledTransaction> scheduledTransactions =
                scheduledTransactionRepository.findByUser_UserIdOrderByCreatedAtDesc(user.getUserId());
        payload.setScheduledTransactions(scheduledTransactions.stream().map(st -> {
            BackupPayload.ScheduledTransactionInfo info = new BackupPayload.ScheduledTransactionInfo();
            info.setScheduleId(st.getScheduleId());
            info.setWalletName(st.getWallet() != null ? st.getWallet().getWalletName() : null);
            info.setCategoryName(st.getCategory() != null ? st.getCategory().getCategoryName() : null);
            info.setTransactionType(st.getTransactionType() != null ? st.getTransactionType().getTypeName() : null);
            info.setAmount(st.getAmount());
            info.setScheduleType(st.getScheduleType().name());
            info.setScheduleTime(st.getScheduleTime());
            info.setNextRunAt(st.getNextRunAt());
            info.setStatus(st.getStatus().name());
            return info;
        }).toList());

        List<Fund> funds = fundRepository.findAccessibleFunds(user.getUserId());
        payload.setFunds(funds.stream().map(fund -> {
            BackupPayload.FundInfo info = new BackupPayload.FundInfo();
            info.setFundId(fund.getFundId());
            info.setFundName(fund.getFundName());
            info.setFundType(fund.getFundType().name());
            info.setTermType(fund.getTermType().name());
            info.setTargetAmount(fund.getTargetAmount());
            info.setCurrentBalance(fund.getWallet().getBalance());
            info.setStartDate(fund.getStartDate());
            info.setEndDate(fund.getEndDate());
            info.setProgressPercent(calculateProgress(fund.getWallet().getBalance(), fund.getTargetAmount()));
            List<FundMember> members = fundMemberRepository.findByFund_FundId(fund.getFundId());
            info.setMembers(members.stream()
                    .map(member -> {
                        BackupPayload.FundMemberInfo mi = new BackupPayload.FundMemberInfo();
                        mi.setFullName(member.getMemberName());
                        mi.setEmail(member.getMemberEmail());
                        mi.setRole(member.getRole().name());
                        return mi;
                    }).toList());
            return info;
        }).toList());

        payload.setGeneratedAt(LocalDateTime.now());
        return payload;
    }

    private double calculateProgress(java.math.BigDecimal current, java.math.BigDecimal target) {
        if (target == null || target.compareTo(java.math.BigDecimal.ZERO) <= 0 || current == null) {
            return 0;
        }
        return current.divide(target, 4, java.math.RoundingMode.HALF_UP)
                .multiply(java.math.BigDecimal.valueOf(100))
                .doubleValue();
    }

    private byte[] gzip(byte[] input) throws IOException {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             GZIPOutputStream gzipOutputStream = new GZIPOutputStream(baos)) {
            gzipOutputStream.write(input);
            gzipOutputStream.finish();
            return baos.toByteArray();
        }
    }

    private void updateStatus(User user, String location, String status, String error) {
        UserBackupStatus backupStatus = backupStatusRepository.findByUser_UserId(user.getUserId())
                .orElseGet(UserBackupStatus::new);
        backupStatus.setUser(user);
        backupStatus.setLastBackupAt(LocalDateTime.now());
        backupStatus.setLastBackupLocation(location);
        backupStatus.setLastStatus(status);
        backupStatus.setLastError(error);
        backupStatusRepository.save(backupStatus);
    }
}

