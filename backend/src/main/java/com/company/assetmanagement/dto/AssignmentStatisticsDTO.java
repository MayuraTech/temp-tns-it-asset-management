package com.company.assetmanagement.dto;

import java.util.List;
import java.util.Map;

/**
 * DTO for assignment statistics.
 * 
 * Contains aggregated statistics about asset assignments including:
 * - Total assigned assets
 * - Assignments by type (USER vs LOCATION)
 * - Available assets by status
 * - Top users and locations by assignment count
 */
public class AssignmentStatisticsDTO {
    
    private long totalAssignedAssets;
    private long userAssignments;
    private long locationAssignments;
    private Map<String, Long> availableAssetsByStatus;
    private List<AssignmentCountDTO> topUsersByAssignments;
    private List<AssignmentCountDTO> topLocationsByAssignments;
    
    // Constructors
    
    public AssignmentStatisticsDTO() {
    }
    
    // Getters and Setters
    
    public long getTotalAssignedAssets() {
        return totalAssignedAssets;
    }
    
    public void setTotalAssignedAssets(long totalAssignedAssets) {
        this.totalAssignedAssets = totalAssignedAssets;
    }
    
    public long getUserAssignments() {
        return userAssignments;
    }
    
    public void setUserAssignments(long userAssignments) {
        this.userAssignments = userAssignments;
    }
    
    public long getLocationAssignments() {
        return locationAssignments;
    }
    
    public void setLocationAssignments(long locationAssignments) {
        this.locationAssignments = locationAssignments;
    }
    
    public Map<String, Long> getAvailableAssetsByStatus() {
        return availableAssetsByStatus;
    }
    
    public void setAvailableAssetsByStatus(Map<String, Long> availableAssetsByStatus) {
        this.availableAssetsByStatus = availableAssetsByStatus;
    }
    
    public List<AssignmentCountDTO> getTopUsersByAssignments() {
        return topUsersByAssignments;
    }
    
    public void setTopUsersByAssignments(List<AssignmentCountDTO> topUsersByAssignments) {
        this.topUsersByAssignments = topUsersByAssignments;
    }
    
    public List<AssignmentCountDTO> getTopLocationsByAssignments() {
        return topLocationsByAssignments;
    }
    
    public void setTopLocationsByAssignments(List<AssignmentCountDTO> topLocationsByAssignments) {
        this.topLocationsByAssignments = topLocationsByAssignments;
    }
    
    /**
     * Inner DTO for assignment count by user or location.
     */
    public static class AssignmentCountDTO {
        private String name;
        private long count;
        
        public AssignmentCountDTO() {
        }
        
        public AssignmentCountDTO(String name, long count) {
            this.name = name;
            this.count = count;
        }
        
        public String getName() {
            return name;
        }
        
        public void setName(String name) {
            this.name = name;
        }
        
        public long getCount() {
            return count;
        }
        
        public void setCount(long count) {
            this.count = count;
        }
        
        @Override
        public String toString() {
            return "AssignmentCountDTO{" +
                    "name='" + name + '\'' +
                    ", count=" + count +
                    '}';
        }
    }
    
    @Override
    public String toString() {
        return "AssignmentStatisticsDTO{" +
                "totalAssignedAssets=" + totalAssignedAssets +
                ", userAssignments=" + userAssignments +
                ", locationAssignments=" + locationAssignments +
                ", availableAssetsByStatus=" + availableAssetsByStatus +
                ", topUsersByAssignments=" + topUsersByAssignments +
                ", topLocationsByAssignments=" + topLocationsByAssignments +
                '}';
    }
}
