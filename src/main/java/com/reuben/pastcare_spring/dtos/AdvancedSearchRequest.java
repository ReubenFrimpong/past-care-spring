package com.reuben.pastcare_spring.dtos;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.List;

/**
 * Request DTO for advanced member search with dynamic filter criteria.
 * Supports complex queries with multiple filters and logical operators (AND/OR).
 */
public record AdvancedSearchRequest(
    @NotNull(message = "Filter groups are required")
    @Valid
    List<FilterGroup> filterGroups,

    /**
     * Logical operator between filter groups (AND/OR).
     * Default: AND
     */
    LogicalOperator groupOperator
) {

    /**
     * Group of filters with a logical operator.
     * Allows nesting filters with AND/OR logic.
     */
    public record FilterGroup(
        @NotNull(message = "Filters are required")
        @Valid
        List<FilterCriteria> filters,

        /**
         * Logical operator between filters in this group (AND/OR).
         * Default: AND
         */
        LogicalOperator operator
    ) {}

    /**
     * Single filter criterion for a specific field.
     */
    public record FilterCriteria(
        @NotNull(message = "Field name is required")
        String field,

        @NotNull(message = "Operator is required")
        FilterOperator operator,

        /**
         * Value(s) to filter by.
         * Can be single value, array of values, or range (min/max).
         * Type depends on the field and operator.
         */
        Object value,

        /**
         * For range operators (BETWEEN), this is the maximum value.
         */
        Object maxValue
    ) {}

    /**
     * Supported filter operators for different field types.
     */
    public enum FilterOperator {
        // Text operators
        EQUALS,           // Exact match (case-insensitive)
        NOT_EQUALS,       // Not equal (case-insensitive)
        CONTAINS,         // Contains substring (case-insensitive)
        STARTS_WITH,      // Starts with prefix (case-insensitive)
        ENDS_WITH,        // Ends with suffix (case-insensitive)

        // Numeric/Date operators
        GREATER_THAN,     // > for numbers, dates
        LESS_THAN,        // < for numbers, dates
        GREATER_OR_EQUAL, // >= for numbers, dates
        LESS_OR_EQUAL,    // <= for numbers, dates
        BETWEEN,          // Between min and max (inclusive)

        // Collection operators
        IN,               // Value in list
        NOT_IN,           // Value not in list

        // Boolean/Null operators
        IS_NULL,          // Field is null
        IS_NOT_NULL       // Field is not null
    }

    /**
     * Logical operators for combining filters.
     */
    public enum LogicalOperator {
        AND,
        OR
    }

    /**
     * Supported searchable fields with their data types.
     */
    public enum SearchField {
        // Personal info
        FIRST_NAME("firstName", FieldType.TEXT),
        LAST_NAME("lastName", FieldType.TEXT),
        PHONE_NUMBER("phoneNumber", FieldType.TEXT),
        EMAIL("email", FieldType.TEXT),

        // Demographics
        SEX("sex", FieldType.TEXT),
        MARITAL_STATUS("maritalStatus", FieldType.TEXT),
        DATE_OF_BIRTH("dateOfBirth", FieldType.DATE),
        AGE("age", FieldType.NUMBER),

        // Church-related
        STATUS("status", FieldType.TEXT),
        MEMBER_SINCE("memberSince", FieldType.DATE),
        IS_VERIFIED("isVerified", FieldType.BOOLEAN),
        PROFILE_COMPLETENESS("profileCompleteness", FieldType.NUMBER),

        // Collections
        TAGS("tags", FieldType.COLLECTION),
        FELLOWSHIPS("fellowships", FieldType.COLLECTION),

        // Location
        CITY("location.city", FieldType.TEXT),
        SUBURB("location.suburb", FieldType.TEXT),
        REGION("location.region", FieldType.TEXT),
        COUNTRY("location.country", FieldType.TEXT);

        private final String fieldPath;
        private final FieldType type;

        SearchField(String fieldPath, FieldType type) {
            this.fieldPath = fieldPath;
            this.type = type;
        }

        public String getFieldPath() {
            return fieldPath;
        }

        public FieldType getType() {
            return type;
        }

        public enum FieldType {
            TEXT, NUMBER, DATE, BOOLEAN, COLLECTION
        }
    }
}
