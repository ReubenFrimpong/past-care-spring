package com.reuben.pastcare_spring.exceptions;

import com.reuben.pastcare_spring.enums.Permission;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Exception thrown when a user attempts to access a resource or perform an action
 * without having the required permission(s).
 *
 * This exception is thrown by the PermissionCheckAspect when a method annotated
 * with @RequirePermission is accessed by a user who lacks the necessary permissions.
 */
public class InsufficientPermissionException extends RuntimeException {

    private final Permission[] requiredPermissions;
    private final String userRole;

    public InsufficientPermissionException(Permission[] requiredPermissions, String userRole) {
        super(buildMessage(requiredPermissions, userRole));
        this.requiredPermissions = requiredPermissions;
        this.userRole = userRole;
    }

    public InsufficientPermissionException(Permission[] requiredPermissions, String userRole, String customMessage) {
        super(customMessage);
        this.requiredPermissions = requiredPermissions;
        this.userRole = userRole;
    }

    private static String buildMessage(Permission[] requiredPermissions, String userRole) {
        String permissionsStr = Arrays.stream(requiredPermissions)
                .map(Permission::name)
                .collect(Collectors.joining(", "));

        return String.format(
                "Access denied. User with role '%s' lacks required permission(s): [%s]",
                userRole,
                permissionsStr
        );
    }

    public Permission[] getRequiredPermissions() {
        return requiredPermissions;
    }

    public String getUserRole() {
        return userRole;
    }
}
