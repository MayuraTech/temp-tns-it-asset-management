package com.company.assetmanagement.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the User entity.
 * 
 * Tests the business logic methods, validation, and entity behavior
 * without requiring database connectivity.
 */
@DisplayName("User Entity Tests")
class UserTest {

    private User user;
    private User adminUser;

    @BeforeEach
    void setUp() {
        user = new User("testuser", "hashedPassword123", "test@example.com");
        user.setId(UUID.randomUUID());
        
        adminUser = new User("admin", "adminHashedPassword", "admin@example.com");
        adminUser.setId(UUID.randomUUID());
    }

    @Test
    @DisplayName("Should create user with required fields")
    void shouldCreateUserWithRequiredFields() {
        // Given & When
        User newUser = new User("newuser", "password123", "new@example.com");
        
        // Then
        assertEquals("newuser", newUser.getUsername());
        assertEquals("password123", newUser.getPasswordHash());
        assertEquals("new@example.com", newUser.getEmail());
        assertTrue(newUser.getIsActive());
        assertFalse(newUser.getAccountLocked());
        assertEquals(0, newUser.getFailedLoginAttempts());
    }

    @Test
    @DisplayName("Should not be currently locked when account is not locked")
    void shouldNotBeCurrentlyLockedWhenAccountNotLocked() {
        // Given
        user.setAccountLocked(false);
        user.setLockUntil(null);
        
        // When & Then
        assertFalse(user.isCurrentlyLocked());
    }

    @Test
    @DisplayName("Should be currently locked when lock time is in future")
    void shouldBeCurrentlyLockedWhenLockTimeInFuture() {
        // Given
        user.setAccountLocked(true);
        user.setLockUntil(LocalDateTime.now().plusMinutes(10));
        
        // When & Then
        assertTrue(user.isCurrentlyLocked());
    }

    @Test
    @DisplayName("Should not be currently locked when lock time has passed")
    void shouldNotBeCurrentlyLockedWhenLockTimeHasPassed() {
        // Given
        user.setAccountLocked(true);
        user.setLockUntil(LocalDateTime.now().minusMinutes(10));
        
        // When & Then
        assertFalse(user.isCurrentlyLocked());
    }

    @Test
    @DisplayName("Should lock account for specified duration")
    void shouldLockAccountForSpecifiedDuration() {
        // Given
        LocalDateTime beforeLock = LocalDateTime.now();
        
        // When
        user.lockAccount(30);
        
        // Then
        assertTrue(user.getAccountLocked());
        assertNotNull(user.getLockUntil());
        assertTrue(user.getLockUntil().isAfter(beforeLock.plusMinutes(29)));
        assertTrue(user.getLockUntil().isBefore(beforeLock.plusMinutes(31)));
    }

    @Test
    @DisplayName("Should unlock account and reset failed attempts")
    void shouldUnlockAccountAndResetFailedAttempts() {
        // Given
        user.setAccountLocked(true);
        user.setLockUntil(LocalDateTime.now().plusMinutes(10));
        user.setFailedLoginAttempts(5);
        
        // When
        user.unlockAccount();
        
        // Then
        assertFalse(user.getAccountLocked());
        assertNull(user.getLockUntil());
        assertEquals(0, user.getFailedLoginAttempts());
    }

    @Test
    @DisplayName("Should increment failed login attempts")
    void shouldIncrementFailedLoginAttempts() {
        // Given
        user.setFailedLoginAttempts(2);
        
        // When
        user.incrementFailedLoginAttempts();
        
        // Then
        assertEquals(3, user.getFailedLoginAttempts());
        assertFalse(user.getAccountLocked()); // Should not lock yet
    }

    @Test
    @DisplayName("Should lock account when failed attempts reach 5")
    void shouldLockAccountWhenFailedAttemptsReachFive() {
        // Given
        user.setFailedLoginAttempts(4);
        
        // When
        user.incrementFailedLoginAttempts();
        
        // Then
        assertEquals(5, user.getFailedLoginAttempts());
        assertTrue(user.getAccountLocked());
        assertNotNull(user.getLockUntil());
    }

    @Test
    @DisplayName("Should reset failed login attempts")
    void shouldResetFailedLoginAttempts() {
        // Given
        user.setFailedLoginAttempts(3);
        
        // When
        user.resetFailedLoginAttempts();
        
        // Then
        assertEquals(0, user.getFailedLoginAttempts());
    }

    @Test
    @DisplayName("Should update last login timestamp")
    void shouldUpdateLastLoginTimestamp() {
        // Given
        LocalDateTime beforeUpdate = LocalDateTime.now();
        
        // When
        user.updateLastLogin();
        
        // Then
        assertNotNull(user.getLastLoginAt());
        assertTrue(user.getLastLoginAt().isAfter(beforeUpdate.minusSeconds(1)));
        assertTrue(user.getLastLoginAt().isBefore(LocalDateTime.now().plusSeconds(1)));
    }

    @Test
    @DisplayName("Should check if user has specific role")
    void shouldCheckIfUserHasSpecificRole() {
        // Given
        UserRole adminRole = new UserRole(user, Role.ADMINISTRATOR, adminUser);
        user.addRole(adminRole);
        
        // When & Then
        assertTrue(user.hasRole(Role.ADMINISTRATOR));
        assertFalse(user.hasRole(Role.VIEWER));
    }

    @Test
    @DisplayName("Should get all role names")
    void shouldGetAllRoleNames() {
        // Given
        UserRole adminRole = new UserRole(user, Role.ADMINISTRATOR, adminUser);
        UserRole managerRole = new UserRole(user, Role.ASSET_MANAGER, adminUser);
        user.addRole(adminRole);
        user.addRole(managerRole);
        
        // When
        var roleNames = user.getRoleNames();
        
        // Then
        assertEquals(2, roleNames.size());
        assertTrue(roleNames.contains(Role.ADMINISTRATOR));
        assertTrue(roleNames.contains(Role.ASSET_MANAGER));
    }

    @Test
    @DisplayName("Should add role to user")
    void shouldAddRoleToUser() {
        // Given
        UserRole viewerRole = new UserRole(user, Role.VIEWER, adminUser);
        
        // When
        user.addRole(viewerRole);
        
        // Then
        assertTrue(user.getRoles().contains(viewerRole));
        assertEquals(user, viewerRole.getUser());
    }

    @Test
    @DisplayName("Should remove role from user")
    void shouldRemoveRoleFromUser() {
        // Given
        UserRole viewerRole = new UserRole(user, Role.VIEWER, adminUser);
        user.addRole(viewerRole);
        
        // When
        user.removeRole(viewerRole);
        
        // Then
        assertFalse(user.getRoles().contains(viewerRole));
        assertNull(viewerRole.getUser());
    }

    @Test
    @DisplayName("Should add session to user")
    void shouldAddSessionToUser() {
        // Given
        Session session = new Session(user, LocalDateTime.now().plusMinutes(30));
        
        // When
        user.addSession(session);
        
        // Then
        assertTrue(user.getSessions().contains(session));
        assertEquals(user, session.getUser());
    }

    @Test
    @DisplayName("Should remove session from user")
    void shouldRemoveSessionFromUser() {
        // Given
        Session session = new Session(user, LocalDateTime.now().plusMinutes(30));
        user.addSession(session);
        
        // When
        user.removeSession(session);
        
        // Then
        assertFalse(user.getSessions().contains(session));
        assertNull(session.getUser());
    }

    @Test
    @DisplayName("Should be equal when usernames are same")
    void shouldBeEqualWhenUsernamesAreSame() {
        // Given
        User user1 = new User("sameuser", "password1", "email1@example.com");
        User user2 = new User("sameuser", "password2", "email2@example.com");
        
        // When & Then
        assertEquals(user1, user2);
        assertEquals(user1.hashCode(), user2.hashCode());
    }

    @Test
    @DisplayName("Should not be equal when usernames are different")
    void shouldNotBeEqualWhenUsernamesAreDifferent() {
        // Given
        User user1 = new User("user1", "password", "email@example.com");
        User user2 = new User("user2", "password", "email@example.com");
        
        // When & Then
        assertNotEquals(user1, user2);
    }

    @Test
    @DisplayName("Should exclude password hash from toString")
    void shouldExcludePasswordHashFromToString() {
        // When
        String userString = user.toString();
        
        // Then
        assertFalse(userString.contains("hashedPassword123"));
        assertTrue(userString.contains("testuser"));
        assertTrue(userString.contains("test@example.com"));
    }

    @Test
    @DisplayName("Should handle null values gracefully")
    void shouldHandleNullValuesGracefully() {
        // Given
        User userWithNulls = new User();
        
        // When & Then
        assertFalse(userWithNulls.isCurrentlyLocked());
        assertFalse(userWithNulls.hasRole(Role.ADMINISTRATOR));
        assertTrue(userWithNulls.getRoleNames().isEmpty());
        assertNotNull(userWithNulls.toString()); // Should not throw exception
    }
}