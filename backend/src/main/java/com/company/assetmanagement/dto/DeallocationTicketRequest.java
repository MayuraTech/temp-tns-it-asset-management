package com.company.assetmanagement.dto;

import com.company.assetmanagement.model.TicketPriority;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.*;

import java.util.UUID;

/**
 * Request DTO for creating a de-allocation ticket.
 * Represents a request to remove an asset assignment.
 * 
 * Validation rules:
 * - Asset ID is required
 * - Priority is required
 * - Deallocation reason must be between 10 and 1000 characters
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DeallocationTicketRequest {
    
    @NotNull(message = "Asset ID is required")
    private UUID assetId;
    
    @NotNull(message = "Priority is required")
    private TicketPriority priority;
    
    @NotBlank(message = "Deallocation reason is required")
    @Size(min = 10, max = 1000, message = "Deallocation reason must be between 10 and 1000 characters")
    private String deallocationReason;
    
    // Constructors
    public DeallocationTicketRequest() {
    }
    
    public DeallocationTicketRequest(UUID assetId, TicketPriority priority, String deallocationReason) {
        this.assetId = assetId;
        this.priority = priority;
        this.deallocationReason = deallocationReason;
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
    
    public String getDeallocationReason() {
        return deallocationReason;
    }
    
    public void setDeallocationReason(String deallocationReason) {
        this.deallocationReason = deallocationReason;
    }
    
    @Override
    public String toString() {
        return "DeallocationTicketRequest{" +
                "assetId=" + assetId +
                ", priority=" + priority +
                ", deallocationReason='" + deallocationReason + '\'' +
                '}';
    }
}
