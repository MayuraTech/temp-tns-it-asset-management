package com.company.assetmanagement.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Entity representing an asset allocation or de-allocation ticket.
 * Tickets follow an approval workflow with status transitions.
 * 
 * Supports two types of tickets:
 * - ALLOCATION: Request to assign an asset to a user or location
 * - DEALLOCATION: Request to remove an asset assignment
 * 
 * Status workflow:
 * PENDING -> APPROVED -> COMPLETED
 * PENDING -> REJECTED (terminal)
 * PENDING -> CANCELLED (terminal)
 * APPROVED -> CANCELLED (terminal)
 */
@Entity
@Table(name = "Tickets", indexes = {
    @Index(name = "IX_Tickets_TicketNumber", columnList = "ticketNumber"),
    @Index(name = "IX_Tickets_Status", columnList = "status"),
    @Index(name = "IX_Tickets_Type", columnList = "type"),
    @Index(name = "IX_Tickets_Priority", columnList = "priority"),
    @Index(name = "IX_Tickets_AssetId", columnList = "assetId"),
    @Index(name = "IX_Tickets_RequesterId", columnList = "requesterId"),
    @Index(name = "IX_Tickets_ApproverId", columnList = "approverId"),
    @Index(name = "IX_Tickets_CreatedAt", columnList = "createdAt")
})
public class Ticket {
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;
    
    @Column(nullable = false, unique = true, length = 50)
    @NotBlank(message = "Ticket number is required")
    @Size(max = 50, message = "Ticket number must not exceed 50 characters")
    private String ticketNumber;
    
    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    @NotNull(message = "Ticket type is required")
    private TicketType type;
    
    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    @NotNull(message = "Ticket status is required")
    private TicketStatus status;
    
    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    @NotNull(message = "Ticket priority is required")
    private TicketPriority priority;
    
    // Asset Information
    @Column(nullable = false)
    @NotNull(message = "Asset ID is required")
    private UUID assetId;
    
    @Column(nullable = false, length = 255)
    @NotBlank(message = "Asset name is required")
    @Size(max = 255, message = "Asset name must not exceed 255 characters")
    private String assetName;
    
    @Column(nullable = false, length = 100)
    @NotBlank(message = "Asset serial number is required")
    @Size(max = 100, message = "Asset serial number must not exceed 100 characters")
    private String assetSerialNumber;
    
    // Requester Information
    @Column(nullable = false)
    @NotNull(message = "Requester ID is required")
    private UUID requesterId;
    
    @Column(nullable = false, length = 255)
    @NotBlank(message = "Requester name is required")
    @Size(max = 255, message = "Requester name must not exceed 255 characters")
    private String requesterName;
    
    @Column(nullable = false, length = 255)
    @Email(message = "Invalid requester email format")
    @Size(max = 255, message = "Requester email must not exceed 255 characters")
    private String requesterEmail;
    
    // Assignment Information (for allocation tickets)
    @Column(length = 255)
    @Size(max = 255, message = "Assign to user must not exceed 255 characters")
    private String assignToUser;
    
    @Column(length = 255)
    @Email(message = "Invalid assign to user email format")
    @Size(max = 255, message = "Assign to user email must not exceed 255 characters")
    private String assignToUserEmail;
    
    @Column(length = 255)
    @Size(max = 255, message = "Assign to location must not exceed 255 characters")
    private String assignToLocation;
    
    // Request Details
    @Column(columnDefinition = "NVARCHAR(MAX)")
    @Size(min = 10, max = 1000, message = "Request reason must be between 10 and 1000 characters")
    private String requestReason;
    
    @Column(columnDefinition = "NVARCHAR(MAX)")
    @Size(min = 10, max = 1000, message = "Deallocation reason must be between 10 and 1000 characters")
    private String deallocationReason;
    
    // Approver Information
    @Column
    private UUID approverId;
    
    @Column(length = 255)
    @Size(max = 255, message = "Approver name must not exceed 255 characters")
    private String approverName;
    
    @Column(columnDefinition = "NVARCHAR(MAX)")
    private String approvalComments;
    
    @Column(columnDefinition = "NVARCHAR(MAX)")
    private String rejectionReason;
    
    // Audit Fields
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(nullable = false)
    private LocalDateTime updatedAt;
    
    @Column
    private LocalDateTime approvedAt;
    
    @Column
    private LocalDateTime rejectedAt;
    
    @Column
    private LocalDateTime completedAt;
    
    @Column
    private LocalDateTime cancelledAt;
    
    // Relationship to TicketStatusHistory with cascade delete
    @OneToMany(mappedBy = "ticket", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<TicketStatusHistory> statusHistory = new ArrayList<>();
    
    // Constructors
    public Ticket() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.status = TicketStatus.PENDING;
    }
    
    // Lifecycle callbacks
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
    
    // Getters and Setters
    public UUID getId() {
        return id;
    }
    
    public void setId(UUID id) {
        this.id = id;
    }
    
    public String getTicketNumber() {
        return ticketNumber;
    }
    
    public void setTicketNumber(String ticketNumber) {
        this.ticketNumber = ticketNumber;
    }
    
    public TicketType getType() {
        return type;
    }
    
    public void setType(TicketType type) {
        this.type = type;
    }
    
    public TicketStatus getStatus() {
        return status;
    }
    
    public void setStatus(TicketStatus status) {
        this.status = status;
    }
    
    public TicketPriority getPriority() {
        return priority;
    }
    
    public void setPriority(TicketPriority priority) {
        this.priority = priority;
    }
    
    public UUID getAssetId() {
        return assetId;
    }
    
    public void setAssetId(UUID assetId) {
        this.assetId = assetId;
    }
    
    public String getAssetName() {
        return assetName;
    }
    
    public void setAssetName(String assetName) {
        this.assetName = assetName;
    }
    
    public String getAssetSerialNumber() {
        return assetSerialNumber;
    }
    
    public void setAssetSerialNumber(String assetSerialNumber) {
        this.assetSerialNumber = assetSerialNumber;
    }
    
    public UUID getRequesterId() {
        return requesterId;
    }
    
    public void setRequesterId(UUID requesterId) {
        this.requesterId = requesterId;
    }
    
    public String getRequesterName() {
        return requesterName;
    }
    
    public void setRequesterName(String requesterName) {
        this.requesterName = requesterName;
    }
    
    public String getRequesterEmail() {
        return requesterEmail;
    }
    
    public void setRequesterEmail(String requesterEmail) {
        this.requesterEmail = requesterEmail;
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
    
    public String getRequestReason() {
        return requestReason;
    }
    
    public void setRequestReason(String requestReason) {
        this.requestReason = requestReason;
    }
    
    public String getDeallocationReason() {
        return deallocationReason;
    }
    
    public void setDeallocationReason(String deallocationReason) {
        this.deallocationReason = deallocationReason;
    }
    
    public UUID getApproverId() {
        return approverId;
    }
    
    public void setApproverId(UUID approverId) {
        this.approverId = approverId;
    }
    
    public String getApproverName() {
        return approverName;
    }
    
    public void setApproverName(String approverName) {
        this.approverName = approverName;
    }
    
    public String getApprovalComments() {
        return approvalComments;
    }
    
    public void setApprovalComments(String approvalComments) {
        this.approvalComments = approvalComments;
    }
    
    public String getRejectionReason() {
        return rejectionReason;
    }
    
    public void setRejectionReason(String rejectionReason) {
        this.rejectionReason = rejectionReason;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    public LocalDateTime getApprovedAt() {
        return approvedAt;
    }
    
    public void setApprovedAt(LocalDateTime approvedAt) {
        this.approvedAt = approvedAt;
    }
    
    public LocalDateTime getRejectedAt() {
        return rejectedAt;
    }
    
    public void setRejectedAt(LocalDateTime rejectedAt) {
        this.rejectedAt = rejectedAt;
    }
    
    public LocalDateTime getCompletedAt() {
        return completedAt;
    }
    
    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }
    
    public LocalDateTime getCancelledAt() {
        return cancelledAt;
    }
    
    public void setCancelledAt(LocalDateTime cancelledAt) {
        this.cancelledAt = cancelledAt;
    }
    
    public List<TicketStatusHistory> getStatusHistory() {
        return statusHistory;
    }
    
    public void setStatusHistory(List<TicketStatusHistory> statusHistory) {
        this.statusHistory = statusHistory;
    }
    
    // Business logic methods
    
    /**
     * Check if this ticket can transition to the target status.
     * 
     * @param targetStatus the target status
     * @return true if transition is valid, false otherwise
     */
    public boolean canTransitionTo(TicketStatus targetStatus) {
        return this.status.canTransitionTo(targetStatus);
    }
    
    /**
     * Check if this ticket is in a terminal state (cannot be modified).
     * 
     * @return true if ticket is in terminal state
     */
    public boolean isTerminal() {
        return status == TicketStatus.COMPLETED 
            || status == TicketStatus.REJECTED 
            || status == TicketStatus.CANCELLED;
    }
    
    /**
     * Check if this ticket requires approval.
     * 
     * @return true if ticket is pending approval
     */
    public boolean isPendingApproval() {
        return status == TicketStatus.PENDING;
    }
    
    /**
     * Check if this ticket has been approved.
     * 
     * @return true if ticket is approved
     */
    public boolean isApproved() {
        return status == TicketStatus.APPROVED;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Ticket)) return false;
        Ticket ticket = (Ticket) o;
        return id != null && id.equals(ticket.id);
    }
    
    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
    
    @Override
    public String toString() {
        return "Ticket{" +
                "id=" + id +
                ", ticketNumber='" + ticketNumber + '\'' +
                ", type=" + type +
                ", status=" + status +
                ", priority=" + priority +
                ", assetName='" + assetName + '\'' +
                ", requesterName='" + requesterName + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}
