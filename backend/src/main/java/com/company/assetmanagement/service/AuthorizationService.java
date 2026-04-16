package com.company.assetmanagement.service;

import com.company.assetmanagement.model.Action;

/**
 * Service interface for authorization and permission checking.
 * 
 * <p>This service provides methods to check if a user has permission to perform
 * specific actions in the system. It integrates with Spring Security to validate
 * user roles and permissions.
 * 
 * <p>The service supports role-based access control with the following roles:
 * <ul>
 *   <li><strong>ADMINISTRATOR</strong> - Full access to all operations</li>
 *   <li><strong>ASSET_MANAGER</strong> - Can create, update, and manage assets</li>
 *   <li><strong>VIEWER</strong> - Read-only access to assets</li>
 * </ul>
 * 
 * <p>Permission mappings:
 * <ul>
 *   <li><strong>CREATE_ASSET</strong> - ADMINISTRATOR, ASSET_MANAGER</li>
 *   <li><strong>UPDATE_ASSET</strong> - ADMINISTRATOR, ASSET_MANAGER</li>
 *   <li><strong>DELETE_ASSET</strong> - ADMINISTRATOR only</li>
 *   <li><strong>VIEW_ASSET</strong> - ADMINISTRATOR, ASSET_MANAGER, VIEWER</li>
 * </ul>
 * 
 * @see Action
 * @author Module 2 Team
 * @version 1.0
 */
public interface AuthorizationService {
    
    /**
     * Checks if a user has permission to perform a specific action.
     * 
     * <p>This method validates the user's roles against the required permissions
     * for the specified action. It integrates with Spring Security to retrieve
     * the user's authentication context and roles.
     * 
     * <p><strong>Implementation Notes:</strong>
     * <ul>
     *   <li>Uses Spring Security's SecurityContextHolder to get current authentication</li>
     *   <li>Validates user roles against action requirements</li>
     *   <li>Returns false if user is not authenticated</li>
     *   <li>Returns false if user lacks required role for the action</li>
     * </ul>
     * 
     * @param userId the ID of the user to check permissions for (must not be null)
     * @param action the action to check permission for (must not be null)
     * @return true if the user has permission to perform the action, false otherwise
     * @throws IllegalArgumentException if userId or action is null
     */
    boolean hasPermission(String userId, Action action);
    
    /**
     * Checks if the current authenticated user has permission to perform a specific action.
     * 
     * <p>This is a convenience method that checks permissions for the currently
     * authenticated user without requiring a userId parameter. It uses Spring Security's
     * SecurityContextHolder to get the current authentication context.
     * 
     * @param action the action to check permission for (must not be null)
     * @return true if the current user has permission to perform the action, false otherwise
     * @throws IllegalArgumentException if action is null
     * @throws IllegalStateException if no user is currently authenticated
     */
    boolean hasPermission(Action action);
    
    /**
     * Gets the current authenticated user's ID.
     * 
     * <p>This method retrieves the user ID from the current Spring Security
     * authentication context. It's useful for audit logging and other operations
     * that need to track which user performed an action.
     * 
     * @return the current user's ID
     * @throws IllegalStateException if no user is currently authenticated
     */
    String getCurrentUserId();
    
    /**
     * Checks if the current authenticated user has any of the specified roles.
     * 
     * <p>This method is useful for checking if a user has one of several acceptable
     * roles for an operation.
     * 
     * @param roles the roles to check (must not be null or empty)
     * @return true if the current user has at least one of the specified roles, false otherwise
     * @throws IllegalArgumentException if roles is null or empty
     * @throws IllegalStateException if no user is currently authenticated
     */
    boolean hasAnyRole(String... roles);
    
    /**
     * Checks if the current authenticated user has a specific role.
     * 
     * <p>This method checks if the current user has exactly the specified role.
     * Role names should include the "ROLE_" prefix (e.g., "ROLE_ADMINISTRATOR").
     * 
     * @param role the role to check (must not be null)
     * @return true if the current user has the specified role, false otherwise
     * @throws IllegalArgumentException if role is null
     * @throws IllegalStateException if no user is currently authenticated
     */
    boolean hasRole(String role);
}
import com.company.assetmanagement.model.Role;

/**
 * Service interface for authorization and permission checking operations.
 * 
 * This service provides comprehensive authorization functionality including:
 * - Permission checking for specific actions
 * - Role verification for users
 * - Account status validation (active, not locked)
 * - Role-based access control (RBAC) enforcement
 * 
 * Authorization Model:
 * - Administrators have all permissions (complete access)
 * - Asset Managers have permissions for asset operations
 * - Viewers have read-only permissions
 * - All operations verify account status before authorization
 * 
 * Security Features:
 * - Account status validation (active and not locked)
 * - Role-based permission mapping
 * - Comprehensive permission checking
 * - Exception-based access denial
 * 
 * Usage:
 * This service should be called before any state-changing operation to verify
 * that the user has the required permissions and their account is in good standing.
 * 
 * @author IT Asset Management System
 * @version 1.0
 * @since 2024-01-15
 */
public interface AuthorizationService {

    /**
     * Checks if a user has permission to perform a specific action.
     * 
     * This method verifies that:
     * 1. The user exists in the system
     * 2. The user's account is active and not locked
     * 3. The user has a role that grants permission for the specified action
     * 
     * Permission Rules:
     * - Administrators have permission for all actions
     * - Asset Managers have permissions for asset-related operations
     * - Viewers have permissions for read-only operations
     * 
     * @param userId the ID of the user to check (must not be null)
     * @param action the action to check permission for (must not be null)
     * @return true if the user has permission for the action, false otherwise
     * @throws IllegalArgumentException if userId or action is null
     * @throws com.company.assetmanagement.exception.UserNotFoundException if user does not exist
     */
    boolean hasPermission(String userId, Action action);

    /**
     * Checks if a user has a specific role.
     * 
     * This method verifies that the user has been assigned the specified role.
     * It does not check account status - use validateAccountStatus() for that.
     * 
     * @param userId the ID of the user to check (must not be null)
     * @param role the role to check for (must not be null)
     * @return true if the user has the specified role, false otherwise
     * @throws IllegalArgumentException if userId or role is null
     * @throws com.company.assetmanagement.exception.UserNotFoundException if user does not exist
     */
    boolean hasRole(String userId, Role role);

    /**
     * Validates that a user's account is active and not locked.
     * 
     * This method performs comprehensive account status validation:
     * 1. Verifies the user exists
     * 2. Checks if the account is active (isActive = true)
     * 3. Checks if the account is not locked or if the lock has expired
     * 
     * Account Lock Behavior:
     * - If account is locked and lockUntil is in the future, throws AccountLockedException
     * - If account is locked but lockUntil has passed, the account is automatically unlocked
     * - If account is inactive (isActive = false), throws AccountDisabledException
     * 
     * @param userId the ID of the user to validate (must not be null)
     * @throws IllegalArgumentException if userId is null
     * @throws com.company.assetmanagement.exception.UserNotFoundException if user does not exist
     * @throws com.company.assetmanagement.exception.AccountLockedException if account is currently locked
     * @throws com.company.assetmanagement.exception.AccountDisabledException if account is inactive
     */
    void validateAccountStatus(String userId);
}
