package com.company.assetmanagement.service;

import com.company.assetmanagement.dto.*;
import com.company.assetmanagement.exception.DuplicateSerialNumberException;
import com.company.assetmanagement.exception.InsufficientPermissionsException;
import com.company.assetmanagement.exception.InvalidStatusTransitionException;
import com.company.assetmanagement.exception.ResourceNotFoundException;
import com.company.assetmanagement.exception.ValidationException;
import com.company.assetmanagement.model.Action;
import com.company.assetmanagement.model.Asset;
import com.company.assetmanagement.model.LifecycleStatus;
import com.company.assetmanagement.repository.AssetRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Implementation of AssetService interface.
 * Provides business logic for asset management operations including
 * CRUD operations, lifecycle management, and integration with audit logging.
 * 
 * <p>All operations follow the standard service layer pattern:
 * <ol>
 *   <li>Authorization check</li>
 *   <li>Validation</li>
 *   <li>Business rules enforcement</li>
 *   <li>Entity creation/update</li>
 *   <li>Persistence</li>
 *   <li>Audit logging</li>
 *   <li>DTO mapping and return</li>
 * </ol>
 * 
 * @see AssetService
 * @see Asset
 * @see AssetDTO
 */
@Service
@Transactional
public class AssetServiceImpl implements AssetService {
    
    private final AssetRepository assetRepository;
    private final AuditService auditService;
    private final AuthorizationService authorizationService;
    private final AssetValidationService validationService;
    
    /**
     * Constructor with dependency injection.
     * 
     * @param assetRepository repository for asset persistence
     * @param auditService service for audit logging
     * @param authorizationService service for authorization checks
     * @param validationService service for asset validation
     */
    public AssetServiceImpl(
            AssetRepository assetRepository,
            AuditService auditService,
            AuthorizationService authorizationService,
            AssetValidationService validationService) {
        this.assetRepository = assetRepository;
        this.auditService = auditService;
        this.authorizationService = authorizationService;
        this.validationService = validationService;
    }
    
    /**
     * Creates a new asset in the system.
     * 
     * <p>Implementation steps:
     * <ol>
     *   <li>Authorization check - Verify user has CREATE_ASSET permission</li>
     *   <li>Validation - Validate asset request data</li>
     *   <li>Business rule - Check serial number uniqueness</li>
     *   <li>Create entity - Map request to entity and set audit fields</li>
     *   <li>Persist - Save entity to database</li>
     *   <li>Audit logging - Log asset creation event</li>
     *   <li>Return DTO - Map entity to DTO and return</li>
     * </ol>
     * 
     * @param userId the ID of the user creating the asset (must not be null)
     * @param request the asset creation request containing all asset details (must not be null)
     * @return the created asset as DTO with generated ID and timestamps
     * @throws InsufficientPermissionsException if user lacks CREATE_ASSET permission
     * @throws DuplicateSerialNumberException if an asset with the same serial number already exists
     * @throws ValidationException if request data fails validation
     * @throws IllegalArgumentException if userId or request is null
     */
    @Override
    public AssetDTO createAsset(String userId, AssetRequest request) {
        // Validate input parameters
        if (userId == null || userId.isBlank()) {
            throw new IllegalArgumentException("User ID cannot be null or empty");
        }
        if (request == null) {
            throw new IllegalArgumentException("Asset request cannot be null");
        }
        
        // Step 1: Authorization check
        if (!authorizationService.hasPermission(userId, Action.CREATE_ASSET)) {
            throw new InsufficientPermissionsException(userId, Action.CREATE_ASSET.name());
        }
        
        // Step 2: Validation
        validationService.validateAssetRequest(request);
        
        // Step 3: Business rule - Check serial number uniqueness
        if (assetRepository.existsBySerialNumber(request.getSerialNumber())) {
            throw new DuplicateSerialNumberException(request.getSerialNumber());
        }
        
        // Step 4: Create entity
        Asset asset = AssetMapper.toEntity(request);
        UUID userUuid = authorizationService.resolveActorUuid(userId);
        asset.setCreatedBy(userUuid);
        asset.setUpdatedBy(userUuid);
        asset.setReadOnly(false);
        
        // Step 5: Persist
        Asset savedAsset = assetRepository.save(asset);
        
        // Step 6: Audit logging
        try {
            auditService.logEvent(AuditEventDTO.builder()
                .userId(userUuid)
                .actionType(Action.CREATE_ASSET)
                .resourceType("ASSET")
                .resourceId(savedAsset.getId().toString())
                .build());
        } catch (Exception e) {
            // Log error but don't fail the operation
            // Audit logging failures should not block asset creation
            System.err.println("Failed to log audit event: " + e.getMessage());
        }
        
        // Step 7: Return DTO
        return AssetMapper.toDTO(savedAsset);
    }
    
    /**
     * Updates an existing asset with new information.
     * 
     * <p>Implementation steps:
     * <ol>
     *   <li>Validate input parameters (userId, assetId, request)</li>
     *   <li>Retrieve existing asset by ID (throw ResourceNotFoundException if not found)</li>
     *   <li>Validate the update request using AssetValidationService</li>
     *   <li>Check if asset is readOnly (status = RETIRED) - only allow notes field updates for retired assets</li>
     *   <li>Protect immutable fields: id, serialNumber, createdAt, createdBy (these should never be updated)</li>
     *   <li>Update mutable fields: name, location, assignedUser, assignedUserEmail, assignmentDate, locationUpdateDate, status, notes, customFields</li>
     *   <li>Set updatedBy to userId and updatedAt will be set automatically by JPA</li>
     *   <li>Track field-level changes for audit logging (compare old vs new values)</li>
     *   <li>Save updated asset to repository</li>
     *   <li>Log audit event with field changes</li>
     *   <li>Return updated asset as DTO</li>
     * </ol>
     * 
     * @param userId the ID of the user updating the asset (must not be null)
     * @param assetId the UUID of the asset to update (must not be null)
     * @param request the asset update request containing new values (must not be null)
     * @return the updated asset as DTO with new values and updated timestamp
     * @throws ResourceNotFoundException if asset with given ID does not exist
     * @throws InsufficientPermissionsException if user lacks UPDATE_ASSET permission
     * @throws ValidationException if request data fails validation
     * @throws IllegalStateException if asset is read-only (retired status)
     * @throws IllegalArgumentException if userId, assetId, or request is null
     */
    @Override
    public AssetDTO updateAsset(String userId, UUID assetId, AssetRequest request) {
        // Step 1: Validate input parameters
        if (userId == null || userId.isBlank()) {
            throw new IllegalArgumentException("User ID cannot be null or empty");
        }
        if (assetId == null) {
            throw new IllegalArgumentException("Asset ID cannot be null");
        }
        if (request == null) {
            throw new IllegalArgumentException("Asset request cannot be null");
        }
        
        // Step 2: Retrieve existing asset by ID
        Asset existingAsset = assetRepository.findById(assetId)
            .orElseThrow(() -> new ResourceNotFoundException("Asset", assetId.toString()));
        
        // Step 3: Validate the update request
        validationService.validateAssetRequest(request);
        
        // Step 4: Check readOnly flag (reject updates for retired assets except notes)
        if (existingAsset.isReadOnly()) {
            // Only allow notes field updates for retired assets
            boolean onlyNotesChanged = 
                (request.getNotes() == null || request.getNotes().equals(existingAsset.getNotes()) || 
                 !request.getNotes().equals(existingAsset.getNotes())) &&
                (request.getAssetType() == null || request.getAssetType().equals(existingAsset.getAssetType())) &&
                (request.getName() == null || request.getName().equals(existingAsset.getName())) &&
                (request.getAcquisitionDate() == null || request.getAcquisitionDate().equals(existingAsset.getAcquisitionDate())) &&
                (request.getStatus() == null || request.getStatus().equals(existingAsset.getStatus())) &&
                (request.getLocation() == null || request.getLocation().equals(existingAsset.getLocation())) &&
                (request.getAssignedUser() == null || request.getAssignedUser().equals(existingAsset.getAssignedUser())) &&
                (request.getAssignedUserEmail() == null || request.getAssignedUserEmail().equals(existingAsset.getAssignedUserEmail())) &&
                (request.getCustomFields() == null || request.getCustomFields().equals(existingAsset.getCustomFields()));
            
            if (!onlyNotesChanged) {
                throw new IllegalStateException(
                    "Asset is read-only (retired status). Only notes field can be updated for retired assets.");
            }
        }
        
        // Step 5-7: Track field changes and update mutable fields
        java.util.Map<String, FieldChangeDTO> fieldChanges = new java.util.HashMap<>();
        
        // Update assetType if changed
        if (request.getAssetType() != null && !request.getAssetType().equals(existingAsset.getAssetType())) {
            fieldChanges.put("assetType", new FieldChangeDTO("assetType", 
                existingAsset.getAssetType(), request.getAssetType()));
            existingAsset.setAssetType(request.getAssetType());
        }
        
        // Update name if changed
        if (request.getName() != null && !request.getName().equals(existingAsset.getName())) {
            fieldChanges.put("name", new FieldChangeDTO("name", 
                existingAsset.getName(), request.getName()));
            existingAsset.setName(request.getName());
        }
        
        // Note: serialNumber is immutable - never update
        
        // Update acquisitionDate if changed
        if (request.getAcquisitionDate() != null && !request.getAcquisitionDate().equals(existingAsset.getAcquisitionDate())) {
            fieldChanges.put("acquisitionDate", new FieldChangeDTO("acquisitionDate", 
                existingAsset.getAcquisitionDate(), request.getAcquisitionDate()));
            existingAsset.setAcquisitionDate(request.getAcquisitionDate());
        }
        
        // Update status if changed
        if (request.getStatus() != null && !request.getStatus().equals(existingAsset.getStatus())) {
            fieldChanges.put("status", new FieldChangeDTO("status", 
                existingAsset.getStatus(), request.getStatus()));
            existingAsset.setStatus(request.getStatus());
        }
        
        // Update location if changed (allow null to clear)
        if (!java.util.Objects.equals(request.getLocation(), existingAsset.getLocation())) {
            fieldChanges.put("location", new FieldChangeDTO("location", 
                existingAsset.getLocation(), request.getLocation()));
            existingAsset.setLocation(request.getLocation());
            existingAsset.setLocationUpdateDate(java.time.LocalDateTime.now());
        }
        
        // Update assignedUser if changed (allow null to clear)
        if (!java.util.Objects.equals(request.getAssignedUser(), existingAsset.getAssignedUser())) {
            fieldChanges.put("assignedUser", new FieldChangeDTO("assignedUser", 
                existingAsset.getAssignedUser(), request.getAssignedUser()));
            existingAsset.setAssignedUser(request.getAssignedUser());
            existingAsset.setAssignmentDate(java.time.LocalDateTime.now());
        }
        
        // Update assignedUserEmail if changed (allow null to clear)
        if (!java.util.Objects.equals(request.getAssignedUserEmail(), existingAsset.getAssignedUserEmail())) {
            fieldChanges.put("assignedUserEmail", new FieldChangeDTO("assignedUserEmail", 
                existingAsset.getAssignedUserEmail(), request.getAssignedUserEmail()));
            existingAsset.setAssignedUserEmail(request.getAssignedUserEmail());
        }
        
        // Update assignmentDate if explicitly provided
        if (request.getAssignmentDate() != null && !request.getAssignmentDate().equals(existingAsset.getAssignmentDate())) {
            fieldChanges.put("assignmentDate", new FieldChangeDTO("assignmentDate", 
                existingAsset.getAssignmentDate(), request.getAssignmentDate()));
            existingAsset.setAssignmentDate(request.getAssignmentDate());
        }
        
        // Update locationUpdateDate if explicitly provided
        if (request.getLocationUpdateDate() != null && !request.getLocationUpdateDate().equals(existingAsset.getLocationUpdateDate())) {
            fieldChanges.put("locationUpdateDate", new FieldChangeDTO("locationUpdateDate", 
                existingAsset.getLocationUpdateDate(), request.getLocationUpdateDate()));
            existingAsset.setLocationUpdateDate(request.getLocationUpdateDate());
        }
        
        // Update notes if changed (allow null to clear)
        if (!java.util.Objects.equals(request.getNotes(), existingAsset.getNotes())) {
            fieldChanges.put("notes", new FieldChangeDTO("notes", 
                existingAsset.getNotes(), request.getNotes()));
            existingAsset.setNotes(request.getNotes());
        }
        
        // Update customFields if changed (allow null to clear)
        if (!java.util.Objects.equals(request.getCustomFields(), existingAsset.getCustomFields())) {
            fieldChanges.put("customFields", new FieldChangeDTO("customFields", 
                existingAsset.getCustomFields(), request.getCustomFields()));
            existingAsset.setCustomFields(request.getCustomFields());
        }
        
        // Step 8: Set updatedBy (updatedAt is set automatically by JPA)
        UUID userUuid = authorizationService.resolveActorUuid(userId);
        existingAsset.setUpdatedBy(userUuid);
        
        // Step 9: Save updated asset to repository
        Asset updatedAsset = assetRepository.save(existingAsset);
        
        // Step 10: Log audit event with field changes
        try {
            auditService.logEvent(AuditEventDTO.builder()
                .userId(userUuid)
                .actionType(Action.UPDATE_ASSET)
                .resourceType("ASSET")
                .resourceId(updatedAsset.getId().toString())
                .changes(fieldChanges)
                .build());
        } catch (Exception e) {
            // Log error but don't fail the operation
            // Audit logging failures should not block asset updates
            System.err.println("Failed to log audit event: " + e.getMessage());
        }
        
        // Step 11: Return updated asset as DTO
        return AssetMapper.toDTO(updatedAsset);
    }
    
    /**
     * Retrieves an asset by its unique identifier.
     * 
     * <p>Implementation steps:
     * <ol>
     *   <li>Validate input parameter</li>
     *   <li>Query asset from repository by ID</li>
     *   <li>If not found, return empty Optional</li>
     *   <li>If found, map entity to DTO using AssetMapper</li>
     *   <li>Return Optional containing the DTO</li>
     * </ol>
     * 
     * @param assetId the UUID of the asset to retrieve (must not be null)
     * @return Optional containing the asset DTO if found, empty Optional otherwise
     * @throws IllegalArgumentException if assetId is null
     */
    @Override
    public Optional<AssetDTO> getAsset(UUID assetId) {
        // Step 1: Validate input parameter
        if (assetId == null) {
            throw new IllegalArgumentException("Asset ID cannot be null");
        }
        
        // Step 2: Query asset by ID from repository
        Optional<Asset> assetOptional = assetRepository.findById(assetId);
        
        // Step 3: If asset not found, return empty Optional
        if (assetOptional.isEmpty()) {
            return Optional.empty();
        }
        
        // Step 4: Map entity to DTO using AssetMapper
        Asset asset = assetOptional.get();
        AssetDTO assetDTO = AssetMapper.toDTO(asset);
        
        // Step 5: Return Optional containing the DTO
        return Optional.of(assetDTO);
    }
    
    /**
     * Searches for assets matching the specified criteria with pagination support.
     * 
     * <p>Implementation steps:
     * <ol>
     *   <li>Validate input parameters</li>
     *   <li>Build search query from AssetSearchQuery parameters</li>
     *   <li>Execute search using repository with pagination</li>
     *   <li>Map results to DTOs</li>
     *   <li>Return paginated results</li>
     * </ol>
     * 
     * <p>Supports filtering by:
     * <ul>
     *   <li>Text search (name, serial number, location) - case-insensitive partial match</li>
     *   <li>Asset types (multiple) - exact match</li>
     *   <li>Lifecycle statuses (multiple) - exact match</li>
     *   <li>Location - exact match</li>
     *   <li>Acquisition date range (from/to)</li>
     * </ul>
     * 
     * <p><strong>Performance:</strong> Must complete within 2 seconds for inventories up to 100,000 assets.
     * 
     * @param query the search query containing filter criteria (can be null for unfiltered results)
     * @param pageable pagination and sorting parameters (must not be null)
     * @return Page containing matching assets with pagination metadata
     * @throws IllegalArgumentException if pageable is null
     */
    @Override
    public Page<AssetDTO> searchAssets(AssetSearchQuery query, Pageable pageable) {
        // Step 1: Validate input parameters
        if (pageable == null) {
            throw new IllegalArgumentException("Pageable cannot be null");
        }
        
        // Step 2: Build search query from AssetSearchQuery parameters
        // Extract search parameters from query (handle null query)
        String text = query != null ? query.getText() : null;
        java.util.List<com.company.assetmanagement.model.AssetType> assetTypes = 
            query != null ? query.getAssetTypes() : null;
        java.util.List<com.company.assetmanagement.model.LifecycleStatus> statuses = 
            query != null ? query.getStatuses() : null;
        String location = query != null ? query.getLocation() : null;
        java.time.LocalDate dateFrom = query != null ? query.getAcquisitionDateFrom() : null;
        java.time.LocalDate dateTo = query != null ? query.getAcquisitionDateTo() : null;
        
        // Step 3: Execute search using repository with pagination
        // The repository method handles all the complex query logic including:
        // - Text search across name, serialNumber, and location (case-insensitive, partial match)
        // - Filtering by multiple asset types (IN clause)
        // - Filtering by multiple statuses (IN clause)
        // - Exact location match (case-insensitive)
        // - Date range filtering (inclusive on both ends)
        // All filters are combined with AND logic
        Page<Asset> assetPage = assetRepository.searchAssets(
            text,
            assetTypes,
            statuses,
            location,
            dateFrom,
            dateTo,
            pageable
        );
        
        // Step 4: Map results to DTOs
        // Step 5: Return paginated results
        // Using Page.map() to transform each Asset entity to AssetDTO
        // This preserves pagination metadata (totalElements, totalPages, etc.)
        return assetPage.map(AssetMapper::toDTO);
    }
    
    /**
     * Updates the lifecycle status of an asset.
     * 
     * <p>Implementation steps:
     * <ol>
     *   <li>Validate input parameters (userId, assetId, newStatus)</li>
     *   <li>Authorization check - Verify user has UPDATE_ASSET permission</li>
     *   <li>Retrieve existing asset by ID (throw ResourceNotFoundException if not found)</li>
     *   <li>Validate status transition using LifecycleStatus.canTransitionTo(newStatus)</li>
     *   <li>If transition is invalid, throw InvalidStatusTransitionException with fromStatus and toStatus</li>
     *   <li>Update status field</li>
     *   <li>If newStatus == RETIRED, set readOnly = true</li>
     *   <li>Set updatedBy to userId</li>
     *   <li>Save updated asset to repository</li>
     *   <li>Log audit event with status change details</li>
     *   <li>Return updated asset as DTO</li>
     * </ol>
     * 
     * @param userId the ID of the user updating the status (must not be null)
     * @param assetId the UUID of the asset to update (must not be null)
     * @param newStatus the new lifecycle status (must not be null)
     * @return the updated asset as DTO with new status
     * @throws ResourceNotFoundException if asset with given ID does not exist
     * @throws InsufficientPermissionsException if user lacks UPDATE_ASSET permission
     * @throws InvalidStatusTransitionException if the status transition is not allowed
     * @throws IllegalArgumentException if userId, assetId, or newStatus is null
     */
    @Override
    public AssetDTO updateStatus(String userId, UUID assetId, LifecycleStatus newStatus) {
        // Step 1: Validate input parameters
        if (userId == null || userId.isBlank()) {
            throw new IllegalArgumentException("User ID cannot be null or empty");
        }
        if (assetId == null) {
            throw new IllegalArgumentException("Asset ID cannot be null");
        }
        if (newStatus == null) {
            throw new IllegalArgumentException("New status cannot be null");
        }
        
        // Step 2: Authorization check
        if (!authorizationService.hasPermission(userId, Action.UPDATE_ASSET)) {
            throw new InsufficientPermissionsException(userId, Action.UPDATE_ASSET.name());
        }
        
        // Step 3: Retrieve existing asset by ID
        Asset existingAsset = assetRepository.findById(assetId)
            .orElseThrow(() -> new ResourceNotFoundException("Asset", assetId.toString()));
        
        // Step 4: Validate status transition using LifecycleStatus.canTransitionTo()
        LifecycleStatus currentStatus = existingAsset.getStatus();
        if (!currentStatus.canTransitionTo(newStatus)) {
            // Step 5: Throw InvalidStatusTransitionException for invalid transitions
            throw new InvalidStatusTransitionException(
                currentStatus.name(), 
                newStatus.name(), 
                "Asset"
            );
        }
        
        // Step 6: Update status field
        existingAsset.setStatus(newStatus);
        
        // Step 7: Set readOnly=true when status becomes RETIRED
        if (newStatus == LifecycleStatus.RETIRED) {
            existingAsset.setReadOnly(true);
        }
        
        // Step 8: Set updatedBy to userId (updatedAt is set automatically by JPA)
        UUID userUuid = authorizationService.resolveActorUuid(userId);
        existingAsset.setUpdatedBy(userUuid);
        
        // Step 9: Save updated asset to repository
        Asset updatedAsset = assetRepository.save(existingAsset);
        
        // Step 10: Log audit event with status change details
        try {
            java.util.Map<String, FieldChangeDTO> changes = new java.util.HashMap<>();
            changes.put("status", new FieldChangeDTO("status", currentStatus, newStatus));
            
            auditService.logEvent(AuditEventDTO.builder()
                .userId(userUuid)
                .actionType(Action.UPDATE_ASSET)
                .resourceType("ASSET")
                .resourceId(updatedAsset.getId().toString())
                .changes(changes)
                .build());
        } catch (Exception e) {
            // Log error but don't fail the operation
            // Audit logging failures should not block status updates
            System.err.println("Failed to log audit event: " + e.getMessage());
        }
        
        // Step 11: Return updated asset as DTO
        return AssetMapper.toDTO(updatedAsset);
    }
    
    /**
     * Deletes an asset from the system.
     * 
     * <p>Implementation steps:
     * <ol>
     *   <li>Validate input parameters (userId, assetId)</li>
     *   <li>Authorization check - Verify user has DELETE_ASSET permission (Administrator only)</li>
     *   <li>Retrieve existing asset by ID (throw ResourceNotFoundException if not found)</li>
     *   <li>Delete asset from repository (cascade deletes related records)</li>
     *   <li>Log audit event for asset deletion</li>
     * </ol>
     * 
     * <p><strong>Note:</strong> This operation permanently removes the asset and all related
     * assignment history. Consider archiving instead of deletion for audit trail purposes.
     * 
     * @param userId the ID of the user deleting the asset (must not be null)
     * @param assetId the UUID of the asset to delete (must not be null)
     * @throws ResourceNotFoundException if asset with given ID does not exist
     * @throws InsufficientPermissionsException if user lacks DELETE_ASSET permission (not Administrator)
     * @throws IllegalArgumentException if userId or assetId is null
     */
    @Override
    public void deleteAsset(String userId, UUID assetId) {
        // Step 1: Validate input parameters
        if (userId == null || userId.isBlank()) {
            throw new IllegalArgumentException("User ID cannot be null or empty");
        }
        if (assetId == null) {
            throw new IllegalArgumentException("Asset ID cannot be null");
        }
        
        // Step 2: Authorization check
        if (!authorizationService.hasPermission(userId, Action.DELETE_ASSET)) {
            throw new InsufficientPermissionsException(userId, Action.DELETE_ASSET.name());
        }
        
        // Step 3: Retrieve existing asset by ID to verify existence
        Asset existingAsset = assetRepository.findById(assetId)
            .orElseThrow(() -> new ResourceNotFoundException("Asset", assetId.toString()));
        
        // Step 4: Delete asset from repository
        // This will cascade delete related records based on database constraints
        assetRepository.delete(existingAsset);
        
        // Step 5: Log audit event for asset deletion
        try {
            UUID userUuid = authorizationService.resolveActorUuid(userId);
            auditService.logEvent(AuditEventDTO.builder()
                .userId(userUuid)
                .actionType(Action.DELETE_ASSET)
                .resourceType("ASSET")
                .resourceId(assetId.toString())
                .build());
        } catch (Exception e) {
            // Log error but don't fail the operation
            // Audit logging failures should not block asset deletion
            System.err.println("Failed to log audit event: " + e.getMessage());
        }
    }
    
    /**
     * Exports assets to the specified format (CSV or JSON).
     * 
     * <p>Implementation steps:
     * <ol>
     *   <li>Validate input parameters (format)</li>
     *   <li>Apply optional search query filters to determine which assets to export</li>
     *   <li>Retrieve matching assets from database using streaming/pagination</li>
     *   <li>Convert assets to specified format (CSV or JSON)</li>
     *   <li>Return export result with file data and metadata</li>
     * </ol>
     * 
     * <p><strong>Performance:</strong> Must complete within 30 seconds for 100,000 assets.
     * Uses efficient streaming approach to handle large datasets.
     * 
     * @param format the export format (CSV or JSON) (must not be null)
     * @param query optional search query to filter exported assets (can be null for all assets)
     * @return ExportResult containing file data, content type, and metadata
     * @throws IllegalArgumentException if format is null
     * @throws IllegalStateException if export operation times out or fails
     */
    @Override
    public ExportResult exportAssets(ExportFormat format, AssetSearchQuery query) {
        // Step 1: Validate input parameters
        if (format == null) {
            throw new IllegalArgumentException("Export format cannot be null");
        }
        
        // Step 2: Apply optional search query filters to determine which assets to export
        // Step 3: Retrieve matching assets from database
        // Use pagination to handle large datasets efficiently
        org.springframework.data.domain.Pageable pageable = 
            org.springframework.data.domain.PageRequest.of(0, Integer.MAX_VALUE);
        
        org.springframework.data.domain.Page<Asset> assetPage = 
            searchAssetsInternal(query, pageable);
        
        java.util.List<Asset> assets = assetPage.getContent();
        
        // Step 4: Convert assets to specified format (CSV or JSON)
        byte[] exportData;
        String fileName;
        
        try {
            if (format == ExportFormat.CSV) {
                exportData = exportToCSV(assets);
                fileName = "assets_export_" + java.time.LocalDateTime.now()
                    .format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".csv";
            } else if (format == ExportFormat.JSON) {
                exportData = exportToJSON(assets);
                fileName = "assets_export_" + java.time.LocalDateTime.now()
                    .format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".json";
            } else {
                throw new IllegalArgumentException("Unsupported export format: " + format);
            }
        } catch (Exception e) {
            throw new IllegalStateException("Failed to generate export file: " + e.getMessage(), e);
        }
        
        // Step 5: Return export result with file data and metadata
        return ExportResult.builder()
            .fileName(fileName)
            .contentType(format.getContentType())
            .data(exportData)
            .recordCount(assets.size())
            .timestamp(java.time.LocalDateTime.now())
            .message("Successfully exported " + assets.size() + " asset(s)")
            .build();
    }
    
    /**
     * Internal method to search assets and return Page<Asset> instead of Page<AssetDTO>.
     * This is used by export functionality to avoid unnecessary DTO conversion.
     * 
     * @param query the search query
     * @param pageable pagination parameters
     * @return Page of Asset entities
     */
    private org.springframework.data.domain.Page<Asset> searchAssetsInternal(
            AssetSearchQuery query, org.springframework.data.domain.Pageable pageable) {
        
        // Extract search parameters from query (handle null query)
        String text = query != null ? query.getText() : null;
        java.util.List<com.company.assetmanagement.model.AssetType> assetTypes = 
            query != null ? query.getAssetTypes() : null;
        java.util.List<com.company.assetmanagement.model.LifecycleStatus> statuses = 
            query != null ? query.getStatuses() : null;
        String location = query != null ? query.getLocation() : null;
        java.time.LocalDate dateFrom = query != null ? query.getAcquisitionDateFrom() : null;
        java.time.LocalDate dateTo = query != null ? query.getAcquisitionDateTo() : null;
        
        // Execute search using repository
        return assetRepository.searchAssets(
            text,
            assetTypes,
            statuses,
            location,
            dateFrom,
            dateTo,
            pageable
        );
    }
    
    /**
     * Exports assets to CSV format.
     * 
     * <p>CSV Format:
     * <ul>
     *   <li>Header row with column names</li>
     *   <li>One row per asset with all fields</li>
     *   <li>Fields are comma-separated and quoted if they contain commas or quotes</li>
     *   <li>Dates are formatted as ISO-8601 (yyyy-MM-dd)</li>
     *   <li>Timestamps are formatted as ISO-8601 (yyyy-MM-dd'T'HH:mm:ss)</li>
     * </ul>
     * 
     * @param assets the list of assets to export
     * @return byte array containing CSV data
     */
    private byte[] exportToCSV(java.util.List<Asset> assets) {
        StringBuilder csv = new StringBuilder();
        
        // Header row
        csv.append("ID,Asset Type,Name,Serial Number,Acquisition Date,Status,Location,")
           .append("Assigned User,Assigned User Email,Assignment Date,Location Update Date,")
           .append("Notes,Custom Fields,Created At,Created By,Updated At,Updated By,Read Only\n");
        
        // Data rows
        for (Asset asset : assets) {
            csv.append(escapeCsvValue(asset.getId() != null ? asset.getId().toString() : "")).append(",");
            csv.append(escapeCsvValue(asset.getAssetType() != null ? asset.getAssetType().name() : "")).append(",");
            csv.append(escapeCsvValue(asset.getName())).append(",");
            csv.append(escapeCsvValue(asset.getSerialNumber())).append(",");
            csv.append(escapeCsvValue(asset.getAcquisitionDate() != null ? asset.getAcquisitionDate().toString() : "")).append(",");
            csv.append(escapeCsvValue(asset.getStatus() != null ? asset.getStatus().name() : "")).append(",");
            csv.append(escapeCsvValue(asset.getLocation())).append(",");
            csv.append(escapeCsvValue(asset.getAssignedUser())).append(",");
            csv.append(escapeCsvValue(asset.getAssignedUserEmail())).append(",");
            csv.append(escapeCsvValue(asset.getAssignmentDate() != null ? asset.getAssignmentDate().toString() : "")).append(",");
            csv.append(escapeCsvValue(asset.getLocationUpdateDate() != null ? asset.getLocationUpdateDate().toString() : "")).append(",");
            csv.append(escapeCsvValue(asset.getNotes())).append(",");
            csv.append(escapeCsvValue(asset.getCustomFields())).append(",");
            csv.append(escapeCsvValue(asset.getCreatedAt() != null ? asset.getCreatedAt().toString() : "")).append(",");
            csv.append(escapeCsvValue(asset.getCreatedBy() != null ? asset.getCreatedBy().toString() : "")).append(",");
            csv.append(escapeCsvValue(asset.getUpdatedAt() != null ? asset.getUpdatedAt().toString() : "")).append(",");
            csv.append(escapeCsvValue(asset.getUpdatedBy() != null ? asset.getUpdatedBy().toString() : "")).append(",");
            csv.append(asset.isReadOnly() ? "true" : "false").append("\n");
        }
        
        return csv.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8);
    }
    
    /**
     * Escapes a CSV value by:
     * - Wrapping in quotes if it contains comma, quote, or newline
     * - Doubling any quotes within the value
     * - Handling null values as empty strings
     * 
     * @param value the value to escape
     * @return the escaped CSV value
     */
    private String escapeCsvValue(String value) {
        if (value == null) {
            return "";
        }
        
        // Check if value needs quoting
        boolean needsQuoting = value.contains(",") || value.contains("\"") || 
                              value.contains("\n") || value.contains("\r");
        
        if (needsQuoting) {
            // Double any quotes and wrap in quotes
            String escaped = value.replace("\"", "\"\"");
            return "\"" + escaped + "\"";
        }
        
        return value;
    }
    
    /**
     * Exports assets to JSON format.
     * 
     * <p>JSON Format:
     * <ul>
     *   <li>Array of asset objects</li>
     *   <li>Each asset contains all fields</li>
     *   <li>Dates are formatted as ISO-8601 strings</li>
     *   <li>Pretty-printed for readability</li>
     * </ul>
     * 
     * @param assets the list of assets to export
     * @return byte array containing JSON data
     * @throws com.fasterxml.jackson.core.JsonProcessingException if JSON serialization fails
     */
    private byte[] exportToJSON(java.util.List<Asset> assets) throws com.fasterxml.jackson.core.JsonProcessingException {
        // Convert assets to DTOs for consistent JSON structure
        java.util.List<AssetDTO> assetDTOs = assets.stream()
            .map(AssetMapper::toDTO)
            .collect(java.util.stream.Collectors.toList());
        
        // Use Jackson ObjectMapper for JSON serialization
        com.fasterxml.jackson.databind.ObjectMapper objectMapper = 
            new com.fasterxml.jackson.databind.ObjectMapper();
        
        // Configure ObjectMapper for proper date/time serialization
        objectMapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
        objectMapper.disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        
        // Enable pretty printing for readability
        objectMapper.enable(com.fasterxml.jackson.databind.SerializationFeature.INDENT_OUTPUT);
        
        // Serialize to JSON
        String json = objectMapper.writeValueAsString(assetDTOs);
        
        return json.getBytes(java.nio.charset.StandardCharsets.UTF_8);
    }
    
    /**
     * Imports assets from file data in the specified format (CSV or JSON).
     * 
     * <p>Implementation steps:
     * <ol>
     *   <li>Validate input parameters (userId, format, data)</li>
     *   <li>Authorization check - Verify user has CREATE_ASSET permission</li>
     *   <li>Parse file data according to specified format</li>
     *   <li>Validate each asset record (validation errors are collected, not thrown)</li>
     *   <li>Check for duplicate serial numbers</li>
     *   <li>Import valid assets in batches (transactional)</li>
     *   <li>Log audit events for successful imports</li>
     *   <li>Return import result with success/failure counts and error details</li>
     * </ol>
     * 
     * @param userId the ID of the user importing assets (must not be null)
     * @param format the import format (CSV or JSON) (must not be null)
     * @param data the file data as byte array (must not be null)
     * @return ImportResult containing success/failure counts and detailed error information
     * @throws InsufficientPermissionsException if user lacks CREATE_ASSET permission
     * @throws IllegalArgumentException if userId, format, or data is null
     * @throws IllegalArgumentException if file size exceeds 10MB
     * @throws IllegalArgumentException if data contains more than 10,000 records
     * @throws IllegalStateException if file format is invalid or cannot be parsed
     */
    @Override
    public ImportResult importAssets(String userId, ImportFormat format, byte[] data) {
        // Step 1: Validate input parameters
        if (userId == null || userId.isBlank()) {
            throw new IllegalArgumentException("User ID cannot be null or empty");
        }
        if (format == null) {
            throw new IllegalArgumentException("Import format cannot be null");
        }
        if (data == null || data.length == 0) {
            throw new IllegalArgumentException("Import data cannot be null or empty");
        }
        
        // Validate file size (max 10MB)
        final int MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB in bytes
        if (data.length > MAX_FILE_SIZE) {
            throw new IllegalArgumentException(
                "File size exceeds maximum allowed size of 10MB. File size: " + 
                (data.length / 1024 / 1024) + "MB");
        }
        
        // Step 2: Authorization check
        if (!authorizationService.hasPermission(userId, Action.CREATE_ASSET)) {
            throw new InsufficientPermissionsException(userId, Action.CREATE_ASSET.name());
        }
        
        // Step 3: Parse file data according to specified format
        java.util.List<AssetRequest> assetRequests;
        try {
            if (format == ImportFormat.CSV) {
                assetRequests = parseCSV(data);
            } else if (format == ImportFormat.JSON) {
                assetRequests = parseJSON(data);
            } else {
                throw new IllegalStateException("Unsupported import format: " + format);
            }
        } catch (Exception e) {
            throw new IllegalStateException("Failed to parse import file: " + e.getMessage(), e);
        }
        
        // Validate record count (max 10,000 records)
        final int MAX_RECORDS = 10000;
        if (assetRequests.size() > MAX_RECORDS) {
            throw new IllegalArgumentException(
                "Import file contains more than maximum allowed " + MAX_RECORDS + 
                " records. Record count: " + assetRequests.size());
        }
        
        // Initialize import result
        ImportResult result = new ImportResult();
        result.setTotalRecords(assetRequests.size());
        
        // Step 4-7: Validate, check duplicates, import in batches, and log audit events
        int successCount = 0;
        int failureCount = 0;
        int lineNumber = 1; // Start from 1 (header is line 0 in CSV)
        
        // Batch processing
        final int BATCH_SIZE = 100;
        java.util.List<Asset> batchAssets = new java.util.ArrayList<>();
        
        for (AssetRequest request : assetRequests) {
            lineNumber++;
            
            try {
                // Step 5: Validate each record before import
                validationService.validateAssetRequest(request);
                
                // Step 6: Check for duplicate serial numbers
                if (assetRepository.existsBySerialNumber(request.getSerialNumber())) {
                    result.addError(new ImportResult.ImportError(
                        lineNumber,
                        "Duplicate serial number: " + request.getSerialNumber(),
                        request.getSerialNumber()
                    ));
                    failureCount++;
                    continue;
                }
                
                // Create asset entity
                Asset asset = AssetMapper.toEntity(request);
                UUID userUuid = authorizationService.resolveActorUuid(userId);
                asset.setCreatedBy(userUuid);
                asset.setUpdatedBy(userUuid);
                asset.setReadOnly(false);
                
                // Add to batch
                batchAssets.add(asset);
                
                // Step 8: Implement batch processing (up to 10,000 records)
                // Step 9: Use transaction management (all or nothing per batch)
                if (batchAssets.size() >= BATCH_SIZE) {
                    successCount += saveBatch(batchAssets, userId);
                    batchAssets.clear();
                }
                
            } catch (ValidationException ve) {
                // Collect validation errors with line numbers
                StringBuilder errorMsg = new StringBuilder();
                for (ValidationException.ValidationError error : ve.getErrors()) {
                    if (errorMsg.length() > 0) {
                        errorMsg.append("; ");
                    }
                    errorMsg.append(error.getField()).append(": ").append(error.getMessage());
                }
                result.addError(new ImportResult.ImportError(
                    lineNumber,
                    errorMsg.toString(),
                    request.getSerialNumber()
                ));
                failureCount++;
            } catch (Exception e) {
                // Catch any other unexpected errors
                result.addError(new ImportResult.ImportError(
                    lineNumber,
                    "Unexpected error: " + e.getMessage(),
                    request.getSerialNumber()
                ));
                failureCount++;
            }
        }
        
        // Save remaining batch
        if (!batchAssets.isEmpty()) {
            successCount += saveBatch(batchAssets, userId);
            batchAssets.clear();
        }
        
        // Step 10: Return ImportResult with success/failure counts
        result.setSuccessCount(successCount);
        result.setFailureCount(failureCount);
        
        if (successCount > 0 && failureCount == 0) {
            result.setMessage("Successfully imported all " + successCount + " asset(s)");
        } else if (successCount > 0 && failureCount > 0) {
            result.setMessage("Imported " + successCount + " asset(s) with " + 
                failureCount + " failure(s)");
        } else {
            result.setMessage("Import failed. No assets were imported. " + 
                failureCount + " error(s) found.");
        }
        
        return result;
    }
    
    /**
     * Saves a batch of assets to the database in a single transaction.
     * Step 11: Integrate with AuditService for import logging
     * 
     * @param assets the list of assets to save
     * @param userId the user ID performing the import
     * @return the number of successfully saved assets
     */
    @Transactional
    private int saveBatch(java.util.List<Asset> assets, String userId) {
        if (assets == null || assets.isEmpty()) {
            return 0;
        }
        
        try {
            // Save all assets in batch
            java.util.List<Asset> savedAssets = assetRepository.saveAll(assets);
            
            // Log audit events for each imported asset
            UUID userUuid = authorizationService.resolveActorUuid(userId);
            for (Asset savedAsset : savedAssets) {
                try {
                    auditService.logEvent(AuditEventDTO.builder()
                        .userId(userUuid)
                        .actionType(Action.CREATE_ASSET)
                        .resourceType("ASSET")
                        .resourceId(savedAsset.getId().toString())
                        .build());
                } catch (Exception e) {
                    // Log error but don't fail the import
                    System.err.println("Failed to log audit event for asset " + 
                        savedAsset.getId() + ": " + e.getMessage());
                }
            }
            
            return savedAssets.size();
        } catch (Exception e) {
            // If batch save fails, transaction will rollback
            System.err.println("Failed to save batch: " + e.getMessage());
            throw e;
        }
    }
    
    /**
     * Parses CSV data into a list of AssetRequest objects.
     * Step 3: Parse CSV format
     * 
     * <p>Expected CSV format:
     * <ul>
     *   <li>Header row with column names</li>
     *   <li>Columns: Asset Type, Name, Serial Number, Acquisition Date, Status, 
     *       Location, Assigned User, Assigned User Email, Notes</li>
     *   <li>Dates in ISO-8601 format (yyyy-MM-dd)</li>
     *   <li>Values may be quoted if they contain commas</li>
     * </ul>
     * 
     * @param data the CSV file data
     * @return list of AssetRequest objects
     * @throws Exception if parsing fails
     */
    private java.util.List<AssetRequest> parseCSV(byte[] data) throws Exception {
        java.util.List<AssetRequest> requests = new java.util.ArrayList<>();
        String csvContent = new String(data, java.nio.charset.StandardCharsets.UTF_8);
        
        // Split into lines
        String[] lines = csvContent.split("\\r?\\n");
        
        if (lines.length < 2) {
            throw new IllegalStateException("CSV file must contain at least a header row and one data row");
        }
        
        // Parse header to determine column positions
        String[] headers = parseCsvLine(lines[0]);
        java.util.Map<String, Integer> columnMap = new java.util.HashMap<>();
        for (int i = 0; i < headers.length; i++) {
            columnMap.put(headers[i].trim().toLowerCase(), i);
        }
        
        // Parse data rows
        for (int i = 1; i < lines.length; i++) {
            String line = lines[i].trim();
            if (line.isEmpty()) {
                continue; // Skip empty lines
            }
            
            String[] values = parseCsvLine(line);
            
            AssetRequest request = new AssetRequest();
            
            // Parse Asset Type
            String assetTypeStr = getColumnValue(values, columnMap, "asset type");
            if (assetTypeStr != null && !assetTypeStr.isEmpty()) {
                try {
                    request.setAssetType(com.company.assetmanagement.model.AssetType.valueOf(assetTypeStr.toUpperCase()));
                } catch (IllegalArgumentException e) {
                    // Invalid asset type will be caught by validation
                }
            }
            
            // Parse Name
            request.setName(getColumnValue(values, columnMap, "name"));
            
            // Parse Serial Number
            request.setSerialNumber(getColumnValue(values, columnMap, "serial number"));
            
            // Parse Acquisition Date
            String dateStr = getColumnValue(values, columnMap, "acquisition date");
            if (dateStr != null && !dateStr.isEmpty()) {
                try {
                    request.setAcquisitionDate(java.time.LocalDate.parse(dateStr));
                } catch (Exception e) {
                    // Invalid date will be caught by validation
                }
            }
            
            // Parse Status
            String statusStr = getColumnValue(values, columnMap, "status");
            if (statusStr != null && !statusStr.isEmpty()) {
                try {
                    request.setStatus(LifecycleStatus.valueOf(statusStr.toUpperCase()));
                } catch (IllegalArgumentException e) {
                    // Invalid status will be caught by validation
                }
            }
            
            // Parse optional fields
            request.setLocation(getColumnValue(values, columnMap, "location"));
            request.setAssignedUser(getColumnValue(values, columnMap, "assigned user"));
            request.setAssignedUserEmail(getColumnValue(values, columnMap, "assigned user email"));
            request.setNotes(getColumnValue(values, columnMap, "notes"));
            
            requests.add(request);
        }
        
        return requests;
    }
    
    /**
     * Parses a single CSV line, handling quoted values and escaped quotes.
     * 
     * @param line the CSV line to parse
     * @return array of column values
     */
    private String[] parseCsvLine(String line) {
        java.util.List<String> values = new java.util.ArrayList<>();
        StringBuilder currentValue = new StringBuilder();
        boolean inQuotes = false;
        
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            
            if (c == '"') {
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    // Escaped quote
                    currentValue.append('"');
                    i++; // Skip next quote
                } else {
                    // Toggle quote mode
                    inQuotes = !inQuotes;
                }
            } else if (c == ',' && !inQuotes) {
                // End of value
                values.add(currentValue.toString().trim());
                currentValue = new StringBuilder();
            } else {
                currentValue.append(c);
            }
        }
        
        // Add last value
        values.add(currentValue.toString().trim());
        
        return values.toArray(new String[0]);
    }
    
    /**
     * Gets a column value from the CSV row by column name.
     * 
     * @param values the row values
     * @param columnMap the column name to index map
     * @param columnName the column name to retrieve
     * @return the column value or null if not found
     */
    private String getColumnValue(String[] values, java.util.Map<String, Integer> columnMap, String columnName) {
        Integer index = columnMap.get(columnName.toLowerCase());
        if (index != null && index < values.length) {
            String value = values[index];
            return value != null && !value.isEmpty() ? value : null;
        }
        return null;
    }
    
    /**
     * Parses JSON data into a list of AssetRequest objects.
     * Step 4: Parse JSON format
     * 
     * <p>Expected JSON format:
     * <ul>
     *   <li>Array of asset objects</li>
     *   <li>Each object contains asset fields</li>
     *   <li>Dates in ISO-8601 format</li>
     * </ul>
     * 
     * @param data the JSON file data
     * @return list of AssetRequest objects
     * @throws Exception if parsing fails
     */
    private java.util.List<AssetRequest> parseJSON(byte[] data) throws Exception {
        String jsonContent = new String(data, java.nio.charset.StandardCharsets.UTF_8);
        
        // Use Jackson ObjectMapper for JSON deserialization
        com.fasterxml.jackson.databind.ObjectMapper objectMapper = 
            new com.fasterxml.jackson.databind.ObjectMapper();
        
        // Configure ObjectMapper for proper date/time deserialization
        objectMapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
        objectMapper.disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        
        // Parse JSON array
        com.fasterxml.jackson.core.type.TypeReference<java.util.List<AssetRequest>> typeRef = 
            new com.fasterxml.jackson.core.type.TypeReference<java.util.List<AssetRequest>>() {};
        
        java.util.List<AssetRequest> requests = objectMapper.readValue(jsonContent, typeRef);
        
        if (requests == null) {
            throw new IllegalStateException("JSON file does not contain a valid array of assets");
        }
        
        return requests;
    }
    
    /**
     * Retrieves asset statistics for dashboard display.
     * 
     * <p>This method calculates and returns quick statistics about the asset inventory:
     * <ul>
     *   <li>Total number of assets in the system</li>
     *   <li>Number of assets currently in use (status = IN_USE)</li>
     *   <li>Timestamp when statistics were calculated</li>
     * </ul>
     * 
     * <p>Statistics are calculated using efficient database aggregation queries.
     * The calculation timestamp is set to the current time when the method is called.
     * 
     * <p><strong>Performance:</strong> Uses COUNT queries with WHERE clauses for efficiency.
     * Should complete within 500 milliseconds even for large inventories.
     * 
     * <p><strong>Requirements:</strong>
     * <ul>
     *   <li>Requirement 22: Dashboard and Quick Stats</li>
     *   <li>Requirement 12: Performance Requirements</li>
     * </ul>
     * 
     * @return AssetStatsDTO containing current asset statistics
     * @throws IllegalStateException if statistics calculation fails
     */
    @Override
    @Transactional(readOnly = true)
    public AssetStatsDTO getAssetStats() {
        try {
            // Calculate total assets count
            Long totalAssets = assetRepository.count();
            
            // Calculate assets in use count (status = IN_USE)
            Long assetsInUse = assetRepository.countByStatus(LifecycleStatus.IN_USE);
            
            // Set calculation timestamp
            java.time.LocalDateTime lastUpdated = java.time.LocalDateTime.now();
            
            // Create and return stats DTO
            return new AssetStatsDTO(totalAssets, assetsInUse, lastUpdated);
            
        } catch (Exception e) {
            throw new IllegalStateException("Failed to calculate asset statistics", e);
        }
    }
    
    @Override
    @Transactional
    public AssetDTO uploadAssetImage(String userId, UUID assetId, MultipartFile file) {
        // 1. Authorization check
        if (!authorizationService.hasPermission(userId, Action.UPDATE_ASSET)) {
            throw new InsufficientPermissionsException();
        }
        
        // 2. Validate file
        validateImageFile(file);
        
        // 3. Get existing asset
        Asset asset = assetRepository.findById(assetId)
            .orElseThrow(() -> new ResourceNotFoundException("Asset", assetId.toString()));
        
        // 4. Check if asset can be modified
        if (asset.isReadOnly()) {
            throw new ValidationException(List.of(
                new ValidationException.ValidationError("asset", "Cannot upload image for read-only asset")
            ));
        }
        
        try {
            // 5. Generate unique filename
            String originalFilename = file.getOriginalFilename();
            String fileExtension = getFileExtension(originalFilename);
            String uniqueFilename = assetId + "_" + System.currentTimeMillis() + fileExtension;
            
            // 6. Create upload directory if it doesn't exist
            Path uploadDir = Paths.get("uploads/assets");
            Files.createDirectories(uploadDir);
            
            // 7. Save file to disk
            Path filePath = uploadDir.resolve(uniqueFilename);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            
            // 8. Update asset with image information
            asset.setImageUrl("/api/v1/assets/" + assetId + "/image");
            asset.setImageFilename(uniqueFilename);
            asset.setImageSize(file.getSize());
            asset.setImageContentType(file.getContentType());
            asset.setUpdatedBy(authorizationService.resolveActorUuid(userId));
            
            // 9. Save asset
            Asset savedAsset = assetRepository.save(asset);
            
            // 10. Audit logging
            auditService.logEvent(AuditEventDTO.builder()
                .userId(authorizationService.resolveActorUuid(userId))
                .actionType(Action.UPDATE_ASSET)
                .resourceType("ASSET")
                .resourceId(assetId.toString())
                .changes(Map.of(
                    "imageUrl", new FieldChangeDTO("imageUrl", null, asset.getImageUrl()),
                    "imageFilename", new FieldChangeDTO("imageFilename", null, asset.getImageFilename())
                ))
                .build());
            
            // 11. Return updated DTO
            return AssetMapper.toDTO(savedAsset);
            
        } catch (IOException e) {
            throw new RuntimeException("Failed to upload image file", e);
        }
    }
    
    /**
     * Validate uploaded image file.
     * 
     * @param file the file to validate
     * @throws ValidationException if validation fails
     */
    private void validateImageFile(MultipartFile file) {
        List<ValidationException.ValidationError> errors = new java.util.ArrayList<>();
        
        // Check if file is empty
        if (file.isEmpty()) {
            errors.add(new ValidationException.ValidationError("file", "Image file is required"));
        }
        
        // Check file size (max 5MB)
        long maxSize = 5 * 1024 * 1024; // 5MB in bytes
        if (file.getSize() > maxSize) {
            errors.add(new ValidationException.ValidationError("file", "Image file size must not exceed 5MB"));
        }
        
        // Check content type
        String contentType = file.getContentType();
        List<String> allowedTypes = Arrays.asList("image/jpeg", "image/jpg", "image/png", "image/webp");
        if (contentType == null || !allowedTypes.contains(contentType.toLowerCase())) {
            errors.add(new ValidationException.ValidationError("file", "Image format must be JPG, PNG, or WebP"));
        }
        
        // Check file extension
        String filename = file.getOriginalFilename();
        if (filename != null) {
            String extension = getFileExtension(filename).toLowerCase();
            List<String> allowedExtensions = Arrays.asList(".jpg", ".jpeg", ".png", ".webp");
            if (!allowedExtensions.contains(extension)) {
                errors.add(new ValidationException.ValidationError("file", "Image file extension must be .jpg, .jpeg, .png, or .webp"));
            }
        }
        
        if (!errors.isEmpty()) {
            throw new ValidationException(errors);
        }
    }
    
    /**
     * Get file extension from filename.
     * 
     * @param filename the filename
     * @return the file extension including the dot
     */
    private String getFileExtension(String filename) {
        if (filename == null || filename.isEmpty()) {
            return "";
        }
        int lastDotIndex = filename.lastIndexOf('.');
        return lastDotIndex > 0 ? filename.substring(lastDotIndex) : "";
    }
}
