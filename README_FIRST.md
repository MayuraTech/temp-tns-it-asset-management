# 🚨 READ THIS FIRST - SQL Server Schema Fix Required

## Current Issue

Your backend is failing with this error:
```
Invalid column name 'account_locked'
```

## Why This Happens

Your SQL Server database has the **wrong schema** from when you were using H2. The column names don't match what Hibernate expects.

## Quick Fix (2 Minutes)

### Step 1: Drop and Recreate Database

Open **SQL Server Management Studio** or **Azure Data Studio** and run:

```sql
USE master;
GO

ALTER DATABASE IT_Asset SET SINGLE_USER WITH ROLLBACK IMMEDIATE;
DROP DATABASE IT_Asset;
GO

CREATE DATABASE IT_Asset;
GO
```

**OR** simply execute the provided script: `FIX_SQL_SERVER_SCHEMA_NOW.sql`

### Step 2: Restart Backend

```bash
cd backend
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

### Step 3: Test Login

1. Open: http://localhost:4200
2. Login: **admin** / **Admin@123456**
3. Should work now!

## What This Does

1. ✅ Drops old database with wrong schema
2. ✅ Creates empty database
3. ✅ Hibernate creates correct schema on startup
4. ✅ DataInitializer seeds default users
5. ✅ Login works without errors

## Detailed Documentation

- **CURRENT_STATUS_AND_FIX.md** - Complete explanation and fix options
- **BACKEND_RESTART_INSTRUCTIONS.md** - Step-by-step restart guide
- **SCHEMA_COMPARISON.md** - Visual comparison of schemas
- **SQL_SERVER_SCHEMA_FIX.md** - Detailed technical explanation
- **QUICK_START_GUIDE.md** - Full setup guide

## Expected Backend Logs After Fix

```
Hibernate: create table Users (
    Id uniqueidentifier not null,
    Username nvarchar(100) not null,
    PasswordHash nvarchar(255) not null,
    ...
)

DataInitializer: Created user: admin
DataInitializer: Created user: manager
DataInitializer: Created user: viewer

Started ItAssetManagementApplication in 8.234 seconds
```

## Current Configuration

From `application-dev.properties`:
- Server: `TNS-IT-DESKTOP\SQLEXPRESS`
- Database: `IT_Asset`
- Username: `itassetuser`
- Password: `its@2345`

## Don't Want to Lose Data?

If you have important data (you probably don't - just test users), see `SQL_SERVER_SCHEMA_FIX.md` Option 3 for column rename script.

## Still Having Issues?

1. Verify SQL Server is running
2. Check connection details in `application-dev.properties`
3. Look at backend logs for specific errors
4. Check the detailed documentation files listed above

---

**TL;DR**: Run `FIX_SQL_SERVER_SCHEMA_NOW.sql`, restart backend, login works. That's it!
