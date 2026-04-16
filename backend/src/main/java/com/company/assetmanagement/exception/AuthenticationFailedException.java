package com.company.assetmanagement.exception;

/**
 * Exception thrown when authentication fails.
 * Used for invalid credentials, account lockout, or other authentication errors.
 */
public class AuthenticationFailedException extends RuntimeException {
    
    private final String username;
    private final String errorType;
    
    public AuthenticationFailedException(String username, String errorType, String message) {
        super(message);
        this.username = username;
        this.errorType = errorType;
    }
    
    public String getUsername() {
        return username;
    }
    
    public String getErrorType() {
        return errorType;
    }
}
