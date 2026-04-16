package com.company.assetmanagement.controller;

import com.company.assetmanagement.dto.*;
import com.company.assetmanagement.model.AssignmentType;
import com.company.assetmanagement.service.AllocationService;
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
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * REST controller for allocation management operations.
 * 
 * Provides endpoints for:
 * - Creating asset assignments (to users or locations)
 * - Deallocating assets
 * - Viewing assignment history
 * - Querying assets by user or location
 * - Getting assignment statistics
 * - Exporting assignment data
 * - Bulk deallocation operations
 * 
 * Authorization:
 * - Write operations (assign, deallocate): ADMINISTRATOR or ASSET_MANAGER role
 * - Read operations (query, history): ADMINISTRATOR, ASSET_MANAGER, or VIEWER role
 */
@RestController
@RequestMapping("/api/v1")
@Validated
@Tag(name = "Allocation Management", description = "Asset allocation and assignment management endpoints")
public class AllocationController {
    
    private final AllocationService allocationService;
    
    public AllocationController(AllocationService allocationService) {
        this.allocationService = allocationService;
    }
    
    /**
     * Create a new asset assignment.
     * Routes to assignToUser or assignToLocation based on assignment type.
     *
     * @param userDetails the authenticated user
     * @param id the asset ID
     * @param request the assignment request
     * @return the created assignment DTO
     */
    @PostMapping("/assets/{id}/assignments")
    @PreAuthorize("hasAnyRole('Administrator', 'Asset_Manager')")
    @Operation(
        summary = "Create asset assignment",
        description = "Assigns an asset to a user or location. The asset must be in an assignable status (IN_USE, DEPLOYED, or STORAGE) and not currently assigned. Requires Administrator or Asset_Manager role."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "201",
            description = "Assignment created successfully",
            content = @Content(schema = @Schema(implementation = AssignmentDTO.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Validation error - invalid request data",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Insufficient permissions"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Asset not found"
        ),
        @ApiResponse(
            responseCode = "409",
            description = "Asset already assigned"
        ),
        @ApiResponse(
            responseCode = "422",
            description = "Asset not in assignable status"
        )
    })
    public ResponseEntity<AssignmentDTO> createAssignment(
            @AuthenticationPrincipal UserDetails userDetails,
            @Parameter(description = "Asset ID")
            @PathVariable UUID id,
            @Parameter(description = "Assignment request containing type and target information")
            @Valid @RequestBody AssignmentRequest request) {
        
        String userId = userDetails.getUsername();
        AssignmentDTO assignment;
        
        if (request.getAssignmentType() == AssignmentType.USER) {
            assignment = allocationService.assignToUser(userId, id, request);
        } else {
            assignment = allocationService.assignToLocation(userId, id, request);
        }
        
        return ResponseEntity.status(HttpStatus.CREATED).body(assignment);
    }
    
    /**
     * Deallocate an asset by removing its current assignment.
     *
     * @param userDetails the authenticated user
     * @param id the asset ID
     * @return 204 No Content on success
     */
    @DeleteMapping("/assets/{id}/assignments")
    @PreAuthorize("hasAnyRole('Administrator', 'Asset_Manager')")
    @Operation(
        summary = "Deallocate asset",
        description = "Removes the current assignment from an asset, making it available for reassignment. The asset must have an active assignment. Requires Administrator or Asset_Manager role."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "204",
            description = "Asset deallocated successfully"
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Insufficient permissions"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Asset not found or not currently assigned"
        )
    })
    public ResponseEntity<Void> deallocate(
            @AuthenticationPrincipal UserDetails userDetails,
            @Parameter(description = "Asset ID")
            @PathVariable UUID id) {
        
        String userId = userDetails.getUsername();
        allocationService.deallocate(userId, id);
        
        return ResponseEntity.noContent().build();
    }

    
    /**
     * Get assignment history for an asset.
     * Returns all assignment records (active and historical) ordered by date descending.
     *
     * @param id the asset ID
     * @param pageable pagination parameters
     * @return page of assignment history records
     */
    @GetMapping("/assets/{id}/assignment-history")
    @PreAuthorize("hasAnyRole('Administrator', 'Asset_Manager', 'Viewer')")
    @Operation(
        summary = "Get assignment history",
        description = "Retrieves the complete assignment history for an asset, including both active and historical assignments. Results are ordered by assignment date descending (most recent first). Requires Administrator, Asset_Manager, or Viewer role."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Assignment history retrieved successfully",
            content = @Content(schema = @Schema(implementation = Page.class))
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Insufficient permissions"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Asset not found"
        )
    })
    public ResponseEntity<Page<AssignmentHistoryDTO>> getAssignmentHistory(
            @AuthenticationPrincipal UserDetails userDetails,
            @Parameter(description = "Asset ID")
            @PathVariable UUID id,
            @PageableDefault(size = 20, sort = "assignedAt", direction = Sort.Direction.DESC)
            Pageable pageable) {
        
        Page<AssignmentHistoryDTO> history = allocationService.getAssignmentHistory(
            userDetails.getUsername(), id, pageable);
        return ResponseEntity.ok(history);
    }
    
    /**
     * Query assets assigned to a specific user.
     * Returns all assets currently assigned to the specified user.
     *
     * @param userName the user name to search for
     * @param pageable pagination parameters
     * @return page of assets assigned to the user
     */
    @GetMapping("/assignments/user/{userName}")
    @PreAuthorize("hasAnyRole('Administrator', 'Asset_Manager', 'Viewer')")
    @Operation(
        summary = "Query assets by user",
        description = "Retrieves all assets currently assigned to a specific user. Search is case-insensitive. Requires Administrator, Asset_Manager, or Viewer role."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Assets retrieved successfully",
            content = @Content(schema = @Schema(implementation = Page.class))
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Insufficient permissions"
        )
    })
    public ResponseEntity<Page<AssetDTO>> getAssetsByUser(
            @Parameter(description = "User name (case-insensitive)")
            @PathVariable String userName,
            @PageableDefault(size = 20, sort = "assignmentDate", direction = Sort.Direction.DESC)
            Pageable pageable) {
        
        Page<AssetDTO> assets = allocationService.getAssetsByUser(userName, pageable);
        return ResponseEntity.ok(assets);
    }
    
    /**
     * Query assets assigned to a specific location.
     * Returns all assets currently assigned to the specified location.
     *
     * @param location the location name to search for
     * @param pageable pagination parameters
     * @return page of assets assigned to the location
     */
    @GetMapping("/assignments/location/{location}")
    @PreAuthorize("hasAnyRole('Administrator', 'Asset_Manager', 'Viewer')")
    @Operation(
        summary = "Query assets by location",
        description = "Retrieves all assets currently assigned to a specific location. Search is case-insensitive. Requires Administrator, Asset_Manager, or Viewer role."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Assets retrieved successfully",
            content = @Content(schema = @Schema(implementation = Page.class))
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Insufficient permissions"
        )
    })
    public ResponseEntity<Page<AssetDTO>> getAssetsByLocation(
            @Parameter(description = "Location name (case-insensitive)")
            @PathVariable String location,
            @PageableDefault(size = 20, sort = "locationUpdateDate", direction = Sort.Direction.DESC)
            Pageable pageable) {
        
        Page<AssetDTO> assets = allocationService.getAssetsByLocation(location, pageable);
        return ResponseEntity.ok(assets);
    }

    
    /**
     * Get assignment statistics and metrics.
     * Returns comprehensive statistics including total assignments, assignments by type,
     * available assets, and top users/locations.
     *
     * @return assignment statistics DTO
     */
    @GetMapping("/assignments/statistics")
    @PreAuthorize("hasAnyRole('Administrator', 'Asset_Manager')")
    @Operation(
        summary = "Get assignment statistics",
        description = "Retrieves comprehensive assignment statistics including total assigned assets, assignments by type (USER vs LOCATION), available assets by status, and top 10 users and locations by assignment count. Requires Administrator or Asset_Manager role."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Statistics retrieved successfully",
            content = @Content(schema = @Schema(implementation = AssignmentStatisticsDTO.class))
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Insufficient permissions"
        )
    })
    public ResponseEntity<AssignmentStatisticsDTO> getStatistics(
            @AuthenticationPrincipal UserDetails userDetails) {
        AssignmentStatisticsDTO statistics = allocationService.getStatistics(userDetails.getUsername());
        return ResponseEntity.ok(statistics);
    }
    
    /**
     * Export assignment data to CSV format.
     * Supports optional filtering by assignment type, date range, and assigned by user.
     * Limited to 10,000 records maximum.
     *
     * @param filters optional filter parameters
     * @return CSV file as byte array
     */
    @GetMapping("/assignments/export")
    @PreAuthorize("hasAnyRole('Administrator', 'Asset_Manager')")
    @Operation(
        summary = "Export assignment data",
        description = "Exports current assignment data to CSV format. Supports filtering by assignment type, date range, and assigned by user. Export is limited to 10,000 records. Requires Administrator or Asset_Manager role."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Export generated successfully",
            content = @Content(mediaType = "text/csv")
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Export too large - apply filters to reduce size"
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Insufficient permissions"
        )
    })
    public ResponseEntity<byte[]> exportAssignments(
            @AuthenticationPrincipal UserDetails userDetails,
            @Parameter(description = "Optional filter parameters (assignmentType, dateFrom, dateTo, assignedBy)")
            @RequestParam(required = false) Map<String, Object> filters) {
        
        byte[] csvData = allocationService.exportAssignments(
            userDetails.getUsername(), 
            filters != null ? filters : Map.of()
        );
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("text/csv"));
        headers.setContentDispositionFormData("attachment", "assignments-export.csv");
        headers.setContentLength(csvData.length);
        
        return ResponseEntity.ok()
            .headers(headers)
            .body(csvData);
    }
    
    /**
     * Bulk deallocate multiple assets.
     * Processes each deallocation independently - some may succeed while others fail.
     * Maximum 50 assets per request.
     *
     * @param userDetails the authenticated user
     * @param assetIds list of asset IDs to deallocate
     * @return bulk deallocation result with success and failure lists
     */
    @PostMapping("/assignments/bulk-deallocate")
    @PreAuthorize("hasAnyRole('Administrator', 'Asset_Manager')")
    @Operation(
        summary = "Bulk deallocate assets",
        description = "Deallocates multiple assets in a single operation. Each deallocation is processed independently - some may succeed while others fail. Returns detailed results for each asset. Maximum 50 assets per request. Requires Administrator or Asset_Manager role."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Bulk deallocation completed (check response for individual results)",
            content = @Content(schema = @Schema(implementation = BulkDeallocationResult.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Request exceeds maximum bulk size (50 assets)"
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Insufficient permissions"
        )
    })
    public ResponseEntity<BulkDeallocationResult> bulkDeallocate(
            @AuthenticationPrincipal UserDetails userDetails,
            @Parameter(description = "List of asset IDs to deallocate (maximum 50)")
            @RequestBody List<UUID> assetIds) {
        
        String userId = userDetails.getUsername();
        BulkDeallocationResult result = allocationService.bulkDeallocate(userId, assetIds);
        
        return ResponseEntity.ok(result);
    }
}
