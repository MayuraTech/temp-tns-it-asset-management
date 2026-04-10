package com.company.assetmanagement.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * Standard error response structure for all API errors.
 * Provides consistent error information across the application.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {
    
    private String type;
    private String message;
    private Object details;
    private LocalDateTime timestamp;
    private String requestId;
    
    public ErrorResponse() {
        this.timestamp = LocalDateTime.now();
    }
    
    public ErrorResponse(String type, String message) {
        this();
        this.type = type;
        this.message = message;
    }
    
    public ErrorResponse(String type, String message, Object details) {
        this(type, message);
        this.details = details;
    }
    
    public ErrorResponse(String type, String message, Object details, String requestId) {
        this(type, message, details);
        this.requestId = requestId;
    }
    
    // Getters and setters
    
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public Object getDetails() {
        return details;
    }
    
    public void setDetails(Object details) {
        this.details = details;
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
     * Builder for creating ErrorResponse instances.
     */
    public static class Builder {
        private final ErrorResponse response;
        
        public Builder() {
            this.response = new ErrorResponse();
        }
        
        public Builder type(String type) {
            response.type = type;
            return this;
        }
        
        public Builder message(String message) {
            response.message = message;
            return this;
        }
        
        public Builder details(Object details) {
            response.details = details;
            return this;
        }
        
        public Builder requestId(String requestId) {
            response.requestId = requestId;
            return this;
        }
        
        public Builder timestamp(LocalDateTime timestamp) {
            response.timestamp = timestamp;
            return this;
        }
        
        public ErrorResponse build() {
            return response;
        }
    }
    
    public static Builder builder() {
        return new Builder();
    }
}
