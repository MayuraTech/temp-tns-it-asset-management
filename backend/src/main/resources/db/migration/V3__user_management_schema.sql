-- V3__user_management_schema.sql
-- User Management Schema Enhancement for IT Asset Management
-- Enhances existing user tables with complete user management functionality
-- Adds missing fields, constraints, triggers, and indexes for comprehensive user management

USE ITAssetManagement;
GO

-- ============================================================================
-- PART 1: Enhance Users Table
-- ============================================================================

-- Add missing columns to Users table for complete user management
ALTER TABLE Users ADD 
    CreatedBy UNIQUEIDENTIFIER NULL,
    UpdatedBy UNIQUEIDENTIFIER NULL;

-- Add foreign key constraints for audit fields
ALTER TABLE Users ADD 
    CONSTRAINT FK_Users_CreatedBy FOREIGN KEY (CreatedBy) REFERENCES Users(Id),
    CONSTRAINT FK_Users_UpdatedBy FOREIGN KEY (UpdatedBy) REFERENCES Users(Id);

-- Add check constraints for data validation
ALTER TABLE Users ADD 
    CONSTRAINT CHK_Users_Username CHECK (LEN(Username) >= 3 AND LEN(Username) <= 100),
    CONSTRAINT CHK_Users_Email CHECK (LEN(Email) >= 5 AND LEN(Email) <= 255 AND Email LIKE '%@%.%');

-- Make Email unique if not already
IF NOT EXISTS (SELECT * FROM sys.indexes WHERE object_id = OBJECT_ID('Users') AND name = 'UQ_Users_Email')
BEGIN
    ALTER TABLE Users ADD CONSTRAINT UQ_Users_Email UNIQUE (Email);
END

-- Add additional indexes for performance
CREATE INDEX IX_Users_IsActive ON Users(IsActive);
CREATE INDEX IX_Users_CreatedAt ON Users(CreatedAt);
CREATE INDEX IX_Users_UpdatedAt ON Users(UpdatedAt);

PRINT 'Users table enhanced successfully.';
GO

-- ============================================================================
-- PART 2: Enhance UserRoles Table
-- ============================================================================

-- Add unique constraint to prevent duplicate role assignments
IF NOT EXISTS (SELECT * FROM sys.indexes WHERE object_id = OBJECT_ID('UserRoles') AND name = 'UQ_UserRoles_UserId_Role')
BEGIN
    ALTER TABLE UserRoles ADD CONSTRAINT UQ_UserRoles_UserId_Role UNIQUE (UserId, Role);
END

PRINT 'UserRoles table enhanced successfully.';
GO

-- ============================================================================
-- PART 3: Enhance Sessions Table
-- ============================================================================

-- Drop existing Sessions table and recreate with proper schema for JWT token management
DROP TABLE Sessions;
GO

-- Create new Sessions table with JWT token support
CREATE TABLE Sessions (
    Id UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
    UserId UNIQUEIDENTIFIER NOT NULL,
    LoginAt DATETIME2 NOT NULL DEFAULT GETUTCDATE(),
    LogoutAt DATETIME2 NULL,
    TokenExpiration DATETIME2 NOT NULL,
    IsActive BIT NOT NULL DEFAULT 1,
    AccessTokenHash NVARCHAR(500) NULL,
    RefreshTokenHash NVARCHAR(500) NULL,
    
    CONSTRAINT FK_Sessions_UserId FOREIGN KEY (UserId) REFERENCES Users(Id) ON DELETE CASCADE
);

-- Create indexes for Sessions table
CREATE INDEX IX_Sessions_UserId ON Sessions(UserId);
CREATE INDEX IX_Sessions_TokenExpiration ON Sessions(TokenExpiration);
CREATE INDEX IX_Sessions_IsActive ON Sessions(IsActive);
CREATE INDEX IX_Sessions_LoginAt ON Sessions(LoginAt);

PRINT 'Sessions table recreated with JWT token support.';
GO

-- ============================================================================
-- PART 4: Create Database Triggers for UpdatedAt Management
-- ============================================================================

-- Create trigger for Users table UpdatedAt field
CREATE OR ALTER TRIGGER TR_Users_UpdatedAt
ON Users
AFTER UPDATE
AS
BEGIN
    SET NOCOUNT ON;
    
    UPDATE Users 
    SET UpdatedAt = GETUTCDATE()
    FROM Users u
    INNER JOIN inserted i ON u.Id = i.Id;
END
GO

PRINT 'UpdatedAt trigger created for Users table.';
GO

-- ============================================================================
-- PART 5: Create Stored Procedures for User Management Operations
-- ============================================================================

-- Stored procedure to clean up expired sessions
CREATE OR ALTER PROCEDURE sp_CleanupExpiredSessions
AS
BEGIN
    SET NOCOUNT ON;
    
    DECLARE @DeletedCount INT;
    
    -- Mark expired sessions as inactive
    UPDATE Sessions 
    SET IsActive = 0, LogoutAt = GETUTCDATE()
    WHERE IsActive = 1 AND TokenExpiration < GETUTCDATE();
    
    SET @DeletedCount = @@ROWCOUNT;
    
    -- Delete sessions older than 30 days
    DELETE FROM Sessions 
    WHERE LogoutAt IS NOT NULL AND LogoutAt < DATEADD(DAY, -30, GETUTCDATE());
    
    PRINT CONCAT('Cleaned up ', @DeletedCount, ' expired sessions.');
END
GO

PRINT 'Session cleanup stored procedure created.';
GO

-- Stored procedure to unlock expired account locks
CREATE OR ALTER PROCEDURE sp_UnlockExpiredAccounts
AS
BEGIN
    SET NOCOUNT ON;
    
    DECLARE @UnlockedCount INT;
    
    UPDATE Users 
    SET AccountLocked = 0, 
        LockUntil = NULL,
        FailedLoginAttempts = 0
    WHERE AccountLocked = 1 
      AND LockUntil IS NOT NULL 
      AND LockUntil < GETUTCDATE();
    
    SET @UnlockedCount = @@ROWCOUNT;
    
    PRINT CONCAT('Unlocked ', @UnlockedCount, ' expired account locks.');
END
GO

PRINT 'Account unlock stored procedure created.';
GO

-- ============================================================================
-- PART 6: Create Views for User Management
-- ============================================================================

-- View for user details with role information
CREATE OR ALTER VIEW vw_UserDetails
AS
SELECT 
    u.Id,
    u.Username,
    u.Email,
    u.IsActive,
    u.AccountLocked,
    u.LockUntil,
    u.FailedLoginAttempts,
    u.LastLoginAt,
    u.CreatedAt,
    u.UpdatedAt,
    cb.Username AS CreatedBy,
    ub.Username AS UpdatedBy,
    STRING_AGG(ur.Role, ', ') AS Roles,
    COUNT(ur.Role) AS RoleCount
FROM Users u
LEFT JOIN Users cb ON u.CreatedBy = cb.Id
LEFT JOIN Users ub ON u.UpdatedBy = ub.Id
LEFT JOIN UserRoles ur ON u.Id = ur.UserId
GROUP BY 
    u.Id, u.Username, u.Email, u.IsActive, u.AccountLocked, 
    u.LockUntil, u.FailedLoginAttempts, u.LastLoginAt, 
    u.CreatedAt, u.UpdatedAt, cb.Username, ub.Username;
GO

PRINT 'User details view created.';
GO

-- View for active sessions
CREATE OR ALTER VIEW vw_ActiveSessions
AS
SELECT 
    s.Id,
    s.UserId,
    u.Username,
    u.Email,
    s.LoginAt,
    s.TokenExpiration,
    DATEDIFF(MINUTE, GETUTCDATE(), s.TokenExpiration) AS MinutesUntilExpiration,
    CASE 
        WHEN s.TokenExpiration > GETUTCDATE() THEN 'Active'
        ELSE 'Expired'
    END AS Status
FROM Sessions s
INNER JOIN Users u ON s.UserId = u.Id
WHERE s.IsActive = 1;
GO

PRINT 'Active sessions view created.';
GO

-- ============================================================================
-- PART 7: Update Default Admin User
-- ============================================================================

-- Update the default admin user to have proper audit fields
DECLARE @AdminUserId UNIQUEIDENTIFIER = (SELECT Id FROM Users WHERE Username = 'admin');

UPDATE Users 
SET CreatedBy = @AdminUserId,
    UpdatedBy = @AdminUserId
WHERE Id = @AdminUserId;

PRINT 'Default admin user updated with audit fields.';
GO

-- ============================================================================
-- PART 8: Create Indexes for Performance Optimization
-- ============================================================================

-- Additional composite indexes for common queries
CREATE INDEX IX_Users_IsActive_AccountLocked ON Users(IsActive, AccountLocked);
CREATE INDEX IX_UserRoles_Role_UserId ON UserRoles(Role, UserId);
CREATE INDEX IX_Sessions_UserId_IsActive ON Sessions(UserId, IsActive);

PRINT 'Performance optimization indexes created.';
GO

-- ============================================================================
-- PART 9: Insert Configuration for User Management
-- ============================================================================

-- Insert user management specific configurations
DECLARE @AdminUserId UNIQUEIDENTIFIER = (SELECT Id FROM Users WHERE Username = 'admin');

-- Insert or update user management configurations
MERGE Configurations AS target
USING (VALUES 
    ('MaxFailedLoginAttempts', '5', 'number', 'Maximum failed login attempts before account lock'),
    ('AccountLockDurationMinutes', '30', 'number', 'Account lock duration in minutes after max failed attempts'),
    ('AccessTokenExpirationMinutes', '30', 'number', 'JWT access token expiration time in minutes'),
    ('RefreshTokenExpirationHours', '24', 'number', 'JWT refresh token expiration time in hours'),
    ('PasswordComplexityRegex', '^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$', 'string', 'Password complexity validation regex'),
    ('UsernameValidationRegex', '^[a-zA-Z0-9_]+$', 'string', 'Username format validation regex'),
    ('SessionCleanupIntervalHours', '24', 'number', 'Interval for automatic session cleanup in hours'),
    ('PasswordHashStrength', '10', 'number', 'BCrypt password hash strength (4-31)')
) AS source (ConfigKey, ConfigValue, ValueType, Description)
ON target.ConfigKey = source.ConfigKey
WHEN MATCHED THEN
    UPDATE SET 
        ConfigValue = source.ConfigValue,
        ValueType = source.ValueType,
        Description = source.Description,
        UpdatedBy = @AdminUserId,
        UpdatedAt = GETUTCDATE()
WHEN NOT MATCHED THEN
    INSERT (Id, ConfigKey, ConfigValue, ValueType, Description, UpdatedBy, UpdatedAt)
    VALUES (NEWID(), source.ConfigKey, source.ConfigValue, source.ValueType, source.Description, @AdminUserId, GETUTCDATE());

PRINT 'User management configurations inserted/updated.';
GO

-- ============================================================================
-- PART 10: Create SQL Server Agent Jobs (Optional - for automated maintenance)
-- ============================================================================

-- Note: These jobs require SQL Server Agent and appropriate permissions
-- They can be created manually by a DBA if needed

/*
-- Job to clean up expired sessions (runs every hour)
EXEC dbo.sp_add_job
    @job_name = N'ITAssetMgmt_SessionCleanup',
    @enabled = 1,
    @description = N'Cleanup expired sessions for IT Asset Management';

EXEC dbo.sp_add_jobstep
    @job_name = N'ITAssetMgmt_SessionCleanup',
    @step_name = N'Cleanup Sessions',
    @subsystem = N'TSQL',
    @database_name = N'ITAssetManagement',
    @command = N'EXEC sp_CleanupExpiredSessions';

EXEC dbo.sp_add_schedule
    @schedule_name = N'Hourly_SessionCleanup',
    @freq_type = 4,
    @freq_interval = 1,
    @freq_subday_type = 8,
    @freq_subday_interval = 1;

EXEC dbo.sp_attach_schedule
    @job_name = N'ITAssetMgmt_SessionCleanup',
    @schedule_name = N'Hourly_SessionCleanup';

-- Job to unlock expired accounts (runs every 5 minutes)
EXEC dbo.sp_add_job
    @job_name = N'ITAssetMgmt_AccountUnlock',
    @enabled = 1,
    @description = N'Unlock expired account locks for IT Asset Management';

EXEC dbo.sp_add_jobstep
    @job_name = N'ITAssetMgmt_AccountUnlock',
    @step_name = N'Unlock Accounts',
    @subsystem = N'TSQL',
    @database_name = N'ITAssetManagement',
    @command = N'EXEC sp_UnlockExpiredAccounts';

EXEC dbo.sp_add_schedule
    @schedule_name = N'Every5Minutes_AccountUnlock',
    @freq_type = 4,
    @freq_interval = 1,
    @freq_subday_type = 4,
    @freq_subday_interval = 5;

EXEC dbo.sp_attach_schedule
    @job_name = N'ITAssetMgmt_AccountUnlock',
    @schedule_name = N'Every5Minutes_AccountUnlock';
*/

-- ============================================================================
-- PART 11: Data Validation and Integrity Checks
-- ============================================================================

-- Verify all users have at least one role
DECLARE @UsersWithoutRoles INT = (
    SELECT COUNT(*)
    FROM Users u
    LEFT JOIN UserRoles ur ON u.Id = ur.UserId
    WHERE ur.UserId IS NULL
);

IF @UsersWithoutRoles > 0
BEGIN
    PRINT CONCAT('WARNING: ', @UsersWithoutRoles, ' users found without roles. Please assign roles to all users.');
END
ELSE
BEGIN
    PRINT 'All users have at least one role assigned.';
END

-- Verify foreign key integrity
DECLARE @OrphanedUserRoles INT = (
    SELECT COUNT(*)
    FROM UserRoles ur
    LEFT JOIN Users u ON ur.UserId = u.Id
    WHERE u.Id IS NULL
);

IF @OrphanedUserRoles > 0
BEGIN
    PRINT CONCAT('ERROR: ', @OrphanedUserRoles, ' orphaned user roles found. Data integrity issue detected.');
END
ELSE
BEGIN
    PRINT 'No orphaned user roles found. Data integrity verified.';
END

-- ============================================================================
-- PART 12: Summary and Next Steps
-- ============================================================================

PRINT '';
PRINT '========================================================================';
PRINT 'User Management Schema Enhancement V3 Completed Successfully';
PRINT '========================================================================';
PRINT '';
PRINT 'Enhancements Made:';
PRINT '  - Enhanced Users table with audit fields (CreatedBy, UpdatedBy)';
PRINT '  - Added comprehensive validation constraints for usernames and emails';
PRINT '  - Recreated Sessions table with JWT token support';
PRINT '  - Added unique constraint for UserRoles to prevent duplicates';
PRINT '  - Created UpdatedAt trigger for automatic timestamp management';
PRINT '  - Added stored procedures for session cleanup and account unlocking';
PRINT '  - Created views for user details and active sessions';
PRINT '  - Added performance optimization indexes';
PRINT '  - Inserted user management specific configurations';
PRINT '';
PRINT 'New Database Objects:';
PRINT '  - Trigger: TR_Users_UpdatedAt (automatic UpdatedAt field management)';
PRINT '  - Stored Procedure: sp_CleanupExpiredSessions';
PRINT '  - Stored Procedure: sp_UnlockExpiredAccounts';
PRINT '  - View: vw_UserDetails (user information with roles)';
PRINT '  - View: vw_ActiveSessions (current active sessions)';
PRINT '';
PRINT 'Configuration Settings Added:';
PRINT '  - MaxFailedLoginAttempts: 5';
PRINT '  - AccountLockDurationMinutes: 30';
PRINT '  - AccessTokenExpirationMinutes: 30';
PRINT '  - RefreshTokenExpirationHours: 24';
PRINT '  - PasswordComplexityRegex: Strong password requirements';
PRINT '  - UsernameValidationRegex: Alphanumeric and underscore only';
PRINT '  - SessionCleanupIntervalHours: 24';
PRINT '  - PasswordHashStrength: 10 (BCrypt strength)';
PRINT '';
PRINT 'Security Features:';
PRINT '  - Account locking after 5 failed login attempts';
PRINT '  - Automatic account unlock after 30 minutes';
PRINT '  - JWT token expiration (30 min access, 24 hour refresh)';
PRINT '  - Password complexity enforcement';
PRINT '  - Username format validation';
PRINT '  - Comprehensive audit trail with CreatedBy/UpdatedBy fields';
PRINT '';
PRINT 'Maintenance Features:';
PRINT '  - Automatic session cleanup for expired sessions';
PRINT '  - Automatic account unlock for expired locks';
PRINT '  - Performance optimized indexes for common queries';
PRINT '  - Data integrity validation and constraints';
PRINT '';
PRINT 'Next Steps for Development:';
PRINT '  1. Implement User entity with JPA annotations';
PRINT '  2. Create UserRole and Session entities';
PRINT '  3. Implement JWT token provider and authentication service';
PRINT '  4. Create user management REST controllers';
PRINT '  5. Add comprehensive unit and integration tests';
PRINT '  6. Implement frontend Angular components';
PRINT '';
PRINT 'Operational Recommendations:';
PRINT '  1. Set up automated jobs for session cleanup and account unlocking';
PRINT '  2. Monitor user management metrics and audit logs';
PRINT '  3. Regularly review and update password complexity requirements';
PRINT '  4. Implement backup and recovery procedures for user data';
PRINT '  5. Consider implementing additional security features (MFA, etc.)';
PRINT '';
PRINT '========================================================================';
GO