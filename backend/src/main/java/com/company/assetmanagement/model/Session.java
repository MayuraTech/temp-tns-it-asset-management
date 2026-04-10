package com.company.assetmanagement.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Session entity representing an authenticated user session.
 * 
 * This entity tracks user authentication sessions and JWT token information:
 * - Session lifecycle (login, logout, expiration)
 * - JWT token management (access and refresh tokens)
 * - Security tracking for audit and session management
 * 
 * Security Features:
 * - Token hashes stored for validation (not actual tokens)
 * - Session expiration tracking
 * - Automatic cleanup of expired sessions
 * - Session invalidation on logout, password change, role change
 * 
 * Business Rules:
 * - Each login creates a new session
 * - Sessions are invalidated on logout
 * - Expired sessions are automatically marked inactive
 * - Token hashes are stored for security validation
 * 
 * @author IT Asset Management System
 * @version 1.0
 * @since 2024-01-15
 */
@Entity
@Table(name = "Sessions", indexes = {
    @Index(name = "IX_Sessions_UserId", columnList = "userId"),
    @Index(name = "IX_Sessions_TokenExpiration", columnList = "tokenExpiration"),
    @Index(name = "IX_Sessions_IsActive", columnList = "isActive"),
    @Index(name = "IX_Sessions_LoginAt", columnList = "loginAt"),
    @Index(name = "IX_Sessions_UserId_IsActive", columnList = "userId, isActive")
})
public class Session {

    /**
     * Unique identifier for the session.
     * Generated automatically using UUID strategy.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "Id", updatable = false, nullable = false)
    private UUID id;

    /**
     * Reference to the user who owns this session.
     * Cannot be null - every session must be associated with a user.
     * Lazy loading to avoid unnecessary queries.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "UserId", nullable = false, referencedColumnName = "Id")
    @NotNull(message = "User is required for session")
    private User user;

    /**
     * Timestamp when the user logged in and the session was created.
     * Set automatically when the session is created.
     * Immutable after creation.
     */
    @Column(name = "LoginAt", nullable = false, updatable = false)
    @NotNull(message = "Login timestamp is required")
    private LocalDateTime loginAt;

    /**
     * Timestamp when the user logged out and the session was terminated.
     * Null for active sessions, set when logout occurs.
     * Used to track session duration and cleanup.
     */
    @Column(name = "LogoutAt")
    private LocalDateTime logoutAt;

    /**
     * Timestamp when the JWT access token expires.
     * Used to determine if the session is still valid.
     * Typically set to loginAt + 30 minutes.
     */
    @Column(name = "TokenExpiration", nullable = false)
    @NotNull(message = "Token expiration is required")
    private LocalDateTime tokenExpiration;

    /**
     * Indicates if the session is currently active.
     * Set to false when session is terminated (logout, expiration, invalidation).
     * Used for quick filtering of active sessions.
     */
    @Column(name = "IsActive", nullable = false)
    @NotNull(message = "Session active status is required")
    private Boolean isActive = true;

    /**
     * Hash of the JWT access token for validation.
     * Stored for security validation without exposing the actual token.
     * Null if no access token is associated with this session.
     */
    @Column(name = "AccessTokenHash", length = 500)
    @Size(max = 500, message = "Access token hash must not exceed 500 characters")
    private String accessTokenHash;

    /**
     * Hash of the JWT refresh token for validation.
     * Stored for security validation without exposing the actual token.
     * Null if no refresh token is associated with this session.
     */
    @Column(name = "RefreshTokenHash", length = 500)
    @Size(max = 500, message = "Refresh token hash must not exceed 500 characters")
    private String refreshTokenHash;

    // ========================================================================
    // Constructors
    // ========================================================================

    /**
     * Default constructor for JPA.
     */
    public Session() {
    }

    /**
     * Constructor for creating a new session.
     * 
     * @param user the user who owns the session
     * @param tokenExpiration when the session token expires
     */
    public Session(User user, LocalDateTime tokenExpiration) {
        this.user = user;
        this.loginAt = LocalDateTime.now();
        this.tokenExpiration = tokenExpiration;
        this.isActive = true;
    }

    /**
     * Constructor for creating a new session with token hashes.
     * 
     * @param user the user who owns the session
     * @param tokenExpiration when the session token expires
     * @param accessTokenHash hash of the access token
     * @param refreshTokenHash hash of the refresh token
     */
    public Session(User user, LocalDateTime tokenExpiration, String accessTokenHash, String refreshTokenHash) {
        this.user = user;
        this.loginAt = LocalDateTime.now();
        this.tokenExpiration = tokenExpiration;
        this.isActive = true;
        this.accessTokenHash = accessTokenHash;
        this.refreshTokenHash = refreshTokenHash;
    }

    // ========================================================================
    // Getters and Setters
    // ========================================================================

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public LocalDateTime getLoginAt() {
        return loginAt;
    }

    public void setLoginAt(LocalDateTime loginAt) {
        this.loginAt = loginAt;
    }

    public LocalDateTime getLogoutAt() {
        return logoutAt;
    }

    public void setLogoutAt(LocalDateTime logoutAt) {
        this.logoutAt = logoutAt;
    }

    public LocalDateTime getTokenExpiration() {
        return tokenExpiration;
    }

    public void setTokenExpiration(LocalDateTime tokenExpiration) {
        this.tokenExpiration = tokenExpiration;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public String getAccessTokenHash() {
        return accessTokenHash;
    }

    public void setAccessTokenHash(String accessTokenHash) {
        this.accessTokenHash = accessTokenHash;
    }

    public String getRefreshTokenHash() {
        return refreshTokenHash;
    }

    public void setRefreshTokenHash(String refreshTokenHash) {
        this.refreshTokenHash = refreshTokenHash;
    }

    // ========================================================================
    // Business Logic Methods
    // ========================================================================

    /**
     * Checks if the session is currently expired.
     * A session is expired if the token expiration time has passed.
     * 
     * @return true if the session is expired
     */
    public boolean isExpired() {
        return tokenExpiration != null && tokenExpiration.isBefore(LocalDateTime.now());
    }

    /**
     * Checks if the session is currently valid.
     * A session is valid if it's active and not expired.
     * 
     * @return true if the session is valid
     */
    public boolean isValid() {
        return isActive && !isExpired();
    }

    /**
     * Terminates the session by setting it inactive and recording logout time.
     * Called when user logs out or session is invalidated.
     */
    public void terminate() {
        this.isActive = false;
        this.logoutAt = LocalDateTime.now();
    }

    /**
     * Marks the session as expired by setting it inactive.
     * Called when the token expiration time is reached.
     */
    public void markExpired() {
        this.isActive = false;
        // Don't set logoutAt for expired sessions - they weren't explicitly logged out
    }

    /**
     * Gets the duration of the session in minutes.
     * If the session is still active, calculates from login to now.
     * If terminated, calculates from login to logout.
     * 
     * @return session duration in minutes
     */
    public long getSessionDurationMinutes() {
        LocalDateTime endTime = logoutAt != null ? logoutAt : LocalDateTime.now();
        return java.time.Duration.between(loginAt, endTime).toMinutes();
    }

    /**
     * Gets the remaining time until token expiration in minutes.
     * Returns 0 if already expired.
     * 
     * @return minutes until expiration, or 0 if expired
     */
    public long getMinutesUntilExpiration() {
        if (isExpired()) {
            return 0;
        }
        return java.time.Duration.between(LocalDateTime.now(), tokenExpiration).toMinutes();
    }

    /**
     * Gets the username of the user who owns this session.
     * 
     * @return the username or null if user is not set
     */
    public String getUserUsername() {
        return user != null ? user.getUsername() : null;
    }

    /**
     * Gets the user ID of the user who owns this session.
     * 
     * @return the user ID or null if user is not set
     */
    public UUID getUserId() {
        return user != null ? user.getId() : null;
    }

    /**
     * Updates the token expiration time.
     * Used when refreshing tokens to extend the session.
     * 
     * @param newExpiration the new expiration time
     */
    public void updateTokenExpiration(LocalDateTime newExpiration) {
        this.tokenExpiration = newExpiration;
    }

    /**
     * Updates the token hashes for this session.
     * Used when tokens are refreshed or rotated.
     * 
     * @param newAccessTokenHash the new access token hash
     * @param newRefreshTokenHash the new refresh token hash
     */
    public void updateTokenHashes(String newAccessTokenHash, String newRefreshTokenHash) {
        this.accessTokenHash = newAccessTokenHash;
        this.refreshTokenHash = newRefreshTokenHash;
    }

    /**
     * Checks if this session has token hashes stored.
     * 
     * @return true if both access and refresh token hashes are present
     */
    public boolean hasTokenHashes() {
        return accessTokenHash != null && refreshTokenHash != null;
    }

    // ========================================================================
    // Object Methods (equals, hashCode, toString)
    // ========================================================================

    /**
     * Equals method based on ID for entity equality.
     * Two Session entities are considered equal if they have the same ID.
     * 
     * @param obj the object to compare
     * @return true if the objects are equal
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        Session session = (Session) obj;
        return Objects.equals(id, session.id);
    }

    /**
     * Hash code based on ID for consistency with equals.
     * 
     * @return hash code value
     */
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    /**
     * String representation of the session.
     * Excludes token hashes for security reasons.
     * Includes key information for debugging and logging.
     * 
     * @return string representation
     */
    @Override
    public String toString() {
        return "Session{" +
                "id=" + id +
                ", user=" + (user != null ? user.getUsername() : null) +
                ", loginAt=" + loginAt +
                ", logoutAt=" + logoutAt +
                ", tokenExpiration=" + tokenExpiration +
                ", isActive=" + isActive +
                ", hasTokenHashes=" + hasTokenHashes() +
                ", isExpired=" + isExpired() +
                ", isValid=" + isValid() +
                '}';
    }
}