# Application Properties Configuration Guide

This document explains the application properties configuration for the IT Infrastructure Asset Management system.

## Configuration Files

The application uses Spring Boot profiles to manage environment-specific configurations:

- **application.properties** - Base configuration shared across all environments
- **application-dev.properties** - Development environment settings
- **application-test.properties** - Test environment settings
- **application-prod.properties** - Production environment settings

## Activating Profiles

### Via Environment Variable

```bash
export SPRING_PROFILES_ACTIVE=dev
```

### Via Command Line

```bash
java -jar it-asset-management.jar --spring.profiles.active=prod
```

### Via Maven

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

### Via IDE (IntelliJ IDEA / Eclipse)

Set the environment variable `SPRING_PROFILES_ACTIVE=dev` in your run configuration.

## Environment-Specific Configuration

### Development (application-dev.properties)

**Purpose:** Local development with verbose logging and relaxed security

**Key Features:**
- Local SQL Server database (localhost:1433)
- SQL logging enabled for debugging
- Debug-level logging for application code
- CORS enabled for localhost:4200 (Angular dev server)
- Smaller connection pool (max 10 connections)
- Development JWT secret (should be changed)
- All actuator endpoints exposed
- Flyway clean enabled for database resets

**Database Setup:**
```sql
CREATE DATABASE ITAssetManagement_Dev;
```

**Required Environment Variables:**
- `DB_USERNAME` (default: sa)
- `DB_PASSWORD` (default: YourStrong@Passw0rd)
- `JWT_SECRET` (optional, has default for dev)

### Test (application-test.properties)

**Purpose:** Testing environment for QA and integration testing

**Key Features:**
- Test database server connection
- Moderate connection pool (max 15 connections)
- INFO-level logging
- CORS configured for test domain
- Flyway clean disabled (data preservation)
- Limited actuator endpoints
- Caching disabled for consistent test results

**Database Setup:**
```sql
CREATE DATABASE IT_Asset;
```

**Required Environment Variables:**
- `DB_USERNAME` (required)
- `DB_PASSWORD` (required)
- `JWT_SECRET` (required)
- `CORS_ALLOWED_ORIGINS` (optional, default: http://test.example.com)

### Production (application-prod.properties)

**Purpose:** Production deployment with security hardening and performance optimization

**Key Features:**
- Production database with SSL/TLS encryption
- Large connection pool (max 20 connections)
- Connection leak detection enabled
- WARN-level logging only
- File-based logging with rotation
- HTTPS-only cookies
- Compression enabled
- Second-level cache enabled
- Prometheus metrics export
- Security headers enforced
- Error details hidden from responses

**Database Setup:**
```sql
CREATE DATABASE ITAssetManagement;
```

**Required Environment Variables:**
- `DB_HOST` (default: prod-db-server)
- `DB_PORT` (default: 1433)
- `DB_NAME` (default: ITAssetManagement)
- `DB_USERNAME` (required)
- `DB_PASSWORD` (required)
- `JWT_SECRET` (required, must be strong)
- `CORS_ALLOWED_ORIGINS` (default: https://app.example.com)
- `LOG_PATH` (default: /var/log/it-asset-management)
- `SSL_ENABLED` (optional, default: false)
- `SSL_KEYSTORE_PATH` (required if SSL enabled)
- `SSL_KEYSTORE_PASSWORD` (required if SSL enabled)

## Configuration Details

### Database Connection Pooling (HikariCP)

HikariCP is the default connection pool in Spring Boot, configured per environment:

| Setting | Dev | Test | Prod | Description |
|---------|-----|------|------|-------------|
| maximum-pool-size | 10 | 15 | 20 | Maximum connections in pool |
| minimum-idle | 2 | 3 | 5 | Minimum idle connections |
| connection-timeout | 30000 | 30000 | 30000 | Max wait for connection (ms) |
| idle-timeout | 600000 | 600000 | 600000 | Max idle time (ms) |
| max-lifetime | 1800000 | 1800000 | 1800000 | Max connection lifetime (ms) |
| leak-detection-threshold | - | - | 60000 | Connection leak detection (ms) |

### JPA/Hibernate Settings

**Common Settings:**
- `hibernate.ddl-auto=validate` - Validates schema without modifications
- `hibernate.dialect=SQLServerDialect` - SQL Server specific SQL
- `hibernate.jdbc.batch_size=20` - Batch insert/update operations
- `hibernate.order_inserts=true` - Optimize insert ordering
- `hibernate.order_updates=true` - Optimize update ordering

**Environment-Specific:**
- **Dev:** SQL logging enabled, statistics disabled
- **Test:** SQL logging disabled, caching disabled
- **Prod:** SQL logging disabled, second-level cache enabled

### JWT Configuration

JWT tokens are used for authentication with the following settings:

- **jwt.expiration:** 1800000 ms (30 minutes) - Access token lifetime
- **jwt.refresh-expiration:** 86400000 ms (24 hours) - Refresh token lifetime
- **jwt.secret:** Must be at least 256 bits (32 characters)

**Security Note:** Always use a strong, randomly generated secret in production. Never commit secrets to version control.

### Logging Configuration

**Log Levels by Environment:**

| Logger | Dev | Test | Prod |
|--------|-----|------|------|
| root | INFO | WARN | WARN |
| com.company.assetmanagement | DEBUG | INFO | INFO |
| org.springframework.web | DEBUG | INFO | WARN |
| org.springframework.security | DEBUG | INFO | WARN |
| org.hibernate.SQL | DEBUG | WARN | WARN |

**Production Logging:**
- File-based logging: `/var/log/it-asset-management/application.log`
- Max file size: 10MB
- Max history: 30 days
- Total size cap: 1GB

### File Upload Limits

All environments support file uploads with the following limits:
- **max-file-size:** 10MB per file
- **max-request-size:** 10MB total request size

These limits apply to asset data import operations.

### CORS Configuration

Cross-Origin Resource Sharing (CORS) is configured per environment:

- **Dev:** `http://localhost:4200,http://localhost:3000`
- **Test:** Configurable via `CORS_ALLOWED_ORIGINS`
- **Prod:** Configurable via `CORS_ALLOWED_ORIGINS` (HTTPS only recommended)

### Session Configuration

- **Timeout:** 30 minutes of inactivity
- **Cookie settings:**
  - `http-only=true` - Prevents JavaScript access
  - `secure=true` (prod only) - HTTPS only
  - `same-site=strict` (prod only) - CSRF protection

### Actuator Endpoints

Spring Boot Actuator provides monitoring and management endpoints:

**Development:**
- All endpoints exposed: health, info, metrics, env, beans, mappings
- Health details always shown

**Test:**
- Limited endpoints: health, info, metrics
- Health details shown when authorized

**Production:**
- Limited endpoints: health, info, metrics, prometheus
- Health details shown when authorized
- Prometheus metrics enabled for monitoring

## Security Best Practices

### 1. Environment Variables

Never hardcode sensitive values. Use environment variables:

```bash
export DB_USERNAME=assetmgmt_user
export DB_PASSWORD=SecurePassword123!
export JWT_SECRET=YourVeryLongAndSecureSecretKeyHere
```

### 2. JWT Secret Generation

Generate a strong JWT secret:

```bash
openssl rand -base64 64
```

### 3. Database Credentials

Use strong passwords and rotate them regularly. Consider using a secrets management service:
- AWS Secrets Manager
- Azure Key Vault
- HashiCorp Vault

### 4. SSL/TLS Configuration

In production, always use HTTPS:

```properties
server.ssl.enabled=true
server.ssl.key-store=classpath:keystore.p12
server.ssl.key-store-password=${SSL_KEYSTORE_PASSWORD}
server.ssl.key-store-type=PKCS12
```

### 5. CORS Configuration

Restrict CORS to specific domains in production:

```bash
export CORS_ALLOWED_ORIGINS=https://app.example.com,https://admin.example.com
```

## Troubleshooting

### Database Connection Issues

**Problem:** Cannot connect to database

**Solutions:**
1. Verify database server is running
2. Check connection string in properties file
3. Verify credentials are correct
4. Check firewall rules allow connection
5. Verify SQL Server is configured for TCP/IP connections

### JWT Token Issues

**Problem:** JWT tokens not working

**Solutions:**
1. Verify JWT_SECRET is set and at least 256 bits
2. Check token expiration times
3. Verify clock synchronization between servers
4. Check for special characters in secret that need escaping

### Flyway Migration Issues

**Problem:** Flyway validation fails

**Solutions:**
1. Check migration files are in correct location
2. Verify migration file naming convention
3. Check database schema matches migration history
4. Use `spring.flyway.baseline-on-migrate=true` for existing databases

### Connection Pool Exhaustion

**Problem:** "Connection is not available" errors

**Solutions:**
1. Increase `maximum-pool-size` in properties
2. Check for connection leaks (enable leak detection in prod)
3. Verify connections are being closed properly
4. Monitor connection usage with actuator metrics

## Monitoring and Metrics

### Health Check Endpoint

```bash
curl http://localhost:8080/actuator/health
```

### Metrics Endpoint

```bash
curl http://localhost:8080/actuator/metrics
```

### Prometheus Metrics (Production)

```bash
curl http://localhost:8080/actuator/prometheus
```

## Docker Configuration

When running in Docker, pass environment variables:

```bash
docker run -d \
  -e SPRING_PROFILES_ACTIVE=prod \
  -e DB_HOST=prod-db-server \
  -e DB_USERNAME=assetmgmt_user \
  -e DB_PASSWORD=SecurePassword123! \
  -e JWT_SECRET=YourSecretKey \
  -p 8080:8080 \
  it-asset-management:latest
```

## Kubernetes Configuration

Use ConfigMaps and Secrets:

```yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: app-config
data:
  SPRING_PROFILES_ACTIVE: "prod"
  DB_HOST: "prod-db-server"
---
apiVersion: v1
kind: Secret
metadata:
  name: app-secrets
type: Opaque
stringData:
  DB_USERNAME: "assetmgmt_user"
  DB_PASSWORD: "SecurePassword123!"
  JWT_SECRET: "YourSecretKey"
```

## References

- [Spring Boot Configuration Properties](https://docs.spring.io/spring-boot/docs/current/reference/html/application-properties.html)
- [HikariCP Configuration](https://github.com/brettwooldridge/HikariCP#configuration-knobs-baby)
- [Hibernate Configuration](https://docs.jboss.org/hibernate/orm/current/userguide/html_single/Hibernate_User_Guide.html#configurations)
- [Spring Security Configuration](https://docs.spring.io/spring-security/reference/servlet/configuration/java.html)
- [Flyway Documentation](https://flywaydb.org/documentation/)
