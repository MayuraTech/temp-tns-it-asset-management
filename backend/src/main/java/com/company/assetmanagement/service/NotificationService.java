package com.company.assetmanagement.service;

import com.company.assetmanagement.dto.NotificationDTO;
import com.company.assetmanagement.model.Ticket;
import com.company.assetmanagement.model.TicketStatus;

import java.util.List;
import java.util.UUID;

/**
 * Service interface for managing notifications.
 * Handles creation, retrieval, and management of user notifications for ticket status changes.
 * 
 * Notifications inform users about important ticket events:
 * - TICKET_APPROVED: Ticket has been approved by an approver
 * - TICKET_REJECTED: Ticket has been rejected with a reason
 * - TICKET_COMPLETED: Ticket has been completed and asset action executed
 * - TICKET_CANCELLED: Ticket has been cancelled
 * 
 * Validates Requirements: 9.1-9.7
 */
public interface NotificationService {
    
    /**
     * Create a notification for a ticket status change.
     * Generates appropriate notification message based on the new status.
     * Validates Requirements 9.1-9.4: Create notifications for status changes.
     *
     * @param ticket the ticket that changed status
     * @param newStatus the new status of the ticket
     * @return the created notification DTO
     */
    NotificationDTO createNotification(Ticket ticket, TicketStatus newStatus);
    
    /**
     * Get all notifications for a specific user.
     * Returns notifications ordered by creation date (newest first).
     * Validates Requirement 9.6: Retrieve all notifications for authenticated user.
     *
     * @param userId the user identifier
     * @return list of notification DTOs for the user
     */
    List<NotificationDTO> getNotifications(UUID userId);
    
    /**
     * Get notifications for a specific user filtered by read status.
     * Validates Requirement 9.7: Filter notifications by read status.
     *
     * @param userId the user identifier
     * @param isRead the read status filter (true for read, false for unread, null for all)
     * @return list of notification DTOs matching the criteria
     */
    List<NotificationDTO> getNotifications(UUID userId, Boolean isRead);
    
    /**
     * Mark a notification as read.
     * Validates authorization to ensure users can only mark their own notifications as read.
     * Validates Requirement 9.5: Allow users to mark notifications as read.
     *
     * @param notificationId the notification identifier
     * @param userId the user identifier (for authorization)
     * @return the updated notification DTO
     * @throws com.company.assetmanagement.exception.ResourceNotFoundException if notification not found
     * @throws com.company.assetmanagement.exception.InsufficientPermissionsException if user doesn't own the notification
     */
    NotificationDTO markAsRead(UUID notificationId, UUID userId);
    
    /**
     * Mark all notifications as read for a specific user.
     * Validates Requirement 9.5: Allow users to mark all notifications as read.
     *
     * @param userId the user identifier
     * @return count of notifications marked as read
     */
    int markAllAsRead(UUID userId);
    
    /**
     * Get the count of unread notifications for a specific user.
     * Used for notification badge counts in the UI.
     * Validates Requirement 9.7: Count unread notifications.
     *
     * @param userId the user identifier
     * @return count of unread notifications
     */
    long getUnreadCount(UUID userId);
}
