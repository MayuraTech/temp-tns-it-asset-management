package com.company.assetmanagement.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

/**
 * Data Transfer Object for asset statistics.
 * 
 * <p>Contains quick statistics about the asset inventory for dashboard display:
 * <ul>
 *   <li>Total number of assets in the system</li>
 *   <li>Number of assets currently in use</li>
 *   <li>Timestamp when statistics were calculated</li>
 * </ul>
 * 
 * <p>This DTO is used by the dashboard stats widget to display real-time
 * asset inventory metrics. Statistics are calculated using efficient database
 * aggregation queries and may be cached for performance.
 * 
 * <p><strong>Requirements:</strong>
 * <ul>
 *   <li>Requirement 22: Dashboard and Quick Stats</li>
 * </ul>
 * 
 * @author Module 2 Team
 * @version 1.0
 * @see com.company.assetmanagement.service.AssetService#getAssetStats()
 */
@Schema(description = "Asset statistics for dashboard display")
public class AssetStatsDTO {
    
    @Schema(
        description = "Total number of assets in the system",
        example = "1250",
        minimum = "0"
    )
    private Long totalAssets;
    
    @Schema(
        description = "Number of assets currently in use (status = IN_USE)",
        example = "987",
        minimum = "0"
    )
    private Long assetsInUse;
    
    @Schema(
        description = "Timestamp when statistics were calculated",
        example = "2024-01-15T10:30:00"
    )
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime lastUpdated;
    
    /**
     * Default constructor for JSON deserialization.
     */
    public AssetStatsDTO() {
    }
    
    /**
     * Constructor with all fields.
     * 
     * @param totalAssets total number of assets in the system
     * @param assetsInUse number of assets currently in use
     * @param lastUpdated timestamp when statistics were calculated
     */
    public AssetStatsDTO(Long totalAssets, Long assetsInUse, LocalDateTime lastUpdated) {
        this.totalAssets = totalAssets;
        this.assetsInUse = assetsInUse;
        this.lastUpdated = lastUpdated;
    }
    
    /**
     * Gets the total number of assets in the system.
     * 
     * @return total asset count
     */
    public Long getTotalAssets() {
        return totalAssets;
    }
    
    /**
     * Sets the total number of assets in the system.
     * 
     * @param totalAssets total asset count (must not be negative)
     */
    public void setTotalAssets(Long totalAssets) {
        this.totalAssets = totalAssets;
    }
    
    /**
     * Gets the number of assets currently in use.
     * 
     * @return assets in use count
     */
    public Long getAssetsInUse() {
        return assetsInUse;
    }
    
    /**
     * Sets the number of assets currently in use.
     * 
     * @param assetsInUse assets in use count (must not be negative)
     */
    public void setAssetsInUse(Long assetsInUse) {
        this.assetsInUse = assetsInUse;
    }
    
    /**
     * Gets the timestamp when statistics were calculated.
     * 
     * @return calculation timestamp
     */
    public LocalDateTime getLastUpdated() {
        return lastUpdated;
    }
    
    /**
     * Sets the timestamp when statistics were calculated.
     * 
     * @param lastUpdated calculation timestamp
     */
    public void setLastUpdated(LocalDateTime lastUpdated) {
        this.lastUpdated = lastUpdated;
    }
    
    /**
     * Calculates the number of available assets (total - in use).
     * 
     * @return number of available assets
     */
    @Schema(
        description = "Number of available assets (calculated: totalAssets - assetsInUse)",
        example = "263"
    )
    public Long getAssetsAvailable() {
        if (totalAssets == null || assetsInUse == null) {
            return 0L;
        }
        return totalAssets - assetsInUse;
    }
    
    /**
     * Calculates the percentage of assets currently in use.
     * 
     * @return usage percentage (0-100)
     */
    @Schema(
        description = "Percentage of assets in use (calculated: assetsInUse / totalAssets * 100)",
        example = "78.96"
    )
    public Double getUsagePercentage() {
        if (totalAssets == null || assetsInUse == null || totalAssets == 0) {
            return 0.0;
        }
        return Math.round((assetsInUse.doubleValue() / totalAssets.doubleValue()) * 100.0 * 100.0) / 100.0;
    }
    
    @Override
    public String toString() {
        return "AssetStatsDTO{" +
                "totalAssets=" + totalAssets +
                ", assetsInUse=" + assetsInUse +
                ", lastUpdated=" + lastUpdated +
                '}';
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        
        AssetStatsDTO that = (AssetStatsDTO) o;
        
        if (totalAssets != null ? !totalAssets.equals(that.totalAssets) : that.totalAssets != null) return false;
        if (assetsInUse != null ? !assetsInUse.equals(that.assetsInUse) : that.assetsInUse != null) return false;
        return lastUpdated != null ? lastUpdated.equals(that.lastUpdated) : that.lastUpdated == null;
    }
    
    @Override
    public int hashCode() {
        int result = totalAssets != null ? totalAssets.hashCode() : 0;
        result = 31 * result + (assetsInUse != null ? assetsInUse.hashCode() : 0);
        result = 31 * result + (lastUpdated != null ? lastUpdated.hashCode() : 0);
        return result;
    }
}