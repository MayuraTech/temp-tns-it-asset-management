package com.company.assetmanagement.repository;

import com.company.assetmanagement.model.Action;
import com.company.assetmanagement.model.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Repository for AuditLog entities.
 * Provides query methods for searching and filtering audit log entries.
 * 
 * Note: This repository does not provide update or delete methods as audit logs are immutable.
 */
@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, UUID> {
    
    /**
     * Find all audit log entries for a specific resource.
     *
     * @param resourceId the resource identifier
     * @return list of audit log entries
     */
    List<AuditLog> findByResourceIdOrderByTimestampDesc(String resourceId);
    
    /**
     * Find audit log entries by user ID.
     *
     * @param userId the user identifier
     * @param pageable pagination information
     * @return page of audit log entries
     */
    Page<AuditLog> findByUserId(UUID userId, Pageable pageable);
    
    /**
     * Find audit log entries by action type.
     *
     * @param actionType the action type
     * @param pageable pagination information
     * @return page of audit log entries
     */
    Page<AuditLog> findByActionType(Action actionType, Pageable pageable);
    
    /**
     * Find audit log entries by resource type.
     *
     * @param resourceType the resource type
     * @param pageable pagination information
     * @return page of audit log entries
     */
    Page<AuditLog> findByResourceType(String resourceType, Pageable pageable);
    
    /**
     * Find audit log entries within a date range.
     *
     * @param startDate the start date
     * @param endDate the end date
     * @param pageable pagination information
     * @return page of audit log entries
     */
    Page<AuditLog> findByTimestampBetween(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);
    
    /**
     * Search audit log entries with multiple filters.
     *
     * @param userId optional user ID filter
     * @param actionType optional action type filter
     * @param resourceType optional resource type filter
     * @param resourceId optional resource ID filter
     * @param startDate optional start date filter
     * @param endDate optional end date filter
     * @param pageable pagination information
     * @return page of audit log entries matching the filters
     */
    @Query("SELECT a FROM AuditLog a WHERE " +
           "(:userId IS NULL OR a.userId = :userId) AND " +
           "(:actionType IS NULL OR a.actionType = :actionType) AND " +
           "(:resourceType IS NULL OR a.resourceType = :resourceType) AND " +
           "(:resourceId IS NULL OR a.resourceId = :resourceId) AND " +
           "(:startDate IS NULL OR a.timestamp >= :startDate) AND " +
           "(:endDate IS NULL OR a.timestamp <= :endDate) " +
           "ORDER BY a.timestamp DESC")
    Page<AuditLog> searchAuditLog(
        @Param("userId") UUID userId,
        @Param("actionType") Action actionType,
        @Param("resourceType") String resourceType,
        @Param("resourceId") String resourceId,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate,
        Pageable pageable
    );
}
