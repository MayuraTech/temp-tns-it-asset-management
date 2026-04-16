package com.company.assetmanagement.exception;

/**
 * Exception thrown when attempting to access a user that does not exist in the system.
 * Contains the user ID that was not found for error reporting and debugging purposes.
 */
public class UserNotFoundException extends RuntimeException {
    
    private final String userId;
    
    /**
     * Creates a new UserNotFoundException with the user ID that was not found.
     * 
     * @param userId the user ID that was not found
     */
    public UserNotFoundException(String userId) {
        super("User not found: " + userId);
        this.userId = userId;
    }
    
    /**
     * Creates a new UserNotFoundException with a custom message and user ID.
     * 
     * @param message custom error message
     * @param userId the user ID that was not found
     */
    public UserNotFoundException(String message, String userId) {
        super(message);
        this.userId = userId;
    }
    
    /**
     * Gets the user ID that was not found.
     * 
     * @return the user ID that caused this exception
     */
    public String getUserId() {
        return userId;
    }
}