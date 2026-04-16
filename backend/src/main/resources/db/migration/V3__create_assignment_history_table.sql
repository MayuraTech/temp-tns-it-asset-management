-- V3__create_assignment_history_table.sql
-- Creates the AssignmentHistory table for Module 3: Allocation Management
-- Tracks the history of asset assignments to users and locations

USE ITAssetManagement;
GO

-- ============================================================================
-- AssignmentHistory Table
-- ============================================================================
-- Tracks the complete history of asset assignments including:
-- - User assignments (asset assigned to a specific user)
-- - Location assignments (asset assigned to a physical/logical location)
-- - Assignment audit trail (who assigned, when, and when unassigned)

-- Check if table already exists (for idempotent migrations)
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'AssignmentHistory' AND schema_id = SCHEMA_ID('dbo'))
BEGIN
    CREATE TABLE AssignmentHistory (
        Id UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
        AssetId UNIQUEIDENTIFIER NOT NULL,
        AssignmentType NVARCHAR(20) NOT NULL,
        AssignedTo NVARCHAR(255) NOT NULL,
        AssignedBy UNIQUEIDENTIFIER NOT NULL,
        AssignedAt DATETIME2 NOT NULL DEFAULT GETUTCDATE(),
        UnassignedAt DATETIME2 NULL,
        
        -- Foreign key constraints
        CONSTRAINT FK_AssignmentHistory_AssetId 
            FOREIGN KEY (AssetId) REFERENCES Assets(Id) ON DELETE CASCADE,
        CONSTRAINT FK_AssignmentHistory_AssignedBy 
            FOREIGN KEY (AssignedBy) REFERENCES Users(Id),
        
        -- Check constraint for AssignmentType enum values
        CONSTRAINT CHK_AssignmentHistory_Type 
            CHECK (AssignmentType IN ('USER', 'LOCATION'))
    );

    PRINT 'AssignmentHistory table created successfully.';
END
ELSE
BEGIN
    PRINT 'AssignmentHistory table already exists. Skipping table creation.';
END
GO

-- ============================================================================
-- Indexes for Performance Optimization
-- ============================================================================

-- Index on AssetId for fast lookup of assignment history by asset
IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name = 'IX_AssignmentHistory_AssetId' AND object_id = OBJECT_ID('AssignmentHistory'))
BEGIN
    CREATE INDEX IX_AssignmentHistory_AssetId ON AssignmentHistory(AssetId);
    PRINT 'Index IX_AssignmentHistory_AssetId created successfully.';
END
ELSE
BEGIN
    PRINT 'Index IX_AssignmentHistory_AssetId already exists.';
END

-- Index on AssignedTo for querying assets by user or location
IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name = 'IX_AssignmentHistory_AssignedTo' AND object_id = OBJECT_ID('AssignmentHistory'))
BEGIN
    CREATE INDEX IX_AssignmentHistory_AssignedTo ON AssignmentHistory(AssignedTo);
    PRINT 'Index IX_AssignmentHistory_AssignedTo created successfully.';
END
ELSE
BEGIN
    PRINT 'Index IX_AssignmentHistory_AssignedTo already exists.';
END

-- Index on AssignedAt for chronological ordering and date range queries
IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name = 'IX_AssignmentHistory_AssignedAt' AND object_id = OBJECT_ID('AssignmentHistory'))
BEGIN
    CREATE INDEX IX_AssignmentHistory_AssignedAt ON AssignmentHistory(AssignedAt);
    PRINT 'Index IX_AssignmentHistory_AssignedAt created successfully.';
END
ELSE
BEGIN
    PRINT 'Index IX_AssignmentHistory_AssignedAt already exists.';
END

PRINT 'All indexes verified/created on AssignmentHistory table.';
GO

-- ============================================================================
-- Verification and Summary
-- ============================================================================

PRINT '';
PRINT '========================================================================';
PRINT 'Database Schema Migration V3 Completed Successfully';
PRINT '========================================================================';
PRINT '';
PRINT 'Table Created:';
PRINT '  - AssignmentHistory';
PRINT '';
PRINT 'Columns:';
PRINT '  - Id: UNIQUEIDENTIFIER (Primary Key, auto-generated)';
PRINT '  - AssetId: UNIQUEIDENTIFIER (Foreign Key to Assets table)';
PRINT '  - AssignmentType: NVARCHAR(20) (USER or LOCATION)';
PRINT '  - AssignedTo: NVARCHAR(255) (User name or location name)';
PRINT '  - AssignedBy: UNIQUEIDENTIFIER (Foreign Key to Users table)';
PRINT '  - AssignedAt: DATETIME2 (Timestamp of assignment, default: current UTC time)';
PRINT '  - UnassignedAt: DATETIME2 (Timestamp of deallocation, nullable)';
PRINT '';
PRINT 'Constraints:';
PRINT '  - FK_AssignmentHistory_AssetId: Foreign key to Assets(Id) with CASCADE DELETE';
PRINT '  - FK_AssignmentHistory_AssignedBy: Foreign key to Users(Id)';
PRINT '  - CHK_AssignmentHistory_Type: AssignmentType must be USER or LOCATION';
PRINT '';
PRINT 'Indexes:';
PRINT '  - IX_AssignmentHistory_AssetId: Index on AssetId for fast asset history lookup';
PRINT '  - IX_AssignmentHistory_AssignedTo: Index on AssignedTo for user/location queries';
PRINT '  - IX_AssignmentHistory_AssignedAt: Index on AssignedAt for chronological ordering';
PRINT '';
PRINT 'Purpose:';
PRINT '  - Maintains complete audit trail of all asset assignments';
PRINT '  - Supports both user and location assignment tracking';
PRINT '  - Enables historical reporting and compliance auditing';
PRINT '  - Provides referential integrity with cascade delete on asset removal';
PRINT '';
PRINT 'Integration:';
PRINT '  - Module 2 (Asset Management): References Assets table';
PRINT '  - Module 1 (User Management): References Users table for AssignedBy';
PRINT '  - Module 3 (Allocation Management): Primary data store for assignments';
PRINT '';
PRINT '========================================================================';
GO
