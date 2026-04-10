package com.company.assetmanagement.security;

import com.company.assetmanagement.service.AuthenticationEventService;
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
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT Authentication Filter for processing JWT tokens in requests.
 * Extracts JWT from Authorization header and authenticates the request.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    
    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    
    @Autowired
    private JwtTokenProvider tokenProvider;
    
    @Autowired
    private UserDetailsService userDetailsService;
    
    @Autowired
    private AuthenticationEventService authenticationEventService;
    
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            String jwt = getJwtFromRequest(request);
            
            if (StringUtils.hasText(jwt)) {
                if (tokenProvider.validateToken(jwt)) {
                    String username = tokenProvider.getUsernameFromToken(jwt);
                    
                    UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                    UsernamePasswordAuthenticationToken authentication = 
                            new UsernamePasswordAuthenticationToken(
                                    userDetails, 
                                    null, 
                                    userDetails.getAuthorities()
                            );
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    logger.debug("Set authentication for user: {}", username);
                } else {
                    // Log invalid token attempt
                    String ipAddress = authenticationEventService.getClientIpAddress(request);
                    authenticationEventService.logInvalidTokenAttempt("Token validation failed", ipAddress);
                    logger.warn("Invalid JWT token from IP: {}", ipAddress);
                }
            }
        } catch (io.jsonwebtoken.ExpiredJwtException ex) {
            // Log expired token
            String ipAddress = authenticationEventService.getClientIpAddress(request);
            authenticationEventService.logInvalidTokenAttempt("Token expired", ipAddress);
            logger.warn("Expired JWT token from IP: {}", ipAddress);
        } catch (io.jsonwebtoken.MalformedJwtException ex) {
            // Log malformed token
            String ipAddress = authenticationEventService.getClientIpAddress(request);
            authenticationEventService.logInvalidTokenAttempt("Malformed token", ipAddress);
            logger.warn("Malformed JWT token from IP: {}", ipAddress);
        } catch (io.jsonwebtoken.SignatureException ex) {
            // Log signature validation failure
            String ipAddress = authenticationEventService.getClientIpAddress(request);
            authenticationEventService.logInvalidTokenAttempt("Invalid signature", ipAddress);
            logger.warn("Invalid JWT signature from IP: {}", ipAddress);
        } catch (Exception ex) {
            // Log general authentication error
            String ipAddress = authenticationEventService.getClientIpAddress(request);
            authenticationEventService.logInvalidTokenAttempt("Authentication error: " + ex.getMessage(), ipAddress);
            logger.error("Could not set user authentication in security context", ex);
        }
        
        filterChain.doFilter(request, response);
    }
    
    /**
     * Extract JWT token from Authorization header.
     *
     * @param request the HTTP request
     * @return JWT token or null if not present
     */
    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
