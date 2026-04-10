# Database Migration Scripts

This directory contains Flyway migration scripts for the IT Asset Management application database.

## Prerequisites

- Microsoft SQL Server 2019 or later
- SQL Server Management Studio (SSMS) or sqlcmd command-line tool
- Administrator access to SQL Server instance

## Initial Database Setup

### Step 1: Execute Initial Setup Script

The `V1__initial_database_setup.sql` script performs the following:
1. Creates the `ITAssetManagement` database
2. Creates a dedicated database user `ITAssetMgmtUser` with appropriate permissions
3. Enables read committed snapshot isolation for optimistic concurrency control
4. Configures database settings for optimal performance

#### Option A: Using SQL Server Management Studio (SSMS)

1. Open SQL Server Management Studio
2. Connect to your SQL Server instance with administrator credentials
3. Open the file `V1__initial_database_setup.sql`
4. **Important**: Before executing, update the password in the script:
   ```sql
   CREATE LOGIN ITAssetMgmtUser WITH PASSWORD = 'YourSecurePassword123!';
   ```
   Replace `YourSecurePassword123!` with a strong password that meets your security requirements.
5. Execute the script (F5 or click Execute)
6. Verify the output messages confirm successful creation

#### Option B: Using sqlcmd Command-Line Tool

```bash
# Update the password in the script first, then run:
sqlcmd -S localhost -U sa -P YourSAPassword -i V1__initial_database_setup.sql
```

### Step 2: Configure Application Connection

Update the `backend/src/main/resources/application.properties` file with the database connection details:

```properties
# Database Configuration
spring.datasource.url=jdbc:sqlserver://localhost:1433;databaseName=ITAssetManagement;encrypt=true;trustServerCertificate=true
spring.datasource.username=ITAssetMgmtUser
spring.datasource.password=YourSecurePassword123!
spring.datasource.driver-class-name=com.microsoft.sqlserver.jdbc.SQLServerDriver

# Flyway Configuration
spring.flyway.enabled=true
spring.flyway.baseline-on-migrate=true
spring.flyway.locations=classpath:db/migration
spring.flyway.sql-migration-prefix=V
spring.flyway.sql-migration-separator=__
spring.flyway.sql-migration-suffixes=.sql
```

### Step 3: Verify Database Setup

Connect to the database and verify the configuration:

```sql
-- Check if database exists
SELECT name, is_read_committed_snapshot_on 
FROM sys.databases 
WHERE name = 'ITAssetManagement';

-- Check if user exists
USE ITAssetManagement;
SELECT name, type_desc 
FROM sys.database_principals 
WHERE name = 'ITAssetMgmtUser';

-- Verify user permissions
SELECT 
    dp.name AS UserName,
    dp.type_desc AS UserType,
    p.permission_name,
    p.state_desc
FROM sys.database_permissions p
INNER JOIN sys.database_principals dp ON p.grantee_principal_id = dp.principal_id
WHERE dp.name = 'ITAssetMgmtUser';
```

Expected results:
- Database `ITAssetManagement` exists with `is_read_committed_snapshot_on = 1`
- User `ITAssetMgmtUser` exists with type `SQL_USER`
- User has SELECT, INSERT, UPDATE, DELETE, and EXECUTE permissions on schema dbo

## Security Considerations

### Password Management

**IMPORTANT**: The default password in the script is for demonstration purposes only. In production:

1. **Generate a strong password** with at least:
   - 16 characters
   - Uppercase and lowercase letters
   - Numbers
   - Special characters

2. **Store the password securely**:
   - Use environment variables: `${DB_PASSWORD}`
   - Use a secrets management service (Azure Key Vault, AWS Secrets Manager, HashiCorp Vault)
   - Never commit passwords to version control

3. **Update application.properties** to use environment variables:
   ```properties
   spring.datasource.username=${DB_USERNAME:ITAssetMgmtUser}
   spring.datasource.password=${DB_PASSWORD}
   ```

4. **Set environment variables** before running the application:
   ```bash
   export DB_USERNAME=ITAssetMgmtUser
   export DB_PASSWORD=YourActualSecurePassword
   ```

### User Permissions

The `ITAssetMgmtUser` has been granted:
- **SELECT**: Read data from tables
- **INSERT**: Create new records
- **UPDATE**: Modify existing records
- **DELETE**: Remove records
- **EXECUTE**: Run stored procedures

The user does **NOT** have:
- **DDL permissions**: Cannot create, alter, or drop tables (schema changes are managed by Flyway migrations)
- **Database administration**: Cannot modify database settings or create other users

This follows the principle of least privilege for application security.

## Read Committed Snapshot Isolation

### What is it?

Read Committed Snapshot Isolation (RCSI) is an optimistic concurrency control mechanism that:
- Allows readers to read a consistent snapshot of data without blocking writers
- Prevents readers from blocking writers and vice versa
- Reduces lock contention and improves concurrency
- Maintains transaction isolation without the overhead of traditional locking

### Why enable it?

For the IT Asset Management application, RCSI provides:
1. **Better concurrency**: Multiple users can read and write assets simultaneously
2. **Improved performance**: Reduced lock waits and deadlocks
3. **Consistent reads**: Readers see a consistent snapshot of data
4. **Optimistic updates**: Writers don't block readers

### How it works

When RCSI is enabled:
- SQL Server maintains row versions in tempdb
- Readers see the last committed version of rows
- Writers create new row versions
- Old versions are cleaned up automatically

### Performance Impact

- **Pros**: Significantly reduces blocking and improves throughput
- **Cons**: Requires additional tempdb space for row versioning
- **Recommendation**: Monitor tempdb size and ensure adequate disk space

## Troubleshooting

### Issue: "Database already exists" error

If the database already exists, the script will skip creation and continue with configuration. This is normal behavior.

### Issue: "Login already exists" error

If the login already exists, the script will skip creation. Verify the password matches your configuration.

### Issue: Permission denied errors

Ensure you're running the script with SQL Server administrator privileges (sa or sysadmin role).

### Issue: Cannot enable read committed snapshot isolation

This can occur if there are active connections to the database. Solution:
```sql
-- Set database to single-user mode
ALTER DATABASE ITAssetManagement SET SINGLE_USER WITH ROLLBACK IMMEDIATE;

-- Enable RCSI
ALTER DATABASE ITAssetManagement SET READ_COMMITTED_SNAPSHOT ON;

-- Set back to multi-user mode
ALTER DATABASE ITAssetManagement SET MULTI_USER;
```

### Issue: Flyway baseline error

If you're adding Flyway to an existing database:
```properties
spring.flyway.baseline-on-migrate=true
spring.flyway.baseline-version=0
```

## Next Steps

After completing the initial database setup:

1. **Run the application**: Spring Boot with Flyway will automatically execute subsequent migration scripts
2. **Create tables**: The next migration script (V2__create_tables.sql) will create all application tables
3. **Seed initial data**: Subsequent scripts will create the default administrator user and initial configuration

## Migration Script Naming Convention

Flyway migration scripts follow this naming pattern:
```
V{version}__{description}.sql

Examples:
V1__initial_database_setup.sql
V2__create_tables.sql
V3__seed_initial_data.sql
V4__add_tickets_table.sql
```

- **V**: Prefix indicating a versioned migration
- **{version}**: Sequential version number (1, 2, 3, ...)
- **__**: Double underscore separator
- **{description}**: Descriptive name using underscores
- **.sql**: File extension

## Additional Resources

- [Flyway Documentation](https://flywaydb.org/documentation/)
- [SQL Server Read Committed Snapshot Isolation](https://docs.microsoft.com/en-us/sql/t-sql/statements/set-transaction-isolation-level-transact-sql)
- [Spring Boot Database Initialization](https://docs.spring.io/spring-boot/docs/current/reference/html/howto.html#howto.data-initialization)
