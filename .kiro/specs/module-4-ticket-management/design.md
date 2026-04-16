
@@ -0,0 +1,1018 @@
# Design Document: Module 4 - Ticket Management

## Overview

Module 4 implements a comprehensive asset request ticketing system with approval workflow for the IT Infrastructure Asset Management application. The module provides structured processes for requesting asset allocations and de-allocations, multi-level approval workflows, complete status tracking, priority management, notification capabilities, and metrics/analytics. The system integrates seamlessly with Module 3 (Allocation Management) to execute approved requests and maintains complete audit trails for compliance.

### Technology Stack

- **Backend**: Spring Boot 3.x (Java 17+)
- **Frontend**: Angular 17+ (TypeScript)
- **Database**: Microsoft SQL Server 2019+
- **Authentication**: Spring Security with JWT
- **ORM**: Spring Data JPA with Hibernate

### Core Capabilities

- Create allocation and de-allocation request tickets
- Multi-level approval workflow (approve/reject)
- Ticket completion with automatic allocation/de-allocation execution
- Ticket cancellation by requesters and managers
- Complete status history tracking
- Priority management (Low, Medium, High, Critical)
- Real-time notification system
- Advanced search and filtering
- Ticket metrics and analytics
- Bulk operations for efficient processing
- Integration with Module 3 for asset assignment

### Design Principles

1. **Workflow Integrity**: Enforce valid status transitions and business rules
2. **Audit Everything**: Complete audit trail for all ticket operations
3. **Authorization First**: Check permissions before all operations
4. **Integration Ready**: Seamless integration with allocation module
5. **Performance**: Sub-second response times for ticket operations
6. **User Experience**: Intuitive interface with real-time updates

## Architecture

### Component Architecture

```
┌─────────────────────────────────────────────────────────┐
│                    Presentation Layer                    │
│         (Angular Components / REST Controllers)          │
└─────────────────────────────────────────────────────────┘
                          │
┌─────────────────────────────────────────────────────────┐
│                   Application Layer                      │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐  │
│  │Ticket Service│  │Notification  │  │  Metrics     │  │
│  │              │  │   Service    │  │  Service     │  │
│  └──────────────┘  └──────────────┘  └──────────────┘  │
│  ┌──────────────┐  ┌──────────────┐                    │
│  │   Workflow   │  │ Integration  │                    │
│  │   Service    │  │   Service    │                    │
│  └──────────────┘  └──────────────┘                    │
└─────────────────────────────────────────────────────────┘
                          │
┌─────────────────────────────────────────────────────────┐
│                     Domain Layer                         │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐  │
│  │Ticket Domain │  │ Notification │  │Status History│  │
│  │              │  │   Domain     │  │   Domain     │  │
│  └──────────────┘  └──────────────┘  └──────────────┘  │
└─────────────────────────────────────────────────────────┘
                          │
┌─────────────────────────────────────────────────────────┐
│                  Data Access Layer                       │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐  │
│  │Ticket Repo   │  │Notification  │  │Status History│  │
│  │              │  │    Repo      │  │    Repo      │  │
│  └──────────────┘  └──────────────┘  └──────────────┘  │
└─────────────────────────────────────────────────────────┘
                          │
┌─────────────────────────────────────────────────────────┐
│                   Persistence Layer                      │
│              (MS SQL Server Database)                    │
└─────────────────────────────────────────────────────────┘
```

### Integration Points

```
Module 4 (Tickets) → Module 1 (Users): RequesterId, ApproverId
Module 4 (Tickets) → Module 2 (Assets): AssetId validation
Module 4 (Tickets) → Module 3 (Allocation): Execute allocation/de-allocation
Module 4 (Tickets) → Audit Service: Log all operations
Module 4 (Tickets) → Module 5 (Reporting): Provide metrics data
```

## Components and Interfaces

### Ticket Service

Core business logic for ticket management operations.

**Interface:**
```typescript
interface TicketService {
  // Create allocation ticket
  createAllocationTicket(userId: string, request: AllocationTicketRequest): Result<Ticket, TicketError>
  
  // Create de-allocation ticket
  createDeallocationTicket(userId: string, request: DeallocationTicketRequest): Result<Ticket, TicketError>
  
  // Get ticket by ID
  getTicket(ticketId: string): Result<Ticket, TicketError>
  
  // Search tickets with filters
  searchTickets(query: TicketSearchQuery, pageable: Pageable): Result<Page<Ticket>, TicketError>
  
  // Get tickets for requester
  getMyTickets(userId: string, pageable: Pageable): Result<Page<Ticket>, TicketError>
  
  // Get pending approvals
  getPendingApprovals(pageable: Pageable): Result<Page<Ticket>, TicketError>
  
  // Get ticket status history
  getStatusHistory(ticketId: string): Result<TicketStatusHistory[], TicketError>
}

interface AllocationTicketRequest {
  assetId: string
  assignToUser?: string
  assignToUserEmail?: string
  assignToLocation?: string
  requestReason: string
  priority: TicketPriority
}

interface DeallocationTicketRequest {
  assetId: string
  deallocationReason: string
  priority: TicketPriority
}

interface TicketSearchQuery {
  status?: TicketStatus[]
  type?: TicketType[]
  priority?: TicketPriority[]
  requesterId?: string
  approverId?: string
  assetId?: string
  createdFrom?: Date
  createdTo?: Date
  sortBy?: string
  sortOrder?: 'ASC' | 'DESC'
}

type TicketError =
  | { type: 'TICKET_NOT_FOUND', ticketId: string }
  | { type: 'ASSET_NOT_FOUND', assetId: string }
  | { type: 'ASSET_NOT_AVAILABLE' }
  | { type: 'ASSET_NOT_ASSIGNED' }
  | { type: 'VALIDATION_FAILED', errors: ValidationError[] }
  | { type: 'INSUFFICIENT_PERMISSIONS' }
```

**Responsibilities:**
- Create and validate allocation/de-allocation tickets
- Retrieve tickets with authorization checks
- Search and filter tickets
- Coordinate with workflow service for status changes
- Integrate with asset service for validation

### Ticket Workflow Service

Manages ticket status transitions and approval workflow.

**Interface:**
```typescript
interface TicketWorkflowService {
  // Approve ticket
  approveTicket(approverId: string, ticketId: string, comments?: string): Result<Ticket, WorkflowError>
  
  // Reject ticket
  rejectTicket(approverId: string, ticketId: string, reason: string): Result<Ticket, WorkflowError>
  
  // Complete ticket (execute allocation/de-allocation)
  completeTicket(userId: string, ticketId: string): Result<Ticket, WorkflowError>
  
  // Cancel ticket
  cancelTicket(userId: string, ticketId: string): Result<Ticket, WorkflowError>
  
  // Update ticket priority
  updatePriority(userId: string, ticketId: string, priority: TicketPriority): Result<Ticket, WorkflowError>
  
  // Validate status transition
  validateTransition(currentStatus: TicketStatus, newStatus: TicketStatus): boolean
  
  // Bulk approve tickets
  bulkApprove(approverId: string, ticketIds: string[], comments?: string): Result<BulkOperationResult, WorkflowError>
  
  // Bulk reject tickets
  bulkReject(approverId: string, ticketIds: string[], reason: string): Result<BulkOperationResult, WorkflowError>
}

interface BulkOperationResult {
  successCount: number
  failureCount: number
  failures: Array<{
    ticketId: string
    error: string
  }>
}

type WorkflowError =
  | { type: 'INVALID_STATUS_TRANSITION', from: TicketStatus, to: TicketStatus }
  | { type: 'TICKET_NOT_FOUND', ticketId: string }
  | { type: 'INSUFFICIENT_PERMISSIONS' }
  | { type: 'ALLOCATION_FAILED', reason: string }
```

**Responsibilities:**
- Enforce valid status transitions
- Execute approval and rejection operations
- Complete tickets by invoking allocation service
- Handle cancellation requests
- Manage bulk operations
- Create status history entries

### Ticket Integration Service

Handles integration with Module 3 (Allocation Management).

**Interface:**
```typescript
interface TicketIntegrationService {
  // Execute allocation for approved ticket
  executeAllocation(ticket: Ticket, executorId: string): Result<void, IntegrationError>
  
  // Execute de-allocation for approved ticket
  executeDeallocation(ticket: Ticket, executorId: string): Result<void, IntegrationError>
  
  // Validate asset availability
  validateAssetAvailability(assetId: string, ticketType: TicketType): Result<boolean, IntegrationError>
}

type IntegrationError =
  | { type: 'ASSET_NOT_FOUND', assetId: string }
  | { type: 'ASSET_NOT_AVAILABLE' }
  | { type: 'ASSET_NOT_ASSIGNED' }
  | { type: 'ALLOCATION_SERVICE_ERROR', message: string }
```

**Responsibilities:**
- Invoke allocation service for ticket completion
- Validate asset availability before ticket creation
- Handle integration errors gracefully
- Map ticket data to allocation service requests

### Notification Service

Manages notifications for ticket status changes.

**Interface:**
```typescript
interface NotificationService {
  // Create notification for ticket status change
  createNotification(ticket: Ticket, statusChange: StatusChange): Result<Notification, NotificationError>
  
  // Get notifications for user
  getNotifications(userId: string, unreadOnly: boolean): Result<Notification[], NotificationError>
  
  // Mark notification as read
  markAsRead(userId: string, notificationId: string): Result<void, NotificationError>
  
  // Mark all notifications as read
  markAllAsRead(userId: string): Result<void, NotificationError>
  
  // Get unread count
  getUnreadCount(userId: string): number
}

interface Notification {
  id: string
  userId: string
  ticketId: string
  ticketNumber: string
  assetName: string
  message: string
  type: NotificationType
  read: boolean
  createdAt: Date
}

enum NotificationType {
  TICKET_APPROVED = 'TICKET_APPROVED',
  TICKET_REJECTED = 'TICKET_REJECTED',
  TICKET_COMPLETED = 'TICKET_COMPLETED',
  TICKET_CANCELLED = 'TICKET_CANCELLED'
}

type NotificationError =
  | { type: 'NOTIFICATION_NOT_FOUND', notificationId: string }
  | { type: 'INSUFFICIENT_PERMISSIONS' }
```

**Responsibilities:**
- Create notifications for status changes
- Deliver notifications to requesters
- Manage read/unread status
- Provide unread count for UI badge

### Ticket Metrics Service

Generates metrics and analytics for ticket system.

**Interface:**
```typescript
interface TicketMetricsService {
  // Generate comprehensive ticket metrics
  generateMetrics(filters: MetricsFilter): Result<TicketMetrics, MetricsError>
  
  // Get tickets by status
  getTicketsByStatus(filters: MetricsFilter): Result<Map<TicketStatus, number>, MetricsError>
  
  // Get tickets by type
  getTicketsByType(filters: MetricsFilter): Result<Map<TicketType, number>, MetricsError>
  
  // Get tickets by priority
  getTicketsByPriority(filters: MetricsFilter): Result<Map<TicketPriority, number>, MetricsError>
  
  // Calculate average approval time
  calculateAverageApprovalTime(filters: MetricsFilter): Result<number, MetricsError>
  
  // Calculate average completion time
  calculateAverageCompletionTime(filters: MetricsFilter): Result<number, MetricsError>
}

interface MetricsFilter {
  dateFrom?: Date
  dateTo?: Date
  requesterId?: string
  approverId?: string
}

interface TicketMetrics {
  totalTickets: number
  ticketsByStatus: Map<TicketStatus, number>
  ticketsByType: Map<TicketType, number>
  ticketsByPriority: Map<TicketPriority, number>
  averageApprovalTimeHours: number
  averageCompletionTimeHours: number
  approvalRate: number
  rejectionRate: number
}

type MetricsError =
  | { type: 'CALCULATION_FAILED', reason: string }
```

**Responsibilities:**
- Aggregate ticket data for metrics
- Calculate approval and completion times
- Compute approval and rejection rates
- Optimize queries for large datasets
- Complete calculations within 5 seconds

## Data Models

### Ticket

```typescript
interface Ticket {
  id: string // UUID
  ticketNumber: string // Human-readable (TKT-2024-00001)
  type: TicketType
  status: TicketStatus
  priority: TicketPriority
  
  // Asset information
  assetId: string
  assetName: string
  assetSerialNumber: string
  
  // Requester information
  requesterId: string
  requesterName: string
  
  // Assignment details (for allocation tickets)
  assignToUser?: string
  assignToUserEmail?: string
  assignToLocation?: string
  
  // Request details
  requestReason?: string
  deallocationReason?: string
  
  // Approval information
  approverId?: string
  approverName?: string
  approvalComments?: string
  rejectionReason?: string
  
  // Timestamps
  createdAt: Date
  updatedAt: Date
  approvedAt?: Date
  rejectedAt?: Date
  completedAt?: Date
  cancelledAt?: Date
}

enum TicketType {
  ALLOCATION = 'ALLOCATION',
  DEALLOCATION = 'DEALLOCATION'
}

enum TicketStatus {
  PENDING = 'PENDING',
  APPROVED = 'APPROVED',
  REJECTED = 'REJECTED',
  COMPLETED = 'COMPLETED',
  CANCELLED = 'CANCELLED'
}

enum TicketPriority {
  LOW = 'LOW',
  STANDARD = 'STANDARD',  // Maps to MEDIUM internally
  URGENT = 'URGENT'       // Maps to HIGH internally
}
```

### Ticket Status History

```typescript
interface TicketStatusHistory {
  id: string
  ticketId: string
  fromStatus: TicketStatus | null // null for initial creation
  toStatus: TicketStatus
  changedBy: string
  changedByName: string
  changedAt: Date
  comments?: string
}
```

### Notification

```typescript
interface Notification {
  id: string
  userId: string
  ticketId: string
  ticketNumber: string
  assetName: string
  message: string
  type: NotificationType
  read: boolean
  createdAt: Date
}
```

## Database Schema

### Tickets Table

```sql
CREATE TABLE Tickets (
  Id UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
  TicketNumber NVARCHAR(50) NOT NULL UNIQUE,
  Type NVARCHAR(20) NOT NULL,
  Status NVARCHAR(20) NOT NULL,
  Priority NVARCHAR(20) NOT NULL,
  
  -- Asset information
  AssetId UNIQUEIDENTIFIER NOT NULL,
  AssetName NVARCHAR(255) NOT NULL,
  AssetSerialNumber NVARCHAR(100) NOT NULL,
  
  -- Requester information
  RequesterId UNIQUEIDENTIFIER NOT NULL,
  RequesterName NVARCHAR(255) NOT NULL,
  
  -- Assignment details
  AssignToUser NVARCHAR(255) NULL,
  AssignToUserEmail NVARCHAR(255) NULL,
  AssignToLocation NVARCHAR(255) NULL,
  
  -- Request details
  RequestReason NVARCHAR(MAX) NULL,
  DeallocationReason NVARCHAR(MAX) NULL,
  
  -- Approval information
  ApproverId UNIQUEIDENTIFIER NULL,
  ApproverName NVARCHAR(255) NULL,
  ApprovalComments NVARCHAR(MAX) NULL,
  RejectionReason NVARCHAR(MAX) NULL,
  
  -- Timestamps
  CreatedAt DATETIME2 NOT NULL DEFAULT GETUTCDATE(),
  UpdatedAt DATETIME2 NOT NULL DEFAULT GETUTCDATE(),
  ApprovedAt DATETIME2 NULL,
  RejectedAt DATETIME2 NULL,
  CompletedAt DATETIME2 NULL,
  CancelledAt DATETIME2 NULL,
  
  CONSTRAINT FK_Tickets_AssetId FOREIGN KEY (AssetId) REFERENCES Assets(Id),
  CONSTRAINT FK_Tickets_RequesterId FOREIGN KEY (RequesterId) REFERENCES Users(Id),
  CONSTRAINT FK_Tickets_ApproverId FOREIGN KEY (ApproverId) REFERENCES Users(Id),
  CONSTRAINT CHK_Tickets_Type CHECK (Type IN ('ALLOCATION', 'DEALLOCATION')),
  CONSTRAINT CHK_Tickets_Status CHECK (Status IN ('PENDING', 'APPROVED', 'REJECTED', 'COMPLETED', 'CANCELLED')),
  CONSTRAINT CHK_Tickets_Priority CHECK (Priority IN ('LOW', 'MEDIUM', 'HIGH', 'CRITICAL')),
  CONSTRAINT CHK_Tickets_Assignment CHECK (
    (Type = 'ALLOCATION' AND (AssignToUser IS NOT NULL OR AssignToLocation IS NOT NULL)) OR
    (Type = 'DEALLOCATION')
  )
);

CREATE INDEX IX_Tickets_TicketNumber ON Tickets(TicketNumber);
CREATE INDEX IX_Tickets_Status ON Tickets(Status);
CREATE INDEX IX_Tickets_Type ON Tickets(Type);
CREATE INDEX IX_Tickets_Priority ON Tickets(Priority);
CREATE INDEX IX_Tickets_AssetId ON Tickets(AssetId);
CREATE INDEX IX_Tickets_RequesterId ON Tickets(RequesterId);
CREATE INDEX IX_Tickets_ApproverId ON Tickets(ApproverId);
CREATE INDEX IX_Tickets_CreatedAt ON Tickets(CreatedAt);
CREATE INDEX IX_Tickets_Status_Priority ON Tickets(Status, Priority);
```

### TicketStatusHistory Table

```sql
CREATE TABLE TicketStatusHistory (
  Id UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
  TicketId UNIQUEIDENTIFIER NOT NULL,
  FromStatus NVARCHAR(20) NULL,
  ToStatus NVARCHAR(20) NOT NULL,
  ChangedBy UNIQUEIDENTIFIER NOT NULL,
  ChangedByName NVARCHAR(255) NOT NULL,
  ChangedAt DATETIME2 NOT NULL DEFAULT GETUTCDATE(),
  Comments NVARCHAR(MAX) NULL,
  
  CONSTRAINT FK_TicketStatusHistory_TicketId FOREIGN KEY (TicketId) REFERENCES Tickets(Id) ON DELETE CASCADE,
  CONSTRAINT FK_TicketStatusHistory_ChangedBy FOREIGN KEY (ChangedBy) REFERENCES Users(Id),
  CONSTRAINT CHK_TicketStatusHistory_ToStatus CHECK (ToStatus IN ('PENDING', 'APPROVED', 'REJECTED', 'COMPLETED', 'CANCELLED'))
);

CREATE INDEX IX_TicketStatusHistory_TicketId ON TicketStatusHistory(TicketId);
CREATE INDEX IX_TicketStatusHistory_ChangedAt ON TicketStatusHistory(ChangedAt);
```

### Notifications Table

```sql
CREATE TABLE Notifications (
  Id UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
  UserId UNIQUEIDENTIFIER NOT NULL,
  TicketId UNIQUEIDENTIFIER NOT NULL,
  TicketNumber NVARCHAR(50) NOT NULL,
  AssetName NVARCHAR(255) NOT NULL,
  Message NVARCHAR(500) NOT NULL,
  Type NVARCHAR(50) NOT NULL,
  IsRead BIT NOT NULL DEFAULT 0,
  CreatedAt DATETIME2 NOT NULL DEFAULT GETUTCDATE(),
  
  CONSTRAINT FK_Notifications_UserId FOREIGN KEY (UserId) REFERENCES Users(Id) ON DELETE CASCADE,
  CONSTRAINT FK_Notifications_TicketId FOREIGN KEY (TicketId) REFERENCES Tickets(Id) ON DELETE CASCADE,
  CONSTRAINT CHK_Notifications_Type CHECK (Type IN ('TICKET_APPROVED', 'TICKET_REJECTED', 'TICKET_COMPLETED', 'TICKET_CANCELLED'))
);

CREATE INDEX IX_Notifications_UserId ON Notifications(UserId);
CREATE INDEX IX_Notifications_IsRead ON Notifications(IsRead);
CREATE INDEX IX_Notifications_CreatedAt ON Notifications(CreatedAt);
CREATE INDEX IX_Notifications_UserId_IsRead ON Notifications(UserId, IsRead);
```

## API Endpoints

### Ticket Management Endpoints

```
GET    /api/v1/tickets
GET    /api/v1/tickets/{id}
POST   /api/v1/tickets/allocation
POST   /api/v1/tickets/deallocation
GET    /api/v1/tickets/my-requests
GET    /api/v1/tickets/pending-approvals
GET    /api/v1/tickets/{id}/status-history
```

### Workflow Endpoints

```
POST   /api/v1/tickets/{id}/approve
POST   /api/v1/tickets/{id}/reject
POST   /api/v1/tickets/{id}/complete
POST   /api/v1/tickets/{id}/cancel
PATCH  /api/v1/tickets/{id}/priority
POST   /api/v1/tickets/bulk-approve
POST   /api/v1/tickets/bulk-reject
```

### Metrics Endpoints

```
GET    /api/v1/tickets/metrics
GET    /api/v1/tickets/metrics/by-status
GET    /api/v1/tickets/metrics/by-type
GET    /api/v1/tickets/metrics/by-priority
```

### Notification Endpoints

```
GET    /api/v1/notifications
GET    /api/v1/notifications/unread-count
PATCH  /api/v1/notifications/{id}/read
PATCH  /api/v1/notifications/mark-all-read
```

## Frontend Component Specifications

### My Requests Dashboard Component

**Component**: `TicketListComponent` (My Requests Dashboard)

**Layout Structure**:
```
┌─────────────────────────────────────────────────────────────┐
│ Header: "My Requests"                                        │
│ Subtitle: "Track and manage your ongoing asset and support  │
│           requests"                                          │
│                                                              │
│ [All] [Pending] [Approved] [Completed]  ← Filter Tabs       │
├─────────────────────────────────────────────────────────────┤
│ ┌──────────────┐  ┌──────────────┐  ┌──────────────┐       │
│ │ IN PROGRESS  │  │   APPROVED   │  │ HIGH PRIORITY│       │
│ │      04      │  │      12      │  │      01      │       │
│ └──────────────┘  └──────────────┘  └──────────────┘       │
├─────────────────────────────────────────────────────────────┤
│ Table:                                                       │
│ ┌────────┬──────────┬──────┬────────┬─────────┬──────┬────┐│
│ │TICKET  │ASSET NAME│ TYPE │ STATUS │PRIORITY │ DATE │    ││
│ │  ID    │          │      │        │         │      │    ││
│ ├────────┼──────────┼──────┼────────┼─────────┼──────┼────┤│
│ │#TK-8821│MacBook   │Procur│PENDING │● High   │Oct 24│VIEW││
│ │        │Pro M3    │ement │        │         │      │    ││
│ ├────────┼──────────┼──────┼────────┼─────────┼──────┼────┤│
│ │#TK-7742│Dell Ultra│Maint │APPROVED│● Medium │Oct 21│VIEW││
│ │        │Sharp 27" │enance│        │         │      │    ││
│ └────────┴──────────┴──────┴────────┴─────────┴──────┴────┘│
├─────────────────────────────────────────────────────────────┤
│ ┌─────────────────────────────────────────────────────────┐ │
│ │ Can't find what you need?                               │ │
│ │                                                         │ │
│ │        [⚡ Create Emergency Ticket]                     │ │
│ └─────────────────────────────────────────────────────────┘ │
│                                                              │
│ Support Panels:                                              │
│ • Dedicated Support                                          │
│ • SLA Guarantee                                              │
└─────────────────────────────────────────────────────────────┘
```

**Visual Design Elements**:
- **Header**: Large bold "My Requests" title with gray subtitle
- **Filter Tabs**: Pill-shaped buttons, active tab in dark blue
- **Summary Cards**: 
  - White background with colored accents
  - Large numbers with descriptive labels
  - Icons for each metric
- **Table**:
  - Clean rows with subtle borders
  - Asset icons next to names
  - Status badges: PENDING (blue), APPROVED (green), COMPLETED (gray)
  - Priority dots: High (red), Medium (yellow), Low (gray)
  - "VIEW DETAILS" link button on each row
- **Emergency Section**: Dark blue background with red CTA button
- **Support Panels**: Light background with icons and descriptions

### Raise Asset Request Component

**Component**: `TicketCreateComponent` (Raise Asset Request Form)

**Layout Structure**:
```
┌─────────────────────────────────────────────────────────────┐
│ Header: "Raise Asset Request"                                │
│ Subtitle: "Initiate a formal request for hardware, software,│
│           or workspace tools."                               │
│                                                              │
│ [Allocation] [De-allocation]  ← Tabs                        │
├─────────────────────────────────────────────────────────────┤
│ SELECT ASSET TYPE                                            │
│ ┌───────────────────────────────────────────────────────┐   │
│ │ 🔍 Search for laptop, monitor, mobile...              │   │
│ └───────────────────────────────────────────────────────┘   │
│                                                              │
│ PRIORITY LEVEL          NEEDED BY                           │
│ [Low] [Standard] [Urgent]   [mm/dd/yyyy]                    │
│                                                              │
│ REASON FOR REQUEST                                           │
│ ┌───────────────────────────────────────────────────────┐   │
│ │ Describe why you need this asset (e.g., New project  │   │
│ │ requirement, Hardware failure...)                     │   │
│ │                                                       │   │
│ │                                                       │   │
│ └───────────────────────────────────────────────────────┘   │
│                                                              │
│ ⓘ Policy Reminder                                           │
│ Requests for High-tier hardware require departmental head   │
│ approval. Standard processing time is 3-5 business days.    │
│                                                              │
│                              [Cancel] [Submit Request ▶]    │
├─────────────────────────────────────────────────────────────┤
│ Side Panels:                                                 │
│ • Recent Requests (last 2-3 tickets)                        │
│ • Need Assistance? (Open Chat option)                       │
│ • Asset Guidelines (Download PDF)                           │
└─────────────────────────────────────────────────────────────┘
```

**Visual Design Elements**:
- **Header**: Large bold title with descriptive subtitle
- **Tabs**: Allocation (active) and De-allocation options
- **Form Fields**:
  - Searchable dropdown with icon
  - Priority buttons: outlined style, selected state in blue
  - Date picker with calendar icon
  - Large text area for reason
- **Policy Box**: Light background with info icon
- **Buttons**: 
  - Cancel: outlined/ghost style
  - Submit: solid blue with arrow icon
- **Side Panels**: 
  - Recent Requests with status indicators
  - Chat assistance with icon
  - PDF download option

## Business Rules

### Ticket Creation Rules

1. Asset must exist and be accessible
2. For allocation tickets: Asset must not be RETIRED
3. For de-allocation tickets: Asset must be currently assigned
4. Request reason must be 10-1000 characters
5. At least one of assignToUser or assignToLocation required for allocation
6. Email format validation for assignToUserEmail
7. Priority levels: LOW, STANDARD (default), URGENT
8. NEEDED BY date must be present or future date

### Status Transition Rules

```
PENDING → APPROVED (by Asset_Manager or Administrator)
PENDING → REJECTED (by Asset_Manager or Administrator)
PENDING → CANCELLED (by Requester, Asset_Manager, or Administrator)
APPROVED → COMPLETED (by Asset_Manager or Administrator)
APPROVED → CANCELLED (by Asset_Manager or Administrator)
COMPLETED → (no transitions allowed)
REJECTED → (no transitions allowed)
CANCELLED → (no transitions allowed)
```

### Authorization Rules

| Operation | Viewer | Asset_Manager | Administrator |
|-----------|--------|---------------|---------------|
| Create Ticket | ✓ | ✓ | ✓ |
| View Own Tickets | ✓ | ✓ | ✓ |
| View All Tickets | ✗ | ✓ | ✓ |
| Approve/Reject | ✗ | ✓ | ✓ |
| Complete | ✗ | ✓ | ✓ |
| Cancel Own Pending | ✓ | ✓ | ✓ |
| Cancel Any | ✗ | ✓ | ✓ |
| Update Priority | ✗ | ✓ | ✓ |
| Bulk Operations | ✗ | ✓ | ✓ |

## Workflow State Machine

```
                    ┌─────────┐
                    │ PENDING │
                    └────┬────┘
                         │
          ┌──────────────┼──────────────┐
          │              │              │
          ▼              ▼              ▼
    ┌──────────┐   ┌──────────┐   ┌───────────┐
    │ APPROVED │   │ REJECTED │   │ CANCELLED │
    └────┬─────┘   └──────────┘   └───────────┘
         │              │              │
         │              │              │
         ▼              ▼              ▼
    ┌───────────┐   (Terminal)    (Terminal)
    │ COMPLETED │
    └───────────┘
         │
         ▼
    (Terminal)
```

## Performance Optimization

### Database Indexes

- Composite index on (Status, Priority) for pending approvals
- Index on RequesterId for "my tickets" queries
- Index on AssetId for asset-related queries
- Index on CreatedAt for date range filtering

### Caching Strategy

- Cache ticket counts by status (5-minute TTL)
- Cache metrics calculations (10-minute TTL)
- No caching for individual tickets (real-time updates required)

### Query Optimization

- Use pagination for all list endpoints (default 20, max 100)
- Fetch join for ticket with asset and user details
- Separate queries for status history (lazy loading)
- Batch operations for bulk approve/reject

## Integration Patterns

### Integration with Module 3 (Allocation)

```typescript
// When completing allocation ticket
async completeAllocationTicket(ticket: Ticket, executorId: string): Promise<void> {
  try {
    // Call allocation service
    await allocationService.assignAsset({
      assetId: ticket.assetId,
      assignToUser: ticket.assignToUser,
      assignToLocation: ticket.assignToLocation,
      assignedBy: executorId
    });
    
    // Update ticket status
    ticket.status = TicketStatus.COMPLETED;
    ticket.completedAt = new Date();
    await ticketRepository.save(ticket);
    
    // Create notification
    await notificationService.createNotification(ticket, 'COMPLETED');
    
    // Audit log
    await auditService.logEvent({
      userId: executorId,
      actionType: 'TICKET_COMPLETE',
      resourceType: 'TICKET',
      resourceId: ticket.id
    });
  } catch (error) {
    // Handle allocation failure
    throw new AllocationFailedError(error.message);
  }
}
```

### Integration with Audit Service

All ticket operations are logged:
- Ticket creation
- Status changes (approve, reject, complete, cancel)
- Priority updates
- Bulk operations

## Error Handling

### Validation Errors

```json
{
  "error": {
    "type": "VALIDATION_ERROR",
    "message": "Validation failed",
    "details": [
      {
        "field": "requestReason",
        "message": "Request reason must be between 10 and 1000 characters"
      },
      {
        "field": "assignToUser",
        "message": "Either assignToUser or assignToLocation must be provided"
      }
    ],
    "timestamp": "2024-01-15T10:30:00Z",
    "requestId": "req-123456"
  }
}
```

### Business Rule Errors

```json
{
  "error": {
    "type": "INVALID_STATUS_TRANSITION",
    "message": "Cannot transition from COMPLETED to APPROVED",
    "details": {
      "currentStatus": "COMPLETED",
      "attemptedStatus": "APPROVED"
    },
    "timestamp": "2024-01-15T10:30:00Z",
    "requestId": "req-123456"
  }
}
```

## Testing Strategy

### Property-Based Tests

Module 4 requires 12 correctness properties (Properties 35-43, 48-49, 51):

1. **Property 35**: Valid ticket creation generates unique identifier and ticket number
2. **Property 36**: Ticket status transitions follow valid state machine rules
3. **Property 37**: Approval requires Asset_Manager or Administrator role
4. **Property 38**: Completed allocation tickets result in asset assignment
5. **Property 39**: Completed de-allocation tickets result in asset unassignment
6. **Property 40**: Ticket cancellation prevents further status changes
7. **Property 41**: Status history maintains chronological order
8. **Property 42**: Notifications are created for all status changes
9. **Property 43**: Ticket metrics calculations are accurate
10. **Property 48**: Bulk operations maintain transaction integrity
11. **Property 49**: Priority updates are reflected immediately
12. **Property 51**: Search filters return only matching tickets

### Unit Tests

- Ticket service methods
- Workflow service state transitions
- Integration service allocation execution
- Notification service message generation
- Metrics service calculations

### Integration Tests

- End-to-end ticket lifecycle
- Integration with allocation module
- Database transaction handling
- Concurrent ticket operations

## Security Considerations

### Authentication

- All endpoints require valid JWT token
- Token must contain user ID and roles

### Authorization

- Role-based access control for all operations
- Requesters can only view/cancel their own tickets
- Asset_Managers and Administrators have full access

### Data Validation

- Validate all inputs at API boundary
- Sanitize user-provided text fields
- Validate foreign key references
- Enforce business rules before persistence

### Audit Logging

- Log all ticket operations
- Include user ID, timestamp, and action
- Prevent modification of audit logs
- Retain logs for compliance

## Deployment Considerations

### Database Migration

```sql
-- V2__add_tickets_table.sql
-- Create Tickets table
CREATE TABLE Tickets (...);

-- Create TicketStatusHistory table
CREATE TABLE TicketStatusHistory (...);

-- Create Notifications table
CREATE TABLE Notifications (...);

-- Create indexes
CREATE INDEX IX_Tickets_Status ON Tickets(Status);
-- ... additional indexes
```

### Configuration

```properties
# application.properties
ticket.number.prefix=TKT
ticket.bulk.max-size=50
ticket.metrics.cache-ttl=600
notification.retention-days=90
```

### Monitoring

- Track ticket creation rate
- Monitor approval times
- Alert on high rejection rates
- Track integration failures with allocation module

## Future Enhancements

1. **Email Notifications**: Send email alerts for status changes
2. **SLA Management**: Track and enforce service level agreements
3. **Escalation Rules**: Auto-escalate overdue approvals
4. **Approval Delegation**: Allow approvers to delegate authority
5. **Ticket Templates**: Pre-defined templates for common requests
6. **Attachment Support**: Allow file attachments to tickets
7. **Comments/Discussion**: Enable threaded discussions on tickets
8. **Approval Chains**: Multi-level approval workflows
9. **Scheduled Tickets**: Schedule ticket execution for future dates
10. **Mobile App**: Native mobile app for ticket management