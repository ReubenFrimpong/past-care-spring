package com.reuben.pastcare_spring.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for generating CSV reports.
 */
@Service
@RequiredArgsConstructor
public class CsvReportService {

    /**
     * Generate a CSV report from headers and data rows.
     *
     * @param headers List of column headers
     * @param rows List of data rows (each row is a list of values)
     * @return CSV file as byte array
     */
    public byte[] generateCsvReport(List<String> headers, List<List<Object>> rows) throws IOException {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream();
             OutputStreamWriter osw = new OutputStreamWriter(out, StandardCharsets.UTF_8);
             PrintWriter writer = new PrintWriter(osw)) {

            // Write headers
            String headerLine = headers.stream()
                    .map(this::escapeCsvValue)
                    .collect(Collectors.joining(","));
            writer.println(headerLine);

            // Write data rows
            for (List<Object> row : rows) {
                String rowLine = row.stream()
                        .map(value -> value != null ? escapeCsvValue(value.toString()) : "")
                        .collect(Collectors.joining(","));
                writer.println(rowLine);
            }

            writer.flush();
            return out.toByteArray();
        }
    }

    /**
     * Escape CSV values containing special characters.
     */
    private String escapeCsvValue(String value) {
        if (value == null) {
            return "";
        }

        // If value contains comma, quote, or newline, wrap in quotes and escape internal quotes
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }

        return value;
    }
}
