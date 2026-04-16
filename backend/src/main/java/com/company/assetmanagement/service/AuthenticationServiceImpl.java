package com.company.assetmanagement.service;

import com.company.assetmanagement.dto.AuditEventDTO;
import com.company.assetmanagement.dto.LoginRequest;
import com.company.assetmanagement.dto.TokenResponse;
import com.company.assetmanagement.exception.AccountDisabledException;
import com.company.assetmanagement.exception.AccountLockedException;
import com.company.assetmanagement.exception.UserNotFoundException;
import com.company.assetmanagement.model.Action;
import com.company.assetmanagement.model.Role;
import com.company.assetmanagement.model.Session;
import com.company.assetmanagement.model.User;
import com.company.assetmanagement.repository.SessionRepository;
import com.company.assetmanagement.repository.UserRepository;
import com.company.assetmanagement.security.JwtTokenProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Implementation of AuthenticationService for user authentication operations.
 * 
 * This service handles:
 * - User login with credential validation
 * - Account status verification (active, locked)
 * - JWT token generation and management
 * - Session creation and tracking
 * - Account locking after failed attempts
 * - Audit logging for all authentication events
 * 
 * Security Implementation:
 * - BCrypt password hashing and validation
 * - Account locking after 5 failed attempts
 * - 30-minute lock duration
 * - Automatic lock expiration checking
 * - Session-based token tracking
 * 
 * Requirements: 1.1, 1.2, 1.3, 1.4, 1.5, 1.6, 1.7, 1.8
 * 
 * @author IT Asset Management System
 * @version 1.0
 * @since 2024-01-15
 */
@Service
@Transactional
public class AuthenticationServiceImpl implements AuthenticationService {
    
    private static final Logger logger = LoggerFactory.getLogger(AuthenticationServiceImpl.class);
    
    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final int LOCK_DURATION_MINUTES = 30;
    private static final long ACCESS_TOKEN_EXPIRATION_MS = 1800000; // 30 minutes
    
    private final UserRepository userRepository;
    private final SessionRepository sessionRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuditService auditService;
    
    /**
     * Constructor with dependency injection.
     *
     * @param userRepository repository for user data access
     * @param sessionRepository repository for session data access
     * @param passwordEncoder encoder for password validation
     * @param jwtTokenProvider provider for JWT token operations
     * @param auditService service for audit logging
     */
    public AuthenticationServiceImpl(
            UserRepository userRepository,
            SessionRepository sessionRepository,
            PasswordEncoder passwordEncoder,
            JwtTokenProvider jwtTokenProvider,
            AuditService auditService) {
        this.userRepository = userRepository;
        this.sessionRepository = sessionRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
        this.auditService = auditService;
    }
    
    /**
     * Authenticates a user with username and password.
     * 
     * Implementation follows these steps:
     * 1. Find user by username
     * 2. Check if account is active
     * 3. Check if account is locked (and unlock if lock expired)
     * 4. Validate password
     * 5. Generate tokens
     * 6. Create session
     * 7. Update user login info
     * 8. Log success
     * 
     * On failure:
     * - Increment failed attempts
     * - Lock account if threshold reached
     * - Log failure
     *
     * @param request login credentials
     * @return token response with access and refresh tokens
     * @throws BadCredentialsException if credentials are invalid
     * @throws AccountLockedException if account is locked
     * @throws AccountDisabledException if account is inactive
     */
    @Override
    public TokenResponse login(LoginRequest request) {
        logger.debug("Login attempt for username: {}", request.getUsername());
        
        // 1. Find user by username
        User user = userRepository.findByUsernameWithRoles(request.getUsername())
                .orElseThrow(() -> {
                    logger.warn("Login failed: User not found - {}", request.getUsername());
                    logAuthenticationFailure(null, request.getUsername(), "User not found");
                    return new BadCredentialsException("Invalid username or password");
                });
        
        try {
            // 2. Check if account is active
            if (!user.getIsActive()) {
                logger.warn("Login failed: Account disabled - {}", user.getUsername());
                logAuthenticationFailure(user.getId(), user.getUsername(), "Account disabled");
                throw new AccountDisabledException(user.getId().toString());
            }
            
            // 3. Check if account is locked
            checkAccountLock(user);
            
            // 4. Validate password
            if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
                logger.warn("Login failed: Invalid password - {}", user.getUsername());
                handleFailedLogin(user);
                throw new BadCredentialsException("Invalid username or password");
            }
            
            // 5. Generate tokens
            String roles = user.getRoles().stream()
                    .map(userRole -> "ROLE_" + userRole.getRole().name())
                    .collect(Collectors.joining(","));
            
            String accessToken = jwtTokenProvider.generateToken(
                    user.getId().toString(),
                    user.getUsername(),
                    roles
            );
            
            String refreshToken = jwtTokenProvider.generateRefreshToken(user.getUsername());
            
            // 6. Create session
            LocalDateTime tokenExpiration = LocalDateTime.now()
                    .plusSeconds(ACCESS_TOKEN_EXPIRATION_MS / 1000);
            
            Session session = new Session(user, tokenExpiration);
            session.setAccessTokenHash(generateTokenHash(accessToken));
            session.setRefreshTokenHash(generateTokenHash(refreshToken));
            sessionRepository.save(session);
            
            // 7. Update user login info
            user.updateLastLogin();
            user.resetFailedLoginAttempts();
            if (user.getAccountLocked()) {
                user.unlockAccount();
            }
            userRepository.save(user);
            
            // 8. Log success
            logAuthenticationSuccess(user);
            
            logger.info("Login successful for user: {}", user.getUsername());
            
            // Return token response
            return new TokenResponse(
                    accessToken,
                    refreshToken,
                    ACCESS_TOKEN_EXPIRATION_MS / 1000 // Convert to seconds
            );
            
        } catch (AccountLockedException | AccountDisabledException e) {
            throw e;
        } catch (BadCredentialsException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Unexpected error during login for user: {}", request.getUsername(), e);
            logAuthenticationFailure(user.getId(), user.getUsername(), "System error");
            throw new RuntimeException("Authentication failed due to system error", e);
        }
    }
    
    /**
     * Logs out a user and invalidates their session.
     *
     * @param userId the user ID to logout
     */
    @Override
    public void logout(String userId) {
        logger.debug("Logout request for user ID: {}", userId);
        
        if (userId == null || userId.isBlank()) {
            throw new IllegalArgumentException("User ID cannot be null or empty");
        }
        
        try {
            UUID userUuid = UUID.fromString(userId);
            
            // Find user
            User user = userRepository.findById(userUuid)
                    .orElseThrow(() -> new UserNotFoundException(userId));
            
            // Invalidate all active sessions
            int invalidatedCount = sessionRepository.invalidateAllUserSessionsByUserId(
                    userUuid,
                    LocalDateTime.now()
            );
            
            logger.info("Logout successful for user: {} - {} sessions invalidated", 
                    user.getUsername(), invalidatedCount);
            
            // Log logout event
            logLogoutEvent(user);
            
        } catch (IllegalArgumentException e) {
            logger.error("Invalid user ID format: {}", userId);
            throw new IllegalArgumentException("Invalid user ID format", e);
        }
    }
    
    /**
     * Refreshes an access token using a valid refresh token.
     *
     * @param refreshToken the refresh token
     * @return new token response
     * @throws BadCredentialsException if refresh token is invalid
     * @throws AccountDisabledException if user account is inactive
     */
    @Override
    public TokenResponse refreshToken(String refreshToken) {
        logger.debug("Token refresh request");
        
        // 1. Validate refresh token
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            logger.warn("Token refresh failed: Invalid refresh token");
            throw new BadCredentialsException("Invalid refresh token");
        }
        
        // 2. Check if it's actually a refresh token
        if (!jwtTokenProvider.isRefreshToken(refreshToken)) {
            logger.warn("Token refresh failed: Not a refresh token");
            throw new BadCredentialsException("Invalid refresh token type");
        }
        
        // 3. Extract username from token
        String username = jwtTokenProvider.getUsernameFromToken(refreshToken);
        
        // 4. Find user
        User user = userRepository.findByUsernameWithRoles(username)
                .orElseThrow(() -> {
                    logger.warn("Token refresh failed: User not found - {}", username);
                    return new BadCredentialsException("User not found");
                });
        
        // 5. Verify account is still active
        if (!user.getIsActive()) {
            logger.warn("Token refresh failed: Account disabled - {}", username);
            throw new AccountDisabledException(user.getId().toString());
        }
        
        // 6. Generate new access token
        String roles = user.getRoles().stream()
                .map(userRole -> userRole.getRole().name())
                .collect(Collectors.joining(","));
        
        String newAccessToken = jwtTokenProvider.generateToken(
                user.getId().toString(),
                user.getUsername(),
                roles
        );
        
        // 7. Generate new refresh token (token rotation for security)
        String newRefreshToken = jwtTokenProvider.generateRefreshToken(user.getUsername());
        
        // 8. Update session with new tokens
        String refreshTokenHash = generateTokenHash(refreshToken);
        Session session = sessionRepository.findActiveSessionByRefreshTokenHash(refreshTokenHash)
                .orElse(null);
        
        if (session != null) {
            LocalDateTime newExpiration = LocalDateTime.now()
                    .plusSeconds(ACCESS_TOKEN_EXPIRATION_MS / 1000);
            session.updateTokenHashes(
                    generateTokenHash(newAccessToken),
                    generateTokenHash(newRefreshToken)
            );
            session.updateTokenExpiration(newExpiration);
            sessionRepository.save(session);
        }
        
        // 9. Log token refresh event
        logTokenRefreshEvent(user);
        
        logger.info("Token refresh successful for user: {}", username);
        
        return new TokenResponse(
                newAccessToken,
                newRefreshToken,
                ACCESS_TOKEN_EXPIRATION_MS / 1000
        );
    }
    
    // ========================================================================
    // Private Helper Methods
    // ========================================================================
    
    /**
     * Checks if account is locked and unlocks if lock period has expired.
     *
     * @param user the user to check
     * @throws AccountLockedException if account is currently locked
     */
    private void checkAccountLock(User user) {
        if (user.getAccountLocked()) {
            if (user.getLockUntil() != null && user.getLockUntil().isAfter(LocalDateTime.now())) {
                // Account is still locked
                logger.warn("Login attempt for locked account: {} - locked until {}", 
                        user.getUsername(), user.getLockUntil());
                throw new AccountLockedException(user.getLockUntil());
            } else {
                // Lock has expired, unlock the account
                logger.info("Unlocking expired account lock for user: {}", user.getUsername());
                user.unlockAccount();
                userRepository.save(user);
            }
        }
    }
    
    /**
     * Handles failed login attempt by incrementing counter and locking if needed.
     *
     * @param user the user with failed login
     */
    private void handleFailedLogin(User user) {
        user.incrementFailedLoginAttempts();
        userRepository.save(user);
        
        if (user.getAccountLocked()) {
            logger.warn("Account locked due to failed attempts: {} - locked until {}", 
                    user.getUsername(), user.getLockUntil());
        }
        
        logAuthenticationFailure(user.getId(), user.getUsername(), "Invalid password");
    }
    
    /**
     * Generates a simple hash of a token for storage.
     * In production, use a proper hashing algorithm.
     *
     * @param token the token to hash
     * @return hash of the token
     */
    private String generateTokenHash(String token) {
        // Simple hash for now - in production use SHA-256 or similar
        return Integer.toHexString(token.hashCode());
    }
    
    // ========================================================================
    // Audit Logging Methods
    // ========================================================================
    
    /**
     * Logs successful authentication event.
     *
     * @param user the authenticated user
     */
    private void logAuthenticationSuccess(User user) {
        try {
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("lastLoginAt", user.getLastLoginAt());
            metadata.put("failedAttemptsReset", true);
            
            AuditEventDTO event = AuditEventDTO.builder()
                    .userId(user.getId())
                    .username(user.getUsername())
                    .actionType(Action.LOGIN_SUCCESS)
                    .resourceType("USER")
                    .resourceId(user.getId().toString())
                    .metadata(metadata)
                    .build();
            
            auditService.logEvent(event);
        } catch (Exception e) {
            logger.error("Failed to log authentication success audit event", e);
        }
    }
    
    /**
     * Logs failed authentication event.
     *
     * @param userId the user ID (may be null if user not found)
     * @param username the attempted username
     * @param reason the failure reason
     */
    private void logAuthenticationFailure(UUID userId, String username, String reason) {
        try {
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("reason", reason);
            metadata.put("success", false);
            
            AuditEventDTO event = AuditEventDTO.builder()
                    .userId(userId)
                    .username(username)
                    .actionType(Action.LOGIN_FAILURE)
                    .resourceType("USER")
                    .resourceId(userId != null ? userId.toString() : "unknown")
                    .metadata(metadata)
                    .build();
            
            auditService.logEvent(event);
        } catch (Exception e) {
            logger.error("Failed to log authentication failure audit event", e);
        }
    }
    
    /**
     * Logs logout event.
     *
     * @param user the user logging out
     */
    private void logLogoutEvent(User user) {
        try {
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("action", "logout");
            
            AuditEventDTO event = AuditEventDTO.builder()
                    .userId(user.getId())
                    .username(user.getUsername())
                    .actionType(Action.LOGOUT)
                    .resourceType("USER")
                    .resourceId(user.getId().toString())
                    .metadata(metadata)
                    .build();
            
            auditService.logEvent(event);
        } catch (Exception e) {
            logger.error("Failed to log logout audit event", e);
        }
    }
    
    /**
     * Logs token refresh event.
     *
     * @param user the user refreshing token
     */
    private void logTokenRefreshEvent(User user) {
        try {
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("action", "token_refresh");
            
            AuditEventDTO event = AuditEventDTO.builder()
                    .userId(user.getId())
                    .username(user.getUsername())
                    .actionType(Action.TOKEN_REFRESH)
                    .resourceType("USER")
                    .resourceId(user.getId().toString())
                    .metadata(metadata)
                    .build();
            
            auditService.logEvent(event);
        } catch (Exception e) {
            logger.error("Failed to log token refresh audit event", e);
        }
    }
}
