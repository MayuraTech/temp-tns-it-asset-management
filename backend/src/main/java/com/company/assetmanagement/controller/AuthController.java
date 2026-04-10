package com.company.assetmanagement.controller;

import com.company.assetmanagement.dto.ErrorResponse;
import com.company.assetmanagement.dto.LoginRequest;
import com.company.assetmanagement.dto.RefreshTokenRequest;
import com.company.assetmanagement.dto.TokenResponse;
import com.company.assetmanagement.service.AuthenticationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for authentication operations.
 * 
 * Provides endpoints for:
 * - User login with credential validation
 * - User logout with session invalidation
 * - Token refresh for obtaining new access tokens
 * 
 * All endpoints follow RESTful conventions and return appropriate HTTP status codes.
 * Comprehensive error handling is implemented with structured error responses.
 * 
 * API Design:
 * - POST /api/v1/auth/login: Authenticate user and receive tokens
 * - POST /api/v1/auth/logout: Logout user and invalidate session
 * - POST /api/v1/auth/refresh: Refresh access token using refresh token
 * 
 * Security:
 * - Login and refresh endpoints are public (no authentication required)
 * - Logout endpoint requires authentication (JWT token)
 * - All endpoints use HTTPS in production
 * - Comprehensive audit logging for all operations
 * 
 * Requirements: 1.1, 1.2, 1.3, 1.4, 1.5, 1.6, 1.7, 1.8, 2.1, 2.2, 2.3, 2.4, 2.5
 * 
 * @author IT Asset Management System
 * @version 1.0
 * @since 2024-01-15
 */
@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication", description = "Authentication and token management endpoints")
public class AuthController {
    
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    
    private final AuthenticationService authenticationService;
    
    /**
     * Constructor with dependency injection.
     *
     * @param authenticationService service for authentication operations
     */
    public AuthController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }
    
    /**
     * Authenticates a user with username and password.
     * 
     * This endpoint validates user credentials and returns JWT tokens for authenticated access.
     * On successful authentication:
     * - Access token (30-minute expiration) for API requests
     * - Refresh token (24-hour expiration) for obtaining new access tokens
     * - Last login timestamp is updated
     * - Failed login attempts counter is reset
     * - Session record is created
     * 
     * Security Features:
     * - Account locking after 5 failed attempts (30-minute lock)
     * - Inactive account detection
     * - Comprehensive audit logging
     * 
     * @param request login credentials (username and password)
     * @return token response with access token, refresh token, and expiration info
     */
    @PostMapping("/login")
    @Operation(
        summary = "User login",
        description = "Authenticates a user with username and password. Returns JWT access and refresh tokens on success. " +
                     "Account will be locked for 30 minutes after 5 consecutive failed login attempts."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Login successful - returns access and refresh tokens",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = TokenResponse.class)
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
            description = "Authentication failed - invalid credentials, account locked, or account disabled",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class)
            )
        )
    })
    public ResponseEntity<TokenResponse> login(@Valid @RequestBody LoginRequest request) {
        logger.info("Login request received for username: {}", request.getUsername());
        
        TokenResponse tokenResponse = authenticationService.login(request);
        
        logger.info("Login successful for username: {}", request.getUsername());
        return ResponseEntity.ok(tokenResponse);
    }
    
    /**
     * Logs out the current authenticated user.
     * 
     * This endpoint invalidates the user's active session and marks it as terminated.
     * The user must be authenticated (valid JWT token required).
     * 
     * Operations performed:
     * - Extracts user ID from security context
     * - Invalidates all active sessions for the user
     * - Records logout timestamp
     * - Logs logout event for audit
     * 
     * After logout, the user must login again to obtain new tokens.
     * 
     * @return 204 No Content on successful logout
     */
    @PostMapping("/logout")
    @Operation(
        summary = "User logout",
        description = "Logs out the current authenticated user and invalidates their session. " +
                     "Requires valid JWT token in Authorization header."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "204",
            description = "Logout successful - session invalidated"
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
    public ResponseEntity<Void> logout() {
        // Extract user ID from security context
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            logger.warn("Logout attempt without valid authentication");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        String userId = extractUserIdFromAuthentication(authentication);
        
        logger.info("Logout request received for user ID: {}", userId);
        
        authenticationService.logout(userId);
        
        logger.info("Logout successful for user ID: {}", userId);
        return ResponseEntity.noContent().build();
    }
    
    /**
     * Refreshes an access token using a valid refresh token.
     * 
     * This endpoint allows clients to obtain a new access token without re-authenticating.
     * The refresh token must be valid and not expired.
     * 
     * Token Rotation:
     * - New access token is generated (30-minute expiration)
     * - New refresh token is generated (24-hour expiration)
     * - Old refresh token is invalidated
     * 
     * Security:
     * - Refresh token is validated
     * - User account status is verified (must be active)
     * - Session is updated with new token information
     * 
     * @param request refresh token request containing the refresh token
     * @return new token response with fresh access and refresh tokens
     */
    @PostMapping("/refresh")
    @Operation(
        summary = "Refresh access token",
        description = "Obtains a new access token using a valid refresh token. " +
                     "Returns new access and refresh tokens (token rotation for enhanced security)."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Token refresh successful - returns new access and refresh tokens",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = TokenResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Validation error - missing or invalid refresh token format",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Authentication failed - invalid or expired refresh token, or account disabled",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class)
            )
        )
    })
    public ResponseEntity<TokenResponse> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        logger.info("Token refresh request received");
        
        TokenResponse tokenResponse = authenticationService.refreshToken(request.getRefreshToken());
        
        logger.info("Token refresh successful");
        return ResponseEntity.ok(tokenResponse);
    }
    
    // ========================================================================
    // Private Helper Methods
    // ========================================================================
    
    /**
     * Extracts user ID from the authentication object.
     * 
     * The JWT token provider stores the user ID in the authentication name.
     * This method retrieves it from the security context.
     *
     * @param authentication the authentication object from security context
     * @return the user ID as a string
     */
    private String extractUserIdFromAuthentication(Authentication authentication) {
        // The authentication name contains the user ID (set by JwtTokenProvider)
        // In the JWT token, we store userId as the subject
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
