# Error Codes Reference - User Management API

## Overview

This document provides a comprehensive reference for all error codes, validation rules, and error responses in the User Management API. All errors follow a consistent structure and provide actionable information for troubleshooting.

## Error Response Structure

All API errors return a consistent JSON structure:

```json
{
  "error": {
    "type": "ERROR_TYPE",
    "message": "Human-readable error message",
    "details": {},
    "timestamp": "2024-01-15T10:30:00Z",
    "requestId": "req-123456"
  }
}
```

### Error Response Fields

| Field | Type | Description |
|-------|------|-------------|
| `type` | string | Error type identifier (see Error Types below) |
| `message` | string | Human-readable error message |
| `details` | object/array | Additional error details (field-specific for validation errors) |
| `timestamp` | string | ISO 8601 timestamp when error occurred |
| `requestId` | string | Unique request identifier for correlation and debugging |

---

## HTTP Status Codes

### Success Codes (2xx)

| Code | Status | Usage | Endpoints |
|------|--------|-------|-----------|
| 200 | OK | Successful GET, PUT, PATCH | GET /users, PUT /users/{id}, etc. |
| 201 | Created | Resource created successfully | POST /users |
| 204 | No Content | Successful operation with no response body | DELETE /users/{id}, PATCH /users/{id}/enable |

### Client Error Codes (4xx)

| Code | Status | Usage | Common Causes |
|------|--------|-------|---------------|
| 400 | Bad Request | Invalid request format or validation failure | Missing required fields, invalid data format |
| 401 | Unauthorized | Authentication required or failed | Missing token, invalid credentials, expired token |
| 403 | Forbidden | Insufficient permissions | User lacks required role for operation |
| 404 | Not Found | Resource not found | Invalid user ID, resource doesn't exist |
| 409 | Conflict | Resource conflict | Duplicate username/email, invalid state transition |
| 422 | Unprocessable Entity | Invalid business logic | Cannot delete last role, self-modification prevention |
| 429 | Too Many Requests | Rate limit exceeded | Too many requests in time window |

### Server Error Codes (5xx)

| Code | Status | Usage | Common Causes |
|------|--------|-------|---------------|
| 500 | Internal Server Error | Unexpected server error | Database connection failure, unhandled exception |
| 503 | Service Unavailable | Service temporarily unavailable | Maintenance mode, database down |

---

## Error Types

### Authentication Errors

#### AUTHENTICATION_ERROR

**HTTP Status**: 401 Unauthorized

**Description**: Invalid credentials or authentication token.

**Common Causes**:
- Incorrect username or password
- Invalid JWT token format
- Expired access token
- Malformed Authorization header

**Example Response**:
```json
{
  "error": {
    "type": "AUTHENTICATION_ERROR",
    "message": "Invalid username or password",
    "timestamp": "2024-01-15T10:30:00Z",
    "requestId": "req-123456"
  }
}
```

**Resolution**:
- Verify username and password are correct
- Check token format in Authorization header
- Refresh access token if expired
- Re-authenticate if token is invalid

---

#### ACCOUNT_LOCKED

**HTTP Status**: 401 Unauthorized

**Description**: User account is temporarily locked due to multiple failed login attempts.

**Common Causes**:
- 5 or more consecutive failed login attempts
- Account locked for 30 minutes from last failed attempt

**Example Response**:
```json
{
  "error": {
    "type": "ACCOUNT_LOCKED",
    "message": "Account is locked until 2024-01-15T11:00:00Z",
    "details": {
      "lockUntil": "2024-01-15T11:00:00Z",
      "reason": "Too many failed login attempts",
      "failedAttempts": 5
    },
    "timestamp": "2024-01-15T10:30:00Z",
    "requestId": "req-123456"
  }
}
```

**Resolution**:
- Wait until `lockUntil` time has passed
- Contact administrator to manually unlock account
- Ensure correct password is used for future attempts

---

#### ACCOUNT_DISABLED

**HTTP Status**: 401 Unauthorized

**Description**: User account has been disabled by an administrator.

**Common Causes**:
- Administrator explicitly disabled the account
- Account deactivated due to policy violation
- Temporary suspension

**Example Response**:
```json
{
  "error": {
    "type": "ACCOUNT_DISABLED",
    "message": "Your account has been disabled. Please contact support.",
    "details": {
      "disabledAt": "2024-01-15T09:00:00Z",
      "disabledBy": "admin"
    },
    "timestamp": "2024-01-15T10:30:00Z",
    "requestId": "req-123456"
  }
}
```

**Resolution**:
- Contact system administrator or support team
- Account must be re-enabled by administrator via PATCH /users/{id}/enable

---

#### INVALID_REFRESH_TOKEN

**HTTP Status**: 401 Unauthorized

**Description**: Refresh token is invalid, expired, or has been revoked.

**Common Causes**:
- Refresh token has expired (24-hour lifetime)
- Token has been invalidated due to logout
- Token has been invalidated due to password change
- Malformed refresh token

**Example Response**:
```json
{
  "error": {
    "type": "INVALID_REFRESH_TOKEN",
    "message": "Refresh token is invalid or expired",
    "timestamp": "2024-01-15T10:30:00Z",
    "requestId": "req-123456"
  }
}
```

**Resolution**:
- Re-authenticate via POST /auth/login to obtain new tokens
- Ensure refresh token is stored and transmitted correctly

---

### Authorization Errors

#### INSUFFICIENT_PERMISSIONS

**HTTP Status**: 403 Forbidden

**Description**: User lacks the required role or permission to perform the requested operation.

**Common Causes**:
- User does not have required role (e.g., ADMINISTRATOR)
- Operation requires higher privileges than user possesses
- Role-based access control (RBAC) restriction

**Example Response**:
```json
{
  "error": {
    "type": "INSUFFICIENT_PERMISSIONS",
    "message": "You do not have permission to perform this action",
    "details": {
      "requiredRoles": ["ADMINISTRATOR"],
      "userRoles": ["ASSET_MANAGER"],
      "operation": "CREATE_USER"
    },
    "timestamp": "2024-01-15T10:30:00Z",
    "requestId": "req-123456"
  }
}
```

**Resolution**:
- Contact administrator to request required role
- Verify operation is allowed for your role
- Check API documentation for role requirements

---

### Validation Errors

#### VALIDATION_ERROR

**HTTP Status**: 400 Bad Request

**Description**: Request data failed validation rules.

**Common Causes**:
- Missing required fields
- Invalid data format
- Data exceeds length limits
- Pattern mismatch (e.g., invalid email format)
- Business rule violation

**Example Response**:
```json
{
  "error": {
    "type": "VALIDATION_ERROR",
    "message": "Validation failed",
    "details": [
      {
        "field": "username",
        "message": "Username must be between 3 and 100 characters",
        "value": "ab"
      },
      {
        "field": "email",
        "message": "Invalid email format",
        "value": "invalid-email"
      },
      {
        "field": "password",
        "message": "Password must contain at least 8 characters, one uppercase letter, one lowercase letter, one digit, and one special character",
        "value": null
      }
    ],
    "timestamp": "2024-01-15T10:30:00Z",
    "requestId": "req-123456"
  }
}
```

**Resolution**:
- Review `details` array for specific field errors
- Correct each field according to validation message
- Refer to Validation Rules section below for requirements

---

### Resource Errors

#### USER_NOT_FOUND

**HTTP Status**: 404 Not Found

**Description**: Requested user ID does not exist in the system.

**Common Causes**:
- Invalid or non-existent user ID
- User has been deleted
- Typo in user ID

**Example Response**:
```json
{
  "error": {
    "type": "USER_NOT_FOUND",
    "message": "User not found with ID: 550e8400-e29b-41d4-a716-446655440000",
    "details": {
      "userId": "550e8400-e29b-41d4-a716-446655440000"
    },
    "timestamp": "2024-01-15T10:30:00Z",
    "requestId": "req-123456"
  }
}
```

**Resolution**:
- Verify user ID is correct
- Check if user still exists via GET /users
- Use valid user ID from user list

---

#### DUPLICATE_USERNAME

**HTTP Status**: 409 Conflict

**Description**: Username already exists in the system.

**Common Causes**:
- Attempting to create user with existing username
- Attempting to update username to one that already exists
- Username uniqueness constraint violation

**Example Response**:
```json
{
  "error": {
    "type": "DUPLICATE_USERNAME",
    "message": "Username already exists: jdoe",
    "details": {
      "username": "jdoe",
      "field": "username"
    },
    "timestamp": "2024-01-15T10:30:00Z",
    "requestId": "req-123456"
  }
}
```

**Resolution**:
- Choose a different username
- Check existing usernames via GET /users
- Username must be unique across all users (case-sensitive)

---

#### DUPLICATE_EMAIL

**HTTP Status**: 409 Conflict

**Description**: Email address already exists in the system.

**Common Causes**:
- Attempting to create user with existing email
- Attempting to update email to one that already exists
- Email uniqueness constraint violation

**Example Response**:
```json
{
  "error": {
    "type": "DUPLICATE_EMAIL",
    "message": "Email already exists: jdoe@example.com",
    "details": {
      "email": "jdoe@example.com",
      "field": "email"
    },
    "timestamp": "2024-01-15T10:30:00Z",
    "requestId": "req-123456"
  }
}
```

**Resolution**:
- Use a different email address
- Check existing emails via GET /users
- Email must be unique across all users (case-insensitive)

---

### Business Logic Errors

#### SELF_MODIFICATION_PREVENTED

**HTTP Status**: 400 Bad Request

**Description**: User attempted to perform a restricted operation on their own account.

**Common Causes**:
- Administrator attempting to delete own account
- Administrator attempting to disable own account
- Administrator attempting to revoke own ADMINISTRATOR role

**Example Response**:
```json
{
  "error": {
    "type": "SELF_MODIFICATION_PREVENTED",
    "message": "You cannot delete your own account",
    "details": {
      "operation": "DELETE_USER",
      "userId": "550e8400-e29b-41d4-a716-446655440000",
      "reason": "Self-deletion is not allowed to prevent accidental lockout"
    },
    "timestamp": "2024-01-15T10:30:00Z",
    "requestId": "req-123456"
  }
}
```

**Resolution**:
- Have another administrator perform the operation
- Cannot perform restricted operations on own account
- This is a security feature to prevent accidental lockout

---

#### LAST_ROLE_REVOCATION_PREVENTED

**HTTP Status**: 400 Bad Request

**Description**: Attempted to revoke the last role from a user.

**Common Causes**:
- User has only one role and attempting to revoke it
- Business rule: users must always have at least one role

**Example Response**:
```json
{
  "error": {
    "type": "LAST_ROLE_REVOCATION_PREVENTED",
    "message": "Cannot revoke last role from user. Users must have at least one role.",
    "details": {
      "userId": "550e8400-e29b-41d4-a716-446655440000",
      "currentRoles": ["ASSET_MANAGER"],
      "attemptedRevocation": "ASSET_MANAGER"
    },
    "timestamp": "2024-01-15T10:30:00Z",
    "requestId": "req-123456"
  }
}
```

**Resolution**:
- Assign a different role before revoking current role
- Users must always have at least one role
- Cannot leave user without any roles

---

#### ROLE_ALREADY_ASSIGNED

**HTTP Status**: 400 Bad Request

**Description**: User already has the role being assigned.

**Common Causes**:
- Attempting to assign a role that user already possesses
- Duplicate role assignment attempt

**Example Response**:
```json
{
  "error": {
    "type": "ROLE_ALREADY_ASSIGNED",
    "message": "User already has role: ADMINISTRATOR",
    "details": {
      "userId": "550e8400-e29b-41d4-a716-446655440000",
      "role": "ADMINISTRATOR",
      "currentRoles": ["ADMINISTRATOR", "ASSET_MANAGER"]
    },
    "timestamp": "2024-01-15T10:30:00Z",
    "requestId": "req-123456"
  }
}
```

**Resolution**:
- Check user's current roles via GET /users/{id}
- No action needed if role is already assigned
- Users cannot have duplicate role assignments

---

#### ROLE_NOT_ASSIGNED

**HTTP Status**: 400 Bad Request

**Description**: Attempted to revoke a role that user doesn't have.

**Common Causes**:
- User doesn't have the role being revoked
- Role was already revoked
- Incorrect role specified

**Example Response**:
```json
{
  "error": {
    "type": "ROLE_NOT_ASSIGNED",
    "message": "User does not have role: VIEWER",
    "details": {
      "userId": "550e8400-e29b-41d4-a716-446655440000",
      "role": "VIEWER",
      "currentRoles": ["ADMINISTRATOR", "ASSET_MANAGER"]
    },
    "timestamp": "2024-01-15T10:30:00Z",
    "requestId": "req-123456"
  }
}
```

**Resolution**:
- Verify user's current roles via GET /users/{id}
- Ensure correct role is specified
- Cannot revoke role that user doesn't have

---

#### INVALID_PASSWORD

**HTTP Status**: 400 Bad Request

**Description**: Password does not meet complexity requirements or current password is incorrect.

**Common Causes**:
- Password too short (< 8 characters)
- Missing required character types
- Current password incorrect (for password change)
- New password same as current password

**Example Response**:
```json
{
  "error": {
    "type": "INVALID_PASSWORD",
    "message": "Current password is incorrect",
    "details": {
      "reason": "CURRENT_PASSWORD_MISMATCH"
    },
    "timestamp": "2024-01-15T10:30:00Z",
    "requestId": "req-123456"
  }
}
```

**Resolution**:
- Verify current password is correct
- Ensure new password meets complexity requirements:
  - Minimum 8 characters
  - At least one uppercase letter
  - At least one lowercase letter
  - At least one digit
  - At least one special character (@$!%*?&)
- New password must be different from current password

---

### Rate Limiting Errors

#### RATE_LIMIT_EXCEEDED

**HTTP Status**: 429 Too Many Requests

**Description**: API rate limit has been exceeded.

**Common Causes**:
- Too many requests in short time period
- Authenticated users: > 1000 requests per hour
- Unauthenticated users: > 100 requests per hour

**Example Response**:
```json
{
  "error": {
    "type": "RATE_LIMIT_EXCEEDED",
    "message": "Rate limit exceeded. Please try again later.",
    "details": {
      "limit": 1000,
      "remaining": 0,
      "resetAt": "2024-01-15T11:00:00Z"
    },
    "timestamp": "2024-01-15T10:30:00Z",
    "requestId": "req-123456"
  }
}
```

**Response Headers**:
```http
X-RateLimit-Limit: 1000
X-RateLimit-Remaining: 0
X-RateLimit-Reset: 1640000000
Retry-After: 3600
```

**Resolution**:
- Wait until rate limit resets (see `resetAt` or `Retry-After` header)
- Implement exponential backoff in client
- Reduce request frequency
- Cache responses when possible

---

### Server Errors

#### INTERNAL_SERVER_ERROR

**HTTP Status**: 500 Internal Server Error

**Description**: Unexpected server error occurred.

**Common Causes**:
- Database connection failure
- Unhandled exception
- Configuration error
- Resource exhaustion

**Example Response**:
```json
{
  "error": {
    "type": "INTERNAL_SERVER_ERROR",
    "message": "An unexpected error occurred. Please try again later.",
    "timestamp": "2024-01-15T10:30:00Z",
    "requestId": "req-123456"
  }
}
```

**Resolution**:
- Retry request after brief delay
- Contact support if error persists
- Provide `requestId` for troubleshooting
- Check system status page

---

## Validation Rules

### Username Validation

| Rule | Requirement | Error Message |
|------|-------------|---------------|
| Required | Must not be null or empty | "Username is required" |
| Length | 3-100 characters | "Username must be between 3 and 100 characters" |
| Pattern | Alphanumeric and underscores only | "Username must contain only alphanumeric characters and underscores" |
| Uniqueness | Must be unique (case-sensitive) | "Username already exists: {username}" |

**Valid Examples**:
- `admin`
- `john_doe`
- `user123`
- `Asset_Manager_01`

**Invalid Examples**:
- `ab` (too short)
- `john-doe` (contains hyphen)
- `user@123` (contains @ symbol)
- `john doe` (contains space)

**Regex Pattern**: `^[a-zA-Z0-9_]+$`

---

### Email Validation

| Rule | Requirement | Error Message |
|------|-------------|---------------|
| Required | Must not be null or empty | "Email is required" |
| Length | 5-255 characters | "Email must be between 5 and 255 characters" |
| Format | Valid email format | "Invalid email format" |
| Uniqueness | Must be unique (case-insensitive) | "Email already exists: {email}" |

**Valid Examples**:
- `user@example.com`
- `john.doe@company.co.uk`
- `admin+test@domain.com`

**Invalid Examples**:
- `user` (missing @ and domain)
- `user@` (missing domain)
- `@example.com` (missing local part)
- `user @example.com` (contains space)

---

### Password Validation

| Rule | Requirement | Error Message |
|------|-------------|---------------|
| Required | Must not be null or empty | "Password is required" |
| Minimum Length | At least 8 characters | "Password must be at least 8 characters" |
| Uppercase | At least one uppercase letter | "Password must contain at least one uppercase letter" |
| Lowercase | At least one lowercase letter | "Password must contain at least one lowercase letter" |
| Digit | At least one digit | "Password must contain at least one digit" |
| Special Character | At least one special character (@$!%*?&) | "Password must contain at least one special character" |
| Complexity | All above requirements | "Password must contain at least 8 characters, one uppercase letter, one lowercase letter, one digit, and one special character" |

**Valid Examples**:
- `SecurePass123!`
- `MyP@ssw0rd`
- `Admin@123456`

**Invalid Examples**:
- `password` (no uppercase, digit, or special char)
- `PASSWORD123!` (no lowercase)
- `Pass123!` (too short)
- `Password123` (no special character)

**Regex Pattern**: `^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&])[A-Za-z\d@$!%*?&]{8,}$`

---

### Role Validation

| Rule | Requirement | Error Message |
|------|-------------|---------------|
| Required | At least one role must be assigned | "At least one role is required" |
| Valid Values | Must be ADMINISTRATOR, ASSET_MANAGER, or VIEWER | "Invalid role: {role}" |
| Minimum Roles | User must always have at least one role | "Cannot revoke last role from user" |
| Uniqueness | User cannot have duplicate roles | "User already has role: {role}" |

**Valid Roles**:
- `ADMINISTRATOR`: Full access to all operations
- `ASSET_MANAGER`: View users, cannot modify
- `VIEWER`: View users, cannot modify

---

### Account Status Validation

| Rule | Requirement | Error Message |
|------|-------------|---------------|
| isActive | Boolean value | "Invalid account status" |
| accountLocked | Boolean value | "Invalid lock status" |
| lockUntil | Valid ISO 8601 datetime or null | "Invalid lock expiration time" |
| Self-Modification | Cannot disable own account | "You cannot disable your own account" |

---

## Request ID Correlation

All error responses include a `requestId` field for correlation and debugging:

```json
{
  "error": {
    "type": "VALIDATION_ERROR",
    "message": "Validation failed",
    "details": [...],
    "timestamp": "2024-01-15T10:30:00Z",
    "requestId": "req-123456"
  }
}
```

### Using Request IDs

1. **Client-Side Logging**: Log request IDs for all API calls
2. **Error Reporting**: Include request ID when reporting errors to support
3. **Server-Side Correlation**: Request IDs are logged server-side for troubleshooting
4. **Debugging**: Use request ID to trace request through system logs

### Request ID Format

- Format: `req-{timestamp}-{random}`
- Example: `req-1705318200000-a1b2c3d4`
- Unique per request
- Included in all error responses and server logs

---

## Best Practices

### Error Handling in Client Applications

1. **Always Check HTTP Status Code**
   ```typescript
   if (response.status >= 400) {
     // Handle error
   }
   ```

2. **Parse Error Response**
   ```typescript
   const error = response.error?.error;
   const errorType = error?.type;
   const errorMessage = error?.message;
   const errorDetails = error?.details;
   ```

3. **Handle Specific Error Types**
   ```typescript
   switch (errorType) {
     case 'ACCOUNT_LOCKED':
       // Show lock expiration time
       break;
     case 'VALIDATION_ERROR':
       // Show field-specific errors
       break;
     case 'INSUFFICIENT_PERMISSIONS':
       // Redirect to unauthorized page
       break;
   }
   ```

4. **Display User-Friendly Messages**
   ```typescript
   const userMessage = this.getUserFriendlyMessage(errorType, errorMessage);
   this.showError(userMessage);
   ```

5. **Log Request IDs**
   ```typescript
   console.error(`Error ${errorType}: ${errorMessage} (Request ID: ${requestId})`);
   ```

### Retry Logic

Implement exponential backoff for transient errors:

```typescript
async function retryWithBackoff(fn, maxRetries = 3) {
  for (let i = 0; i < maxRetries; i++) {
    try {
      return await fn();
    } catch (error) {
      if (i === maxRetries - 1) throw error;
      
      const shouldRetry = [500, 503, 429].includes(error.status);
      if (!shouldRetry) throw error;
      
      const delay = Math.pow(2, i) * 1000; // Exponential backoff
      await new Promise(resolve => setTimeout(resolve, delay));
    }
  }
}
```

### Error Monitoring

1. **Track Error Rates**: Monitor error types and frequencies
2. **Alert on Anomalies**: Set up alerts for unusual error patterns
3. **Analyze Trends**: Review error logs regularly
4. **User Impact**: Track which errors affect users most

---

## Support and Troubleshooting

### Getting Help

1. **Check Documentation**: Review API documentation and this error reference
2. **Search Logs**: Use request ID to find detailed server logs
3. **Contact Support**: Provide request ID and error details
4. **Report Bugs**: Include request ID, error type, and reproduction steps

### Support Channels

- **Email**: support@example.com
- **Documentation**: http://localhost:8080/swagger-ui.html
- **Status Page**: https://status.example.com

---

**Last Updated**: 2024-01-15  
**Document Version**: 1.0.0
