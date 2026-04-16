package com.company.assetmanagement.service;

import com.company.assetmanagement.dto.AssignmentDTO;
import com.company.assetmanagement.dto.AssignmentHistoryDTO;
import com.company.assetmanagement.dto.AssignmentRequest;
import com.company.assetmanagement.dto.AssignmentStatisticsDTO;
import com.company.assetmanagement.dto.AssetDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

/**
 * Service interface for allocation management operations.
 * 
 * Provides methods to assign assets to users and locations, track assignment history,
 * and manage asset allocations. All allocation operations are audited and require
 * appropriate authorization.
 * 
 * <p>Authorization Requirements:
 * <ul>
 *   <li>Write operations (assign, deallocate, reassign): ADMINISTRATOR or ASSET_MANAGER role</li>
 *   <li>Read operations (query, history): ADMINISTRATOR, ASSET_MANAGER, or VIEWER role</li>
 * </ul>
 * 
 * @see AssignmentDTO
 * @see AssignmentRequest
 * @see AssignmentHistoryDTO
 */
public interface AllocationService {
    
    /**
     * Assigns an asset to a user.
     * 
     * Creates a new assignment record with type USER and updates the asset's
     * assigned user fields. The asset must be in an assignable status (IN_USE,
     * DEPLOYED, or STORAGE) and must not be currently assigned.
     * 
     * <p>This operation:
     * <ul>
     *   <li>Validates user has ADMINISTRATOR or ASSET_MANAGER role</li>
     *   <li>Checks asset is not already assigned</li>
     *   <li>Validates asset status is assignable</li>
     *   <li>Creates assignment history record</li>
     *   <li>Updates asset assignment fields</li>
     *   <li>Logs operation to audit service</li>
     * </ul>
     *
     * @param userId the ID of the user performing the assignment
     * @param assetId the ID of the asset to assign
     * @param request the assignment request containing user details
     * @return the created assignment DTO
     * @throws com.company.assetmanagement.exception.InsufficientPermissionsException 
     *         if user lacks required permissions
     * @throws com.company.assetmanagement.exception.ResourceNotFoundException 
     *         if asset does not exist
     * @throws com.company.assetmanagement.exception.ValidationException 
     *         if request data is invalid
     * @throws AssetAlreadyAssignedException 
     *         if asset is already assigned
     * @throws AssetNotAssignableException 
     *         if asset status is not assignable
     */
    AssignmentDTO assignToUser(String userId, UUID assetId, AssignmentRequest request);
    
    /**
     * Assigns an asset to a location.
     * 
     * Creates a new assignment record with type LOCATION and updates the asset's
     * location field. The asset must be in an assignable status (IN_USE, DEPLOYED,
     * or STORAGE) and must not be currently assigned.
     * 
     * <p>This operation:
     * <ul>
     *   <li>Validates user has ADMINISTRATOR or ASSET_MANAGER role</li>
     *   <li>Checks asset is not already assigned</li>
     *   <li>Validates asset status is assignable</li>
     *   <li>Creates assignment history record</li>
     *   <li>Updates asset location field</li>
     *   <li>Logs operation to audit service</li>
     * </ul>
     *
     * @param userId the ID of the user performing the assignment
     * @param assetId the ID of the asset to assign
     * @param request the assignment request containing location details
     * @return the created assignment DTO
     * @throws com.company.assetmanagement.exception.InsufficientPermissionsException 
     *         if user lacks required permissions
     * @throws com.company.assetmanagement.exception.ResourceNotFoundException 
     *         if asset does not exist
     * @throws com.company.assetmanagement.exception.ValidationException 
     *         if request data is invalid
     * @throws AssetAlreadyAssignedException 
     *         if asset is already assigned
     * @throws AssetNotAssignableException 
     *         if asset status is not assignable
     */
    AssignmentDTO assignToLocation(String userId, UUID assetId, AssignmentRequest request);
    
    /**
     * Deallocates an asset by removing its current assignment.
     * 
     * Sets the UnassignedAt timestamp on the current assignment record and clears
     * all asset assignment fields. The asset must have an active assignment.
     * 
     * <p>This operation:
     * <ul>
     *   <li>Validates user has ADMINISTRATOR or ASSET_MANAGER role</li>
     *   <li>Checks asset has an active assignment</li>
     *   <li>Closes assignment history record</li>
     *   <li>Clears asset assignment fields</li>
     *   <li>Logs operation to audit service</li>
     * </ul>
     *
     * @param userId the ID of the user performing the deallocation
     * @param assetId the ID of the asset to deallocate
     * @throws com.company.assetmanagement.exception.InsufficientPermissionsException 
     *         if user lacks required permissions
     * @throws com.company.assetmanagement.exception.ResourceNotFoundException 
     *         if asset does not exist
     * @throws AssetNotAssignedException 
     *         if asset is not currently assigned
     */
    void deallocate(String userId, UUID assetId);
    
    /**
     * Reassigns an asset from its current assignment to a new user or location.
     * 
     * Atomically closes the current assignment and creates a new assignment in a
     * single transaction. This is more efficient than separate deallocate and
     * assign operations.
     * 
     * <p>This operation:
     * <ul>
     *   <li>Validates user has ADMINISTRATOR or ASSET_MANAGER role</li>
     *   <li>Checks asset has an active assignment</li>
     *   <li>Validates new assignment details</li>
     *   <li>Closes current assignment record</li>
     *   <li>Creates new assignment record</li>
     *   <li>Updates asset assignment fields</li>
     *   <li>Logs both operations to audit service</li>
     * </ul>
     * 
     * <p>The operation is atomic - both the deallocation and new allocation
     * succeed or both fail to maintain data consistency.
     *
     * @param userId the ID of the user performing the reassignment
     * @param assetId the ID of the asset to reassign
     * @param request the assignment request containing new assignment details
     * @return the created assignment DTO
     * @throws com.company.assetmanagement.exception.InsufficientPermissionsException 
     *         if user lacks required permissions
     * @throws com.company.assetmanagement.exception.ResourceNotFoundException 
     *         if asset does not exist
     * @throws com.company.assetmanagement.exception.ValidationException 
     *         if request data is invalid
     * @throws AssetNotAssignedException 
     *         if asset is not currently assigned
     */
    AssignmentDTO reassign(String userId, UUID assetId, AssignmentRequest request);
    
    /**
     * Retrieves the complete assignment history for an asset.
     * 
     * Returns all assignment records (both active and historical) for the specified
     * asset, ordered by assignment date descending (most recent first). Supports
     * pagination for assets with many assignments.
     * 
     * <p>Authorization: Requires ADMINISTRATOR, ASSET_MANAGER, or VIEWER role.
     *
     * @param userId the ID of the user requesting the history
     * @param assetId the ID of the asset
     * @param pageable pagination information (page number, size, sort)
     * @return page of assignment history records
     * @throws com.company.assetmanagement.exception.InsufficientPermissionsException 
     *         if user lacks required permissions
     * @throws com.company.assetmanagement.exception.ResourceNotFoundException 
     *         if asset does not exist
     */
    Page<AssignmentHistoryDTO> getAssignmentHistory(String userId, UUID assetId, Pageable pageable);
    
    /**
     * Queries all assets currently assigned to a specific user.
     * 
     * Returns assets where the AssignedUser field matches the specified user name
     * and the assignment is active (UnassignedAt is null). Supports pagination
     * and case-insensitive matching.
     * 
     * <p>Authorization: Requires ADMINISTRATOR, ASSET_MANAGER, or VIEWER role.
     *
     * @param userName the name of the user (case-insensitive)
     * @param pageable pagination information (page number, size, sort)
     * @return page of assets assigned to the user
     */
    Page<AssetDTO> getAssetsByUser(String userName, Pageable pageable);
    
    /**
     * Queries all assets currently assigned to a specific location.
     * 
     * Returns assets where the Location field matches the specified location name
     * and the assignment is active (UnassignedAt is null). Supports pagination
     * and case-insensitive matching.
     * 
     * <p>Authorization: Requires ADMINISTRATOR, ASSET_MANAGER, or VIEWER role.
     *
     * @param location the location name (case-insensitive)
     * @param pageable pagination information (page number, size, sort)
     * @return page of assets assigned to the location
     */
    Page<AssetDTO> getAssetsByLocation(String location, Pageable pageable);
    
    /**
     * Retrieves assignment statistics and metrics.
     * 
     * Returns comprehensive statistics including:
     * <ul>
     *   <li>Total count of currently assigned assets</li>
     *   <li>Count of assets assigned to users vs locations</li>
     *   <li>Count of available (unassigned) assets by status</li>
     *   <li>Top 10 users by number of assigned assets</li>
     *   <li>Top 10 locations by number of assigned assets</li>
     * </ul>
     * 
     * <p>Statistics are calculated efficiently using database aggregation queries.
     * 
     * <p>Authorization: Requires ADMINISTRATOR or ASSET_MANAGER role.
     *
     * @param userId the ID of the user requesting statistics
     * @return assignment statistics DTO
     * @throws com.company.assetmanagement.exception.InsufficientPermissionsException 
     *         if user lacks required permissions
     */
    AssignmentStatisticsDTO getStatistics(String userId);
    
    /**
     * Deallocates multiple assets in a single operation.
     * 
     * Processes each deallocation independently - some may succeed while others fail.
     * Returns a result object containing lists of successful and failed deallocations
     * with detailed information for each.
     * 
     * <p>This operation:
     * <ul>
     *   <li>Validates user has ADMINISTRATOR or ASSET_MANAGER role once</li>
     *   <li>Processes each asset deallocation independently</li>
     *   <li>Continues processing even if some deallocations fail</li>
     *   <li>Logs each deallocation separately to audit service</li>
     *   <li>Returns detailed results for each asset (success or failure)</li>
     * </ul>
     * 
     * <p>Maximum bulk size is 50 assets per request to prevent performance issues.
     * 
     * <p>Authorization: Requires ADMINISTRATOR or ASSET_MANAGER role.
     *
     * @param userId the ID of the user performing the bulk deallocation
     * @param assetIds list of asset IDs to deallocate (maximum 50)
     * @return bulk deallocation result with success and failure lists
     * @throws com.company.assetmanagement.exception.InsufficientPermissionsException 
     *         if user lacks required permissions
     * @throws com.company.assetmanagement.exception.ValidationException 
     *         if request exceeds maximum bulk size
     */
    com.company.assetmanagement.dto.BulkDeallocationResult bulkDeallocate(String userId, List<UUID> assetIds);
    
    /**
     * Exports assignment data to CSV format with optional filtering.
     * 
     * Generates a CSV file containing current assignment data with columns for
     * asset ID, asset name, serial number, asset type, assignment type, assigned to,
     * assigned by, and assigned at timestamp. Supports filtering by assignment type,
     * date range, and assigned by user.
     * 
     * <p>Export is limited to maximum 10,000 records to prevent performance issues.
     * If the export would exceed this limit, an error is returned suggesting filters.
     * 
     * <p>This operation is logged to the audit service with action type EXPORT_DATA
     * and resource type ASSIGNMENT, including metadata with record count and applied filters.
     * 
     * <p>Authorization: Requires ADMINISTRATOR or ASSET_MANAGER role.
     *
     * @param userId the ID of the user performing the export
     * @param filters map of filter criteria (assignmentType, dateFrom, dateTo, assignedBy)
     * @return byte array containing CSV data
     * @throws com.company.assetmanagement.exception.InsufficientPermissionsException 
     *         if user lacks required permissions
     * @throws com.company.assetmanagement.exception.ValidationException 
     *         if export would exceed maximum size
     */
    byte[] exportAssignments(String userId, java.util.Map<String, Object> filters);
}
