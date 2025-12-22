package com.reuben.pastcare_spring.dtos;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.Map;

/**
 * Request DTO for bulk importing members from CSV/Excel files.
 * Supports column mapping to handle different file formats.
 */
public record MemberBulkImportRequest(

    @NotNull(message = "Member data is required")
    @Size(min = 1, max = 1000, message = "Can import between 1 and 1000 members at once")
    List<Map<String, String>> members,

    /**
     * Column mapping from file columns to expected fields.
     * Example: {"First Name": "firstName", "Last Name": "lastName"}
     */
    Map<String, String> columnMapping,

    /**
     * Whether to skip rows with validation errors or fail entire import.
     * If true, skips invalid rows and continues. If false, fails on first error.
     */
    boolean skipInvalidRows,

    /**
     * Whether to update existing members (matched by phone number).
     * If true, updates existing members. If false, skips duplicates.
     */
    boolean updateExisting

) {
    /**
     * Constructor with defaults
     */
    public MemberBulkImportRequest {
        if (columnMapping == null) {
            columnMapping = Map.of();
        }
    }
}
