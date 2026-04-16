package com.company.assetmanagement.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;

/**
 * Wrapper for asset API responses.
 * Provides consistent response structure with metadata.
 * Extends the generic ApiResponse for asset-specific responses.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AssetResponse {
    
    private boolean success;
    private String message;
    private AssetDTO data;
    private LocalDateTime timestamp;
    private String requestId;
    
    public AssetResponse() {
        this.timestamp = LocalDateTime.now();
    }
    
    public AssetResponse(boolean success, AssetDTO data) {
        this();
        this.success = success;
        this.data = data;
    }
    
    public AssetResponse(boolean success, String message, AssetDTO data) {
        this(success, data);
        this.message = message;
    }
    
    // Getters and setters
    
    public boolean isSuccess() {
        return success;
    }
    
    public void setSuccess(boolean success) {
        this.success = success;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public AssetDTO getData() {
        return data;
    }
    
    public void setData(AssetDTO data) {
        this.data = data;
    }
    
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
    
    public String getRequestId() {
        return requestId;
    }
    
    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }
    
    /**
     * Builder for creating AssetResponse instances.
     */
    public static class Builder {
        private final AssetResponse response;
        
        public Builder() {
            this.response = new AssetResponse();
        }
        
        public Builder success(boolean success) {
            response.success = success;
            return this;
        }
        
        public Builder message(String message) {
            response.message = message;
            return this;
        }
        
        public Builder data(AssetDTO data) {
            response.data = data;
            return this;
        }
        
        public Builder timestamp(LocalDateTime timestamp) {
            response.timestamp = timestamp;
            return this;
        }
        
        public Builder requestId(String requestId) {
            response.requestId = requestId;
            return this;
        }
        
        public AssetResponse build() {
            return response;
        }
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    /**
     * Create a successful response with asset data.
     */
    public static AssetResponse success(AssetDTO data) {
        return new AssetResponse(true, data);
    }
    
    /**
     * Create a successful response with message and asset data.
     */
    public static AssetResponse success(String message, AssetDTO data) {
        return new AssetResponse(true, message, data);
    }
    
    /**
     * Create a failure response with message.
     */
    public static AssetResponse failure(String message) {
        return new AssetResponse(false, message, null);
    }
}
