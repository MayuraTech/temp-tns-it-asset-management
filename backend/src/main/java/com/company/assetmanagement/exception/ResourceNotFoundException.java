package com.company.assetmanagement.exception;

/**
 * Exception thrown when a requested resource (asset, user, ticket, etc.)
 * cannot be found in the system.
 */
public class ResourceNotFoundException extends RuntimeException {
    
    private final String resourceType;
    private final String resourceId;
    
    public ResourceNotFoundException(String resourceType, String resourceId) {
        super(resourceType + " with ID '" + resourceId + "' not found");
        this.resourceType = resourceType;
        this.resourceId = resourceId;
    }
    
    public String getResourceType() {
        return resourceType;
    }
    
    public String getResourceId() {
        return resourceId;
    }
}
