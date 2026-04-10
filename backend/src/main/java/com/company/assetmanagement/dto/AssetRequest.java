package com.company.assetmanagement.dto;

import com.company.assetmanagement.model.AssetType;
import com.company.assetmanagement.model.LifecycleStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Data Transfer Object for asset create/update requests.
 * Includes validation annotations to ensure data integrity.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Request object for creating or updating an asset")
public class AssetRequest {
    
    @NotNull(message = "Asset type is required")
    @Schema(description = "Type of the asset", example = "SERVER", required = true)
    private AssetType assetType;
    
    @NotBlank(message = "Name is required")
    @Size(min = 1, max = 255, message = "Name must be between 1 and 255 characters")
    @Schema(description = "Name of the asset", example = "Production Server 01", required = true, minLength = 1, maxLength = 255)
    private String name;
    
    @NotBlank(message = "Serial number is required")
    @Size(min = 5, max = 100, message = "Serial number must be between 5 and 100 characters")
    @Schema(description = "Unique serial number of the asset (must be unique across all assets)", example = "SRV-PROD-001", required = true, minLength = 5, maxLength = 100)
    private String serialNumber;
    
    @NotNull(message = "Acquisition date is required")
    @PastOrPresent(message = "Acquisition date cannot be in the future")
    @Schema(description = "Date when the asset was acquired (cannot be in the future, ISO 8601 format)", example = "2024-01-15", required = true)
    private LocalDate acquisitionDate;
    
    @NotNull(message = "Status is required")
    @Schema(description = "Initial lifecycle status of the asset", example = "ORDERED", required = true)
    private LifecycleStatus status;
    
    @Size(max = 255, message = "Location must not exceed 255 characters")
    @Schema(description = "Physical location of the asset", example = "Data Center A, Rack 12", maxLength = 255)
    private String location;
    
    @Size(max = 255, message = "Assigned user must not exceed 255 characters")
    @Schema(description = "Name of the user to whom the asset is assigned", example = "John Doe", maxLength = 255)
    private String assignedUser;
    
    @Email(message = "Invalid email format")
    @Size(max = 255, message = "Assigned user email must not exceed 255 characters")
    @Schema(description = "Email address of the assigned user (must be valid email format)", example = "john.doe@company.com", maxLength = 255)
    private String assignedUserEmail;
    
    @Schema(description = "Date and time when the asset was assigned to the current user", example = "2024-01-15T10:30:00")
    private LocalDateTime assignmentDate;
    
    @Schema(description = "Date and time when the asset location was last updated", example = "2024-01-15T10:30:00")
    private LocalDateTime locationUpdateDate;
    
    @Schema(description = "Additional notes or comments about the asset", example = "Primary application server for production environment")
    private String notes;
    
    @Schema(description = "Custom fields in JSON format for extensibility", example = "{\"warranty_expiry\": \"2026-01-15\", \"vendor\": \"Dell\"}")
    private String customFields;
    
    public AssetRequest() {
    }
    
    // Getters and setters
    
    public AssetType getAssetType() {
        return assetType;
    }
    
    public void setAssetType(AssetType assetType) {
        this.assetType = assetType;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getSerialNumber() {
        return serialNumber;
    }
    
    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }
    
    public LocalDate getAcquisitionDate() {
        return acquisitionDate;
    }
    
    public void setAcquisitionDate(LocalDate acquisitionDate) {
        this.acquisitionDate = acquisitionDate;
    }
    
    public LifecycleStatus getStatus() {
        return status;
    }
    
    public void setStatus(LifecycleStatus status) {
        this.status = status;
    }
    
    public String getLocation() {
        return location;
    }
    
    public void setLocation(String location) {
        this.location = location;
    }
    
    public String getAssignedUser() {
        return assignedUser;
    }
    
    public void setAssignedUser(String assignedUser) {
        this.assignedUser = assignedUser;
    }
    
    public String getAssignedUserEmail() {
        return assignedUserEmail;
    }
    
    public void setAssignedUserEmail(String assignedUserEmail) {
        this.assignedUserEmail = assignedUserEmail;
    }
    
    public LocalDateTime getAssignmentDate() {
        return assignmentDate;
    }
    
    public void setAssignmentDate(LocalDateTime assignmentDate) {
        this.assignmentDate = assignmentDate;
    }
    
    public LocalDateTime getLocationUpdateDate() {
        return locationUpdateDate;
    }
    
    public void setLocationUpdateDate(LocalDateTime locationUpdateDate) {
        this.locationUpdateDate = locationUpdateDate;
    }
    
    public String getNotes() {
        return notes;
    }
    
    public void setNotes(String notes) {
        this.notes = notes;
    }
    
    public String getCustomFields() {
        return customFields;
    }
    
    public void setCustomFields(String customFields) {
        this.customFields = customFields;
    }
    
    /**
     * Builder for creating AssetRequest instances.
     */
    public static class Builder {
        private final AssetRequest request;
        
        public Builder() {
            this.request = new AssetRequest();
        }
        
        public Builder assetType(AssetType assetType) {
            request.assetType = assetType;
            return this;
        }
        
        public Builder name(String name) {
            request.name = name;
            return this;
        }
        
        public Builder serialNumber(String serialNumber) {
            request.serialNumber = serialNumber;
            return this;
        }
        
        public Builder acquisitionDate(LocalDate acquisitionDate) {
            request.acquisitionDate = acquisitionDate;
            return this;
        }
        
        public Builder status(LifecycleStatus status) {
            request.status = status;
            return this;
        }
        
        public Builder location(String location) {
            request.location = location;
            return this;
        }
        
        public Builder assignedUser(String assignedUser) {
            request.assignedUser = assignedUser;
            return this;
        }
        
        public Builder assignedUserEmail(String assignedUserEmail) {
            request.assignedUserEmail = assignedUserEmail;
            return this;
        }
        
        public Builder assignmentDate(LocalDateTime assignmentDate) {
            request.assignmentDate = assignmentDate;
            return this;
        }
        
        public Builder locationUpdateDate(LocalDateTime locationUpdateDate) {
            request.locationUpdateDate = locationUpdateDate;
            return this;
        }
        
        public Builder notes(String notes) {
            request.notes = notes;
            return this;
        }
        
        public Builder customFields(String customFields) {
            request.customFields = customFields;
            return this;
        }
        
        public AssetRequest build() {
            return request;
        }
    }
    
    public static Builder builder() {
        return new Builder();
    }
}
