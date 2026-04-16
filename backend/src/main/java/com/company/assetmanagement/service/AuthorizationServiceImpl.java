package com.company.assetmanagement.service;

import com.company.assetmanagement.exception.AccountDisabledException;
import com.company.assetmanagement.exception.AccountLockedException;
import com.company.assetmanagement.exception.UserNotFoundException;
import com.company.assetmanagement.model.Action;
import com.company.assetmanagement.model.Role;
import com.company.assetmanagement.model.User;
import com.company.assetmanagement.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.Set;
import java.util.UUID;

/**
 * Implementation of the AuthorizationService interface.
 * 
 * This service provides role-based access control (RBAC) for the IT Asset Management System.
 * It implements a comprehensive permission model where:
 * - Administrators have all permissions
 * - Asset Managers have permissions for asset operations and ticket management
 * - Viewers have read-only permissions
 * 
 * Permission Model:
 * The service uses a role-to-permission mapping where each role is associated with
 * a set of actions they can perform. The Administrator role has a special property
 * where it grants permission for all actions (Property 17 from design document).
 * 
 * Account Status Validation:
 * Before checking permissions, the service validates that the user's account is:
 * - Active (isActive = true)
 * - Not locked (accountLocked = false or lockUntil has expired)
 * 
 * This ensures that disabled or locked accounts cannot perform any operations,
 * regardless of their assigned roles.
 * 
 * @author IT Asset Management System
 * @version 1.0
 * @since 2024-01-15
 */
@Service
@Transactional(readOnly = true)
public class AuthorizationServiceImpl implements AuthorizationService {

    private static final Logger logger = LoggerFactory.getLogger(AuthorizationServiceImpl.class);

    private final UserRepository userRepository;

    /**
     * Role-to-permission mapping for Asset Manager role.
     * Asset Managers can perform asset-related operations and ticket management.
     * They can view users but cannot create, update, or delete them.
     */
    private static final Set<Action> ASSET_MANAGER_PERMISSIONS = EnumSet.of(
        Action.CREATE_ASSET,
        Action.UPDATE_ASSET,
        Action.DELETE_ASSET,
        Action.VIEW_ASSET,
        Action.CREATE_TICKET,
        Action.APPROVE_TICKET,
        Action.REJECT_TICKET,
        Action.COMPLETE_TICKET,
        Action.VIEW_TICKET,
        Action.EXPORT_DATA,
        Action.IMPORT_DATA,
        Action.VIEW_USER
    );

    /**
     * Role-to-permission mapping for Viewer role.
     * Viewers have read-only access to assets and tickets.
     * They can only view their own profile (enforced at service layer).
     */
    private static final Set<Action> VIEWER_PERMISSIONS = EnumSet.of(
        Action.VIEW_ASSET,
        Action.VIEW_TICKET
    );

    /**
     * Constructor with dependency injection.
     * 
     * @param userRepository the user repository for database operations
     */
    public AuthorizationServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * {@inheritDoc}
     * 
     * Implementation Details:
     * 1. Validates input parameters
     * 2. Retrieves user from database with roles
     * 3. Validates account status (active and not locked)
     * 4. Checks if user has Administrator role (grants all permissions)
     * 5. Checks if user's roles grant permission for the action
     * 
     * Performance Optimization:
     * - Uses findByUsernameWithRoles to fetch user and roles in single query
     * - Short-circuits on Administrator role (no need to check specific permissions)
     * - Caches role permissions in static sets for fast lookup
     */
    @Override
    public boolean hasPermission(String userId, Action action) {
        // Validate input parameters
        if (userId == null || userId.isBlank()) {
            logger.warn("hasPermission called with null or blank userId");
            throw new IllegalArgumentException("User ID must not be null or blank");
        }
        if (action == null) {
            logger.warn("hasPermission called with null action for userId: {}", userId);
            throw new IllegalArgumentException("Action must not be null");
        }

        logger.debug("Checking permission for userId: {} and action: {}", userId, action);

        // Retrieve user with roles (supports principal UUID or username)
        User user = loadUserWithRolesByIdOrUsername(userId);

        // Validate account status before checking permissions
        validateAccountStatusInternal(user);

        // Get user's roles
        Set<Role> userRoles = user.getRoleNames();

        // Property 17: Administrator has all permissions
        if (userRoles.contains(Role.ADMINISTRATOR)) {
            logger.debug("User {} has ADMINISTRATOR role - granting permission for action: {}", userId, action);
            return true;
        }

        // Check if any of the user's roles grant permission for this action
        boolean hasPermission = userRoles.stream()
            .anyMatch(role -> getRolePermissions(role).contains(action));

        if (hasPermission) {
            logger.debug("User {} has permission for action: {}", userId, action);
        } else {
            logger.debug("User {} does NOT have permission for action: {}", userId, action);
        }

        return hasPermission;
    }

    /**
     * {@inheritDoc}
     * 
     * Implementation Details:
     * 1. Validates input parameters
     * 2. Retrieves user from database with roles
     * 3. Checks if user has the specified role
     * 
     * Note: This method does NOT validate account status. Use validateAccountStatus()
     * separately if you need to ensure the account is active and not locked.
     */
    @Override
    public boolean hasRole(String userId, Role role) {
        // Validate input parameters
        if (userId == null || userId.isBlank()) {
            logger.warn("hasRole called with null or blank userId");
            throw new IllegalArgumentException("User ID must not be null or blank");
        }
        if (role == null) {
            logger.warn("hasRole called with null role for userId: {}", userId);
            throw new IllegalArgumentException("Role must not be null");
        }

        logger.debug("Checking if userId: {} has role: {}", userId, role);

        // Retrieve user with roles (supports principal UUID or username)
        User user = loadUserWithRolesByIdOrUsername(userId);

        // Check if user has the specified role
        boolean hasRole = user.hasRole(role);

        if (hasRole) {
            logger.debug("User {} has role: {}", userId, role);
        } else {
            logger.debug("User {} does NOT have role: {}", userId, role);
        }

        return hasRole;
    }

    /**
     * {@inheritDoc}
     * 
     * Implementation Details:
     * 1. Validates input parameter
     * 2. Retrieves user from database
     * 3. Checks if account is active
     * 4. Checks if account is locked
     * 5. If locked, checks if lock has expired and unlocks if necessary
     * 6. Throws appropriate exception if account is disabled or locked
     * 
     * Account Lock Expiration:
     * If the account is marked as locked but the lockUntil time has passed,
     * this method automatically unlocks the account by calling unlockAccount().
     * This ensures that temporary locks expire automatically without manual intervention.
     */
    @Override
    @Transactional
    public void validateAccountStatus(String userId) {
        // Validate input parameter
        if (userId == null || userId.isBlank()) {
            logger.warn("validateAccountStatus called with null or blank userId");
            throw new IllegalArgumentException("User ID must not be null or blank");
        }

        logger.debug("Validating account status for userId: {}", userId);

        User user = loadUserWithRolesByIdOrUsername(userId);

        // Perform internal validation
        validateAccountStatusInternal(user);

        logger.debug("Account status validation passed for userId: {}", userId);
    }

    @Override
    public UUID resolveActorUuid(String userIdOrUsername) {
        if (userIdOrUsername == null || userIdOrUsername.isBlank()) {
            throw new IllegalArgumentException("User ID or username must not be null or blank");
        }
        return loadUserWithRolesByIdOrUsername(userIdOrUsername).getId();
    }

    // ========================================================================
    // Private Helper Methods
    // ========================================================================

    /**
     * Loads a user with roles by UUID string, or by username when the value is not a valid UUID
     * (e.g. Spring Security {@code UserDetails#getUsername()}).
     */
    private User loadUserWithRolesByIdOrUsername(String userIdOrUsername) {
        try {
            UUID userUuid = UUID.fromString(userIdOrUsername);
            return userRepository.findById(userUuid)
                .map(user -> {
                    user.getRoles().size();
                    return user;
                })
                .orElseThrow(() -> {
                    logger.error("User not found with ID: {}", userIdOrUsername);
                    return new UserNotFoundException(userIdOrUsername);
                });
        } catch (IllegalArgumentException e) {
            return userRepository.findByUsernameWithRoles(userIdOrUsername)
                .map(user -> {
                    user.getRoles().size();
                    return user;
                })
                .orElseThrow(() -> {
                    logger.error("User not found with username: {}", userIdOrUsername);
                    return new UserNotFoundException(userIdOrUsername);
                });
        }
    }

    /**
     * Internal method to validate account status.
     * Checks if account is active and not locked.
     * Automatically unlocks accounts whose lock has expired.
     * 
     * @param user the User entity to validate
     * @throws AccountDisabledException if account is inactive
     * @throws AccountLockedException if account is currently locked
     */
    @Transactional
    private void validateAccountStatusInternal(User user) {
        // Check if account is active
        if (!user.getIsActive()) {
            logger.warn("Account is disabled for user: {}", user.getUsername());
            throw new AccountDisabledException();
        }

        // Check if account is locked
        if (user.getAccountLocked()) {
            LocalDateTime lockUntil = user.getLockUntil();
            LocalDateTime now = LocalDateTime.now();

            // If lock has expired, unlock the account
            if (lockUntil != null && lockUntil.isBefore(now)) {
                logger.info("Account lock has expired for user: {}. Unlocking account.", user.getUsername());
                user.unlockAccount();
                userRepository.save(user);
            } else {
                // Account is still locked
                logger.warn("Account is locked for user: {} until: {}", user.getUsername(), lockUntil);
                throw new AccountLockedException(lockUntil);
            }
        }
    }

    /**
     * Gets the set of permissions for a specific role.
     * 
     * Permission Mapping:
     * - ADMINISTRATOR: All permissions (handled separately in hasPermission)
     * - ASSET_MANAGER: Asset operations, ticket management, data import/export
     * - VIEWER: Read-only access to assets and tickets
     * 
     * @param role the role to get permissions for
     * @return Set of actions the role can perform
     */
    private Set<Action> getRolePermissions(Role role) {
        return switch (role) {
            case ADMINISTRATOR -> EnumSet.allOf(Action.class); // All permissions
            case ASSET_MANAGER -> ASSET_MANAGER_PERMISSIONS;
            case VIEWER -> VIEWER_PERMISSIONS;
        };
    }
}
