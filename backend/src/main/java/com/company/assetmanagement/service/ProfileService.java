package com.company.assetmanagement.service;

import com.company.assetmanagement.dto.ChangePasswordRequest;
import com.company.assetmanagement.dto.ProfileUpdateRequest;
import com.company.assetmanagement.dto.UserDTO;
import com.company.assetmanagement.exception.UserNotFoundException;
import com.company.assetmanagement.exception.ValidationException;

/**
 * Service interface for user profile self-service operations.
 * 
 * Provides methods for users to view and update their own profile information
 * and change their passwords without administrator privileges. This service
 * enforces security constraints to prevent users from modifying restricted
 * fields like username and roles.
 * 
 * Security Features:
 * - Users can only access and modify their own profile
 * - Password changes require current password verification
 * - Email uniqueness is enforced on updates
 * - Password hashes are never exposed in responses
 * - Sessions are invalidated after password changes
 * 
 * Validation:
 * - Email format and uniqueness validation
 * - Password complexity requirements enforcement
 * - Current password verification before changes
 * 
 * Audit Logging:
 * - All profile updates are logged
 * - Password changes are logged (without password values)
 * 
 * @author IT Asset Management System
 * @version 1.0
 * @since 2024-01-15
 */
public interface ProfileService {
    
    /**
     * Retrieves the current user's profile.
     * 
     * Returns complete user profile information including username, email,
     * account status, roles, and audit timestamps. Password hash is explicitly
     * excluded from the response for security.
     * 
     * @param userId the user ID of the current user
     * @return user profile DTO with password hash excluded
     * @throws UserNotFoundException if user not found
     */
    UserDTO getProfile(String userId);
    
    /**
     * Updates the current user's profile.
     * 
     * Allows users to update their email address. Username and roles cannot
     * be changed through this endpoint - those require administrator privileges.
     * Email uniqueness is validated if the email is being changed.
     * 
     * Business Rules:
     * - Email must be unique across all users (case-insensitive)
     * - Email format must be valid
     * - Username cannot be changed through profile endpoint
     * - Roles cannot be changed through profile endpoint
     * - Password changes use separate changePassword endpoint
     * 
     * Audit Logging:
     * - Logs profile update event with changed fields
     * - Records user ID and timestamp
     * 
     * @param userId the user ID of the current user
     * @param request profile update request containing new email
     * @return updated user profile DTO with password hash excluded
     * @throws UserNotFoundException if user not found
     * @throws ValidationException if request data is invalid
     * @throws com.company.assetmanagement.exception.DuplicateEmailException if email already exists
     */
    UserDTO updateProfile(String userId, ProfileUpdateRequest request);
    
    /**
     * Changes the current user's password.
     * 
     * Validates the current password before allowing the change. New password
     * must meet complexity requirements (minimum 8 characters, one uppercase,
     * one lowercase, one digit, one special character). After successful password
     * change, all active sessions for the user are invalidated to force re-authentication.
     * 
     * Business Rules:
     * - Current password must be correct
     * - New password must meet complexity requirements
     * - New password cannot be the same as current password
     * - Password is hashed with BCrypt strength 10 before storage
     * - All user sessions are invalidated after password change
     * 
     * Security:
     * - Current password is verified using BCrypt comparison
     * - New password is hashed before storage
     * - Plain-text passwords are never stored or logged
     * - Session invalidation forces re-authentication
     * 
     * Audit Logging:
     * - Logs password change event with user ID and timestamp
     * - Does not log password values
     * 
     * @param userId the user ID of the current user
     * @param request password change request with current and new passwords
     * @throws UserNotFoundException if user not found
     * @throws ValidationException if current password is incorrect or new password is invalid
     */
    void changePassword(String userId, ChangePasswordRequest request);
}
