-- ============================================================================
-- Fix Foreign Key Constraints - SQL Server
-- ============================================================================
-- This script drops all foreign key constraints and lets Hibernate recreate them
-- Run this in SQL Server Management Studio or Azure Data Studio
-- ============================================================================

USE IT_Asset;
GO

PRINT 'Dropping all foreign key constraints from Users table...';
GO

-- Drop all foreign key constraints on Users table
DECLARE @sql NVARCHAR(MAX) = '';

SELECT @sql += 'ALTER TABLE ' + QUOTENAME(OBJECT_SCHEMA_NAME(parent_object_id)) + '.' + 
               QUOTENAME(OBJECT_NAME(parent_object_id)) + 
               ' DROP CONSTRAINT ' + QUOTENAME(name) + ';' + CHAR(13)
FROM sys.foreign_keys
WHERE referenced_object_id = OBJECT_ID('Users') 
   OR parent_object_id = OBJECT_ID('Users');

PRINT 'Executing SQL:';
PRINT @sql;

EXEC sp_executesql @sql;

PRINT 'All foreign key constraints dropped successfully.';
PRINT '';
PRINT '============================================================================';
PRINT 'NEXT STEPS:';
PRINT '============================================================================';
PRINT '1. Stop any running backend instances';
PRINT '2. Kill process on port 8080 if needed';
PRINT '3. Start backend: mvn spring-boot:run -Dspring-boot.run.profiles=dev';
PRINT '4. Hibernate will recreate foreign keys with correct column names';
PRINT '============================================================================';
GO
