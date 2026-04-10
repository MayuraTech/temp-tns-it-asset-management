-- V2__initial_schema.sql
-- Initial database schema for IT Asset Management
-- Creates all core tables, indexes, constraints, and seeds default data

USE ITAssetManagement;
GO

-- ============================================================================
-- PART 1: Table Creation
-- ============================================================================

-- Users Table
-- Stores user account information including authentication and account status
CREATE TABLE Users (
    Id UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
    Username NVARCHAR(100) NOT NULL UNIQUE,
    PasswordHash NVARCHAR(255) NOT NULL,
    Email NVARCHAR(255) NOT NULL,
    CreatedAt DATETIME2 NOT NULL DEFAULT GETUTCDATE(),
    UpdatedAt DATETIME2 NOT NULL DEFAULT GETUTCDATE(),
    LastLoginAt DATETIME2 NULL,
    AccountLocked BIT NOT NULL DEFAULT 0,
    LockUntil DATETIME2 NULL,
    FailedLoginAttempts INT NOT NULL DEFAULT 0,
    IsActive BIT NOT NULL DEFAULT 1
);

-- Create indexes for Users table
CREATE INDEX IX_Users_Username ON Users(Username);
CREATE INDEX IX_Users_Email ON Users(Email);
CREATE INDEX IX_Users_AccountLocked ON Users(AccountLocked);

PRINT 'Users table created successfully.';
GO

-- UserRoles Table
-- Stores role assignments for users with audit information
CREATE TABLE UserRoles (
    Id UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
    UserId UNIQUEIDENTIFIER NOT NULL,
    Role NVARCHAR(50) NOT NULL,
    AssignedBy UNIQUEIDENTIFIER NOT NULL,
    AssignedAt DATETIME2 NOT NULL DEFAULT GETUTCDATE(),
    
    CONSTRAINT FK_UserRoles_UserId FOREIGN KEY (UserId) REFERENCES Users(Id) ON DELETE CASCADE,
    CONSTRAINT FK_UserRoles_AssignedBy FOREIGN KEY (AssignedBy) REFERENCES Users(Id),
    CONSTRAINT CHK_UserRoles_Role CHECK (Role IN ('Administrator', 'Asset_Manager', 'Viewer'))
);

-- Create indexes for UserRoles table
CREATE INDEX IX_UserRoles_UserId ON UserRoles(UserId);
CREATE INDEX IX_UserRoles_Role ON UserRoles(Role);

PRINT 'UserRoles table created successfully.';
GO

-- Sessions Table
-- Stores active user sessions for authentication and session management
CREATE TABLE Sessions (
    Id UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
    UserId UNIQUEIDENTIFIER NOT NULL,
    SessionToken NVARCHAR(500) NOT NULL UNIQUE,
    CreatedAt DATETIME2 NOT NULL DEFAULT GETUTCDATE(),
    LastActivityAt DATETIME2 NOT NULL DEFAULT GETUTCDATE(),
    ExpiresAt DATETIME2 NOT NULL,
    IpAddress NVARCHAR(45) NULL,
    UserAgent NVARCHAR(500) NULL,
    IsActive BIT NOT NULL DEFAULT 1,
    
    CONSTRAINT FK_Sessions_UserId FOREIGN KEY (UserId) REFERENCES Users(Id) ON DELETE CASCADE
);

-- Create indexes for Sessions table
CREATE INDEX IX_Sessions_UserId ON Sessions(UserId);
CREATE INDEX IX_Sessions_SessionToken ON Sessions(SessionToken);
CREATE INDEX IX_Sessions_ExpiresAt ON Sessions(ExpiresAt);
CREATE INDEX IX_Sessions_IsActive ON Sessions(IsActive);

PRINT 'Sessions table created successfully.';
GO

-- Assets Table
-- Stores IT asset information including lifecycle status and assignment details
CREATE TABLE Assets (
    Id UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
    AssetType NVARCHAR(50) NOT NULL,
    Name NVARCHAR(255) NOT NULL,
    SerialNumber NVARCHAR(100) NOT NULL UNIQUE,
    AcquisitionDate DATE NOT NULL,
    Status NVARCHAR(50) NOT NULL,
    Location NVARCHAR(255) NULL,
    AssignedUser NVARCHAR(255) NULL,
    AssignedUserEmail NVARCHAR(255) NULL,
    AssignmentDate DATETIME2 NULL,
    LocationUpdateDate DATETIME2 NULL,
    Notes NVARCHAR(MAX) NULL,
    CustomFields NVARCHAR(MAX) NULL, -- JSON string
    CreatedAt DATETIME2 NOT NULL DEFAULT GETUTCDATE(),
    CreatedBy UNIQUEIDENTIFIER NOT NULL,
    UpdatedAt DATETIME2 NOT NULL DEFAULT GETUTCDATE(),
    UpdatedBy UNIQUEIDENTIFIER NOT NULL,
    ReadOnly BIT NOT NULL DEFAULT 0,
    
    CONSTRAINT FK_Assets_CreatedBy FOREIGN KEY (CreatedBy) REFERENCES Users(Id),
    CONSTRAINT FK_Assets_UpdatedBy FOREIGN KEY (UpdatedBy) REFERENCES Users(Id),
    CONSTRAINT CHK_Assets_AssetType CHECK (AssetType IN ('server', 'workstation', 'network_device', 'storage_device', 'software_license', 'peripheral', 'keyboard', 'mouse', 'laptop', 'monitor', 'headset', 'laptop_charger', 'hdmi_cable', 'network_cable', 'access_card')),
    CONSTRAINT CHK_Assets_Status CHECK (Status IN ('ordered', 'received', 'deployed', 'in_use', 'maintenance', 'storage', 'retired')),
    CONSTRAINT CHK_Assets_AcquisitionDate CHECK (AcquisitionDate <= CAST(GETUTCDATE() AS DATE))
);

-- Create indexes for Assets table
CREATE INDEX IX_Assets_AssetType ON Assets(AssetType);
CREATE INDEX IX_Assets_SerialNumber ON Assets(SerialNumber);
CREATE INDEX IX_Assets_Status ON Assets(Status);
CREATE INDEX IX_Assets_Location ON Assets(Location);
CREATE INDEX IX_Assets_AssignedUser ON Assets(AssignedUser);
CREATE INDEX IX_Assets_AcquisitionDate ON Assets(AcquisitionDate);
CREATE INDEX IX_Assets_CreatedBy ON Assets(CreatedBy);

PRINT 'Assets table created successfully.';
GO

-- AssignmentHistory Table
-- Tracks the history of asset assignments to users and locations
CREATE TABLE AssignmentHistory (
    Id UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
    AssetId UNIQUEIDENTIFIER NOT NULL,
    AssignmentType NVARCHAR(20) NOT NULL,
    AssignedTo NVARCHAR(255) NOT NULL,
    AssignedBy UNIQUEIDENTIFIER NOT NULL,
    AssignedAt DATETIME2 NOT NULL DEFAULT GETUTCDATE(),
    UnassignedAt DATETIME2 NULL,
    
    CONSTRAINT FK_AssignmentHistory_AssetId FOREIGN KEY (AssetId) REFERENCES Assets(Id) ON DELETE CASCADE,
    CONSTRAINT FK_AssignmentHistory_AssignedBy FOREIGN KEY (AssignedBy) REFERENCES Users(Id),
    CONSTRAINT CHK_AssignmentHistory_Type CHECK (AssignmentType IN ('USER', 'LOCATION'))
);

-- Create indexes for AssignmentHistory table
CREATE INDEX IX_AssignmentHistory_AssetId ON AssignmentHistory(AssetId);
CREATE INDEX IX_AssignmentHistory_AssignedTo ON AssignmentHistory(AssignedTo);
CREATE INDEX IX_AssignmentHistory_AssignedAt ON AssignmentHistory(AssignedAt);

PRINT 'AssignmentHistory table created successfully.';
GO

-- AuditLog Table
-- Immutable audit trail of all system operations for compliance and investigation
CREATE TABLE AuditLog (
    Id UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
    Timestamp DATETIME2 NOT NULL DEFAULT GETUTCDATE(),
    UserId UNIQUEIDENTIFIER NOT NULL,
    Username NVARCHAR(100) NOT NULL,
    ActionType NVARCHAR(50) NOT NULL,
    ResourceType NVARCHAR(50) NOT NULL,
    ResourceId NVARCHAR(100) NOT NULL,
    Changes NVARCHAR(MAX) NULL, -- JSON string
    Metadata NVARCHAR(MAX) NULL, -- JSON string
    IpAddress NVARCHAR(45) NULL,
    
    CONSTRAINT FK_AuditLog_UserId FOREIGN KEY (UserId) REFERENCES Users(Id),
    CONSTRAINT CHK_AuditLog_ActionType CHECK (ActionType IN ('CREATE', 'UPDATE', 'DELETE', 'STATUS_CHANGE', 'LOGIN', 'LOGOUT', 'FAILED_LOGIN', 'TICKET_CREATE', 'TICKET_UPDATE', 'TICKET_APPROVE', 'TICKET_REJECT', 'TICKET_COMPLETE'))
);

-- Create indexes for AuditLog table
CREATE INDEX IX_AuditLog_Timestamp ON AuditLog(Timestamp);
CREATE INDEX IX_AuditLog_UserId ON AuditLog(UserId);
CREATE INDEX IX_AuditLog_ActionType ON AuditLog(ActionType);
CREATE INDEX IX_AuditLog_ResourceType ON AuditLog(ResourceType);
CREATE INDEX IX_AuditLog_ResourceId ON AuditLog(ResourceId);

PRINT 'AuditLog table created successfully.';
GO

-- Tickets Table
-- Stores asset allocation and de-allocation requests with approval workflow
CREATE TABLE Tickets (
    Id UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
    TicketNumber NVARCHAR(50) NOT NULL UNIQUE,
    Type NVARCHAR(20) NOT NULL,
    Status NVARCHAR(20) NOT NULL,
    Priority NVARCHAR(20) NOT NULL,
    AssetId UNIQUEIDENTIFIER NOT NULL,
    AssetName NVARCHAR(255) NOT NULL,
    AssetSerialNumber NVARCHAR(100) NOT NULL,
    RequesterId UNIQUEIDENTIFIER NOT NULL,
    RequesterName NVARCHAR(255) NOT NULL,
    AssignToUser NVARCHAR(255) NULL,
    AssignToUserEmail NVARCHAR(255) NULL,
    AssignToLocation NVARCHAR(255) NULL,
    RequestReason NVARCHAR(MAX) NULL,
    DeallocationReason NVARCHAR(MAX) NULL,
    ApproverId UNIQUEIDENTIFIER NULL,
    ApproverName NVARCHAR(255) NULL,
    ApprovalComments NVARCHAR(MAX) NULL,
    RejectionReason NVARCHAR(MAX) NULL,
    CreatedAt DATETIME2 NOT NULL DEFAULT GETUTCDATE(),
    UpdatedAt DATETIME2 NOT NULL DEFAULT GETUTCDATE(),
    ApprovedAt DATETIME2 NULL,
    RejectedAt DATETIME2 NULL,
    CompletedAt DATETIME2 NULL,
    CancelledAt DATETIME2 NULL,
    
    CONSTRAINT FK_Tickets_AssetId FOREIGN KEY (AssetId) REFERENCES Assets(Id),
    CONSTRAINT FK_Tickets_RequesterId FOREIGN KEY (RequesterId) REFERENCES Users(Id),
    CONSTRAINT FK_Tickets_ApproverId FOREIGN KEY (ApproverId) REFERENCES Users(Id),
    CONSTRAINT CHK_Tickets_Type CHECK (Type IN ('allocation', 'deallocation')),
    CONSTRAINT CHK_Tickets_Status CHECK (Status IN ('pending', 'approved', 'rejected', 'in_progress', 'completed', 'cancelled')),
    CONSTRAINT CHK_Tickets_Priority CHECK (Priority IN ('low', 'medium', 'high', 'urgent'))
);

-- Create indexes for Tickets table
CREATE INDEX IX_Tickets_TicketNumber ON Tickets(TicketNumber);
CREATE INDEX IX_Tickets_Status ON Tickets(Status);
CREATE INDEX IX_Tickets_Type ON Tickets(Type);
CREATE INDEX IX_Tickets_Priority ON Tickets(Priority);
CREATE INDEX IX_Tickets_AssetId ON Tickets(AssetId);
CREATE INDEX IX_Tickets_RequesterId ON Tickets(RequesterId);
CREATE INDEX IX_Tickets_ApproverId ON Tickets(ApproverId);
CREATE INDEX IX_Tickets_CreatedAt ON Tickets(CreatedAt);

PRINT 'Tickets table created successfully.';
GO

-- TicketStatusHistory Table
-- Tracks the history of ticket status changes for audit and workflow tracking
CREATE TABLE TicketStatusHistory (
    Id UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
    TicketId UNIQUEIDENTIFIER NOT NULL,
    FromStatus NVARCHAR(20) NULL,
    ToStatus NVARCHAR(20) NOT NULL,
    ChangedBy UNIQUEIDENTIFIER NOT NULL,
    ChangedAt DATETIME2 NOT NULL DEFAULT GETUTCDATE(),
    Comments NVARCHAR(MAX) NULL,
    
    CONSTRAINT FK_TicketStatusHistory_TicketId FOREIGN KEY (TicketId) REFERENCES Tickets(Id) ON DELETE CASCADE,
    CONSTRAINT FK_TicketStatusHistory_ChangedBy FOREIGN KEY (ChangedBy) REFERENCES Users(Id),
    CONSTRAINT CHK_TicketStatusHistory_ToStatus CHECK (ToStatus IN ('pending', 'approved', 'rejected', 'in_progress', 'completed', 'cancelled'))
);

-- Create indexes for TicketStatusHistory table
CREATE INDEX IX_TicketStatusHistory_TicketId ON TicketStatusHistory(TicketId);
CREATE INDEX IX_TicketStatusHistory_ChangedAt ON TicketStatusHistory(ChangedAt);

PRINT 'TicketStatusHistory table created successfully.';
GO

-- Configurations Table
-- Stores system configuration settings with versioning and audit information
CREATE TABLE Configurations (
    Id UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
    ConfigKey NVARCHAR(100) NOT NULL UNIQUE,
    ConfigValue NVARCHAR(MAX) NOT NULL,
    ValueType NVARCHAR(50) NOT NULL,
    Description NVARCHAR(500) NULL,
    UpdatedBy UNIQUEIDENTIFIER NOT NULL,
    UpdatedAt DATETIME2 NOT NULL DEFAULT GETUTCDATE(),
    
    CONSTRAINT FK_Configurations_UpdatedBy FOREIGN KEY (UpdatedBy) REFERENCES Users(Id),
    CONSTRAINT CHK_Configurations_ValueType CHECK (ValueType IN ('string', 'number', 'boolean', 'json'))
);

-- Create index for Configurations table
CREATE INDEX IX_Configurations_ConfigKey ON Configurations(ConfigKey);

PRINT 'Configurations table created successfully.';
GO

-- ============================================================================
-- PART 2: Default Data Insertion
-- ============================================================================

-- Insert default administrator user
-- Password: Admin@123456
-- BCrypt hash with strength 10
DECLARE @AdminUserId UNIQUEIDENTIFIER = NEWID();

INSERT INTO Users (Id, Username, PasswordHash, Email, AccountLocked, FailedLoginAttempts, IsActive)
VALUES (
    @AdminUserId,
    'admin',
    '$2a$10$rZ5c3qLHPpJQKEKGXJZvHOxM5Y8vYqKZQJZvHOxM5Y8vYqKZQJZvHO', -- BCrypt hash for 'Admin@123456'
    'admin@example.com',
    0,
    0,
    1
);

PRINT 'Default admin user created successfully.';
GO

-- Assign Administrator role to default admin
DECLARE @AdminUserId UNIQUEIDENTIFIER = (SELECT Id FROM Users WHERE Username = 'admin');

INSERT INTO UserRoles (Id, UserId, Role, AssignedBy, AssignedAt)
VALUES (
    NEWID(),
    @AdminUserId,
    'Administrator',
    @AdminUserId,
    GETUTCDATE()
);

PRINT 'Administrator role assigned to default admin user.';
GO

-- Insert default system configurations
DECLARE @AdminUserId UNIQUEIDENTIFIER = (SELECT Id FROM Users WHERE Username = 'admin');

INSERT INTO Configurations (Id, ConfigKey, ConfigValue, ValueType, Description, UpdatedBy, UpdatedAt)
VALUES 
    (
        NEWID(),
        'SessionTimeoutMinutes',
        '30',
        'number',
        'Session timeout duration in minutes (10-120)',
        @AdminUserId,
        GETUTCDATE()
    ),
    (
        NEWID(),
        'LifespanThresholds',
        '{"server":60,"workstation":48,"network_device":72,"storage_device":60,"software_license":36,"peripheral":36,"keyboard":24,"mouse":24,"laptop":48,"monitor":60,"headset":24,"laptop_charger":24,"hdmi_cable":36,"network_cable":36,"access_card":60}',
        'json',
        'Asset lifespan thresholds in months by asset type',
        @AdminUserId,
        GETUTCDATE()
    ),
    (
        NEWID(),
        'CustomAssetTypes',
        '[]',
        'json',
        'Custom asset types defined by administrators',
        @AdminUserId,
        GETUTCDATE()
    ),
    (
        NEWID(),
        'CustomFields',
        '[]',
        'json',
        'Custom field definitions for assets',
        @AdminUserId,
        GETUTCDATE()
    );

PRINT 'Default configurations inserted successfully.';
GO

-- ============================================================================
-- PART 3: Summary and Verification
-- ============================================================================

PRINT '';
PRINT '========================================================================';
PRINT 'Database Schema Migration V2 Completed Successfully';
PRINT '========================================================================';
PRINT '';
PRINT 'Tables Created:';
PRINT '  - Users (with indexes on Username, Email, AccountLocked)';
PRINT '  - UserRoles (with indexes on UserId, Role)';
PRINT '  - Sessions (with indexes on UserId, SessionToken, ExpiresAt, IsActive)';
PRINT '  - Assets (with indexes on AssetType, SerialNumber, Status, Location, AssignedUser, AcquisitionDate, CreatedBy)';
PRINT '  - AssignmentHistory (with indexes on AssetId, AssignedTo, AssignedAt)';
PRINT '  - AuditLog (with indexes on Timestamp, UserId, ActionType, ResourceType, ResourceId)';
PRINT '  - Tickets (with indexes on TicketNumber, Status, Type, Priority, AssetId, RequesterId, ApproverId, CreatedAt)';
PRINT '  - TicketStatusHistory (with indexes on TicketId, ChangedAt)';
PRINT '  - Configurations (with index on ConfigKey)';
PRINT '';
PRINT 'Default Data Inserted:';
PRINT '  - Default admin user (username: admin, password: Admin@123456)';
PRINT '  - Administrator role assigned to admin user';
PRINT '  - Default configurations:';
PRINT '    * SessionTimeoutMinutes: 30';
PRINT '    * LifespanThresholds: Asset-specific thresholds in months';
PRINT '    * CustomAssetTypes: Empty array (ready for customization)';
PRINT '    * CustomFields: Empty array (ready for customization)';
PRINT '';
PRINT 'Security Notes:';
PRINT '  - Change the default admin password immediately after first login';
PRINT '  - Review and adjust configuration values as needed for your environment';
PRINT '  - Ensure proper backup procedures are in place';
PRINT '';
PRINT 'Next Steps:';
PRINT '  1. Log in with admin credentials (admin / Admin@123456)';
PRINT '  2. Change the default admin password';
PRINT '  3. Create additional user accounts as needed';
PRINT '  4. Configure system settings via the Configurations table';
PRINT '  5. Begin registering assets in the system';
PRINT '';
PRINT '========================================================================';
GO
