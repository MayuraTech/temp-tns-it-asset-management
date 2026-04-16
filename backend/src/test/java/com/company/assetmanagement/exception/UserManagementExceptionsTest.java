package com.company.assetmanagement.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for user management specific exceptions.
 */
@DisplayName("User Management Exceptions")
class UserManagementExceptionsTest {

    @Test
    @DisplayName("AccountLockedException should contain lock expiration time")
    void accountLockedExceptionShouldContainLockExpirationTime() {
        // Given
        LocalDateTime lockUntil = LocalDateTime.now().plusMinutes(30);
        
        // When
        AccountLockedException exception = new AccountLockedException(lockUntil);
        
        // Then
        assertThat(exception.getMessage()).contains("Account is locked until");
        assertThat(exception.getLockUntil()).isEqualTo(lockUntil);
    }

    @Test
    @DisplayName("AccountLockedException should support custom message")
    void accountLockedExceptionShouldSupportCustomMessage() {
        // Given
        String customMessage = "Custom lock message";
        LocalDateTime lockUntil = LocalDateTime.now().plusMinutes(30);
        
        // When
        AccountLockedException exception = new AccountLockedException(customMessage, lockUntil);
        
        // Then
        assertThat(exception.getMessage()).isEqualTo(customMessage);
        assertThat(exception.getLockUntil()).isEqualTo(lockUntil);
    }

    @Test
    @DisplayName("AccountDisabledException should have default message")
    void accountDisabledExceptionShouldHaveDefaultMessage() {
        // When
        AccountDisabledException exception = new AccountDisabledException();
        
        // Then
        assertThat(exception.getMessage()).isEqualTo("Account is disabled");
        assertThat(exception.getUserId()).isNull();
    }

    @Test
    @DisplayName("AccountDisabledException should contain user ID")
    void accountDisabledExceptionShouldContainUserId() {
        // Given
        String userId = "user-123";
        
        // When
        AccountDisabledException exception = new AccountDisabledException(userId);
        
        // Then
        assertThat(exception.getMessage()).contains(userId);
        assertThat(exception.getUserId()).isEqualTo(userId);
    }

    @Test
    @DisplayName("AccountDisabledException should support custom message")
    void accountDisabledExceptionShouldSupportCustomMessage() {
        // Given
        String customMessage = "Custom disabled message";
        String userId = "user-123";
        
        // When
        AccountDisabledException exception = new AccountDisabledException(customMessage, userId);
        
        // Then
        assertThat(exception.getMessage()).isEqualTo(customMessage);
        assertThat(exception.getUserId()).isEqualTo(userId);
    }

    @Test
    @DisplayName("DuplicateUsernameException should contain username")
    void duplicateUsernameExceptionShouldContainUsername() {
        // Given
        String username = "testuser";
        
        // When
        DuplicateUsernameException exception = new DuplicateUsernameException(username);
        
        // Then
        assertThat(exception.getMessage()).contains("Username already exists: " + username);
        assertThat(exception.getUsername()).isEqualTo(username);
    }

    @Test
    @DisplayName("DuplicateUsernameException should support custom message")
    void duplicateUsernameExceptionShouldSupportCustomMessage() {
        // Given
        String customMessage = "Custom duplicate username message";
        String username = "testuser";
        
        // When
        DuplicateUsernameException exception = new DuplicateUsernameException(customMessage, username);
        
        // Then
        assertThat(exception.getMessage()).isEqualTo(customMessage);
        assertThat(exception.getUsername()).isEqualTo(username);
    }

    @Test
    @DisplayName("DuplicateEmailException should contain email")
    void duplicateEmailExceptionShouldContainEmail() {
        // Given
        String email = "test@example.com";
        
        // When
        DuplicateEmailException exception = new DuplicateEmailException(email);
        
        // Then
        assertThat(exception.getMessage()).contains("Email already exists: " + email);
        assertThat(exception.getEmail()).isEqualTo(email);
    }

    @Test
    @DisplayName("DuplicateEmailException should support custom message")
    void duplicateEmailExceptionShouldSupportCustomMessage() {
        // Given
        String customMessage = "Custom duplicate email message";
        String email = "test@example.com";
        
        // When
        DuplicateEmailException exception = new DuplicateEmailException(customMessage, email);
        
        // Then
        assertThat(exception.getMessage()).isEqualTo(customMessage);
        assertThat(exception.getEmail()).isEqualTo(email);
    }

    @Test
    @DisplayName("UserNotFoundException should contain user ID")
    void userNotFoundExceptionShouldContainUserId() {
        // Given
        String userId = "user-123";
        
        // When
        UserNotFoundException exception = new UserNotFoundException(userId);
        
        // Then
        assertThat(exception.getMessage()).contains("User not found: " + userId);
        assertThat(exception.getUserId()).isEqualTo(userId);
    }

    @Test
    @DisplayName("UserNotFoundException should support custom message")
    void userNotFoundExceptionShouldSupportCustomMessage() {
        // Given
        String customMessage = "Custom user not found message";
        String userId = "user-123";
        
        // When
        UserNotFoundException exception = new UserNotFoundException(customMessage, userId);
        
        // Then
        assertThat(exception.getMessage()).isEqualTo(customMessage);
        assertThat(exception.getUserId()).isEqualTo(userId);
    }
}