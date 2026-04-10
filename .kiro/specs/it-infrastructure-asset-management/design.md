# Design Document: IT Infrastructure Asset Management

## Overview

The IT Infrastructure Asset Management application is a comprehensive system for tracking, managing, and monitoring IT infrastructure assets throughout their lifecycle. The system provides secure multi-user access with role-based permissions, enabling organizations to maintain accurate inventory records, track asset assignments and locations, monitor lifecycle status, and generate compliance reports.

### Technology Stack

- **Frontend**: Angular (TypeScript-based SPA framework)
- **Backend**: Spring Boot (Java-based REST API)
- **Database**: Microsoft SQL Server
- **Authentication**: Spring Security with JWT tokens
- **ORM**: Spring Data JPA with Hibernate

### Core Capabilities

- Secure user authentication and role-based authorization
- Complete asset lifecycle management from acquisition to retirement
- Real-time asset search and filtering across large inventories
- Assignment tracking for users and locations
- Asset allocation and de-allocation ticketing system with approval workflow
- Comprehensive audit logging for compliance
- Bulk data import/export for system integration
- Configurable validation rules and custom fields
- Performance-optimized for inventories up to 100,000 assets

### Design Principles

1. **Security First**: All operations require authentication and authorization checks
2. **Audit Everything**: Complete audit trail for compliance and investigation
3. **Data Integrity**: Validation at all entry points with immutable audit logs
4. **Performance**: Sub-second search and 2-second report generation for large inventories
5. **Extensibility**: Configurable asset types, custom fields, and validation rules

## Architecture

### System Architecture

The application follows a layered architecture pattern with clear separation of concerns:

```
┌─────────────────────────────────────────────────────────┐
│                    Presentation Layer                    │
│              (Angular SPA / REST API)                    │
└─────────────────────────────────────────────────────────┘
                          │
┌─────────────────────────────────────────────────────────┐
│                   Application Layer                      │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐  │
│  │ Auth Service │  │ Asset Service│  │Report Service│  │
│  └──────────────┘  └──────────────┘  └──────────────┘  │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐  │
│  │ Audit Service│  │Import/Export │  │Config Service│  │
│  └──────────────┘  └──────────────┘  └──────────────┘  │
│  ┌──────────────┐                                       │
│  │Ticket Service│                                       │
│  └──────────────┘                                       │
└─────────────────────────────────────────────────────────┘
                          │
┌─────────────────────────────────────────────────────────┐
│                     Domain Layer                         │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐  │
│  │ Asset Domain │  │  User Domain │  │ Audit Domain │  │
│  └──────────────┘  └──────────────┘  └──────────────┘  │
│  ┌──────────────┐                                       │
│  │Ticket Domain │                                       │
│  └──────────────┘                                       │
└─────────────────────────────────────────────────────────┘
                          │
┌─────────────────────────────────────────────────────────┐
│                  Data Access Layer                       │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐  │
│  │Asset Repo    │  │ User Repo    │  │ Audit Repo   │  │
│  └──────────────┘  └──────────────┘  └──────────────┘  │
│  ┌──────────────┐                                       │
│  │Ticket Repo   │                                       │
│  └──────────────┘                                       │
└─────────────────────────────────────────────────────────┘
                          │
┌─────────────────────────────────────────────────────────┐
│                   Persistence Layer                      │
│              (MS SQL Server Database)                    │
└─────────────────────────────────────────────────────────┘
```

### Key Architectural Decisions

1. **Layered Architecture**: Separates concerns and enables independent testing of business logic
2. **Repository Pattern**: Abstracts data access to enable different storage backends
3. **Service Layer**: Encapsulates business logic and orchestrates domain operations
4. **Immutable Audit Log**: Append-only audit storage prevents tampering
5. **Indexed Search**: Database indexes on searchable fields for performance
6. **Ticketing Workflow**: Approval-based workflow for asset allocation/de-allocation operations

## Components and Interfaces

### Authentication Service

Handles user authentication, session management, and account security.

**Interface:**
```typescript
interface AuthenticationService {
  // Authenticate user and create session
  login(username: string, password: string): Result<Session, AuthError>
  
  // Terminate user session
  logout(sessionId: string): Result<void, AuthError>
  
  // Validate session is active
  validateSession(sessionId: string): Result<Session, AuthError>
  
  // Check if account is locked
  isAccountLocked(username: string): boolean
  
  // Record failed login attempt
  recordFailedLogin(username: string): void
}

type AuthError = 
  | { type: 'INVALID_CREDENTIALS' }
  | { type: 'ACCOUNT_LOCKED', unlockTime: Date }
  | { type: 'SESSION_EXPIRED' }
  | { type: 'SESSION_INVALID' }
```

**Responsibilities:**
- Verify user credentials against stored hashes
- Enforce password complexity requirements
- Track failed login attempts and lock accounts after 5 failures
- Manage session lifecycle with 30-minute inactivity timeout
- Generate and validate session tokens

### Authorization Service

Manages user roles and permissions, enforcing access control.

**Interface:**
```typescript
interface AuthorizationService {
  // Check if user has permission for action
  hasPermission(userId: string, action: Action): boolean
  
  // Assign role to user
  assignRole(adminId: string, userId: string, role: Role): Result<void, AuthzError>
  
  // Revoke role from user
  revokeRole(adminId: string, userId: string, role: Role): Result<void, AuthzError>
  
  // Get user's roles
  getUserRoles(userId: string): Role[]
}

enum Role {
  ADMINISTRATOR = 'Administrator',
  ASSET_MANAGER = 'Asset_Manager',
  VIEWER = 'Viewer'
}

enum Action {
  CREATE_ASSET,
  UPDATE_ASSET,
  DELETE_ASSET,
  VIEW_ASSET,
  MANAGE_USERS,
  VIEW_AUDIT_LOG,
  EXPORT_DATA,
  IMPORT_DATA,
  CONFIGURE_SYSTEM
}

type AuthzError =
  | { type: 'INSUFFICIENT_PERMISSIONS' }
  | { type: 'USER_NOT_FOUND' }
  | { type: 'INVALID_ROLE' }
```

**Responsibilities:**
- Map roles to permissions
- Validate user has required permissions before operations
- Enforce role hierarchy (Administrator > Asset_Manager > Viewer)
- Prevent privilege escalation

### Asset Service

Core business logic for asset management operations.

**Interface:**
```typescript
interface AssetService {
  // Create new asset
  createAsset(userId: string, asset: AssetInput): Result<Asset, AssetError>
  
  // Update existing asset
  updateAsset(userId: string, assetId: string, updates: AssetUpdate): Result<Asset, AssetError>
  
  // Get asset by ID
  getAsset(assetId: string): Result<Asset, AssetError>
  
  // Search assets
  searchAssets(query: SearchQuery): Result<Asset[], AssetError>
  
  // Update lifecycle status
  updateStatus(userId: string, assetId: string, newStatus: LifecycleStatus): Result<Asset, AssetError>
  
  // Assign asset to user or location
  assignAsset(userId: string, assetId: string, assignment: Assignment): Result<Asset, AssetError>
  
  // Get assignment history
  getAssignmentHistory(assetId: string): AssignmentHistory[]
}

type AssetError =
  | { type: 'DUPLICATE_SERIAL_NUMBER', serialNumber: string }
  | { type: 'ASSET_NOT_FOUND', assetId: string }
  | { type: 'VALIDATION_FAILED', errors: ValidationError[] }
  | { type: 'INVALID_STATUS_TRANSITION', from: LifecycleStatus, to: LifecycleStatus }
  | { type: 'ASSET_RETIRED', assetId: string }
```

**Responsibilities:**
- Validate asset data before persistence
- Enforce business rules (unique serial numbers, valid status transitions)
- Coordinate with audit service for logging
- Manage asset lifecycle state machine
- Track assignment history

### Audit Service

Records all system activities for compliance and investigation.

**Interface:**
```typescript
interface AuditService {
  // Log an audit event
  logEvent(event: AuditEvent): void
  
  // Search audit log
  searchAuditLog(query: AuditQuery): Result<AuditEntry[], AuditError>
  
  // Get audit entries for specific asset
  getAssetAuditTrail(assetId: string): AuditEntry[]
}

interface AuditEvent {
  timestamp: Date
  userId: string
  actionType: ActionType
  resourceType: ResourceType
  resourceId: string
  changes?: FieldChange[]
  metadata?: Record<string, any>
}

interface FieldChange {
  field: string
  oldValue: any
  newValue: any
}

enum ActionType {
  CREATE = 'CREATE',
  UPDATE = 'UPDATE',
  DELETE = 'DELETE',
  STATUS_CHANGE = 'STATUS_CHANGE',
  LOGIN = 'LOGIN',
  LOGOUT = 'LOGOUT',
  FAILED_LOGIN = 'FAILED_LOGIN'
}
```

**Responsibilities:**
- Append-only audit log storage
- Record all CRUD operations with before/after values
- Prevent modification or deletion of audit entries
- Support efficient querying by date, user, action, and resource
- Retain entries for minimum 7 years

### Report Service

Generates inventory and compliance reports.

**Interface:**
```typescript
interface ReportService {
  // Generate asset count by type
  generateAssetCountByType(): Result<Report, ReportError>
  
  // Generate asset distribution by location
  generateAssetsByLocation(): Result<Report, ReportError>
  
  // Generate assets by lifecycle status
  generateAssetsByStatus(): Result<Report, ReportError>
  
  // Generate end-of-life report
  generateEndOfLifeReport(thresholds: LifespanThresholds): Result<Report, ReportError>
}

interface Report {
  reportType: string
  generatedAt: Date
  generatedBy: string
  data: ReportData
  metadata: ReportMetadata
}

interface ReportMetadata {
  totalAssets: number
  executionTimeMs: number
  filters?: any
}
```

**Responsibilities:**
- Aggregate asset data for reporting
- Apply configurable thresholds for end-of-life calculations
- Optimize queries for large inventories (100,000+ assets)
- Complete report generation within 10 seconds

### Import/Export Service

Handles bulk data operations for system integration.

**Interface:**
```typescript
interface ImportExportService {
  // Export assets to format
  exportAssets(format: ExportFormat, filters?: SearchQuery): Result<ExportResult, ExportError>
  
  // Import assets from file
  importAssets(userId: string, format: ImportFormat, data: string): Result<ImportResult, ImportError>
  
  // Validate import data without persisting
  validateImport(format: ImportFormat, data: string): ValidationResult
}

enum ExportFormat {
  CSV = 'CSV',
  JSON = 'JSON'
}

enum ImportFormat {
  CSV = 'CSV',
  JSON = 'JSON'
}

interface ImportResult {
  successCount: number
  failureCount: number
  errors: ImportError[]
}

interface ImportError {
  lineNumber: number
  field?: string
  message: string
  data: any
}
```

**Responsibilities:**
- Parse CSV and JSON formats
- Validate all records before import
- Report validation errors with line numbers
- Support bulk operations up to 10,000 records
- Generate exports within 30 seconds for 100,000 assets

### Configuration Service

Manages system-wide configuration settings.

**Interface:**
```typescript
interface ConfigurationService {
  // Get configuration value
  getConfig<T>(key: string): T | undefined
  
  // Update configuration
  updateConfig(adminId: string, key: string, value: any): Result<void, ConfigError>
  
  // Get all configurations
  getAllConfigs(): Record<string, any>
  
  // Validate configuration change
  validateConfig(key: string, value: any): Result<void, ValidationError[]>
}

interface ConfigurableSettings {
  sessionTimeoutMinutes: number // 10-120
  lifespanThresholds: Record<AssetType, number>
  customAssetTypes: string[]
  customFields: CustomFieldDefinition[]
}

interface CustomFieldDefinition {
  name: string
  type: 'string' | 'number' | 'date' | 'boolean'
  required: boolean
  validation?: ValidationRule
}
```

**Responsibilities:**
- Store and retrieve configuration settings
- Validate configuration changes
- Apply changes immediately without restart
- Support custom asset types and fields

### Ticket Service

Manages asset allocation and de-allocation tickets with approval workflow.

**Interface:**
```typescript
interface TicketService {
  // Create allocation ticket
  createAllocationTicket(userId: string, ticket: AllocationTicketInput): Result<Ticket, TicketError>
  
  // Create de-allocation ticket
  createDeallocationTicket(userId: string, ticket: DeallocationTicketInput): Result<Ticket, TicketError>
  
  // Update ticket status
  updateTicketStatus(userId: string, ticketId: string, status: TicketStatus, comments?: string): Result<Ticket, TicketError>
  
  // Get ticket by ID
  getTicket(ticketId: string): Result<Ticket, TicketError>
  
  // Search tickets
  searchTickets(query: TicketSearchQuery): Result<Ticket[], TicketError>
  
  // Approve ticket
  approveTicket(approverId: string, ticketId: string, comments?: string): Result<Ticket, TicketError>
  
  // Reject ticket
  rejectTicket(approverId: string, ticketId: string, reason: string): Result<Ticket, TicketError>
  
  // Complete ticket (execute allocation/de-allocation)
  completeTicket(userId: string, ticketId: string): Result<Ticket, TicketError>
  
  // Generate ticket metrics report
  generateTicketMetrics(filters: TicketMetricsFilter): Result<TicketMetrics, TicketError>
}

enum TicketType {
  ALLOCATION = 'allocation',
  DEALLOCATION = 'deallocation'
}

enum TicketStatus {
  PENDING = 'pending',
  APPROVED = 'approved',
  REJECTED = 'rejected',
  IN_PROGRESS = 'in_progress',
  COMPLETED = 'completed',
  CANCELLED = 'cancelled'
}

interface AllocationTicketInput {
  assetId: string
  assignToUser?: string
  assignToLocation?: string
  requestReason: string
  priority: TicketPriority
}

interface DeallocationTicketInput {
  assetId: string
  deallocationReason: string
  priority: TicketPriority
}

enum TicketPriority {
  LOW = 'low',
  MEDIUM = 'medium',
  HIGH = 'high',
  URGENT = 'urgent'
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

interface TicketMetrics {
  totalTickets: number
  ticketsByStatus: Record<TicketStatus, number>
  ticketsByType: Record<TicketType, number>
  ticketsByPriority: Record<TicketPriority, number>
  averageApprovalTimeHours: number
  averageCompletionTimeHours: number
  approvalRate: number
  rejectionRate: number
}

type TicketError =
  | { type: 'TICKET_NOT_FOUND', ticketId: string }
  | { type: 'INVALID_STATUS_TRANSITION', from: TicketStatus, to: TicketStatus }
  | { type: 'ASSET_NOT_FOUND', assetId: string }
  | { type: 'ASSET_ALREADY_ASSIGNED' }
  | { type: 'ASSET_NOT_ASSIGNED' }
  | { type: 'INSUFFICIENT_PERMISSIONS' }
  | { type: 'VALIDATION_FAILED', errors: ValidationError[] }
```

**Responsibilities:**
- Create and manage allocation/de-allocation tickets
- Enforce ticket workflow state machine
- Track ticket approval and completion
- Generate ticket metrics and reports
- Integrate with asset assignment workflow
- Audit all ticket operations

## Data Models

### User

```typescript
interface User {
  id: string // UUID
  username: string // unique
  passwordHash: string
  email: string
  roles: Role[]
  createdAt: Date
  updatedAt: Date
  lastLoginAt?: Date
  accountLocked: boolean
  lockUntil?: Date
  failedLoginAttempts: number
}
```

### Session

```typescript
interface Session {
  id: string // UUID
  userId: string
  createdAt: Date
  lastActivityAt: Date
  expiresAt: Date
  ipAddress: string
  userAgent: string
}
```

### Asset

```typescript
interface Asset {
  id: string // UUID, immutable
  assetType: AssetType
  name: string
  serialNumber: string // unique, immutable
  acquisitionDate: Date
  status: LifecycleStatus
  location?: string
  assignedUser?: string
  assignedUserEmail?: string
  assignmentDate?: Date
  locationUpdateDate?: Date
  notes?: string
  customFields?: Record<string, any>
  createdAt: Date
  createdBy: string
  updatedAt: Date
  updatedBy: string
  readOnly: boolean // true when status = retired
}

enum AssetType {
  SERVER = 'server',
  WORKSTATION = 'workstation',
  NETWORK_DEVICE = 'network_device',
  STORAGE_DEVICE = 'storage_device',
  SOFTWARE_LICENSE = 'software_license',
  PERIPHERAL = 'peripheral',
  KEYBOARD = 'keyboard',
  MOUSE = 'mouse',
  LAPTOP = 'laptop',
  MONITOR = 'monitor',
  HEADSET = 'headset',
  LAPTOP_CHARGER = 'laptop_charger',
  HDMI_CABLE = 'hdmi_cable',
  NETWORK_CABLE = 'network_cable',
  ACCESS_CARD = 'access_card'
}

enum LifecycleStatus {
  ORDERED = 'ordered',
  RECEIVED = 'received',
  DEPLOYED = 'deployed',
  IN_USE = 'in_use',
  MAINTENANCE = 'maintenance',
  STORAGE = 'storage',
  RETIRED = 'retired'
}
```

### Assignment History

```typescript
interface AssignmentHistory {
  id: string
  assetId: string
  assignmentType: 'USER' | 'LOCATION'
  assignedTo: string
  assignedBy: string
  assignedAt: Date
  unassignedAt?: Date
}
```

### Audit Entry

```typescript
interface AuditEntry {
  id: string // UUID
  timestamp: Date
  userId: string
  username: string
  actionType: ActionType
  resourceType: ResourceType
  resourceId: string
  changes?: FieldChange[]
  metadata?: Record<string, any>
  ipAddress: string
}
```

### Ticket

```typescript
interface Ticket {
  id: string // UUID
  ticketNumber: string // Human-readable ticket number (e.g., TKT-2024-00001)
  type: TicketType
  status: TicketStatus
  priority: TicketPriority
  assetId: string
  assetName: string
  assetSerialNumber: string
  requesterId: string
  requesterName: string
  assignToUser?: string
  assignToUserEmail?: string
  assignToLocation?: string
  requestReason?: string
  deallocationReason?: string
  approverId?: string
  approverName?: string
  approvalComments?: string
  rejectionReason?: string
  createdAt: Date
  updatedAt: Date
  approvedAt?: Date
  rejectedAt?: Date
  completedAt?: Date
  cancelledAt?: Date
}
```

### Ticket Status History

```typescript
interface TicketStatusHistory {
  id: string
  ticketId: string
  fromStatus: TicketStatus | null
  toStatus: TicketStatus
  changedBy: string
  changedAt: Date
  comments?: string
}
```

### Search Query

```typescript
interface SearchQuery {
  text?: string // searches across multiple fields
  filters?: {
    assetType?: AssetType[]
    status?: LifecycleStatus[]
    location?: string[]
    acquisitionDateFrom?: Date
    acquisitionDateTo?: Date
    assignedUser?: string
  }
  sortBy?: string
  sortOrder?: 'ASC' | 'DESC'
  limit?: number
  offset?: number
}
```

### Validation Rules

```typescript
interface ValidationRule {
  required?: boolean
  minLength?: number
  maxLength?: number
  pattern?: RegExp
  min?: number
  max?: number
  custom?: (value: any) => ValidationError | null
}

interface ValidationError {
  field: string
  message: string
  value?: any
}
```

## Database Schema

### MS SQL Server Table Structures

#### Users Table

```sql
CREATE TABLE Users (
  Id UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
  Username NVARCHAR(100) NOT NULL UNIQUE,
  PasswordHash NVARCHAR(255) NOT NULL,
  Email NVARCHAR(255) NOT NULL,
  CreatedAt DATETIME2 NOT NULL DEFAULT GETUTCDATE(),
  UpdatedAt DATETIME2 NOT NULL DEFAULT GETUTCDATE(),
  LastLoginAt DATETIME2 NULL,
  AccountLocked BIT NOT NULL DEFAULT 0,
  LockUntil DATETIME2 NULL,
  FailedLoginAttempts INT NOT NULL DEFAULT 0,
  IsActive BIT NOT NULL DEFAULT 1,
  
  INDEX IX_Users_Username (Username),
  INDEX IX_Users_Email (Email),
  INDEX IX_Users_AccountLocked (AccountLocked)
);
```

#### UserRoles Table

```sql
CREATE TABLE UserRoles (
  Id UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
  UserId UNIQUEIDENTIFIER NOT NULL,
  Role NVARCHAR(50) NOT NULL,
  AssignedBy UNIQUEIDENTIFIER NOT NULL,
  AssignedAt DATETIME2 NOT NULL DEFAULT GETUTCDATE(),
  
  CONSTRAINT FK_UserRoles_UserId FOREIGN KEY (UserId) REFERENCES Users(Id) ON DELETE CASCADE,
  CONSTRAINT FK_UserRoles_AssignedBy FOREIGN KEY (AssignedBy) REFERENCES Users(Id),
  CONSTRAINT CHK_UserRoles_Role CHECK (Role IN ('Administrator', 'Asset_Manager', 'Viewer')),
  
  INDEX IX_UserRoles_UserId (UserId),
  INDEX IX_UserRoles_Role (Role)
);
```

#### Sessions Table

```sql
CREATE TABLE Sessions (
  Id UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
  UserId UNIQUEIDENTIFIER NOT NULL,
  SessionToken NVARCHAR(500) NOT NULL UNIQUE,
  CreatedAt DATETIME2 NOT NULL DEFAULT GETUTCDATE(),
  LastActivityAt DATETIME2 NOT NULL DEFAULT GETUTCDATE(),
  ExpiresAt DATETIME2 NOT NULL,
  IpAddress NVARCHAR(45) NULL,
  UserAgent NVARCHAR(500) NULL,
  IsActive BIT NOT NULL DEFAULT 1,
  
  CONSTRAINT FK_Sessions_UserId FOREIGN KEY (UserId) REFERENCES Users(Id) ON DELETE CASCADE,
  
  INDEX IX_Sessions_UserId (UserId),
  INDEX IX_Sessions_SessionToken (SessionToken),
  INDEX IX_Sessions_ExpiresAt (ExpiresAt),
  INDEX IX_Sessions_IsActive (IsActive)
);
```

#### Assets Table

```sql
CREATE TABLE Assets (
  Id UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
  AssetType NVARCHAR(50) NOT NULL,
  Name NVARCHAR(255) NOT NULL,
  SerialNumber NVARCHAR(100) NOT NULL UNIQUE,
  AcquisitionDate DATE NOT NULL,
  Status NVARCHAR(50) NOT NULL,
  Location NVARCHAR(255) NULL,
  AssignedUser NVARCHAR(255) NULL,
  AssignedUserEmail NVARCHAR(255) NULL,
  AssignmentDate DATETIME2 NULL,
  LocationUpdateDate DATETIME2 NULL,
  Notes NVARCHAR(MAX) NULL,
  CustomFields NVARCHAR(MAX) NULL, -- JSON string
  CreatedAt DATETIME2 NOT NULL DEFAULT GETUTCDATE(),
  CreatedBy UNIQUEIDENTIFIER NOT NULL,
  UpdatedAt DATETIME2 NOT NULL DEFAULT GETUTCDATE(),
  UpdatedBy UNIQUEIDENTIFIER NOT NULL,
  ReadOnly BIT NOT NULL DEFAULT 0,
  
  CONSTRAINT FK_Assets_CreatedBy FOREIGN KEY (CreatedBy) REFERENCES Users(Id),
  CONSTRAINT FK_Assets_UpdatedBy FOREIGN KEY (UpdatedBy) REFERENCES Users(Id),
  CONSTRAINT CHK_Assets_AssetType CHECK (AssetType IN ('server', 'workstation', 'network_device', 'storage_device', 'software_license', 'peripheral', 'keyboard', 'mouse', 'laptop', 'monitor', 'headset', 'laptop_charger', 'hdmi_cable', 'network_cable', 'access_card')),
  CONSTRAINT CHK_Assets_Status CHECK (Status IN ('ordered', 'received', 'deployed', 'in_use', 'maintenance', 'storage', 'retired')),
  CONSTRAINT CHK_Assets_AcquisitionDate CHECK (AcquisitionDate <= CAST(GETUTCDATE() AS DATE)),
  
  INDEX IX_Assets_AssetType (AssetType),
  INDEX IX_Assets_SerialNumber (SerialNumber),
  INDEX IX_Assets_Status (Status),
  INDEX IX_Assets_Location (Location),
  INDEX IX_Assets_AssignedUser (AssignedUser),
  INDEX IX_Assets_AcquisitionDate (AcquisitionDate),
  INDEX IX_Assets_CreatedBy (CreatedBy)
);
```

#### AssignmentHistory Table

```sql
CREATE TABLE AssignmentHistory (
  Id UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
  AssetId UNIQUEIDENTIFIER NOT NULL,
  AssignmentType NVARCHAR(20) NOT NULL,
  AssignedTo NVARCHAR(255) NOT NULL,
  AssignedBy UNIQUEIDENTIFIER NOT NULL,
  AssignedAt DATETIME2 NOT NULL DEFAULT GETUTCDATE(),
  UnassignedAt DATETIME2 NULL,
  
  CONSTRAINT FK_AssignmentHistory_AssetId FOREIGN KEY (AssetId) REFERENCES Assets(Id) ON DELETE CASCADE,
  CONSTRAINT FK_AssignmentHistory_AssignedBy FOREIGN KEY (AssignedBy) REFERENCES Users(Id),
  CONSTRAINT CHK_AssignmentHistory_Type CHECK (AssignmentType IN ('USER', 'LOCATION')),
  
  INDEX IX_AssignmentHistory_AssetId (AssetId),
  INDEX IX_AssignmentHistory_AssignedTo (AssignedTo),
  INDEX IX_AssignmentHistory_AssignedAt (AssignedAt)
);
```

#### AuditLog Table

```sql
CREATE TABLE AuditLog (
  Id UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
  Timestamp DATETIME2 NOT NULL DEFAULT GETUTCDATE(),
  UserId UNIQUEIDENTIFIER NOT NULL,
  Username NVARCHAR(100) NOT NULL,
  ActionType NVARCHAR(50) NOT NULL,
  ResourceType NVARCHAR(50) NOT NULL,
  ResourceId NVARCHAR(100) NOT NULL,
  Changes NVARCHAR(MAX) NULL, -- JSON string
  Metadata NVARCHAR(MAX) NULL, -- JSON string
  IpAddress NVARCHAR(45) NULL,
  
  CONSTRAINT FK_AuditLog_UserId FOREIGN KEY (UserId) REFERENCES Users(Id),
  CONSTRAINT CHK_AuditLog_ActionType CHECK (ActionType IN ('CREATE', 'UPDATE', 'DELETE', 'STATUS_CHANGE', 'LOGIN', 'LOGOUT', 'FAILED_LOGIN', 'TICKET_CREATE', 'TICKET_UPDATE', 'TICKET_APPROVE', 'TICKET_REJECT', 'TICKET_COMPLETE')),
  
  INDEX IX_AuditLog_Timestamp (Timestamp),
  INDEX IX_AuditLog_UserId (UserId),
  INDEX IX_AuditLog_ActionType (ActionType),
  INDEX IX_AuditLog_ResourceType (ResourceType),
  INDEX IX_AuditLog_ResourceId (ResourceId)
);
```

#### Tickets Table

```sql
CREATE TABLE Tickets (
  Id UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
  TicketNumber NVARCHAR(50) NOT NULL UNIQUE,
  Type NVARCHAR(20) NOT NULL,
  Status NVARCHAR(20) NOT NULL,
  Priority NVARCHAR(20) NOT NULL,
  AssetId UNIQUEIDENTIFIER NOT NULL,
  AssetName NVARCHAR(255) NOT NULL,
  AssetSerialNumber NVARCHAR(100) NOT NULL,
  RequesterId UNIQUEIDENTIFIER NOT NULL,
  RequesterName NVARCHAR(255) NOT NULL,
  AssignToUser NVARCHAR(255) NULL,
  AssignToUserEmail NVARCHAR(255) NULL,
  AssignToLocation NVARCHAR(255) NULL,
  RequestReason NVARCHAR(MAX) NULL,
  DeallocationReason NVARCHAR(MAX) NULL,
  ApproverId UNIQUEIDENTIFIER NULL,
  ApproverName NVARCHAR(255) NULL,
  ApprovalComments NVARCHAR(MAX) NULL,
  RejectionReason NVARCHAR(MAX) NULL,
  CreatedAt DATETIME2 NOT NULL DEFAULT GETUTCDATE(),
  UpdatedAt DATETIME2 NOT NULL DEFAULT GETUTCDATE(),
  ApprovedAt DATETIME2 NULL,
  RejectedAt DATETIME2 NULL,
  CompletedAt DATETIME2 NULL,
  CancelledAt DATETIME2 NULL,
  
  CONSTRAINT FK_Tickets_AssetId FOREIGN KEY (AssetId) REFERENCES Assets(Id),
  CONSTRAINT FK_Tickets_RequesterId FOREIGN KEY (RequesterId) REFERENCES Users(Id),
  CONSTRAINT FK_Tickets_ApproverId FOREIGN KEY (ApproverId) REFERENCES Users(Id),
  CONSTRAINT CHK_Tickets_Type CHECK (Type IN ('allocation', 'deallocation')),
  CONSTRAINT CHK_Tickets_Status CHECK (Status IN ('pending', 'approved', 'rejected', 'in_progress', 'completed', 'cancelled')),
  CONSTRAINT CHK_Tickets_Priority CHECK (Priority IN ('low', 'medium', 'high', 'urgent')),
  
  INDEX IX_Tickets_TicketNumber (TicketNumber),
  INDEX IX_Tickets_Status (Status),
  INDEX IX_Tickets_Type (Type),
  INDEX IX_Tickets_Priority (Priority),
  INDEX IX_Tickets_AssetId (AssetId),
  INDEX IX_Tickets_RequesterId (RequesterId),
  INDEX IX_Tickets_ApproverId (ApproverId),
  INDEX IX_Tickets_CreatedAt (CreatedAt)
);
```

#### TicketStatusHistory Table

```sql
CREATE TABLE TicketStatusHistory (
  Id UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
  TicketId UNIQUEIDENTIFIER NOT NULL,
  FromStatus NVARCHAR(20) NULL,
  ToStatus NVARCHAR(20) NOT NULL,
  ChangedBy UNIQUEIDENTIFIER NOT NULL,
  ChangedAt DATETIME2 NOT NULL DEFAULT GETUTCDATE(),
  Comments NVARCHAR(MAX) NULL,
  
  CONSTRAINT FK_TicketStatusHistory_TicketId FOREIGN KEY (TicketId) REFERENCES Tickets(Id) ON DELETE CASCADE,
  CONSTRAINT FK_TicketStatusHistory_ChangedBy FOREIGN KEY (ChangedBy) REFERENCES Users(Id),
  CONSTRAINT CHK_TicketStatusHistory_ToStatus CHECK (ToStatus IN ('pending', 'approved', 'rejected', 'in_progress', 'completed', 'cancelled')),
  
  INDEX IX_TicketStatusHistory_TicketId (TicketId),
  INDEX IX_TicketStatusHistory_ChangedAt (ChangedAt)
);
```

#### Configurations Table

```sql
CREATE TABLE Configurations (
  Id UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
  ConfigKey NVARCHAR(100) NOT NULL UNIQUE,
  ConfigValue NVARCHAR(MAX) NOT NULL,
  ValueType NVARCHAR(50) NOT NULL,
  Description NVARCHAR(500) NULL,
  UpdatedBy UNIQUEIDENTIFIER NOT NULL,
  UpdatedAt DATETIME2 NOT NULL DEFAULT GETUTCDATE(),
  
  CONSTRAINT FK_Configurations_UpdatedBy FOREIGN KEY (UpdatedBy) REFERENCES Users(Id),
  CONSTRAINT CHK_Configurations_ValueType CHECK (ValueType IN ('string', 'number', 'boolean', 'json')),
  
  INDEX IX_Configurations_ConfigKey (ConfigKey)
);
```

### Database Initialization Script

```sql
-- Create database
CREATE DATABASE ITAssetManagement;
GO

USE ITAssetManagement;
GO

-- Enable row versioning for optimistic concurrency
ALTER DATABASE ITAssetManagement SET READ_COMMITTED_SNAPSHOT ON;
GO

-- Create tables (in order of dependencies)
-- [Execute table creation scripts above in order]

-- Insert default administrator user (password: Admin@123456)
INSERT INTO Users (Id, Username, PasswordHash, Email, AccountLocked, FailedLoginAttempts, IsActive)
VALUES (
  NEWID(),
  'admin',
  '$2a$10$YourHashedPasswordHere', -- Use BCrypt hash
  'admin@example.com',
  0,
  0,
  1
);

-- Assign Administrator role to default admin
DECLARE @AdminUserId UNIQUEIDENTIFIER = (SELECT Id FROM Users WHERE Username = 'admin');
INSERT INTO UserRoles (UserId, Role, AssignedBy)
VALUES (@AdminUserId, 'Administrator', @AdminUserId);

-- Insert default configurations
INSERT INTO Configurations (ConfigKey, ConfigValue, ValueType, Description, UpdatedBy)
VALUES 
  ('SessionTimeoutMinutes', '30', 'number', 'Session timeout in minutes', @AdminUserId),
  ('LifespanThresholds', '{"server":60,"workstation":48,"network_device":72,"storage_device":60,"software_license":36,"peripheral":36,"keyboard":24,"mouse":24,"laptop":48,"monitor":60,"headset":24,"laptop_charger":24,"hdmi_cable":36,"network_cable":36,"access_card":60}', 'json', 'Asset lifespan thresholds in months', @AdminUserId),
  ('CustomAssetTypes', '[]', 'json', 'Custom asset types', @AdminUserId),
  ('CustomFields', '[]', 'json', 'Custom field definitions', @AdminUserId);
GO
```


## Correctness Properties

*A property is a characteristic or behavior that should hold true across all valid executions of a system—essentially, a formal statement about what the system should do. Properties serve as the bridge between human-readable specifications and machine-verifiable correctness guarantees.*

### Property 1: Valid credentials create authenticated sessions

*For any* valid username and password combination, the authentication service SHALL create a session with a unique session ID, correct user ID, and valid expiration time.

**Validates: Requirements 1.1**

### Property 2: Invalid credentials are rejected and logged

*For any* invalid username or password combination, the authentication service SHALL reject the login attempt and create an audit log entry recording the failed attempt.

**Validates: Requirements 1.2**

### Property 3: Password complexity validation

*For any* password string, the validation SHALL accept passwords with 12+ characters containing uppercase, lowercase, numbers, and special characters, and SHALL reject passwords not meeting these criteria.

**Validates: Requirements 1.3**

### Property 4: Authorization checks enforce role permissions

*For any* user, role, and action combination, the authorization service SHALL allow the action only if the user's role has the required permission for that action.

**Validates: Requirements 2.2**

### Property 5: Role assignment restricted to administrators

*For any* user attempting to assign or revoke roles, the operation SHALL succeed only if the user has the Administrator role.

**Validates: Requirements 2.4**

### Property 6: Asset modification restricted by role

*For any* user attempting to modify an asset, the operation SHALL succeed only if the user has Administrator or Asset_Manager role.

**Validates: Requirements 2.5**

### Property 7: Valid asset creation generates unique identifier

*For any* valid asset data with all required fields, creating the asset SHALL generate a unique asset ID and persist the asset with all provided fields.

**Validates: Requirements 3.1**

### Property 8: Required field validation

*For any* asset data missing one or more required fields (asset_type, name, serial_number, acquisition_date, initial_status), the validation SHALL fail and return errors identifying all missing fields.

**Validates: Requirements 3.2, 11.1**

### Property 9: Serial number uniqueness enforcement

*For any* asset with a serial number that already exists in the system, attempting to create a new asset with that serial number SHALL fail with a duplicate serial number error.

**Validates: Requirements 3.3**

### Property 10: Asset updates persist correctly

*For any* existing asset and valid update data, updating the asset SHALL persist all changed fields and leave unchanged fields unmodified.

**Validates: Requirements 4.1**

### Property 11: Immutable field protection

*For any* existing asset, attempting to modify the asset ID or serial_number fields SHALL fail with an immutability error.

**Validates: Requirements 4.4**

### Property 12: Validation errors are comprehensive

*For any* asset data with multiple validation failures, the validation SHALL return error messages identifying all validation failures, not just the first one.

**Validates: Requirements 4.5, 11.4**

### Property 13: Valid status transitions are allowed

*For any* asset and status transition that follows the allowed transition rules (ordered→received, received→deployed, deployed→in_use, any→maintenance), the status update SHALL succeed.

**Validates: Requirements 5.2, 5.3**

### Property 14: Invalid status transitions are rejected

*For any* asset and status transition that violates the allowed transition rules, the status update SHALL fail with an invalid transition error.

**Validates: Requirements 5.2, 5.3**

### Property 15: Retired assets become read-only

*For any* asset transitioned to retired status, the asset SHALL be marked as read-only and all subsequent modification attempts SHALL fail.

**Validates: Requirements 5.4**

### Property 16: Search returns matching assets

*For any* search query with text and/or filters, the search SHALL return all and only those assets that match the query criteria across the searchable fields (asset_type, name, serial_number, location, assigned_user).

**Validates: Requirements 6.1, 6.3**

### Property 17: Multiple filters use AND logic

*For any* search query with multiple filter criteria, the search SHALL return only assets that satisfy all filter criteria simultaneously.

**Validates: Requirements 6.4**

### Property 18: Asset assignment updates fields

*For any* asset and assignment (user or location), assigning the asset SHALL update the appropriate assignment fields (assigned_user/location and assignment_date/location_update_date).

**Validates: Requirements 7.1, 7.2**

### Property 19: Assignment history is maintained

*For any* sequence of asset assignments, the assignment history SHALL contain all assignments in chronological order with correct dates and assignees.

**Validates: Requirements 7.3**

### Property 20: Assignment queries return correct results

*For any* user or location, querying assets assigned to that user or location SHALL return all and only those assets currently assigned to that user or location.

**Validates: Requirements 7.5**

### Property 21: Report aggregations are accurate

*For any* collection of assets, reports grouping by asset_type, location, or lifecycle_status SHALL produce counts that sum to the total number of assets and correctly categorize each asset.

**Validates: Requirements 8.1, 8.2, 8.3**

### Property 22: End-of-life calculations are correct

*For any* asset with an acquisition date and configured lifespan threshold for its asset type, the end-of-life report SHALL include the asset if and only if the time since acquisition exceeds the threshold.

**Validates: Requirements 8.4**

### Property 23: All operations are audit logged

*For any* create, update, delete, or status change operation on an asset, an audit log entry SHALL be created with timestamp, user ID, action type, asset ID, and field changes.

**Validates: Requirements 3.5, 4.2, 5.5, 7.4, 9.1, 9.2**

### Property 24: Audit log search returns matching entries

*For any* audit log search query with filters (date range, user, action type, asset), the search SHALL return all and only those audit entries matching the filter criteria.

**Validates: Requirements 9.4**

### Property 25: Audit log entries are immutable

*For any* existing audit log entry, attempting to modify or delete the entry SHALL fail.

**Validates: Requirements 9.5**

### Property 26: Export-import round-trip preserves data

*For any* collection of assets, exporting to CSV or JSON format and then importing the exported data SHALL produce assets equivalent to the original assets.

**Validates: Requirements 10.1, 10.2**

### Property 27: Import validation reports all errors

*For any* import data containing invalid records, the import validation SHALL report errors for all invalid records with line numbers and specific error messages.

**Validates: Requirements 10.3**

### Property 28: Serial number format validation

*For any* asset type and serial number, the validation SHALL accept serial numbers matching the expected format for that asset type and reject those that don't.

**Validates: Requirements 11.2**

### Property 29: Acquisition date validation

*For any* acquisition date, the validation SHALL reject dates in the future and accept dates in the past or present.

**Validates: Requirements 11.3**

### Property 30: Email format validation

*For any* email address string, the validation SHALL accept strings matching standard email format (local@domain) and reject invalid formats.

**Validates: Requirements 11.5**

### Property 31: Configuration range validation

*For any* session timeout configuration value, the validation SHALL accept values between 10 and 120 minutes inclusive and reject values outside this range.

**Validates: Requirements 12.1**

### Property 32: Custom asset types are usable

*For any* custom asset type defined by an administrator, assets can be created with that asset type and all asset operations SHALL work correctly with the custom type.

**Validates: Requirements 12.3**

### Property 33: Custom fields are applied

*For any* custom field definition added by an administrator, new assets SHALL include the custom field and validation SHALL enforce the custom field's rules.

**Validates: Requirements 12.4**

### Property 34: Configuration changes apply immediately

*For any* valid configuration change, the new configuration SHALL take effect immediately for all subsequent operations without requiring system restart.

**Validates: Requirements 12.5**

### Property 35: Valid ticket creation generates unique identifier

*For any* valid allocation or de-allocation ticket data with all required fields, creating the ticket SHALL generate a unique ticket ID and ticket number, and persist the ticket with status 'pending'.

**Validates: Ticketing System Requirements**

### Property 36: Ticket status transitions follow workflow rules

*For any* ticket and status transition, the transition SHALL succeed only if it follows valid workflow rules (pending→approved/rejected, approved→in_progress, in_progress→completed).

**Validates: Ticketing System Requirements**

### Property 37: Ticket approval updates status and records approver

*For any* pending ticket, when approved by an authorized user, the ticket SHALL transition to 'approved' status and record the approver ID, approver name, and approval timestamp.

**Validates: Ticketing System Requirements**

### Property 38: Ticket rejection requires reason

*For any* pending ticket, when rejected, the rejection SHALL fail if no rejection reason is provided, and SHALL succeed with reason recording the rejection reason and timestamp.

**Validates: Ticketing System Requirements**

### Property 39: Completed allocation tickets update asset assignment

*For any* approved allocation ticket, when completed, the system SHALL update the asset's assignment fields to match the ticket's assignment details.

**Validates: Ticketing System Requirements**

### Property 40: Completed de-allocation tickets clear asset assignment

*For any* approved de-allocation ticket, when completed, the system SHALL clear the asset's assignment fields (assigned_user, assigned_user_email, location).

**Validates: Ticketing System Requirements**

### Property 41: Ticket search returns matching tickets

*For any* ticket search query with filters (status, type, priority, requester, approver, asset, date range), the search SHALL return all and only those tickets matching the filter criteria.

**Validates: Ticketing System Requirements**

### Property 42: Ticket metrics calculations are accurate

*For any* collection of tickets, the metrics report SHALL accurately calculate total counts, counts by status/type/priority, average approval time, average completion time, and approval/rejection rates.

**Validates: Ticketing System Requirements**

### Property 43: Ticket operations are audit logged

*For any* ticket create, update, approve, reject, or complete operation, an audit log entry SHALL be created with timestamp, user ID, action type, ticket ID, and status changes.

**Validates: Ticketing System Requirements**

## Error Handling

### Error Categories

The system defines clear error categories for different failure modes:

1. **Authentication Errors**
   - Invalid credentials
   - Account locked
   - Session expired
   - Session invalid

2. **Authorization Errors**
   - Insufficient permissions
   - User not found
   - Invalid role

3. **Validation Errors**
   - Missing required fields
   - Invalid field format
   - Invalid field value
   - Constraint violation

4. **Business Logic Errors**
   - Duplicate serial number
   - Invalid status transition
   - Asset retired (read-only)
   - Asset not found

5. **System Errors**
   - Database connection failure
   - External service unavailable
   - Timeout
   - Internal server error

### Error Handling Principles

1. **Fail Fast**: Validate inputs at system boundaries before processing
2. **Comprehensive Validation**: Report all validation errors, not just the first
3. **Specific Error Messages**: Include actionable information (field names, line numbers, constraint details)
4. **No Sensitive Data Leakage**: Authentication errors don't reveal whether username or password was incorrect
5. **Audit Failed Operations**: Log failed operations for security monitoring
6. **Graceful Degradation**: System remains operational even if non-critical services fail

### Error Response Format

All errors follow a consistent structure:

```typescript
interface ErrorResponse {
  error: {
    type: string // Error category
    message: string // Human-readable message
    details?: any // Additional context (field errors, line numbers, etc.)
    timestamp: Date
    requestId: string // For tracing
  }
}
```

### Specific Error Handling

**Authentication Failures:**
- Log failed attempts with IP address and timestamp
- Increment failed login counter
- Lock account after 5 consecutive failures
- Return generic "invalid credentials" message (don't reveal which field was wrong)

**Authorization Failures:**
- Log unauthorized access attempts
- Return 403 Forbidden with minimal details
- Don't reveal existence of resources user can't access

**Validation Failures:**
- Collect all validation errors before returning
- Include field names and specific validation rules violated
- For imports, include line numbers
- Return 400 Bad Request

**Business Logic Failures:**
- Return 409 Conflict for constraint violations (duplicate serial number)
- Return 422 Unprocessable Entity for invalid state transitions
- Include specific reason in error message

**Not Found:**
- Return 404 Not Found for missing resources
- Don't reveal whether resource exists if user lacks permission

**System Failures:**
- Log full error details internally
- Return 500 Internal Server Error with generic message
- Include request ID for support correlation
- Retry transient failures (database connection, network)

## Testing Strategy

### Testing Approach

The testing strategy employs a comprehensive multi-layered approach:

1. **Property-Based Tests**: Verify universal properties across randomized inputs
2. **Unit Tests**: Test specific examples, edge cases, and error conditions
3. **Integration Tests**: Verify component interactions and external dependencies
4. **Performance Tests**: Validate performance requirements with large datasets
5. **Security Tests**: Verify authentication, authorization, and audit logging

### Property-Based Testing

**Library**: fast-check (for TypeScript/JavaScript implementation)

**Configuration**:
- Minimum 100 iterations per property test
- Each test tagged with: `Feature: it-infrastructure-asset-management, Property {number}: {property_text}`

**Property Test Coverage**:
- All 43 correctness properties defined in this document
- Each property implemented as a single property-based test
- Generators for: users, roles, assets, status transitions, search queries, audit entries, configurations, tickets, ticket status transitions

**Key Generators**:

```typescript
// Generate valid assets with all required fields
const validAssetGen = fc.record({
  assetType: fc.constantFrom(...Object.values(AssetType)),
  name: fc.string({ minLength: 1, maxLength: 100 }),
  serialNumber: fc.string({ minLength: 5, maxLength: 50 }),
  acquisitionDate: fc.date({ max: new Date() }),
  status: fc.constantFrom(...Object.values(LifecycleStatus))
})

// Generate assets with missing required fields
const invalidAssetGen = fc.record({
  assetType: fc.option(fc.constantFrom(...Object.values(AssetType))),
  name: fc.option(fc.string()),
  serialNumber: fc.option(fc.string()),
  acquisitionDate: fc.option(fc.date()),
  status: fc.option(fc.constantFrom(...Object.values(LifecycleStatus)))
}).filter(asset => 
  !asset.assetType || !asset.name || !asset.serialNumber || 
  !asset.acquisitionDate || !asset.status
)

// Generate valid status transitions
const validTransitionGen = fc.constantFrom(
  ['ordered', 'received'],
  ['received', 'deployed'],
  ['deployed', 'in_use'],
  ['ordered', 'maintenance'],
  ['received', 'maintenance'],
  // ... all valid transitions
)

// Generate passwords with varying complexity
const passwordGen = fc.string({ minLength: 0, maxLength: 30 })

// Generate search queries
const searchQueryGen = fc.record({
  text: fc.option(fc.string()),
  filters: fc.option(fc.record({
    assetType: fc.option(fc.array(fc.constantFrom(...Object.values(AssetType)))),
    status: fc.option(fc.array(fc.constantFrom(...Object.values(LifecycleStatus)))),
    location: fc.option(fc.array(fc.string())),
    acquisitionDateFrom: fc.option(fc.date()),
    acquisitionDateTo: fc.option(fc.date())
  }))
})

// Generate valid tickets
const validTicketGen = fc.record({
  type: fc.constantFrom('allocation', 'deallocation'),
  priority: fc.constantFrom('low', 'medium', 'high', 'urgent'),
  assetId: fc.uuid(),
  requestReason: fc.string({ minLength: 10, maxLength: 500 }),
  assignToUser: fc.option(fc.string()),
  assignToLocation: fc.option(fc.string())
})

// Generate valid ticket status transitions
const validTicketTransitionGen = fc.constantFrom(
  ['pending', 'approved'],
  ['pending', 'rejected'],
  ['approved', 'in_progress'],
  ['in_progress', 'completed'],
  ['pending', 'cancelled']
)
```

### Unit Testing

**Focus Areas**:
- Specific examples demonstrating correct behavior
- Edge cases (empty strings, boundary values, null/undefined)
- Error conditions with specific inputs
- Account locking after exactly 5 failures
- Session timeout after exactly 30 minutes
- Empty search results with informative message

**Example Unit Tests**:
- Administrator can create/modify/delete users
- Non-administrators cannot manage users
- Viewer role cannot modify assets
- Search with no matches returns empty result
- Retired asset cannot be modified
- Ticket approval requires Administrator or Asset_Manager role
- Rejected ticket cannot be completed
- Ticket completion updates asset assignment correctly

### Integration Testing

**Focus Areas**:
- Database persistence and retrieval
- Transaction handling and rollback
- Search performance with 100,000 assets (< 2 seconds)
- Report generation with 100,000 assets (< 10 seconds)
- Export generation with 100,000 assets (< 30 seconds)
- Bulk import of 10,000 assets
- Audit log retention (7 years)
- Session cleanup and timeout enforcement
- Ticket workflow integration with asset assignment
- Ticket metrics report generation

**Test Environment**:
- Use test database with realistic data volumes
- Seed database with 100,000 test assets for performance tests
- Mock external services where appropriate
- Use in-memory database for fast unit tests

### Security Testing

**Focus Areas**:
- Authentication bypass attempts
- Authorization bypass attempts
- SQL injection in search queries
- XSS in asset fields
- CSRF protection
- Session hijacking prevention
- Audit log tampering attempts
- Password complexity enforcement
- Account lockout mechanism

### Performance Testing

**Requirements**:
- Search: < 2 seconds for 100,000 assets
- Reports: < 10 seconds for 100,000 assets
- Export: < 30 seconds for 100,000 assets
- Import: Support 10,000 assets in single operation

**Approach**:
- Load test database with 100,000 assets
- Measure response times under load
- Profile slow queries and optimize indexes
- Test concurrent user scenarios

### Test Data Management

**Strategies**:
- Use factories for creating test data
- Seed database with realistic data for integration tests
- Clean up test data after each test
- Use transactions for test isolation
- Generate random data with property-based testing

### Continuous Integration

**CI Pipeline**:
1. Run unit tests (fast, < 1 minute)
2. Run property-based tests (100 iterations each)
3. Run integration tests (with test database)
4. Run security tests
5. Run performance tests (on schedule, not every commit)
6. Generate coverage report (target: > 80%)

### Test Organization

```
tests/
├── unit/
│   ├── auth/
│   │   ├── authentication.test.ts
│   │   └── authorization.test.ts
│   ├── asset/
│   │   ├── creation.test.ts
│   │   ├── update.test.ts
│   │   ├── lifecycle.test.ts
│   │   └── validation.test.ts
│   ├── audit/
│   │   └── audit-log.test.ts
│   ├── ticket/
│   │   ├── creation.test.ts
│   │   ├── workflow.test.ts
│   │   ├── approval.test.ts
│   │   └── metrics.test.ts
│   └── config/
│       └── configuration.test.ts
├── property/
│   ├── auth.property.test.ts
│   ├── asset.property.test.ts
│   ├── lifecycle.property.test.ts
│   ├── search.property.test.ts
│   ├── assignment.property.test.ts
│   ├── audit.property.test.ts
│   ├── import-export.property.test.ts
│   ├── validation.property.test.ts
│   ├── ticket.property.test.ts
│   └── config.property.test.ts
├── integration/
│   ├── database.test.ts
│   ├── search-performance.test.ts
│   ├── report-performance.test.ts
│   ├── ticket-workflow.test.ts
│   └── import-export.test.ts
├── security/
│   ├── authentication.test.ts
│   ├── authorization.test.ts
│   └── audit.test.ts
└── fixtures/
    ├── users.ts
    ├── assets.ts
    ├── tickets.ts
    └── generators.ts
```

### Coverage Goals

- Unit test coverage: > 80%
- Property test coverage: All 43 properties
- Integration test coverage: All external dependencies
- Security test coverage: All authentication/authorization paths
- Performance test coverage: All performance requirements



## User Interface Design

### Frontend Architecture

The Angular frontend follows a modular architecture with feature-based organization:

```
src/app/
├── core/                           # Singleton services and guards
│   ├── auth/
│   │   ├── auth.service.ts
│   │   ├── auth.guard.ts
│   │   └── jwt.interceptor.ts
│   ├── services/
│   │   ├── notification.service.ts
│   │   └── user.service.ts
│   └── interceptors/
│       └── error.interceptor.ts
├── shared/                         # Shared components and utilities
│   ├── components/
│   │   ├── header/
│   │   ├── sidebar/
│   │   ├── notification-badge/
│   │   └── status-badge/
│   ├── models/
│   └── pipes/
├── features/                       # Feature modules
│   ├── dashboard/
│   │   ├── dashboard.component.ts
│   │   └── dashboard.module.ts
│   ├── assets/
│   │   ├── asset-list/
│   │   ├── asset-detail/
│   │   ├── asset-form/
│   │   └── assets.module.ts
│   ├── tickets/
│   │   ├── ticket-list/
│   │   ├── ticket-detail/
│   │   ├── ticket-create/
│   │   └── tickets.module.ts
│   ├── users/
│   │   ├── user-list/
│   │   ├── user-form/
│   │   ├── user-profile/
│   │   └── users.module.ts
│   └── reports/
│       └── reports.module.ts
└── app.component.ts
```

### User Management Interface

#### User List Screen

**Purpose**: Display all users with ability to create, edit, enable/disable accounts

**Components**:
- User data table with columns: Username, Email, Roles, Status, Last Login, Actions
- Search and filter controls (by role, status)
- "Create User" button (Administrator only)
- Action buttons per row: Edit, Enable/Disable, View Details

**Responsive Behavior**:
- Desktop: Full table with all columns
- Tablet: Hide Last Login column, show essential info
- Mobile: Card-based layout with expandable details

#### User Form Screen (Create/Edit)

**Purpose**: Create new users or edit existing user information

**Form Fields**:
- Username (required, unique, disabled when editing)
- Email (required, unique, email validation)
- Password (required for create, optional for edit)
- Confirm Password (required when password provided)
- Roles (multi-select: Administrator, Asset_Manager, Viewer)
- Account Status (Active/Disabled toggle)

**Validation**:
- Real-time validation with error messages
- Password complexity check (12+ chars, mixed case, numbers, special chars)
- Unique username and email validation
- Form submission disabled until all validations pass

**Actions**:
- Save button
- Cancel button (returns to user list)

#### User Profile Screen

**Purpose**: Allow users to view and update their own profile

**Sections**:
- Profile Information (read-only): Username, Roles, Account Status
- Contact Information (editable): Email
- Change Password section with fields:
  - Current Password
  - New Password
  - Confirm New Password

**Actions**:
- Update Profile button
- Change Password button

### Asset Request Interface

#### Dashboard Screen

**Purpose**: Provide overview of user's activities and quick access to common actions

**Widgets**:
1. **Summary Cards**:
   - Pending Requests (count with icon)
   - Approved Requests (count with icon)
   - Assigned Assets (count with icon)
   - Recent Activity (count with icon)

2. **Recent Notifications Panel**:
   - List of recent ticket status changes
   - Unread indicator
   - Mark as read functionality
   - Link to full notification history

3. **Quick Actions**:
   - Create Allocation Request button
   - Create De-allocation Request button
   - View All My Requests button

4. **Recent Tickets Table**:
   - Last 5 tickets with status
   - Quick view of ticket details
   - Link to full ticket list

**Responsive Behavior**:
- Desktop: 2x2 grid of summary cards, side-by-side panels
- Tablet: 2x2 grid, stacked panels
- Mobile: Single column, cards stack vertically

#### My Requests Screen

**Purpose**: Display all tickets created by the logged-in user

**Components**:
1. **Filter Bar**:
   - Status filter (All, Pending, Approved, Rejected, In Progress, Completed, Cancelled)
   - Type filter (All, Allocation, De-allocation)
   - Date range filter
   - Search by ticket number or asset name

2. **Tickets Table/List**:
   - Columns: Ticket Number, Type, Asset Name, Status, Priority, Created Date, Last Updated
   - Status badges with color coding:
     - Pending: Yellow/Warning
     - Approved: Blue/Info
     - Rejected: Red/Danger
     - In Progress: Purple/Primary
     - Completed: Green/Success
     - Cancelled: Gray/Secondary
   - Click row to view details

3. **Pagination**:
   - Page size selector (10, 20, 50 items)
   - Page navigation controls

**Responsive Behavior**:
- Desktop: Full table with all columns
- Tablet: Hide Created Date, show essential columns
- Mobile: Card-based layout with status badge, ticket number, asset name, and last updated date

#### Ticket Detail Screen

**Purpose**: Display complete information about a specific ticket

**Sections**:
1. **Ticket Header**:
   - Ticket number (large, prominent)
   - Status badge
   - Type badge
   - Priority indicator

2. **Asset Information**:
   - Asset name
   - Asset serial number
   - Asset type
   - Current status

3. **Request Details**:
   - Requester name and email
   - Request reason (for allocation)
   - De-allocation reason (for de-allocation)
   - Assignment details (for allocation: user/location)
   - Priority level

4. **Approval Information** (if applicable):
   - Approver name
   - Approval/Rejection date
   - Approval comments or rejection reason

5. **Timeline**:
   - Status history with timestamps
   - Visual timeline showing progression

6. **Actions** (context-dependent):
   - Cancel button (if status is pending)
   - Back to My Requests button

**Responsive Behavior**:
- Desktop: Two-column layout (details left, timeline right)
- Tablet: Two-column layout with adjusted spacing
- Mobile: Single column, sections stack vertically

#### Create Request Screen

**Purpose**: Allow users to create allocation or de-allocation requests

**Tab Navigation**:
- Allocation Request tab
- De-allocation Request tab

**Allocation Request Form**:
1. **Asset Selection**:
   - Search/autocomplete for assets
   - Display asset details when selected
   - Show availability status

2. **Assignment Details**:
   - Assign to User (radio button)
     - User search/select field
     - User email field
   - Assign to Location (radio button)
     - Location input field

3. **Request Information**:
   - Request reason (textarea, required)
   - Priority (dropdown: Low, Medium, High, Urgent)

4. **Actions**:
   - Submit Request button
   - Cancel button

**De-allocation Request Form**:
1. **Asset Selection**:
   - Search/autocomplete for currently assigned assets
   - Display current assignment details

2. **Request Information**:
   - De-allocation reason (textarea, required)
   - Priority (dropdown: Low, Medium, High, Urgent)

3. **Actions**:
   - Submit Request button
   - Cancel button

**Validation**:
- All required fields must be filled
- Asset must be selected
- For allocation: either user or location must be specified
- Real-time validation feedback

**Success Flow**:
- Display confirmation modal with ticket number
- Provide options: View Ticket, Create Another Request, Go to Dashboard

**Responsive Behavior**:
- Desktop: Full-width form with side-by-side fields where appropriate
- Tablet: Full-width form, fields stack
- Mobile: Single column, all fields stack vertically

### Navigation Structure

**Main Navigation** (Sidebar/Top Nav):
- Dashboard (home icon)
- My Requests (ticket icon with unread count badge)
- Assets (for Asset_Manager and Administrator roles)
- Users (for Administrator role only)
- Reports (for Administrator and Asset_Manager roles)
- Profile (user icon)
- Logout

**Notification Bell** (Top Right):
- Unread notification count badge
- Dropdown showing recent notifications
- Mark all as read option
- View all notifications link

### Responsive Design Breakpoints

```scss
// Breakpoints
$mobile: 320px;
$tablet: 768px;
$desktop: 1024px;
$wide: 1440px;

// Media queries
@media (max-width: 767px) {
  // Mobile styles
  // Single column layouts
  // Card-based lists
  // Bottom navigation
}

@media (min-width: 768px) and (max-width: 1023px) {
  // Tablet styles
  // Two-column layouts where appropriate
  // Condensed tables
  // Side navigation
}

@media (min-width: 1024px) {
  // Desktop styles
  // Full table layouts
  // Multi-column grids
  // Side navigation with labels
}
```

### UI Component Library

**Recommended**: Angular Material or PrimeNG for consistent, accessible components

**Key Components**:
- Data tables with sorting, filtering, pagination
- Form controls with validation
- Modal dialogs
- Toast notifications
- Status badges
- Loading spinners
- Date pickers
- Autocomplete/typeahead
- Tabs
- Cards
- Buttons with icons

### Accessibility Considerations

1. **WCAG 2.1 AA Compliance**:
   - Proper heading hierarchy
   - Alt text for images and icons
   - Keyboard navigation support
   - Focus indicators
   - Color contrast ratios (4.5:1 minimum)

2. **ARIA Labels**:
   - Screen reader support for all interactive elements
   - ARIA live regions for dynamic content updates
   - Proper form labels and error associations

3. **Keyboard Shortcuts**:
   - Tab navigation through forms
   - Enter to submit
   - Escape to cancel/close modals
   - Arrow keys for table navigation

## Additional Correctness Properties for New Features

### Property 44: User account creation generates unique identifier

*For any* valid user account data with all required fields, creating the user SHALL generate a unique user ID and persist the user with all provided fields.

**Validates: Requirements 13.1**

### Property 45: Username and email uniqueness enforcement

*For any* user account with a username or email that already exists in the system, attempting to create a new user with that username or email SHALL fail with a uniqueness error.

**Validates: Requirements 13.5**

### Property 46: Disabled user accounts prevent login

*For any* user account that is disabled, login attempts SHALL fail and any active sessions SHALL be terminated.

**Validates: Requirements 13.4**

### Property 47: User profile updates are restricted

*For any* user attempting to update their own profile, modifications to roles or account status SHALL fail with an authorization error.

**Validates: Requirements 14.4**

### Property 48: Ticket list filtering returns matching tickets

*For any* ticket search query with filters (status, type, date range), the search SHALL return all and only those tickets matching the filter criteria for the logged-in user.

**Validates: Requirements 15.3, 15.4**

### Property 49: Ticket creation validates required fields

*For any* ticket creation request missing required fields (asset, reason, priority), the validation SHALL fail and return errors identifying all missing fields.

**Validates: Requirements 16.3**

### Property 50: Dashboard statistics are accurate

*For any* logged-in user, the dashboard SHALL display accurate counts for pending requests, approved requests, and assigned assets based on current data.

**Validates: Requirements 18.1**

### Property 51: Notifications are created for ticket status changes

*For any* ticket status change (pending to approved, approved to completed, etc.), a notification SHALL be created for the ticket requester.

**Validates: Requirements 18.2**

### Property 52: User account audit logging

*For any* user account create, update, enable, or disable operation, an audit log entry SHALL be created with timestamp, admin user ID, action type, and changed fields.

**Validates: Requirements 13.7**

## Updated Testing Strategy

### Additional UI Testing

**Component Tests**:
- User management components (list, form, profile)
- Ticket management components (list, detail, create)
- Dashboard components
- Notification components

**E2E Tests**:
- User management workflow (create, edit, enable/disable)
- Ticket creation and tracking workflow
- Dashboard navigation and quick actions
- Responsive behavior on different screen sizes

**Accessibility Tests**:
- Keyboard navigation
- Screen reader compatibility
- Color contrast validation
- ARIA label verification

### Updated Test Organization

```
tests/
├── unit/
│   ├── auth/
│   ├── asset/
│   ├── audit/
│   ├── ticket/
│   ├── user/                    # New
│   │   ├── user-management.test.ts
│   │   └── user-profile.test.ts
│   └── config/
├── property/
│   ├── auth.property.test.ts
│   ├── asset.property.test.ts
│   ├── lifecycle.property.test.ts
│   ├── search.property.test.ts
│   ├── assignment.property.test.ts
│   ├── audit.property.test.ts
│   ├── import-export.property.test.ts
│   ├── validation.property.test.ts
│   ├── ticket.property.test.ts
│   ├── user.property.test.ts    # New
│   ├── ui.property.test.ts      # New
│   └── config.property.test.ts
├── integration/
│   ├── database.test.ts
│   ├── search-performance.test.ts
│   ├── report-performance.test.ts
│   ├── ticket-workflow.test.ts
│   ├── user-management.test.ts  # New
│   └── import-export.test.ts
├── e2e/
│   ├── asset-management.e2e.ts
│   ├── ticket-workflow.e2e.ts
│   ├── user-management.e2e.ts   # New
│   ├── dashboard.e2e.ts         # New
│   └── responsive.e2e.ts        # New
├── accessibility/               # New
│   ├── wcag-compliance.test.ts
│   └── keyboard-navigation.test.ts
└── fixtures/
    ├── users.ts
    ├── assets.ts
    ├── tickets.ts
    └── generators.ts
```

### Coverage Goals (Updated)

- Unit test coverage: > 80%
- Property test coverage: All 52 properties (updated from 43)
- Integration test coverage: All external dependencies
- E2E test coverage: All critical user workflows including new UI features
- Accessibility test coverage: WCAG 2.1 AA compliance
- Security test coverage: All authentication/authorization paths
- Performance test coverage: All performance requirements
