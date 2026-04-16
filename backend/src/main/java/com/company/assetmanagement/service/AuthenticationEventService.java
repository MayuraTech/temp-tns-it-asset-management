package com.company.assetmanagement.service;

import com.company.assetmanagement.dto.AuditEventDTO;
import com.company.assetmanagement.model.Action;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Service for logging authentication events.
 * Tracks login attempts, failures, and security-related events without exposing sensitive data.
 */
@Service
public class AuthenticationEventService {
    
    private static final Logger logger = LoggerFactory.getLogger(AuthenticationEventService.class);
    private static final Logger securityLogger = LoggerFactory.getLogger("SECURITY_AUDIT");
    
    private final AuditService auditService;
    
    public AuthenticationEventService(AuditService auditService) {
        this.auditService = auditService;
    }
    
    /**
     * Log successful login attempt.
     *
     * @param username the username that logged in
     * @param ipAddress the IP address of the request
     * @param userAgent the user agent string
     */
    public void logLoginSuccess(String username, String ipAddress, String userAgent) {
        try {
            // Log to security audit log
            securityLogger.info("LOGIN_SUCCESS: username={}, ip={}, userAgent={}", 
                username, ipAddress, maskUserAgent(userAgent));
            
            // Create audit event
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("userAgent", maskUserAgent(userAgent));
            metadata.put("loginTime", LocalDateTime.now().toString());
            
            AuditEventDTO event = AuditEventDTO.builder()
                .timestamp(LocalDateTime.now())
                .username(username)
                .actionType(Action.LOGIN_SUCCESS)
                .resourceType("AUTHENTICATION")
                .resourceId(username)
                .ipAddress(ipAddress)
                .metadata(metadata)
                .build();
            
            auditService.logEvent(event);
            
        } catch (Exception e) {
            logger.error("Failed to log successful login for user: {}", username, e);
        }
    }
    
    /**
     * Log failed login attempt.
     *
     * @param username the username that attempted to log in
     * @param errorType the type of error (INVALID_CREDENTIALS, ACCOUNT_LOCKED, etc.)
     * @param ipAddress the IP address of the request
     * @param userAgent the user agent string
     */
    public void logLoginFailure(String username, String errorType, String ipAddress, String userAgent) {
        try {
            // Log to security audit log (without sensitive data)
            securityLogger.warn("LOGIN_FAILURE: username={}, errorType={}, ip={}, userAgent={}", 
                username, errorType, ipAddress, maskUserAgent(userAgent));
            
            // Create audit event
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("errorType", errorType);
            metadata.put("userAgent", maskUserAgent(userAgent));
            metadata.put("attemptTime", LocalDateTime.now().toString());
            
            AuditEventDTO event = AuditEventDTO.builder()
                .timestamp(LocalDateTime.now())
                .username(username)
                .actionType(Action.LOGIN_FAILURE)
                .resourceType("AUTHENTICATION")
                .resourceId(username)
                .ipAddress(ipAddress)
                .metadata(metadata)
                .build();
            
            auditService.logEvent(event);
            
        } catch (Exception e) {
            logger.error("Failed to log failed login for user: {}", username, e);
        }
    }
    
    /**
     * Log logout event.
     *
     * @param username the username that logged out
     * @param ipAddress the IP address of the request
     */
    public void logLogout(String username, String ipAddress) {
        try {
            securityLogger.info("LOGOUT: username={}, ip={}", username, ipAddress);
            
            AuditEventDTO event = AuditEventDTO.builder()
                .timestamp(LocalDateTime.now())
                .username(username)
                .actionType(Action.LOGOUT)
                .resourceType("AUTHENTICATION")
                .resourceId(username)
                .ipAddress(ipAddress)
                .build();
            
            auditService.logEvent(event);
            
        } catch (Exception e) {
            logger.error("Failed to log logout for user: {}", username, e);
        }
    }
    
    /**
     * Log token refresh event.
     *
     * @param username the username refreshing the token
     * @param ipAddress the IP address of the request
     */
    public void logTokenRefresh(String username, String ipAddress) {
        try {
            securityLogger.debug("TOKEN_REFRESH: username={}, ip={}", username, ipAddress);
            
            AuditEventDTO event = AuditEventDTO.builder()
                .timestamp(LocalDateTime.now())
                .username(username)
                .actionType(Action.TOKEN_REFRESH)
                .resourceType("AUTHENTICATION")
                .resourceId(username)
                .ipAddress(ipAddress)
                .build();
            
            auditService.logEvent(event);
            
        } catch (Exception e) {
            logger.error("Failed to log token refresh for user: {}", username, e);
        }
    }
    
    /**
     * Log invalid token attempt.
     *
     * @param reason the reason the token was invalid
     * @param ipAddress the IP address of the request
     */
    public void logInvalidTokenAttempt(String reason, String ipAddress) {
        try {
            securityLogger.warn("INVALID_TOKEN: reason={}, ip={}", reason, ipAddress);
            
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("reason", reason);
            metadata.put("attemptTime", LocalDateTime.now().toString());
            
            AuditEventDTO event = AuditEventDTO.builder()
                .timestamp(LocalDateTime.now())
                .actionType(Action.LOGIN_FAILURE)
                .resourceType("AUTHENTICATION")
                .resourceId("INVALID_TOKEN")
                .ipAddress(ipAddress)
                .metadata(metadata)
                .build();
            
            auditService.logEvent(event);
            
        } catch (Exception e) {
            logger.error("Failed to log invalid token attempt from IP: {}", ipAddress, e);
        }
    }
    
    /**
     * Extract IP address from HTTP request.
     *
     * @param request the HTTP request
     * @return the IP address
     */
    public String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }
    
    /**
     * Mask user agent string to avoid logging excessive data.
     *
     * @param userAgent the full user agent string
     * @return masked user agent (browser and OS only)
     */
    private String maskUserAgent(String userAgent) {
        if (userAgent == null || userAgent.isEmpty()) {
            return "Unknown";
        }
        
        // Extract browser and OS info only, truncate to 100 chars
        if (userAgent.length() > 100) {
            return userAgent.substring(0, 100) + "...";
        }
        
        return userAgent;
    }
}
