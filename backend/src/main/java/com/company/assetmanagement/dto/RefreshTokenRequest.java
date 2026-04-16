package com.company.assetmanagement.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;

/**
 * Data Transfer Object for refresh token requests.
 * Contains the refresh token used to obtain new access tokens.
 * 
 * Requirements: 2.1, 2.2, 2.3, 2.4
 */
public class RefreshTokenRequest {
    
    @NotBlank(message = "Refresh token is required")
    @JsonProperty("refresh_token")
    private String refreshToken;
    
    /**
     * Default constructor for JSON deserialization.
     */
    public RefreshTokenRequest() {
    }
    
    /**
     * Constructor with refresh token.
     *
     * @param refreshToken the refresh token
     */
    public RefreshTokenRequest(String refreshToken) {
        this.refreshToken = refreshToken;
    }
    
    /**
     * Gets the refresh token.
     *
     * @return the refresh token
     */
    public String getRefreshToken() {
        return refreshToken;
    }
    
    /**
     * Sets the refresh token.
     *
     * @param refreshToken the refresh token to set
     */
    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
    
    @Override
    public String toString() {
        return "RefreshTokenRequest{" +
                "refreshToken='[PROTECTED]'" +
                '}';
    }
}