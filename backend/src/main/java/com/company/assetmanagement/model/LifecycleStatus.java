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
}
