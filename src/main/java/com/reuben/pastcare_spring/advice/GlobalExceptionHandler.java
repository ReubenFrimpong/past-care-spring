package com.reuben.pastcare_spring.advice;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

import com.reuben.pastcare_spring.dtos.ErrorResponse;
import com.reuben.pastcare_spring.exceptions.AccountLockedException;
import com.reuben.pastcare_spring.exceptions.DuplicateChurchException;
import com.reuben.pastcare_spring.exceptions.DuplicateResourceException;
import com.reuben.pastcare_spring.exceptions.DuplicateUserException;
import com.reuben.pastcare_spring.exceptions.FileUploadException;
import com.reuben.pastcare_spring.exceptions.InvalidCredentialsException;
import com.reuben.pastcare_spring.exceptions.ResourceNotFoundException;
import com.reuben.pastcare_spring.exceptions.TooManyRequestsException;
import com.reuben.pastcare_spring.exceptions.UnauthorizedException;

import java.time.format.DateTimeFormatter;

@ControllerAdvice
public class GlobalExceptionHandler {

  private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

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
