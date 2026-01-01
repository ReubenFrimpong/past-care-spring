package com.reuben.pastcare_spring.advice;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.dao.DataIntegrityViolationException;

import com.reuben.pastcare_spring.dtos.ErrorResponse;
import com.reuben.pastcare_spring.exceptions.AccountLockedException;
import com.reuben.pastcare_spring.exceptions.AddonAlreadyPurchasedException;
import com.reuben.pastcare_spring.exceptions.DuplicateChurchException;
import com.reuben.pastcare_spring.exceptions.DuplicateResourceException;
import com.reuben.pastcare_spring.exceptions.DuplicateUserException;
import com.reuben.pastcare_spring.exceptions.FileUploadException;
import com.reuben.pastcare_spring.exceptions.InsufficientPermissionException;
import com.reuben.pastcare_spring.exceptions.InvalidCredentialsException;
import com.reuben.pastcare_spring.exceptions.ResourceNotFoundException;
import com.reuben.pastcare_spring.exceptions.StorageLimitExceededException;
import com.reuben.pastcare_spring.exceptions.SubscriptionRequiredForAddonException;
import com.reuben.pastcare_spring.exceptions.TenantViolationException;
import com.reuben.pastcare_spring.exceptions.TierLimitExceededException;
import com.reuben.pastcare_spring.exceptions.TooManyRequestsException;
import com.reuben.pastcare_spring.exceptions.UnauthorizedException;
import com.reuben.pastcare_spring.services.SecurityMonitoringService;

import java.time.format.DateTimeFormatter;

@ControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

  private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);
  private final SecurityMonitoringService securityMonitoringService;

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(
      MethodArgumentNotValidException exp,
      WebRequest request) {
    Map<String, List<String>> validationErrors = new HashMap<>();

    exp.getBindingResult().getAllErrors().forEach(error -> {
      String fieldName = ((FieldError) error).getField();
      String errorMessage = error.getDefaultMessage();

      validationErrors.computeIfAbsent(fieldName, k -> new ArrayList<>()).add(errorMessage);
    });

    logger.warn("Validation failed for request {}: {}", request.getDescription(false), validationErrors);

    ErrorResponse errorResponse = new ErrorResponse(
      HttpStatus.BAD_REQUEST.value(),
      "Validation Failed",
      "Invalid input data. Please check the fields and try again.",
      request.getDescription(false).replace("uri=", ""),
      validationErrors
    );

    return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(InvalidCredentialsException.class)
  public ResponseEntity<ErrorResponse> handleInvalidCredentialsException(
      InvalidCredentialsException exp,
      WebRequest request) {
    logger.warn("Invalid credentials for request {}: {}", request.getDescription(false), exp.getMessage());

    ErrorResponse errorResponse = new ErrorResponse(
      HttpStatus.UNAUTHORIZED.value(),
      "Authentication Failed",
      exp.getMessage(),
      request.getDescription(false).replace("uri=", "")
    );

    return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
  }

  @ExceptionHandler({BadCredentialsException.class, AuthenticationException.class})
  public ResponseEntity<ErrorResponse> handleAuthenticationException(
      Exception exp,
      WebRequest request) {
    logger.warn("Authentication failed for request {}: {} - {}",
      request.getDescription(false), exp.getClass().getSimpleName(), exp.getMessage());

    String errorMessage = "Invalid email or password. Please check your credentials and try again.";

    // Provide more specific messages based on exception type
    if (exp instanceof BadCredentialsException) {
      errorMessage = "Incorrect email or password. Please verify your credentials.";
    } else if (exp.getMessage() != null && exp.getMessage().contains("disabled")) {
      errorMessage = "Your account has been disabled. Please contact support.";
    } else if (exp.getMessage() != null && exp.getMessage().contains("locked")) {
      errorMessage = "Your account has been locked. Please contact support.";
    }

    ErrorResponse errorResponse = new ErrorResponse(
      HttpStatus.UNAUTHORIZED.value(),
      "Authentication Failed",
      errorMessage,
      request.getDescription(false).replace("uri=", "")
    );

    return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
  }

  @ExceptionHandler(UnauthorizedException.class)
  public ResponseEntity<ErrorResponse> handleUnauthorizedException(
      UnauthorizedException exp,
      WebRequest request) {
    logger.warn("Unauthorized access attempt for request {}: {}", request.getDescription(false), exp.getMessage());

    ErrorResponse errorResponse = new ErrorResponse(
      HttpStatus.FORBIDDEN.value(),
      "Access Denied",
      exp.getMessage(),
      request.getDescription(false).replace("uri=", "")
    );

    return new ResponseEntity<>(errorResponse, HttpStatus.FORBIDDEN);
  }

  @ExceptionHandler(InsufficientPermissionException.class)
  public ResponseEntity<ErrorResponse> handleInsufficientPermissionException(
      InsufficientPermissionException exp,
      WebRequest request) {
    logger.warn("Insufficient permissions for request {}: {} - Role: {}, Required: {}",
        request.getDescription(false),
        exp.getMessage(),
        exp.getUserRole(),
        java.util.Arrays.toString(exp.getRequiredPermissions()));

    ErrorResponse errorResponse = new ErrorResponse(
      HttpStatus.FORBIDDEN.value(),
      "Insufficient Permissions",
      exp.getMessage(),
      request.getDescription(false).replace("uri=", "")
    );

    return new ResponseEntity<>(errorResponse, HttpStatus.FORBIDDEN);
  }

  @ExceptionHandler(TenantViolationException.class)
  public ResponseEntity<ErrorResponse> handleTenantViolationException(
      TenantViolationException exp,
      WebRequest request) {
    logger.error("SECURITY VIOLATION - Cross-tenant access attempt: {} - User: {}, Attempted Church: {}, Actual Church: {}, Resource: {}",
        exp.getMessage(),
        exp.getUserId(),
        exp.getAttemptedChurchId(),
        exp.getActualChurchId(),
        exp.getResourceType());

    // Log to security monitoring service for audit trail and alerting
    securityMonitoringService.logTenantViolation(exp);

    ErrorResponse errorResponse = new ErrorResponse(
      HttpStatus.FORBIDDEN.value(),
      "Access Denied",
      "You do not have permission to access this resource.",
      request.getDescription(false).replace("uri=", "")
    );

    return new ResponseEntity<>(errorResponse, HttpStatus.FORBIDDEN);
  }

  @ExceptionHandler(ResourceNotFoundException.class)
  public ResponseEntity<ErrorResponse> handleResourceNotFoundException(
      ResourceNotFoundException exp,
      WebRequest request) {
    logger.warn("Resource not found for request {}: {}", request.getDescription(false), exp.getMessage());

    ErrorResponse errorResponse = new ErrorResponse(
      HttpStatus.NOT_FOUND.value(),
      "Resource Not Found",
      exp.getMessage(),
      request.getDescription(false).replace("uri=", "")
    );

    return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
  }

  @ExceptionHandler(DuplicateResourceException.class)
  public ResponseEntity<ErrorResponse> handleDuplicateResourceException(
      DuplicateResourceException exp,
      WebRequest request) {
    logger.warn("Duplicate resource detected for request {}: {}", request.getDescription(false), exp.getMessage());

    ErrorResponse errorResponse = new ErrorResponse(
      HttpStatus.CONFLICT.value(),
      "Duplicate Resource",
      exp.getMessage(),
      request.getDescription(false).replace("uri=", "")
    );

    return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
  }

  @ExceptionHandler(DuplicateChurchException.class)
  public ResponseEntity<ErrorResponse> handleDuplicateChurchException(
      DuplicateChurchException exp,
      WebRequest request) {
    logger.warn("Duplicate church detected for request {}: {}", request.getDescription(false), exp.getMessage());

    ErrorResponse errorResponse = new ErrorResponse(
      HttpStatus.CONFLICT.value(),
      "Duplicate Church",
      exp.getMessage(),
      request.getDescription(false).replace("uri=", "")
    );

    return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
  }

  @ExceptionHandler(DuplicateUserException.class)
  public ResponseEntity<ErrorResponse> handleDuplicateUserException(
      DuplicateUserException exp,
      WebRequest request) {
    logger.warn("Duplicate user detected for request {}: {}", request.getDescription(false), exp.getMessage());

    ErrorResponse errorResponse = new ErrorResponse(
      HttpStatus.CONFLICT.value(),
      "Duplicate User",
      exp.getMessage(),
      request.getDescription(false).replace("uri=", "")
    );

    return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
  }

  @ExceptionHandler(AccountLockedException.class)
  public ResponseEntity<ErrorResponse> handleAccountLockedException(
      AccountLockedException exp,
      WebRequest request) {
    logger.warn("Account locked for request {}: {}", request.getDescription(false), exp.getMessage());

    String message = exp.getMessage();
    if (exp.getLockedUntil() != null) {
      DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
      String lockedUntilTime = exp.getLockedUntil().format(formatter);
      message += " Please try again after " + lockedUntilTime + ".";
    }

    ErrorResponse errorResponse = new ErrorResponse(
      HttpStatus.LOCKED.value(),
      "Account Locked",
      message,
      request.getDescription(false).replace("uri=", "")
    );

    return new ResponseEntity<>(errorResponse, HttpStatus.LOCKED);
  }

  @ExceptionHandler(TooManyRequestsException.class)
  public ResponseEntity<ErrorResponse> handleTooManyRequestsException(
      TooManyRequestsException exp,
      WebRequest request) {
    logger.warn("Too many requests for request {}: {}", request.getDescription(false), exp.getMessage());

    ErrorResponse errorResponse = new ErrorResponse(
      HttpStatus.TOO_MANY_REQUESTS.value(),
      "Too Many Requests",
      exp.getMessage(),
      request.getDescription(false).replace("uri=", "")
    );

    return new ResponseEntity<>(errorResponse, HttpStatus.TOO_MANY_REQUESTS);
  }

  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<ErrorResponse> handleIllegalArgumentException(
      IllegalArgumentException exp,
      WebRequest request) {
    logger.warn("Invalid argument for request {}: {}", request.getDescription(false), exp.getMessage());

    ErrorResponse errorResponse = new ErrorResponse(
      HttpStatus.BAD_REQUEST.value(),
      "Invalid Request",
      exp.getMessage(),
      request.getDescription(false).replace("uri=", "")
    );

    return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(FileUploadException.class)
  public ResponseEntity<ErrorResponse> handleFileUploadException(
      FileUploadException exp,
      WebRequest request
  ) {
    logger.warn(
        "File upload failed for request {}: {}",
        request.getDescription(false),
        exp.getMessage()
    );

    ErrorResponse errorResponse = new ErrorResponse(
        HttpStatus.BAD_REQUEST.value(),
        "Invalid Request",
        exp.getMessage(),
        request.getDescription(false).replace("uri=", "")
    );

    return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
  }

  /**
   * Handle storage limit exceeded exception (HTTP 413 Payload Too Large).
   *
   * <p>Triggered when:
   * <ul>
   *   <li>File upload would exceed church's storage limit</li>
   *   <li>Member creation exceeds storage quota</li>
   * </ul>
   *
   * <p>Response includes:
   * <ul>
   *   <li>Current usage in MB</li>
   *   <li>Storage limit in MB</li>
   *   <li>File size that was attempted</li>
   *   <li>Percentage used</li>
   *   <li>Suggested action (purchase addon or delete files)</li>
   * </ul>
   */
  @ExceptionHandler(StorageLimitExceededException.class)
  public ResponseEntity<Map<String, Object>> handleStorageLimitExceededException(
      StorageLimitExceededException exp,
      WebRequest request) {
    logger.warn("Storage limit exceeded for request {}: Current: {:.2f} MB, Limit: {} MB, Attempted file: {:.2f} MB ({:.1f}% used)",
        request.getDescription(false),
        exp.getCurrentUsageMb(),
        exp.getLimitMb(),
        exp.getFileSizeMb(),
        exp.getPercentageUsed());

    Map<String, Object> errorResponse = new HashMap<>();
    errorResponse.put("status", HttpStatus.PAYLOAD_TOO_LARGE.value());
    errorResponse.put("error", "STORAGE_LIMIT_EXCEEDED");
    errorResponse.put("title", "Storage Limit Exceeded");
    errorResponse.put("message", exp.getUserFriendlyMessage());
    errorResponse.put("path", request.getDescription(false).replace("uri=", ""));

    // Include detailed metrics for client-side display
    Map<String, Object> details = new HashMap<>();
    details.put("currentUsageMb", exp.getCurrentUsageMb());
    details.put("limitMb", exp.getLimitMb());
    details.put("fileSizeMb", exp.getFileSizeMb());
    details.put("newTotalMb", exp.getNewTotalMb());
    details.put("percentageUsed", exp.getPercentageUsed());
    details.put("suggestedAction", "Purchase additional storage or delete unused files");
    errorResponse.put("details", details);

    return new ResponseEntity<>(errorResponse, HttpStatus.PAYLOAD_TOO_LARGE);
  }

  /**
   * Handle tier limit exceeded exception (HTTP 403 Forbidden).
   *
   * <p>Triggered when church attempts to add members beyond their pricing tier's limit.
   * Critical security handler to prevent tier bypass through bulk uploads.
   *
   * <p>Frontend should:
   * <ul>
   *   <li>Display clear message about tier limit</li>
   *   <li>Show current vs max member count</li>
   *   <li>Suggest tier upgrade</li>
   *   <li>Prevent bulk upload form submission if would exceed</li>
   * </ul>
   */
  @ExceptionHandler(TierLimitExceededException.class)
  public ResponseEntity<Map<String, Object>> handleTierLimitExceededException(
      TierLimitExceededException exp,
      WebRequest request) {
    logger.warn("Tier limit exceeded for request {}: Current: {}, Tier max: {}, Attempting to add: {}, Would total: {} ({:.1f}% of limit)",
        request.getDescription(false),
        exp.getCurrentMemberCount(),
        exp.getTierMaxMembers(),
        exp.getMembersToAdd(),
        exp.getNewTotalMembers(),
        exp.getPercentageUsed());

    Map<String, Object> errorResponse = new HashMap<>();
    errorResponse.put("status", HttpStatus.FORBIDDEN.value());
    errorResponse.put("error", "TIER_LIMIT_EXCEEDED");
    errorResponse.put("title", "Tier Member Limit Exceeded");
    errorResponse.put("message", exp.getDetailedMessage());
    errorResponse.put("path", request.getDescription(false).replace("uri=", ""));

    // Include detailed metrics for client-side display
    Map<String, Object> details = new HashMap<>();
    details.put("currentMemberCount", exp.getCurrentMemberCount());
    details.put("tierMaxMembers", exp.getTierMaxMembers());
    details.put("membersToAdd", exp.getMembersToAdd());
    details.put("newTotalMembers", exp.getNewTotalMembers());
    details.put("percentageUsed", exp.getPercentageUsed());
    details.put("upgradeRecommendation", exp.getUpgradeRecommendation());
    details.put("suggestedAction", "Upgrade your pricing tier to add more members");
    errorResponse.put("details", details);

    return new ResponseEntity<>(errorResponse, HttpStatus.FORBIDDEN);
  }

  /**
   * Handle addon already purchased exception (HTTP 409 Conflict).
   *
   * <p>Triggered when church attempts to purchase an addon they already have active.
   *
   * <p>Frontend should:
   * <ul>
   *   <li>Display message that addon is already active</li>
   *   <li>Suggest canceling existing addon first if they want to change</li>
   *   <li>Show addon details (expiry date, storage capacity)</li>
   * </ul>
   */
  @ExceptionHandler(AddonAlreadyPurchasedException.class)
  public ResponseEntity<ErrorResponse> handleAddonAlreadyPurchasedException(
      AddonAlreadyPurchasedException exp,
      WebRequest request) {
    logger.warn("Addon already purchased for request {}: {}",
        request.getDescription(false), exp.getMessage());

    ErrorResponse errorResponse = new ErrorResponse(
        HttpStatus.CONFLICT.value(),
        "Addon Already Active",
        exp.getMessage(),
        request.getDescription(false).replace("uri=", "")
    );

    return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
  }

  /**
   * Handle subscription required for addon exception (HTTP 402 Payment Required).
   *
   * <p>Triggered when church tries to purchase addon without active subscription.
   *
   * <p>Frontend should:
   * <ul>
   *   <li>Redirect to subscription activation page</li>
   *   <li>Show message that base subscription is required first</li>
   *   <li>Display subscription plans</li>
   * </ul>
   */
  @ExceptionHandler(SubscriptionRequiredForAddonException.class)
  public ResponseEntity<ErrorResponse> handleSubscriptionRequiredForAddonException(
      SubscriptionRequiredForAddonException exp,
      WebRequest request) {
    logger.warn("Subscription required for addon purchase: {}",
        request.getDescription(false));

    ErrorResponse errorResponse = new ErrorResponse(
        HttpStatus.PAYMENT_REQUIRED.value(),
        "Subscription Required",
        exp.getMessage(),
        request.getDescription(false).replace("uri=", "")
    );

    return new ResponseEntity<>(errorResponse, HttpStatus.PAYMENT_REQUIRED);
  }

  @ExceptionHandler(DataIntegrityViolationException.class)
  public ResponseEntity<ErrorResponse> handleDataIntegrityViolationException(
      DataIntegrityViolationException exp,
      WebRequest request) {
    logger.warn("Database constraint violation for request {}: {}",
        request.getDescription(false), exp.getMessage());

    String userMessage = "Unable to complete the operation due to a data conflict.";
    String errorTitle = "Data Conflict";

    // Extract the root cause message
    String rootMessage = exp.getMostSpecificCause().getMessage();

    // Check for common constraint violations and provide user-friendly messages
    if (rootMessage != null) {
      String lowerMessage = rootMessage.toLowerCase();

      // Email unique constraint violation
      if (lowerMessage.contains("duplicate") && lowerMessage.contains("email")) {
        userMessage = "This email address is already registered. Please use a different email or try logging in.";
        errorTitle = "Email Already Registered";
      }
      // Church name unique constraint violation
      else if (lowerMessage.contains("duplicate") && lowerMessage.contains("church") && lowerMessage.contains("name")) {
        userMessage = "A church with this name already exists. Please choose a different name.";
        errorTitle = "Church Name Already Exists";
      }
      // Phone number unique constraint violation
      else if (lowerMessage.contains("duplicate") && lowerMessage.contains("phone")) {
        userMessage = "This phone number is already registered. Please use a different phone number.";
        errorTitle = "Phone Number Already Registered";
      }
      // Partnership code unique constraint violation
      else if (lowerMessage.contains("duplicate") && lowerMessage.contains("code")) {
        userMessage = "This code already exists. Please use a different code.";
        errorTitle = "Duplicate Code";
      }
      // Primary key constraint violation (unusual case - likely a system issue)
      else if (lowerMessage.contains("primary") || lowerMessage.contains("pk_")) {
        userMessage = "A system error occurred. Please try again. If the problem persists, contact support.";
        errorTitle = "System Error";
        logger.error("PRIMARY KEY constraint violation - possible data corruption or ID generation issue: {}",
            rootMessage);
      }
      // Foreign key constraint violation
      else if (lowerMessage.contains("foreign key") || lowerMessage.contains("fk_")) {
        userMessage = "This operation cannot be completed because the referenced data does not exist or has been deleted.";
        errorTitle = "Invalid Reference";
      }
      // Not null constraint violation
      else if (lowerMessage.contains("null") && lowerMessage.contains("not")) {
        userMessage = "Required information is missing. Please fill in all required fields.";
        errorTitle = "Missing Required Information";
      }
      // Generic duplicate entry
      else if (lowerMessage.contains("duplicate entry") || lowerMessage.contains("duplicate key")) {
        userMessage = "This information already exists in the system. Please check your input and try again.";
        errorTitle = "Duplicate Entry";
      }
    }

    ErrorResponse errorResponse = new ErrorResponse(
        HttpStatus.CONFLICT.value(),
        errorTitle,
        userMessage,
        request.getDescription(false).replace("uri=", "")
    );

    return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleGlobalException(
      Exception exp,
      WebRequest request) {
    logger.error("Unexpected error for request {}: {}", request.getDescription(false), exp.getMessage(), exp);

    ErrorResponse errorResponse = new ErrorResponse(
      HttpStatus.INTERNAL_SERVER_ERROR.value(),
      "Internal Server Error",
      "An unexpected error occurred. Please try again later.",
      request.getDescription(false).replace("uri=", "")
    );

    return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
  }

  @ExceptionHandler(UsernameNotFoundException.class)
  public ResponseEntity<ErrorResponse> handleUsernameNotFoundException(
      UsernameNotFoundException exp,
      WebRequest request) {

      // No logging to prevent terminal spam during development
      // UsernameNotFoundException is expected during login attempts with invalid emails
      // Security monitoring can be done via authentication failure metrics instead

      // Return same generic message as BadCredentialsException for security
      // This prevents attackers from determining which emails exist in the system
      ErrorResponse errorResponse = new ErrorResponse(
          HttpStatus.UNAUTHORIZED.value(),
          "Authentication Failed",
          "Incorrect email or password. Please verify your credentials.",
          request.getDescription(false).replace("uri=", "")
      );

      return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
  }

}
