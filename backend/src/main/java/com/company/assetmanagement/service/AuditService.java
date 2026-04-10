package com.company.assetmanagement.service;

import com.company.assetmanagement.dto.AuditEventDTO;
import com.company.assetmanagement.dto.AuditLogDTO;
import com.company.assetmanagement.model.Action;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Service interface for audit logging operations.
 * 
 * Provides methods to log system events and search audit logs.
 * All audit log entries are immutable and cannot be modified or deleted.
 */
public interface AuditService {
    
    /**
     * Log an audit event.
     * Creates an immutable audit log entry for the specified event.
     *
     * @param event the audit event to log
     */
    void logEvent(AuditEventDTO event);
    
    /**
     * Search audit log entries with filtering.
     * Supports filtering by date range, user, action type, resource type, and resource ID.
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
    Page<AuditLogDTO> searchAuditLog(
        UUID userId,
        Action actionType,
        String resourceType,
        String resourceId,
        LocalDateTime startDate,
        LocalDateTime endDate,
        Pageable pageable
    );
    
    /**
     * Get audit trail for a specific resource.
     * Returns all audit log entries for the specified resource in chronological order.
     *
     * @param resourceId the resource identifier
     * @return list of audit log entries for the resource
     */
    List<AuditLogDTO> getResourceAuditTrail(String resourceId);
    
    /**
     * Get audit log entry by ID.
     *
     * @param id the audit log entry ID
     * @return the audit log entry, or null if not found
     */
    AuditLogDTO getAuditLogById(UUID id);
}
