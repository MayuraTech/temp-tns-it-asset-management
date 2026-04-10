package com.company.assetmanagement.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("SecurityConfig Integration Tests")
class SecurityConfigTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Test
    @DisplayName("Should allow access to public endpoints without authentication")
    void shouldAllowPublicEndpoints() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk());
    }
    
    @Test
    @DisplayName("Should reject access to protected endpoints without authentication")
    void shouldRejectProtectedEndpointsWithoutAuth() throws Exception {
        mockMvc.perform(get("/api/v1/assets"))
                .andExpect(status().isUnauthorized());
    }
    
    @Test
    @DisplayName("Should configure CORS headers")
    void shouldConfigureCorsHeaders() throws Exception {
        mockMvc.perform(options("/api/v1/assets")
                        .header("Origin", "http://localhost:4200")
                        .header("Access-Control-Request-Method", "GET"))
                .andExpect(status().isOk())
                .andExpect(header().exists("Access-Control-Allow-Origin"))
                .andExpect(header().exists("Access-Control-Allow-Methods"));
    }
    
    @Test
    @DisplayName("Should configure security headers")
    void shouldConfigureSecurityHeaders() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(header().exists("X-Content-Type-Options"))
                .andExpect(header().exists("X-Frame-Options"))
                .andExpect(header().exists("X-XSS-Protection"))
                .andExpect(header().exists("Content-Security-Policy"));
    }
    
    @Test
    @DisplayName("Should configure HSTS header")
    void shouldConfigureHstsHeader() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(header().exists("Strict-Transport-Security"))
                .andExpect(header().string("Strict-Transport-Security", 
                        org.hamcrest.Matchers.containsString("max-age=31536000")))
                .andExpect(header().string("Strict-Transport-Security", 
                        org.hamcrest.Matchers.containsString("includeSubDomains")));
    }
    
    @Test
    @DisplayName("Should use BCrypt password encoder")
    void shouldUseBCryptPasswordEncoder() {
        // Given
        String rawPassword = "TestPassword123!";
        
        // When
        String encodedPassword = passwordEncoder.encode(rawPassword);
        
        // Then
        assertThat(encodedPassword).isNotEqualTo(rawPassword);
        assertThat(encodedPassword).startsWith("$2a$"); // BCrypt prefix
        assertThat(passwordEncoder.matches(rawPassword, encodedPassword)).isTrue();
    }
    
    @Test
    @DisplayName("Should encode same password to different hashes")
    void shouldEncodeSamePasswordToDifferentHashes() {
        // Given
        String rawPassword = "TestPassword123!";
        
        // When
        String hash1 = passwordEncoder.encode(rawPassword);
        String hash2 = passwordEncoder.encode(rawPassword);
        
        // Then
        assertThat(hash1).isNotEqualTo(hash2);
        assertThat(passwordEncoder.matches(rawPassword, hash1)).isTrue();
        assertThat(passwordEncoder.matches(rawPassword, hash2)).isTrue();
    }
    
    @Test
    @DisplayName("Should reject wrong password")
    void shouldRejectWrongPassword() {
        // Given
        String rawPassword = "TestPassword123!";
        String wrongPassword = "WrongPassword123!";
        String encodedPassword = passwordEncoder.encode(rawPassword);
        
        // When/Then
        assertThat(passwordEncoder.matches(wrongPassword, encodedPassword)).isFalse();
    }
}
