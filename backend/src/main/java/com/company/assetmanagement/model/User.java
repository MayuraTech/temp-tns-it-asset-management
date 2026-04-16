package com.company.assetmanagement.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/**
 * User entity representing a system user with authentication and authorization capabilities.
 * 
 * This entity implements comprehensive user management functionality including:
 * - User authentication with password hashing
 * - Account security features (locking, failed login tracking)
 * - Role-based access control through UserRole relationships
 * - Audit trail with creation and modification tracking
 * - Session management support
 * 
 * Security Features:
 * - Passwords are stored as BCrypt hashes (never plain text)
 * - Account locking after 5 failed login attempts for 30 minutes
 * - Username and email uniqueness enforcement
 * - Input validation for all fields
 * 
 * @author IT Asset Management System
 * @version 1.0
 * @since 2024-01-15
 */
@Entity
@Table(name = "Users", indexes = {
    @Index(name = "IX_Users_Username", columnList = "username"),
    @Index(name = "IX_Users_Email", columnList = "email"),
    @Index(name = "IX_Users_AccountLocked", columnList = "accountLocked"),
    @Index(name = "IX_Users_IsActive", columnList = "isActive"),
    @Index(name = "IX_Users_CreatedAt", columnList = "createdAt"),
    @Index(name = "IX_Users_UpdatedAt", columnList = "updatedAt"),
    @Index(name = "IX_Users_IsActive_AccountLocked", columnList = "isActive, accountLocked")
})
@EntityListeners(AuditingEntityListener.class)
public class User {

    /**
     * Unique identifier for the user.
     * Generated automatically using UUID strategy.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "Id", updatable = false, nullable = false)
    private UUID id;

    /**
     * Unique username for the user.
     * Must be 3-100 characters, alphanumeric and underscores only.
     * Used for authentication and system identification.
     */
    @Column(name = "Username", nullable = false, unique = true, length = 100)
    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 100, message = "Username must be between 3 and 100 characters")
    @Pattern(
        regexp = "^[a-zA-Z0-9_]+$", 
        message = "Username must contain only alphanumeric characters and underscores"
    )
    private String username;

    /**
     * BCrypt hashed password for authentication.
     * Never stored in plain text. Minimum strength 10.
     * Excluded from toString() method for security.
     */
    @Column(name = "PasswordHash", nullable = false, length = 255)
    @NotBlank(message = "Password hash is required")
    @Size(max = 255, message = "Password hash must not exceed 255 characters")
    private String passwordHash;

    /**
     * User's email address.
     * Must be unique across all users and follow valid email format.
     * Used for notifications and account recovery.
     */
    @Column(name = "Email", nullable = false, unique = true, length = 255)
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Size(min = 5, max = 255, message = "Email must be between 5 and 255 characters")
    private String email;

    /**
     * Account active status.
     * When false, user cannot authenticate or perform any operations.
     * Defaults to true for new accounts.
     */
    @Column(name = "IsActive", nullable = false)
    @NotNull(message = "Account active status is required")
    private Boolean isActive = true;

    /**
     * Account locked status.
     * Set to true after 5 consecutive failed login attempts.
     * Automatically reset to false after lockUntil time expires.
     */
    @Column(name = "AccountLocked", nullable = false)
    @NotNull(message = "Account locked status is required")
    private Boolean accountLocked = false;

    /**
     * Timestamp until which the account remains locked.
     * Set to current time + 30 minutes when account is locked.
     * Null when account is not locked.
     */
    @Column(name = "LockUntil")
    private LocalDateTime lockUntil;

    /**
     * Counter for consecutive failed login attempts.
     * Incremented on each failed login, reset to 0 on successful login.
     * Account is locked when this reaches 5.
     */
    @Column(name = "FailedLoginAttempts", nullable = false)
    @NotNull(message = "Failed login attempts counter is required")
    @Min(value = 0, message = "Failed login attempts cannot be negative")
    private Integer failedLoginAttempts = 0;

    /**
     * Timestamp of the user's last successful login.
     * Updated on each successful authentication.
     * Null for users who have never logged in.
     */
    @Column(name = "LastLoginAt")
    private LocalDateTime lastLoginAt;

    /**
     * Timestamp when the user account was created.
     * Automatically set by Spring Data JPA auditing.
     * Immutable after creation.
     */
    @CreatedDate
    @Column(name = "CreatedAt", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Timestamp when the user account was last updated.
     * Automatically updated by Spring Data JPA auditing on any field change.
     */
    @LastModifiedDate
    @Column(name = "UpdatedAt", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * Reference to the user who created this account.
     * Used for audit trail and accountability.
     * Can be null for system-created accounts.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CreatedBy", referencedColumnName = "Id")
    private User createdBy;

    /**
     * Reference to the user who last updated this account.
     * Used for audit trail and change tracking.
     * Updated on every modification.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "UpdatedBy", referencedColumnName = "Id")
    private User updatedBy;

    /**
     * Set of roles assigned to this user.
     * Defines the user's permissions and access levels.
     * Cascade operations ensure role assignments are managed with the user.
     */
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<UserRole> roles = new HashSet<>();

    /**
     * Set of sessions associated with this user.
     * Tracks active and historical login sessions.
     * Cascade delete ensures sessions are cleaned up when user is deleted.
     */
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<Session> sessions = new HashSet<>();

    // ========================================================================
    // Constructors
    // ========================================================================

    /**
     * Default constructor for JPA.
     */
    public User() {
    }

    /**
     * Constructor for creating a new user with required fields.
     * 
     * @param username the unique username
     * @param passwordHash the BCrypt hashed password
     * @param email the unique email address
     */
    public User(String username, String passwordHash, String email) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.email = email;
        this.isActive = true;
        this.accountLocked = false;
        this.failedLoginAttempts = 0;
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

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public Boolean getAccountLocked() {
        return accountLocked;
    }

    public void setAccountLocked(Boolean accountLocked) {
        this.accountLocked = accountLocked;
    }

    public LocalDateTime getLockUntil() {
        return lockUntil;
    }

    public void setLockUntil(LocalDateTime lockUntil) {
        this.lockUntil = lockUntil;
    }

    public Integer getFailedLoginAttempts() {
        return failedLoginAttempts;
    }

    public void setFailedLoginAttempts(Integer failedLoginAttempts) {
        this.failedLoginAttempts = failedLoginAttempts;
    }

    public LocalDateTime getLastLoginAt() {
        return lastLoginAt;
    }

    public void setLastLoginAt(LocalDateTime lastLoginAt) {
        this.lastLoginAt = lastLoginAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public User getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(User createdBy) {
        this.createdBy = createdBy;
    }

    public User getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(User updatedBy) {
        this.updatedBy = updatedBy;
    }

    public Set<UserRole> getRoles() {
        return roles;
    }

    public void setRoles(Set<UserRole> roles) {
        this.roles = roles;
    }

    public Set<Session> getSessions() {
        return sessions;
    }

    public void setSessions(Set<Session> sessions) {
        this.sessions = sessions;
    }

    // ========================================================================
    // Business Logic Methods
    // ========================================================================

    /**
     * Checks if the account is currently locked.
     * An account is considered locked if accountLocked is true and lockUntil is in the future.
     * 
     * @return true if the account is currently locked
     */
    public boolean isCurrentlyLocked() {
        return accountLocked && lockUntil != null && lockUntil.isAfter(LocalDateTime.now());
    }

    /**
     * Locks the account for the specified duration.
     * Sets accountLocked to true and lockUntil to current time + duration.
     * 
     * @param lockDurationMinutes the duration to lock the account in minutes
     */
    public void lockAccount(int lockDurationMinutes) {
        this.accountLocked = true;
        this.lockUntil = LocalDateTime.now().plusMinutes(lockDurationMinutes);
    }

    /**
     * Unlocks the account.
     * Sets accountLocked to false, clears lockUntil, and resets failed login attempts.
     */
    public void unlockAccount() {
        this.accountLocked = false;
        this.lockUntil = null;
        this.failedLoginAttempts = 0;
    }

    /**
     * Increments the failed login attempts counter.
     * If the counter reaches 5, the account is automatically locked for 30 minutes.
     */
    public void incrementFailedLoginAttempts() {
        this.failedLoginAttempts++;
        if (this.failedLoginAttempts >= 5) {
            lockAccount(30); // Lock for 30 minutes
        }
    }

    /**
     * Resets the failed login attempts counter to 0.
     * Called on successful login.
     */
    public void resetFailedLoginAttempts() {
        this.failedLoginAttempts = 0;
    }

    /**
     * Updates the last login timestamp to current time.
     * Called on successful authentication.
     */
    public void updateLastLogin() {
        this.lastLoginAt = LocalDateTime.now();
    }

    /**
     * Checks if the user has a specific role.
     * 
     * @param role the role to check
     * @return true if the user has the specified role
     */
    public boolean hasRole(Role role) {
        return roles.stream()
                .anyMatch(userRole -> userRole.getRole() == role);
    }

    /**
     * Gets all role names for this user.
     * 
     * @return set of role names
     */
    public Set<Role> getRoleNames() {
        return roles.stream()
                .map(UserRole::getRole)
                .collect(java.util.stream.Collectors.toSet());
    }

    /**
     * Adds a role to this user.
     * Creates a new UserRole association.
     * 
     * @param userRole the UserRole to add
     */
    public void addRole(UserRole userRole) {
        roles.add(userRole);
        userRole.setUser(this);
    }

    /**
     * Removes a role from this user.
     * 
     * @param userRole the UserRole to remove
     */
    public void removeRole(UserRole userRole) {
        roles.remove(userRole);
        userRole.setUser(null);
    }

    /**
     * Adds a session to this user.
     * 
     * @param session the Session to add
     */
    public void addSession(Session session) {
        sessions.add(session);
        session.setUser(this);
    }

    /**
     * Removes a session from this user.
     * 
     * @param session the Session to remove
     */
    public void removeSession(Session session) {
        sessions.remove(session);
        session.setUser(null);
    }

    // ========================================================================
    // Object Methods (equals, hashCode, toString)
    // ========================================================================

    /**
     * Equals method based on username for business equality.
     * Two users are considered equal if they have the same username.
     * 
     * @param obj the object to compare
     * @return true if the objects are equal
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        User user = (User) obj;
        return Objects.equals(username, user.username);
    }

    /**
     * Hash code based on username for consistency with equals.
     * 
     * @return hash code value
     */
    @Override
    public int hashCode() {
        return Objects.hash(username);
    }

    /**
     * String representation of the user.
     * Excludes password hash for security reasons.
     * Includes key identifying information and status.
     * 
     * @return string representation
     */
    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", isActive=" + isActive +
                ", accountLocked=" + accountLocked +
                ", lockUntil=" + lockUntil +
                ", failedLoginAttempts=" + failedLoginAttempts +
                ", lastLoginAt=" + lastLoginAt +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                ", rolesCount=" + (roles != null ? roles.size() : 0) +
                '}';
    }
}