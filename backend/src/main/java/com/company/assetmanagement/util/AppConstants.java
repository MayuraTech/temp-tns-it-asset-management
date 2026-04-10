package com.company.assetmanagement.util;

/**
 * Application-wide constants used throughout the IT Asset Management system.
 * Centralizes configuration values and magic numbers.
 */
public final class AppConstants {
    
    private AppConstants() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }
    
    // ========== Authentication & Security Constants ==========
    
    /**
     * Maximum number of failed login attempts before account lockout.
     * Requirement 1.4: Account lockout after 5 failed attempts
     */
    public static final int MAX_FAILED_LOGIN_ATTEMPTS = 5;
    
    /**
     * Account lockout duration in minutes.
     * Requirement 1.4: 15-minute lockout period
     */
    public static final int ACCOUNT_LOCKOUT_MINUTES = 15;
    
    /**
     * Session inactivity timeout in minutes.
     * Requirement 1.5: 30-minute inactivity timeout
     */
    public static final int SESSION_TIMEOUT_MINUTES = 30;
    
    /**
     * Minimum password length.
     * Requirement 1.3: Minimum 12 characters
     */
    public static final int MIN_PASSWORD_LENGTH = 12;
    
    /**
     * JWT token expiration time in milliseconds (30 minutes).
     */
    public static final long JWT_EXPIRATION_MS = 1800000L;
    
    /**
     * JWT refresh token expiration time in milliseconds (24 hours).
     */
    public static final long JWT_REFRESH_EXPIRATION_MS = 86400000L;
    
    // ========== Validation Constants ==========
    
    /**
     * Maximum length for asset name field.
     */
    public static final int MAX_ASSET_NAME_LENGTH = 255;
    
    /**
     * Minimum length for serial number.
     */
    public static final int MIN_SERIAL_NUMBER_LENGTH = 5;
    
    /**
     * Maximum length for serial number.
     */
    public static final int MAX_SERIAL_NUMBER_LENGTH = 100;
    
    /**
     * Maximum length for location field.
     */
    public static final int MAX_LOCATION_LENGTH = 255;
    
    /**
     * Maximum length for email field.
     */
    public static final int MAX_EMAIL_LENGTH = 255;
    
    /**
     * Maximum length for username field.
     */
    public static final int MAX_USERNAME_LENGTH = 100;
    
    /**
     * Maximum length for notes field.
     */
    public static final int MAX_NOTES_LENGTH = 4000;
    
    // ========== Pagination Constants ==========
    
    /**
     * Default page size for paginated results.
     */
    public static final int DEFAULT_PAGE_SIZE = 20;
    
    /**
     * Maximum page size for paginated results.
     */
    public static final int MAX_PAGE_SIZE = 100;
    
    /**
     * Default page number (zero-based).
     */
    public static final int DEFAULT_PAGE_NUMBER = 0;
    
    // ========== Performance Constants ==========
    
    /**
     * Maximum search response time in seconds.
     * Requirement 6.2: Sub-2-second search for 100,000 assets
     */
    public static final int MAX_SEARCH_TIME_SECONDS = 2;
    
    /**
     * Maximum report generation time in seconds.
     * Requirement 8.5: 10-second report generation for 100,000 assets
     */
    public static final int MAX_REPORT_TIME_SECONDS = 10;
    
    /**
     * Maximum export generation time in seconds.
     * Requirement 10.5: 30-second export for 100,000 assets
     */
    public static final int MAX_EXPORT_TIME_SECONDS = 30;
    
    /**
     * Maximum bulk import size.
     * Requirement 10.4: Support up to 10,000 records
     */
    public static final int MAX_BULK_IMPORT_SIZE = 10000;
    
    // ========== Audit Log Constants ==========
    
    /**
     * Minimum audit log retention period in years.
     * Requirement 9.3: 7-year retention
     */
    public static final int AUDIT_LOG_RETENTION_YEARS = 7;
    
    // ========== Configuration Constants ==========
    
    /**
     * Minimum session timeout in minutes (configurable).
     * Requirement 12.1: 10-120 minute range
     */
    public static final int MIN_SESSION_TIMEOUT_MINUTES = 10;
    
    /**
     * Maximum session timeout in minutes (configurable).
     * Requirement 12.1: 10-120 minute range
     */
    public static final int MAX_SESSION_TIMEOUT_MINUTES = 120;
    
    // ========== API Constants ==========
    
    /**
     * API version prefix.
     */
    public static final String API_VERSION = "v1";
    
    /**
     * API base path.
     */
    public static final String API_BASE_PATH = "/api/" + API_VERSION;
    
    /**
     * Request ID header name.
     */
    public static final String REQUEST_ID_HEADER = "X-Request-ID";
    
    /**
     * Rate limit header name.
     */
    public static final String RATE_LIMIT_HEADER = "X-RateLimit-Limit";
    
    /**
     * Rate limit remaining header name.
     */
    public static final String RATE_LIMIT_REMAINING_HEADER = "X-RateLimit-Remaining";
    
    /**
     * Rate limit reset header name.
     */
    public static final String RATE_LIMIT_RESET_HEADER = "X-RateLimit-Reset";
    
    // ========== Role Constants ==========
    
    /**
     * Administrator role name.
     */
    public static final String ROLE_ADMINISTRATOR = "Administrator";
    
    /**
     * Asset Manager role name.
     */
    public static final String ROLE_ASSET_MANAGER = "Asset_Manager";
    
    /**
     * Viewer role name.
     */
    public static final String ROLE_VIEWER = "Viewer";
    
    // ========== Ticket Constants ==========
    
    /**
     * Ticket number prefix.
     */
    public static final String TICKET_NUMBER_PREFIX = "TKT";
    
    /**
     * Ticket number format (e.g., TKT-2024-00001).
     */
    public static final String TICKET_NUMBER_FORMAT = "%s-%d-%05d";
    
    // ========== Date Format Constants ==========
    
    /**
     * ISO date format pattern.
     */
    public static final String ISO_DATE_PATTERN = "yyyy-MM-dd";
    
    /**
     * ISO datetime format pattern.
     */
    public static final String ISO_DATETIME_PATTERN = "yyyy-MM-dd'T'HH:mm:ss";
    
    /**
     * Display date format pattern.
     */
    public static final String DISPLAY_DATE_PATTERN = "yyyy-MM-dd";
    
    /**
     * Display datetime format pattern.
     */
    public static final String DISPLAY_DATETIME_PATTERN = "yyyy-MM-dd HH:mm:ss";
    
    // ========== File Upload Constants ==========
    
    /**
     * Maximum file upload size in bytes (10 MB).
     */
    public static final long MAX_FILE_UPLOAD_SIZE = 10485760L;
    
    /**
     * Allowed import file formats.
     */
    public static final String[] ALLOWED_IMPORT_FORMATS = {"csv", "json"};
    
    /**
     * Allowed export file formats.
     */
    public static final String[] ALLOWED_EXPORT_FORMATS = {"csv", "json"};
}
