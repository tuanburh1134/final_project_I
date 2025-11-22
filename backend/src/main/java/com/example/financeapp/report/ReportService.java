package com.example.financeapp.report;

import com.example.financeapp.transaction.entity.Transaction;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class ReportService {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final String[] HEADERS = {
            "Ngày giao dịch", "Loại", "Danh mục", "Ví", "Số tiền", "Ghi chú"
    };

    public ReportFile generateTransactionReport(List<Transaction> transactions, ReportFormat format) {
        return switch (format) {
            case PDF -> generatePdf(transactions);
            case EXCEL -> generateExcel(transactions);
        };
    }

    private ReportFile generateExcel(List<Transaction> transactions) {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Transactions");

            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < HEADERS.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(HEADERS[i]);
            }

            int rowIdx = 1;
            for (Transaction tx : transactions) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(formatDate(tx));
                row.createCell(1).setCellValue(tx.getTransactionType() != null ? tx.getTransactionType().getTypeName() : "");
                row.createCell(2).setCellValue(tx.getCategory() != null ? tx.getCategory().getCategoryName() : "");
                row.createCell(3).setCellValue(tx.getWallet() != null ? tx.getWallet().getWalletName() : "");
                row.createCell(4).setCellValue(formatAmount(tx.getAmount()));
                row.createCell(5).setCellValue(tx.getNote() != null ? tx.getNote() : "");
            }

            for (int i = 0; i < HEADERS.length; i++) {
                sheet.autoSizeColumn(i);
            }

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);

            return new ReportFile(outputStream.toByteArray(),
                    ReportFormat.EXCEL.getContentType(),
                    buildFileName("xlsx"));
        } catch (IOException e) {
            throw new RuntimeException("Không thể tạo file Excel", e);
        }
    }

    private ReportFile generatePdf(List<Transaction> transactions) {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4.rotate());
            PdfWriter.getInstance(document, outputStream);
            document.open();

            document.add(new Paragraph("Báo cáo giao dịch"));
            document.add(new Paragraph("\n"));

            PdfPTable table = new PdfPTable(HEADERS.length);
            table.setWidthPercentage(100);

            for (String header : HEADERS) {
                PdfPCell cell = new PdfPCell();
                cell.setPhrase(new com.lowagie.text.Phrase(header));
                table.addCell(cell);
            }

            for (Transaction tx : transactions) {
                table.addCell(formatDate(tx));
                table.addCell(tx.getTransactionType() != null ? tx.getTransactionType().getTypeName() : "");
                table.addCell(tx.getCategory() != null ? tx.getCategory().getCategoryName() : "");
                table.addCell(tx.getWallet() != null ? tx.getWallet().getWalletName() : "");
                table.addCell(formatAmount(tx.getAmount()));
                table.addCell(tx.getNote() != null ? tx.getNote() : "");
            }

            document.add(table);
            document.close();

            return new ReportFile(outputStream.toByteArray(),
                    ReportFormat.PDF.getContentType(),
                    buildFileName("pdf"));
        } catch (DocumentException | IOException e) {
            throw new RuntimeException("Không thể tạo file PDF", e);
        }
    }

    private String formatDate(Transaction tx) {
        return tx.getTransactionDate() != null
                ? tx.getTransactionDate().format(DATE_TIME_FORMATTER)
                : "";
    }

    private String formatAmount(BigDecimal amount) {
        if (amount == null) return "0";
        return amount.stripTrailingZeros().toPlainString();
    }

    private String buildFileName(String extension) {
        String timestamp = java.time.LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        return "transactions_report_" + timestamp + "." + extension;
    }
}

