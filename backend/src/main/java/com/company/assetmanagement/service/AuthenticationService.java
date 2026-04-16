package com.company.assetmanagement.service;

import com.company.assetmanagement.dto.LoginRequest;
import com.company.assetmanagement.dto.TokenResponse;
import com.company.assetmanagement.exception.AccountDisabledException;
import com.company.assetmanagement.exception.AccountLockedException;
import org.springframework.security.core.AuthenticationException;

/**
 * Service interface for authentication operations.
 * 
 * Provides comprehensive authentication functionality including:
 * - User login with credential validation
 * - Account status checks (active, locked)
 * - JWT token generation (access and refresh tokens)
 * - Session management and tracking
 * - Account locking after failed attempts
 * - Audit logging for authentication events
 * 
 * Security Features:
 * - BCrypt password validation
 * - Account locking after 5 failed attempts for 30 minutes
 * - Automatic account unlock after lock period expires
 * - Session creation and tracking
 * - Comprehensive audit logging
 * 
 * Business Rules:
 * - Failed login attempts are tracked per user
 * - Account locks automatically after 5 consecutive failures
 * - Lock duration is 30 minutes from last failed attempt
 * - Successful login resets failed attempt counter
 * - Inactive accounts cannot authenticate
 * - Locked accounts cannot authenticate until lock expires
 * 
 * Requirements: 1.1, 1.2, 1.3, 1.4, 1.5, 1.6, 1.7, 1.8
 * 
 * @author IT Asset Management System
 * @version 1.0
 * @since 2024-01-15
 */
public interface AuthenticationService {
    
    /**
     * Authenticates a user with username and password.
     * 
     * This method performs the following operations:
     * 1. Validates user credentials (username and password)
     * 2. Checks if account is active
     * 3. Checks if account is locked
     * 4. Validates password against stored hash
     * 5. Generates JWT access token (30-minute expiration)
     * 6. Generates JWT refresh token (24-hour expiration)
     * 7. Creates session record
     * 8. Updates last login timestamp
     * 9. Resets failed login attempts counter
     * 10. Logs authentication success event
     * 
     * On authentication failure:
     * - Increments failed login attempts counter
     * - Locks account if attempts reach 5
     * - Logs authentication failure event
     * 
     * @param request login credentials containing username and password
     * @return token response with access token, refresh token, and expiration info
     * @throws AuthenticationException if credentials are invalid or user not found
     * @throws AccountLockedException if account is locked due to failed attempts
     * @throws AccountDisabledException if account is inactive
     */
    TokenResponse login(LoginRequest request);
    
    /**
     * Logs out a user and invalidates their session.
     * 
     * This method performs the following operations:
     * 1. Finds active session for the user
     * 2. Marks session as inactive
     * 3. Records logout timestamp
     * 4. Logs logout event
     * 
     * @param userId the user ID to logout
     * @throws IllegalArgumentException if userId is null or invalid
     */
    void logout(String userId);
    
    /**
     * Refreshes an access token using a valid refresh token.
     * 
     * This method performs the following operations:
     * 1. Validates refresh token
     * 2. Extracts user information from token
     * 3. Verifies user account is still active
     * 4. Generates new access token (30-minute expiration)
     * 5. Optionally generates new refresh token (token rotation)
     * 6. Updates session with new token information
     * 7. Logs token refresh event
     * 
     * @param refreshToken the refresh token to use for generating new access token
     * @return new token response with fresh access token and optionally new refresh token
     * @throws AuthenticationException if refresh token is invalid, expired, or user not found
     * @throws AccountDisabledException if user account is no longer active
     */
    TokenResponse refreshToken(String refreshToken);
}
