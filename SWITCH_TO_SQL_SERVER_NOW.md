# Quick Switch to SQL Server

## What Changed

I've updated `application-dev.properties` to use SQL Server instead of H2.

## Before You Start

**Do you have SQL Server installed?**

### Quick Check (Windows)
```bash
# Check if SQL Server is running
sc query MSSQLSERVER
# OR for Express edition
sc query "MSSQL$SQLEXPRESS"
```

### If Not Installed - Quick Docker Option
```bash
docker run -e "ACCEPT_EULA=Y" -e "SA_PASSWORD=YourStrong@Passw0rd" \
   -p 1433:1433 --name sql-server \
   -d mcr.microsoft.com/mssql/server:2019-latest
```

## Configuration Update Required

**IMPORTANT**: Update the password in `application-dev.properties`

1. Open: `backend/src/main/resources/application-dev.properties`
2. Find this line:
   ```properties
   spring.datasource.password=YourStrong@Passw0rd
   ```
3. Replace with YOUR SQL Server SA password

## Start Backend with SQL Server

```bash
cd backend
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

## What to Expect

### On First Startup

You'll see logs like:
```
HikariPool-1 - Starting...
HikariPool-1 - Start completed.
Hibernate: create table Users (...)
Hibernate: create table UserRoles (...)
Hibernate: create table Sessions (...)
```

Then:
```
DataInitializer: Seeding database with default users...
DataInitializer: Created user: admin
DataInitializer: Created user: manager
DataInitializer: Created user: viewer
```

### Success Indicators

✅ No connection errors
✅ Tables created automatically
✅ Three users seeded
✅ Application starts successfully

## Verify in SQL Server

### Using SSMS or Azure Data Studio

```sql
USE ITAssetManagement;

-- Check users
SELECT username, email, is_active FROM Users;

-- Should see:
-- admin    | admin@example.com    | 1
-- manager  | manager@example.com  | 1
-- viewer   | viewer@example.com   | 1
```

## Test Login

1. Start frontend: `npm start` (in frontend directory)
2. Go to: `http://localhost:4200/login`
3. Login: `admin` / `Admin@123456`
4. Should work!

## Troubleshooting

### "Login failed for user 'sa'"
→ Update password in `application-dev.properties`

### "Cannot open database 'ITAssetManagement'"
→ Database will be created automatically on first run
→ Or create manually: `CREATE DATABASE ITAssetManagement;`

### "Connection refused" or "Cannot connect"
→ SQL Server not running
→ Start SQL Server service or Docker container

### "The driver could not establish a secure connection"
→ Already handled with `trustServerCertificate=true`

## Connection String Reference

**Current configuration**:
```properties
spring.datasource.url=jdbc:sqlserver://localhost:1433;databaseName=ITAssetManagement;encrypt=true;trustServerCertificate=true
spring.datasource.username=sa
spring.datasource.password=YourStrong@Passw0rd
```

**For SQL Server Express** (named instance):
```properties
spring.datasource.url=jdbc:sqlserver://localhost;instanceName=SQLEXPRESS;databaseName=ITAssetManagement;encrypt=true;trustServerCertificate=true
```

## Benefits

✅ **Persistent data** - Survives backend restarts
✅ **Production-like** - Same database as production
✅ **Inspect data** - Use SSMS/Azure Data Studio
✅ **Better testing** - Test with real SQL Server

## Quick Commands

```bash
# Start SQL Server (Docker)
docker start sql-server

# Start Backend
cd backend
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# Check SQL Server is running (Windows)
sc query MSSQLSERVER

# Connect with sqlcmd
sqlcmd -S localhost -U sa -P YourStrong@Passw0rd -Q "SELECT @@VERSION"
```

## Need Help?

See `SQL_SERVER_SETUP_GUIDE.md` for detailed setup instructions.

## Still Want H2?

If you want to switch back to H2, I can revert the changes. Just let me know!
