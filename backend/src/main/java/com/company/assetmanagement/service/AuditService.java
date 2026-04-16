package com.company.assetmanagement.service;

import com.company.assetmanagement.dto.AuditEventDTO;
import com.company.assetmanagement.dto.AuditLogDTO;
import com.company.assetmanagement.dto.FieldChangeDTO;
import com.company.assetmanagement.model.Action;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
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
    
    // ========== User Management Audit Logging Convenience Methods ==========
    
    /**
     * Log successful authentication event.
     *
     * @param userId the authenticated user ID
     * @param username the username
     * @param ipAddress the IP address of the login attempt
     */
    void logAuthenticationSuccess(UUID userId, String username, String ipAddress);
    
    /**
     * Log failed authentication event.
     *
     * @param userId the user ID (may be null if user not found)
     * @param username the attempted username
     * @param reason the failure reason
     * @param ipAddress the IP address of the login attempt
     */
    void logAuthenticationFailure(UUID userId, String username, String reason, String ipAddress);
    
    /**
     * Log logout event.
     *
     * @param userId the user ID
     * @param username the username
     * @param ipAddress the IP address
     */
    void logLogout(UUID userId, String username, String ipAddress);
    
    /**
     * Log token refresh event.
     *
     * @param userId the user ID
     * @param username the username
     * @param ipAddress the IP address
     */
    void logTokenRefresh(UUID userId, String username, String ipAddress);
    
    /**
     * Log user creation event.
     *
     * @param creatorId the ID of the user creating the account
     * @param creatorUsername the username of the creator
     * @param newUserId the ID of the newly created user
     * @param newUsername the username of the newly created user
     * @param assignedRoles the roles assigned to the new user
     * @param ipAddress the IP address
     */
    void logUserCreation(UUID creatorId, String creatorUsername, UUID newUserId, 
                        String newUsername, List<String> assignedRoles, String ipAddress);
    
    /**
     * Log user update event.
     *
     * @param updaterId the ID of the user performing the update
     * @param updaterUsername the username of the updater
     * @param targetUserId the ID of the user being updated
     * @param targetUsername the username of the user being updated
     * @param changes map of field changes
     * @param ipAddress the IP address
     */
    void logUserUpdate(UUID updaterId, String updaterUsername, UUID targetUserId, 
                      String targetUsername, Map<String, FieldChangeDTO> changes, String ipAddress);
    
    /**
     * Log user deletion event.
     *
     * @param deleterId the ID of the user performing the deletion
     * @param deleterUsername the username of the deleter
     * @param deletedUserId the ID of the deleted user
     * @param deletedUsername the username of the deleted user
     * @param ipAddress the IP address
     */
    void logUserDeletion(UUID deleterId, String deleterUsername, UUID deletedUserId, 
                        String deletedUsername, String ipAddress);
    
    /**
     * Log role assignment event.
     *
     * @param adminId the ID of the administrator assigning the role
     * @param adminUsername the username of the administrator
     * @param targetUserId the ID of the user receiving the role
     * @param targetUsername the username of the user receiving the role
     * @param role the role being assigned
     * @param ipAddress the IP address
     */
    void logRoleAssignment(UUID adminId, String adminUsername, UUID targetUserId, 
                          String targetUsername, String role, String ipAddress);
    
    /**
     * Log role revocation event.
     *
     * @param adminId the ID of the administrator revoking the role
     * @param adminUsername the username of the administrator
     * @param targetUserId the ID of the user losing the role
     * @param targetUsername the username of the user losing the role
     * @param role the role being revoked
     * @param ipAddress the IP address
     */
    void logRoleRevocation(UUID adminId, String adminUsername, UUID targetUserId, 
                          String targetUsername, String role, String ipAddress);
    
    /**
     * Log password change event.
     * Note: Password values are never logged for security.
     *
     * @param userId the ID of the user changing password
     * @param username the username
     * @param ipAddress the IP address
     */
    void logPasswordChange(UUID userId, String username, String ipAddress);
    
    /**
     * Log user account enable event.
     *
     * @param adminId the ID of the administrator enabling the account
     * @param adminUsername the username of the administrator
     * @param targetUserId the ID of the user being enabled
     * @param targetUsername the username of the user being enabled
     * @param ipAddress the IP address
     */
    void logUserEnable(UUID adminId, String adminUsername, UUID targetUserId, 
                      String targetUsername, String ipAddress);
    
    /**
     * Log user account disable event.
     *
     * @param adminId the ID of the administrator disabling the account
     * @param adminUsername the username of the administrator
     * @param targetUserId the ID of the user being disabled
     * @param targetUsername the username of the user being disabled
     * @param ipAddress the IP address
     */
    void logUserDisable(UUID adminId, String adminUsername, UUID targetUserId, 
                       String targetUsername, String ipAddress);
}
