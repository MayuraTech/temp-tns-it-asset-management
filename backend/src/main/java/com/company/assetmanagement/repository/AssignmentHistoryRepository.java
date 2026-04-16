package com.company.assetmanagement.repository;

import com.company.assetmanagement.model.AssignmentHistory;
import com.company.assetmanagement.model.AssignmentType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repository for AssignmentHistory entities.
 * Provides query methods for tracking asset assignment history, including both active
 * and historical assignments.
 * 
 * <p>This repository supports:
 * <ul>
 *   <li>Querying assignment history by asset ID</li>
 *   <li>Searching assignments by assigned to value (user name or location)</li>
 *   <li>Finding active assignments for assets</li>
 *   <li>Aggregating assignment statistics</li>
 * </ul>
 * 
 * @see AssignmentHistory
 */
@Repository
public interface AssignmentHistoryRepository extends JpaRepository<AssignmentHistory, UUID> {
    
    /**
     * Find all assignment history records for a specific asset, ordered by assignment date descending.
     * Returns both active assignments (UnassignedAt is null) and historical assignments.
     *
     * @param assetId the asset identifier
     * @param pageable pagination information
     * @return page of assignment history records ordered by most recent first
     */
    Page<AssignmentHistory> findByAssetIdOrderByAssignedAtDesc(UUID assetId, Pageable pageable);
    
    /**
     * Search assignment history by assigned to value (user name or location) with case-insensitive matching.
     * Useful for finding all assignments for a specific user or location across all assets.
     *
     * @param assignedTo the user name or location to search for (partial match supported)
     * @param pageable pagination information
     * @return page of assignment history records matching the search criteria
     */
    Page<AssignmentHistory> findByAssignedToContainingIgnoreCase(String assignedTo, Pageable pageable);
    
    /**
     * Find active assignments for a specific asset.
     * Active assignments are those where UnassignedAt is null.
     *
     * @param assetId the asset identifier
     * @return list of active assignment records (typically 0 or 1 record)
     */
    @Query("SELECT ah FROM AssignmentHistory ah WHERE ah.assetId = :assetId AND ah.unassignedAt IS NULL")
    List<AssignmentHistory> findActiveAssignmentsByAssetId(@Param("assetId") UUID assetId);
    
    /**
     * Get assignment statistics including total counts by assignment type.
     * Returns aggregated data for dashboard and reporting purposes.
     *
     * @return list of objects containing assignment type and count
     */
    @Query("SELECT ah.assignmentType, COUNT(ah) FROM AssignmentHistory ah " +
           "WHERE ah.unassignedAt IS NULL " +
           "GROUP BY ah.assignmentType")
    List<Object[]> getAssignmentStatistics();
    
    /**
     * Get top users by number of currently assigned assets.
     * Returns the assigned to value and count for user assignments only.
     *
     * @param pageable pagination information (use to limit to top N)
     * @return list of objects containing assigned to value and count
     */
    @Query("SELECT ah.assignedTo, COUNT(ah) FROM AssignmentHistory ah " +
           "WHERE ah.unassignedAt IS NULL AND ah.assignmentType = :assignmentType " +
           "GROUP BY ah.assignedTo " +
           "ORDER BY COUNT(ah) DESC")
    List<Object[]> getTopAssignmentsByType(@Param("assignmentType") AssignmentType assignmentType, Pageable pageable);
    
    /**
     * Count total active assignments.
     *
     * @return count of active assignments
     */
    @Query("SELECT COUNT(ah) FROM AssignmentHistory ah WHERE ah.unassignedAt IS NULL")
    long countActiveAssignments();
    
    /**
     * Find all assignment history records by assignment type.
     *
     * @param assignmentType the assignment type (USER or LOCATION)
     * @param pageable pagination information
     * @return page of assignment history records
     */
    Page<AssignmentHistory> findByAssignmentType(AssignmentType assignmentType, Pageable pageable);
    
    /**
     * Find active assignments by assignment type.
     *
     * @param assignmentType the assignment type (USER or LOCATION)
     * @return list of active assignment records
     */
    @Query("SELECT ah FROM AssignmentHistory ah " +
           "WHERE ah.assignmentType = :assignmentType AND ah.unassignedAt IS NULL")
    List<AssignmentHistory> findActiveAssignmentsByType(@Param("assignmentType") AssignmentType assignmentType);
    
    /**
     * Find all assignments by assigned by user ID.
     * Useful for tracking which user performed assignments.
     *
     * @param assignedBy the user ID who performed the assignment
     * @param pageable pagination information
     * @return page of assignment history records
     */
    Page<AssignmentHistory> findByAssignedBy(UUID assignedBy, Pageable pageable);
    
    /**
     * Find active assignments with optional filtering by type, date range, and assigned by user.
     * This method supports flexible filtering for export operations.
     *
     * @param assignmentType optional assignment type filter (USER or LOCATION)
     * @param dateFrom optional start date for assignedAt filter (inclusive)
     * @param dateTo optional end date for assignedAt filter (inclusive)
     * @param assignedBy optional user ID who performed the assignment
     * @return list of assignment history records matching the filters
     */
    @Query("SELECT ah FROM AssignmentHistory ah " +
           "WHERE ah.unassignedAt IS NULL " +
           "AND (:assignmentType IS NULL OR ah.assignmentType = :assignmentType) " +
           "AND (:dateFrom IS NULL OR ah.assignedAt >= :dateFrom) " +
           "AND (:dateTo IS NULL OR ah.assignedAt <= :dateTo) " +
           "AND (:assignedBy IS NULL OR ah.assignedBy = :assignedBy) " +
           "ORDER BY ah.assignedAt DESC")
    List<AssignmentHistory> findActiveAssignmentsWithFilters(
        @Param("assignmentType") AssignmentType assignmentType,
        @Param("dateFrom") java.time.LocalDateTime dateFrom,
        @Param("dateTo") java.time.LocalDateTime dateTo,
        @Param("assignedBy") UUID assignedBy
    );
}
