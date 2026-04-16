package com.company.assetmanagement.repository;

import com.company.assetmanagement.model.Role;
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
 * Repository interface for User entity operations.
 * 
 * Provides comprehensive data access methods for user management including:
 * - Standard CRUD operations through JpaRepository
 * - Custom finder methods for username, email, and role-based queries
 * - Pagination and sorting support for user listing
 * - Account status and security-related queries
 * - Bulk operations for user management
 * 
 * Security Features:
 * - Username and email uniqueness validation
 * - Account status filtering (active/inactive, locked/unlocked)
 * - Role-based user queries
 * - Failed login attempt tracking
 * 
 * Performance Optimizations:
 * - Indexed queries on frequently searched fields
 * - Pagination support for large user lists
 * - Optimized queries with JOIN FETCH for role loading
 * - Bulk update operations for efficiency
 * 
 * @author IT Asset Management System
 * @version 1.0
 * @since 2024-01-15
 */
@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    // ========================================================================
    // Username-based queries
    // ========================================================================

    /**
     * Finds a user by their username.
     * Used for authentication and user lookup operations.
     * 
     * @param username the username to search for (case-sensitive)
     * @return Optional containing the user if found, empty otherwise
     */
    Optional<User> findByUsername(String username);

    /**
     * Finds a user by their username with roles eagerly loaded.
     * Optimized query that fetches user and their roles in a single query.
     * Used when role information is needed immediately.
     * 
     * @param username the username to search for
     * @return Optional containing the user with roles if found, empty otherwise
     */
    @Query("SELECT u FROM User u LEFT JOIN FETCH u.roles r WHERE u.username = :username")
    Optional<User> findByUsernameWithRoles(@Param("username") String username);

    /**
     * Checks if a username already exists in the system.
     * Used for username uniqueness validation during user creation and updates.
     * 
     * @param username the username to check
     * @return true if username exists, false otherwise
     */
    boolean existsByUsername(String username);

    /**
     * Checks if a username exists for a different user (excluding specified user ID).
     * Used for username uniqueness validation during user updates.
     * 
     * @param username the username to check
     * @param userId the user ID to exclude from the check
     * @return true if username exists for a different user, false otherwise
     */
    @Query("SELECT COUNT(u) > 0 FROM User u WHERE u.username = :username AND u.id != :userId")
    boolean existsByUsernameAndIdNot(@Param("username") String username, @Param("userId") UUID userId);

    // ========================================================================
    // Email-based queries
    // ========================================================================

    /**
     * Finds a user by their email address.
     * Used for user lookup and email-based operations.
     * 
     * @param email the email address to search for (case-insensitive)
     * @return Optional containing the user if found, empty otherwise
     */
    Optional<User> findByEmailIgnoreCase(String email);

    /**
     * Checks if an email address already exists in the system.
     * Used for email uniqueness validation during user creation and updates.
     * 
     * @param email the email address to check (case-insensitive)
     * @return true if email exists, false otherwise
     */
    boolean existsByEmailIgnoreCase(String email);

    /**
     * Checks if an email exists for a different user (excluding specified user ID).
     * Used for email uniqueness validation during user updates.
     * 
     * @param email the email address to check
     * @param userId the user ID to exclude from the check
     * @return true if email exists for a different user, false otherwise
     */
    @Query("SELECT COUNT(u) > 0 FROM User u WHERE LOWER(u.email) = LOWER(:email) AND u.id != :userId")
    boolean existsByEmailIgnoreCaseAndIdNot(@Param("email") String email, @Param("userId") UUID userId);

    // ========================================================================
    // Account status queries
    // ========================================================================

    /**
     * Finds all active users with pagination support.
     * Used for listing active users in the system.
     * 
     * @param pageable pagination parameters
     * @return Page of active users
     */
    Page<User> findByIsActiveTrue(Pageable pageable);

    /**
     * Finds all inactive users with pagination support.
     * Used for listing inactive users in the system.
     * 
     * @param pageable pagination parameters
     * @return Page of inactive users
     */
    Page<User> findByIsActiveFalse(Pageable pageable);

    /**
     * Finds all locked users.
     * Used for administrative monitoring of locked accounts.
     * 
     * @return List of locked users
     */
    List<User> findByAccountLockedTrue();

    /**
     * Finds users whose account lock has expired.
     * Used for automatic account unlocking processes.
     * 
     * @param currentTime the current timestamp
     * @return List of users whose lock has expired
     */
    @Query("SELECT u FROM User u WHERE u.accountLocked = true AND u.lockUntil <= :currentTime")
    List<User> findUsersWithExpiredLocks(@Param("currentTime") LocalDateTime currentTime);

    /**
     * Finds users with failed login attempts above a threshold.
     * Used for security monitoring and analysis.
     * 
     * @param threshold the minimum number of failed attempts
     * @return List of users with high failed login attempts
     */
    List<User> findByFailedLoginAttemptsGreaterThanEqual(Integer threshold);

    // ========================================================================
    // Role-based queries
    // ========================================================================

    /**
     * Finds users who have a specific role with pagination support.
     * Uses JOIN query to efficiently filter users by role.
     * 
     * @param role the role to filter by
     * @param pageable pagination parameters
     * @return Page of users with the specified role
     */
    @Query("SELECT DISTINCT u FROM User u JOIN u.roles r WHERE r.role = :role")
    Page<User> findByRole(@Param("role") Role role, Pageable pageable);

    /**
     * Finds users who have any of the specified roles with pagination support.
     * Used for finding users with multiple possible roles.
     * 
     * @param roles the list of roles to filter by
     * @param pageable pagination parameters
     * @return Page of users with any of the specified roles
     */
    @Query("SELECT DISTINCT u FROM User u JOIN u.roles r WHERE r.role IN :roles")
    Page<User> findByRoleIn(@Param("roles") List<Role> roles, Pageable pageable);

    /**
     * Finds active users who have a specific role.
     * Combines role filtering with active status filtering.
     * 
     * @param role the role to filter by
     * @param pageable pagination parameters
     * @return Page of active users with the specified role
     */
    @Query("SELECT DISTINCT u FROM User u JOIN u.roles r WHERE r.role = :role AND u.isActive = true")
    Page<User> findActiveUsersByRole(@Param("role") Role role, Pageable pageable);

    /**
     * Counts users by role.
     * Used for dashboard metrics and reporting.
     * 
     * @param role the role to count
     * @return number of users with the specified role
     */
    @Query("SELECT COUNT(DISTINCT u) FROM User u JOIN u.roles r WHERE r.role = :role")
    long countByRole(@Param("role") Role role);

    // ========================================================================
    // Search and filtering queries
    // ========================================================================

    /**
     * Searches users by text across username, email fields with pagination.
     * Case-insensitive search across multiple fields.
     * 
     * @param searchText the text to search for
     * @param pageable pagination parameters
     * @return Page of users matching the search criteria
     */
    @Query("SELECT u FROM User u WHERE " +
           "LOWER(u.username) LIKE LOWER(CONCAT('%', :searchText, '%')) OR " +
           "LOWER(u.email) LIKE LOWER(CONCAT('%', :searchText, '%'))")
    Page<User> findBySearchText(@Param("searchText") String searchText, Pageable pageable);

    /**
     * Searches active users by text with pagination.
     * Combines text search with active status filtering.
     * 
     * @param searchText the text to search for
     * @param pageable pagination parameters
     * @return Page of active users matching the search criteria
     */
    @Query("SELECT u FROM User u WHERE u.isActive = true AND (" +
           "LOWER(u.username) LIKE LOWER(CONCAT('%', :searchText, '%')) OR " +
           "LOWER(u.email) LIKE LOWER(CONCAT('%', :searchText, '%')))")
    Page<User> findActiveUsersBySearchText(@Param("searchText") String searchText, Pageable pageable);

    /**
     * Advanced search with multiple filters.
     * Supports filtering by active status, role, and text search.
     * 
     * @param searchText optional text to search for (can be null)
     * @param isActive optional active status filter (can be null)
     * @param role optional role filter (can be null)
     * @param pageable pagination parameters
     * @return Page of users matching all specified criteria
     */
    @Query("SELECT DISTINCT u FROM User u LEFT JOIN u.roles r WHERE " +
           "(:searchText IS NULL OR " +
           " LOWER(u.username) LIKE LOWER(CONCAT('%', :searchText, '%')) OR " +
           " LOWER(u.email) LIKE LOWER(CONCAT('%', :searchText, '%'))) AND " +
           "(:isActive IS NULL OR u.isActive = :isActive) AND " +
           "(:role IS NULL OR r.role = :role)")
    Page<User> findWithFilters(@Param("searchText") String searchText,
                              @Param("isActive") Boolean isActive,
                              @Param("role") Role role,
                              Pageable pageable);

    // ========================================================================
    // Bulk update operations
    // ========================================================================

    /**
     * Updates the last login timestamp for a user.
     * Optimized bulk update operation for authentication.
     * 
     * @param userId the user ID
     * @param lastLoginAt the new last login timestamp
     * @return number of rows updated (should be 1)
     */
    @Modifying
    @Query("UPDATE User u SET u.lastLoginAt = :lastLoginAt, u.updatedAt = CURRENT_TIMESTAMP " +
           "WHERE u.id = :userId")
    int updateLastLoginAt(@Param("userId") UUID userId, @Param("lastLoginAt") LocalDateTime lastLoginAt);

    /**
     * Resets failed login attempts for a user.
     * Used after successful authentication.
     * 
     * @param userId the user ID
     * @return number of rows updated (should be 1)
     */
    @Modifying
    @Query("UPDATE User u SET u.failedLoginAttempts = 0, u.updatedAt = CURRENT_TIMESTAMP " +
           "WHERE u.id = :userId")
    int resetFailedLoginAttempts(@Param("userId") UUID userId);

    /**
     * Increments failed login attempts for a user.
     * Used after failed authentication.
     * 
     * @param userId the user ID
     * @return number of rows updated (should be 1)
     */
    @Modifying
    @Query("UPDATE User u SET u.failedLoginAttempts = u.failedLoginAttempts + 1, " +
           "u.updatedAt = CURRENT_TIMESTAMP WHERE u.id = :userId")
    int incrementFailedLoginAttempts(@Param("userId") UUID userId);

    /**
     * Locks a user account until the specified time.
     * Sets accountLocked to true and lockUntil to the specified time.
     * 
     * @param userId the user ID
     * @param lockUntil the timestamp until which the account is locked
     * @return number of rows updated (should be 1)
     */
    @Modifying
    @Query("UPDATE User u SET u.accountLocked = true, u.lockUntil = :lockUntil, " +
           "u.updatedAt = CURRENT_TIMESTAMP WHERE u.id = :userId")
    int lockUserAccount(@Param("userId") UUID userId, @Param("lockUntil") LocalDateTime lockUntil);

    /**
     * Unlocks user accounts whose lock time has expired.
     * Bulk operation to automatically unlock expired account locks.
     * 
     * @param currentTime the current timestamp
     * @return number of accounts unlocked
     */
    @Modifying
    @Query("UPDATE User u SET u.accountLocked = false, u.lockUntil = null, " +
           "u.failedLoginAttempts = 0, u.updatedAt = CURRENT_TIMESTAMP " +
           "WHERE u.accountLocked = true AND u.lockUntil <= :currentTime")
    int unlockExpiredAccounts(@Param("currentTime") LocalDateTime currentTime);

    /**
     * Updates user active status.
     * Used for enabling/disabling user accounts.
     * 
     * @param userId the user ID
     * @param isActive the new active status
     * @param updatedBy the user performing the update
     * @return number of rows updated (should be 1)
     */
    @Modifying
    @Query("UPDATE User u SET u.isActive = :isActive, u.updatedBy = :updatedBy, " +
           "u.updatedAt = CURRENT_TIMESTAMP WHERE u.id = :userId")
    int updateUserActiveStatus(@Param("userId") UUID userId, 
                              @Param("isActive") Boolean isActive,
                              @Param("updatedBy") User updatedBy);

    // ========================================================================
    // Statistics and reporting queries
    // ========================================================================

    /**
     * Counts total number of active users.
     * Used for dashboard metrics.
     * 
     * @return number of active users
     */
    long countByIsActiveTrue();

    /**
     * Counts total number of inactive users.
     * Used for dashboard metrics.
     * 
     * @return number of inactive users
     */
    long countByIsActiveFalse();

    /**
     * Counts users created within a date range.
     * Used for reporting and analytics.
     * 
     * @param startDate the start date (inclusive)
     * @param endDate the end date (inclusive)
     * @return number of users created in the date range
     */
    @Query("SELECT COUNT(u) FROM User u WHERE u.createdAt >= :startDate AND u.createdAt <= :endDate")
    long countUsersCreatedBetween(@Param("startDate") LocalDateTime startDate, 
                                 @Param("endDate") LocalDateTime endDate);

    /**
     * Finds users who have never logged in.
     * Used for identifying inactive accounts.
     * 
     * @param pageable pagination parameters
     * @return Page of users who have never logged in
     */
    Page<User> findByLastLoginAtIsNull(Pageable pageable);

    /**
     * Finds users who haven't logged in since a specific date.
     * Used for identifying stale accounts.
     * 
     * @param cutoffDate the cutoff date
     * @param pageable pagination parameters
     * @return Page of users who haven't logged in since the cutoff date
     */
    @Query("SELECT u FROM User u WHERE u.lastLoginAt < :cutoffDate OR u.lastLoginAt IS NULL")
    Page<User> findUsersNotLoggedInSince(@Param("cutoffDate") LocalDateTime cutoffDate, 
                                        Pageable pageable);

    // ========================================================================
    // Administrative queries
    // ========================================================================

    /**
     * Finds all users created by a specific user.
     * Used for audit trail and administrative oversight.
     * 
     * @param createdBy the user who created the accounts
     * @param pageable pagination parameters
     * @return Page of users created by the specified user
     */
    Page<User> findByCreatedBy(User createdBy, Pageable pageable);

    /**
     * Finds all users updated by a specific user.
     * Used for audit trail and change tracking.
     * 
     * @param updatedBy the user who updated the accounts
     * @param pageable pagination parameters
     * @return Page of users updated by the specified user
     */
    Page<User> findByUpdatedBy(User updatedBy, Pageable pageable);

    /**
     * Finds users created within the last N days.
     * Used for recent activity monitoring.
     * 
     * @param daysAgo the number of days to look back
     * @return List of recently created users
     */
    @Query("SELECT u FROM User u WHERE u.createdAt >= :cutoffDate ORDER BY u.createdAt DESC")
    List<User> findRecentlyCreatedUsers(@Param("cutoffDate") LocalDateTime cutoffDate);

    /**
     * Custom query to find users with specific criteria for advanced filtering.
     * This method provides maximum flexibility for complex user searches.
     * 
     * @param username optional username filter (exact match)
     * @param email optional email filter (exact match, case-insensitive)
     * @param isActive optional active status filter
     * @param accountLocked optional locked status filter
     * @param hasRole optional role filter
     * @param createdAfter optional creation date filter (after this date)
     * @param createdBefore optional creation date filter (before this date)
     * @param pageable pagination parameters
     * @return Page of users matching the specified criteria
     */
    @Query("SELECT DISTINCT u FROM User u LEFT JOIN u.roles r WHERE " +
           "(:username IS NULL OR u.username = :username) AND " +
           "(:email IS NULL OR LOWER(u.email) = LOWER(:email)) AND " +
           "(:isActive IS NULL OR u.isActive = :isActive) AND " +
           "(:accountLocked IS NULL OR u.accountLocked = :accountLocked) AND " +
           "(:hasRole IS NULL OR r.role = :hasRole) AND " +
           "(:createdAfter IS NULL OR u.createdAt >= :createdAfter) AND " +
           "(:createdBefore IS NULL OR u.createdAt <= :createdBefore)")
    Page<User> findWithAdvancedFilters(@Param("username") String username,
                                      @Param("email") String email,
                                      @Param("isActive") Boolean isActive,
                                      @Param("accountLocked") Boolean accountLocked,
                                      @Param("hasRole") Role hasRole,
                                      @Param("createdAfter") LocalDateTime createdAfter,
                                      @Param("createdBefore") LocalDateTime createdBefore,
                                      Pageable pageable);
}
