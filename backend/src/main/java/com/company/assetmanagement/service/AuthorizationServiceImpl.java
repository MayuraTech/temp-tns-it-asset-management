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