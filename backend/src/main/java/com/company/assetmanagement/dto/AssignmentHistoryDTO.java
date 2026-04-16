package com.company.assetmanagement.dto;

import com.company.assetmanagement.model.AssignmentType;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO for assignment history data.
 * 
 * Represents a historical assignment record.
 * Used for API responses when querying assignment history.
 */
public class AssignmentHistoryDTO {
    
    private UUID id;
    private UUID assetId;
    private AssignmentType assignmentType;
    private String assignedTo;
    private UUID assignedBy;
    private String assignedByUsername;
    private LocalDateTime assignedAt;
    private LocalDateTime unassignedAt;
    private boolean active;
    
    // Constructors
    
    public AssignmentHistoryDTO() {
    }
    
    // Getters and Setters
    
    public UUID getId() {
        return id;
    }
    
    public void setId(UUID id) {
        this.id = id;
    }
    
    public UUID getAssetId() {
        return assetId;
    }
    
    public void setAssetId(UUID assetId) {
        this.assetId = assetId;
    }
    
    public AssignmentType getAssignmentType() {
        return assignmentType;
    }
    
    public void setAssignmentType(AssignmentType assignmentType) {
        this.assignmentType = assignmentType;
    }
    
    public String getAssignedTo() {
        return assignedTo;
    }
    
    public void setAssignedTo(String assignedTo) {
        this.assignedTo = assignedTo;
    }
    
    public UUID getAssignedBy() {
        return assignedBy;
    }
    
    public void setAssignedBy(UUID assignedBy) {
        this.assignedBy = assignedBy;
    }
    
    public String getAssignedByUsername() {
        return assignedByUsername;
    }
    
    public void setAssignedByUsername(String assignedByUsername) {
        this.assignedByUsername = assignedByUsername;
    }
    
    public LocalDateTime getAssignedAt() {
        return assignedAt;
    }
    
    public void setAssignedAt(LocalDateTime assignedAt) {
        this.assignedAt = assignedAt;
    }
    
    public LocalDateTime getUnassignedAt() {
        return unassignedAt;
    }
    
    public void setUnassignedAt(LocalDateTime unassignedAt) {
        this.unassignedAt = unassignedAt;
    }
    
    public boolean isActive() {
        return active;
    }
    
    public void setActive(boolean active) {
        this.active = active;
    }
    
    @Override
    public String toString() {
        return "AssignmentHistoryDTO{" +
                "id=" + id +
                ", assetId=" + assetId +
                ", assignmentType=" + assignmentType +
                ", assignedTo='" + assignedTo + '\'' +
                ", assignedBy=" + assignedBy +
                ", assignedByUsername='" + assignedByUsername + '\'' +
                ", assignedAt=" + assignedAt +
                ", unassignedAt=" + unassignedAt +
                ", active=" + active +
                '}';
    }
}
