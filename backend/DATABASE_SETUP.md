# Database Setup Guide

This guide provides instructions for setting up the Microsoft SQL Server database for the IT Asset Management application.

## Quick Start

### Option 1: Automated Setup (Recommended)

Execute the provided setup script:

```bash
# Using sqlcmd
sqlcmd -S localhost -U sa -P YourSAPassword -i database-setup.sql

# Or using SQL Server Management Studio (SSMS)
# 1. Open SSMS and connect to your SQL Server instance
# 2. Open database-setup.sql
# 3. Execute the script (F5)
```

### Option 2: Manual Setup

If you prefer to set up the database manually or need to customize the configuration:

1. **Create the database:**
   ```sql
   CREATE DATABASE ITAssetManagement;
   ```

2. **Enable read committed snapshot isolation:**
   ```sql
   ALTER DATABASE ITAssetManagement SET READ_COMMITTED_SNAPSHOT ON;
   ```

3. **Create application user:**
   ```sql
   CREATE LOGIN ITAssetMgmtUser WITH PASSWORD = 'YourSecurePassword123!';
   USE ITAssetManagement;
   CREATE USER ITAssetMgmtUser FOR LOGIN ITAssetMgmtUser;
   ```

4. **Grant permissions:**
   ```sql
   GRANT SELECT, INSERT, UPDATE, DELETE, EXECUTE ON SCHEMA::dbo TO ITAssetMgmtUser;
   ```

## What Gets Created

### Database: ITAssetManagement

- **Purpose**: Main application database
- **Configuration**:
  - Read Committed Snapshot Isolation: ENABLED
  - Recovery Model: FULL
  - Compatibility Level: 150 (SQL Server 2019)
  - Auto Update Statistics: ENABLED
  - Auto Create Statistics: ENABLED

### User: ITAssetMgmtUser

- **Purpose**: Application database user with limited permissions
- **Permissions**:
  - ✅ SELECT (read data)
  - ✅ INSERT (create records)
  - ✅ UPDATE (modify records)
  - ✅ DELETE (remove records)
  - ✅ EXECUTE (run stored procedures)
  - ❌ DDL operations (CREATE, ALTER, DROP tables)
  - ❌ Database administration

### Read Committed Snapshot Isolation (RCSI)

**What it does:**
- Provides optimistic concurrency control
- Readers don't block writers, writers don't block readers
- Reduces lock contention and improves performance
- Maintains transaction isolation without traditional locking

**Why it's important:**
- Multiple users can read and write assets simultaneously
- Better performance for concurrent operations
- Prevents deadlocks in high-concurrency scenarios

**Trade-off:**
- Requires additional tempdb space for row versioning
- Monitor tempdb size and ensure adequate disk space

## Configuration

### Application Properties

Update `backend/src/main/resources/application.properties`:

```properties
# Database Connection
spring.datasource.url=jdbc:sqlserver://localhost:1433;databaseName=ITAssetManagement;encrypt=true;trustServerCertificate=true
spring.datasource.username=ITAssetMgmtUser
spring.datasource.password=YourSecurePassword123!
spring.datasource.driver-class-name=com.microsoft.sqlserver.jdbc.SQLServerDriver

# Connection Pool (HikariCP)
spring.datasource.hikari.maximum-pool-size=20
spring.datasource.hikari.minimum-idle=5
spring.datasource.hikari.connection-timeout=30000
spring.datasource.hikari.idle-timeout=600000
spring.datasource.hikari.max-lifetime=1800000

# JPA/Hibernate
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.show-sql=false
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.SQLServerDialect
spring.jpa.properties.hibernate.format_sql=true

# Flyway Migration
spring.flyway.enabled=true
spring.flyway.baseline-on-migrate=true
spring.flyway.locations=classpath:db/migration
spring.flyway.sql-migration-prefix=V
spring.flyway.sql-migration-separator=__
spring.flyway.sql-migration-suffixes=.sql
```

### Environment Variables (Production)

For production deployments, use environment variables instead of hardcoded credentials:

```bash
# Set environment variables
export DB_HOST=your-sql-server-host
export DB_PORT=1433
export DB_NAME=ITAssetManagement
export DB_USERNAME=ITAssetMgmtUser
export DB_PASSWORD=YourSecurePassword

# Update application.properties to use environment variables
spring.datasource.url=jdbc:sqlserver://${DB_HOST:localhost}:${DB_PORT:1433};databaseName=${DB_NAME:ITAssetManagement}
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}
```

## Security Best Practices

### 1. Change Default Password

The setup script uses a default password for demonstration. **Change it immediately:**

```sql
ALTER LOGIN ITAssetMgmtUser WITH PASSWORD = 'YourNewSecurePassword';
```

### 2. Use Strong Passwords

Ensure passwords meet these requirements:
- Minimum 16 characters
- Mix of uppercase and lowercase letters
- Numbers and special characters
- Not based on dictionary words
- Unique to this application

### 3. Secure Credential Storage

**Never commit credentials to version control!**

Options for secure storage:
- **Environment Variables**: Simple, works for most deployments
- **Azure Key Vault**: For Azure deployments
- **AWS Secrets Manager**: For AWS deployments
- **HashiCorp Vault**: For on-premises or multi-cloud
- **Kubernetes Secrets**: For Kubernetes deployments

### 4. Network Security

- Enable SQL Server encryption (TLS/SSL)
- Use firewall rules to restrict database access
- Consider using private networks/VPNs
- Enable SQL Server auditing

### 5. Principle of Least Privilege

The `ITAssetMgmtUser` has only the permissions needed for the application:
- Can read, write, update, and delete data
- Cannot modify database schema
- Cannot create other users
- Cannot access other databases

## Verification

### Check Database Configuration

```sql
-- Verify database exists and RCSI is enabled
SELECT 
    name,
    is_read_committed_snapshot_on,
    recovery_model_desc,
    compatibility_level
FROM sys.databases 
WHERE name = 'ITAssetManagement';
```

Expected result:
- `is_read_committed_snapshot_on`: 1 (enabled)
- `recovery_model_desc`: FULL
- `compatibility_level`: 150

### Check User Permissions

```sql
USE ITAssetManagement;

-- Verify user exists
SELECT name, type_desc 
FROM sys.database_principals 
WHERE name = 'ITAssetMgmtUser';

-- Verify permissions
SELECT 
    dp.name AS UserName,
    p.permission_name,
    p.state_desc
FROM sys.database_permissions p
INNER JOIN sys.database_principals dp ON p.grantee_principal_id = dp.principal_id
WHERE dp.name = 'ITAssetMgmtUser'
ORDER BY p.permission_name;
```

Expected permissions:
- SELECT (GRANT)
- INSERT (GRANT)
- UPDATE (GRANT)
- DELETE (GRANT)
- EXECUTE (GRANT)

### Test Connection

Test the connection from your application:

```bash
# Start the Spring Boot application
cd backend
./mvnw spring-boot:run

# Check logs for successful database connection
# Look for: "HikariPool-1 - Start completed"
```

## Troubleshooting

### Issue: "Cannot enable read committed snapshot isolation"

**Cause**: Active connections to the database

**Solution**:
```sql
-- Force disconnect all users
ALTER DATABASE ITAssetManagement SET SINGLE_USER WITH ROLLBACK IMMEDIATE;

-- Enable RCSI
ALTER DATABASE ITAssetManagement SET READ_COMMITTED_SNAPSHOT ON;

-- Allow multi-user access
ALTER DATABASE ITAssetManagement SET MULTI_USER;
```

### Issue: "Login failed for user 'ITAssetMgmtUser'"

**Possible causes:**
1. Password mismatch
2. User not created
3. User not mapped to database

**Solution**:
```sql
-- Verify login exists
SELECT name FROM sys.server_principals WHERE name = 'ITAssetMgmtUser';

-- Verify user exists in database
USE ITAssetManagement;
SELECT name FROM sys.database_principals WHERE name = 'ITAssetMgmtUser';

-- If user doesn't exist, create it
CREATE USER ITAssetMgmtUser FOR LOGIN ITAssetMgmtUser;
```

### Issue: "Permission denied" errors in application

**Cause**: User lacks required permissions

**Solution**:
```sql
USE ITAssetManagement;

-- Grant all required permissions
GRANT SELECT, INSERT, UPDATE, DELETE, EXECUTE ON SCHEMA::dbo TO ITAssetMgmtUser;
```

### Issue: Flyway migration errors

**Cause**: Flyway baseline not set

**Solution**:
```properties
# In application.properties
spring.flyway.baseline-on-migrate=true
spring.flyway.baseline-version=0
```

### Issue: Connection timeout

**Possible causes:**
1. SQL Server not running
2. Firewall blocking connection
3. Wrong server address/port

**Solution**:
```bash
# Check SQL Server is running
# Windows: Services -> SQL Server (MSSQLSERVER)
# Linux: systemctl status mssql-server

# Test connection
sqlcmd -S localhost -U ITAssetMgmtUser -P YourPassword

# Check firewall allows port 1433
# Windows: Windows Firewall -> Inbound Rules
# Linux: sudo ufw allow 1433/tcp
```

## Maintenance

### Backup Strategy

Implement regular backups:

```sql
-- Full backup (daily)
BACKUP DATABASE ITAssetManagement
TO DISK = 'C:\Backups\ITAssetManagement_Full.bak'
WITH FORMAT, COMPRESSION;

-- Differential backup (every 6 hours)
BACKUP DATABASE ITAssetManagement
TO DISK = 'C:\Backups\ITAssetManagement_Diff.bak'
WITH DIFFERENTIAL, COMPRESSION;

-- Transaction log backup (every 15 minutes)
BACKUP LOG ITAssetManagement
TO DISK = 'C:\Backups\ITAssetManagement_Log.trn'
WITH COMPRESSION;
```

### Index Maintenance

Monitor and maintain indexes:

```sql
-- Check index fragmentation
SELECT 
    OBJECT_NAME(ips.object_id) AS TableName,
    i.name AS IndexName,
    ips.avg_fragmentation_in_percent
FROM sys.dm_db_index_physical_stats(DB_ID(), NULL, NULL, NULL, 'LIMITED') ips
INNER JOIN sys.indexes i ON ips.object_id = i.object_id AND ips.index_id = i.index_id
WHERE ips.avg_fragmentation_in_percent > 10
ORDER BY ips.avg_fragmentation_in_percent DESC;

-- Rebuild fragmented indexes (>30% fragmentation)
ALTER INDEX ALL ON TableName REBUILD;

-- Reorganize moderately fragmented indexes (10-30% fragmentation)
ALTER INDEX ALL ON TableName REORGANIZE;
```

### Statistics Update

Keep statistics current for optimal query performance:

```sql
-- Update all statistics
EXEC sp_updatestats;

-- Update statistics for specific table with full scan
UPDATE STATISTICS TableName WITH FULLSCAN;
```

## Next Steps

After completing database setup:

1. ✅ Database created and configured
2. ✅ User created with appropriate permissions
3. ✅ Read committed snapshot isolation enabled
4. ⏭️ Run Spring Boot application (Flyway will create tables)
5. ⏭️ Verify default admin user is created
6. ⏭️ Test application functionality

## Additional Resources

- [SQL Server Documentation](https://docs.microsoft.com/en-us/sql/sql-server/)
- [Read Committed Snapshot Isolation](https://docs.microsoft.com/en-us/sql/t-sql/statements/set-transaction-isolation-level-transact-sql)
- [Flyway Documentation](https://flywaydb.org/documentation/)
- [Spring Boot Database Initialization](https://docs.spring.io/spring-boot/docs/current/reference/html/howto.html#howto.data-initialization)
- [HikariCP Configuration](https://github.com/brettwooldridge/HikariCP#configuration-knobs-baby)
