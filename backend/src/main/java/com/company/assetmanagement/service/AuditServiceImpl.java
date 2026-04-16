package com.company.assetmanagement.service;

import com.company.assetmanagement.dto.AuditEventDTO;
import com.company.assetmanagement.dto.AuditLogDTO;
import com.company.assetmanagement.dto.FieldChangeDTO;
import com.company.assetmanagement.model.Action;
import com.company.assetmanagement.model.AuditLog;
import com.company.assetmanagement.model.User;
import com.company.assetmanagement.repository.AuditLogRepository;
import com.company.assetmanagement.repository.UserRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Implementation of AuditService.
 * 
 * Handles audit logging for all system operations.
 * Audit logs are immutable and retained for compliance requirements.
 */
@Service
@Transactional(readOnly = true)
public class AuditServiceImpl implements AuditService {
    
    private static final Logger logger = LoggerFactory.getLogger(AuditServiceImpl.class);
    
    private final AuditLogRepository auditLogRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;
    
    public AuditServiceImpl(
            AuditLogRepository auditLogRepository,
            UserRepository userRepository,
            ObjectMapper objectMapper) {
        this.auditLogRepository = auditLogRepository;
        this.userRepository = userRepository;
        this.objectMapper = objectMapper;
    }
    
    @Override
    @Transactional
    public void logEvent(AuditEventDTO event) {
        try {
            AuditLog auditLog = new AuditLog();
            auditLog.setTimestamp(event.getTimestamp() != null ? event.getTimestamp() : LocalDateTime.now());
            auditLog.setUserId(event.getUserId());
            auditLog.setUsername(resolveUsernameForAudit(event));
            auditLog.setActionType(event.getActionType());
            auditLog.setResourceType(event.getResourceType());
            auditLog.setResourceId(event.getResourceId());
            auditLog.setIpAddress(event.getIpAddress());
            
            // Serialize changes to JSON
            if (event.getChanges() != null && !event.getChanges().isEmpty()) {
                try {
                    String changesJson = objectMapper.writeValueAsString(event.getChanges());
                    auditLog.setChanges(changesJson);
                } catch (JsonProcessingException e) {
                    logger.error("Failed to serialize audit log changes", e);
                }
            }
            
            // Serialize metadata to JSON
            if (event.getMetadata() != null && !event.getMetadata().isEmpty()) {
                try {
                    String metadataJson = objectMapper.writeValueAsString(event.getMetadata());
                    auditLog.setMetadata(metadataJson);
                } catch (JsonProcessingException e) {
                    logger.error("Failed to serialize audit log metadata", e);
                }
            }
            
            auditLogRepository.save(auditLog);
            
            logger.debug("Audit log created: userId={}, action={}, resource={}/{}", 
                event.getUserId(), event.getActionType(), event.getResourceType(), event.getResourceId());
            
        } catch (Exception e) {
            logger.error("Failed to create audit log entry", e);
            // Don't throw exception - audit logging should not break business operations
        }
    }

    /**
     * SQL Server and the {@link AuditLog} entity require a non-null username. Callers often
     * only supply {@code userId}; resolve the login name from the user table when needed.
     */
    private String resolveUsernameForAudit(AuditEventDTO event) {
        String username = event.getUsername();
        if (username != null && !username.isBlank()) {
            return username;
        }
        UUID userId = event.getUserId();
        if (userId != null) {
            return userRepository.findById(userId)
                .map(User::getUsername)
                .filter(u -> u != null && !u.isBlank())
                .orElseGet(() -> "id:" + userId);
        }
        return "unknown";
    }
    
    @Override
    public Page<AuditLogDTO> searchAuditLog(
            UUID userId,
            Action actionType,
            String resourceType,
            String resourceId,
            LocalDateTime startDate,
            LocalDateTime endDate,
            Pageable pageable) {
        
        Page<AuditLog> auditLogs = auditLogRepository.searchAuditLog(
            userId, actionType, resourceType, resourceId, startDate, endDate, pageable
        );
        
        return auditLogs.map(this::mapToDTO);
    }
    
    @Override
    public List<AuditLogDTO> getResourceAuditTrail(String resourceId) {
        List<AuditLog> auditLogs = auditLogRepository.findByResourceIdOrderByTimestampDesc(resourceId);
        return auditLogs.stream()
            .map(this::mapToDTO)
            .collect(Collectors.toList());
    }
    
    @Override
    public AuditLogDTO getAuditLogById(UUID id) {
        return auditLogRepository.findById(id)
            .map(this::mapToDTO)
            .orElse(null);
    }
    
    /**
     * Map AuditLog entity to DTO.
     *
     * @param auditLog the audit log entity
     * @return the audit log DTO
     */
    private AuditLogDTO mapToDTO(AuditLog auditLog) {
        AuditLogDTO dto = new AuditLogDTO();
        dto.setId(auditLog.getId());
        dto.setTimestamp(auditLog.getTimestamp());
        dto.setUserId(auditLog.getUserId());
        dto.setUsername(auditLog.getUsername());
        dto.setActionType(auditLog.getActionType());
        dto.setResourceType(auditLog.getResourceType());
        dto.setResourceId(auditLog.getResourceId());
        dto.setIpAddress(auditLog.getIpAddress());
        
        // Deserialize changes from JSON
        if (auditLog.getChanges() != null && !auditLog.getChanges().isEmpty()) {
            try {
                Map<String, FieldChangeDTO> changes = objectMapper.readValue(
                    auditLog.getChanges(),
                    new TypeReference<Map<String, FieldChangeDTO>>() {}
                );
                dto.setChanges(changes);
            } catch (JsonProcessingException e) {
                logger.error("Failed to deserialize audit log changes", e);
            }
        }
        
        // Deserialize metadata from JSON
        if (auditLog.getMetadata() != null && !auditLog.getMetadata().isEmpty()) {
            try {
                Map<String, Object> metadata = objectMapper.readValue(
                    auditLog.getMetadata(),
                    new TypeReference<Map<String, Object>>() {}
                );
                dto.setMetadata(metadata);
            } catch (JsonProcessingException e) {
                logger.error("Failed to deserialize audit log metadata", e);
            }
        }
        
        return dto;
    }
    
    // ========== User Management Audit Logging Convenience Methods ==========
    
    @Override
    @Transactional
    public void logAuthenticationSuccess(UUID userId, String username, String ipAddress) {
        try {
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("success", true);
            metadata.put("timestamp", LocalDateTime.now());
            
            AuditEventDTO event = AuditEventDTO.builder()
                    .userId(userId)
                    .username(username)
                    .actionType(Action.LOGIN_SUCCESS)
                    .resourceType("USER")
                    .resourceId(userId.toString())
                    .metadata(metadata)
                    .ipAddress(ipAddress)
                    .build();
            
            logEvent(event);
            logger.debug("Logged authentication success for user: {}", username);
        } catch (Exception e) {
            logger.error("Failed to log authentication success audit event", e);
        }
    }
    
    @Override
    @Transactional
    public void logAuthenticationFailure(UUID userId, String username, String reason, String ipAddress) {
        try {
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("success", false);
            metadata.put("reason", reason);
            metadata.put("timestamp", LocalDateTime.now());
            
            AuditEventDTO event = AuditEventDTO.builder()
                    .userId(userId)
                    .username(username)
                    .actionType(Action.LOGIN_FAILURE)
                    .resourceType("USER")
                    .resourceId(userId != null ? userId.toString() : "unknown")
                    .metadata(metadata)
                    .ipAddress(ipAddress)
                    .build();
            
            logEvent(event);
            logger.debug("Logged authentication failure for user: {} - reason: {}", username, reason);
        } catch (Exception e) {
            logger.error("Failed to log authentication failure audit event", e);
        }
    }
    
    @Override
    @Transactional
    public void logLogout(UUID userId, String username, String ipAddress) {
        try {
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("action", "logout");
            metadata.put("timestamp", LocalDateTime.now());
            
            AuditEventDTO event = AuditEventDTO.builder()
                    .userId(userId)
                    .username(username)
                    .actionType(Action.LOGOUT)
                    .resourceType("USER")
                    .resourceId(userId.toString())
                    .metadata(metadata)
                    .ipAddress(ipAddress)
                    .build();
            
            logEvent(event);
            logger.debug("Logged logout for user: {}", username);
        } catch (Exception e) {
            logger.error("Failed to log logout audit event", e);
        }
    }
    
    @Override
    @Transactional
    public void logTokenRefresh(UUID userId, String username, String ipAddress) {
        try {
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("action", "token_refresh");
            metadata.put("timestamp", LocalDateTime.now());
            
            AuditEventDTO event = AuditEventDTO.builder()
                    .userId(userId)
                    .username(username)
                    .actionType(Action.TOKEN_REFRESH)
                    .resourceType("USER")
                    .resourceId(userId.toString())
                    .metadata(metadata)
                    .ipAddress(ipAddress)
                    .build();
            
            logEvent(event);
            logger.debug("Logged token refresh for user: {}", username);
        } catch (Exception e) {
            logger.error("Failed to log token refresh audit event", e);
        }
    }
    
    @Override
    @Transactional
    public void logUserCreation(UUID creatorId, String creatorUsername, UUID newUserId, 
                               String newUsername, List<String> assignedRoles, String ipAddress) {
        try {
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("newUserId", newUserId.toString());
            metadata.put("newUsername", newUsername);
            metadata.put("assignedRoles", assignedRoles);
            metadata.put("timestamp", LocalDateTime.now());
            
            AuditEventDTO event = AuditEventDTO.builder()
                    .userId(creatorId)
                    .username(creatorUsername)
                    .actionType(Action.CREATE_USER)
                    .resourceType("USER")
                    .resourceId(newUserId.toString())
                    .metadata(metadata)
                    .ipAddress(ipAddress)
                    .build();
            
            logEvent(event);
            logger.debug("Logged user creation: {} created user {}", creatorUsername, newUsername);
        } catch (Exception e) {
            logger.error("Failed to log user creation audit event", e);
        }
    }
    
    @Override
    @Transactional
    public void logUserUpdate(UUID updaterId, String updaterUsername, UUID targetUserId, 
                             String targetUsername, Map<String, FieldChangeDTO> changes, String ipAddress) {
        try {
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("targetUserId", targetUserId.toString());
            metadata.put("targetUsername", targetUsername);
            metadata.put("fieldsChanged", changes.keySet());
            metadata.put("timestamp", LocalDateTime.now());
            
            AuditEventDTO event = AuditEventDTO.builder()
                    .userId(updaterId)
                    .username(updaterUsername)
                    .actionType(Action.UPDATE_USER)
                    .resourceType("USER")
                    .resourceId(targetUserId.toString())
                    .changes(changes)
                    .metadata(metadata)
                    .ipAddress(ipAddress)
                    .build();
            
            logEvent(event);
            logger.debug("Logged user update: {} updated user {}", updaterUsername, targetUsername);
        } catch (Exception e) {
            logger.error("Failed to log user update audit event", e);
        }
    }
    
    @Override
    @Transactional
    public void logUserDeletion(UUID deleterId, String deleterUsername, UUID deletedUserId, 
                               String deletedUsername, String ipAddress) {
        try {
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("deletedUserId", deletedUserId.toString());
            metadata.put("deletedUsername", deletedUsername);
            metadata.put("timestamp", LocalDateTime.now());
            
            AuditEventDTO event = AuditEventDTO.builder()
                    .userId(deleterId)
                    .username(deleterUsername)
                    .actionType(Action.DELETE_USER)
                    .resourceType("USER")
                    .resourceId(deletedUserId.toString())
                    .metadata(metadata)
                    .ipAddress(ipAddress)
                    .build();
            
            logEvent(event);
            logger.debug("Logged user deletion: {} deleted user {}", deleterUsername, deletedUsername);
        } catch (Exception e) {
            logger.error("Failed to log user deletion audit event", e);
        }
    }
    
    @Override
    @Transactional
    public void logRoleAssignment(UUID adminId, String adminUsername, UUID targetUserId, 
                                 String targetUsername, String role, String ipAddress) {
        try {
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("targetUserId", targetUserId.toString());
            metadata.put("targetUsername", targetUsername);
            metadata.put("role", role);
            metadata.put("action", "assign");
            metadata.put("timestamp", LocalDateTime.now());
            
            AuditEventDTO event = AuditEventDTO.builder()
                    .userId(adminId)
                    .username(adminUsername)
                    .actionType(Action.ASSIGN_ROLE)
                    .resourceType("USER")
                    .resourceId(targetUserId.toString())
                    .metadata(metadata)
                    .ipAddress(ipAddress)
                    .build();
            
            logEvent(event);
            logger.debug("Logged role assignment: {} assigned role {} to user {}", 
                        adminUsername, role, targetUsername);
        } catch (Exception e) {
            logger.error("Failed to log role assignment audit event", e);
        }
    }
    
    @Override
    @Transactional
    public void logRoleRevocation(UUID adminId, String adminUsername, UUID targetUserId, 
                                 String targetUsername, String role, String ipAddress) {
        try {
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("targetUserId", targetUserId.toString());
            metadata.put("targetUsername", targetUsername);
            metadata.put("role", role);
            metadata.put("action", "revoke");
            metadata.put("timestamp", LocalDateTime.now());
            
            AuditEventDTO event = AuditEventDTO.builder()
                    .userId(adminId)
                    .username(adminUsername)
                    .actionType(Action.REVOKE_ROLE)
                    .resourceType("USER")
                    .resourceId(targetUserId.toString())
                    .metadata(metadata)
                    .ipAddress(ipAddress)
                    .build();
            
            logEvent(event);
            logger.debug("Logged role revocation: {} revoked role {} from user {}", 
                        adminUsername, role, targetUsername);
        } catch (Exception e) {
            logger.error("Failed to log role revocation audit event", e);
        }
    }
    
    @Override
    @Transactional
    public void logPasswordChange(UUID userId, String username, String ipAddress) {
        try {
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("action", "password_change");
            metadata.put("timestamp", LocalDateTime.now());
            // IMPORTANT: Never log password values
            metadata.put("note", "Password values are not logged for security");
            
            AuditEventDTO event = AuditEventDTO.builder()
                    .userId(userId)
                    .username(username)
                    .actionType(Action.PASSWORD_CHANGE)
                    .resourceType("USER")
                    .resourceId(userId.toString())
                    .metadata(metadata)
                    .ipAddress(ipAddress)
                    .build();
            
            logEvent(event);
            logger.debug("Logged password change for user: {}", username);
        } catch (Exception e) {
            logger.error("Failed to log password change audit event", e);
        }
    }
    
    @Override
    @Transactional
    public void logUserEnable(UUID adminId, String adminUsername, UUID targetUserId, 
                             String targetUsername, String ipAddress) {
        try {
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("targetUserId", targetUserId.toString());
            metadata.put("targetUsername", targetUsername);
            metadata.put("action", "enable");
            metadata.put("timestamp", LocalDateTime.now());
            
            AuditEventDTO event = AuditEventDTO.builder()
                    .userId(adminId)
                    .username(adminUsername)
                    .actionType(Action.ENABLE_USER)
                    .resourceType("USER")
                    .resourceId(targetUserId.toString())
                    .metadata(metadata)
                    .ipAddress(ipAddress)
                    .build();
            
            logEvent(event);
            logger.debug("Logged user enable: {} enabled user {}", adminUsername, targetUsername);
        } catch (Exception e) {
            logger.error("Failed to log user enable audit event", e);
        }
    }
    
    @Override
    @Transactional
    public void logUserDisable(UUID adminId, String adminUsername, UUID targetUserId, 
                              String targetUsername, String ipAddress) {
        try {
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("targetUserId", targetUserId.toString());
            metadata.put("targetUsername", targetUsername);
            metadata.put("action", "disable");
            metadata.put("timestamp", LocalDateTime.now());
            
            AuditEventDTO event = AuditEventDTO.builder()
                    .userId(adminId)
                    .username(adminUsername)
                    .actionType(Action.DISABLE_USER)
                    .resourceType("USER")
                    .resourceId(targetUserId.toString())
                    .metadata(metadata)
                    .ipAddress(ipAddress)
                    .build();
            
            logEvent(event);
            logger.debug("Logged user disable: {} disabled user {}", adminUsername, targetUsername);
        } catch (Exception e) {
            logger.error("Failed to log user disable audit event", e);
        }
    }
}
