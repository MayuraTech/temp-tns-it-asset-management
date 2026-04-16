# JWT Token Handling Fix

## Issue Summary

The frontend was sending malformed JWT tokens to the backend, causing authentication errors:
- Error: "Invalid JWT token: Invalid compact JWT string: Compact JWSs must contain exactly 2 period characters, and compact JWEs must contain exactly 4. Found: 0"
- Error: "rejected value [null]" - refresh token was null/empty

## Root Cause

**Field Name Mismatch**: The backend API returns JSON with snake_case field names (`access_token`, `refresh_token`, `token_type`, `expires_in`), but the frontend TypeScript interface was expecting camelCase (`accessToken`, `refreshToken`, `tokenType`, `expiresIn`).

Angular's HttpClient does not automatically convert between snake_case and camelCase, so the frontend was:
1. Receiving the response with snake_case fields
2. Trying to access camelCase properties (which were undefined)
3. Storing `undefined` values in storage
4. Sending `undefined` or empty strings as tokens to the backend

## Solution

Updated the frontend to match the backend's JSON field naming convention:

### 1. Updated LoginResponse Interface

**File**: `frontend/src/app/core/models/auth.model.ts`

```typescript
// Before (incorrect - camelCase)
export interface LoginResponse {
  accessToken: string;
  refreshToken: string;
  tokenType: string;
  expiresIn: number;
}

// After (correct - snake_case to match backend)
export interface LoginResponse {
  access_token: string;
  refresh_token: string;
  token_type: string;
  expires_in: number;
}
```

### 2. Updated AuthService Token Storage

**File**: `frontend/src/app/core/services/auth.service.ts`

```typescript
// Updated to use snake_case field names
private storeTokens(response: LoginResponse, persistent: boolean = false): void {
  this.storageService.setItem('access_token', response.access_token, persistent);
  this.storageService.setItem('refresh_token', response.refresh_token, persistent);
  this.storageService.setItem('token_type', response.token_type, persistent);
}

// Updated expiration timer
this.startTokenExpirationTimer(response.expires_in);
```

### 3. Updated Refresh Token Request

**File**: `frontend/src/app/core/services/auth.service.ts`

```typescript
// Backend expects refresh_token (snake_case) in JSON
return this.http.post<LoginResponse>(`${this.apiUrl}/refresh`, { refresh_token: refreshToken })
```

### 4. Updated JWT Interceptor

**File**: `frontend/src/app/core/interceptors/jwt.interceptor.ts`

```typescript
// Updated to use snake_case field name
refreshTokenSubject.next(response.access_token);
return next(addAuthorizationHeader(req, response.access_token));
```

## Backend API Contract

The backend uses Jackson's `@JsonProperty` annotations to define JSON field names:

```java
// TokenResponse.java
@JsonProperty("access_token")
private String accessToken;

@JsonProperty("refresh_token")
private String refreshToken;

@JsonProperty("token_type")
private String tokenType;

@JsonProperty("expires_in")
private Long expiresIn;

// RefreshTokenRequest.java
@JsonProperty("refresh_token")
private String refreshToken;
```

## Testing

To verify the fix:

1. **Start Backend**:
   ```bash
   cd backend
   mvn spring-boot:run -Dspring-boot.run.profiles=dev
   ```

2. **Start Frontend**:
   ```bash
   cd frontend
   npm start
   ```

3. **Test Login Flow**:
   - Navigate to http://localhost:4200/login
   - Login with: `admin` / `Admin@123456`
   - Check browser DevTools Network tab:
     - Login response should contain `access_token`, `refresh_token`, etc.
     - Subsequent API requests should have `Authorization: Bearer <token>` header
   - Check browser DevTools Application tab:
     - localStorage/sessionStorage should contain valid JWT tokens
   - Backend logs should show successful authentication without JWT parsing errors

4. **Test Token Refresh**:
   - Wait for token to expire (or manually trigger refresh)
   - Verify that refresh token request sends `refresh_token` field
   - Verify that new tokens are received and stored correctly

## Files Modified

1. `frontend/src/app/core/models/auth.model.ts` - Updated LoginResponse interface
2. `frontend/src/app/core/services/auth.service.ts` - Updated token storage and refresh logic
3. `frontend/src/app/core/interceptors/jwt.interceptor.ts` - Updated token field access

## Verification

Run TypeScript compilation check:
```bash
cd frontend
npm run build
```

All files should compile without errors.

## Related Documentation

- Backend API: `backend/src/main/java/com/company/assetmanagement/controller/AuthController.java`
- Token Response DTO: `backend/src/main/java/com/company/assetmanagement/dto/TokenResponse.java`
- Refresh Token Request DTO: `backend/src/main/java/com/company/assetmanagement/dto/RefreshTokenRequest.java`
- Quick Start Guide: `QUICK_START_GUIDE.md`

## Status

✅ **FIXED** - Frontend now correctly handles JWT tokens with proper field name mapping to match backend API contract.
