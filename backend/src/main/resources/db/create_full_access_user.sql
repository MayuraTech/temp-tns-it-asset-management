-- Full-access user (Administrator). Run against IT_Asset (or set DB in your client).
-- Login: fullaccess / FullAccess@123
-- BCrypt (cost 10), compatible with Spring Security BCryptPasswordEncoder.
USE IT_Asset;

DECLARE @Username  NVARCHAR(100) = N'fullaccess';
DECLARE @Email     NVARCHAR(255) = N'fullaccess@example.com';
DECLARE @NewUserId UNIQUEIDENTIFIER = NEWID();
DECLARE @PwdHash   NVARCHAR(255) = N'$2b$10$1VQTN.hPTV8HV6e2lXgY4OKO8aq.rkPjY0PgKoiZ07gNoC2Nf3FOq';

IF EXISTS (SELECT 1 FROM dbo.Users WHERE Username = @Username)
BEGIN
    RAISERROR (N'User already exists with that Username.', 16, 1);
    RETURN;
END;

INSERT INTO dbo.Users (
    Id,
    Username,
    PasswordHash,
    Email,
    CreatedAt,
    UpdatedAt,
    IsActive,
    AccountLocked,
    FailedLoginAttempts
)
VALUES (
    @NewUserId,
    @Username,
    @PwdHash,
    @Email,
    SYSUTCDATETIME(),
    SYSUTCDATETIME(),
    1,
    0,
    0
);

INSERT INTO dbo.UserRoles (Id, UserId, Role, AssignedBy, AssignedAt)
VALUES (NEWID(), @NewUserId, N'Administrator', @NewUserId, SYSUTCDATETIME());
