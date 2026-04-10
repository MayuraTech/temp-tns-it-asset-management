# Exception Handling Implementation

## Overview

This document describes the implementation of comprehensive global exception handling for the IT Infrastructure Asset Management application, completed as part of Task 3.2.

## Implementation Summary

### Components Created

1. **Custom Exception Classes** (`backend/src/main/java/com/company/assetmanagement/exception/`)
   - `ValidationException.java` - Handles validation errors with field-level details
   - `DuplicateSerialNumberException.java` - Handles duplicate serial number conflicts
   - `InsufficientPermissionsException.java` - Handles authorization failures
   - `ResourceNotFoundException.java` - Handles missing resources
   - `InvalidStatusTransitionException.java` - Handles invalid state transitions

2. **Error Response DTO** (`backend/src/main/java/com/company/assetmanagement/dto/`)
   - `ErrorResponse.java` - Standard error response structure with builder pattern

3. **Global Exception Handler** (`backend/src/main/java/com/company/assetmanagement/exception/`)
   - `GlobalExceptionHandler.java` - Centralized exception handling with @RestControllerAdvice

4. **Unit Tests** (`backend/src/test/java/com/company/assetmanagement/exception/`)
   - `GlobalExceptionHandlerTest.java` - Comprehensive test coverage for all exception types

5. **Documentation**
   - `backend/src/main/java/com/company/assetmanagement/exception/README.md` - Detailed usage guide
   - `backend/EXCEPTION_HANDLING_IMPLEMENTATION.md` - This implementation summary

6. **Example Controller** (for demonstration)
   - `backend/src/main/java/com/company/assetmanagement/controller/ExampleController.java`

## Exception Mapping

| Exception | HTTP Status | Error Type | Use Case |
|-----------|-------------|------------|----------|
| ValidationException | 400 Bad Request | VALIDATION_ERROR | Invalid input data, missing required fields |
| MethodArgumentNotValidException | 400 Bad Request | VALIDATION_ERROR | Bean Validation (@Valid) failures |
| DuplicateSerialNumberException | 409 Conflict | DUPLICATE_SERIAL_NUMBER | Asset serial number already exists |
| InsufficientPermissionsException | 403 Forbidden | INSUFFICIENT_PERMISSIONS | User lacks required permissions |
| ResourceNotFoundException | 404 Not Found | RESOURCE_NOT_FOUND | Asset, user, or ticket not found |
| InvalidStatusTransitionException | 422 Unprocessable Entity | INVALID_STATUS_TRANSITION | Invalid lifecycle or ticket status change |
| Exception (generic) | 500 Internal Server Error | INTERNAL_SERVER_ERROR | Unexpected errors |

## Key Features

### 1. Comprehensive Error Details

All validation errors are collected and returned together, not just the first error:

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

### 2. Request ID Tracking

Every error response includes a request ID for tracing:
- Extracted from `X-Request-ID` header if provided
- Auto-generated UUID if not provided
- Enables correlation of errors with specific requests

### 3. Structured Logging

All exceptions are logged with appropriate levels:
- WARN for client errors (4xx)
- ERROR for server errors (5xx)
- Includes contextual information for debugging

### 4. Security Considerations

- No sensitive data in error responses
- Generic messages for authentication failures
- Minimal details for authorization failures
- No stack traces exposed to clients

### 5. Consistent Error Format

All errors follow the same structure:
```json
{
  "type": "ERROR_TYPE",
  "message": "Human-readable message",
  "details": { /* Context-specific details */ },
  "timestamp": "ISO-8601 timestamp",
  "requestId": "Correlation ID"
}
```

## Usage Examples

### Service Layer

```java
@Service
public class AssetService {
    
    public AssetDTO createAsset(String userId, AssetRequest request) {
        // Authorization check
        if (!authorizationService.hasPermission(userId, Action.CREATE_ASSET)) {
            throw new InsufficientPermissionsException(userId, "CREATE_ASSET");
        }
        
        // Validation
        List<ValidationException.ValidationError> errors = new ArrayList<>();
        if (request.getName() == null || request.getName().isBlank()) {
            errors.add(new ValidationException.ValidationError("name", "Name is required"));
        }
        if (!errors.isEmpty()) {
            throw new ValidationException(errors);
        }
        
        // Business logic
        if (assetRepository.existsBySerialNumber(request.getSerialNumber())) {
            throw new DuplicateSerialNumberException(request.getSerialNumber());
        }
        
        // ... rest of implementation
    }
    
    public AssetDTO getAsset(String assetId) {
        return assetRepository.findById(UUID.fromString(assetId))
            .map(this::mapToDTO)
            .orElseThrow(() -> new ResourceNotFoundException("Asset", assetId));
    }
    
    public AssetDTO updateStatus(String assetId, String newStatus) {
        Asset asset = getAssetOrThrow(assetId);
        
        if (!isValidTransition(asset.getStatus(), newStatus)) {
            throw new InvalidStatusTransitionException(
                asset.getStatus(), 
                newStatus, 
                "Asset"
            );
        }
        
        // ... rest of implementation
    }
}
```

### Controller Layer

```java
@RestController
@RequestMapping("/api/v1/assets")
public class AssetController {
    
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'ASSET_MANAGER')")
    public ResponseEntity<AssetDTO> createAsset(
            @Valid @RequestBody AssetRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        // @Valid triggers Bean Validation
        // ValidationException thrown automatically if validation fails
        
        AssetDTO asset = assetService.createAsset(userDetails.getUsername(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(asset);
    }
}
```

### DTO with Bean Validation

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
    
    @NotNull(message = "Acquisition date is required")
    @PastOrPresent(message = "Acquisition date cannot be in the future")
    private LocalDate acquisitionDate;
}
```

## Testing

### Unit Tests

Comprehensive unit tests cover all exception types and scenarios:

```bash
mvn test -Dtest=GlobalExceptionHandlerTest
```

Test coverage includes:
- ✅ ValidationException with multiple errors
- ✅ ValidationException with single error
- ✅ MethodArgumentNotValidException (Bean Validation)
- ✅ DuplicateSerialNumberException
- ✅ InsufficientPermissionsException (with and without details)
- ✅ ResourceNotFoundException
- ✅ InvalidStatusTransitionException
- ✅ Generic Exception (500 error)
- ✅ Request ID extraction and generation

### Manual Testing

Use the example controller to test exception handling:

```bash
# Validation error
curl http://localhost:8080/api/v1/examples/validation-error

# Duplicate serial number
curl http://localhost:8080/api/v1/examples/duplicate-serial

# Insufficient permissions
curl http://localhost:8080/api/v1/examples/insufficient-permissions

# Resource not found
curl http://localhost:8080/api/v1/examples/not-found

# Invalid status transition
curl http://localhost:8080/api/v1/examples/invalid-transition

# Server error
curl http://localhost:8080/api/v1/examples/server-error
```

**Note:** Remove `ExampleController.java` before deploying to production.

## Integration with Requirements

This implementation satisfies the following requirements:

- **Requirement 3.3**: Duplicate serial number detection and error handling
- **Requirement 4.5**: Comprehensive validation error reporting
- **Requirement 5.2**: Invalid status transition handling
- **Requirement 11.4**: Validation errors with field-level details

## Design Alignment

The implementation follows the design specifications:

1. **Error Response Format** (from design.md):
   - ✅ Consistent structure with type, message, details, timestamp, requestId
   - ✅ Comprehensive validation error details
   - ✅ Specific error messages with actionable information

2. **Error Categories** (from design.md):
   - ✅ Authentication Errors (can be extended)
   - ✅ Authorization Errors (InsufficientPermissionsException)
   - ✅ Validation Errors (ValidationException)
   - ✅ Business Logic Errors (DuplicateSerialNumberException, InvalidStatusTransitionException)
   - ✅ System Errors (Generic Exception handling)

3. **Error Handling Principles** (from design.md):
   - ✅ Fail Fast: Validation at system boundaries
   - ✅ Comprehensive Validation: All errors reported together
   - ✅ Specific Error Messages: Field names, IDs, constraint details included
   - ✅ No Sensitive Data Leakage: Generic messages for security errors
   - ✅ Audit Failed Operations: Logging infrastructure in place
   - ✅ Graceful Degradation: System remains operational

## API Design Compliance

Follows the API design guidelines from `it-asset-management-api-design.md`:

- ✅ Correct HTTP status codes for each error type
- ✅ Structured error responses with consistent format
- ✅ Request ID tracking for correlation
- ✅ Comprehensive validation error details
- ✅ Security-conscious error messages

## Coding Standards Compliance

Follows the coding standards from `it-asset-management-coding-standards.md`:

- ✅ Proper package structure
- ✅ Clear naming conventions
- ✅ Comprehensive JavaDoc comments
- ✅ Builder pattern for ErrorResponse
- ✅ Proper exception hierarchy
- ✅ Comprehensive unit tests

## Next Steps

1. **Remove Example Controller**: Delete `ExampleController.java` before production deployment
2. **Integrate with Services**: Use these exceptions in service layer implementations
3. **Add More Specific Exceptions**: Create additional exception types as needed (e.g., `AssetRetiredException`, `TicketAlreadyProcessedException`)
4. **Enhance Logging**: Add structured logging with correlation IDs
5. **Add Metrics**: Track exception rates and types for monitoring
6. **Integration Tests**: Create integration tests that verify end-to-end error handling

## Files Created

```
backend/src/main/java/com/company/assetmanagement/
├── dto/
│   └── ErrorResponse.java
├── exception/
│   ├── DuplicateSerialNumberException.java
│   ├── GlobalExceptionHandler.java
│   ├── InsufficientPermissionsException.java
│   ├── InvalidStatusTransitionException.java
│   ├── ResourceNotFoundException.java
│   ├── ValidationException.java
│   └── README.md
└── controller/
    └── ExampleController.java (remove before production)

backend/src/test/java/com/company/assetmanagement/
└── exception/
    └── GlobalExceptionHandlerTest.java

backend/
└── EXCEPTION_HANDLING_IMPLEMENTATION.md
```

## Conclusion

The global exception handling implementation is complete and ready for integration with the rest of the application. All exceptions return structured error responses with appropriate HTTP status codes, comprehensive error details, and request tracking capabilities.

The implementation follows all design specifications, API guidelines, and coding standards, providing a solid foundation for error handling throughout the IT Asset Management application.
