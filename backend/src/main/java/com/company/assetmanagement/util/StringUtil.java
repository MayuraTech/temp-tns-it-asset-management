package com.company.assetmanagement.util;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Utility class for string operations.
 * Provides common string manipulation methods used throughout the application.
 */
public final class StringUtil {
    
    private StringUtil() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }
    
    /**
     * Checks if a string is null or empty (blank).
     *
     * @param value the string to check
     * @return true if the string is null or blank, false otherwise
     */
    public static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
    
    /**
     * Checks if a string is not null and not empty.
     *
     * @param value the string to check
     * @return true if the string is not null and not blank, false otherwise
     */
    public static boolean isNotBlank(String value) {
        return value != null && !value.isBlank();
    }
    
    /**
     * Trims a string and returns null if the result is empty.
     *
     * @param value the string to trim
     * @return trimmed string or null if empty
     */
    public static String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
    
    /**
     * Trims a string and returns empty string if null.
     *
     * @param value the string to trim
     * @return trimmed string or empty string if null
     */
    public static String trimToEmpty(String value) {
        return value == null ? "" : value.trim();
    }
    
    /**
     * Capitalizes the first letter of a string.
     *
     * @param value the string to capitalize
     * @return string with first letter capitalized, or null if input is null
     */
    public static String capitalize(String value) {
        if (isBlank(value)) {
            return value;
        }
        return value.substring(0, 1).toUpperCase() + value.substring(1);
    }
    
    /**
     * Converts a string to camelCase.
     *
     * @param value the string to convert
     * @return camelCase string
     */
    public static String toCamelCase(String value) {
        if (isBlank(value)) {
            return value;
        }
        
        String[] words = value.split("[\\s_-]+");
        if (words.length == 0) {
            return value;
        }
        
        StringBuilder result = new StringBuilder(words[0].toLowerCase());
        for (int i = 1; i < words.length; i++) {
            result.append(capitalize(words[i].toLowerCase()));
        }
        return result.toString();
    }
    
    /**
     * Converts a string to snake_case.
     *
     * @param value the string to convert
     * @return snake_case string
     */
    public static String toSnakeCase(String value) {
        if (isBlank(value)) {
            return value;
        }
        
        return value.replaceAll("([a-z])([A-Z])", "$1_$2")
                   .replaceAll("[\\s-]+", "_")
                   .toLowerCase();
    }
    
    /**
     * Converts a string to kebab-case.
     *
     * @param value the string to convert
     * @return kebab-case string
     */
    public static String toKebabCase(String value) {
        if (isBlank(value)) {
            return value;
        }
        
        return value.replaceAll("([a-z])([A-Z])", "$1-$2")
                   .replaceAll("[\\s_]+", "-")
                   .toLowerCase();
    }
    
    /**
     * Truncates a string to the specified length.
     *
     * @param value the string to truncate
     * @param maxLength maximum length
     * @return truncated string
     */
    public static String truncate(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength);
    }
    
    /**
     * Truncates a string to the specified length and adds ellipsis.
     *
     * @param value the string to truncate
     * @param maxLength maximum length (including ellipsis)
     * @return truncated string with ellipsis
     */
    public static String truncateWithEllipsis(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) {
            return value;
        }
        if (maxLength < 3) {
            return truncate(value, maxLength);
        }
        return value.substring(0, maxLength - 3) + "...";
    }
    
    /**
     * Joins a list of strings with a delimiter.
     *
     * @param values the list of strings to join
     * @param delimiter the delimiter
     * @return joined string
     */
    public static String join(List<String> values, String delimiter) {
        if (values == null || values.isEmpty()) {
            return "";
        }
        return String.join(delimiter, values);
    }
    
    /**
     * Splits a string by delimiter and trims each part.
     *
     * @param value the string to split
     * @param delimiter the delimiter
     * @return list of trimmed strings
     */
    public static List<String> splitAndTrim(String value, String delimiter) {
        if (isBlank(value)) {
            return List.of();
        }
        return Arrays.stream(value.split(delimiter))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.toList());
    }
    
    /**
     * Masks a string by replacing characters with asterisks.
     * Useful for masking sensitive data like passwords or tokens.
     *
     * @param value the string to mask
     * @param visibleChars number of characters to keep visible at the end
     * @return masked string
     */
    public static String mask(String value, int visibleChars) {
        if (isBlank(value)) {
            return value;
        }
        if (value.length() <= visibleChars) {
            return "*".repeat(value.length());
        }
        int maskLength = value.length() - visibleChars;
        return "*".repeat(maskLength) + value.substring(maskLength);
    }
    
    /**
     * Removes all whitespace from a string.
     *
     * @param value the string to process
     * @return string with all whitespace removed
     */
    public static String removeWhitespace(String value) {
        if (value == null) {
            return null;
        }
        return value.replaceAll("\\s+", "");
    }
    
    /**
     * Checks if a string contains only digits.
     *
     * @param value the string to check
     * @return true if the string contains only digits, false otherwise
     */
    public static boolean isNumeric(String value) {
        if (isBlank(value)) {
            return false;
        }
        return value.matches("\\d+");
    }
    
    /**
     * Pads a string to the left with a specified character.
     *
     * @param value the string to pad
     * @param length the desired length
     * @param padChar the character to pad with
     * @return padded string
     */
    public static String padLeft(String value, int length, char padChar) {
        if (value == null) {
            value = "";
        }
        if (value.length() >= length) {
            return value;
        }
        return String.valueOf(padChar).repeat(length - value.length()) + value;
    }
    
    /**
     * Pads a string to the right with a specified character.
     *
     * @param value the string to pad
     * @param length the desired length
     * @param padChar the character to pad with
     * @return padded string
     */
    public static String padRight(String value, int length, char padChar) {
        if (value == null) {
            value = "";
        }
        if (value.length() >= length) {
            return value;
        }
        return value + String.valueOf(padChar).repeat(length - value.length());
    }
}
