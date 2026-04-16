package com.company.assetmanagement.security;

import com.company.assetmanagement.exception.AccountDisabledException;
import com.company.assetmanagement.exception.AccountLockedException;
import com.company.assetmanagement.model.Role;
import com.company.assetmanagement.model.User;
import com.company.assetmanagement.model.UserRole;
import com.company.assetmanagement.repository.UserRepository;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for JwtAuthenticationFilter.
 * 
 * Tests cover:
 * - Valid token authentication
 * - Invalid token handling
 * - Expired token handling
 * - Account status validation (active, locked)
 * - Automatic account unlocking
 * - Error handling for various scenarios
 */
@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {
    
    @Mock
    private JwtTokenProvider tokenProvider;
    
    @Mock
    private UserDetailsService userDetailsService;
    
    @Mock
    private UserRepository userRepository;
    
    @Mock
    private FilterChain filterChain;
    
    @InjectMocks
    private JwtAuthenticationFilter jwtAuthenticationFilter;
    
    private MockHttpServletRequest request;
    private MockHttpServletResponse response;
    private User testUser;
    private UserDetails userDetails;
    
    @BeforeEach
    void setUp() {
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        SecurityContextHolder.clearContext();
        
        // Create test user
        testUser = new User("testuser", "hashedPassword", "test@example.com");
        testUser.setId(UUID.randomUUID());
        testUser.setIsActive(true);
        testUser.setAccountLocked(false);
        
        // Add role to test user
        UserRole userRole = new UserRole();
        userRole.setRole(Role.VIEWER);
        userRole.setUser(testUser);
        testUser.getRoles().add(userRole);
        
        // Create UserDetails
        userDetails = org.springframework.security.core.userdetails.User.builder()
                .username("testuser")
                .password("hashedPassword")
                .authorities(new SimpleGrantedAuthority("ROLE_VIEWER"))
                .build();
    }
    
    @Test
    @DisplayName("Should authenticate successfully with valid token and active account")
    void shouldAuthenticateWithValidToken() throws ServletException, IOException {
        // Given
        String validToken = "valid.jwt.token";
        request.addHeader("Authorization", "Bearer " + validToken);
        
        when(tokenProvider.validateToken(validToken)).thenReturn(true);
        when(tokenProvider.getUsernameFromToken(validToken)).thenReturn("testuser");
        when(userDetailsService.loadUserByUsername("testuser")).thenReturn(userDetails);
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        
        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);
        
        // Then
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
        assertThat(SecurityContextHolder.getContext().getAuthentication().getName()).isEqualTo("testuser");
        assertThat(SecurityContextHolder.getContext().getAuthentication().isAuthenticated()).isTrue();
        
        verify(filterChain).doFilter(request, response);
        verify(tokenProvider).validateToken(validToken);
        verify(userDetailsService).loadUserByUsername("testuser");
        verify(userRepository).findByUsername("testuser");
    }
    
    @Test
    @DisplayName("Should not authenticate when Authorization header is missing")
    void shouldNotAuthenticateWithoutAuthorizationHeader() throws ServletException, IOException {
        // Given - no Authorization header
        
        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);
        
        // Then
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(request, response);
        verify(tokenProvider, never()).validateToken(anyString());
    }
    
    @Test
    @DisplayName("Should not authenticate when token is invalid")
    void shouldNotAuthenticateWithInvalidToken() throws ServletException, IOException {
        // Given
        String invalidToken = "invalid.jwt.token";
        request.addHeader("Authorization", "Bearer " + invalidToken);
        
        when(tokenProvider.validateToken(invalidToken)).thenReturn(false);
        
        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);
        
        // Then
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(request, response);
        verify(tokenProvider).validateToken(invalidToken);
        verify(userDetailsService, never()).loadUserByUsername(anyString());
    }
    
    @Test
    @DisplayName("Should handle expired token gracefully")
    void shouldHandleExpiredToken() throws ServletException, IOException {
        // Given
        String expiredToken = "expired.jwt.token";
        request.addHeader("Authorization", "Bearer " + expiredToken);
        
        when(tokenProvider.validateToken(expiredToken)).thenReturn(true);
        when(tokenProvider.getUsernameFromToken(expiredToken))
                .thenThrow(new ExpiredJwtException(null, null, "Token expired"));
        
        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);
        
        // Then
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(request, response);
    }
    
    @Test
    @DisplayName("Should handle malformed token gracefully")
    void shouldHandleMalformedToken() throws ServletException, IOException {
        // Given
        String malformedToken = "malformed.token";
        request.addHeader("Authorization", "Bearer " + malformedToken);
        
        when(tokenProvider.validateToken(malformedToken)).thenReturn(true);
        when(tokenProvider.getUsernameFromToken(malformedToken))
                .thenThrow(new MalformedJwtException("Malformed token"));
        
        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);
        
        // Then
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(request, response);
    }
    
    @Test
    @DisplayName("Should handle invalid signature gracefully")
    void shouldHandleInvalidSignature() throws ServletException, IOException {
        // Given
        String tamperedToken = "tampered.jwt.token";
        request.addHeader("Authorization", "Bearer " + tamperedToken);
        
        when(tokenProvider.validateToken(tamperedToken)).thenReturn(true);
        when(tokenProvider.getUsernameFromToken(tamperedToken))
                .thenThrow(new SignatureException("Invalid signature"));
        
        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);
        
        // Then
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(request, response);
    }
    
    @Test
    @DisplayName("Should reject authentication for disabled account")
    void shouldRejectDisabledAccount() throws ServletException, IOException {
        // Given
        String validToken = "valid.jwt.token";
        request.addHeader("Authorization", "Bearer " + validToken);
        
        testUser.setIsActive(false);  // Disable account
        
        when(tokenProvider.validateToken(validToken)).thenReturn(true);
        when(tokenProvider.getUsernameFromToken(validToken)).thenReturn("testuser");
        when(userDetailsService.loadUserByUsername("testuser")).thenReturn(userDetails);
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        
        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);
        
        // Then
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(request, response);
    }
    
    @Test
    @DisplayName("Should reject authentication for locked account")
    void shouldRejectLockedAccount() throws ServletException, IOException {
        // Given
        String validToken = "valid.jwt.token";
        request.addHeader("Authorization", "Bearer " + validToken);
        
        testUser.setAccountLocked(true);
        testUser.setLockUntil(LocalDateTime.now().plusMinutes(30));  // Locked for 30 minutes
        
        when(tokenProvider.validateToken(validToken)).thenReturn(true);
        when(tokenProvider.getUsernameFromToken(validToken)).thenReturn("testuser");
        when(userDetailsService.loadUserByUsername("testuser")).thenReturn(userDetails);
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        
        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);
        
        // Then
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(request, response);
    }
    
    @Test
    @DisplayName("Should automatically unlock account when lock period expires")
    void shouldAutoUnlockExpiredLock() throws ServletException, IOException {
        // Given
        String validToken = "valid.jwt.token";
        request.addHeader("Authorization", "Bearer " + validToken);
        
        testUser.setAccountLocked(true);
        testUser.setLockUntil(LocalDateTime.now().minusMinutes(1));  // Lock expired 1 minute ago
        
        when(tokenProvider.validateToken(validToken)).thenReturn(true);
        when(tokenProvider.getUsernameFromToken(validToken)).thenReturn("testuser");
        when(userDetailsService.loadUserByUsername("testuser")).thenReturn(userDetails);
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        
        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);
        
        // Then
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
        assertThat(testUser.getAccountLocked()).isFalse();
        assertThat(testUser.getLockUntil()).isNull();
        assertThat(testUser.getFailedLoginAttempts()).isEqualTo(0);
        
        verify(userRepository).save(testUser);
        verify(filterChain).doFilter(request, response);
    }
    
    @Test
    @DisplayName("Should handle user not found gracefully")
    void shouldHandleUserNotFound() throws ServletException, IOException {
        // Given
        String validToken = "valid.jwt.token";
        request.addHeader("Authorization", "Bearer " + validToken);
        
        when(tokenProvider.validateToken(validToken)).thenReturn(true);
        when(tokenProvider.getUsernameFromToken(validToken)).thenReturn("nonexistent");
        when(userDetailsService.loadUserByUsername("nonexistent"))
                .thenThrow(new UsernameNotFoundException("User not found"));
        
        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);
        
        // Then
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(request, response);
    }
    
    @Test
    @DisplayName("Should extract token from Bearer authorization header")
    void shouldExtractTokenFromBearerHeader() throws ServletException, IOException {
        // Given
        String token = "my.jwt.token";
        request.addHeader("Authorization", "Bearer " + token);
        
        when(tokenProvider.validateToken(token)).thenReturn(true);
        when(tokenProvider.getUsernameFromToken(token)).thenReturn("testuser");
        when(userDetailsService.loadUserByUsername("testuser")).thenReturn(userDetails);
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        
        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);
        
        // Then
        verify(tokenProvider).validateToken(token);
        verify(filterChain).doFilter(request, response);
    }
    
    @Test
    @DisplayName("Should not extract token from non-Bearer authorization header")
    void shouldNotExtractTokenFromNonBearerHeader() throws ServletException, IOException {
        // Given
        request.addHeader("Authorization", "Basic dXNlcjpwYXNz");
        
        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);
        
        // Then
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(tokenProvider, never()).validateToken(anyString());
        verify(filterChain).doFilter(request, response);
    }
    
    @Test
    @DisplayName("Should continue filter chain even when authentication fails")
    void shouldContinueFilterChainOnAuthenticationFailure() throws ServletException, IOException {
        // Given
        String invalidToken = "invalid.token";
        request.addHeader("Authorization", "Bearer " + invalidToken);
        
        when(tokenProvider.validateToken(invalidToken)).thenReturn(false);
        
        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);
        
        // Then
        verify(filterChain).doFilter(request, response);
    }
}
