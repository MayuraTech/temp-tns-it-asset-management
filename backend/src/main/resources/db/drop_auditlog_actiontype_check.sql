-- Drops legacy CHK_AuditLog_ActionType so enum action names can be stored.
-- Select database IT_Asset in your client if USE is not supported.
USE IT_Asset;

IF EXISTS (
    SELECT 1 FROM sys.check_constraints
    WHERE name = N'CHK_AuditLog_ActionType'
      AND parent_object_id = OBJECT_ID(N'dbo.AuditLog', N'U')
)
BEGIN
    ALTER TABLE dbo.AuditLog DROP CONSTRAINT CHK_AuditLog_ActionType;
END;
