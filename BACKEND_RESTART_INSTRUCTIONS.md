# Backend Restart Instructions - SQL Server Schema Fix

## Current Problem

Your SQL Server database has the **wrong schema** from when you were using H2. The column names don't match what Hibernate expects:

**Database has** (snake_case):
```
account_locked, is_active, created_at, updated_at, etc.
```

**Hibernate expects** (camelCase):
```
accountLocked, isActive, createdAt, updatedAt, etc.
```

This causes the error: `Invalid column name 'account_locked'`

## Quick Fix (5 minutes)

### Step 1: Drop and Recreate Database

**Option A: Using SQL Script (Recommended)**

1. Open **SQL Server Management Studio** or **Azure Data Studio**
2. Connect to: `TNS-IT-DESKTOP\SQLEXPRESS`
3. Open the file: `FIX_SQL_SERVER_SCHEMA_NOW.sql`
4. Execute the script (F5 or click Execute)
5. You should see: "Database dropped successfully" and "IT_Asset database created successfully"

**Option B: Manual SQL Commands**

Run these commands in SQL Server:

```sql
USE master;
GO

-- Drop database
ALTER DATABASE IT_Asset SET SINGLE_USER WITH ROLLBACK IMMEDIATE;
DROP DATABASE IT_Asset;
GO

-- Create fresh database
CREATE DATABASE IT_Asset;
GO
```

### Step 2: Restart Backend

1. **Stop backend** if running (Ctrl+C in terminal)

2. **Start backend**:
   ```bash
   cd backend
   mvn spring-boot:run -Dspring-boot.run.profiles=dev
   ```

3. **Watch the logs** - you should see:
   ```
   Hibernate: create table Users (
       Id uniqueidentifier not null,
       Username nvarchar(100) not null,
       PasswordHash nvarchar(255) not null,
       Email nvarchar(255) not null,
       IsActive bit not null,
       AccountLocked bit not null,
       ...
   )
   
   DataInitializer: Checking if default users exist...
   DataInitializer: Created user: admin
   DataInitializer: Created user: manager
   DataInitializer: Created user: viewer
   DataInitializer: Default users initialized successfully
   ```

4. **Verify startup** - look for:
   ```
   Started ItAssetManagementApplication in X.XXX seconds
   ```

### Step 3: Test Login

1. Open browser: http://localhost:4200
2. Login with: **admin** / **Admin@123456**
3. Should successfully login and see dashboard

## What This Does

1. **Drops old database** with wrong schema (snake_case columns)
2. **Creates empty database** 
3. **Hibernate creates correct schema** with camelCase columns on startup
4. **DataInitializer seeds users** automatically (admin, manager, viewer)

## Why This Happened

When you switched from H2 to SQL Server, the database already existed with H2's schema. Hibernate's `ddl-auto=update` doesn't rename columns - it only adds/modifies them.

## Verification

After restart, check the Users table schema:

```sql
USE IT_Asset;
GO

-- Should show camelCase column names
SELECT COLUMN_NAME, DATA_TYPE 
FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_NAME = 'Users'
ORDER BY ORDINAL_POSITION;
```

Expected columns:
- Id
- Username
- PasswordHash
- Email
- IsActive
- AccountLocked
- LockUntil
- FailedLoginAttempts
- LastLoginAt
- CreatedAt
- UpdatedAt
- CreatedBy
- UpdatedBy

## Troubleshooting

### If backend fails to start:

1. Check SQL Server is running
2. Verify connection string in `application-dev.properties`
3. Verify username/password: `itassetuser` / `its@2345`
4. Check logs for specific error

### If login still fails:

1. Clear browser storage (F12 > Application > Clear storage)
2. Check backend logs for JWT errors
3. Verify users were created (check logs for "DataInitializer: Created user")

### If you see "database already exists" error:

The database wasn't dropped properly. Run:
```sql
USE master;
GO
ALTER DATABASE IT_Asset SET SINGLE_USER WITH ROLLBACK IMMEDIATE;
DROP DATABASE IT_Asset;
GO
```

## Need Help?

Check these files:
- `SQL_SERVER_SCHEMA_FIX.md` - Detailed explanation
- `QUICK_START_GUIDE.md` - Full setup guide
- Backend logs - Look for Hibernate DDL statements and DataInitializer messages
