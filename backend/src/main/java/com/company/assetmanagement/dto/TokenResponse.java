package com.company.assetmanagement.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Data Transfer Object for authentication token responses.
 * Contains access token, refresh token, token type, and expiration information.
 * 
 * Requirements: 1.1, 1.2, 2.1, 2.2, 2.5
 */
public class TokenResponse {
    
    @JsonProperty("access_token")
    private String accessToken;
    
    @JsonProperty("refresh_token")
    private String refreshToken;
    
    @JsonProperty("token_type")
    private String tokenType = "Bearer";
    
    @JsonProperty("expires_in")
    private Long expiresIn;
    
    /**
     * Default constructor for JSON serialization.
     */
    public TokenResponse() {
    }
    
    /**
     * Constructor with all fields.
     *
     * @param accessToken the access token
     * @param refreshToken the refresh token
     * @param tokenType the token type (typically "Bearer")
     * @param expiresIn the expiration time in seconds
     */
    public TokenResponse(String accessToken, String refreshToken, String tokenType, Long expiresIn) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.tokenType = tokenType;
        this.expiresIn = expiresIn;
    }
    
    /**
     * Convenience constructor with default token type.
     *
     * @param accessToken the access token
     * @param refreshToken the refresh token
     * @param expiresIn the expiration time in seconds
     */
    public TokenResponse(String accessToken, String refreshToken, Long expiresIn) {
        this(accessToken, refreshToken, "Bearer", expiresIn);
    }
    
    /**
     * Gets the access token.
     *
     * @return the access token
     */
    public String getAccessToken() {
        return accessToken;
    }
    
    /**
     * Sets the access token.
     *
     * @param accessToken the access token to set
     */
    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
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
    
    /**
     * Gets the token type.
     *
     * @return the token type
     */
    public String getTokenType() {
        return tokenType;
    }
    
    /**
     * Sets the token type.
     *
     * @param tokenType the token type to set
     */
    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }
    
    /**
     * Gets the expiration time in seconds.
     *
     * @return the expiration time in seconds
     */
    public Long getExpiresIn() {
        return expiresIn;
    }
    
    /**
     * Sets the expiration time in seconds.
     *
     * @param expiresIn the expiration time in seconds to set
     */
    public void setExpiresIn(Long expiresIn) {
        this.expiresIn = expiresIn;
    }
    
    @Override
    public String toString() {
        return "TokenResponse{" +
                "accessToken='[PROTECTED]'" +
                ", refreshToken='[PROTECTED]'" +
                ", tokenType='" + tokenType + '\'' +
                ", expiresIn=" + expiresIn +
                '}';
    }
}