package com.reuben.pastcare_spring.validators;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import com.reuben.pastcare_spring.annotations.Unique;

import java.lang.reflect.Field;

import org.hibernate.validator.constraintvalidation.HibernateConstraintValidatorContext;
import org.springframework.stereotype.Component;

@Component
public class UniqueValidator implements ConstraintValidator<Unique, Object> {

    @PersistenceContext
    private EntityManager entityManager;

    private String table;
    private String column;

    @Override
    public void initialize(Unique constraintAnnotation) {
        this.table = constraintAnnotation.table();
        this.column = constraintAnnotation.column();
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        if (value == null) {
            return true; // Null values should be handled by @NotNull
        }

        // Get the entity being validated (root object)
        Object entity = context.unwrap(HibernateConstraintValidatorContext.class)
                .getConstraintValidatorPayload(Object.class);

        // Build the base query
        String queryStr = String.format("SELECT COUNT(e) FROM %s e WHERE e.%s = :value", table, column);
        Query query = entityManager.createQuery(queryStr)
                .setParameter("value", value);

        // If this is an update operation (entity has ID), exclude current record
        if (entity != null) {
            try {
                Field idField = entity.getClass().getDeclaredField("id");
                idField.setAccessible(true);
                Object idValue = idField.get(entity);

                if (idValue != null) {
                    queryStr += " AND e.id != :id";
                    query = entityManager.createQuery(queryStr)
                            .setParameter("value", value)
                            .setParameter("id", idValue);
                }
            } catch (NoSuchFieldException | IllegalAccessException e) {
                // If ID field not found or inaccessible, proceed without exclusion
            }
        }

        Long count = (Long) query.getSingleResult();
        return count == 0;
    }
}