package com.reuben.pastcare_spring.enums;

/**
 * Enumeration of supported report export formats.
 */
public enum ReportFormat {
    PDF("application/pdf", ".pdf"),
    EXCEL("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", ".xlsx"),
    CSV("text/csv", ".csv");

    private final String mimeType;
    private final String extension;

    ReportFormat(String mimeType, String extension) {
        this.mimeType = mimeType;
        this.extension = extension;
    }

    public String getMimeType() {
        return mimeType;
    }

    public String getExtension() {
        return extension;
    }
}
