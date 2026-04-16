package com.company.assetmanagement.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Entity representing the assignment history of assets to users or locations.
 * 
 * <p>This entity tracks the complete chronological record of all assignments for an asset,
 * including both active assignments (UnassignedAt is null) and historical assignments
 * (UnassignedAt is not null). Each record captures who assigned the asset, when it was
 * assigned, and when it was unassigned (if applicable).
 * 
 * <p>The AssignmentHistory table maintains referential integrity with:
 * <ul>
 *   <li>Assets table - CASCADE DELETE when asset is removed</li>
 *   <li>Users table - References the user who performed the assignment</li>
 * </ul>
 * 
 * <p>Assignment types:
 * <ul>
 *   <li>USER - Asset assigned to a specific user</li>
 *   <li>LOCATION - Asset assigned to a physical or logical location</li>
 * </ul>
 * 
 * @see AssignmentType
 * @see com.company.assetmanagement.repository.AssignmentHistoryRepository
 */
@Entity
@Table(name = "AssignmentHistory", indexes = {
    @Index(name = "IX_AssignmentHistory_AssetId", columnList = "assetId"),
    @Index(name = "IX_AssignmentHistory_AssignedTo", columnList = "assignedTo"),
    @Index(name = "IX_AssignmentHistory_AssignedAt", columnList = "assignedAt")
})
public class AssignmentHistory {
    
    /**
     * Unique identifier for the assignment record.
     * Auto-generated UUID.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;
    
    /**
     * The ID of the asset being assigned.
     * References the Assets table with CASCADE DELETE.
     */
    @Column(nullable = false)
    @NotNull(message = "Asset ID is required")
    private UUID assetId;
    
    /**
     * The type of assignment (USER or LOCATION).
     * Stored as string in database for readability.
     */
    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    @NotNull(message = "Assignment type is required")
    private AssignmentType assignmentType;
    
    /**
     * The user name or location name to which the asset is assigned.
     * For USER assignments: contains the user's full name.
     * For LOCATION assignments: contains the location name.
     */
    @Column(nullable = false, length = 255)
    @NotBlank(message = "Assigned to is required")
    @Size(max = 255, message = "Assigned to must not exceed 255 characters")
    private String assignedTo;
    
    /**
     * The ID of the user who performed the assignment.
     * References the Users table.
     */
    @Column(nullable = false)
    @NotNull(message = "Assigned by is required")
    private UUID assignedBy;
    
    /**
     * The timestamp when the assignment was created.
     * Defaults to current UTC time.
     */
    @Column(nullable = false, updatable = false)
    @NotNull(message = "Assigned at timestamp is required")
    private LocalDateTime assignedAt;
    
    /**
     * The timestamp when the assignment was removed (deallocation).
     * Null indicates an active assignment.
     * Non-null indicates a historical (closed) assignment.
     */
    @Column(nullable = true)
    private LocalDateTime unassignedAt;
    
    /**
     * Default constructor.
     * Initializes assignedAt to current timestamp.
     */
    public AssignmentHistory() {
        this.assignedAt = LocalDateTime.now();
    }
    
    /**
     * Constructor with required fields.
     * 
     * @param assetId the ID of the asset being assigned
     * @param assignmentType the type of assignment (USER or LOCATION)
     * @param assignedTo the user name or location name
     * @param assignedBy the ID of the user performing the assignment
     */
    public AssignmentHistory(UUID assetId, AssignmentType assignmentType, 
                            String assignedTo, UUID assignedBy) {
        this.assetId = assetId;
        this.assignmentType = assignmentType;
        this.assignedTo = assignedTo;
        this.assignedBy = assignedBy;
        this.assignedAt = LocalDateTime.now();
    }
    
    // Getters and Setters
    
    /**
     * Gets the unique identifier of this assignment record.
     * 
     * @return the assignment ID
     */
    public UUID getId() {
        return id;
    }
    
    /**
     * Sets the unique identifier of this assignment record.
     * 
     * @param id the assignment ID
     */
    public void setId(UUID id) {
        this.id = id;
    }
    
    /**
     * Gets the ID of the asset being assigned.
     * 
     * @return the asset ID
     */
    public UUID getAssetId() {
        return assetId;
    }
    
    /**
     * Sets the ID of the asset being assigned.
     * 
     * @param assetId the asset ID
     */
    public void setAssetId(UUID assetId) {
        this.assetId = assetId;
    }
    
    /**
     * Gets the type of assignment.
     * 
     * @return the assignment type (USER or LOCATION)
     */
    public AssignmentType getAssignmentType() {
        return assignmentType;
    }
    
    /**
     * Sets the type of assignment.
     * 
     * @param assignmentType the assignment type (USER or LOCATION)
     */
    public void setAssignmentType(AssignmentType assignmentType) {
        this.assignmentType = assignmentType;
    }
    
    /**
     * Gets the user name or location name to which the asset is assigned.
     * 
     * @return the assigned to value
     */
    public String getAssignedTo() {
        return assignedTo;
    }
    
    /**
     * Sets the user name or location name to which the asset is assigned.
     * 
     * @param assignedTo the assigned to value
     */
    public void setAssignedTo(String assignedTo) {
        this.assignedTo = assignedTo;
    }
    
    /**
     * Gets the ID of the user who performed the assignment.
     * 
     * @return the assigned by user ID
     */
    public UUID getAssignedBy() {
        return assignedBy;
    }
    
    /**
     * Sets the ID of the user who performed the assignment.
     * 
     * @param assignedBy the assigned by user ID
     */
    public void setAssignedBy(UUID assignedBy) {
        this.assignedBy = assignedBy;
    }
    
    /**
     * Gets the timestamp when the assignment was created.
     * 
     * @return the assigned at timestamp
     */
    public LocalDateTime getAssignedAt() {
        return assignedAt;
    }
    
    /**
     * Sets the timestamp when the assignment was created.
     * 
     * @param assignedAt the assigned at timestamp
     */
    public void setAssignedAt(LocalDateTime assignedAt) {
        this.assignedAt = assignedAt;
    }
    
    /**
     * Gets the timestamp when the assignment was removed.
     * 
     * @return the unassigned at timestamp, or null if assignment is still active
     */
    public LocalDateTime getUnassignedAt() {
        return unassignedAt;
    }
    
    /**
     * Sets the timestamp when the assignment was removed.
     * 
     * @param unassignedAt the unassigned at timestamp
     */
    public void setUnassignedAt(LocalDateTime unassignedAt) {
        this.unassignedAt = unassignedAt;
    }
    
    /**
     * Checks if this assignment is currently active.
     * 
     * @return true if the assignment is active (not unassigned), false otherwise
     */
    public boolean isActive() {
        return unassignedAt == null;
    }
    
    /**
     * Compares this assignment history record with another object for equality.
     * Two assignment history records are considered equal if they have the same ID.
     * 
     * @param o the object to compare with
     * @return true if the objects are equal, false otherwise
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AssignmentHistory that = (AssignmentHistory) o;
        return Objects.equals(id, that.id);
    }
    
    /**
     * Generates a hash code for this assignment history record.
     * The hash code is based on the ID field.
     * 
     * @return the hash code
     */
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    /**
     * Returns a string representation of this assignment history record.
     * 
     * @return a string containing the key fields of this assignment
     */
    @Override
    public String toString() {
        return "AssignmentHistory{" +
                "id=" + id +
                ", assetId=" + assetId +
                ", assignmentType=" + assignmentType +
                ", assignedTo='" + assignedTo + '\'' +
                ", assignedBy=" + assignedBy +
                ", assignedAt=" + assignedAt +
                ", unassignedAt=" + unassignedAt +
                ", active=" + isActive() +
                '}';
    }
}
