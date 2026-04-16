# JWT Interceptor Implementation - Task 18.2

## Overview

This document describes the implementation of the JWT interceptor for automatic token attachment and token refresh functionality in the IT Infrastructure Asset Management application.

## Implementation Summary

### Features Implemented

1. **Automatic Token Attachment**
   - Automatically attaches JWT access token to all outgoing HTTP requests
   - Adds `Authorization: Bearer <token>` header to requests
   - Skips token attachment for authentication endpoints (`/auth/login`, `/auth/refresh`)

2. **Token Refresh on Expiration**
   - Detects 401 Unauthorized responses indicating expired tokens
   - Automatically attempts to refresh the access token using the refresh token
   - Retries the original request with the new access token after successful refresh
   - Implements request queuing to prevent multiple simultaneous refresh requests

3. **Error Handling**
   - Redirects to login page when refresh token is unavailable
   - Redirects to login page when token refresh fails
   - Includes return URL and session expiration reason in query parameters
   - Clears session data on authentication failures
   - Passes through non-401 errors to the error interceptor

### Technical Implementation

#### Request Queuing Mechanism

The interceptor implements a sophisticated queuing mechanism to handle multiple simultaneous requests when a token expires:

1. **First 401 Error**: Initiates token refresh process
2. **Subsequent 401 Errors**: Queued until refresh completes
3. **After Refresh**: All queued requests are retried with the new token

This prevents multiple simultaneous token refresh requests and ensures all pending requests are properly handled.

#### Token Refresh Flow

```
Request → 401 Error → Check if refresh in progress
                      ↓
                      No → Start refresh
                           ↓
                           Refresh successful → Retry request with new token
                           ↓
                           Refresh failed → Clear session → Redirect to login
                      ↓
                      Yes → Queue request
                            ↓
                            Wait for refresh → Retry with new token
```

### Files Modified

1. **frontend/src/app/core/interceptors/jwt.interceptor.ts**
   - Enhanced with token refresh logic
   - Added request queuing mechanism
   - Implemented proper error handling

2. **frontend/src/app/core/interceptors/error.interceptor.ts**
   - Removed token refresh logic (now handled by JWT interceptor)
   - Simplified 401 error handling

3. **frontend/src/app/core/interceptors/jwt.interceptor.spec.ts**
   - Added comprehensive unit tests for token attachment
   - Added tests for token refresh functionality
   - Added tests for request queuing
   - Added tests for error handling

## Requirements Validation

### Requirement 1.1: User Authentication
✅ JWT tokens are automatically attached to all authenticated requests

### Requirement 1.2: Token Generation
✅ Access tokens are properly used in Authorization headers

### Requirement 2.1: Token Refresh
✅ Expired access tokens are automatically refreshed using refresh tokens

### Requirement 2.2: New Token Generation
✅ New access tokens are obtained and used after successful refresh

### Requirement 2.3: Invalid Token Handling
✅ Invalid or expired refresh tokens trigger session cleanup and redirect to login

### Requirement 2.4: Session Invalidation
✅ Failed authentication clears session and redirects to login

### Requirement 2.5: Token Payload
✅ JWT tokens with user ID and roles are properly handled

## Testing

### Unit Tests Implemented

1. **Token Attachment Tests**
   - ✅ Adds Authorization header when token exists
   - ✅ Does not add header when no token exists
   - ✅ Skips header for login endpoint
   - ✅ Skips header for refresh endpoint

2. **Token Refresh Tests**
   - ✅ Refreshes token and retries request on 401 error
   - ✅ Redirects to login when refresh token unavailable
   - ✅ Redirects to login when token refresh fails
   - ✅ Queues multiple requests during token refresh
   - ✅ Does not attempt refresh for auth endpoints

3. **Error Handling Tests**
   - ✅ Passes through non-401 errors
   - ✅ Handles network errors gracefully

### Test Execution

To run the JWT interceptor tests:

```bash
cd frontend
npm test -- --include='**/jwt.interceptor.spec.ts' --watch=false
```

## Usage Example

The JWT interceptor is automatically applied to all HTTP requests through the Angular HTTP client configuration in `app.config.ts`:

```typescript
export const appConfig: ApplicationConfig = {
  providers: [
    provideHttpClient(
      withInterceptors([
        jwtInterceptor,      // Handles token attachment and refresh
        loadingInterceptor,
        errorInterceptor
      ])
    )
  ]
};
```

### Example Request Flow

```typescript
// Component makes a request
this.http.get('/api/v1/assets').subscribe({
  next: (assets) => {
    // Request automatically includes Authorization header
    // If token expires, it's automatically refreshed
    // Request is retried with new token
    console.log('Assets:', assets);
  },
  error: (error) => {
    // Only reaches here if refresh fails or other errors occur
    console.error('Error:', error);
  }
});
```

## Security Considerations

1. **Token Storage**: Tokens are stored in localStorage (consider HttpOnly cookies for production)
2. **Token Expiration**: Access tokens expire after 30 minutes
3. **Refresh Token Rotation**: Refresh tokens are rotated on each refresh for enhanced security
4. **Session Cleanup**: All session data is cleared on authentication failures
5. **HTTPS Only**: All API communications use HTTPS in production

## Best Practices Followed

1. **Single Responsibility**: JWT interceptor focuses solely on token management
2. **Error Separation**: Authentication errors handled separately from other HTTP errors
3. **Request Queuing**: Prevents multiple simultaneous token refresh requests
4. **Graceful Degradation**: Proper fallback behavior when refresh fails
5. **User Experience**: Seamless token refresh without user intervention
6. **Security**: Automatic session cleanup on authentication failures

## Integration with AuthService

The JWT interceptor integrates seamlessly with the AuthService:

- **getAccessToken()**: Retrieves current access token for request headers
- **getRefreshToken()**: Retrieves refresh token for token refresh
- **refreshToken()**: Performs token refresh operation
- **logout()**: Clears session data on authentication failures

## Future Enhancements

1. **Token Storage**: Migrate from localStorage to HttpOnly cookies for enhanced security
2. **Token Preemptive Refresh**: Refresh tokens before they expire (currently done in AuthService)
3. **Retry Logic**: Add configurable retry attempts for failed requests
4. **Rate Limiting**: Implement rate limiting for token refresh attempts
5. **Metrics**: Add monitoring for token refresh success/failure rates

## Conclusion

The JWT interceptor implementation successfully provides:
- ✅ Automatic token attachment to all HTTP requests
- ✅ Seamless token refresh on expiration
- ✅ Proper error handling for authentication failures
- ✅ Request queuing to prevent multiple refresh attempts
- ✅ Comprehensive unit test coverage

The implementation follows Angular best practices and integrates seamlessly with the existing authentication infrastructure.
