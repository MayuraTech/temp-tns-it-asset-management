package com.company.assetmanagement.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entity representing a notification sent to users for ticket status changes.
 * Notifications inform users about important events related to their tickets.
 * 
 * Notification types:
 * - TICKET_APPROVED: Ticket has been approved by an approver
 * - TICKET_REJECTED: Ticket has been rejected with a reason
 * - TICKET_COMPLETED: Ticket has been completed and asset action executed
 * - TICKET_CANCELLED: Ticket has been cancelled
 */
@Entity
@Table(name = "Notifications", indexes = {
    @Index(name = "IX_Notifications_UserId", columnList = "userId"),
    @Index(name = "IX_Notifications_TicketId", columnList = "ticketId"),
    @Index(name = "IX_Notifications_IsRead", columnList = "isRead"),
    @Index(name = "IX_Notifications_CreatedAt", columnList = "createdAt")
})
public class Notification {
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;
    
    @Column(nullable = false)
    @NotNull(message = "User ID is required")
    private UUID userId;
    
    @Column(nullable = false)
    @NotNull(message = "Ticket ID is required")
    private UUID ticketId;
    
    @Column(nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    @NotNull(message = "Notification type is required")
    private NotificationType notificationType;
    
    @Column(nullable = false, columnDefinition = "NVARCHAR(MAX)")
    @NotBlank(message = "Message is required")
    private String message;
    
    @Column(nullable = false)
    private Boolean isRead;
    
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    // Relationship to Ticket entity
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ticketId", insertable = false, updatable = false)
    private Ticket ticket;
    
    // Constructors
    public Notification() {
        this.isRead = false;
        this.createdAt = LocalDateTime.now();
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
    
    public Ticket getTicket() {
        return ticket;
    }
    
    public void setTicket(Ticket ticket) {
        this.ticket = ticket;
    }
    
    // Business logic methods
    
    /**
     * Mark this notification as read.
     */
    public void markAsRead() {
        this.isRead = true;
    }
    
    /**
     * Mark this notification as unread.
     */
    public void markAsUnread() {
        this.isRead = false;
    }
    
    /**
     * Check if this notification is unread.
     * 
     * @return true if notification is unread
     */
    public boolean isUnread() {
        return !this.isRead;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Notification)) return false;
        Notification that = (Notification) o;
        return id != null && id.equals(that.id);
    }
    
    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
    
    @Override
    public String toString() {
        return "Notification{" +
                "id=" + id +
                ", userId=" + userId +
                ", ticketId=" + ticketId +
                ", notificationType=" + notificationType +
                ", isRead=" + isRead +
                ", createdAt=" + createdAt +
                '}';
    }
}
