package com.company.assetmanagement.dto;

/**
 * Enumeration of supported import formats for asset data.
 * Used in import operations to specify the format of the input file.
 * 
 * @see ImportResult
 */
public enum ImportFormat {
    /**
     * Comma-Separated Values format.
     * Expected Content-Type: text/csv
     */
    CSV("text/csv", ".csv"),
    
    /**
     * JavaScript Object Notation format.
     * Expected Content-Type: application/json
     */
    JSON("application/json", ".json");
    
    private final String contentType;
    private final String fileExtension;
    
    ImportFormat(String contentType, String fileExtension) {
        this.contentType = contentType;
        this.fileExtension = fileExtension;
    }
    
    /**
     * Gets the expected MIME content type for this import format.
     * 
     * @return the content type (e.g., "text/csv", "application/json")
     */
    public String getContentType() {
        return contentType;
    }
    
    /**
     * Gets the expected file extension for this import format.
     * 
     * @return the file extension including the dot (e.g., ".csv", ".json")
     */
    public String getFileExtension() {
        return fileExtension;
    }
}
