# Postman Collection Guide - User Management API

## Overview

This guide provides a complete Postman collection for testing the User Management API. Import this collection into Postman to quickly test all endpoints with pre-configured requests and examples.

## Table of Contents

1. [Setup Instructions](#setup-instructions)
2. [Environment Variables](#environment-variables)
3. [Collection Structure](#collection-structure)
4. [Authentication Flow](#authentication-flow)
5. [Complete Request Examples](#complete-request-examples)
6. [Testing Scenarios](#testing-scenarios)

---

## Setup Instructions

### 1. Import Collection

Create a new collection in Postman named "User Management API" and add the requests below.

### 2. Configure Environment

Create a new environment named "User Management - Local" with the following variables:

| Variable | Initial Value | Current Value |
|----------|---------------|---------------|
| `baseUrl` | `http://localhost:8080/api/v1` | `http://localhost:8080/api/v1` |
| `accessToken` | | (auto-populated after login) |
| `refreshToken` | | (auto-populated after login) |
| `userId` | | (auto-populated after user creation) |

### 3. Collection-Level Authorization

Set up collection-level authorization:
- Type: Bearer Token
- Token: `{{accessToken}}`

This will automatically add the Authorization header to all requests.

---

## Environment Variables

### Base URL

```
{{baseUrl}} = http://localhost:8080/api/v1
```

### Authentication Tokens

```
{{accessToken}} = (populated after login)
{{refreshToken}} = (populated after login)
```

### Dynamic IDs

```
{{userId}} = (populated after user creation)
```

---

## Collection Structure

```
User Management API/
├── Authentication/
│   ├── Login
│   ├── Logout
│   └── Refresh Token
├── User Management/
│   ├── Create User
│   ├── Get All Users
│   ├── Get User by ID
│   ├── Update User
│   ├── Delete User
│   ├── Enable User
│   ├── Disable User
│   ├── Assign Role
│   └── Revoke Role
└── Profile Management/
    ├── Get Profile
    ├── Update Profile
    └── Change Password
```

---

## Authentication Flow

### Step 1: Login

**Request**: POST `{{baseUrl}}/auth/login`

**Headers**:
```
Content-Type: application/json
```

**Body** (raw JSON):
```json
{
  "username": "admin",
  "password": "Admin@123456"
}
```

**Tests** (Postman Tests tab):
```javascript
// Parse response
const response = pm.response.json();

// Save tokens to environment
if (response.accessToken) {
    pm.environment.set("accessToken", response.accessToken);
}

if (response.refreshToken) {
    pm.environment.set("refreshToken", response.refreshToken);
}

// Verify response
pm.test("Status code is 200", function () {
    pm.response.to.have.status(200);
});

pm.test("Response has accessToken", function () {
    pm.expect(response).to.have.property("accessToken");
});

pm.test("Response has refreshToken", function () {
    pm.expect(response).to.have.property("refreshToken");
});

pm.test("Token type is Bearer", function () {
    pm.expect(response.tokenType).to.eql("Bearer");
});
```

**Expected Response** (200 OK):
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "tokenType": "Bearer",
  "expiresIn": 1800
}
```

---

### Step 2: Use Access Token

All subsequent requests automatically use `{{accessToken}}` via collection-level authorization.

---

### Step 3: Refresh Token

**Request**: POST `{{baseUrl}}/auth/refresh`

**Headers**:
```
Content-Type: application/json
```

**Body** (raw JSON):
```json
{
  "refreshToken": "{{refreshToken}}"
}
```

**Tests**:
```javascript
const response = pm.response.json();

if (response.accessToken) {
    pm.environment.set("accessToken", response.accessToken);
}

if (response.refreshToken) {
    pm.environment.set("refreshToken", response.refreshToken);
}

pm.test("Status code is 200", function () {
    pm.response.to.have.status(200);
});
```

---

### Step 4: Logout

**Request**: POST `{{baseUrl}}/auth/logout`

**Headers**:
```
Authorization: Bearer {{accessToken}}
```

**Tests**:
```javascript
pm.test("Status code is 204", function () {
    pm.response.to.have.status(204);
});

// Clear tokens from environment
pm.environment.unset("accessToken");
pm.environment.unset("refreshToken");
```

---

## Complete Request Examples

### User Management Endpoints

#### 1. Create User

**Request**: POST `{{baseUrl}}/users`

**Headers**:
```
Content-Type: application/json
Authorization: Bearer {{accessToken}}
```

**Body** (raw JSON):
```json
{
  "username": "jdoe",
  "email": "jdoe@example.com",
  "password": "SecurePass123!",
  "roles": ["ASSET_MANAGER"]
}
```

**Tests**:
```javascript
const response = pm.response.json();

// Save user ID for subsequent requests
if (response.id) {
    pm.environment.set("userId", response.id);
}

pm.test("Status code is 201", function () {
    pm.response.to.have.status(201);
});

pm.test("Response has user ID", function () {
    pm.expect(response).to.have.property("id");
});

pm.test("Username matches request", function () {
    pm.expect(response.username).to.eql("jdoe");
});

pm.test("Email matches request", function () {
    pm.expect(response.email).to.eql("jdoe@example.com");
});

pm.test("User is active", function () {
    pm.expect(response.isActive).to.be.true;
});

pm.test("Account is not locked", function () {
    pm.expect(response.accountLocked).to.be.false;
});

pm.test("Roles include ASSET_MANAGER", function () {
    pm.expect(response.roles).to.include("ASSET_MANAGER");
});
```

**Expected Response** (201 Created):
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

---

#### 2. Get All Users

**Request**: GET `{{baseUrl}}/users?page=0&size=20&sort=createdAt,desc`

**Headers**:
```
Authorization: Bearer {{accessToken}}
```

**Query Parameters**:
- `page`: 0
- `size`: 20
- `sort`: createdAt,desc
- `role`: (optional) ADMINISTRATOR, ASSET_MANAGER, or VIEWER

**Tests**:
```javascript
const response = pm.response.json();

pm.test("Status code is 200", function () {
    pm.response.to.have.status(200);
});

pm.test("Response has content array", function () {
    pm.expect(response).to.have.property("content");
    pm.expect(response.content).to.be.an("array");
});

pm.test("Response has pagination info", function () {
    pm.expect(response).to.have.property("totalElements");
    pm.expect(response).to.have.property("totalPages");
    pm.expect(response).to.have.property("size");
    pm.expect(response).to.have.property("number");
});
```

**Expected Response** (200 OK):
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

---

#### 3. Get User by ID

**Request**: GET `{{baseUrl}}/users/{{userId}}`

**Headers**:
```
Authorization: Bearer {{accessToken}}
```

**Tests**:
```javascript
const response = pm.response.json();

pm.test("Status code is 200", function () {
    pm.response.to.have.status(200);
});

pm.test("Response has user ID", function () {
    pm.expect(response).to.have.property("id");
});

pm.test("User ID matches request", function () {
    pm.expect(response.id).to.eql(pm.environment.get("userId"));
});
```

---

#### 4. Update User

**Request**: PUT `{{baseUrl}}/users/{{userId}}`

**Headers**:
```
Content-Type: application/json
Authorization: Bearer {{accessToken}}
```

**Body** (raw JSON):
```json
{
  "username": "jdoe_updated",
  "email": "jdoe_new@example.com"
}
```

**Tests**:
```javascript
const response = pm.response.json();

pm.test("Status code is 200", function () {
    pm.response.to.have.status(200);
});

pm.test("Username updated", function () {
    pm.expect(response.username).to.eql("jdoe_updated");
});

pm.test("Email updated", function () {
    pm.expect(response.email).to.eql("jdoe_new@example.com");
});
```

---

#### 5. Delete User

**Request**: DELETE `{{baseUrl}}/users/{{userId}}`

**Headers**:
```
Authorization: Bearer {{accessToken}}
```

**Tests**:
```javascript
pm.test("Status code is 204", function () {
    pm.response.to.have.status(204);
});

// Clear user ID from environment
pm.environment.unset("userId");
```

---

#### 6. Enable User

**Request**: PATCH `{{baseUrl}}/users/{{userId}}/enable`

**Headers**:
```
Authorization: Bearer {{accessToken}}
```

**Tests**:
```javascript
pm.test("Status code is 204", function () {
    pm.response.to.have.status(204);
});
```

---

#### 7. Disable User

**Request**: PATCH `{{baseUrl}}/users/{{userId}}/disable`

**Headers**:
```
Authorization: Bearer {{accessToken}}
```

**Tests**:
```javascript
pm.test("Status code is 204", function () {
    pm.response.to.have.status(204);
});
```

---

#### 8. Assign Role

**Request**: POST `{{baseUrl}}/users/{{userId}}/roles`

**Headers**:
```
Content-Type: application/json
Authorization: Bearer {{accessToken}}
```

**Body** (raw JSON):
```json
{
  "role": "ADMINISTRATOR"
}
```

**Tests**:
```javascript
pm.test("Status code is 204", function () {
    pm.response.to.have.status(204);
});
```

---

#### 9. Revoke Role

**Request**: DELETE `{{baseUrl}}/users/{{userId}}/roles/VIEWER`

**Headers**:
```
Authorization: Bearer {{accessToken}}
```

**Tests**:
```javascript
pm.test("Status code is 204", function () {
    pm.response.to.have.status(204);
});
```

---

### Profile Management Endpoints

#### 1. Get Profile

**Request**: GET `{{baseUrl}}/profile`

**Headers**:
```
Authorization: Bearer {{accessToken}}
```

**Tests**:
```javascript
const response = pm.response.json();

pm.test("Status code is 200", function () {
    pm.response.to.have.status(200);
});

pm.test("Response has user data", function () {
    pm.expect(response).to.have.property("id");
    pm.expect(response).to.have.property("username");
    pm.expect(response).to.have.property("email");
    pm.expect(response).to.have.property("roles");
});
```

---

#### 2. Update Profile

**Request**: PUT `{{baseUrl}}/profile`

**Headers**:
```
Content-Type: application/json
Authorization: Bearer {{accessToken}}
```

**Body** (raw JSON):
```json
{
  "email": "newemail@example.com"
}
```

**Tests**:
```javascript
const response = pm.response.json();

pm.test("Status code is 200", function () {
    pm.response.to.have.status(200);
});

pm.test("Email updated", function () {
    pm.expect(response.email).to.eql("newemail@example.com");
});
```

---

#### 3. Change Password

**Request**: POST `{{baseUrl}}/profile/change-password`

**Headers**:
```
Content-Type: application/json
Authorization: Bearer {{accessToken}}
```

**Body** (raw JSON):
```json
{
  "currentPassword": "OldPass123!",
  "newPassword": "NewSecurePass456!"
}
```

**Tests**:
```javascript
pm.test("Status code is 204", function () {
    pm.response.to.have.status(204);
});

// Note: All sessions are invalidated after password change
// You will need to login again
pm.environment.unset("accessToken");
pm.environment.unset("refreshToken");
```

---

## Testing Scenarios

### Scenario 1: Complete User Lifecycle

1. **Login as Administrator**
   - POST /auth/login
   - Save access token

2. **Create New User**
   - POST /users
   - Save user ID

3. **Verify User Created**
   - GET /users/{id}
   - Verify user details

4. **Update User Email**
   - PUT /users/{id}
   - Verify email updated

5. **Assign Additional Role**
   - POST /users/{id}/roles
   - Verify role assigned

6. **Disable User**
   - PATCH /users/{id}/disable
   - Verify user disabled

7. **Enable User**
   - PATCH /users/{id}/enable
   - Verify user enabled

8. **Delete User**
   - DELETE /users/{id}
   - Verify user deleted

9. **Logout**
   - POST /auth/logout

---

### Scenario 2: Authentication Flow

1. **Login with Valid Credentials**
   - POST /auth/login
   - Verify tokens received

2. **Access Protected Endpoint**
   - GET /users
   - Verify access granted

3. **Wait for Token Expiration** (or manually expire)
   - GET /users
   - Verify 401 Unauthorized

4. **Refresh Access Token**
   - POST /auth/refresh
   - Verify new tokens received

5. **Access Protected Endpoint Again**
   - GET /users
   - Verify access granted

6. **Logout**
   - POST /auth/logout
   - Verify tokens invalidated

---

### Scenario 3: Error Handling

1. **Login with Invalid Credentials**
   - POST /auth/login with wrong password
   - Verify 401 Unauthorized
   - Verify error type: AUTHENTICATION_ERROR

2. **Create User with Duplicate Username**
   - POST /users with existing username
   - Verify 409 Conflict
   - Verify error type: DUPLICATE_USERNAME

3. **Create User with Invalid Password**
   - POST /users with weak password
   - Verify 400 Bad Request
   - Verify error type: VALIDATION_ERROR

4. **Access Endpoint Without Token**
   - GET /users without Authorization header
   - Verify 401 Unauthorized

5. **Perform Admin Operation as Non-Admin**
   - Login as ASSET_MANAGER
   - POST /users
   - Verify 403 Forbidden
   - Verify error type: INSUFFICIENT_PERMISSIONS

---

### Scenario 4: Role Management

1. **Login as Administrator**
   - POST /auth/login

2. **Create User with Single Role**
   - POST /users with VIEWER role

3. **Assign Additional Role**
   - POST /users/{id}/roles with ASSET_MANAGER

4. **Verify User Has Both Roles**
   - GET /users/{id}
   - Verify roles array contains both

5. **Attempt to Assign Duplicate Role**
   - POST /users/{id}/roles with ASSET_MANAGER again
   - Verify 400 Bad Request
   - Verify error type: ROLE_ALREADY_ASSIGNED

6. **Revoke One Role**
   - DELETE /users/{id}/roles/VIEWER

7. **Attempt to Revoke Last Role**
   - DELETE /users/{id}/roles/ASSET_MANAGER
   - Verify 400 Bad Request
   - Verify error type: LAST_ROLE_REVOCATION_PREVENTED

---

### Scenario 5: Profile Management

1. **Login as Regular User**
   - POST /auth/login

2. **Get Own Profile**
   - GET /profile
   - Verify profile data

3. **Update Email**
   - PUT /profile with new email
   - Verify email updated

4. **Change Password**
   - POST /profile/change-password
   - Verify 204 No Content

5. **Verify Session Invalidated**
   - GET /profile
   - Verify 401 Unauthorized

6. **Login with New Password**
   - POST /auth/login with new password
   - Verify login successful

---

## Pre-Request Scripts

### Collection-Level Pre-Request Script

Add this to the collection's Pre-request Script tab:

```javascript
// Log request details
console.log(`${pm.request.method} ${pm.request.url}`);

// Add timestamp to request
pm.request.headers.add({
    key: 'X-Request-Timestamp',
    value: new Date().toISOString()
});

// Generate request ID
const requestId = 'req-' + Date.now() + '-' + Math.random().toString(36).substr(2, 9);
pm.request.headers.add({
    key: 'X-Request-ID',
    value: requestId
});
pm.environment.set('lastRequestId', requestId);
```

---

## Collection Variables

Add these to the collection variables:

| Variable | Value | Description |
|----------|-------|-------------|
| `adminUsername` | `admin` | Default admin username |
| `adminPassword` | `Admin@123456` | Default admin password |
| `testUsername` | `testuser` | Test user username |
| `testEmail` | `test@example.com` | Test user email |
| `testPassword` | `TestPass123!` | Test user password |

---

## Export Collection

To export this collection:

1. Click on the collection name
2. Click the three dots (...)
3. Select "Export"
4. Choose "Collection v2.1"
5. Save as `User_Management_API.postman_collection.json`

---

## Import into Postman

To import:

1. Open Postman
2. Click "Import" button
3. Select the exported JSON file
4. Collection will be imported with all requests and tests

---

## Tips and Best Practices

### 1. Use Environment Variables

Always use environment variables for:
- Base URLs
- Tokens
- Dynamic IDs
- Test data

### 2. Write Tests

Add tests to verify:
- HTTP status codes
- Response structure
- Data correctness
- Error handling

### 3. Use Pre-Request Scripts

Use pre-request scripts to:
- Generate dynamic data
- Set up test prerequisites
- Log request details

### 4. Organize Requests

Group related requests into folders:
- Authentication
- User Management
- Profile Management
- Error Scenarios

### 5. Document Requests

Add descriptions to requests explaining:
- Purpose
- Prerequisites
- Expected behavior
- Common errors

### 6. Use Collection Runner

Run entire collection to:
- Test all endpoints
- Verify integration
- Regression testing
- Performance testing

---

## Troubleshooting

### Token Expired

**Problem**: 401 Unauthorized after some time

**Solution**: 
1. Run "Refresh Token" request
2. Or run "Login" request again

### Invalid Request ID

**Problem**: Cannot find user by ID

**Solution**:
1. Verify `{{userId}}` is set in environment
2. Run "Create User" request first
3. Check user exists via "Get All Users"

### Permission Denied

**Problem**: 403 Forbidden on admin operations

**Solution**:
1. Verify logged in as ADMINISTRATOR
2. Check user roles via "Get Profile"
3. Login with admin credentials

---

## Support

For additional help:
- **API Documentation**: http://localhost:8080/swagger-ui.html
- **Error Reference**: See ERROR_CODES_REFERENCE.md
- **Support Email**: support@example.com

---

**Last Updated**: 2024-01-15  
**Guide Version**: 1.0.0
