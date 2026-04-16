package com.company.assetmanagement.security;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Date;

import static org.assertj.core.api.Assertions.*;

@DisplayName("JwtTokenProvider Unit Tests")
class JwtTokenProviderTest {
    
    private JwtTokenProvider tokenProvider;
    private static final String TEST_SECRET = "testSecretKeyForJwtTokenProviderMinimum256BitsRequired";
    private static final long JWT_EXPIRATION = 1800000; // 30 minutes
    private static final long REFRESH_EXPIRATION = 86400000; // 24 hours
    
    @BeforeEach
    void setUp() {
        tokenProvider = new JwtTokenProvider(TEST_SECRET, JWT_EXPIRATION, REFRESH_EXPIRATION);
    }
    
    @Test
    @DisplayName("Should generate valid JWT token from authentication")
    void shouldGenerateValidToken() {
        // Given
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                "testuser",
                "password",
                Arrays.asList(
                        new SimpleGrantedAuthority("ROLE_ADMINISTRATOR"),
                        new SimpleGrantedAuthority("ROLE_ASSET_MANAGER")
                )
        );
        
        // When
        String token = tokenProvider.generateToken(authentication);
        
        // Then
        assertThat(token).isNotNull();
        assertThat(token).isNotEmpty();
        assertThat(tokenProvider.validateToken(token)).isTrue();
    }
    
    @Test
    @DisplayName("Should extract username from token")
    void shouldExtractUsernameFromToken() {
        // Given
        String expectedUsername = "testuser";
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                expectedUsername,
                "password",
                Arrays.asList(new SimpleGrantedAuthority("ROLE_ADMINISTRATOR"))
        );
        String token = tokenProvider.generateToken(authentication);
        
        // When
        String actualUsername = tokenProvider.getUsernameFromToken(token);
        
        // Then
        assertThat(actualUsername).isEqualTo(expectedUsername);
    }
    
    @Test
    @DisplayName("Should include roles in token claims")
    void shouldIncludeRolesInToken() {
        // Given
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                "testuser",
                "password",
                Arrays.asList(
                        new SimpleGrantedAuthority("ROLE_ADMINISTRATOR"),
                        new SimpleGrantedAuthority("ROLE_ASSET_MANAGER")
                )
        );
        String token = tokenProvider.generateToken(authentication);
        
        // When
        SecretKey key = Keys.hmacShaKeyFor(TEST_SECRET.getBytes(StandardCharsets.UTF_8));
        String roles = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .get("roles", String.class);
        
        // Then
        assertThat(roles).contains("ROLE_ADMINISTRATOR");
        assertThat(roles).contains("ROLE_ASSET_MANAGER");
    }
    
    @Test
    @DisplayName("Should generate refresh token")
    void shouldGenerateRefreshToken() {
        // Given
        String username = "testuser";
        
        // When
        String refreshToken = tokenProvider.generateRefreshToken(username);
        
        // Then
        assertThat(refreshToken).isNotNull();
        assertThat(refreshToken).isNotEmpty();
        assertThat(tokenProvider.validateToken(refreshToken)).isTrue();
        assertThat(tokenProvider.isRefreshToken(refreshToken)).isTrue();
    }
    
    @Test
    @DisplayName("Should distinguish between access and refresh tokens")
    void shouldDistinguishTokenTypes() {
        // Given
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                "testuser",
                "password",
                Arrays.asList(new SimpleGrantedAuthority("ROLE_ADMINISTRATOR"))
        );
        String accessToken = tokenProvider.generateToken(authentication);
        String refreshToken = tokenProvider.generateRefreshToken("testuser");
        
        // When/Then
        assertThat(tokenProvider.isRefreshToken(accessToken)).isFalse();
        assertThat(tokenProvider.isRefreshToken(refreshToken)).isTrue();
    }
    
    @Test
    @DisplayName("Should validate valid token")
    void shouldValidateValidToken() {
        // Given
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                "testuser",
                "password",
                Arrays.asList(new SimpleGrantedAuthority("ROLE_ADMINISTRATOR"))
        );
        String token = tokenProvider.generateToken(authentication);
        
        // When
        boolean isValid = tokenProvider.validateToken(token);
        
        // Then
        assertThat(isValid).isTrue();
    }
    
    @Test
    @DisplayName("Should reject invalid token")
    void shouldRejectInvalidToken() {
        // Given
        String invalidToken = "invalid.jwt.token";
        
        // When
        boolean isValid = tokenProvider.validateToken(invalidToken);
        
        // Then
        assertThat(isValid).isFalse();
    }
    
    @Test
    @DisplayName("Should reject token with wrong signature")
    void shouldRejectTokenWithWrongSignature() {
        // Given
        String wrongSecret = "wrongSecretKeyForJwtTokenProviderMinimum256BitsRequired";
        SecretKey wrongKey = Keys.hmacShaKeyFor(wrongSecret.getBytes(StandardCharsets.UTF_8));
        
        String tokenWithWrongSignature = Jwts.builder()
                .subject("testuser")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + JWT_EXPIRATION))
                .signWith(wrongKey)
                .compact();
        
        // When
        boolean isValid = tokenProvider.validateToken(tokenWithWrongSignature);
        
        // Then
        assertThat(isValid).isFalse();
    }
    
    @Test
    @DisplayName("Should reject expired token")
    void shouldRejectExpiredToken() {
        // Given
        SecretKey key = Keys.hmacShaKeyFor(TEST_SECRET.getBytes(StandardCharsets.UTF_8));
        String expiredToken = Jwts.builder()
                .subject("testuser")
                .issuedAt(new Date(System.currentTimeMillis() - 3600000)) // 1 hour ago
                .expiration(new Date(System.currentTimeMillis() - 1800000)) // 30 minutes ago
                .signWith(key)
                .compact();
        
        // When
        boolean isValid = tokenProvider.validateToken(expiredToken);
        
        // Then
        assertThat(isValid).isFalse();
    }
    
    @Test
    @DisplayName("Should handle null token gracefully")
    void shouldHandleNullToken() {
        // When/Then
        assertThat(tokenProvider.validateToken(null)).isFalse();
    }
    
    @Test
    @DisplayName("Should handle empty token gracefully")
    void shouldHandleEmptyToken() {
        // When/Then
        assertThat(tokenProvider.validateToken("")).isFalse();
    }
    
    @Test
    @DisplayName("Should pad short secret key")
    void shouldPadShortSecretKey() {
        // Given
        String shortSecret = "short";
        
        // When
        JwtTokenProvider provider = new JwtTokenProvider(shortSecret, JWT_EXPIRATION, REFRESH_EXPIRATION);
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                "testuser",
                "password",
                Arrays.asList(new SimpleGrantedAuthority("ROLE_ADMINISTRATOR"))
        );
        String token = provider.generateToken(authentication);
        
        // Then
        assertThat(token).isNotNull();
        assertThat(provider.validateToken(token)).isTrue();
    }
    
    @Test
    @DisplayName("Should generate token with userId and roles")
    void shouldGenerateTokenWithUserIdAndRoles() {
        // Given
        String userId = "550e8400-e29b-41d4-a716-446655440000";
        String username = "testuser";
        String roles = "ROLE_ADMINISTRATOR,ROLE_ASSET_MANAGER";
        
        // When
        String token = tokenProvider.generateToken(userId, username, roles);
        
        // Then
        assertThat(token).isNotNull();
        assertThat(token).isNotEmpty();
        assertThat(tokenProvider.validateToken(token)).isTrue();
    }
    
    @Test
    @DisplayName("Should extract userId from token")
    void shouldExtractUserIdFromToken() {
        // Given
        String expectedUserId = "550e8400-e29b-41d4-a716-446655440000";
        String username = "testuser";
        String roles = "ROLE_ADMINISTRATOR";
        String token = tokenProvider.generateToken(expectedUserId, username, roles);
        
        // When
        String actualUserId = tokenProvider.getUserIdFromToken(token);
        
        // Then
        assertThat(actualUserId).isEqualTo(expectedUserId);
    }
    
    @Test
    @DisplayName("Should extract roles from token")
    void shouldExtractRolesFromToken() {
        // Given
        String userId = "550e8400-e29b-41d4-a716-446655440000";
        String username = "testuser";
        String expectedRoles = "ROLE_ADMINISTRATOR,ROLE_ASSET_MANAGER";
        String token = tokenProvider.generateToken(userId, username, expectedRoles);
        
        // When
        String actualRoles = tokenProvider.getRolesFromToken(token);
        
        // Then
        assertThat(actualRoles).isEqualTo(expectedRoles);
    }
    
    @Test
    @DisplayName("Should include userId in token claims")
    void shouldIncludeUserIdInToken() {
        // Given
        String expectedUserId = "550e8400-e29b-41d4-a716-446655440000";
        String username = "testuser";
        String roles = "ROLE_ADMINISTRATOR";
        String token = tokenProvider.generateToken(expectedUserId, username, roles);
        
        // When
        SecretKey key = Keys.hmacShaKeyFor(TEST_SECRET.getBytes(StandardCharsets.UTF_8));
        String userId = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .get("userId", String.class);
        
        // Then
        assertThat(userId).isEqualTo(expectedUserId);
    }
    
    @Test
    @DisplayName("Should extract username from token with userId")
    void shouldExtractUsernameFromTokenWithUserId() {
        // Given
        String userId = "550e8400-e29b-41d4-a716-446655440000";
        String expectedUsername = "testuser";
        String roles = "ROLE_ADMINISTRATOR";
        String token = tokenProvider.generateToken(userId, expectedUsername, roles);
        
        // When
        String actualUsername = tokenProvider.getUsernameFromToken(token);
        
        // Then
        assertThat(actualUsername).isEqualTo(expectedUsername);
    }
    
    @Test
    @DisplayName("Should validate token generated with userId")
    void shouldValidateTokenGeneratedWithUserId() {
        // Given
        String userId = "550e8400-e29b-41d4-a716-446655440000";
        String username = "testuser";
        String roles = "ROLE_ADMINISTRATOR";
        String token = tokenProvider.generateToken(userId, username, roles);
        
        // When
        boolean isValid = tokenProvider.validateToken(token);
        
        // Then
        assertThat(isValid).isTrue();
    }
}
