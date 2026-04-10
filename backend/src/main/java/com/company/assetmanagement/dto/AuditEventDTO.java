package com.company.assetmanagement.dto;

import com.company.assetmanagement.model.Action;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * Data Transfer Object for audit events.
 * Used to capture audit information before persisting to the audit log.
 */
public class AuditEventDTO {
    
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
    public AuditEventDTO() {
        this.timestamp = LocalDateTime.now();
    }
    
    // Builder pattern
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private final AuditEventDTO event = new AuditEventDTO();
        
        public Builder timestamp(LocalDateTime timestamp) {
            event.timestamp = timestamp;
            return this;
        }
        
        public Builder userId(UUID userId) {
            event.userId = userId;
            return this;
        }
        
        public Builder username(String username) {
            event.username = username;
            return this;
        }
        
        public Builder actionType(Action actionType) {
            event.actionType = actionType;
            return this;
        }
        
        public Builder resourceType(String resourceType) {
            event.resourceType = resourceType;
            return this;
        }
        
        public Builder resourceId(String resourceId) {
            event.resourceId = resourceId;
            return this;
        }
        
        public Builder changes(Map<String, FieldChangeDTO> changes) {
            event.changes = changes;
            return this;
        }
        
        public Builder metadata(Map<String, Object> metadata) {
            event.metadata = metadata;
            return this;
        }
        
        public Builder ipAddress(String ipAddress) {
            event.ipAddress = ipAddress;
            return this;
        }
        
        public AuditEventDTO build() {
            return event;
        }
    }
    
    // Getters and Setters
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
