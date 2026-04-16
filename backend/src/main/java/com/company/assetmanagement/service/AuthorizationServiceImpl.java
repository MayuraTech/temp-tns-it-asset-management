package com.company.assetmanagement.service;

import com.company.assetmanagement.model.Action;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Set;

/**
 * Implementation of AuthorizationService for permission checking.
 * 
 * <p>This service integrates with Spring Security to validate user permissions
 * based on their roles and the action they want to perform. It uses the current
 * authentication context to determine user roles and permissions.
 * 
 * <p><strong>Role-Based Access Control:</strong>
 * <ul>
 *   <li><strong>ROLE_ADMINISTRATOR</strong> - Full access to all operations</li>
 *   <li><strong>ROLE_ASSET_MANAGER</strong> - Can create, update, and manage assets</li>
 *   <li><strong>ROLE_VIEWER</strong> - Read-only access to assets</li>
 * </ul>
 * 
 * @see AuthorizationService
 * @see Action
 */
@Service
public class AuthorizationServiceImpl implements AuthorizationService {
    
    // Role constants
    private static final String ROLE_ADMINISTRATOR = "ROLE_ADMINISTRATOR";
    private static final String ROLE_ASSET_MANAGER = "ROLE_ASSET_MANAGER";
    private static final String ROLE_VIEWER = "ROLE_VIEWER";
    
    /**
     * Checks if a user has permission to perform a specific action.
     * 
     * <p>This implementation uses Spring Security's SecurityContextHolder to get
     * the current authentication and validate permissions based on user roles.
     * 
     * <p><strong>Permission Matrix:</strong>
     * <table border="1">
     *   <tr><th>Action</th><th>Administrator</th><th>Asset Manager</th><th>Viewer</th></tr>
     *   <tr><td>CREATE_ASSET</td><td>✓</td><td>✓</td><td>✗</td></tr>
     *   <tr><td>UPDATE_ASSET</td><td>✓</td><td>✓</td><td>✗</td></tr>
     *   <tr><td>DELETE_ASSET</td><td>✓</td><td>✗</td><td>✗</td></tr>
     *   <tr><td>VIEW_ASSET</td><td>✓</td><td>✓</td><td>✓</td></tr>
     * </table>
     * 
     * @param userId the ID of the user to check permissions for (must not be null)
     * @param action the action to check permission for (must not be null)
     * @return true if the user has permission to perform the action, false otherwise
     * @throws IllegalArgumentException if userId or action is null
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
            throw new IllegalArgumentException("User ID cannot be null or empty");
        }
        if (action == null) {
            throw new IllegalArgumentException("Action cannot be null");
        }
        
        // Get current authentication from Spring Security context
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        // If no authentication or not authenticated, deny access
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }
        
        // Get user authorities (roles)
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        Set<String> roles = authorities.stream()
            .map(GrantedAuthority::getAuthority)
            .collect(java.util.stream.Collectors.toSet());
        
        // Check permissions based on action and roles
        return checkActionPermission(action, roles);
    }
    
    /**
     * Checks if the current authenticated user has permission to perform a specific action.
     * 
     * @param action the action to check permission for (must not be null)
     * @return true if the current user has permission to perform the action, false otherwise
     * @throws IllegalArgumentException if action is null
     * @throws IllegalStateException if no user is currently authenticated
     */
    @Override
    public boolean hasPermission(Action action) {
        if (action == null) {
            throw new IllegalArgumentException("Action cannot be null");
        }
        
        String currentUserId = getCurrentUserId();
        return hasPermission(currentUserId, action);
    }
    
    /**
     * Gets the current authenticated user's ID.
     * 
     * @return the current user's ID
     * @throws IllegalStateException if no user is currently authenticated
     */
    @Override
    public String getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("No authenticated user found");
        }
        
        Object principal = authentication.getPrincipal();
        
        // If principal is UserDetails, get username
        if (principal instanceof UserDetails) {
            return ((UserDetails) principal).getUsername();
        }
        
        // If principal is a string (username), return it
        if (principal instanceof String) {
            return (String) principal;
        }
        
        // Fallback to toString()
        return principal.toString();
    }
    
    /**
     * Checks if the current authenticated user has any of the specified roles.
     * 
     * @param roles the roles to check (must not be null or empty)
     * @return true if the current user has at least one of the specified roles, false otherwise
     * @throws IllegalArgumentException if roles is null or empty
     * @throws IllegalStateException if no user is currently authenticated
     */
    @Override
    public boolean hasAnyRole(String... roles) {
        if (roles == null || roles.length == 0) {
            throw new IllegalArgumentException("Roles cannot be null or empty");
        }
        
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("No authenticated user found");
        }
        
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        Set<String> userRoles = authorities.stream()
            .map(GrantedAuthority::getAuthority)
            .collect(java.util.stream.Collectors.toSet());
        
        // Check if user has any of the specified roles
        for (String role : roles) {
            if (userRoles.contains(role)) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Checks if the current authenticated user has a specific role.
     * 
     * @param role the role to check (must not be null)
     * @return true if the current user has the specified role, false otherwise
     * @throws IllegalArgumentException if role is null
     * @throws IllegalStateException if no user is currently authenticated
     */
    @Override
    public boolean hasRole(String role) {
        if (role == null) {
            throw new IllegalArgumentException("Role cannot be null");
        }
        
        return hasAnyRole(role);
    }
    
    /**
     * Checks if the user has permission for a specific action based on their roles.
     * 
     * <p>This method implements the permission matrix:
     * <ul>
     *   <li><strong>ADMINISTRATOR</strong> - Can perform all actions</li>
     *   <li><strong>ASSET_MANAGER</strong> - Can create, update, and view assets</li>
     *   <li><strong>VIEWER</strong> - Can only view assets</li>
     * </ul>
     * 
     * @param action the action to check
     * @param roles the user's roles
     * @return true if the user has permission, false otherwise
     */
    private boolean checkActionPermission(Action action, Set<String> roles) {
        // Administrators can do everything
        if (roles.contains(ROLE_ADMINISTRATOR)) {
            return true;
        }
        
        // Check specific action permissions
        switch (action) {
            case CREATE_ASSET:
            case UPDATE_ASSET:
                // Asset managers and administrators can create/update assets
                return roles.contains(ROLE_ASSET_MANAGER);
                
            case DELETE_ASSET:
                // Only administrators can delete assets
                return false; // Already checked administrator role above
                
            case VIEW_ASSET:
                // All authenticated users can view assets
                return roles.contains(ROLE_ASSET_MANAGER) || roles.contains(ROLE_VIEWER);
                
            case EXPORT_DATA:
            case IMPORT_DATA:
                // Asset managers and administrators can import/export
                return roles.contains(ROLE_ASSET_MANAGER);
                
            default:
                // Deny access for unknown actions
                return false;
        }
    }
}
            logger.warn("hasPermission called with null or blank userId");
            throw new IllegalArgumentException("User ID must not be null or blank");
        }
        if (action == null) {
            logger.warn("hasPermission called with null action for userId: {}", userId);
            throw new IllegalArgumentException("Action must not be null");
        }

        logger.debug("Checking permission for userId: {} and action: {}", userId, action);

        // Retrieve user with roles
        User user = getUserWithRoles(userId);

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

        // Retrieve user with roles
        User user = getUserWithRoles(userId);

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

        // Retrieve user from database
        UUID userUuid;
        try {
            userUuid = UUID.fromString(userId);
        } catch (IllegalArgumentException e) {
            logger.error("Invalid UUID format for userId: {}", userId);
            throw new UserNotFoundException(userId);
        }

        User user = userRepository.findById(userUuid)
            .orElseThrow(() -> {
                logger.error("User not found with ID: {}", userId);
                return new UserNotFoundException(userId);
            });

        // Perform internal validation
        validateAccountStatusInternal(user);

        logger.debug("Account status validation passed for userId: {}", userId);
    }

    // ========================================================================
    // Private Helper Methods
    // ========================================================================

    /**
     * Retrieves a user with their roles eagerly loaded.
     * Uses optimized query to fetch user and roles in a single database call.
     * 
     * @param userId the user ID as string
     * @return the User entity with roles loaded
     * @throws UserNotFoundException if user does not exist
     */
    private User getUserWithRoles(String userId) {
        UUID userUuid;
        try {
            userUuid = UUID.fromString(userId);
        } catch (IllegalArgumentException e) {
            logger.error("Invalid UUID format for userId: {}", userId);
            throw new UserNotFoundException(userId);
        }

        // First try to find by ID with roles
        return userRepository.findById(userUuid)
            .map(user -> {
                // Force initialization of roles collection
                user.getRoles().size();
                return user;
            })
            .orElseThrow(() -> {
                logger.error("User not found with ID: {}", userId);
                return new UserNotFoundException(userId);
            });
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
