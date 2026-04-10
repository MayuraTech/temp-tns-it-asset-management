package com.company.assetmanagement.model;

/**
 * Enumeration of notification types in the system.
 * Defines the types of notifications sent to users for ticket status changes.
 */
public enum NotificationType {
    TICKET_APPROVED("ticket_approved"),
    TICKET_REJECTED("ticket_rejected"),
    TICKET_COMPLETED("ticket_completed"),
    TICKET_CANCELLED("ticket_cancelled");
    
    private final String value;
    
    NotificationType(String value) {
        this.value = value;
    }
    
    public String getValue() {
        return value;
    }
    
    /**
     * Get NotificationType from string value.
     * 
     * @param value the string value
     * @return the corresponding NotificationType
     * @throws IllegalArgumentException if value doesn't match any NotificationType
     */
    public static NotificationType fromValue(String value) {
        for (NotificationType type : NotificationType.values()) {
            if (type.value.equals(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown notification type: " + value);
    }
    
    /**
     * Get the corresponding notification type for a ticket status.
     * 
     * @param status the ticket status
     * @return the corresponding NotificationType, or null if no notification should be sent
     */
    public static NotificationType fromTicketStatus(TicketStatus status) {
        switch (status) {
            case APPROVED:
                return TICKET_APPROVED;
            case REJECTED:
                return TICKET_REJECTED;
            case COMPLETED:
                return TICKET_COMPLETED;
            case CANCELLED:
                return TICKET_CANCELLED;
            default:
                return null;
        }
    }
}
