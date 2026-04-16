package com.company.assetmanagement.exception;

/**
 * Exception thrown when attempting to create or update a user with an email address that already exists.
 * Contains the duplicate email for error reporting and debugging purposes.
 */
public class DuplicateEmailException extends RuntimeException {
    
    private final String email;
    
    /**
     * Creates a new DuplicateEmailException with the duplicate email.
     * 
     * @param email the email address that already exists
     */
    public DuplicateEmailException(String email) {
        super("Email already exists: " + email);
        this.email = email;
    }
    
    /**
     * Creates a new DuplicateEmailException with a custom message and email.
     * 
     * @param message custom error message
     * @param email the email address that already exists
     */
    public DuplicateEmailException(String message, String email) {
        super(message);
        this.email = email;
    }
    
    /**
     * Gets the duplicate email address that caused this exception.
     * 
     * @return the duplicate email address
     */
    public String getEmail() {
        return email;
    }
}