# Configuration Quick Start Guide

## Running the Application

### Development Mode

```bash
# Set environment variables
export SPRING_PROFILES_ACTIVE=dev
export DB_USERNAME=sa
export DB_PASSWORD=YourStrong@Passw0rd

# Run with Maven
mvn spring-boot:run

# Or run with Java
java -jar target/it-asset-management-1.0.0-SNAPSHOT.jar
```

### Test Mode

```bash
# Set environment variables
export SPRING_PROFILES_ACTIVE=test
export DB_USERNAME=your_test_user
export DB_PASSWORD=your_test_password
export JWT_SECRET=your_jwt_secret_key_minimum_256_bits

# Run application
mvn spring-boot:run
```

### Production Mode

```bash
# Set environment variables
export SPRING_PROFILES_ACTIVE=prod
export DB_HOST=your-prod-db-server
export DB_USERNAME=your_prod_user
export DB_PASSWORD=your_prod_password
export JWT_SECRET=your_strong_jwt_secret_key
export CORS_ALLOWED_ORIGINS=https://your-app-domain.com

# Run application
java -jar target/it-asset-management-1.0.0-SNAPSHOT.jar
```

## Required Environment Variables by Profile

### Development (dev)
- `DB_USERNAME` - Optional (default: sa)
- `DB_PASSWORD` - Optional (default: YourStrong@Passw0rd)
- `JWT_SECRET` - Optional (has default, but should be changed)

### Test (test)
- `DB_USERNAME` - **Required**
- `DB_PASSWORD` - **Required**
- `JWT_SECRET` - **Required**
- `CORS_ALLOWED_ORIGINS` - Optional

### Production (prod)
- `DB_HOST` - Optional (default: prod-db-server)
- `DB_PORT` - Optional (default: 1433)
- `DB_NAME` - Optional (default: ITAssetManagement)
- `DB_USERNAME` - **Required**
- `DB_PASSWORD` - **Required**
- `JWT_SECRET` - **Required**
- `CORS_ALLOWED_ORIGINS` - **Required**
- `LOG_PATH` - Optional (default: /var/log/it-asset-management)
- `SSL_ENABLED` - Optional (default: false)
- `SSL_KEYSTORE_PATH` - Required if SSL enabled
- `SSL_KEYSTORE_PASSWORD` - Required if SSL enabled

## Database Setup

### Create Development Database

```sql
CREATE DATABASE ITAssetManagement_Dev;
GO
```

### Create Test Database

```sql
CREATE DATABASE ITAssetManagement_Test;
GO
```

### Create Production Database

```sql
CREATE DATABASE ITAssetManagement;
GO
```

## Configuration Files

- **application.properties** - Base configuration (all environments)
- **application-dev.properties** - Development overrides
- **application-test.properties** - Test overrides
- **application-prod.properties** - Production overrides

## Key Configuration Settings

### Connection Pool Sizes

| Environment | Max Pool Size | Min Idle |
|-------------|---------------|----------|
| Development | 10            | 2        |
| Test        | 15            | 3        |
| Production  | 20            | 5        |

### JWT Token Expiration

- Access Token: 30 minutes (1800000 ms)
- Refresh Token: 24 hours (86400000 ms)

### Session Timeout

- All environments: 30 minutes

### File Upload Limits

- Max file size: 10MB
- Max request size: 10MB

## Troubleshooting

### Cannot connect to database

1. Verify SQL Server is running
2. Check connection string in properties file
3. Verify credentials
4. Check firewall rules

### JWT errors

1. Ensure JWT_SECRET is at least 256 bits (32 characters)
2. Generate strong secret: `openssl rand -base64 64`

### Flyway migration errors

1. Check migration files in `src/main/resources/db/migration`
2. Verify database schema matches migration history
3. Use `spring.flyway.baseline-on-migrate=true` for existing databases

## Health Check

```bash
# Check application health
curl http://localhost:8080/actuator/health

# Check metrics
curl http://localhost:8080/actuator/metrics
```

## API Documentation

Once running, access Swagger UI at:
```
http://localhost:8080/swagger-ui.html
```

## For More Details

See `src/main/resources/APPLICATION_PROPERTIES_README.md` for comprehensive documentation.
