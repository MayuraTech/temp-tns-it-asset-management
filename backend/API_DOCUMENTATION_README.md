# User Management API - Documentation Overview

## Welcome

This document provides an overview of the complete API documentation suite for the User Management module of the IT Infrastructure Asset Management System. All documentation is comprehensive, up-to-date, and designed to help developers integrate with the API quickly and effectively.

---

## Documentation Structure

The API documentation is organized into several specialized documents, each serving a specific purpose:

### 1. **API_DOCUMENTATION.md** - Core API Reference
**Purpose**: Complete REST API endpoint reference with request/response examples

**Contents**:
- Authentication endpoints and flow
- User management CRUD operations
- Profile management endpoints
- Data models and schemas
- HTTP status codes
- Pagination and filtering
- Rate limiting
- Security best practices

**Use When**:
- Learning the API structure
- Looking up endpoint details
- Understanding request/response formats
- Implementing API calls

**Quick Links**:
- [Authentication](#authentication)
- [User Management](#user-management)
- [Profile Management](#profile-management)
- [Error Handling](#error-handling)

---

### 2. **ERROR_CODES_REFERENCE.md** - Complete Error Reference
**Purpose**: Comprehensive guide to all error codes, validation rules, and troubleshooting

**Contents**:
- All error types with descriptions
- HTTP status code mappings
- Validation rules for all fields
- Error response structures
- Resolution steps for each error
- Request ID correlation
- Best practices for error handling

**Use When**:
- Debugging API errors
- Understanding validation failures
- Implementing error handling
- Troubleshooting issues
- Looking up validation requirements

**Key Sections**:
- Authentication Errors (ACCOUNT_LOCKED, AUTHENTICATION_ERROR, etc.)
- Authorization Errors (INSUFFICIENT_PERMISSIONS)
- Validation Errors (VALIDATION_ERROR with field details)
- Resource Errors (USER_NOT_FOUND, DUPLICATE_USERNAME, etc.)
- Business Logic Errors (SELF_MODIFICATION_PREVENTED, etc.)
- Complete validation rules for username, email, password, roles

---

### 3. **API_USAGE_GUIDE.md** - Frontend Integration Guide
**Purpose**: Practical guide for Angular/TypeScript frontend developers

**Contents**:
- Complete Angular service implementations
- TypeScript interfaces and models
- HTTP interceptors for auth and errors
- Form validation examples
- Error handling patterns
- Route guards
- Best practices
- Common patterns and examples

**Use When**:
- Integrating API into Angular application
- Implementing authentication flow
- Creating services and components
- Handling errors in frontend
- Setting up interceptors and guards

**Includes**:
- AuthService with token management
- UserService with all CRUD operations
- ProfileService for self-service
- ErrorService for error handling
- Complete component examples
- Form validation patterns

---

### 4. **POSTMAN_COLLECTION_GUIDE.md** - API Testing Guide
**Purpose**: Complete Postman collection for testing all endpoints

**Contents**:
- Pre-configured request examples
- Environment setup instructions
- Collection-level authorization
- Automated tests for each endpoint
- Testing scenarios and workflows
- Pre-request scripts
- Tips and best practices

**Use When**:
- Testing API endpoints
- Exploring API functionality
- Debugging integration issues
- Running automated tests
- Learning API behavior

**Features**:
- Complete authentication flow
- All CRUD operations
- Error scenario testing
- Role management testing
- Profile management testing
- Automated token management

---

### 5. **OpenAPI/Swagger Documentation** - Interactive API Docs
**Purpose**: Interactive, auto-generated API documentation

**Access**:
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **OpenAPI JSON**: http://localhost:8080/v3/api-docs
- **OpenAPI YAML**: http://localhost:8080/v3/api-docs.yaml

**Features**:
- Interactive API testing
- Try-it-out functionality
- Schema definitions
- Example requests/responses
- Authentication configuration
- Real-time API exploration

**Use When**:
- Exploring API interactively
- Testing endpoints in browser
- Viewing schema definitions
- Generating client code
- Sharing API specs

---

## Quick Start Guide

### For Backend Developers

1. **Start with**: `API_DOCUMENTATION.md`
   - Understand API structure and endpoints
   - Review data models and schemas

2. **Reference**: `ERROR_CODES_REFERENCE.md`
   - Implement proper error handling
   - Understand validation rules

3. **Test with**: Swagger UI or Postman
   - Test endpoints interactively
   - Verify implementations

### For Frontend Developers

1. **Start with**: `API_USAGE_GUIDE.md`
   - Copy service implementations
   - Understand integration patterns

2. **Reference**: `API_DOCUMENTATION.md`
   - Look up endpoint details
   - Understand request/response formats

3. **Handle Errors**: `ERROR_CODES_REFERENCE.md`
   - Implement error handling
   - Display user-friendly messages

4. **Test with**: `POSTMAN_COLLECTION_GUIDE.md`
   - Test integration scenarios
   - Debug issues

### For QA/Testers

1. **Start with**: `POSTMAN_COLLECTION_GUIDE.md`
   - Import collection into Postman
   - Run automated tests

2. **Reference**: `ERROR_CODES_REFERENCE.md`
   - Verify error scenarios
   - Test validation rules

3. **Explore**: Swagger UI
   - Interactive testing
   - Edge case exploration

---

## API Overview

### Base URL

```
http://localhost:8080/api/v1
```

### Authentication

The API uses JWT (JSON Web Token) based authentication:

- **Access Token**: 30-minute expiration, used for API requests
- **Refresh Token**: 24-hour expiration, used to obtain new access tokens

**Authentication Flow**:
1. Login: `POST /auth/login` → Receive tokens
2. Use: Include `Authorization: Bearer {accessToken}` in requests
3. Refresh: `POST /auth/refresh` → Get new tokens
4. Logout: `POST /auth/logout` → Invalidate session

### Roles and Permissions

| Role | Permissions |
|------|-------------|
| **ADMINISTRATOR** | Full access to all operations |
| **ASSET_MANAGER** | View users, cannot modify |
| **VIEWER** | View users, cannot modify |
| **All Users** | View/update own profile, change own password |

### Key Features

- ✅ JWT-based stateless authentication
- ✅ Role-based access control (RBAC)
- ✅ Account locking after failed attempts
- ✅ Password complexity enforcement
- ✅ Session tracking and management
- ✅ Comprehensive audit logging
- ✅ Pagination and filtering
- ✅ Rate limiting
- ✅ Detailed error responses

---

## Endpoint Summary

### Authentication Endpoints

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| POST | `/auth/login` | User login | No |
| POST | `/auth/logout` | User logout | Yes |
| POST | `/auth/refresh` | Refresh access token | No |

### User Management Endpoints

| Method | Endpoint | Description | Required Role |
|--------|----------|-------------|---------------|
| POST | `/users` | Create user | ADMINISTRATOR |
| GET | `/users` | List users | All authenticated |
| GET | `/users/{id}` | Get user by ID | All authenticated |
| PUT | `/users/{id}` | Update user | ADMINISTRATOR |
| DELETE | `/users/{id}` | Delete user | ADMINISTRATOR |
| PATCH | `/users/{id}/enable` | Enable user | ADMINISTRATOR |
| PATCH | `/users/{id}/disable` | Disable user | ADMINISTRATOR |
| POST | `/users/{id}/roles` | Assign role | ADMINISTRATOR |
| DELETE | `/users/{id}/roles/{role}` | Revoke role | ADMINISTRATOR |

### Profile Management Endpoints

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| GET | `/profile` | Get own profile | Yes |
| PUT | `/profile` | Update own profile | Yes |
| POST | `/profile/change-password` | Change own password | Yes |

---

## Common Use Cases

### 1. User Registration and Setup

```
1. Admin logs in → POST /auth/login
2. Admin creates user → POST /users
3. Admin assigns roles → POST /users/{id}/roles
4. User logs in → POST /auth/login
5. User updates profile → PUT /profile
```

### 2. Authentication Flow

```
1. User logs in → POST /auth/login
2. Access protected resources → GET /users (with token)
3. Token expires → POST /auth/refresh
4. Continue accessing resources
5. User logs out → POST /auth/logout
```

### 3. User Management

```
1. List all users → GET /users
2. Filter by role → GET /users?role=ADMINISTRATOR
3. View user details → GET /users/{id}
4. Update user info → PUT /users/{id}
5. Manage user status → PATCH /users/{id}/enable or /disable
```

### 4. Role Management

```
1. View user roles → GET /users/{id}
2. Assign new role → POST /users/{id}/roles
3. Revoke role → DELETE /users/{id}/roles/{role}
4. Verify permissions → User sessions invalidated, must re-login
```

### 5. Profile Self-Service

```
1. View own profile → GET /profile
2. Update email → PUT /profile
3. Change password → POST /profile/change-password
4. Re-login required → POST /auth/login (sessions invalidated)
```

---

## Data Models

### User

```typescript
interface User {
  id: string;                    // UUID
  username: string;              // 3-100 chars, alphanumeric + underscores
  email: string;                 // Valid email format
  isActive: boolean;             // Account status
  accountLocked: boolean;        // Lock status
  lockUntil: string | null;      // Lock expiration (ISO 8601)
  lastLoginAt: string | null;    // Last login timestamp
  roles: Role[];                 // Assigned roles
  createdAt: string;             // Creation timestamp
  updatedAt: string;             // Last update timestamp
  createdBy?: string;            // Creator username
  updatedBy?: string;            // Last updater username
}
```

### Role

```typescript
enum Role {
  ADMINISTRATOR = 'ADMINISTRATOR',
  ASSET_MANAGER = 'ASSET_MANAGER',
  VIEWER = 'VIEWER'
}
```

### Error Response

```typescript
interface ErrorResponse {
  error: {
    type: string;              // Error type identifier
    message: string;           // Human-readable message
    details?: any;             // Additional error details
    timestamp: string;         // ISO 8601 timestamp
    requestId: string;         // Request correlation ID
  }
}
```

---

## Validation Rules Summary

### Username
- **Length**: 3-100 characters
- **Pattern**: Alphanumeric and underscores only (`^[a-zA-Z0-9_]+$`)
- **Unique**: Yes (case-sensitive)

### Email
- **Length**: 5-255 characters
- **Format**: Valid email format
- **Unique**: Yes (case-insensitive)

### Password
- **Minimum Length**: 8 characters
- **Requirements**:
  - At least one uppercase letter
  - At least one lowercase letter
  - At least one digit
  - At least one special character (@$!%*?&)
- **Pattern**: `^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&])[A-Za-z\d@$!%*?&]{8,}$`

### Roles
- **Valid Values**: ADMINISTRATOR, ASSET_MANAGER, VIEWER
- **Minimum**: At least one role required
- **Uniqueness**: No duplicate roles per user

---

## Error Handling

### Common Error Types

| Error Type | HTTP Status | Description |
|------------|-------------|-------------|
| `AUTHENTICATION_ERROR` | 401 | Invalid credentials or token |
| `ACCOUNT_LOCKED` | 401 | Account locked after failed attempts |
| `ACCOUNT_DISABLED` | 401 | Account disabled by admin |
| `INSUFFICIENT_PERMISSIONS` | 403 | User lacks required role |
| `VALIDATION_ERROR` | 400 | Request validation failed |
| `USER_NOT_FOUND` | 404 | User ID not found |
| `DUPLICATE_USERNAME` | 409 | Username already exists |
| `DUPLICATE_EMAIL` | 409 | Email already exists |

### Error Response Example

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
      }
    ],
    "timestamp": "2024-01-15T10:30:00Z",
    "requestId": "req-123456"
  }
}
```

---

## Security Considerations

### Authentication Security

1. **Token Storage**: Store tokens securely (HttpOnly cookies recommended)
2. **Token Expiration**: Access tokens expire after 30 minutes
3. **Token Refresh**: Implement automatic token refresh
4. **Logout**: Always call logout endpoint to invalidate sessions

### Password Security

1. **Complexity**: Enforce strong password requirements
2. **Hashing**: Passwords hashed with BCrypt (strength 10)
3. **Never Logged**: Passwords never logged or exposed in responses
4. **Change Impact**: Password changes invalidate all sessions

### Account Security

1. **Account Locking**: 5 failed attempts → 30-minute lock
2. **Session Tracking**: All sessions tracked and can be invalidated
3. **Role Changes**: Role changes invalidate sessions
4. **Self-Modification**: Users cannot delete/disable own accounts

### API Security

1. **HTTPS Only**: Always use HTTPS in production
2. **Rate Limiting**: 1000 requests/hour (authenticated), 100/hour (unauthenticated)
3. **Authorization**: All endpoints enforce role-based access control
4. **Input Validation**: All inputs validated server-side

---

## Performance Considerations

### Pagination

- **Default Page Size**: 20 items
- **Maximum Page Size**: 100 items
- **Sorting**: Supports multi-field sorting
- **Filtering**: Supports role-based filtering

### Caching

- **Token Caching**: Cache tokens client-side
- **User Data**: Cache user lists with appropriate TTL
- **Profile Data**: Cache profile data, invalidate on updates

### Rate Limiting

- **Authenticated**: 1000 requests per hour
- **Unauthenticated**: 100 requests per hour
- **Headers**: Rate limit info in response headers

---

## Testing

### Unit Testing

- All service methods have unit tests
- Mock external dependencies
- Test success and error scenarios
- Verify validation rules

### Integration Testing

- Test complete API workflows
- Verify database operations
- Test authentication and authorization
- Verify error handling

### End-to-End Testing

- Test complete user journeys
- Verify frontend-backend integration
- Test error scenarios
- Verify security controls

### Postman Testing

- Import Postman collection
- Run automated tests
- Verify all endpoints
- Test error scenarios

---

## Support and Resources

### Documentation

- **API Reference**: `API_DOCUMENTATION.md`
- **Error Reference**: `ERROR_CODES_REFERENCE.md`
- **Frontend Guide**: `API_USAGE_GUIDE.md`
- **Postman Guide**: `POSTMAN_COLLECTION_GUIDE.md`
- **Swagger UI**: http://localhost:8080/swagger-ui.html

### Support Channels

- **Email**: support@example.com
- **Documentation**: http://localhost:8080/swagger-ui.html
- **Issue Tracker**: (Your issue tracker URL)

### Additional Resources

- **Requirements**: `.kiro/specs/module1-user-management/requirements.md`
- **Design**: `.kiro/specs/module1-user-management/design.md`
- **Tasks**: `.kiro/specs/module1-user-management/tasks.md`

---

## Version History

### Version 1.0.0 (2024-01-15)

**Initial Release**:
- Complete authentication system
- User CRUD operations
- Role management
- Profile self-service
- Comprehensive error handling
- Complete documentation suite

**Features**:
- JWT-based authentication
- Role-based access control
- Account locking
- Password complexity
- Session management
- Audit logging
- Pagination and filtering
- Rate limiting

---

## Feedback and Contributions

We welcome feedback and contributions to improve the API and documentation:

1. **Report Issues**: Use issue tracker for bugs and feature requests
2. **Suggest Improvements**: Email suggestions to support team
3. **Documentation Updates**: Submit documentation improvements
4. **Code Contributions**: Follow contribution guidelines

---

## License

Proprietary - IT Infrastructure Asset Management System

---

**Last Updated**: 2024-01-15  
**API Version**: 1.0.0  
**Documentation Version**: 1.0.0

---

## Quick Reference Card

### Authentication
```bash
# Login
POST /api/v1/auth/login
Body: { "username": "admin", "password": "Admin@123456" }

# Logout
POST /api/v1/auth/logout
Header: Authorization: Bearer {token}

# Refresh
POST /api/v1/auth/refresh
Body: { "refreshToken": "{refresh_token}" }
```

### User Management
```bash
# Create User
POST /api/v1/users
Body: { "username": "jdoe", "email": "jdoe@example.com", "password": "Pass123!", "roles": ["ASSET_MANAGER"] }

# List Users
GET /api/v1/users?page=0&size=20&sort=createdAt,desc

# Get User
GET /api/v1/users/{id}

# Update User
PUT /api/v1/users/{id}
Body: { "username": "jdoe_updated", "email": "new@example.com" }

# Delete User
DELETE /api/v1/users/{id}
```

### Profile Management
```bash
# Get Profile
GET /api/v1/profile

# Update Profile
PUT /api/v1/profile
Body: { "email": "newemail@example.com" }

# Change Password
POST /api/v1/profile/change-password
Body: { "currentPassword": "Old123!", "newPassword": "New456!" }
```

---

**End of Documentation Overview**
