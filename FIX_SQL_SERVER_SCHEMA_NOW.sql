-- ============================================================================
-- SQL Server Schema Fix Script
-- ============================================================================
-- This script drops and recreates the IT_Asset database with the correct schema
-- Run this in SQL Server Management Studio or Azure Data Studio
-- ============================================================================

USE master;
GO

-- Step 1: Drop the database if it exists
IF EXISTS (SELECT name FROM sys.databases WHERE name = 'IT_Asset')
BEGIN
    PRINT 'Dropping existing IT_Asset database...';
    ALTER DATABASE IT_Asset SET SINGLE_USER WITH ROLLBACK IMMEDIATE;
    DROP DATABASE IT_Asset;
    PRINT 'Database dropped successfully.';
END
ELSE
BEGIN
    PRINT 'IT_Asset database does not exist. Creating new database...';
END
GO

-- Step 2: Create fresh database
CREATE DATABASE IT_Asset;
GO

PRINT 'IT_Asset database created successfully.';
PRINT '';
PRINT '============================================================================';
PRINT 'NEXT STEPS:';
PRINT '============================================================================';
PRINT '1. Stop your backend application (Ctrl+C if running)';
PRINT '2. Start backend with: mvn spring-boot:run -Dspring-boot.run.profiles=dev';
PRINT '3. Watch logs for: "Hibernate: create table Users..."';
PRINT '4. Watch logs for: "DataInitializer: Created user: admin"';
PRINT '5. Test login at http://localhost:4200 with admin/Admin@123456';
PRINT '============================================================================';
GO
