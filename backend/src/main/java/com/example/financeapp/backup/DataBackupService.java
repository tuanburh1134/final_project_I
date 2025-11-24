package com.example.financeapp.backup;

import com.example.financeapp.budget.entity.Budget;
import com.example.financeapp.budget.repository.BudgetRepository;
import com.example.financeapp.transaction.entity.Transaction;
import com.example.financeapp.transaction.repository.TransactionRepository;
import com.example.financeapp.user.entity.User;
import com.example.financeapp.user.repository.UserRepository;
import com.example.financeapp.wallet.entity.Wallet;
import com.example.financeapp.wallet.repository.WalletRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
@RequiredArgsConstructor
public class DataBackupService {

    private static final Logger log = LoggerFactory.getLogger(DataBackupService.class);

    private final UserRepository userRepository;
    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;
    private final BudgetRepository budgetRepository;
    private final CloudStorageService cloudStorageService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public List<String> backupAllUsers() {
        List<User> users = userRepository.findAll();
        List<String> locations = new ArrayList<>();

        for (User user : users) {
            try {
                byte[] archive = buildUserBackup(user);
                String key = "backups/%d/%s.zip".formatted(
                        user.getUserId(),
                        LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"))
                );
                String location = cloudStorageService.store(key, archive, "application/zip");
                locations.add(location);
                log.info("Đã backup user {} tới {}", user.getEmail(), location);
            } catch (Exception ex) {
                log.error("Backup thất bại cho user {}", user.getEmail(), ex);
            }
        }
        return locations;
    }

    private byte[] buildUserBackup(User user) throws IOException {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("user", Map.of(
                "userId", user.getUserId(),
                "email", user.getEmail(),
                "fullName", user.getFullName(),
                "role", user.getRole() != null ? user.getRole().name() : null,
                "createdAt", user.getCreatedAt()
        ));

        List<Wallet> wallets = walletRepository.findByUser_UserId(user.getUserId());
        payload.put("wallets", wallets.stream().map(wallet -> Map.of(
                "walletId", wallet.getWalletId(),
                "walletName", wallet.getWalletName(),
                "currency", wallet.getCurrencyCode(),
                "balance", wallet.getBalance(),
                "walletType", wallet.getWalletType(),
                "createdAt", wallet.getCreatedAt()
        )).toList());

        List<Transaction> transactions = transactionRepository.findByUser_UserIdOrderByTransactionDateDesc(user.getUserId());
        payload.put("transactions", transactions.stream().map(tx -> Map.of(
                "transactionId", tx.getTransactionId(),
                "type", tx.getTransactionType().getTypeName(),
                "category", tx.getCategory().getCategoryName(),
                "wallet", tx.getWallet().getWalletName(),
                "amount", tx.getAmount(),
                "note", tx.getNote(),
                "transactionDate", tx.getTransactionDate()
        )).toList());

        List<Budget> budgets = budgetRepository.findByUserOrderByStartDateDesc(user);
        payload.put("budgets", budgets.stream().map(budget -> Map.of(
                "budgetId", budget.getBudgetId(),
                "category", budget.getCategory().getCategoryName(),
                "walletId", budget.getWallet() != null ? budget.getWallet().getWalletId() : null,
                "amountLimit", budget.getAmountLimit(),
                "startDate", budget.getStartDate(),
                "endDate", budget.getEndDate(),
                "status", budget.getStatus() != null ? budget.getStatus().name() : null
        )).toList());

        byte[] json = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsBytes(payload);

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ZipOutputStream zos = new ZipOutputStream(baos)) {
            ZipEntry entry = new ZipEntry("data.json");
            zos.putNextEntry(entry);
            zos.write(json);
            zos.closeEntry();
            zos.finish();
            return baos.toByteArray();
        }
    }
}

