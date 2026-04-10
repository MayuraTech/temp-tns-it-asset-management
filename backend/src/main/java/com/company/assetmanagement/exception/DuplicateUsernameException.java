package com.company.assetmanagement.exception;

/**
 * Exception thrown when attempting to create or update a user with a username that already exists.
 * Contains the duplicate username for error reporting and debugging purposes.
 */
public class DuplicateUsernameException extends RuntimeException {
    
    private final String username;
    
    /**
     * Creates a new DuplicateUsernameException with the duplicate username.
     * 
     * @param username the username that already exists
     */
    public DuplicateUsernameException(String username) {
        super("Username already exists: " + username);
        this.username = username;
    }
    
    /**
     * Creates a new DuplicateUsernameException with a custom message and username.
     * 
     * @param message custom error message
     * @param username the username that already exists
     */
    public DuplicateUsernameException(String message, String username) {
        super(message);
        this.username = username;
    }
    
    /**
     * Gets the duplicate username that caused this exception.
     * 
     * @return the duplicate username
     */
    public String getUsername() {
        return username;
    }
}