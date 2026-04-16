package com.company.assetmanagement.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * Data Transfer Object for password change requests.
 * Contains current password and new password with complexity validation.
 * 
 * Requirements: 3.1, 3.2, 3.3, 3.4, 3.5, 3.6
 */
public class ChangePasswordRequest {
    
    @NotBlank(message = "Current password is required")
    @JsonProperty("current_password")
    private String currentPassword;
    
    @NotBlank(message = "New password is required")
    @Pattern(
        regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$",
        message = "Password must contain at least 8 characters, one uppercase letter, one lowercase letter, one digit, and one special character (@$!%*?&)"
    )
    @JsonProperty("new_password")
    private String newPassword;
    
    /**
     * Default constructor for JSON deserialization.
     */
    public ChangePasswordRequest() {
    }
    
    /**
     * Constructor with all fields.
     *
     * @param currentPassword the current password
     * @param newPassword the new password
     */
    public ChangePasswordRequest(String currentPassword, String newPassword) {
        this.currentPassword = currentPassword;
        this.newPassword = newPassword;
    }
    
    /**
     * Gets the current password.
     *
     * @return the current password
     */
    public String getCurrentPassword() {
        return currentPassword;
    }
    
    /**
     * Sets the current password.
     *
     * @param currentPassword the current password to set
     */
    public void setCurrentPassword(String currentPassword) {
        this.currentPassword = currentPassword;
    }
    
    /**
     * Gets the new password.
     *
     * @return the new password
     */
    public String getNewPassword() {
        return newPassword;
    }
    
    /**
     * Sets the new password.
     *
     * @param newPassword the new password to set
     */
    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }
    
    @Override
    public String toString() {
        return "ChangePasswordRequest{" +
                "currentPassword='[PROTECTED]'" +
                ", newPassword='[PROTECTED]'" +
                '}';
    }
}