package com.company.assetmanagement.exception;

/**
 * Exception thrown when a user attempts to perform an action
 * they do not have permission to execute.
 */
public class InsufficientPermissionsException extends RuntimeException {
    
    private final String userId;
    private final String action;
    
    public InsufficientPermissionsException() {
        super("You do not have permission to perform this action");
        this.userId = null;
        this.action = null;
    }
    
    public InsufficientPermissionsException(String userId, String action) {
        super("User '" + userId + "' does not have permission to perform action: " + action);
        this.userId = userId;
        this.action = action;
    }
    
    public String getUserId() {
        return userId;
    }
    
    public String getAction() {
        return action;
    }
}
