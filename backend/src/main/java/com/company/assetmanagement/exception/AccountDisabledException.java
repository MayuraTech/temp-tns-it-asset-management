package com.company.assetmanagement.exception;

/**
 * Exception thrown when attempting to authenticate with a disabled user account.
 * Indicates that the account has been administratively disabled and cannot be used for authentication.
 */
public class AccountDisabledException extends RuntimeException {
    
    private final String userId;
    
    /**
     * Creates a new AccountDisabledException with a default message.
     */
    public AccountDisabledException() {
        super("Account is disabled");
        this.userId = null;
    }
    
    /**
     * Creates a new AccountDisabledException with the user ID.
     * 
     * @param userId the ID of the disabled user account
     */
    public AccountDisabledException(String userId) {
        super("Account is disabled for user: " + userId);
        this.userId = userId;
    }
    
    /**
     * Creates a new AccountDisabledException with a custom message and user ID.
     * 
     * @param message custom error message
     * @param userId the ID of the disabled user account
     */
    public AccountDisabledException(String message, String userId) {
        super(message);
        this.userId = userId;
    }
    
    /**
     * Gets the ID of the disabled user account.
     * 
     * @return the user ID, or null if not specified
     */
    public String getUserId() {
        return userId;
    }
}