package com.company.assetmanagement.exception;

/**
 * Exception thrown when attempting to create an asset with a serial number
 * that already exists in the system.
 */
public class DuplicateSerialNumberException extends RuntimeException {
    
    private final String serialNumber;
    
    public DuplicateSerialNumberException(String serialNumber) {
        super("Asset with serial number '" + serialNumber + "' already exists");
        this.serialNumber = serialNumber;
    }
    
    public String getSerialNumber() {
        return serialNumber;
    }
}
