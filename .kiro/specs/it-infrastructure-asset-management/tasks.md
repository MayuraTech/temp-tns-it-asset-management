# Implementation Plan: IT Infrastructure Asset Management

## Overview

This implementation plan focuses on project setup and infrastructure tasks for the IT Infrastructure Asset Management system. The system uses Angular 17+ for the frontend, Spring Boot 3.x (Java 17+) for the backend, and Microsoft SQL Server 2019+ for the database. The plan covers repository setup, database configuration, shared infrastructure components, CI/CD pipeline, and development environment setup.

## Tasks

- [x] 1. Initialize project repositories and build configuration
  - Create backend repository with Spring Boot 3.x project structure
  - Create frontend repository with Angular 17+ project structure
  - Configure Maven for backend (pom.xml with Spring Boot parent, dependencies)
  - Configure npm/package.json for frontend with Angular CLI
  - Set up .gitignore files for both repositories
  - Create README.md files with project overview and setup instructions
  - _Requirements: Project Setup_

- [ ] 2. Configure database and migration framework
  - [x] 2.1 Set up Microsoft SQL Server database
    - Create ITAssetManagement database
    - Configure database user with appropriate permissions
    - Enable read committed snapshot isolation
    - _Requirements: Database Setup_
  
  - [x] 2.2 Configure Flyway for database migrations
    - Add Flyway dependency to pom.xml
    - Configure Flyway properties (baseline-on-migrate, locations, validation)
    - Create db/migration directory structure
    - _Requirements: Database Setup_
  
  - [x] 2.3 Create initial database schema migration (V1__initial_schema.sql)
    - Create Users table with indexes
    - Create UserRoles table with foreign keys and constraints
    - Create Sessions table with indexes
    - Create Assets table with indexes and constraints
    - Create AssignmentHistory table
    - Create AuditLog table with indexes
    - Create Configurations table
    - Insert default admin user with BCrypt hashed password
    - Insert default configurations (session timeout, lifespan thresholds)
    - _Requirements: 1, 2, 3, 4, 5, 7, 9, 12_
  
  - [x] 2.4 Create tickets schema migration (V2__add_tickets_table.sql)
    - Create Tickets table with indexes and constraints
    - Create TicketStatusHistory table
    - Add ticket-related action types to AuditLog constraints
    - _Requirements: 15, 16, 18_

- [ ] 3. Implement shared backend infrastructure
  - [x] 3.1 Configure Spring Security with JWT
    - Create SecurityConfig class with security filter chain
    - Implement JwtTokenProvider for token generation and validation
    - Create JwtAuthenticationFilter for request authentication
    - Configure CORS settings
    - Configure security headers (CSP, XSS protection, HSTS)
    - _Requirements: 1.1, 1.2, 1.4, 1.5_
  
  - [x] 3.2 Implement global exception handling
    - Create GlobalExceptionHandler with @RestControllerAdvice
    - Handle ValidationException with comprehensive error details
    - Handle DuplicateSerialNumberException (409 Conflict)
    - Handle InsufficientPermissionsException (403 Forbidden)
    - Handle ResourceNotFoundException (404 Not Found)
    - Handle InvalidStatusTransitionException (422 Unprocessable Entity)
    - Create ErrorResponse DTO with type, message, details, timestamp, requestId
    - _Requirements: 3.3, 4.5, 5.2, 11.4_
  
  - [x] 3.3 Create shared DTOs and models
    - Create ErrorResponse DTO
    - Create PageResponse DTO for pagination
    - Create ApiResponse wrapper DTO
    - Create ValidationError DTO
    - Define AssetType enum (15 types)
    - Define LifecycleStatus enum (7 statuses)
    - Define Role enum (Administrator, Asset_Manager, Viewer)
    - Define Action enum for permissions
    - Define TicketType, TicketStatus, TicketPriority enums
    - _Requirements: 2.1, 3.4, 5.1_
  
  - [x] 3.4 Implement audit logging service
    - Create AuditLog entity with JPA annotations
    - Create AuditLogRepository extending JpaRepository
    - Create AuditService interface
    - Implement AuditServiceImpl with logEvent method
    - Create AuditEvent DTO
    - Implement searchAuditLog with filtering by date, user, action, resource
    - Ensure audit log entries are immutable (no update/delete operations)
    - _Requirements: 9.1, 9.2, 9.3, 9.4, 9.5_
  
  - [x] 3.5 Create utility classes
    - Create DateUtil for date formatting and validation
    - Create ValidationUtil for common validation logic
    - Create StringUtil for string operations
    - Create AppConstants for application-wide constants
    - Create ErrorMessages for standardized error messages
    - _Requirements: 11.1, 11.2, 11.3, 11.4, 11.5_

- [ ] 4. Implement shared frontend infrastructure
  - [x] 4.1 Set up Angular core services
    - Create AuthService for authentication operations
    - Create AuthGuard for route protection
    - Create JwtInterceptor for adding auth tokens to requests
    - Create RoleGuard for role-based route protection
    - Create ErrorInterceptor for global error handling
    - Create LoadingInterceptor for loading state management
    - _Requirements: 1.1, 1.5, 2.2_
  
  - [x] 4.2 Create shared Angular components
    - Create HeaderComponent with navigation and user menu
    - Create SidebarComponent with role-based menu items
    - Create FooterComponent
    - Create LoadingSpinnerComponent
    - Create ConfirmationDialogComponent
    - Create StatusBadgeComponent for asset/ticket status display
    - Create NotificationBadgeComponent with unread count
    - _Requirements: 15.7, 18.2, 18.4_
  
  - [x] 4.3 Create shared models and interfaces
    - Create User model/interface
    - Create Asset model/interface
    - Create Ticket model/interface
    - Create SearchQuery interface
    - Create PageResponse interface
    - Create ErrorResponse interface
    - Create enums for AssetType, LifecycleStatus, TicketType, TicketStatus, TicketPriority
    - _Requirements: 3.4, 5.1, 15.2_
  
  - [x] 4.4 Implement shared pipes and directives
    - Create DateFormatPipe for consistent date formatting
    - Create StatusColorPipe for status badge colors
    - Create custom validators (email, password complexity, date not in future)
    - _Requirements: 1.3, 11.3, 11.5_
  
  - [x] 4.5 Configure Angular Material or PrimeNG
    - Install UI component library
    - Configure theme and styling
    - Import required modules (tables, forms, dialogs, badges, etc.)
    - Set up responsive breakpoints in SCSS
    - _Requirements: 17.1, 17.2, 17.3, 17.4, 17.5_

- [ ] 5. Configure application properties and environment settings
  - [x] 5.1 Create backend application properties
    - Configure application.properties (base configuration)
    - Configure application-dev.properties (development settings)
    - Configure application-test.properties (test settings)
    - Configure application-prod.properties (production settings)
    - Set up database connection pooling (HikariCP)
    - Configure JPA/Hibernate settings
    - Configure JWT settings (secret, expiration times)
    - Configure logging levels and patterns
    - Configure file upload limits
    - _Requirements: 1.5, 12.1_
  
  - [x] 5.2 Create frontend environment files
    - Configure environment.ts (development API URLs)
    - Configure environment.prod.ts (production API URLs)
    - Set up API base URL configuration
    - Configure authentication settings
    - _Requirements: API Integration_

- [ ] 6. Set up CI/CD pipeline
  - [ ] 6.1 Create GitHub Actions workflow for backend
    - Configure workflow triggers (push, pull_request)
    - Set up JDK 17 environment
    - Add step to run unit tests (./mvnw test)
    - Add step to run integration tests (./mvnw verify)
    - Add step to generate JaCoCo coverage report
    - Add step to build JAR (./mvnw clean package)
    - Add step to upload coverage to Codecov
    - _Requirements: Testing, Build_
  
  - [ ] 6.2 Create GitHub Actions workflow for frontend
    - Configure workflow triggers
    - Set up Node.js environment
    - Add step to install dependencies (npm ci)
    - Add step to run linting (ng lint)
    - Add step to run unit tests (ng test --watch=false)
    - Add step to build production bundle (ng build --configuration production)
    - Add step to upload build artifacts
    - _Requirements: Testing, Build_
  
  - [ ] 6.3 Create Docker configuration
    - Create Dockerfile for backend (multi-stage build with Maven and JRE)
    - Create Dockerfile for frontend (multi-stage build with Node and Nginx)
    - Create docker-compose.yml for local development (database, backend, frontend)
    - Create nginx.conf for frontend production serving
    - _Requirements: Deployment_

- [ ] 7. Set up development environment documentation
  - [ ] 7.1 Create backend setup guide
    - Document prerequisites (JDK 17, Maven, SQL Server)
    - Document database setup steps
    - Document how to run Flyway migrations
    - Document how to run application locally (./mvnw spring-boot:run)
    - Document how to run tests
    - Document environment variable configuration
    - _Requirements: Developer Onboarding_
  
  - [ ] 7.2 Create frontend setup guide
    - Document prerequisites (Node.js 18+, npm, Angular CLI)
    - Document how to install dependencies (npm install)
    - Document how to run development server (ng serve)
    - Document how to run tests (ng test)
    - Document how to build for production (ng build --prod)
    - Document environment configuration
    - _Requirements: Developer Onboarding_
  
  - [ ] 7.3 Create API documentation setup
    - Add Springdoc OpenAPI dependency to pom.xml
    - Configure Swagger UI endpoint (/swagger-ui.html)
    - Add OpenAPI annotations to controller methods
    - Document authentication requirements
    - Document all endpoints with request/response examples
    - _Requirements: API Documentation_

- [ ] 8. Implement database connection and JPA configuration
  - [ ] 8.1 Configure Spring Data JPA
    - Create DatabaseConfig class
    - Configure entity scanning
    - Configure transaction management
    - Set up connection pool settings
    - Configure SQL logging for development
    - _Requirements: Database Access_
  
  - [ ] 8.2 Create base entity classes
    - Create BaseEntity with id, createdAt, updatedAt fields
    - Add JPA auditing annotations (@CreatedDate, @LastModifiedDate)
    - Enable JPA auditing in configuration
    - _Requirements: 3.5, 4.2, 9.1_

- [ ] 9. Set up logging and monitoring
  - [ ] 9.1 Configure backend logging
    - Create logback-spring.xml configuration
    - Configure console appender with JSON format
    - Configure file appender with rolling policy
    - Set up log levels for different packages
    - Configure request ID generation and logging
    - _Requirements: 9.1, 9.2_
  
  - [ ] 9.2 Configure Spring Boot Actuator
    - Add Actuator dependency
    - Configure health endpoint
    - Configure metrics endpoint
    - Configure Prometheus endpoint for metrics export
    - Secure actuator endpoints with authentication
    - _Requirements: Monitoring_

- [ ] 10. Checkpoint - Verify infrastructure setup
  - Ensure all tests pass
  - Verify database migrations run successfully
  - Verify backend application starts without errors
  - Verify frontend application starts and connects to backend
  - Verify CI/CD pipeline runs successfully
  - Verify Docker containers build and run
  - Ask the user if questions arise

## Notes

- This plan focuses on foundational infrastructure and shared components
- All configuration follows the coding standards and API design guidelines
- Database migrations use Flyway for version control
- Security is configured from the start with JWT authentication
- CI/CD pipeline ensures code quality and automated testing
- Docker configuration enables consistent deployment across environments
- Comprehensive documentation supports developer onboarding
- Audit logging is built into the infrastructure for compliance
- The checkpoint ensures all infrastructure is working before feature development begins
