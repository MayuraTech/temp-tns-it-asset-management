package com.company.assetmanagement.dto;

import com.company.assetmanagement.model.AssetType;
import com.company.assetmanagement.model.LifecycleStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Data Transfer Object for Asset responses.
 * Used to transfer asset data from backend to frontend.
 * Includes all asset fields for complete representation.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Asset data transfer object containing complete asset information")
public class AssetDTO {
    
    @Schema(description = "Unique identifier of the asset (UUID format)", example = "550e8400-e29b-41d4-a716-446655440000", accessMode = Schema.AccessMode.READ_ONLY)
    private String id;
    
    @Schema(description = "Type of the asset", example = "SERVER", required = true)
    private AssetType assetType;
    
    @Schema(description = "Name of the asset", example = "Production Server 01", required = true, maxLength = 255)
    private String name;
    
    @Schema(description = "Unique serial number of the asset (immutable after creation)", example = "SRV-PROD-001", required = true, minLength = 5, maxLength = 100, accessMode = Schema.AccessMode.READ_ONLY)
    private String serialNumber;
    
    @Schema(description = "Date when the asset was acquired (ISO 8601 format)", example = "2024-01-15", required = true)
    private LocalDate acquisitionDate;
    
    @Schema(description = "Current lifecycle status of the asset", example = "IN_USE", required = true)
    private LifecycleStatus status;
    
    @Schema(description = "Physical location of the asset", example = "Data Center A, Rack 12", maxLength = 255)
    private String location;
    
    @Schema(description = "Name of the user to whom the asset is assigned", example = "John Doe", maxLength = 255)
    private String assignedUser;
    
    @Schema(description = "Email address of the assigned user", example = "john.doe@company.com", maxLength = 255)
    private String assignedUserEmail;
    
    @Schema(description = "Date and time when the asset was assigned to the current user", example = "2024-01-15T10:30:00")
    private LocalDateTime assignmentDate;
    
    @Schema(description = "Date and time when the asset location was last updated", example = "2024-01-15T10:30:00")
    private LocalDateTime locationUpdateDate;
    
    @Schema(description = "Additional notes or comments about the asset", example = "Primary application server for production environment")
    private String notes;
    
    @Schema(description = "Custom fields in JSON format for extensibility", example = "{\"warranty_expiry\": \"2026-01-15\", \"vendor\": \"Dell\"}")
    private String customFields;
    
    @Schema(description = "URL of the asset image", example = "https://cdn.example.com/assets/images/server-001.jpg", maxLength = 500)
    private String imageUrl;
    
    @Schema(description = "Original filename of the uploaded image", example = "server-photo.jpg", maxLength = 255)
    private String imageFilename;
    
    @Schema(description = "Size of the image file in bytes", example = "2048576")
    private Long imageSize;
    
    @Schema(description = "MIME type of the image", example = "image/jpeg", maxLength = 50)
    private String imageContentType;
    
    @Schema(description = "Timestamp when the asset was created", example = "2024-01-15T10:30:00", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime createdAt;
    
    @Schema(description = "User ID who created the asset", example = "admin", accessMode = Schema.AccessMode.READ_ONLY)
    private String createdBy;
    
    @Schema(description = "Timestamp when the asset was last updated", example = "2024-01-15T14:45:00", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime updatedAt;
    
    @Schema(description = "User ID who last updated the asset", example = "admin", accessMode = Schema.AccessMode.READ_ONLY)
    private String updatedBy;
    
    @Schema(description = "Indicates if the asset is read-only (e.g., retired assets)", example = "false", accessMode = Schema.AccessMode.READ_ONLY)
    private boolean readOnly;
    
    public AssetDTO() {
    }
    
    // Getters and setters
    
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
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
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public String getCreatedBy() {
        return createdBy;
    }
    
    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    public String getUpdatedBy() {
        return updatedBy;
    }
    
    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }
    
    public boolean isReadOnly() {
        return readOnly;
    }
    
    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
    }
    
    public String getImageUrl() {
        return imageUrl;
    }
    
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
    
    public String getImageFilename() {
        return imageFilename;
    }
    
    public void setImageFilename(String imageFilename) {
        this.imageFilename = imageFilename;
    }
    
    public Long getImageSize() {
        return imageSize;
    }
    
    public void setImageSize(Long imageSize) {
        this.imageSize = imageSize;
    }
    
    public String getImageContentType() {
        return imageContentType;
    }
    
    public void setImageContentType(String imageContentType) {
        this.imageContentType = imageContentType;
    }
    
    /**
     * Builder for creating AssetDTO instances.
     */
    public static class Builder {
        private final AssetDTO dto;
        
        public Builder() {
            this.dto = new AssetDTO();
        }
        
        public Builder id(String id) {
            dto.id = id;
            return this;
        }
        
        public Builder assetType(AssetType assetType) {
            dto.assetType = assetType;
            return this;
        }
        
        public Builder name(String name) {
            dto.name = name;
            return this;
        }
        
        public Builder serialNumber(String serialNumber) {
            dto.serialNumber = serialNumber;
            return this;
        }
        
        public Builder acquisitionDate(LocalDate acquisitionDate) {
            dto.acquisitionDate = acquisitionDate;
            return this;
        }
        
        public Builder status(LifecycleStatus status) {
            dto.status = status;
            return this;
        }
        
        public Builder location(String location) {
            dto.location = location;
            return this;
        }
        
        public Builder assignedUser(String assignedUser) {
            dto.assignedUser = assignedUser;
            return this;
        }
        
        public Builder assignedUserEmail(String assignedUserEmail) {
            dto.assignedUserEmail = assignedUserEmail;
            return this;
        }
        
        public Builder assignmentDate(LocalDateTime assignmentDate) {
            dto.assignmentDate = assignmentDate;
            return this;
        }
        
        public Builder locationUpdateDate(LocalDateTime locationUpdateDate) {
            dto.locationUpdateDate = locationUpdateDate;
            return this;
        }
        
        public Builder notes(String notes) {
            dto.notes = notes;
            return this;
        }
        
        public Builder customFields(String customFields) {
            dto.customFields = customFields;
            return this;
        }
        
        public Builder imageUrl(String imageUrl) {
            dto.imageUrl = imageUrl;
            return this;
        }
        
        public Builder imageFilename(String imageFilename) {
            dto.imageFilename = imageFilename;
            return this;
        }
        
        public Builder imageSize(Long imageSize) {
            dto.imageSize = imageSize;
            return this;
        }
        
        public Builder imageContentType(String imageContentType) {
            dto.imageContentType = imageContentType;
            return this;
        }
        
        public Builder createdAt(LocalDateTime createdAt) {
            dto.createdAt = createdAt;
            return this;
        }
        
        public Builder createdBy(String createdBy) {
            dto.createdBy = createdBy;
            return this;
        }
        
        public Builder updatedAt(LocalDateTime updatedAt) {
            dto.updatedAt = updatedAt;
            return this;
        }
        
        public Builder updatedBy(String updatedBy) {
            dto.updatedBy = updatedBy;
            return this;
        }
        
        public Builder readOnly(boolean readOnly) {
            dto.readOnly = readOnly;
            return this;
        }
        
        public AssetDTO build() {
            return dto;
        }
    }
    
    public static Builder builder() {
        return new Builder();
    }
}
