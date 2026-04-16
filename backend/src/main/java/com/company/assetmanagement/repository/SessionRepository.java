package com.company.assetmanagement.repository;

import com.company.assetmanagement.model.Session;
import com.company.assetmanagement.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for Session entity operations.
 * 
 * Provides comprehensive data access methods for session management including:
 * - Active session tracking and management
 * - Session lifecycle operations (create, terminate, expire)
 * - User-based session queries and cleanup
 * - Token-based session validation and lookup
 * - Expired session cleanup and maintenance
 * - Session audit and monitoring support
 * 
 * Security Features:
 * - Token hash validation for secure session verification
 * - Automatic expired session detection and cleanup
 * - Session invalidation for security events (logout, password change, role change)
 * - User-based session management for administrative control
 * 
 * Performance Optimizations:
 * - Indexed queries on frequently searched fields (userId, tokenExpiration, isActive)
 * - Bulk operations for session cleanup and invalidation
 * - Optimized queries with proper filtering and pagination
 * - Efficient session validation without loading full entities
 * 
 * Business Rules Enforced:
 * - Sessions are automatically marked inactive when expired
 * - User sessions are invalidated on security events
 * - Token hashes are used for secure session validation
 * - Session cleanup maintains system performance
 * 
 * @author IT Asset Management System
 * @version 1.0
 * @since 2024-01-15
 */
@Repository
public interface SessionRepository extends JpaRepository<Session, UUID> {

    // ========================================================================
    // Active session management
    // ========================================================================

    /**
     * Finds all active sessions for a specific user.
     * Used to track current user sessions and enforce session limits.
     * 
     * @param user the user whose active sessions to retrieve
     * @return List of active sessions for the user
     */
    List<Session> findByUserAndIsActiveTrue(User user);

    /**
     * Finds all active sessions for a user by user ID.
     * Convenient method when only the user ID is available.
     * 
     * @param userId the ID of the user whose active sessions to retrieve
     * @return List of active sessions for the user
     */
    @Query("SELECT s FROM Session s WHERE s.user.id = :userId AND s.isActive = true")
    List<Session> findActiveSessionsByUserId(@Param("userId") UUID userId);

    /**
     * Finds all active sessions with pagination.
     * Used for administrative monitoring of all active sessions in the system.
     * 
     * @param pageable pagination parameters
     * @return Page of active sessions
     */
    Page<Session> findByIsActiveTrue(Pageable pageable);

    /**
     * Counts the number of active sessions for a specific user.
     * Used to enforce session limits and monitor user activity.
     * 
     * @param user the user whose active sessions to count
     * @return number of active sessions for the user
     */
    long countByUserAndIsActiveTrue(User user);

    /**
     * Counts the number of active sessions for a user by user ID.
     * 
     * @param userId the ID of the user whose active sessions to count
     * @return number of active sessions for the user
     */
    @Query("SELECT COUNT(s) FROM Session s WHERE s.user.id = :userId AND s.isActive = true")
    long countActiveSessionsByUserId(@Param("userId") UUID userId);

    /**
     * Counts total number of active sessions in the system.
     * Used for dashboard metrics and system monitoring.
     * 
     * @return total number of active sessions
     */
    long countByIsActiveTrue();

    // ========================================================================
    // Session lifecycle operations
    // ========================================================================

    /**
     * Finds sessions that have expired but are still marked as active.
     * Used for cleanup operations to mark expired sessions as inactive.
     * 
     * @param currentTime the current timestamp
     * @return List of expired sessions that are still marked active
     */
    @Query("SELECT s FROM Session s WHERE s.isActive = true AND s.tokenExpiration <= :currentTime")
    List<Session> findExpiredActiveSessions(@Param("currentTime") LocalDateTime currentTime);

    /**
     * Finds sessions that have expired but are still marked as active with pagination.
     * Used for batch processing of expired sessions.
     * 
     * @param currentTime the current timestamp
     * @param pageable pagination parameters
     * @return Page of expired sessions that are still marked active
     */
    @Query("SELECT s FROM Session s WHERE s.isActive = true AND s.tokenExpiration <= :currentTime")
    Page<Session> findExpiredActiveSessions(@Param("currentTime") LocalDateTime currentTime, Pageable pageable);

    /**
     * Finds all sessions (active and inactive) that have expired.
     * Used for cleanup operations and audit purposes.
     * 
     * @param currentTime the current timestamp
     * @return List of all expired sessions
     */
    List<Session> findByTokenExpirationLessThanEqual(LocalDateTime currentTime);

    /**
     * Finds sessions that will expire within a specified time window.
     * Used for proactive session management and user notifications.
     * 
     * @param expirationThreshold the time threshold for upcoming expiration
     * @param currentTime the current timestamp
     * @return List of sessions expiring soon
     */
    @Query("SELECT s FROM Session s WHERE s.isActive = true AND " +
           "s.tokenExpiration > :currentTime AND s.tokenExpiration <= :expirationThreshold")
    List<Session> findSessionsExpiringSoon(@Param("currentTime") LocalDateTime currentTime,
                                          @Param("expirationThreshold") LocalDateTime expirationThreshold);

    // ========================================================================
    // User-based session queries
    // ========================================================================

    /**
     * Finds all sessions (active and inactive) for a specific user.
     * Used for user session history and audit purposes.
     * 
     * @param user the user whose sessions to retrieve
     * @return List of all sessions for the user
     */
    List<Session> findByUser(User user);

    /**
     * Finds all sessions for a user with pagination.
     * 
     * @param user the user whose sessions to retrieve
     * @param pageable pagination parameters
     * @return Page of sessions for the user
     */
    Page<Session> findByUser(User user, Pageable pageable);

    /**
     * Finds all sessions for a user by user ID.
     * 
     * @param userId the ID of the user whose sessions to retrieve
     * @return List of all sessions for the user
     */
    @Query("SELECT s FROM Session s WHERE s.user.id = :userId")
    List<Session> findByUserId(@Param("userId") UUID userId);

    /**
     * Finds sessions for a user within a specific date range.
     * Used for audit reporting and activity analysis.
     * 
     * @param user the user whose sessions to retrieve
     * @param startDate the start date (inclusive)
     * @param endDate the end date (inclusive)
     * @return List of sessions within the date range
     */
    @Query("SELECT s FROM Session s WHERE s.user = :user AND " +
           "s.loginAt >= :startDate AND s.loginAt <= :endDate")
    List<Session> findByUserAndLoginAtBetween(@Param("user") User user,
                                             @Param("startDate") LocalDateTime startDate,
                                             @Param("endDate") LocalDateTime endDate);

    /**
     * Finds the most recent session for a user.
     * Used to get the user's last login information.
     * 
     * @param user the user whose most recent session to retrieve
     * @return Optional containing the most recent session if found
     */
    @Query("SELECT s FROM Session s WHERE s.user = :user ORDER BY s.loginAt DESC LIMIT 1")
    Optional<Session> findMostRecentSessionByUser(@Param("user") User user);

    /**
     * Finds the most recent active session for a user.
     * Used to get the user's current active session.
     * 
     * @param user the user whose most recent active session to retrieve
     * @return Optional containing the most recent active session if found
     */
    @Query("SELECT s FROM Session s WHERE s.user = :user AND s.isActive = true " +
           "ORDER BY s.loginAt DESC LIMIT 1")
    Optional<Session> findMostRecentActiveSessionByUser(@Param("user") User user);

    // ========================================================================
    // Token-based session validation
    // ========================================================================

    /**
     * Finds a session by access token hash.
     * Used for token validation during authentication.
     * 
     * @param accessTokenHash the hash of the access token
     * @return Optional containing the session if found
     */
    Optional<Session> findByAccessTokenHash(String accessTokenHash);

    /**
     * Finds a session by refresh token hash.
     * Used for token refresh operations.
     * 
     * @param refreshTokenHash the hash of the refresh token
     * @return Optional containing the session if found
     */
    Optional<Session> findByRefreshTokenHash(String refreshTokenHash);

    /**
     * Finds an active session by access token hash.
     * More efficient validation that checks both token and active status.
     * 
     * @param accessTokenHash the hash of the access token
     * @return Optional containing the active session if found
     */
    @Query("SELECT s FROM Session s WHERE s.accessTokenHash = :accessTokenHash AND s.isActive = true")
    Optional<Session> findActiveSessionByAccessTokenHash(@Param("accessTokenHash") String accessTokenHash);

    /**
     * Finds an active session by refresh token hash.
     * Used for secure token refresh validation.
     * 
     * @param refreshTokenHash the hash of the refresh token
     * @return Optional containing the active session if found
     */
    @Query("SELECT s FROM Session s WHERE s.refreshTokenHash = :refreshTokenHash AND s.isActive = true")
    Optional<Session> findActiveSessionByRefreshTokenHash(@Param("refreshTokenHash") String refreshTokenHash);

    /**
     * Checks if an access token hash exists and is active.
     * Efficient existence check without loading the full session entity.
     * 
     * @param accessTokenHash the hash of the access token
     * @return true if an active session with this token hash exists
     */
    @Query("SELECT COUNT(s) > 0 FROM Session s WHERE s.accessTokenHash = :accessTokenHash AND s.isActive = true")
    boolean existsActiveSessionByAccessTokenHash(@Param("accessTokenHash") String accessTokenHash);

    /**
     * Checks if a refresh token hash exists and is active.
     * 
     * @param refreshTokenHash the hash of the refresh token
     * @return true if an active session with this token hash exists
     */
    @Query("SELECT COUNT(s) > 0 FROM Session s WHERE s.refreshTokenHash = :refreshTokenHash AND s.isActive = true")
    boolean existsActiveSessionByRefreshTokenHash(@Param("refreshTokenHash") String refreshTokenHash);

    // ========================================================================
    // Session invalidation operations
    // ========================================================================

    /**
     * Invalidates all active sessions for a specific user.
     * Used when user logs out from all devices, changes password, or roles change.
     * 
     * @param user the user whose sessions to invalidate
     * @param logoutTime the timestamp when sessions were invalidated
     * @return number of sessions invalidated
     */
    @Modifying
    @Query("UPDATE Session s SET s.isActive = false, s.logoutAt = :logoutTime " +
           "WHERE s.user = :user AND s.isActive = true")
    int invalidateAllUserSessions(@Param("user") User user, @Param("logoutTime") LocalDateTime logoutTime);

    /**
     * Invalidates all active sessions for a user by user ID.
     * 
     * @param userId the ID of the user whose sessions to invalidate
     * @param logoutTime the timestamp when sessions were invalidated
     * @return number of sessions invalidated
     */
    @Modifying
    @Query("UPDATE Session s SET s.isActive = false, s.logoutAt = :logoutTime " +
           "WHERE s.user.id = :userId AND s.isActive = true")
    int invalidateAllUserSessionsByUserId(@Param("userId") UUID userId, @Param("logoutTime") LocalDateTime logoutTime);

    /**
     * Invalidates all active sessions for a user by user ID (convenience method without logout time).
     * Sets logout time to current timestamp automatically.
     * 
     * @param userId the ID of the user whose sessions to invalidate
     * @return number of sessions invalidated
     */
    @Modifying
    @Query("UPDATE Session s SET s.isActive = false, s.logoutAt = CURRENT_TIMESTAMP " +
           "WHERE s.user.id = :userId AND s.isActive = true")
    int invalidateUserSessions(@Param("userId") UUID userId);

    /**
     * Invalidates a specific session by ID.
     * Used for single session logout operations.
     * 
     * @param sessionId the ID of the session to invalidate
     * @param logoutTime the timestamp when session was invalidated
     * @return number of sessions invalidated (should be 0 or 1)
     */
    @Modifying
    @Query("UPDATE Session s SET s.isActive = false, s.logoutAt = :logoutTime " +
           "WHERE s.id = :sessionId AND s.isActive = true")
    int invalidateSession(@Param("sessionId") UUID sessionId, @Param("logoutTime") LocalDateTime logoutTime);

    /**
     * Invalidates a session by access token hash.
     * Used for token-based logout operations.
     * 
     * @param accessTokenHash the hash of the access token
     * @param logoutTime the timestamp when session was invalidated
     * @return number of sessions invalidated (should be 0 or 1)
     */
    @Modifying
    @Query("UPDATE Session s SET s.isActive = false, s.logoutAt = :logoutTime " +
           "WHERE s.accessTokenHash = :accessTokenHash AND s.isActive = true")
    int invalidateSessionByAccessTokenHash(@Param("accessTokenHash") String accessTokenHash,
                                          @Param("logoutTime") LocalDateTime logoutTime);

    /**
     * Marks expired sessions as inactive without setting logout time.
     * Used for automatic cleanup of expired sessions.
     * 
     * @param currentTime the current timestamp
     * @return number of sessions marked as expired
     */
    @Modifying
    @Query("UPDATE Session s SET s.isActive = false " +
           "WHERE s.isActive = true AND s.tokenExpiration <= :currentTime")
    int markExpiredSessionsInactive(@Param("currentTime") LocalDateTime currentTime);

    // ========================================================================
    // Session cleanup and maintenance
    // ========================================================================

    /**
     * Deletes sessions older than a specified date.
     * Used for periodic cleanup of old session records.
     * 
     * @param cutoffDate sessions older than this date will be deleted
     * @return number of sessions deleted
     */
    @Modifying
    @Query("DELETE FROM Session s WHERE s.loginAt < :cutoffDate")
    int deleteSessionsOlderThan(@Param("cutoffDate") LocalDateTime cutoffDate);

    /**
     * Deletes inactive sessions older than a specified date.
     * More conservative cleanup that only removes inactive sessions.
     * 
     * @param cutoffDate inactive sessions older than this date will be deleted
     * @return number of sessions deleted
     */
    @Modifying
    @Query("DELETE FROM Session s WHERE s.isActive = false AND s.loginAt < :cutoffDate")
    int deleteInactiveSessionsOlderThan(@Param("cutoffDate") LocalDateTime cutoffDate);

    /**
     * Finds sessions that are candidates for cleanup.
     * Returns sessions that are inactive and older than the specified date.
     * 
     * @param cutoffDate sessions older than this date are cleanup candidates
     * @param pageable pagination parameters for batch processing
     * @return Page of sessions that can be cleaned up
     */
    @Query("SELECT s FROM Session s WHERE s.isActive = false AND s.loginAt < :cutoffDate")
    Page<Session> findSessionsForCleanup(@Param("cutoffDate") LocalDateTime cutoffDate, Pageable pageable);

    /**
     * Counts sessions that are candidates for cleanup.
     * 
     * @param cutoffDate sessions older than this date are cleanup candidates
     * @return number of sessions that can be cleaned up
     */
    @Query("SELECT COUNT(s) FROM Session s WHERE s.isActive = false AND s.loginAt < :cutoffDate")
    long countSessionsForCleanup(@Param("cutoffDate") LocalDateTime cutoffDate);

    // ========================================================================
    // Session audit and monitoring
    // ========================================================================

    /**
     * Finds sessions created within a specific date range.
     * Used for audit reporting and activity analysis.
     * 
     * @param startDate the start date (inclusive)
     * @param endDate the end date (inclusive)
     * @return List of sessions created within the date range
     */
    @Query("SELECT s FROM Session s WHERE s.loginAt >= :startDate AND s.loginAt <= :endDate")
    List<Session> findSessionsByLoginAtBetween(@Param("startDate") LocalDateTime startDate,
                                              @Param("endDate") LocalDateTime endDate);

    /**
     * Finds sessions created within a specific date range with pagination.
     * 
     * @param startDate the start date (inclusive)
     * @param endDate the end date (inclusive)
     * @param pageable pagination parameters
     * @return Page of sessions created within the date range
     */
    @Query("SELECT s FROM Session s WHERE s.loginAt >= :startDate AND s.loginAt <= :endDate")
    Page<Session> findSessionsByLoginAtBetween(@Param("startDate") LocalDateTime startDate,
                                              @Param("endDate") LocalDateTime endDate,
                                              Pageable pageable);

    /**
     * Finds long-running sessions (active for more than specified duration).
     * Used for monitoring and security analysis.
     * 
     * @param cutoffTime sessions active since before this time are considered long-running
     * @return List of long-running active sessions
     */
    @Query("SELECT s FROM Session s WHERE s.isActive = true AND s.loginAt <= :cutoffTime")
    List<Session> findLongRunningSessions(@Param("cutoffTime") LocalDateTime cutoffTime);

    /**
     * Gets session statistics for a date range.
     * Returns daily session counts for the specified period.
     * 
     * @param startDate the start date
     * @param endDate the end date
     * @return List of Object arrays containing [Date, SessionCount]
     */
    @Query("SELECT DATE(s.loginAt), COUNT(s) FROM Session s " +
           "WHERE s.loginAt >= :startDate AND s.loginAt <= :endDate " +
           "GROUP BY DATE(s.loginAt) ORDER BY DATE(s.loginAt)")
    List<Object[]> getSessionStatistics(@Param("startDate") LocalDateTime startDate,
                                       @Param("endDate") LocalDateTime endDate);

    /**
     * Gets user activity statistics.
     * Returns users with their session counts for a date range.
     * 
     * @param startDate the start date
     * @param endDate the end date
     * @param pageable pagination parameters
     * @return List of Object arrays containing [User, SessionCount]
     */
    @Query("SELECT s.user, COUNT(s) FROM Session s " +
           "WHERE s.loginAt >= :startDate AND s.loginAt <= :endDate " +
           "GROUP BY s.user ORDER BY COUNT(s) DESC")
    List<Object[]> getUserActivityStatistics(@Param("startDate") LocalDateTime startDate,
                                           @Param("endDate") LocalDateTime endDate,
                                           Pageable pageable);

    // ========================================================================
    // Advanced session queries
    // ========================================================================

    /**
     * Finds sessions with user information eagerly loaded.
     * Optimized query that fetches session and user data in a single query.
     * 
     * @param pageable pagination parameters
     * @return Page of sessions with user information loaded
     */
    @Query("SELECT s FROM Session s LEFT JOIN FETCH s.user")
    Page<Session> findAllWithUser(Pageable pageable);

    /**
     * Finds active sessions with user information eagerly loaded.
     * 
     * @param pageable pagination parameters
     * @return Page of active sessions with user information loaded
     */
    @Query("SELECT s FROM Session s LEFT JOIN FETCH s.user WHERE s.isActive = true")
    Page<Session> findActiveSessionsWithUser(Pageable pageable);

    /**
     * Advanced search with multiple filters for sessions.
     * Supports filtering by user, active status, and date range.
     * 
     * @param user optional user filter (can be null)
     * @param isActive optional active status filter (can be null)
     * @param loginAfter optional login date filter (after this date)
     * @param loginBefore optional login date filter (before this date)
     * @param tokenExpiresAfter optional token expiration filter (expires after this date)
     * @param tokenExpiresBefore optional token expiration filter (expires before this date)
     * @param pageable pagination parameters
     * @return Page of sessions matching the specified criteria
     */
    @Query("SELECT s FROM Session s WHERE " +
           "(:user IS NULL OR s.user = :user) AND " +
           "(:isActive IS NULL OR s.isActive = :isActive) AND " +
           "(:loginAfter IS NULL OR s.loginAt >= :loginAfter) AND " +
           "(:loginBefore IS NULL OR s.loginAt <= :loginBefore) AND " +
           "(:tokenExpiresAfter IS NULL OR s.tokenExpiration >= :tokenExpiresAfter) AND " +
           "(:tokenExpiresBefore IS NULL OR s.tokenExpiration <= :tokenExpiresBefore)")
    Page<Session> findWithFilters(@Param("user") User user,
                                 @Param("isActive") Boolean isActive,
                                 @Param("loginAfter") LocalDateTime loginAfter,
                                 @Param("loginBefore") LocalDateTime loginBefore,
                                 @Param("tokenExpiresAfter") LocalDateTime tokenExpiresAfter,
                                 @Param("tokenExpiresBefore") LocalDateTime tokenExpiresBefore,
                                 Pageable pageable);

    /**
     * Finds sessions that have token hashes stored.
     * Used for identifying sessions with complete token information.
     * 
     * @return List of sessions with both access and refresh token hashes
     */
    @Query("SELECT s FROM Session s WHERE s.accessTokenHash IS NOT NULL AND s.refreshTokenHash IS NOT NULL")
    List<Session> findSessionsWithTokenHashes();

    /**
     * Finds sessions missing token hashes.
     * Used for data integrity checks and cleanup.
     * 
     * @return List of sessions missing token hash information
     */
    @Query("SELECT s FROM Session s WHERE s.accessTokenHash IS NULL OR s.refreshTokenHash IS NULL")
    List<Session> findSessionsWithoutTokenHashes();

    /**
     * Updates token hashes for a specific session.
     * Used when tokens are refreshed or rotated.
     * 
     * @param sessionId the ID of the session to update
     * @param accessTokenHash the new access token hash
     * @param refreshTokenHash the new refresh token hash
     * @param newExpiration the new token expiration time
     * @return number of sessions updated (should be 0 or 1)
     */
    @Modifying
    @Query("UPDATE Session s SET s.accessTokenHash = :accessTokenHash, " +
           "s.refreshTokenHash = :refreshTokenHash, s.tokenExpiration = :newExpiration " +
           "WHERE s.id = :sessionId")
    int updateSessionTokens(@Param("sessionId") UUID sessionId,
                           @Param("accessTokenHash") String accessTokenHash,
                           @Param("refreshTokenHash") String refreshTokenHash,
                           @Param("newExpiration") LocalDateTime newExpiration);

    /**
     * Updates token expiration for a specific session.
     * Used when extending session lifetime.
     * 
     * @param sessionId the ID of the session to update
     * @param newExpiration the new token expiration time
     * @return number of sessions updated (should be 0 or 1)
     */
    @Modifying
    @Query("UPDATE Session s SET s.tokenExpiration = :newExpiration WHERE s.id = :sessionId")
    int updateSessionExpiration(@Param("sessionId") UUID sessionId,
                               @Param("newExpiration") LocalDateTime newExpiration);
}