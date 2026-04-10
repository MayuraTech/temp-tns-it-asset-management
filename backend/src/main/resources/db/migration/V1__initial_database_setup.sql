-- V1__initial_database_setup.sql
-- Initial database setup for IT Asset Management
-- This script creates the database, configures user permissions, and enables read committed snapshot isolation

-- Note: This script should be run by a SQL Server administrator with appropriate privileges
-- The database creation and user configuration parts may need to be run separately from the table creation

-- ============================================================================
-- PART 1: Database Creation and Configuration
-- ============================================================================
-- This part should be executed first by a database administrator

-- Create database if it doesn't exist
IF NOT EXISTS (SELECT name FROM sys.databases WHERE name = 'ITAssetManagement')
BEGIN
    CREATE DATABASE ITAssetManagement;
    PRINT 'Database ITAssetManagement created successfully.';
END
ELSE
BEGIN
    PRINT 'Database ITAssetManagement already exists.';
END
GO

-- Switch to the new database
USE ITAssetManagement;
GO

-- Enable read committed snapshot isolation for optimistic concurrency control
-- This allows readers to read a consistent snapshot without blocking writers
IF (SELECT is_read_committed_snapshot_on FROM sys.databases WHERE name = 'ITAssetManagement') = 0
BEGIN
    ALTER DATABASE ITAssetManagement SET READ_COMMITTED_SNAPSHOT ON;
    PRINT 'Read committed snapshot isolation enabled.';
END
ELSE
BEGIN
    PRINT 'Read committed snapshot isolation already enabled.';
END
GO

-- ============================================================================
-- PART 2: User Creation and Permissions
-- ============================================================================
-- Create a dedicated database user for the application

-- Create SQL Server login (if it doesn't exist)
-- Note: Replace 'YourSecurePassword123!' with a strong password
IF NOT EXISTS (SELECT name FROM sys.server_principals WHERE name = 'ITAssetMgmtUser')
BEGIN
    CREATE LOGIN ITAssetMgmtUser WITH PASSWORD = 'YourSecurePassword123!';
    PRINT 'Login ITAssetMgmtUser created successfully.';
END
ELSE
BEGIN
    PRINT 'Login ITAssetMgmtUser already exists.';
END
GO

-- Create database user for the login
USE ITAssetManagement;
GO

IF NOT EXISTS (SELECT name FROM sys.database_principals WHERE name = 'ITAssetMgmtUser')
BEGIN
    CREATE USER ITAssetMgmtUser FOR LOGIN ITAssetMgmtUser;
    PRINT 'Database user ITAssetMgmtUser created successfully.';
END
ELSE
BEGIN
    PRINT 'Database user ITAssetMgmtUser already exists.';
END
GO

-- Grant appropriate permissions to the application user
-- The user needs to read, write, update, and delete data, but not alter schema
GRANT SELECT, INSERT, UPDATE, DELETE ON SCHEMA::dbo TO ITAssetMgmtUser;
GRANT EXECUTE ON SCHEMA::dbo TO ITAssetMgmtUser;
PRINT 'Permissions granted to ITAssetMgmtUser.';
GO

-- ============================================================================
-- PART 3: Database Configuration Settings
-- ============================================================================

-- Set database recovery model to FULL for production (enables point-in-time recovery)
ALTER DATABASE ITAssetManagement SET RECOVERY FULL;
GO

-- Set database compatibility level to SQL Server 2019 (150)
ALTER DATABASE ITAssetManagement SET COMPATIBILITY_LEVEL = 150;
GO

-- Enable automatic statistics updates for query optimization
ALTER DATABASE ITAssetManagement SET AUTO_UPDATE_STATISTICS ON;
GO

-- Enable automatic statistics creation
ALTER DATABASE ITAssetManagement SET AUTO_CREATE_STATISTICS ON;
GO

PRINT 'Database setup completed successfully.';
PRINT 'Database: ITAssetManagement';
PRINT 'User: ITAssetMgmtUser';
PRINT 'Read Committed Snapshot Isolation: ENABLED';
PRINT '';
PRINT 'Next steps:';
PRINT '1. Update the application.properties file with the connection string:';
PRINT '   spring.datasource.url=jdbc:sqlserver://localhost:1433;databaseName=ITAssetManagement;encrypt=true;trustServerCertificate=true';
PRINT '   spring.datasource.username=ITAssetMgmtUser';
PRINT '   spring.datasource.password=YourSecurePassword123!';
PRINT '2. Run subsequent migration scripts to create tables and seed initial data.';
GO
