package com.company.assetmanagement.controller;

import com.company.assetmanagement.dto.AssetStatsDTO;
import com.company.assetmanagement.service.AssetService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for AssetController stats endpoint.
 * Tests the GET /api/v1/assets/stats endpoint with mocked service layer.
 */
@WebMvcTest(AssetController.class)
@DisplayName("Asset Stats Controller Tests")
class AssetStatsControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @MockBean
    private AssetService assetService;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Test
    @DisplayName("Should return asset statistics for authenticated user")
    @WithMockUser(roles = {"ADMINISTRATOR"})
    void shouldReturnAssetStatisticsForAuthenticatedUser() throws Exception {
        // Given
        LocalDateTime timestamp = LocalDateTime.of(2024, 1, 15, 10, 30, 0);
        AssetStatsDTO mockStats = new AssetStatsDTO(150L, 120L, timestamp);
        when(assetService.getAssetStats()).thenReturn(mockStats);
        
        // When & Then
        mockMvc.perform(get("/api/v1/assets/stats")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.totalAssets").value(150))
                .andExpect(jsonPath("$.assetsInUse").value(120))
                .andExpect(jsonPath("$.lastUpdated").value("2024-01-15T10:30:00"))
                .andExpect(jsonPath("$.assetsAvailable").value(30))
                .andExpect(jsonPath("$.usagePercentage").value(80.0));
    }
    
    @Test
    @DisplayName("Should return asset statistics for asset manager")
    @WithMockUser(roles = {"ASSET_MANAGER"})
    void shouldReturnAssetStatisticsForAssetManager() throws Exception {
        // Given
        LocalDateTime timestamp = LocalDateTime.now();
        AssetStatsDTO mockStats = new AssetStatsDTO(100L, 75L, timestamp);
        when(assetService.getAssetStats()).thenReturn(mockStats);
        
        // When & Then
        mockMvc.perform(get("/api/v1/assets/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalAssets").value(100))
                .andExpect(jsonPath("$.assetsInUse").value(75));
    }
    
    @Test
    @DisplayName("Should return asset statistics for viewer")
    @WithMockUser(roles = {"VIEWER"})
    void shouldReturnAssetStatisticsForViewer() throws Exception {
        // Given
        LocalDateTime timestamp = LocalDateTime.now();
        AssetStatsDTO mockStats = new AssetStatsDTO(50L, 30L, timestamp);
        when(assetService.getAssetStats()).thenReturn(mockStats);
        
        // When & Then
        mockMvc.perform(get("/api/v1/assets/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalAssets").value(50))
                .andExpect(jsonPath("$.assetsInUse").value(30));
    }
    
    @Test
    @DisplayName("Should return 401 for unauthenticated request")
    void shouldReturn401ForUnauthenticatedRequest() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/v1/assets/stats"))
                .andExpect(status().isUnauthorized());
    }
    
    @Test
    @DisplayName("Should return 403 for user without required role")
    @WithMockUser(roles = {"UNKNOWN_ROLE"})
    void shouldReturn403ForUserWithoutRequiredRole() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/v1/assets/stats"))
                .andExpect(status().isForbidden());
    }
    
    @Test
    @DisplayName("Should handle zero assets correctly")
    @WithMockUser(roles = {"ADMINISTRATOR"})
    void shouldHandleZeroAssetsCorrectly() throws Exception {
        // Given
        LocalDateTime timestamp = LocalDateTime.now();
        AssetStatsDTO mockStats = new AssetStatsDTO(0L, 0L, timestamp);
        when(assetService.getAssetStats()).thenReturn(mockStats);
        
        // When & Then
        mockMvc.perform(get("/api/v1/assets/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalAssets").value(0))
                .andExpect(jsonPath("$.assetsInUse").value(0))
                .andExpect(jsonPath("$.assetsAvailable").value(0))
                .andExpect(jsonPath("$.usagePercentage").value(0.0));
    }
}