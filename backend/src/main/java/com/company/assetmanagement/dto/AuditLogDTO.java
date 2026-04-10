package com.company.assetmanagement.dto;

import com.company.assetmanagement.model.Action;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * Data Transfer Object for AuditLog entity.
 * Used to return audit log information to clients.
 */
public class AuditLogDTO {
    
    private UUID id;
    private LocalDateTime timestamp;
    private UUID userId;
    private String username;
    private Action actionType;
    private String resourceType;
    private String resourceId;
    private Map<String, FieldChangeDTO> changes;
    private Map<String, Object> metadata;
    private String ipAddress;
    
    // Constructors
    public AuditLogDTO() {
    }
    
    // Getters and Setters
    public UUID getId() {
        return id;
    }
    
    public void setId(UUID id) {
        this.id = id;
    }
    
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
    
    public UUID getUserId() {
        return userId;
    }
    
    public void setUserId(UUID userId) {
        this.userId = userId;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public Action getActionType() {
        return actionType;
    }
    
    public void setActionType(Action actionType) {
        this.actionType = actionType;
    }
    
    public String getResourceType() {
        return resourceType;
    }
    
    public void setResourceType(String resourceType) {
        this.resourceType = resourceType;
    }
    
    public String getResourceId() {
        return resourceId;
    }
    
    public void setResourceId(String resourceId) {
        this.resourceId = resourceId;
    }
    
    public Map<String, FieldChangeDTO> getChanges() {
        return changes;
    }
    
    public void setChanges(Map<String, FieldChangeDTO> changes) {
        this.changes = changes;
    }
    
    public Map<String, Object> getMetadata() {
        return metadata;
    }
    
    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }
    
    public String getIpAddress() {
        return ipAddress;
    }
    
    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }
}
