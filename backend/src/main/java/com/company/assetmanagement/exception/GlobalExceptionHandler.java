package com.company.assetmanagement.exception;

import com.company.assetmanagement.dto.ErrorResponse;
import com.company.assetmanagement.service.AuthenticationEventService;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Global exception handler for the IT Asset Management application.
 * Handles all exceptions and returns structured error responses with appropriate HTTP status codes.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    
    @Autowired
    private AuthenticationEventService authenticationEventService;
    
    /**
     * Handle AuthenticationFailedException - returns 401 Unauthorized.
     */
    @ExceptionHandler(AuthenticationFailedException.class)
    public ResponseEntity<ErrorResponse> handleAuthenticationFailed(
            AuthenticationFailedException ex, HttpServletRequest request) {
        
        // Log authentication failure (without sensitive data)
        String ipAddress = authenticationEventService.getClientIpAddress(request);
        String userAgent = request.getHeader("User-Agent");
        authenticationEventService.logLoginFailure(ex.getUsername(), ex.getErrorType(), ipAddress, userAgent);
        
        logger.warn("Authentication failed for user: {}, type: {}, IP: {}", 
            ex.getUsername(), ex.getErrorType(), ipAddress);
        
        Map<String, String> details = new HashMap<>();
        details.put("errorType", ex.getErrorType());
        
        ErrorResponse errorResponse = ErrorResponse.builder()
            .type("AUTHENTICATION_FAILED")
            .message(ex.getMessage())
            .details(details)
            .requestId(getRequestId(request))
            .build();
        
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
    }
    
    /**
     * Handle AccountLockedException - returns 423 Locked.
     */
    @ExceptionHandler(AccountLockedException.class)
    public ResponseEntity<ErrorResponse> handleAccountLocked(
            AccountLockedException ex, HttpServletRequest request) {
        
        logger.warn("Account locked: {}", ex.getMessage());
        
        Map<String, Object> details = new HashMap<>();
        if (ex.getLockUntil() != null) {
            details.put("lockUntil", ex.getLockUntil().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            details.put("reason", "Multiple failed login attempts");
            details.put("lockDurationMinutes", 30);
        }
        
        ErrorResponse errorResponse = ErrorResponse.builder()
            .type("ACCOUNT_LOCKED")
            .message(ex.getMessage())
            .details(details)
            .requestId(getRequestId(request))
            .build();
        
        return ResponseEntity.status(HttpStatus.LOCKED).body(errorResponse);
    }
    
    /**
     * Handle Spring Security AuthenticationException - returns 401 Unauthorized.
     */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthenticationException(
            AuthenticationException ex, HttpServletRequest request) {
        
        // Log authentication error
        String ipAddress = authenticationEventService.getClientIpAddress(request);
        String userAgent = request.getHeader("User-Agent");
        
        String errorType = ex instanceof BadCredentialsException ? "INVALID_CREDENTIALS" : "AUTHENTICATION_ERROR";
        authenticationEventService.logLoginFailure("unknown", errorType, ipAddress, userAgent);
        
        logger.warn("Authentication exception: {}, IP: {}", ex.getMessage(), ipAddress);
        
        ErrorResponse errorResponse = ErrorResponse.builder()
            .type("AUTHENTICATION_ERROR")
            .message("Authentication failed. Please check your credentials.")
            .requestId(getRequestId(request))
            .build();
        
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
    }
    
    /**
     * Handle ValidationException - returns 400 Bad Request with comprehensive error details.
     */
    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
            ValidationException ex, HttpServletRequest request) {
        
        logger.warn("Validation error: {}", ex.getMessage());
        
        List<Map<String, Object>> errorDetails = ex.getErrors().stream()
            .map(error -> {
                Map<String, Object> errorMap = new HashMap<>();
                errorMap.put("field", error.getField());
                errorMap.put("message", error.getMessage());
                if (error.getValue() != null) {
                    errorMap.put("value", error.getValue());
                }
                return errorMap;
            })
            .collect(Collectors.toList());
        
        ErrorResponse errorResponse = ErrorResponse.builder()
            .type("VALIDATION_ERROR")
            .message("Validation failed")
            .details(errorDetails)
            .requestId(getRequestId(request))
            .build();
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }
    
    /**
     * Handle Bean Validation errors - returns 400 Bad Request.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, HttpServletRequest request) {
        
        logger.warn("Bean validation error: {}", ex.getMessage());
        
        List<Map<String, Object>> errorDetails = ex.getBindingResult().getFieldErrors().stream()
            .map(error -> {
                Map<String, Object> errorMap = new HashMap<>();
                errorMap.put("field", error.getField());
                errorMap.put("message", error.getDefaultMessage());
                errorMap.put("rejectedValue", error.getRejectedValue());
                return errorMap;
            })
            .collect(Collectors.toList());
        
        ErrorResponse errorResponse = ErrorResponse.builder()
            .type("VALIDATION_ERROR")
            .message("Validation failed")
            .details(errorDetails)
            .requestId(getRequestId(request))
            .build();
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }
    
    /**
     * Handle DuplicateSerialNumberException - returns 409 Conflict.
     */
    @ExceptionHandler(DuplicateSerialNumberException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateSerialNumber(
            DuplicateSerialNumberException ex, HttpServletRequest request) {
        
        logger.warn("Duplicate serial number: {}", ex.getSerialNumber());
        
        Map<String, String> details = new HashMap<>();
        details.put("serialNumber", ex.getSerialNumber());
        
        ErrorResponse errorResponse = ErrorResponse.builder()
            .type("DUPLICATE_SERIAL_NUMBER")
            .message("Asset with serial number already exists")
            .details(details)
            .requestId(getRequestId(request))
            .build();
        
        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
    }
    
    /**
     * Handle InsufficientPermissionsException - returns 403 Forbidden.
     */
    @ExceptionHandler(InsufficientPermissionsException.class)
    public ResponseEntity<ErrorResponse> handleInsufficientPermissions(
            InsufficientPermissionsException ex, HttpServletRequest request) {
        
        logger.warn("Insufficient permissions: {}", ex.getMessage());
        
        Map<String, String> details = new HashMap<>();
        if (ex.getUserId() != null) {
            details.put("userId", ex.getUserId());
        }
        if (ex.getAction() != null) {
            details.put("action", ex.getAction());
        }
        
        ErrorResponse errorResponse = ErrorResponse.builder()
            .type("INSUFFICIENT_PERMISSIONS")
            .message("You do not have permission to perform this action")
            .details(details.isEmpty() ? null : details)
            .requestId(getRequestId(request))
            .build();
        
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
    }
    
    /**
     * Handle ResourceNotFoundException - returns 404 Not Found.
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFound(
            ResourceNotFoundException ex, HttpServletRequest request) {
        
        logger.warn("Resource not found: {} with ID {}", ex.getResourceType(), ex.getResourceId());
        
        Map<String, String> details = new HashMap<>();
        details.put("resourceType", ex.getResourceType());
        details.put("resourceId", ex.getResourceId());
        
        ErrorResponse errorResponse = ErrorResponse.builder()
            .type("RESOURCE_NOT_FOUND")
            .message(ex.getMessage())
            .details(details)
            .requestId(getRequestId(request))
            .build();
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }
    
    /**
     * Handle InvalidStatusTransitionException - returns 422 Unprocessable Entity.
     */
    @ExceptionHandler(InvalidStatusTransitionException.class)
    public ResponseEntity<ErrorResponse> handleInvalidStatusTransition(
            InvalidStatusTransitionException ex, HttpServletRequest request) {
        
        logger.warn("Invalid status transition: from {} to {} for {}", 
            ex.getFromStatus(), ex.getToStatus(), ex.getResourceType());
        
        Map<String, String> details = new HashMap<>();
        details.put("fromStatus", ex.getFromStatus());
        details.put("toStatus", ex.getToStatus());
        details.put("resourceType", ex.getResourceType());
        
        ErrorResponse errorResponse = ErrorResponse.builder()
            .type("INVALID_STATUS_TRANSITION")
            .message(ex.getMessage())
            .details(details)
            .requestId(getRequestId(request))
            .build();
        
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(errorResponse);
    }
    
    /**
     * Handle BadCredentialsException - returns 401 Unauthorized.
     */
    @ExceptionHandler(org.springframework.security.authentication.BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentials(
            org.springframework.security.authentication.BadCredentialsException ex, HttpServletRequest request) {
        
        logger.warn("Authentication failed: {}", ex.getMessage());
        
        ErrorResponse errorResponse = ErrorResponse.builder()
            .type("AUTHENTICATION_FAILED")
            .message("Invalid username or password")
            .requestId(getRequestId(request))
            .build();
        
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
    }
    
    /**
     * Handle AccountDisabledException - returns 401 Unauthorized.
     * Provides information about the disabled account status.
     */
    @ExceptionHandler(AccountDisabledException.class)
    public ResponseEntity<ErrorResponse> handleAccountDisabled(
            AccountDisabledException ex, HttpServletRequest request) {
        
        logger.warn("Account disabled: {}", ex.getMessage());
        
        Map<String, Object> details = new HashMap<>();
        if (ex.getUserId() != null) {
            details.put("userId", ex.getUserId());
        }
        details.put("reason", "Account has been administratively disabled");
        details.put("action", "Contact your system administrator to reactivate your account");
        
        ErrorResponse errorResponse = ErrorResponse.builder()
            .type("ACCOUNT_DISABLED")
            .message("Account has been disabled by an administrator. Please contact support for assistance.")
            .details(details)
            .requestId(getRequestId(request))
            .build();
        
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
    }
    
    /**
     * Handle DuplicateUsernameException - returns 409 Conflict.
     * Provides information about the duplicate username constraint violation.
     */
    @ExceptionHandler(DuplicateUsernameException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateUsername(
            DuplicateUsernameException ex, HttpServletRequest request) {
        
        logger.warn("Duplicate username: {}", ex.getUsername());
        
        Map<String, String> details = new HashMap<>();
        details.put("username", ex.getUsername());
        details.put("field", "username");
        details.put("constraint", "unique");
        
        ErrorResponse errorResponse = ErrorResponse.builder()
            .type("DUPLICATE_USERNAME")
            .message("Username already exists. Please choose a different username.")
            .details(details)
            .requestId(getRequestId(request))
            .build();
        
        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
    }
    
    /**
     * Handle DuplicateEmailException - returns 409 Conflict.
     * Provides information about the duplicate email constraint violation.
     */
    @ExceptionHandler(DuplicateEmailException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateEmail(
            DuplicateEmailException ex, HttpServletRequest request) {
        
        logger.warn("Duplicate email: {}", ex.getEmail());
        
        Map<String, String> details = new HashMap<>();
        details.put("email", ex.getEmail());
        details.put("field", "email");
        details.put("constraint", "unique");
        
        ErrorResponse errorResponse = ErrorResponse.builder()
            .type("DUPLICATE_EMAIL")
            .message("Email address already exists. Please use a different email address.")
            .details(details)
            .requestId(getRequestId(request))
            .build();
        
        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
    }
    
    /**
     * Handle UserNotFoundException - returns 404 Not Found.
     * Provides information about the user that could not be found.
     */
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUserNotFound(
            UserNotFoundException ex, HttpServletRequest request) {
        
        logger.warn("User not found: {}", ex.getUserId());
        
        Map<String, String> details = new HashMap<>();
        details.put("userId", ex.getUserId());
        details.put("resourceType", "User");
        
        ErrorResponse errorResponse = ErrorResponse.builder()
            .type("USER_NOT_FOUND")
            .message("User not found with the specified ID")
            .details(details)
            .requestId(getRequestId(request))
            .build();
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }
    
    /**
     * Handle generic exceptions - returns 500 Internal Server Error.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(
            Exception ex, HttpServletRequest request) {
        
        logger.error("Unexpected error occurred", ex);
        
        ErrorResponse errorResponse = ErrorResponse.builder()
            .type("INTERNAL_SERVER_ERROR")
            .message("An unexpected error occurred. Please try again later.")
            .requestId(getRequestId(request))
            .build();
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
    
    /**
     * Extract request ID from request headers or generate a new one.
     */
    private String getRequestId(HttpServletRequest request) {
        String requestId = request.getHeader("X-Request-ID");
        if (requestId == null || requestId.isEmpty()) {
            requestId = java.util.UUID.randomUUID().toString();
        }
        return requestId;
    }
}
