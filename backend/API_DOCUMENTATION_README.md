# User Management API Documentation - Quick Start

## Overview

This directory contains comprehensive API documentation for the User Management module of the IT Infrastructure Asset Management System.

## Documentation Files

### 1. **API_DOCUMENTATION.md**
Complete API reference documentation including:
- All endpoint specifications
- Request/response schemas
- Error codes and handling
- Authentication flow
- Validation rules
- Example requests and responses

**Use this for**: Understanding the complete API specification, endpoint details, and error handling.

### 2. **API_USAGE_GUIDE.md**
Practical integration guide for frontend developers including:
- Angular service implementations
- TypeScript interfaces
- Authentication implementation
- Error handling patterns
- Common usage patterns
- Best practices

**Use this for**: Implementing the API in your Angular frontend application.

### 3. **Interactive Documentation (Swagger UI)**
Live, interactive API documentation accessible at:
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **OpenAPI JSON**: http://localhost:8080/v3/api-docs
- **OpenAPI YAML**: http://localhost:8080/v3/api-docs.yaml

**Use this for**: Testing API endpoints interactively and exploring the API structure.

## Quick Start

### For Backend Developers

1. **Start the application**:
   ```bash
   cd backend
   ./mvnw spring-boot:run
   ```

2. **Access Swagger UI**:
   Open http://localhost:8080/swagger-ui.html in your browser

3. **Test endpoints**:
   - Click "Authorize" button
   - Login via POST /api/v1/auth/login
   - Copy the access token
   - Enter `Bearer {access_token}` in the authorization dialog
   - Test any endpoint

### For Frontend Developers

1. **Read the API Usage Guide**:
   Start with `API_USAGE_GUIDE.md` for practical Angular examples

2. **Copy the TypeScript interfaces**:
   Use the interface definitions provided in the guide

3. **Implement authentication**:
   Follow the AuthService example for JWT token management

4. **Implement services**:
   Use the UserService and ProfileService examples as templates

5. **Handle errors**:
   Implement the ErrorService pattern for consistent error handling

## API Endpoints Summary

### Authentication
- `POST /api/v1/auth/login` - User login
- `POST /api/v1/auth/logout` - User logout
- `POST /api/v1/auth/refresh` - Refresh access token

### User Management (Admin only)
- `POST /api/v1/users` - Create user
- `GET /api/v1/users` - List users (with pagination)
- `GET /api/v1/users/{id}` - Get user by ID
- `PUT /api/v1/users/{id}` - Update user
- `DELETE /api/v1/users/{id}` - Delete user
- `PATCH /api/v1/users/{id}/enable` - Enable user
- `PATCH /api/v1/users/{id}/disable` - Disable user
- `POST /api/v1/users/{id}/roles` - Assign role
- `DELETE /api/v1/users/{id}/roles/{role}` - Revoke role

### Profile Management (All users)
- `GET /api/v1/profile` - Get current user profile
- `PUT /api/v1/profile` - Update current user profile
- `POST /api/v1/profile/change-password` - Change password

## Authentication Flow

```
1. Login → POST /api/v1/auth/login
   ↓
2. Receive tokens (access: 30min, refresh: 24h)
   ↓
3. Use access token in Authorization header
   ↓
4. When access token expires → POST /api/v1/auth/refresh
   ↓
5. Logout → POST /api/v1/auth/logout
```

## Roles and Permissions

| Role | Permissions |
|------|-------------|
| **ADMINISTRATOR** | Full access to all operations |
| **ASSET_MANAGER** | View users only |
| **VIEWER** | View users only |
| **All Authenticated** | View/update own profile, change own password |

## Error Codes

| Code | Meaning | Common Causes |
|------|---------|---------------|
| 400 | Bad Request | Validation errors, invalid input |
| 401 | Unauthorized | Missing/invalid token, wrong credentials |
| 403 | Forbidden | Insufficient permissions |
| 404 | Not Found | User ID not found |
| 409 | Conflict | Duplicate username/email |
| 500 | Server Error | Unexpected server errors |

## Security Features

- **JWT Authentication**: Stateless token-based authentication
- **Token Rotation**: New refresh tokens issued on use
- **Account Locking**: 30-minute lock after 5 failed attempts
- **Password Complexity**: Enforced minimum requirements
- **Session Tracking**: All sessions tracked and can be invalidated
- **Audit Logging**: All operations logged for security

## Validation Rules

### Username
- 3-100 characters
- Alphanumeric and underscores only
- Must be unique

### Email
- 5-255 characters
- Valid email format
- Must be unique

### Password
- Minimum 8 characters
- At least one uppercase letter
- At least one lowercase letter
- At least one digit
- At least one special character (@$!%*?&)

## Example Requests

### Login
```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "Admin@123456"
  }'
```

### Create User
```bash
curl -X POST http://localhost:8080/api/v1/users \
  -H "Authorization: Bearer {access_token}" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "jdoe",
    "email": "jdoe@example.com",
    "password": "SecurePass123!",
    "roles": ["ASSET_MANAGER"]
  }'
```

### Get Users
```bash
curl -X GET "http://localhost:8080/api/v1/users?page=0&size=20" \
  -H "Authorization: Bearer {access_token}"
```

### Change Password
```bash
curl -X POST http://localhost:8080/api/v1/profile/change-password \
  -H "Authorization: Bearer {access_token}" \
  -H "Content-Type: application/json" \
  -d '{
    "currentPassword": "OldPass123!",
    "newPassword": "NewSecurePass456!"
  }'
```

## Testing with Swagger UI

1. Open http://localhost:8080/swagger-ui.html
2. Click "Authorize" button (top right)
3. Login to get access token:
   - Expand POST /api/v1/auth/login
   - Click "Try it out"
   - Enter credentials
   - Click "Execute"
   - Copy the accessToken from response
4. Enter token in authorization dialog:
   - Paste token in the value field
   - Click "Authorize"
   - Click "Close"
5. Test any endpoint:
   - Expand the endpoint
   - Click "Try it out"
   - Fill in parameters
   - Click "Execute"

## Configuration

### OpenAPI Configuration
Location: `src/main/java/com/company/assetmanagement/config/OpenApiConfig.java`

Features:
- API metadata and description
- Server URLs for different environments
- JWT Bearer authentication scheme
- Global security requirements

### Application Properties
Location: `src/main/resources/application.properties`

```properties
springdoc.api-docs.path=/api-docs
springdoc.swagger-ui.path=/swagger-ui.html
springdoc.swagger-ui.operationsSorter=method
springdoc.swagger-ui.tagsSorter=alpha
```

## Support and Resources

- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **OpenAPI Spec**: http://localhost:8080/v3/api-docs
- **API Documentation**: `API_DOCUMENTATION.md`
- **Frontend Guide**: `API_USAGE_GUIDE.md`
- **Support Email**: support@example.com

## Best Practices

1. **Always use HTTPS** in production
2. **Store tokens securely** (HttpOnly cookies recommended)
3. **Implement token refresh** before expiration
4. **Handle 401 responses** by redirecting to login
5. **Never log passwords** or tokens
6. **Validate inputs** on both client and server
7. **Follow password complexity** requirements
8. **Use request IDs** for error tracking
9. **Implement proper error handling** without exposing sensitive data
10. **Monitor authentication failures** for security threats

## Troubleshooting

### Common Issues

**Issue**: 401 Unauthorized on protected endpoints
- **Solution**: Ensure access token is included in Authorization header with "Bearer " prefix

**Issue**: Token expired
- **Solution**: Use refresh token to get new access token via POST /api/v1/auth/refresh

**Issue**: Account locked
- **Solution**: Wait 30 minutes or contact administrator to unlock account

**Issue**: Validation errors
- **Solution**: Check error response details for specific field errors and requirements

**Issue**: CORS errors
- **Solution**: Ensure frontend origin is configured in CORS settings

## Next Steps

1. **Backend Developers**: 
   - Review `API_DOCUMENTATION.md` for complete API specification
   - Test endpoints using Swagger UI
   - Implement additional features as needed

2. **Frontend Developers**:
   - Read `API_USAGE_GUIDE.md` for integration examples
   - Copy TypeScript interfaces to your project
   - Implement authentication and services
   - Test integration with backend API

3. **QA/Testing**:
   - Use Swagger UI for manual testing
   - Refer to example requests in documentation
   - Test all error scenarios
   - Verify security features (account locking, token expiration)

---

**Documentation Version**: 1.0.0  
**Last Updated**: 2024-01-15  
**API Version**: 1.0.0
