package com.company.assetmanagement.controller;

import com.company.assetmanagement.dto.TicketDTO;
import com.company.assetmanagement.dto.TicketMetricsDTO;
import com.company.assetmanagement.model.TicketPriority;
import com.company.assetmanagement.model.TicketStatus;
import com.company.assetmanagement.model.TicketType;
import com.company.assetmanagement.service.TicketMetricsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

/**
 * REST controller for ticket metrics and analytics operations.
 * Provides endpoints for generating aggregated statistics about ticket processing,
 * performance metrics, and filtered ticket retrieval by various criteria.
 * 
 * All metrics operations support optional date range filtering for time-based analysis.
 * Restricted to Asset_Manager and Administrator roles for security.
 * 
 * Validates Requirements: 10.1-10.7
 */
@RestController
@RequestMapping("/api/v1/tickets/metrics")
@Tag(name = "Ticket Metrics", description = "Ticket metrics and analytics endpoints for monitoring system usage")
public class TicketMetricsController {
    
    private final TicketMetricsService ticketMetricsService;
    
    public TicketMetricsController(TicketMetricsService ticketMetricsService) {
        this.ticketMetricsService = ticketMetricsService;
    }
    
    /**
     * Generate comprehensive ticket metrics with optional date range filtering.
     * 
     * Calculates total tickets, tickets by status/type/priority, average approval/completion times,
     * and approval/rejection rates.
     * 
     * Validates Requirements: 10.1-10.7
     *
     * @param fromDate optional start date for filtering (inclusive)
     * @param toDate optional end date for filtering (inclusive)
     * @return comprehensive ticket metrics
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'ASSET_MANAGER')")
    @Operation(
        summary = "Generate ticket metrics",
        description = "Generate comprehensive ticket metrics including counts by status/type/priority, " +
                     "average approval and completion times, and approval/rejection rates. " +
                     "Supports optional date range filtering. Requires ADMINISTRATOR or ASSET_MANAGER role."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Metrics generated successfully",
            content = @Content(schema = @Schema(implementation = TicketMetricsDTO.class))
        ),
        @ApiResponse(responseCode = "401", description = "Not authenticated"),
        @ApiResponse(responseCode = "403", description = "Insufficient permissions - requires ADMINISTRATOR or ASSET_MANAGER role")
    })
    public ResponseEntity<TicketMetricsDTO> getMetrics(
            @Parameter(description = "Start date for filtering (inclusive, format: yyyy-MM-dd)")
            @RequestParam(required = false) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) 
            LocalDate fromDate,
            
            @Parameter(description = "End date for filtering (inclusive, format: yyyy-MM-dd)")
            @RequestParam(required = false) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) 
            LocalDate toDate) {
        
        TicketMetricsDTO metrics = ticketMetricsService.generateMetrics(fromDate, toDate);
        return ResponseEntity.ok(metrics);
    }
    
    /**
     * Get tickets filtered by status with pagination.
     * 
     * Supports optional date range filtering for time-based analysis.
     * 
     * Validates Requirement 10.1
     *
     * @param status the ticket status to filter by
     * @param fromDate optional start date for filtering (inclusive)
     * @param toDate optional end date for filtering (inclusive)
     * @param pageable pagination parameters
     * @return page of tickets with the specified status
     */
    @GetMapping("/by-status")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'ASSET_MANAGER')")
    @Operation(
        summary = "Get tickets by status",
        description = "Retrieve tickets filtered by status with pagination. " +
                     "Supports optional date range filtering. Requires ADMINISTRATOR or ASSET_MANAGER role."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Tickets retrieved successfully",
            content = @Content(schema = @Schema(implementation = Page.class))
        ),
        @ApiResponse(responseCode = "400", description = "Invalid status value"),
        @ApiResponse(responseCode = "401", description = "Not authenticated"),
        @ApiResponse(responseCode = "403", description = "Insufficient permissions - requires ADMINISTRATOR or ASSET_MANAGER role")
    })
    public ResponseEntity<Page<TicketDTO>> getTicketsByStatus(
            @Parameter(description = "Ticket status to filter by (PENDING, APPROVED, REJECTED, COMPLETED, CANCELLED)", required = true)
            @RequestParam TicketStatus status,
            
            @Parameter(description = "Start date for filtering (inclusive, format: yyyy-MM-dd)")
            @RequestParam(required = false) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) 
            LocalDate fromDate,
            
            @Parameter(description = "End date for filtering (inclusive, format: yyyy-MM-dd)")
            @RequestParam(required = false) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) 
            LocalDate toDate,
            
            @Parameter(description = "Pagination parameters (page, size, sort)")
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) 
            Pageable pageable) {
        
        Page<TicketDTO> tickets = ticketMetricsService.getTicketsByStatus(status, fromDate, toDate, pageable);
        return ResponseEntity.ok(tickets);
    }
    
    /**
     * Get tickets filtered by type with pagination.
     * 
     * Supports optional date range filtering for time-based analysis.
     * 
     * Validates Requirement 10.1
     *
     * @param type the ticket type to filter by
     * @param fromDate optional start date for filtering (inclusive)
     * @param toDate optional end date for filtering (inclusive)
     * @param pageable pagination parameters
     * @return page of tickets with the specified type
     */
    @GetMapping("/by-type")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'ASSET_MANAGER')")
    @Operation(
        summary = "Get tickets by type",
        description = "Retrieve tickets filtered by type (ALLOCATION or DEALLOCATION) with pagination. " +
                     "Supports optional date range filtering. Requires ADMINISTRATOR or ASSET_MANAGER role."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Tickets retrieved successfully",
            content = @Content(schema = @Schema(implementation = Page.class))
        ),
        @ApiResponse(responseCode = "400", description = "Invalid type value"),
        @ApiResponse(responseCode = "401", description = "Not authenticated"),
        @ApiResponse(responseCode = "403", description = "Insufficient permissions - requires ADMINISTRATOR or ASSET_MANAGER role")
    })
    public ResponseEntity<Page<TicketDTO>> getTicketsByType(
            @Parameter(description = "Ticket type to filter by (ALLOCATION, DEALLOCATION)", required = true)
            @RequestParam TicketType type,
            
            @Parameter(description = "Start date for filtering (inclusive, format: yyyy-MM-dd)")
            @RequestParam(required = false) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) 
            LocalDate fromDate,
            
            @Parameter(description = "End date for filtering (inclusive, format: yyyy-MM-dd)")
            @RequestParam(required = false) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) 
            LocalDate toDate,
            
            @Parameter(description = "Pagination parameters (page, size, sort)")
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) 
            Pageable pageable) {
        
        Page<TicketDTO> tickets = ticketMetricsService.getTicketsByType(type, fromDate, toDate, pageable);
        return ResponseEntity.ok(tickets);
    }
    
    /**
     * Get tickets filtered by priority with pagination.
     * 
     * Supports optional date range filtering for time-based analysis.
     * 
     * Validates Requirement 10.1
     *
     * @param priority the ticket priority to filter by
     * @param fromDate optional start date for filtering (inclusive)
     * @param toDate optional end date for filtering (inclusive)
     * @param pageable pagination parameters
     * @return page of tickets with the specified priority
     */
    @GetMapping("/by-priority")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'ASSET_MANAGER')")
    @Operation(
        summary = "Get tickets by priority",
        description = "Retrieve tickets filtered by priority (LOW, MEDIUM, HIGH, CRITICAL) with pagination. " +
                     "Supports optional date range filtering. Requires ADMINISTRATOR or ASSET_MANAGER role."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Tickets retrieved successfully",
            content = @Content(schema = @Schema(implementation = Page.class))
        ),
        @ApiResponse(responseCode = "400", description = "Invalid priority value"),
        @ApiResponse(responseCode = "401", description = "Not authenticated"),
        @ApiResponse(responseCode = "403", description = "Insufficient permissions - requires ADMINISTRATOR or ASSET_MANAGER role")
    })
    public ResponseEntity<Page<TicketDTO>> getTicketsByPriority(
            @Parameter(description = "Ticket priority to filter by (LOW, MEDIUM, HIGH, CRITICAL)", required = true)
            @RequestParam TicketPriority priority,
            
            @Parameter(description = "Start date for filtering (inclusive, format: yyyy-MM-dd)")
            @RequestParam(required = false) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) 
            LocalDate fromDate,
            
            @Parameter(description = "End date for filtering (inclusive, format: yyyy-MM-dd)")
            @RequestParam(required = false) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) 
            LocalDate toDate,
            
            @Parameter(description = "Pagination parameters (page, size, sort)")
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) 
            Pageable pageable) {
        
        Page<TicketDTO> tickets = ticketMetricsService.getTicketsByPriority(priority, fromDate, toDate, pageable);
        return ResponseEntity.ok(tickets);
    }
}
