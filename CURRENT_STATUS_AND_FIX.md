# Current Status and Fix - SQL Server Schema Mismatch

## Current Situation

✅ **Backend code is correct** - All entities use camelCase column names
✅ **SQL Server is connected** - Connection to `TNS-IT-DESKTOP\SQLEXPRESS` works
✅ **Database exists** - `IT_Asset` database exists
❌ **Schema is wrong** - Database has old H2 schema with snake_case columns

## The Problem

When you switched from H2 to SQL Server, the `IT_Asset` database already existed with the **wrong schema**:

```
Current Database Schema (WRONG):
- account_locked  ❌
- is_active       ❌
- created_at      ❌
- updated_at      ❌
- etc.

Expected Schema (CORRECT):
- AccountLocked   ✅
- IsActive        ✅
- CreatedAt       ✅
- UpdatedAt       ✅
- etc.
```

## Error You're Seeing

```
Invalid column name 'account_locked'
```

This happens because:
1. Hibernate generates SQL with camelCase: `SELECT ... accountLocked ...`
2. SQL Server looks for column `accountLocked`
3. But the column is actually named `account_locked` (from H2)
4. SQL Server throws error: "Invalid column name"

## The Fix (Choose One)

### Option 1: SQL Script (Easiest - 2 minutes)

1. Open **SQL Server Management Studio** or **Azure Data Studio**
2. Connect to: `TNS-IT-DESKTOP\SQLEXPRESS`
3. Open file: `FIX_SQL_SERVER_SCHEMA_NOW.sql`
4. Execute (F5)
5. Stop backend (Ctrl+C)
6. Start backend: `mvn spring-boot:run -Dspring-boot.run.profiles=dev`
7. Test login: admin/Admin@123456

### Option 2: Manual Commands (3 minutes)

```sql
USE master;
GO

ALTER DATABASE IT_Asset SET SINGLE_USER WITH ROLLBACK IMMEDIATE;
DROP DATABASE IT_Asset;
GO

CREATE DATABASE IT_Asset;
GO
```

Then restart backend.

### Option 3: Temporary create-drop (If you want to see it happen)

1. Edit `backend/src/main/resources/application-dev.properties`
2. Change: `spring.jpa.hibernate.ddl-auto=create-drop`
3. Restart backend
4. Watch logs - Hibernate will drop and recreate all tables
5. Change back to: `spring.jpa.hibernate.ddl-auto=update`
6. Restart backend again

## What Happens After Fix

1. **Database is empty** - Old data is gone (only test users anyway)
2. **Hibernate creates correct schema** - All tables with camelCase columns
3. **DataInitializer runs** - Creates admin, manager, viewer users
4. **Login works** - No more "Invalid column name" errors

## Expected Backend Logs After Fix

```
Hibernate: drop table if exists Users cascade
Hibernate: drop table if exists UserRoles cascade
Hibernate: drop table if exists Sessions cascade

Hibernate: create table Users (
    Id uniqueidentifier not null,
    Username nvarchar(100) not null,
    PasswordHash nvarchar(255) not null,
    Email nvarchar(255) not null,
    IsActive bit not null,
    AccountLocked bit not null,
    LockUntil datetime2,
    FailedLoginAttempts int not null,
    LastLoginAt datetime2,
    CreatedAt datetime2 not null,
    UpdatedAt datetime2 not null,
    CreatedBy uniqueidentifier,
    UpdatedBy uniqueidentifier,
    primary key (Id)
)

DataInitializer: Checking if default users exist...
DataInitializer: Created user: admin
DataInitializer: Created user: manager
DataInitializer: Created user: viewer
DataInitializer: Default users initialized successfully

Started ItAssetManagementApplication in 8.234 seconds
```

## Verification Steps

After backend restarts successfully:

1. **Check database schema**:
   ```sql
   USE IT_Asset;
   GO
   
   SELECT COLUMN_NAME 
   FROM INFORMATION_SCHEMA.COLUMNS 
   WHERE TABLE_NAME = 'Users'
   ORDER BY ORDINAL_POSITION;
   ```
   
   Should show: `Id`, `Username`, `PasswordHash`, `Email`, `IsActive`, `AccountLocked`, etc.

2. **Check users were created**:
   ```sql
   SELECT Username, Email FROM Users;
   ```
   
   Should show: admin, manager, viewer

3. **Test login**:
   - Open: http://localhost:4200
   - Login: admin / Admin@123456
   - Should see dashboard

## Why This Happened

1. You started with H2 database (in-memory)
2. H2 created tables with snake_case columns
3. You switched to SQL Server
4. SQL Server database `IT_Asset` already existed with H2's schema
5. Hibernate's `ddl-auto=update` doesn't rename columns
6. Result: Schema mismatch

## Prevention for Future

When switching databases:
1. Always drop the old database first
2. Or use `ddl-auto=create-drop` for first run
3. Then switch to `ddl-auto=update` after schema is correct

## Files to Reference

- `FIX_SQL_SERVER_SCHEMA_NOW.sql` - SQL script to drop/recreate database
- `BACKEND_RESTART_INSTRUCTIONS.md` - Step-by-step restart guide
- `SQL_SERVER_SCHEMA_FIX.md` - Detailed explanation
- `QUICK_START_GUIDE.md` - Full setup guide

## Current Configuration

From `application-dev.properties`:
```properties
spring.datasource.url=jdbc:sqlserver://TNS-IT-DESKTOP\\SQLEXPRESS:1433;databaseName=IT_Asset
spring.datasource.username=itassetuser
spring.datasource.password=its@2345
spring.jpa.hibernate.ddl-auto=update
```

## Next Steps

1. ✅ Run `FIX_SQL_SERVER_SCHEMA_NOW.sql`
2. ✅ Restart backend
3. ✅ Test login
4. ✅ Continue development

That's it! The fix is simple - just drop and recreate the database.
