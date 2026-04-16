package com.company.assetmanagement.util;

import com.company.assetmanagement.dto.TicketDTO;
import com.company.assetmanagement.model.Ticket;

/**
 * Utility class for mapping between Ticket entity and TicketDTO.
 * Provides static methods for entity-to-DTO conversions.
 */
public class TicketMapper {
    
    private TicketMapper() {
        // Private constructor to prevent instantiation
    }
    
    /**
     * Maps a Ticket entity to a TicketDTO.
     * 
     * @param ticket the ticket entity to map
     * @return the mapped TicketDTO, or null if input is null
     */
    public static TicketDTO toDTO(Ticket ticket) {
        if (ticket == null) {
            return null;
        }
        
        TicketDTO dto = new TicketDTO();
        dto.setId(ticket.getId());
        dto.setTicketNumber(ticket.getTicketNumber());
        dto.setType(ticket.getType());
        dto.setStatus(ticket.getStatus());
        dto.setPriority(ticket.getPriority());
        
        // Asset Information
        dto.setAssetId(ticket.getAssetId());
        dto.setAssetName(ticket.getAssetName());
        dto.setAssetSerialNumber(ticket.getAssetSerialNumber());
        
        // Requester Information
        dto.setRequesterId(ticket.getRequesterId());
        dto.setRequesterName(ticket.getRequesterName());
        dto.setRequesterEmail(ticket.getRequesterEmail());
        
        // Assignment Information
        dto.setAssignToUser(ticket.getAssignToUser());
        dto.setAssignToUserEmail(ticket.getAssignToUserEmail());
        dto.setAssignToLocation(ticket.getAssignToLocation());
        
        // Request Details
        dto.setRequestReason(ticket.getRequestReason());
        dto.setDeallocationReason(ticket.getDeallocationReason());
        
        // Approver Information
        dto.setApproverId(ticket.getApproverId());
        dto.setApproverName(ticket.getApproverName());
        dto.setApprovalComments(ticket.getApprovalComments());
        dto.setRejectionReason(ticket.getRejectionReason());
        
        // Audit Fields
        dto.setCreatedAt(ticket.getCreatedAt());
        dto.setUpdatedAt(ticket.getUpdatedAt());
        dto.setApprovedAt(ticket.getApprovedAt());
        dto.setRejectedAt(ticket.getRejectedAt());
        dto.setCompletedAt(ticket.getCompletedAt());
        dto.setCancelledAt(ticket.getCancelledAt());
        
        return dto;
    }
}
