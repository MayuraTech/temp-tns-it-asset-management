# JWT Role Prefix Fix - 403 Forbidden Error

## Problem
User management pages were returning 403 Forbidden error:
```
GET http://localhost:8080/api/v1/users?page=0&size=20 403 (Forbidden)
Error: You do not have permission to perform this action.
```

## Root Cause
The JWT tokens were being generated with roles WITHOUT the "ROLE_" prefix (e.g., "ADMINISTRATOR"), but Spring Security's `@PreAuthorize` annotations expect roles WITH the "ROLE_" prefix (e.g., "ROLE_ADMINISTRATOR").

### How Spring Security Role-Based Authorization Works

1. **UserDetailsService** loads user roles and adds "ROLE_" prefix:
   ```java
   // CustomUserDetailsService.java
   private Collection<? extends GrantedAuthority> getAuthorities(Set<Role> roles) {
       return roles.stream()
               .map(role -> new SimpleGrantedAuthority("ROLE_" + role.name()))
               .collect(Collectors.toList());
   }
   ```

2. **JWT Token** stores roles in claims for stateless authentication

3. **@PreAuthorize** annotations check for roles:
   ```java
   @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'ASSET_MANAGER', 'VIEWER')")
   ```
   This internally checks for "ROLE_ADMINISTRATOR", "ROLE_ASSET_MANAGER", "ROLE_VIEWER"

4. **Mismatch**: JWT tokens had "ADMINISTRATOR" but Spring Security expected "ROLE_ADMINISTRATOR"

## Solution
Added "ROLE_" prefix when generating JWT tokens in `AuthenticationServiceImpl.login()`.

## Changes Made

### File: `backend/src/main/java/com/company/assetmanagement/service/AuthenticationServiceImpl.java`

**Before (Line 163-166):**
```java
String roles = user.getRoles().stream()
        .map(userRole -> userRole.getRole().name())
        .collect(Collectors.joining(","));
```

**After:**
```java
String roles = user.getRoles().stream()
        .map(userRole -> "ROLE_" + userRole.getRole().name())
        .collect(Collectors.joining(","));
```

## JWT Token Structure

### Before Fix
```json
{
  "sub": "admin",
  "userId": "550e8400-e29b-41d4-a716-446655440000",
  "roles": "ADMINISTRATOR",
  "iat": 1640000000,
  "exp": 1640001800
}
```

### After Fix
```json
{
  "sub": "admin",
  "userId": "550e8400-e29b-41d4-a716-446655440000",
  "roles": "ROLE_ADMINISTRATOR",
  "iat": 1640000000,
  "exp": 1640001800
}
```

## Testing Instructions

### 1. Restart Backend
**IMPORTANT**: You must restart the backend for this fix to take effect.

```bash
cd backend
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

### 2. Clear Browser Storage
Since old JWT tokens have the wrong role format, you need to clear them:

**Option A: Clear via Browser DevTools**
1. Open browser DevTools (F12)
2. Go to Application tab
3. Under Storage → Local Storage → http://localhost:4200
4. Delete all items
5. Refresh the page

**Option B: Logout and Login Again**
1. Click logout in the application
2. Login again with `admin/Admin@123456`
3. This will generate a new JWT token with correct role format

### 3. Test User Management Pages

1. **Login** at http://localhost:4200/login
   - Username: `admin`
   - Password: `Admin@123456`

2. **Navigate to Users**
   - Click "Users" in sidebar
   - URL: `http://localhost:4200/users`
   - Should load successfully without 403 error

3. **Verify API Call**
   - Open browser DevTools → Network tab
   - Refresh the users page
   - Check the request to `/api/v1/users?page=0&size=20`
   - Should return 200 OK with user data

4. **Test Other User Management Features**
   - Create User: `http://localhost:4200/users/create`
   - View User: Click on any user
   - Edit User: Click edit button
   - All should work without 403 errors

### 4. Verify JWT Token (Optional)

To verify the JWT token has correct role format:

1. Open browser DevTools → Application tab
2. Under Storage → Local Storage → http://localhost:4200
3. Copy the value of `access_token`
4. Go to https://jwt.io
5. Paste the token in the "Encoded" section
6. Check the "Payload" section - `roles` should be "ROLE_ADMINISTRATOR"

## Role-Based Access Control

After this fix, the following endpoints will work correctly:

### User Management Endpoints
- **GET /api/v1/users** - All authenticated users (ADMINISTRATOR, ASSET_MANAGER, VIEWER)
- **POST /api/v1/users** - ADMINISTRATOR only
- **GET /api/v1/users/{id}** - All authenticated users
- **PUT /api/v1/users/{id}** - ADMINISTRATOR only
- **DELETE /api/v1/users/{id}** - ADMINISTRATOR only
- **PATCH /api/v1/users/{id}/enable** - ADMINISTRATOR only
- **PATCH /api/v1/users/{id}/disable** - ADMINISTRATOR only
- **POST /api/v1/users/{id}/roles** - ADMINISTRATOR only
- **DELETE /api/v1/users/{id}/roles/{role}** - ADMINISTRATOR only

### Test Users
From `DataInitializer.java`:
- **admin** / Admin@123456 - ROLE_ADMINISTRATOR
- **manager** / Manager@123456 - ROLE_ASSET_MANAGER
- **viewer** / Viewer@123456 - ROLE_VIEWER

## Related Files
- `backend/src/main/java/com/company/assetmanagement/service/AuthenticationServiceImpl.java` - JWT token generation (FIXED)
- `backend/src/main/java/com/company/assetmanagement/security/CustomUserDetailsService.java` - Adds ROLE_ prefix to authorities
- `backend/src/main/java/com/company/assetmanagement/security/JwtTokenProvider.java` - JWT token operations
- `backend/src/main/java/com/company/assetmanagement/controller/UserController.java` - @PreAuthorize annotations

## Requirements Addressed
- **1.3**: JWT token authentication with proper role-based authorization
- **12.1**: Role-based access control (RBAC) implementation
- **12.2**: Authorization enforcement at controller level

## Status
✅ Fix implemented
✅ No compilation errors
⏳ Awaiting backend restart and testing
