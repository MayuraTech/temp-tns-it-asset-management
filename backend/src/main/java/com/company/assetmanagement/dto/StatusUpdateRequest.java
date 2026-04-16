package com.company.assetmanagement.dto;

import com.company.assetmanagement.model.LifecycleStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

/**
 * Data Transfer Object for asset status update requests.
 * Used for PATCH /api/v1/assets/{id}/status endpoint.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Request object for updating an asset's lifecycle status")
public class StatusUpdateRequest {
    
    @NotNull(message = "New status is required")
    @Schema(description = "New lifecycle status for the asset (must be a valid transition from current status)", example = "IN_USE", required = true)
    private LifecycleStatus newStatus;
    
    @Schema(description = "Optional reason for the status change", example = "Asset deployed to production environment")
    private String reason;
    
    public StatusUpdateRequest() {
    }
    
    public StatusUpdateRequest(LifecycleStatus newStatus) {
        this.newStatus = newStatus;
    }
    
    public StatusUpdateRequest(LifecycleStatus newStatus, String reason) {
        this.newStatus = newStatus;
        this.reason = reason;
    }
    
    // Getters and setters
    
    public LifecycleStatus getNewStatus() {
        return newStatus;
    }
    
    public void setNewStatus(LifecycleStatus newStatus) {
        this.newStatus = newStatus;
    }
    
    public String getReason() {
        return reason;
    }
    
    public void setReason(String reason) {
        this.reason = reason;
    }
    
    /**
     * Builder for creating StatusUpdateRequest instances.
     */
    public static class Builder {
        private final StatusUpdateRequest request;
        
        public Builder() {
            this.request = new StatusUpdateRequest();
        }
        
        public Builder newStatus(LifecycleStatus newStatus) {
            request.newStatus = newStatus;
            return this;
        }
        
        public Builder reason(String reason) {
            request.reason = reason;
            return this;
        }
        
        public StatusUpdateRequest build() {
            return request;
        }
    }
    
    public static Builder builder() {
        return new Builder();
    }
}
