package com.company.assetmanagement.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for authentication DTOs validation.
 * Tests validation annotations and constraints for LoginRequest, ChangePasswordRequest, and RefreshTokenRequest.
 */
class AuthenticationDTOTest {
    
    private Validator validator;
    
    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }
    
    @Test
    @DisplayName("LoginRequest should validate successfully with valid data")
    void loginRequest_shouldValidateSuccessfully_whenDataIsValid() {
        // Given
        LoginRequest request = new LoginRequest("testuser", "password123");
        
        // When
        Set<ConstraintViolation<LoginRequest>> violations = validator.validate(request);
        
        // Then
        assertThat(violations).isEmpty();
    }
    
    @Test
    @DisplayName("LoginRequest should fail validation when username is blank")
    void loginRequest_shouldFailValidation_whenUsernameIsBlank() {
        // Given
        LoginRequest request = new LoginRequest("", "password123");
        
        // When
        Set<ConstraintViolation<LoginRequest>> violations = validator.validate(request);
        
        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Username is required");
    }
    
    @Test
    @DisplayName("LoginRequest should fail validation when username is too short")
    void loginRequest_shouldFailValidation_whenUsernameIsTooShort() {
        // Given
        LoginRequest request = new LoginRequest("ab", "password123");
        
        // When
        Set<ConstraintViolation<LoginRequest>> violations = validator.validate(request);
        
        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Username must be between 3 and 100 characters");
    }
    
    @Test
    @DisplayName("LoginRequest should fail validation when password is blank")
    void loginRequest_shouldFailValidation_whenPasswordIsBlank() {
        // Given
        LoginRequest request = new LoginRequest("testuser", "");
        
        // When
        Set<ConstraintViolation<LoginRequest>> violations = validator.validate(request);
        
        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Password is required");
    }
    
    @Test
    @DisplayName("ChangePasswordRequest should validate successfully with valid passwords")
    void changePasswordRequest_shouldValidateSuccessfully_whenPasswordsAreValid() {
        // Given
        ChangePasswordRequest request = new ChangePasswordRequest("oldPassword123!", "NewPassword123!");
        
        // When
        Set<ConstraintViolation<ChangePasswordRequest>> violations = validator.validate(request);
        
        // Then
        assertThat(violations).isEmpty();
    }
    
    @Test
    @DisplayName("ChangePasswordRequest should fail validation when current password is blank")
    void changePasswordRequest_shouldFailValidation_whenCurrentPasswordIsBlank() {
        // Given
        ChangePasswordRequest request = new ChangePasswordRequest("", "NewPassword123!");
        
        // When
        Set<ConstraintViolation<ChangePasswordRequest>> violations = validator.validate(request);
        
        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Current password is required");
    }
    
    @Test
    @DisplayName("ChangePasswordRequest should fail validation when new password doesn't meet complexity requirements")
    void changePasswordRequest_shouldFailValidation_whenNewPasswordIsWeak() {
        // Given
        ChangePasswordRequest request = new ChangePasswordRequest("oldPassword123!", "weak");
        
        // When
        Set<ConstraintViolation<ChangePasswordRequest>> violations = validator.validate(request);
        
        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
            .isEqualTo("Password must contain at least 8 characters, one uppercase letter, one lowercase letter, one digit, and one special character (@$!%*?&)");
    }
    
    @Test
    @DisplayName("ChangePasswordRequest should fail validation when new password lacks uppercase letter")
    void changePasswordRequest_shouldFailValidation_whenNewPasswordLacksUppercase() {
        // Given
        ChangePasswordRequest request = new ChangePasswordRequest("oldPassword123!", "newpassword123!");
        
        // When
        Set<ConstraintViolation<ChangePasswordRequest>> violations = validator.validate(request);
        
        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
            .contains("Password must contain at least 8 characters, one uppercase letter, one lowercase letter, one digit, and one special character");
    }
    
    @Test
    @DisplayName("ChangePasswordRequest should fail validation when new password lacks special character")
    void changePasswordRequest_shouldFailValidation_whenNewPasswordLacksSpecialChar() {
        // Given
        ChangePasswordRequest request = new ChangePasswordRequest("oldPassword123!", "NewPassword123");
        
        // When
        Set<ConstraintViolation<ChangePasswordRequest>> violations = validator.validate(request);
        
        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
            .contains("Password must contain at least 8 characters, one uppercase letter, one lowercase letter, one digit, and one special character");
    }
    
    @Test
    @DisplayName("RefreshTokenRequest should validate successfully with valid token")
    void refreshTokenRequest_shouldValidateSuccessfully_whenTokenIsValid() {
        // Given
        RefreshTokenRequest request = new RefreshTokenRequest("valid.refresh.token");
        
        // When
        Set<ConstraintViolation<RefreshTokenRequest>> violations = validator.validate(request);
        
        // Then
        assertThat(violations).isEmpty();
    }
    
    @Test
    @DisplayName("RefreshTokenRequest should fail validation when token is blank")
    void refreshTokenRequest_shouldFailValidation_whenTokenIsBlank() {
        // Given
        RefreshTokenRequest request = new RefreshTokenRequest("");
        
        // When
        Set<ConstraintViolation<RefreshTokenRequest>> violations = validator.validate(request);
        
        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Refresh token is required");
    }
    
    @Test
    @DisplayName("TokenResponse should create successfully with all fields")
    void tokenResponse_shouldCreateSuccessfully_withAllFields() {
        // Given
        String accessToken = "access.token.here";
        String refreshToken = "refresh.token.here";
        String tokenType = "Bearer";
        Long expiresIn = 1800L;
        
        // When
        TokenResponse response = new TokenResponse(accessToken, refreshToken, tokenType, expiresIn);
        
        // Then
        assertThat(response.getAccessToken()).isEqualTo(accessToken);
        assertThat(response.getRefreshToken()).isEqualTo(refreshToken);
        assertThat(response.getTokenType()).isEqualTo(tokenType);
        assertThat(response.getExpiresIn()).isEqualTo(expiresIn);
    }
    
    @Test
    @DisplayName("TokenResponse should use default Bearer token type")
    void tokenResponse_shouldUseDefaultBearerType_whenNotSpecified() {
        // Given
        String accessToken = "access.token.here";
        String refreshToken = "refresh.token.here";
        Long expiresIn = 1800L;
        
        // When
        TokenResponse response = new TokenResponse(accessToken, refreshToken, expiresIn);
        
        // Then
        assertThat(response.getTokenType()).isEqualTo("Bearer");
    }
    
    @Test
    @DisplayName("All DTOs should protect sensitive data in toString methods")
    void allDTOs_shouldProtectSensitiveData_inToStringMethods() {
        // Given
        LoginRequest loginRequest = new LoginRequest("testuser", "password123");
        ChangePasswordRequest changePasswordRequest = new ChangePasswordRequest("old", "NewPassword123!");
        RefreshTokenRequest refreshTokenRequest = new RefreshTokenRequest("refresh.token");
        TokenResponse tokenResponse = new TokenResponse("access", "refresh", 1800L);
        
        // When & Then
        assertThat(loginRequest.toString()).contains("[PROTECTED]");
        assertThat(loginRequest.toString()).doesNotContain("password123");
        
        assertThat(changePasswordRequest.toString()).contains("[PROTECTED]");
        assertThat(changePasswordRequest.toString()).doesNotContain("old");
        assertThat(changePasswordRequest.toString()).doesNotContain("NewPassword123!");
        
        assertThat(refreshTokenRequest.toString()).contains("[PROTECTED]");
        assertThat(refreshTokenRequest.toString()).doesNotContain("refresh.token");
        
        assertThat(tokenResponse.toString()).contains("[PROTECTED]");
        assertThat(tokenResponse.toString()).doesNotContain("access");
        assertThat(tokenResponse.toString()).doesNotContain("refresh");
    }
}