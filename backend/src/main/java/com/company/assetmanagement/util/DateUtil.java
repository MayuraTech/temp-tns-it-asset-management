package com.company.assetmanagement.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Utility class for date formatting and validation operations.
 * Provides common date-related functionality used throughout the application.
 */
public final class DateUtil {
    
    private static final DateTimeFormatter ISO_DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;
    private static final DateTimeFormatter ISO_DATETIME_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    private static final DateTimeFormatter DISPLAY_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DISPLAY_DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    private DateUtil() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }
    
    /**
     * Validates that a date is not in the future.
     *
     * @param date the date to validate
     * @return true if the date is today or in the past, false if in the future
     */
    public static boolean isNotInFuture(LocalDate date) {
        if (date == null) {
            return false;
        }
        return !date.isAfter(LocalDate.now());
    }
    
    /**
     * Validates that a date is in the future.
     *
     * @param date the date to validate
     * @return true if the date is in the future, false otherwise
     */
    public static boolean isInFuture(LocalDate date) {
        if (date == null) {
            return false;
        }
        return date.isAfter(LocalDate.now());
    }
    
    /**
     * Validates that a date is in the past.
     *
     * @param date the date to validate
     * @return true if the date is in the past, false otherwise
     */
    public static boolean isInPast(LocalDate date) {
        if (date == null) {
            return false;
        }
        return date.isBefore(LocalDate.now());
    }
    
    /**
     * Formats a LocalDate to ISO format (yyyy-MM-dd).
     *
     * @param date the date to format
     * @return formatted date string, or null if date is null
     */
    public static String formatDate(LocalDate date) {
        if (date == null) {
            return null;
        }
        return date.format(ISO_DATE_FORMATTER);
    }
    
    /**
     * Formats a LocalDateTime to ISO format (yyyy-MM-ddTHH:mm:ss).
     *
     * @param dateTime the datetime to format
     * @return formatted datetime string, or null if datetime is null
     */
    public static String formatDateTime(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.format(ISO_DATETIME_FORMATTER);
    }
    
    /**
     * Formats a LocalDate for display (yyyy-MM-dd).
     *
     * @param date the date to format
     * @return formatted date string, or null if date is null
     */
    public static String formatDateForDisplay(LocalDate date) {
        if (date == null) {
            return null;
        }
        return date.format(DISPLAY_DATE_FORMATTER);
    }
    
    /**
     * Formats a LocalDateTime for display (yyyy-MM-dd HH:mm:ss).
     *
     * @param dateTime the datetime to format
     * @return formatted datetime string, or null if datetime is null
     */
    public static String formatDateTimeForDisplay(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.format(DISPLAY_DATETIME_FORMATTER);
    }
    
    /**
     * Parses a date string in ISO format (yyyy-MM-dd).
     *
     * @param dateString the date string to parse
     * @return parsed LocalDate, or null if string is null or empty
     * @throws DateTimeParseException if the string cannot be parsed
     */
    public static LocalDate parseDate(String dateString) {
        if (dateString == null || dateString.isBlank()) {
            return null;
        }
        return LocalDate.parse(dateString, ISO_DATE_FORMATTER);
    }
    
    /**
     * Parses a datetime string in ISO format (yyyy-MM-ddTHH:mm:ss).
     *
     * @param dateTimeString the datetime string to parse
     * @return parsed LocalDateTime, or null if string is null or empty
     * @throws DateTimeParseException if the string cannot be parsed
     */
    public static LocalDateTime parseDateTime(String dateTimeString) {
        if (dateTimeString == null || dateTimeString.isBlank()) {
            return null;
        }
        return LocalDateTime.parse(dateTimeString, ISO_DATETIME_FORMATTER);
    }
    
    /**
     * Validates if a string is a valid date in ISO format.
     *
     * @param dateString the date string to validate
     * @return true if the string is a valid date, false otherwise
     */
    public static boolean isValidDate(String dateString) {
        if (dateString == null || dateString.isBlank()) {
            return false;
        }
        try {
            LocalDate.parse(dateString, ISO_DATE_FORMATTER);
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }
    
    /**
     * Validates if a string is a valid datetime in ISO format.
     *
     * @param dateTimeString the datetime string to validate
     * @return true if the string is a valid datetime, false otherwise
     */
    public static boolean isValidDateTime(String dateTimeString) {
        if (dateTimeString == null || dateTimeString.isBlank()) {
            return false;
        }
        try {
            LocalDateTime.parse(dateTimeString, ISO_DATETIME_FORMATTER);
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }
}
