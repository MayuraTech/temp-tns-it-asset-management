package com.company.assetmanagement.service;

import com.company.assetmanagement.model.Action;
import com.company.assetmanagement.model.Role;

import java.util.UUID;

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

    /**
     * Resolves the authenticated principal string (UUID or username from {@link org.springframework.security.core.userdetails.UserDetails})
     * to the user's persistent {@link java.util.UUID}.
     *
     * @param userIdOrUsername UUID string or username (must not be null or blank)
     * @return the user's id
     * @throws com.company.assetmanagement.exception.UserNotFoundException if no user matches
     */
    UUID resolveActorUuid(String userIdOrUsername);
}
