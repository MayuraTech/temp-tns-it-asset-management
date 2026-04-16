package com.company.assetmanagement.repository;

import com.company.assetmanagement.model.TicketStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repository for TicketStatusHistory entities.
 * Provides query methods for retrieving ticket status change history.
 * 
 * Status history entries are immutable and cannot be modified or deleted.
 * They provide a complete audit trail of all ticket status transitions.
 * 
 * Validates Requirements: 6.1, 6.3
 */
@Repository
public interface TicketStatusHistoryRepository extends JpaRepository<TicketStatusHistory, UUID> {
    
    /**
     * Find all status history entries for a specific ticket, ordered by timestamp.
     * Returns entries in chronological order (oldest first) to show the progression
     * of status changes over time.
     * 
     * Validates Requirement 6.3: Returns all status history entries in chronological order.
     *
     * @param ticketId the ticket identifier
     * @return list of status history entries ordered by changedAt timestamp ascending
     */
    List<TicketStatusHistory> findByTicketIdOrderByChangedAtAsc(UUID ticketId);
    
    /**
     * Find all status history entries for a specific ticket, ordered by timestamp descending.
     * Returns entries in reverse chronological order (newest first) to show the most
     * recent status changes first.
     *
     * @param ticketId the ticket identifier
     * @return list of status history entries ordered by changedAt timestamp descending
     */
    List<TicketStatusHistory> findByTicketIdOrderByChangedAtDesc(UUID ticketId);
    
    /**
     * Count the number of status changes for a specific ticket.
     * Used for analytics and metrics.
     *
     * @param ticketId the ticket identifier
     * @return count of status history entries for the ticket
     */
    long countByTicketId(UUID ticketId);
    
    /**
     * Find all status history entries made by a specific user.
     * Used for user activity tracking and audit purposes.
     *
     * @param changedBy the user identifier who made the status change
     * @return list of status history entries made by the user
     */
    List<TicketStatusHistory> findByChangedByOrderByChangedAtDesc(UUID changedBy);
}
