package com.company.assetmanagement.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Asset entity representing IT infrastructure assets in the system.
 * Supports 15 asset types and 7 lifecycle statuses with full audit trail.
 * 
 * This entity maps to the Assets table in the database and includes:
 * - JPA annotations for persistence
 * - Validation annotations for data integrity
 * - Audit annotations for tracking changes
 * - Table indexes for performance optimization
 * 
 * @see AssetType
 * @see LifecycleStatus
 */
@Entity
@Table(name = "Assets", indexes = {
    @Index(name = "IX_Assets_SerialNumber", columnList = "serialNumber"),
    @Index(name = "IX_Assets_AssetType", columnList = "assetType"),
    @Index(name = "IX_Assets_Status", columnList = "status"),
    @Index(name = "IX_Assets_Location", columnList = "location"),
    @Index(name = "IX_Assets_AssignedUser", columnList = "assignedUser"),
    @Index(name = "IX_Assets_AcquisitionDate", columnList = "acquisitionDate"),
    @Index(name = "IX_Assets_CreatedBy", columnList = "createdBy")
})
@EntityListeners(AuditingEntityListener.class)
public class Asset {
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "Id", updatable = false, nullable = false)
    private UUID id;
    
    @NotNull(message = "Asset type is required")
    @Column(name = "AssetType", nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private AssetType assetType;
    
    @NotBlank(message = "Name is required")
    @Size(min = 1, max = 255, message = "Name must be between 1 and 255 characters")
    @Column(name = "Name", nullable = false, length = 255)
    private String name;
    
    @NotBlank(message = "Serial number is required")
    @Size(min = 5, max = 100, message = "Serial number must be between 5 and 100 characters")
    @Column(name = "SerialNumber", nullable = false, unique = true, length = 100, updatable = false)
    private String serialNumber;
    
    @NotNull(message = "Acquisition date is required")
    @PastOrPresent(message = "Acquisition date cannot be in the future")
    @Column(name = "AcquisitionDate", nullable = false)
    private LocalDate acquisitionDate;
    
    @NotNull(message = "Status is required")
    @Column(name = "Status", nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private LifecycleStatus status;
    
    @Size(max = 255, message = "Location must not exceed 255 characters")
    @Column(name = "Location", length = 255)
    private String location;
    
    @Size(max = 255, message = "Assigned user must not exceed 255 characters")
    @Column(name = "AssignedUser", length = 255)
    private String assignedUser;
    
    @Email(message = "Invalid email format")
    @Size(max = 255, message = "Assigned user email must not exceed 255 characters")
    @Column(name = "AssignedUserEmail", length = 255)
    private String assignedUserEmail;
    
    @Column(name = "AssignmentDate")
    private LocalDateTime assignmentDate;
    
    @Column(name = "LocationUpdateDate")
    private LocalDateTime locationUpdateDate;
    
    @Column(name = "Notes", columnDefinition = "NVARCHAR(MAX)")
    private String notes;
    
    @Column(name = "CustomFields", columnDefinition = "NVARCHAR(MAX)")
    private String customFields; // JSON string
    
    @Size(max = 500, message = "Image URL must not exceed 500 characters")
    @Column(name = "ImageUrl", length = 500)
    private String imageUrl;
    
    @Size(max = 255, message = "Image filename must not exceed 255 characters")
    @Column(name = "ImageFilename", length = 255)
    private String imageFilename;
    
    @Column(name = "ImageSize")
    private Long imageSize;
    
    @Size(max = 50, message = "Image content type must not exceed 50 characters")
    @Column(name = "ImageContentType", length = 50)
    private String imageContentType;
    
    @CreatedDate
    @Column(name = "CreatedAt", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @NotNull(message = "Created by user is required")
    @Column(name = "CreatedBy", nullable = false, updatable = false)
    private UUID createdBy;
    
    @LastModifiedDate
    @Column(name = "UpdatedAt", nullable = false)
    private LocalDateTime updatedAt;
    
    @NotNull(message = "Updated by user is required")
    @Column(name = "UpdatedBy", nullable = false)
    private UUID updatedBy;
    
    @Column(name = "ReadOnly", nullable = false)
    private boolean readOnly = false;
    
    /**
     * Default constructor required by JPA.
     */
    public Asset() {
    }
    
    /**
     * Constructor with required fields for asset creation.
     * 
     * @param assetType the type of asset
     * @param name the asset name
     * @param serialNumber the unique serial number
     * @param acquisitionDate the date the asset was acquired
     * @param status the initial lifecycle status
     */
    public Asset(AssetType assetType, String name, String serialNumber, 
                 LocalDate acquisitionDate, LifecycleStatus status) {
        this.assetType = assetType;
        this.name = name;
        this.serialNumber = serialNumber;
        this.acquisitionDate = acquisitionDate;
        this.status = status;
    }
    
    // Getters and Setters
    
    public UUID getId() {
        return id;
    }
    
    public void setId(UUID id) {
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
    
    public UUID getCreatedBy() {
        return createdBy;
    }
    
    public void setCreatedBy(UUID createdBy) {
        this.createdBy = createdBy;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    public UUID getUpdatedBy() {
        return updatedBy;
    }
    
    public void setUpdatedBy(UUID updatedBy) {
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
     * Checks if the asset is currently assigned to a user.
     * 
     * @return true if the asset has an assigned user, false otherwise
     */
    public boolean isAssigned() {
        return assignedUser != null && !assignedUser.isBlank();
    }
    
    /**
     * Checks if the asset is retired.
     * 
     * @return true if the asset status is RETIRED, false otherwise
     */
    public boolean isRetired() {
        return status == LifecycleStatus.RETIRED;
    }
    
    /**
     * Checks if the asset can be modified.
     * Retired assets are read-only except for notes field.
     * 
     * @return true if the asset can be modified, false otherwise
     */
    public boolean canBeModified() {
        return !readOnly;
    }
    
    /**
     * Equals method based on serial number (business key).
     * Two assets are considered equal if they have the same serial number.
     * 
     * @param o the object to compare
     * @return true if the objects are equal, false otherwise
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Asset asset = (Asset) o;
        return Objects.equals(serialNumber, asset.serialNumber);
    }
    
    /**
     * Hash code based on serial number (business key).
     * 
     * @return the hash code
     */
    @Override
    public int hashCode() {
        return Objects.hash(serialNumber);
    }
    
    /**
     * String representation of the asset for logging and debugging.
     * 
     * @return string representation of the asset
     */
    @Override
    public String toString() {
        return "Asset{" +
                "id=" + id +
                ", assetType=" + assetType +
                ", name='" + name + '\'' +
                ", serialNumber='" + serialNumber + '\'' +
                ", acquisitionDate=" + acquisitionDate +
                ", status=" + status +
                ", location='" + location + '\'' +
                ", assignedUser='" + assignedUser + '\'' +
                ", assignedUserEmail='" + assignedUserEmail + '\'' +
                ", assignmentDate=" + assignmentDate +
                ", locationUpdateDate=" + locationUpdateDate +
                ", createdAt=" + createdAt +
                ", createdBy=" + createdBy +
                ", updatedAt=" + updatedAt +
                ", updatedBy=" + updatedBy +
                ", readOnly=" + readOnly +
                '}';
    }
}
