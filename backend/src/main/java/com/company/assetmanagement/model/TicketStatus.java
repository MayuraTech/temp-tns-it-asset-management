package com.company.assetmanagement.model;

/**
 * Enumeration of ticket statuses in the system.
 * Defines the 7 standard statuses for asset allocation/de-allocation tickets.
 */
public enum TicketStatus {
    PENDING("pending"),
    APPROVED("approved"),
    REJECTED("rejected"),
    IN_PROGRESS("in_progress"),
    COMPLETED("completed"),
    CANCELLED("cancelled");
    
    private final String value;
    
    TicketStatus(String value) {
        this.value = value;
    }
    
    public String getValue() {
        return value;
    }
    
    /**
     * Get TicketStatus from string value.
     * 
     * @param value the string value
     * @return the corresponding TicketStatus
     * @throws IllegalArgumentException if value doesn't match any TicketStatus
     */
    public static TicketStatus fromValue(String value) {
        for (TicketStatus status : TicketStatus.values()) {
            if (status.value.equals(value)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown ticket status: " + value);
    }
    
    /**
     * Check if transition from this status to target status is valid.
     * 
     * @param target the target status
     * @return true if transition is valid, false otherwise
     */
    public boolean canTransitionTo(TicketStatus target) {
        switch (this) {
            case PENDING:
                return target == APPROVED || target == REJECTED || target == CANCELLED;
            case APPROVED:
                return target == IN_PROGRESS || target == CANCELLED;
            case IN_PROGRESS:
                return target == COMPLETED || target == CANCELLED;
            case REJECTED:
            case COMPLETED:
            case CANCELLED:
                return false; // Terminal states
            default:
                return false;
        }
    }
}
