package com.company.assetmanagement.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entity representing an IT asset.
 * 
 * Placeholder entity for allocation management implementation.
 * Full implementation will be provided by Asset Management module.
 */
@Entity
@Table(name = "Assets")
public class Asset {
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;
    
    @Column(nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private AssetType assetType;
    
    @Column(nullable = false, length = 255)
    private String name;
    
    @Column(nullable = false, unique = true, length = 100)
    private String serialNumber;
    
    @Column(nullable = false)
    private LocalDate acquisitionDate;
    
    @Column(nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private LifecycleStatus status;
    
    @Column(length = 255)
    private String location;
    
    @Column(length = 255)
    private String assignedUser;
    
    @Column(length = 255)
    private String assignedUserEmail;
    
    @Column
    private LocalDateTime assignmentDate;
    
    @Column
    private LocalDateTime locationUpdateDate;
    
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(nullable = false)
    private LocalDateTime updatedAt;
    
    // Constructors
    
    public Asset() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
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
}
