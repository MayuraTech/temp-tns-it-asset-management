package com.company.assetmanagement.controller;

import com.company.assetmanagement.dto.ChangePasswordRequest;
import com.company.assetmanagement.dto.ErrorResponse;
import com.company.assetmanagement.dto.ProfileUpdateRequest;
import com.company.assetmanagement.dto.UserDTO;
import com.company.assetmanagement.service.ProfileService;
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
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for user profile self-service operations.
 * 
 * Provides endpoints for:
 * - Viewing current user profile
 * - Updating current user profile (email only)
 * - Changing current user password
 * 
 * All endpoints are accessible by authenticated users for their own profile only.
 * Users cannot modify restricted fields like username and roles through these endpoints.
 * 
 * API Design:
 * - GET /api/v1/profile: Get current user profile
 * - PUT /api/v1/profile: Update current user profile
 * - POST /api/v1/profile/change-password: Change current user password
 * 
 * Security:
 * - All endpoints require authentication (JWT token)
 * - Users can only access and modify their own profile
 * - Password changes require current password verification
 * - Sessions are invalidated after password changes
 * - Comprehensive audit logging for all operations
 * 
 * Requirements: 11.1, 11.2, 11.3, 11.4, 11.5, 11.6, 3.1, 3.2, 3.3, 3.4, 3.5, 3.6
 * 
 * @author IT Asset Management System
 * @version 1.0
 * @since 2024-01-15
 */
@RestController
@RequestMapping("/api/v1/profile")
@Tag(name = "Profile Management", description = "User profile self-service endpoints")
public class ProfileController {
    
    private static final Logger logger = LoggerFactory.getLogger(ProfileController.class);
    
    private final ProfileService profileService;
    
    /**
     * Constructor with dependency injection.
     *
     * @param profileService service for profile management operations
     */
    public ProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }
    
    /**
     * Retrieves the current user's profile.
     * 
     * This endpoint returns complete profile information for the authenticated user,
     * including username, email, account status, roles, and audit timestamps.
     * Password hash is explicitly excluded from the response for security.
     * 
     * Accessible by all authenticated users for their own profile.
     * 
     * @return user profile DTO with password hash excluded
     */
    @GetMapping
    @Operation(
        summary = "Get current user profile",
        description = "Retrieves the profile information for the currently authenticated user. " +
                     "Password hash is excluded from the response. Accessible by all authenticated users."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Profile retrieved successfully",
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
    public ResponseEntity<UserDTO> getProfile() {
        String userId = extractUserIdFromAuthentication();
        
        logger.info("Get profile request received for user ID: {}", userId);
        
        UserDTO profile = profileService.getProfile(userId);
        
        logger.info("Profile retrieved successfully for user ID: {}", userId);
        
        return ResponseEntity.ok(profile);
    }
    
    /**
     * Updates the current user's profile.
     * 
     * This endpoint allows users to update their email address. Username and roles
     * cannot be changed through this endpoint - those require administrator privileges.
     * Email uniqueness is validated if the email is being changed.
     * 
     * Operations performed:
     * - Validates email format if provided
     * - Checks email uniqueness if email is being changed
     * - Updates user profile with new email
     * - Logs audit event with changed fields
     * 
     * Business Rules:
     * - Email must be unique across all users (case-insensitive)
     * - Email format must be valid
     * - Username cannot be changed through profile endpoint
     * - Roles cannot be changed through profile endpoint
     * - Password changes use separate changePassword endpoint
     * 
     * @param request profile update request containing new email
     * @return updated user profile DTO with password hash excluded
     */
    @PutMapping
    @Operation(
        summary = "Update current user profile",
        description = "Updates the profile information for the currently authenticated user. " +
                     "Only email can be updated through this endpoint. Username and roles require administrator privileges. " +
                     "Password changes use the separate change-password endpoint."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Profile updated successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = UserDTO.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Validation error - invalid email format",
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
            responseCode = "404",
            description = "User not found",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "409",
            description = "Conflict - email already exists",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class)
            )
        )
    })
    public ResponseEntity<UserDTO> updateProfile(
            @Parameter(description = "Profile update request with new email")
            @Valid @RequestBody ProfileUpdateRequest request) {
        
        String userId = extractUserIdFromAuthentication();
        
        logger.info("Update profile request received for user ID: {}", userId);
        
        UserDTO updatedProfile = profileService.updateProfile(userId, request);
        
        logger.info("Profile updated successfully for user ID: {}", userId);
        
        return ResponseEntity.ok(updatedProfile);
    }
    
    /**
     * Changes the current user's password.
     * 
     * This endpoint allows users to change their password by providing their current
     * password and a new password that meets complexity requirements. After successful
     * password change, all active sessions for the user are invalidated to force
     * re-authentication with the new password.
     * 
     * Operations performed:
     * - Validates current password is correct
     * - Validates new password meets complexity requirements
     * - Validates new password is different from current password
     * - Hashes new password with BCrypt strength 10
     * - Updates password in database
     * - Invalidates all active sessions for the user
     * - Logs audit event (without password values)
     * 
     * Business Rules:
     * - Current password must be correct
     * - New password must meet complexity requirements:
     *   - Minimum 8 characters
     *   - At least one uppercase letter
     *   - At least one lowercase letter
     *   - At least one digit
     *   - At least one special character (@$!%*?&)
     * - New password cannot be the same as current password
     * - All user sessions are invalidated after password change
     * 
     * Security:
     * - Current password is verified using BCrypt comparison
     * - New password is hashed before storage
     * - Plain-text passwords are never stored or logged
     * - Session invalidation forces re-authentication
     * 
     * @param request password change request with current and new passwords
     * @return 204 No Content on successful password change
     */
    @PostMapping("/change-password")
    @Operation(
        summary = "Change current user password",
        description = "Changes the password for the currently authenticated user. " +
                     "Requires current password verification. New password must meet complexity requirements. " +
                     "All active sessions are invalidated after password change, requiring re-authentication."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "204",
            description = "Password changed successfully - all sessions invalidated"
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Validation error - current password incorrect, new password invalid, " +
                         "or new password same as current",
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
            responseCode = "404",
            description = "User not found",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class)
            )
        )
    })
    public ResponseEntity<Void> changePassword(
            @Parameter(description = "Password change request with current and new passwords")
            @Valid @RequestBody ChangePasswordRequest request) {
        
        String userId = extractUserIdFromAuthentication();
        
        logger.info("Change password request received for user ID: {}", userId);
        
        profileService.changePassword(userId, request);
        
        logger.info("Password changed successfully for user ID: {} - all sessions invalidated", userId);
        
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
