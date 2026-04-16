package com.company.assetmanagement.repository;

import com.company.assetmanagement.model.Role;
import com.company.assetmanagement.model.Session;
import com.company.assetmanagement.model.User;
import com.company.assetmanagement.model.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.TestPropertySource;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for SessionRepository JPA functionality.
 * 
 * Tests repository operations, database constraints, and entity relationships
 * using an in-memory H2 database to verify session management functionality.
 */
@DataJpaTest
@TestPropertySource(properties = {
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.datasource.url=jdbc:h2:mem:testdb",
    "spring.jpa.show-sql=true"
})
@DisplayName("SessionRepository Integration Tests")
class SessionRepositoryIntegrationTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private SessionRepository sessionRepository;

    private User testUser;
    private Session testSession;

    @BeforeEach
    void setUp() {
        // Create and persist test user
        testUser = new User("testuser", "$2a$10$hashedpassword", "test@example.com");
        testUser.setIsActive(true);
        testUser.setAccountLocked(false);
        testUser.setFailedLoginAttempts(0);
        
        testUser = entityManager.persistAndFlush(testUser);

        // Create user role
        UserRole userRole = new UserRole(testUser, Role.VIEWER, testUser);
        entityManager.persistAndFlush(userRole);

        // Create and persist test session
        LocalDateTime expiration = LocalDateTime.now().plusMinutes(30);
        testSession = new Session(testUser, expiration);
        testSession.setAccessTokenHash("test_access_hash");
        testSession.setRefreshTokenHash("test_refresh_hash");
        
        testSession = entityManager.persistAndFlush(testSession);
        entityManager.clear();
    }

    @Test
    @DisplayName("Should persist session entity with all fields")
    void shouldPersistSessionEntityWithAllFields() {
        // Given
        LocalDateTime expiration = LocalDateTime.now().plusHours(1);
        Session session = new Session(testUser, expiration);
        session.setAccessTokenHash("new_access_hash");
        session.setRefreshTokenHash("new_refresh_hash");
        
        // When
        Session savedSession = sessionRepository.save(session);
        
        // Then
        assertNotNull(savedSession.getId());
        assertEquals(testUser.getId(), savedSession.getUserId());
        assertTrue(savedSession.getIsActive());
        assertNotNull(savedSession.getLoginAt());
        assertEquals(expiration, savedSession.getTokenExpiration());
        assertEquals("new_access_hash", savedSession.getAccessTokenHash());
        assertEquals("new_refresh_hash", savedSession.getRefreshTokenHash());
        assertNull(savedSession.getLogoutAt());
    }

    @Test
    @DisplayName("Should find session by user")
    void shouldFindSessionByUser() {
        // When
        List<Session> sessions = sessionRepository.findByUser(testUser);
        
        // Then
        assertEquals(1, sessions.size());
        assertEquals(testSession.getId(), sessions.get(0).getId());
        assertEquals(testUser.getId(), sessions.get(0).getUserId());
    }

    @Test
    @DisplayName("Should find active sessions by user")
    void shouldFindActiveSessionsByUser() {
        // When
        List<Session> activeSessions = sessionRepository.findByUserAndIsActiveTrue(testUser);
        
        // Then
        assertEquals(1, activeSessions.size());
        assertTrue(activeSessions.get(0).getIsActive());
        assertEquals(testUser.getId(), activeSessions.get(0).getUserId());
    }

    @Test
    @DisplayName("Should find session by access token hash")
    void shouldFindSessionByAccessTokenHash() {
        // When
        Optional<Session> session = sessionRepository.findByAccessTokenHash("test_access_hash");
        
        // Then
        assertTrue(session.isPresent());
        assertEquals(testSession.getId(), session.get().getId());
        assertEquals("test_access_hash", session.get().getAccessTokenHash());
    }

    @Test
    @DisplayName("Should find session by refresh token hash")
    void shouldFindSessionByRefreshTokenHash() {
        // When
        Optional<Session> session = sessionRepository.findByRefreshTokenHash("test_refresh_hash");
        
        // Then
        assertTrue(session.isPresent());
        assertEquals(testSession.getId(), session.get().getId());
        assertEquals("test_refresh_hash", session.get().getRefreshTokenHash());
    }

    @Test
    @DisplayName("Should count active sessions by user")
    void shouldCountActiveSessionsByUser() {
        // When
        long count = sessionRepository.countByUserAndIsActiveTrue(testUser);
        
        // Then
        assertEquals(1, count);
    }

    @Test
    @DisplayName("Should invalidate user sessions")
    void shouldInvalidateUserSessions() {
        // Given
        LocalDateTime logoutTime = LocalDateTime.now();
        
        // When
        int invalidatedCount = sessionRepository.invalidateAllUserSessions(testUser, logoutTime);
        
        // Then
        assertEquals(1, invalidatedCount);
        
        // Verify session is invalidated
        entityManager.clear();
        Session updatedSession = entityManager.find(Session.class, testSession.getId());
        assertFalse(updatedSession.getIsActive());
        assertNotNull(updatedSession.getLogoutAt());
    }

    @Test
    @DisplayName("Should find expired active sessions")
    void shouldFindExpiredActiveSessions() {
        // Given - Create an expired session
        LocalDateTime pastExpiration = LocalDateTime.now().minusMinutes(10);
        Session expiredSession = new Session(testUser, pastExpiration);
        expiredSession.setAccessTokenHash("expired_access_hash");
        expiredSession.setRefreshTokenHash("expired_refresh_hash");
        entityManager.persistAndFlush(expiredSession);
        entityManager.clear();
        
        LocalDateTime currentTime = LocalDateTime.now();
        
        // When
        List<Session> expiredSessions = sessionRepository.findExpiredActiveSessions(currentTime);
        
        // Then
        assertEquals(1, expiredSessions.size());
        assertTrue(expiredSessions.get(0).getIsActive());
        assertTrue(expiredSessions.get(0).isExpired());
    }

    @Test
    @DisplayName("Should mark expired sessions as inactive")
    void shouldMarkExpiredSessionsAsInactive() {
        // Given - Create an expired session
        LocalDateTime pastExpiration = LocalDateTime.now().minusMinutes(10);
        Session expiredSession = new Session(testUser, pastExpiration);
        expiredSession.setAccessTokenHash("expired_access_hash_2");
        expiredSession.setRefreshTokenHash("expired_refresh_hash_2");
        entityManager.persistAndFlush(expiredSession);
        entityManager.clear();
        
        LocalDateTime currentTime = LocalDateTime.now();
        
        // When
        int markedCount = sessionRepository.markExpiredSessionsInactive(currentTime);
        
        // Then
        assertEquals(1, markedCount);
        
        // Verify session is marked inactive
        entityManager.clear();
        Session updatedSession = entityManager.find(Session.class, expiredSession.getId());
        assertFalse(updatedSession.getIsActive());
        assertNull(updatedSession.getLogoutAt()); // Should not set logout time for expired sessions
    }

    @Test
    @DisplayName("Should update session tokens")
    void shouldUpdateSessionTokens() {
        // Given
        String newAccessHash = "updated_access_hash";
        String newRefreshHash = "updated_refresh_hash";
        LocalDateTime newExpiration = LocalDateTime.now().plusHours(1);
        
        // When
        int updatedCount = sessionRepository.updateSessionTokens(
            testSession.getId(), newAccessHash, newRefreshHash, newExpiration);
        
        // Then
        assertEquals(1, updatedCount);
        
        // Verify update
        entityManager.clear();
        Session updatedSession = entityManager.find(Session.class, testSession.getId());
        assertEquals(newAccessHash, updatedSession.getAccessTokenHash());
        assertEquals(newRefreshHash, updatedSession.getRefreshTokenHash());
        assertEquals(newExpiration, updatedSession.getTokenExpiration());
    }

    @Test
    @DisplayName("Should cascade delete sessions when user is deleted")
    void shouldCascadeDeleteSessionsWhenUserDeleted() {
        // Given
        long sessionCountBefore = sessionRepository.count();
        
        // When
        entityManager.remove(entityManager.merge(testUser));
        entityManager.flush();
        
        // Then
        long sessionCountAfter = sessionRepository.count();
        assertEquals(sessionCountBefore - 1, sessionCountAfter);
        
        // Verify session is deleted
        Optional<Session> deletedSession = sessionRepository.findById(testSession.getId());
        assertFalse(deletedSession.isPresent());
    }

    @Test
    @DisplayName("Should handle session business logic methods correctly")
    void shouldHandleSessionBusinessLogicMethodsCorrectly() {
        // Given
        Session session = sessionRepository.findById(testSession.getId()).orElseThrow();
        
        // Then - Test business logic methods
        assertTrue(session.isValid()); // Should be valid (active and not expired)
        assertFalse(session.isExpired()); // Should not be expired
        assertTrue(session.hasTokenHashes()); // Should have token hashes
        assertEquals(testUser.getUsername(), session.getUserUsername());
        assertEquals(testUser.getId(), session.getUserId());
        
        // Test session termination
        session.terminate();
        sessionRepository.save(session);
        
        entityManager.clear();
        Session terminatedSession = sessionRepository.findById(testSession.getId()).orElseThrow();
        assertFalse(terminatedSession.getIsActive());
        assertNotNull(terminatedSession.getLogoutAt());
        assertFalse(terminatedSession.isValid());
    }
}