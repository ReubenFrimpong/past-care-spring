package com.reuben.pastcare_spring.specifications;

import com.reuben.pastcare_spring.dtos.AdvancedSearchRequest;
import com.reuben.pastcare_spring.dtos.AdvancedSearchRequest.FilterCriteria;
import com.reuben.pastcare_spring.dtos.AdvancedSearchRequest.FilterGroup;
import com.reuben.pastcare_spring.dtos.AdvancedSearchRequest.FilterOperator;
import com.reuben.pastcare_spring.dtos.AdvancedSearchRequest.LogicalOperator;
import com.reuben.pastcare_spring.models.Church;
import com.reuben.pastcare_spring.models.Fellowship;
import com.reuben.pastcare_spring.models.Member;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * JPA Specification builder for dynamic member queries.
 * Supports complex filters with AND/OR logic, nested groups, and various operators.
 */
public class MemberSpecification {

    /**
     * Build a specification from an advanced search request.
     *
     * @param request The search request with filter criteria
     * @param church  The church to filter by (ensures data isolation)
     * @return Specification for querying members
     */
    public static Specification<Member> fromAdvancedSearch(AdvancedSearchRequest request, Church church) {
        return (root, query, criteriaBuilder) -> {
            // Always filter by church for security
            Predicate churchPredicate = criteriaBuilder.equal(root.get("church"), church);

            if (request.filterGroups() == null || request.filterGroups().isEmpty()) {
                return churchPredicate;
            }

            // Build predicates for each filter group
            List<Predicate> groupPredicates = new ArrayList<>();
            for (FilterGroup group : request.filterGroups()) {
                Predicate groupPredicate = buildGroupPredicate(group, root, query, criteriaBuilder);
                if (groupPredicate != null) {
                    groupPredicates.add(groupPredicate);
                }
            }

            // Combine group predicates with the specified operator
            Predicate filterPredicate;
            if (groupPredicates.isEmpty()) {
                filterPredicate = criteriaBuilder.conjunction();
            } else if (request.groupOperator() == LogicalOperator.OR) {
                filterPredicate = criteriaBuilder.or(groupPredicates.toArray(new Predicate[0]));
            } else {
                filterPredicate = criteriaBuilder.and(groupPredicates.toArray(new Predicate[0]));
            }

            // Combine church filter with search filters
            return criteriaBuilder.and(churchPredicate, filterPredicate);
        };
    }

    /**
     * Build a predicate for a filter group.
     */
    private static Predicate buildGroupPredicate(
            FilterGroup group,
            Root<Member> root,
            CriteriaQuery<?> query,
            CriteriaBuilder criteriaBuilder) {

        if (group.filters() == null || group.filters().isEmpty()) {
            return null;
        }

        List<Predicate> predicates = new ArrayList<>();
        for (FilterCriteria criteria : group.filters()) {
            Predicate predicate = buildCriteriaPredicate(criteria, root, query, criteriaBuilder);
            if (predicate != null) {
                predicates.add(predicate);
            }
        }

        if (predicates.isEmpty()) {
            return null;
        }

        // Combine predicates with the specified operator
        if (group.operator() == LogicalOperator.OR) {
            return criteriaBuilder.or(predicates.toArray(new Predicate[0]));
        } else {
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        }
    }

    /**
     * Build a predicate for a single filter criterion.
     */
    private static Predicate buildCriteriaPredicate(
            FilterCriteria criteria,
            Root<Member> root,
            CriteriaQuery<?> query,
            CriteriaBuilder criteriaBuilder) {

        String field = criteria.field();
        FilterOperator operator = criteria.operator();
        Object value = criteria.value();

        // Handle nested fields (e.g., "location.city")
        Path<?> fieldPath = getFieldPath(root, field);

        return switch (operator) {
            case EQUALS -> buildEqualsPredicate(fieldPath, value, criteriaBuilder);
            case NOT_EQUALS -> buildNotEqualsPredicate(fieldPath, value, criteriaBuilder);
            case CONTAINS -> buildContainsPredicate(fieldPath, value, criteriaBuilder);
            case STARTS_WITH -> buildStartsWithPredicate(fieldPath, value, criteriaBuilder);
            case ENDS_WITH -> buildEndsWithPredicate(fieldPath, value, criteriaBuilder);
            case GREATER_THAN -> buildGreaterThanPredicate(fieldPath, value, criteriaBuilder);
            case LESS_THAN -> buildLessThanPredicate(fieldPath, value, criteriaBuilder);
            case GREATER_OR_EQUAL -> buildGreaterOrEqualPredicate(fieldPath, value, criteriaBuilder);
            case LESS_OR_EQUAL -> buildLessOrEqualPredicate(fieldPath, value, criteriaBuilder);
            case BETWEEN -> buildBetweenPredicate(fieldPath, value, criteria.maxValue(), criteriaBuilder);
            case IN -> buildInPredicate(field, value, root, query, criteriaBuilder);
            case NOT_IN -> buildNotInPredicate(field, value, root, query, criteriaBuilder);
            case IS_NULL -> criteriaBuilder.isNull(fieldPath);
            case IS_NOT_NULL -> criteriaBuilder.isNotNull(fieldPath);
        };
    }

    /**
     * Get field path, handling nested fields (e.g., "location.city").
     */
    private static Path<?> getFieldPath(Root<Member> root, String field) {
        String[] parts = field.split("\\.");
        Path<?> path = root;
        for (String part : parts) {
            path = path.get(part);
        }
        return path;
    }

    // ========== Predicate Builders ==========

    private static Predicate buildEqualsPredicate(Path<?> path, Object value, CriteriaBuilder cb) {
        if (path.getJavaType() == String.class) {
            return cb.equal(cb.lower(path.as(String.class)), value.toString().toLowerCase());
        }
        return cb.equal(path, convertValue(value, path.getJavaType()));
    }

    private static Predicate buildNotEqualsPredicate(Path<?> path, Object value, CriteriaBuilder cb) {
        if (path.getJavaType() == String.class) {
            return cb.notEqual(cb.lower(path.as(String.class)), value.toString().toLowerCase());
        }
        return cb.notEqual(path, convertValue(value, path.getJavaType()));
    }

    private static Predicate buildContainsPredicate(Path<?> path, Object value, CriteriaBuilder cb) {
        return cb.like(cb.lower(path.as(String.class)), "%" + value.toString().toLowerCase() + "%");
    }

    private static Predicate buildStartsWithPredicate(Path<?> path, Object value, CriteriaBuilder cb) {
        return cb.like(cb.lower(path.as(String.class)), value.toString().toLowerCase() + "%");
    }

    private static Predicate buildEndsWithPredicate(Path<?> path, Object value, CriteriaBuilder cb) {
        return cb.like(cb.lower(path.as(String.class)), "%" + value.toString().toLowerCase());
    }

    @SuppressWarnings("unchecked")
    private static Predicate buildGreaterThanPredicate(Path<?> path, Object value, CriteriaBuilder cb) {
        if (path.getJavaType() == LocalDate.class) {
            return cb.greaterThan((Expression<LocalDate>) path, parseDate(value));
        } else if (path.getJavaType() == YearMonth.class) {
            return cb.greaterThan((Expression<YearMonth>) path, parseYearMonth(value));
        } else if (Number.class.isAssignableFrom(path.getJavaType())) {
            return cb.gt((Expression<Number>) path, ((Number) value).doubleValue());
        }
        return cb.conjunction();
    }

    @SuppressWarnings("unchecked")
    private static Predicate buildLessThanPredicate(Path<?> path, Object value, CriteriaBuilder cb) {
        if (path.getJavaType() == LocalDate.class) {
            return cb.lessThan((Expression<LocalDate>) path, parseDate(value));
        } else if (path.getJavaType() == YearMonth.class) {
            return cb.lessThan((Expression<YearMonth>) path, parseYearMonth(value));
        } else if (Number.class.isAssignableFrom(path.getJavaType())) {
            return cb.lt((Expression<Number>) path, ((Number) value).doubleValue());
        }
        return cb.conjunction();
    }

    @SuppressWarnings("unchecked")
    private static Predicate buildGreaterOrEqualPredicate(Path<?> path, Object value, CriteriaBuilder cb) {
        if (path.getJavaType() == LocalDate.class) {
            return cb.greaterThanOrEqualTo((Expression<LocalDate>) path, parseDate(value));
        } else if (path.getJavaType() == YearMonth.class) {
            return cb.greaterThanOrEqualTo((Expression<YearMonth>) path, parseYearMonth(value));
        } else if (Number.class.isAssignableFrom(path.getJavaType())) {
            return cb.ge((Expression<Number>) path, ((Number) value).doubleValue());
        }
        return cb.conjunction();
    }

    @SuppressWarnings("unchecked")
    private static Predicate buildLessOrEqualPredicate(Path<?> path, Object value, CriteriaBuilder cb) {
        if (path.getJavaType() == LocalDate.class) {
            return cb.lessThanOrEqualTo((Expression<LocalDate>) path, parseDate(value));
        } else if (path.getJavaType() == YearMonth.class) {
            return cb.lessThanOrEqualTo((Expression<YearMonth>) path, parseYearMonth(value));
        } else if (Number.class.isAssignableFrom(path.getJavaType())) {
            return cb.le((Expression<Number>) path, ((Number) value).doubleValue());
        }
        return cb.conjunction();
    }

    @SuppressWarnings("unchecked")
    private static Predicate buildBetweenPredicate(Path<?> path, Object minValue, Object maxValue, CriteriaBuilder cb) {
        if (path.getJavaType() == LocalDate.class) {
            return cb.between((Expression<LocalDate>) path, parseDate(minValue), parseDate(maxValue));
        } else if (path.getJavaType() == YearMonth.class) {
            return cb.between((Expression<YearMonth>) path, parseYearMonth(minValue), parseYearMonth(maxValue));
        } else if (Number.class.isAssignableFrom(path.getJavaType())) {
            return cb.between((Expression<Double>) path,
                    ((Number) minValue).doubleValue(),
                    ((Number) maxValue).doubleValue());
        }
        return cb.conjunction();
    }

    private static Predicate buildInPredicate(
            String field,
            Object value,
            Root<Member> root,
            CriteriaQuery<?> query,
            CriteriaBuilder cb) {

        // Handle collection fields (tags, fellowships)
        if ("tags".equals(field)) {
            return buildTagsInPredicate(value, root, cb);
        } else if ("fellowships".equals(field)) {
            return buildFellowshipsInPredicate(value, root, query, cb);
        }

        // Handle regular fields
        Path<?> path = getFieldPath(root, field);
        Collection<?> values = (Collection<?>) value;
        return path.in(values);
    }

    private static Predicate buildNotInPredicate(
            String field,
            Object value,
            Root<Member> root,
            CriteriaQuery<?> query,
            CriteriaBuilder cb) {

        // Handle collection fields
        if ("tags".equals(field)) {
            return buildTagsNotInPredicate(value, root, cb);
        } else if ("fellowships".equals(field)) {
            return buildFellowshipsNotInPredicate(value, root, query, cb);
        }

        // Handle regular fields
        Path<?> path = getFieldPath(root, field);
        Collection<?> values = (Collection<?>) value;
        return cb.not(path.in(values));
    }

    /**
     * Build predicate for members with ANY of the specified tags.
     */
    private static Predicate buildTagsInPredicate(Object value, Root<Member> root, CriteriaBuilder cb) {
        Collection<?> tags = (Collection<?>) value;
        List<Predicate> tagPredicates = new ArrayList<>();

        for (Object tag : tags) {
            tagPredicates.add(cb.isMember(tag.toString(), root.get("tags")));
        }

        return cb.or(tagPredicates.toArray(new Predicate[0]));
    }

    /**
     * Build predicate for members WITHOUT any of the specified tags.
     */
    private static Predicate buildTagsNotInPredicate(Object value, Root<Member> root, CriteriaBuilder cb) {
        Collection<?> tags = (Collection<?>) value;
        List<Predicate> tagPredicates = new ArrayList<>();

        for (Object tag : tags) {
            tagPredicates.add(cb.isNotMember(tag.toString(), root.get("tags")));
        }

        return cb.and(tagPredicates.toArray(new Predicate[0]));
    }

    /**
     * Build predicate for members in ANY of the specified fellowships.
     */
    private static Predicate buildFellowshipsInPredicate(
            Object value,
            Root<Member> root,
            CriteriaQuery<?> query,
            CriteriaBuilder cb) {

        Collection<?> fellowshipIds = (Collection<?>) value;

        // Join with fellowships table
        Join<Member, Fellowship> fellowshipJoin = root.join("fellowships", JoinType.LEFT);

        // Convert to Long IDs
        List<Long> ids = fellowshipIds.stream()
                .map(id -> id instanceof Number ? ((Number) id).longValue() : Long.parseLong(id.toString()))
                .toList();

        return fellowshipJoin.get("id").in(ids);
    }

    /**
     * Build predicate for members NOT in any of the specified fellowships.
     */
    private static Predicate buildFellowshipsNotInPredicate(
            Object value,
            Root<Member> root,
            CriteriaQuery<?> query,
            CriteriaBuilder cb) {

        Collection<?> fellowshipIds = (Collection<?>) value;

        // Subquery to find members in the specified fellowships
        Subquery<Long> subquery = query.subquery(Long.class);
        Root<Member> subRoot = subquery.from(Member.class);
        Join<Member, Fellowship> subFellowshipJoin = subRoot.join("fellowships");

        List<Long> ids = fellowshipIds.stream()
                .map(id -> id instanceof Number ? ((Number) id).longValue() : Long.parseLong(id.toString()))
                .toList();

        subquery.select(subRoot.get("id"))
                .where(subFellowshipJoin.get("id").in(ids));

        return cb.not(root.get("id").in(subquery));
    }

    // ========== Value Converters ==========

    private static Object convertValue(Object value, Class<?> targetType) {
        if (value == null) {
            return null;
        }

        if (targetType.isInstance(value)) {
            return value;
        }

        if (targetType == LocalDate.class) {
            return parseDate(value);
        }

        if (targetType == YearMonth.class) {
            return parseYearMonth(value);
        }

        if (targetType == Boolean.class || targetType == boolean.class) {
            return Boolean.parseBoolean(value.toString());
        }

        if (targetType == Integer.class || targetType == int.class) {
            return Integer.parseInt(value.toString());
        }

        if (targetType == Long.class || targetType == long.class) {
            return Long.parseLong(value.toString());
        }

        if (targetType == Double.class || targetType == double.class) {
            return Double.parseDouble(value.toString());
        }

        return value;
    }

    private static LocalDate parseDate(Object value) {
        if (value instanceof LocalDate) {
            return (LocalDate) value;
        }
        return LocalDate.parse(value.toString(), DateTimeFormatter.ISO_LOCAL_DATE);
    }

    private static YearMonth parseYearMonth(Object value) {
        if (value instanceof YearMonth) {
            return (YearMonth) value;
        }
        return YearMonth.parse(value.toString());
    }
}
