package com.reuben.pastcare_spring.services;

import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Service for generating Excel reports using Apache POI.
 */
@Service
@RequiredArgsConstructor
public class ExcelReportService {

    /**
     * Generate an Excel report from headers and data rows.
     *
     * @param headers List of column headers
     * @param rows List of data rows (each row is a list of values)
     * @param sheetName Name of the worksheet
     * @return Excel file as byte array
     */
    public byte[] generateExcelReport(List<String> headers, List<List<Object>> rows, String sheetName) throws IOException {
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet(sheetName);

            // Create styles
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle dateStyle = createDateStyle(workbook);
            CellStyle dateTimeStyle = createDateTimeStyle(workbook);
            CellStyle currencyStyle = createCurrencyStyle(workbook);

            // Create header row
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.size(); i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers.get(i));
                cell.setCellStyle(headerStyle);
            }

            // Create data rows
            for (int rowIndex = 0; rowIndex < rows.size(); rowIndex++) {
                Row row = sheet.createRow(rowIndex + 1);
                List<Object> rowData = rows.get(rowIndex);

                for (int colIndex = 0; colIndex < rowData.size(); colIndex++) {
                    Cell cell = row.createCell(colIndex);
                    Object value = rowData.get(colIndex);

                    if (value == null) {
                        cell.setCellValue("");
                    } else if (value instanceof Number) {
                        cell.setCellValue(((Number) value).doubleValue());
                    } else if (value instanceof LocalDate) {
                        cell.setCellValue((LocalDate) value);
                        cell.setCellStyle(dateStyle);
                    } else if (value instanceof LocalDateTime) {
                        cell.setCellValue((LocalDateTime) value);
                        cell.setCellStyle(dateTimeStyle);
                    } else if (value instanceof Boolean) {
                        cell.setCellValue(((Boolean) value) ? "Yes" : "No");
                    } else {
                        cell.setCellValue(value.toString());
                    }
                }
            }

            // Auto-size columns
            autoSizeColumns(sheet, headers.size());

            workbook.write(out);
            return out.toByteArray();
        }
    }

    /**
     * Create header style with bold font and background color.
     */
    public CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);

        style.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);

        return style;
    }

    /**
     * Create date style for formatting date cells.
     */
    public CellStyle createDateStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        CreationHelper createHelper = workbook.getCreationHelper();
        style.setDataFormat(createHelper.createDataFormat().getFormat("yyyy-mm-dd"));
        return style;
    }

    /**
     * Create date-time style for formatting date-time cells.
     */
    public CellStyle createDateTimeStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        CreationHelper createHelper = workbook.getCreationHelper();
        style.setDataFormat(createHelper.createDataFormat().getFormat("yyyy-mm-dd hh:mm:ss"));
        return style;
    }

    /**
     * Create currency style for formatting currency cells.
     */
    public CellStyle createCurrencyStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        CreationHelper createHelper = workbook.getCreationHelper();
        style.setDataFormat(createHelper.createDataFormat().getFormat("$#,##0.00"));
        return style;
    }

    /**
     * Auto-size all columns to fit content.
     */
    public void autoSizeColumns(Sheet sheet, int columnCount) {
        for (int i = 0; i < columnCount; i++) {
            sheet.autoSizeColumn(i);
        }
    }
}
