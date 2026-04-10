package com.company.assetmanagement.util;

import java.util.regex.Pattern;

/**
 * Utility class for common validation operations.
 * Provides validation methods used throughout the application.
 */
public final class ValidationUtil {
    
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );
    
    private static final Pattern PASSWORD_PATTERN = Pattern.compile(
        "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&#])[A-Za-z\\d@$!%*?&#]{12,}$"
    );
    
    private static final Pattern ALPHANUMERIC_PATTERN = Pattern.compile("^[A-Za-z0-9]+$");
    
    private ValidationUtil() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }
    
    /**
     * Validates if a string is null or empty (blank).
     *
     * @param value the string to validate
     * @return true if the string is null or blank, false otherwise
     */
    public static boolean isNullOrEmpty(String value) {
        return value == null || value.isBlank();
    }
    
    /**
     * Validates if a string is not null and not empty.
     *
     * @param value the string to validate
     * @return true if the string is not null and not blank, false otherwise
     */
    public static boolean isNotNullOrEmpty(String value) {
        return value != null && !value.isBlank();
    }
    
    /**
     * Validates if an email address matches standard email format.
     * Requirement 11.5: Email format validation
     *
     * @param email the email address to validate
     * @return true if the email is valid, false otherwise
     */
    public static boolean isValidEmail(String email) {
        if (isNullOrEmpty(email)) {
            return false;
        }
        return EMAIL_PATTERN.matcher(email).matches();
    }
    
    /**
     * Validates if a password meets complexity requirements.
     * Requirement 1.3: Password complexity (min 12 chars, mixed case, numbers, special chars)
     *
     * @param password the password to validate
     * @return true if the password meets complexity requirements, false otherwise
     */
    public static boolean isValidPassword(String password) {
        if (isNullOrEmpty(password)) {
            return false;
        }
        return PASSWORD_PATTERN.matcher(password).matches();
    }
    
    /**
     * Validates if a string contains only alphanumeric characters.
     *
     * @param value the string to validate
     * @return true if the string contains only alphanumeric characters, false otherwise
     */
    public static boolean isAlphanumeric(String value) {
        if (isNullOrEmpty(value)) {
            return false;
        }
        return ALPHANUMERIC_PATTERN.matcher(value).matches();
    }
    
    /**
     * Validates if a string length is within the specified range.
     *
     * @param value the string to validate
     * @param minLength minimum length (inclusive)
     * @param maxLength maximum length (inclusive)
     * @return true if the string length is within range, false otherwise
     */
    public static boolean isLengthInRange(String value, int minLength, int maxLength) {
        if (value == null) {
            return false;
        }
        int length = value.length();
        return length >= minLength && length <= maxLength;
    }
    
    /**
     * Validates if a string matches a specific pattern.
     *
     * @param value the string to validate
     * @param pattern the regex pattern to match
     * @return true if the string matches the pattern, false otherwise
     */
    public static boolean matchesPattern(String value, String pattern) {
        if (isNullOrEmpty(value) || isNullOrEmpty(pattern)) {
            return false;
        }
        return Pattern.compile(pattern).matcher(value).matches();
    }
    
    /**
     * Validates if a serial number format is valid for the given asset type.
     * Requirement 11.2: Serial number format validation
     *
     * @param serialNumber the serial number to validate
     * @param assetType the asset type
     * @return true if the serial number format is valid for the asset type, false otherwise
     */
    public static boolean isValidSerialNumberFormat(String serialNumber, String assetType) {
        if (isNullOrEmpty(serialNumber) || isNullOrEmpty(assetType)) {
            return false;
        }
        
        // Serial number must be between 5 and 100 characters
        if (!isLengthInRange(serialNumber, 5, 100)) {
            return false;
        }
        
        // Serial number should contain alphanumeric characters and hyphens
        return matchesPattern(serialNumber, "^[A-Za-z0-9-]+$");
    }
    
    /**
     * Validates if a numeric value is within the specified range.
     *
     * @param value the value to validate
     * @param min minimum value (inclusive)
     * @param max maximum value (inclusive)
     * @return true if the value is within range, false otherwise
     */
    public static boolean isInRange(int value, int min, int max) {
        return value >= min && value <= max;
    }
    
    /**
     * Validates if a numeric value is within the specified range.
     *
     * @param value the value to validate
     * @param min minimum value (inclusive)
     * @param max maximum value (inclusive)
     * @return true if the value is within range, false otherwise
     */
    public static boolean isInRange(long value, long min, long max) {
        return value >= min && value <= max;
    }
    
    /**
     * Validates if a numeric value is positive.
     *
     * @param value the value to validate
     * @return true if the value is positive, false otherwise
     */
    public static boolean isPositive(int value) {
        return value > 0;
    }
    
    /**
     * Validates if a numeric value is non-negative.
     *
     * @param value the value to validate
     * @return true if the value is non-negative, false otherwise
     */
    public static boolean isNonNegative(int value) {
        return value >= 0;
    }
}
