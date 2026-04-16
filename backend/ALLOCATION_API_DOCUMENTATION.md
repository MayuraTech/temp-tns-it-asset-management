# Allocation Management API Documentation

## Overview

The Allocation Management API provides endpoints for managing asset assignments to users and locations. This includes creating assignments, deallocating assets, viewing assignment history, and generating statistics and reports.

**Base URL**: `/api/v1`

**Authentication**: All endpoints require JWT authentication via Bearer token in the Authorization header.

**Authorization Roles**:
- **Administrator**: Full access to all operations
- **Asset_Manager**: Can create, update, and view assignments
- **Viewer**: Can only view assignment data

---

## Endpoints

### 1. Create Asset Assignment

**POST** `/assets/{id}/assignments`

Creates a new assignment for an asset to either a user or a location.

**Authorization**: Administrator, Asset_Manager

**Path Parameters**:
- `id` (UUID, required): The asset ID

**Request Body**:
```json
{
  "assignmentType": "USER",
  "assignedTo": "John Doe",
  "assignedUserEmail": "john.doe@example.com"
}
```

**Request Body Fields**:
- `assignmentType` (string, required): Either "USER" or "LOCATION"
- `assignedTo` (string, required): User name or location name (max 255 characters)
- `assignedUserEmail` (string, required for USER type): Valid email address

**Response** (201 Created):
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "assetId": "123e4567-e89b-12d3-a456-426614174000",
  "assignmentType": "USER",
  "assignedTo": "John Doe",
  "assignedBy": "7c9e6679-7425-40de-944b-e07fc1f90ae7",
  "assignedByUsername": "admin",
  "assignedAt": "2024-01-15T10:30:00Z",
  "unassignedAt": null,
  "active": true
}
```

**Error Responses**:
- `400 Bad Request`: Validation error (missing required fields, invalid email format)
- `403 Forbidden`: Insufficient permissions
- `404 Not Found`: Asset not found
- `409 Conflict`: Asset already assigned
- `422 Unprocessable Entity`: Asset not in assignable status (must be IN_USE, DEPLOYED, or STORAGE)

**Example Request**:
```bash
curl -X POST "https://api.example.com/api/v1/assets/123e4567-e89b-12d3-a456-426614174000/assignments" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "assignmentType": "USER",
    "assignedTo": "John Doe",
    "assignedUserEmail": "john.doe@example.com"
  }'
```

---

### 2. Deallocate Asset

**DELETE** `/assets/{id}/assignments`

Removes the current assignment from an asset, making it available for reassignment.

**Authorization**: Administrator, Asset_Manager

**Path Parameters**:
- `id` (UUID, required): The asset ID

**Response** (204 No Content): Empty body

**Error Responses**:
- `403 Forbidden`: Insufficient permissions
- `404 Not Found`: Asset not found or not currently assigned

**Example Request**:
```bash
curl -X DELETE "https://api.example.com/api/v1/assets/123e4567-e89b-12d3-a456-426614174000/assignments" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

---

### 3. Get Assignment History

**GET** `/assets/{id}/assignment-history`

Retrieves the complete assignment history for an asset, including both active and historical assignments.

**Authorization**: Administrator, Asset_Manager, Viewer

**Path Parameters**:
- `id` (UUID, required): The asset ID

**Query Parameters**:
- `page` (integer, optional): Page number (0-indexed, default: 0)
- `size` (integer, optional): Page size (default: 20, max: 100)
- `sort` (string, optional): Sort field and direction (default: "assignedAt,desc")

**Response** (200 OK):
```json
{
  "content": [
    {
      "id": "550e8400-e29b-41d4-a716-446655440000",
      "assetId": "123e4567-e89b-12d3-a456-426614174000",
      "assignmentType": "USER",
      "assignedTo": "John Doe",
      "assignedBy": "7c9e6679-7425-40de-944b-e07fc1f90ae7",
      "assignedByUsername": "admin",
      "assignedAt": "2024-01-15T10:30:00Z",
      "unassignedAt": "2024-01-20T14:00:00Z",
      "active": false
    },
    {
      "id": "660e8400-e29b-41d4-a716-446655440001",
      "assetId": "123e4567-e89b-12d3-a456-426614174000",
      "assignmentType": "LOCATION",
      "assignedTo": "Data Center A",
      "assignedBy": "7c9e6679-7425-40de-944b-e07fc1f90ae7",
      "assignedByUsername": "admin",
      "assignedAt": "2024-01-10T09:00:00Z",
      "unassignedAt": "2024-01-15T10:30:00Z",
      "active": false
    }
  ],
  "page": {
    "size": 20,
    "number": 0,
    "totalElements": 2,
    "totalPages": 1
  }
}
```

**Error Responses**:
- `403 Forbidden`: Insufficient permissions
- `404 Not Found`: Asset not found

**Example Request**:
```bash
curl -X GET "https://api.example.com/api/v1/assets/123e4567-e89b-12d3-a456-426614174000/assignment-history?page=0&size=20" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

---

### 4. Query Assets by User

**GET** `/assignments/user/{userName}`

Retrieves all assets currently assigned to a specific user.

**Authorization**: Administrator, Asset_Manager, Viewer

**Path Parameters**:
- `userName` (string, required): User name (case-insensitive)

**Query Parameters**:
- `page` (integer, optional): Page number (0-indexed, default: 0)
- `size` (integer, optional): Page size (default: 20, max: 100)
- `sort` (string, optional): Sort field and direction (default: "assignmentDate,desc")

**Response** (200 OK):
```json
{
  "content": [
    {
      "id": "123e4567-e89b-12d3-a456-426614174000",
      "assetType": "SERVER",
      "name": "Production Server 01",
      "serialNumber": "SRV-PROD-001",
      "status": "IN_USE",
      "assignedUser": "John Doe",
      "assignedUserEmail": "john.doe@example.com",
      "assignmentDate": "2024-01-15T10:30:00Z",
      "location": null
    }
  ],
  "page": {
    "size": 20,
    "number": 0,
    "totalElements": 1,
    "totalPages": 1
  }
}
```

**Error Responses**:
- `403 Forbidden`: Insufficient permissions

**Example Request**:
```bash
curl -X GET "https://api.example.com/api/v1/assignments/user/John%20Doe?page=0&size=20" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

---

### 5. Query Assets by Location

**GET** `/assignments/location/{location}`

Retrieves all assets currently assigned to a specific location.

**Authorization**: Administrator, Asset_Manager, Viewer

**Path Parameters**:
- `location` (string, required): Location name (case-insensitive)

**Query Parameters**:
- `page` (integer, optional): Page number (0-indexed, default: 0)
- `size` (integer, optional): Page size (default: 20, max: 100)
- `sort` (string, optional): Sort field and direction (default: "locationUpdateDate,desc")

**Response** (200 OK):
```json
{
  "content": [
    {
      "id": "123e4567-e89b-12d3-a456-426614174000",
      "assetType": "SERVER",
      "name": "Storage Server 01",
      "serialNumber": "SRV-STOR-001",
      "status": "STORAGE",
      "assignedUser": null,
      "assignedUserEmail": null,
      "assignmentDate": null,
      "location": "Data Center A",
      "locationUpdateDate": "2024-01-15T10:30:00Z"
    }
  ],
  "page": {
    "size": 20,
    "number": 0,
    "totalElements": 1,
    "totalPages": 1
  }
}
```

**Error Responses**:
- `403 Forbidden`: Insufficient permissions

**Example Request**:
```bash
curl -X GET "https://api.example.com/api/v1/assignments/location/Data%20Center%20A?page=0&size=20" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

---

### 6. Get Assignment Statistics

**GET** `/assignments/statistics`

Retrieves comprehensive assignment statistics including total assigned assets, assignments by type, available assets, and top users/locations.

**Authorization**: Administrator, Asset_Manager

**Response** (200 OK):
```json
{
  "totalAssignedAssets": 150,
  "userAssignments": 120,
  "locationAssignments": 30,
  "availableAssetsByStatus": {
    "IN_USE": 50,
    "DEPLOYED": 30,
    "STORAGE": 20
  },
  "topUsersByAssetCount": [
    {
      "userName": "John Doe",
      "assetCount": 15
    },
    {
      "userName": "Jane Smith",
      "assetCount": 12
    }
  ],
  "topLocationsByAssetCount": [
    {
      "location": "Data Center A",
      "assetCount": 25
    },
    {
      "location": "Office Building B",
      "assetCount": 18
    }
  ]
}
```

**Error Responses**:
- `403 Forbidden`: Insufficient permissions

**Example Request**:
```bash
curl -X GET "https://api.example.com/api/v1/assignments/statistics" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

---

### 7. Export Assignment Data

**GET** `/assignments/export`

Exports current assignment data to CSV format with optional filtering.

**Authorization**: Administrator, Asset_Manager

**Query Parameters** (all optional):
- `assignmentType` (string): Filter by "USER" or "LOCATION"
- `dateFrom` (date): Filter assignments from this date (ISO 8601 format: YYYY-MM-DD)
- `dateTo` (date): Filter assignments to this date (ISO 8601 format: YYYY-MM-DD)
- `assignedBy` (UUID): Filter by user who created the assignment

**Response** (200 OK):
- Content-Type: `text/csv`
- Content-Disposition: `attachment; filename="assignments-export.csv"`

**CSV Format**:
```csv
Asset ID,Asset Name,Serial Number,Asset Type,Assignment Type,Assigned To,Assigned By,Assigned At
123e4567-e89b-12d3-a456-426614174000,Production Server 01,SRV-PROD-001,SERVER,USER,John Doe,admin,2024-01-15T10:30:00Z
234e5678-e89b-12d3-a456-426614174001,Storage Server 01,SRV-STOR-001,SERVER,LOCATION,Data Center A,admin,2024-01-15T11:00:00Z
```

**Error Responses**:
- `400 Bad Request`: Export too large (exceeds 10,000 records) - apply filters to reduce size
- `403 Forbidden`: Insufficient permissions

**Example Request**:
```bash
curl -X GET "https://api.example.com/api/v1/assignments/export?assignmentType=USER&dateFrom=2024-01-01&dateTo=2024-12-31" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -o assignments-export.csv
```

---

### 8. Bulk Deallocate Assets

**POST** `/assignments/bulk-deallocate`

Deallocates multiple assets in a single operation. Each deallocation is processed independently.

**Authorization**: Administrator, Asset_Manager

**Request Body**:
```json
[
  "123e4567-e89b-12d3-a456-426614174000",
  "234e5678-e89b-12d3-a456-426614174001",
  "345e6789-e89b-12d3-a456-426614174002"
]
```

**Request Body**: Array of asset IDs (UUIDs), maximum 50 assets per request

**Response** (200 OK):
```json
{
  "totalRequested": 3,
  "successCount": 2,
  "failureCount": 1,
  "successfulDeallocations": [
    {
      "assetId": "123e4567-e89b-12d3-a456-426614174000",
      "assetName": "Production Server 01",
      "serialNumber": "SRV-PROD-001"
    },
    {
      "assetId": "234e5678-e89b-12d3-a456-426614174001",
      "assetName": "Storage Server 01",
      "serialNumber": "SRV-STOR-001"
    }
  ],
  "failedDeallocations": [
    {
      "assetId": "345e6789-e89b-12d3-a456-426614174002",
      "assetName": "Test Server 01",
      "serialNumber": "SRV-TEST-001",
      "errorType": "ASSET_NOT_ASSIGNED",
      "errorMessage": "Asset is not currently assigned"
    }
  ]
}
```

**Error Responses**:
- `400 Bad Request`: Request exceeds maximum bulk size (50 assets)
- `403 Forbidden`: Insufficient permissions

**Example Request**:
```bash
curl -X POST "https://api.example.com/api/v1/assignments/bulk-deallocate" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '[
    "123e4567-e89b-12d3-a456-426614174000",
    "234e5678-e89b-12d3-a456-426614174001"
  ]'
```

---

## Error Response Format

All error responses follow a consistent format:

```json
{
  "error": {
    "type": "ERROR_TYPE",
    "message": "Human-readable error message",
    "details": {
      "field": "fieldName",
      "message": "Field-specific error message"
    },
    "timestamp": "2024-01-15T10:30:00Z",
    "requestId": "req-123456"
  }
}
```

**Common Error Types**:
- `VALIDATION_ERROR`: Request validation failed
- `RESOURCE_NOT_FOUND`: Requested resource does not exist
- `INSUFFICIENT_PERMISSIONS`: User lacks required permissions
- `ASSET_ALREADY_ASSIGNED`: Asset is already assigned
- `ASSET_NOT_ASSIGNED`: Asset is not currently assigned
- `ASSET_NOT_ASSIGNABLE`: Asset status does not allow assignment
- `BULK_OPERATION_TOO_LARGE`: Bulk request exceeds maximum size
- `EXPORT_TOO_LARGE`: Export exceeds maximum record count

---

## Authentication

All API endpoints require authentication using JWT (JSON Web Token).

**Header Format**:
```
Authorization: Bearer YOUR_JWT_TOKEN
```

**Obtaining a Token**:
```bash
curl -X POST "https://api.example.com/api/v1/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "your-username",
    "password": "your-password"
  }'
```

**Response**:
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "tokenType": "Bearer",
  "expiresIn": 1800
}
```

**Token Expiration**: Access tokens expire after 30 minutes. Use the refresh token to obtain a new access token without re-authenticating.

---

## Rate Limiting

API requests are rate-limited to prevent abuse:

- **Authenticated users**: 1000 requests per hour
- **Per endpoint**: No specific limits, but bulk operations are limited by size

**Rate Limit Headers**:
```
X-RateLimit-Limit: 1000
X-RateLimit-Remaining: 999
X-RateLimit-Reset: 1640000000
```

When rate limit is exceeded:
```
HTTP/1.1 429 Too Many Requests
Retry-After: 3600

{
  "error": {
    "type": "RATE_LIMIT_EXCEEDED",
    "message": "Rate limit exceeded. Please try again later.",
    "timestamp": "2024-01-15T10:30:00Z",
    "requestId": "req-123456"
  }
}
```

---

## Pagination

List endpoints support pagination using query parameters:

**Parameters**:
- `page`: Page number (0-indexed, default: 0)
- `size`: Page size (default: 20, max: 100)
- `sort`: Sort field and direction (e.g., "assignedAt,desc")

**Response Format**:
```json
{
  "content": [ /* array of items */ ],
  "page": {
    "size": 20,
    "number": 0,
    "totalElements": 150,
    "totalPages": 8
  }
}
```

---

## Best Practices

1. **Always use HTTPS** in production environments
2. **Store JWT tokens securely** (HttpOnly cookies recommended)
3. **Implement token refresh** to maintain user sessions
4. **Handle errors gracefully** and display user-friendly messages
5. **Use pagination** for large result sets
6. **Apply filters** when exporting data to reduce response size
7. **Validate input** on the client side before sending requests
8. **Log request IDs** for troubleshooting and support

---

## Support

For API support and questions:
- **Documentation**: https://api.example.com/swagger-ui.html
- **Email**: api-support@example.com
- **Issue Tracker**: https://github.com/company/it-asset-management/issues

---

## Changelog

### Version 1.0.0 (2024-01-15)
- Initial release
- All allocation management endpoints
- JWT authentication
- Role-based authorization
- Comprehensive error handling
- OpenAPI/Swagger documentation
