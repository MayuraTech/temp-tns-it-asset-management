package com.company.assetmanagement.dto;

import com.company.assetmanagement.model.TicketPriority;
import com.company.assetmanagement.model.TicketStatus;
import com.company.assetmanagement.model.TicketType;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Data Transfer Object for Ticket entity.
 * Used to return ticket information to clients.
 * 
 * Contains all ticket fields including:
 * - Basic ticket information (ID, number, type, status, priority)
 * - Asset information (ID, name, serial number)
 * - Requester information (ID, name, email)
 * - Assignment information (for allocation tickets)
 * - Request details (reasons, comments)
 * - Approver information (ID, name, comments, rejection reason)
 * - Audit timestamps (created, updated, approved, rejected, completed, cancelled)
 */
public class TicketDTO {
    
    private UUID id;
    private String ticketNumber;
    private TicketType type;
    private TicketStatus status;
    private TicketPriority priority;
    
    // Asset Information
    private UUID assetId;
    private String assetName;
    private String assetSerialNumber;
    
    // Requester Information
    private UUID requesterId;
    private String requesterName;
    private String requesterEmail;
    
    // Assignment Information (for allocation tickets)
    private String assignToUser;
    private String assignToUserEmail;
    private String assignToLocation;
    
    // Request Details
    private String requestReason;
    private String deallocationReason;
    
    // Approver Information
    private UUID approverId;
    private String approverName;
    private String approvalComments;
    private String rejectionReason;
    
    // Audit Fields
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime approvedAt;
    private LocalDateTime rejectedAt;
    private LocalDateTime completedAt;
    private LocalDateTime cancelledAt;
    
    // Constructors
    public TicketDTO() {
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
}
