package com.company.assetmanagement.controller;

import com.company.assetmanagement.dto.BulkOperationResultDTO;
import com.company.assetmanagement.dto.TicketDTO;
import com.company.assetmanagement.model.TicketPriority;
import com.company.assetmanagement.service.TicketWorkflowService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST controller for ticket workflow operations.
 * Provides endpoints for approving, rejecting, completing, and cancelling tickets.
 * Supports bulk operations for efficient processing of multiple tickets.
 * 
 * All workflow operations enforce authorization checks and valid status transitions.
 * Status changes are logged to the audit service and trigger notifications.
 * 
 * Validates Requirements: 3.1-3.6, 4.1-4.6, 5.1-5.5, 7.3, 7.4, 12.4-12.7, 18.1-18.6
 */
@RestController
@RequestMapping("/api/v1/tickets")
@Tag(name = "Ticket Workflow", description = "Ticket workflow endpoints for approval, rejection, completion, and cancellation")
public class TicketWorkflowController {
    
    private final TicketWorkflowService ticketWorkflowService;
    
    public TicketWorkflowController(TicketWorkflowService ticketWorkflowService) {
        this.ticketWorkflowService = ticketWorkflowService;
    }
    
    /**
     * Approve a ticket.
     * Updates ticket status from PENDING to APPROVED.
     * Requires Asset_Manager or Administrator role.
     * 
     * Validates Requirements: 3.1, 3.4, 3.5, 3.6, 12.4, 13.1
     *
     * @param id the ticket identifier
     * @param request the approval request containing optional comments
     * @param authentication the authenticated user
     * @return the updated ticket DTO
     */
    @PostMapping("/{id}/approve")
    @PreAuthorize("hasAnyRole('Asset_Manager', 'Administrator')")
    @Operation(
        summary = "Approve a ticket",
        description = "Approve a PENDING ticket. Updates status to APPROVED and records approver information. " +
                     "Requires Asset_Manager or Administrator role. Creates notification for requester."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Ticket approved successfully",
            content = @Content(schema = @Schema(implementation = TicketDTO.class))
        ),
        @ApiResponse(responseCode = "400", description = "Invalid status transition - ticket is not in PENDING status"),
        @ApiResponse(responseCode = "401", description = "Not authenticated"),
        @ApiResponse(responseCode = "403", description = "Insufficient permissions - requires Asset_Manager or Administrator role"),
        @ApiResponse(responseCode = "404", description = "Ticket not found")
    })
    public ResponseEntity<TicketDTO> approveTicket(
            @Parameter(description = "Ticket ID")
            @PathVariable UUID id,
            
            @Parameter(description = "Approval request with optional comments")
            @Valid @RequestBody ApprovalRequest request,
            
            Authentication authentication) {
        
        String userId = authentication.getName();
        TicketDTO ticket = ticketWorkflowService.approveTicket(userId, id.toString(), request.getComments());
        return ResponseEntity.ok(ticket);
    }
    
    /**
     * Reject a ticket.
     * Updates ticket status from PENDING to REJECTED.
     * Requires Asset_Manager or Administrator role.
     * Rejection reason is mandatory.
     * 
     * Validates Requirements: 3.2, 3.4, 3.6, 12.4, 13.2
     *
     * @param id the ticket identifier
     * @param request the rejection request containing mandatory rejection reason
     * @param authentication the authenticated user
     * @return the updated ticket DTO
     */
    @PostMapping("/{id}/reject")
    @PreAuthorize("hasAnyRole('Asset_Manager', 'Administrator')")
    @Operation(
        summary = "Reject a ticket",
        description = "Reject a PENDING ticket with a mandatory rejection reason. Updates status to REJECTED. " +
                     "Requires Asset_Manager or Administrator role. Creates notification for requester with rejection reason."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Ticket rejected successfully",
            content = @Content(schema = @Schema(implementation = TicketDTO.class))
        ),
        @ApiResponse(responseCode = "400", description = "Validation error - rejection reason is required or invalid status transition"),
        @ApiResponse(responseCode = "401", description = "Not authenticated"),
        @ApiResponse(responseCode = "403", description = "Insufficient permissions - requires Asset_Manager or Administrator role"),
        @ApiResponse(responseCode = "404", description = "Ticket not found")
    })
    public ResponseEntity<TicketDTO> rejectTicket(
            @Parameter(description = "Ticket ID")
            @PathVariable UUID id,
            
            @Parameter(description = "Rejection request with mandatory rejection reason")
            @Valid @RequestBody RejectionRequest request,
            
            Authentication authentication) {
        
        String userId = authentication.getName();
        TicketDTO ticket = ticketWorkflowService.rejectTicket(userId, id.toString(), request.getRejectionReason());
        return ResponseEntity.ok(ticket);
    }
    
    /**
     * Complete a ticket.
     * Updates ticket status from APPROVED to COMPLETED.
     * Executes the allocation or de-allocation operation.
     * Requires Asset_Manager or Administrator role.
     * 
     * Validates Requirements: 4.1, 4.2, 4.3, 4.4, 4.5, 4.6, 12.5, 13.4
     *
     * @param id the ticket identifier
     * @param authentication the authenticated user
     * @return the updated ticket DTO
     */
    @PostMapping("/{id}/complete")
    @PreAuthorize("hasAnyRole('Asset_Manager', 'Administrator')")
    @Operation(
        summary = "Complete a ticket",
        description = "Complete an APPROVED ticket by executing the allocation or de-allocation operation. " +
                     "Updates status to COMPLETED. Requires Asset_Manager or Administrator role. " +
                     "If allocation/de-allocation fails, ticket remains in APPROVED status with error details."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Ticket completed successfully",
            content = @Content(schema = @Schema(implementation = TicketDTO.class))
        ),
        @ApiResponse(responseCode = "400", description = "Invalid status transition - ticket is not in APPROVED status"),
        @ApiResponse(responseCode = "401", description = "Not authenticated"),
        @ApiResponse(responseCode = "403", description = "Insufficient permissions - requires Asset_Manager or Administrator role"),
        @ApiResponse(responseCode = "404", description = "Ticket not found"),
        @ApiResponse(responseCode = "409", description = "Allocation or de-allocation operation failed")
    })
    public ResponseEntity<TicketDTO> completeTicket(
            @Parameter(description = "Ticket ID")
            @PathVariable UUID id,
            
            Authentication authentication) {
        
        String userId = authentication.getName();
        TicketDTO ticket = ticketWorkflowService.completeTicket(userId, id.toString());
        return ResponseEntity.ok(ticket);
    }
    
    /**
     * Cancel a ticket.
     * Updates ticket status to CANCELLED.
     * 
     * Authorization rules:
     * - Requesters can cancel their own PENDING tickets
     * - Asset_Managers and Administrators can cancel any PENDING or APPROVED ticket
     * 
     * Validates Requirements: 5.1, 5.2, 5.3, 5.4, 5.5, 12.6, 12.7, 13.3, 13.5
     *
     * @param id the ticket identifier
     * @param authentication the authenticated user
     * @return the updated ticket DTO
     */
    @PostMapping("/{id}/cancel")
    @PreAuthorize("isAuthenticated()")
    @Operation(
        summary = "Cancel a ticket",
        description = "Cancel a ticket. Requesters can cancel their own PENDING tickets. " +
                     "Asset_Managers and Administrators can cancel any PENDING or APPROVED ticket. " +
                     "Cannot cancel COMPLETED, REJECTED, or already CANCELLED tickets."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Ticket cancelled successfully",
            content = @Content(schema = @Schema(implementation = TicketDTO.class))
        ),
        @ApiResponse(responseCode = "400", description = "Invalid status transition - ticket cannot be cancelled"),
        @ApiResponse(responseCode = "401", description = "Not authenticated"),
        @ApiResponse(responseCode = "403", description = "Insufficient permissions - user cannot cancel this ticket"),
        @ApiResponse(responseCode = "404", description = "Ticket not found")
    })
    public ResponseEntity<TicketDTO> cancelTicket(
            @Parameter(description = "Ticket ID")
            @PathVariable UUID id,
            
            Authentication authentication) {
        
        String userId = authentication.getName();
        TicketDTO ticket = ticketWorkflowService.cancelTicket(userId, id.toString());
        return ResponseEntity.ok(ticket);
    }
    
    /**
     * Update ticket priority.
     * Allows Asset_Managers and Administrators to modify ticket priority.
     * Records the priority change in the audit log.
     * 
     * Validates Requirements: 7.3, 7.4
     *
     * @param id the ticket identifier
     * @param request the priority update request
     * @param authentication the authenticated user
     * @return the updated ticket DTO
     */
    @PatchMapping("/{id}/priority")
    @PreAuthorize("hasAnyRole('Asset_Manager', 'Administrator')")
    @Operation(
        summary = "Update ticket priority",
        description = "Update the priority level of a ticket. Requires Asset_Manager or Administrator role. " +
                     "Priority change is recorded in the audit log."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Ticket priority updated successfully",
            content = @Content(schema = @Schema(implementation = TicketDTO.class))
        ),
        @ApiResponse(responseCode = "400", description = "Validation error - invalid priority value"),
        @ApiResponse(responseCode = "401", description = "Not authenticated"),
        @ApiResponse(responseCode = "403", description = "Insufficient permissions - requires Asset_Manager or Administrator role"),
        @ApiResponse(responseCode = "404", description = "Ticket not found")
    })
    public ResponseEntity<TicketDTO> updatePriority(
            @Parameter(description = "Ticket ID")
            @PathVariable UUID id,
            
            @Parameter(description = "Priority update request")
            @Valid @RequestBody PriorityUpdateRequest request,
            
            Authentication authentication) {
        
        String userId = authentication.getName();
        TicketDTO ticket = ticketWorkflowService.updatePriority(userId, id.toString(), request.getPriority());
        return ResponseEntity.ok(ticket);
    }
    
    /**
     * Bulk approve multiple tickets.
     * Approves multiple PENDING tickets in a single operation.
     * Limited to a maximum of 50 tickets per request.
     * Requires Asset_Manager or Administrator role.
     * 
     * Validates Requirements: 18.1, 18.3, 18.4, 18.5, 18.6
     *
     * @param request the bulk approval request containing ticket IDs and optional comments
     * @param authentication the authenticated user
     * @return bulk operation result with success/failure counts and details
     */
    @PostMapping("/bulk-approve")
    @PreAuthorize("hasAnyRole('Asset_Manager', 'Administrator')")
    @Operation(
        summary = "Bulk approve tickets",
        description = "Approve multiple PENDING tickets in a single operation. Each ticket is validated independently. " +
                     "Limited to 50 tickets per request. Requires Asset_Manager or Administrator role. " +
                     "Returns success and failure counts with details of any failures."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Bulk approval completed",
            content = @Content(schema = @Schema(implementation = BulkOperationResultDTO.class))
        ),
        @ApiResponse(responseCode = "400", description = "Validation error - more than 50 tickets or invalid ticket IDs"),
        @ApiResponse(responseCode = "401", description = "Not authenticated"),
        @ApiResponse(responseCode = "403", description = "Insufficient permissions - requires Asset_Manager or Administrator role")
    })
    public ResponseEntity<BulkOperationResultDTO> bulkApprove(
            @Parameter(description = "Bulk approval request with ticket IDs and optional comments")
            @Valid @RequestBody BulkApprovalRequest request,
            
            Authentication authentication) {
        
        String userId = authentication.getName();
        BulkOperationResultDTO result = ticketWorkflowService.bulkApprove(
            userId, 
            request.getTicketIds(), 
            request.getComments()
        );
        return ResponseEntity.ok(result);
    }
    
    /**
     * Bulk reject multiple tickets.
     * Rejects multiple PENDING tickets in a single operation with a common rejection reason.
     * Limited to a maximum of 50 tickets per request.
     * Requires Asset_Manager or Administrator role.
     * 
     * Validates Requirements: 18.2, 18.3, 18.4, 18.5, 18.6
     *
     * @param request the bulk rejection request containing ticket IDs and mandatory rejection reason
     * @param authentication the authenticated user
     * @return bulk operation result with success/failure counts and details
     */
    @PostMapping("/bulk-reject")
    @PreAuthorize("hasAnyRole('Asset_Manager', 'Administrator')")
    @Operation(
        summary = "Bulk reject tickets",
        description = "Reject multiple PENDING tickets in a single operation with a common rejection reason. " +
                     "Each ticket is validated independently. Limited to 50 tickets per request. " +
                     "Requires Asset_Manager or Administrator role. Returns success and failure counts with details of any failures."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Bulk rejection completed",
            content = @Content(schema = @Schema(implementation = BulkOperationResultDTO.class))
        ),
        @ApiResponse(responseCode = "400", description = "Validation error - more than 50 tickets, invalid ticket IDs, or missing rejection reason"),
        @ApiResponse(responseCode = "401", description = "Not authenticated"),
        @ApiResponse(responseCode = "403", description = "Insufficient permissions - requires Asset_Manager or Administrator role")
    })
    public ResponseEntity<BulkOperationResultDTO> bulkReject(
            @Parameter(description = "Bulk rejection request with ticket IDs and mandatory rejection reason")
            @Valid @RequestBody BulkRejectionRequest request,
            
            Authentication authentication) {
        
        String userId = authentication.getName();
        BulkOperationResultDTO result = ticketWorkflowService.bulkReject(
            userId, 
            request.getTicketIds(), 
            request.getRejectionReason()
        );
        return ResponseEntity.ok(result);
    }
    
    // Inner request classes
    
    /**
     * Request DTO for ticket approval.
     */
    public static class ApprovalRequest {
        
        @Schema(description = "Optional approval comments", example = "Approved for immediate deployment")
        private String comments;
        
        public ApprovalRequest() {
        }
        
        public String getComments() {
            return comments;
        }
        
        public void setComments(String comments) {
            this.comments = comments;
        }
    }
    
    /**
     * Request DTO for ticket rejection.
     */
    public static class RejectionRequest {
        
        @NotBlank(message = "Rejection reason is required")
        @Size(min = 10, max = 1000, message = "Rejection reason must be between 10 and 1000 characters")
        @Schema(description = "Mandatory rejection reason", example = "Asset is no longer available", required = true)
        private String rejectionReason;
        
        public RejectionRequest() {
        }
        
        public String getRejectionReason() {
            return rejectionReason;
        }
        
        public void setRejectionReason(String rejectionReason) {
            this.rejectionReason = rejectionReason;
        }
    }
    
    /**
     * Request DTO for priority update.
     */
    public static class PriorityUpdateRequest {
        
        @NotNull(message = "Priority is required")
        @Schema(description = "New priority level", example = "URGENT", required = true)
        private TicketPriority priority;
        
        public PriorityUpdateRequest() {
        }
        
        public TicketPriority getPriority() {
            return priority;
        }
        
        public void setPriority(TicketPriority priority) {
            this.priority = priority;
        }
    }
    
    /**
     * Request DTO for bulk approval.
     */
    public static class BulkApprovalRequest {
        
        @NotNull(message = "Ticket IDs are required")
        @Size(min = 1, max = 50, message = "Must provide between 1 and 50 ticket IDs")
        @Schema(description = "List of ticket IDs to approve (max 50)", required = true)
        private List<String> ticketIds;
        
        @Schema(description = "Optional approval comments applied to all tickets", example = "Batch approval for Q1 requests")
        private String comments;
        
        public BulkApprovalRequest() {
        }
        
        public List<String> getTicketIds() {
            return ticketIds;
        }
        
        public void setTicketIds(List<String> ticketIds) {
            this.ticketIds = ticketIds;
        }
        
        public String getComments() {
            return comments;
        }
        
        public void setComments(String comments) {
            this.comments = comments;
        }
    }
    
    /**
     * Request DTO for bulk rejection.
     */
    public static class BulkRejectionRequest {
        
        @NotNull(message = "Ticket IDs are required")
        @Size(min = 1, max = 50, message = "Must provide between 1 and 50 ticket IDs")
        @Schema(description = "List of ticket IDs to reject (max 50)", required = true)
        private List<String> ticketIds;
        
        @NotBlank(message = "Rejection reason is required")
        @Size(min = 10, max = 1000, message = "Rejection reason must be between 10 and 1000 characters")
        @Schema(description = "Mandatory rejection reason applied to all tickets", example = "Budget constraints for Q1", required = true)
        private String rejectionReason;
        
        public BulkRejectionRequest() {
        }
        
        public List<String> getTicketIds() {
            return ticketIds;
        }
        
        public void setTicketIds(List<String> ticketIds) {
            this.ticketIds = ticketIds;
        }
        
        public String getRejectionReason() {
            return rejectionReason;
        }
        
        public void setRejectionReason(String rejectionReason) {
            this.rejectionReason = rejectionReason;
        }
    }
}
