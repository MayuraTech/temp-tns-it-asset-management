package com.company.assetmanagement.service;

import com.company.assetmanagement.exception.AccountDisabledException;
import com.company.assetmanagement.exception.AccountLockedException;
import com.company.assetmanagement.exception.UserNotFoundException;
import com.company.assetmanagement.model.Action;
import com.company.assetmanagement.model.Role;
import com.company.assetmanagement.model.User;
import com.company.assetmanagement.model.UserRole;
import com.company.assetmanagement.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AuthorizationServiceImpl.
 * 
 * Tests cover:
 * - Permission checking for different roles
 * - Role verification
 * - Account status validation
 * - Administrator permission completeness (Property 17)
 * - Account locking and unlocking behavior
 * - Error handling for invalid inputs
 */
@ExtendWith(MockitoExtension.class)
class AuthorizationServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AuthorizationServiceImpl authorizationService;

    private User testUser;
    private UUID testUserId;
    private User adminUser;

    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID();
        
        // Create test user with Asset Manager role
        testUser = new User("testuser", "hashedPassword", "test@example.com");
        testUser.setId(testUserId);
        testUser.setIsActive(true);
        testUser.setAccountLocked(false);
        
        UserRole assetManagerRole = new UserRole(testUser, Role.ASSET_MANAGER, testUser);
        testUser.addRole(assetManagerRole);

        // Create admin user
        adminUser = new User("admin", "hashedPassword", "admin@example.com");
        adminUser.setId(UUID.randomUUID());
        adminUser.setIsActive(true);
        adminUser.setAccountLocked(false);
        
        UserRole adminRole = new UserRole(adminUser, Role.ADMINISTRATOR, adminUser);
        adminUser.addRole(adminRole);
    }

    // ========================================================================
    // hasPermission() Tests
    // ========================================================================

    @Test
    @DisplayName("Should grant permission when user has required role")
    void shouldGrantPermissionWhenUserHasRequiredRole() {
        // Given
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));

        // When
        boolean hasPermission = authorizationService.hasPermission(
            testUserId.toString(), 
            Action.CREATE_ASSET
        );

        // Then
        assertThat(hasPermission).isTrue();
        verify(userRepository).findById(testUserId);
    }

    @Test
    @DisplayName("Should deny permission when user lacks required role")
    void shouldDenyPermissionWhenUserLacksRequiredRole() {
        // Given
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));

        // When
        boolean hasPermission = authorizationService.hasPermission(
            testUserId.toString(), 
            Action.MANAGE_USERS
        );

        // Then
        assertThat(hasPermission).isFalse();
        verify(userRepository).findById(testUserId);
    }

    @Test
    @DisplayName("Should grant all permissions to Administrator (Property 17)")
    void shouldGrantAllPermissionsToAdministrator() {
        // Given
        when(userRepository.findById(adminUser.getId())).thenReturn(Optional.of(adminUser));

        // When & Then - Test all actions
        for (Action action : Action.values()) {
            boolean hasPermission = authorizationService.hasPermission(
                adminUser.getId().toString(), 
                action
            );
            assertThat(hasPermission)
                .as("Administrator should have permission for action: " + action)
                .isTrue();
        }
    }

    @Test
    @DisplayName("Should throw UserNotFoundException when user does not exist")
    void shouldThrowUserNotFoundExceptionWhenUserDoesNotExist() {
        // Given
        UUID nonExistentUserId = UUID.randomUUID();
        when(userRepository.findById(nonExistentUserId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> authorizationService.hasPermission(
            nonExistentUserId.toString(), 
            Action.VIEW_ASSET
        ))
            .isInstanceOf(UserNotFoundException.class)
            .hasMessageContaining(nonExistentUserId.toString());
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when userId is null")
    void shouldThrowIllegalArgumentExceptionWhenUserIdIsNull() {
        // When & Then
        assertThatThrownBy(() -> authorizationService.hasPermission(null, Action.VIEW_ASSET))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("User ID must not be null");
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when action is null")
    void shouldThrowIllegalArgumentExceptionWhenActionIsNull() {
        // When & Then
        assertThatThrownBy(() -> authorizationService.hasPermission(testUserId.toString(), null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Action must not be null");
    }

    @Test
    @DisplayName("Should throw AccountDisabledException when account is inactive")
    void shouldThrowAccountDisabledExceptionWhenAccountIsInactive() {
        // Given
        testUser.setIsActive(false);
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));

        // When & Then
        assertThatThrownBy(() -> authorizationService.hasPermission(
            testUserId.toString(), 
            Action.VIEW_ASSET
        ))
            .isInstanceOf(AccountDisabledException.class);
    }

    @Test
    @DisplayName("Should throw AccountLockedException when account is locked")
    void shouldThrowAccountLockedExceptionWhenAccountIsLocked() {
        // Given
        testUser.lockAccount(30);
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));

        // When & Then
        assertThatThrownBy(() -> authorizationService.hasPermission(
            testUserId.toString(), 
            Action.VIEW_ASSET
        ))
            .isInstanceOf(AccountLockedException.class);
    }

    // ========================================================================
    // hasRole() Tests
    // ========================================================================

    @Test
    @DisplayName("Should return true when user has the specified role")
    void shouldReturnTrueWhenUserHasSpecifiedRole() {
        // Given
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));

        // When
        boolean hasRole = authorizationService.hasRole(
            testUserId.toString(), 
            Role.ASSET_MANAGER
        );

        // Then
        assertThat(hasRole).isTrue();
    }

    @Test
    @DisplayName("Should return false when user does not have the specified role")
    void shouldReturnFalseWhenUserDoesNotHaveSpecifiedRole() {
        // Given
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));

        // When
        boolean hasRole = authorizationService.hasRole(
            testUserId.toString(), 
            Role.ADMINISTRATOR
        );

        // Then
        assertThat(hasRole).isFalse();
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when role is null")
    void shouldThrowIllegalArgumentExceptionWhenRoleIsNull() {
        // When & Then
        assertThatThrownBy(() -> authorizationService.hasRole(testUserId.toString(), null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Role must not be null");
    }

    // ========================================================================
    // validateAccountStatus() Tests
    // ========================================================================

    @Test
    @DisplayName("Should pass validation when account is active and not locked")
    void shouldPassValidationWhenAccountIsActiveAndNotLocked() {
        // Given
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));

        // When & Then
        assertThatCode(() -> authorizationService.validateAccountStatus(testUserId.toString()))
            .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Should throw AccountDisabledException when account is inactive")
    void shouldThrowAccountDisabledExceptionWhenValidatingInactiveAccount() {
        // Given
        testUser.setIsActive(false);
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));

        // When & Then
        assertThatThrownBy(() -> authorizationService.validateAccountStatus(testUserId.toString()))
            .isInstanceOf(AccountDisabledException.class);
    }

    @Test
    @DisplayName("Should throw AccountLockedException when account is currently locked")
    void shouldThrowAccountLockedExceptionWhenAccountIsCurrentlyLocked() {
        // Given
        testUser.lockAccount(30);
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));

        // When & Then
        assertThatThrownBy(() -> authorizationService.validateAccountStatus(testUserId.toString()))
            .isInstanceOf(AccountLockedException.class)
            .hasMessageContaining("Account is locked until");
    }

    @Test
    @DisplayName("Should automatically unlock account when lock has expired")
    void shouldAutomaticallyUnlockAccountWhenLockHasExpired() {
        // Given
        testUser.setAccountLocked(true);
        testUser.setLockUntil(LocalDateTime.now().minusMinutes(1)); // Lock expired 1 minute ago
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When
        authorizationService.validateAccountStatus(testUserId.toString());

        // Then
        assertThat(testUser.getAccountLocked()).isFalse();
        assertThat(testUser.getLockUntil()).isNull();
        assertThat(testUser.getFailedLoginAttempts()).isZero();
        verify(userRepository).save(testUser);
    }

    @Test
    @DisplayName("Should throw UserNotFoundException when user does not exist")
    void shouldThrowUserNotFoundExceptionWhenValidatingNonExistentUser() {
        // Given
        UUID nonExistentUserId = UUID.randomUUID();
        when(userRepository.findById(nonExistentUserId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> authorizationService.validateAccountStatus(
            nonExistentUserId.toString()
        ))
            .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when userId is null in validateAccountStatus")
    void shouldThrowIllegalArgumentExceptionWhenUserIdIsNullInValidateAccountStatus() {
        // When & Then
        assertThatThrownBy(() -> authorizationService.validateAccountStatus(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("User ID must not be null");
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when userId is blank")
    void shouldThrowIllegalArgumentExceptionWhenUserIdIsBlank() {
        // When & Then
        assertThatThrownBy(() -> authorizationService.validateAccountStatus("   "))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("User ID must not be null");
    }

    // ========================================================================
    // Role Permission Mapping Tests
    // ========================================================================

    @Test
    @DisplayName("Asset Manager should have asset operation permissions")
    void assetManagerShouldHaveAssetOperationPermissions() {
        // Given
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));

        // When & Then
        assertThat(authorizationService.hasPermission(testUserId.toString(), Action.CREATE_ASSET)).isTrue();
        assertThat(authorizationService.hasPermission(testUserId.toString(), Action.UPDATE_ASSET)).isTrue();
        assertThat(authorizationService.hasPermission(testUserId.toString(), Action.DELETE_ASSET)).isTrue();
        assertThat(authorizationService.hasPermission(testUserId.toString(), Action.VIEW_ASSET)).isTrue();
    }

    @Test
    @DisplayName("Asset Manager should have ticket management permissions")
    void assetManagerShouldHaveTicketManagementPermissions() {
        // Given
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));

        // When & Then
        assertThat(authorizationService.hasPermission(testUserId.toString(), Action.CREATE_TICKET)).isTrue();
        assertThat(authorizationService.hasPermission(testUserId.toString(), Action.APPROVE_TICKET)).isTrue();
        assertThat(authorizationService.hasPermission(testUserId.toString(), Action.REJECT_TICKET)).isTrue();
        assertThat(authorizationService.hasPermission(testUserId.toString(), Action.COMPLETE_TICKET)).isTrue();
        assertThat(authorizationService.hasPermission(testUserId.toString(), Action.VIEW_TICKET)).isTrue();
    }

    @Test
    @DisplayName("Asset Manager should NOT have user management permissions")
    void assetManagerShouldNotHaveUserManagementPermissions() {
        // Given
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));

        // When & Then
        assertThat(authorizationService.hasPermission(testUserId.toString(), Action.MANAGE_USERS)).isFalse();
        assertThat(authorizationService.hasPermission(testUserId.toString(), Action.CONFIGURE_SYSTEM)).isFalse();
    }

    @Test
    @DisplayName("Viewer should only have read permissions")
    void viewerShouldOnlyHaveReadPermissions() {
        // Given
        User viewerUser = new User("viewer", "hashedPassword", "viewer@example.com");
        viewerUser.setId(UUID.randomUUID());
        viewerUser.setIsActive(true);
        viewerUser.setAccountLocked(false);
        
        UserRole viewerRole = new UserRole(viewerUser, Role.VIEWER, adminUser);
        viewerUser.addRole(viewerRole);
        
        when(userRepository.findById(viewerUser.getId())).thenReturn(Optional.of(viewerUser));

        // When & Then
        assertThat(authorizationService.hasPermission(viewerUser.getId().toString(), Action.VIEW_ASSET)).isTrue();
        assertThat(authorizationService.hasPermission(viewerUser.getId().toString(), Action.VIEW_TICKET)).isTrue();
        assertThat(authorizationService.hasPermission(viewerUser.getId().toString(), Action.CREATE_ASSET)).isFalse();
        assertThat(authorizationService.hasPermission(viewerUser.getId().toString(), Action.UPDATE_ASSET)).isFalse();
        assertThat(authorizationService.hasPermission(viewerUser.getId().toString(), Action.DELETE_ASSET)).isFalse();
    }
}
