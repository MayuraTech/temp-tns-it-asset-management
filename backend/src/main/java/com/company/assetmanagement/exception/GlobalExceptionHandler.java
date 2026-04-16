package com.company.assetmanagement.exception;

import com.company.assetmanagement.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

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
     * Handle AssetAlreadyAssignedException - returns 409 Conflict.
     */
    @ExceptionHandler(AssetAlreadyAssignedException.class)
    public ResponseEntity<ErrorResponse> handleAssetAlreadyAssigned(
            AssetAlreadyAssignedException ex, HttpServletRequest request) {
        
        logger.warn("Asset already assigned: {}", ex.getAssetId());
        
        Map<String, String> details = new HashMap<>();
        details.put("assetId", ex.getAssetId().toString());
        
        ErrorResponse errorResponse = ErrorResponse.builder()
            .type("ASSET_ALREADY_ASSIGNED")
            .message(ex.getMessage())
            .details(details)
            .requestId(getRequestId(request))
            .build();
        
        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
    }
    
    /**
     * Handle AssetNotAssignedException - returns 409 Conflict.
     */
    @ExceptionHandler(AssetNotAssignedException.class)
    public ResponseEntity<ErrorResponse> handleAssetNotAssigned(
            AssetNotAssignedException ex, HttpServletRequest request) {
        
        logger.warn("Asset not assigned: {}", ex.getAssetId());
        
        Map<String, String> details = new HashMap<>();
        details.put("assetId", ex.getAssetId().toString());
        
        ErrorResponse errorResponse = ErrorResponse.builder()
            .type("ASSET_NOT_ASSIGNED")
            .message(ex.getMessage())
            .details(details)
            .requestId(getRequestId(request))
            .build();
        
        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
    }
    
    /**
     * Handle AssetNotAssignableException - returns 422 Unprocessable Entity.
     */
    @ExceptionHandler(AssetNotAssignableException.class)
    public ResponseEntity<ErrorResponse> handleAssetNotAssignable(
            AssetNotAssignableException ex, HttpServletRequest request) {
        
        logger.warn("Asset not assignable: {} with status {}", ex.getAssetId(), ex.getStatus());
        
        Map<String, String> details = new HashMap<>();
        details.put("assetId", ex.getAssetId().toString());
        details.put("status", ex.getStatus().toString());
        
        ErrorResponse errorResponse = ErrorResponse.builder()
            .type("ASSET_NOT_ASSIGNABLE")
            .message(ex.getMessage())
            .details(details)
            .requestId(getRequestId(request))
            .build();
        
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(errorResponse);
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
