package com.company.assetmanagement.dto;

import com.company.assetmanagement.model.AssetType;
import com.company.assetmanagement.model.LifecycleStatus;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDate;
import java.util.List;

/**
 * Data Transfer Object for asset search query parameters.
 * Supports filtering by multiple criteria for advanced search functionality.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AssetSearchQuery {
    
    private String text;
    private List<AssetType> assetTypes;
    private List<LifecycleStatus> statuses;
    private String location;
    private LocalDate acquisitionDateFrom;
    private LocalDate acquisitionDateTo;
    private String assignedUser;
    
    public AssetSearchQuery() {
    }
    
    // Getters and setters
    
    public String getText() {
        return text;
    }
    
    public void setText(String text) {
        this.text = text;
    }
    
    public List<AssetType> getAssetTypes() {
        return assetTypes;
    }
    
    public void setAssetTypes(List<AssetType> assetTypes) {
        this.assetTypes = assetTypes;
    }
    
    public List<LifecycleStatus> getStatuses() {
        return statuses;
    }
    
    public void setStatuses(List<LifecycleStatus> statuses) {
        this.statuses = statuses;
    }
    
    public String getLocation() {
        return location;
    }
    
    public void setLocation(String location) {
        this.location = location;
    }
    
    public LocalDate getAcquisitionDateFrom() {
        return acquisitionDateFrom;
    }
    
    public void setAcquisitionDateFrom(LocalDate acquisitionDateFrom) {
        this.acquisitionDateFrom = acquisitionDateFrom;
    }
    
    public LocalDate getAcquisitionDateTo() {
        return acquisitionDateTo;
    }
    
    public void setAcquisitionDateTo(LocalDate acquisitionDateTo) {
        this.acquisitionDateTo = acquisitionDateTo;
    }
    
    public String getAssignedUser() {
        return assignedUser;
    }
    
    public void setAssignedUser(String assignedUser) {
        this.assignedUser = assignedUser;
    }
    
    /**
     * Builder for creating AssetSearchQuery instances.
     */
    public static class Builder {
        private final AssetSearchQuery query;
        
        public Builder() {
            this.query = new AssetSearchQuery();
        }
        
        public Builder text(String text) {
            query.text = text;
            return this;
        }
        
        public Builder assetTypes(List<AssetType> assetTypes) {
            query.assetTypes = assetTypes;
            return this;
        }
        
        public Builder statuses(List<LifecycleStatus> statuses) {
            query.statuses = statuses;
            return this;
        }
        
        public Builder location(String location) {
            query.location = location;
            return this;
        }
        
        public Builder acquisitionDateFrom(LocalDate acquisitionDateFrom) {
            query.acquisitionDateFrom = acquisitionDateFrom;
            return this;
        }
        
        public Builder acquisitionDateTo(LocalDate acquisitionDateTo) {
            query.acquisitionDateTo = acquisitionDateTo;
            return this;
        }
        
        public Builder assignedUser(String assignedUser) {
            query.assignedUser = assignedUser;
            return this;
        }
        
        public AssetSearchQuery build() {
            return query;
        }
    }
    
    public static Builder builder() {
        return new Builder();
    }
}
