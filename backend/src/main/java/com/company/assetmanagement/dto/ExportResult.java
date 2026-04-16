package com.company.assetmanagement.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;

/**
 * Data Transfer Object for asset export operation results.
 * Contains export metadata and file information.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ExportResult {
    
    private String fileName;
    private String contentType;
    private long fileSize;
    private int recordCount;
    private byte[] data;
    private LocalDateTime timestamp;
    private String message;
    
    public ExportResult() {
        this.timestamp = LocalDateTime.now();
    }
    
    public ExportResult(String fileName, String contentType, byte[] data, int recordCount) {
        this();
        this.fileName = fileName;
        this.contentType = contentType;
        this.data = data;
        this.recordCount = recordCount;
        this.fileSize = data != null ? data.length : 0;
    }
    
    // Getters and setters
    
    public String getFileName() {
        return fileName;
    }
    
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
    
    public String getContentType() {
        return contentType;
    }
    
    public void setContentType(String contentType) {
        this.contentType = contentType;
    }
    
    public long getFileSize() {
        return fileSize;
    }
    
    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }
    
    public int getRecordCount() {
        return recordCount;
    }
    
    public void setRecordCount(int recordCount) {
        this.recordCount = recordCount;
    }
    
    public byte[] getData() {
        return data;
    }
    
    public void setData(byte[] data) {
        this.data = data;
        this.fileSize = data != null ? data.length : 0;
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
     * Builder for creating ExportResult instances.
     */
    public static class Builder {
        private final ExportResult result;
        
        public Builder() {
            this.result = new ExportResult();
        }
        
        public Builder fileName(String fileName) {
            result.fileName = fileName;
            return this;
        }
        
        public Builder contentType(String contentType) {
            result.contentType = contentType;
            return this;
        }
        
        public Builder fileSize(long fileSize) {
            result.fileSize = fileSize;
            return this;
        }
        
        public Builder recordCount(int recordCount) {
            result.recordCount = recordCount;
            return this;
        }
        
        public Builder data(byte[] data) {
            result.data = data;
            result.fileSize = data != null ? data.length : 0;
            return this;
        }
        
        public Builder timestamp(LocalDateTime timestamp) {
            result.timestamp = timestamp;
            return this;
        }
        
        public Builder message(String message) {
            result.message = message;
            return this;
        }
        
        public ExportResult build() {
            return result;
        }
    }
    
    public static Builder builder() {
        return new Builder();
    }
}
