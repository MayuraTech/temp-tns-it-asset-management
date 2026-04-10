package com.company.assetmanagement.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Represents a single validation error for a field.
 * Used in validation error responses to provide detailed field-level error information.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ValidationError {
    
    private String field;
    private String message;
    private Object value;
    
    public ValidationError() {
    }
    
    public ValidationError(String field, String message) {
        this.field = field;
        this.message = message;
    }
    
    public ValidationError(String field, String message, Object value) {
        this.field = field;
        this.message = message;
        this.value = value;
    }
    
    // Getters and setters
    
    public String getField() {
        return field;
    }
    
    public void setField(String field) {
        this.field = field;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public Object getValue() {
        return value;
    }
    
    public void setValue(Object value) {
        this.value = value;
    }
    
    /**
     * Builder for creating ValidationError instances.
     */
    public static class Builder {
        private final ValidationError error;
        
        public Builder() {
            this.error = new ValidationError();
        }
        
        public Builder field(String field) {
            error.field = field;
            return this;
        }
        
        public Builder message(String message) {
            error.message = message;
            return this;
        }
        
        public Builder value(Object value) {
            error.value = value;
            return this;
        }
        
        public ValidationError build() {
            return error;
        }
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    @Override
    public String toString() {
        return "ValidationError{" +
                "field='" + field + '\'' +
                ", message='" + message + '\'' +
                ", value=" + value +
                '}';
    }
}
