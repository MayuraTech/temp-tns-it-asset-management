package com.company.assetmanagement.controller;

import com.company.assetmanagement.dto.AllocationTicketRequest;
import com.company.assetmanagement.dto.DeallocationTicketRequest;
import com.company.assetmanagement.dto.TicketDTO;
import com.company.assetmanagement.dto.TicketSearchQuery;
import com.company.assetmanagement.dto.TicketStatusHistoryDTO;
import com.company.assetmanagement.model.TicketPriority;
import com.company.assetmanagement.model.TicketStatus;
import com.company.assetmanagement.model.TicketType;
import com.company.assetmanagement.service.TicketService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * REST controller for ticket management operations.
 * Provides endpoints for creating, retrieving, and searching tickets.
 * 
 * Handles allocation and de-allocation ticket workflows with validation and authorization.
 * All ticket operations are logged to the audit service for compliance.
 * 
 * Validates Requirements: 1.1-1.6, 2.1-2.5, 6.3, 8.1-8.10, 12.1-12.7
 */
@RestController
@RequestMapping("/api/v1/tickets")
@Tag(name = "Tickets", description = "Ticket management endpoints for asset allocation and de-allocation requests")
public class TicketController {
    
    private final TicketService ticketService;
    
    public TicketController(TicketService ticketService) {
        this.ticketService = ticketService;
    }
    
    /**
     * Search tickets with filtering and pagination.
     * Supports filtering by status, type, priority, requester, approver, asset, and date range.
     * 
     * Validates Requirements: 8.1-8.10
     *
     * @param statuses optional list of ticket statuses to filter by
     * @param types optional list of ticket types to filter by
     * @param priorities optional list of ticket priorities to filter by
     * @param requesterId optional requester ID to filter by
     * @param approverId optional approver ID to filter by
     * @param assetId optional asset ID to filter by
     * @param createdFrom optional start date for creation date range filter
     * @param createdTo optional end date for creation date range filter
     * @param pageable pagination parameters
     * @param authentication the authenticated user
     * @return page of tickets matching the search criteria
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(
        summary = "Search tickets",
        description = "Search and filter tickets with pagination. Users can view their own tickets, " +
                     "while Asset_Managers and Administrators can view all tickets."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Tickets retrieved successfully",
            content = @Content(schema = @Schema(implementation = Page.class))
        ),
        @ApiResponse(responseCode = "401", description = "Not authenticated"),
        @ApiResponse(responseCode = "403", description = "Insufficient permissions")
    })
    public ResponseEntity<Page<TicketDTO>> searchTickets(
            @Parameter(description = "Filter by ticket statuses")
            @RequestParam(required = false) List<TicketStatus> statuses,
            
            @Parameter(description = "Filter by ticket types")
            @RequestParam(required = false) List<TicketType> types,
            
            @Parameter(description = "Filter by ticket priorities")
            @RequestParam(required = false) List<TicketPriority> priorities,
            
            @Parameter(description = "Filter by requester ID")
            @RequestParam(required = false) UUID requesterId,
            
            @Parameter(description = "Filter by approver ID")
            @RequestParam(required = false) UUID approverId,
            
            @Parameter(description = "Filter by asset ID")
            @RequestParam(required = false) UUID assetId,
            
            @Parameter(description = "Filter by creation date from (ISO format: yyyy-MM-dd)")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate createdFrom,
            
            @Parameter(description = "Filter by creation date to (ISO format: yyyy-MM-dd)")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate createdTo,
            
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable,
            
            Authentication authentication) {
        
        String userId = authentication.getName();
        
        TicketSearchQuery query = TicketSearchQuery.builder()
            .statuses(statuses)
            .types(types)
            .priorities(priorities)
            .requesterId(requesterId)
            .approverId(approverId)
            .assetId(assetId)
            .createdFrom(createdFrom)
            .createdTo(createdTo)
            .build();
        
        Page<TicketDTO> tickets = ticketService.searchTickets(userId, query, pageable);
        return ResponseEntity.ok(tickets);
    }
    
    /**
     * Get a ticket by ID.
     * Users can only view their own tickets unless they have Asset_Manager or Administrator role.
     * 
     * Validates Requirements: 12.2, 12.3
     *
     * @param id the ticket identifier
     * @param authentication the authenticated user
     * @return the ticket DTO
     */
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(
        summary = "Get ticket by ID",
        description = "Retrieve a specific ticket by its ID. Users can only view their own tickets " +
                     "unless they have Asset_Manager or Administrator role."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Ticket retrieved successfully",
            content = @Content(schema = @Schema(implementation = TicketDTO.class))
        ),
        @ApiResponse(responseCode = "401", description = "Not authenticated"),
        @ApiResponse(responseCode = "403", description = "Insufficient permissions"),
        @ApiResponse(responseCode = "404", description = "Ticket not found")
    })
    public ResponseEntity<TicketDTO> getTicket(
            @Parameter(description = "Ticket ID")
            @PathVariable UUID id,
            
            Authentication authentication) {
        
        String userId = authentication.getName();
        
        return ticketService.getTicket(userId, id.toString())
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * Create an allocation ticket.
     * Validates the asset exists and is available for allocation.
     * Generates a unique ticket number in format TKT-YYYY-NNNNN.
     * 
     * Validates Requirements: 1.1-1.6, 12.1
     *
     * @param request the allocation ticket request
     * @param authentication the authenticated user
     * @return the created ticket DTO
     */
    @PostMapping("/allocation")
    @PreAuthorize("isAuthenticated()")
    @Operation(
        summary = "Create allocation ticket",
        description = "Create a new allocation ticket to request asset assignment to a user or location. " +
                     "Validates the asset exists and is available for allocation."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "201",
            description = "Allocation ticket created successfully",
            content = @Content(schema = @Schema(implementation = TicketDTO.class))
        ),
        @ApiResponse(responseCode = "400", description = "Validation error"),
        @ApiResponse(responseCode = "401", description = "Not authenticated"),
        @ApiResponse(responseCode = "404", description = "Asset not found"),
        @ApiResponse(responseCode = "409", description = "Asset not available for allocation")
    })
    public ResponseEntity<TicketDTO> createAllocationTicket(
            @Parameter(description = "Allocation ticket request")
            @Valid @RequestBody AllocationTicketRequest request,
            
            Authentication authentication) {
        
        String userId = authentication.getName();
        TicketDTO ticket = ticketService.createAllocationTicket(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ticket);
    }
    
    /**
     * Create a de-allocation ticket.
     * Validates the asset exists and is currently assigned.
     * Generates a unique ticket number in format TKT-YYYY-NNNNN.
     * 
     * Validates Requirements: 2.1-2.5, 12.1
     *
     * @param request the de-allocation ticket request
     * @param authentication the authenticated user
     * @return the created ticket DTO
     */
    @PostMapping("/deallocation")
    @PreAuthorize("isAuthenticated()")
    @Operation(
        summary = "Create de-allocation ticket",
        description = "Create a new de-allocation ticket to request removal of asset assignment. " +
                     "Validates the asset exists and is currently assigned."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "201",
            description = "De-allocation ticket created successfully",
            content = @Content(schema = @Schema(implementation = TicketDTO.class))
        ),
        @ApiResponse(responseCode = "400", description = "Validation error"),
        @ApiResponse(responseCode = "401", description = "Not authenticated"),
        @ApiResponse(responseCode = "404", description = "Asset not found"),
        @ApiResponse(responseCode = "409", description = "Asset is not currently assigned")
    })
    public ResponseEntity<TicketDTO> createDeallocationTicket(
            @Parameter(description = "De-allocation ticket request")
            @Valid @RequestBody DeallocationTicketRequest request,
            
            Authentication authentication) {
        
        String userId = authentication.getName();
        TicketDTO ticket = ticketService.createDeallocationTicket(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ticket);
    }
    
    /**
     * Get tickets created by the authenticated user (My Requests view).
     * Returns only tickets where the user is the requester.
     * 
     * Validates Requirements: 8.4, 8.9, 12.2
     *
     * @param pageable pagination parameters
     * @param authentication the authenticated user
     * @return page of tickets created by the user
     */
    @GetMapping("/my-requests")
    @PreAuthorize("isAuthenticated()")
    @Operation(
        summary = "Get my ticket requests",
        description = "Retrieve all tickets created by the authenticated user. " +
                     "Returns tickets in reverse chronological order."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Tickets retrieved successfully",
            content = @Content(schema = @Schema(implementation = Page.class))
        ),
        @ApiResponse(responseCode = "401", description = "Not authenticated")
    })
    public ResponseEntity<Page<TicketDTO>> getMyRequests(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable,
            
            Authentication authentication) {
        
        String userId = authentication.getName();
        Page<TicketDTO> tickets = ticketService.getMyTickets(userId, pageable);
        return ResponseEntity.ok(tickets);
    }
    
    /**
     * Get pending tickets for approval (Pending Approvals view).
     * Returns all tickets with PENDING status.
     * Requires Asset_Manager or Administrator role.
     * 
     * Validates Requirements: 8.10, 12.3, 12.4
     *
     * @param pageable pagination parameters
     * @param authentication the authenticated user
     * @return page of pending tickets
     */
    @GetMapping("/pending-approvals")
    @PreAuthorize("hasAnyRole('Asset_Manager', 'Administrator')")
    @Operation(
        summary = "Get pending approvals",
        description = "Retrieve all tickets with PENDING status awaiting approval. " +
                     "Requires Asset_Manager or Administrator role."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Pending tickets retrieved successfully",
            content = @Content(schema = @Schema(implementation = Page.class))
        ),
        @ApiResponse(responseCode = "401", description = "Not authenticated"),
        @ApiResponse(responseCode = "403", description = "Insufficient permissions - requires Asset_Manager or Administrator role")
    })
    public ResponseEntity<Page<TicketDTO>> getPendingApprovals(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.ASC)
            Pageable pageable,
            
            Authentication authentication) {
        
        String userId = authentication.getName();
        Page<TicketDTO> tickets = ticketService.getPendingApprovals(userId, pageable);
        return ResponseEntity.ok(tickets);
    }
    
    /**
     * Get status history for a ticket.
     * Returns all status transitions in chronological order.
     * 
     * Validates Requirements: 6.1, 6.2, 6.3
     *
     * @param id the ticket identifier
     * @param authentication the authenticated user
     * @return list of status history entries in chronological order
     */
    @GetMapping("/{id}/status-history")
    @PreAuthorize("isAuthenticated()")
    @Operation(
        summary = "Get ticket status history",
        description = "Retrieve complete status history for a ticket showing all status transitions. " +
                     "Returns entries in chronological order."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Status history retrieved successfully",
            content = @Content(schema = @Schema(implementation = List.class))
        ),
        @ApiResponse(responseCode = "401", description = "Not authenticated"),
        @ApiResponse(responseCode = "403", description = "Insufficient permissions"),
        @ApiResponse(responseCode = "404", description = "Ticket not found")
    })
    public ResponseEntity<List<TicketStatusHistoryDTO>> getStatusHistory(
            @Parameter(description = "Ticket ID")
            @PathVariable UUID id,
            
            Authentication authentication) {
        
        String userId = authentication.getName();
        List<TicketStatusHistoryDTO> history = ticketService.getStatusHistory(userId, id.toString());
        return ResponseEntity.ok(history);
    }
}
