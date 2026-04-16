# SQL Server Setup Guide for Development

## Overview

The application is now configured to use SQL Server for development instead of H2 in-memory database.

## Prerequisites

You need SQL Server installed and running on your local machine.

### Option 1: SQL Server Express (Free)

1. **Download SQL Server Express**:
   - Go to: https://www.microsoft.com/en-us/sql-server/sql-server-downloads
   - Download "Express" edition (free)

2. **Install SQL Server Express**:
   - Run the installer
   - Choose "Basic" installation
   - Accept defaults
   - Note the instance name (usually `SQLEXPRESS`)

3. **Enable TCP/IP** (if needed):
   - Open SQL Server Configuration Manager
   - SQL Server Network Configuration → Protocols for SQLEXPRESS
   - Enable TCP/IP
   - Restart SQL Server service

### Option 2: SQL Server Developer Edition (Free)

1. **Download SQL Server Developer**:
   - Go to: https://www.microsoft.com/en-us/sql-server/sql-server-downloads
   - Download "Developer" edition (free, full-featured)

2. **Install with defaults**

### Option 3: Docker (Easiest)

```bash
docker run -e "ACCEPT_EULA=Y" -e "SA_PASSWORD=YourStrong@Passw0rd" \
   -p 1433:1433 --name sql-server \
   -d mcr.microsoft.com/mssql/server:2019-latest
```

## Database Configuration

### Current Configuration

**File**: `backend/src/main/resources/application-dev.properties`

```properties
spring.datasource.url=jdbc:sqlserver://localhost:1433;databaseName=ITAssetManagement;encrypt=true;trustServerCertificate=true
spring.datasource.driver-class-name=com.microsoft.sqlserver.jdbc.SQLServerDriver
spring.datasource.username=sa
spring.datasource.password=YourStrong@Passw0rd
```

### Update Password

**IMPORTANT**: Change the password to match your SQL Server SA password!

1. Open `backend/src/main/resources/application-dev.properties`
2. Update this line:
   ```properties
   spring.datasource.password=YourStrong@Passw0rd
   ```
3. Replace `YourStrong@Passw0rd` with your actual SA password

### For Named Instances

If you're using SQL Server Express with a named instance (e.g., `SQLEXPRESS`):

```properties
spring.datasource.url=jdbc:sqlserver://localhost:1433\\SQLEXPRESS;databaseName=ITAssetManagement;encrypt=true;trustServerCertificate=true
```

Or use dynamic port:
```properties
spring.datasource.url=jdbc:sqlserver://localhost;instanceName=SQLEXPRESS;databaseName=ITAssetManagement;encrypt=true;trustServerCertificate=true
```

## Create Database

### Option 1: Automatic (Recommended)

The application will automatically create the database on first run with:
```properties
spring.jpa.hibernate.ddl-auto=update
```

### Option 2: Manual

Using SQL Server Management Studio (SSMS) or Azure Data Studio:

```sql
CREATE DATABASE ITAssetManagement;
GO

USE ITAssetManagement;
GO
```

## Test Connection

### Using SQL Server Management Studio (SSMS)

1. Open SSMS
2. Connect to: `localhost` or `localhost\SQLEXPRESS`
3. Login: `sa` / `YourStrong@Passw0rd`
4. Verify connection

### Using Azure Data Studio

1. Open Azure Data Studio
2. New Connection
3. Server: `localhost` or `localhost,1433`
4. Authentication: SQL Login
5. Username: `sa`
6. Password: `YourStrong@Passw0rd`
7. Test connection

### Using Command Line (sqlcmd)

```bash
sqlcmd -S localhost -U sa -P YourStrong@Passw0rd -Q "SELECT @@VERSION"
```

## Start Application with SQL Server

### 1. Ensure SQL Server is Running

**Windows Services**:
- Press `Win + R`
- Type `services.msc`
- Find "SQL Server (MSSQLSERVER)" or "SQL Server (SQLEXPRESS)"
- Ensure it's "Running"

**Docker**:
```bash
docker start sql-server
```

### 2. Start Backend

```bash
cd backend
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

### 3. Verify Connection

Look for these log messages:
```
HikariPool-1 - Starting...
HikariPool-1 - Start completed.
Hibernate: create table Users (...)
```

If you see connection errors, check:
- SQL Server is running
- Password is correct
- Port 1433 is accessible
- Firewall allows connections

## Data Seeding

The `DataInitializer` will automatically create three users on first startup:

- **admin** / `Admin@123456` (ADMINISTRATOR)
- **manager** / `Manager@123456` (ASSET_MANAGER)
- **viewer** / `Viewer@123456` (VIEWER)

These users are created only if they don't already exist (idempotent).

## Verify Data in SQL Server

### Using SSMS or Azure Data Studio

```sql
USE ITAssetManagement;
GO

-- Check users
SELECT * FROM Users;

-- Check user roles
SELECT * FROM UserRoles;

-- Check sessions
SELECT * FROM Sessions;
```

### Expected Results

You should see 3 users with their respective roles.

## Troubleshooting

### Error: "Login failed for user 'sa'"

**Solution**: Update password in `application-dev.properties`

### Error: "Cannot open database"

**Solution**: 
1. Database doesn't exist - let Hibernate create it automatically
2. Or create manually: `CREATE DATABASE ITAssetManagement;`

### Error: "Connection refused"

**Solution**:
1. Check SQL Server is running
2. Check port 1433 is open
3. Enable TCP/IP in SQL Server Configuration Manager
4. Restart SQL Server service

### Error: "The driver could not establish a secure connection"

**Solution**: Already handled with `trustServerCertificate=true` in connection string

### Error: "This driver is not configured for integrated authentication"

**Solution**: Using SQL authentication (sa user), not Windows authentication

## Connection String Options

### Standard Connection
```properties
spring.datasource.url=jdbc:sqlserver://localhost:1433;databaseName=ITAssetManagement;encrypt=true;trustServerCertificate=true
```

### Named Instance
```properties
spring.datasource.url=jdbc:sqlserver://localhost;instanceName=SQLEXPRESS;databaseName=ITAssetManagement;encrypt=true;trustServerCertificate=true
```

### Windows Authentication (if preferred)
```properties
spring.datasource.url=jdbc:sqlserver://localhost:1433;databaseName=ITAssetManagement;integratedSecurity=true;encrypt=true;trustServerCertificate=true
# Remove username and password properties
```

## Benefits of SQL Server for Development

✅ **Persistent Data**: Data survives backend restarts
✅ **Production-like**: Same database as production
✅ **Better Testing**: Test with real SQL Server features
✅ **Data Inspection**: Use SSMS/Azure Data Studio to inspect data
✅ **Performance Testing**: Test with realistic database performance

## Switching Back to H2 (if needed)

If you want to switch back to H2 temporarily:

1. Create `application-h2.properties` with H2 configuration
2. Run with: `mvn spring-boot:run -Dspring-boot.run.profiles=h2`

Or just update `application-dev.properties` back to H2 configuration.

## Next Steps

1. ✅ Install SQL Server (Express, Developer, or Docker)
2. ✅ Update password in `application-dev.properties`
3. ✅ Start SQL Server
4. ✅ Start backend: `mvn spring-boot:run -Dspring-boot.run.profiles=dev`
5. ✅ Verify users are created
6. ✅ Test login with admin/Admin@123456

Your application is now using SQL Server for development!
