package com.company.assetmanagement.security;

import com.company.assetmanagement.exception.AccountDisabledException;
import com.company.assetmanagement.exception.AccountLockedException;
import com.company.assetmanagement.model.User;
import com.company.assetmanagement.repository.UserRepository;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;

/**
 * JWT Authentication Filter for processing JWT tokens in requests.
 * 
 * This filter:
 * - Extracts JWT tokens from the Authorization header
 * - Validates token signature and expiration
 * - Loads user details and verifies account status
 * - Sets authentication in SecurityContext for valid requests
 * - Handles token validation errors with appropriate logging
 * 
 * Account Status Validation:
 * - Verifies user account is active (not disabled)
 * - Checks if account is locked and validates lock expiration
 * - Automatically unlocks accounts when lock period expires
 * 
 * Error Handling:
 * - Expired tokens: Logged with specific error message
 * - Invalid signatures: Logged as security warning
 * - Malformed tokens: Logged with error details
 * - Account locked: Logged with lock expiration time
 * - Account disabled: Logged with user information
 * 
 * Requirements: 1.3, 1.4, 1.5, 12.1, 12.2
 * 
 * @author IT Asset Management System
 * @version 1.0
 * @since 2024-01-15
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    
    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    
    @Autowired
    private JwtTokenProvider tokenProvider;
    
    @Autowired
    private UserDetailsService userDetailsService;
    
    @Autowired
    private UserRepository userRepository;
    
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            String jwt = getJwtFromRequest(request);
            
            if (StringUtils.hasText(jwt)) {
                // Validate token signature and expiration
                if (tokenProvider.validateToken(jwt)) {
                    String username = tokenProvider.getUsernameFromToken(jwt);
                    
                    // Load user details
                    UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                    
                    // Validate account status (active and not locked)
                    validateAccountStatus(username);
                    
                    // Create authentication token
                    UsernamePasswordAuthenticationToken authentication = 
                            new UsernamePasswordAuthenticationToken(
                                    userDetails, 
                                    null, 
                                    userDetails.getAuthorities()
                            );
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    
                    // Set authentication in security context
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    logger.debug("Successfully authenticated user: {}", username);
                } else {
                    logger.debug("Invalid JWT token in request");
                }
            }
        } catch (ExpiredJwtException ex) {
            logger.error("JWT token has expired: {}", ex.getMessage());
            // Token expired - authentication will fail, user needs to refresh token
        } catch (SignatureException ex) {
            logger.error("Invalid JWT signature: {}", ex.getMessage());
            // Invalid signature - possible tampering attempt
        } catch (MalformedJwtException ex) {
            logger.error("Malformed JWT token: {}", ex.getMessage());
            // Token format is invalid
        } catch (AccountLockedException ex) {
            logger.warn("Authentication attempt with locked account: {}", ex.getMessage());
            // Account is locked - user must wait for unlock time
        } catch (AccountDisabledException ex) {
            logger.warn("Authentication attempt with disabled account: {}", ex.getMessage());
            // Account is disabled - user cannot authenticate
        } catch (UsernameNotFoundException ex) {
            logger.error("User not found during JWT authentication: {}", ex.getMessage());
            // User doesn't exist - token may be for deleted user
        } catch (Exception ex) {
            logger.error("Could not set user authentication in security context", ex);
            // Unexpected error - log for investigation
        }
        
        // Continue filter chain regardless of authentication result
        // Failed authentication will result in 401/403 from Spring Security
        filterChain.doFilter(request, response);
    }
    
    /**
     * Validates the account status for the authenticated user.
     * 
     * Checks:
     * 1. Account is active (not disabled)
     * 2. Account is not locked, or lock has expired
     * 3. Automatically unlocks accounts when lock period expires
     * 
     * @param username the username to validate
     * @throws AccountDisabledException if account is disabled
     * @throws AccountLockedException if account is currently locked
     * @throws UsernameNotFoundException if user doesn't exist
     */
    private void validateAccountStatus(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
        
        // Check if account is active
        if (!user.getIsActive()) {
            logger.warn("Disabled account attempted authentication: {}", username);
            throw new AccountDisabledException("Account is disabled", user.getId().toString());
        }
        
        // Check if account is locked
        if (user.getAccountLocked()) {
            LocalDateTime now = LocalDateTime.now();
            
            // Check if lock has expired
            if (user.getLockUntil() != null && user.getLockUntil().isBefore(now)) {
                // Lock has expired - automatically unlock the account
                user.unlockAccount();
                userRepository.save(user);
                logger.info("Automatically unlocked expired account: {}", username);
            } else {
                // Account is still locked
                logger.warn("Locked account attempted authentication: {} (locked until: {})", 
                           username, user.getLockUntil());
                throw new AccountLockedException(
                    "Account is locked until " + user.getLockUntil(),
                    user.getLockUntil()
                );
            }
        }
    }
    
    /**
     * Extracts JWT token from the Authorization header.
     * 
     * Expected format: "Bearer {token}"
     * 
     * @param request the HTTP request
     * @return JWT token string, or null if not present or invalid format
     */
    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
