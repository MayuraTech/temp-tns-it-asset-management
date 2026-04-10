package com.company.assetmanagement.dto;

import com.company.assetmanagement.model.TicketStatus;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Data Transfer Object for TicketStatusHistory entity.
 * Used to return ticket status history information to clients.
 * 
 * Represents a single status transition in a ticket's lifecycle:
 * - From status (previous status, null for initial creation)
 * - To status (new status)
 * - Who made the change (changedBy)
 * - When the change occurred (changedAt)
 * - Optional comments explaining the change
 */
public class TicketStatusHistoryDTO {
    
    private UUID id;
    private UUID ticketId;
    private TicketStatus fromStatus;
    private TicketStatus toStatus;
    private UUID changedBy;
    private String changedByName;
    private LocalDateTime changedAt;
    private String comments;
    
    // Constructors
    public TicketStatusHistoryDTO() {
    }
    
    // Getters and Setters
    public UUID getId() {
        return id;
    }
    
    public void setId(UUID id) {
        this.id = id;
    }
    
    public UUID getTicketId() {
        return ticketId;
    }
    
    public void setTicketId(UUID ticketId) {
        this.ticketId = ticketId;
    }
    
    public TicketStatus getFromStatus() {
        return fromStatus;
    }
    
    public void setFromStatus(TicketStatus fromStatus) {
        this.fromStatus = fromStatus;
    }
    
    public TicketStatus getToStatus() {
        return toStatus;
    }
    
    public void setToStatus(TicketStatus toStatus) {
        this.toStatus = toStatus;
    }
    
    public UUID getChangedBy() {
        return changedBy;
    }
    
    public void setChangedBy(UUID changedBy) {
        this.changedBy = changedBy;
    }
    
    public String getChangedByName() {
        return changedByName;
    }
    
    public void setChangedByName(String changedByName) {
        this.changedByName = changedByName;
    }
    
    public LocalDateTime getChangedAt() {
        return changedAt;
    }
    
    public void setChangedAt(LocalDateTime changedAt) {
        this.changedAt = changedAt;
    }
    
    public String getComments() {
        return comments;
    }
    
    public void setComments(String comments) {
        this.comments = comments;
    }
}
