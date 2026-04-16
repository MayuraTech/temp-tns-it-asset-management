package com.company.assetmanagement.model;

/**
 * Enumeration of assignment types for asset allocation.
 * Defines whether an asset is assigned to a user or a location.
 * 
 * <p>This enum is used in the AssignmentHistory entity to distinguish between:
 * <ul>
 *   <li>USER assignments - when an asset is assigned to a specific user</li>
 *   <li>LOCATION assignments - when an asset is assigned to a physical or logical location</li>
 * </ul>
 * 
 * @see com.company.assetmanagement.model.AssignmentHistory
 */
public enum AssignmentType {
    /**
     * Indicates the asset is assigned to a user.
     * The AssignedTo field will contain the user's name.
     */
    USER("user"),
    
    /**
     * Indicates the asset is assigned to a location.
     * The AssignedTo field will contain the location name.
     */
    LOCATION("location");
    
    private final String value;
    
    AssignmentType(String value) {
        this.value = value;
    }
    
    /**
     * Gets the string value representation of this assignment type.
     * 
     * @return the string value (lowercase)
     */
    public String getValue() {
        return value;
    }
    
    /**
     * Get AssignmentType from string value.
     * 
     * @param value the string value (case-insensitive)
     * @return the corresponding AssignmentType
     * @throws IllegalArgumentException if value doesn't match any AssignmentType
     */
    public static AssignmentType fromValue(String value) {
        for (AssignmentType type : AssignmentType.values()) {
            if (type.value.equalsIgnoreCase(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown assignment type: " + value);
    }
}
