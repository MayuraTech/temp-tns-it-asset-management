-- ============================================================================
-- Quick Test Script for Migration V3: AssignmentHistory Table
-- ============================================================================
-- Purpose: Rapid verification of V3 migration success
-- Usage: Execute this script in SSMS after running the V3 migration
-- ============================================================================

USE ITAssetManagement;  -- Or IT_Asset for dev environment
GO

PRINT '========================================================================';
PRINT 'Migration V3 Verification Tests';
PRINT '========================================================================';
PRINT '';

-- ============================================================================
-- TEST 1: Verify Table Exists
-- ============================================================================
PRINT 'TEST 1: Verifying AssignmentHistory table exists...';

IF EXISTS (SELECT * FROM sys.tables WHERE name = 'AssignmentHistory')
    PRINT '✓ PASSED: AssignmentHistory table exists';
ELSE
BEGIN
    PRINT '✗ FAILED: AssignmentHistory table does not exist';
    PRINT 'ACTION: Run V3 migration script';
END
GO

-- ============================================================================
-- TEST 2: Verify Table Structure
-- ============================================================================
PRINT '';
PRINT 'TEST 2: Verifying table structure...';

DECLARE @ColumnCount INT = (
    SELECT COUNT(*) 
    FROM sys.columns 
    WHERE object_id = OBJECT_ID('AssignmentHistory')
);

IF @ColumnCount = 7
    PRINT '✓ PASSED: Table has correct number of columns (7)';
ELSE
    PRINT '✗ FAILED: Table has ' + CAST(@ColumnCount AS NVARCHAR(10)) + ' columns (expected 7)';

-- Verify specific columns
DECLARE @MissingColumns TABLE (ColumnName NVARCHAR(100));

IF NOT EXISTS (SELECT 1 FROM sys.columns WHERE object_id = OBJECT_ID('AssignmentHistory') AND name = 'Id')
    INSERT INTO @MissingColumns VALUES ('Id');
IF NOT EXISTS (SELECT 1 FROM sys.columns WHERE object_id = OBJECT_ID('AssignmentHistory') AND name = 'AssetId')
    INSERT INTO @MissingColumns VALUES ('AssetId');
IF NOT EXISTS (SELECT 1 FROM sys.columns WHERE object_id = OBJECT_ID('AssignmentHistory') AND name = 'AssignmentType')
    INSERT INTO @MissingColumns VALUES ('AssignmentType');
IF NOT EXISTS (SELECT 1 FROM sys.columns WHERE object_id = OBJECT_ID('AssignmentHistory') AND name = 'AssignedTo')
    INSERT INTO @MissingColumns VALUES ('AssignedTo');
IF NOT EXISTS (SELECT 1 FROM sys.columns WHERE object_id = OBJECT_ID('AssignmentHistory') AND name = 'AssignedBy')
    INSERT INTO @MissingColumns VALUES ('AssignedBy');
IF NOT EXISTS (SELECT 1 FROM sys.columns WHERE object_id = OBJECT_ID('AssignmentHistory') AND name = 'AssignedAt')
    INSERT INTO @MissingColumns VALUES ('AssignedAt');
IF NOT EXISTS (SELECT 1 FROM sys.columns WHERE object_id = OBJECT_ID('AssignmentHistory') AND name = 'UnassignedAt')
    INSERT INTO @MissingColumns VALUES ('UnassignedAt');

IF NOT EXISTS (SELECT 1 FROM @MissingColumns)
    PRINT '✓ PASSED: All required columns exist';
ELSE
BEGIN
    PRINT '✗ FAILED: Missing columns:';
    SELECT '  - ' + ColumnName FROM @MissingColumns;
END
GO

-- ============================================================================
-- TEST 3: Verify Foreign Key Constraints
-- ============================================================================
PRINT '';
PRINT 'TEST 3: Verifying foreign key constraints...';

DECLARE @FKCount INT = (
    SELECT COUNT(*) 
    FROM sys.foreign_keys 
    WHERE parent_object_id = OBJECT_ID('AssignmentHistory')
);

IF @FKCount = 2
    PRINT '✓ PASSED: Table has correct number of foreign keys (2)';
ELSE
    PRINT '✗ FAILED: Table has ' + CAST(@FKCount AS NVARCHAR(10)) + ' foreign keys (expected 2)';

-- Verify FK to Assets with CASCADE DELETE
IF EXISTS (
    SELECT 1 
    FROM sys.foreign_keys fk
    WHERE fk.parent_object_id = OBJECT_ID('AssignmentHistory')
    AND fk.name = 'FK_AssignmentHistory_AssetId'
    AND fk.delete_referential_action = 1  -- CASCADE
)
    PRINT '✓ PASSED: FK_AssignmentHistory_AssetId exists with CASCADE DELETE';
ELSE
    PRINT '✗ FAILED: FK_AssignmentHistory_AssetId missing or incorrect';

-- Verify FK to Users
IF EXISTS (
    SELECT 1 
    FROM sys.foreign_keys 
    WHERE parent_object_id = OBJECT_ID('AssignmentHistory')
    AND name = 'FK_AssignmentHistory_AssignedBy'
)
    PRINT '✓ PASSED: FK_AssignmentHistory_AssignedBy exists';
ELSE
    PRINT '✗ FAILED: FK_AssignmentHistory_AssignedBy missing';
GO

-- ============================================================================
-- TEST 4: Verify Check Constraint
-- ============================================================================
PRINT '';
PRINT 'TEST 4: Verifying check constraint...';

IF EXISTS (
    SELECT 1 
    FROM sys.check_constraints 
    WHERE parent_object_id = OBJECT_ID('AssignmentHistory')
    AND name = 'CHK_AssignmentHistory_Type'
)
    PRINT '✓ PASSED: CHK_AssignmentHistory_Type constraint exists';
ELSE
    PRINT '✗ FAILED: CHK_AssignmentHistory_Type constraint missing';
GO

-- ============================================================================
-- TEST 5: Verify Indexes
-- ============================================================================
PRINT '';
PRINT 'TEST 5: Verifying indexes...';

DECLARE @IndexCount INT = (
    SELECT COUNT(DISTINCT i.name)
    FROM sys.indexes i
    WHERE i.object_id = OBJECT_ID('AssignmentHistory')
    AND i.name IS NOT NULL  -- Exclude heap
);

IF @IndexCount >= 4  -- PK + 3 indexes
    PRINT '✓ PASSED: Table has correct number of indexes (4+)';
ELSE
    PRINT '✗ FAILED: Table has ' + CAST(@IndexCount AS NVARCHAR(10)) + ' indexes (expected 4+)';

-- Verify specific indexes
IF EXISTS (SELECT 1 FROM sys.indexes WHERE object_id = OBJECT_ID('AssignmentHistory') AND name = 'IX_AssignmentHistory_AssetId')
    PRINT '✓ PASSED: IX_AssignmentHistory_AssetId exists';
ELSE
    PRINT '✗ FAILED: IX_AssignmentHistory_AssetId missing';

IF EXISTS (SELECT 1 FROM sys.indexes WHERE object_id = OBJECT_ID('AssignmentHistory') AND name = 'IX_AssignmentHistory_AssignedTo')
    PRINT '✓ PASSED: IX_AssignmentHistory_AssignedTo exists';
ELSE
    PRINT '✗ FAILED: IX_AssignmentHistory_AssignedTo missing';

IF EXISTS (SELECT 1 FROM sys.indexes WHERE object_id = OBJECT_ID('AssignmentHistory') AND name = 'IX_AssignmentHistory_AssignedAt')
    PRINT '✓ PASSED: IX_AssignmentHistory_AssignedAt exists';
ELSE
    PRINT '✗ FAILED: IX_AssignmentHistory_AssignedAt missing';
GO

-- ============================================================================
-- TEST 6: Functional Test - Insert USER Assignment
-- ============================================================================
PRINT '';
PRINT 'TEST 6: Testing USER assignment insertion...';

BEGIN TRY
    -- Get test user and asset
    DECLARE @TestUserId UNIQUEIDENTIFIER = (SELECT TOP 1 Id FROM Users WHERE Username = 'admin');
    DECLARE @TestAssetId UNIQUEIDENTIFIER;
    
    -- Create or get test asset
    IF EXISTS (SELECT 1 FROM Assets WHERE SerialNumber = 'TEST-V3-001')
    BEGIN
        SET @TestAssetId = (SELECT Id FROM Assets WHERE SerialNumber = 'TEST-V3-001');
    END
    ELSE
    BEGIN
        SET @TestAssetId = NEWID();
        INSERT INTO Assets (Id, AssetType, Name, SerialNumber, AcquisitionDate, Status, CreatedBy, UpdatedBy)
        VALUES (
            @TestAssetId,
            'laptop',
            'Test Laptop V3',
            'TEST-V3-001',
            CAST(GETUTCDATE() AS DATE),
            'in_use',
            @TestUserId,
            @TestUserId
        );
    END
    
    -- Insert USER assignment
    INSERT INTO AssignmentHistory (Id, AssetId, AssignmentType, AssignedTo, AssignedBy, AssignedAt)
    VALUES (
        NEWID(),
        @TestAssetId,
        'USER',
        'Test User',
        @TestUserId,
        GETUTCDATE()
    );
    
    PRINT '✓ PASSED: USER assignment inserted successfully';
END TRY
BEGIN CATCH
    PRINT '✗ FAILED: USER assignment insertion failed';
    PRINT '  Error: ' + ERROR_MESSAGE();
END CATCH
GO

-- ============================================================================
-- TEST 7: Functional Test - Insert LOCATION Assignment
-- ============================================================================
PRINT '';
PRINT 'TEST 7: Testing LOCATION assignment insertion...';

BEGIN TRY
    DECLARE @TestUserId UNIQUEIDENTIFIER = (SELECT TOP 1 Id FROM Users WHERE Username = 'admin');
    DECLARE @TestAssetId UNIQUEIDENTIFIER = (SELECT Id FROM Assets WHERE SerialNumber = 'TEST-V3-001');
    
    -- Insert LOCATION assignment
    INSERT INTO AssignmentHistory (Id, AssetId, AssignmentType, AssignedTo, AssignedBy, AssignedAt)
    VALUES (
        NEWID(),
        @TestAssetId,
        'LOCATION',
        'Building A - Floor 3',
        @TestUserId,
        GETUTCDATE()
    );
    
    PRINT '✓ PASSED: LOCATION assignment inserted successfully';
END TRY
BEGIN CATCH
    PRINT '✗ FAILED: LOCATION assignment insertion failed';
    PRINT '  Error: ' + ERROR_MESSAGE();
END CATCH
GO

-- ============================================================================
-- TEST 8: Constraint Test - Invalid AssignmentType
-- ============================================================================
PRINT '';
PRINT 'TEST 8: Testing check constraint (should reject invalid type)...';

BEGIN TRY
    DECLARE @TestUserId UNIQUEIDENTIFIER = (SELECT TOP 1 Id FROM Users WHERE Username = 'admin');
    DECLARE @TestAssetId UNIQUEIDENTIFIER = (SELECT Id FROM Assets WHERE SerialNumber = 'TEST-V3-001');
    
    -- Attempt to insert invalid AssignmentType
    INSERT INTO AssignmentHistory (Id, AssetId, AssignmentType, AssignedTo, AssignedBy, AssignedAt)
    VALUES (
        NEWID(),
        @TestAssetId,
        'INVALID_TYPE',
        'Test User',
        @TestUserId,
        GETUTCDATE()
    );
    
    PRINT '✗ FAILED: Invalid AssignmentType was accepted (should have been rejected)';
END TRY
BEGIN CATCH
    IF ERROR_NUMBER() = 547  -- Check constraint violation
        PRINT '✓ PASSED: Invalid AssignmentType correctly rejected';
    ELSE
        PRINT '✗ FAILED: Unexpected error: ' + ERROR_MESSAGE();
END CATCH
GO

-- ============================================================================
-- TEST 9: Foreign Key Test - Non-existent Asset
-- ============================================================================
PRINT '';
PRINT 'TEST 9: Testing foreign key constraint (should reject non-existent asset)...';

BEGIN TRY
    DECLARE @TestUserId UNIQUEIDENTIFIER = (SELECT TOP 1 Id FROM Users WHERE Username = 'admin');
    DECLARE @NonExistentAssetId UNIQUEIDENTIFIER = NEWID();
    
    -- Attempt to insert with non-existent AssetId
    INSERT INTO AssignmentHistory (Id, AssetId, AssignmentType, AssignedTo, AssignedBy, AssignedAt)
    VALUES (
        NEWID(),
        @NonExistentAssetId,
        'USER',
        'Test User',
        @TestUserId,
        GETUTCDATE()
    );
    
    PRINT '✗ FAILED: Non-existent AssetId was accepted (should have been rejected)';
END TRY
BEGIN CATCH
    IF ERROR_NUMBER() = 547  -- Foreign key violation
        PRINT '✓ PASSED: Non-existent AssetId correctly rejected';
    ELSE
        PRINT '✗ FAILED: Unexpected error: ' + ERROR_MESSAGE();
END CATCH
GO

-- ============================================================================
-- TEST 10: CASCADE DELETE Test
-- ============================================================================
PRINT '';
PRINT 'TEST 10: Testing CASCADE DELETE...';

BEGIN TRY
    DECLARE @TestAssetId UNIQUEIDENTIFIER = (SELECT Id FROM Assets WHERE SerialNumber = 'TEST-V3-001');
    
    -- Count assignments before delete
    DECLARE @AssignmentCountBefore INT = (SELECT COUNT(*) FROM AssignmentHistory WHERE AssetId = @TestAssetId);
    
    -- Delete the test asset
    DELETE FROM Assets WHERE Id = @TestAssetId;
    
    -- Count assignments after delete (should be 0 due to CASCADE)
    DECLARE @AssignmentCountAfter INT = (SELECT COUNT(*) FROM AssignmentHistory WHERE AssetId = @TestAssetId);
    
    IF @AssignmentCountBefore > 0 AND @AssignmentCountAfter = 0
        PRINT '✓ PASSED: CASCADE DELETE working correctly (' + CAST(@AssignmentCountBefore AS NVARCHAR(10)) + ' assignments deleted)';
    ELSE IF @AssignmentCountBefore = 0
        PRINT '⚠ WARNING: No assignments found to test CASCADE DELETE';
    ELSE
        PRINT '✗ FAILED: CASCADE DELETE not working (assignments still exist)';
END TRY
BEGIN CATCH
    PRINT '✗ FAILED: CASCADE DELETE test error: ' + ERROR_MESSAGE();
END CATCH
GO

-- ============================================================================
-- TEST 11: Query Performance Test
-- ============================================================================
PRINT '';
PRINT 'TEST 11: Testing query performance with indexes...';

BEGIN TRY
    -- Test query by AssetId (should use IX_AssignmentHistory_AssetId)
    DECLARE @TestAssetId UNIQUEIDENTIFIER = (SELECT TOP 1 Id FROM Assets);
    
    SET STATISTICS IO ON;
    SET STATISTICS TIME ON;
    
    SELECT * FROM AssignmentHistory WHERE AssetId = @TestAssetId;
    
    SET STATISTICS IO OFF;
    SET STATISTICS TIME OFF;
    
    PRINT '✓ PASSED: Query executed successfully (check execution plan for index usage)';
END TRY
BEGIN CATCH
    PRINT '✗ FAILED: Query performance test error: ' + ERROR_MESSAGE();
END CATCH
GO

-- ============================================================================
-- Summary
-- ============================================================================
PRINT '';
PRINT '========================================================================';
PRINT 'Migration V3 Verification Complete';
PRINT '========================================================================';
PRINT '';
PRINT 'Review the test results above. All tests should show ✓ PASSED.';
PRINT '';
PRINT 'If any tests failed:';
PRINT '  1. Review the error messages';
PRINT '  2. Check the migration script for issues';
PRINT '  3. Verify prerequisite migrations (V1, V2) were successful';
PRINT '  4. Consult MIGRATION_V3_TESTING_GUIDE.md for troubleshooting';
PRINT '';
PRINT 'Next Steps:';
PRINT '  1. If all tests passed, proceed with application development';
PRINT '  2. Implement AssignmentHistory JPA entity';
PRINT '  3. Create AssignmentHistoryRepository';
PRINT '  4. Implement AllocationService business logic';
PRINT '';
PRINT '========================================================================';
GO

-- ============================================================================
-- Optional: Display Table Information
-- ============================================================================
PRINT '';
PRINT 'Table Structure Summary:';
PRINT '------------------------';

SELECT 
    c.name AS ColumnName,
    t.name AS DataType,
    c.max_length AS MaxLength,
    CASE WHEN c.is_nullable = 1 THEN 'YES' ELSE 'NO' END AS IsNullable,
    CASE WHEN pk.column_id IS NOT NULL THEN 'YES' ELSE 'NO' END AS IsPrimaryKey
FROM sys.columns c
INNER JOIN sys.types t ON c.user_type_id = t.user_type_id
LEFT JOIN (
    SELECT ic.object_id, ic.column_id
    FROM sys.index_columns ic
    INNER JOIN sys.indexes i ON ic.object_id = i.object_id AND ic.index_id = i.index_id
    WHERE i.is_primary_key = 1
) pk ON c.object_id = pk.object_id AND c.column_id = pk.column_id
WHERE c.object_id = OBJECT_ID('AssignmentHistory')
ORDER BY c.column_id;

PRINT '';
PRINT 'Indexes:';
PRINT '--------';

SELECT 
    i.name AS IndexName,
    i.type_desc AS IndexType,
    CASE WHEN i.is_unique = 1 THEN 'YES' ELSE 'NO' END AS IsUnique,
    COL_NAME(ic.object_id, ic.column_id) AS ColumnName
FROM sys.indexes i
INNER JOIN sys.index_columns ic ON i.object_id = ic.object_id AND i.index_id = ic.index_id
WHERE i.object_id = OBJECT_ID('AssignmentHistory')
AND i.name IS NOT NULL
ORDER BY i.name, ic.key_ordinal;

PRINT '';
PRINT 'Foreign Keys:';
PRINT '-------------';

SELECT 
    fk.name AS ForeignKeyName,
    COL_NAME(fkc.parent_object_id, fkc.parent_column_id) AS ColumnName,
    OBJECT_NAME(fk.referenced_object_id) AS ReferencedTable,
    COL_NAME(fkc.referenced_object_id, fkc.referenced_column_id) AS ReferencedColumn,
    fk.delete_referential_action_desc AS DeleteAction
FROM sys.foreign_keys fk
INNER JOIN sys.foreign_key_columns fkc ON fk.object_id = fkc.constraint_object_id
WHERE fk.parent_object_id = OBJECT_ID('AssignmentHistory')
ORDER BY fk.name;

PRINT '';
PRINT 'Check Constraints:';
PRINT '------------------';

SELECT 
    cc.name AS ConstraintName,
    cc.definition AS ConstraintDefinition
FROM sys.check_constraints cc
WHERE cc.parent_object_id = OBJECT_ID('AssignmentHistory');

GO
