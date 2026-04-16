package com.company.assetmanagement.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Data Transfer Object for user login requests.
 * Contains username and password credentials for authentication.
 * 
 * Requirements: 1.1, 1.2, 1.3, 1.4, 1.5, 1.6, 1.7, 1.8
 */
public class LoginRequest {
    
    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 100, message = "Username must be between 3 and 100 characters")
    private String username;
    
    @NotBlank(message = "Password is required")
    private String password;
    
    /**
     * Default constructor for JSON deserialization.
     */
    public LoginRequest() {
    }
    
    /**
     * Constructor with all fields.
     *
     * @param username the username
     * @param password the password
     */
    public LoginRequest(String username, String password) {
        this.username = username;
        this.password = password;
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
     * Gets the password.
     *
     * @return the password
     */
    public String getPassword() {
        return password;
    }
    
    /**
     * Sets the password.
     *
     * @param password the password to set
     */
    public void setPassword(String password) {
        this.password = password;
    }
    
    @Override
    public String toString() {
        return "LoginRequest{" +
                "username='" + username + '\'' +
                ", password='[PROTECTED]'" +
                '}';
    }
}