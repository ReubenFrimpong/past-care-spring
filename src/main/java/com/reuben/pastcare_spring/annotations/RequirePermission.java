package com.reuben.pastcare_spring.annotations;

import com.reuben.pastcare_spring.enums.Permission;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to enforce permission-based access control on controller methods.
 *
 * Usage:
 * - Single permission: @RequirePermission(Permission.MEMBER_CREATE)
 * - Multiple permissions with OR logic: @RequirePermission({Permission.MEMBER_DELETE, Permission.ADMIN})
 * - Multiple permissions with AND logic: @RequirePermission(value = {Permission.MEMBER_VIEW_ALL, Permission.MEMBER_EDIT_ALL}, operation = LogicalOperation.AND)
 *
 * When operation is OR (default), the user needs ANY of the specified permissions.
 * When operation is AND, the user needs ALL of the specified permissions.
 *
 * SUPERADMIN role automatically bypasses all permission checks.
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequirePermission {

    /**
     * The permission(s) required to access this method.
     * @return array of required permissions
     */
    Permission[] value();

    /**
     * Logical operation to apply when multiple permissions are specified.
     * Default is OR (user needs ANY of the permissions).
     * @return logical operation (AND or OR)
     */
    LogicalOperation operation() default LogicalOperation.OR;

    /**
     * Optional custom error message to return when access is denied.
     * @return custom error message
     */
    String message() default "";

    /**
     * Logical operation enum for combining multiple permissions.
     */
    enum LogicalOperation {
        /**
         * User must have ALL specified permissions
         */
        AND,

        /**
         * User must have AT LEAST ONE of the specified permissions (default)
         */
        OR
    }
}
