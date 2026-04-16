# Tasks: Allocation Management

## Overview

This task list breaks down the implementation of Module 3: Allocation Management into manageable development tasks. The module handles asset assignment to users and locations, tracks assignment history, and maintains a complete audit trail.

**Developer**: Developer 3  
**Module**: Module 3 - Allocation Management  
**Dependencies**: Module 1 (User Management), Module 2 (Asset Management), Audit Service

---

## Phase 1: Database and Model Setup

### 1.1 Create Database Migration
- [x] Create Flyway migration file `V3__create_assignment_history_table.sql`
- [x] Define AssignmentHistory table with all required columns
- [x] Add foreign key constraints to Assets and Users tables
- [x] Create indexes on AssetId, AssignedTo, and AssignedAt columns
- [x] Add check constraint for AssignmentType enum values
- [x] Test migration on local database

### 1.2 Create Domain Models
- [x] Create `AssignmentType` enum (USER, LOCATION)
- [x] Create `AssignmentHistory` entity with JPA annotations
- [x] Add proper indexes using @Index annotation
- [x] Implement equals() and hashCode() methods
- [x] Add validation annotations
- [x] Write unit tests for entity validation

---

## Phase 2: Backend Repository Layer

### 2.1 Create AssignmentHistory Repository
- [x] Create `AssignmentHistoryRepository` interface extending JpaRepository
- [x] Add custom query method `findByAssetIdOrderByAssignedAtDesc`
- [x] Add query method `findByAssignedToContainingIgnoreCase`
- [x] Add query method `findActiveAssignmentsByAssetId`
- [x] Add query method for statistics aggregation
- [x] Write integration tests for all repository methods

---

## Phase 3: Backend Service Layer

### 3.1 Create Allocation Service Interface
- [x] Define `AllocationService` interface
- [x] Add method signatures for assign to user
- [x] Add method signatures for assign to location
- [x] Add method signatures for deallocate
- [x] Add method signatures for reassign
- [x] Add method signatures for query operations
- [x] Add method signatures for statistics

### 3.2 Implement Allocation Service - Assignment Operations
- [x] Implement `assignToUser()` method
  - [x] Add authorization check
  - [x] Validate assignment request
  - [x] Check asset availability and status
  - [x] Verify asset not already assigned
  - [x] Create AssignmentHistory record
  - [x] Update Asset assignment fields
  - [x] Log to Audit Service
  - [x] Handle transaction rollback on failure
- [x] Implement `assignToLocation()` method
  - [x] Add authorization check
  - [x] Validate location assignment request
  - [x] Check asset availability
  - [x] Create AssignmentHistory record
  - [x] Update Asset location field
  - [x] Log to Audit Service
- [x] Write unit tests for assignment operations
- [x] Write property-based test for Property 18 (assignment creation)

### 3.3 Implement Allocation Service - Deallocation Operations
- [x] Implement `deallocate()` method
  - [x] Add authorization check
  - [x] Verify asset is currently assigned
  - [x] Set UnassignedAt timestamp on current assignment
  - [x] Clear asset assignment fields
  - [x] Log to Audit Service
  - [x] Handle transaction rollback on failure
- [x] Implement `bulkDeallocate()` method
  - [x] Validate bulk request size (max 50)
  - [x] Process each deallocation independently
  - [x] Collect success and failure results
  - [x] Log each operation separately
- [x] Write unit tests for deallocation operations
- [x] Write property-based test for Property 20 (deallocation completeness)

### 3.4 Implement Allocation Service - Reassignment Operations
- [x] Implement `reassign()` method
  - [x] Add authorization check
  - [x] Verify asset is currently assigned
  - [x] Validate new assignment details
  - [x] Close current assignment (set UnassignedAt)
  - [x] Create new assignment record
  - [x] Update asset fields
  - [x] Ensure atomic operation (both succeed or both fail)
  - [x] Log both operations to Audit Service
- [x] Write unit tests for reassignment
- [x] Test transaction rollback scenarios

### 3.5 Implement Allocation Service - Query Operations
- [x] Implement `getAssignmentHistory()` method
  - [x] Add authorization check
  - [x] Query with pagination
  - [x] Order by AssignedAt descending
  - [x] Include username of assigner
  - [x] Return paginated response
- [x] Implement `getAssetsByUser()` method
  - [x] Query assets with active user assignments
  - [x] Support case-insensitive matching
  - [x] Include pagination
- [x] Implement `getAssetsByLocation()` method
  - [x] Query assets with active location assignments
  - [x] Support case-insensitive matching
  - [x] Include pagination
- [x] Write unit tests for query operations
- [x] Write property-based test for Property 19 (history order)

### 3.6 Implement Allocation Service - Statistics and Export
- [x] Implement `getStatistics()` method
  - [x] Count total assigned assets
  - [x] Count user vs location assignments
  - [x] Count available assets by status
  - [x] Get top 10 users by asset count
  - [x] Get top 10 locations by asset count
  - [x] Use efficient aggregation queries
- [x] Implement `exportAssignments()` method
  - [x] Generate CSV with assignment data
  - [x] Support filtering by type, date range, user
  - [x] Limit to 10,000 records
  - [x] Log export operation to Audit Service
- [x] Write unit tests for statistics and export

### 3.7 Implement Validation Methods
- [x] Create `validateAssignmentRequest()` method
  - [x] Validate asset ID format (UUID)
  - [x] Validate assigned to field (not empty, max 255 chars)
  - [x] Validate email format for user assignments
  - [x] Return comprehensive validation errors
- [x] Create `validateAssetAssignable()` method
  - [x] Check asset status (IN_USE, DEPLOYED, STORAGE)
  - [x] Return specific error for each invalid status
- [x] Write unit tests for validation methods

---

## Phase 4: Backend Controller Layer

### 4.1 Create Allocation Controller
- [x] Create `AllocationController` class with @RestController
- [x] Add @RequestMapping for /api/v1/assets
- [x] Inject AllocationService dependency

### 4.2 Implement Assignment Endpoints
- [x] Implement POST /api/v1/assets/{id}/assignments
  - [x] Add @PreAuthorize for ADMINISTRATOR and ASSET_MANAGER
  - [x] Validate request body with @Valid
  - [x] Route to assignToUser or assignToLocation based on type
  - [x] Return 201 Created with assignment DTO
- [x] Implement DELETE /api/v1/assets/{id}/assignments
  - [x] Add @PreAuthorize for ADMINISTRATOR and ASSET_MANAGER
  - [x] Call deallocate service method
  - [x] Return 204 No Content
- [x] Write integration tests for assignment endpoints

### 4.3 Implement Query Endpoints
- [x] Implement GET /api/v1/assets/{id}/assignment-history
  - [x] Add @PreAuthorize for all roles
  - [x] Accept pagination parameters
  - [x] Return paginated history
- [x] Implement GET /api/v1/assignments/user/{userId}
  - [x] Add @PreAuthorize for all roles
  - [x] Accept pagination parameters
  - [x] Return paginated asset list
- [x] Implement GET /api/v1/assignments/location/{location}
  - [x] Add @PreAuthorize for all roles
  - [x] Accept pagination parameters
  - [x] Return paginated asset list
- [x] Write integration tests for query endpoints

### 4.4 Implement Statistics and Export Endpoints
- [x] Implement GET /api/v1/assignments/statistics
  - [x] Add @PreAuthorize for ADMINISTRATOR and ASSET_MANAGER
  - [x] Return statistics DTO
- [x] Implement GET /api/v1/assignments/export
  - [x] Add @PreAuthorize for ADMINISTRATOR and ASSET_MANAGER
  - [x] Accept filter parameters
  - [x] Return CSV file
- [x] Implement POST /api/v1/assignments/bulk-deallocate
  - [x] Add @PreAuthorize for ADMINISTRATOR and ASSET_MANAGER
  - [x] Validate bulk request
  - [x] Return bulk operation result
- [x] Write integration tests for statistics and export endpoints

---

## Phase 5: Backend DTOs

### 5.1 Create Request DTOs
- [x] Create `AssignmentRequest` DTO
  - [x] Add assignmentType field with @NotNull
  - [x] Add assignedTo field with @NotBlank and @Size
  - [x] Add assignedUserEmail field with @Email
  - [x] Add custom validation for email required when type is USER
- [x] Write unit tests for DTO validation

### 5.2 Create Response DTOs
- [x] Create `AssignmentDTO` class
  - [x] Add all assignment fields
  - [x] Add mapper method from entity
- [x] Create `AssignmentHistoryDTO` class
  - [x] Add all history fields
  - [x] Include username of assigner
  - [x] Add mapper method from entity
- [x] Create `AssignmentStatisticsDTO` class
  - [x] Add statistics fields
  - [x] Add top users and locations lists
- [x] Create `BulkDeallocationResult` DTO
  - [x] Add success and failure lists
  - [x] Add summary counts

---

## Phase 6: Backend Exception Handling

### 6.1 Create Custom Exceptions
- [x] Create `AssetAlreadyAssignedException`
- [x] Create `AssetNotAssignedException`
- [x] Create `AssetNotAssignableException`
- [x] Create `BulkOperationTooLargeException`
- [x] Create `ExportTooLargeException`

### 6.2 Implement Exception Handlers
- [x] Add @ExceptionHandler for AssetAlreadyAssignedException (409 Conflict)
- [x] Add @ExceptionHandler for AssetNotAssignedException (404 Not Found)
- [x] Add @ExceptionHandler for AssetNotAssignableException (422 Unprocessable Entity)
- [x] Add @ExceptionHandler for BulkOperationTooLargeException (400 Bad Request)
- [x] Add @ExceptionHandler for ExportTooLargeException (400 Bad Request)
- [x] Write tests for exception handling

---

## Phase 7: Frontend Service Layer

### 7.1 Create Allocation Service
- [x] Create `allocation.service.ts` in services folder
- [x] Inject HttpClient dependency
- [x] Define API base URL

### 7.2 Implement Assignment Methods
- [x] Implement `assignToUser()` method
  - [x] POST request to /api/v1/assets/{id}/assignments
  - [x] Set assignmentType to USER
  - [x] Handle errors with catchError
- [x] Implement `assignToLocation()` method
  - [x] POST request to /api/v1/assets/{id}/assignments
  - [x] Set assignmentType to LOCATION
  - [x] Handle errors with catchError
- [x] Implement `deallocate()` method
  - [x] DELETE request to /api/v1/assets/{id}/assignments
  - [x] Handle errors with catchError
- [x] Implement `reassign()` method
  - [x] Combine deallocate and assign operations
  - [x] Handle errors appropriately

### 7.3 Implement Query Methods
- [x] Implement `getAssignmentHistory()` method
  - [x] GET request with pagination parameters
  - [x] Return Observable<Page<AssignmentHistory>>
- [x] Implement `getAssetsByUser()` method
  - [x] GET request with pagination
  - [x] Return Observable<Page<Asset>>
- [x] Implement `getAssetsByLocation()` method
  - [x] GET request with pagination
  - [x] Return Observable<Page<Asset>>
- [x] Implement `getStatistics()` method
  - [x] GET request to statistics endpoint
  - [x] Return Observable<AssignmentStatistics>

### 7.4 Implement Export and Bulk Operations
- [x] Implement `exportAssignments()` method
  - [x] GET request with filter parameters
  - [x] Handle file download
- [x] Implement `bulkDeallocate()` method
  - [x] POST request with asset IDs
  - [x] Return Observable<BulkDeallocationResult>
- [x] Write unit tests for all service methods

---

## Phase 8: Frontend Models

### 8.1 Create TypeScript Models
- [x] Create `allocation.model.ts`
  - [x] Define Assignment interface
  - [x] Define AssignmentType enum
- [x] Create `assignment-history.model.ts`
  - [x] Define AssignmentHistory interface
- [x] Create `assignment-request.model.ts`
  - [x] Define AssignmentRequest interface
- [x] Create `assignment-statistics.model.ts`
  - [x] Define AssignmentStatistics interface
  - [x] Define TopUser and TopLocation interfaces

---

## Phase 9: Frontend Components

### 9.1 Create Allocation Form Component
- [x] Generate component with Angular CLI
- [x] Create reactive form with FormBuilder
  - [x] Add assignmentType field
  - [x] Add assignedTo field
  - [x] Add assignedUserEmail field (conditional)
- [x] Implement conditional validation for email
  - [x] Watch assignmentType changes
  - [x] Add/remove email validators dynamically
- [x] Implement onSubmit() method
  - [x] Validate form
  - [x] Call appropriate service method
  - [x] Show success/error messages
  - [x] Navigate on success
- [x] Create component template
  - [x] Add form fields with Material components
  - [x] Add validation error messages
  - [x] Add submit and cancel buttons
- [x] Create component styles following Editorial Geometry
- [x] Write unit tests for component

### 9.2 Create Assignment History Component
- [x] Generate component with Angular CLI
- [x] Create observables for data and loading state
- [x] Implement ngOnInit() to load history
  - [x] Get asset ID from route
  - [x] Call service to fetch history
  - [x] Handle pagination
- [x] Create component template
  - [x] Display history in table or list
  - [x] Show assignment type, assigned to, dates
  - [x] Add pagination controls
  - [x] Show loading spinner
  - [x] Show empty state
- [x] Create component styles following Editorial Geometry
- [x] Write unit tests for component

### 9.3 Create Deallocation Form Component
- [x] Generate component with Angular CLI
- [x] Create confirmation dialog
- [x] Implement deallocate() method
  - [x] Show confirmation dialog
  - [x] Call service on confirmation
  - [x] Show success/error messages
  - [x] Navigate on success
- [x] Create component template
  - [x] Show asset details
  - [x] Show current assignment info
  - [x] Add deallocate button
  - [x] Add cancel button
- [x] Create component styles following Editorial Geometry
- [x] Write unit tests for component

### 9.4 Create Assignment Statistics Component
- [x] Generate component with Angular CLI
- [x] Create observables for statistics data
- [x] Implement ngOnInit() to load statistics
- [x] Create component template
  - [x] Display total assigned assets
  - [x] Show user vs location breakdown
  - [x] Display available assets by status
  - [x] Show top 10 users chart
  - [x] Show top 10 locations chart
- [x] Create component styles following Editorial Geometry
- [x] Add chart visualization (using Chart.js or similar)
- [x] Write unit tests for component

---

## Phase 10: Frontend Module Configuration

### 10.1 Create Allocation Module
- [x] Create `allocation.module.ts`
- [x] Import CommonModule, FormsModule, ReactiveFormsModule
- [x] Import Material modules (MatFormField, MatInput, MatButton, etc.)
- [x] Import HttpClientModule
- [x] Declare all allocation components
- [x] Export components for use in other modules

### 10.2 Configure Routing
- [x] Add routes for allocation form
- [x] Add routes for assignment history
- [x] Add routes for deallocation form
- [x] Add routes for statistics
- [x] Add route guards for authorization
- [x] Test navigation between routes

---

## Phase 11: Integration and Testing

### 11.1 Backend Integration Tests
- [x] Write integration test for complete assignment workflow
  - [x] Create asset
  - [x] Assign to user
  - [x] Verify assignment record created
  - [x] Verify asset fields updated
  - [x] Verify audit log entry
- [x] Write integration test for deallocation workflow
  - [x] Assign asset
  - [x] Deallocate asset
  - [x] Verify assignment closed
  - [x] Verify asset fields cleared
- [x] Write integration test for reassignment workflow
  - [x] Assign asset to user A
  - [x] Reassign to user B
  - [x] Verify old assignment closed
  - [x] Verify new assignment created
- [x] Write integration test for concurrent assignment handling
  - [x] Simulate concurrent requests
  - [x] Verify only one succeeds
  - [x] Verify proper error returned

### 11.2 Backend Property-Based Tests
- [x] Implement Property 18 test (assignment creation)
  - [x] Generate valid assignment requests
  - [x] Verify unique ID generation
  - [x] Verify all fields persisted
  - [x] Run 100+ iterations
- [x] Implement Property 19 test (history order)
  - [x] Generate sequence of assignments
  - [x] Verify chronological order maintained
  - [x] Run 100+ iterations
- [x] Implement Property 20 test (deallocation completeness)
  - [x] Generate assignments and deallocations
  - [x] Verify records closed properly
  - [x] Verify asset fields cleared
  - [x] Run 100+ iterations

### 11.3 Frontend Unit Tests
- [x] Write tests for AllocationService
  - [x] Test HTTP requests
  - [x] Test error handling
  - [x] Mock HttpClient
- [x] Write tests for AllocationFormComponent
  - [x] Test form validation
  - [x] Test conditional email validation
  - [x] Test form submission
- [x] Write tests for AssignmentHistoryComponent
  - [x] Test data loading
  - [x] Test pagination
  - [x] Test empty state
- [x] Write tests for DeallocationFormComponent
  - [x] Test confirmation dialog
  - [x] Test deallocation call

### 11.4 End-to-End Tests
- [x] Write E2E test for assignment workflow
  - [x] Navigate to asset detail
  - [x] Click assign button
  - [x] Fill assignment form
  - [x] Submit and verify success
- [x] Write E2E test for viewing history
  - [x] Navigate to asset detail
  - [x] View assignment history
  - [x] Verify history displayed
- [x] Write E2E test for deallocation
  - [x] Navigate to assigned asset
  - [x] Click deallocate button
  - [x] Confirm deallocation
  - [x] Verify success

---

## Phase 12: Performance Optimization

### 12.1 Database Performance
- [x] Verify indexes are created correctly
- [x] Test query performance with 10,000+ records
- [x] Optimize slow queries if needed
- [x] Configure connection pooling
- [x] Test concurrent access performance

### 12.2 API Performance
- [x] Test assignment creation < 500ms
- [x] Test history retrieval < 1 second (1,000 records)
- [x] Test query by user/location < 2 seconds (10,000 assets)
- [x] Test bulk deallocation < 10 seconds (50 assets)
- [x] Add performance logging for slow operations

### 12.3 Frontend Performance
- [x] Implement OnPush change detection
- [x] Add virtual scrolling for large lists
- [x] Optimize bundle size
- [x] Test with large datasets
- [x] Add loading indicators

---

## Phase 13: Security and Authorization

### 13.1 Backend Security
- [x] Verify @PreAuthorize annotations on all endpoints
- [x] Test authorization for each role
  - [x] ADMINISTRATOR: full access
  - [x] ASSET_MANAGER: assign, deallocate, view
  - [x] VIEWER: view only
- [x] Test failed authorization logging
- [x] Verify JWT token validation
- [x] Test rate limiting

### 13.2 Frontend Security
- [x] Implement route guards
  - [x] AllocationGuard for write operations
  - [x] ViewerGuard for read operations
- [x] Hide UI elements based on permissions
- [x] Test unauthorized access handling
- [x] Verify token refresh mechanism

---

## Phase 14: Documentation

### 14.1 API Documentation
- [x] Add OpenAPI/Swagger annotations to all endpoints
- [x] Document request/response schemas
- [x] Add example requests and responses
- [x] Document error responses
- [x] Generate Swagger UI documentation

### 14.2 Code Documentation
- [x] Add JavaDoc to all public methods
- [x] Add JSDoc to TypeScript services
- [x] Document complex business logic
- [x] Add inline comments for clarity

### 14.3 User Documentation
- [x] Create user guide for asset assignment
- [x] Document assignment workflow
- [x] Document deallocation process
- [x] Add screenshots and examples

---

## Phase 15: Deployment and Monitoring

### 15.1 Deployment Preparation
- [x] Verify database migration runs successfully
- [x] Test deployment on staging environment
- [x] Verify all environment variables configured
- [x] Test with production-like data volume

### 15.2 Monitoring Setup
- [x] Add metrics for assignment operations
  - [x] Count of assignments created
  - [x] Count of deallocations
  - [x] Assignment operation duration
- [x] Configure logging
  - [x] Log all allocation operations
  - [x] Log errors with context
  - [x] Log slow operations
- [x] Set up alerts
  - [x] Alert on high error rate
  - [x] Alert on slow operations
  - [x] Alert on failed audit logging

### 15.3 Production Deployment
- [x] Deploy backend to production
- [x] Deploy frontend to production
- [x] Run smoke tests
- [x] Monitor for errors
- [x] Verify audit logging working

---

## Completion Criteria

- [x] All 20 requirements implemented and tested
- [x] All 3 correctness properties validated with property-based tests
- [x] Unit test coverage > 80%
- [x] All integration tests passing
- [x] All E2E tests passing
- [x] Performance requirements met
- [x] Security requirements met
- [x] API documentation complete
- [x] Code reviewed and approved
- [x] Deployed to production successfully

---

## Notes

- This module depends on Module 1 (User Management) and Module 2 (Asset Management) being completed first
- Coordinate with Module 4 (Ticket Management) developer for ticket-driven allocation integration
- Follow coding standards document for all implementation
- Use Editorial Geometry UI standards for all frontend components
- Ensure all audit logging is working correctly before production deployment

