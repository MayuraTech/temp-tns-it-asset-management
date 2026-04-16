# Schema Comparison - Current vs Expected

## Visual Comparison

### Current Database Schema (WRONG - from H2)

```sql
CREATE TABLE Users (
    Id uniqueidentifier PRIMARY KEY,
    Username nvarchar(100) NOT NULL,
    password_hash nvarchar(255) NOT NULL,      -- ❌ snake_case
    Email nvarchar(255) NOT NULL,
    is_active bit NOT NULL,                    -- ❌ snake_case
    account_locked bit NOT NULL,               -- ❌ snake_case
    lock_until datetime2,                      -- ❌ snake_case
    failed_login_attempts int NOT NULL,        -- ❌ snake_case
    last_login_at datetime2,                   -- ❌ snake_case
    created_at datetime2 NOT NULL,             -- ❌ snake_case
    updated_at datetime2 NOT NULL,             -- ❌ snake_case
    created_by uniqueidentifier,               -- ❌ snake_case
    updated_by uniqueidentifier                -- ❌ snake_case
);
```

### Expected Database Schema (CORRECT - from Hibernate)

```sql
CREATE TABLE Users (
    Id uniqueidentifier PRIMARY KEY,
    Username nvarchar(100) NOT NULL,
    PasswordHash nvarchar(255) NOT NULL,       -- ✅ camelCase
    Email nvarchar(255) NOT NULL,
    IsActive bit NOT NULL,                     -- ✅ camelCase
    AccountLocked bit NOT NULL,                -- ✅ camelCase
    LockUntil datetime2,                       -- ✅ camelCase
    FailedLoginAttempts int NOT NULL,          -- ✅ camelCase
    LastLoginAt datetime2,                     -- ✅ camelCase
    CreatedAt datetime2 NOT NULL,              -- ✅ camelCase
    UpdatedAt datetime2 NOT NULL,              -- ✅ camelCase
    CreatedBy uniqueidentifier,                -- ✅ camelCase
    UpdatedBy uniqueidentifier                 -- ✅ camelCase
);
```

## Column Name Mapping

| Current (Wrong) | Expected (Correct) | Status |
|----------------|-------------------|--------|
| password_hash | PasswordHash | ❌ Mismatch |
| is_active | IsActive | ❌ Mismatch |
| account_locked | AccountLocked | ❌ Mismatch |
| lock_until | LockUntil | ❌ Mismatch |
| failed_login_attempts | FailedLoginAttempts | ❌ Mismatch |
| last_login_at | LastLoginAt | ❌ Mismatch |
| created_at | CreatedAt | ❌ Mismatch |
| updated_at | UpdatedAt | ❌ Mismatch |
| created_by | CreatedBy | ❌ Mismatch |
| updated_by | UpdatedBy | ❌ Mismatch |

## What Hibernate Generates vs What Database Has

### Hibernate's SQL Query (What it tries to execute)

```sql
SELECT 
    u.Id,
    u.Username,
    u.PasswordHash,        -- Looking for "PasswordHash"
    u.Email,
    u.IsActive,            -- Looking for "IsActive"
    u.AccountLocked,       -- Looking for "AccountLocked"
    u.LockUntil,
    u.FailedLoginAttempts,
    u.LastLoginAt,
    u.CreatedAt,
    u.UpdatedAt
FROM Users u
WHERE u.Username = ?
```

### SQL Server's Response

```
Msg 207, Level 16, State 1
Invalid column name 'AccountLocked'.
Invalid column name 'IsActive'.
Invalid column name 'PasswordHash'.
Invalid column name 'LockUntil'.
Invalid column name 'FailedLoginAttempts'.
Invalid column name 'LastLoginAt'.
Invalid column name 'CreatedAt'.
Invalid column name 'UpdatedAt'.
```

Because the actual columns are:
- `account_locked` (not `AccountLocked`)
- `is_active` (not `IsActive`)
- `password_hash` (not `PasswordHash`)
- etc.

## How to Check Your Current Schema

Run this in SQL Server:

```sql
USE IT_Asset;
GO

SELECT 
    COLUMN_NAME,
    DATA_TYPE,
    IS_NULLABLE,
    CHARACTER_MAXIMUM_LENGTH
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_NAME = 'Users'
ORDER BY ORDINAL_POSITION;
```

### If You See This (WRONG):

```
COLUMN_NAME              DATA_TYPE    IS_NULLABLE
-------------------------------------------------
Id                       uniqueidentifier  NO
Username                 nvarchar          NO
password_hash            nvarchar          NO      ❌
Email                    nvarchar          NO
is_active                bit               NO      ❌
account_locked           bit               NO      ❌
lock_until               datetime2         YES     ❌
failed_login_attempts    int               NO      ❌
last_login_at            datetime2         YES     ❌
created_at               datetime2         NO      ❌
updated_at               datetime2         NO      ❌
created_by               uniqueidentifier  YES     ❌
updated_by               uniqueidentifier  YES     ❌
```

### You Should See This (CORRECT):

```
COLUMN_NAME              DATA_TYPE    IS_NULLABLE
-------------------------------------------------
Id                       uniqueidentifier  NO
Username                 nvarchar          NO
PasswordHash             nvarchar          NO      ✅
Email                    nvarchar          NO
IsActive                 bit               NO      ✅
AccountLocked            bit               NO      ✅
LockUntil                datetime2         YES     ✅
FailedLoginAttempts      int               NO      ✅
LastLoginAt              datetime2         YES     ✅
CreatedAt                datetime2         NO      ✅
UpdatedAt                datetime2         NO      ✅
CreatedBy                uniqueidentifier  YES     ✅
UpdatedBy                uniqueidentifier  YES     ✅
```

## The Fix

Drop and recreate the database so Hibernate can create the correct schema:

```sql
USE master;
GO

ALTER DATABASE IT_Asset SET SINGLE_USER WITH ROLLBACK IMMEDIATE;
DROP DATABASE IT_Asset;
GO

CREATE DATABASE IT_Asset;
GO
```

Then restart backend - Hibernate will create the correct schema automatically.

## Why Hibernate Uses camelCase

From `User.java`:

```java
@Column(name = "AccountLocked", nullable = false)
private Boolean accountLocked = false;

@Column(name = "IsActive", nullable = false)
private Boolean isActive = true;

@Column(name = "CreatedAt", nullable = false, updatable = false)
private LocalDateTime createdAt;
```

The `@Column(name = "...")` annotation explicitly specifies the column name in the database. This is the correct approach for SQL Server which is case-sensitive in column names.

## After Fix Verification

After dropping/recreating database and restarting backend, verify:

```sql
-- Should return 3 rows
SELECT Username, Email, IsActive, AccountLocked 
FROM Users;

-- Should show:
-- admin    | admin@example.com    | 1 | 0
-- manager  | manager@example.com  | 1 | 0
-- viewer   | viewer@example.com   | 1 | 0
```

And login should work without errors!
