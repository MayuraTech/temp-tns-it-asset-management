# Module 2 Database Migration Testing Guide

## Overview

This guide provides step-by-step instructions for testing the Module 2 Assets table database migration.

## Prerequisites

Before testing, ensure you have:

1. ✅ Microsoft SQL Server 2019+ installed and running
2. ✅ SQL Server Management Studio (SSMS) or sqlcmd
3. ✅ Java 17+ installed
4. ✅ Maven 3.8+ installed (or use Maven wrapper)
5. ✅ Database connection credentials

## Test Environment Setup

### Option 1: Using Existing Database

If you already have the ITAssetManagement database from V1 and V2 migrations:

```bash
# Set environment variables
export DB_USERNAME=ITAssetMgmtUser
export DB_PASSWORD=YourSecurePassword123!
export JWT_SECRET=YourVeryLongAndSecureSecretKeyHere

# Navigate to backend directory
cd backend

# Run Flyway migration
mvn flyway:migrate

# Or start the application (Flyway runs automatically)
mvn spring-boot:run
```

### Option 2: Fresh Database Setup

If starting from scratch:

#### Step 1: Create Database

```sql
-- Connect to SQL Server as administrator
-- Execute V1__initial_database_setup.sql
sqlcmd -S localhost -U sa -P YourSAPassword -i src/main/resources/db/migration/V1__initial_database_setup.sql
```

#### Step 2: Create Initial Schema

```sql
-- Execute V2__initial_schema.sql
sqlcmd -S localhost -U sa -P YourSAPassword -i src/main/resources/db/migration/V2__initial_schema.sql
```

#### Step 3: Run Verification

```sql
-- Execute V3__verify_assets_table_module2.sql
sqlcmd -S localhost -U sa -P YourSAPassword -i src/main/resources/db/migration/V3__verify_assets_table_module2.sql
```

## Manual Testing Steps

### Test 1: Verify Table Structure

```sql
USE ITAssetManagement;
GO

-- Check if Assets table exists
SELECT 
    TABLE_NAME,
    TABLE_TYPE
FROM INFORMATION_SCHEMA.TABLES
WHERE TABLE_NAME = 'Assets';

-- Expected: 1 row with TABLE_NAME = 'Assets', TABLE_TYPE = 'BASE TABLE'
```

**Expected Result**: ✅ Assets table exists

### Test 2: Verify Columns

```sql
-- List all columns with data types
SELECT 
    COLUMN_NAME,
    DATA_TYPE,
    CHARACTER_MAXIMUM_LENGTH,
    IS_NULLABLE,
    COLUMN_DEFAULT
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_NAME = 'Assets'
ORDER BY ORDINAL_POSITION;

-- Expected: 18 columns
```

**Expected Columns**:
1. Id (uniqueidentifier, NOT NULL)
2. AssetType (nvarchar(50), NOT NULL)
3. Name (nvarchar(255), NOT NULL)
4. SerialNumber (nvarchar(100), NOT NULL)
5. AcquisitionDate (date, NOT NULL)
6. Status (nvarchar(50), NOT NULL)
7. Location (nvarchar(255), NULL)
8. AssignedUser (nvarchar(255), NULL)
9. AssignedUserEmail (nvarchar(255), NULL)
10. AssignmentDate (datetime2, NULL)
11. LocationUpdateDate (datetime2, NULL)
12. Notes (nvarchar(MAX), NULL)
13. CustomFields (nvarchar(MAX), NULL)
14. CreatedAt (datetime2, NOT NULL)
15. CreatedBy (uniqueidentifier, NOT NULL)
16. UpdatedAt (datetime2, NOT NULL)
17. UpdatedBy (uniqueidentifier, NOT NULL)
18. ReadOnly (bit, NOT NULL)

### Test 3: Verify Constraints

```sql
-- List all constraints
SELECT 
    CONSTRAINT_NAME,
    CONSTRAINT_TYPE
FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS
WHERE TABLE_NAME = 'Assets'
ORDER BY CONSTRAINT_TYPE, CONSTRAINT_NAME;

-- Expected constraints:
-- PRIMARY KEY: PK__Assets__*
-- UNIQUE: UQ__Assets__SerialNumber__*
-- CHECK: CHK_Assets_AssetType
-- CHECK: CHK_Assets_Status
-- CHECK: CHK_Assets_AcquisitionDate
-- FOREIGN KEY: FK_Assets_CreatedBy
-- FOREIGN KEY: FK_Assets_UpdatedBy
```

**Expected Result**: ✅ 7 constraints (1 PK, 1 UNIQUE, 3 CHECK, 2 FK)

### Test 4: Verify Indexes

```sql
-- List all indexes
SELECT 
    i.name AS IndexName,
    i.type_desc AS IndexType,
    i.is_unique AS IsUnique,
    COL_NAME(ic.object_id, ic.column_id) AS ColumnName
FROM sys.indexes i
INNER JOIN sys.index_columns ic ON i.object_id = ic.object_id AND i.index_id = ic.index_id
WHERE i.object_id = OBJECT_ID('Assets')
AND i.name IS NOT NULL
ORDER BY i.name, ic.key_ordinal;

-- Expected indexes:
-- IX_Assets_AcquisitionDate
-- IX_Assets_AssetType
-- IX_Assets_AssignedUser
-- IX_Assets_CreatedBy
-- IX_Assets_Location
-- IX_Assets_SerialNumber
-- IX_Assets_Status
```

**Expected Result**: ✅ 7 indexes on key columns

### Test 5: Test Data Insertion

```sql
-- Get admin user ID
DECLARE @AdminUserId UNIQUEIDENTIFIER = (SELECT Id FROM Users WHERE Username = 'admin');

-- Insert test asset
INSERT INTO Assets (
    Id,
    AssetType,
    Name,
    SerialNumber,
    AcquisitionDate,
    Status,
    Location,
    Notes,
    CreatedAt,
    CreatedBy,
    UpdatedAt,
    UpdatedBy,
    ReadOnly
)
VALUES (
    NEWID(),
    'server',
    'Test Production Server',
    'TEST-SRV-MODULE2-001',
    '2024-01-15',
    'ordered',
    'Data Center A - Rack 5',
    'Test asset for Module 2 verification',
    GETUTCDATE(),
    @AdminUserId,
    GETUTCDATE(),
    @AdminUserId,
    0
);

-- Verify insertion
SELECT 
    Id,
    AssetType,
    Name,
    SerialNumber,
    Status,
    Location,
    CreatedAt
FROM Assets
WHERE SerialNumber = 'TEST-SRV-MODULE2-001';
```

**Expected Result**: ✅ 1 row inserted successfully

### Test 6: Test UNIQUE Constraint

```sql
-- Attempt to insert duplicate serial number (should fail)
DECLARE @AdminUserId UNIQUEIDENTIFIER = (SELECT Id FROM Users WHERE Username = 'admin');

INSERT INTO Assets (
    Id,
    AssetType,
    Name,
    SerialNumber,
    AcquisitionDate,
    Status,
    CreatedAt,
    CreatedBy,
    UpdatedAt,
    UpdatedBy,
    ReadOnly
)
VALUES (
    NEWID(),
    'workstation',
    'Duplicate Serial Test',
    'TEST-SRV-MODULE2-001', -- Duplicate!
    '2024-01-15',
    'ordered',
    GETUTCDATE(),
    @AdminUserId,
    GETUTCDATE(),
    @AdminUserId,
    0
);

-- Expected error: Violation of UNIQUE KEY constraint
```

**Expected Result**: ✅ Error - Duplicate serial number rejected

### Test 7: Test CHECK Constraint on AssetType

```sql
-- Attempt to insert invalid asset type (should fail)
DECLARE @AdminUserId UNIQUEIDENTIFIER = (SELECT Id FROM Users WHERE Username = 'admin');

INSERT INTO Assets (
    Id,
    AssetType,
    Name,
    SerialNumber,
    AcquisitionDate,
    Status,
    CreatedAt,
    CreatedBy,
    UpdatedAt,
    UpdatedBy,
    ReadOnly
)
VALUES (
    NEWID(),
    'invalid_type', -- Invalid!
    'Invalid Type Test',
    'TEST-INVALID-001',
    '2024-01-15',
    'ordered',
    GETUTCDATE(),
    @AdminUserId,
    GETUTCDATE(),
    @AdminUserId,
    0
);

-- Expected error: CHECK constraint violation
```

**Expected Result**: ✅ Error - Invalid asset type rejected

### Test 8: Test CHECK Constraint on Status

```sql
-- Attempt to insert invalid status (should fail)
DECLARE @AdminUserId UNIQUEIDENTIFIER = (SELECT Id FROM Users WHERE Username = 'admin');

INSERT INTO Assets (
    Id,
    AssetType,
    Name,
    SerialNumber,
    AcquisitionDate,
    Status,
    CreatedAt,
    CreatedBy,
    UpdatedAt,
    UpdatedBy,
    ReadOnly
)
VALUES (
    NEWID(),
    'server',
    'Invalid Status Test',
    'TEST-STATUS-001',
    '2024-01-15',
    'invalid_status', -- Invalid!
    GETUTCDATE(),
    @AdminUserId,
    GETUTCDATE(),
    @AdminUserId,
    0
);

-- Expected error: CHECK constraint violation
```

**Expected Result**: ✅ Error - Invalid status rejected

### Test 9: Test CHECK Constraint on AcquisitionDate

```sql
-- Attempt to insert future date (should fail)
DECLARE @AdminUserId UNIQUEIDENTIFIER = (SELECT Id FROM Users WHERE Username = 'admin');

INSERT INTO Assets (
    Id,
    AssetType,
    Name,
    SerialNumber,
    AcquisitionDate,
    Status,
    CreatedAt,
    CreatedBy,
    UpdatedAt,
    UpdatedBy,
    ReadOnly
)
VALUES (
    NEWID(),
    'server',
    'Future Date Test',
    'TEST-FUTURE-001',
    '2099-12-31', -- Future date!
    'ordered',
    GETUTCDATE(),
    @AdminUserId,
    GETUTCDATE(),
    @AdminUserId,
    0
);

-- Expected error: CHECK constraint violation
```

**Expected Result**: ✅ Error - Future date rejected

### Test 10: Test Foreign Key Constraint

```sql
-- Attempt to insert with non-existent user (should fail)
INSERT INTO Assets (
    Id,
    AssetType,
    Name,
    SerialNumber,
    AcquisitionDate,
    Status,
    CreatedAt,
    CreatedBy,
    UpdatedAt,
    UpdatedBy,
    ReadOnly
)
VALUES (
    NEWID(),
    'server',
    'Invalid User Test',
    'TEST-FK-001',
    '2024-01-15',
    'ordered',
    GETUTCDATE(),
    '00000000-0000-0000-0000-000000000000', -- Non-existent user!
    GETUTCDATE(),
    '00000000-0000-0000-0000-000000000000',
    0
);

-- Expected error: FOREIGN KEY constraint violation
```

**Expected Result**: ✅ Error - Invalid user reference rejected

### Test 11: Test All 15 Asset Types

```sql
-- Insert one asset of each type
DECLARE @AdminUserId UNIQUEIDENTIFIER = (SELECT Id FROM Users WHERE Username = 'admin');
DECLARE @AssetTypes TABLE (AssetType NVARCHAR(50));

INSERT INTO @AssetTypes VALUES
    ('server'), ('workstation'), ('network_device'), ('storage_device'),
    ('software_license'), ('peripheral'), ('keyboard'), ('mouse'),
    ('laptop'), ('monitor'), ('headset'), ('laptop_charger'),
    ('hdmi_cable'), ('network_cable'), ('access_card');

DECLARE @Type NVARCHAR(50);
DECLARE @Counter INT = 1;

DECLARE type_cursor CURSOR FOR SELECT AssetType FROM @AssetTypes;
OPEN type_cursor;
FETCH NEXT FROM type_cursor INTO @Type;

WHILE @@FETCH_STATUS = 0
BEGIN
    INSERT INTO Assets (
        Id, AssetType, Name, SerialNumber, AcquisitionDate, Status,
        CreatedAt, CreatedBy, UpdatedAt, UpdatedBy, ReadOnly
    )
    VALUES (
        NEWID(),
        @Type,
        'Test ' + @Type,
        'TEST-TYPE-' + RIGHT('000' + CAST(@Counter AS VARCHAR), 3),
        '2024-01-15',
        'ordered',
        GETUTCDATE(),
        @AdminUserId,
        GETUTCDATE(),
        @AdminUserId,
        0
    );
    
    SET @Counter = @Counter + 1;
    FETCH NEXT FROM type_cursor INTO @Type;
END

CLOSE type_cursor;
DEALLOCATE type_cursor;

-- Verify all types inserted
SELECT AssetType, COUNT(*) AS Count
FROM Assets
WHERE SerialNumber LIKE 'TEST-TYPE-%'
GROUP BY AssetType
ORDER BY AssetType;
```

**Expected Result**: ✅ 15 rows, one for each asset type

### Test 12: Test All 7 Lifecycle Statuses

```sql
-- Update test assets to different statuses
DECLARE @AdminUserId UNIQUEIDENTIFIER = (SELECT Id FROM Users WHERE Username = 'admin');
DECLARE @Statuses TABLE (Status NVARCHAR(50));

INSERT INTO @Statuses VALUES
    ('ordered'), ('received'), ('deployed'), ('in_use'),
    ('maintenance'), ('storage'), ('retired');

DECLARE @Status NVARCHAR(50);
DECLARE @Counter INT = 1;

DECLARE status_cursor CURSOR FOR SELECT Status FROM @Statuses;
OPEN status_cursor;
FETCH NEXT FROM status_cursor INTO @Status;

WHILE @@FETCH_STATUS = 0
BEGIN
    INSERT INTO Assets (
        Id, AssetType, Name, SerialNumber, AcquisitionDate, Status,
        CreatedAt, CreatedBy, UpdatedAt, UpdatedBy, ReadOnly
    )
    VALUES (
        NEWID(),
        'server',
        'Test Status ' + @Status,
        'TEST-STATUS-' + RIGHT('000' + CAST(@Counter AS VARCHAR), 3),
        '2024-01-15',
        @Status,
        GETUTCDATE(),
        @AdminUserId,
        GETUTCDATE(),
        @AdminUserId,
        CASE WHEN @Status = 'retired' THEN 1 ELSE 0 END
    );
    
    SET @Counter = @Counter + 1;
    FETCH NEXT FROM status_cursor INTO @Status;
END

CLOSE status_cursor;
DEALLOCATE status_cursor;

-- Verify all statuses
SELECT Status, COUNT(*) AS Count
FROM Assets
WHERE SerialNumber LIKE 'TEST-STATUS-%'
GROUP BY Status
ORDER BY Status;
```

**Expected Result**: ✅ 7 rows, one for each status

### Test 13: Clean Up Test Data

```sql
-- Remove all test data
DELETE FROM Assets
WHERE SerialNumber LIKE 'TEST-%';

-- Verify cleanup
SELECT COUNT(*) AS RemainingTestAssets
FROM Assets
WHERE SerialNumber LIKE 'TEST-%';

-- Expected: 0 rows
```

**Expected Result**: ✅ All test data removed

## Automated Testing with Spring Boot

### Integration Test Example

Create a test class to verify the database schema:

```java
@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Transactional
class AssetsDatabaseSchemaTest {
    
    @Autowired
    private DataSource dataSource;
    
    @Test
    @DisplayName("Assets table should exist")
    void assetsTableShouldExist() throws SQLException {
        try (Connection conn = dataSource.getConnection()) {
            DatabaseMetaData metaData = conn.getMetaData();
            ResultSet tables = metaData.getTables(null, null, "Assets", null);
            assertTrue(tables.next(), "Assets table should exist");
        }
    }
    
    @Test
    @DisplayName("Assets table should have 18 columns")
    void assetsTableShouldHave18Columns() throws SQLException {
        try (Connection conn = dataSource.getConnection()) {
            DatabaseMetaData metaData = conn.getMetaData();
            ResultSet columns = metaData.getColumns(null, null, "Assets", null);
            
            int columnCount = 0;
            while (columns.next()) {
                columnCount++;
            }
            
            assertEquals(18, columnCount, "Assets table should have 18 columns");
        }
    }
    
    @Test
    @DisplayName("SerialNumber should have unique constraint")
    void serialNumberShouldHaveUniqueConstraint() throws SQLException {
        try (Connection conn = dataSource.getConnection()) {
            DatabaseMetaData metaData = conn.getMetaData();
            ResultSet indexes = metaData.getIndexInfo(null, null, "Assets", true, false);
            
            boolean hasUniqueIndex = false;
            while (indexes.next()) {
                String columnName = indexes.getString("COLUMN_NAME");
                if ("SerialNumber".equalsIgnoreCase(columnName)) {
                    hasUniqueIndex = true;
                    break;
                }
            }
            
            assertTrue(hasUniqueIndex, "SerialNumber should have unique constraint");
        }
    }
}
```

### Run Integration Tests

```bash
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=AssetsDatabaseSchemaTest

# Run with specific profile
mvn test -Dspring.profiles.active=test
```

## Test Results Checklist

Mark each test as complete:

- [ ] Test 1: Table exists ✅
- [ ] Test 2: 18 columns present ✅
- [ ] Test 3: 7 constraints (1 PK, 1 UNIQUE, 3 CHECK, 2 FK) ✅
- [ ] Test 4: 7 indexes on key columns ✅
- [ ] Test 5: Data insertion works ✅
- [ ] Test 6: UNIQUE constraint enforced ✅
- [ ] Test 7: AssetType CHECK constraint enforced ✅
- [ ] Test 8: Status CHECK constraint enforced ✅
- [ ] Test 9: AcquisitionDate CHECK constraint enforced ✅
- [ ] Test 10: Foreign key constraints enforced ✅
- [ ] Test 11: All 15 asset types accepted ✅
- [ ] Test 12: All 7 statuses accepted ✅
- [ ] Test 13: Test data cleanup successful ✅

## Troubleshooting

### Issue: Cannot connect to database

**Solution**: Verify connection string and credentials in application.properties

### Issue: Flyway migration fails

**Solution**: Check Flyway version history:
```sql
SELECT * FROM flyway_schema_history ORDER BY installed_rank;
```

### Issue: Constraint violations

**Solution**: Review constraint definitions and ensure data meets requirements

## Success Criteria

The database migration is successful when:

✅ All 13 manual tests pass  
✅ V3 verification script executes without errors  
✅ Integration tests pass  
✅ No constraint violations on valid data  
✅ All constraints properly reject invalid data  

## Next Steps

After successful testing:

1. Proceed to Task 2: Implement Domain Enums
2. Proceed to Task 3: Implement Asset Entity
3. Proceed to Task 4: Implement Asset Repository

## References

- **Migration V2**: `V2__initial_schema.sql`
- **Verification V3**: `V3__verify_assets_table_module2.sql`
- **Documentation**: `MODULE2_ASSETS_TABLE_README.md`
- **Requirements**: Module 2 Requirements Document
- **Design**: Module 2 Design Document
