package com.company.assetmanagement.service;

import com.company.assetmanagement.dto.AuditEventDTO;
import com.company.assetmanagement.dto.AuditLogDTO;
import com.company.assetmanagement.dto.FieldChangeDTO;
import com.company.assetmanagement.model.Action;
import com.company.assetmanagement.model.AuditLog;
import com.company.assetmanagement.repository.AuditLogRepository;
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
    private final ObjectMapper objectMapper;
    
    public AuditServiceImpl(AuditLogRepository auditLogRepository, ObjectMapper objectMapper) {
        this.auditLogRepository = auditLogRepository;
        this.objectMapper = objectMapper;
    }
    
    @Override
    @Transactional
    public void logEvent(AuditEventDTO event) {
        try {
            AuditLog auditLog = new AuditLog();
            auditLog.setTimestamp(event.getTimestamp() != null ? event.getTimestamp() : LocalDateTime.now());
            auditLog.setUserId(event.getUserId());
            auditLog.setUsername(event.getUsername());
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
}
