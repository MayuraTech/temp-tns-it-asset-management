# User Management API Documentation

## Table of Contents

1. [Overview](#overview)
2. [Authentication](#authentication)
3. [API Endpoints](#api-endpoints)
4. [Data Models](#data-models)
5. [Error Handling](#error-handling)
6. [Examples](#examples)
7. [Validation Rules](#validation-rules)

## Overview

The User Management API provides comprehensive user account lifecycle management for the IT Infrastructure Asset Management System. This RESTful API enables secure authentication, authorization, user CRUD operations, role management, and profile self-service capabilities.

### Base URL

```
http://localhost:8080/api/v1
```

### API Version

Version: 1.0.0

### Interactive Documentation

- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **OpenAPI JSON**: http://localhost:8080/v3/api-docs
- **OpenAPI YAML**: http://localhost:8080/v3/api-docs.yaml

## Authentication

### Overview

The API uses JWT (JSON Web Token) based authentication with access and refresh tokens.

### Token Types

| Token Type | Expiration | Purpose |
|------------|------------|---------|
| Access Token | 30 minutes | API request authentication |
| Refresh Token | 24 hours | Obtaining new access tokens |

### Authentication Flow

1. **Login**: POST `/api/v1/auth/login` with username and password
2. **Receive Tokens**: Get access token and refresh token
3. **Use Access Token**: Include in Authorization header for protected endpoints
4. **Refresh Token**: POST `/api/v1/auth/refresh` when access token expires
5. **Logout**: POST `/api/v1/auth/logout` to invalidate session

### Using Access Tokens

Include the access token in the Authorization header of all protected requests:

```http
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

### Security Features

- **Account Locking**: Accounts are locked for 30 minutes after 5 consecutive failed login attempts
- **Password Complexity**: Enforced minimum requirements for password strength
- **Session Tracking**: All active sessions are tracked and can be invalidated
- **Token Rotation**: New refresh tokens are issued when used (enhanced security)

## API Endpoints

### Authentication Endpoints

#### POST /api/v1/auth/login

Authenticates a user with username and password.

**Request Body:**
```json
{
  "username": "admin",
  "password": "Admin@123456"
}
```

**Success Response (200 OK):**
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "tokenType": "Bearer",
  "expiresIn": 1800
}
```

**Error Responses:**
- `400 Bad Request`: Validation error (missing username or password)
- `401 Unauthorized`: Invalid credentials, account locked, or account disabled

**Example Error (Account Locked):**
```json
{
  "error": {
    "type": "ACCOUNT_LOCKED",
    "message": "Account is locked until 2024-01-15T11:00:00Z",
    "details": {
      "lockUntil": "2024-01-15T11:00:00Z",
      "reason": "Too many failed login attempts"
    },
    "timestamp": "2024-01-15T10:30:00Z",
    "requestId": "req-123456"
  }
}
```

---

#### POST /api/v1/auth/logout

Logs out the current authenticated user and invalidates their session.

**Headers:**
```http
Authorization: Bearer {access_token}
```

**Success Response (204 No Content)**

**Error Responses:**
- `401 Unauthorized`: Missing or invalid JWT token

---

#### POST /api/v1/auth/refresh

Obtains a new access token using a valid refresh token.

**Request Body:**
```json
{
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

**Success Response (200 OK):**
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "tokenType": "Bearer",
  "expiresIn": 1800
}
```

**Error Responses:**
- `400 Bad Request`: Missing or invalid refresh token format
- `401 Unauthorized`: Invalid or expired refresh token, or account disabled

---

### User Management Endpoints

#### POST /api/v1/users

Creates a new user account. **Requires ADMINISTRATOR role.**

**Headers:**
```http
Authorization: Bearer {access_token}
```

**Request Body:**
```json
{
  "username": "jdoe",
  "email": "jdoe@example.com",
  "password": "SecurePass123!",
  "roles": ["ASSET_MANAGER"]
}
```

**Success Response (201 Created):**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "username": "jdoe",
  "email": "jdoe@example.com",
  "isActive": true,
  "accountLocked": false,
  "lockUntil": null,
  "lastLoginAt": null,
  "roles": ["ASSET_MANAGER"],
  "createdAt": "2024-01-15T10:30:00Z",
  "updatedAt": "2024-01-15T10:30:00Z",
  "createdBy": "admin",
  "updatedBy": "admin"
}
```

**Error Responses:**
- `400 Bad Request`: Validation error (invalid format or missing required fields)
- `401 Unauthorized`: Missing or invalid JWT token
- `403 Forbidden`: Insufficient permissions (requires ADMINISTRATOR role)
- `409 Conflict`: Username or email already exists

---

#### GET /api/v1/users

Retrieves a paginated list of all users. **Accessible by all authenticated users.**

**Headers:**
```http
Authorization: Bearer {access_token}
```

**Query Parameters:**
- `role` (optional): Filter by role (ADMINISTRATOR, ASSET_MANAGER, VIEWER)
- `page` (optional): Page number (0-indexed, default: 0)
- `size` (optional): Items per page (default: 20, max: 100)
- `sort` (optional): Sort field and direction (e.g., `createdAt,desc`)

**Example Request:**
```http
GET /api/v1/users?role=ADMINISTRATOR&page=0&size=20&sort=createdAt,desc
```

**Success Response (200 OK):**
```json
{
  "content": [
    {
      "id": "550e8400-e29b-41d4-a716-446655440000",
      "username": "admin",
      "email": "admin@example.com",
      "isActive": true,
      "accountLocked": false,
      "roles": ["ADMINISTRATOR"],
      "createdAt": "2024-01-01T00:00:00Z",
      "updatedAt": "2024-01-15T10:30:00Z"
    }
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 20,
    "sort": {
      "sorted": true,
      "unsorted": false,
      "empty": false
    }
  },
  "totalElements": 150,
  "totalPages": 8,
  "last": false,
  "first": true,
  "size": 20,
  "number": 0
}
```

**Error Responses:**
- `401 Unauthorized`: Missing or invalid JWT token

---

#### GET /api/v1/users/{id}

Retrieves a specific user by ID. **Accessible by all authenticated users.**

**Headers:**
```http
Authorization: Bearer {access_token}
```

**Path Parameters:**
- `id`: User ID (UUID format)

**Success Response (200 OK):**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "username": "jdoe",
  "email": "jdoe@example.com",
  "isActive": true,
  "accountLocked": false,
  "lockUntil": null,
  "lastLoginAt": "2024-01-15T09:00:00Z",
  "roles": ["ASSET_MANAGER"],
  "createdAt": "2024-01-10T10:00:00Z",
  "updatedAt": "2024-01-15T10:30:00Z",
  "createdBy": "admin",
  "updatedBy": "admin"
}
```

**Error Responses:**
- `401 Unauthorized`: Missing or invalid JWT token
- `404 Not Found`: User not found

---

#### PUT /api/v1/users/{id}

Updates an existing user account. **Requires ADMINISTRATOR role.**

**Headers:**
```http
Authorization: Bearer {access_token}
```

**Path Parameters:**
- `id`: User ID (UUID format)

**Request Body:**
```json
{
  "username": "jdoe_updated",
  "email": "jdoe_new@example.com"
}
```

**Success Response (200 OK):**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "username": "jdoe_updated",
  "email": "jdoe_new@example.com",
  "isActive": true,
  "accountLocked": false,
  "roles": ["ASSET_MANAGER"],
  "createdAt": "2024-01-10T10:00:00Z",
  "updatedAt": "2024-01-15T11:00:00Z",
  "createdBy": "admin",
  "updatedBy": "admin"
}
```

**Error Responses:**
- `400 Bad Request`: Validation error
- `401 Unauthorized`: Missing or invalid JWT token
- `403 Forbidden`: Insufficient permissions
- `404 Not Found`: User not found
- `409 Conflict`: New username or email already exists

---

#### DELETE /api/v1/users/{id}

Permanently deletes a user account. **Requires ADMINISTRATOR role.**

**Headers:**
```http
Authorization: Bearer {access_token}
```

**Path Parameters:**
- `id`: User ID (UUID format)

**Success Response (204 No Content)**

**Error Responses:**
- `400 Bad Request`: Attempting to delete own account
- `401 Unauthorized`: Missing or invalid JWT token
- `403 Forbidden`: Insufficient permissions
- `404 Not Found`: User not found

---

#### PATCH /api/v1/users/{id}/enable

Enables a user account. **Requires ADMINISTRATOR role.**

**Headers:**
```http
Authorization: Bearer {access_token}
```

**Path Parameters:**
- `id`: User ID (UUID format)

**Success Response (204 No Content)**

**Error Responses:**
- `401 Unauthorized`: Missing or invalid JWT token
- `403 Forbidden`: Insufficient permissions
- `404 Not Found`: User not found

---

#### PATCH /api/v1/users/{id}/disable

Disables a user account and invalidates all sessions. **Requires ADMINISTRATOR role.**

**Headers:**
```http
Authorization: Bearer {access_token}
```

**Path Parameters:**
- `id`: User ID (UUID format)

**Success Response (204 No Content)**

**Error Responses:**
- `400 Bad Request`: Attempting to disable own account
- `401 Unauthorized`: Missing or invalid JWT token
- `403 Forbidden`: Insufficient permissions
- `404 Not Found`: User not found

---

#### POST /api/v1/users/{id}/roles

Assigns a role to a user. **Requires ADMINISTRATOR role.**

**Headers:**
```http
Authorization: Bearer {access_token}
```

**Path Parameters:**
- `id`: User ID (UUID format)

**Request Body:**
```json
{
  "role": "ADMINISTRATOR"
}
```

**Success Response (204 No Content)**

**Error Responses:**
- `400 Bad Request`: User already has the role
- `401 Unauthorized`: Missing or invalid JWT token
- `403 Forbidden`: Insufficient permissions
- `404 Not Found`: User not found

---

#### DELETE /api/v1/users/{id}/roles/{role}

Revokes a role from a user. **Requires ADMINISTRATOR role.**

**Headers:**
```http
Authorization: Bearer {access_token}
```

**Path Parameters:**
- `id`: User ID (UUID format)
- `role`: Role to revoke (ADMINISTRATOR, ASSET_MANAGER, or VIEWER)

**Success Response (204 No Content)**

**Error Responses:**
- `400 Bad Request`: User doesn't have the role, it's their last role, or admin is revoking their own ADMINISTRATOR role
- `401 Unauthorized`: Missing or invalid JWT token
- `403 Forbidden`: Insufficient permissions
- `404 Not Found`: User not found

---

### Profile Management Endpoints

#### GET /api/v1/profile

Retrieves the current user's profile. **Accessible by all authenticated users.**

**Headers:**
```http
Authorization: Bearer {access_token}
```

**Success Response (200 OK):**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "username": "jdoe",
  "email": "jdoe@example.com",
  "isActive": true,
  "accountLocked": false,
  "lastLoginAt": "2024-01-15T09:00:00Z",
  "roles": ["ASSET_MANAGER"],
  "createdAt": "2024-01-10T10:00:00Z",
  "updatedAt": "2024-01-15T10:30:00Z"
}
```

**Error Responses:**
- `401 Unauthorized`: Missing or invalid JWT token
- `404 Not Found`: User not found

---

#### PUT /api/v1/profile

Updates the current user's profile. **Accessible by all authenticated users.**

**Headers:**
```http
Authorization: Bearer {access_token}
```

**Request Body:**
```json
{
  "email": "jdoe_new@example.com"
}
```

**Success Response (200 OK):**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "username": "jdoe",
  "email": "jdoe_new@example.com",
  "isActive": true,
  "accountLocked": false,
  "roles": ["ASSET_MANAGER"],
  "createdAt": "2024-01-10T10:00:00Z",
  "updatedAt": "2024-01-15T11:00:00Z"
}
```

**Error Responses:**
- `400 Bad Request`: Invalid email format
- `401 Unauthorized`: Missing or invalid JWT token
- `404 Not Found`: User not found
- `409 Conflict`: Email already exists

---

#### POST /api/v1/profile/change-password

Changes the current user's password. **Accessible by all authenticated users.**

**Headers:**
```http
Authorization: Bearer {access_token}
```

**Request Body:**
```json
{
  "currentPassword": "OldPass123!",
  "newPassword": "NewSecurePass456!"
}
```

**Success Response (204 No Content)**

**Note**: All active sessions are invalidated after password change.

**Error Responses:**
- `400 Bad Request`: Current password incorrect, new password invalid, or new password same as current
- `401 Unauthorized`: Missing or invalid JWT token
- `404 Not Found`: User not found

---

## Data Models

### UserDTO

```json
{
  "id": "string (UUID)",
  "username": "string (3-100 chars)",
  "email": "string (valid email)",
  "isActive": "boolean",
  "accountLocked": "boolean",
  "lockUntil": "string (ISO 8601 datetime) | null",
  "lastLoginAt": "string (ISO 8601 datetime) | null",
  "roles": ["string (Role enum)"],
  "createdAt": "string (ISO 8601 datetime)",
  "updatedAt": "string (ISO 8601 datetime)",
  "createdBy": "string (username) | null",
  "updatedBy": "string (username) | null"
}
```

### TokenResponse

```json
{
  "accessToken": "string (JWT)",
  "refreshToken": "string (JWT)",
  "tokenType": "string (always 'Bearer')",
  "expiresIn": "number (seconds)"
}
```

### ErrorResponse

```json
{
  "error": {
    "type": "string (error type)",
    "message": "string (human-readable message)",
    "details": "object (additional error details)",
    "timestamp": "string (ISO 8601 datetime)",
    "requestId": "string (request correlation ID)"
  }
}
```

### Role Enum

- `ADMINISTRATOR`: Full access to all operations
- `ASSET_MANAGER`: View users, cannot modify
- `VIEWER`: View users, cannot modify

---

## Error Handling

### HTTP Status Codes

| Code | Meaning | Usage |
|------|---------|-------|
| 200 OK | Success | GET, PUT successful |
| 201 Created | Resource created | POST successful |
| 204 No Content | Success, no body | DELETE, PATCH successful |
| 400 Bad Request | Invalid request | Validation errors |
| 401 Unauthorized | Not authenticated | Missing/invalid token |
| 403 Forbidden | Not authorized | Insufficient permissions |
| 404 Not Found | Resource not found | Invalid ID |
| 409 Conflict | Resource conflict | Duplicate username/email |
| 500 Internal Server Error | Server error | Unexpected errors |

### Error Types

| Error Type | Description | HTTP Status |
|------------|-------------|-------------|
| VALIDATION_ERROR | Request validation failed | 400 |
| AUTHENTICATION_ERROR | Invalid credentials or token | 401 |
| ACCOUNT_LOCKED | Account locked due to failed attempts | 401 |
| ACCOUNT_DISABLED | Account is inactive | 401 |
| INSUFFICIENT_PERMISSIONS | User lacks required permissions | 403 |
| DUPLICATE_USERNAME | Username already exists | 409 |
| DUPLICATE_EMAIL | Email already exists | 409 |
| USER_NOT_FOUND | User ID not found | 404 |
| INVALID_STATUS_TRANSITION | Invalid account status change | 400 |

### Validation Error Details

Validation errors include detailed field-level error information:

```json
{
  "error": {
    "type": "VALIDATION_ERROR",
    "message": "Validation failed",
    "details": [
      {
        "field": "username",
        "message": "Username must be between 3 and 100 characters"
      },
      {
        "field": "password",
        "message": "Password must contain at least 8 characters, one uppercase letter, one lowercase letter, one digit, and one special character"
      }
    ],
    "timestamp": "2024-01-15T10:30:00Z",
    "requestId": "req-123456"
  }
}
```

---

## Examples

### Complete Authentication Flow

```bash
# 1. Login
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "Admin@123456"
  }'

# Response:
# {
#   "accessToken": "eyJhbGc...",
#   "refreshToken": "eyJhbGc...",
#   "tokenType": "Bearer",
#   "expiresIn": 1800
# }

# 2. Use access token for protected requests
curl -X GET http://localhost:8080/api/v1/users \
  -H "Authorization: Bearer eyJhbGc..."

# 3. Refresh token when access token expires
curl -X POST http://localhost:8080/api/v1/auth/refresh \
  -H "Content-Type: application/json" \
  -d '{
    "refreshToken": "eyJhbGc..."
  }'

# 4. Logout
curl -X POST http://localhost:8080/api/v1/auth/logout \
  -H "Authorization: Bearer eyJhbGc..."
```

### Create User

```bash
curl -X POST http://localhost:8080/api/v1/users \
  -H "Authorization: Bearer eyJhbGc..." \
  -H "Content-Type: application/json" \
  -d '{
    "username": "jdoe",
    "email": "jdoe@example.com",
    "password": "SecurePass123!",
    "roles": ["ASSET_MANAGER"]
  }'
```

### List Users with Pagination

```bash
curl -X GET "http://localhost:8080/api/v1/users?page=0&size=20&sort=createdAt,desc" \
  -H "Authorization: Bearer eyJhbGc..."
```

### Update User Profile

```bash
curl -X PUT http://localhost:8080/api/v1/profile \
  -H "Authorization: Bearer eyJhbGc..." \
  -H "Content-Type: application/json" \
  -d '{
    "email": "newemail@example.com"
  }'
```

### Change Password

```bash
curl -X POST http://localhost:8080/api/v1/profile/change-password \
  -H "Authorization: Bearer eyJhbGc..." \
  -H "Content-Type: application/json" \
  -d '{
    "currentPassword": "OldPass123!",
    "newPassword": "NewSecurePass456!"
  }'
```

### Assign Role

```bash
curl -X POST http://localhost:8080/api/v1/users/550e8400-e29b-41d4-a716-446655440000/roles \
  -H "Authorization: Bearer eyJhbGc..." \
  -H "Content-Type: application/json" \
  -d '{
    "role": "ADMINISTRATOR"
  }'
```

---

## Validation Rules

### Username

- **Required**: Yes
- **Length**: 3-100 characters
- **Pattern**: Alphanumeric and underscores only (`^[a-zA-Z0-9_]+$`)
- **Unique**: Yes (case-sensitive)

### Email

- **Required**: Yes
- **Length**: 5-255 characters
- **Format**: Valid email format
- **Unique**: Yes (case-insensitive)

### Password

- **Required**: Yes (for user creation)
- **Minimum Length**: 8 characters
- **Requirements**:
  - At least one uppercase letter
  - At least one lowercase letter
  - At least one digit
  - At least one special character (@$!%*?&)
- **Pattern**: `^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&])[A-Za-z\d@$!%*?&]{8,}$`

### Roles

- **Required**: Yes (at least one role)
- **Valid Values**: ADMINISTRATOR, ASSET_MANAGER, VIEWER
- **Business Rules**:
  - Users must always have at least one role
  - Cannot revoke last role from user
  - Administrators cannot revoke their own ADMINISTRATOR role

### Account Status

- **isActive**: Boolean (default: true)
- **accountLocked**: Boolean (default: false)
- **Business Rules**:
  - Inactive accounts cannot authenticate
  - Locked accounts cannot authenticate until lock expires
  - Administrators cannot disable their own accounts

---

## Rate Limiting

The API implements rate limiting to prevent abuse:

- **Authenticated users**: 1000 requests per hour
- **Unauthenticated users**: 100 requests per hour

Rate limit information is included in response headers:

```http
X-RateLimit-Limit: 1000
X-RateLimit-Remaining: 999
X-RateLimit-Reset: 1640000000
```

When rate limit is exceeded, the API returns:

```http
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

## Security Best Practices

1. **Always use HTTPS** in production environments
2. **Store tokens securely** (HttpOnly cookies recommended for web applications)
3. **Implement token refresh** before access token expires to maintain seamless user experience
4. **Handle 401 responses** by redirecting to login page
5. **Never log or expose** password values or hashes
6. **Validate all inputs** on both client and server sides
7. **Follow password complexity** requirements strictly
8. **Implement proper error handling** without exposing sensitive information
9. **Use request IDs** for error correlation and debugging
10. **Monitor authentication failures** for security threats

---

## Support and Resources

- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **OpenAPI Specification**: http://localhost:8080/v3/api-docs
- **Frontend Integration Guide**: See `API_USAGE_GUIDE.md`
- **Support Email**: support@example.com

---

**Last Updated**: 2024-01-15  
**API Version**: 1.0.0
