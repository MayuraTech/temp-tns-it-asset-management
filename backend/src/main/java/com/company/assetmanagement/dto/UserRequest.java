package com.company.assetmanagement.dto;

import com.company.assetmanagement.model.Role;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;

import java.util.Set;

/**
 * Data Transfer Object for user creation requests.
 * Contains all required information to create a new user account including
 * username, email, password, and initial role assignments.
 * 
 * Validation Rules:
 * - Username: Required, 3-100 characters, alphanumeric and underscores only
 * - Email: Required, valid email format, 5-255 characters
 * - Password: Required, minimum 8 characters with complexity requirements
 * - Roles: At least one role must be assigned
 * 
 * Requirements: 4.1, 4.2, 4.3, 4.4, 4.5, 4.6, 5.1, 5.2, 5.3, 5.4, 5.5, 5.6, 
 *              6.1, 6.2, 6.3, 6.4, 6.5, 6.6, 11.1, 11.2, 11.3, 11.4, 11.5, 11.6
 */
public class UserRequest {
    
    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 100, message = "Username must be between 3 and 100 characters")
    @Pattern(
        regexp = "^[a-zA-Z0-9_]+$", 
        message = "Username must contain only alphanumeric characters and underscores"
    )
    private String username;
    
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Size(min = 5, max = 255, message = "Email must be between 5 and 255 characters")
    private String email;
    
    @NotBlank(message = "Password is required")
    @Pattern(
        regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$",
        message = "Password must contain at least 8 characters, one uppercase letter, one lowercase letter, one digit, and one special character (@$!%*?&)"
    )
    private String password;
    
    @NotNull(message = "At least one role is required")
    @Size(min = 1, message = "At least one role is required")
    private Set<Role> roles;
    
    /**
     * Default constructor for JSON deserialization.
     */
    public UserRequest() {
    }
    
    /**
     * Constructor with all fields.
     *
     * @param username the username
     * @param email the email address
     * @param password the password
     * @param roles the set of roles to assign
     */
    public UserRequest(String username, String email, String password, Set<Role> roles) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.roles = roles;
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
    
    /**
     * Gets the set of roles.
     *
     * @return the set of roles
     */
    public Set<Role> getRoles() {
        return roles;
    }
    
    /**
     * Sets the set of roles.
     *
     * @param roles the set of roles to set
     */
    public void setRoles(Set<Role> roles) {
        this.roles = roles;
    }
    
    @Override
    public String toString() {
        return "UserRequest{" +
                "username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", password='[PROTECTED]'" +
                ", roles=" + roles +
                '}';
    }
}