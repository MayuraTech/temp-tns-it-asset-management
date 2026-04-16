package com.company.assetmanagement.service;

import com.company.assetmanagement.dto.*;
import com.company.assetmanagement.exception.*;
import com.company.assetmanagement.model.LifecycleStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

/**
 * Service interface for asset management business operations.
 * Defines the contract for all asset-related business logic including
 * CRUD operations, lifecycle management, search, validation, and import/export.
 * 
 * <p>This service is the core of Module 2: Asset Management and handles:
 * <ul>
 *   <li>Asset creation with validation and uniqueness checks</li>
 *   <li>Asset retrieval and search with filtering</li>
 *   <li>Asset updates with audit logging</li>
 *   <li>Lifecycle status management with transition validation</li>
 *   <li>Asset deletion with authorization checks</li>
 *   <li>Bulk import/export operations</li>
 * </ul>
 * 
 * <p>All operations enforce authorization checks and log audit events.
 * 
 * @see AssetRequest
 * @see AssetDTO
 * @see AssetSearchQuery
 * @author Module 2 Team
 * @version 1.0
 */
public interface AssetService {
    
    /**
     * Creates a new asset in the system.
     * 
     * <p>This operation performs the following steps:
     * <ol>
     *   <li>Validates user has CREATE_ASSET permission</li>
     *   <li>Validates asset request data (required fields, formats, constraints)</li>
     *   <li>Checks serial number uniqueness</li>
     *   <li>Creates and persists the asset entity</li>
     *   <li>Logs audit event for asset creation</li>
     *   <li>Returns the created asset as DTO</li>
     * </ol>
     * 
     * <p><strong>Requirements:</strong>
     * <ul>
     *   <li>Requirement 1: Asset Registration</li>
     *   <li>Requirement 6: Asset Data Validation</li>
     *   <li>Requirement 7: Serial Number Uniqueness Enforcement</li>
     *   <li>Requirement 13: Authorization and Security</li>
     *   <li>Requirement 14: Audit Logging Integration</li>
     * </ul>
     * 
     * @param userId the ID of the user creating the asset (must not be null)
     * @param request the asset creation request containing all asset details (must not be null)
     * @return the created asset as DTO with generated ID and timestamps
     * @throws InsufficientPermissionsException if user lacks CREATE_ASSET permission
     * @throws DuplicateSerialNumberException if an asset with the same serial number already exists
     * @throws ValidationException if request data fails validation (contains all validation errors)
     * @throws IllegalArgumentException if userId or request is null
     */
    AssetDTO createAsset(String userId, AssetRequest request);
    
    /**
     * Updates an existing asset with new information.
     * 
     * <p>This operation performs the following steps:
     * <ol>
     *   <li>Validates user has UPDATE_ASSET permission</li>
     *   <li>Retrieves existing asset by ID</li>
     *   <li>Validates asset is not read-only (not retired)</li>
     *   <li>Validates update request data</li>
     *   <li>Updates mutable fields (preserves immutable fields like id, serialNumber, createdAt, createdBy)</li>
     *   <li>Logs audit event with field-level changes</li>
     *   <li>Returns updated asset as DTO</li>
     * </ol>
     * 
     * <p><strong>Immutable Fields:</strong> id, serialNumber, createdAt, createdBy
     * <p><strong>Mutable Fields:</strong> name, location, assignedUser, assignedUserEmail, 
     * assignmentDate, locationUpdateDate, status, notes, customFields
     * 
     * <p><strong>Requirements:</strong>
     * <ul>
     *   <li>Requirement 3: Asset Information Update</li>
     *   <li>Requirement 6: Asset Data Validation</li>
     *   <li>Requirement 13: Authorization and Security</li>
     *   <li>Requirement 14: Audit Logging Integration</li>
     * </ul>
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
    AssetDTO updateAsset(String userId, UUID assetId, AssetRequest request);
    
    /**
     * Retrieves an asset by its unique identifier.
     * 
     * <p>This operation returns the complete asset information including all fields.
     * No authorization check is performed at this level (handled by controller layer).
     * 
     * <p><strong>Requirements:</strong>
     * <ul>
     *   <li>Requirement 2: Asset Information Retrieval</li>
     * </ul>
     * 
     * @param assetId the UUID of the asset to retrieve (must not be null)
     * @return Optional containing the asset DTO if found, empty Optional otherwise
     * @throws IllegalArgumentException if assetId is null
     */
    Optional<AssetDTO> getAsset(UUID assetId);
    
    /**
     * Searches for assets matching the specified criteria with pagination support.
     * 
     * <p>This operation supports filtering by:
     * <ul>
     *   <li>Text search (name, serial number, location) - case-insensitive partial match</li>
     *   <li>Asset types (multiple) - exact match</li>
     *   <li>Lifecycle statuses (multiple) - exact match</li>
     *   <li>Location - exact match</li>
     *   <li>Acquisition date range (from/to)</li>
     *   <li>Assigned user</li>
     * </ul>
     * 
     * <p>Multiple filters are combined using AND logic.
     * Results are paginated and can be sorted by any field.
     * 
     * <p><strong>Performance:</strong> Must complete within 2 seconds for inventories up to 100,000 assets.
     * 
     * <p><strong>Requirements:</strong>
     * <ul>
     *   <li>Requirement 5: Asset Search and Filtering</li>
     *   <li>Requirement 12: Performance Requirements</li>
     * </ul>
     * 
     * @param query the search query containing filter criteria (can be null for unfiltered results)
     * @param pageable pagination and sorting parameters (must not be null)
     * @return Page containing matching assets with pagination metadata
     * @throws IllegalArgumentException if pageable is null
     */
    Page<AssetDTO> searchAssets(AssetSearchQuery query, Pageable pageable);
    
    /**
     * Updates the lifecycle status of an asset.
     * 
     * <p>This operation performs the following steps:
     * <ol>
     *   <li>Validates user has UPDATE_ASSET permission</li>
     *   <li>Retrieves existing asset by ID</li>
     *   <li>Validates status transition is allowed according to lifecycle rules</li>
     *   <li>Updates asset status</li>
     *   <li>Sets readOnly flag to true if transitioning to RETIRED status</li>
     *   <li>Logs audit event for status change</li>
     *   <li>Returns updated asset as DTO</li>
     * </ol>
     * 
     * <p><strong>Valid Status Transitions:</strong>
     * <ul>
     *   <li>ORDERED → RECEIVED</li>
     *   <li>RECEIVED → DEPLOYED</li>
     *   <li>DEPLOYED → IN_USE or STORAGE</li>
     *   <li>IN_USE → STORAGE or RETIRED</li>
     *   <li>STORAGE → DEPLOYED or RETIRED</li>
     *   <li>Any status → MAINTENANCE</li>
     *   <li>MAINTENANCE → Any status (except RETIRED)</li>
     *   <li>RETIRED → No transitions allowed</li>
     * </ul>
     * 
     * <p><strong>Requirements:</strong>
     * <ul>
     *   <li>Requirement 4: Asset Lifecycle Status Management</li>
     *   <li>Requirement 13: Authorization and Security</li>
     *   <li>Requirement 14: Audit Logging Integration</li>
     * </ul>
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
    AssetDTO updateStatus(String userId, UUID assetId, LifecycleStatus newStatus);
    
    /**
     * Deletes an asset from the system.
     * 
     * <p>This operation performs the following steps:
     * <ol>
     *   <li>Validates user has DELETE_ASSET permission (Administrator only)</li>
     *   <li>Retrieves existing asset by ID</li>
     *   <li>Deletes the asset entity (cascade deletes related records)</li>
     *   <li>Logs audit event for asset deletion</li>
     * </ol>
     * 
     * <p><strong>Note:</strong> This operation permanently removes the asset and all related
     * assignment history. Consider archiving instead of deletion for audit trail purposes.
     * 
     * <p><strong>Requirements:</strong>
     * <ul>
     *   <li>Requirement 8: Asset Deletion</li>
     *   <li>Requirement 13: Authorization and Security</li>
     *   <li>Requirement 14: Audit Logging Integration</li>
     * </ul>
     * 
     * @param userId the ID of the user deleting the asset (must not be null)
     * @param assetId the UUID of the asset to delete (must not be null)
     * @throws ResourceNotFoundException if asset with given ID does not exist
     * @throws InsufficientPermissionsException if user lacks DELETE_ASSET permission (not Administrator)
     * @throws IllegalArgumentException if userId or assetId is null
     */
    void deleteAsset(String userId, UUID assetId);
    
    /**
     * Exports assets to the specified format (CSV or JSON).
     * 
     * <p>This operation performs the following steps:
     * <ol>
     *   <li>Applies optional search query filters to determine which assets to export</li>
     *   <li>Retrieves matching assets from database</li>
     *   <li>Converts assets to specified format (CSV or JSON)</li>
     *   <li>Returns export result with file data and metadata</li>
     * </ol>
     * 
     * <p><strong>Exported Fields:</strong> id, assetType, name, serialNumber, acquisitionDate,
     * status, location, assignedUser, assignedUserEmail, assignmentDate, locationUpdateDate,
     * notes, createdAt, createdBy, updatedAt, updatedBy
     * 
     * <p><strong>Performance:</strong> Must complete within 30 seconds for 100,000 assets.
     * 
     * <p><strong>Requirements:</strong>
     * <ul>
     *   <li>Requirement 9: Asset Data Export</li>
     *   <li>Requirement 12: Performance Requirements</li>
     *   <li>Requirement 13: Authorization and Security</li>
     * </ul>
     * 
     * @param format the export format (CSV or JSON) (must not be null)
     * @param query optional search query to filter exported assets (can be null for all assets)
     * @return ExportResult containing file data, content type, and metadata
     * @throws IllegalArgumentException if format is null
     * @throws IllegalStateException if export operation times out or fails
     */
    ExportResult exportAssets(ExportFormat format, AssetSearchQuery query);
    
    /**
     * Imports assets from file data in the specified format (CSV or JSON).
     * 
     * <p>This operation performs the following steps:
     * <ol>
     *   <li>Validates user has CREATE_ASSET permission</li>
     *   <li>Parses file data according to specified format</li>
     *   <li>Validates each asset record (validation errors are collected, not thrown)</li>
     *   <li>Checks for duplicate serial numbers</li>
     *   <li>Imports valid assets in batches (transactional)</li>
     *   <li>Logs audit events for successful imports</li>
     *   <li>Returns import result with success/failure counts and error details</li>
     * </ol>
     * 
     * <p><strong>Validation:</strong> Each record is validated independently. Invalid records
     * are reported in the result with line numbers and error messages, but do not prevent
     * valid records from being imported.
     * 
     * <p><strong>Batch Size:</strong> Supports up to 10,000 records per import operation.
     * 
     * <p><strong>Requirements:</strong>
     * <ul>
     *   <li>Requirement 10: Asset Data Import</li>
     *   <li>Requirement 13: Authorization and Security</li>
     *   <li>Requirement 14: Audit Logging Integration</li>
     * </ul>
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
    ImportResult importAssets(String userId, ImportFormat format, byte[] data);
    
    /**
     * Retrieves asset statistics for dashboard display.
     * 
     * <p>This operation calculates and returns quick statistics about the asset inventory:
     * <ul>
     *   <li>Total number of assets in the system</li>
     *   <li>Number of assets currently in use (status = IN_USE)</li>
     *   <li>Timestamp when statistics were calculated</li>
     * </ul>
     * 
     * <p>Statistics are calculated using efficient database aggregation queries
     * and may be cached for up to 5 minutes to improve performance.
     * 
     * <p><strong>Performance:</strong> Should complete within 500 milliseconds.
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
    AssetStatsDTO getAssetStats();
    
    /**
     * Upload an image for an asset.
     * 
     * <p>This method handles asset image upload with validation for file format,
     * size, and content type. Supported formats are JPG, PNG, and WebP with
     * a maximum file size of 5MB.
     * 
     * <p><strong>Authorization:</strong> Requires UPDATE_ASSET permission.
     * 
     * <p><strong>Validation:</strong>
     * <ul>
     *   <li>File format must be JPG, PNG, or WebP</li>
     *   <li>File size must not exceed 5MB</li>
     *   <li>Asset must exist and not be read-only</li>
     * </ul>
     * 
     * <p><strong>Requirements:</strong>
     * <ul>
     *   <li>Requirement 21: Visual Asset Representation</li>
     *   <li>Requirement 13: Authorization and Security</li>
     * </ul>
     * 
     * @param userId User uploading the image
     * @param assetId Asset ID to associate the image with
     * @param file Image file to upload
     * @return Updated asset DTO with image information
     * @throws ResourceNotFoundException if asset not found
     * @throws ValidationException if file validation fails
     * @throws InsufficientPermissionsException if user lacks permission
     */
    AssetDTO uploadAssetImage(String userId, UUID assetId, org.springframework.web.multipart.MultipartFile file);
}
