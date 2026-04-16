# Final Fix Instructions - Foreign Key Constraint Issue

## The Real Problem

Your database has a **mixed schema**:
- ✅ Column names are correct (camelCase): `UpdatedBy`, `CreatedBy`, `AccountLocked`, etc.
- ❌ Foreign key constraints reference OLD names (snake_case): `updated_by`, `created_by`, etc.

This happened because the columns were renamed but the foreign key constraints weren't updated.

## Error You're Seeing

```
Foreign key 'FKci7xr690rvyv3bnfappbyh8x0' references invalid column 'updated_by' in referencing table 'users'.
```

## The Fix (3 Steps - 5 Minutes)

### Step 1: Drop Foreign Key Constraints

Open **SQL Server Management Studio** or **Azure Data Studio** and run:

```sql
USE IT_Asset;
GO

-- Drop all foreign key constraints
DECLARE @sql NVARCHAR(MAX) = '';

SELECT @sql += 'ALTER TABLE ' + QUOTENAME(OBJECT_SCHEMA_NAME(parent_object_id)) + '.' + 
               QUOTENAME(OBJECT_NAME(parent_object_id)) + 
               ' DROP CONSTRAINT ' + QUOTENAME(name) + ';' + CHAR(13)
FROM sys.foreign_keys
WHERE referenced_object_id = OBJECT_ID('Users') 
   OR parent_object_id = OBJECT_ID('Users');

EXEC sp_executesql @sql;
GO
```

**OR** simply execute: `FIX_FOREIGN_KEY_CONSTRAINTS.sql`

### Step 2: Kill Any Running Backend Process

```powershell
# Find process on port 8080
netstat -ano | findstr :8080

# Kill it (replace PID with actual process ID)
taskkill /PID <PID> /F
```

### Step 3: Start Backend

```bash
cd backend
mvn spring-boot:run "-Dspring-boot.run.profiles=dev"
```

Hibernate will automatically recreate the foreign key constraints with the correct column names!

## What This Does

1. ✅ Drops all foreign key constraints that reference old column names
2. ✅ Hibernate recreates constraints with correct column names on startup
3. ✅ DataInitializer seeds default users
4. ✅ Application starts successfully

## Expected Backend Logs After Fix

```
Hibernate: alter table sessions 
    add constraint FKhwt0sg4wx88xx4y5m0p4y9ilx 
    foreign key (user_id) 
    references Users (Id)

Hibernate: alter table user_roles 
    add constraint FKhfh9dx7w3ubf1co1vdev94g3f 
    foreign key (user_id) 
    references Users (Id)

Hibernate: alter table Users 
    add constraint FKn8pl63y4abe7n0ls6topbqjh2 
    foreign key (CreatedBy) 
    references Users (Id)

Hibernate: alter table Users 
    add constraint FKsqnxvdcpxk8b1odxknfg6jvpg 
    foreign key (UpdatedBy) 
    references Users (Id)

DataInitializer: Checking if default users exist...
DataInitializer: Created user: admin
DataInitializer: Created user: manager
DataInitializer: Created user: viewer

Started ItAssetManagementApplication in 8.234 seconds
```

Notice the foreign keys now reference `CreatedBy` and `UpdatedBy` (camelCase) instead of `created_by` and `updated_by` (snake_case)!

## Verification

After backend starts successfully:

### 1. Check Foreign Key Constraints

```sql
USE IT_Asset;
GO

SELECT 
    fk.name AS ForeignKeyName,
    OBJECT_NAME(fk.parent_object_id) AS TableName,
    COL_NAME(fkc.parent_object_id, fkc.parent_column_id) AS ColumnName,
    OBJECT_NAME(fk.referenced_object_id) AS ReferencedTable,
    COL_NAME(fkc.referenced_object_id, fkc.referenced_column_id) AS ReferencedColumn
FROM sys.foreign_keys AS fk
INNER JOIN sys.foreign_key_columns AS fkc 
    ON fk.object_id = fkc.constraint_object_id
WHERE fk.referenced_object_id = OBJECT_ID('Users')
   OR fk.parent_object_id = OBJECT_ID('Users')
ORDER BY TableName, ForeignKeyName;
```

Should show:
- `Users.CreatedBy` → `Users.Id` ✅
- `Users.UpdatedBy` → `Users.Id` ✅
- `UserRoles.UserId` → `Users.Id` ✅
- `Sessions.UserId` → `Users.Id` ✅

### 2. Test Login

1. Open: http://localhost:4200
2. Login: **admin** / **Admin@123456**
3. Should work without errors!

## Why This Happened

1. You started with H2 database (snake_case columns)
2. Switched to SQL Server
3. Columns were renamed to camelCase (manually or via Hibernate)
4. But foreign key constraints still referenced old snake_case names
5. Result: Mixed schema causing foreign key errors

## Alternative: Complete Database Recreate

If the above doesn't work, you can drop and recreate the entire database:

```sql
USE master;
GO

ALTER DATABASE IT_Asset SET SINGLE_USER WITH ROLLBACK IMMEDIATE;
DROP DATABASE IT_Asset;
GO

CREATE DATABASE IT_Asset;
GO
```

Then restart backend - Hibernate will create everything from scratch with correct schema.

## Files Created

- `FIX_FOREIGN_KEY_CONSTRAINTS.sql` - SQL script to drop foreign keys
- `FINAL_FIX_INSTRUCTIONS.md` - This file
- `CURRENT_STATUS_AND_FIX.md` - Detailed explanation
- `SCHEMA_COMPARISON.md` - Visual schema comparison

## Current Configuration

From `application-dev.properties`:
- Server: `TNS-IT-DESKTOP\SQLEXPRESS`
- Database: `IT_Asset`
- Username: `itassetuser`
- Password: `its@2345`

## Troubleshooting

### If foreign key drop fails:

```sql
-- Check what foreign keys exist
SELECT name FROM sys.foreign_keys 
WHERE referenced_object_id = OBJECT_ID('Users') 
   OR parent_object_id = OBJECT_ID('Users');

-- Drop them manually
ALTER TABLE Users DROP CONSTRAINT [constraint_name];
ALTER TABLE UserRoles DROP CONSTRAINT [constraint_name];
ALTER TABLE Sessions DROP CONSTRAINT [constraint_name];
```

### If port 8080 is still in use:

```powershell
netstat -ano | findstr :8080
taskkill /PID <PID> /F
```

### If backend still fails:

Check the logs for specific errors and share them for further diagnosis.

---

**TL;DR**: Run `FIX_FOREIGN_KEY_CONSTRAINTS.sql`, kill port 8080, restart backend. Done!
