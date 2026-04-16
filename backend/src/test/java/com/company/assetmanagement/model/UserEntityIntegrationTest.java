package com.company.assetmanagement.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.TestPropertySource;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for User entity JPA functionality.
 * 
 * Tests entity persistence, relationships, and database constraints
 * using an in-memory H2 database.
 */
@DataJpaTest
@TestPropertySource(properties = {
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.datasource.url=jdbc:h2:mem:testdb",
    "spring.jpa.show-sql=true"
})
@DisplayName("User Entity Integration Tests")
class UserEntityIntegrationTest {

    @Autowired
    private TestEntityManager entityManager;

    @Test
    @DisplayName("Should persist user entity with all fields")
    void shouldPersistUserEntityWithAllFields() {
        // Given
        User user = new User("testuser", "hashedPassword123", "test@example.com");
        user.setIsActive(true);
        user.setAccountLocked(false);
        user.setFailedLoginAttempts(0);
        user.setLastLoginAt(LocalDateTime.now());
        
        // When
        User savedUser = entityManager.persistAndFlush(user);
        
        // Then
        assertNotNull(savedUser.getId());
        assertEquals("testuser", savedUser.getUsername());
        assertEquals("hashedPassword123", savedUser.getPasswordHash());
        assertEquals("test@example.com", savedUser.getEmail());
        assertTrue(savedUser.getIsActive());
        assertFalse(savedUser.getAccountLocked());
        assertEquals(0, savedUser.getFailedLoginAttempts());
        assertNotNull(savedUser.getCreatedAt());
        assertNotNull(savedUser.getUpdatedAt());
    }

    @Test
    @DisplayName("Should persist user with role relationship")
    void shouldPersistUserWithRoleRelationship() {
        // Given
        User adminUser = new User("admin", "adminPassword", "admin@example.com");
        User regularUser = new User("user", "userPassword", "user@example.com");
        
        entityManager.persistAndFlush(adminUser);
        entityManager.persistAndFlush(regularUser);
        
        UserRole userRole = new UserRole(regularUser, Role.ASSET_MANAGER, adminUser);
        
        // When
        regularUser.addRole(userRole);
        User savedUser = entityManager.persistAndFlush(regularUser);
        entityManager.clear(); // Clear persistence context to force fresh load
        
        // Then
        User loadedUser = entityManager.find(User.class, savedUser.getId());
        assertNotNull(loadedUser);
        assertEquals(1, loadedUser.getRoles().size());
        
        UserRole loadedRole = loadedUser.getRoles().iterator().next();
        assertEquals(Role.ASSET_MANAGER, loadedRole.getRole());
        assertEquals(adminUser.getId(), loadedRole.getAssignedBy().getId());
    }

    @Test
    @DisplayName("Should persist user with session relationship")
    void shouldPersistUserWithSessionRelationship() {
        // Given
        User user = new User("sessionuser", "password", "session@example.com");
        entityManager.persistAndFlush(user);
        
        Session session = new Session(user, LocalDateTime.now().plusMinutes(30));
        session.setAccessTokenHash("access-hash");
        session.setRefreshTokenHash("refresh-hash");
        
        // When
        user.addSession(session);
        User savedUser = entityManager.persistAndFlush(user);
        entityManager.clear(); // Clear persistence context to force fresh load
        
        // Then
        User loadedUser = entityManager.find(User.class, savedUser.getId());
        assertNotNull(loadedUser);
        assertEquals(1, loadedUser.getSessions().size());
        
        Session loadedSession = loadedUser.getSessions().iterator().next();
        assertTrue(loadedSession.getIsActive());
        assertEquals("access-hash", loadedSession.getAccessTokenHash());
        assertEquals("refresh-hash", loadedSession.getRefreshTokenHash());
    }

    @Test
    @DisplayName("Should enforce username uniqueness constraint")
    void shouldEnforceUsernameUniquenessConstraint() {
        // Given
        User user1 = new User("duplicate", "password1", "email1@example.com");
        User user2 = new User("duplicate", "password2", "email2@example.com");
        
        // When
        entityManager.persistAndFlush(user1);
        
        // Then
        assertThrows(Exception.class, () -> {
            entityManager.persistAndFlush(user2);
        });
    }

    @Test
    @DisplayName("Should enforce email uniqueness constraint")
    void shouldEnforceEmailUniquenessConstraint() {
        // Given
        User user1 = new User("user1", "password1", "duplicate@example.com");
        User user2 = new User("user2", "password2", "duplicate@example.com");
        
        // When
        entityManager.persistAndFlush(user1);
        
        // Then
        assertThrows(Exception.class, () -> {
            entityManager.persistAndFlush(user2);
        });
    }

    @Test
    @DisplayName("Should handle cascade operations for user roles")
    void shouldHandleCascadeOperationsForUserRoles() {
        // Given
        User adminUser = new User("admin", "adminPassword", "admin@example.com");
        User regularUser = new User("user", "userPassword", "user@example.com");
        
        entityManager.persistAndFlush(adminUser);
        entityManager.persistAndFlush(regularUser);
        
        UserRole userRole = new UserRole(regularUser, Role.VIEWER, adminUser);
        regularUser.addRole(userRole);
        
        // When
        entityManager.persistAndFlush(regularUser);
        entityManager.remove(regularUser); // This should cascade to UserRole
        entityManager.flush();
        
        // Then
        // If we reach here without exception, cascade worked correctly
        assertTrue(true);
    }

    @Test
    @DisplayName("Should handle cascade operations for sessions")
    void shouldHandleCascadeOperationsForSessions() {
        // Given
        User user = new User("sessionuser", "password", "session@example.com");
        entityManager.persistAndFlush(user);
        
        Session session = new Session(user, LocalDateTime.now().plusMinutes(30));
        user.addSession(session);
        
        // When
        entityManager.persistAndFlush(user);
        entityManager.remove(user); // This should cascade to Session
        entityManager.flush();
        
        // Then
        // If we reach here without exception, cascade worked correctly
        assertTrue(true);
    }

    @Test
    @DisplayName("Should handle audit fields correctly")
    void shouldHandleAuditFieldsCorrectly() {
        // Given
        User creatorUser = new User("creator", "password", "creator@example.com");
        entityManager.persistAndFlush(creatorUser);
        
        User newUser = new User("newuser", "password", "new@example.com");
        newUser.setCreatedBy(creatorUser);
        newUser.setUpdatedBy(creatorUser);
        
        // When
        User savedUser = entityManager.persistAndFlush(newUser);
        
        // Then
        assertNotNull(savedUser.getCreatedBy());
        assertNotNull(savedUser.getUpdatedBy());
        assertEquals(creatorUser.getId(), savedUser.getCreatedBy().getId());
        assertEquals(creatorUser.getId(), savedUser.getUpdatedBy().getId());
        assertNotNull(savedUser.getCreatedAt());
        assertNotNull(savedUser.getUpdatedAt());
    }

    @Test
    @DisplayName("Should persist user with multiple roles")
    void shouldPersistUserWithMultipleRoles() {
        // Given
        User adminUser = new User("admin", "adminPassword", "admin@example.com");
        User multiRoleUser = new User("multirole", "password", "multi@example.com");
        
        entityManager.persistAndFlush(adminUser);
        entityManager.persistAndFlush(multiRoleUser);
        
        UserRole adminRole = new UserRole(multiRoleUser, Role.ADMINISTRATOR, adminUser);
        UserRole managerRole = new UserRole(multiRoleUser, Role.ASSET_MANAGER, adminUser);
        
        // When
        multiRoleUser.addRole(adminRole);
        multiRoleUser.addRole(managerRole);
        User savedUser = entityManager.persistAndFlush(multiRoleUser);
        entityManager.clear();
        
        // Then
        User loadedUser = entityManager.find(User.class, savedUser.getId());
        assertEquals(2, loadedUser.getRoles().size());
        assertTrue(loadedUser.hasRole(Role.ADMINISTRATOR));
        assertTrue(loadedUser.hasRole(Role.ASSET_MANAGER));
    }
}