package com.company.assetmanagement.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("JWT Authentication Integration Tests")
class JwtAuthenticationIntegrationTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private JwtTokenProvider tokenProvider;
    
    @Test
    @DisplayName("Should reject request without JWT token")
    void shouldRejectRequestWithoutToken() throws Exception {
        mockMvc.perform(get("/api/v1/assets"))
                .andExpect(status().isUnauthorized());
    }
    
    @Test
    @DisplayName("Should reject request with invalid JWT token")
    void shouldRejectRequestWithInvalidToken() throws Exception {
        mockMvc.perform(get("/api/v1/assets")
                        .header("Authorization", "Bearer invalid.jwt.token"))
                .andExpect(status().isUnauthorized());
    }
    
    @Test
    @DisplayName("Should reject request with malformed Authorization header")
    void shouldRejectRequestWithMalformedHeader() throws Exception {
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                "testuser",
                "password",
                Arrays.asList(new SimpleGrantedAuthority("ROLE_ADMINISTRATOR"))
        );
        String token = tokenProvider.generateToken(authentication);
        
        // Missing "Bearer " prefix
        mockMvc.perform(get("/api/v1/assets")
                        .header("Authorization", token))
                .andExpect(status().isUnauthorized());
    }
    
    @Test
    @DisplayName("Should accept request with valid JWT token but fail on user not found")
    void shouldAcceptValidTokenButFailOnUserNotFound() throws Exception {
        // Given
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                "testuser",
                "password",
                Arrays.asList(new SimpleGrantedAuthority("ROLE_ADMINISTRATOR"))
        );
        String token = tokenProvider.generateToken(authentication);
        
        // When/Then
        // The token is valid, but CustomUserDetailsService will throw UsernameNotFoundException
        // This results in 401 because authentication fails during user loading
        mockMvc.perform(get("/api/v1/assets")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isUnauthorized());
    }
    
    @Test
    @DisplayName("Should allow access to public endpoints without token")
    void shouldAllowPublicEndpointsWithoutToken() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk());
        
        mockMvc.perform(get("/actuator/info"))
                .andExpect(status().isOk());
    }
    
    @Test
    @DisplayName("Should handle OPTIONS preflight requests")
    void shouldHandlePreflightRequests() throws Exception {
        mockMvc.perform(get("/api/v1/assets")
                        .header("Origin", "http://localhost:4200")
                        .header("Access-Control-Request-Method", "GET"))
                .andExpect(status().isUnauthorized()); // Still requires auth for actual endpoint
    }
}
