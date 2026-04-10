package com.company.assetmanagement.model;

/**
 * Enumeration of ticket types in the system.
 * Defines the types of asset allocation/de-allocation requests.
 */
public enum TicketType {
    ALLOCATION("allocation"),
    DEALLOCATION("deallocation");
    
    private final String value;
    
    TicketType(String value) {
        this.value = value;
    }
    
    public String getValue() {
        return value;
    }
    
    /**
     * Get TicketType from string value.
     * 
     * @param value the string value
     * @return the corresponding TicketType
     * @throws IllegalArgumentException if value doesn't match any TicketType
     */
    public static TicketType fromValue(String value) {
        for (TicketType type : TicketType.values()) {
            if (type.value.equals(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown ticket type: " + value);
    }
}
