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
        return fromPersistedString(value);
    }

    /**
     * Resolves a role from the database or API string.
     * Accepts {@link #getValue()} (e.g. {@code Administrator}) or {@link #name()} (e.g. {@code ADMINISTRATOR}).
     */
    public static Role fromPersistedString(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        for (Role role : Role.values()) {
            if (role.name().equals(raw) || role.value.equals(raw)) {
                return role;
            }
        }
        throw new IllegalArgumentException("Unknown role: " + raw);
    }
}
