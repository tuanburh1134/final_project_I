package com.example.financeapp.report;

import com.example.financeapp.transaction.entity.Transaction;
import com.example.financeapp.transaction.service.TransactionService;
import com.example.financeapp.user.entity.User;
import com.example.financeapp.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/reports")
public class ReportController {

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private ReportService reportService;

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/transactions/export")
    public ResponseEntity<byte[]> exportTransactions(
            @RequestParam(defaultValue = "excel") String format,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) Long walletId
    ) {
        Long userId = getCurrentUserId();
        ReportFormat reportFormat = ReportFormat.fromString(format);

        List<Transaction> transactions = transactionService.getTransactionsForReport(userId, startDate, endDate, walletId);
        ReportFile reportFile = reportService.generateTransactionReport(transactions, reportFormat);

        String contentType = reportFile.getContentType();
        String safeContentType = contentType != null
                ? contentType
                : MediaType.APPLICATION_OCTET_STREAM_VALUE;
        MediaType outputMediaType = MediaType.parseMediaType(Objects.requireNonNull(safeContentType));

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + reportFile.getFileName())
                .contentType(outputMediaType)
                .body(reportFile.getData());
    }

    private Long getCurrentUserId() {
        org.springframework.security.core.Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null) {
            throw new RuntimeException("Không tìm thấy thông tin người dùng");
        }
        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));
        return user.getUserId();
    }
}

