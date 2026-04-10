# Utility Classes

This package contains utility classes that provide common functionality used throughout the IT Infrastructure Asset Management application.

## Overview

The utility classes follow these principles:
- **Stateless**: All methods are static and classes cannot be instantiated
- **Thread-safe**: All methods are safe to use in concurrent environments
- **Null-safe**: Methods handle null inputs gracefully
- **Well-tested**: Comprehensive unit tests ensure reliability

## Classes

### DateUtil

Provides date formatting and validation operations.

**Key Features:**
- Date validation (not in future, in past, in future)
- Date formatting (ISO format, display format)
- Date parsing with validation
- Format validation

**Example Usage:**
```java
// Validate acquisition date is not in future (Requirement 11.3)
if (!DateUtil.isNotInFuture(acquisitionDate)) {
    throw new ValidationException("Acquisition date cannot be in the future");
}

// Format date for API response
String formattedDate = DateUtil.formatDate(asset.getAcquisitionDate());

// Parse date from user input
LocalDate date = DateUtil.parseDate("2024-01-15");
```

### ValidationUtil

Provides common validation logic for data integrity.

**Key Features:**
- Email format validation (Requirement 11.5)
- Password complexity validation (Requirement 1.3)
- Serial number format validation (Requirement 11.2)
- String length validation
- Pattern matching
- Numeric range validation

**Example Usage:**
```java
// Validate email format (Requirement 11.5)
if (!ValidationUtil.isValidEmail(email)) {
    errors.add(new ValidationError("email", ErrorMessages.EMAIL_INVALID));
}

// Validate password complexity (Requirement 1.3)
if (!ValidationUtil.isValidPassword(password)) {
    errors.add(new ValidationError("password", ErrorMessages.PASSWORD_COMPLEXITY));
}

// Validate serial number format (Requirement 11.2)
if (!ValidationUtil.isValidSerialNumberFormat(serialNumber, assetType)) {
    errors.add(new ValidationError("serialNumber", ErrorMessages.SERIAL_NUMBER_INVALID_FORMAT));
}
```

### StringUtil

Provides string manipulation and formatting operations.

**Key Features:**
- Null-safe string operations
- Case conversion (camelCase, snake_case, kebab-case)
- String truncation with ellipsis
- String masking for sensitive data
- Padding operations
- Split and join operations

**Example Usage:**
```java
// Trim and handle null
String trimmed = StringUtil.trimToNull(userInput);

// Mask sensitive data for logging
String maskedToken = StringUtil.mask(token, 4); // Shows last 4 chars

// Convert to snake_case for database columns
String columnName = StringUtil.toSnakeCase("assetType"); // "asset_type"

// Truncate long text for display
String preview = StringUtil.truncateWithEllipsis(notes, 100);
```

### AppConstants

Centralizes application-wide constants and configuration values.

**Key Features:**
- Authentication & security constants
- Validation limits
- Pagination defaults
- Performance thresholds
- API configuration
- Role names
- Date format patterns

**Example Usage:**
```java
// Check failed login attempts (Requirement 1.4)
if (user.getFailedLoginAttempts() >= AppConstants.MAX_FAILED_LOGIN_ATTEMPTS) {
    lockAccount(user, AppConstants.ACCOUNT_LOCKOUT_MINUTES);
}

// Validate serial number length
if (!ValidationUtil.isLengthInRange(serialNumber, 
    AppConstants.MIN_SERIAL_NUMBER_LENGTH, 
    AppConstants.MAX_SERIAL_NUMBER_LENGTH)) {
    throw new ValidationException(ErrorMessages.SERIAL_NUMBER_TOO_SHORT);
}

// Set pagination defaults
Pageable pageable = PageRequest.of(
    AppConstants.DEFAULT_PAGE_NUMBER, 
    AppConstants.DEFAULT_PAGE_SIZE
);
```

### ErrorMessages

Provides standardized error messages for consistent user feedback.

**Key Features:**
- Validation error messages
- Authentication error messages
- Authorization error messages
- Business logic error messages
- Resource not found messages
- Helper methods for formatting

**Example Usage:**
```java
// Use predefined error messages
throw new ValidationException(ErrorMessages.SERIAL_NUMBER_REQUIRED);

// Format error messages with parameters
String error = ErrorMessages.format(
    ErrorMessages.ASSET_NOT_FOUND, 
    assetId
);

// Use helper methods
String error = ErrorMessages.fieldRequired("name");
String error = ErrorMessages.invalidStatusTransition("ORDERED", "RETIRED");
```

## Requirements Mapping

These utility classes support the following requirements:

### Requirement 1.3: Password Complexity
- `ValidationUtil.isValidPassword()` enforces minimum 12 characters with mixed case, numbers, and special characters

### Requirement 1.4: Account Lockout
- `AppConstants.MAX_FAILED_LOGIN_ATTEMPTS` defines the lockout threshold (5 attempts)
- `AppConstants.ACCOUNT_LOCKOUT_MINUTES` defines the lockout duration (15 minutes)

### Requirement 1.5: Session Timeout
- `AppConstants.SESSION_TIMEOUT_MINUTES` defines the inactivity timeout (30 minutes)

### Requirement 11.1: Required Field Validation
- `ValidationUtil.isNullOrEmpty()` checks for required fields
- `ErrorMessages` provides standardized required field messages

### Requirement 11.2: Serial Number Format Validation
- `ValidationUtil.isValidSerialNumberFormat()` validates serial number patterns
- `AppConstants` defines min/max serial number lengths

### Requirement 11.3: Acquisition Date Validation
- `DateUtil.isNotInFuture()` validates dates are not in the future
- `ErrorMessages.ACQUISITION_DATE_FUTURE` provides error message

### Requirement 11.4: Comprehensive Validation Errors
- `ErrorMessages` provides detailed, field-specific error messages
- Helper methods format errors consistently

### Requirement 11.5: Email Format Validation
- `ValidationUtil.isValidEmail()` validates email format
- `ErrorMessages.EMAIL_INVALID` provides error message

## Testing

All utility classes have comprehensive unit tests:
- `DateUtilTest` - Tests date validation and formatting
- `ValidationUtilTest` - Tests validation logic
- `StringUtilTest` - Tests string operations

Run tests with:
```bash
mvn test -Dtest="DateUtilTest,ValidationUtilTest,StringUtilTest"
```

## Best Practices

1. **Use constants instead of magic numbers**
   ```java
   // Good
   if (password.length() < AppConstants.MIN_PASSWORD_LENGTH)
   
   // Bad
   if (password.length() < 12)
   ```

2. **Use standardized error messages**
   ```java
   // Good
   throw new ValidationException(ErrorMessages.EMAIL_INVALID);
   
   // Bad
   throw new ValidationException("Invalid email");
   ```

3. **Validate early and comprehensively**
   ```java
   List<ValidationError> errors = new ArrayList<>();
   
   if (ValidationUtil.isNullOrEmpty(name)) {
       errors.add(new ValidationError("name", ErrorMessages.NAME_REQUIRED));
   }
   
   if (!ValidationUtil.isValidEmail(email)) {
       errors.add(new ValidationError("email", ErrorMessages.EMAIL_INVALID));
   }
   
   if (!errors.isEmpty()) {
       throw new ValidationException(errors);
   }
   ```

4. **Use utility methods for consistency**
   ```java
   // Good - consistent formatting
   String formatted = DateUtil.formatDate(date);
   
   // Bad - inconsistent formatting
   String formatted = date.toString();
   ```

## Thread Safety

All utility classes are thread-safe because:
- All methods are static
- No mutable state is maintained
- Pattern objects are compiled once and reused safely

## Performance Considerations

- Pattern compilation is done once at class loading time
- String operations use efficient StringBuilder internally
- Date formatting uses cached DateTimeFormatter instances
- No unnecessary object creation

## Future Enhancements

Potential additions to consider:
- Additional date operations (business days, date ranges)
- More string manipulation methods as needed
- Additional validation patterns
- Localization support for error messages
- Custom validation rule builders
