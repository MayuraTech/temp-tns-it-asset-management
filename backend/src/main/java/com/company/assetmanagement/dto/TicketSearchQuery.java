package com.company.assetmanagement.dto;

import com.company.assetmanagement.model.TicketPriority;
import com.company.assetmanagement.model.TicketStatus;
import com.company.assetmanagement.model.TicketType;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Query DTO for searching and filtering tickets.
 * Supports filtering by multiple criteria with AND logic.
 * 
 * Supported filters:
 * - Status (PENDING, APPROVED, REJECTED, COMPLETED, CANCELLED)
 * - Type (ALLOCATION, DEALLOCATION)
 * - Priority (LOW, STANDARD, URGENT)
 * - Requester ID
 * - Approver ID
 * - Asset ID
 * - Date range (created from/to)
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TicketSearchQuery {
    
    private List<TicketStatus> statuses;
    private List<TicketType> types;
    private List<TicketPriority> priorities;
    private UUID requesterId;
    private UUID approverId;
    private UUID assetId;
    private LocalDate createdFrom;
    private LocalDate createdTo;
    
    // Constructors
    public TicketSearchQuery() {
    }
    
    // Getters and Setters
    public List<TicketStatus> getStatuses() {
        return statuses;
    }
    
    public void setStatuses(List<TicketStatus> statuses) {
        this.statuses = statuses;
    }
    
    public List<TicketType> getTypes() {
        return types;
    }
    
    public void setTypes(List<TicketType> types) {
        this.types = types;
    }
    
    public List<TicketPriority> getPriorities() {
        return priorities;
    }
    
    public void setPriorities(List<TicketPriority> priorities) {
        this.priorities = priorities;
    }
    
    public UUID getRequesterId() {
        return requesterId;
    }
    
    public void setRequesterId(UUID requesterId) {
        this.requesterId = requesterId;
    }
    
    public UUID getApproverId() {
        return approverId;
    }
    
    public void setApproverId(UUID approverId) {
        this.approverId = approverId;
    }
    
    public UUID getAssetId() {
        return assetId;
    }
    
    public void setAssetId(UUID assetId) {
        this.assetId = assetId;
    }
    
    public LocalDate getCreatedFrom() {
        return createdFrom;
    }
    
    public void setCreatedFrom(LocalDate createdFrom) {
        this.createdFrom = createdFrom;
    }
    
    public LocalDate getCreatedTo() {
        return createdTo;
    }
    
    public void setCreatedTo(LocalDate createdTo) {
        this.createdTo = createdTo;
    }
    
    /**
     * Check if any filter criteria is specified.
     * 
     * @return true if at least one filter is set
     */
    public boolean hasFilters() {
        return (statuses != null && !statuses.isEmpty())
            || (types != null && !types.isEmpty())
            || (priorities != null && !priorities.isEmpty())
            || requesterId != null
            || approverId != null
            || assetId != null
            || createdFrom != null
            || createdTo != null;
    }
    
    /**
     * Builder for creating TicketSearchQuery instances.
     */
    public static class Builder {
        private final TicketSearchQuery query;
        
        public Builder() {
            this.query = new TicketSearchQuery();
        }
        
        public Builder statuses(List<TicketStatus> statuses) {
            query.statuses = statuses;
            return this;
        }
        
        public Builder types(List<TicketType> types) {
            query.types = types;
            return this;
        }
        
        public Builder priorities(List<TicketPriority> priorities) {
            query.priorities = priorities;
            return this;
        }
        
        public Builder requesterId(UUID requesterId) {
            query.requesterId = requesterId;
            return this;
        }
        
        public Builder approverId(UUID approverId) {
            query.approverId = approverId;
            return this;
        }
        
        public Builder assetId(UUID assetId) {
            query.assetId = assetId;
            return this;
        }
        
        public Builder createdFrom(LocalDate createdFrom) {
            query.createdFrom = createdFrom;
            return this;
        }
        
        public Builder createdTo(LocalDate createdTo) {
            query.createdTo = createdTo;
            return this;
        }
        
        public TicketSearchQuery build() {
            return query;
        }
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    @Override
    public String toString() {
        return "TicketSearchQuery{" +
                "statuses=" + statuses +
                ", types=" + types +
                ", priorities=" + priorities +
                ", requesterId=" + requesterId +
                ", approverId=" + approverId +
                ", assetId=" + assetId +
                ", createdFrom=" + createdFrom +
                ", createdTo=" + createdTo +
                '}';
    }
}
