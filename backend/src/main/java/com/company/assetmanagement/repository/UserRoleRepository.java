package com.company.assetmanagement.repository;

import com.company.assetmanagement.model.Role;
import com.company.assetmanagement.model.User;
import com.company.assetmanagement.model.UserRole;
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
import java.util.Set;
import java.util.UUID;

/**
 * Repository interface for UserRole entity operations.
 * 
 * Provides comprehensive data access methods for role management including:
 * - Role assignment and revocation operations
 * - Finder methods for roles by user and users by role
 * - Cascade delete handling for user deletion
 * - Role validation and business rule enforcement
 * - Audit trail support for role changes
 * 
 * Business Rules Enforced:
 * - Users must have at least one role (minimum role requirement)
 * - No duplicate role assignments per user (uniqueness constraint)
 * - Role assignments are tracked with assigner and timestamp
 * - Cascade delete removes all role assignments when user is deleted
 * 
 * Performance Optimizations:
 * - Indexed queries on frequently searched fields (userId, role)
 * - Batch operations for bulk role management
 * - Optimized queries with proper JOIN strategies
 * - Pagination support for large result sets
 * 
 * @author IT Asset Management System
 * @version 1.0
 * @since 2024-01-15
 */
@Repository
public interface UserRoleRepository extends JpaRepository<UserRole, UUID> {

    // ========================================================================
    // Role assignment and revocation operations
    // ========================================================================

    /**
     * Finds a specific role assignment for a user.
     * Used to check if a user already has a specific role before assignment.
     * 
     * @param user the user to check
     * @param role the role to check
     * @return Optional containing the UserRole if found, empty otherwise
     */
    Optional<UserRole> findByUserAndRole(User user, Role role);

    /**
     * Checks if a user has a specific role.
     * More efficient than findByUserAndRole when only existence check is needed.
     * 
     * @param user the user to check
     * @param role the role to check
     * @return true if the user has the specified role, false otherwise
     */
    boolean existsByUserAndRole(User user, Role role);

    /**
     * Deletes a specific role assignment for a user.
     * Used for role revocation operations.
     * Returns the number of deleted records (should be 0 or 1).
     * 
     * @param user the user whose role to revoke
     * @param role the role to revoke
     * @return number of deleted role assignments
     */
    @Modifying
    @Query("DELETE FROM UserRole ur WHERE ur.user = :user AND ur.role = :role")
    int deleteByUserAndRole(@Param("user") User user, @Param("role") Role role);

    // ========================================================================
    // Finder methods for roles by user
    // ========================================================================

    /**
     * Finds all role assignments for a specific user.
     * Returns the complete UserRole entities with assignment metadata.
     * 
     * @param user the user whose roles to retrieve
     * @return List of UserRole assignments for the user
     */
    List<UserRole> findByUser(User user);

    /**
     * Finds all role assignments for a specific user with pagination.
     * Useful when a user might have many role assignments.
     * 
     * @param user the user whose roles to retrieve
     * @param pageable pagination parameters
     * @return Page of UserRole assignments for the user
     */
    Page<UserRole> findByUser(User user, Pageable pageable);

    /**
     * Finds all roles for a specific user (returns only the Role enum values).
     * More efficient when only the role names are needed, not the full UserRole entities.
     * 
     * @param user the user whose roles to retrieve
     * @return Set of Role enums assigned to the user
     */
    @Query("SELECT ur.role FROM UserRole ur WHERE ur.user = :user")
    Set<Role> findRolesByUser(@Param("user") User user);

    /**
     * Finds all role assignments for a user by user ID.
     * Convenient method when only the user ID is available.
     * 
     * @param userId the ID of the user whose roles to retrieve
     * @return List of UserRole assignments for the user
     */
    @Query("SELECT ur FROM UserRole ur WHERE ur.user.id = :userId")
    List<UserRole> findByUserId(@Param("userId") UUID userId);

    /**
     * Counts the number of roles assigned to a specific user.
     * Used to enforce the minimum role requirement (must be >= 1).
     * 
     * @param user the user whose roles to count
     * @return number of roles assigned to the user
     */
    long countByUser(User user);

    /**
     * Counts the number of roles assigned to a user by user ID.
     * Convenient method when only the user ID is available.
     * 
     * @param userId the ID of the user whose roles to count
     * @return number of roles assigned to the user
     */
    @Query("SELECT COUNT(ur) FROM UserRole ur WHERE ur.user.id = :userId")
    long countByUserId(@Param("userId") UUID userId);

    // ========================================================================
    // Finder methods for users by role
    // ========================================================================

    /**
     * Finds all users who have a specific role.
     * Returns the complete UserRole entities with user and assignment metadata.
     * 
     * @param role the role to search for
     * @return List of UserRole assignments for the specified role
     */
    List<UserRole> findByRole(Role role);

    /**
     * Finds all users who have a specific role with pagination.
     * Useful for roles that might be assigned to many users.
     * 
     * @param role the role to search for
     * @param pageable pagination parameters
     * @return Page of UserRole assignments for the specified role
     */
    Page<UserRole> findByRole(Role role, Pageable pageable);

    /**
     * Finds all users who have a specific role (returns only the User entities).
     * More efficient when only the user information is needed.
     * 
     * @param role the role to search for
     * @return List of Users who have the specified role
     */
    @Query("SELECT ur.user FROM UserRole ur WHERE ur.role = :role")
    List<User> findUsersByRole(@Param("role") Role role);

    /**
     * Finds all active users who have a specific role.
     * Combines role filtering with active status filtering.
     * 
     * @param role the role to search for
     * @return List of active Users who have the specified role
     */
    @Query("SELECT ur.user FROM UserRole ur WHERE ur.role = :role AND ur.user.isActive = true")
    List<User> findActiveUsersByRole(@Param("role") Role role);

    /**
     * Finds all active users who have a specific role with pagination.
     * 
     * @param role the role to search for
     * @param pageable pagination parameters
     * @return Page of active Users who have the specified role
     */
    @Query("SELECT ur.user FROM UserRole ur WHERE ur.role = :role AND ur.user.isActive = true")
    Page<User> findActiveUsersByRole(@Param("role") Role role, Pageable pageable);

    /**
     * Counts the number of users who have a specific role.
     * Used for dashboard metrics and reporting.
     * 
     * @param role the role to count
     * @return number of users with the specified role
     */
    long countByRole(Role role);

    /**
     * Counts the number of active users who have a specific role.
     * 
     * @param role the role to count
     * @return number of active users with the specified role
     */
    @Query("SELECT COUNT(ur) FROM UserRole ur WHERE ur.role = :role AND ur.user.isActive = true")
    long countActiveUsersByRole(@Param("role") Role role);

    // ========================================================================
    // Finder methods for users with multiple roles
    // ========================================================================

    /**
     * Finds all users who have any of the specified roles.
     * Useful for finding users with multiple possible roles.
     * 
     * @param roles the list of roles to search for
     * @return List of Users who have any of the specified roles
     */
    @Query("SELECT DISTINCT ur.user FROM UserRole ur WHERE ur.role IN :roles")
    List<User> findUsersByRoleIn(@Param("roles") List<Role> roles);

    /**
     * Finds all users who have any of the specified roles with pagination.
     * 
     * @param roles the list of roles to search for
     * @param pageable pagination parameters
     * @return Page of Users who have any of the specified roles
     */
    @Query("SELECT DISTINCT ur.user FROM UserRole ur WHERE ur.role IN :roles")
    Page<User> findUsersByRoleIn(@Param("roles") List<Role> roles, Pageable pageable);

    /**
     * Finds all users who have all of the specified roles.
     * Used to find users with specific role combinations.
     * 
     * @param roles the list of roles that users must have (all of them)
     * @param roleCount the number of roles in the list (for validation)
     * @return List of Users who have all the specified roles
     */
    @Query("SELECT ur.user FROM UserRole ur WHERE ur.role IN :roles " +
           "GROUP BY ur.user HAVING COUNT(DISTINCT ur.role) = :roleCount")
    List<User> findUsersByAllRoles(@Param("roles") List<Role> roles, @Param("roleCount") long roleCount);

    // ========================================================================
    // Assignment tracking and audit queries
    // ========================================================================

    /**
     * Finds all role assignments made by a specific user.
     * Used for audit trail and administrative oversight.
     * 
     * @param assignedBy the user who assigned the roles
     * @return List of UserRole assignments made by the specified user
     */
    List<UserRole> findByAssignedBy(User assignedBy);

    /**
     * Finds all role assignments made by a specific user with pagination.
     * 
     * @param assignedBy the user who assigned the roles
     * @param pageable pagination parameters
     * @return Page of UserRole assignments made by the specified user
     */
    Page<UserRole> findByAssignedBy(User assignedBy, Pageable pageable);

    /**
     * Finds role assignments made within a specific date range.
     * Used for audit reporting and activity analysis.
     * 
     * @param startDate the start date (inclusive)
     * @param endDate the end date (inclusive)
     * @return List of UserRole assignments made within the date range
     */
    @Query("SELECT ur FROM UserRole ur WHERE ur.assignedAt >= :startDate AND ur.assignedAt <= :endDate")
    List<UserRole> findByAssignedAtBetween(@Param("startDate") LocalDateTime startDate, 
                                          @Param("endDate") LocalDateTime endDate);

    /**
     * Finds role assignments made within a specific date range with pagination.
     * 
     * @param startDate the start date (inclusive)
     * @param endDate the end date (inclusive)
     * @param pageable pagination parameters
     * @return Page of UserRole assignments made within the date range
     */
    @Query("SELECT ur FROM UserRole ur WHERE ur.assignedAt >= :startDate AND ur.assignedAt <= :endDate")
    Page<UserRole> findByAssignedAtBetween(@Param("startDate") LocalDateTime startDate, 
                                          @Param("endDate") LocalDateTime endDate,
                                          Pageable pageable);

    /**
     * Finds recent role assignments (within the last N days).
     * Used for monitoring recent role changes.
     * 
     * @param cutoffDate the cutoff date (assignments after this date)
     * @return List of recent UserRole assignments
     */
    @Query("SELECT ur FROM UserRole ur WHERE ur.assignedAt >= :cutoffDate ORDER BY ur.assignedAt DESC")
    List<UserRole> findRecentAssignments(@Param("cutoffDate") LocalDateTime cutoffDate);

    // ========================================================================
    // Cascade delete handling
    // ========================================================================

    /**
     * Deletes all role assignments for a specific user.
     * Used during user deletion to ensure proper cascade cleanup.
     * This method is called automatically by JPA cascade operations,
     * but can also be used for explicit cleanup.
     * 
     * @param user the user whose role assignments to delete
     * @return number of deleted role assignments
     */
    @Modifying
    @Query("DELETE FROM UserRole ur WHERE ur.user = :user")
    int deleteByUser(@Param("user") User user);

    /**
     * Deletes all role assignments for a user by user ID.
     * Convenient method when only the user ID is available.
     * 
     * @param userId the ID of the user whose role assignments to delete
     * @return number of deleted role assignments
     */
    @Modifying
    @Query("DELETE FROM UserRole ur WHERE ur.user.id = :userId")
    int deleteByUserId(@Param("userId") UUID userId);

    // ========================================================================
    // Business rule validation queries
    // ========================================================================

    /**
     * Checks if removing a specific role would leave the user with no roles.
     * Used to enforce the minimum role requirement before role revocation.
     * 
     * @param user the user whose role is being revoked
     * @param role the role being revoked
     * @return true if this is the user's last role, false otherwise
     */
    @Query("SELECT COUNT(ur) = 1 FROM UserRole ur WHERE ur.user = :user AND ur.role != :role")
    boolean isLastRole(@Param("user") User user, @Param("role") Role role);

    /**
     * Finds users who have only one role (the specified role).
     * Used to identify users who cannot have their role revoked.
     * 
     * @param role the role to check
     * @return List of Users who have only the specified role
     */
    @Query("SELECT ur.user FROM UserRole ur WHERE ur.role = :role " +
           "GROUP BY ur.user HAVING COUNT(ur.role) = 1")
    List<User> findUsersWithOnlyRole(@Param("role") Role role);

    /**
     * Finds users who would have no roles if the specified role is revoked.
     * More comprehensive check that considers the specific role being revoked.
     * 
     * @param role the role being considered for revocation
     * @return List of Users who would have no roles if this role is revoked
     */
    @Query("SELECT ur.user FROM UserRole ur WHERE ur.role = :role AND " +
           "ur.user NOT IN (SELECT ur2.user FROM UserRole ur2 WHERE ur2.role != :role)")
    List<User> findUsersWhoWouldHaveNoRolesWithoutRole(@Param("role") Role role);

    // ========================================================================
    // Advanced queries for role management
    // ========================================================================

    /**
     * Finds all role assignments with user and assigner information eagerly loaded.
     * Optimized query that fetches all related data in a single query.
     * Used when complete role assignment information is needed.
     * 
     * @return List of UserRole assignments with eagerly loaded relationships
     */
    @Query("SELECT ur FROM UserRole ur " +
           "LEFT JOIN FETCH ur.user u " +
           "LEFT JOIN FETCH ur.assignedBy ab")
    List<UserRole> findAllWithUserAndAssigner();

    /**
     * Finds role assignments for a specific user with assigner information.
     * 
     * @param user the user whose role assignments to retrieve
     * @return List of UserRole assignments with assigner information
     */
    @Query("SELECT ur FROM UserRole ur " +
           "LEFT JOIN FETCH ur.assignedBy " +
           "WHERE ur.user = :user")
    List<UserRole> findByUserWithAssigner(@Param("user") User user);

    /**
     * Finds role assignments by role with user information eagerly loaded.
     * 
     * @param role the role to search for
     * @return List of UserRole assignments with user information
     */
    @Query("SELECT ur FROM UserRole ur " +
           "LEFT JOIN FETCH ur.user " +
           "WHERE ur.role = :role")
    List<UserRole> findByRoleWithUser(@Param("role") Role role);

    /**
     * Advanced search with multiple filters for role assignments.
     * Supports filtering by user, role, assigner, and date range.
     * 
     * @param user optional user filter (can be null)
     * @param role optional role filter (can be null)
     * @param assignedBy optional assigner filter (can be null)
     * @param startDate optional start date filter (can be null)
     * @param endDate optional end date filter (can be null)
     * @param pageable pagination parameters
     * @return Page of UserRole assignments matching the specified criteria
     */
    @Query("SELECT ur FROM UserRole ur WHERE " +
           "(:user IS NULL OR ur.user = :user) AND " +
           "(:role IS NULL OR ur.role = :role) AND " +
           "(:assignedBy IS NULL OR ur.assignedBy = :assignedBy) AND " +
           "(:startDate IS NULL OR ur.assignedAt >= :startDate) AND " +
           "(:endDate IS NULL OR ur.assignedAt <= :endDate)")
    Page<UserRole> findWithFilters(@Param("user") User user,
                                  @Param("role") Role role,
                                  @Param("assignedBy") User assignedBy,
                                  @Param("startDate") LocalDateTime startDate,
                                  @Param("endDate") LocalDateTime endDate,
                                  Pageable pageable);

    // ========================================================================
    // Statistics and reporting queries
    // ========================================================================

    /**
     * Gets role distribution statistics.
     * Returns the count of users for each role.
     * 
     * @return List of Object arrays containing [Role, Count]
     */
    @Query("SELECT ur.role, COUNT(DISTINCT ur.user) FROM UserRole ur GROUP BY ur.role")
    List<Object[]> getRoleDistribution();

    /**
     * Gets role assignment activity statistics for a date range.
     * Returns the count of role assignments per day.
     * 
     * @param startDate the start date
     * @param endDate the end date
     * @return List of Object arrays containing [Date, Count]
     */
    @Query("SELECT DATE(ur.assignedAt), COUNT(ur) FROM UserRole ur " +
           "WHERE ur.assignedAt >= :startDate AND ur.assignedAt <= :endDate " +
           "GROUP BY DATE(ur.assignedAt) ORDER BY DATE(ur.assignedAt)")
    List<Object[]> getRoleAssignmentActivity(@Param("startDate") LocalDateTime startDate,
                                           @Param("endDate") LocalDateTime endDate);

    /**
     * Gets the most active role assigners.
     * Returns users who have assigned the most roles.
     * 
     * @param limit the maximum number of results to return
     * @return List of Object arrays containing [User, AssignmentCount]
     */
    @Query("SELECT ur.assignedBy, COUNT(ur) FROM UserRole ur " +
           "GROUP BY ur.assignedBy ORDER BY COUNT(ur) DESC")
    List<Object[]> getMostActiveAssigners(Pageable pageable);

    /**
     * Finds orphaned role assignments (where assignedBy user no longer exists).
     * Used for data cleanup and integrity checks.
     * 
     * @return List of UserRole assignments with invalid assignedBy references
     */
    @Query("SELECT ur FROM UserRole ur WHERE ur.assignedBy IS NULL")
    List<UserRole> findOrphanedAssignments();
}