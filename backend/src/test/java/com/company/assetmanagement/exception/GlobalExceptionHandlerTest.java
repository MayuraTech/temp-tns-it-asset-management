package com.company.assetmanagement.exception;

import com.company.assetmanagement.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("GlobalExceptionHandler Tests")
class GlobalExceptionHandlerTest {
    
    private GlobalExceptionHandler exceptionHandler;
    
    @Mock
    private HttpServletRequest request;
    
    @Mock
    private MethodArgumentNotValidException methodArgumentNotValidException;
    
    @Mock
    private BindingResult bindingResult;
    
    @BeforeEach
    void setUp() {
        exceptionHandler = new GlobalExceptionHandler();
        when(request.getHeader("X-Request-ID")).thenReturn("test-request-123");
    }
    
    @Test
    @DisplayName("Should handle ValidationException with comprehensive error details")
    void shouldHandleValidationException() {
        // Given
        List<ValidationException.ValidationError> errors = List.of(
            new ValidationException.ValidationError("name", "Name is required"),
            new ValidationException.ValidationError("serialNumber", "Serial number must be between 5 and 100 characters", "ABC")
        );
        ValidationException exception = new ValidationException(errors);
        
        // When
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleValidationException(exception, request);
        
        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getType()).isEqualTo("VALIDATION_ERROR");
        assertThat(response.getBody().getMessage()).isEqualTo("Validation failed");
        assertThat(response.getBody().getRequestId()).isEqualTo("test-request-123");
        
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> details = (List<Map<String, Object>>) response.getBody().getDetails();
        assertThat(details).hasSize(2);
        assertThat(details.get(0).get("field")).isEqualTo("name");
        assertThat(details.get(0).get("message")).isEqualTo("Name is required");
        assertThat(details.get(1).get("field")).isEqualTo("serialNumber");
        assertThat(details.get(1).get("value")).isEqualTo("ABC");
    }
    
    @Test
    @DisplayName("Should handle single field ValidationException")
    void shouldHandleSingleFieldValidationException() {
        // Given
        ValidationException exception = new ValidationException("email", "Invalid email format");
        
        // When
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleValidationException(exception, request);
        
        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getType()).isEqualTo("VALIDATION_ERROR");
        
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> details = (List<Map<String, Object>>) response.getBody().getDetails();
        assertThat(details).hasSize(1);
        assertThat(details.get(0).get("field")).isEqualTo("email");
        assertThat(details.get(0).get("message")).isEqualTo("Invalid email format");
    }
    
    @Test
    @DisplayName("Should handle MethodArgumentNotValidException from Bean Validation")
    void shouldHandleMethodArgumentNotValidException() {
        // Given
        FieldError fieldError1 = new FieldError("assetRequest", "name", "invalid", false, null, null, "Name is required");
        FieldError fieldError2 = new FieldError("assetRequest", "serialNumber", "AB", false, null, null, "Serial number must be at least 5 characters");
        
        when(methodArgumentNotValidException.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError1, fieldError2));
        
        // When
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleMethodArgumentNotValid(methodArgumentNotValidException, request);
        
        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getType()).isEqualTo("VALIDATION_ERROR");
        assertThat(response.getBody().getMessage()).isEqualTo("Validation failed");
        
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> details = (List<Map<String, Object>>) response.getBody().getDetails();
        assertThat(details).hasSize(2);
    }
    
    @Test
    @DisplayName("Should handle DuplicateSerialNumberException with 409 Conflict")
    void shouldHandleDuplicateSerialNumberException() {
        // Given
        DuplicateSerialNumberException exception = new DuplicateSerialNumberException("SRV-001");
        
        // When
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleDuplicateSerialNumber(exception, request);
        
        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getType()).isEqualTo("DUPLICATE_SERIAL_NUMBER");
        assertThat(response.getBody().getMessage()).isEqualTo("Asset with serial number already exists");
        assertThat(response.getBody().getRequestId()).isEqualTo("test-request-123");
        
        @SuppressWarnings("unchecked")
        Map<String, String> details = (Map<String, String>) response.getBody().getDetails();
        assertThat(details.get("serialNumber")).isEqualTo("SRV-001");
    }
    
    @Test
    @DisplayName("Should handle InsufficientPermissionsException with 403 Forbidden")
    void shouldHandleInsufficientPermissionsException() {
        // Given
        InsufficientPermissionsException exception = new InsufficientPermissionsException("user-123", "CREATE_ASSET");
        
        // When
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleInsufficientPermissions(exception, request);
        
        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getType()).isEqualTo("INSUFFICIENT_PERMISSIONS");
        assertThat(response.getBody().getMessage()).isEqualTo("You do not have permission to perform this action");
        assertThat(response.getBody().getRequestId()).isEqualTo("test-request-123");
        
        @SuppressWarnings("unchecked")
        Map<String, String> details = (Map<String, String>) response.getBody().getDetails();
        assertThat(details.get("userId")).isEqualTo("user-123");
        assertThat(details.get("action")).isEqualTo("CREATE_ASSET");
    }
    
    @Test
    @DisplayName("Should handle InsufficientPermissionsException without details")
    void shouldHandleInsufficientPermissionsExceptionWithoutDetails() {
        // Given
        InsufficientPermissionsException exception = new InsufficientPermissionsException();
        
        // When
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleInsufficientPermissions(exception, request);
        
        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getType()).isEqualTo("INSUFFICIENT_PERMISSIONS");
        assertThat(response.getBody().getDetails()).isNull();
    }
    
    @Test
    @DisplayName("Should handle ResourceNotFoundException with 404 Not Found")
    void shouldHandleResourceNotFoundException() {
        // Given
        ResourceNotFoundException exception = new ResourceNotFoundException("Asset", "550e8400-e29b-41d4-a716-446655440000");
        
        // When
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleResourceNotFound(exception, request);
        
        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getType()).isEqualTo("RESOURCE_NOT_FOUND");
        assertThat(response.getBody().getMessage()).contains("Asset with ID");
        assertThat(response.getBody().getMessage()).contains("not found");
        assertThat(response.getBody().getRequestId()).isEqualTo("test-request-123");
        
        @SuppressWarnings("unchecked")
        Map<String, String> details = (Map<String, String>) response.getBody().getDetails();
        assertThat(details.get("resourceType")).isEqualTo("Asset");
        assertThat(details.get("resourceId")).isEqualTo("550e8400-e29b-41d4-a716-446655440000");
    }
    
    @Test
    @DisplayName("Should handle InvalidStatusTransitionException with 422 Unprocessable Entity")
    void shouldHandleInvalidStatusTransitionException() {
        // Given
        InvalidStatusTransitionException exception = new InvalidStatusTransitionException("retired", "in_use", "Asset");
        
        // When
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleInvalidStatusTransition(exception, request);
        
        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getType()).isEqualTo("INVALID_STATUS_TRANSITION");
        assertThat(response.getBody().getMessage()).contains("Invalid status transition");
        assertThat(response.getBody().getRequestId()).isEqualTo("test-request-123");
        
        @SuppressWarnings("unchecked")
        Map<String, String> details = (Map<String, String>) response.getBody().getDetails();
        assertThat(details.get("fromStatus")).isEqualTo("retired");
        assertThat(details.get("toStatus")).isEqualTo("in_use");
        assertThat(details.get("resourceType")).isEqualTo("Asset");
    }
    
    @Test
    @DisplayName("Should handle generic Exception with 500 Internal Server Error")
    void shouldHandleGenericException() {
        // Given
        Exception exception = new RuntimeException("Unexpected database error");
        
        // When
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleGenericException(exception, request);
        
        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getType()).isEqualTo("INTERNAL_SERVER_ERROR");
        assertThat(response.getBody().getMessage()).isEqualTo("An unexpected error occurred. Please try again later.");
        assertThat(response.getBody().getRequestId()).isEqualTo("test-request-123");
        assertThat(response.getBody().getDetails()).isNull();
    }
    
    @Test
    @DisplayName("Should generate request ID when not provided in header")
    void shouldGenerateRequestIdWhenNotProvided() {
        // Given
        when(request.getHeader("X-Request-ID")).thenReturn(null);
        ValidationException exception = new ValidationException("field", "error");
        
        // When
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleValidationException(exception, request);
        
        // Then
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getRequestId()).isNotNull();
        assertThat(response.getBody().getRequestId()).isNotEmpty();
    }
}
