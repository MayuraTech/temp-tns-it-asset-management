package com.company.assetmanagement.exception;

/**
 * Exception thrown when an asset allocation or de-allocation operation fails.
 * This exception is used to indicate that the integration with the Allocation Management
 * module encountered an error during execution.
 * 
 * Requirements: 4.5, 14.3
 */
public class AllocationFailedException extends RuntimeException {
    
    private final String ticketId;
    private final String assetId;
    private final String operationType;
    
    public AllocationFailedException(String message) {
        super(message);
        this.ticketId = null;
        this.assetId = null;
        this.operationType = null;
    }
    
    public AllocationFailedException(String message, Throwable cause) {
        super(message, cause);
        this.ticketId = null;
        this.assetId = null;
        this.operationType = null;
    }
    
    public AllocationFailedException(String ticketId, String assetId, String operationType, String message) {
        super("Failed to " + operationType + " asset " + assetId + " for ticket " + ticketId + ": " + message);
        this.ticketId = ticketId;
        this.assetId = assetId;
        this.operationType = operationType;
    }
    
    public AllocationFailedException(String ticketId, String assetId, String operationType, String message, Throwable cause) {
        super("Failed to " + operationType + " asset " + assetId + " for ticket " + ticketId + ": " + message, cause);
        this.ticketId = ticketId;
        this.assetId = assetId;
        this.operationType = operationType;
    }
    
    public String getTicketId() {
        return ticketId;
    }
    
    public String getAssetId() {
        return assetId;
    }
    
    public String getOperationType() {
        return operationType;
    }
}
