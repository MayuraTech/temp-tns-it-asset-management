package com.company.assetmanagement.exception;

import java.util.ArrayList;
import java.util.List;

/**
 * Exception thrown when validation of input data fails.
 * Contains a list of validation errors with field names and messages.
 */
public class ValidationException extends RuntimeException {
    
    private final List<ValidationError> errors;
    
    public ValidationException(List<ValidationError> errors) {
        super("Validation failed");
        this.errors = new ArrayList<>(errors);
    }
    
    public ValidationException(String field, String message) {
        super("Validation failed");
        this.errors = List.of(new ValidationError(field, message));
    }
    
    public List<ValidationError> getErrors() {
        return new ArrayList<>(errors);
    }
    
    /**
     * Represents a single validation error for a specific field.
     */
    public static class ValidationError {
        private final String field;
        private final String message;
        private final Object value;
        
        public ValidationError(String field, String message) {
            this(field, message, null);
        }
        
        public ValidationError(String field, String message, Object value) {
            this.field = field;
            this.message = message;
            this.value = value;
        }
        
        public String getField() {
            return field;
        }
        
        public String getMessage() {
            return message;
        }
        
        public Object getValue() {
            return value;
        }
    }
}
