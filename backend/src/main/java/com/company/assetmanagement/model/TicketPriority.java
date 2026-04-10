package com.company.assetmanagement.model;

/**
 * Enumeration of ticket priority levels.
 * Defines the 3 standard priority levels for asset allocation/de-allocation tickets.
 * STANDARD maps to MEDIUM internally for backward compatibility.
 */
public enum TicketPriority {
    LOW("low"),
    STANDARD("standard"),
    URGENT("urgent");
    
    private final String value;
    
    TicketPriority(String value) {
        this.value = value;
    }
    
    public String getValue() {
        return value;
    }
    
    /**
     * Get TicketPriority from string value.
     * Supports both STANDARD and MEDIUM for backward compatibility.
     * 
     * @param value the string value
     * @return the corresponding TicketPriority
     * @throws IllegalArgumentException if value doesn't match any TicketPriority
     */
    public static TicketPriority fromValue(String value) {
        // Map MEDIUM to STANDARD for backward compatibility
        if ("medium".equalsIgnoreCase(value)) {
            return STANDARD;
        }
        
        for (TicketPriority priority : TicketPriority.values()) {
            if (priority.value.equalsIgnoreCase(value)) {
                return priority;
            }
        }
        throw new IllegalArgumentException("Unknown ticket priority: " + value);
    }
    
    /**
     * Get the internal mapping value for database storage.
     * STANDARD maps to MEDIUM for backward compatibility.
     * 
     * @return the internal storage value
     */
    public String getInternalValue() {
        return this == STANDARD ? "medium" : value;
    }
    
    /**
     * Get the numeric level of this priority (higher number = higher priority).
     * 
     * @return numeric priority level (1-3)
     */
    public int getLevel() {
        switch (this) {
            case LOW:
                return 1;
            case STANDARD:
                return 2;
            case URGENT:
                return 3;
            default:
                return 0;
        }
    }
    
    /**
     * Get the display color indicator for this priority.
     * 
     * @return color indicator (gray, yellow, or red)
     */
    public String getColorIndicator() {
        switch (this) {
            case LOW:
                return "gray";
            case STANDARD:
                return "yellow";
            case URGENT:
                return "red";
            default:
                return "gray";
        }
    }
}
