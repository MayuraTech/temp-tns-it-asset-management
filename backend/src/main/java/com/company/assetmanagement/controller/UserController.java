package com.company.assetmanagement.controller;

import com.company.assetmanagement.dto.*;
import com.company.assetmanagement.model.Role;
import com.company.assetmanagement.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * REST Controller for user management operations.
 * 
 * Provides endpoints for:
 * - User CRUD operations (Create, Read, Update, Delete)
 * - Account status management (Enable, Disable)
 * - Role management (Assign, Revoke)
 * 
 * All endpoints follow RESTful conventions and return appropriate HTTP status codes.
 * Comprehensive error handling is implemented with structured error responses.
 * 
 * API Design:
 * - POST /api/v1/users: Create new user (Admin only)
 * - GET /api/v1/users: List all users with pagination
 * - GET /api/v1/users/{id}: Get user by ID
 * - PUT /api/v1/users/{id}: Update user (Admin only)
 * - DELETE /api/v1/users/{id}: Delete user (Admin only)
 * - PATCH /api/v1/users/{id}/enable: Enable user account (Admin only)
 * - PATCH /api/v1/users/{id}/disable: Disable user account (Admin only)
 * - POST /api/v1/users/{id}/roles: Assign role to user (Admin only)
 * - DELETE /api/v1/users/{id}/roles/{role}: Revoke role from user (Admin only)
 * 
 * Security:
 * - All endpoints require authentication (JWT token)
 * - Authorization is enforced via @PreAuthorize annotations
 * - Administrators have full access to all operations
 * - Asset Managers can view users but not modify
 * - Viewers can only view users
 * - Comprehensive audit logging for all operations
 * 
 * Requirements: 4.1, 4.2, 4.3, 4.4, 4.5, 4.6, 4.7, 4.8, 5.1, 5.2, 5.3, 5.4, 5.5, 5.6,
 *              6.1, 6.2, 6.3, 6.4, 6.5, 6.6, 7.1, 7.2, 7.3, 7.4, 7.5, 8.1, 8.2, 8.3, 8.4, 8.5,
 *              9.1, 9.2, 9.3, 9.4, 9.5, 10.1, 10.2, 10.3, 10.4, 10.5
 * 
 * @author IT Asset Management System
 * @version 1.0
 * @since 2024-01-15
 */
@RestController
@RequestMapping("/api/v1/users")
@Tag(name = "User Management", description = "User account lifecycle management endpoints")
public class UserController {
    
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);
    
    private final UserService userService;
    
    /**
     * Constructor with dependency injection.
     *
     * @param userService service for user management operations
     */
    public UserController(UserService userService) {
        this.userService = userService;
    }
    
    /**
     * Creates a new user account.
     * 
     * This endpoint allows administrators to create new user accounts with specified
     * username, email, password, and role assignments. The password is hashed before
     * storage and never exposed in responses.
     * 
     * Operations performed:
     * - Validates request data (username format, email format, password complexity)
     * - Checks username and email uniqueness
     * - Hashes password with BCrypt (strength 10)
     * - Creates user entity with provided data
     * - Assigns specified roles
     * - Logs audit event for user creation
     * 
     * Business Rules:
     * - Username must be 3-100 characters, alphanumeric and underscores only
     * - Email must be valid format and unique
     * - Password must meet complexity requirements (8+ chars, uppercase, lowercase, digit, special char)
     * - At least one role must be assigned
     * 
     * @param request user creation request containing username, email, password, and roles
     * @return created user DTO with 201 Created status
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    @Operation(
        summary = "Create new user",
        description = "Creates a new user account with specified username, email, password, and roles. " +
                     "Requires ADMINISTRATOR role. Password is hashed before storage."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "User created successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = UserDTO.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Validation error - invalid request format or missing required fields",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized - missing or invalid JWT token",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Forbidden - insufficient permissions (requires ADMINISTRATOR role)",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "409",
            description = "Conflict - username or email already exists",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class)
            )
        )
    })
    public ResponseEntity<UserDTO> createUser(
            @Parameter(description = "User creation request with username, email, password, and roles")
            @Valid @RequestBody UserRequest request) {
        
        String creatorId = extractUserIdFromAuthentication();
        
        logger.info("Create user request received by user ID: {} for username: {}", 
                   creatorId, request.getUsername());
        
        UserDTO createdUser = userService.createUser(creatorId, request);
        
        logger.info("User created successfully with ID: {} by user ID: {}", 
                   createdUser.getId(), creatorId);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
    }
    
    /**
     * Retrieves all users with pagination and optional role filtering.
     * 
     * This endpoint returns a paginated list of all users in the system.
     * Results exclude password hashes and include role information.
     * 
     * Pagination:
     * - Default page size: 20
     * - Default sort: createdAt descending (newest first)
     * - Supports custom page size, page number, and sort parameters
     * 
     * Filtering:
     * - Optional role parameter to filter users by assigned role
     * 
     * @param role optional role filter (ADMINISTRATOR, ASSET_MANAGER, or VIEWER)
     * @param pageable pagination parameters (page, size, sort)
     * @return paginated list of user DTOs
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'ASSET_MANAGER', 'VIEWER')")
    @Operation(
        summary = "List all users",
        description = "Retrieves a paginated list of all users. Supports filtering by role. " +
                     "Accessible by all authenticated users."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Users retrieved successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Page.class)
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized - missing or invalid JWT token",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class)
            )
        )
    })
    public ResponseEntity<Page<UserDTO>> getAllUsers(
            @Parameter(description = "Optional role filter (ADMINISTRATOR, ASSET_MANAGER, VIEWER)")
            @RequestParam(required = false) Role role,
            @Parameter(description = "Pagination parameters (page, size, sort)")
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) 
            Pageable pageable) {
        
        logger.info("Get all users request received with role filter: {}, page: {}, size: {}", 
                   role, pageable.getPageNumber(), pageable.getPageSize());
        
        Page<UserDTO> users;
        if (role != null) {
            users = userService.getUsersByRole(role, pageable);
            logger.info("Retrieved {} users with role: {}", users.getTotalElements(), role);
        } else {
            users = userService.getAllUsers(pageable);
            logger.info("Retrieved {} total users", users.getTotalElements());
        }
        
        return ResponseEntity.ok(users);
    }
    
    /**
     * Retrieves a specific user by ID.
     * 
     * This endpoint returns complete user information excluding the password hash.
     * Accessible by all authenticated users.
     * 
     * @param id the user ID (UUID format)
     * @return user DTO if found, 404 Not Found otherwise
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'ASSET_MANAGER', 'VIEWER')")
    @Operation(
        summary = "Get user by ID",
        description = "Retrieves detailed information for a specific user by ID. " +
                     "Accessible by all authenticated users."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "User retrieved successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = UserDTO.class)
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized - missing or invalid JWT token",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "User not found",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class)
            )
        )
    })
    public ResponseEntity<UserDTO> getUser(
            @Parameter(description = "User ID (UUID format)")
            @PathVariable UUID id) {
        
        logger.info("Get user request received for user ID: {}", id);
        
        return userService.getUser(id.toString())
            .map(user -> {
                logger.info("User found with ID: {}", id);
                return ResponseEntity.ok(user);
            })
            .orElseGet(() -> {
                logger.warn("User not found with ID: {}", id);
                return ResponseEntity.notFound().build();
            });
    }
    
    /**
     * Updates an existing user account.
     * 
     * This endpoint allows administrators to update user information including
     * username and email. Password updates are not allowed through this endpoint
     * (use ProfileService.changePassword() instead). Role updates are handled
     * through separate role assignment endpoints.
     * 
     * Operations performed:
     * - Validates request data if provided
     * - Checks username uniqueness if username is being changed
     * - Checks email uniqueness if email is being changed
     * - Updates user fields (only non-null fields in request)
     * - Logs audit event with changed fields
     * 
     * @param id the user ID to update
     * @param request update request containing fields to update (username, email)
     * @return updated user DTO
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    @Operation(
        summary = "Update user",
        description = "Updates user information (username, email). Requires ADMINISTRATOR role. " +
                     "Password and role updates use separate endpoints."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "User updated successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = UserDTO.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Validation error - invalid request format",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized - missing or invalid JWT token",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Forbidden - insufficient permissions (requires ADMINISTRATOR role)",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "User not found",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "409",
            description = "Conflict - new username or email already exists",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class)
            )
        )
    })
    public ResponseEntity<UserDTO> updateUser(
            @Parameter(description = "User ID (UUID format)")
            @PathVariable UUID id,
            @Parameter(description = "User update request with fields to update")
            @Valid @RequestBody UserUpdateRequest request) {
        
        String updaterId = extractUserIdFromAuthentication();
        
        logger.info("Update user request received for user ID: {} by user ID: {}", 
                   id, updaterId);
        
        UserDTO updatedUser = userService.updateUser(updaterId, id.toString(), request);
        
        logger.info("User updated successfully with ID: {} by user ID: {}", 
                   id, updaterId);
        
        return ResponseEntity.ok(updatedUser);
    }
    
    /**
     * Deletes a user account.
     * 
     * This endpoint allows administrators to permanently delete user accounts.
     * All associated data (roles, sessions) are cascade deleted.
     * 
     * Operations performed:
     * - Validates user is not deleting their own account
     * - Invalidates all active sessions for the user
     * - Deletes user from database (cascade deletes roles and sessions)
     * - Logs audit event for user deletion
     * 
     * Business Rule: Users cannot delete their own accounts to prevent accidental lockout.
     * 
     * @param id the user ID to delete
     * @return 204 No Content on successful deletion
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    @Operation(
        summary = "Delete user",
        description = "Permanently deletes a user account. Requires ADMINISTRATOR role. " +
                     "Users cannot delete their own accounts. All sessions are invalidated."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "204",
            description = "User deleted successfully"
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Validation error - attempting to delete own account",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized - missing or invalid JWT token",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Forbidden - insufficient permissions (requires ADMINISTRATOR role)",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "User not found",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class)
            )
        )
    })
    public ResponseEntity<Void> deleteUser(
            @Parameter(description = "User ID (UUID format)")
            @PathVariable UUID id) {
        
        String deleterId = extractUserIdFromAuthentication();
        
        logger.info("Delete user request received for user ID: {} by user ID: {}", 
                   id, deleterId);
        
        userService.deleteUser(deleterId, id.toString());
        
        logger.info("User deleted successfully with ID: {} by user ID: {}", 
                   id, deleterId);
        
        return ResponseEntity.noContent().build();
    }
    
    /**
     * Enables a user account.
     * 
     * This endpoint sets the user's isActive flag to true, allowing them to
     * authenticate and use the system.
     * 
     * Operations performed:
     * - Sets isActive to true
     * - Logs audit event for account enable
     * 
     * @param id the user ID to enable
     * @return 204 No Content on successful enable
     */
    @PatchMapping("/{id}/enable")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    @Operation(
        summary = "Enable user account",
        description = "Enables a user account, allowing authentication and system access. " +
                     "Requires ADMINISTRATOR role."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "204",
            description = "User account enabled successfully"
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized - missing or invalid JWT token",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Forbidden - insufficient permissions (requires ADMINISTRATOR role)",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "User not found",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class)
            )
        )
    })
    public ResponseEntity<Void> enableUser(
            @Parameter(description = "User ID (UUID format)")
            @PathVariable UUID id) {
        
        String adminId = extractUserIdFromAuthentication();
        
        logger.info("Enable user request received for user ID: {} by admin ID: {}", 
                   id, adminId);
        
        userService.enableUser(adminId, id.toString());
        
        logger.info("User enabled successfully with ID: {} by admin ID: {}", 
                   id, adminId);
        
        return ResponseEntity.noContent().build();
    }
    
    /**
     * Disables a user account.
     * 
     * This endpoint sets the user's isActive flag to false, preventing them from
     * authenticating or performing any operations. All active sessions are invalidated.
     * 
     * Operations performed:
     * - Validates admin is not disabling their own account
     * - Sets isActive to false
     * - Invalidates all active sessions for the user
     * - Logs audit event for account disable
     * 
     * Business Rule: Administrators cannot disable their own accounts to prevent lockout.
     * 
     * @param id the user ID to disable
     * @return 204 No Content on successful disable
     */
    @PatchMapping("/{id}/disable")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    @Operation(
        summary = "Disable user account",
        description = "Disables a user account, preventing authentication and system access. " +
                     "Requires ADMINISTRATOR role. Administrators cannot disable their own accounts. " +
                     "All sessions are invalidated."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "204",
            description = "User account disabled successfully"
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Validation error - attempting to disable own account",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized - missing or invalid JWT token",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Forbidden - insufficient permissions (requires ADMINISTRATOR role)",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "User not found",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class)
            )
        )
    })
    public ResponseEntity<Void> disableUser(
            @Parameter(description = "User ID (UUID format)")
            @PathVariable UUID id) {
        
        String adminId = extractUserIdFromAuthentication();
        
        logger.info("Disable user request received for user ID: {} by admin ID: {}", 
                   id, adminId);
        
        userService.disableUser(adminId, id.toString());
        
        logger.info("User disabled successfully with ID: {} by admin ID: {}", 
                   id, adminId);
        
        return ResponseEntity.noContent().build();
    }
    
    /**
     * Assigns a role to a user.
     * 
     * This endpoint creates a new UserRole association between the user and the
     * specified role. All active sessions are invalidated to force re-authentication
     * with new permissions.
     * 
     * Operations performed:
     * - Validates role is one of: ADMINISTRATOR, ASSET_MANAGER, VIEWER
     * - Checks user does not already have the role
     * - Creates UserRole entity with assignedBy reference
     * - Invalidates all active sessions for the user
     * - Logs audit event for role assignment
     * 
     * @param id the user ID to assign role to
     * @param request role assignment request containing the role
     * @return 204 No Content on successful assignment
     */
    @PostMapping("/{id}/roles")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    @Operation(
        summary = "Assign role to user",
        description = "Assigns a role to a user. Requires ADMINISTRATOR role. " +
                     "All user sessions are invalidated to refresh permissions."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "204",
            description = "Role assigned successfully"
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Validation error - user already has the role",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized - missing or invalid JWT token",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Forbidden - insufficient permissions (requires ADMINISTRATOR role)",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "User not found",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class)
            )
        )
    })
    public ResponseEntity<Void> assignRole(
            @Parameter(description = "User ID (UUID format)")
            @PathVariable UUID id,
            @Parameter(description = "Role assignment request with role to assign")
            @Valid @RequestBody RoleAssignmentRequest request) {
        
        String adminId = extractUserIdFromAuthentication();
        
        logger.info("Assign role request received for user ID: {} with role: {} by admin ID: {}", 
                   id, request.getRole(), adminId);
        
        userService.assignRole(adminId, id.toString(), request.getRole());
        
        logger.info("Role {} assigned successfully to user ID: {} by admin ID: {}", 
                   request.getRole(), id, adminId);
        
        return ResponseEntity.noContent().build();
    }
    
    /**
     * Revokes a role from a user.
     * 
     * This endpoint removes a UserRole association between the user and the specified
     * role. All active sessions are invalidated to force re-authentication with updated
     * permissions.
     * 
     * Operations performed:
     * - Validates user has the role to be revoked
     * - Validates user will have at least one role remaining after revocation
     * - Validates admin is not revoking their own ADMINISTRATOR role
     * - Removes UserRole entity from database
     * - Invalidates all active sessions for the user
     * - Logs audit event for role revocation
     * 
     * Business Rules:
     * - Users must always have at least one role
     * - Administrators cannot revoke their own ADMINISTRATOR role
     * 
     * @param id the user ID to revoke role from
     * @param role the role to revoke
     * @return 204 No Content on successful revocation
     */
    @DeleteMapping("/{id}/roles/{role}")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    @Operation(
        summary = "Revoke role from user",
        description = "Revokes a role from a user. Requires ADMINISTRATOR role. " +
                     "Users must have at least one role. Administrators cannot revoke their own ADMINISTRATOR role. " +
                     "All user sessions are invalidated to refresh permissions."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "204",
            description = "Role revoked successfully"
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Validation error - user doesn't have the role, it's their last role, " +
                         "or admin is revoking their own ADMINISTRATOR role",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized - missing or invalid JWT token",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Forbidden - insufficient permissions (requires ADMINISTRATOR role)",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "User not found",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class)
            )
        )
    })
    public ResponseEntity<Void> revokeRole(
            @Parameter(description = "User ID (UUID format)")
            @PathVariable UUID id,
            @Parameter(description = "Role to revoke (ADMINISTRATOR, ASSET_MANAGER, or VIEWER)")
            @PathVariable Role role) {
        
        String adminId = extractUserIdFromAuthentication();
        
        logger.info("Revoke role request received for user ID: {} with role: {} by admin ID: {}", 
                   id, role, adminId);
        
        userService.revokeRole(adminId, id.toString(), role);
        
        logger.info("Role {} revoked successfully from user ID: {} by admin ID: {}", 
                   role, id, adminId);
        
        return ResponseEntity.noContent().build();
    }
    
    // ========================================================================
    // Private Helper Methods
    // ========================================================================
    
    /**
     * Extracts user ID from the current authentication context.
     * 
     * The JWT token provider stores the user ID in the authentication name.
     * This method retrieves it from the security context.
     *
     * @return the user ID as a string
     */
    private String extractUserIdFromAuthentication() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            logger.error("No authenticated user found in security context");
            throw new IllegalStateException("No authenticated user found");
        }
        
        Object principal = authentication.getPrincipal();
        
        if (principal instanceof org.springframework.security.core.userdetails.UserDetails) {
            // If using UserDetails, the username is the user ID
            return ((org.springframework.security.core.userdetails.UserDetails) principal).getUsername();
        } else {
            // Otherwise, use the name directly
            return authentication.getName();
        }
    }
}
