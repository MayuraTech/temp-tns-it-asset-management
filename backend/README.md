# IT Infrastructure Asset Management - Backend

Spring Boot backend application for the IT Infrastructure Asset Management system.

## Overview

This application provides a comprehensive REST API for tracking, managing, and monitoring IT infrastructure assets throughout their lifecycle. It supports secure multi-user access with role-based permissions, asset lifecycle management, ticketing workflows, audit logging, and reporting capabilities.

## Technology Stack

- **Framework**: Spring Boot 3.2.1
- **Java Version**: 17+
- **Database**: Microsoft SQL Server 2019+
- **ORM**: Spring Data JPA with Hibernate
- **Authentication**: Spring Security with JWT
- **Database Migrations**: Flyway
- **API Documentation**: SpringDoc OpenAPI (Swagger)
- **Testing**: JUnit 5, Mockito, jqwik (property-based testing)

## Prerequisites

- JDK 17 or higher
- Maven 3.6+
- Microsoft SQL Server 2019+ (or Docker container)
- Git

## Project Structure

```
src/
├── main/
│   ├── java/com/company/assetmanagement/
│   │   ├── config/              # Configuration classes
│   │   ├── controller/          # REST controllers
│   │   ├── service/             # Business logic services
│   │   ├── repository/          # JPA repositories
│   │   ├── model/              # Domain entities
│   │   ├── dto/                # Data transfer objects
│   │   ├── exception/          # Custom exceptions
│   │   ├── security/           # Security configuration
│   │   └── util/               # Utility classes
│   └── resources/
│       ├── db/migration/       # Flyway migration scripts
│       ├── application.properties
│       └── application-{profile}.properties
└── test/
    └── java/com/company/assetmanagement/
        ├── unit/               # Unit tests
        ├── integration/        # Integration tests
        └── property/           # Property-based tests
```

## Getting Started

### 1. Database Setup

Create a SQL Server database:

```sql
CREATE DATABASE ITAssetManagement;
GO

-- Create application user
CREATE LOGIN assetmgmt_user WITH PASSWORD = 'YourSecurePassword123!';
GO

USE ITAssetManagement;
GO

CREATE USER assetmgmt_user FOR LOGIN assetmgmt_user;
GO

GRANT SELECT, INSERT, UPDATE, DELETE ON SCHEMA::dbo TO assetmgmt_user;
GO
```

### 2. Configure Application

Create `src/main/resources/application-local.properties`:

```properties
# Database Configuration
spring.datasource.url=jdbc:sqlserver://localhost:1433;databaseName=ITAssetManagement;encrypt=true;trustServerCertificate=true
spring.datasource.username=assetmgmt_user
spring.datasource.password=YourSecurePassword123!

# JWT Configuration
jwt.secret=YourVeryLongAndSecureSecretKeyHereAtLeast256BitsForHS256Algorithm
jwt.expiration=1800000
jwt.refresh-expiration=86400000

# Logging
logging.level.com.company.assetmanagement=DEBUG
```

### 3. Run Database Migrations

Flyway migrations run automatically on application startup. To run manually:

```bash
./mvnw flyway:migrate
```

### 4. Build the Application

```bash
./mvnw clean package
```

### 5. Run the Application

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

The application will start on `http://localhost:8080`

### 6. Access API Documentation

Once the application is running, access Swagger UI at:

```
http://localhost:8080/swagger-ui.html
```

## Running Tests

### Run All Tests

```bash
./mvnw test
```

### Run Unit Tests Only

```bash
./mvnw test -Dtest=**/*Test
```

### Run Property-Based Tests

```bash
./mvnw test -Dtest=**/*PropertyTest
```

### Run Integration Tests

```bash
./mvnw verify -P integration-tests
```

### Generate Code Coverage Report

```bash
./mvnw jacoco:report
```

Coverage report will be available at `target/site/jacoco/index.html`

## Environment Variables

The following environment variables can be used to configure the application:

| Variable | Description | Default |
|----------|-------------|---------|
| `DB_USERNAME` | Database username | - |
| `DB_PASSWORD` | Database password | - |
| `JWT_SECRET` | JWT signing secret | - |
| `SPRING_PROFILES_ACTIVE` | Active Spring profile | `local` |
| `SERVER_PORT` | Application port | `8080` |

## API Endpoints

### Authentication

- `POST /api/v1/auth/login` - User login
- `POST /api/v1/auth/logout` - User logout
- `POST /api/v1/auth/refresh` - Refresh access token

### Assets

- `GET /api/v1/assets` - List assets with filtering and pagination
- `GET /api/v1/assets/{id}` - Get asset by ID
- `POST /api/v1/assets` - Create new asset
- `PUT /api/v1/assets/{id}` - Update asset
- `DELETE /api/v1/assets/{id}` - Delete asset
- `PATCH /api/v1/assets/{id}/status` - Update asset status
- `GET /api/v1/assets/{id}/assignment-history` - Get assignment history

### Tickets

- `GET /api/v1/tickets` - List tickets
- `POST /api/v1/tickets/allocation` - Create allocation ticket
- `POST /api/v1/tickets/deallocation` - Create de-allocation ticket
- `POST /api/v1/tickets/{id}/approve` - Approve ticket
- `POST /api/v1/tickets/{id}/reject` - Reject ticket

### Users

- `GET /api/v1/users` - List users
- `POST /api/v1/users` - Create user
- `PUT /api/v1/users/{id}` - Update user
- `POST /api/v1/users/{id}/roles` - Assign role

### Reports

- `GET /api/v1/reports/assets/by-type` - Asset count by type
- `GET /api/v1/reports/assets/by-location` - Assets by location
- `GET /api/v1/reports/assets/end-of-life` - End-of-life report

## Default Credentials

After running migrations, a default administrator account is created:

- **Username**: `admin`
- **Password**: `Admin@123456`

**Important**: Change this password immediately in production environments.

## Development Guidelines

- Follow the coding standards defined in `.kiro/steering/it-asset-management-coding-standards.md`
- Follow the API design guidelines in `.kiro/steering/it-asset-management-api-design.md`
- Write tests for all new functionality (unit, integration, and property-based tests)
- Ensure code coverage remains above 80%
- Run tests before committing code
- Use meaningful commit messages

## Troubleshooting

### Database Connection Issues

If you encounter database connection errors:

1. Verify SQL Server is running
2. Check connection string in `application-local.properties`
3. Verify database user has correct permissions
4. Check firewall settings

### Flyway Migration Failures

If migrations fail:

```bash
# Check migration status
./mvnw flyway:info

# Repair failed migrations
./mvnw flyway:repair

# Clean database (WARNING: deletes all data)
./mvnw flyway:clean
```

## Contributing

1. Create a feature branch from `develop`
2. Make your changes following coding standards
3. Write tests for new functionality
4. Ensure all tests pass
5. Submit a pull request

## License

Copyright © 2024 Company Name. All rights reserved.
