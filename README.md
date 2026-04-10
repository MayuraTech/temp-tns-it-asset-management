# IT Infrastructure Asset Management System

A comprehensive web-based application for tracking, managing, and monitoring IT infrastructure assets throughout their lifecycle.

## Overview

The IT Infrastructure Asset Management system provides organizations with a centralized platform to manage their IT assets from acquisition through retirement. The system features secure multi-user access with role-based permissions, asset lifecycle tracking, ticketing workflows for allocation/de-allocation, comprehensive audit logging, and powerful reporting capabilities.

## Features

- **Asset Lifecycle Management**: Track assets from ordered to retired status
- **Role-Based Access Control**: Three-tier permission system (Administrator, Asset Manager, Viewer)
- **Ticketing System**: Approval-based workflow for asset allocation and de-allocation
- **Advanced Search**: Fast, indexed search across large inventories (100,000+ assets)
- **Assignment Tracking**: Complete history of asset assignments to users and locations
- **Audit Logging**: Immutable audit trail for compliance and investigation
- **Reporting**: Asset distribution, lifecycle status, and end-of-life reports
- **Bulk Operations**: Import/export assets in CSV and JSON formats
- **Responsive Design**: Optimized for desktop, tablet, and mobile devices

## Technology Stack

### Backend
- **Framework**: Spring Boot 3.2.1
- **Language**: Java 17+
- **Database**: Microsoft SQL Server 2019+
- **ORM**: Spring Data JPA with Hibernate
- **Authentication**: Spring Security with JWT
- **Database Migrations**: Flyway
- **API Documentation**: SpringDoc OpenAPI (Swagger)

### Frontend
- **Framework**: Angular 17+
- **Language**: TypeScript 5.2+
- **UI Components**: Angular Material
- **State Management**: RxJS
- **Testing**: Jasmine, Karma, fast-check

## Project Structure

```
.
├── backend/                 # Spring Boot backend application
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/
│   │   │   └── resources/
│   │   └── test/
│   ├── pom.xml
│   └── README.md
├── frontend/                # Angular frontend application
│   ├── src/
│   │   ├── app/
│   │   ├── assets/
│   │   └── environments/
│   ├── package.json
│   └── README.md
├── .kiro/
│   ├── specs/              # Specification documents
│   └── steering/           # Development guidelines
└── README.md
```

## Quick Start

### Prerequisites

- **Backend**:
  - JDK 17 or higher
  - Maven 3.6+
  - Microsoft SQL Server 2019+

- **Frontend**:
  - Node.js 18+
  - npm 9+
  - Angular CLI 17+

### Backend Setup

1. Navigate to the backend directory:
   ```bash
   cd backend
   ```

2. Configure database connection in `src/main/resources/application-local.properties`

3. Run database migrations:
   ```bash
   ./mvnw flyway:migrate
   ```

4. Start the backend server:
   ```bash
   ./mvnw spring-boot:run -Dspring-boot.run.profiles=local
   ```

The backend API will be available at `http://localhost:8080`

API documentation: `http://localhost:8080/swagger-ui.html`

### Frontend Setup

1. Navigate to the frontend directory:
   ```bash
   cd frontend
   ```

2. Install dependencies:
   ```bash
   npm install
   ```

3. Start the development server:
   ```bash
   npm start
   ```

The frontend application will be available at `http://localhost:4200`

## Default Credentials

After running database migrations, a default administrator account is created:

- **Username**: `admin`
- **Password**: `Admin@123456`

**Important**: Change this password immediately in production environments.

## Development

### Backend Development

See [backend/README.md](backend/README.md) for detailed backend development instructions.

Key commands:
```bash
# Run tests
./mvnw test

# Run with specific profile
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev

# Build JAR
./mvnw clean package

# Generate coverage report
./mvnw jacoco:report
```

### Frontend Development

See [frontend/README.md](frontend/README.md) for detailed frontend development instructions.

Key commands:
```bash
# Start dev server
npm start

# Run tests
npm test

# Build for production
npm run build:prod

# Run linter
npm run lint
```

## Testing

### Backend Testing

The backend includes comprehensive test coverage:
- **Unit Tests**: Test individual components in isolation
- **Integration Tests**: Test component interactions and database operations
- **Property-Based Tests**: Verify correctness properties with jqwik

```bash
cd backend
./mvnw test                              # All tests
./mvnw test -Dtest=**/*PropertyTest      # Property-based tests only
./mvnw verify -P integration-tests       # Integration tests
```

### Frontend Testing

The frontend includes:
- **Unit Tests**: Component and service tests with Jasmine
- **Property-Based Tests**: Correctness verification with fast-check

```bash
cd frontend
npm test                    # Run all tests
npm run test:coverage       # Generate coverage report
```

## Documentation

- **API Documentation**: Available at `/swagger-ui.html` when backend is running
- **Requirements**: See `.kiro/specs/it-infrastructure-asset-management/requirements.md`
- **Design**: See `.kiro/specs/it-infrastructure-asset-management/design.md`
- **Tasks**: See `.kiro/specs/it-infrastructure-asset-management/tasks.md`
- **Coding Standards**: See `.kiro/steering/it-asset-management-coding-standards.md`
- **API Design Guide**: See `.kiro/steering/it-asset-management-api-design.md`
- **Testing Guide**: See `.kiro/steering/it-asset-management-testing-guide.md`
- **Deployment Guide**: See `.kiro/steering/it-asset-management-deployment.md`

## Deployment

### Docker Deployment

Build and run with Docker Compose:

```bash
docker-compose up -d
```

This will start:
- SQL Server database
- Backend API server
- Frontend web server

### Production Deployment

See [.kiro/steering/it-asset-management-deployment.md](.kiro/steering/it-asset-management-deployment.md) for detailed deployment instructions including:
- Kubernetes deployment
- CI/CD pipeline setup
- Database backup strategies
- Monitoring and logging configuration

## Architecture

The application follows a layered architecture:

```
┌─────────────────────────────────────────┐
│         Presentation Layer              │
│      (Angular SPA / REST API)           │
└─────────────────────────────────────────┘
                  │
┌─────────────────────────────────────────┐
│         Application Layer               │
│  (Services, Controllers, Business Logic)│
└─────────────────────────────────────────┘
                  │
┌─────────────────────────────────────────┐
│           Domain Layer                  │
│     (Entities, Value Objects)           │
└─────────────────────────────────────────┘
                  │
┌─────────────────────────────────────────┐
│        Data Access Layer                │
│    (Repositories, JPA, Hibernate)       │
└─────────────────────────────────────────┘
                  │
┌─────────────────────────────────────────┐
│        Persistence Layer                │
│      (MS SQL Server Database)           │
└─────────────────────────────────────────┘
```

## Security

- **Authentication**: JWT-based authentication with 30-minute token expiration
- **Authorization**: Role-based access control with three permission levels
- **Password Security**: BCrypt hashing with strength 10+
- **API Security**: HTTPS only, CORS configuration, security headers
- **Audit Logging**: Complete audit trail for all state-changing operations

## Performance

The system is optimized for large-scale deployments:
- Search operations complete within 2 seconds for 100,000+ assets
- Report generation within 10 seconds for 100,000+ assets
- Database indexing on frequently queried columns
- Connection pooling with HikariCP
- Frontend lazy loading and virtual scrolling

## Contributing

1. Create a feature branch from `develop`
2. Follow coding standards and guidelines
3. Write tests for new functionality
4. Ensure all tests pass
5. Submit a pull request

## License

Copyright © 2024 Company Name. All rights reserved.

## Support

For issues, questions, or contributions, please contact the development team.
