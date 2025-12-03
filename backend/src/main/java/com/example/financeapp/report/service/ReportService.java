package com.example.financeapp.report.service;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import com.example.financeapp.transaction.entity.Transaction;
import com.example.financeapp.transaction.repository.TransactionRepository;

@Service
public class ReportService {

    private final TransactionRepository transactionRepository;

    public ReportService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    public byte[] generateTransactionsExcel(Long walletId, LocalDate from, LocalDate to) throws Exception {
        List<Transaction> list = transactionRepository.findDetailedByWalletId(walletId);

        // Filter by date range if provided
        if (from != null || to != null) {
            LocalDateTime start = from != null ? from.atStartOfDay() : LocalDate.MIN.atStartOfDay();
            LocalDateTime end = to != null ? to.atTime(LocalTime.MAX) : LocalDate.MAX.atTime(LocalTime.MAX);
            list = list.stream()
                    .filter(t -> {
                        LocalDateTime d = t.getTransactionDate();
                        return (d.isEqual(start) || d.isAfter(start)) && (d.isEqual(end) || d.isBefore(end));
                    }).collect(Collectors.toList());
        }

        try (Workbook wb = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            var sheet = wb.createSheet("Transactions");

            XSSFFont headerFont = ((XSSFWorkbook) wb).createFont();
            headerFont.setBold(true);
            headerFont.setFontHeightInPoints((short) 11);

            XSSFCellStyle headerStyle = ((XSSFWorkbook) wb).createCellStyle();
            headerStyle.setFont(headerFont);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);

            // Header
            Row header = sheet.createRow(0);
            String[] cols = new String[]{"STT", "Thời gian", "Loại", "Ghi chú", "Số tiền", "Tiền tệ", "Người tạo"};
            for (int i = 0; i < cols.length; i++) {
                var c = header.createCell(i);
                c.setCellValue(cols[i]);
                c.setCellStyle(headerStyle);
            }

            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            int rowIdx = 1;
            for (Transaction t : list) {
                Row r = sheet.createRow(rowIdx++);
                int col = 0;
                r.createCell(col++).setCellValue(rowIdx - 1);
                r.createCell(col++).setCellValue(t.getTransactionDate() != null ? t.getTransactionDate().format(dtf) : "");
                String type = t.getTransactionType() != null ? t.getTransactionType().getTypeName() : "";
                r.createCell(col++).setCellValue(type);
                r.createCell(col++).setCellValue(t.getNote() != null ? t.getNote() : "");
                BigDecimal amount = t.getAmount() != null ? t.getAmount() : BigDecimal.ZERO;
                r.createCell(col++).setCellValue(amount.doubleValue());
                String currency = t.getOriginalCurrency() != null ? t.getOriginalCurrency() : (t.getWallet() != null ? t.getWallet().getCurrencyCode() : "");
                r.createCell(col++).setCellValue(currency != null ? currency : "");
                String creator = t.getUser() != null ? (t.getUser().getEmail() != null ? t.getUser().getEmail() : String.valueOf(t.getUser().getUserId())) : "";
                r.createCell(col++).setCellValue(creator);
            }

            for (int i = 0; i < cols.length; i++) {
                sheet.autoSizeColumn(i);
            }

            wb.write(out);
            return out.toByteArray();
        }
    }

    public String generateTransactionsCsv(Long walletId, LocalDate from, LocalDate to) {
        List<Transaction> list = transactionRepository.findDetailedByWalletId(walletId);
        if (from != null || to != null) {
            LocalDateTime start = from != null ? from.atStartOfDay() : LocalDate.MIN.atStartOfDay();
            LocalDateTime end = to != null ? to.atTime(LocalTime.MAX) : LocalDate.MAX.atTime(LocalTime.MAX);
            list = list.stream()
                    .filter(t -> {
                        LocalDateTime d = t.getTransactionDate();
                        return (d.isEqual(start) || d.isAfter(start)) && (d.isEqual(end) || d.isBefore(end));
                    }).collect(Collectors.toList());
        }

        StringBuilder sb = new StringBuilder();
        sb.append("STT,Thoi_gian,Loai,Ghi_chu,So_tien,Tien_te,Nguoi_tao\n");
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        int idx = 1;
        for (Transaction t : list) {
            String time = t.getTransactionDate() != null ? t.getTransactionDate().format(dtf) : "";
            String type = t.getTransactionType() != null ? t.getTransactionType().getTypeName() : "";
            String note = t.getNote() != null ? t.getNote().replaceAll("[\\r\\n,]", " ") : "";
            String amount = t.getAmount() != null ? t.getAmount().toPlainString() : "0";
            String currency = t.getOriginalCurrency() != null ? t.getOriginalCurrency() : (t.getWallet() != null ? t.getWallet().getCurrencyCode() : "");
            String creator = t.getUser() != null ? (t.getUser().getEmail() != null ? t.getUser().getEmail() : String.valueOf(t.getUser().getUserId())) : "";
            sb.append(idx++).append(",").append(time).append(",").append(type).append(",")
              .append(note).append(",").append(amount).append(",").append(currency).append(",").append(creator).append("\n");
        }
        return sb.toString();
    }
}
