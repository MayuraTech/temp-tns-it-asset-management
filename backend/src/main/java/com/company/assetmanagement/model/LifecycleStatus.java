package com.company.assetmanagement.model;

/**
 * Enumeration of asset lifecycle statuses.
 * Defines the 7 standard lifecycle stages from acquisition to retirement.
 */
public enum LifecycleStatus {
    ORDERED("ordered"),
    RECEIVED("received"),
    DEPLOYED("deployed"),
    IN_USE("in_use"),
    MAINTENANCE("maintenance"),
    STORAGE("storage"),
    RETIRED("retired");
    
    private final String value;
    
    LifecycleStatus(String value) {
        this.value = value;
    }
    
    public String getValue() {
        return value;
    }
    
    /**
     * Get LifecycleStatus from string value.
     * 
     * @param value the string value
     * @return the corresponding LifecycleStatus
     * @throws IllegalArgumentException if value doesn't match any LifecycleStatus
     */
    public static LifecycleStatus fromValue(String value) {
        for (LifecycleStatus status : LifecycleStatus.values()) {
            if (status.value.equals(value)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown lifecycle status: " + value);
    }
    
    /**
     * Validates if a transition from the current status to a new status is allowed.
     * 
     * Valid transitions:
     * - ORDERED → RECEIVED
     * - RECEIVED → DEPLOYED
     * - DEPLOYED → IN_USE or STORAGE
     * - IN_USE → STORAGE or RETIRED
     * - STORAGE → DEPLOYED or RETIRED
     * - Any status → MAINTENANCE
     * - MAINTENANCE → Any status (except RETIRED)
     * - RETIRED → No transitions allowed
     * 
     * @param newStatus the target status to transition to
     * @return true if the transition is valid, false otherwise
     */
    public boolean canTransitionTo(LifecycleStatus newStatus) {
        if (this == RETIRED) {
            return false; // No transitions from RETIRED
        }
        
        if (newStatus == MAINTENANCE) {
            return true; // Can always go to MAINTENANCE
        }
        
        switch (this) {
            case ORDERED:
                return newStatus == RECEIVED;
            case RECEIVED:
                return newStatus == DEPLOYED;
            case DEPLOYED:
                return newStatus == IN_USE || newStatus == STORAGE;
            case IN_USE:
                return newStatus == STORAGE || newStatus == RETIRED;
            case MAINTENANCE:
                return newStatus != RETIRED; // Can return to any status except RETIRED
            case STORAGE:
                return newStatus == DEPLOYED || newStatus == RETIRED;
            default:
                return false;
        }
    }
}
