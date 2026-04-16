# Backend Database Configuration Fix

## Problem

The Spring Boot application was failing to start with the following error:

```
Driver com.microsoft.sqlserver.jdbc.SQLServerDriver claims to not accept jdbcUrl, 
jdbc:h2:mem:c072956f-e4a0-44ff-a97e-a77f9291f28b;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE
```

### Root Cause

The application had a configuration mismatch:
- **Base configuration** (`application.properties`) specified SQL Server driver
- **No active profile** was set, so Spring Boot auto-configured H2 in-memory database
- **Flyway** was enabled and trying to use SQL Server driver with H2 database URL
- This caused a driver mismatch error during startup

## Solution

Created a dedicated development profile configuration that properly configures H2 database for local development.

### New File: `application-dev.properties`

This configuration:
1. **Uses H2 in-memory database** with SQL Server compatibility mode
2. **Disables Flyway** (not needed for H2 with `create-drop` mode)
3. **Enables H2 console** for debugging at `/h2-console`
4. **Configures Hibernate** to auto-create schema on startup
5. **Sets development-friendly logging** with DEBUG level
6. **Enables detailed error messages** for easier debugging
7. **Configures CORS** for local frontend development

## How to Use

### Option 1: Run with dev profile (Recommended for local development)

```bash
# Using Maven
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# Using Java
java -jar target/it-asset-management-1.0.0-SNAPSHOT.jar --spring.profiles.active=dev

# Using IDE (IntelliJ/Eclipse)
Set VM options: -Dspring.profiles.active=dev
```

### Option 2: Run with test profile (Uses SQL Server)

```bash
# Requires SQL Server to be running
mvn spring-boot:run -Dspring-boot.run.profiles=test
```

### Option 3: Set default profile in application.properties

Add this line to `application.properties`:
```properties
spring.profiles.active=dev
```

## Configuration Profiles Overview

| Profile | Database | Flyway | Use Case |
|---------|----------|--------|----------|
| **dev** | H2 in-memory | Disabled | Local development without SQL Server |
| **test** | SQL Server | Enabled | Integration testing with real database |
| **prod** | SQL Server | Enabled | Production deployment |

## Development Workflow

### 1. Local Development (No SQL Server Required)

```bash
# Start backend with dev profile
cd backend
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# Access H2 Console (optional)
# URL: http://localhost:8080/h2-console
# JDBC URL: jdbc:h2:mem:itassetdb
# Username: sa
# Password: (leave empty)
```

### 2. Testing with SQL Server

```bash
# Ensure SQL Server is running
# Update credentials in application-test.properties if needed

# Start backend with test profile
mvn spring-boot:run -Dspring-boot.run.profiles=test
```

### 3. Production Deployment

```bash
# Set environment variables
export SPRING_PROFILES_ACTIVE=prod
export DB_URL=jdbc:sqlserver://prod-server:1433;databaseName=ITAssetManagement
export DB_USERNAME=prod_user
export DB_PASSWORD=secure_password
export JWT_SECRET=your-secure-jwt-secret-key

# Run application
java -jar it-asset-management.jar
```

## H2 Database Features

### SQL Server Compatibility Mode

The H2 database is configured with `MODE=MSSQLServer` to provide compatibility with SQL Server syntax:
- Supports SQL Server-specific functions
- Compatible with SQL Server data types
- Allows testing without SQL Server installation

### Schema Auto-Creation

With `spring.jpa.hibernate.ddl-auto=create-drop`:
- Schema is automatically created on startup
- Schema is dropped on shutdown
- Perfect for development and testing
- No manual database setup required

### H2 Console Access

Access the H2 console for debugging:
1. Start application with dev profile
2. Navigate to: `http://localhost:8080/h2-console`
3. Use connection details:
   - JDBC URL: `jdbc:h2:mem:itassetdb`
   - Username: `sa`
   - Password: (empty)

## Troubleshooting

### Issue: Application still fails with driver mismatch

**Solution**: Ensure you're running with the dev profile:
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

### Issue: H2 console not accessible

**Solution**: Verify dev profile is active and check:
```properties
spring.h2.console.enabled=true
spring.h2.console.path=/h2-console
```

### Issue: Need to use SQL Server for development

**Solution**: 
1. Install SQL Server Express
2. Create database: `IT_Asset`
3. Update `application-test.properties` with your credentials
4. Run with test profile: `mvn spring-boot:run -Dspring-boot.run.profiles=test`

### Issue: Flyway migrations not running

**Solution**: Flyway is disabled in dev profile (H2 auto-creates schema). To enable:
1. Use test or prod profile with SQL Server
2. Flyway will automatically run migrations from `src/main/resources/db/migration/`

## Next Steps

1. **Start backend**: `mvn spring-boot:run -Dspring-boot.run.profiles=dev`
2. **Verify startup**: Check logs for "Started AssetManagementApplication"
3. **Test endpoints**: Access Swagger UI at `http://localhost:8080/swagger-ui.html`
4. **Start frontend**: Navigate to frontend directory and run `npm start`

## Files Modified

1. **Created**: `backend/src/main/resources/application-dev.properties` - Development profile configuration

## Additional Notes

- **Security**: Dev profile uses a hardcoded JWT secret for convenience. Never use this in production.
- **Performance**: H2 in-memory database is fast but data is lost on restart.
- **Testing**: For integration tests with persistent data, use the test profile with SQL Server.
- **Production**: Always use the prod profile with proper environment variables and secrets management.
