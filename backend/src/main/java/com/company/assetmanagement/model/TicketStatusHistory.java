package com.company.assetmanagement.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entity representing the history of status changes for a ticket.
 * Maintains an immutable audit trail of all ticket status transitions.
 * 
 * Each entry records:
 * - The previous status (from_status)
 * - The new status (to_status)
 * - Who made the change (changed_by)
 * - When the change occurred (changed_at)
 * - Optional comments explaining the change
 * 
 * Status history entries are immutable and cannot be modified or deleted.
 * When a ticket is deleted, all associated status history entries are also deleted (cascade).
 */
@Entity
@Table(name = "TicketStatusHistory", indexes = {
    @Index(name = "IX_TicketStatusHistory_TicketId", columnList = "ticketId"),
    @Index(name = "IX_TicketStatusHistory_ChangedAt", columnList = "changedAt"),
    @Index(name = "IX_TicketStatusHistory_ChangedBy", columnList = "changedBy")
})
public class TicketStatusHistory {
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;
    
    @Column(nullable = false)
    @NotNull(message = "Ticket ID is required")
    private UUID ticketId;
    
    @Column(length = 20)
    @Enumerated(EnumType.STRING)
    private TicketStatus fromStatus;
    
    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    @NotNull(message = "To status is required")
    private TicketStatus toStatus;
    
    @Column(nullable = false)
    @NotNull(message = "Changed by is required")
    private UUID changedBy;
    
    @Column(nullable = false, updatable = false)
    @NotNull(message = "Changed at is required")
    private LocalDateTime changedAt;
    
    @Column(columnDefinition = "NVARCHAR(MAX)")
    private String comments;
    
    // Relationship to Ticket entity with cascade delete
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ticketId", insertable = false, updatable = false)
    private Ticket ticket;
    
    // Constructors
    public TicketStatusHistory() {
        this.changedAt = LocalDateTime.now();
    }
    
    public TicketStatusHistory(UUID ticketId, TicketStatus fromStatus, TicketStatus toStatus, 
                               UUID changedBy, String comments) {
        this.ticketId = ticketId;
        this.fromStatus = fromStatus;
        this.toStatus = toStatus;
        this.changedBy = changedBy;
        this.changedAt = LocalDateTime.now();
        this.comments = comments;
    }
    
    // Getters only - no setters to enforce immutability after creation
    public UUID getId() {
        return id;
    }
    
    public UUID getTicketId() {
        return ticketId;
    }
    
    public TicketStatus getFromStatus() {
        return fromStatus;
    }
    
    public TicketStatus getToStatus() {
        return toStatus;
    }
    
    public UUID getChangedBy() {
        return changedBy;
    }
    
    public LocalDateTime getChangedAt() {
        return changedAt;
    }
    
    public String getComments() {
        return comments;
    }
    
    public Ticket getTicket() {
        return ticket;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TicketStatusHistory)) return false;
        TicketStatusHistory that = (TicketStatusHistory) o;
        return id != null && id.equals(that.id);
    }
    
    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
    
    @Override
    public String toString() {
        return "TicketStatusHistory{" +
                "id=" + id +
                ", ticketId=" + ticketId +
                ", fromStatus=" + fromStatus +
                ", toStatus=" + toStatus +
                ", changedBy=" + changedBy +
                ", changedAt=" + changedAt +
                '}';
    }
}
