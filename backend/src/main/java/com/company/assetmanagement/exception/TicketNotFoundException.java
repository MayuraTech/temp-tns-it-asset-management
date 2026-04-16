package com.company.assetmanagement.exception;

/**
 * Exception thrown when a requested ticket cannot be found in the system.
 * This is a specialized version of ResourceNotFoundException for ticket-specific operations.
 * 
 * Requirements: 13.9, 14.3
 */
public class TicketNotFoundException extends ResourceNotFoundException {
    
    private final String ticketId;
    
    public TicketNotFoundException(String ticketId) {
        super("Ticket", ticketId);
        this.ticketId = ticketId;
    }
    
    public TicketNotFoundException(String ticketId, String message) {
        super("Ticket", ticketId);
        this.ticketId = ticketId;
    }
    
    public String getTicketId() {
        return ticketId;
    }
}
