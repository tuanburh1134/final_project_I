package com.example.financeapp.report;

public class ReportFile {
    private final byte[] data;
    private final String contentType;
    private final String fileName;

    public ReportFile(byte[] data, String contentType, String fileName) {
        this.data = data;
        this.contentType = contentType;
        this.fileName = fileName;
    }

    public byte[] getData() {
        return data;
    }

    public String getContentType() {
        return contentType;
    }

    public String getFileName() {
        return fileName;
    }
}

