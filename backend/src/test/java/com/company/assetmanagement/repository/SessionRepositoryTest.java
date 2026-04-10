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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for SessionRepository.
 * 
 * Tests the custom query methods and ensures proper functionality
 * of session data access operations including:
 * - Active session management and tracking
 * - User-based session queries and cleanup
 * - Token-based session validation and lookup
 * - Session lifecycle operations (create, terminate, expire)
 * - Session cleanup and maintenance operations
 * - Session audit and monitoring queries
 * 
 * Uses @DataJpaTest for focused repository testing with in-memory database.
 */
@DataJpaTest
@ActiveProfiles("test")
@DisplayName("SessionRepository Tests")
class SessionRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private SessionRepository sessionRepository;

    private User testUser1;
    private User testUser2;
    private User adminUser;
    private Session activeSession1;
    private Session activeSession2;
    private Session expiredSession;
    private Session inactiveSession;

    @BeforeEach
    void setUp() {
        // Create test users
        testUser1 = new User("testuser1", "$2a$10$hashedpassword1", "test1@example.com");
        testUser1.setIsActive(true);
        testUser1.setAccountLocked(false);

        testUser2 = new User("testuser2", "$2a$10$hashedpassword2", "test2@example.com");
        testUser2.setIsActive(true);
        testUser2.setAccountLocked(false);

        adminUser = new User("admin", "$2a$10$hashedpasswordadmin", "admin@example.com");
        adminUser.setIsActive(true);
        adminUser.setAccountLocked(false);

        // Persist users
        testUser1 = entityManager.persistAndFlush(testUser1);
        testUser2 = entityManager.persistAndFlush(testUser2);
        adminUser = entityManager.persistAndFlush(adminUser);

        // Create roles
        UserRole adminRole = new UserRole(adminUser, Role.ADMINISTRATOR, adminUser);
        UserRole managerRole = new UserRole(testUser1, Role.ASSET_MANAGER, adminUser);
        UserRole viewerRole = new UserRole(testUser2, Role.VIEWER, adminUser);

        entityManager.persistAndFlush(adminRole);
        entityManager.persistAndFlush(managerRole);
        entityManager.persistAndFlush(viewerRole);

        // Create test sessions
        LocalDateTime now = LocalDateTime.now();
        
        // Active session for testUser1
        activeSession1 = new Session(testUser1, now.plusMinutes(30));
        activeSession1.setAccessTokenHash("hash_access_token_1");
        activeSession1.setRefreshTokenHash("hash_refresh_token_1");
        
        // Active session for testUser2
        activeSession2 = new Session(testUser2, now.plusMinutes(30));
        activeSession2.setAccessTokenHash("hash_access_token_2");
        activeSession2.setRefreshTokenHash("hash_refresh_token_2");
        
        // Expired but still marked active session
        expiredSession = new Session(testUser1, now.minusMinutes(10));
        expiredSession.setAccessTokenHash("hash_expired_token");
        expiredSession.setRefreshTokenHash("hash_expired_refresh");
        
        // Inactive session (logged out)
        inactiveSession = new Session(testUser1, now.plusMinutes(30));
        inactiveSession.setIsActive(false);
        inactiveSession.setLogoutAt(now.minusMinutes(5));
        inactiveSession.setAccessTokenHash("hash_inactive_token");
        inactiveSession.setRefreshTokenHash("hash_inactive_refresh");

        // Persist sessions
        activeSession1 = entityManager.persistAndFlush(activeSession1);
        activeSession2 = entityManager.persistAndFlush(activeSession2);
        expiredSession = entityManager.persistAndFlush(expiredSession);
        inactiveSession = entityManager.persistAndFlush(inactiveSession);

        entityManager.clear();
    }

    // ========================================================================
    // Active session management tests
    // ========================================================================

    @Test
    @DisplayName("Should find active sessions by user")
    void shouldFindActiveSessionsByUser() {
        // When
        List<Session> activeSessions = sessionRepository.findByUserAndIsActiveTrue(testUser1);

        // Then
        assertThat(activeSessions).hasSize(2); // activeSession1 and expiredSession (still marked active)
        assertThat(activeSessions).extracting(Session::getIsActive).containsOnly(true);
        assertThat(activeSessions).extracting(Session::getUserId).containsOnly(testUser1.getId());
    }

    @Test
    @DisplayName("Should find active sessions by user ID")
    void shouldFindActiveSessionsByUserId() {
        // When
        List<Session> activeSessions = sessionRepository.findActiveSessionsByUserId(testUser1.getId());

        // Then
        assertThat(activeSessions).hasSize(2);
        assertThat(activeSessions).extracting(Session::getIsActive).containsOnly(true);
    }

    @Test
    @DisplayName("Should count active sessions by user")
    void shouldCountActiveSessionsByUser() {
        // When
        long count = sessionRepository.countByUserAndIsActiveTrue(testUser1);

        // Then
        assertThat(count).isEqualTo(2);
    }

    @Test
    @DisplayName("Should count active sessions by user ID")
    void shouldCountActiveSessionsByUserId() {
        // When
        long count = sessionRepository.countActiveSessionsByUserId(testUser2.getId());

        // Then
        assertThat(count).isEqualTo(1);
    }

    @Test
    @DisplayName("Should count total active sessions")
    void shouldCountTotalActiveSessions() {
        // When
        long count = sessionRepository.countByIsActiveTrue();

        // Then
        assertThat(count).isEqualTo(3); // activeSession1, activeSession2, expiredSession
    }

    @Test
    @DisplayName("Should find all active sessions with pagination")
    void shouldFindAllActiveSessionsWithPagination() {
        // Given
        Pageable pageable = PageRequest.of(0, 2);

        // When
        Page<Session> activeSessions = sessionRepository.findByIsActiveTrue(pageable);

        // Then
        assertThat(activeSessions.getContent()).hasSize(2);
        assertThat(activeSessions.getTotalElements()).isEqualTo(3);
        assertThat(activeSessions.getContent()).extracting(Session::getIsActive).containsOnly(true);
    }

    // ========================================================================
    // Session lifecycle operation tests
    // ========================================================================

    @Test
    @DisplayName("Should find expired active sessions")
    void shouldFindExpiredActiveSessions() {
        // Given
        LocalDateTime currentTime = LocalDateTime.now();

        // When
        List<Session> expiredSessions = sessionRepository.findExpiredActiveSessions(currentTime);

        // Then
        assertThat(expiredSessions).hasSize(1);
        assertThat(expiredSessions.get(0).getId()).isEqualTo(expiredSession.getId());
        assertThat(expiredSessions.get(0).getIsActive()).isTrue();
        assertThat(expiredSessions.get(0).isExpired()).isTrue();
    }

    @Test
    @DisplayName("Should find sessions expiring soon")
    void shouldFindSessionsExpiringSoon() {
        // Given
        LocalDateTime currentTime = LocalDateTime.now();
        LocalDateTime soonThreshold = currentTime.plusMinutes(45);

        // When
        List<Session> expiringSoon = sessionRepository.findSessionsExpiringSoon(currentTime, soonThreshold);

        // Then
        assertThat(expiringSoon).hasSize(2); // activeSession1 and activeSession2 expire in 30 minutes
        assertThat(expiringSoon).extracting(Session::getIsActive).containsOnly(true);
    }

    @Test
    @DisplayName("Should mark expired sessions as inactive")
    void shouldMarkExpiredSessionsInactive() {
        // Given
        LocalDateTime currentTime = LocalDateTime.now();

        // When
        int updatedCount = sessionRepository.markExpiredSessionsInactive(currentTime);

        // Then
        assertThat(updatedCount).isEqualTo(1);
        
        // Verify the expired session is now inactive
        entityManager.clear();
        Session updatedSession = entityManager.find(Session.class, expiredSession.getId());
        assertThat(updatedSession.getIsActive()).isFalse();
    }

    // ========================================================================
    // User-based session query tests
    // ========================================================================

    @Test
    @DisplayName("Should find all sessions by user")
    void shouldFindAllSessionsByUser() {
        // When
        List<Session> userSessions = sessionRepository.findByUser(testUser1);

        // Then
        assertThat(userSessions).hasSize(3); // activeSession1, expiredSession, inactiveSession
        assertThat(userSessions).extracting(Session::getUserId).containsOnly(testUser1.getId());
    }

    @Test
    @DisplayName("Should find sessions by user with pagination")
    void shouldFindSessionsByUserWithPagination() {
        // Given
        Pageable pageable = PageRequest.of(0, 2);

        // When
        Page<Session> userSessions = sessionRepository.findByUser(testUser1, pageable);

        // Then
        assertThat(userSessions.getContent()).hasSize(2);
        assertThat(userSessions.getTotalElements()).isEqualTo(3);
    }

    @Test
    @DisplayName("Should find sessions by user ID")
    void shouldFindSessionsByUserId() {
        // When
        List<Session> userSessions = sessionRepository.findByUserId(testUser2.getId());

        // Then
        assertThat(userSessions).hasSize(1);
        assertThat(userSessions.get(0).getId()).isEqualTo(activeSession2.getId());
    }

    @Test
    @DisplayName("Should find most recent session by user")
    void shouldFindMostRecentSessionByUser() {
        // When
        Optional<Session> recentSession = sessionRepository.findMostRecentSessionByUser(testUser1);

        // Then
        assertThat(recentSession).isPresent();
        // The most recent should be one of the sessions created for testUser1
        assertThat(recentSession.get().getUserId()).isEqualTo(testUser1.getId());
    }

    @Test
    @DisplayName("Should find most recent active session by user")
    void shouldFindMostRecentActiveSessionByUser() {
        // When
        Optional<Session> recentActiveSession = sessionRepository.findMostRecentActiveSessionByUser(testUser1);

        // Then
        assertThat(recentActiveSession).isPresent();
        assertThat(recentActiveSession.get().getIsActive()).isTrue();
        assertThat(recentActiveSession.get().getUserId()).isEqualTo(testUser1.getId());
    }

    // ========================================================================
    // Token-based session validation tests
    // ========================================================================

    @Test
    @DisplayName("Should find session by access token hash")
    void shouldFindSessionByAccessTokenHash() {
        // When
        Optional<Session> session = sessionRepository.findByAccessTokenHash("hash_access_token_1");

        // Then
        assertThat(session).isPresent();
        assertThat(session.get().getId()).isEqualTo(activeSession1.getId());
    }

    @Test
    @DisplayName("Should find session by refresh token hash")
    void shouldFindSessionByRefreshTokenHash() {
        // When
        Optional<Session> session = sessionRepository.findByRefreshTokenHash("hash_refresh_token_2");

        // Then
        assertThat(session).isPresent();
        assertThat(session.get().getId()).isEqualTo(activeSession2.getId());
    }

    @Test
    @DisplayName("Should find active session by access token hash")
    void shouldFindActiveSessionByAccessTokenHash() {
        // When
        Optional<Session> activeSession = sessionRepository.findActiveSessionByAccessTokenHash("hash_access_token_1");
        Optional<Session> inactiveSession = sessionRepository.findActiveSessionByAccessTokenHash("hash_inactive_token");

        // Then
        assertThat(activeSession).isPresent();
        assertThat(activeSession.get().getIsActive()).isTrue();
        assertThat(inactiveSession).isEmpty(); // Should not find inactive session
    }

    @Test
    @DisplayName("Should find active session by refresh token hash")
    void shouldFindActiveSessionByRefreshTokenHash() {
        // When
        Optional<Session> activeSession = sessionRepository.findActiveSessionByRefreshTokenHash("hash_refresh_token_2");
        Optional<Session> inactiveSession = sessionRepository.findActiveSessionByRefreshTokenHash("hash_inactive_refresh");

        // Then
        assertThat(activeSession).isPresent();
        assertThat(activeSession.get().getIsActive()).isTrue();
        assertThat(inactiveSession).isEmpty(); // Should not find inactive session
    }

    @Test
    @DisplayName("Should check if active session exists by access token hash")
    void shouldCheckIfActiveSessionExistsByAccessTokenHash() {
        // When
        boolean activeExists = sessionRepository.existsActiveSessionByAccessTokenHash("hash_access_token_1");
        boolean inactiveExists = sessionRepository.existsActiveSessionByAccessTokenHash("hash_inactive_token");

        // Then
        assertThat(activeExists).isTrue();
        assertThat(inactiveExists).isFalse();
    }

    @Test
    @DisplayName("Should check if active session exists by refresh token hash")
    void shouldCheckIfActiveSessionExistsByRefreshTokenHash() {
        // When
        boolean activeExists = sessionRepository.existsActiveSessionByRefreshTokenHash("hash_refresh_token_2");
        boolean inactiveExists = sessionRepository.existsActiveSessionByRefreshTokenHash("hash_inactive_refresh");

        // Then
        assertThat(activeExists).isTrue();
        assertThat(inactiveExists).isFalse();
    }

    // ========================================================================
    // Session invalidation operation tests
    // ========================================================================

    @Test
    @DisplayName("Should invalidate all user sessions")
    void shouldInvalidateAllUserSessions() {
        // Given
        LocalDateTime logoutTime = LocalDateTime.now();

        // When
        int invalidatedCount = sessionRepository.invalidateAllUserSessions(testUser1, logoutTime);

        // Then
        assertThat(invalidatedCount).isEqualTo(2); // Only active sessions are invalidated
        
        // Verify sessions are invalidated
        entityManager.clear();
        List<Session> userSessions = sessionRepository.findByUser(testUser1);
        long activeCount = userSessions.stream().mapToLong(s -> s.getIsActive() ? 1 : 0).sum();
        assertThat(activeCount).isEqualTo(0);
    }

    @Test
    @DisplayName("Should invalidate all user sessions by user ID")
    void shouldInvalidateAllUserSessionsByUserId() {
        // Given
        LocalDateTime logoutTime = LocalDateTime.now();

        // When
        int invalidatedCount = sessionRepository.invalidateAllUserSessionsByUserId(testUser2.getId(), logoutTime);

        // Then
        assertThat(invalidatedCount).isEqualTo(1);
        
        // Verify session is invalidated
        entityManager.clear();
        List<Session> userSessions = sessionRepository.findByUserId(testUser2.getId());
        assertThat(userSessions.get(0).getIsActive()).isFalse();
    }

    @Test
    @DisplayName("Should invalidate session by access token hash")
    void shouldInvalidateSessionByAccessTokenHash() {
        // Given
        LocalDateTime logoutTime = LocalDateTime.now();

        // When
        int invalidatedCount = sessionRepository.invalidateSessionByAccessTokenHash("hash_access_token_1", logoutTime);

        // Then
        assertThat(invalidatedCount).isEqualTo(1);
        
        // Verify session is invalidated
        entityManager.clear();
        Optional<Session> session = sessionRepository.findByAccessTokenHash("hash_access_token_1");
        assertThat(session).isPresent();
        assertThat(session.get().getIsActive()).isFalse();
        assertThat(session.get().getLogoutAt()).isNotNull();
    }

    // ========================================================================
    // Session cleanup and maintenance tests
    // ========================================================================

    @Test
    @DisplayName("Should find sessions for cleanup")
    void shouldFindSessionsForCleanup() {
        // Given
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(30);
        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<Session> cleanupCandidates = sessionRepository.findSessionsForCleanup(cutoffDate, pageable);

        // Then
        // Since our test sessions are recent, there should be no cleanup candidates
        assertThat(cleanupCandidates.getContent()).isEmpty();
    }

    @Test
    @DisplayName("Should count sessions for cleanup")
    void shouldCountSessionsForCleanup() {
        // Given
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(30);

        // When
        long cleanupCount = sessionRepository.countSessionsForCleanup(cutoffDate);

        // Then
        assertThat(cleanupCount).isEqualTo(0); // No old sessions in test data
    }

    // ========================================================================
    // Session audit and monitoring tests
    // ========================================================================

    @Test
    @DisplayName("Should find sessions by login date range")
    void shouldFindSessionsByLoginDateRange() {
        // Given
        LocalDateTime startDate = LocalDateTime.now().minusHours(1);
        LocalDateTime endDate = LocalDateTime.now().plusHours(1);

        // When
        List<Session> sessions = sessionRepository.findSessionsByLoginAtBetween(startDate, endDate);

        // Then
        assertThat(sessions).hasSize(4); // All test sessions should be in this range
    }

    @Test
    @DisplayName("Should find long running sessions")
    void shouldFindLongRunningSessions() {
        // Given
        LocalDateTime cutoffTime = LocalDateTime.now().minusHours(1);

        // When
        List<Session> longRunningSessions = sessionRepository.findLongRunningSessions(cutoffTime);

        // Then
        // Since our sessions are recent, they should all be considered long-running
        assertThat(longRunningSessions).hasSize(3); // Only active sessions
        assertThat(longRunningSessions).extracting(Session::getIsActive).containsOnly(true);
    }

    // ========================================================================
    // Advanced session query tests
    // ========================================================================

    @Test
    @DisplayName("Should find sessions with token hashes")
    void shouldFindSessionsWithTokenHashes() {
        // When
        List<Session> sessionsWithHashes = sessionRepository.findSessionsWithTokenHashes();

        // Then
        assertThat(sessionsWithHashes).hasSize(4); // All test sessions have token hashes
        assertThat(sessionsWithHashes).allMatch(Session::hasTokenHashes);
    }

    @Test
    @DisplayName("Should update session tokens")
    void shouldUpdateSessionTokens() {
        // Given
        String newAccessHash = "new_access_hash";
        String newRefreshHash = "new_refresh_hash";
        LocalDateTime newExpiration = LocalDateTime.now().plusMinutes(60);

        // When
        int updatedCount = sessionRepository.updateSessionTokens(
            activeSession1.getId(), newAccessHash, newRefreshHash, newExpiration);

        // Then
        assertThat(updatedCount).isEqualTo(1);
        
        // Verify update
        entityManager.clear();
        Session updatedSession = entityManager.find(Session.class, activeSession1.getId());
        assertThat(updatedSession.getAccessTokenHash()).isEqualTo(newAccessHash);
        assertThat(updatedSession.getRefreshTokenHash()).isEqualTo(newRefreshHash);
        assertThat(updatedSession.getTokenExpiration()).isEqualTo(newExpiration);
    }

    @Test
    @DisplayName("Should update session expiration")
    void shouldUpdateSessionExpiration() {
        // Given
        LocalDateTime newExpiration = LocalDateTime.now().plusHours(2);

        // When
        int updatedCount = sessionRepository.updateSessionExpiration(activeSession2.getId(), newExpiration);

        // Then
        assertThat(updatedCount).isEqualTo(1);
        
        // Verify update
        entityManager.clear();
        Session updatedSession = entityManager.find(Session.class, activeSession2.getId());
        assertThat(updatedSession.getTokenExpiration()).isEqualTo(newExpiration);
    }

    @Test
    @DisplayName("Should find sessions with advanced filters")
    void shouldFindSessionsWithAdvancedFilters() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        LocalDateTime loginAfter = LocalDateTime.now().minusHours(1);
        LocalDateTime loginBefore = LocalDateTime.now().plusHours(1);

        // When
        Page<Session> filteredSessions = sessionRepository.findWithFilters(
            testUser1, true, loginAfter, loginBefore, null, null, pageable);

        // Then
        assertThat(filteredSessions.getContent()).hasSize(2); // Active sessions for testUser1
        assertThat(filteredSessions.getContent()).extracting(Session::getIsActive).containsOnly(true);
        assertThat(filteredSessions.getContent()).extracting(Session::getUserId).containsOnly(testUser1.getId());
    }
}