package com.example.financeapp.report;

public enum ReportFormat {
    EXCEL("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", "xlsx"),
    PDF("application/pdf", "pdf");

    private final String contentType;
    private final String extension;

    ReportFormat(String contentType, String extension) {
        this.contentType = contentType;
        this.extension = extension;
    }

    public String getContentType() {
        return contentType;
    }

    public String getExtension() {
        return extension;
    }

    public static ReportFormat fromString(String value) {
        if (value == null) {
            return EXCEL;
        }
        return switch (value.trim().toLowerCase()) {
            case "pdf" -> PDF;
            case "excel", "xlsx" -> EXCEL;
            default -> throw new IllegalArgumentException("Định dạng báo cáo không hỗ trợ: " + value);
        };
    }
}

