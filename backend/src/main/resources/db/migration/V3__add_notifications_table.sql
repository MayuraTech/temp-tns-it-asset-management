-- V3__add_notifications_table.sql
-- Adds Notifications table for ticket status change notifications
-- Supports Module 4: Ticket Management notification requirements

USE ITAssetManagement;
GO

-- ============================================================================
-- Notifications Table
-- ============================================================================

-- Notifications Table
-- Stores system-generated notifications for ticket status changes
CREATE TABLE Notifications (
    Id UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
    UserId UNIQUEIDENTIFIER NOT NULL,
    TicketId UNIQUEIDENTIFIER NOT NULL,
    NotificationType NVARCHAR(50) NOT NULL,
    Message NVARCHAR(MAX) NOT NULL,
    IsRead BIT NOT NULL DEFAULT 0,
    CreatedAt DATETIME2 NOT NULL DEFAULT GETUTCDATE(),
    
    CONSTRAINT FK_Notifications_UserId FOREIGN KEY (UserId) REFERENCES Users(Id) ON DELETE CASCADE,
    CONSTRAINT FK_Notifications_TicketId FOREIGN KEY (TicketId) REFERENCES Tickets(Id) ON DELETE CASCADE,
    CONSTRAINT CHK_Notifications_Type CHECK (NotificationType IN ('TICKET_APPROVED', 'TICKET_REJECTED', 'TICKET_COMPLETED', 'TICKET_CANCELLED', 'TICKET_STATUS_CHANGE'))
);

-- Create indexes for Notifications table
CREATE INDEX IX_Notifications_UserId ON Notifications(UserId);
CREATE INDEX IX_Notifications_TicketId ON Notifications(TicketId);
CREATE INDEX IX_Notifications_IsRead ON Notifications(IsRead);
CREATE INDEX IX_Notifications_CreatedAt ON Notifications(CreatedAt);
CREATE INDEX IX_Notifications_UserId_IsRead ON Notifications(UserId, IsRead);

PRINT 'Notifications table created successfully.';
GO

-- ============================================================================
-- Summary
-- ============================================================================

PRINT '';
PRINT '========================================================================';
PRINT 'Database Schema Migration V3 Completed Successfully';
PRINT '========================================================================';
PRINT '';
PRINT 'Tables Created:';
PRINT '  - Notifications (with indexes on UserId, TicketId, IsRead, CreatedAt)';
PRINT '';
PRINT 'Features Added:';
PRINT '  - Ticket status change notifications';
PRINT '  - User notification tracking';
PRINT '  - Read/unread status management';
PRINT '  - Foreign key constraints to Users and Tickets tables';
PRINT '';
PRINT 'Indexes Created:';
PRINT '  - IX_Notifications_UserId: Fast lookup by user';
PRINT '  - IX_Notifications_TicketId: Fast lookup by ticket';
PRINT '  - IX_Notifications_IsRead: Filter unread notifications';
PRINT '  - IX_Notifications_CreatedAt: Sort by creation date';
PRINT '  - IX_Notifications_UserId_IsRead: Composite index for user unread count';
PRINT '';
PRINT '========================================================================';
GO
