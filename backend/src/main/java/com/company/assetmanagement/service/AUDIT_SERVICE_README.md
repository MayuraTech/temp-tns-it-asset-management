# Audit Logging Service

## Overview

The Audit Logging Service provides comprehensive audit trail functionality for the IT Infrastructure Asset Management application. It records all system operations for compliance, security monitoring, and investigation purposes.

## Features

- **Immutable Audit Logs**: All audit log entries are append-only and cannot be modified or deleted
- **Comprehensive Tracking**: Records all CRUD operations with before/after values
- **Flexible Search**: Search and filter by date, user, action type, resource type, and resource ID
- **7-Year Retention**: Audit logs are retained for minimum 7 years for compliance
- **Performance Optimized**: Indexed for fast querying across large datasets

## Components

### 1. AuditLog Entity

JPA entity representing an immutable audit log entry.

**Fields:**
- `id`: Unique identifier (UUID)
- `timestamp`: When the event occurred
- `userId`: User who performed the action
- `username`: Username for display
- `actionType`: Type of action (CREATE, UPDATE, DELETE, etc.)
- `resourceType`: Type of resource affected (ASSET, USER, TICKET, etc.)
- `resourceId`: Identifier of the affected resource
- `changes`: JSON string of field changes (before/after values)
- `metadata`: JSON string of additional context
- `ipAddress`: IP address of the user

### 2. AuditLogRepository

Spring Data JPA repository for querying audit logs.

**Key Methods:**
- `findByResourceIdOrderByTimestampDesc()`: Get audit trail for a resource
- `findByUserId()`: Get all actions by a user
- `findByActionType()`: Get all actions of a specific type
- `searchAuditLog()`: Advanced search with multiple filters

### 3. AuditService Interface

Service interface defining audit logging operations.

**Methods:**
- `logEvent(AuditEventDTO)`: Log an audit event
- `searchAuditLog(...)`: Search with filters
- `getResourceAuditTrail(String)`: Get complete audit trail for a resource
- `getAuditLogById(UUID)`: Get specific audit log entry

### 4. AuditServiceImpl

Implementation of the audit service.

**Features:**
- Automatic JSON serialization of changes and metadata
- Error handling that doesn't break business operations
- Efficient querying with pagination support

### 5. DTOs

**AuditEventDTO**: Used to capture audit information before persisting
- Builder pattern for easy construction
- Supports field changes and metadata

**AuditLogDTO**: Used to return audit log information to clients
- Includes deserialized changes and metadata

**FieldChangeDTO**: Represents a single field change
- `field`: Field name
- `oldValue`: Previous value
- `newValue`: New value

### 6. AuditLogController

REST controller providing audit log endpoints.

**Endpoints:**
- `GET /api/v1/audit-logs`: Search audit logs with filters
- `GET /api/v1/audit-logs/{id}`: Get specific audit log entry
- `GET /api/v1/audit-logs/resource/{resourceId}`: Get resource audit trail

## Usage Examples

### Logging an Audit Event

```java
@Service
public class AssetService {
    
    private final AuditService auditService;
    
    public AssetDTO createAsset(String userId, AssetRequest request) {
        // ... create asset logic ...
        
        // Log the creation
        AuditEventDTO event = AuditEventDTO.builder()
            .userId(UUID.fromString(userId))
            .username(getCurrentUsername())
            .actionType(Action.CREATE)
            .resourceType("ASSET")
            .resourceId(asset.getId().toString())
            .ipAddress(getClientIpAddress())
            .build();
        
        auditService.logEvent(event);
        
        return assetDTO;
    }
}
```

### Logging an Update with Changes

```java
public AssetDTO updateAsset(String userId, String assetId, AssetUpdateRequest request) {
    Asset existingAsset = findAssetById(assetId);
    
    // Track changes
    Map<String, FieldChangeDTO> changes = new HashMap<>();
    
    if (!existingAsset.getStatus().equals(request.getStatus())) {
        changes.put("status", new FieldChangeDTO(
            "status",
            existingAsset.getStatus(),
            request.getStatus()
        ));
    }
    
    if (!Objects.equals(existingAsset.getLocation(), request.getLocation())) {
        changes.put("location", new FieldChangeDTO(
            "location",
            existingAsset.getLocation(),
            request.getLocation()
        ));
    }
    
    // ... update asset logic ...
    
    // Log the update with changes
    AuditEventDTO event = AuditEventDTO.builder()
        .userId(UUID.fromString(userId))
        .username(getCurrentUsername())
        .actionType(Action.UPDATE)
        .resourceType("ASSET")
        .resourceId(assetId)
        .changes(changes)
        .ipAddress(getClientIpAddress())
        .build();
    
    auditService.logEvent(event);
    
    return assetDTO;
}
```

### Logging with Metadata

```java
public void approveTicket(String approverId, String ticketId, String comments) {
    // ... approval logic ...
    
    Map<String, Object> metadata = new HashMap<>();
    metadata.put("comments", comments);
    metadata.put("approvalTime", LocalDateTime.now());
    
    AuditEventDTO event = AuditEventDTO.builder()
        .userId(UUID.fromString(approverId))
        .username(getCurrentUsername())
        .actionType(Action.TICKET_APPROVE)
        .resourceType("TICKET")
        .resourceId(ticketId)
        .metadata(metadata)
        .ipAddress(getClientIpAddress())
        .build();
    
    auditService.logEvent(event);
}
```

### Searching Audit Logs

```java
// Search by user
Page<AuditLogDTO> userActions = auditService.searchAuditLog(
    userId, null, null, null, null, null, pageable
);

// Search by action type
Page<AuditLogDTO> deletions = auditService.searchAuditLog(
    null, Action.DELETE, null, null, null, null, pageable
);

// Search by date range
Page<AuditLogDTO> recentLogs = auditService.searchAuditLog(
    null, null, null, null, 
    LocalDateTime.now().minusDays(7), 
    LocalDateTime.now(), 
    pageable
);

// Complex search
Page<AuditLogDTO> specificLogs = auditService.searchAuditLog(
    userId, 
    Action.UPDATE, 
    "ASSET", 
    assetId,
    startDate, 
    endDate, 
    pageable
);
```

### Getting Resource Audit Trail

```java
// Get complete history for an asset
List<AuditLogDTO> assetHistory = auditService.getResourceAuditTrail(assetId);

// Display to user
assetHistory.forEach(log -> {
    System.out.println(log.getTimestamp() + " - " + 
                      log.getUsername() + " - " + 
                      log.getActionType());
    
    if (log.getChanges() != null) {
        log.getChanges().forEach((field, change) -> {
            System.out.println("  " + field + ": " + 
                             change.getOldValue() + " -> " + 
                             change.getNewValue());
        });
    }
});
```

## API Examples

### Search Audit Logs

```bash
# Get all audit logs (paginated)
GET /api/v1/audit-logs?page=0&size=20

# Filter by user
GET /api/v1/audit-logs?userId=550e8400-e29b-41d4-a716-446655440000

# Filter by action type
GET /api/v1/audit-logs?actionType=UPDATE

# Filter by resource type
GET /api/v1/audit-logs?resourceType=ASSET

# Filter by date range
GET /api/v1/audit-logs?startDate=2024-01-01T00:00:00&endDate=2024-12-31T23:59:59

# Complex filter
GET /api/v1/audit-logs?userId=550e8400-e29b-41d4-a716-446655440000&actionType=UPDATE&resourceType=ASSET&startDate=2024-01-01T00:00:00
```

### Get Specific Audit Log

```bash
GET /api/v1/audit-logs/550e8400-e29b-41d4-a716-446655440000
```

### Get Resource Audit Trail

```bash
GET /api/v1/audit-logs/resource/550e8400-e29b-41d4-a716-446655440000
```

## Database Schema

```sql
CREATE TABLE AuditLog (
    Id UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
    Timestamp DATETIME2 NOT NULL DEFAULT GETUTCDATE(),
    UserId UNIQUEIDENTIFIER NOT NULL,
    Username NVARCHAR(100) NOT NULL,
    ActionType NVARCHAR(50) NOT NULL,
    ResourceType NVARCHAR(50) NOT NULL,
    ResourceId NVARCHAR(100) NOT NULL,
    Changes NVARCHAR(MAX) NULL,
    Metadata NVARCHAR(MAX) NULL,
    IpAddress NVARCHAR(45) NULL,
    
    CONSTRAINT FK_AuditLog_UserId FOREIGN KEY (UserId) REFERENCES Users(Id)
);

-- Indexes for performance
CREATE INDEX IX_AuditLog_Timestamp ON AuditLog(Timestamp);
CREATE INDEX IX_AuditLog_UserId ON AuditLog(UserId);
CREATE INDEX IX_AuditLog_ActionType ON AuditLog(ActionType);
CREATE INDEX IX_AuditLog_ResourceType ON AuditLog(ResourceType);
CREATE INDEX IX_AuditLog_ResourceId ON AuditLog(ResourceId);
```

## Action Types

The following action types are supported:

- `CREATE`: Resource creation
- `UPDATE`: Resource update
- `DELETE`: Resource deletion
- `STATUS_CHANGE`: Status transition
- `LOGIN`: User login
- `LOGOUT`: User logout
- `FAILED_LOGIN`: Failed login attempt
- `TICKET_CREATE`: Ticket creation
- `TICKET_UPDATE`: Ticket update
- `TICKET_APPROVE`: Ticket approval
- `TICKET_REJECT`: Ticket rejection
- `TICKET_COMPLETE`: Ticket completion

## Best Practices

1. **Always Log State Changes**: Log all create, update, delete, and status change operations
2. **Include Context**: Use metadata to provide additional context about why an action was taken
3. **Track Changes**: For updates, always include before/after values
4. **Don't Break Operations**: Audit logging failures should not prevent business operations
5. **Use Appropriate Action Types**: Choose the most specific action type available
6. **Include IP Address**: Always capture the client IP address for security tracking
7. **Search Efficiently**: Use indexes and pagination for large result sets

## Security Considerations

1. **Access Control**: Only Administrators can search all audit logs
2. **Immutability**: Audit logs cannot be modified or deleted through the application
3. **Sensitive Data**: Never log passwords, tokens, or other sensitive credentials
4. **Retention**: Audit logs are retained for 7 years minimum for compliance
5. **Performance**: Indexes ensure fast querying even with millions of records

## Testing

Comprehensive tests are provided:

- **Unit Tests**: `AuditServiceImplTest` - Tests service logic with mocks
- **Integration Tests**: `AuditServiceIntegrationTest` - Tests complete workflow with database

Run tests:
```bash
mvn test -Dtest=AuditServiceImplTest
mvn test -Dtest=AuditServiceIntegrationTest
```

## Compliance

The audit logging service meets the following compliance requirements:

- **Requirement 9.1**: Records all CRUD operations
- **Requirement 9.2**: Includes timestamp, user, action type, resource ID, and field changes
- **Requirement 9.3**: Retains entries for minimum 7 years
- **Requirement 9.4**: Supports search and filtering by date, user, action, and resource
- **Requirement 9.5**: Prevents modification or deletion of audit entries
