# Exception Handling

This package contains the global exception handling infrastructure for the IT Asset Management application.

## Overview

The application uses a centralized exception handling approach with `@RestControllerAdvice` to ensure consistent error responses across all API endpoints.

## Components

### Custom Exceptions

1. **ValidationException** - Thrown when input validation fails
   - HTTP Status: 400 Bad Request
   - Contains list of field-level validation errors
   - Example: Missing required fields, invalid formats

2. **DuplicateSerialNumberException** - Thrown when attempting to create an asset with an existing serial number
   - HTTP Status: 409 Conflict
   - Contains the duplicate serial number

3. **InsufficientPermissionsException** - Thrown when a user lacks required permissions
   - HTTP Status: 403 Forbidden
   - Optionally contains user ID and action details

4. **ResourceNotFoundException** - Thrown when a requested resource doesn't exist
   - HTTP Status: 404 Not Found
   - Contains resource type and ID

5. **InvalidStatusTransitionException** - Thrown when attempting an invalid status transition
   - HTTP Status: 422 Unprocessable Entity
   - Contains from/to status and resource type

### Error Response DTO

**ErrorResponse** - Standard error response structure
```json
{
  "type": "VALIDATION_ERROR",
  "message": "Validation failed",
  "details": [
    {
      "field": "name",
      "message": "Name is required"
    }
  ],
  "timestamp": "2024-01-15T10:30:00",
  "requestId": "req-123456"
}
```

Fields:
- `type`: Error category (e.g., VALIDATION_ERROR, DUPLICATE_SERIAL_NUMBER)
- `message`: Human-readable error message
- `details`: Additional context (field errors, resource IDs, etc.)
- `timestamp`: When the error occurred
- `requestId`: Request correlation ID for tracing

### Global Exception Handler

**GlobalExceptionHandler** - Centralized exception handling with `@RestControllerAdvice`

Handles:
- Custom application exceptions
- Bean Validation errors (`@Valid` annotations)
- Generic exceptions (500 Internal Server Error)

Features:
- Comprehensive error details for validation failures
- Request ID extraction from headers or generation
- Structured logging of all errors
- Consistent error response format

## Usage Examples

### Throwing Validation Errors

```java
// Single field error
throw new ValidationException("email", "Invalid email format");

// Multiple field errors
List<ValidationException.ValidationError> errors = new ArrayList<>();
errors.add(new ValidationException.ValidationError("name", "Name is required"));
errors.add(new ValidationException.ValidationError("serialNumber", "Serial number must be at least 5 characters"));
throw new ValidationException(errors);
```

### Throwing Business Logic Errors

```java
// Duplicate serial number
if (assetRepository.existsBySerialNumber(serialNumber)) {
    throw new DuplicateSerialNumberException(serialNumber);
}

// Insufficient permissions
if (!authorizationService.hasPermission(userId, Action.CREATE_ASSET)) {
    throw new InsufficientPermissionsException(userId, "CREATE_ASSET");
}

// Resource not found
Asset asset = assetRepository.findById(id)
    .orElseThrow(() -> new ResourceNotFoundException("Asset", id.toString()));

// Invalid status transition
if (!isValidTransition(currentStatus, newStatus)) {
    throw new InvalidStatusTransitionException(currentStatus, newStatus, "Asset");
}
```

### Bean Validation

Use standard Bean Validation annotations in DTOs:

```java
public class AssetRequest {
    @NotNull(message = "Asset type is required")
    private AssetType assetType;
    
    @NotBlank(message = "Name is required")
    @Size(max = 255, message = "Name must not exceed 255 characters")
    private String name;
    
    @NotBlank(message = "Serial number is required")
    @Size(min = 5, max = 100, message = "Serial number must be between 5 and 100 characters")
    private String serialNumber;
}
```

Controller method:
```java
@PostMapping
public ResponseEntity<AssetDTO> createAsset(@Valid @RequestBody AssetRequest request) {
    // Validation happens automatically
    // ValidationException thrown if validation fails
}
```

## Error Response Examples

### Validation Error (400)

```json
{
  "type": "VALIDATION_ERROR",
  "message": "Validation failed",
  "details": [
    {
      "field": "name",
      "message": "Name is required"
    },
    {
      "field": "serialNumber",
      "message": "Serial number must be between 5 and 100 characters",
      "value": "ABC"
    }
  ],
  "timestamp": "2024-01-15T10:30:00",
  "requestId": "req-123456"
}
```

### Duplicate Serial Number (409)

```json
{
  "type": "DUPLICATE_SERIAL_NUMBER",
  "message": "Asset with serial number already exists",
  "details": {
    "serialNumber": "SRV-001"
  },
  "timestamp": "2024-01-15T10:30:00",
  "requestId": "req-123456"
}
```

### Insufficient Permissions (403)

```json
{
  "type": "INSUFFICIENT_PERMISSIONS",
  "message": "You do not have permission to perform this action",
  "details": {
    "userId": "user-123",
    "action": "CREATE_ASSET"
  },
  "timestamp": "2024-01-15T10:30:00",
  "requestId": "req-123456"
}
```

### Resource Not Found (404)

```json
{
  "type": "RESOURCE_NOT_FOUND",
  "message": "Asset with ID '550e8400-e29b-41d4-a716-446655440000' not found",
  "details": {
    "resourceType": "Asset",
    "resourceId": "550e8400-e29b-41d4-a716-446655440000"
  },
  "timestamp": "2024-01-15T10:30:00",
  "requestId": "req-123456"
}
```

### Invalid Status Transition (422)

```json
{
  "type": "INVALID_STATUS_TRANSITION",
  "message": "Invalid status transition for Asset from 'retired' to 'in_use'",
  "details": {
    "fromStatus": "retired",
    "toStatus": "in_use",
    "resourceType": "Asset"
  },
  "timestamp": "2024-01-15T10:30:00",
  "requestId": "req-123456"
}
```

### Internal Server Error (500)

```json
{
  "type": "INTERNAL_SERVER_ERROR",
  "message": "An unexpected error occurred. Please try again later.",
  "timestamp": "2024-01-15T10:30:00",
  "requestId": "req-123456"
}
```

## Request ID Tracking

The exception handler extracts request IDs from the `X-Request-ID` header or generates a new UUID if not provided. This enables:
- Request tracing across services
- Correlation of errors with specific requests
- Support ticket investigation

Clients should include the request ID in support requests for faster troubleshooting.

## Testing

Comprehensive unit tests are provided in `GlobalExceptionHandlerTest.java` covering:
- All custom exception types
- Bean validation errors
- Request ID handling
- Error response structure validation

Run tests with:
```bash
mvn test -Dtest=GlobalExceptionHandlerTest
```

## Best Practices

1. **Use specific exceptions** - Throw the most specific exception type for the error condition
2. **Provide context** - Include relevant details (field names, IDs, values) in exceptions
3. **Don't expose sensitive data** - Never include passwords, tokens, or internal system details in error responses
4. **Log appropriately** - Use appropriate log levels (WARN for client errors, ERROR for server errors)
5. **Return all validation errors** - Don't fail fast on first error; collect and return all validation failures
6. **Use consistent error types** - Follow the established error type naming convention

## Security Considerations

- Authentication errors don't reveal whether username or password was incorrect
- Authorization errors provide minimal details to prevent information disclosure
- Internal server errors don't expose stack traces or internal implementation details
- All errors are logged server-side for security monitoring and investigation
