package com.reuben.pastcare_spring.dtos;

import java.time.LocalDate;
import java.util.List;

/**
 * Request DTO for exporting attendance data
 */
public record AttendanceExportRequest(
    LocalDate startDate,
    LocalDate endDate,
    Long fellowshipId,
    String serviceType,
    String format, // EXCEL, PDF, CSV
    List<String> fields, // Optional: specific fields to export
    Boolean includeAbsent, // Include absent members
    Boolean includeLateArrivals,
    String sortBy, // name, date, status
    String sortOrder // asc, desc
) {
    public AttendanceExportRequest {
        if (format == null) {
            format = "EXCEL";
        }
        if (includeAbsent == null) {
            includeAbsent = true;
        }
        if (includeLateArrivals == null) {
            includeLateArrivals = true;
        }
        if (sortBy == null) {
            sortBy = "date";
        }
        if (sortOrder == null) {
            sortOrder = "desc";
        }
    }
}
