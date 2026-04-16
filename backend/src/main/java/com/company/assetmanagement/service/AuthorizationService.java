package com.company.assetmanagement.service;

import com.company.assetmanagement.model.Action;
import com.company.assetmanagement.model.Role;

import java.util.List;

/**
 * Service interface for authorization operations.
 * 
 * Placeholder interface for allocation management implementation.
 * Full implementation will be provided by User Management module.
 */
public interface AuthorizationService {
    
    /**
     * Check if a user has permission to perform an action.
     *
     * @param userId the user ID
     * @param action the action to check
     * @return true if user has permission, false otherwise
     */
    boolean hasPermission(String userId, Action action);
    
    /**
     * Check if a user has any of the specified roles.
     *
     * @param userId the user ID
     * @param roles the roles to check
     * @return true if user has any of the roles, false otherwise
     */
    boolean hasAnyRole(String userId, List<Role> roles);
}
