package com.company.assetmanagement.service;

import com.company.assetmanagement.dto.UserDTO;
import com.company.assetmanagement.dto.UserRequest;
import com.company.assetmanagement.dto.UserUpdateRequest;
import com.company.assetmanagement.model.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

/**
 * Service interface for user management operations.
 * 
 * This service provides comprehensive user account lifecycle management including:
 * - User creation with validation, uniqueness checks, and audit logging
 * - User retrieval with pagination and role-based filtering
 * - User updates with validation and authorization
 * - User deletion with safety checks
 * - Account status management (enable/disable)
 * - Role assignment and revocation
 * 
 * Security Features:
 * - All operations enforce authorization checks via AuthorizationService
 * - Password hashing with BCrypt before storage
 * - Username and email uniqueness validation
 * - Self-operation prevention (cannot delete/disable own account)
 * - Comprehensive audit logging for all operations
 * 
 * Business Rules:
 * - Users must have at least one role
 * - Usernames and emails must be unique
 * - Users cannot delete or disable their own accounts
 * - Administrators cannot revoke their own administrator role
 * - All operations are logged for audit trail
 * 
 * Requirements: 4.1, 4.2, 4.3, 4.4, 4.5, 4.6, 4.7, 4.8, 5.1, 5.2, 5.3, 5.4, 5.5, 5.6,
 *              6.1, 6.2, 6.3, 6.4, 6.5, 6.6, 7.1, 7.2, 7.3, 7.4, 7.5, 8.1, 8.2, 8.3, 8.4, 8.5,
 *              9.1, 9.2, 9.3, 9.4, 9.5, 10.1, 10.2, 10.3, 10.4, 10.5, 12.1, 12.2, 12.3, 12.4, 12.5,
 *              14.1, 14.2, 14.3, 14.4, 14.5, 15.3, 15.4, 15.5, 15.6
 * 
 * @author IT Asset Management System
 * @version 1.0
 * @since 2024-01-15
 */
public interface UserService {

    /**
     * Creates a new user account.
     * 
     * This method performs the following operations:
     * 1. Validates authorization (creator must have CREATE_USER permission)
     * 2. Validates request data (username format, email format, password complexity)
     * 3. Checks username uniqueness
     * 4. Checks email uniqueness
     * 5. Hashes password with BCrypt (strength 10)
     * 6. Creates user entity with provided data
     * 7. Assigns specified roles to the user
     * 8. Persists user to database
     * 9. Logs audit event for user creation
     * 10. Returns UserDTO (excluding password hash)
     * 
     * Validation Rules:
     * - Username: 3-100 characters, alphanumeric and underscores only, unique
     * - Email: Valid email format, 5-255 characters, unique
     * - Password: Minimum 8 characters, 1 uppercase, 1 lowercase, 1 digit, 1 special char
     * - Roles: At least one role must be assigned
     * 
     * @param creatorId the ID of the user creating the account (must not be null)
     * @param request user creation request containing username, email, password, and roles
     * @return created user DTO with all fields except password hash
     * @throws IllegalArgumentException if creatorId or request is null
     * @throws com.company.assetmanagement.exception.InsufficientPermissionsException if creator lacks CREATE_USER permission
     * @throws com.company.assetmanagement.exception.ValidationException if request data is invalid
     * @throws com.company.assetmanagement.exception.DuplicateUsernameException if username already exists
     * @throws com.company.assetmanagement.exception.DuplicateEmailException if email already exists
     * @throws com.company.assetmanagement.exception.UserNotFoundException if creator user not found
     */
    UserDTO createUser(String creatorId, UserRequest request);

    /**
     * Retrieves a user by ID.
     * 
     * This method returns complete user information excluding the password hash.
     * No authorization check is performed - use controller-level security for access control.
     * 
     * @param userId the user ID to retrieve (must not be null)
     * @return Optional containing user DTO if found, empty otherwise
     * @throws IllegalArgumentException if userId is null
     */
    Optional<UserDTO> getUser(String userId);

    /**
     * Retrieves all users with pagination.
     * 
     * This method returns a paginated list of all users in the system.
     * Results exclude password hashes and include role information.
     * No authorization check is performed - use controller-level security for access control.
     * 
     * Default sorting: By creation date descending (newest first)
     * 
     * @param pageable pagination parameters (page number, size, sort)
     * @return Page of user DTOs
     * @throws IllegalArgumentException if pageable is null
     */
    Page<UserDTO> getAllUsers(Pageable pageable);

    /**
     * Retrieves users by role with pagination.
     * 
     * This method returns a paginated list of users who have the specified role.
     * A user is included if they have the role among their assigned roles.
     * Results exclude password hashes and include all role information.
     * 
     * @param role the role to filter by (must not be null)
     * @param pageable pagination parameters (page number, size, sort)
     * @return Page of user DTOs with the specified role
     * @throws IllegalArgumentException if role or pageable is null
     */
    Page<UserDTO> getUsersByRole(Role role, Pageable pageable);

    /**
     * Updates a user account.
     * 
     * This method performs the following operations:
     * 1. Validates authorization (updater must have UPDATE_USER permission)
     * 2. Verifies user exists
     * 3. Validates request data if provided (username format, email format)
     * 4. Checks username uniqueness if username is being changed
     * 5. Checks email uniqueness if email is being changed
     * 6. Updates user fields (only non-null fields in request)
     * 7. Sets updatedBy reference to updater
     * 8. Persists changes to database
     * 9. Logs audit event with changed fields
     * 10. Returns updated UserDTO
     * 
     * Note: Password cannot be updated through this method. Use ProfileService.changePassword() instead.
     * Note: Roles cannot be updated through this method. Use assignRole() and revokeRole() instead.
     * 
     * @param updaterId the ID of the user performing the update (must not be null)
     * @param userId the user ID to update (must not be null)
     * @param request update request containing fields to update (username, email)
     * @return updated user DTO
     * @throws IllegalArgumentException if updaterId, userId, or request is null
     * @throws com.company.assetmanagement.exception.InsufficientPermissionsException if updater lacks UPDATE_USER permission
     * @throws com.company.assetmanagement.exception.UserNotFoundException if user not found
     * @throws com.company.assetmanagement.exception.ValidationException if request data is invalid
     * @throws com.company.assetmanagement.exception.DuplicateUsernameException if new username already exists
     * @throws com.company.assetmanagement.exception.DuplicateEmailException if new email already exists
     */
    UserDTO updateUser(String updaterId, String userId, UserUpdateRequest request);

    /**
     * Deletes a user account.
     * 
     * This method performs the following operations:
     * 1. Validates authorization (deleter must have DELETE_USER permission)
     * 2. Verifies user exists
     * 3. Validates user is not deleting their own account
     * 4. Invalidates all active sessions for the user
     * 5. Deletes user from database (cascade deletes roles and sessions)
     * 6. Logs audit event for user deletion
     * 
     * Business Rule: Users cannot delete their own accounts to prevent accidental lockout.
     * 
     * @param deleterId the ID of the user performing the deletion (must not be null)
     * @param userId the user ID to delete (must not be null)
     * @throws IllegalArgumentException if deleterId or userId is null
     * @throws com.company.assetmanagement.exception.InsufficientPermissionsException if deleter lacks DELETE_USER permission
     * @throws com.company.assetmanagement.exception.UserNotFoundException if user not found
     * @throws com.company.assetmanagement.exception.ValidationException if attempting to delete own account
     */
    void deleteUser(String deleterId, String userId);

    /**
     * Enables a user account.
     * 
     * This method sets the user's isActive flag to true, allowing them to authenticate
     * and use the system. No session invalidation occurs since the account is being enabled.
     * 
     * Operations performed:
     * 1. Validates authorization (admin must have MANAGE_USER_STATUS permission)
     * 2. Verifies user exists
     * 3. Sets isActive to true
     * 4. Sets updatedBy reference to admin
     * 5. Persists changes to database
     * 6. Logs audit event for account enable
     * 
     * @param adminId the ID of the administrator performing the operation (must not be null)
     * @param userId the user ID to enable (must not be null)
     * @throws IllegalArgumentException if adminId or userId is null
     * @throws com.company.assetmanagement.exception.InsufficientPermissionsException if admin lacks MANAGE_USER_STATUS permission
     * @throws com.company.assetmanagement.exception.UserNotFoundException if user not found
     */
    void enableUser(String adminId, String userId);

    /**
     * Disables a user account.
     * 
     * This method sets the user's isActive flag to false, preventing them from authenticating
     * or performing any operations. All active sessions are invalidated immediately.
     * 
     * Operations performed:
     * 1. Validates authorization (admin must have MANAGE_USER_STATUS permission)
     * 2. Verifies user exists
     * 3. Validates admin is not disabling their own account
     * 4. Sets isActive to false
     * 5. Sets updatedBy reference to admin
     * 6. Invalidates all active sessions for the user
     * 7. Persists changes to database
     * 8. Logs audit event for account disable
     * 
     * Business Rule: Administrators cannot disable their own accounts to prevent lockout.
     * 
     * @param adminId the ID of the administrator performing the operation (must not be null)
     * @param userId the user ID to disable (must not be null)
     * @throws IllegalArgumentException if adminId or userId is null
     * @throws com.company.assetmanagement.exception.InsufficientPermissionsException if admin lacks MANAGE_USER_STATUS permission
     * @throws com.company.assetmanagement.exception.UserNotFoundException if user not found
     * @throws com.company.assetmanagement.exception.ValidationException if attempting to disable own account
     */
    void disableUser(String adminId, String userId);

    /**
     * Assigns a role to a user.
     * 
     * This method creates a new UserRole association between the user and the specified role.
     * All active sessions are invalidated to force re-authentication with new permissions.
     * 
     * Operations performed:
     * 1. Validates authorization (admin must have ASSIGN_ROLE permission)
     * 2. Verifies user exists
     * 3. Validates role is one of: ADMINISTRATOR, ASSET_MANAGER, VIEWER
     * 4. Checks user does not already have the role
     * 5. Creates UserRole entity with assignedBy reference
     * 6. Persists role assignment to database
     * 7. Invalidates all active sessions for the user
     * 8. Logs audit event for role assignment
     * 
     * @param adminId the ID of the administrator performing the operation (must not be null)
     * @param userId the user ID to assign role to (must not be null)
     * @param role the role to assign (must not be null)
     * @throws IllegalArgumentException if adminId, userId, or role is null
     * @throws com.company.assetmanagement.exception.InsufficientPermissionsException if admin lacks ASSIGN_ROLE permission
     * @throws com.company.assetmanagement.exception.UserNotFoundException if user not found
     * @throws com.company.assetmanagement.exception.ValidationException if user already has the role
     */
    void assignRole(String adminId, String userId, Role role);

    /**
     * Revokes a role from a user.
     * 
     * This method removes a UserRole association between the user and the specified role.
     * All active sessions are invalidated to force re-authentication with updated permissions.
     * 
     * Operations performed:
     * 1. Validates authorization (admin must have REVOKE_ROLE permission)
     * 2. Verifies user exists
     * 3. Validates user has the role to be revoked
     * 4. Validates user will have at least one role remaining after revocation
     * 5. Validates admin is not revoking their own ADMINISTRATOR role
     * 6. Removes UserRole entity from database
     * 7. Invalidates all active sessions for the user
     * 8. Logs audit event for role revocation
     * 
     * Business Rules:
     * - Users must always have at least one role
     * - Administrators cannot revoke their own ADMINISTRATOR role
     * 
     * @param adminId the ID of the administrator performing the operation (must not be null)
     * @param userId the user ID to revoke role from (must not be null)
     * @param role the role to revoke (must not be null)
     * @throws IllegalArgumentException if adminId, userId, or role is null
     * @throws com.company.assetmanagement.exception.InsufficientPermissionsException if admin lacks REVOKE_ROLE permission
     * @throws com.company.assetmanagement.exception.UserNotFoundException if user not found
     * @throws com.company.assetmanagement.exception.ValidationException if user doesn't have the role, 
     *         it's their last role, or admin is revoking their own ADMINISTRATOR role
     */
    void revokeRole(String adminId, String userId, Role role);
}
