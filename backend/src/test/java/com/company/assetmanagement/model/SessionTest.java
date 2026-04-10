package com.company.assetmanagement.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the Session entity.
 * 
 * Tests the business logic methods, validation, and entity behavior
 * without requiring database connectivity.
 */
@DisplayName("Session Entity Tests")
class SessionTest {

    private User user;
    private Session session;
    private LocalDateTime futureExpiration;
    private LocalDateTime pastExpiration;

    @BeforeEach
    void setUp() {
        user = new User("testuser", "hashedPassword123", "test@example.com");
        user.setId(UUID.randomUUID());
        
        futureExpiration = LocalDateTime.now().plusMinutes(30);
        pastExpiration = LocalDateTime.now().minusMinutes(30);
        
        session = new Session(user, futureExpiration);
        session.setId(UUID.randomUUID());
    }

    @Test
    @DisplayName("Should create session with required fields")
    void shouldCreateSessionWithRequiredFields() {
        // Given & When
        Session newSession = new Session(user, futureExpiration);
        
        // Then
        assertEquals(user, newSession.getUser());
        assertEquals(futureExpiration, newSession.getTokenExpiration());
        assertTrue(newSession.getIsActive());
        assertNotNull(newSession.getLoginAt());
        assertNull(newSession.getLogoutAt());
    }

    @Test
    @DisplayName("Should create session with token hashes")
    void shouldCreateSessionWithTokenHashes() {
        // Given
        String accessTokenHash = "access-hash-123";
        String refreshTokenHash = "refresh-hash-456";
        
        // When
        Session sessionWithTokens = new Session(user, futureExpiration, accessTokenHash, refreshTokenHash);
        
        // Then
        assertEquals(user, sessionWithTokens.getUser());
        assertEquals(futureExpiration, sessionWithTokens.getTokenExpiration());
        assertEquals(accessTokenHash, sessionWithTokens.getAccessTokenHash());
        assertEquals(refreshTokenHash, sessionWithTokens.getRefreshTokenHash());
        assertTrue(sessionWithTokens.hasTokenHashes());
    }

    @Test
    @DisplayName("Should not be expired when expiration is in future")
    void shouldNotBeExpiredWhenExpirationInFuture() {
        // Given
        session.setTokenExpiration(LocalDateTime.now().plusMinutes(10));
        
        // When & Then
        assertFalse(session.isExpired());
    }

    @Test
    @DisplayName("Should be expired when expiration is in past")
    void shouldBeExpiredWhenExpirationInPast() {
        // Given
        session.setTokenExpiration(pastExpiration);
        
        // When & Then
        assertTrue(session.isExpired());
    }

    @Test
    @DisplayName("Should be valid when active and not expired")
    void shouldBeValidWhenActiveAndNotExpired() {
        // Given
        session.setIsActive(true);
        session.setTokenExpiration(futureExpiration);
        
        // When & Then
        assertTrue(session.isValid());
    }

    @Test
    @DisplayName("Should not be valid when inactive")
    void shouldNotBeValidWhenInactive() {
        // Given
        session.setIsActive(false);
        session.setTokenExpiration(futureExpiration);
        
        // When & Then
        assertFalse(session.isValid());
    }

    @Test
    @DisplayName("Should not be valid when expired")
    void shouldNotBeValidWhenExpired() {
        // Given
        session.setIsActive(true);
        session.setTokenExpiration(pastExpiration);
        
        // When & Then
        assertFalse(session.isValid());
    }

    @Test
    @DisplayName("Should terminate session correctly")
    void shouldTerminateSessionCorrectly() {
        // Given
        LocalDateTime beforeTermination = LocalDateTime.now();
        
        // When
        session.terminate();
        
        // Then
        assertFalse(session.getIsActive());
        assertNotNull(session.getLogoutAt());
        assertTrue(session.getLogoutAt().isAfter(beforeTermination.minusSeconds(1)));
        assertTrue(session.getLogoutAt().isBefore(LocalDateTime.now().plusSeconds(1)));
    }

    @Test
    @DisplayName("Should mark session as expired correctly")
    void shouldMarkSessionAsExpiredCorrectly() {
        // When
        session.markExpired();
        
        // Then
        assertFalse(session.getIsActive());
        assertNull(session.getLogoutAt()); // Should not set logout time for expired sessions
    }

    @Test
    @DisplayName("Should calculate session duration for active session")
    void shouldCalculateSessionDurationForActiveSession() {
        // Given
        LocalDateTime loginTime = LocalDateTime.now().minusMinutes(15);
        session.setLoginAt(loginTime);
        
        // When
        long duration = session.getSessionDurationMinutes();
        
        // Then
        assertTrue(duration >= 14 && duration <= 16); // Allow for small timing differences
    }

    @Test
    @DisplayName("Should calculate session duration for terminated session")
    void shouldCalculateSessionDurationForTerminatedSession() {
        // Given
        LocalDateTime loginTime = LocalDateTime.now().minusMinutes(20);
        LocalDateTime logoutTime = LocalDateTime.now().minusMinutes(5);
        session.setLoginAt(loginTime);
        session.setLogoutAt(logoutTime);
        
        // When
        long duration = session.getSessionDurationMinutes();
        
        // Then
        assertEquals(15, duration);
    }

    @Test
    @DisplayName("Should calculate minutes until expiration for valid session")
    void shouldCalculateMinutesUntilExpirationForValidSession() {
        // Given
        session.setTokenExpiration(LocalDateTime.now().plusMinutes(25));
        
        // When
        long minutesUntilExpiration = session.getMinutesUntilExpiration();
        
        // Then
        assertTrue(minutesUntilExpiration >= 24 && minutesUntilExpiration <= 26);
    }

    @Test
    @DisplayName("Should return zero minutes until expiration for expired session")
    void shouldReturnZeroMinutesUntilExpirationForExpiredSession() {
        // Given
        session.setTokenExpiration(pastExpiration);
        
        // When
        long minutesUntilExpiration = session.getMinutesUntilExpiration();
        
        // Then
        assertEquals(0, minutesUntilExpiration);
    }

    @Test
    @DisplayName("Should get user username")
    void shouldGetUserUsername() {
        // When
        String username = session.getUserUsername();
        
        // Then
        assertEquals("testuser", username);
    }

    @Test
    @DisplayName("Should return null username when user is null")
    void shouldReturnNullUsernameWhenUserIsNull() {
        // Given
        Session sessionWithNullUser = new Session();
        
        // When
        String username = sessionWithNullUser.getUserUsername();
        
        // Then
        assertNull(username);
    }

    @Test
    @DisplayName("Should get user ID")
    void shouldGetUserId() {
        // When
        UUID userId = session.getUserId();
        
        // Then
        assertEquals(user.getId(), userId);
    }

    @Test
    @DisplayName("Should return null user ID when user is null")
    void shouldReturnNullUserIdWhenUserIsNull() {
        // Given
        Session sessionWithNullUser = new Session();
        
        // When
        UUID userId = sessionWithNullUser.getUserId();
        
        // Then
        assertNull(userId);
    }

    @Test
    @DisplayName("Should update token expiration")
    void shouldUpdateTokenExpiration() {
        // Given
        LocalDateTime newExpiration = LocalDateTime.now().plusHours(1);
        
        // When
        session.updateTokenExpiration(newExpiration);
        
        // Then
        assertEquals(newExpiration, session.getTokenExpiration());
    }

    @Test
    @DisplayName("Should update token hashes")
    void shouldUpdateTokenHashes() {
        // Given
        String newAccessHash = "new-access-hash";
        String newRefreshHash = "new-refresh-hash";
        
        // When
        session.updateTokenHashes(newAccessHash, newRefreshHash);
        
        // Then
        assertEquals(newAccessHash, session.getAccessTokenHash());
        assertEquals(newRefreshHash, session.getRefreshTokenHash());
        assertTrue(session.hasTokenHashes());
    }

    @Test
    @DisplayName("Should detect when session has token hashes")
    void shouldDetectWhenSessionHasTokenHashes() {
        // Given
        session.setAccessTokenHash("access-hash");
        session.setRefreshTokenHash("refresh-hash");
        
        // When & Then
        assertTrue(session.hasTokenHashes());
    }

    @Test
    @DisplayName("Should detect when session does not have token hashes")
    void shouldDetectWhenSessionDoesNotHaveTokenHashes() {
        // Given
        session.setAccessTokenHash(null);
        session.setRefreshTokenHash(null);
        
        // When & Then
        assertFalse(session.hasTokenHashes());
    }

    @Test
    @DisplayName("Should detect when session has partial token hashes")
    void shouldDetectWhenSessionHasPartialTokenHashes() {
        // Given
        session.setAccessTokenHash("access-hash");
        session.setRefreshTokenHash(null);
        
        // When & Then
        assertFalse(session.hasTokenHashes());
    }

    @Test
    @DisplayName("Should be equal when IDs are same")
    void shouldBeEqualWhenIdsAreSame() {
        // Given
        UUID sameId = UUID.randomUUID();
        Session session1 = new Session();
        session1.setId(sameId);
        Session session2 = new Session();
        session2.setId(sameId);
        
        // When & Then
        assertEquals(session1, session2);
        assertEquals(session1.hashCode(), session2.hashCode());
    }

    @Test
    @DisplayName("Should not be equal when IDs are different")
    void shouldNotBeEqualWhenIdsAreDifferent() {
        // Given
        Session session1 = new Session();
        session1.setId(UUID.randomUUID());
        Session session2 = new Session();
        session2.setId(UUID.randomUUID());
        
        // When & Then
        assertNotEquals(session1, session2);
    }

    @Test
    @DisplayName("Should exclude token hashes from toString")
    void shouldExcludeTokenHashesFromToString() {
        // Given
        session.setAccessTokenHash("secret-access-hash");
        session.setRefreshTokenHash("secret-refresh-hash");
        
        // When
        String sessionString = session.toString();
        
        // Then
        assertFalse(sessionString.contains("secret-access-hash"));
        assertFalse(sessionString.contains("secret-refresh-hash"));
        assertTrue(sessionString.contains("testuser"));
        assertTrue(sessionString.contains("Session{"));
        assertTrue(sessionString.contains("hasTokenHashes=true"));
    }

    @Test
    @DisplayName("Should handle null values gracefully in toString")
    void shouldHandleNullValuesGracefullyInToString() {
        // Given
        Session sessionWithNulls = new Session();
        
        // When
        String sessionString = sessionWithNulls.toString();
        
        // Then
        assertNotNull(sessionString);
        assertTrue(sessionString.contains("Session{"));
    }

    @Test
    @DisplayName("Should handle null values gracefully in business methods")
    void shouldHandleNullValuesGracefullyInBusinessMethods() {
        // Given
        Session sessionWithNulls = new Session();
        
        // When & Then
        assertNull(sessionWithNulls.getUserUsername());
        assertNull(sessionWithNulls.getUserId());
        assertFalse(sessionWithNulls.hasTokenHashes());
        assertNotNull(sessionWithNulls.toString());
    }
}