-- V4__user_management_performance_optimization.sql
-- Performance Optimization for User Management Module
-- Adds additional indexes, query optimization, and caching strategies
-- Implements performance requirements for user management operations

USE ITAssetManagement;
GO

-- ============================================================================
-- PART 1: Add Missing Performance Indexes
-- ============================================================================

PRINT 'Adding performance optimization indexes...';
GO

-- Users table: Optimize email lookups (case-insensitive searches)
-- Create computed column for lowercase email to support case-insensitive index
IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('Users') AND name = 'EmailLower')
BEGIN
    ALTER TABLE Users ADD EmailLower AS LOWER(Email) PERSISTED;
    CREATE INDEX IX_Users_EmailLower ON Users(EmailLower);
    PRINT '  - Created case-insensitive email index';
END
GO

-- Users table: Optimize username lookups (case-insensitive searches)
IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('Users') AND name = 'UsernameLower')
BEGIN
    ALTER TABLE Users ADD UsernameLower AS LOWER(Username) PERSISTED;
    CREATE INDEX IX_Users_UsernameLower ON Users(UsernameLower);
    PRINT '  - Created case-insensitive username index';
END
GO

-- Users table: Composite index for active user searches with text filtering
IF NOT EXISTS (SELECT * FROM sys.indexes WHERE object_id = OBJECT_ID('Users') AND name = 'IX_Users_IsActive_Username_Email')
BEGIN
    CREATE INDEX IX_Users_IsActive_Username_Email ON Users(IsActive, Username, Email);
    PRINT '  - Created composite index for active user searches';
END
GO

-- Users table: Index for last login queries (finding inactive users)
IF NOT EXISTS (SELECT * FROM sys.indexes WHERE object_id = OBJECT_ID('Users') AND name = 'IX_Users_LastLoginAt')
BEGIN
    CREATE INDEX IX_Users_LastLoginAt ON Users(LastLoginAt) WHERE LastLoginAt IS NOT NULL;
    PRINT '  - Created filtered index for last login queries';
END
GO

-- Users table: Index for locked account queries
IF NOT EXISTS (SELECT * FROM sys.indexes WHERE object_id = OBJECT_ID('Users') AND name = 'IX_Users_AccountLocked_LockUntil')
BEGIN
    CREATE INDEX IX_Users_AccountLocked_LockUntil ON Users(AccountLocked, LockUntil) 
    WHERE AccountLocked = 1;
    PRINT '  - Created filtered index for locked accounts';
END
GO

-- Users table: Index for failed login attempt monitoring
IF NOT EXISTS (SELECT * FROM sys.indexes WHERE object_id = OBJECT_ID('Users') AND name = 'IX_Users_FailedLoginAttempts')
BEGIN
    CREATE INDEX IX_Users_FailedLoginAttempts ON Users(FailedLoginAttempts) 
    WHERE FailedLoginAttempts > 0;
    PRINT '  - Created filtered index for failed login attempts';
END
GO

-- UserRoles table: Composite index for role-based user queries
IF NOT EXISTS (SELECT * FROM sys.indexes WHERE object_id = OBJECT_ID('UserRoles') AND name = 'IX_UserRoles_Role_AssignedAt')
BEGIN
    CREATE INDEX IX_UserRoles_Role_AssignedAt ON UserRoles(Role, AssignedAt DESC);
    PRINT '  - Created composite index for role queries with sorting';
END
GO

-- UserRoles table: Index for assignment tracking and audit
IF NOT EXISTS (SELECT * FROM sys.indexes WHERE object_id = OBJECT_ID('UserRoles') AND name = 'IX_UserRoles_AssignedBy_AssignedAt')
BEGIN
    CREATE INDEX IX_UserRoles_AssignedBy_AssignedAt ON UserRoles(AssignedBy, AssignedAt DESC);
    PRINT '  - Created index for role assignment audit queries';
END
GO

-- Sessions table: Composite index for active session queries by user
IF NOT EXISTS (SELECT * FROM sys.indexes WHERE object_id = OBJECT_ID('Sessions') AND name = 'IX_Sessions_UserId_IsActive_TokenExpiration')
BEGIN
    CREATE INDEX IX_Sessions_UserId_IsActive_TokenExpiration 
    ON Sessions(UserId, IsActive, TokenExpiration);
    PRINT '  - Created composite index for active session queries';
END
GO

-- Sessions table: Index for token hash lookups (authentication)
IF NOT EXISTS (SELECT * FROM sys.indexes WHERE object_id = OBJECT_ID('Sessions') AND name = 'IX_Sessions_AccessTokenHash')
BEGIN
    CREATE INDEX IX_Sessions_AccessTokenHash ON Sessions(AccessTokenHash) 
    WHERE AccessTokenHash IS NOT NULL AND IsActive = 1;
    PRINT '  - Created filtered index for access token validation';
END
GO

IF NOT EXISTS (SELECT * FROM sys.indexes WHERE object_id = OBJECT_ID('Sessions') AND name = 'IX_Sessions_RefreshTokenHash')
BEGIN
    CREATE INDEX IX_Sessions_RefreshTokenHash ON Sessions(RefreshTokenHash) 
    WHERE RefreshTokenHash IS NOT NULL AND IsActive = 1;
    PRINT '  - Created filtered index for refresh token validation';
END
GO

-- Sessions table: Index for session cleanup queries
IF NOT EXISTS (SELECT * FROM sys.indexes WHERE object_id = OBJECT_ID('Sessions') AND name = 'IX_Sessions_IsActive_LoginAt')
BEGIN
    CREATE INDEX IX_Sessions_IsActive_LoginAt ON Sessions(IsActive, LoginAt);
    PRINT '  - Created index for session cleanup operations';
END
GO

-- Sessions table: Index for expired session detection
IF NOT EXISTS (SELECT * FROM sys.indexes WHERE object_id = OBJECT_ID('Sessions') AND name = 'IX_Sessions_IsActive_TokenExpiration_Cleanup')
BEGIN
    CREATE INDEX IX_Sessions_IsActive_TokenExpiration_Cleanup 
    ON Sessions(IsActive, TokenExpiration) 
    WHERE IsActive = 1;
    PRINT '  - Created filtered index for expired session detection';
END
GO

PRINT 'Performance indexes created successfully.';
GO

-- ============================================================================
-- PART 2: Create Indexed Views for Common Queries
-- ============================================================================

PRINT 'Creating indexed views for query optimization...';
GO

-- Indexed view for user role counts (frequently accessed for dashboard)
IF OBJECT_ID('vw_UserRoleCounts', 'V') IS NOT NULL
    DROP VIEW vw_UserRoleCounts;
GO

CREATE VIEW vw_UserRoleCounts
WITH SCHEMABINDING
AS
SELECT 
    Role,
    COUNT_BIG(*) AS UserCount
FROM dbo.UserRoles
GROUP BY Role;
GO

-- Create unique clustered index on the view for materialization
CREATE UNIQUE CLUSTERED INDEX IX_vw_UserRoleCounts 
ON vw_UserRoleCounts(Role);
GO

PRINT '  - Created indexed view for user role counts';
GO

-- Indexed view for active user statistics
IF OBJECT_ID('vw_ActiveUserStats', 'V') IS NOT NULL
    DROP VIEW vw_ActiveUserStats;
GO

CREATE VIEW vw_ActiveUserStats
WITH SCHEMABINDING
AS
SELECT 
    IsActive,
    AccountLocked,
    COUNT_BIG(*) AS UserCount
FROM dbo.Users
GROUP BY IsActive, AccountLocked;
GO

CREATE UNIQUE CLUSTERED INDEX IX_vw_ActiveUserStats 
ON vw_ActiveUserStats(IsActive, AccountLocked);
GO

PRINT '  - Created indexed view for active user statistics';
GO

-- ============================================================================
-- PART 3: Optimize Stored Procedures
-- ============================================================================

PRINT 'Optimizing stored procedures...';
GO

-- Enhanced session cleanup procedure with better performance
CREATE OR ALTER PROCEDURE sp_CleanupExpiredSessions
    @BatchSize INT = 1000,
    @RetentionDays INT = 30
AS
BEGIN
    SET NOCOUNT ON;
    
    DECLARE @DeletedCount INT = 0;
    DECLARE @TotalDeleted INT = 0;
    DECLARE @CurrentTime DATETIME2 = GETUTCDATE();
    DECLARE @CutoffDate DATETIME2 = DATEADD(DAY, -@RetentionDays, @CurrentTime);
    
    -- Mark expired sessions as inactive in batches
    WHILE 1 = 1
    BEGIN
        UPDATE TOP (@BatchSize) Sessions 
        SET IsActive = 0, LogoutAt = @CurrentTime
        WHERE IsActive = 1 AND TokenExpiration < @CurrentTime;
        
        SET @DeletedCount = @@ROWCOUNT;
        SET @TotalDeleted = @TotalDeleted + @DeletedCount;
        
        IF @DeletedCount < @BatchSize
            BREAK;
            
        -- Small delay to avoid blocking
        WAITFOR DELAY '00:00:00.100';
    END
    
    -- Delete old inactive sessions in batches
    SET @DeletedCount = 0;
    WHILE 1 = 1
    BEGIN
        DELETE TOP (@BatchSize) FROM Sessions 
        WHERE IsActive = 0 AND LoginAt < @CutoffDate;
        
        SET @DeletedCount = @@ROWCOUNT;
        
        IF @DeletedCount < @BatchSize
            BREAK;
            
        WAITFOR DELAY '00:00:00.100';
    END
    
    PRINT CONCAT('Cleaned up ', @TotalDeleted, ' expired sessions.');
    
    -- Return statistics
    SELECT 
        @TotalDeleted AS ExpiredSessionsMarked,
        (SELECT COUNT(*) FROM Sessions WHERE IsActive = 1) AS ActiveSessions,
        (SELECT COUNT(*) FROM Sessions WHERE IsActive = 0) AS InactiveSessions;
END
GO

PRINT '  - Enhanced session cleanup procedure';
GO

-- Enhanced account unlock procedure with logging
CREATE OR ALTER PROCEDURE sp_UnlockExpiredAccounts
AS
BEGIN
    SET NOCOUNT ON;
    
    DECLARE @UnlockedCount INT;
    DECLARE @CurrentTime DATETIME2 = GETUTCDATE();
    
    -- Create temp table to track unlocked accounts
    CREATE TABLE #UnlockedAccounts (
        UserId UNIQUEIDENTIFIER,
        Username NVARCHAR(100),
        LockDuration INT
    );
    
    -- Capture accounts being unlocked
    INSERT INTO #UnlockedAccounts (UserId, Username, LockDuration)
    SELECT 
        Id,
        Username,
        DATEDIFF(MINUTE, LockUntil, @CurrentTime) AS LockDuration
    FROM Users 
    WHERE AccountLocked = 1 
      AND LockUntil IS NOT NULL 
      AND LockUntil < @CurrentTime;
    
    -- Unlock the accounts
    UPDATE Users 
    SET AccountLocked = 0, 
        LockUntil = NULL,
        FailedLoginAttempts = 0,
        UpdatedAt = @CurrentTime
    WHERE Id IN (SELECT UserId FROM #UnlockedAccounts);
    
    SET @UnlockedCount = @@ROWCOUNT;
    
    -- Return statistics
    SELECT 
        @UnlockedCount AS AccountsUnlocked,
        AVG(LockDuration) AS AvgLockDurationMinutes,
        MAX(LockDuration) AS MaxLockDurationMinutes
    FROM #UnlockedAccounts;
    
    DROP TABLE #UnlockedAccounts;
    
    PRINT CONCAT('Unlocked ', @UnlockedCount, ' expired account locks.');
END
GO

PRINT '  - Enhanced account unlock procedure';
GO

-- New procedure for user search with optimized query
CREATE OR ALTER PROCEDURE sp_SearchUsers
    @SearchText NVARCHAR(255) = NULL,
    @IsActive BIT = NULL,
    @Role NVARCHAR(50) = NULL,
    @PageNumber INT = 0,
    @PageSize INT = 20
AS
BEGIN
    SET NOCOUNT ON;
    
    DECLARE @Offset INT = @PageNumber * @PageSize;
    DECLARE @SearchTextLower NVARCHAR(255) = LOWER(@SearchText);
    
    -- Get total count
    DECLARE @TotalCount INT;
    
    SELECT @TotalCount = COUNT(DISTINCT u.Id)
    FROM Users u
    LEFT JOIN UserRoles ur ON u.Id = ur.UserId
    WHERE (@SearchText IS NULL OR 
           u.UsernameLower LIKE '%' + @SearchTextLower + '%' OR 
           u.EmailLower LIKE '%' + @SearchTextLower + '%')
      AND (@IsActive IS NULL OR u.IsActive = @IsActive)
      AND (@Role IS NULL OR ur.Role = @Role);
    
    -- Get paginated results
    SELECT DISTINCT
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
        @TotalCount AS TotalCount,
        CEILING(CAST(@TotalCount AS FLOAT) / @PageSize) AS TotalPages
    FROM Users u
    LEFT JOIN UserRoles ur ON u.Id = ur.UserId
    WHERE (@SearchText IS NULL OR 
           u.UsernameLower LIKE '%' + @SearchTextLower + '%' OR 
           u.EmailLower LIKE '%' + @SearchTextLower + '%')
      AND (@IsActive IS NULL OR u.IsActive = @IsActive)
      AND (@Role IS NULL OR ur.Role = @Role)
    ORDER BY u.Username
    OFFSET @Offset ROWS
    FETCH NEXT @PageSize ROWS ONLY;
END
GO

PRINT '  - Created optimized user search procedure';
GO

-- New procedure for session statistics
CREATE OR ALTER PROCEDURE sp_GetSessionStatistics
    @StartDate DATETIME2 = NULL,
    @EndDate DATETIME2 = NULL
AS
BEGIN
    SET NOCOUNT ON;
    
    SET @StartDate = ISNULL(@StartDate, DATEADD(DAY, -30, GETUTCDATE()));
    SET @EndDate = ISNULL(@EndDate, GETUTCDATE());
    
    -- Overall statistics
    SELECT 
        COUNT(*) AS TotalSessions,
        COUNT(CASE WHEN IsActive = 1 THEN 1 END) AS ActiveSessions,
        COUNT(CASE WHEN IsActive = 0 THEN 1 END) AS InactiveSessions,
        COUNT(DISTINCT UserId) AS UniqueUsers,
        AVG(DATEDIFF(MINUTE, LoginAt, ISNULL(LogoutAt, TokenExpiration))) AS AvgSessionDurationMinutes
    FROM Sessions
    WHERE LoginAt BETWEEN @StartDate AND @EndDate;
    
    -- Daily session counts
    SELECT 
        CAST(LoginAt AS DATE) AS SessionDate,
        COUNT(*) AS SessionCount,
        COUNT(DISTINCT UserId) AS UniqueUsers
    FROM Sessions
    WHERE LoginAt BETWEEN @StartDate AND @EndDate
    GROUP BY CAST(LoginAt AS DATE)
    ORDER BY SessionDate DESC;
    
    -- Top users by session count
    SELECT TOP 10
        u.Username,
        u.Email,
        COUNT(s.Id) AS SessionCount,
        MAX(s.LoginAt) AS LastLogin
    FROM Sessions s
    INNER JOIN Users u ON s.UserId = u.Id
    WHERE s.LoginAt BETWEEN @StartDate AND @EndDate
    GROUP BY u.Username, u.Email
    ORDER BY SessionCount DESC;
END
GO

PRINT '  - Created session statistics procedure';
GO

-- ============================================================================
-- PART 4: Add Query Hints and Plan Guides (Optional)
-- ============================================================================

PRINT 'Configuring query optimization settings...';
GO

-- Update statistics on all user management tables
UPDATE STATISTICS Users WITH FULLSCAN;
UPDATE STATISTICS UserRoles WITH FULLSCAN;
UPDATE STATISTICS Sessions WITH FULLSCAN;

PRINT '  - Updated statistics for all tables';
GO

-- ============================================================================
-- PART 5: Create Performance Monitoring Views
-- ============================================================================

PRINT 'Creating performance monitoring views...';
GO

-- View for monitoring query performance
CREATE OR ALTER VIEW vw_UserManagementPerformance
AS
SELECT 
    'Users' AS TableName,
    (SELECT COUNT(*) FROM Users) AS TotalRecords,
    (SELECT COUNT(*) FROM Users WHERE IsActive = 1) AS ActiveRecords,
    (SELECT COUNT(*) FROM Users WHERE AccountLocked = 1) AS LockedRecords,
    (SELECT AVG(FailedLoginAttempts) FROM Users WHERE FailedLoginAttempts > 0) AS AvgFailedAttempts
UNION ALL
SELECT 
    'UserRoles' AS TableName,
    (SELECT COUNT(*) FROM UserRoles) AS TotalRecords,
    NULL AS ActiveRecords,
    NULL AS LockedRecords,
    NULL AS AvgFailedAttempts
UNION ALL
SELECT 
    'Sessions' AS TableName,
    (SELECT COUNT(*) FROM Sessions) AS TotalRecords,
    (SELECT COUNT(*) FROM Sessions WHERE IsActive = 1) AS ActiveRecords,
    (SELECT COUNT(*) FROM Sessions WHERE IsActive = 1 AND TokenExpiration < GETUTCDATE()) AS LockedRecords,
    NULL AS AvgFailedAttempts;
GO

PRINT '  - Created performance monitoring view';
GO

-- View for index usage statistics
CREATE OR ALTER VIEW vw_IndexUsageStats
AS
SELECT 
    OBJECT_NAME(s.object_id) AS TableName,
    i.name AS IndexName,
    i.type_desc AS IndexType,
    s.user_seeks AS UserSeeks,
    s.user_scans AS UserScans,
    s.user_lookups AS UserLookups,
    s.user_updates AS UserUpdates,
    s.last_user_seek AS LastUserSeek,
    s.last_user_scan AS LastUserScan
FROM sys.dm_db_index_usage_stats s
INNER JOIN sys.indexes i ON s.object_id = i.object_id AND s.index_id = i.index_id
WHERE s.database_id = DB_ID()
  AND OBJECT_NAME(s.object_id) IN ('Users', 'UserRoles', 'Sessions');
GO

PRINT '  - Created index usage statistics view';
GO

-- ============================================================================
-- PART 6: Add Caching Configuration
-- ============================================================================

PRINT 'Configuring caching settings...';
GO

-- Insert caching configuration
DECLARE @AdminUserId UNIQUEIDENTIFIER = (SELECT Id FROM Users WHERE Username = 'admin');

MERGE Configurations AS target
USING (VALUES 
    ('UserCacheTTLSeconds', '300', 'number', 'User data cache TTL in seconds (5 minutes)'),
    ('RoleCacheTTLSeconds', '600', 'number', 'Role permission cache TTL in seconds (10 minutes)'),
    ('SessionCacheTTLSeconds', '60', 'number', 'Session validation cache TTL in seconds (1 minute)'),
    ('EnableQueryResultCaching', 'true', 'boolean', 'Enable query result caching for user searches'),
    ('MaxCachedUsers', '1000', 'number', 'Maximum number of users to cache in memory'),
    ('EnableIndexedViews', 'true', 'boolean', 'Enable indexed views for performance optimization')
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

PRINT '  - Caching configuration added';
GO

-- ============================================================================
-- PART 7: Performance Testing and Validation
-- ============================================================================

PRINT 'Running performance validation...';
GO

-- Test query performance with new indexes
DECLARE @StartTime DATETIME2;
DECLARE @EndTime DATETIME2;
DECLARE @Duration INT;

-- Test 1: User search by username
SET @StartTime = SYSDATETIME();
SELECT TOP 100 * FROM Users WHERE UsernameLower LIKE '%admin%';
SET @EndTime = SYSDATETIME();
SET @Duration = DATEDIFF(MILLISECOND, @StartTime, @EndTime);
PRINT CONCAT('  - User search by username: ', @Duration, 'ms');

-- Test 2: Active user query
SET @StartTime = SYSDATETIME();
SELECT COUNT(*) FROM Users WHERE IsActive = 1;
SET @EndTime = SYSDATETIME();
SET @Duration = DATEDIFF(MILLISECOND, @StartTime, @EndTime);
PRINT CONCAT('  - Active user count: ', @Duration, 'ms');

-- Test 3: Role-based user query
SET @StartTime = SYSDATETIME();
SELECT DISTINCT u.* FROM Users u 
INNER JOIN UserRoles ur ON u.Id = ur.UserId 
WHERE ur.Role = 'Administrator';
SET @EndTime = SYSDATETIME();
SET @Duration = DATEDIFF(MILLISECOND, @StartTime, @EndTime);
PRINT CONCAT('  - Role-based user query: ', @Duration, 'ms');

-- Test 4: Active session query
SET @StartTime = SYSDATETIME();
SELECT COUNT(*) FROM Sessions WHERE IsActive = 1 AND TokenExpiration > GETUTCDATE();
SET @EndTime = SYSDATETIME();
SET @Duration = DATEDIFF(MILLISECOND, @StartTime, @EndTime);
PRINT CONCAT('  - Active session count: ', @Duration, 'ms');

PRINT 'Performance validation completed.';
GO

-- ============================================================================
-- PART 8: Summary and Recommendations
-- ============================================================================

PRINT '';
PRINT '========================================================================';
PRINT 'User Management Performance Optimization V4 Completed Successfully';
PRINT '========================================================================';
PRINT '';
PRINT 'Performance Enhancements:';
PRINT '  - Added 14 new performance indexes (including filtered and composite)';
PRINT '  - Created 2 indexed views for frequently accessed aggregations';
PRINT '  - Optimized stored procedures with batch processing';
PRINT '  - Added case-insensitive search optimization with computed columns';
PRINT '  - Implemented query result caching configuration';
PRINT '  - Created performance monitoring views';
PRINT '';
PRINT 'New Indexes Created:';
PRINT '  - IX_Users_EmailLower (case-insensitive email search)';
PRINT '  - IX_Users_UsernameLower (case-insensitive username search)';
PRINT '  - IX_Users_IsActive_Username_Email (composite for active user searches)';
PRINT '  - IX_Users_LastLoginAt (filtered index for inactive user detection)';
PRINT '  - IX_Users_AccountLocked_LockUntil (filtered index for locked accounts)';
PRINT '  - IX_Users_FailedLoginAttempts (filtered index for security monitoring)';
PRINT '  - IX_UserRoles_Role_AssignedAt (composite for role queries with sorting)';
PRINT '  - IX_UserRoles_AssignedBy_AssignedAt (audit trail optimization)';
PRINT '  - IX_Sessions_UserId_IsActive_TokenExpiration (composite for session queries)';
PRINT '  - IX_Sessions_AccessTokenHash (filtered index for token validation)';
PRINT '  - IX_Sessions_RefreshTokenHash (filtered index for token refresh)';
PRINT '  - IX_Sessions_IsActive_LoginAt (session cleanup optimization)';
PRINT '  - IX_Sessions_IsActive_TokenExpiration_Cleanup (expired session detection)';
PRINT '';
PRINT 'New Stored Procedures:';
PRINT '  - sp_SearchUsers (optimized user search with pagination)';
PRINT '  - sp_GetSessionStatistics (session analytics and reporting)';
PRINT '  - Enhanced sp_CleanupExpiredSessions (batch processing)';
PRINT '  - Enhanced sp_UnlockExpiredAccounts (with logging)';
PRINT '';
PRINT 'New Views:';
PRINT '  - vw_UserRoleCounts (indexed view for role distribution)';
PRINT '  - vw_ActiveUserStats (indexed view for user statistics)';
PRINT '  - vw_UserManagementPerformance (performance monitoring)';
PRINT '  - vw_IndexUsageStats (index usage analysis)';
PRINT '';
PRINT 'Performance Targets:';
PRINT '  - User search queries: < 200ms for pages up to 100 users';
PRINT '  - Authentication (login): < 500ms under normal load';
PRINT '  - Session validation: < 50ms with caching';
PRINT '  - Role-based queries: < 100ms with indexed views';
PRINT '';
PRINT 'Caching Configuration:';
PRINT '  - User data cache: 5 minutes TTL';
PRINT '  - Role permissions cache: 10 minutes TTL';
PRINT '  - Session validation cache: 1 minute TTL';
PRINT '  - Maximum cached users: 1000';
PRINT '';
PRINT 'Recommendations:';
PRINT '  1. Monitor index usage with vw_IndexUsageStats view';
PRINT '  2. Run sp_CleanupExpiredSessions daily during off-peak hours';
PRINT '  3. Implement application-level caching for frequently accessed data';
PRINT '  4. Consider partitioning Sessions table if volume exceeds 1M records';
PRINT '  5. Review and update statistics weekly for optimal query plans';
PRINT '  6. Monitor query performance with vw_UserManagementPerformance';
PRINT '  7. Implement connection pooling with HikariCP (min: 5, max: 20)';
PRINT '  8. Enable query result caching in Spring Data JPA';
PRINT '';
PRINT 'Next Steps:';
PRINT '  1. Configure HikariCP connection pool in application.properties';
PRINT '  2. Implement Redis caching for user and session data';
PRINT '  3. Add @Cacheable annotations to frequently called service methods';
PRINT '  4. Monitor database performance metrics in production';
PRINT '  5. Set up automated index maintenance jobs';
PRINT '';
PRINT '========================================================================';
GO
