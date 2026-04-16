package com.company.assetmanagement.dto;

import com.company.assetmanagement.model.NotificationType;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Data Transfer Object for Notification entity.
 * Used to return notification information to clients.
 * 
 * Notifications inform users about ticket status changes:
 * - TICKET_APPROVED: Ticket has been approved
 * - TICKET_REJECTED: Ticket has been rejected with a reason
 * - TICKET_COMPLETED: Ticket has been completed
 * - TICKET_CANCELLED: Ticket has been cancelled
 * 
 * Includes ticket details for display in notification UI.
 */
public class NotificationDTO {
    
    private UUID id;
    private UUID userId;
    private UUID ticketId;
    private String ticketNumber;
    private String assetName;
    private NotificationType notificationType;
    private String message;
    private Boolean isRead;
    private LocalDateTime createdAt;
    
    // Constructors
    public NotificationDTO() {
    }
    
    // Getters and Setters
    public UUID getId() {
        return id;
    }
    
    public void setId(UUID id) {
        this.id = id;
    }
    
    public UUID getUserId() {
        return userId;
    }
    
    public void setUserId(UUID userId) {
        this.userId = userId;
    }
    
    public UUID getTicketId() {
        return ticketId;
    }
    
    public void setTicketId(UUID ticketId) {
        this.ticketId = ticketId;
    }
    
    public String getTicketNumber() {
        return ticketNumber;
    }
    
    public void setTicketNumber(String ticketNumber) {
        this.ticketNumber = ticketNumber;
    }
    
    public String getAssetName() {
        return assetName;
    }
    
    public void setAssetName(String assetName) {
        this.assetName = assetName;
    }
    
    public NotificationType getNotificationType() {
        return notificationType;
    }
    
    public void setNotificationType(NotificationType notificationType) {
        this.notificationType = notificationType;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public Boolean getIsRead() {
        return isRead;
    }
    
    public void setIsRead(Boolean isRead) {
        this.isRead = isRead;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
