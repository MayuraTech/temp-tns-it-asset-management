package com.company.assetmanagement.repository;

import com.company.assetmanagement.model.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repository for Notification entities.
 * Provides query methods for managing user notifications related to ticket status changes.
 * 
 * Notifications inform users about important events:
 * - TICKET_APPROVED: Ticket has been approved
 * - TICKET_REJECTED: Ticket has been rejected with reason
 * - TICKET_COMPLETED: Ticket has been completed
 * - TICKET_CANCELLED: Ticket has been cancelled
 * 
 * Validates Requirements: 9.6, 9.7
 */
@Repository
public interface NotificationRepository extends JpaRepository<Notification, UUID> {
    
    /**
     * Find all notifications for a specific user.
     * Returns notifications ordered by creation date (newest first).
     * Validates Requirement 9.6: Retrieve all notifications for authenticated user.
     *
     * @param userId the user identifier
     * @return list of notifications for the user, ordered by creation date descending
     */
    List<Notification> findByUserIdOrderByCreatedAtDesc(UUID userId);
    
    /**
     * Find all notifications for a specific user with pagination.
     * Validates Requirement 9.6: Retrieve all notifications for authenticated user.
     *
     * @param userId the user identifier
     * @param pageable pagination information
     * @return page of notifications for the user
     */
    Page<Notification> findByUserId(UUID userId, Pageable pageable);
    
    /**
     * Find notifications by user ID and read status.
     * Used to filter read or unread notifications.
     * Validates Requirement 9.7: Filter notifications by read status.
     *
     * @param userId the user identifier
     * @param isRead the read status (true for read, false for unread)
     * @return list of notifications matching the criteria, ordered by creation date descending
     */
    List<Notification> findByUserIdAndIsReadOrderByCreatedAtDesc(UUID userId, Boolean isRead);
    
    /**
     * Find notifications by user ID and read status with pagination.
     * Validates Requirement 9.7: Filter notifications by read status.
     *
     * @param userId the user identifier
     * @param isRead the read status (true for read, false for unread)
     * @param pageable pagination information
     * @return page of notifications matching the criteria
     */
    Page<Notification> findByUserIdAndIsRead(UUID userId, Boolean isRead, Pageable pageable);
    
    /**
     * Count unread notifications for a specific user.
     * Used for notification badge counts in the UI.
     * Validates Requirement 9.7: Count unread notifications.
     *
     * @param userId the user identifier
     * @return count of unread notifications for the user
     */
    long countByUserIdAndIsRead(UUID userId, Boolean isRead);
    
    /**
     * Find all notifications related to a specific ticket.
     * Used for ticket-specific notification history.
     *
     * @param ticketId the ticket identifier
     * @return list of notifications related to the ticket, ordered by creation date descending
     */
    List<Notification> findByTicketIdOrderByCreatedAtDesc(UUID ticketId);
    
    /**
     * Find unread notifications for a specific user.
     * Convenience method for retrieving only unread notifications.
     *
     * @param userId the user identifier
     * @return list of unread notifications for the user, ordered by creation date descending
     */
    default List<Notification> findUnreadByUserId(UUID userId) {
        return findByUserIdAndIsReadOrderByCreatedAtDesc(userId, false);
    }
    
    /**
     * Count unread notifications for a specific user.
     * Convenience method for notification badge counts.
     *
     * @param userId the user identifier
     * @return count of unread notifications
     */
    default long countUnreadByUserId(UUID userId) {
        return countByUserIdAndIsRead(userId, false);
    }
}
