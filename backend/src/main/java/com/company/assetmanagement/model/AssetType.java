package com.company.assetmanagement.model;

/**
 * Enumeration of asset types supported by the system.
 * Defines the 15 standard asset types for IT infrastructure management.
 */
public enum AssetType {
    SERVER("server"),
    WORKSTATION("workstation"),
    NETWORK_DEVICE("network_device"),
    STORAGE_DEVICE("storage_device"),
    SOFTWARE_LICENSE("software_license"),
    PERIPHERAL("peripheral"),
    KEYBOARD("keyboard"),
    MOUSE("mouse"),
    LAPTOP("laptop"),
    MONITOR("monitor"),
    HEADSET("headset"),
    LAPTOP_CHARGER("laptop_charger"),
    HDMI_CABLE("hdmi_cable"),
    NETWORK_CABLE("network_cable"),
    ACCESS_CARD("access_card");
    
    private final String value;
    
    AssetType(String value) {
        this.value = value;
    }
    
    public String getValue() {
        return value;
    }
    
    /**
     * Get AssetType from string value.
     * 
     * @param value the string value
     * @return the corresponding AssetType
     * @throws IllegalArgumentException if value doesn't match any AssetType
     */
    public static AssetType fromValue(String value) {
        for (AssetType type : AssetType.values()) {
            if (type.value.equals(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown asset type: " + value);
    }
}
