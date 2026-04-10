package com.company.assetmanagement.dto;

import com.company.assetmanagement.model.Role;
import jakarta.validation.constraints.NotNull;

/**
 * Data Transfer Object for role assignment requests.
 * Contains the role to be assigned to or revoked from a user.
 * 
 * Used for both role assignment and revocation operations:
 * - POST /api/v1/users/{id}/roles - Assign role to user
 * - DELETE /api/v1/users/{id}/roles/{role} - Revoke role from user (role in path)
 * 
 * Validation Rules:
 * - Role: Required, must be one of ADMINISTRATOR, ASSET_MANAGER, or VIEWER
 * 
 * Requirements: 9.1, 9.2, 9.3, 9.4, 9.5, 10.1, 10.2, 10.3, 10.4, 10.5
 */
public class RoleAssignmentRequest {
    
    @NotNull(message = "Role is required")
    private Role role;
    
    /**
     * Default constructor for JSON deserialization.
     */
    public RoleAssignmentRequest() {
    }
    
    /**
     * Constructor with role.
     *
     * @param role the role to assign or revoke
     */
    public RoleAssignmentRequest(Role role) {
        this.role = role;
    }
    
    /**
     * Gets the role.
     *
     * @return the role
     */
    public Role getRole() {
        return role;
    }
    
    /**
     * Sets the role.
     *
     * @param role the role to set
     */
    public void setRole(Role role) {
        this.role = role;
    }
    
    @Override
    public String toString() {
        return "RoleAssignmentRequest{" +
                "role=" + role +
                '}';
    }
}