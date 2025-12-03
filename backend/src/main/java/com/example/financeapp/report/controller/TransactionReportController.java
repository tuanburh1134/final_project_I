package com.example.financeapp.report.controller;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.financeapp.report.service.ReportService;

@RestController
@RequestMapping("/api/reports")
public class TransactionReportController {

    private final ReportService reportService;

    public TransactionReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/transactions")
    public ResponseEntity<byte[]> exportTransactions(
            @RequestParam("walletId") Long walletId,
            @RequestParam(value = "from", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(value = "to", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(value = "format", defaultValue = "xlsx") String format
    ) throws Exception {

        if (format == null) format = "xlsx";
        format = format.toLowerCase();

        if (format.equals("csv")) {
            String csv = reportService.generateTransactionsCsv(walletId, from, to);
            byte[] data = csv.getBytes(java.nio.charset.StandardCharsets.UTF_8);
            String filename = String.format("transactions_wallet_%d.csv", walletId);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                    .body(data);
        }

        // default: xlsx
        byte[] bytes = reportService.generateTransactionsExcel(walletId, from, to);
        String filename = String.format("transactions_wallet_%d.xlsx", walletId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(bytes);
    }
}
