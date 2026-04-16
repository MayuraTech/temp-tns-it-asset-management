package com.company.assetmanagement.service;

import com.company.assetmanagement.dto.BulkOperationResultDTO;
import com.company.assetmanagement.dto.TicketDTO;
import com.company.assetmanagement.model.TicketPriority;
import com.company.assetmanagement.model.TicketStatus;

import java.util.List;
import java.util.UUID;

/**
 * Service interface for ticket workflow operations.
 * 
 * Handles ticket status transitions, approval workflow, and bulk operations.
 * Enforces state machine rules for valid status transitions.
 * Integrates with NotificationService for status change notifications.
 * Integrates with AuditService for logging all workflow operations.
 * 
 * Valid status transitions:
 * - PENDING -> APPROVED, REJECTED, CANCELLED
 * - APPROVED -> COMPLETED, CANCELLED
 * - REJECTED, COMPLETED, CANCELLED -> No transitions (terminal states)
 * 
 * Validates Requirements: 3.1-3.6, 4.1-4.6, 5.1-5.5, 7.3, 7.4, 13.1-13.9, 16.1-16.7, 18.1-18.6
 */
public interface TicketWorkflowService {
    
    /**
     * Approve a ticket.
     * Updates ticket status from PENDING to APPROVED.
     * Records approver information and timestamp.
     * Creates status history entry and notification.
     * 
     * Validates Requirements: 3.1, 3.4, 3.5, 3.6, 12.4, 13.1, 16.4
     *
     * @param userId the ID of the user approving the ticket (must have Asset_Manager or Administrator role)
     * @param ticketId the ticket identifier
     * @param comments optional approval comments
     * @return the updated ticket DTO
     * @throws ResourceNotFoundException if ticket is not found
     * @throws InsufficientPermissionsException if user lacks APPROVE_TICKET permission
     * @throws InvalidStatusTransitionException if ticket is not in PENDING status
     */
    TicketDTO approveTicket(String userId, String ticketId, String comments);
    
    /**
     * Reject a ticket.
     * Updates ticket status from PENDING to REJECTED.
     * Requires a rejection reason to be provided.
     * Creates status history entry and notification with rejection reason.
     * 
     * Validates Requirements: 3.2, 3.4, 3.6, 12.4, 13.2, 16.4
     *
     * @param userId the ID of the user rejecting the ticket (must have Asset_Manager or Administrator role)
     * @param ticketId the ticket identifier
     * @param rejectionReason the reason for rejection (required)
     * @return the updated ticket DTO
     * @throws ResourceNotFoundException if ticket is not found
     * @throws InsufficientPermissionsException if user lacks REJECT_TICKET permission
     * @throws InvalidStatusTransitionException if ticket is not in PENDING status
     * @throws ValidationException if rejection reason is not provided
     */
    TicketDTO rejectTicket(String userId, String ticketId, String rejectionReason);
    
    /**
     * Complete a ticket.
     * Updates ticket status from APPROVED to COMPLETED.
     * Executes the allocation or de-allocation operation via integration service.
     * Creates status history entry and notification.
     * 
     * If the allocation/de-allocation operation fails, the ticket remains in APPROVED status
     * and the error details are recorded.
     * 
     * Validates Requirements: 4.1, 4.2, 4.3, 4.4, 4.5, 4.6, 12.5, 13.4, 14.1-14.5, 16.5
     *
     * @param userId the ID of the user completing the ticket (must have Asset_Manager or Administrator role)
     * @param ticketId the ticket identifier
     * @return the updated ticket DTO
     * @throws ResourceNotFoundException if ticket is not found
     * @throws InsufficientPermissionsException if user lacks COMPLETE_TICKET permission
     * @throws InvalidStatusTransitionException if ticket is not in APPROVED status
     * @throws AllocationFailedException if the allocation/de-allocation operation fails
     */
    TicketDTO completeTicket(String userId, String ticketId);
    
    /**
     * Cancel a ticket.
     * Updates ticket status to CANCELLED.
     * 
     * Authorization rules:
     * - Requesters can cancel their own PENDING tickets
     * - Asset_Managers and Administrators can cancel any PENDING or APPROVED ticket
     * 
     * Creates status history entry and notification.
     * 
     * Validates Requirements: 5.1, 5.2, 5.3, 5.4, 5.5, 12.6, 12.7, 13.3, 13.5, 16.5
     *
     * @param userId the ID of the user cancelling the ticket
     * @param ticketId the ticket identifier
     * @return the updated ticket DTO
     * @throws ResourceNotFoundException if ticket is not found
     * @throws InsufficientPermissionsException if user lacks permission to cancel the ticket
     * @throws InvalidStatusTransitionException if ticket cannot be cancelled (COMPLETED, REJECTED, or CANCELLED)
     */
    TicketDTO cancelTicket(String userId, String ticketId);
    
    /**
     * Update ticket priority.
     * Allows Asset_Managers and Administrators to modify ticket priority.
     * Records the priority change in the audit log.
     * 
     * Validates Requirements: 7.3, 7.4, 16.3
     *
     * @param userId the ID of the user updating the priority (must have Asset_Manager or Administrator role)
     * @param ticketId the ticket identifier
     * @param newPriority the new priority level
     * @return the updated ticket DTO
     * @throws ResourceNotFoundException if ticket is not found
     * @throws InsufficientPermissionsException if user lacks permission to update priority
     */
    TicketDTO updatePriority(String userId, String ticketId, TicketPriority newPriority);
    
    /**
     * Validate if a status transition is valid.
     * Checks if the ticket can transition from its current status to the target status.
     * 
     * Validates Requirements: 13.1-13.9
     *
     * @param currentStatus the current ticket status
     * @param targetStatus the target ticket status
     * @return true if transition is valid, false otherwise
     */
    boolean validateTransition(TicketStatus currentStatus, TicketStatus targetStatus);
    
    /**
     * Bulk approve multiple tickets.
     * Approves multiple PENDING tickets in a single operation.
     * Each ticket is validated independently.
     * Returns success and failure counts with details of any failures.
     * 
     * Limited to a maximum of 50 tickets per request.
     * Each ticket status change is recorded individually in the audit log.
     * 
     * Validates Requirements: 18.1, 18.3, 18.4, 18.5, 18.6
     *
     * @param userId the ID of the user performing the bulk approval (must have Asset_Manager or Administrator role)
     * @param ticketIds list of ticket identifiers to approve (max 50)
     * @param comments optional approval comments applied to all tickets
     * @return bulk operation result with success/failure counts and details
     * @throws InsufficientPermissionsException if user lacks APPROVE_TICKET permission
     * @throws ValidationException if more than 50 tickets are provided
     */
    BulkOperationResultDTO bulkApprove(String userId, List<String> ticketIds, String comments);
    
    /**
     * Bulk reject multiple tickets.
     * Rejects multiple PENDING tickets in a single operation with a common rejection reason.
     * Each ticket is validated independently.
     * Returns success and failure counts with details of any failures.
     * 
     * Limited to a maximum of 50 tickets per request.
     * Each ticket status change is recorded individually in the audit log.
     * 
     * Validates Requirements: 18.2, 18.3, 18.4, 18.5, 18.6
     *
     * @param userId the ID of the user performing the bulk rejection (must have Asset_Manager or Administrator role)
     * @param ticketIds list of ticket identifiers to reject (max 50)
     * @param rejectionReason the reason for rejection (required, applied to all tickets)
     * @return bulk operation result with success/failure counts and details
     * @throws InsufficientPermissionsException if user lacks REJECT_TICKET permission
     * @throws ValidationException if more than 50 tickets are provided or rejection reason is not provided
     */
    BulkOperationResultDTO bulkReject(String userId, List<String> ticketIds, String rejectionReason);
}
