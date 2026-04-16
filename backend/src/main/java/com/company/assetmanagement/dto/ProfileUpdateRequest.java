package com.company.assetmanagement.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

/**
 * Data Transfer Object for user profile update requests.
 * Contains fields that users can update in their own profile.
 * 
 * Note: Username and roles cannot be changed through the profile endpoint.
 * Password changes use a separate changePassword endpoint for security.
 * Only administrators can modify usernames and roles through user management endpoints.
 * 
 * Validation Rules:
 * - Email: Optional, if provided must be valid email format, 5-255 characters
 * 
 * Requirements: 11.1, 11.2, 11.3, 11.4, 11.5, 11.6
 */
public class ProfileUpdateRequest {
    
    @Email(message = "Invalid email format")
    @Size(min = 5, max = 255, message = "Email must be between 5 and 255 characters")
    private String email;
    
    /**
     * Default constructor for JSON deserialization.
     */
    public ProfileUpdateRequest() {
    }
    
    /**
     * Constructor with email.
     *
     * @param email the email address
     */
    public ProfileUpdateRequest(String email) {
        this.email = email;
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
        return "ProfileUpdateRequest{" +
                "email='" + email + '\'' +
                '}';
    }
}