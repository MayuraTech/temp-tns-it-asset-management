package com.company.assetmanagement.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDateTime;

/**
 * Generic API response wrapper for consistent response structure.
 * Can be used to wrap any response data with metadata.
 * 
 * @param <T> the type of data in the response
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {
    
    private boolean success;
    private String message;
    private T data;
    private LocalDateTime timestamp;
    private String requestId;
    
    public ApiResponse() {
        this.timestamp = LocalDateTime.now();
    }
    
    public ApiResponse(boolean success, T data) {
        this();
        this.success = success;
        this.data = data;
    }
    
    public ApiResponse(boolean success, String message, T data) {
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
    
    public T getData() {
        return data;
    }
    
    public void setData(T data) {
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
     * Builder for creating ApiResponse instances.
     */
    public static class Builder<T> {
        private final ApiResponse<T> response;
        
        public Builder() {
            this.response = new ApiResponse<>();
        }
        
        public Builder<T> success(boolean success) {
            response.success = success;
            return this;
        }
        
        public Builder<T> message(String message) {
            response.message = message;
            return this;
        }
        
        public Builder<T> data(T data) {
            response.data = data;
            return this;
        }
        
        public Builder<T> timestamp(LocalDateTime timestamp) {
            response.timestamp = timestamp;
            return this;
        }
        
        public Builder<T> requestId(String requestId) {
            response.requestId = requestId;
            return this;
        }
        
        public ApiResponse<T> build() {
            return response;
        }
    }
    
    public static <T> Builder<T> builder() {
        return new Builder<>();
    }
    
    /**
     * Create a successful response with data.
     */
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, data);
    }
    
    /**
     * Create a successful response with message and data.
     */
    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(true, message, data);
    }
    
    /**
     * Create a failure response with message.
     */
    public static <T> ApiResponse<T> failure(String message) {
        return new ApiResponse<>(false, message, null);
    }
}
