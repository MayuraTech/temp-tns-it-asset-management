# V3 Migration Test Documentation

## Migration Overview

**Version:** V3  
**Description:** Add Notifications table for ticket status change notifications  
**Requirements:** 1.1, 1.6, 2.1, 6.1, 6.2, 9.1-9.4  
**Dependencies:** V2__initial_schema.sql (requires Users and Tickets tables)

## What This Migration Creates

### Notifications Table

**Purpose:** Stores system-generated notifications for ticket status changes

**Columns:**
- `Id` (UNIQUEIDENTIFIER, PRIMARY KEY) - Unique notification identifier
- `UserId` (UNIQUEIDENTIFIER, NOT NULL, FK to Users) - User receiving the notification
- `TicketId` (UNIQUEIDENTIFIER, NOT NULL, FK to Tickets) - Related ticket
- `NotificationType` (NVARCHAR(50), NOT NULL) - Type of notification
- `Message` (NVARCHAR(MAX), NOT NULL) - Notification message content
- `IsRead` (BIT, NOT NULL, DEFAULT 0) - Read status flag
- `CreatedAt` (DATETIME2, NOT NULL, DEFAULT GETUTCDATE()) - Creation timestamp

**Constraints:**
- `FK_Notifications_UserId` - Foreign key to Users table with CASCADE DELETE
- `FK_Notifications_TicketId` - Foreign key to Tickets table with CASCADE DELETE
- `CHK_Notifications_Type` - Check constraint for valid notification types:
  - TICKET_APPROVED
  - TICKET_REJECTED
  - TICKET_COMPLETED
  - TICKET_CANCELLED
  - TICKET_STATUS_CHANGE

**Indexes:**
- `IX_Notifications_UserId` - Fast lookup by user
- `IX_Notifications_TicketId` - Fast lookup by ticket
- `IX_Notifications_IsRead` - Filter unread notifications
- `IX_Notifications_CreatedAt` - Sort by creation date
- `IX_Notifications_UserId_IsRead` - Composite index for user unread count queries

## Testing Instructions

### Prerequisites

1. SQL Server 2019+ installed and running
2. ITAssetManagement database exists
3. V1 and V2 migrations have been successfully applied
4. Database user has appropriate permissions

### Manual Testing (SQL Server Management Studio)

#### Step 1: Verify Prerequisites

```sql
-- Check database exists
SELECT name FROM sys.databases WHERE name = 'ITAssetManagement';

-- Check V1 and V2 migrations are applied
USE ITAssetManagement;
SELECT * FROM flyway_schema_history ORDER BY installed_rank;

-- Verify Users and Tickets tables exist
SELECT TABLE_NAME 
FROM INFORMATION_SCHEMA.TABLES 
WHERE TABLE_NAME IN ('Users', 'Tickets')
ORDER BY TABLE_NAME;
```

Expected results:
- Database exists
- V1 and V2 migrations show as successful
- Both Users and Tickets tables exist

#### Step 2: Apply V3 Migration

```sql
-- Execute the migration script
USE ITAssetManagement;
GO

-- Run the V3__add_notifications_table.sql script
-- (Copy and paste the contents or execute via SSMS)
```

#### Step 3: Verify Table Creation

```sql
-- Check Notifications table exists
SELECT TABLE_NAME 
FROM INFORMATION_SCHEMA.TABLES 
WHERE TABLE_NAME = 'Notifications';

-- Verify table structure
SELECT 
    COLUMN_NAME,
    DATA_TYPE,
    CHARACTER_MAXIMUM_LENGTH,
    IS_NULLABLE,
    COLUMN_DEFAULT
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_NAME = 'Notifications'
ORDER BY ORDINAL_POSITION;
```

Expected columns:
- Id (uniqueidentifier, NOT NULL)
- UserId (uniqueidentifier, NOT NULL)
- TicketId (uniqueidentifier, NOT NULL)
- NotificationType (nvarchar(50), NOT NULL)
- Message (nvarchar(MAX), NOT NULL)
- IsRead (bit, NOT NULL, DEFAULT 0)
- CreatedAt (datetime2, NOT NULL, DEFAULT GETUTCDATE())

#### Step 4: Verify Foreign Key Constraints

```sql
-- Check foreign key constraints
SELECT 
    fk.name AS ForeignKeyName,
    OBJECT_NAME(fk.parent_object_id) AS TableName,
    COL_NAME(fkc.parent_object_id, fkc.parent_column_id) AS ColumnName,
    OBJECT_NAME(fk.referenced_object_id) AS ReferencedTable,
    COL_NAME(fkc.referenced_object_id, fkc.referenced_column_id) AS ReferencedColumn,
    fk.delete_referential_action_desc AS DeleteAction
FROM sys.foreign_keys fk
INNER JOIN sys.foreign_key_columns fkc ON fk.object_id = fkc.constraint_object_id
WHERE OBJECT_NAME(fk.parent_object_id) = 'Notifications'
ORDER BY fk.name;
```

Expected foreign keys:
- FK_Notifications_UserId → Users(Id) with CASCADE DELETE
- FK_Notifications_TicketId → Tickets(Id) with CASCADE DELETE

#### Step 5: Verify Check Constraint

```sql
-- Check constraint on NotificationType
SELECT 
    cc.name AS ConstraintName,
    cc.definition AS ConstraintDefinition
FROM sys.check_constraints cc
WHERE OBJECT_NAME(cc.parent_object_id) = 'Notifications';
```

Expected constraint:
- CHK_Notifications_Type with valid notification types

#### Step 6: Verify Indexes

```sql
-- Check indexes on Notifications table
SELECT 
    i.name AS IndexName,
    i.type_desc AS IndexType,
    COL_NAME(ic.object_id, ic.column_id) AS ColumnName,
    ic.key_ordinal AS KeyOrdinal
FROM sys.indexes i
INNER JOIN sys.index_columns ic ON i.object_id = ic.object_id AND i.index_id = ic.index_id
WHERE OBJECT_NAME(i.object_id) = 'Notifications'
ORDER BY i.name, ic.key_ordinal;
```

Expected indexes:
- PK_Notifications (clustered, on Id)
- IX_Notifications_UserId (nonclustered, on UserId)
- IX_Notifications_TicketId (nonclustered, on TicketId)
- IX_Notifications_IsRead (nonclustered, on IsRead)
- IX_Notifications_CreatedAt (nonclustered, on CreatedAt)
- IX_Notifications_UserId_IsRead (nonclustered, on UserId, IsRead)

#### Step 7: Test Data Insertion

```sql
-- Get a valid UserId and TicketId for testing
DECLARE @UserId UNIQUEIDENTIFIER = (SELECT TOP 1 Id FROM Users);
DECLARE @TicketId UNIQUEIDENTIFIER = (SELECT TOP 1 Id FROM Tickets);

-- Insert test notification
INSERT INTO Notifications (UserId, TicketId, NotificationType, Message, IsRead)
VALUES (
    @UserId,
    @TicketId,
    'TICKET_APPROVED',
    'Your ticket has been approved by the asset manager.',
    0
);

-- Verify insertion
SELECT * FROM Notifications;

-- Test check constraint (should fail)
BEGIN TRY
    INSERT INTO Notifications (UserId, TicketId, NotificationType, Message, IsRead)
    VALUES (
        @UserId,
        @TicketId,
        'INVALID_TYPE',
        'This should fail',
        0
    );
END TRY
BEGIN CATCH
    PRINT 'Check constraint working correctly - invalid notification type rejected';
END CATCH

-- Clean up test data
DELETE FROM Notifications;
```

#### Step 8: Test Foreign Key Cascade Delete

```sql
-- Create test user and ticket
DECLARE @TestUserId UNIQUEIDENTIFIER = NEWID();
DECLARE @TestTicketId UNIQUEIDENTIFIER = NEWID();
DECLARE @AdminUserId UNIQUEIDENTIFIER = (SELECT TOP 1 Id FROM Users WHERE Username = 'admin');
DECLARE @TestAssetId UNIQUEIDENTIFIER = (SELECT TOP 1 Id FROM Assets);

-- Insert test user
INSERT INTO Users (Id, Username, PasswordHash, Email)
VALUES (@TestUserId, 'testuser_notification', 'hash', 'test@example.com');

-- Insert test ticket
INSERT INTO Tickets (
    Id, TicketNumber, Type, Status, Priority, 
    AssetId, AssetName, AssetSerialNumber,
    RequesterId, RequesterName
)
VALUES (
    @TestTicketId, 'TKT-2024-00001', 'allocation', 'pending', 'medium',
    @TestAssetId, 'Test Asset', 'TEST-001',
    @TestUserId, 'testuser_notification'
);

-- Insert notification
INSERT INTO Notifications (UserId, TicketId, NotificationType, Message)
VALUES (@TestUserId, @TestTicketId, 'TICKET_APPROVED', 'Test notification');

-- Verify notification exists
SELECT COUNT(*) AS NotificationCount FROM Notifications WHERE UserId = @TestUserId;

-- Delete ticket (should cascade delete notification)
DELETE FROM Tickets WHERE Id = @TestTicketId;

-- Verify notification was deleted
SELECT COUNT(*) AS NotificationCount FROM Notifications WHERE UserId = @TestUserId;
-- Expected: 0

-- Clean up
DELETE FROM Users WHERE Id = @TestUserId;
```

### Automated Testing (Spring Boot)

#### Step 1: Run Application with Flyway

```bash
# Set environment variables
export DB_USERNAME=ITAssetMgmtUser
export DB_PASSWORD=YourSecurePassword123!

# Run Spring Boot application
cd backend
./mvnw spring-boot:run
```

#### Step 2: Check Application Logs

Look for these log messages:
```
Flyway: Migrating schema to version 3 - add notifications table
Flyway: Successfully applied 1 migration to schema
```

#### Step 3: Verify via JPA Repository

Create a test repository and verify:

```java
@SpringBootTest
class NotificationMigrationTest {
    
    @Autowired
    private EntityManager entityManager;
    
    @Test
    void notificationsTableExists() {
        String sql = "SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLES " +
                    "WHERE TABLE_NAME = 'Notifications'";
        
        BigInteger count = (BigInteger) entityManager
            .createNativeQuery(sql)
            .getSingleResult();
            
        assertEquals(1, count.intValue());
    }
    
    @Test
    void notificationsTableHasCorrectColumns() {
        String sql = "SELECT COLUMN_NAME FROM INFORMATION_SCHEMA.COLUMNS " +
                    "WHERE TABLE_NAME = 'Notifications' " +
                    "ORDER BY ORDINAL_POSITION";
        
        List<String> columns = entityManager
            .createNativeQuery(sql)
            .getResultList();
            
        assertThat(columns).containsExactly(
            "Id", "UserId", "TicketId", "NotificationType", 
            "Message", "IsRead", "CreatedAt"
        );
    }
}
```

## Performance Verification

### Index Effectiveness

```sql
-- Test query performance with indexes
SET STATISTICS IO ON;
SET STATISTICS TIME ON;

-- Query 1: Get unread notifications for a user (uses IX_Notifications_UserId_IsRead)
DECLARE @UserId UNIQUEIDENTIFIER = (SELECT TOP 1 Id FROM Users);
SELECT * FROM Notifications 
WHERE UserId = @UserId AND IsRead = 0
ORDER BY CreatedAt DESC;

-- Query 2: Get all notifications for a ticket (uses IX_Notifications_TicketId)
DECLARE @TicketId UNIQUEIDENTIFIER = (SELECT TOP 1 Id FROM Tickets);
SELECT * FROM Notifications 
WHERE TicketId = @TicketId
ORDER BY CreatedAt DESC;

SET STATISTICS IO OFF;
SET STATISTICS TIME OFF;
```

Expected results:
- Queries should use index seeks, not table scans
- Logical reads should be minimal

## Rollback Instructions

If you need to rollback this migration:

```sql
-- Drop indexes
DROP INDEX IF EXISTS IX_Notifications_UserId_IsRead ON Notifications;
DROP INDEX IF EXISTS IX_Notifications_CreatedAt ON Notifications;
DROP INDEX IF EXISTS IX_Notifications_IsRead ON Notifications;
DROP INDEX IF EXISTS IX_Notifications_TicketId ON Notifications;
DROP INDEX IF EXISTS IX_Notifications_UserId ON Notifications;

-- Drop table (will automatically drop foreign keys and check constraint)
DROP TABLE IF EXISTS Notifications;

-- Remove from Flyway history
DELETE FROM flyway_schema_history WHERE version = '3';
```

## Common Issues and Solutions

### Issue 1: Foreign Key Constraint Violation

**Error:** "The INSERT statement conflicted with the FOREIGN KEY constraint"

**Solution:** Ensure the UserId and TicketId exist in their respective tables before inserting notifications.

### Issue 2: Check Constraint Violation

**Error:** "The INSERT statement conflicted with the CHECK constraint"

**Solution:** Use only valid notification types: TICKET_APPROVED, TICKET_REJECTED, TICKET_COMPLETED, TICKET_CANCELLED, TICKET_STATUS_CHANGE

### Issue 3: Migration Already Applied

**Error:** "Flyway detected that the schema history table has been modified"

**Solution:** Check flyway_schema_history table and verify V3 hasn't already been applied.

## Success Criteria

✅ Notifications table created successfully  
✅ All 7 columns present with correct data types  
✅ Primary key constraint on Id  
✅ Foreign key constraints to Users and Tickets with CASCADE DELETE  
✅ Check constraint on NotificationType  
✅ All 5 indexes created  
✅ Test data can be inserted and queried  
✅ Foreign key cascade delete works correctly  
✅ Check constraint rejects invalid notification types  
✅ Flyway schema history updated with V3 entry  

## Next Steps

After successful migration:

1. Create Notification entity class in Java
2. Create NotificationRepository interface
3. Create NotificationService for business logic
4. Implement notification creation in TicketService
5. Create REST API endpoints for notifications
6. Add notification UI components in frontend
7. Implement real-time notification updates (WebSocket/SSE)

## References

- Requirements: `.kiro/specs/module-4-ticket-management/requirements.md`
- Task: Task 1 - Database schema and migration
- Related Tables: Users, Tickets
- Flyway Documentation: https://flywaydb.org/documentation/
