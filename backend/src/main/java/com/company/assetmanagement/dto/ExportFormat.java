package com.company.assetmanagement.dto;

/**
 * Enumeration of supported export formats for asset data.
 * Used in export operations to specify the desired output format.
 * 
 * @see ExportResult
 */
public enum ExportFormat {
    /**
     * Comma-Separated Values format.
     * Content-Type: text/csv
     */
    CSV("text/csv", ".csv"),
    
    /**
     * JavaScript Object Notation format.
     * Content-Type: application/json
     */
    JSON("application/json", ".json");
    
    private final String contentType;
    private final String fileExtension;
    
    ExportFormat(String contentType, String fileExtension) {
        this.contentType = contentType;
        this.fileExtension = fileExtension;
    }
    
    /**
     * Gets the MIME content type for this export format.
     * 
     * @return the content type (e.g., "text/csv", "application/json")
     */
    public String getContentType() {
        return contentType;
    }
    
    /**
     * Gets the file extension for this export format.
     * 
     * @return the file extension including the dot (e.g., ".csv", ".json")
     */
    public String getFileExtension() {
        return fileExtension;
    }
}
