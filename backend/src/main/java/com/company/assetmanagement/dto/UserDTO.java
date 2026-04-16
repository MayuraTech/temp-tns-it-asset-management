package com.company.assetmanagement.dto;

import com.company.assetmanagement.model.Role;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

/**
 * Data Transfer Object for user information responses.
 * Contains all user information that can be safely exposed via API.
 * 
 * Security Note: Password hash is explicitly excluded from this DTO
 * to ensure it is never exposed in API responses.
 * 
 * Requirements: 5.1, 5.2, 5.3, 5.4, 5.5, 5.6, 11.1, 11.2, 11.3, 11.4, 11.5, 11.6
 */
public class UserDTO {
    
    private UUID id;
    
    private String username;
    
    private String email;
    
    @JsonProperty("is_active")
    private Boolean isActive;
    
    @JsonProperty("account_locked")
    private Boolean accountLocked;
    
    @JsonProperty("lock_until")
    private LocalDateTime lockUntil;
    
    @JsonProperty("last_login_at")
    private LocalDateTime lastLoginAt;
    
    private Set<Role> roles;
    
    @JsonProperty("created_at")
    private LocalDateTime createdAt;
    
    @JsonProperty("updated_at")
    private LocalDateTime updatedAt;
    
    @JsonProperty("created_by")
    private String createdBy;
    
    @JsonProperty("updated_by")
    private String updatedBy;
    
    /**
     * Default constructor for JSON serialization.
     */
    public UserDTO() {
    }
    
    /**
     * Constructor with all fields.
     *
     * @param id the user ID
     * @param username the username
     * @param email the email address
     * @param isActive the active status
     * @param accountLocked the locked status
     * @param lockUntil the lock expiration time
     * @param lastLoginAt the last login timestamp
     * @param roles the set of assigned roles
     * @param createdAt the creation timestamp
     * @param updatedAt the last update timestamp
     * @param createdBy the username of the creator
     * @param updatedBy the username of the last updater
     */
    public UserDTO(UUID id, String username, String email, Boolean isActive, 
                   Boolean accountLocked, LocalDateTime lockUntil, LocalDateTime lastLoginAt,
                   Set<Role> roles, LocalDateTime createdAt, LocalDateTime updatedAt,
                   String createdBy, String updatedBy) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.isActive = isActive;
        this.accountLocked = accountLocked;
        this.lockUntil = lockUntil;
        this.lastLoginAt = lastLoginAt;
        this.roles = roles;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.createdBy = createdBy;
        this.updatedBy = updatedBy;
    }
    
    /**
     * Gets the user ID.
     *
     * @return the user ID
     */
    public UUID getId() {
        return id;
    }
    
    /**
     * Sets the user ID.
     *
     * @param id the user ID to set
     */
    public void setId(UUID id) {
        this.id = id;
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
     * Gets the active status.
     *
     * @return the active status
     */
    public Boolean getIsActive() {
        return isActive;
    }
    
    /**
     * Sets the active status.
     *
     * @param isActive the active status to set
     */
    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }
    
    /**
     * Gets the locked status.
     *
     * @return the locked status
     */
    public Boolean getAccountLocked() {
        return accountLocked;
    }
    
    /**
     * Sets the locked status.
     *
     * @param accountLocked the locked status to set
     */
    public void setAccountLocked(Boolean accountLocked) {
        this.accountLocked = accountLocked;
    }
    
    /**
     * Gets the lock expiration time.
     *
     * @return the lock expiration time
     */
    public LocalDateTime getLockUntil() {
        return lockUntil;
    }
    
    /**
     * Sets the lock expiration time.
     *
     * @param lockUntil the lock expiration time to set
     */
    public void setLockUntil(LocalDateTime lockUntil) {
        this.lockUntil = lockUntil;
    }
    
    /**
     * Gets the last login timestamp.
     *
     * @return the last login timestamp
     */
    public LocalDateTime getLastLoginAt() {
        return lastLoginAt;
    }
    
    /**
     * Sets the last login timestamp.
     *
     * @param lastLoginAt the last login timestamp to set
     */
    public void setLastLoginAt(LocalDateTime lastLoginAt) {
        this.lastLoginAt = lastLoginAt;
    }
    
    /**
     * Gets the set of assigned roles.
     *
     * @return the set of assigned roles
     */
    public Set<Role> getRoles() {
        return roles;
    }
    
    /**
     * Sets the set of assigned roles.
     *
     * @param roles the set of assigned roles to set
     */
    public void setRoles(Set<Role> roles) {
        this.roles = roles;
    }
    
    /**
     * Gets the creation timestamp.
     *
     * @return the creation timestamp
     */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    /**
     * Sets the creation timestamp.
     *
     * @param createdAt the creation timestamp to set
     */
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    /**
     * Gets the last update timestamp.
     *
     * @return the last update timestamp
     */
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    /**
     * Sets the last update timestamp.
     *
     * @param updatedAt the last update timestamp to set
     */
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    /**
     * Gets the username of the creator.
     *
     * @return the username of the creator
     */
    public String getCreatedBy() {
        return createdBy;
    }
    
    /**
     * Sets the username of the creator.
     *
     * @param createdBy the username of the creator to set
     */
    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }
    
    /**
     * Gets the username of the last updater.
     *
     * @return the username of the last updater
     */
    public String getUpdatedBy() {
        return updatedBy;
    }
    
    /**
     * Sets the username of the last updater.
     *
     * @param updatedBy the username of the last updater to set
     */
    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }
    
    @Override
    public String toString() {
        return "UserDTO{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", isActive=" + isActive +
                ", accountLocked=" + accountLocked +
                ", lockUntil=" + lockUntil +
                ", lastLoginAt=" + lastLoginAt +
                ", roles=" + roles +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                ", createdBy='" + createdBy + '\'' +
                ", updatedBy='" + updatedBy + '\'' +
                '}';
    }
}