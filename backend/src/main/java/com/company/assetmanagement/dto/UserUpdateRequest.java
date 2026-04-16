package com.company.assetmanagement.dto;

import jakarta.validation.constraints.*;

/**
 * Data Transfer Object for user update requests.
 * Contains fields that can be updated for an existing user account.
 * 
 * Note: Password updates are not allowed through this endpoint.
 * Use ProfileService.changePassword() for password changes.
 * Role updates are handled through separate role assignment endpoints.
 * 
 * Validation Rules:
 * - Username: Optional, if provided must be 3-100 characters, alphanumeric and underscores only
 * - Email: Optional, if provided must be valid email format, 5-255 characters
 * 
 * Requirements: 6.1, 6.2, 6.3, 6.4, 6.5, 6.6
 */
public class UserUpdateRequest {
    
    @Size(min = 3, max = 100, message = "Username must be between 3 and 100 characters")
    @Pattern(
        regexp = "^[a-zA-Z0-9_]+$", 
        message = "Username must contain only alphanumeric characters and underscores"
    )
    private String username;
    
    @Email(message = "Invalid email format")
    @Size(min = 5, max = 255, message = "Email must be between 5 and 255 characters")
    private String email;
    
    /**
     * Default constructor for JSON deserialization.
     */
    public UserUpdateRequest() {
    }
    
    /**
     * Constructor with all fields.
     *
     * @param username the username (optional)
     * @param email the email address (optional)
     */
    public UserUpdateRequest(String username, String email) {
        this.username = username;
        this.email = email;
    }
    
    /**
     * Gets the username.
     *
     * @return the username
     */
    public String getUsername() {
        return username;
    }
    
    /**
     * Sets the username.
     *
     * @param username the username to set
     */
    public void setUsername(String username) {
        this.username = username;
    }
    
    /**
     * Gets the email address.
     *
     * @return the email address
     */
    public String getEmail() {
        return email;
    }
    
    /**
     * Sets the email address.
     *
     * @param email the email address to set
     */
    public void setEmail(String email) {
        this.email = email;
    }
    
    @Override
    public String toString() {
        return "UserUpdateRequest{" +
                "username='" + username + '\'' +
                ", email='" + email + '\'' +
                '}';
    }
}