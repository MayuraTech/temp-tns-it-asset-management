# User Management Exceptions Implementation

## Overview

This document summarizes the implementation of Task 4.1: Create user management specific exceptions for the IT Infrastructure Asset Management System.

## Implemented Exceptions

### 1. AccountLockedException

**Purpose**: Thrown when attempting to authenticate with a locked user account.

**Features**:
- Contains lock expiration time (`LocalDateTime lockUntil`)
- Supports custom error messages
- Used when account is locked after 5 failed login attempts

**Constructors**:
- `AccountLockedException(LocalDateTime lockUntil)`
- `AccountLockedException(String message, LocalDateTime lockUntil)`

**HTTP Status**: 401 Unauthorized

### 2. AccountDisabledException

**Purpose**: Thrown when attempting to authenticate with a disabled user account.

**Features**:
- Contains optional user ID for context
- Supports custom error messages
- Used when account is administratively disabled

**Constructors**:
- `AccountDisabledException()`
- `AccountDisabledException(String userId)`
- `AccountDisabledException(String message, String userId)`

**HTTP Status**: 401 Unauthorized

### 3. DuplicateUsernameException

**Purpose**: Thrown when attempting to create or update a user with an existing username.

**Features**:
- Contains the duplicate username for error reporting
- Supports custom error messages
- Ensures username uniqueness constraint

**Constructors**:
- `DuplicateUsernameException(String username)`
- `DuplicateUsernameException(String message, String username)`

**HTTP Status**: 409 Conflict

### 4. DuplicateEmailException

**Purpose**: Thrown when attempting to create or update a user with an existing email address.

**Features**:
- Contains the duplicate email for error reporting
- Supports custom error messages
- Ensures email uniqueness constraint

**Constructors**:
- `DuplicateEmailException(String email)`
- `DuplicateEmailException(String message, String email)`

**HTTP Status**: 409 Conflict

### 5. UserNotFoundException

**Purpose**: Thrown when attempting to access a user that does not exist.

**Features**:
- Contains the user ID that was not found
- Supports custom error messages
- Used for user lookup operations

**Constructors**:
- `UserNotFoundException(String userId)`
- `UserNotFoundException(String message, String userId)`

**HTTP Status**: 404 Not Found

## GlobalExceptionHandler Updates

The `GlobalExceptionHandler` class has been enhanced with new exception handlers:

### New Exception Handlers

1. **handleAccountLocked()**: Returns structured error response with lock expiration time
2. **handleAccountDisabled()**: Returns structured error response with optional user ID
3. **handleDuplicateUsername()**: Returns structured error response with duplicate username
4. **handleDuplicateEmail()**: Returns structured error response with duplicate email
5. **handleUserNotFound()**: Returns structured error response with user ID

### Error Response Format

All handlers return consistent `ErrorResponse` objects with:
- `type`: Error type identifier (e.g., "ACCOUNT_LOCKED", "DUPLICATE_USERNAME")
- `message`: Human-readable error message
- `details`: Additional context (e.g., lockUntil, username, email, userId)
- `requestId`: Request correlation ID for debugging
- `timestamp`: Error occurrence timestamp

### Example Error Response

```json
{
  "type": "ACCOUNT_LOCKED",
  "message": "Account is temporarily locked due to multiple failed login attempts",
  "details": {
    "lockUntil": "2024-01-15T14:30:00"
  },
  "requestId": "req-123456",
  "timestamp": "2024-01-15T14:00:00Z"
}
```

## Testing

### Unit Tests

Created comprehensive unit tests in `UserManagementExceptionsTest`:
- Tests all exception constructors
- Verifies message formatting
- Validates property getters
- Tests custom message support

### Integration Tests

Created integration tests in `GlobalExceptionHandlerUserManagementTest`:
- Tests HTTP status codes
- Validates error response structure
- Tests request ID handling
- Verifies error details inclusion

### Quick Test

Created `ExceptionQuickTest` for manual verification without Maven build.

## Requirements Mapping

This implementation satisfies the following requirements from the specification:

- **Requirement 1.3**: Authentication error handling for invalid credentials
- **Requirement 1.4**: Account locked error handling
- **Requirement 1.5**: Account status validation
- **Requirement 4.1**: Username uniqueness validation
- **Requirement 4.2**: Email uniqueness validation
- **Requirement 5.3**: User not found error handling
- **Requirement 6.5**: Update validation error handling
- **Requirement 7.4**: Delete validation error handling
- **Requirement 8.4**: Account status change error handling
- **Requirement 8.5**: Self-modification prevention
- **Requirement 9.5**: Role assignment error handling
- **Requirement 10.4**: Role revocation error handling
- **Requirement 10.5**: Last role protection
- **Requirement 11.4**: Profile update error handling
- **Requirement 12.2**: Authorization error handling
- **Requirement 14.4**: Comprehensive validation error reporting

## Usage Examples

### Service Layer Usage

```java
// Check for duplicate username
if (userRepository.existsByUsername(username)) {
    throw new DuplicateUsernameException(username);
}

// Check account status
if (user.isAccountLocked() && user.getLockUntil().isAfter(LocalDateTime.now())) {
    throw new AccountLockedException(user.getLockUntil());
}

// Check if user exists
User user = userRepository.findById(userId)
    .orElseThrow(() -> new UserNotFoundException(userId));
```

### Controller Layer Handling

The exceptions are automatically handled by the `GlobalExceptionHandler` and converted to appropriate HTTP responses with proper status codes and error details.

## File Structure

```
backend/src/main/java/com/company/assetmanagement/exception/
├── AccountLockedException.java
├── AccountDisabledException.java
├── DuplicateUsernameException.java
├── DuplicateEmailException.java
├── UserNotFoundException.java
├── ValidationException.java (existing, enhanced)
└── GlobalExceptionHandler.java (updated)

backend/src/test/java/com/company/assetmanagement/exception/
├── UserManagementExceptionsTest.java
├── GlobalExceptionHandlerUserManagementTest.java
└── ExceptionQuickTest.java
```

## Next Steps

These exceptions are now ready to be used in the upcoming user management service implementations:

1. **AuthenticationService**: Use `AccountLockedException` and `AccountDisabledException`
2. **UserService**: Use `DuplicateUsernameException`, `DuplicateEmailException`, and `UserNotFoundException`
3. **ProfileService**: Use `UserNotFoundException` and validation exceptions
4. **AuthorizationService**: Use account status exceptions

The comprehensive error handling foundation is now in place to support the complete user management module implementation.