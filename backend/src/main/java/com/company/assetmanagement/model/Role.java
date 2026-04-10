package com.company.assetmanagement.model;

/**
 * Enumeration of user roles in the system.
 * Defines the 3 standard roles with different permission levels.
 */
public enum Role {
    ADMINISTRATOR("Administrator"),
    ASSET_MANAGER("Asset_Manager"),
    VIEWER("Viewer");
    
    private final String value;
    
    Role(String value) {
        this.value = value;
    }
    
    public String getValue() {
        return value;
    }
    
    /**
     * Get Role from string value.
     * 
     * @param value the string value
     * @return the corresponding Role
     * @throws IllegalArgumentException if value doesn't match any Role
     */
    public static Role fromValue(String value) {
        for (Role role : Role.values()) {
            if (role.value.equals(value)) {
                return role;
            }
        }
        throw new IllegalArgumentException("Unknown role: " + value);
    }
}
