package com.company.assetmanagement.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entity representing an immutable audit log entry.
 * Records all system operations for compliance and investigation.
 * 
 * Audit logs must be retained for a minimum of 7 years and cannot be modified or deleted.
 */
@Entity
@Table(name = "AuditLog", indexes = {
    @Index(name = "IX_AuditLog_Timestamp", columnList = "timestamp"),
    @Index(name = "IX_AuditLog_UserId", columnList = "userId"),
    @Index(name = "IX_AuditLog_ActionType", columnList = "actionType"),
    @Index(name = "IX_AuditLog_ResourceType", columnList = "resourceType"),
    @Index(name = "IX_AuditLog_ResourceId", columnList = "resourceId")
})
public class AuditLog {
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;
    
    @Column(nullable = false)
    private LocalDateTime timestamp;
    
    @Column(nullable = false)
    private UUID userId;
    
    @Column(nullable = false, length = 100)
    private String username;
    
    @Column(nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private Action actionType;
    
    @Column(nullable = false, length = 50)
    private String resourceType;
    
    @Column(nullable = false, length = 100)
    private String resourceId;
    
    @Column(columnDefinition = "NVARCHAR(MAX)")
    private String changes; // JSON string of field changes
    
    @Column(columnDefinition = "NVARCHAR(MAX)")
    private String metadata; // JSON string of additional metadata
    
    @Column(length = 45)
    private String ipAddress;
    
    // Constructors
    public AuditLog() {
        this.timestamp = LocalDateTime.now();
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
    
    public String getChanges() {
        return changes;
    }
    
    public void setChanges(String changes) {
        this.changes = changes;
    }
    
    public String getMetadata() {
        return metadata;
    }
    
    public void setMetadata(String metadata) {
        this.metadata = metadata;
    }
    
    public String getIpAddress() {
        return ipAddress;
    }
    
    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }
}
