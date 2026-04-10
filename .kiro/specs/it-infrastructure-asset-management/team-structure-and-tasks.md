# IT Infrastructure Asset Management - Team Structure & Task Distribution

## Project Overview

**Project Name**: IT Infrastructure Asset Management System  
**Tech Stack**: Angular (Frontend) + Spring Boot (Backend) + MS SQL Server (Database)  
**Team Size**: 5 Developers  
**Project Duration**: TBD  
**Team Lead**: [Name]

---

## Common Codebase Structure

### Backend Structure (Spring Boot)

```
it-asset-management-backend/
├── src/
│   ├── main/
│   │   ├── java/com/company/assetmanagement/
│   │   │   ├── config/                      # Shared configuration
│   │   │   │   ├── SecurityConfig.java
│   │   │   │   ├── JwtConfig.java
│   │   │   │   ├── CorsConfig.java
│   │   │   │   └── DatabaseConfig.java
│   │   │   ├── common/                      # Shared utilities
│   │   │   │   ├── dto/
│   │   │   │   │   ├── ErrorResponse.java
│   │   │   │   │   ├── PageResponse.java
│   │   │   │   │   └── ApiResponse.java
│   │   │   │   ├── exception/
│   │   │   │   │   ├── GlobalExceptionHandler.java
│   │   │   │   │   ├── ResourceNotFoundException.java
│   │   │   │   │   ├── ValidationException.java
│   │   │   │   │   └── DuplicateResourceException.java
│   │   │   │   ├── util/
│   │   │   │   │   ├── DateUtil.java
│   │   │   │   │   ├── ValidationUtil.java
│   │   │   │   │   └── StringUtil.java
│   │   │   │   └── constants/
│   │   │   │       ├── AppConstants.java
│   │   │   │       └── ErrorMessages.java
│   │   │   ├── security/                    # Shared security
│   │   │   │   ├── JwtTokenProvider.java
│   │   │   │   ├── JwtAuthenticationFilter.java
│   │   │   │   ├── UserDetailsServiceImpl.java
│   │   │   │   └── SecurityUtils.java
│   │   │   ├── audit/                       # Shared audit service
│   │   │   │   ├── service/
│   │   │   │   │   ├── AuditService.java
│   │   │   │   │   └── AuditServiceImpl.java
│   │   │   │   ├── repository/
│   │   │   │   │   └── AuditLogRepository.java
│   │   │   │   ├── model/
│   │   │   │   │   └── AuditLog.java
│   │   │   │   └── dto/
│   │   │   │       └── AuditLogDTO.java
│   │   │   │
│   │   │   ├── module1/                     # Module 1: User Management
│   │   │   │   ├── controller/
│   │   │   │   ├── service/
│   │   │   │   ├── repository/
│   │   │   │   ├── model/
│   │   │   │   └── dto/
│   │   │   │
│   │   │   ├── module2/                     # Module 2: Asset Management
│   │   │   │   ├── controller/
│   │   │   │   ├── service/
│   │   │   │   ├── repository/
│   │   │   │   ├── model/
│   │   │   │   └── dto/
│   │   │   │
│   │   │   ├── module3/                     # Module 3: Allocation Management
│   │   │   │   ├── controller/
│   │   │   │   ├── service/
│   │   │   │   ├── repository/
│   │   │   │   ├── model/
│   │   │   │   └── dto/
│   │   │   │
│   │   │   ├── module4/                     # Module 4: Ticket Management
│   │   │   │   ├── controller/
│   │   │   │   ├── service/
│   │   │   │   ├── repository/
│   │   │   │   ├── model/
│   │   │   │   └── dto/
│   │   │   │
│   │   │   └── module5/                     # Module 5: Reporting
│   │   │       ├── controller/
│   │   │       ├── service/
│   │   │       ├── repository/
│   │   │       └── dto/
│   │   │
│   │   └── resources/
│   │       ├── application.properties
│   │       ├── application-dev.properties
│   │       ├── application-prod.properties
│   │       └── db/migration/                # Flyway migrations
│   │           ├── V1__initial_schema.sql
│   │           ├── V2__add_tickets_table.sql
│   │           └── V3__add_indexes.sql
│   │
│   └── test/
│       └── java/com/company/assetmanagement/
│           ├── module1/
│           ├── module2/
│           ├── module3/
│           ├── module4/
│           └── module5/
│
├── pom.xml
└── README.md
```

### Frontend Structure (Angular)

```
it-asset-management-frontend/
├── src/
│   ├── app/
│   │   ├── core/                            # Shared core services
│   │   │   ├── auth/
│   │   │   │   ├── auth.service.ts
│   │   │   │   ├── auth.guard.ts
│   │   │   │   ├── jwt.interceptor.ts
│   │   │   │   └── role.guard.ts
│   │   │   ├── services/
│   │   │   │   ├── api.service.ts
│   │   │   │   ├── notification.service.ts
│   │   │   │   └── storage.service.ts
│   │   │   └── interceptors/
│   │   │       ├── error.interceptor.ts
│   │   │       └── loading.interceptor.ts
│   │   │
│   │   ├── shared/                          # Shared components
│   │   │   ├── components/
│   │   │   │   ├── header/
│   │   │   │   ├── sidebar/
│   │   │   │   ├── footer/
│   │   │   │   ├── loading-spinner/
│   │   │   │   ├── confirmation-dialog/
│   │   │   │   ├── status-badge/
│   │   │   │   └── notification-badge/
│   │   │   ├── models/
│   │   │   │   ├── user.model.ts
│   │   │   │   ├── asset.model.ts
│   │   │   │   ├── ticket.model.ts
│   │   │   │   └── common.model.ts
│   │   │   ├── pipes/
│   │   │   │   ├── date-format.pipe.ts
│   │   │   │   └── status-color.pipe.ts
│   │   │   ├── directives/
│   │   │   └── validators/
│   │   │       └── custom-validators.ts
│   │   │
│   │   ├── features/
│   │   │   ├── auth/                        # Login/Logout
│   │   │   │   ├── login/
│   │   │   │   └── auth.module.ts
│   │   │   │
│   │   │   ├── dashboard/                   # Dashboard
│   │   │   │   ├── dashboard.component.ts
│   │   │   │   └── dashboard.module.ts
│   │   │   │
│   │   │   ├── module1-users/               # Module 1: User Management
│   │   │   │   ├── user-list/
│   │   │   │   ├── user-form/
│   │   │   │   ├── user-profile/
│   │   │   │   ├── services/
│   │   │   │   └── users.module.ts
│   │   │   │
│   │   │   ├── module2-assets/              # Module 2: Asset Management
│   │   │   │   ├── asset-list/
│   │   │   │   ├── asset-form/
│   │   │   │   ├── asset-detail/
│   │   │   │   ├── services/
│   │   │   │   └── assets.module.ts
│   │   │   │
│   │   │   ├── module3-allocation/          # Module 3: Allocation Management
│   │   │   │   ├── allocation-form/
│   │   │   │   ├── allocation-history/
│   │   │   │   ├── services/
│   │   │   │   └── allocation.module.ts
│   │   │   │
│   │   │   ├── module4-tickets/             # Module 4: Ticket Management
│   │   │   │   ├── ticket-list/
│   │   │   │   ├── ticket-detail/
│   │   │   │   ├── ticket-create/
│   │   │   │   ├── services/
│   │   │   │   └── tickets.module.ts
│   │   │   │
│   │   │   └── module5-reports/             # Module 5: Reporting
│   │   │       ├── asset-reports/
│   │   │       ├── ticket-reports/
│   │   │       ├── services/
│   │   │       └── reports.module.ts
│   │   │
│   │   ├── app-routing.module.ts
│   │   ├── app.component.ts
│   │   └── app.module.ts
│   │
│   ├── assets/
│   ├── environments/
│   │   ├── environment.ts
│   │   └── environment.prod.ts
│   └── styles/
│       ├── styles.scss
│       ├── _variables.scss
│       └── _themes.scss
│
├── angular.json
├── package.json
└── README.md
```

---

## Module Distribution & Task Assignment

### Module 1: User Management (Developer 1)

**Responsibility**: Complete user account lifecycle management

#### Backend Tasks:
- **Package**: `com.company.assetmanagement.module1`
- **Model**: `User.java`, `UserRole.java`, `Session.java`
- **Repository**: `UserRepository.java`, `UserRoleRepository.java`, `SessionRepository.java`
- **Service**: `UserService.java`, `UserServiceImpl.java`, `AuthenticationService.java`, `AuthorizationService.java`
- **Controller**: `UserController.java`, `AuthController.java`, `ProfileController.java`
- **DTO**: `UserDTO.java`, `UserRequest.java`, `UserResponse.java`, `LoginRequest.java`, `LoginResponse.java`, `ProfileDTO.java`

#### Frontend Tasks:
- **Module**: `features/module1-users/`
- **Components**:
  - `user-list.component.ts` - Display all users with search/filter
  - `user-form.component.ts` - Create/Edit user form
  - `user-profile.component.ts` - User profile management
- **Services**:
  - `user.service.ts` - User CRUD operations
  - `auth.service.ts` - Authentication operations
- **Models**: `user.model.ts`, `role.model.ts`

#### API Endpoints:
```
POST   /api/v1/auth/login
POST   /api/v1/auth/logout
POST   /api/v1/auth/refresh
POST   /api/v1/auth/change-password

GET    /api/v1/users
GET    /api/v1/users/{id}
POST   /api/v1/users
PUT    /api/v1/users/{id}
DELETE /api/v1/users/{id}
POST   /api/v1/users/{id}/roles
DELETE /api/v1/users/{id}/roles/{role}
PATCH  /api/v1/users/{id}/status

GET    /api/v1/profile
PUT    /api/v1/profile
```

#### Database Tables:
- Users
- UserRoles
- Sessions

#### Key Features:
- User authentication (login/logout)
- User CRUD operations
- Role assignment/revocation
- Enable/Disable user accounts
- Profile management
- Password change
- Session management

---

### Module 2: Asset Management (Developer 2)

**Responsibility**: Complete asset lifecycle management

#### Backend Tasks:
- **Package**: `com.company.assetmanagement.module2`
- **Model**: `Asset.java`, `AssetType.java` (enum), `LifecycleStatus.java` (enum)
- **Repository**: `AssetRepository.java`
- **Service**: `AssetService.java`, `AssetServiceImpl.java`, `AssetValidationService.java`
- **Controller**: `AssetController.java`
- **DTO**: `AssetDTO.java`, `AssetRequest.java`, `AssetResponse.java`, `AssetSearchQuery.java`

#### Frontend Tasks:
- **Module**: `features/module2-assets/`
- **Components**:
  - `asset-list.component.ts` - Display all assets with search/filter
  - `asset-form.component.ts` - Create/Edit asset form
  - `asset-detail.component.ts` - Asset detail view
- **Services**:
  - `asset.service.ts` - Asset CRUD operations
- **Models**: `asset.model.ts`, `asset-type.enum.ts`, `lifecycle-status.enum.ts`

#### API Endpoints:
```
GET    /api/v1/assets
GET    /api/v1/assets/{id}
POST   /api/v1/assets
PUT    /api/v1/assets/{id}
PATCH  /api/v1/assets/{id}
DELETE /api/v1/assets/{id}
PATCH  /api/v1/assets/{id}/status
GET    /api/v1/assets/search
GET    /api/v1/assets/export
POST   /api/v1/assets/import
```

#### Database Tables:
- Assets

#### Key Features:
- Asset registration (15 asset types)
- Asset CRUD operations
- Asset lifecycle tracking (7 statuses)
- Asset search and filtering
- Asset status transitions
- Data validation
- Import/Export functionality
- Serial number uniqueness enforcement

---

### Module 3: Allocation Management (Developer 3)

**Responsibility**: Asset assignment and allocation history

#### Backend Tasks:
- **Package**: `com.company.assetmanagement.module3`
- **Model**: `AssignmentHistory.java`, `Assignment.java`
- **Repository**: `AssignmentHistoryRepository.java`
- **Service**: `AllocationService.java`, `AllocationServiceImpl.java`
- **Controller**: `AllocationController.java`, `AssignmentHistoryController.java`
- **DTO**: `AssignmentDTO.java`, `AssignmentRequest.java`, `AssignmentHistoryDTO.java`

#### Frontend Tasks:
- **Module**: `features/module3-allocation/`
- **Components**:
  - `allocation-form.component.ts` - Assign/Reassign assets
  - `allocation-history.component.ts` - View assignment history
  - `deallocation-form.component.ts` - De-allocate assets
- **Services**:
  - `allocation.service.ts` - Allocation operations
- **Models**: `assignment.model.ts`, `assignment-history.model.ts`

#### API Endpoints:
```
POST   /api/v1/assets/{id}/assignments
GET    /api/v1/assets/{id}/assignment-history
GET    /api/v1/assignments/user/{userId}
GET    /api/v1/assignments/location/{location}
DELETE /api/v1/assets/{id}/assignments
```

#### Database Tables:
- AssignmentHistory

#### Key Features:
- Assign assets to users
- Assign assets to locations
- Reassign assets
- De-allocate assets
- View assignment history
- Query assets by user/location
- Track assignment dates
- Maintain complete assignment audit trail

---

### Module 4: Ticket Management (Developer 4)

**Responsibility**: Asset request ticketing system with approval workflow

#### Backend Tasks:
- **Package**: `com.company.assetmanagement.module4`
- **Model**: `Ticket.java`, `TicketStatusHistory.java`, `TicketType.java` (enum), `TicketStatus.java` (enum), `TicketPriority.java` (enum)
- **Repository**: `TicketRepository.java`, `TicketStatusHistoryRepository.java`
- **Service**: `TicketService.java`, `TicketServiceImpl.java`, `TicketWorkflowService.java`, `NotificationService.java`
- **Controller**: `TicketController.java`, `TicketApprovalController.java`
- **DTO**: `TicketDTO.java`, `AllocationTicketRequest.java`, `DeallocationTicketRequest.java`, `TicketApprovalDTO.java`, `TicketMetricsDTO.java`

#### Frontend Tasks:
- **Module**: `features/module4-tickets/`
- **Components**:
  - `ticket-list.component.ts` - Display user's tickets with filters
  - `ticket-detail.component.ts` - Ticket detail with timeline
  - `ticket-create.component.ts` - Create allocation/de-allocation requests
  - `ticket-approval.component.ts` - Approve/Reject tickets (Admin/Manager)
- **Services**:
  - `ticket.service.ts` - Ticket operations
  - `notification.service.ts` - Notification management
- **Models**: `ticket.model.ts`, `ticket-status.enum.ts`, `notification.model.ts`

#### API Endpoints:
```
GET    /api/v1/tickets
GET    /api/v1/tickets/{id}
POST   /api/v1/tickets/allocation
POST   /api/v1/tickets/deallocation
POST   /api/v1/tickets/{id}/approve
POST   /api/v1/tickets/{id}/reject
POST   /api/v1/tickets/{id}/complete
POST   /api/v1/tickets/{id}/cancel
GET    /api/v1/tickets/{id}/status-history
GET    /api/v1/tickets/my-requests
GET    /api/v1/tickets/pending-approvals
GET    /api/v1/tickets/metrics
GET    /api/v1/notifications
PATCH  /api/v1/notifications/{id}/read
```

#### Database Tables:
- Tickets
- TicketStatusHistory
- Notifications (optional)

#### Key Features:
- Create allocation requests
- Create de-allocation requests
- Ticket approval workflow
- Ticket rejection with reason
- Ticket completion
- Ticket cancellation
- Status tracking with history
- Priority management
- Notification system
- Ticket metrics
- Filter by status/type/priority
- Real-time status updates

---

### Module 5: Reporting & Analytics (Developer 5)

**Responsibility**: Generate reports and analytics dashboards

#### Backend Tasks:
- **Package**: `com.company.assetmanagement.module5`
- **Model**: `Report.java`, `ReportMetadata.java`
- **Repository**: Custom query methods in existing repositories
- **Service**: `ReportService.java`, `ReportServiceImpl.java`, `AnalyticsService.java`, `ExportService.java`
- **Controller**: `ReportController.java`, `AnalyticsController.java`, `DashboardController.java`
- **DTO**: `ReportDTO.java`, `AssetCountByTypeDTO.java`, `AssetsByLocationDTO.java`, `AssetsByStatusDTO.java`, `EndOfLifeReportDTO.java`, `TicketMetricsDTO.java`, `DashboardSummaryDTO.java`

#### Frontend Tasks:
- **Module**: `features/module5-reports/`
- **Components**:
  - `dashboard.component.ts` - Main dashboard with summary cards
  - `asset-reports.component.ts` - Asset reports with charts
  - `ticket-reports.component.ts` - Ticket metrics and analytics
  - `audit-log.component.ts` - Audit log viewer
  - `eol-report.component.ts` - End-of-life assets report
- **Services**:
  - `report.service.ts` - Report generation
  - `dashboard.service.ts` - Dashboard data
  - `analytics.service.ts` - Analytics operations
- **Models**: `report.model.ts`, `dashboard-summary.model.ts`, `chart-data.model.ts`

#### API Endpoints:
```
GET    /api/v1/dashboard/summary
GET    /api/v1/dashboard/notifications

GET    /api/v1/reports/assets/by-type
GET    /api/v1/reports/assets/by-location
GET    /api/v1/reports/assets/by-status
GET    /api/v1/reports/assets/end-of-life
GET    /api/v1/reports/tickets/metrics
GET    /api/v1/reports/tickets/by-status
GET    /api/v1/reports/tickets/by-type

GET    /api/v1/audit-logs
GET    /api/v1/audit-logs/asset/{assetId}
GET    /api/v1/audit-logs/user/{userId}

GET    /api/v1/analytics/asset-trends
GET    /api/v1/analytics/ticket-trends
```

#### Database Tables:
- AuditLog (read-only access)
- Uses data from all other tables for reporting

#### Key Features:
- Dashboard with summary statistics
- Asset count by type report
- Asset distribution by location report
- Assets by lifecycle status report
- End-of-life assets report
- Ticket metrics and analytics
- Audit log viewer with search/filter
- Export reports to CSV/PDF
- Charts and visualizations
- Performance optimization for large datasets
- Real-time dashboard updates

---

## Shared Responsibilities

### All Developers:

1. **Code Quality**:
   - Follow coding standards document
   - Write unit tests (80% coverage minimum)
   - Write property-based tests for their module
   - Document all public APIs with JavaDoc/JSDoc
   - Code review other modules

2. **Integration**:
   - Ensure APIs follow RESTful conventions
   - Use shared DTOs and models where applicable
   - Integrate with audit service for logging
   - Implement proper error handling
   - Follow security best practices

3. **Testing**:
   - Unit tests for services and components
   - Integration tests for API endpoints
   - Property-based tests for correctness properties
   - E2E tests for critical workflows

4. **Documentation**:
   - API documentation (Swagger/OpenAPI)
   - Component documentation
   - README for their module
   - Update main README

---

## Development Workflow

### Phase 1: Setup & Foundation (Week 1-2)
- **Team Lead**: Set up repositories, CI/CD, database
- **All Developers**: Set up local development environment
- **All Developers**: Create module structure and shared components

### Phase 2: Core Development (Week 3-8)
- **Parallel Development**: Each developer works on their assigned module
- **Daily Standups**: Progress updates and blocker resolution
- **Weekly Integration**: Merge and test integrated features

### Phase 3: Integration & Testing (Week 9-10)
- **Integration Testing**: Test module interactions
- **Bug Fixes**: Resolve integration issues
- **Performance Testing**: Optimize queries and API responses

### Phase 4: UAT & Deployment (Week 11-12)
- **User Acceptance Testing**: Stakeholder testing
- **Bug Fixes**: Address UAT feedback
- **Deployment**: Production deployment
- **Documentation**: Final documentation updates

---

## Communication & Collaboration

### Daily Standup (15 minutes)
- What did you complete yesterday?
- What will you work on today?
- Any blockers?

### Weekly Team Meeting (1 hour)
- Demo completed features
- Discuss integration points
- Review and plan next week's tasks
- Address technical challenges

### Code Review Process
- All PRs require at least 1 approval
- Team lead reviews critical changes
- Use PR templates for consistency
- Address review comments within 24 hours

### Documentation
- Update API documentation with each PR
- Maintain module README files
- Document design decisions
- Keep task tracking updated

---

## Git Branching Strategy

```
main (production)
  ├── develop (integration branch)
      ├── feature/module1-user-management
      ├── feature/module2-asset-management
      ├── feature/module3-allocation-management
      ├── feature/module4-ticket-management
      └── feature/module5-reporting
```

### Branch Naming Convention:
- Feature: `feature/module{X}-{feature-name}`
- Bugfix: `bugfix/module{X}-{bug-description}`
- Hotfix: `hotfix/{critical-issue}`

### Commit Message Format:
```
[Module{X}] {Type}: {Short description}

{Detailed description}

Closes #{issue-number}
```

Example:
```
[Module1] feat: Add user creation API endpoint

- Implemented UserController.createUser()
- Added validation for username uniqueness
- Integrated with audit service

Closes #123
```

---

## Dependencies Between Modules

### Module Dependencies:
```
Module 1 (Users) ← Module 2 (Assets) [CreatedBy, UpdatedBy]
Module 1 (Users) ← Module 3 (Allocation) [AssignedBy]
Module 1 (Users) ← Module 4 (Tickets) [RequesterId, ApproverId]
Module 2 (Assets) ← Module 3 (Allocation) [AssetId]
Module 2 (Assets) ← Module 4 (Tickets) [AssetId]
Module 3 (Allocation) ← Module 4 (Tickets) [Ticket completion triggers allocation]
All Modules → Audit Service (Shared)
All Modules → Module 5 (Reporting) [Data source]
```

### Integration Points:
1. **Module 1 → All**: User authentication and authorization
2. **Module 2 → Module 3**: Asset availability for allocation
3. **Module 4 → Module 3**: Ticket approval triggers allocation/de-allocation
4. **All → Module 5**: Data for reporting and analytics
5. **All → Audit**: Logging all operations

---

## Testing Strategy by Module

### Module 1 (User Management):
- Properties 1-5, 44-47, 52 (9 properties)
- Unit tests: Authentication, Authorization, User CRUD
- Integration tests: Login flow, Role assignment

### Module 2 (Asset Management):
- Properties 7-12, 16-17, 28-29, 32-33 (12 properties)
- Unit tests: Asset CRUD, Validation, Lifecycle
- Integration tests: Asset search, Import/Export

### Module 3 (Allocation Management):
- Properties 18-20 (3 properties)
- Unit tests: Assignment logic, History tracking
- Integration tests: Allocation workflow

### Module 4 (Ticket Management):
- Properties 35-43, 48-49, 51 (12 properties)
- Unit tests: Ticket workflow, Approval logic
- Integration tests: End-to-end ticket lifecycle

### Module 5 (Reporting):
- Properties 21-22, 24, 50 (4 properties)
- Unit tests: Report generation, Data aggregation
- Integration tests: Performance with large datasets

### Shared (Audit):
- Properties 23, 25 (2 properties)
- Unit tests: Audit logging
- Integration tests: Audit log immutability

**Total**: 52 correctness properties across all modules

---

## Success Criteria

### Module Completion Checklist:
- [ ] All API endpoints implemented and documented
- [ ] All UI components implemented and responsive
- [ ] Unit test coverage > 80%
- [ ] All property-based tests passing
- [ ] Integration tests passing
- [ ] Code reviewed and approved
- [ ] Documentation complete
- [ ] No critical or high-severity bugs
- [ ] Performance requirements met

### Project Completion Criteria:
- [ ] All 5 modules integrated successfully
- [ ] All 52 correctness properties validated
- [ ] End-to-end workflows tested
- [ ] UAT completed and approved
- [ ] Production deployment successful
- [ ] Team training completed
- [ ] Documentation handed over

---

## Contact & Escalation

**Team Lead**: [Name] - [Email] - [Phone]  
**Project Manager**: [Name] - [Email] - [Phone]  
**Technical Architect**: [Name] - [Email] - [Phone]

**Escalation Path**:
1. Team Lead (for technical issues)
2. Project Manager (for timeline/resource issues)
3. Technical Architect (for architectural decisions)

---

## Appendix

### Useful Commands

**Backend**:
```bash
# Run application
./mvnw spring-boot:run

# Run tests
./mvnw test

# Run specific module tests
./mvnw test -Dtest=Module1*

# Build
./mvnw clean package
```

**Frontend**:
```bash
# Install dependencies
npm install

# Run development server
ng serve

# Run tests
ng test

# Build for production
ng build --prod
```

**Database**:
```bash
# Run migrations
./mvnw flyway:migrate

# Rollback migration
./mvnw flyway:undo
```

### Reference Documents:
- [Requirements Document](./requirements.md)
- [Design Document](./design.md)
- [Coding Standards](../../steering/it-asset-management-coding-standards.md)
- [Testing Guide](../../steering/it-asset-management-testing-guide.md)
- [API Design Guide](../../steering/it-asset-management-api-design.md)
- [Deployment Guide](../../steering/it-asset-management-deployment.md)
- [UI Screen Prompts](./ui-screen-prompts.md)
