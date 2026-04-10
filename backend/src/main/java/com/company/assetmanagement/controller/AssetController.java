package com.company.assetmanagement.controller;

import com.company.assetmanagement.dto.*;
import com.company.assetmanagement.model.AssetType;
import com.company.assetmanagement.model.LifecycleStatus;
import com.company.assetmanagement.service.AssetService;
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
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * REST Controller for Asset Management operations.
 * Provides endpoints for CRUD operations, search, status management,
 * and import/export functionality.
 * 
 * <p>All endpoints require authentication and enforce role-based authorization.
 * 
 * <p><strong>Base URL:</strong> /api/v1/assets
 * 
 * <p><strong>Supported Operations:</strong>
 * <ul>
 *   <li>List assets with pagination and filtering</li>
 *   <li>Get single asset by ID</li>
 *   <li>Create new asset</li>
 *   <li>Update asset (full and partial)</li>
 *   <li>Delete asset</li>
 *   <li>Update asset status</li>
 *   <li>Search assets with advanced filters</li>
 *   <li>Export assets to CSV/JSON</li>
 *   <li>Import assets from CSV/JSON</li>
 * </ul>
 * 
 * @see AssetService
 * @see AssetDTO
 * @see AssetRequest
 */
@RestController
@RequestMapping("/api/v1/assets")
@Validated
@Tag(name = "Asset Management", description = "APIs for managing IT infrastructure assets including CRUD operations, search, status management, and import/export functionality")
public class AssetController {
    
    private final AssetService assetService;
    
    /**
     * Constructor with dependency injection.
     * 
     * @param assetService the asset service for business logic
     */
    public AssetController(AssetService assetService) {
        this.assetService = assetService;
    }
    
    /**
     * List all assets with pagination and filtering.
     * 
     * <p><strong>Endpoint:</strong> GET /api/v1/assets
     * 
     * <p><strong>Authorization:</strong> ADMINISTRATOR, ASSET_MANAGER, VIEWER
     * 
     * <p><strong>Query Parameters:</strong>
     * <ul>
     *   <li>text - Text search across name, serial number, location</li>
     *   <li>assetTypes - Filter by asset types (multiple)</li>
     *   <li>statuses - Filter by lifecycle statuses (multiple)</li>
     *   <li>location - Filter by exact location</li>
     *   <li>acquisitionDateFrom - Filter by acquisition date from</li>
     *   <li>acquisitionDateTo - Filter by acquisition date to</li>
     *   <li>page - Page number (default: 0)</li>
     *   <li>size - Page size (default: 20, max: 100)</li>
     *   <li>sort - Sort field and direction (e.g., name,asc)</li>
     * </ul>
     * 
     * @param text optional text search query
     * @param assetTypes optional list of asset types to filter by
     * @param statuses optional list of statuses to filter by
     * @param location optional location to filter by
     * @param acquisitionDateFrom optional acquisition date from
     * @param acquisitionDateTo optional acquisition date to
     * @param pageable pagination and sorting parameters
     * @return paginated list of assets
     */
    @Operation(
        summary = "List all assets with pagination and filtering",
        description = "Retrieves a paginated list of assets with optional filtering by text search, asset types, statuses, location, and acquisition date range. Supports sorting and pagination. Requires ADMINISTRATOR, ASSET_MANAGER, or VIEWER role."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Successfully retrieved asset list",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Page.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid request parameters",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Authentication required - missing or invalid token",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Insufficient permissions - requires ADMINISTRATOR, ASSET_MANAGER, or VIEWER role",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class)
            )
        )
    })
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'ASSET_MANAGER', 'VIEWER')")
    public ResponseEntity<Page<AssetDTO>> getAssets(
            @Parameter(description = "Text search across asset name, serial number, and location", example = "server")
            @RequestParam(required = false) String text,
            @Parameter(description = "Filter by asset types (can specify multiple)", example = "[\"SERVER\", \"WORKSTATION\"]")
            @RequestParam(required = false) List<AssetType> assetTypes,
            @Parameter(description = "Filter by lifecycle statuses (can specify multiple)", example = "[\"IN_USE\", \"DEPLOYED\"]")
            @RequestParam(required = false) List<LifecycleStatus> statuses,
            @Parameter(description = "Filter by exact location", example = "Data Center A")
            @RequestParam(required = false) String location,
            @Parameter(description = "Filter by acquisition date from (ISO 8601 format)", example = "2024-01-01")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate acquisitionDateFrom,
            @Parameter(description = "Filter by acquisition date to (ISO 8601 format)", example = "2024-12-31")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate acquisitionDateTo,
            @Parameter(description = "Pagination and sorting parameters (page, size, sort)", example = "page=0&size=20&sort=name,asc")
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        
        AssetSearchQuery query = AssetSearchQuery.builder()
                .text(text)
                .assetTypes(assetTypes)
                .statuses(statuses)
                .location(location)
                .acquisitionDateFrom(acquisitionDateFrom)
                .acquisitionDateTo(acquisitionDateTo)
                .build();
        
        Page<AssetDTO> assets = assetService.searchAssets(query, pageable);
        return ResponseEntity.ok(assets);
    }
    
    /**
     * Get single asset by ID.
     * 
     * <p><strong>Endpoint:</strong> GET /api/v1/assets/{id}
     * 
     * <p><strong>Authorization:</strong> ADMINISTRATOR, ASSET_MANAGER, VIEWER
     * 
     * @param id the asset UUID
     * @return the asset DTO if found
     */
    @Operation(
        summary = "Get asset by ID",
        description = "Retrieves detailed information about a specific asset by its unique identifier. Requires ADMINISTRATOR, ASSET_MANAGER, or VIEWER role."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Asset found and returned successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = AssetDTO.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid asset ID format",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Authentication required - missing or invalid token",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Insufficient permissions - requires ADMINISTRATOR, ASSET_MANAGER, or VIEWER role",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Asset not found with the specified ID",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class)
            )
        )
    })
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'ASSET_MANAGER', 'VIEWER')")
    public ResponseEntity<AssetDTO> getAsset(
            @Parameter(description = "Unique identifier of the asset (UUID format)", required = true, example = "550e8400-e29b-41d4-a716-446655440000")
            @PathVariable UUID id) {
        return assetService.getAsset(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * Create a new asset.
     * 
     * <p><strong>Endpoint:</strong> POST /api/v1/assets
     * 
     * <p><strong>Authorization:</strong> ADMINISTRATOR, ASSET_MANAGER
     * 
     * <p><strong>Request Body:</strong> AssetRequest with all required fields
     * 
     * @param authentication the authenticated user
     * @param request the asset creation request
     * @return the created asset DTO with HTTP 201 Created
     */
    @Operation(
        summary = "Create a new asset",
        description = "Creates a new asset in the system with the provided details. Serial number must be unique. Requires ADMINISTRATOR or ASSET_MANAGER role. Automatically logs the creation in the audit trail."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "Asset created successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = AssetDTO.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Validation error - invalid or missing required fields",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Authentication required - missing or invalid token",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Insufficient permissions - requires ADMINISTRATOR or ASSET_MANAGER role",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "409",
            description = "Conflict - asset with the same serial number already exists",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "422",
            description = "Unprocessable entity - business rule violation",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class)
            )
        )
    })
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'ASSET_MANAGER')")
    public ResponseEntity<AssetDTO> createAsset(
            Authentication authentication,
            @Parameter(description = "Asset creation request with all required fields", required = true)
            @Valid @RequestBody AssetRequest request) {
        
        String userId = getUserId(authentication);
        AssetDTO asset = assetService.createAsset(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(asset);
    }
    
    /**
     * Update an entire asset (full update).
     * 
     * <p><strong>Endpoint:</strong> PUT /api/v1/assets/{id}
     * 
     * <p><strong>Authorization:</strong> ADMINISTRATOR, ASSET_MANAGER
     * 
     * <p><strong>Request Body:</strong> AssetRequest with all fields
     * 
     * @param id the asset UUID
     * @param authentication the authenticated user
     * @param request the asset update request
     * @return the updated asset DTO
     */
    @Operation(
        summary = "Update an entire asset",
        description = "Performs a full update of an existing asset. All fields must be provided. Serial number cannot be changed. Requires ADMINISTRATOR or ASSET_MANAGER role. Changes are logged in the audit trail."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Asset updated successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = AssetDTO.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Validation error - invalid or missing required fields",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Authentication required - missing or invalid token",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Insufficient permissions - requires ADMINISTRATOR or ASSET_MANAGER role",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Asset not found with the specified ID",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "422",
            description = "Unprocessable entity - business rule violation (e.g., invalid status transition)",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class)
            )
        )
    })
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'ASSET_MANAGER')")
    public ResponseEntity<AssetDTO> updateAsset(
            @Parameter(description = "Unique identifier of the asset to update (UUID format)", required = true, example = "550e8400-e29b-41d4-a716-446655440000")
            @PathVariable UUID id,
            Authentication authentication,
            @Parameter(description = "Asset update request with all fields", required = true)
            @Valid @RequestBody AssetRequest request) {
        
        String userId = getUserId(authentication);
        AssetDTO asset = assetService.updateAsset(userId, id, request);
        return ResponseEntity.ok(asset);
    }
    
    /**
     * Partially update an asset.
     * 
     * <p><strong>Endpoint:</strong> PATCH /api/v1/assets/{id}
     * 
     * <p><strong>Authorization:</strong> ADMINISTRATOR, ASSET_MANAGER
     * 
     * <p><strong>Request Body:</strong> Map of field names to values
     * 
     * <p><strong>Note:</strong> This endpoint accepts a map of field updates.
     * Only provided fields will be updated. The implementation converts the map
     * to an AssetRequest and calls the update service method.
     * 
     * @param id the asset UUID
     * @param authentication the authenticated user
     * @param updates map of field names to new values
     * @return the updated asset DTO
     */
    @Operation(
        summary = "Partially update an asset",
        description = "Performs a partial update of an existing asset. Only the provided fields will be updated. Immutable fields (id, serialNumber, createdAt, createdBy) are ignored. Requires ADMINISTRATOR or ASSET_MANAGER role. Changes are logged in the audit trail."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Asset updated successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = AssetDTO.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Validation error - invalid field values",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Authentication required - missing or invalid token",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Insufficient permissions - requires ADMINISTRATOR or ASSET_MANAGER role",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Asset not found with the specified ID",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "422",
            description = "Unprocessable entity - business rule violation",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class)
            )
        )
    })
    @PatchMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'ASSET_MANAGER')")
    public ResponseEntity<AssetDTO> patchAsset(
            @Parameter(description = "Unique identifier of the asset to update (UUID format)", required = true, example = "550e8400-e29b-41d4-a716-446655440000")
            @PathVariable UUID id,
            Authentication authentication,
            @Parameter(description = "Map of field names to new values (only provided fields will be updated)", required = true)
            @RequestBody Map<String, Object> updates) {
        
        String userId = getUserId(authentication);
        
        // Get existing asset to merge with partial updates
        AssetDTO existingAsset = assetService.getAsset(id)
                .orElseThrow(() -> new com.company.assetmanagement.exception.ResourceNotFoundException("Asset", id.toString()));
        
        // Build AssetRequest from existing asset and apply updates
        AssetRequest request = buildRequestFromUpdates(existingAsset, updates);
        
        AssetDTO asset = assetService.updateAsset(userId, id, request);
        return ResponseEntity.ok(asset);
    }
    
    /**
     * Delete an asset.
     * 
     * <p><strong>Endpoint:</strong> DELETE /api/v1/assets/{id}
     * 
     * <p><strong>Authorization:</strong> ADMINISTRATOR only
     * 
     * @param id the asset UUID
     * @param authentication the authenticated user
     * @return HTTP 204 No Content on success
     */
    @Operation(
        summary = "Delete an asset",
        description = "Permanently deletes an asset from the system. This operation cannot be undone. Requires ADMINISTRATOR role. Deletion is logged in the audit trail."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "204",
            description = "Asset deleted successfully (no content returned)"
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid asset ID format",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Authentication required - missing or invalid token",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Insufficient permissions - requires ADMINISTRATOR role",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Asset not found with the specified ID",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class)
            )
        )
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ResponseEntity<Void> deleteAsset(
            @Parameter(description = "Unique identifier of the asset to delete (UUID format)", required = true, example = "550e8400-e29b-41d4-a716-446655440000")
            @PathVariable UUID id,
            Authentication authentication) {
        
        String userId = getUserId(authentication);
        assetService.deleteAsset(userId, id);
        return ResponseEntity.noContent().build();
    }
    
    /**
     * Update asset lifecycle status.
     * 
     * <p><strong>Endpoint:</strong> PATCH /api/v1/assets/{id}/status
     * 
     * <p><strong>Authorization:</strong> ADMINISTRATOR, ASSET_MANAGER
     * 
     * <p><strong>Request Body:</strong> StatusUpdateRequest with new status
     * 
     * @param id the asset UUID
     * @param authentication the authenticated user
     * @param request the status update request
     * @return the updated asset DTO
     */
    @Operation(
        summary = "Update asset lifecycle status",
        description = "Updates the lifecycle status of an asset. Status transitions are validated according to business rules (e.g., cannot transition from RETIRED). Requires ADMINISTRATOR or ASSET_MANAGER role. Status changes are logged in the audit trail."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Asset status updated successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = AssetDTO.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Validation error - invalid status value",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Authentication required - missing or invalid token",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Insufficient permissions - requires ADMINISTRATOR or ASSET_MANAGER role",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Asset not found with the specified ID",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "422",
            description = "Invalid status transition (e.g., cannot transition from RETIRED to IN_USE)",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class)
            )
        )
    })
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'ASSET_MANAGER')")
    public ResponseEntity<AssetDTO> updateStatus(
            @Parameter(description = "Unique identifier of the asset (UUID format)", required = true, example = "550e8400-e29b-41d4-a716-446655440000")
            @PathVariable UUID id,
            Authentication authentication,
            @Parameter(description = "Status update request with new lifecycle status", required = true)
            @Valid @RequestBody StatusUpdateRequest request) {
        
        String userId = getUserId(authentication);
        AssetDTO asset = assetService.updateStatus(userId, id, request.getNewStatus());
        return ResponseEntity.ok(asset);
    }
    
    /**
     * Advanced search for assets.
     * 
     * <p><strong>Endpoint:</strong> GET /api/v1/assets/search
     * 
     * <p><strong>Authorization:</strong> ADMINISTRATOR, ASSET_MANAGER, VIEWER
     * 
     * <p><strong>Query Parameters:</strong> Same as GET /api/v1/assets
     * 
     * <p><strong>Note:</strong> This endpoint provides the same functionality as
     * GET /api/v1/assets but with a more explicit URL for advanced search operations.
     * 
     * @param text optional text search query
     * @param assetTypes optional list of asset types to filter by
     * @param statuses optional list of statuses to filter by
     * @param location optional location to filter by
     * @param acquisitionDateFrom optional acquisition date from
     * @param acquisitionDateTo optional acquisition date to
     * @param pageable pagination and sorting parameters
     * @return paginated list of matching assets
     */
    @Operation(
        summary = "Advanced search for assets",
        description = "Performs an advanced search for assets with the same filtering capabilities as the list endpoint. Provides a more explicit URL for search operations. Supports text search, filtering by multiple criteria, pagination, and sorting. Requires ADMINISTRATOR, ASSET_MANAGER, or VIEWER role."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Search completed successfully, matching assets returned",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Page.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid search parameters",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Authentication required - missing or invalid token",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Insufficient permissions - requires ADMINISTRATOR, ASSET_MANAGER, or VIEWER role",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class)
            )
        )
    })
    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'ASSET_MANAGER', 'VIEWER')")
    public ResponseEntity<Page<AssetDTO>> searchAssets(
            @Parameter(description = "Text search across asset name, serial number, and location", example = "server")
            @RequestParam(required = false) String text,
            @Parameter(description = "Filter by asset types (can specify multiple)", example = "[\"SERVER\", \"WORKSTATION\"]")
            @RequestParam(required = false) List<AssetType> assetTypes,
            @Parameter(description = "Filter by lifecycle statuses (can specify multiple)", example = "[\"IN_USE\", \"DEPLOYED\"]")
            @RequestParam(required = false) List<LifecycleStatus> statuses,
            @Parameter(description = "Filter by exact location", example = "Data Center A")
            @RequestParam(required = false) String location,
            @Parameter(description = "Filter by acquisition date from (ISO 8601 format)", example = "2024-01-01")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate acquisitionDateFrom,
            @Parameter(description = "Filter by acquisition date to (ISO 8601 format)", example = "2024-12-31")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate acquisitionDateTo,
            @Parameter(description = "Pagination and sorting parameters", example = "page=0&size=20&sort=name,asc")
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        
        AssetSearchQuery query = AssetSearchQuery.builder()
                .text(text)
                .assetTypes(assetTypes)
                .statuses(statuses)
                .location(location)
                .acquisitionDateFrom(acquisitionDateFrom)
                .acquisitionDateTo(acquisitionDateTo)
                .build();
        
        Page<AssetDTO> assets = assetService.searchAssets(query, pageable);
        return ResponseEntity.ok(assets);
    }
    
    /**
     * Export assets to specified format (CSV or JSON).
     * 
     * <p><strong>Endpoint:</strong> GET /api/v1/assets/export
     * 
     * <p><strong>Authorization:</strong> ADMINISTRATOR, ASSET_MANAGER
     * 
     * <p><strong>Query Parameters:</strong>
     * <ul>
     *   <li>format - Export format (CSV or JSON, default: CSV)</li>
     *   <li>text - Optional text search to filter exports</li>
     *   <li>assetTypes - Optional asset types to filter exports</li>
     *   <li>statuses - Optional statuses to filter exports</li>
     *   <li>location - Optional location to filter exports</li>
     * </ul>
     * 
     * @param format the export format (CSV or JSON)
     * @param text optional text search query
     * @param assetTypes optional list of asset types to filter by
     * @param statuses optional list of statuses to filter by
     * @param location optional location to filter by
     * @return the exported file as byte array with appropriate headers
     */
    @Operation(
        summary = "Export assets to CSV or JSON",
        description = "Exports assets to the specified format (CSV or JSON). Supports filtering to export only specific assets. Maximum 100,000 assets per export. Requires ADMINISTRATOR or ASSET_MANAGER role. Export operation is logged in the audit trail."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Assets exported successfully, file returned as byte array",
            content = @Content(
                mediaType = "application/octet-stream"
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid export parameters or format",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Authentication required - missing or invalid token",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Insufficient permissions - requires ADMINISTRATOR or ASSET_MANAGER role",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class)
            )
        )
    })
    @GetMapping("/export")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'ASSET_MANAGER')")
    public ResponseEntity<byte[]> exportAssets(
            @Parameter(description = "Export format (CSV or JSON)", example = "CSV")
            @RequestParam(defaultValue = "CSV") ExportFormat format,
            @Parameter(description = "Optional text search to filter exported assets", example = "server")
            @RequestParam(required = false) String text,
            @Parameter(description = "Optional asset types to filter exported assets", example = "[\"SERVER\", \"WORKSTATION\"]")
            @RequestParam(required = false) List<AssetType> assetTypes,
            @Parameter(description = "Optional statuses to filter exported assets", example = "[\"IN_USE\", \"DEPLOYED\"]")
            @RequestParam(required = false) List<LifecycleStatus> statuses,
            @Parameter(description = "Optional location to filter exported assets", example = "Data Center A")
            @RequestParam(required = false) String location) {
        
        AssetSearchQuery query = AssetSearchQuery.builder()
                .text(text)
                .assetTypes(assetTypes)
                .statuses(statuses)
                .location(location)
                .build();
        
        ExportResult result = assetService.exportAssets(format, query);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(result.getContentType()));
        headers.setContentDispositionFormData("attachment", result.getFileName());
        headers.setContentLength(result.getFileSize());
        
        return ResponseEntity.ok()
                .headers(headers)
                .body(result.getData());
    }
    
    /**
     * Import assets from file (CSV or JSON).
     * 
     * <p><strong>Endpoint:</strong> POST /api/v1/assets/import
     * 
     * <p><strong>Authorization:</strong> ADMINISTRATOR, ASSET_MANAGER
     * 
     * <p><strong>Request Parameters:</strong>
     * <ul>
     *   <li>format - Import format (CSV or JSON)</li>
     *   <li>file - The file to import (multipart/form-data)</li>
     * </ul>
     * 
     * <p><strong>Constraints:</strong>
     * <ul>
     *   <li>Maximum file size: 10MB</li>
     *   <li>Maximum records per import: 10,000</li>
     * </ul>
     * 
     * @param authentication the authenticated user
     * @param format the import format (CSV or JSON)
     * @param file the uploaded file
     * @return import result with success/failure counts and errors
     */
    @Operation(
        summary = "Import assets from CSV or JSON file",
        description = "Imports assets from an uploaded file in CSV or JSON format. Validates each record and reports success/failure counts. Maximum file size is 10MB and maximum 10,000 records per import. Duplicate serial numbers are rejected. Requires ADMINISTRATOR or ASSET_MANAGER role. Import operation is logged in the audit trail."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Import completed (may include partial failures - check ImportResult for details)",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ImportResult.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid file format, file too large, or file reading error",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Authentication required - missing or invalid token",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Insufficient permissions - requires ADMINISTRATOR or ASSET_MANAGER role",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "422",
            description = "Too many records in file (exceeds 10,000 limit)",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class)
            )
        )
    })
    @PostMapping("/import")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'ASSET_MANAGER')")
    public ResponseEntity<ImportResult> importAssets(
            Authentication authentication,
            @Parameter(description = "Import format (CSV or JSON)", required = true, example = "CSV")
            @RequestParam ImportFormat format,
            @Parameter(description = "File to import (multipart/form-data, max 10MB)", required = true)
            @RequestParam("file") MultipartFile file) {
        
        String userId = getUserId(authentication);
        
        try {
            byte[] data = file.getBytes();
            ImportResult result = assetService.importAssets(userId, format, data);
            
            // Return 200 OK for successful import, even if some records failed
            // The ImportResult contains detailed success/failure information
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            // Handle file reading errors
            ImportResult errorResult = ImportResult.builder()
                    .successCount(0)
                    .failureCount(0)
                    .totalRecords(0)
                    .message("Failed to read import file: " + e.getMessage())
                    .build();
            
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResult);
        }
    }
    
    /**
     * Extract user ID from authentication object.
     * 
     * @param authentication the authentication object
     * @return the user ID as string
     */
    private String getUserId(Authentication authentication) {
        if (authentication == null || authentication.getPrincipal() == null) {
            throw new IllegalStateException("Authentication is required");
        }
        
        Object principal = authentication.getPrincipal();
        
        if (principal instanceof UserDetails) {
            return ((UserDetails) principal).getUsername();
        }
        
        return principal.toString();
    }
    
    /**
     * Build AssetRequest from existing asset and partial updates.
     * 
     * @param existingAsset the existing asset DTO
     * @param updates map of field names to new values
     * @return AssetRequest with merged values
     */
    private AssetRequest buildRequestFromUpdates(AssetDTO existingAsset, Map<String, Object> updates) {
        AssetRequest.Builder builder = AssetRequest.builder()
                .assetType(existingAsset.getAssetType())
                .name(existingAsset.getName())
                .serialNumber(existingAsset.getSerialNumber())
                .acquisitionDate(existingAsset.getAcquisitionDate())
                .status(existingAsset.getStatus())
                .location(existingAsset.getLocation())
                .assignedUser(existingAsset.getAssignedUser())
                .assignedUserEmail(existingAsset.getAssignedUserEmail())
                .assignmentDate(existingAsset.getAssignmentDate())
                .locationUpdateDate(existingAsset.getLocationUpdateDate())
                .notes(existingAsset.getNotes())
                .customFields(existingAsset.getCustomFields());
        
        // Apply updates from the map
        updates.forEach((key, value) -> {
            switch (key) {
                case "assetType":
                    if (value instanceof String) {
                        builder.assetType(AssetType.valueOf((String) value));
                    } else if (value instanceof AssetType) {
                        builder.assetType((AssetType) value);
                    }
                    break;
                case "name":
                    builder.name((String) value);
                    break;
                case "status":
                    if (value instanceof String) {
                        builder.status(LifecycleStatus.valueOf((String) value));
                    } else if (value instanceof LifecycleStatus) {
                        builder.status((LifecycleStatus) value);
                    }
                    break;
                case "location":
                    builder.location((String) value);
                    break;
                case "assignedUser":
                    builder.assignedUser((String) value);
                    break;
                case "assignedUserEmail":
                    builder.assignedUserEmail((String) value);
                    break;
                case "notes":
                    builder.notes((String) value);
                    break;
                case "customFields":
                    builder.customFields((String) value);
                    break;
                // Immutable fields are ignored: id, serialNumber, createdAt, createdBy
            }
        });
        
        return builder.build();
    }
    
    /**
     * Get asset statistics for dashboard display.
     * 
     * <p><strong>Endpoint:</strong> GET /api/v1/assets/stats
     * 
     * <p><strong>Authorization:</strong> ADMINISTRATOR, ASSET_MANAGER, VIEWER
     * 
     * <p>Returns quick statistics about the asset inventory including:
     * <ul>
     *   <li>Total number of assets in the system</li>
     *   <li>Number of assets currently in use</li>
     *   <li>Timestamp when statistics were calculated</li>
     * </ul>
     * 
     * <p>Statistics are calculated using efficient database aggregation queries
     * and should complete within 500 milliseconds.
     * 
     * <p><strong>Requirements:</strong>
     * <ul>
     *   <li>Requirement 22: Dashboard and Quick Stats</li>
     *   <li>Requirement 12: Performance Requirements</li>
     * </ul>
     * 
     * @return ResponseEntity containing asset statistics
     */
    @Operation(
        summary = "Get asset statistics",
        description = "Retrieves quick statistics about the asset inventory for dashboard display. " +
                     "Returns total assets, assets in use, and calculation timestamp."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Statistics retrieved successfully",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = AssetStatsDTO.class)
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Authentication required",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Insufficient permissions",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Internal server error",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        )
    })
    @GetMapping("/stats")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'ASSET_MANAGER', 'VIEWER')")
    public ResponseEntity<AssetStatsDTO> getAssetStats() {
        AssetStatsDTO stats = assetService.getAssetStats();
        return ResponseEntity.ok(stats);
    }
}
