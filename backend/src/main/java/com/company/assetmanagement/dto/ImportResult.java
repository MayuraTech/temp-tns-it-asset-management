package com.company.assetmanagement.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Transfer Object for asset import operation results.
 * Contains success/failure counts and detailed error information.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ImportResult {
    
    private int successCount;
    private int failureCount;
    private int totalRecords;
    private List<ImportError> errors;
    private LocalDateTime timestamp;
    private String message;
    
    public ImportResult() {
        this.errors = new ArrayList<>();
        this.timestamp = LocalDateTime.now();
    }
    
    public ImportResult(int successCount, int failureCount, int totalRecords) {
        this();
        this.successCount = successCount;
        this.failureCount = failureCount;
        this.totalRecords = totalRecords;
    }
    
    // Getters and setters
    
    public int getSuccessCount() {
        return successCount;
    }
    
    public void setSuccessCount(int successCount) {
        this.successCount = successCount;
    }
    
    public int getFailureCount() {
        return failureCount;
    }
    
    public void setFailureCount(int failureCount) {
        this.failureCount = failureCount;
    }
    
    public int getTotalRecords() {
        return totalRecords;
    }
    
    public void setTotalRecords(int totalRecords) {
        this.totalRecords = totalRecords;
    }
    
    public List<ImportError> getErrors() {
        return errors;
    }
    
    public void setErrors(List<ImportError> errors) {
        this.errors = errors;
    }
    
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    /**
     * Add an import error to the result.
     */
    public void addError(ImportError error) {
        if (this.errors == null) {
            this.errors = new ArrayList<>();
        }
        this.errors.add(error);
    }
    
    /**
     * Add an import error with line number and message.
     */
    public void addError(int lineNumber, String errorMessage) {
        addError(new ImportError(lineNumber, errorMessage));
    }
    
    /**
     * Check if the import was completely successful.
     */
    public boolean isSuccess() {
        return failureCount == 0;
    }
    
    /**
     * Represents a single import error with line number and message.
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ImportError {
        private int lineNumber;
        private String errorMessage;
        private String serialNumber;
        
        public ImportError() {
        }
        
        public ImportError(int lineNumber, String errorMessage) {
            this.lineNumber = lineNumber;
            this.errorMessage = errorMessage;
        }
        
        public ImportError(int lineNumber, String errorMessage, String serialNumber) {
            this.lineNumber = lineNumber;
            this.errorMessage = errorMessage;
            this.serialNumber = serialNumber;
        }
        
        public int getLineNumber() {
            return lineNumber;
        }
        
        public void setLineNumber(int lineNumber) {
            this.lineNumber = lineNumber;
        }
        
        public String getErrorMessage() {
            return errorMessage;
        }
        
        public void setErrorMessage(String errorMessage) {
            this.errorMessage = errorMessage;
        }
        
        public String getSerialNumber() {
            return serialNumber;
        }
        
        public void setSerialNumber(String serialNumber) {
            this.serialNumber = serialNumber;
        }
    }
    
    /**
     * Builder for creating ImportResult instances.
     */
    public static class Builder {
        private final ImportResult result;
        
        public Builder() {
            this.result = new ImportResult();
        }
        
        public Builder successCount(int successCount) {
            result.successCount = successCount;
            return this;
        }
        
        public Builder failureCount(int failureCount) {
            result.failureCount = failureCount;
            return this;
        }
        
        public Builder totalRecords(int totalRecords) {
            result.totalRecords = totalRecords;
            return this;
        }
        
        public Builder errors(List<ImportError> errors) {
            result.errors = errors;
            return this;
        }
        
        public Builder message(String message) {
            result.message = message;
            return this;
        }
        
        public Builder timestamp(LocalDateTime timestamp) {
            result.timestamp = timestamp;
            return this;
        }
        
        public Builder addError(ImportError error) {
            result.addError(error);
            return this;
        }
        
        public Builder addError(int lineNumber, String errorMessage) {
            result.addError(lineNumber, errorMessage);
            return this;
        }
        
        public ImportResult build() {
            return result;
        }
    }
    
    public static Builder builder() {
        return new Builder();
    }
}
