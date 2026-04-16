package com.company.assetmanagement.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for AssetStatsDTO.
 * Tests the data transfer object for asset statistics including
 * calculated fields and edge cases.
 */
@DisplayName("AssetStatsDTO Tests")
class AssetStatsDTOTest {
    
    @Test
    @DisplayName("Should create AssetStatsDTO with all fields")
    void shouldCreateAssetStatsDTOWithAllFields() {
        // Given
        Long totalAssets = 100L;
        Long assetsInUse = 75L;
        LocalDateTime lastUpdated = LocalDateTime.now();
        
        // When
        AssetStatsDTO stats = new AssetStatsDTO(totalAssets, assetsInUse, lastUpdated);
        
        // Then
        assertThat(stats.getTotalAssets()).isEqualTo(totalAssets);
        assertThat(stats.getAssetsInUse()).isEqualTo(assetsInUse);
        assertThat(stats.getLastUpdated()).isEqualTo(lastUpdated);
    }
    
    @Test
    @DisplayName("Should calculate available assets correctly")
    void shouldCalculateAvailableAssetsCorrectly() {
        // Given
        AssetStatsDTO stats = new AssetStatsDTO(100L, 75L, LocalDateTime.now());
        
        // When
        Long availableAssets = stats.getAssetsAvailable();
        
        // Then
        assertThat(availableAssets).isEqualTo(25L);
    }
    
    @Test
    @DisplayName("Should calculate usage percentage correctly")
    void shouldCalculateUsagePercentageCorrectly() {
        // Given
        AssetStatsDTO stats = new AssetStatsDTO(100L, 75L, LocalDateTime.now());
        
        // When
        Double usagePercentage = stats.getUsagePercentage();
        
        // Then
        assertThat(usagePercentage).isEqualTo(75.0);
    }
    
    @Test
    @DisplayName("Should handle zero total assets for percentage calculation")
    void shouldHandleZeroTotalAssetsForPercentageCalculation() {
        // Given
        AssetStatsDTO stats = new AssetStatsDTO(0L, 0L, LocalDateTime.now());
        
        // When
        Double usagePercentage = stats.getUsagePercentage();
        
        // Then
        assertThat(usagePercentage).isEqualTo(0.0);
    }
    
    @Test
    @DisplayName("Should handle null values for available assets calculation")
    void shouldHandleNullValuesForAvailableAssetsCalculation() {
        // Given
        AssetStatsDTO stats = new AssetStatsDTO(null, null, LocalDateTime.now());
        
        // When
        Long availableAssets = stats.getAssetsAvailable();
        
        // Then
        assertThat(availableAssets).isEqualTo(0L);
    }
    
    @Test
    @DisplayName("Should handle null values for usage percentage calculation")
    void shouldHandleNullValuesForUsagePercentageCalculation() {
        // Given
        AssetStatsDTO stats = new AssetStatsDTO(null, null, LocalDateTime.now());
        
        // When
        Double usagePercentage = stats.getUsagePercentage();
        
        // Then
        assertThat(usagePercentage).isEqualTo(0.0);
    }
    
    @Test
    @DisplayName("Should round usage percentage to two decimal places")
    void shouldRoundUsagePercentageToTwoDecimalPlaces() {
        // Given - 33 out of 99 = 33.333...%
        AssetStatsDTO stats = new AssetStatsDTO(99L, 33L, LocalDateTime.now());
        
        // When
        Double usagePercentage = stats.getUsagePercentage();
        
        // Then
        assertThat(usagePercentage).isEqualTo(33.33);
    }
    
    @Test
    @DisplayName("Should implement equals and hashCode correctly")
    void shouldImplementEqualsAndHashCodeCorrectly() {
        // Given
        LocalDateTime timestamp = LocalDateTime.now();
        AssetStatsDTO stats1 = new AssetStatsDTO(100L, 75L, timestamp);
        AssetStatsDTO stats2 = new AssetStatsDTO(100L, 75L, timestamp);
        AssetStatsDTO stats3 = new AssetStatsDTO(200L, 150L, timestamp);
        
        // Then
        assertThat(stats1).isEqualTo(stats2);
        assertThat(stats1).isNotEqualTo(stats3);
        assertThat(stats1.hashCode()).isEqualTo(stats2.hashCode());
        assertThat(stats1.hashCode()).isNotEqualTo(stats3.hashCode());
    }
    
    @Test
    @DisplayName("Should implement toString correctly")
    void shouldImplementToStringCorrectly() {
        // Given
        LocalDateTime timestamp = LocalDateTime.of(2024, 1, 15, 10, 30, 0);
        AssetStatsDTO stats = new AssetStatsDTO(100L, 75L, timestamp);
        
        // When
        String toString = stats.toString();
        
        // Then
        assertThat(toString).contains("AssetStatsDTO");
        assertThat(toString).contains("totalAssets=100");
        assertThat(toString).contains("assetsInUse=75");
        assertThat(toString).contains("lastUpdated=2024-01-15T10:30");
    }
}