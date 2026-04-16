# SQL Server Schema Fix - URGENT

## Problem

The error shows:
```
Invalid column name 'account_locked'
```

## Root Cause

Your SQL Server database has the **OLD schema** from when you were using H2. The column names don't match what Hibernate expects:

**Database has** (snake_case from H2):
- `account_locked`
- `is_active`
- `created_at`
- etc.

**Hibernate expects** (camelCase):
- `accountLocked`
- `isActive`
- `createdAt`
- etc.

## Quick Fix - Drop and Recreate Database

### Option 1: Let Hibernate Recreate (Easiest)

1. **Drop the database**:
   ```sql
   USE master;
   GO
   DROP DATABASE ITAssetManagement;
   GO
   ```

2. **Update application-dev.properties** to create-drop:
   ```properties
   spring.jpa.hibernate.ddl-auto=create-drop
   ```

3. **Restart backend**:
   ```bash
   cd backend
   mvn spring-boot:run -Dspring-boot.run.profiles=dev
   ```

4. **After first successful start, change back to update**:
   ```properties
   spring.jpa.hibernate.ddl-auto=update
   ```

### Option 2: Manual SQL Script

Run this SQL to drop and recreate:

```sql
USE master;
GO

-- Drop database if exists
IF EXISTS (SELECT name FROM sys.databases WHERE name = 'ITAssetManagement')
BEGIN
    ALTER DATABASE ITAssetManagement SET SINGLE_USER WITH ROLLBACK IMMEDIATE;
    DROP DATABASE ITAssetManagement;
END
GO

-- Create fresh database
CREATE DATABASE ITAssetManagement;
GO

USE ITAssetManagement;
GO
```

Then restart the backend - Hibernate will create the correct schema.

### Option 3: Keep Data (If you have important data)

If you have data you want to keep, you need to rename columns:

```sql
USE ITAssetManagement;
GO

-- Rename columns in Users table
EXEC sp_rename 'Users.account_locked', 'accountLocked', 'COLUMN';
EXEC sp_rename 'Users.is_active', 'isActive', 'COLUMN';
EXEC sp_rename 'Users.created_at', 'createdAt', 'COLUMN';
EXEC sp_rename 'Users.updated_at', 'updatedAt', 'COLUMN';
EXEC sp_rename 'Users.created_by', 'createdBy', 'COLUMN';
EXEC sp_rename 'Users.updated_by', 'updatedBy', 'COLUMN';
EXEC sp_rename 'Users.last_login_at', 'lastLoginAt', 'COLUMN';
EXEC sp_rename 'Users.lock_until', 'lockUntil', 'COLUMN';
EXEC sp_rename 'Users.failed_login_attempts', 'failedLoginAttempts', 'COLUMN';
EXEC sp_rename 'Users.password_hash', 'passwordHash', 'COLUMN';

-- Repeat for other tables...
```

## Recommended Solution

**Just drop and recreate** - you don't have important data yet (only test users that will be re-seeded automatically).

## Steps to Fix NOW

1. **Open SQL Server Management Studio or Azure Data Studio**

2. **Run this**:
   ```sql
   USE master;
   GO
   DROP DATABASE ITAssetManagement;
   GO
   ```

3. **Stop backend** (Ctrl+C)

4. **Start backend**:
   ```bash
   cd backend
   mvn spring-boot:run -Dspring-boot.run.profiles=dev
   ```

5. **Watch logs** - should see:
   ```
   Hibernate: create table Users (...)
   DataInitializer: Created user: admin
   DataInitializer: Created user: manager
   DataInitializer: Created user: viewer
   ```

6. **Test login** - should work now!

## Why This Happened

When you switched from H2 to SQL Server, the database already existed with H2's schema (snake_case column names). Hibernate's `ddl-auto=update` doesn't rename columns, it only adds/modifies them.

## Prevention

Always use `create-drop` or `create` when switching databases for the first time, then change to `update` after the schema is correct.

## Verification

After fix, login should work and you should see proper SQL queries in logs without column name errors.
