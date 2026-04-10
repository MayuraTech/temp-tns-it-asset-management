package com.company.assetmanagement.controller;

import com.company.assetmanagement.dto.AuditLogDTO;
import com.company.assetmanagement.model.Action;
import com.company.assetmanagement.service.AuditService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * REST controller for audit log operations.
 * Provides endpoints for searching and viewing audit logs.
 * 
 * All endpoints require Administrator role for access.
 */
@RestController
@RequestMapping("/api/v1/audit-logs")
@Tag(name = "Audit Logs", description = "Audit log management endpoints")
public class AuditLogController {
    
    private final AuditService auditService;
    
    public AuditLogController(AuditService auditService) {
        this.auditService = auditService;
    }
    
    /**
     * Search audit logs with filtering.
     * Supports filtering by user, action type, resource type, resource ID, and date range.
     *
     * @param userId optional user ID filter
     * @param actionType optional action type filter
     * @param resourceType optional resource type filter
     * @param resourceId optional resource ID filter
     * @param startDate optional start date filter (ISO format: yyyy-MM-dd'T'HH:mm:ss)
     * @param endDate optional end date filter (ISO format: yyyy-MM-dd'T'HH:mm:ss)
     * @param pageable pagination parameters
     * @return page of audit log entries
     */
    @GetMapping
    @PreAuthorize("hasRole('Administrator')")
    @Operation(
        summary = "Search audit logs",
        description = "Search and filter audit log entries. Requires Administrator role."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Audit logs retrieved successfully"),
        @ApiResponse(responseCode = "403", description = "Insufficient permissions")
    })
    public ResponseEntity<Page<AuditLogDTO>> searchAuditLogs(
            @Parameter(description = "Filter by user ID")
            @RequestParam(required = false) UUID userId,
            
            @Parameter(description = "Filter by action type")
            @RequestParam(required = false) Action actionType,
            
            @Parameter(description = "Filter by resource type")
            @RequestParam(required = false) String resourceType,
            
            @Parameter(description = "Filter by resource ID")
            @RequestParam(required = false) String resourceId,
            
            @Parameter(description = "Filter by start date (ISO format)")
            @RequestParam(required = false) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            
            @Parameter(description = "Filter by end date (ISO format)")
            @RequestParam(required = false) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            
            @PageableDefault(size = 20, sort = "timestamp", direction = Sort.Direction.DESC) 
            Pageable pageable) {
        
        Page<AuditLogDTO> auditLogs = auditService.searchAuditLog(
            userId, actionType, resourceType, resourceId, startDate, endDate, pageable
        );
        
        return ResponseEntity.ok(auditLogs);
    }
    
    /**
     * Get audit log entry by ID.
     *
     * @param id the audit log entry ID
     * @return the audit log entry
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('Administrator')")
    @Operation(
        summary = "Get audit log by ID",
        description = "Retrieve a specific audit log entry by its ID. Requires Administrator role."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Audit log retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Audit log not found"),
        @ApiResponse(responseCode = "403", description = "Insufficient permissions")
    })
    public ResponseEntity<AuditLogDTO> getAuditLog(
            @Parameter(description = "Audit log ID")
            @PathVariable UUID id) {
        
        AuditLogDTO auditLog = auditService.getAuditLogById(id);
        
        if (auditLog == null) {
            return ResponseEntity.notFound().build();
        }
        
        return ResponseEntity.ok(auditLog);
    }
    
    /**
     * Get audit trail for a specific resource.
     * Returns all audit log entries for the specified resource in chronological order.
     *
     * @param resourceId the resource identifier
     * @return list of audit log entries for the resource
     */
    @GetMapping("/resource/{resourceId}")
    @PreAuthorize("hasAnyRole('Administrator', 'Asset_Manager')")
    @Operation(
        summary = "Get resource audit trail",
        description = "Retrieve complete audit trail for a specific resource. Requires Administrator or Asset_Manager role."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Audit trail retrieved successfully"),
        @ApiResponse(responseCode = "403", description = "Insufficient permissions")
    })
    public ResponseEntity<List<AuditLogDTO>> getResourceAuditTrail(
            @Parameter(description = "Resource ID")
            @PathVariable String resourceId) {
        
        List<AuditLogDTO> auditTrail = auditService.getResourceAuditTrail(resourceId);
        return ResponseEntity.ok(auditTrail);
    }
}
