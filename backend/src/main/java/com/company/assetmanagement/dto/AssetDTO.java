package com.company.assetmanagement.dto;

import com.company.assetmanagement.model.AssetType;
import com.company.assetmanagement.model.LifecycleStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO for asset data.
 * 
 * Represents an asset with all relevant information.
 * Used for API responses when querying assets.
 */
public class AssetDTO {
    
    private UUID id;
    private AssetType assetType;
    private String name;
    private String serialNumber;
    private LocalDate acquisitionDate;
    private LifecycleStatus status;
    private String location;
    private String assignedUser;
    private String assignedUserEmail;
    private LocalDateTime assignmentDate;
    private LocalDateTime locationUpdateDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Constructors
    
    public AssetDTO() {
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
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    @Override
    public String toString() {
        return "AssetDTO{" +
                "id=" + id +
                ", assetType=" + assetType +
                ", name='" + name + '\'' +
                ", serialNumber='" + serialNumber + '\'' +
                ", status=" + status +
                ", location='" + location + '\'' +
                ", assignedUser='" + assignedUser + '\'' +
                '}';
    }
}
