# Task 16.1: GlobalExceptionHandler Enhancement for User Management

## Overview

Enhanced the GlobalExceptionHandler to provide comprehensive exception handling for all user management specific exceptions with detailed error reporting, proper HTTP status code mapping, and request ID tracking for error correlation.

## Changes Made

### 1. Enhanced Exception Handlers

#### AccountLockedException Handler
- **HTTP Status**: 401 Unauthorized
- **Error Type**: `ACCOUNT_LOCKED`
- **Enhanced Details**:
  - `lockUntil`: Timestamp when the account will be unlocked
  - `reason`: "Multiple failed login attempts"
  - `lockDurationMinutes`: 30
- **Improved Message**: Provides clear guidance on when the account will be unlocked
- **Use Case**: Triggered when a user attempts to login with an account locked due to 5 consecutive failed login attempts

#### AccountDisabledException Handler
- **HTTP Status**: 401 Unauthorized
- **Error Type**: `ACCOUNT_DISABLED`
- **Enhanced Details**:
  - `userId`: ID of the disabled user (if available)
  - `reason`: "Account has been administratively disabled"
  - `action`: "Contact your system administrator to reactivate your account"
- **Improved Message**: Provides clear guidance to contact support
- **Use Case**: Triggered when a user attempts to login with an administratively disabled account

#### DuplicateUsernameException Handler
- **HTTP Status**: 409 Conflict
- **Error Type**: `DUPLICATE_USERNAME`
- **Enhanced Details**:
  - `username`: The duplicate username
  - `field`: "username"
  - `constraint`: "unique"
- **Improved Message**: Suggests choosing a different username
- **Use Case**: Triggered when creating or updating a user with an existing username

#### DuplicateEmailException Handler
- **HTTP Status**: 409 Conflict
- **Error Type**: `DUPLICATE_EMAIL`
- **Enhanced Details**:
  - `email`: The duplicate email address
  - `field`: "email"
  - `constraint`: "unique"
- **Improved Message**: Suggests using a different email address
- **Use Case**: Triggered when creating or updating a user with an existing email

#### UserNotFoundException Handler
- **HTTP Status**: 404 Not Found
- **Error Type**: `USER_NOT_FOUND`
- **Enhanced Details**:
  - `userId`: The user ID that was not found
  - `resourceType`: "User"
- **Improved Message**: Clarifies that the user was not found with the specified ID
- **Use Case**: Triggered when attempting to access a non-existent user

### 2. Existing Handlers (Already Implemented)

The following handlers were already properly implemented:

- **ValidationException**: Returns 400 Bad Request with comprehensive field-level validation errors
- **MethodArgumentNotValidException**: Returns 400 Bad Request for Bean Validation errors
- **BadCredentialsException**: Returns 401 Unauthorized for invalid credentials
- **InsufficientPermissionsException**: Returns 403 Forbidden for authorization failures
- **ResourceNotFoundException**: Returns 404 Not Found for missing resources
- **InvalidStatusTransitionException**: Returns 422 Unprocessable Entity for invalid state transitions
- **DuplicateSerialNumberException**: Returns 409 Conflict for duplicate serial numbers
- **Generic Exception**: Returns 500 Internal Server Error for unexpected errors

### 3. Request ID Tracking

All exception handlers include request ID tracking:
- Extracts `X-Request-ID` from request headers
- Generates a new UUID if not provided
- Includes request ID in all error responses for correlation and debugging

### 4. Comprehensive Error Response Structure

All error responses follow a consistent structure:

```json
{
  "type": "ERROR_TYPE",
  "message": "Human-readable error message",
  "details": {
    "field1": "value1",
    "field2": "value2"
  },
  "timestamp": "2024-01-15T10:30:00",
  "requestId": "req-123456"
}
```

## Testing

### Added Comprehensive Unit Tests

Created unit tests for all user management exception handlers in `GlobalExceptionHandlerTest.java`:

1. **testHandleAccountLockedException**: Verifies proper handling of locked accounts with lock expiration details
2. **testHandleAccountDisabledException**: Verifies proper handling of disabled accounts with guidance
3. **testHandleAccountDisabledExceptionWithoutUserId**: Verifies handling when userId is not provided
4. **testHandleDuplicateUsernameException**: Verifies proper handling of duplicate username conflicts
5. **testHandleDuplicateEmailException**: Verifies proper handling of duplicate email conflicts
6. **testHandleUserNotFoundException**: Verifies proper handling of user not found errors
7. **testHandleBadCredentialsException**: Verifies proper handling of authentication failures

All tests verify:
- Correct HTTP status codes
- Proper error types
- Comprehensive error details
- Request ID inclusion
- Appropriate error messages

## Benefits

### 1. Comprehensive Validation Error Reporting
- All validation errors are returned in a single response
- Field-level error details with rejected values
- Clear, actionable error messages

### 2. Proper HTTP Status Code Mapping
- 400 Bad Request: Validation errors
- 401 Unauthorized: Authentication failures, locked/disabled accounts
- 403 Forbidden: Authorization failures
- 404 Not Found: Resource not found
- 409 Conflict: Duplicate constraints
- 422 Unprocessable Entity: Invalid state transitions
- 500 Internal Server Error: Unexpected errors

### 3. Request ID Tracking
- Every error response includes a unique request ID
- Enables correlation between client requests and server logs
- Facilitates debugging and troubleshooting
- Supports distributed tracing

### 4. Enhanced User Experience
- Clear, actionable error messages
- Detailed context for troubleshooting
- Consistent error response structure
- Helpful guidance for resolution

### 5. Improved Debugging
- Comprehensive error details in responses
- Request ID for log correlation
- Proper logging of all exceptions
- Stack traces logged for unexpected errors

## API Error Response Examples

### Account Locked Error
```json
{
  "type": "ACCOUNT_LOCKED",
  "message": "Account is temporarily locked due to multiple failed login attempts. Please try again after 2024-01-15T11:00:00",
  "details": {
    "lockUntil": "2024-01-15T11:00:00",
    "reason": "Multiple failed login attempts",
    "lockDurationMinutes": 30
  },
  "timestamp": "2024-01-15T10:30:00",
  "requestId": "req-123456"
}
```

### Account Disabled Error
```json
{
  "type": "ACCOUNT_DISABLED",
  "message": "Account has been disabled by an administrator. Please contact support for assistance.",
  "details": {
    "userId": "user-123",
    "reason": "Account has been administratively disabled",
    "action": "Contact your system administrator to reactivate your account"
  },
  "timestamp": "2024-01-15T10:30:00",
  "requestId": "req-123456"
}
```

### Duplicate Username Error
```json
{
  "type": "DUPLICATE_USERNAME",
  "message": "Username already exists. Please choose a different username.",
  "details": {
    "username": "john_doe",
    "field": "username",
    "constraint": "unique"
  },
  "timestamp": "2024-01-15T10:30:00",
  "requestId": "req-123456"
}
```

### Duplicate Email Error
```json
{
  "type": "DUPLICATE_EMAIL",
  "message": "Email address already exists. Please use a different email address.",
  "details": {
    "email": "john@example.com",
    "field": "email",
    "constraint": "unique"
  },
  "timestamp": "2024-01-15T10:30:00",
  "requestId": "req-123456"
}
```

### User Not Found Error
```json
{
  "type": "USER_NOT_FOUND",
  "message": "User not found with the specified ID",
  "details": {
    "userId": "550e8400-e29b-41d4-a716-446655440000",
    "resourceType": "User"
  },
  "timestamp": "2024-01-15T10:30:00",
  "requestId": "req-123456"
}
```

### Validation Error
```json
{
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
    }
  ],
  "timestamp": "2024-01-15T10:30:00",
  "requestId": "req-123456"
}
```

## Requirements Satisfied

### Requirement 14.1: Input Validation
- ✅ Comprehensive validation error reporting with all errors in a single response
- ✅ Field-level error details with rejected values
- ✅ Clear, actionable error messages

### Requirement 14.2: Duplicate Username Handling
- ✅ Proper handling of DuplicateUsernameException
- ✅ 409 Conflict status code
- ✅ Detailed error information with username and constraint

### Requirement 14.3: Duplicate Email Handling
- ✅ Proper handling of DuplicateEmailException
- ✅ 409 Conflict status code
- ✅ Detailed error information with email and constraint

### Requirement 14.4: User Not Found Handling
- ✅ Proper handling of UserNotFoundException
- ✅ 404 Not Found status code
- ✅ Detailed error information with user ID

### Additional Requirements Satisfied
- ✅ Account locked error handling with lock expiration details
- ✅ Account disabled error handling with guidance
- ✅ Authentication failure handling
- ✅ Request ID tracking for all errors
- ✅ Consistent error response structure
- ✅ Comprehensive logging of all exceptions

## Files Modified

1. **GlobalExceptionHandler.java**
   - Enhanced AccountLockedException handler with detailed lock information
   - Enhanced AccountDisabledException handler with guidance
   - Enhanced DuplicateUsernameException handler with constraint details
   - Enhanced DuplicateEmailException handler with constraint details
   - Enhanced UserNotFoundException handler with resource type

2. **GlobalExceptionHandlerTest.java**
   - Added test for AccountLockedException handling
   - Added test for AccountDisabledException handling (with and without userId)
   - Added test for DuplicateUsernameException handling
   - Added test for DuplicateEmailException handling
   - Added test for UserNotFoundException handling
   - Added test for BadCredentialsException handling

## Conclusion

The GlobalExceptionHandler has been successfully enhanced to provide comprehensive exception handling for all user management operations. The implementation includes:

- ✅ All user management specific exception handlers
- ✅ Comprehensive validation error reporting
- ✅ Proper HTTP status code mapping
- ✅ Request ID tracking for error correlation
- ✅ Clear, actionable error messages
- ✅ Detailed error context for debugging
- ✅ Consistent error response structure
- ✅ Comprehensive unit test coverage

The enhanced exception handling provides a robust foundation for user management operations, ensuring that all errors are properly handled, logged, and communicated to clients with appropriate detail and guidance.
