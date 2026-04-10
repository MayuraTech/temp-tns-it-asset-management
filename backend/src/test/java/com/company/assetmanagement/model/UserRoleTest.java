package com.company.assetmanagement.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the UserRole entity.
 * 
 * Tests the business logic methods, validation, and entity behavior
 * without requiring database connectivity.
 */
@DisplayName("UserRole Entity Tests")
class UserRoleTest {

    private User user;
    private User adminUser;
    private UserRole userRole;

    @BeforeEach
    void setUp() {
        user = new User("testuser", "hashedPassword123", "test@example.com");
        user.setId(UUID.randomUUID());
        
        adminUser = new User("admin", "adminHashedPassword", "admin@example.com");
        adminUser.setId(UUID.randomUUID());
        
        userRole = new UserRole(user, Role.ASSET_MANAGER, adminUser);
        userRole.setId(UUID.randomUUID());
        userRole.setAssignedAt(LocalDateTime.now());
    }

    @Test
    @DisplayName("Should create user role with required fields")
    void shouldCreateUserRoleWithRequiredFields() {
        // Given & When
        UserRole newUserRole = new UserRole(user, Role.ADMINISTRATOR, adminUser);
        
        // Then
        assertEquals(user, newUserRole.getUser());
        assertEquals(Role.ADMINISTRATOR, newUserRole.getRole());
        assertEquals(adminUser, newUserRole.getAssignedBy());
    }

    @Test
    @DisplayName("Should identify administrator role correctly")
    void shouldIdentifyAdministratorRoleCorrectly() {
        // Given
        UserRole adminRole = new UserRole(user, Role.ADMINISTRATOR, adminUser);
        
        // When & Then
        assertTrue(adminRole.isAdministratorRole());
        assertFalse(adminRole.isAssetManagerRole());
        assertFalse(adminRole.isViewerRole());
    }

    @Test
    @DisplayName("Should identify asset manager role correctly")
    void shouldIdentifyAssetManagerRoleCorrectly() {
        // Given
        UserRole managerRole = new UserRole(user, Role.ASSET_MANAGER, adminUser);
        
        // When & Then
        assertFalse(managerRole.isAdministratorRole());
        assertTrue(managerRole.isAssetManagerRole());
        assertFalse(managerRole.isViewerRole());
    }

    @Test
    @DisplayName("Should identify viewer role correctly")
    void shouldIdentifyViewerRoleCorrectly() {
        // Given
        UserRole viewerRole = new UserRole(user, Role.VIEWER, adminUser);
        
        // When & Then
        assertFalse(viewerRole.isAdministratorRole());
        assertFalse(viewerRole.isAssetManagerRole());
        assertTrue(viewerRole.isViewerRole());
    }

    @Test
    @DisplayName("Should get role display name")
    void shouldGetRoleDisplayName() {
        // Given
        UserRole adminRole = new UserRole(user, Role.ADMINISTRATOR, adminUser);
        
        // When
        String displayName = adminRole.getRoleDisplayName();
        
        // Then
        assertEquals("Administrator", displayName);
    }

    @Test
    @DisplayName("Should return null display name when role is null")
    void shouldReturnNullDisplayNameWhenRoleIsNull() {
        // Given
        UserRole roleWithNullRole = new UserRole();
        
        // When
        String displayName = roleWithNullRole.getRoleDisplayName();
        
        // Then
        assertNull(displayName);
    }

    @Test
    @DisplayName("Should detect self-assigned role")
    void shouldDetectSelfAssignedRole() {
        // Given
        UserRole selfAssignedRole = new UserRole(user, Role.VIEWER, user);
        
        // When & Then
        assertTrue(selfAssignedRole.isSelfAssigned());
    }

    @Test
    @DisplayName("Should detect non-self-assigned role")
    void shouldDetectNonSelfAssignedRole() {
        // When & Then
        assertFalse(userRole.isSelfAssigned());
    }

    @Test
    @DisplayName("Should get user username")
    void shouldGetUserUsername() {
        // When
        String username = userRole.getUserUsername();
        
        // Then
        assertEquals("testuser", username);
    }

    @Test
    @DisplayName("Should return null username when user is null")
    void shouldReturnNullUsernameWhenUserIsNull() {
        // Given
        UserRole roleWithNullUser = new UserRole();
        
        // When
        String username = roleWithNullUser.getUserUsername();
        
        // Then
        assertNull(username);
    }

    @Test
    @DisplayName("Should get assigned by username")
    void shouldGetAssignedByUsername() {
        // When
        String assignedByUsername = userRole.getAssignedByUsername();
        
        // Then
        assertEquals("admin", assignedByUsername);
    }

    @Test
    @DisplayName("Should return null assigned by username when assignedBy is null")
    void shouldReturnNullAssignedByUsernameWhenAssignedByIsNull() {
        // Given
        UserRole roleWithNullAssignedBy = new UserRole();
        
        // When
        String assignedByUsername = roleWithNullAssignedBy.getAssignedByUsername();
        
        // Then
        assertNull(assignedByUsername);
    }

    @Test
    @DisplayName("Should be equal when user and role are same")
    void shouldBeEqualWhenUserAndRoleAreSame() {
        // Given
        UserRole role1 = new UserRole(user, Role.ADMINISTRATOR, adminUser);
        UserRole role2 = new UserRole(user, Role.ADMINISTRATOR, adminUser);
        
        // When & Then
        assertEquals(role1, role2);
        assertEquals(role1.hashCode(), role2.hashCode());
    }

    @Test
    @DisplayName("Should not be equal when users are different")
    void shouldNotBeEqualWhenUsersAreDifferent() {
        // Given
        User differentUser = new User("different", "password", "different@example.com");
        UserRole role1 = new UserRole(user, Role.ADMINISTRATOR, adminUser);
        UserRole role2 = new UserRole(differentUser, Role.ADMINISTRATOR, adminUser);
        
        // When & Then
        assertNotEquals(role1, role2);
    }

    @Test
    @DisplayName("Should not be equal when roles are different")
    void shouldNotBeEqualWhenRolesAreDifferent() {
        // Given
        UserRole role1 = new UserRole(user, Role.ADMINISTRATOR, adminUser);
        UserRole role2 = new UserRole(user, Role.VIEWER, adminUser);
        
        // When & Then
        assertNotEquals(role1, role2);
    }

    @Test
    @DisplayName("Should include key information in toString")
    void shouldIncludeKeyInformationInToString() {
        // When
        String userRoleString = userRole.toString();
        
        // Then
        assertTrue(userRoleString.contains("testuser"));
        assertTrue(userRoleString.contains("ASSET_MANAGER"));
        assertTrue(userRoleString.contains("admin"));
        assertTrue(userRoleString.contains("UserRole{"));
    }

    @Test
    @DisplayName("Should handle null values gracefully in toString")
    void shouldHandleNullValuesGracefullyInToString() {
        // Given
        UserRole roleWithNulls = new UserRole();
        
        // When
        String userRoleString = roleWithNulls.toString();
        
        // Then
        assertNotNull(userRoleString);
        assertTrue(userRoleString.contains("UserRole{"));
    }

    @Test
    @DisplayName("Should handle null values gracefully in business methods")
    void shouldHandleNullValuesGracefullyInBusinessMethods() {
        // Given
        UserRole roleWithNulls = new UserRole();
        
        // When & Then
        assertFalse(roleWithNulls.isSelfAssigned());
        assertNull(roleWithNulls.getUserUsername());
        assertNull(roleWithNulls.getAssignedByUsername());
        assertNull(roleWithNulls.getRoleDisplayName());
    }
}