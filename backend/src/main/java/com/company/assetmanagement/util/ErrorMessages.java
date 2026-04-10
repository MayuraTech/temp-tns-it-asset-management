package com.company.assetmanagement.util;

/**
 * Standardized error messages used throughout the IT Asset Management system.
 * Centralizes error message strings for consistency and maintainability.
 */
public final class ErrorMessages {
    
    private ErrorMessages() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }
    
    // ========== Authentication Error Messages ==========
    
    public static final String INVALID_CREDENTIALS = "Invalid username or password";
    public static final String ACCOUNT_LOCKED = "Account is locked due to multiple failed login attempts. Please try again after %d minutes";
    public static final String SESSION_EXPIRED = "Your session has expired. Please log in again";
    public static final String SESSION_INVALID = "Invalid or expired session token";
    public static final String UNAUTHORIZED = "Authentication is required to access this resource";
    
    // ========== Authorization Error Messages ==========
    
    public static final String INSUFFICIENT_PERMISSIONS = "You do not have permission to perform this action";
    public static final String ROLE_NOT_FOUND = "Role '%s' not found";
    public static final String INVALID_ROLE = "Invalid role: %s";
    public static final String CANNOT_MODIFY_OWN_ROLES = "You cannot modify your own roles";
    
    // ========== Validation Error Messages ==========
    
    public static final String FIELD_REQUIRED = "%s is required";
    public static final String FIELD_TOO_SHORT = "%s must be at least %d characters";
    public static final String FIELD_TOO_LONG = "%s must not exceed %d characters";
    public static final String FIELD_INVALID_FORMAT = "%s has invalid format";
    public static final String FIELD_INVALID_VALUE = "%s has invalid value";
    
    public static final String NAME_REQUIRED = "Name is required";
    public static final String NAME_TOO_LONG = "Name must not exceed " + AppConstants.MAX_ASSET_NAME_LENGTH + " characters";
    
    public static final String SERIAL_NUMBER_REQUIRED = "Serial number is required";
    public static final String SERIAL_NUMBER_TOO_SHORT = "Serial number must be at least " + AppConstants.MIN_SERIAL_NUMBER_LENGTH + " characters";
    public static final String SERIAL_NUMBER_TOO_LONG = "Serial number must not exceed " + AppConstants.MAX_SERIAL_NUMBER_LENGTH + " characters";
    public static final String SERIAL_NUMBER_INVALID_FORMAT = "Serial number contains invalid characters";
    public static final String SERIAL_NUMBER_DUPLICATE = "Asset with serial number '%s' already exists";
    
    public static final String EMAIL_REQUIRED = "Email is required";
    public static final String EMAIL_INVALID = "Email address has invalid format";
    public static final String EMAIL_TOO_LONG = "Email must not exceed " + AppConstants.MAX_EMAIL_LENGTH + " characters";
    public static final String EMAIL_DUPLICATE = "Email address '%s' is already in use";
    
    public static final String USERNAME_REQUIRED = "Username is required";
    public static final String USERNAME_TOO_LONG = "Username must not exceed " + AppConstants.MAX_USERNAME_LENGTH + " characters";
    public static final String USERNAME_DUPLICATE = "Username '%s' is already in use";
    
    public static final String PASSWORD_REQUIRED = "Password is required";
    public static final String PASSWORD_TOO_SHORT = "Password must be at least " + AppConstants.MIN_PASSWORD_LENGTH + " characters";
    public static final String PASSWORD_COMPLEXITY = "Password must contain at least one uppercase letter, one lowercase letter, one number, and one special character";
    public static final String PASSWORD_MISMATCH = "Passwords do not match";
    public static final String PASSWORD_INCORRECT = "Current password is incorrect";
    
    public static final String ACQUISITION_DATE_REQUIRED = "Acquisition date is required";
    public static final String ACQUISITION_DATE_FUTURE = "Acquisition date cannot be in the future";
    public static final String ACQUISITION_DATE_INVALID = "Acquisition date has invalid format";
    
    public static final String ASSET_TYPE_REQUIRED = "Asset type is required";
    public static final String ASSET_TYPE_INVALID = "Invalid asset type: %s";
    
    public static final String STATUS_REQUIRED = "Status is required";
    public static final String STATUS_INVALID = "Invalid status: %s";
    
    public static final String LOCATION_TOO_LONG = "Location must not exceed " + AppConstants.MAX_LOCATION_LENGTH + " characters";
    
    public static final String NOTES_TOO_LONG = "Notes must not exceed " + AppConstants.MAX_NOTES_LENGTH + " characters";
    
    // ========== Resource Not Found Error Messages ==========
    
    public static final String ASSET_NOT_FOUND = "Asset with ID '%s' not found";
    public static final String USER_NOT_FOUND = "User with ID '%s' not found";
    public static final String TICKET_NOT_FOUND = "Ticket with ID '%s' not found";
    public static final String AUDIT_LOG_NOT_FOUND = "Audit log entry with ID '%s' not found";
    public static final String CONFIGURATION_NOT_FOUND = "Configuration '%s' not found";
    
    // ========== Business Logic Error Messages ==========
    
    public static final String ASSET_RETIRED = "Asset '%s' is retired and cannot be modified";
    public static final String ASSET_ALREADY_ASSIGNED = "Asset is already assigned";
    public static final String ASSET_NOT_ASSIGNED = "Asset is not currently assigned";
    
    public static final String INVALID_STATUS_TRANSITION = "Cannot transition from status '%s' to '%s'";
    public static final String INVALID_TICKET_STATUS_TRANSITION = "Cannot transition ticket from status '%s' to '%s'";
    
    public static final String TICKET_ALREADY_APPROVED = "Ticket has already been approved";
    public static final String TICKET_ALREADY_REJECTED = "Ticket has already been rejected";
    public static final String TICKET_ALREADY_COMPLETED = "Ticket has already been completed";
    public static final String TICKET_ALREADY_CANCELLED = "Ticket has already been cancelled";
    
    public static final String CANNOT_DELETE_ASSIGNED_ASSET = "Cannot delete asset that is currently assigned";
    public static final String CANNOT_DELETE_ACTIVE_USER = "Cannot delete user with active sessions";
    
    // ========== Import/Export Error Messages ==========
    
    public static final String IMPORT_FILE_REQUIRED = "Import file is required";
    public static final String IMPORT_FILE_TOO_LARGE = "Import file exceeds maximum size of %d MB";
    public static final String IMPORT_FILE_INVALID_FORMAT = "Invalid file format. Supported formats: %s";
    public static final String IMPORT_VALIDATION_FAILED = "Import validation failed at line %d: %s";
    public static final String IMPORT_TOO_MANY_RECORDS = "Import file contains too many records. Maximum allowed: " + AppConstants.MAX_BULK_IMPORT_SIZE;
    
    public static final String EXPORT_FORMAT_REQUIRED = "Export format is required";
    public static final String EXPORT_FORMAT_INVALID = "Invalid export format. Supported formats: %s";
    public static final String EXPORT_FAILED = "Failed to generate export file";
    
    // ========== Configuration Error Messages ==========
    
    public static final String CONFIG_KEY_REQUIRED = "Configuration key is required";
    public static final String CONFIG_VALUE_REQUIRED = "Configuration value is required";
    public static final String CONFIG_VALUE_INVALID = "Configuration value is invalid for key '%s'";
    public static final String CONFIG_VALUE_OUT_OF_RANGE = "Configuration value must be between %d and %d";
    
    public static final String SESSION_TIMEOUT_OUT_OF_RANGE = "Session timeout must be between " + 
        AppConstants.MIN_SESSION_TIMEOUT_MINUTES + " and " + AppConstants.MAX_SESSION_TIMEOUT_MINUTES + " minutes";
    
    // ========== Rate Limiting Error Messages ==========
    
    public static final String RATE_LIMIT_EXCEEDED = "Rate limit exceeded. Please try again later";
    
    // ========== General Error Messages ==========
    
    public static final String INTERNAL_SERVER_ERROR = "An unexpected error occurred. Please try again later";
    public static final String SERVICE_UNAVAILABLE = "Service is temporarily unavailable. Please try again later";
    public static final String BAD_REQUEST = "Invalid request";
    public static final String VALIDATION_FAILED = "Validation failed";
    public static final String OPERATION_FAILED = "Operation failed: %s";
    
    // ========== Audit Log Error Messages ==========
    
    public static final String AUDIT_LOG_IMMUTABLE = "Audit log entries cannot be modified or deleted";
    public static final String AUDIT_LOG_CREATION_FAILED = "Failed to create audit log entry";
    
    // ========== Helper Methods ==========
    
    /**
     * Formats an error message with parameters.
     *
     * @param message the message template
     * @param params the parameters to insert
     * @return formatted error message
     */
    public static String format(String message, Object... params) {
        return String.format(message, params);
    }
    
    /**
     * Creates a field required error message.
     *
     * @param fieldName the name of the required field
     * @return formatted error message
     */
    public static String fieldRequired(String fieldName) {
        return format(FIELD_REQUIRED, fieldName);
    }
    
    /**
     * Creates a field too short error message.
     *
     * @param fieldName the name of the field
     * @param minLength the minimum length
     * @return formatted error message
     */
    public static String fieldTooShort(String fieldName, int minLength) {
        return format(FIELD_TOO_SHORT, fieldName, minLength);
    }
    
    /**
     * Creates a field too long error message.
     *
     * @param fieldName the name of the field
     * @param maxLength the maximum length
     * @return formatted error message
     */
    public static String fieldTooLong(String fieldName, int maxLength) {
        return format(FIELD_TOO_LONG, fieldName, maxLength);
    }
    
    /**
     * Creates a resource not found error message.
     *
     * @param resourceType the type of resource
     * @param resourceId the ID of the resource
     * @return formatted error message
     */
    public static String resourceNotFound(String resourceType, String resourceId) {
        return format("%s with ID '%s' not found", resourceType, resourceId);
    }
    
    /**
     * Creates an invalid status transition error message.
     *
     * @param fromStatus the current status
     * @param toStatus the target status
     * @return formatted error message
     */
    public static String invalidStatusTransition(String fromStatus, String toStatus) {
        return format(INVALID_STATUS_TRANSITION, fromStatus, toStatus);
    }
    
    /**
     * Creates a duplicate resource error message.
     *
     * @param resourceType the type of resource
     * @param value the duplicate value
     * @return formatted error message
     */
    public static String duplicateResource(String resourceType, String value) {
        return format("%s '%s' already exists", resourceType, value);
    }
}
