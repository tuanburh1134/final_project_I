package com.example.financeapp.report;

import com.example.financeapp.backup.DataBackupService;
import com.example.financeapp.transaction.entity.Transaction;
import com.example.financeapp.transaction.repository.TransactionRepository;
import com.example.financeapp.user.entity.User;
import com.example.financeapp.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/reports")
@RequiredArgsConstructor
public class ReportController {

    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;
    private final ReportExportService reportExportService;
    private final DataBackupService dataBackupService;

    private Long getCurrentUserId() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .map(User::getUserId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy user"));
    }

    @GetMapping("/transactions/excel")
    public ResponseEntity<byte[]> exportTransactionsExcel() {
        Long userId = getCurrentUserId();
        List<Transaction> transactions = transactionRepository.findByUser_UserIdOrderByTransactionDateDesc(userId);
        byte[] data = reportExportService.exportTransactionsToExcel(transactions);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=transactions.xlsx")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(data);
    }

    @GetMapping("/transactions/pdf")
    public ResponseEntity<byte[]> exportTransactionsPdf() {
        Long userId = getCurrentUserId();
        List<Transaction> transactions = transactionRepository.findByUser_UserIdOrderByTransactionDateDesc(userId);
        byte[] data = reportExportService.exportTransactionsToPdf(transactions);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=transactions.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(data);
    }

    @PostMapping("/backup/run")
    public ResponseEntity<Map<String, Object>> triggerBackup() {
        Map<String, Object> res = new HashMap<>();
        res.put("locations", dataBackupService.backupAllUsers());
        res.put("message", "Đã chạy backup thủ công");
        return ResponseEntity.ok(res);
    }
}

