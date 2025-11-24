package com.example.financeapp.report;

import com.example.financeapp.transaction.entity.Transaction;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class ReportExportService {

    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public byte[] exportTransactionsToExcel(List<Transaction> transactions) {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Transactions");
            sheet.setDefaultColumnWidth(20);

            Row header = sheet.createRow(0);
            String[] headers = {"Ngày giao dịch", "Loại", "Danh mục", "Ví", "Số tiền", "Ghi chú"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = header.createCell(i);
                cell.setCellValue(headers[i]);
                CellStyle style = workbook.createCellStyle();
                Font font = workbook.createFont();
                font.setBold(true);
                style.setFont(font);
                cell.setCellStyle(style);
            }

            int rowIdx = 1;
            for (Transaction tx : transactions) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(DATE_TIME_FORMATTER.format(tx.getTransactionDate()));
                row.createCell(1).setCellValue(tx.getTransactionType().getTypeName());
                row.createCell(2).setCellValue(tx.getCategory().getCategoryName());
                row.createCell(3).setCellValue(tx.getWallet().getWalletName());
                row.createCell(4).setCellValue(tx.getAmount().doubleValue());
                row.createCell(5).setCellValue(tx.getNote() != null ? tx.getNote() : "");
            }

            workbook.write(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Không thể xuất Excel", e);
        }
    }

    public byte[] exportTransactionsToPdf(List<Transaction> transactions) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document document = new Document();
            PdfWriter.getInstance(document, out);
            document.open();

            document.add(new Paragraph("Báo cáo giao dịch",
                    FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16)));
            document.add(new Paragraph(" "));

            PdfPTable table = new PdfPTable(6);
            table.setWidthPercentage(100);
            table.addCell("Ngày giao dịch");
            table.addCell("Loại");
            table.addCell("Danh mục");
            table.addCell("Ví");
            table.addCell("Số tiền");
            table.addCell("Ghi chú");

            for (Transaction tx : transactions) {
                table.addCell(DATE_TIME_FORMATTER.format(tx.getTransactionDate()));
                table.addCell(tx.getTransactionType().getTypeName());
                table.addCell(tx.getCategory().getCategoryName());
                table.addCell(tx.getWallet().getWalletName());
                table.addCell(tx.getAmount().toPlainString());
                table.addCell(tx.getNote() != null ? tx.getNote() : "");
            }

            document.add(table);
            document.close();
            return out.toByteArray();
        } catch (DocumentException | IOException e) {
            throw new RuntimeException("Không thể xuất PDF", e);
        }
    }
}

