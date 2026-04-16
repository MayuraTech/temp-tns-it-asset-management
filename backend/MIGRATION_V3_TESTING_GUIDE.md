# Migration V3 Testing Guide: AssignmentHistory Table

This guide provides comprehensive instructions for testing the Flyway migration V3 that creates the AssignmentHistory table for the Allocation Management module.

## Overview

**Migration File**: `V3__create_assignment_history_table.sql`

**Purpose**: Creates the AssignmentHistory table to track asset assignments to users and locations, including:
- Table creation with all required columns
- Foreign key constraints to Assets and Users tables
- Check constraint for AssignmentType enum validation
- Performance indexes for common queries

## Prerequisites

Before testing the migration, ensure:

1. ✅ SQL Server 2019+ is installed and running
2. ✅ Database `IT_Asset` (or `IT_Asset`) exists
3. ✅ Migrations V1 and V2 have been successfully applied
4. ✅ Tables `Assets` and `Users` exist (created by V2)
5. ✅ Application user has appropriate permissions

## Testing Approach

We'll test the migration in two ways:
1. **Manual SQL Execution**: Run the migration script directly in SSMS to verify SQL correctness
2. **Flyway Execution**: Run the Spring Boot application to test Flyway integration

---

## Method 1: Manual SQL Testing (Recommended First)

### Step 1: Verify Current Database State

Open SQL Server Management Studio (SSMS) and connect to your SQL Server instance.

```sql
-- Check current database
USE IT_Asset;  -- Or IT_Asset for dev environment
GO

-- Verify V1 and V2 migrations have run
SELECT * FROM sys.tables 
WHERE name IN ('Users', 'Assets', 'AssignmentHistory')
ORDER BY name;

-- Expected: Users and Assets exist, AssignmentHistory does NOT exist yet
```

**Expected Output**:
- `Assets` table exists
- `Users` table exists
- `AssignmentHistory` table does NOT exist

### Step 2: Execute Migration Script

```sql
-- Execute the V3 migration script
-- Option A: Open the file in SSMS and execute (F5)
-- File location: backend/src/main/resources/db/migration/V3__create_assignment_history_table.sql

-- Option B: Use sqlcmd
-- sqlcmd -S localhost\SQLEXPRESS -d ITAssetManagement -i "backend/src/main/resources/db/migration/V3__create_assignment_history_table.sql"
```

**Expected Output**:
```
AssignmentHistory table created successfully.
Index IX_AssignmentHistory_AssetId created successfully.
Index IX_AssignmentHistory_AssignedTo created successfully.
Index IX_AssignmentHistory_AssignedAt created successfully.
All indexes verified/created on AssignmentHistory table.

========================================================================
Database Schema Migration V3 Completed Successfully
========================================================================
...
```

### Step 3: Verify Table Creation

```sql
-- Verify table exists
SELECT * FROM sys.tables WHERE name = 'AssignmentHistory';

-- Check table structure
SELECT 
    c.name AS ColumnName,
    t.name AS DataType,
    c.max_length AS MaxLength,
    c.is_nullable AS IsNullable,
    c.is_identity AS IsIdentity
FROM sys.columns c
INNER JOIN sys.types t ON c.user_type_id = t.user_type_id
WHERE c.object_id = OBJECT_ID('AssignmentHistory')
ORDER BY c.column_id;
```

**Expected Columns**:
| ColumnName | DataType | MaxLength | IsNullable | IsIdentity |
|------------|----------|-----------|------------|------------|
| Id | uniqueidentifier | 16 | 0 | 0 |
| AssetId | uniqueidentifier | 16 | 0 | 0 |
| AssignmentType | nvarchar | 40 | 0 | 0 |
| AssignedTo | nvarchar | 510 | 0 | 0 |
| AssignedBy | uniqueidentifier | 16 | 0 | 0 |
| AssignedAt | datetime2 | 8 | 0 | 0 |
| UnassignedAt | datetime2 | 8 | 1 | 0 |

### Step 4: Verify Foreign Key Constraints

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
WHERE fk.parent_object_id = OBJECT_ID('AssignmentHistory')
ORDER BY fk.name;
```

**Expected Foreign Keys**:
1. **FK_AssignmentHistory_AssetId**
   - Column: `AssetId`
   - References: `Assets(Id)`
   - Delete Action: `CASCADE`

2. **FK_AssignmentHistory_AssignedBy**
   - Column: `AssignedBy`
   - References: `Users(Id)`
   - Delete Action: `NO_ACTION`

### Step 5: Verify Check Constraint

```sql
-- Check constraint for AssignmentType
SELECT 
    cc.name AS ConstraintName,
    cc.definition AS ConstraintDefinition
FROM sys.check_constraints cc
WHERE cc.parent_object_id = OBJECT_ID('AssignmentHistory');
```

**Expected Constraint**:
- **Name**: `CHK_AssignmentHistory_Type`
- **Definition**: `([AssignmentType]='LOCATION' OR [AssignmentType]='USER')`

### Step 6: Verify Indexes

```sql
-- Check indexes
SELECT 
    i.name AS IndexName,
    i.type_desc AS IndexType,
    COL_NAME(ic.object_id, ic.column_id) AS ColumnName,
    i.is_unique AS IsUnique
FROM sys.indexes i
INNER JOIN sys.index_columns ic ON i.object_id = ic.object_id AND i.index_id = ic.index_id
WHERE i.object_id = OBJECT_ID('AssignmentHistory')
ORDER BY i.name, ic.key_ordinal;
```

**Expected Indexes**:
1. **PK_AssignmentHistory** (Clustered, Unique) - on `Id`
2. **IX_AssignmentHistory_AssetId** (Nonclustered) - on `AssetId`
3. **IX_AssignmentHistory_AssignedTo** (Nonclustered) - on `AssignedTo`
4. **IX_AssignmentHistory_AssignedAt** (Nonclustered) - on `AssignedAt`

### Step 7: Test Data Insertion

```sql
-- Get a test user ID (admin user from V2 migration)
DECLARE @TestUserId UNIQUEIDENTIFIER = (SELECT TOP 1 Id FROM Users WHERE Username = 'admin');

-- Get or create a test asset
DECLARE @TestAssetId UNIQUEIDENTIFIER;

-- Check if test asset exists
IF EXISTS (SELECT 1 FROM Assets WHERE SerialNumber = 'TEST-MIGRATION-001')
BEGIN
    SET @TestAssetId = (SELECT Id FROM Assets WHERE SerialNumber = 'TEST-MIGRATION-001');
END
ELSE
BEGIN
    -- Create test asset
    SET @TestAssetId = NEWID();
    INSERT INTO Assets (Id, AssetType, Name, SerialNumber, AcquisitionDate, Status, CreatedBy, UpdatedBy)
    VALUES (
        @TestAssetId,
        'laptop',
        'Test Laptop for Migration',
        'TEST-MIGRATION-001',
        CAST(GETUTCDATE() AS DATE),
        'in_use',
        @TestUserId,
        @TestUserId
    );
END

-- Test 1: Insert USER assignment
INSERT INTO AssignmentHistory (Id, AssetId, AssignmentType, AssignedTo, AssignedBy, AssignedAt)
VALUES (
    NEWID(),
    @TestAssetId,
    'USER',
    'John Doe',
    @TestUserId,
    GETUTCDATE()
);

PRINT '✓ Test 1 PASSED: USER assignment inserted successfully';

-- Test 2: Insert LOCATION assignment
INSERT INTO AssignmentHistory (Id, AssetId, AssignmentType, AssignedTo, AssignedBy, AssignedAt)
VALUES (
    NEWID(),
    @TestAssetId,
    'LOCATION',
    'Building A - Floor 3',
    @TestUserId,
    GETUTCDATE()
);

PRINT '✓ Test 2 PASSED: LOCATION assignment inserted successfully';

-- Test 3: Verify data was inserted
SELECT 
    Id,
    AssetId,
    AssignmentType,
    AssignedTo,
    AssignedBy,
    AssignedAt,
    UnassignedAt
FROM AssignmentHistory
WHERE AssetId = @TestAssetId;

PRINT '✓ Test 3 PASSED: Data retrieved successfully';
```

**Expected Output**: 2 rows showing the USER and LOCATION assignments

### Step 8: Test Check Constraint

```sql
-- Test 4: Attempt to insert invalid AssignmentType (should FAIL)
DECLARE @TestUserId UNIQUEIDENTIFIER = (SELECT TOP 1 Id FROM Users WHERE Username = 'admin');
DECLARE @TestAssetId UNIQUEIDENTIFIER = (SELECT TOP 1 Id FROM Assets WHERE SerialNumber = 'TEST-MIGRATION-001');

BEGIN TRY
    INSERT INTO AssignmentHistory (Id, AssetId, AssignmentType, AssignedTo, AssignedBy, AssignedAt)
    VALUES (
        NEWID(),
        @TestAssetId,
        'INVALID_TYPE',  -- This should fail
        'Test User',
        @TestUserId,
        GETUTCDATE()
    );
    
    PRINT '✗ Test 4 FAILED: Invalid AssignmentType was accepted (should have been rejected)';
END TRY
BEGIN CATCH
    IF ERROR_NUMBER() = 547  -- Check constraint violation
        PRINT '✓ Test 4 PASSED: Invalid AssignmentType correctly rejected';
    ELSE
        PRINT '✗ Test 4 FAILED: Unexpected error: ' + ERROR_MESSAGE();
END CATCH
```

**Expected Output**: `✓ Test 4 PASSED: Invalid AssignmentType correctly rejected`

### Step 9: Test Foreign Key Constraints

```sql
-- Test 5: Attempt to insert with non-existent AssetId (should FAIL)
DECLARE @TestUserId UNIQUEIDENTIFIER = (SELECT TOP 1 Id FROM Users WHERE Username = 'admin');
DECLARE @NonExistentAssetId UNIQUEIDENTIFIER = NEWID();

BEGIN TRY
    INSERT INTO AssignmentHistory (Id, AssetId, AssignmentType, AssignedTo, AssignedBy, AssignedAt)
    VALUES (
        NEWID(),
        @NonExistentAssetId,  -- Non-existent asset
        'USER',
        'Test User',
        @TestUserId,
        GETUTCDATE()
    );
    
    PRINT '✗ Test 5 FAILED: Non-existent AssetId was accepted (should have been rejected)';
END TRY
BEGIN CATCH
    IF ERROR_NUMBER() = 547  -- Foreign key violation
        PRINT '✓ Test 5 PASSED: Non-existent AssetId correctly rejected';
    ELSE
        PRINT '✗ Test 5 FAILED: Unexpected error: ' + ERROR_MESSAGE();
END CATCH

-- Test 6: Verify CASCADE DELETE on Assets
DECLARE @TestAssetId UNIQUEIDENTIFIER = (SELECT Id FROM Assets WHERE SerialNumber = 'TEST-MIGRATION-001');

-- Count assignments before delete
DECLARE @AssignmentCountBefore INT = (SELECT COUNT(*) FROM AssignmentHistory WHERE AssetId = @TestAssetId);

-- Delete the test asset
DELETE FROM Assets WHERE Id = @TestAssetId;

-- Count assignments after delete (should be 0 due to CASCADE)
DECLARE @AssignmentCountAfter INT = (SELECT COUNT(*) FROM AssignmentHistory WHERE AssetId = @TestAssetId);

IF @AssignmentCountBefore > 0 AND @AssignmentCountAfter = 0
    PRINT '✓ Test 6 PASSED: CASCADE DELETE working correctly';
ELSE
    PRINT '✗ Test 6 FAILED: CASCADE DELETE not working as expected';
```

**Expected Output**: 
- `✓ Test 5 PASSED: Non-existent AssetId correctly rejected`
- `✓ Test 6 PASSED: CASCADE DELETE working correctly`

### Step 10: Clean Up Test Data

```sql
-- Clean up test data
DELETE FROM Assets WHERE SerialNumber = 'TEST-MIGRATION-001';
-- AssignmentHistory records will be automatically deleted due to CASCADE

PRINT 'Test data cleaned up successfully';
```

---

## Method 2: Flyway Integration Testing

### Step 1: Configure Application Properties

Verify your `application-dev.properties` has correct database connection:

```properties
# backend/src/main/resources/application-dev.properties
spring.datasource.url=jdbc:sqlserver://TNS-IT-DESKTOP\\SQLEXPRESS:1433;databaseName=IT_Asset;encrypt=true;trustServerCertificate=true
spring.datasource.username=${DB_USERNAME:itassetuser}
spring.datasource.password=${DB_PASSWORD:its@1234}

# Flyway Configuration
spring.flyway.enabled=true
spring.flyway.baseline-on-migrate=true
```

### Step 2: Check Flyway Migration Status

Before running the application, check which migrations have been applied:

```sql
-- Check Flyway schema history
SELECT * FROM flyway_schema_history
ORDER BY installed_rank;
```

**Expected**: V1 and V2 should be present, V3 should NOT be present yet.

### Step 3: Run Spring Boot Application

```bash
cd backend

# Run with dev profile
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev

# Or on Windows
mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=dev
```

### Step 4: Monitor Application Logs

Watch for Flyway migration logs:

```
INFO  Flyway Community Edition ... by Redgate
INFO  Database: jdbc:sqlserver://TNS-IT-DESKTOP\SQLEXPRESS:1433;databaseName=IT_Asset (Microsoft SQL Server 15.0)
INFO  Successfully validated 3 migrations (execution time 00:00.015s)
INFO  Current version of schema [dbo]: 2
INFO  Migrating schema [dbo] to version "3 - create assignment history table"
INFO  Successfully applied 1 migration to schema [dbo], now at version v3 (execution time 00:00.123s)
```

**Success Indicators**:
- ✅ "Successfully validated 3 migrations"
- ✅ "Migrating schema [dbo] to version 3"
- ✅ "Successfully applied 1 migration"
- ✅ No error messages

**Failure Indicators**:
- ❌ "Migration checksum mismatch"
- ❌ "Syntax error"
- ❌ "Foreign key constraint failed"

### Step 5: Verify Flyway Schema History

```sql
-- Check Flyway migration history
SELECT 
    installed_rank,
    version,
    description,
    type,
    script,
    checksum,
    installed_on,
    execution_time,
    success
FROM flyway_schema_history
ORDER BY installed_rank;
```

**Expected Output**:
| installed_rank | version | description | success |
|----------------|---------|-------------|---------|
| 1 | 1 | initial database setup | 1 |
| 2 | 2 | initial schema | 1 |
| 3 | 3 | create assignment history table | 1 |

### Step 6: Verify Table via Application

```sql
-- Verify table exists and has correct structure
SELECT 
    TABLE_NAME,
    TABLE_TYPE
FROM INFORMATION_SCHEMA.TABLES
WHERE TABLE_NAME = 'AssignmentHistory';

-- Verify all constraints and indexes
EXEC sp_help 'AssignmentHistory';
```

---

## Verification Checklist

Use this checklist to ensure the migration was successful:

### Table Structure
- [ ] Table `AssignmentHistory` exists
- [ ] Column `Id` (UNIQUEIDENTIFIER, PRIMARY KEY)
- [ ] Column `AssetId` (UNIQUEIDENTIFIER, NOT NULL)
- [ ] Column `AssignmentType` (NVARCHAR(20), NOT NULL)
- [ ] Column `AssignedTo` (NVARCHAR(255), NOT NULL)
- [ ] Column `AssignedBy` (UNIQUEIDENTIFIER, NOT NULL)
- [ ] Column `AssignedAt` (DATETIME2, NOT NULL, DEFAULT GETUTCDATE())
- [ ] Column `UnassignedAt` (DATETIME2, NULL)

### Constraints
- [ ] Primary Key on `Id`
- [ ] Foreign Key `FK_AssignmentHistory_AssetId` → `Assets(Id)` with CASCADE DELETE
- [ ] Foreign Key `FK_AssignmentHistory_AssignedBy` → `Users(Id)`
- [ ] Check Constraint `CHK_AssignmentHistory_Type` (USER or LOCATION)

### Indexes
- [ ] Index `IX_AssignmentHistory_AssetId` on `AssetId`
- [ ] Index `IX_AssignmentHistory_AssignedTo` on `AssignedTo`
- [ ] Index `IX_AssignmentHistory_AssignedAt` on `AssignedAt`

### Functionality
- [ ] Can insert USER assignment
- [ ] Can insert LOCATION assignment
- [ ] Cannot insert invalid AssignmentType
- [ ] Cannot insert with non-existent AssetId
- [ ] Cannot insert with non-existent AssignedBy UserId
- [ ] CASCADE DELETE works when Asset is deleted
- [ ] Can query assignment history by AssetId
- [ ] Can query assignment history by AssignedTo
- [ ] Can query assignment history by date range

### Flyway Integration
- [ ] Migration appears in `flyway_schema_history` table
- [ ] Migration marked as successful
- [ ] Application starts without errors
- [ ] No checksum mismatch errors

---

## Troubleshooting

### Issue: "Table already exists"

**Cause**: Migration was run manually before Flyway execution

**Solution**:
```sql
-- Option 1: Drop the table and let Flyway recreate it
DROP TABLE AssignmentHistory;

-- Option 2: Mark migration as completed in Flyway
INSERT INTO flyway_schema_history (installed_rank, version, description, type, script, checksum, installed_by, installed_on, execution_time, success)
VALUES (
    3,
    '3',
    'create assignment history table',
    'SQL',
    'V3__create_assignment_history_table.sql',
    -1234567890,  -- Use actual checksum from Flyway logs
    'ITAssetMgmtUser',
    GETUTCDATE(),
    100,
    1
);
```

### Issue: "Foreign key constraint failed"

**Cause**: Referenced tables (Assets or Users) don't exist

**Solution**:
```sql
-- Verify prerequisite tables exist
SELECT name FROM sys.tables WHERE name IN ('Assets', 'Users');

-- If missing, run V2 migration first
```

### Issue: "Checksum mismatch"

**Cause**: Migration file was modified after being applied

**Solution**:
```sql
-- Option 1: Repair Flyway schema history
-- Run: ./mvnw flyway:repair

-- Option 2: Update checksum in database
UPDATE flyway_schema_history
SET checksum = <new_checksum>
WHERE version = '3';
```

### Issue: "Permission denied"

**Cause**: Database user lacks CREATE TABLE permission

**Solution**:
```sql
-- Grant necessary permissions
USE IT_Asset;
GRANT CREATE TABLE TO ITAssetUser;
GRANT ALTER ON SCHEMA::dbo TO ITAssetUser;
```

---

## Performance Validation

After successful migration, validate index performance:

```sql
-- Check index usage statistics
SELECT 
    OBJECT_NAME(s.object_id) AS TableName,
    i.name AS IndexName,
    s.user_seeks,
    s.user_scans,
    s.user_lookups,
    s.user_updates
FROM sys.dm_db_index_usage_stats s
INNER JOIN sys.indexes i ON s.object_id = i.object_id AND s.index_id = i.index_id
WHERE OBJECT_NAME(s.object_id) = 'AssignmentHistory'
ORDER BY s.user_seeks + s.user_scans + s.user_lookups DESC;
```

---

## Next Steps

After successful migration testing:

1. ✅ Migration V3 is complete and verified
2. ⏭️ Implement `AssignmentHistory` JPA entity
3. ⏭️ Create `AssignmentHistoryRepository` interface
4. ⏭️ Implement `AllocationService` business logic
5. ⏭️ Create `AllocationController` REST endpoints
6. ⏭️ Write unit and integration tests

---

## Summary

This migration creates the foundation for the Allocation Management module by establishing the AssignmentHistory table with:

- **Robust data integrity**: Foreign keys ensure referential integrity
- **Data validation**: Check constraints enforce valid AssignmentType values
- **Performance optimization**: Indexes on commonly queried columns
- **Audit trail**: Complete history of all asset assignments
- **Cascade delete**: Automatic cleanup when assets are removed

The migration is idempotent and can be safely re-run if needed.
