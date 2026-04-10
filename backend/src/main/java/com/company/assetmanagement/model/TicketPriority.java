package com.company.assetmanagement.model;

/**
 * Enumeration of ticket priority levels.
 * Defines the 4 standard priority levels for asset allocation/de-allocation tickets.
 */
public enum TicketPriority {
    LOW("low"),
    MEDIUM("medium"),
    HIGH("high"),
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
     * 
     * @param value the string value
     * @return the corresponding TicketPriority
     * @throws IllegalArgumentException if value doesn't match any TicketPriority
     */
    public static TicketPriority fromValue(String value) {
        for (TicketPriority priority : TicketPriority.values()) {
            if (priority.value.equals(value)) {
                return priority;
            }
        }
        throw new IllegalArgumentException("Unknown ticket priority: " + value);
    }
    
    /**
     * Get the numeric level of this priority (higher number = higher priority).
     * 
     * @return numeric priority level (1-4)
     */
    public int getLevel() {
        switch (this) {
            case LOW:
                return 1;
            case MEDIUM:
                return 2;
            case HIGH:
                return 3;
            case URGENT:
                return 4;
            default:
                return 0;
        }
    }
}
