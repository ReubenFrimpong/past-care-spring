package com.reuben.pastcare_spring.services;

import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Service for generating PDF reports using iText.
 */
@Service
@RequiredArgsConstructor
public class PdfReportService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * Generate a PDF report from headers and data rows.
     *
     * @param title Report title
     * @param headers List of column headers
     * @param rows List of data rows (each row is a list of values)
     * @return PDF file as byte array
     */
    public byte[] generatePdfReport(String title, List<String> headers, List<List<Object>> rows) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try (PdfWriter writer = new PdfWriter(out);
             PdfDocument pdf = new PdfDocument(writer);
             Document document = new Document(pdf)) {

            // Add title
            Paragraph titleParagraph = new Paragraph(title)
                    .setFontSize(18)
                    .setBold()
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(20);
            document.add(titleParagraph);

            // Add generation date
            Paragraph dateParagraph = new Paragraph("Generated on: " + LocalDate.now().format(DATE_FORMATTER))
                    .setFontSize(10)
                    .setTextAlignment(TextAlignment.RIGHT)
                    .setMarginBottom(10);
            document.add(dateParagraph);

            // Create table with dynamic number of columns
            float[] columnWidths = new float[headers.size()];
            for (int i = 0; i < headers.size(); i++) {
                columnWidths[i] = 1f; // Equal width for all columns
            }

            Table table = new Table(UnitValue.createPercentArray(columnWidths));
            table.setWidth(UnitValue.createPercentValue(100));

            // Add header cells
            for (String header : headers) {
                Cell headerCell = new Cell()
                        .add(new Paragraph(header).setBold())
                        .setBackgroundColor(ColorConstants.LIGHT_GRAY)
                        .setTextAlignment(TextAlignment.CENTER);
                table.addHeaderCell(headerCell);
            }

            // Add data rows
            for (List<Object> row : rows) {
                for (Object value : row) {
                    String cellValue = formatValue(value);
                    Cell cell = new Cell().add(new Paragraph(cellValue));
                    table.addCell(cell);
                }
            }

            document.add(table);

            // Add footer
            Paragraph footer = new Paragraph("Total Records: " + rows.size())
                    .setFontSize(10)
                    .setMarginTop(10);
            document.add(footer);
        }

        return out.toByteArray();
    }

    /**
     * Format a value for display in PDF.
     */
    private String formatValue(Object value) {
        if (value == null) {
            return "";
        } else if (value instanceof LocalDate) {
            return ((LocalDate) value).format(DATE_FORMATTER);
        } else if (value instanceof LocalDateTime) {
            return ((LocalDateTime) value).format(DATE_TIME_FORMATTER);
        } else if (value instanceof Boolean) {
            return ((Boolean) value) ? "Yes" : "No";
        } else {
            return value.toString();
        }
    }

    /**
     * Add a header to the PDF document with church branding.
     * (To be enhanced in Phase 3)
     */
    public void addHeader(Document document, String churchName, String reportTitle) {
        Paragraph header = new Paragraph(churchName)
                .setFontSize(14)
                .setBold()
                .setTextAlignment(TextAlignment.CENTER);
        document.add(header);

        Paragraph title = new Paragraph(reportTitle)
                .setFontSize(16)
                .setBold()
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(20);
        document.add(title);
    }
}
