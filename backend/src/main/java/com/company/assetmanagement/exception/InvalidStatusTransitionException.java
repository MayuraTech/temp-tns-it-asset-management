package com.company.assetmanagement.exception;

/**
 * Exception thrown when attempting an invalid lifecycle status transition
 * or ticket status transition that violates workflow rules.
 */
public class InvalidStatusTransitionException extends RuntimeException {
    
    private final String fromStatus;
    private final String toStatus;
    private final String resourceType;
    
    public InvalidStatusTransitionException(String fromStatus, String toStatus) {
        this(fromStatus, toStatus, "Resource");
    }
    
    public InvalidStatusTransitionException(String fromStatus, String toStatus, String resourceType) {
        super("Invalid status transition for " + resourceType + " from '" + fromStatus + "' to '" + toStatus + "'");
        this.fromStatus = fromStatus;
        this.toStatus = toStatus;
        this.resourceType = resourceType;
    }
    
    public String getFromStatus() {
        return fromStatus;
    }
    
    public String getToStatus() {
        return toStatus;
    }
    
    public String getResourceType() {
        return resourceType;
    }
}
