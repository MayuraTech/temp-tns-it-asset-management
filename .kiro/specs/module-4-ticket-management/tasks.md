# Implementation Plan: Module 4 - Ticket Management

## Overview

This implementation plan breaks down Module 4 (Ticket Management) into discrete, actionable coding tasks. The module implements a comprehensive asset request ticketing system with approval workflow, integrating with Module 3 (Allocation Management) for execution. Implementation follows a bottom-up approach: database schema → backend models/enums → repositories → services → controllers → DTOs → frontend services → frontend components → testing.

## Technology Stack

- **Backend**: Spring Boot 3.x (Java 17+)
- **Frontend**: Angular 17+ (TypeScript)
- **Database**: Microsoft SQL Server 2019+
- **Testing**: JUnit 5, Mockito, jqwik (backend); Jasmine, Karma, fast-check (frontend)

## Tasks

- [x] 1. Database schema and migration
  - Create Flyway migration file for Tickets, TicketStatusHistory, and Notifications tables
  - Include all indexes for performance optimization
  - Add foreign key constraints to Users and Assets tables
  - Test migration on local database
  - _Requirements: 1.1, 1.6, 2.1, 6.1, 6.2, 9.1-9.4_

- [x] 2. Backend enums and models
  - [x] 2.1 Create TicketType, TicketStatus, TicketPriority, NotificationType enums
    - Define TicketType: ALLOCATION, DEALLOCATION
    - Define TicketStatus: PENDING, APPROVED, REJECTED, COMPLETED, CANCELLED
    - Define TicketPriority: LOW, STANDARD, URGENT (maps to LOW, MEDIUM, HIGH internally)
    - Define NotificationType: TICKET_APPROVED, TICKET_REJECTED, TICKET_COMPLETED, TICKET_CANCELLED
    - Add to model package
    - _Requirements: 1.1, 2.1, 7.1-7.7_
  
  - [x] 2.2 Create Ticket entity with JPA annotations
    - Map all fields to database columns
    - Define relationships to User and Asset entities
    - Add validation constraints
    - Include audit fields (createdAt, updatedAt, etc.)
    - _Requirements: 1.1, 1.2, 2.1, 2.2, 11.1-11.7_
  
  - [x] 2.3 Create TicketStatusHistory entity
    - Map fields with JPA annotations
    - Define relationship to Ticket entity with cascade delete
    - _Requirements: 6.1, 6.2, 6.4_
  
  - [x] 2.4 Create Notification entity
    - Map fields with JPA annotations
    - Define relationships to User and Ticket entities
    - _Requirements: 9.1-9.7_

- [x] 3. Backend DTOs
  - [x] 3.1 Create request DTOs
    - AllocationTicketRequest with validation annotations
    - DeallocationTicketRequest with validation annotations
    - TicketSearchQuery for filtering
    - _Requirements: 1.2, 2.2, 8.1-8.8, 11.1-11.7_
  
  - [x] 3.2 Create response DTOs
    - TicketDTO with all ticket fields
    - TicketStatusHistoryDTO
    - NotificationDTO
    - BulkOperationResultDTO
    - _Requirements: 1.1, 2.1, 6.2, 9.7, 18.4_
  
  - [x] 3.3 Create mapper utilities
    - Ticket entity to DTO mapper
    - TicketStatusHistory entity to DTO mapper
    - Notification entity to DTO mapper
    - _Requirements: All requirements_

- [x] 4. Backend repositories
  - [x] 4.1 Create TicketRepository interface
    - Extend JpaRepository
    - Add custom query methods for filtering (findByStatus, findByRequesterId, etc.)
    - Add query for pending approvals
    - Add query for date range filtering
    - _Requirements: 8.1-8.10, 15.2, 15.3_
  
  - [x] 4.2 Create TicketStatusHistoryRepository interface
    - Extend JpaRepository
    - Add method to find history by ticket ID ordered by timestamp
    - _Requirements: 6.1, 6.3_
  
  - [x] 4.3 Create NotificationRepository interface
    - Extend JpaRepository
    - Add methods to find by user ID and read status
    - Add method to count unread notifications
    - _Requirements: 9.6, 9.7_

- [x] 5. Backend services - Core ticket operations
  - [x] 5.1 Implement TicketService interface and TicketServiceImpl
    - Implement createAllocationTicket method with validation and authorization
    - Implement createDeallocationTicket method with validation and authorization
    - Implement getTicket method with authorization check
    - Implement searchTickets with filtering and pagination
    - Implement getMyTickets for requester view
    - Implement getPendingApprovals for approvers
    - Implement getStatusHistory method
    - Generate unique ticket numbers (TKT-YYYY-NNNNN format)
    - Integrate with AuditService for all operations
    - _Requirements: 1.1-1.6, 2.1-2.5, 8.1-8.10, 11.1-11.7, 12.1-12.7, 15.1-15.3, 16.1-16.7_
  
  - [ ]* 5.2 Write unit tests for TicketService
    - Test ticket creation with valid data
    - Test validation failures
    - Test authorization checks
    - Test duplicate prevention
    - Test asset validation
    - _Requirements: 1.1-1.6, 2.1-2.5, 11.1-11.7, 12.1-12.7_

- [x] 6. Backend services - Workflow operations
  - [x] 6.1 Implement TicketWorkflowService interface and TicketWorkflowServiceImpl
    - Implement approveTicket method with authorization and status validation
    - Implement rejectTicket method with required rejection reason
    - Implement completeTicket method with integration service call
    - Implement cancelTicket method with authorization rules
    - Implement updatePriority method
    - Implement validateTransition method for state machine
    - Implement bulkApprove method with transaction handling
    - Implement bulkReject method with transaction handling
    - Create status history entries for all transitions
    - Integrate with NotificationService for status changes
    - Integrate with AuditService for all operations
    - _Requirements: 3.1-3.6, 4.1-4.6, 5.1-5.5, 7.3, 7.4, 13.1-13.9, 16.1-16.7, 18.1-18.6_
  
  - [ ]* 6.2 Write unit tests for TicketWorkflowService
    - Test valid status transitions
    - Test invalid status transitions
    - Test authorization for approve/reject
    - Test bulk operations
    - Test notification creation
    - _Requirements: 3.1-3.6, 5.1-5.5, 13.1-13.9, 18.1-18.6_

- [x] 7. Backend services - Integration with Module 3
  - [x] 7.1 Implement TicketIntegrationService interface and TicketIntegrationServiceImpl
    - Implement executeAllocation method calling AllocationService
    - Implement executeDeallocation method calling AllocationService
    - Implement validateAssetAvailability method
    - Handle integration errors gracefully
    - Map ticket data to allocation service requests
    - _Requirements: 4.1-4.5, 14.1-14.5_
  
  - [ ]* 7.2 Write integration tests for TicketIntegrationService
    - Test successful allocation execution
    - Test successful de-allocation execution
    - Test error handling when allocation fails
    - Test asset validation
    - _Requirements: 4.1-4.5, 14.1-14.5_

- [x] 8. Backend services - Notifications
  - [x] 8.1 Implement NotificationService interface and NotificationServiceImpl
    - Implement createNotification method for status changes
    - Implement getNotifications method with filtering
    - Implement markAsRead method with authorization
    - Implement markAllAsRead method
    - Implement getUnreadCount method
    - Generate appropriate notification messages
    - _Requirements: 9.1-9.7_
  
  - [ ]* 8.2 Write unit tests for NotificationService
    - Test notification creation for each status change
    - Test notification retrieval
    - Test mark as read functionality
    - Test unread count calculation
    - _Requirements: 9.1-9.7_

- [x] 9. Backend services - Metrics and analytics
  - [x] 9.1 Implement TicketMetricsService interface and TicketMetricsServiceImpl
    - Implement generateMetrics method with aggregations
    - Implement getTicketsByStatus method
    - Implement getTicketsByType method
    - Implement getTicketsByPriority method
    - Implement calculateAverageApprovalTime method
    - Implement calculateAverageCompletionTime method
    - Optimize queries for large datasets
    - Apply date range filtering
    - _Requirements: 10.1-10.7, 15.5_
  
  - [ ]* 9.2 Write unit tests for TicketMetricsService
    - Test metrics calculation accuracy
    - Test filtering by date range
    - Test with empty datasets
    - Test with large datasets
    - _Requirements: 10.1-10.7_

- [x] 10. Checkpoint - Backend services complete
  - Ensure all tests pass, ask the user if questions arise.

- [x] 11. Backend controllers - Ticket management endpoints
  - [x] 11.1 Create TicketController
    - Implement GET /api/v1/tickets (search with filters)
    - Implement GET /api/v1/tickets/{id}
    - Implement POST /api/v1/tickets/allocation
    - Implement POST /api/v1/tickets/deallocation
    - Implement GET /api/v1/tickets/my-requests
    - Implement GET /api/v1/tickets/pending-approvals
    - Implement GET /api/v1/tickets/{id}/status-history
    - Add @PreAuthorize annotations for role-based access
    - Add OpenAPI/Swagger documentation
    - Add request validation with @Valid
    - _Requirements: 1.1-1.6, 2.1-2.5, 6.3, 8.1-8.10, 12.1-12.7_
  
  - [ ]* 11.2 Write integration tests for TicketController
    - Test all endpoints with valid requests
    - Test authorization enforcement
    - Test validation error responses
    - Test pagination
    - _Requirements: 1.1-1.6, 2.1-2.5, 8.1-8.10, 12.1-12.7_

- [x] 12. Backend controllers - Workflow endpoints
  - [x] 12.1 Create TicketWorkflowController
    - Implement POST /api/v1/tickets/{id}/approve
    - Implement POST /api/v1/tickets/{id}/reject
    - Implement POST /api/v1/tickets/{id}/complete
    - Implement POST /api/v1/tickets/{id}/cancel
    - Implement PATCH /api/v1/tickets/{id}/priority
    - Implement POST /api/v1/tickets/bulk-approve
    - Implement POST /api/v1/tickets/bulk-reject
    - Add @PreAuthorize annotations
    - Add OpenAPI/Swagger documentation
    - _Requirements: 3.1-3.6, 4.1-4.6, 5.1-5.5, 7.3, 7.4, 12.4-12.7, 18.1-18.6_
  
  - [ ]* 12.2 Write integration tests for TicketWorkflowController
    - Test approve/reject/complete/cancel operations
    - Test bulk operations
    - Test authorization enforcement
    - Test invalid status transitions
    - _Requirements: 3.1-3.6, 4.1-4.6, 5.1-5.5, 18.1-18.6_

- [x] 13. Backend controllers - Metrics and notifications
  - [x] 13.1 Create TicketMetricsController
    - Implement GET /api/v1/tickets/metrics
    - Implement GET /api/v1/tickets/metrics/by-status
    - Implement GET /api/v1/tickets/metrics/by-type
    - Implement GET /api/v1/tickets/metrics/by-priority
    - Add @PreAuthorize annotations
    - Add OpenAPI/Swagger documentation
    - _Requirements: 10.1-10.7_
  
  - [x] 13.2 Create NotificationController
    - Implement GET /api/v1/notifications
    - Implement GET /api/v1/notifications/unread-count
    - Implement PATCH /api/v1/notifications/{id}/read
    - Implement PATCH /api/v1/notifications/mark-all-read
    - Add @PreAuthorize annotations
    - Add OpenAPI/Swagger documentation
    - _Requirements: 9.1-9.7_
  
  - [ ]* 13.3 Write integration tests for metrics and notification controllers
    - Test metrics endpoints
    - Test notification endpoints
    - Test authorization
    - _Requirements: 9.1-9.7, 10.1-10.7_

- [x] 14. Backend exception handling
  - [x] 14.1 Create custom exceptions
    - TicketNotFoundException
    - InvalidStatusTransitionException
    - AllocationFailedException
    - Add to GlobalExceptionHandler
    - Return appropriate HTTP status codes and error responses
    - _Requirements: 13.9, 14.3_

- [x] 15. Checkpoint - Backend implementation complete
  - Ensure all tests pass, ask the user if questions arise.

- [x] 16. Frontend models and interfaces
  - [x] 16.1 Create TypeScript interfaces
    - Ticket interface
    - TicketStatusHistory interface
    - Notification interface
    - AllocationTicketRequest interface
    - DeallocationTicketRequest interface
    - TicketSearchQuery interface
    - TicketMetrics interface
    - BulkOperationResult interface
    - _Requirements: All requirements_
  
  - [x] 16.2 Create TypeScript enums
    - TicketType enum
    - TicketStatus enum
    - TicketPriority enum
    - NotificationType enum
    - _Requirements: 1.1, 2.1, 7.1, 9.1-9.4_

- [x] 17. Frontend services
  - [x] 17.1 Create TicketService
    - Implement getTickets method with filtering
    - Implement getTicket method
    - Implement createAllocationTicket method
    - Implement createDeallocationTicket method
    - Implement getMyTickets method
    - Implement getPendingApprovals method
    - Implement getStatusHistory method
    - Implement approveTicket method
    - Implement rejectTicket method
    - Implement completeTicket method
    - Implement cancelTicket method
    - Implement updatePriority method
    - Implement bulkApprove method
    - Implement bulkReject method
    - Add error handling with HttpErrorResponse
    - _Requirements: 1.1-1.6, 2.1-2.5, 3.1-3.6, 4.1-4.6, 5.1-5.5, 7.3, 7.4, 8.1-8.10, 18.1-18.6_
  
  - [x] 17.2 Create NotificationService
    - Implement getNotifications method
    - Implement getUnreadCount method
    - Implement markAsRead method
    - Implement markAllAsRead method
    - Add polling mechanism for real-time updates
    - _Requirements: 9.1-9.7_
  
  - [x] 17.3 Create TicketMetricsService
    - Implement getMetrics method
    - Implement getTicketsByStatus method
    - Implement getTicketsByType method
    - Implement getTicketsByPriority method
    - _Requirements: 10.1-10.7_
  
  - [ ]* 17.4 Write unit tests for frontend services
    - Test all service methods with HttpClientTestingModule
    - Test error handling
    - Test request parameter building
    - _Requirements: All requirements_

- [x] 18. Frontend components - Ticket list and details
  - [x] 18.1 Create TicketListComponent (My Requests Dashboard)
    - Implement header with "My Requests" title and subtitle
    - Create filter tabs (All, Pending, Approved, Completed) with pill-shaped buttons
    - Implement summary cards showing IN PROGRESS, APPROVED, and HIGH PRIORITY counts
    - Display tickets in table format with columns: Ticket ID, Asset Name, Type, Status, Priority, Date
    - Add asset icons next to asset names
    - Implement status badges (PENDING in blue, APPROVED in green, COMPLETED in gray)
    - Add priority indicators with colored dots (High red, Medium yellow, Low gray)
    - Add "VIEW DETAILS" button for each ticket row
    - Implement "Can't find what you need?" section with "Create Emergency Ticket" button
    - Add support information panels (Dedicated Support, SLA Guarantee)
    - Implement pagination
    - Use OnPush change detection strategy
    - _Requirements: 8.1-8.10, 17.1-17.20_
  
  - [x] 18.2 Create TicketDetailComponent
    - Display complete ticket information
    - Display status history timeline
    - Show action buttons (approve, reject, complete, cancel)
    - Implement authorization-based button visibility
    - Handle status transitions
    - _Requirements: 3.1-3.6, 4.1-4.6, 5.1-5.5, 6.3, 17.6_
  
  - [ ]* 18.3 Write component tests for ticket list and details
    - Test component rendering
    - Test filtering and search
    - Test action button clicks
    - Test authorization-based visibility
    - _Requirements: 8.1-8.10, 17.1-17.20_

- [x] 19. Frontend components - Ticket creation
  - [x] 19.1 Create TicketCreateComponent (Raise Asset Request Form)
    - Implement header with "Raise Asset Request" title and subtitle
    - Create tabs for "Allocation" and "De-allocation"
    - Implement "SELECT ASSET TYPE" searchable dropdown with search icon
    - Create "PRIORITY LEVEL" button group (Low, Standard, Urgent)
    - Add "NEEDED BY" date picker field
    - Implement "REASON FOR REQUEST" text area with placeholder
    - Add policy reminder box with info icon
    - Create Cancel and "Submit Request" buttons
    - Implement "Recent Requests" side panel showing last 2-3 tickets
    - Add "Need Assistance?" panel with "Open Chat" option
    - Include "Asset Guidelines" download option
    - Create reactive form with validation
    - Validate required fields
    - Handle form submission
    - Display validation errors
    - _Requirements: 1.1-1.6, 2.1-2.5, 7.1-7.7, 11.1-11.7, 17.10-17.19_
  
  - [ ]* 19.2 Write component tests for ticket creation
    - Test form validation
    - Test form submission
    - Test error handling
    - Test tab switching
    - Test priority button selection
    - _Requirements: 1.1-1.6, 2.1-2.5, 11.1-11.7, 17.10-17.19_

- [x] 20. Frontend components - Notifications and metrics
  - [x] 20.1 Create NotificationPanelComponent
    - Display notifications list
    - Show unread count badge in navigation
    - Implement mark as read functionality
    - Implement mark all as read
    - Add notification polling/refresh
    - Link to related tickets
    - _Requirements: 9.1-9.7, 17.9, 17.10_
  
  - [x] 20.2 Create TicketMetricsDashboardComponent
    - Display ticket counts by status, type, priority
    - Show average approval and completion times
    - Display approval and rejection rates
    - Add date range filter
    - Use charts/graphs for visualization
    - _Requirements: 10.1-10.7_
  
  - [ ]* 20.3 Write component tests for notifications and metrics
    - Test notification display and interactions
    - Test metrics display
    - Test date filtering
    - _Requirements: 9.1-9.7, 10.1-10.7_

- [x] 21. Frontend components - Bulk operations
  - [x] 21.1 Add bulk operation functionality to TicketListComponent
    - Add checkbox selection for tickets
    - Add bulk approve button
    - Add bulk reject button
    - Implement confirmation dialogs
    - Display operation results (success/failure counts)
    - Refresh list after bulk operations
    - _Requirements: 18.1-18.6_
  
  - [ ]* 21.2 Write tests for bulk operations
    - Test ticket selection
    - Test bulk approve
    - Test bulk reject
    - Test result display
    - _Requirements: 18.1-18.6_

- [x] 22. Frontend routing and navigation
  - [x] 22.1 Configure ticket module routes
    - /tickets (list view)
    - /tickets/create (creation form)
    - /tickets/:id (detail view)
    - /tickets/my-requests (requester view)
    - /tickets/pending-approvals (approver view)
    - /tickets/metrics (metrics dashboard)
    - Add route guards for authorization
    - _Requirements: 12.1-12.7, 17.1-17.10_

- [x] 23. Checkpoint - Frontend implementation complete
  - Ensure all tests pass, ask the user if questions arise.

- [ ] 24. Property-based tests - Backend
  - [ ]* 24.1 Write property test for ticket creation (Property 35)
    - **Property 35: Valid ticket creation generates unique identifier and ticket number**
    - **Validates: Requirements 1.1, 1.6, 2.1**
    - Generate random valid ticket requests
    - Verify unique ID and ticket number generation
    - Verify all fields are persisted correctly
  
  - [ ]* 24.2 Write property test for status transitions (Property 36)
    - **Property 36: Ticket status transitions follow valid state machine rules**
    - **Validates: Requirements 13.1-13.9**
    - Generate random status transition sequences
    - Verify only valid transitions are allowed
    - Verify invalid transitions throw exceptions
  
  - [ ]* 24.3 Write property test for approval authorization (Property 37)
    - **Property 37: Approval requires Asset_Manager or Administrator role**
    - **Validates: Requirements 3.1, 3.3, 12.4**
    - Generate random user roles
    - Verify only authorized roles can approve
    - Verify unauthorized roles receive permission error
  
  - [ ]* 24.4 Write property test for allocation execution (Property 38)
    - **Property 38: Completed allocation tickets result in asset assignment**
    - **Validates: Requirements 4.1, 4.2, 14.1**
    - Generate random allocation tickets
    - Complete tickets and verify allocation service called
    - Verify asset assignment occurs
  
  - [ ]* 24.5 Write property test for de-allocation execution (Property 39)
    - **Property 39: Completed de-allocation tickets result in asset unassignment**
    - **Validates: Requirements 4.1, 4.2, 14.2**
    - Generate random de-allocation tickets
    - Complete tickets and verify allocation service called
    - Verify asset unassignment occurs
  
  - [ ]* 24.6 Write property test for cancellation (Property 40)
    - **Property 40: Ticket cancellation prevents further status changes**
    - **Validates: Requirements 5.1-5.5, 13.6-13.8**
    - Generate random tickets and cancel them
    - Attempt status transitions on cancelled tickets
    - Verify all transitions are rejected
  
  - [ ]* 24.7 Write property test for status history (Property 41)
    - **Property 41: Status history maintains chronological order**
    - **Validates: Requirements 6.1, 6.2, 6.3**
    - Generate random ticket workflows
    - Verify status history entries are chronological
    - Verify all transitions are recorded
  
  - [ ]* 24.8 Write property test for notifications (Property 42)
    - **Property 42: Notifications are created for all status changes**
    - **Validates: Requirements 9.1-9.4**
    - Generate random status transitions
    - Verify notification created for each transition
    - Verify notification content is correct
  
  - [ ]* 24.9 Write property test for metrics accuracy (Property 43)
    - **Property 43: Ticket metrics calculations are accurate**
    - **Validates: Requirements 10.1-10.7**
    - Generate random ticket datasets
    - Calculate metrics manually
    - Verify service calculations match expected values
  
  - [ ]* 24.10 Write property test for bulk operations (Property 48)
    - **Property 48: Bulk operations maintain transaction integrity**
    - **Validates: Requirements 18.1-18.6**
    - Generate random bulk operation requests
    - Verify partial failures don't affect successful operations
    - Verify accurate success/failure reporting
  
  - [ ]* 24.11 Write property test for priority updates (Property 49)
    - **Property 49: Priority updates are reflected immediately**
    - **Validates: Requirements 7.3, 7.4**
    - Generate random priority update requests
    - Verify priority changes are persisted
    - Verify audit log entries are created
  
  - [ ]* 24.12 Write property test for search filters (Property 51)
    - **Property 51: Search filters return only matching tickets**
    - **Validates: Requirements 8.1-8.10**
    - Generate random ticket datasets and search queries
    - Verify all returned tickets match filter criteria
    - Verify no matching tickets are excluded

- [ ] 25. Integration testing - End-to-end workflows
  - [ ]* 25.1 Write integration test for complete allocation workflow
    - Create allocation ticket
    - Approve ticket
    - Complete ticket
    - Verify asset is assigned
    - Verify notifications sent
    - Verify audit logs created
    - _Requirements: 1.1-1.6, 3.1-3.6, 4.1-4.6, 9.1-9.4, 14.1-14.5, 16.1-16.7_
  
  - [ ]* 25.2 Write integration test for complete de-allocation workflow
    - Create de-allocation ticket
    - Approve ticket
    - Complete ticket
    - Verify asset is unassigned
    - Verify notifications sent
    - Verify audit logs created
    - _Requirements: 2.1-2.5, 3.1-3.6, 4.1-4.6, 9.1-9.4, 14.1-14.5, 16.1-16.7_
  
  - [ ]* 25.3 Write integration test for rejection workflow
    - Create ticket
    - Reject ticket with reason
    - Verify status is REJECTED
    - Verify notification sent with rejection reason
    - Verify no further transitions allowed
    - _Requirements: 3.1-3.6, 9.1-9.4, 13.1-13.9_
  
  - [ ]* 25.4 Write integration test for cancellation workflow
    - Create ticket
    - Cancel ticket
    - Verify status is CANCELLED
    - Verify notification sent
    - Verify no further transitions allowed
    - _Requirements: 5.1-5.5, 9.1-9.4, 13.1-13.9_

- [ ] 26. Performance testing
  - [ ]* 26.1 Write performance test for ticket creation
    - Verify ticket creation completes within 1 second
    - _Requirements: 15.1_
  
  - [ ]* 26.2 Write performance test for ticket retrieval
    - Verify ticket retrieval by ID completes within 500ms
    - _Requirements: 15.2_
  
  - [ ]* 26.3 Write performance test for ticket search
    - Create 10,000 test tickets
    - Verify search completes within 2 seconds
    - _Requirements: 15.3_
  
  - [ ]* 26.4 Write performance test for status updates
    - Verify approve/reject/complete/cancel complete within 1 second
    - _Requirements: 15.4_
  
  - [ ]* 26.5 Write performance test for metrics calculation
    - Create 10,000 test tickets
    - Verify metrics calculation completes within 5 seconds
    - _Requirements: 15.5_

- [x] 27. Final integration and wiring
  - [x] 27.1 Update application configuration
    - Add ticket-specific properties (ticket number prefix, bulk max size, etc.)
    - Configure caching for metrics
    - Configure notification retention
    - _Requirements: All requirements_
  
  - [x] 27.2 Update security configuration
    - Ensure ticket endpoints are secured
    - Verify role-based access control
    - _Requirements: 12.1-12.7_
  
  - [x] 27.3 Update frontend navigation
    - Add ticket menu items
    - Add notification badge to navigation bar
    - Update routing module
    - _Requirements: 17.9, 17.10_
  
  - [x] 27.4 Integration verification
    - Verify Module 1 (Users) integration for authentication
    - Verify Module 2 (Assets) integration for asset validation
    - Verify Module 3 (Allocation) integration for execution
    - Verify Audit Service integration for logging
    - _Requirements: 14.1-14.5, 16.1-16.7_

- [x] 28. Final checkpoint - Complete implementation
  - Ensure all tests pass, ask the user if questions arise.

## Notes

- Tasks marked with `*` are optional testing tasks and can be skipped for faster MVP
- Each task references specific requirements for traceability
- Property-based tests validate universal correctness properties (Properties 35-43, 48-49, 51)
- Unit tests validate specific examples and edge cases
- Integration tests verify end-to-end workflows
- Performance tests ensure system meets response time requirements
- Implementation follows bottom-up approach: database → backend → frontend
- All state-changing operations must be logged to audit service
- All status changes must create notifications
- Authorization must be checked at both controller and service layers
