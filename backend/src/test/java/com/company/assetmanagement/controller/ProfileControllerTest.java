package com.company.assetmanagement.controller;

import com.company.assetmanagement.dto.ChangePasswordRequest;
import com.company.assetmanagement.dto.ProfileUpdateRequest;
import com.company.assetmanagement.dto.UserDTO;
import com.company.assetmanagement.exception.DuplicateEmailException;
import com.company.assetmanagement.exception.UserNotFoundException;
import com.company.assetmanagement.exception.ValidationException;
import com.company.assetmanagement.model.Role;
import com.company.assetmanagement.service.ProfileService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProfileControllerTest {
    
    @Mock
    private ProfileService profileService;
    
    @Mock
    private SecurityContext securityContext;
    
    @InjectMocks
    private ProfileController profileController;
    
    private String testUserId;
    private UserDTO testUserDTO;
    
    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID().toString();
        
        testUserDTO = new UserDTO();
        testUserDTO.setId(UUID.fromString(testUserId));
        testUserDTO.setUsername("testuser");
        testUserDTO.setEmail("test@example.com");
        testUserDTO.setIsActive(true);
        testUserDTO.setAccountLocked(false);
        testUserDTO.setRoles(Set.of(Role.VIEWER));
        testUserDTO.setCreatedAt(LocalDateTime.now());
        testUserDTO.setUpdatedAt(LocalDateTime.now());
        
        // Setup security context
        SecurityContextHolder.setContext(securityContext);
    }
    
    // ========================================================================
    // GET /api/v1/profile Tests
    // ========================================================================
    
    @Test
    @DisplayName("Should return user profile when authenticated")
    void shouldReturnUserProfileWhenAuthenticated() {
        // Given
        setupAuthenticatedUser(testUserId);
        when(profileService.getProfile(testUserId)).thenReturn(testUserDTO);
        
        // When
        ResponseEntity<UserDTO> response = profileController.getProfile();
        
        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getId().toString()).isEqualTo(testUserId);
        assertThat(response.getBody().getUsername()).isEqualTo("testuser");
        assertThat(response.getBody().getEmail()).isEqualTo("test@example.com");
        
        verify(profileService).getProfile(testUserId);
    }
    
    @Test
    @DisplayName("Should throw exception when user not found")
    void shouldThrowExceptionWhenUserNotFound() {
        // Given
        setupAuthenticatedUser(testUserId);
        when(profileService.getProfile(testUserId))
            .thenThrow(new UserNotFoundException(testUserId));
        
        // When/Then
        assertThatThrownBy(() -> profileController.getProfile())
            .isInstanceOf(UserNotFoundException.class)
            .hasMessageContaining(testUserId);
        
        verify(profileService).getProfile(testUserId);
    }
    
    @Test
    @DisplayName("Should throw exception when no authenticated user")
    void shouldThrowExceptionWhenNoAuthenticatedUser() {
        // Given
        when(securityContext.getAuthentication()).thenReturn(null);
        
        // When/Then
        assertThatThrownBy(() -> profileController.getProfile())
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("No authenticated user found");
        
        verify(profileService, never()).getProfile(any());
    }
    
    // ========================================================================
    // PUT /api/v1/profile Tests
    // ========================================================================
    
    @Test
    @DisplayName("Should update profile with valid email")
    void shouldUpdateProfileWithValidEmail() {
        // Given
        setupAuthenticatedUser(testUserId);
        ProfileUpdateRequest request = new ProfileUpdateRequest("newemail@example.com");
        
        UserDTO updatedUser = new UserDTO();
        updatedUser.setId(UUID.fromString(testUserId));
        updatedUser.setUsername("testuser");
        updatedUser.setEmail("newemail@example.com");
        updatedUser.setIsActive(true);
        updatedUser.setAccountLocked(false);
        updatedUser.setRoles(Set.of(Role.VIEWER));
        
        when(profileService.updateProfile(eq(testUserId), any(ProfileUpdateRequest.class)))
            .thenReturn(updatedUser);
        
        // When
        ResponseEntity<UserDTO> response = profileController.updateProfile(request);
        
        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getEmail()).isEqualTo("newemail@example.com");
        
        verify(profileService).updateProfile(eq(testUserId), any(ProfileUpdateRequest.class));
    }
    
    @Test
    @DisplayName("Should throw exception when email already exists")
    void shouldThrowExceptionWhenEmailAlreadyExists() {
        // Given
        setupAuthenticatedUser(testUserId);
        ProfileUpdateRequest request = new ProfileUpdateRequest("existing@example.com");
        
        when(profileService.updateProfile(eq(testUserId), any(ProfileUpdateRequest.class)))
            .thenThrow(new DuplicateEmailException("existing@example.com"));
        
        // When/Then
        assertThatThrownBy(() -> profileController.updateProfile(request))
            .isInstanceOf(DuplicateEmailException.class)
            .hasMessageContaining("existing@example.com");
        
        verify(profileService).updateProfile(eq(testUserId), any(ProfileUpdateRequest.class));
    }
    
    @Test
    @DisplayName("Should throw exception when email format is invalid")
    void shouldThrowExceptionWhenEmailFormatInvalid() {
        // Given
        setupAuthenticatedUser(testUserId);
        ProfileUpdateRequest request = new ProfileUpdateRequest("invalid-email");
        
        when(profileService.updateProfile(eq(testUserId), any(ProfileUpdateRequest.class)))
            .thenThrow(new ValidationException("Invalid email format"));
        
        // When/Then
        assertThatThrownBy(() -> profileController.updateProfile(request))
            .isInstanceOf(ValidationException.class)
            .hasMessageContaining("Invalid email format");
        
        verify(profileService).updateProfile(eq(testUserId), any(ProfileUpdateRequest.class));
    }
    
    // ========================================================================
    // POST /api/v1/profile/change-password Tests
    // ========================================================================
    
    @Test
    @DisplayName("Should change password with valid request")
    void shouldChangePasswordWithValidRequest() {
        // Given
        setupAuthenticatedUser(testUserId);
        ChangePasswordRequest request = new ChangePasswordRequest(
            "OldPassword123!",
            "NewPassword123!"
        );
        
        doNothing().when(profileService).changePassword(eq(testUserId), any(ChangePasswordRequest.class));
        
        // When
        ResponseEntity<Void> response = profileController.changePassword(request);
        
        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(response.getBody()).isNull();
        
        verify(profileService).changePassword(eq(testUserId), any(ChangePasswordRequest.class));
    }
    
    @Test
    @DisplayName("Should throw exception when current password is incorrect")
    void shouldThrowExceptionWhenCurrentPasswordIncorrect() {
        // Given
        setupAuthenticatedUser(testUserId);
        ChangePasswordRequest request = new ChangePasswordRequest(
            "WrongPassword123!",
            "NewPassword123!"
        );
        
        doThrow(new ValidationException("Current password is incorrect"))
            .when(profileService).changePassword(eq(testUserId), any(ChangePasswordRequest.class));
        
        // When/Then
        assertThatThrownBy(() -> profileController.changePassword(request))
            .isInstanceOf(ValidationException.class)
            .hasMessageContaining("Current password is incorrect");
        
        verify(profileService).changePassword(eq(testUserId), any(ChangePasswordRequest.class));
    }
    
    @Test
    @DisplayName("Should throw exception when new password is same as current")
    void shouldThrowExceptionWhenNewPasswordSameAsCurrent() {
        // Given
        setupAuthenticatedUser(testUserId);
        ChangePasswordRequest request = new ChangePasswordRequest(
            "SamePassword123!",
            "SamePassword123!"
        );
        
        doThrow(new ValidationException("New password cannot be the same as current password"))
            .when(profileService).changePassword(eq(testUserId), any(ChangePasswordRequest.class));
        
        // When/Then
        assertThatThrownBy(() -> profileController.changePassword(request))
            .isInstanceOf(ValidationException.class)
            .hasMessageContaining("New password cannot be the same as current password");
        
        verify(profileService).changePassword(eq(testUserId), any(ChangePasswordRequest.class));
    }
    
    @Test
    @DisplayName("Should throw exception when new password does not meet complexity requirements")
    void shouldThrowExceptionWhenNewPasswordDoesNotMeetComplexity() {
        // Given
        setupAuthenticatedUser(testUserId);
        ChangePasswordRequest request = new ChangePasswordRequest(
            "OldPassword123!",
            "weak"
        );
        
        doThrow(new ValidationException("Password must contain at least 8 characters, one uppercase letter, one lowercase letter, one digit, and one special character"))
            .when(profileService).changePassword(eq(testUserId), any(ChangePasswordRequest.class));
        
        // When/Then
        assertThatThrownBy(() -> profileController.changePassword(request))
            .isInstanceOf(ValidationException.class)
            .hasMessageContaining("Password must contain at least 8 characters");
        
        verify(profileService).changePassword(eq(testUserId), any(ChangePasswordRequest.class));
    }
    
    // ========================================================================
    // Helper Methods
    // ========================================================================
    
    /**
     * Sets up an authenticated user in the security context.
     *
     * @param userId the user ID to authenticate
     */
    private void setupAuthenticatedUser(String userId) {
        UserDetails userDetails = User.builder()
            .username(userId)
            .password("password")
            .authorities(Collections.emptyList())
            .build();
        
        Authentication authentication = new UsernamePasswordAuthenticationToken(
            userDetails,
            null,
            userDetails.getAuthorities()
        );
        
        when(securityContext.getAuthentication()).thenReturn(authentication);
    }
}
