-- ============================================================================
-- IT Asset Management - Database Setup Script
-- ============================================================================
-- Purpose: Create and configure the ITAssetManagement database
-- 
-- This script performs:
-- 1. Database creation
-- 2. User creation with appropriate permissions
-- 3. Read committed snapshot isolation configuration
--
-- Prerequisites:
-- - SQL Server 2019 or later
-- - Execute with administrator privileges (sa or sysadmin role)
--
-- Usage:
--   sqlcmd -S localhost -U sa -P YourPassword -i database-setup.sql
--   OR execute in SQL Server Management Studio
--
-- IMPORTANT: Update the password before executing in production!
-- ============================================================================

PRINT '============================================================================';
PRINT 'IT Asset Management - Database Setup';
PRINT '============================================================================';
PRINT '';

-- ============================================================================
-- STEP 1: Create Database
-- ============================================================================
PRINT 'STEP 1: Creating database...';

IF NOT EXISTS (SELECT name FROM sys.databases WHERE name = 'ITAssetManagement')
BEGIN
    CREATE DATABASE ITAssetManagement;
    PRINT '✓ Database ITAssetManagement created successfully.';
END
ELSE
BEGIN
    PRINT '⚠ Database ITAssetManagement already exists. Skipping creation.';
END
GO

-- ============================================================================
-- STEP 2: Configure Database Settings
-- ============================================================================
PRINT '';
PRINT 'STEP 2: Configuring database settings...';

-- Switch to the database
USE ITAssetManagement;
GO

-- Enable read committed snapshot isolation
IF (SELECT is_read_committed_snapshot_on FROM sys.databases WHERE name = 'ITAssetManagement') = 0
BEGIN
    -- Note: This requires exclusive access to the database
    -- If there are active connections, they will be rolled back
    ALTER DATABASE ITAssetManagement SET SINGLE_USER WITH ROLLBACK IMMEDIATE;
    ALTER DATABASE ITAssetManagement SET READ_COMMITTED_SNAPSHOT ON;
    ALTER DATABASE ITAssetManagement SET MULTI_USER;
    PRINT '✓ Read committed snapshot isolation enabled.';
END
ELSE
BEGIN
    PRINT '⚠ Read committed snapshot isolation already enabled.';
END
GO

-- Set recovery model to FULL for production
IF (SELECT recovery_model_desc FROM sys.databases WHERE name = 'ITAssetManagement') <> 'FULL'
BEGIN
    ALTER DATABASE ITAssetManagement SET RECOVERY FULL;
    PRINT '✓ Recovery model set to FULL.';
END
ELSE
BEGIN
    PRINT '⚠ Recovery model already set to FULL.';
END
GO

-- Set compatibility level to SQL Server 2019 (150)
IF (SELECT compatibility_level FROM sys.databases WHERE name = 'ITAssetManagement') <> 150
BEGIN
    ALTER DATABASE ITAssetManagement SET COMPATIBILITY_LEVEL = 150;
    PRINT '✓ Compatibility level set to 150 (SQL Server 2019).';
END
ELSE
BEGIN
    PRINT '⚠ Compatibility level already set to 150.';
END
GO

-- Enable automatic statistics
ALTER DATABASE ITAssetManagement SET AUTO_UPDATE_STATISTICS ON;
ALTER DATABASE ITAssetManagement SET AUTO_CREATE_STATISTICS ON;
PRINT '✓ Automatic statistics enabled.';
GO

-- ============================================================================
-- STEP 3: Create Application User
-- ============================================================================
PRINT '';
PRINT 'STEP 3: Creating application user...';

-- Create SQL Server login
-- ⚠️ SECURITY WARNING: Change this password before running in production!
DECLARE @Password NVARCHAR(128) = 'YourSecurePassword123!';

IF NOT EXISTS (SELECT name FROM sys.server_principals WHERE name = 'ITAssetMgmtUser')
BEGIN
    DECLARE @CreateLoginSQL NVARCHAR(MAX);
    SET @CreateLoginSQL = 'CREATE LOGIN ITAssetMgmtUser WITH PASSWORD = ''' + @Password + ''', CHECK_POLICY = ON, CHECK_EXPIRATION = OFF;';
    EXEC sp_executesql @CreateLoginSQL;
    PRINT '✓ Login ITAssetMgmtUser created successfully.';
    PRINT '⚠️ WARNING: Default password is being used. Change it immediately in production!';
END
ELSE
BEGIN
    PRINT '⚠ Login ITAssetMgmtUser already exists. Skipping creation.';
END
GO

-- Create database user
USE ITAssetManagement;
GO

IF NOT EXISTS (SELECT name FROM sys.database_principals WHERE name = 'ITAssetMgmtUser')
BEGIN
    CREATE USER ITAssetMgmtUser FOR LOGIN ITAssetMgmtUser;
    PRINT '✓ Database user ITAssetMgmtUser created successfully.';
END
ELSE
BEGIN
    PRINT '⚠ Database user ITAssetMgmtUser already exists. Skipping creation.';
END
GO

-- ============================================================================
-- STEP 4: Grant Permissions
-- ============================================================================
PRINT '';
PRINT 'STEP 4: Granting permissions...';

-- Grant data manipulation permissions
GRANT SELECT ON SCHEMA::dbo TO ITAssetMgmtUser;
PRINT '✓ SELECT permission granted.';

GRANT INSERT ON SCHEMA::dbo TO ITAssetMgmtUser;
PRINT '✓ INSERT permission granted.';

GRANT UPDATE ON SCHEMA::dbo TO ITAssetMgmtUser;
PRINT '✓ UPDATE permission granted.';

GRANT DELETE ON SCHEMA::dbo TO ITAssetMgmtUser;
PRINT '✓ DELETE permission granted.';

GRANT EXECUTE ON SCHEMA::dbo TO ITAssetMgmtUser;
PRINT '✓ EXECUTE permission granted.';

-- Note: User does NOT have DDL permissions (CREATE, ALTER, DROP)
-- Schema changes are managed through Flyway migrations
GO

-- ============================================================================
-- STEP 5: Verification
-- ============================================================================
PRINT '';
PRINT 'STEP 5: Verifying setup...';
PRINT '';

-- Verify database configuration
PRINT 'Database Configuration:';
SELECT 
    name AS DatabaseName,
    CASE WHEN is_read_committed_snapshot_on = 1 THEN '✓ Enabled' ELSE '✗ Disabled' END AS ReadCommittedSnapshot,
    recovery_model_desc AS RecoveryModel,
    compatibility_level AS CompatibilityLevel
FROM sys.databases 
WHERE name = 'ITAssetManagement';

PRINT '';
PRINT 'User Permissions:';
SELECT 
    dp.name AS UserName,
    p.permission_name AS Permission,
    p.state_desc AS State
FROM sys.database_permissions p
INNER JOIN sys.database_principals dp ON p.grantee_principal_id = dp.principal_id
WHERE dp.name = 'ITAssetMgmtUser'
ORDER BY p.permission_name;

-- ============================================================================
-- STEP 6: Summary and Next Steps
-- ============================================================================
PRINT '';
PRINT '============================================================================';
PRINT 'Database Setup Complete!';
PRINT '============================================================================';
PRINT '';
PRINT 'Summary:';
PRINT '  • Database: ITAssetManagement';
PRINT '  • User: ITAssetMgmtUser';
PRINT '  • Read Committed Snapshot Isolation: ENABLED';
PRINT '  • Recovery Model: FULL';
PRINT '  • Compatibility Level: 150 (SQL Server 2019)';
PRINT '';
PRINT 'Next Steps:';
PRINT '  1. Update application.properties with connection details:';
PRINT '     spring.datasource.url=jdbc:sqlserver://localhost:1433;databaseName=ITAssetManagement';
PRINT '     spring.datasource.username=ITAssetMgmtUser';
PRINT '     spring.datasource.password=YourSecurePassword123!';
PRINT '';
PRINT '  2. For production, change the password:';
PRINT '     ALTER LOGIN ITAssetMgmtUser WITH PASSWORD = ''YourNewSecurePassword'';';
PRINT '';
PRINT '  3. Store credentials securely using environment variables:';
PRINT '     export DB_USERNAME=ITAssetMgmtUser';
PRINT '     export DB_PASSWORD=YourSecurePassword';
PRINT '';
PRINT '  4. Run the Spring Boot application. Flyway will automatically:';
PRINT '     • Create all database tables';
PRINT '     • Set up indexes and constraints';
PRINT '     • Seed initial data (default admin user)';
PRINT '';
PRINT '============================================================================';
GO
