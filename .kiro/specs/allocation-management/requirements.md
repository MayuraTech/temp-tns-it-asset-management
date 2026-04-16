# Requirements Document: Allocation Management

## Introduction

This document specifies the requirements for Module 3: Allocation Management of the IT Infrastructure Asset Management System. The Allocation Management module handles the assignment of assets to users and locations, tracks assignment history, and maintains a complete audit trail of all allocation operations. This module integrates with Module 1 (User Management) for user references, Module 2 (Asset Management) for asset availability, and Module 4 (Ticket Management) for approval-driven allocations.

## Glossary

- **Allocation_System**: The software component responsible for managing asset assignments and allocation history
- **Assignment**: The act of associating an asset with a user or location
- **Assignment_Record**: A database entry representing a current or historical asset assignment
- **Asset**: An IT infrastructure item tracked in the system (from Module 2)
- **User**: A system user who can be assigned assets (from Module 1)
- **Location**: A physical or logical place where assets can be assigned
- **Assignment_Type**: The category of assignment, either USER or LOCATION
- **Allocation_Manager**: A user with permission to allocate and deallocate assets
- **Administrator**: A user with full system permissions including allocation management
- **Assignment_History**: The complete chronological record of all assignments for an asset
- **Deallocation**: The act of removing an asset's current assignment
- **Reassignment**: The act of changing an asset's assignment from one user/location to another
- **Audit_Service**: The shared service that logs all system operations (from Common Services)
- **Available_Asset**: An asset with status IN_USE, DEPLOYED, or STORAGE that is not currently assigned

## Requirements

### Requirement 1: Assign Asset to User

**User Story:** As an Allocation Manager, I want to assign assets to users, so that I can track which user is responsible for each asset.

#### Acceptance Criteria

1. WHEN a valid assignment request is submitted, THE Allocation_System SHALL create an Assignment_Record with assignment type USER
2. WHEN creating a user assignment, THE Allocation_System SHALL update the asset's AssignedUser and AssignedUserEmail fields
3. WHEN creating a user assignment, THE Allocation_System SHALL set the AssignmentDate to the current timestamp
4. IF the asset is already assigned to a user or location, THEN THE Allocation_System SHALL return an error indicating the asset is already assigned
5. WHERE the user has ADMINISTRATOR or ASSET_MANAGER role, THE Allocation_System SHALL permit the assignment operation
6. WHEN an assignment is created, THE Allocation_System SHALL log the operation to the Audit_Service with action type CREATE and resource type ASSIGNMENT
7. THE Allocation_System SHALL validate that the assigned user email is in valid email format
8. THE Allocation_System SHALL validate that the asset exists and is in an assignable status (IN_USE, DEPLOYED, or STORAGE)

### Requirement 2: Assign Asset to Location

**User Story:** As an Allocation Manager, I want to assign assets to locations, so that I can track where assets are physically located.

#### Acceptance Criteria

1. WHEN a valid location assignment request is submitted, THE Allocation_System SHALL create an Assignment_Record with assignment type LOCATION
2. WHEN creating a location assignment, THE Allocation_System SHALL update the asset's Location field
3. WHEN creating a location assignment, THE Allocation_System SHALL set the LocationUpdateDate to the current timestamp
4. IF the asset is already assigned to a user or location, THEN THE Allocation_System SHALL return an error indicating the asset is already assigned
5. WHERE the user has ADMINISTRATOR or ASSET_MANAGER role, THE Allocation_System SHALL permit the assignment operation
6. WHEN a location assignment is created, THE Allocation_System SHALL log the operation to the Audit_Service
7. THE Allocation_System SHALL validate that the location name is not empty and does not exceed 255 characters
8. THE Allocation_System SHALL validate that the asset exists and is in an assignable status

### Requirement 3: Deallocate Asset

**User Story:** As an Allocation Manager, I want to deallocate assets, so that I can make them available for reassignment.

#### Acceptance Criteria

1. WHEN a deallocation request is submitted for an assigned asset, THE Allocation_System SHALL set the UnassignedAt timestamp on the current Assignment_Record
2. WHEN deallocating an asset, THE Allocation_System SHALL clear the asset's AssignedUser, AssignedUserEmail, and Location fields
3. WHEN deallocating an asset, THE Allocation_System SHALL clear the asset's AssignmentDate and LocationUpdateDate fields
4. IF the asset is not currently assigned, THEN THE Allocation_System SHALL return an error indicating no active assignment exists
5. WHERE the user has ADMINISTRATOR or ASSET_MANAGER role, THE Allocation_System SHALL permit the deallocation operation
6. WHEN a deallocation is completed, THE Allocation_System SHALL log the operation to the Audit_Service with action type DELETE and resource type ASSIGNMENT
7. THE Allocation_System SHALL validate that the asset exists before attempting deallocation

### Requirement 4: Reassign Asset

**User Story:** As an Allocation Manager, I want to reassign assets from one user/location to another, so that I can efficiently manage asset transfers without manual deallocation.

#### Acceptance Criteria

1. WHEN a reassignment request is submitted, THE Allocation_System SHALL close the current Assignment_Record by setting UnassignedAt
2. WHEN a reassignment request is submitted, THE Allocation_System SHALL create a new Assignment_Record with the new assignment details
3. WHEN reassigning an asset, THE Allocation_System SHALL update the asset's assignment fields to reflect the new assignment
4. IF the asset is not currently assigned, THEN THE Allocation_System SHALL return an error indicating no active assignment exists
5. WHERE the user has ADMINISTRATOR or ASSET_MANAGER role, THE Allocation_System SHALL permit the reassignment operation
6. WHEN a reassignment is completed, THE Allocation_System SHALL log both the deallocation and new allocation to the Audit_Service
7. THE Allocation_System SHALL validate that the new assignment details are valid before processing the reassignment
8. THE Allocation_System SHALL ensure the reassignment operation is atomic (both close old and create new succeed or both fail)

### Requirement 5: View Assignment History

**User Story:** As an Administrator, I want to view the complete assignment history for an asset, so that I can audit asset usage and track accountability.

#### Acceptance Criteria

1. WHEN an assignment history request is submitted for an asset, THE Allocation_System SHALL return all Assignment_Records for that asset ordered by AssignedAt descending
2. THE Allocation_System SHALL include both active assignments (UnassignedAt is null) and historical assignments (UnassignedAt is not null)
3. THE Allocation_System SHALL include the assignment type, assigned to value, assigned by user, assigned at timestamp, and unassigned at timestamp for each record
4. WHERE the user has ADMINISTRATOR, ASSET_MANAGER, or VIEWER role, THE Allocation_System SHALL permit viewing assignment history
5. IF the asset does not exist, THEN THE Allocation_System SHALL return an error indicating asset not found
6. THE Allocation_System SHALL support pagination for assignment history with default page size of 20 records
7. WHEN retrieving assignment history, THE Allocation_System SHALL include the username of the user who performed each assignment

### Requirement 6: Query Assets by User

**User Story:** As an Allocation Manager, I want to query all assets assigned to a specific user, so that I can see what assets a user is responsible for.

#### Acceptance Criteria

1. WHEN a query by user request is submitted, THE Allocation_System SHALL return all assets where AssignedUser matches the specified user
2. THE Allocation_System SHALL return only assets with active assignments (UnassignedAt is null in Assignment_History)
3. THE Allocation_System SHALL include asset ID, name, serial number, asset type, status, and assignment date for each result
4. WHERE the user has ADMINISTRATOR, ASSET_MANAGER, or VIEWER role, THE Allocation_System SHALL permit querying assets by user
5. THE Allocation_System SHALL support pagination for query results with default page size of 20 records
6. THE Allocation_System SHALL return an empty list if no assets are assigned to the specified user
7. THE Allocation_System SHALL support case-insensitive user name matching

### Requirement 7: Query Assets by Location

**User Story:** As an Allocation Manager, I want to query all assets assigned to a specific location, so that I can perform location-based asset audits.

#### Acceptance Criteria

1. WHEN a query by location request is submitted, THE Allocation_System SHALL return all assets where Location matches the specified location
2. THE Allocation_System SHALL return only assets with active location assignments (UnassignedAt is null in Assignment_History)
3. THE Allocation_System SHALL include asset ID, name, serial number, asset type, status, and location update date for each result
4. WHERE the user has ADMINISTRATOR, ASSET_MANAGER, or VIEWER role, THE Allocation_System SHALL permit querying assets by location
5. THE Allocation_System SHALL support pagination for query results with default page size of 20 records
6. THE Allocation_System SHALL return an empty list if no assets are assigned to the specified location
7. THE Allocation_System SHALL support case-insensitive location name matching

### Requirement 8: Validate Assignment Authorization

**User Story:** As a System Administrator, I want allocation operations to be restricted by role, so that only authorized users can allocate and deallocate assets.

#### Acceptance Criteria

1. WHEN a user attempts an allocation operation, THE Allocation_System SHALL verify the user has ADMINISTRATOR or ASSET_MANAGER role
2. IF the user lacks required permissions, THEN THE Allocation_System SHALL return an error with type INSUFFICIENT_PERMISSIONS and HTTP status 403
3. WHEN a user attempts to view assignment data, THE Allocation_System SHALL verify the user has ADMINISTRATOR, ASSET_MANAGER, or VIEWER role
4. THE Allocation_System SHALL check authorization before performing any business logic or database operations
5. THE Allocation_System SHALL log failed authorization attempts to the Audit_Service with action type FAILED_AUTHORIZATION
6. THE Allocation_System SHALL include the user ID, attempted action, and resource ID in authorization failure logs

### Requirement 9: Validate Assignment Data

**User Story:** As a System Administrator, I want assignment data to be validated, so that the system maintains data integrity and prevents invalid assignments.

#### Acceptance Criteria

1. WHEN an assignment request is submitted, THE Allocation_System SHALL validate that the asset ID is a valid UUID format
2. WHEN an assignment request is submitted, THE Allocation_System SHALL validate that the asset exists in the database
3. WHEN a user assignment is requested, THE Allocation_System SHALL validate that the assigned user name is not empty and does not exceed 255 characters
4. WHEN a user assignment is requested, THE Allocation_System SHALL validate that the assigned user email is in valid email format
5. WHEN a location assignment is requested, THE Allocation_System SHALL validate that the location name is not empty and does not exceed 255 characters
6. IF any validation fails, THEN THE Allocation_System SHALL return an error response with type VALIDATION_ERROR, HTTP status 400, and a list of all validation errors
7. THE Allocation_System SHALL validate that the asset status is IN_USE, DEPLOYED, or STORAGE before allowing assignment
8. IF the asset status is ORDERED, RECEIVED, MAINTENANCE, or RETIRED, THEN THE Allocation_System SHALL return an error indicating the asset is not in an assignable status

### Requirement 10: Maintain Assignment Audit Trail

**User Story:** As a Compliance Officer, I want all allocation operations to be logged, so that I can audit asset assignments for compliance and accountability.

#### Acceptance Criteria

1. WHEN an assignment is created, THE Allocation_System SHALL log the operation to the Audit_Service with action type CREATE, resource type ASSIGNMENT, and resource ID of the assignment record
2. WHEN an assignment is deleted (deallocation), THE Allocation_System SHALL log the operation to the Audit_Service with action type DELETE and resource type ASSIGNMENT
3. WHEN an assignment is updated (reassignment), THE Allocation_System SHALL log both the deletion of the old assignment and creation of the new assignment
4. THE Allocation_System SHALL include the user ID of the user performing the operation in all audit log entries
5. THE Allocation_System SHALL include the asset ID, assignment type, and assigned to value in the audit log metadata
6. THE Allocation_System SHALL ensure audit logging occurs within the same database transaction as the allocation operation
7. IF audit logging fails, THEN THE Allocation_System SHALL roll back the allocation operation to maintain audit trail integrity

### Requirement 11: Handle Concurrent Assignment Requests

**User Story:** As a System Administrator, I want the system to handle concurrent assignment requests safely, so that race conditions do not result in invalid data states.

#### Acceptance Criteria

1. WHEN multiple assignment requests for the same asset are submitted concurrently, THE Allocation_System SHALL process them serially using database-level locking
2. THE Allocation_System SHALL use pessimistic locking when reading asset assignment status to prevent race conditions
3. IF an asset is assigned between the time a request is validated and executed, THEN THE Allocation_System SHALL return an error indicating the asset is already assigned
4. THE Allocation_System SHALL ensure all assignment operations are atomic (all database changes succeed or all fail)
5. THE Allocation_System SHALL use database transactions with appropriate isolation level to prevent dirty reads and phantom reads
6. WHEN a concurrent assignment conflict occurs, THE Allocation_System SHALL return an error with type CONFLICT and HTTP status 409

### Requirement 12: Support Assignment History Pagination

**User Story:** As an Administrator, I want assignment history to be paginated, so that I can efficiently view history for assets with many assignments.

#### Acceptance Criteria

1. WHEN requesting assignment history, THE Allocation_System SHALL accept page and size query parameters
2. THE Allocation_System SHALL default to page 0 and size 20 if pagination parameters are not provided
3. THE Allocation_System SHALL limit maximum page size to 100 records to prevent performance issues
4. THE Allocation_System SHALL return a paginated response including content array, page metadata (size, number, totalElements, totalPages), and navigation links (self, first, next, last)
5. THE Allocation_System SHALL order assignment history by AssignedAt timestamp in descending order (most recent first)
6. IF the requested page number exceeds available pages, THEN THE Allocation_System SHALL return an empty content array with correct page metadata

### Requirement 13: Integrate with Ticket Management

**User Story:** As a System Administrator, I want allocation operations to integrate with the ticket management system, so that allocations can be approval-driven when required.

#### Acceptance Criteria

1. WHEN a ticket with type ALLOCATION is completed, THE Allocation_System SHALL create the assignment specified in the ticket
2. WHEN a ticket with type DEALLOCATION is completed, THE Allocation_System SHALL remove the assignment specified in the ticket
3. THE Allocation_System SHALL validate that the asset is still in an assignable state when processing a ticket-driven allocation
4. IF the asset state has changed since ticket creation, THEN THE Allocation_System SHALL return an error indicating the asset is no longer assignable
5. THE Allocation_System SHALL use the ticket approver's user ID as the AssignedBy value for ticket-driven allocations
6. THE Allocation_System SHALL log ticket-driven allocations with a reference to the ticket ID in the audit log metadata
7. THE Allocation_System SHALL ensure ticket-driven allocation operations are atomic with ticket status updates

### Requirement 14: Provide Assignment Statistics

**User Story:** As an Administrator, I want to view assignment statistics, so that I can understand asset utilization and allocation patterns.

#### Acceptance Criteria

1. WHEN assignment statistics are requested, THE Allocation_System SHALL return the total count of currently assigned assets
2. THE Allocation_System SHALL return the count of assets assigned to users versus assets assigned to locations
3. THE Allocation_System SHALL return the count of available (unassigned) assets by status (IN_USE, DEPLOYED, STORAGE)
4. THE Allocation_System SHALL return the top 10 users by number of assigned assets
5. THE Allocation_System SHALL return the top 10 locations by number of assigned assets
6. WHERE the user has ADMINISTRATOR or ASSET_MANAGER role, THE Allocation_System SHALL permit viewing assignment statistics
7. THE Allocation_System SHALL calculate statistics efficiently using database aggregation queries

### Requirement 15: Support Assignment Search and Filtering

**User Story:** As an Allocation Manager, I want to search and filter assignment history, so that I can find specific assignments quickly.

#### Acceptance Criteria

1. WHEN searching assignment history, THE Allocation_System SHALL support filtering by assignment type (USER or LOCATION)
2. THE Allocation_System SHALL support filtering by date range using assignedFrom and assignedTo query parameters
3. THE Allocation_System SHALL support filtering by assigned by user ID
4. THE Allocation_System SHALL support text search across assigned to values (user names and location names)
5. THE Allocation_System SHALL support filtering to show only active assignments (UnassignedAt is null) or only historical assignments (UnassignedAt is not null)
6. THE Allocation_System SHALL combine multiple filters using AND logic
7. THE Allocation_System SHALL return paginated results for filtered assignment history

### Requirement 16: Validate Asset Availability

**User Story:** As an Allocation Manager, I want the system to validate asset availability before assignment, so that I cannot assign assets that are not ready for use.

#### Acceptance Criteria

1. WHEN an assignment request is submitted, THE Allocation_System SHALL verify the asset status is IN_USE, DEPLOYED, or STORAGE
2. IF the asset status is ORDERED, THEN THE Allocation_System SHALL return an error indicating the asset has not been received
3. IF the asset status is RECEIVED, THEN THE Allocation_System SHALL return an error indicating the asset has not been deployed
4. IF the asset status is MAINTENANCE, THEN THE Allocation_System SHALL return an error indicating the asset is under maintenance
5. IF the asset status is RETIRED, THEN THE Allocation_System SHALL return an error indicating the asset has been retired
6. THE Allocation_System SHALL include the current asset status in the error response to help users understand why assignment failed
7. THE Allocation_System SHALL check asset availability within the same database transaction as the assignment operation

### Requirement 17: Support Bulk Deallocation

**User Story:** As an Allocation Manager, I want to deallocate multiple assets at once, so that I can efficiently process asset returns or location changes.

#### Acceptance Criteria

1. WHEN a bulk deallocation request is submitted with multiple asset IDs, THE Allocation_System SHALL process each deallocation independently
2. THE Allocation_System SHALL return a response indicating which assets were successfully deallocated and which failed
3. THE Allocation_System SHALL continue processing remaining assets even if some deallocations fail
4. THE Allocation_System SHALL validate authorization once for the entire bulk operation
5. THE Allocation_System SHALL log each deallocation separately to the Audit_Service
6. THE Allocation_System SHALL limit bulk operations to a maximum of 50 assets per request to prevent performance issues
7. IF the bulk request exceeds 50 assets, THEN THE Allocation_System SHALL return an error indicating the request is too large

### Requirement 18: Maintain Assignment Referential Integrity

**User Story:** As a System Administrator, I want assignment records to maintain referential integrity, so that orphaned records do not exist in the database.

#### Acceptance Criteria

1. WHEN an asset is deleted, THE Allocation_System SHALL automatically delete all associated Assignment_Records through cascade delete
2. THE Allocation_System SHALL prevent deletion of a user who is referenced as AssignedBy in any Assignment_Record
3. IF a user deletion is attempted while they have assignment references, THEN THE Allocation_System SHALL return an error indicating the user cannot be deleted
4. THE Allocation_System SHALL enforce foreign key constraints at the database level for AssetId and AssignedBy fields
5. THE Allocation_System SHALL validate that referenced users exist before creating Assignment_Records
6. THE Allocation_System SHALL validate that referenced assets exist before creating Assignment_Records

### Requirement 19: Support Assignment Export

**User Story:** As an Administrator, I want to export assignment data, so that I can perform offline analysis and reporting.

#### Acceptance Criteria

1. WHEN an assignment export request is submitted, THE Allocation_System SHALL generate a CSV file containing all current assignments
2. THE Allocation_System SHALL include columns for asset ID, asset name, serial number, asset type, assignment type, assigned to, assigned by, and assigned at timestamp
3. THE Allocation_System SHALL support filtering the export by assignment type, date range, and assigned by user
4. WHERE the user has ADMINISTRATOR or ASSET_MANAGER role, THE Allocation_System SHALL permit exporting assignment data
5. THE Allocation_System SHALL limit exports to a maximum of 10,000 records to prevent performance issues
6. IF the export would exceed 10,000 records, THEN THE Allocation_System SHALL return an error indicating the export is too large and suggesting filters
7. THE Allocation_System SHALL log export operations to the Audit_Service with action type EXPORT and resource type ASSIGNMENT

### Requirement 20: Provide Assignment Performance Metrics

**User Story:** As a System Administrator, I want assignment operations to meet performance requirements, so that the system remains responsive under load.

#### Acceptance Criteria

1. WHEN creating a single assignment, THE Allocation_System SHALL complete the operation within 500 milliseconds
2. WHEN retrieving assignment history for an asset, THE Allocation_System SHALL return results within 1 second for up to 1,000 historical records
3. WHEN querying assets by user or location, THE Allocation_System SHALL return results within 2 seconds for up to 10,000 assets
4. WHEN processing bulk deallocation of 50 assets, THE Allocation_System SHALL complete within 10 seconds
5. THE Allocation_System SHALL use database indexes on AssetId, AssignedTo, and AssignedAt columns to optimize query performance
6. THE Allocation_System SHALL use connection pooling with minimum 5 and maximum 20 database connections
7. THE Allocation_System SHALL log slow operations (exceeding performance thresholds) for monitoring and optimization

## Integration Points

### Module 1 (User Management)
- **AssignedBy Reference**: Assignment records reference the user who performed the assignment
- **User Validation**: Validate that users exist before creating assignments
- **Authorization**: Use Module 1's authorization service to check user permissions

### Module 2 (Asset Management)
- **Asset Reference**: Assignment records reference assets from Module 2
- **Asset Status**: Check asset status to determine if asset is assignable
- **Asset Updates**: Update asset assignment fields when creating/removing assignments

### Module 4 (Ticket Management)
- **Ticket Completion**: Process allocation/deallocation when tickets are completed
- **Ticket Validation**: Validate asset state when processing ticket-driven allocations
- **Audit Linkage**: Include ticket ID in audit logs for ticket-driven operations

### Audit Service (Common)
- **Operation Logging**: Log all allocation operations (create, update, delete)
- **Authorization Failures**: Log failed authorization attempts
- **Export Operations**: Log data export operations

## Data Validation Rules

### Assignment Request Validation
- Asset ID: Required, valid UUID format, must exist in database
- Assignment Type: Required, must be USER or LOCATION
- Assigned To: Required, not empty, maximum 255 characters
- Assigned User Email: Required for USER assignments, valid email format
- Location Name: Required for LOCATION assignments, maximum 255 characters

### Asset Status Validation
- Asset must have status: IN_USE, DEPLOYED, or STORAGE
- Asset must not be currently assigned (unless reassigning)
- Asset must exist and not be soft-deleted

### Authorization Validation
- User must be authenticated
- User must have ADMINISTRATOR or ASSET_MANAGER role for write operations
- User must have ADMINISTRATOR, ASSET_MANAGER, or VIEWER role for read operations

## Performance Requirements

- Single assignment creation: < 500ms
- Assignment history retrieval: < 1 second (up to 1,000 records)
- Query by user/location: < 2 seconds (up to 10,000 assets)
- Bulk deallocation (50 assets): < 10 seconds
- Database connection pool: 5-20 connections
- Maximum page size: 100 records
- Maximum bulk operation size: 50 assets
- Maximum export size: 10,000 records

## Error Handling Scenarios

### Validation Errors (400 Bad Request)
- Missing required fields (asset ID, assignment type, assigned to)
- Invalid data format (invalid UUID, invalid email)
- Field length violations (assigned to > 255 characters)
- Invalid assignment type (not USER or LOCATION)

### Authorization Errors (403 Forbidden)
- User lacks required role for operation
- User not authenticated

### Not Found Errors (404 Not Found)
- Asset does not exist
- Assignment record does not exist

### Conflict Errors (409 Conflict)
- Asset already assigned (when creating new assignment)
- Asset not assigned (when deallocating)
- Concurrent assignment conflict

### Unprocessable Entity Errors (422 Unprocessable Entity)
- Asset status not assignable (ORDERED, RECEIVED, MAINTENANCE, RETIRED)
- Asset state changed since ticket creation

### Server Errors (500 Internal Server Error)
- Database connection failure
- Audit service unavailable
- Unexpected system errors

## Audit Logging Requirements

All allocation operations must be logged with:
- User ID of the user performing the operation
- Timestamp of the operation
- Action type (CREATE, UPDATE, DELETE, FAILED_AUTHORIZATION, EXPORT)
- Resource type (ASSIGNMENT)
- Resource ID (assignment record ID)
- Metadata including:
  - Asset ID
  - Assignment type
  - Assigned to value
  - Ticket ID (for ticket-driven operations)
  - IP address of the request

Audit logs must be:
- Immutable (no updates or deletes)
- Created within the same transaction as the allocation operation
- Retained for minimum 7 years for compliance

## Security Requirements

### Authentication
- All API endpoints require valid JWT token
- Token must not be expired
- Token must contain valid user ID

### Authorization
- Write operations (assign, deallocate, reassign): ADMINISTRATOR or ASSET_MANAGER role
- Read operations (view history, query): ADMINISTRATOR, ASSET_MANAGER, or VIEWER role
- Export operations: ADMINISTRATOR or ASSET_MANAGER role

### Data Protection
- Do not log sensitive user information in audit logs
- Validate all inputs to prevent SQL injection
- Sanitize user inputs to prevent XSS attacks
- Use parameterized queries for all database operations

### Rate Limiting
- Authenticated users: 1000 requests per hour
- Failed authorization attempts: Log and monitor for security threats

## API Endpoints Summary

```
POST   /api/v1/assets/{id}/assignments          - Create assignment
DELETE /api/v1/assets/{id}/assignments          - Deallocate asset
GET    /api/v1/assets/{id}/assignment-history   - View assignment history
GET    /api/v1/assignments/user/{userId}        - Query assets by user
GET    /api/v1/assignments/location/{location}  - Query assets by location
GET    /api/v1/assignments/statistics           - Get assignment statistics
GET    /api/v1/assignments/export               - Export assignment data
POST   /api/v1/assignments/bulk-deallocate      - Bulk deallocate assets
```

## Database Schema Summary

### AssignmentHistory Table
- Id: UNIQUEIDENTIFIER (Primary Key)
- AssetId: UNIQUEIDENTIFIER (Foreign Key to Assets)
- AssignmentType: NVARCHAR(20) - USER or LOCATION
- AssignedTo: NVARCHAR(255) - User name or location name
- AssignedBy: UNIQUEIDENTIFIER (Foreign Key to Users)
- AssignedAt: DATETIME2 (Default: current timestamp)
- UnassignedAt: DATETIME2 (Nullable)

### Indexes
- IX_AssignmentHistory_AssetId (AssetId)
- IX_AssignmentHistory_AssignedTo (AssignedTo)
- IX_AssignmentHistory_AssignedAt (AssignedAt)

## Correctness Properties

The following correctness properties from the team structure document apply to this module:

- **Property 18**: Assignment creation generates unique identifier and persists all fields
- **Property 19**: Assignment history maintains chronological order and completeness
- **Property 20**: Deallocation properly closes assignment records and clears asset fields

These properties will be validated through property-based testing using jqwik (backend) and fast-check (frontend).
