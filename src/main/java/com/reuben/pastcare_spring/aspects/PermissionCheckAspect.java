package com.reuben.pastcare_spring.aspects;

import com.reuben.pastcare_spring.annotations.RequirePermission;
import com.reuben.pastcare_spring.enums.Permission;
import com.reuben.pastcare_spring.enums.Role;
import com.reuben.pastcare_spring.exceptions.InsufficientPermissionException;
import com.reuben.pastcare_spring.security.TenantContext;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * Aspect that intercepts methods annotated with @RequirePermission
 * and enforces permission-based access control.
 *
 * This aspect runs before the annotated method executes and checks if the
 * current user has the required permission(s). If not, it throws an
 * InsufficientPermissionException.
 *
 * SUPERADMIN role automatically bypasses all permission checks.
 */
@Aspect
@Component
public class PermissionCheckAspect {

    private static final Logger logger = LoggerFactory.getLogger(PermissionCheckAspect.class);

    /**
     * Intercepts all methods annotated with @RequirePermission.
     * Runs before the method execution to validate permissions.
     *
     * @param joinPoint the join point representing the intercepted method
     * @throws InsufficientPermissionException if user lacks required permissions
     */
    @Before("@annotation(com.reuben.pastcare_spring.annotations.RequirePermission)")
    public void checkPermission(JoinPoint joinPoint) throws InsufficientPermissionException {
        // Extract method signature
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();

        // Get the @RequirePermission annotation
        RequirePermission annotation = method.getAnnotation(RequirePermission.class);
        if (annotation == null) {
            // If annotation is not on method, check class level
            annotation = joinPoint.getTarget().getClass().getAnnotation(RequirePermission.class);
        }

        if (annotation == null) {
            logger.warn("@RequirePermission annotation not found on method or class: {}",
                    method.getName());
            return;
        }

        // Get current user's role from TenantContext
        String roleStr = TenantContext.getCurrentUserRole();
        if (roleStr == null) {
            logger.error("No user role found in TenantContext for permission check. Method: {}",
                    method.getName());
            throw new InsufficientPermissionException(
                    annotation.value(),
                    "UNKNOWN",
                    "Authentication required"
            );
        }

        // SUPERADMIN bypasses all permission checks
        if ("SUPERADMIN".equals(roleStr)) {
            logger.debug("SUPERADMIN access granted for method: {}", method.getName());
            return;
        }

        // Parse role enum
        Role role;
        try {
            role = Role.valueOf(roleStr);
        } catch (IllegalArgumentException e) {
            logger.error("Invalid role in TenantContext: {}. Method: {}", roleStr, method.getName());
            throw new InsufficientPermissionException(
                    annotation.value(),
                    roleStr,
                    "Invalid user role"
            );
        }

        // Get required permissions from annotation
        Permission[] requiredPermissions = annotation.value();
        RequirePermission.LogicalOperation operation = annotation.operation();

        // Check permissions based on operation (AND or OR)
        boolean hasAccess = false;

        if (operation == RequirePermission.LogicalOperation.AND) {
            // User must have ALL permissions
            hasAccess = role.hasAllPermissions(requiredPermissions);
            logger.debug("Permission check (AND) for role {}: required={}, hasAccess={}",
                    role, Arrays.toString(requiredPermissions), hasAccess);
        } else {
            // User must have ANY permission (OR logic)
            hasAccess = role.hasAnyPermission(requiredPermissions);
            logger.debug("Permission check (OR) for role {}: required={}, hasAccess={}",
                    role, Arrays.toString(requiredPermissions), hasAccess);
        }

        // Throw exception if access denied
        if (!hasAccess) {
            Long userId = TenantContext.getCurrentUserId();
            Long churchId = TenantContext.getCurrentChurchId();

            logger.warn("PERMISSION DENIED: userId={}, churchId={}, role={}, method={}, " +
                            "requiredPermissions={}, operation={}",
                    userId, churchId, role, method.getName(),
                    Arrays.toString(requiredPermissions), operation);

            // Use custom message if provided
            if (annotation.message() != null && !annotation.message().isEmpty()) {
                throw new InsufficientPermissionException(
                        requiredPermissions,
                        role.name(),
                        annotation.message()
                );
            } else {
                throw new InsufficientPermissionException(requiredPermissions, role.name());
            }
        }

        logger.debug("Permission check PASSED for role {} on method: {}", role, method.getName());
    }
}
