package com.company.assetmanagement.dto;

import com.company.assetmanagement.model.TicketPriority;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.*;

import java.util.UUID;

/**
 * Request DTO for creating an allocation ticket.
 * Represents a request to assign an asset to a user or location.
 * 
 * Validation rules:
 * - Asset ID is required
 * - Priority is required
 * - Request reason must be between 10 and 1000 characters
 * - At least one of assignToUser or assignToLocation must be provided
 * - Email format validation for assignToUserEmail if provided
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AllocationTicketRequest {
    
    @NotNull(message = "Asset ID is required")
    private UUID assetId;
    
    @NotNull(message = "Priority is required")
    private TicketPriority priority;
    
    @NotBlank(message = "Request reason is required")
    @Size(min = 10, max = 1000, message = "Request reason must be between 10 and 1000 characters")
    private String requestReason;
    
    @Size(max = 255, message = "Assign to user must not exceed 255 characters")
    private String assignToUser;
    
    @Email(message = "Invalid email format")
    @Size(max = 255, message = "Assign to user email must not exceed 255 characters")
    private String assignToUserEmail;
    
    @Size(max = 255, message = "Assign to location must not exceed 255 characters")
    private String assignToLocation;
    
    // Constructors
    public AllocationTicketRequest() {
    }
    
    public AllocationTicketRequest(UUID assetId, TicketPriority priority, String requestReason) {
        this.assetId = assetId;
        this.priority = priority;
        this.requestReason = requestReason;
    }
    
    // Getters and Setters
    public UUID getAssetId() {
        return assetId;
    }
    
    public void setAssetId(UUID assetId) {
        this.assetId = assetId;
    }
    
    public TicketPriority getPriority() {
        return priority;
    }
    
    public void setPriority(TicketPriority priority) {
        this.priority = priority;
    }
    
    public String getRequestReason() {
        return requestReason;
    }
    
    public void setRequestReason(String requestReason) {
        this.requestReason = requestReason;
    }
    
    public String getAssignToUser() {
        return assignToUser;
    }
    
    public void setAssignToUser(String assignToUser) {
        this.assignToUser = assignToUser;
    }
    
    public String getAssignToUserEmail() {
        return assignToUserEmail;
    }
    
    public void setAssignToUserEmail(String assignToUserEmail) {
        this.assignToUserEmail = assignToUserEmail;
    }
    
    public String getAssignToLocation() {
        return assignToLocation;
    }
    
    public void setAssignToLocation(String assignToLocation) {
        this.assignToLocation = assignToLocation;
    }
    
    /**
     * Validates that at least one assignment target is provided.
     * 
     * @return true if at least one of assignToUser or assignToLocation is provided
     */
    @AssertTrue(message = "At least one of assignToUser or assignToLocation must be provided")
    public boolean isValidAssignment() {
        return (assignToUser != null && !assignToUser.isBlank()) 
            || (assignToLocation != null && !assignToLocation.isBlank());
    }
    
    @Override
    public String toString() {
        return "AllocationTicketRequest{" +
                "assetId=" + assetId +
                ", priority=" + priority +
                ", assignToUser='" + assignToUser + '\'' +
                ", assignToLocation='" + assignToLocation + '\'' +
                '}';
    }
}
