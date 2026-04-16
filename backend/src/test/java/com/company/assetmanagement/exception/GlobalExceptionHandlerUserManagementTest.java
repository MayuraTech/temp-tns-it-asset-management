package com.company.assetmanagement.exception;

import com.company.assetmanagement.dto.ErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;

import java.time.LocalDateTime;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for GlobalExceptionHandler with user management exceptions.
 */
@DisplayName("GlobalExceptionHandler - User Management")
class GlobalExceptionHandlerUserManagementTest {

    private GlobalExceptionHandler exceptionHandler;
    private MockHttpServletRequest request;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        exceptionHandler = new GlobalExceptionHandler();
        request = new MockHttpServletRequest();
        request.addHeader("X-Request-ID", "test-request-123");
        objectMapper = new ObjectMapper();
    }

    @Test
    @DisplayName("Should handle AccountLockedException with 401 status")
    void shouldHandleAccountLockedExceptionWith401Status() {
        // Given
        LocalDateTime lockUntil = LocalDateTime.of(2024, 1, 15, 14, 30);
        AccountLockedException exception = new AccountLockedException(lockUntil);

        // When
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleAccountLocked(exception, request);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        
        ErrorResponse errorResponse = response.getBody();
        assertThat(errorResponse).isNotNull();
        assertThat(errorResponse.getType()).isEqualTo("ACCOUNT_LOCKED");
        assertThat(errorResponse.getMessage()).isEqualTo("Account is temporarily locked due to multiple failed login attempts");
        assertThat(errorResponse.getRequestId()).isEqualTo("test-request-123");
        
        @SuppressWarnings("unchecked")
        Map<String, Object> details = (Map<String, Object>) errorResponse.getDetails();
        assertThat(details).containsKey("lockUntil");
        assertThat(details.get("lockUntil")).isEqualTo(lockUntil.toString());
    }

    @Test
    @DisplayName("Should handle AccountDisabledException with 401 status")
    void shouldHandleAccountDisabledExceptionWith401Status() {
        // Given
        String userId = "user-123";
        AccountDisabledException exception = new AccountDisabledException(userId);

        // When
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleAccountDisabled(exception, request);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        
        ErrorResponse errorResponse = response.getBody();
        assertThat(errorResponse).isNotNull();
        assertThat(errorResponse.getType()).isEqualTo("ACCOUNT_DISABLED");
        assertThat(errorResponse.getMessage()).isEqualTo("Account has been disabled by an administrator");
        assertThat(errorResponse.getRequestId()).isEqualTo("test-request-123");
        
        @SuppressWarnings("unchecked")
        Map<String, Object> details = (Map<String, Object>) errorResponse.getDetails();
        assertThat(details).containsKey("userId");
        assertThat(details.get("userId")).isEqualTo(userId);
    }

    @Test
    @DisplayName("Should handle AccountDisabledException without userId")
    void shouldHandleAccountDisabledExceptionWithoutUserId() {
        // Given
        AccountDisabledException exception = new AccountDisabledException();

        // When
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleAccountDisabled(exception, request);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        
        ErrorResponse errorResponse = response.getBody();
        assertThat(errorResponse).isNotNull();
        assertThat(errorResponse.getType()).isEqualTo("ACCOUNT_DISABLED");
        assertThat(errorResponse.getDetails()).isNull();
    }

    @Test
    @DisplayName("Should handle DuplicateUsernameException with 409 status")
    void shouldHandleDuplicateUsernameExceptionWith409Status() {
        // Given
        String username = "testuser";
        DuplicateUsernameException exception = new DuplicateUsernameException(username);

        // When
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleDuplicateUsername(exception, request);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        
        ErrorResponse errorResponse = response.getBody();
        assertThat(errorResponse).isNotNull();
        assertThat(errorResponse.getType()).isEqualTo("DUPLICATE_USERNAME");
        assertThat(errorResponse.getMessage()).isEqualTo("Username already exists");
        assertThat(errorResponse.getRequestId()).isEqualTo("test-request-123");
        
        @SuppressWarnings("unchecked")
        Map<String, Object> details = (Map<String, Object>) errorResponse.getDetails();
        assertThat(details).containsKey("username");
        assertThat(details.get("username")).isEqualTo(username);
    }

    @Test
    @DisplayName("Should handle DuplicateEmailException with 409 status")
    void shouldHandleDuplicateEmailExceptionWith409Status() {
        // Given
        String email = "test@example.com";
        DuplicateEmailException exception = new DuplicateEmailException(email);

        // When
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleDuplicateEmail(exception, request);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        
        ErrorResponse errorResponse = response.getBody();
        assertThat(errorResponse).isNotNull();
        assertThat(errorResponse.getType()).isEqualTo("DUPLICATE_EMAIL");
        assertThat(errorResponse.getMessage()).isEqualTo("Email address already exists");
        assertThat(errorResponse.getRequestId()).isEqualTo("test-request-123");
        
        @SuppressWarnings("unchecked")
        Map<String, Object> details = (Map<String, Object>) errorResponse.getDetails();
        assertThat(details).containsKey("email");
        assertThat(details.get("email")).isEqualTo(email);
    }

    @Test
    @DisplayName("Should handle UserNotFoundException with 404 status")
    void shouldHandleUserNotFoundExceptionWith404Status() {
        // Given
        String userId = "user-123";
        UserNotFoundException exception = new UserNotFoundException(userId);

        // When
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleUserNotFound(exception, request);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        
        ErrorResponse errorResponse = response.getBody();
        assertThat(errorResponse).isNotNull();
        assertThat(errorResponse.getType()).isEqualTo("USER_NOT_FOUND");
        assertThat(errorResponse.getMessage()).isEqualTo("User not found");
        assertThat(errorResponse.getRequestId()).isEqualTo("test-request-123");
        
        @SuppressWarnings("unchecked")
        Map<String, Object> details = (Map<String, Object>) errorResponse.getDetails();
        assertThat(details).containsKey("userId");
        assertThat(details.get("userId")).isEqualTo(userId);
    }

    @Test
    @DisplayName("Should generate request ID when not provided in header")
    void shouldGenerateRequestIdWhenNotProvidedInHeader() {
        // Given
        MockHttpServletRequest requestWithoutId = new MockHttpServletRequest();
        UserNotFoundException exception = new UserNotFoundException("user-123");

        // When
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleUserNotFound(exception, requestWithoutId);

        // Then
        ErrorResponse errorResponse = response.getBody();
        assertThat(errorResponse).isNotNull();
        assertThat(errorResponse.getRequestId()).isNotNull();
        assertThat(errorResponse.getRequestId()).isNotEmpty();
        // Should be a valid UUID format
        assertThat(errorResponse.getRequestId()).matches("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}");
    }
}