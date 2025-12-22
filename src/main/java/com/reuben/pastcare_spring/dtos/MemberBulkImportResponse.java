package com.reuben.pastcare_spring.dtos;

import java.util.List;
import java.util.Map;

/**
 * Response DTO for bulk import operations.
 * Contains summary of import results including successes, failures, and errors.
 */
public record MemberBulkImportResponse(
    int totalRows,
    int successCount,
    int failureCount,
    int duplicateCount,
    int updatedCount,
    List<ImportError> errors,
    List<MemberResponse> importedMembers
) {

    /**
     * Represents an error that occurred during import for a specific row.
     */
    public record ImportError(
        int rowNumber,
        String fieldName,
        String errorMessage,
        Map<String, String> rowData
    ) {}
}
