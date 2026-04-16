package com.company.assetmanagement.service;

import com.company.assetmanagement.dto.NotificationDTO;
import com.company.assetmanagement.exception.InsufficientPermissionsException;
import com.company.assetmanagement.exception.ResourceNotFoundException;
import com.company.assetmanagement.model.Notification;
import com.company.assetmanagement.model.NotificationType;
import com.company.assetmanagement.model.Ticket;
import com.company.assetmanagement.model.TicketStatus;
import com.company.assetmanagement.repository.NotificationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Implementation of NotificationService.
 * Manages creation, retrieval, and management of user notifications for ticket status changes.
 * 
 * Notifications inform users about important ticket events:
 * - TICKET_APPROVED: Ticket has been approved by an approver
 * - TICKET_REJECTED: Ticket has been rejected with a reason
 * - TICKET_COMPLETED: Ticket has been completed and asset action executed
 * - TICKET_CANCELLED: Ticket has been cancelled
 * 
 * Validates Requirements: 9.1-9.7
 */
@Service
@Transactional
public class NotificationServiceImpl implements NotificationService {
    
    private static final Logger logger = LoggerFactory.getLogger(NotificationServiceImpl.class);
    
    private final NotificationRepository notificationRepository;
    
    public NotificationServiceImpl(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }
    
    /**
     * Create a notification for a ticket status change.
     * Generates appropriate notification message based on the new status.
     * Validates Requirements 9.1-9.4: Create notifications for status changes.
     *
     * @param ticket the ticket that changed status
     * @param newStatus the new status of the ticket
     * @return the created notification DTO
     */
    @Override
    public NotificationDTO createNotification(Ticket ticket, TicketStatus newStatus) {
        logger.debug("Creating notification for ticket {} with status {}", ticket.getTicketNumber(), newStatus);
        
        // Determine notification type from status
        NotificationType notificationType = NotificationType.fromTicketStatus(newStatus);
        
        // Only create notifications for specific status changes
        if (notificationType == null) {
            logger.debug("No notification type for status {}, skipping notification creation", newStatus);
            return null;
        }
        
        // Generate notification message
        String message = generateNotificationMessage(ticket, newStatus);
        
        // Create notification entity
        Notification notification = new Notification();
        notification.setUserId(ticket.getRequesterId());
        notification.setTicketId(ticket.getId());
        notification.setNotificationType(notificationType);
        notification.setMessage(message);
        notification.setIsRead(false);
        
        // Save notification
        Notification savedNotification = notificationRepository.save(notification);
        
        logger.info("Created notification {} for user {} regarding ticket {}", 
                   savedNotification.getId(), ticket.getRequesterId(), ticket.getTicketNumber());
        
        // Convert to DTO and return
        return mapToDTO(savedNotification, ticket);
    }
    
    /**
     * Get all notifications for a specific user.
     * Returns notifications ordered by creation date (newest first).
     * Validates Requirement 9.6: Retrieve all notifications for authenticated user.
     *
     * @param userId the user identifier
     * @return list of notification DTOs for the user
     */
    @Override
    @Transactional(readOnly = true)
    public List<NotificationDTO> getNotifications(UUID userId) {
        logger.debug("Retrieving all notifications for user {}", userId);
        
        List<Notification> notifications = notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
        
        logger.debug("Found {} notifications for user {}", notifications.size(), userId);
        
        return notifications.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Get notifications for a specific user filtered by read status.
     * Validates Requirement 9.7: Filter notifications by read status.
     *
     * @param userId the user identifier
     * @param isRead the read status filter (true for read, false for unread, null for all)
     * @return list of notification DTOs matching the criteria
     */
    @Override
    @Transactional(readOnly = true)
    public List<NotificationDTO> getNotifications(UUID userId, Boolean isRead) {
        logger.debug("Retrieving notifications for user {} with isRead={}", userId, isRead);
        
        List<Notification> notifications;
        
        if (isRead == null) {
            // Return all notifications
            notifications = notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
        } else {
            // Filter by read status
            notifications = notificationRepository.findByUserIdAndIsReadOrderByCreatedAtDesc(userId, isRead);
        }
        
        logger.debug("Found {} notifications for user {} with isRead={}", notifications.size(), userId, isRead);
        
        return notifications.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Mark a notification as read.
     * Validates authorization to ensure users can only mark their own notifications as read.
     * Validates Requirement 9.5: Allow users to mark notifications as read.
     *
     * @param notificationId the notification identifier
     * @param userId the user identifier (for authorization)
     * @return the updated notification DTO
     * @throws ResourceNotFoundException if notification not found
     * @throws InsufficientPermissionsException if user doesn't own the notification
     */
    @Override
    public NotificationDTO markAsRead(UUID notificationId, UUID userId) {
        logger.debug("Marking notification {} as read for user {}", notificationId, userId);
        
        // Retrieve notification
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification", notificationId.toString()));
        
        // Authorization check: ensure user owns the notification
        if (!notification.getUserId().equals(userId)) {
            logger.warn("User {} attempted to mark notification {} owned by user {} as read", 
                       userId, notificationId, notification.getUserId());
            throw new InsufficientPermissionsException(userId.toString(), "mark notification as read");
        }
        
        // Mark as read
        notification.markAsRead();
        Notification updatedNotification = notificationRepository.save(notification);
        
        logger.info("Marked notification {} as read for user {}", notificationId, userId);
        
        return mapToDTO(updatedNotification);
    }
    
    /**
     * Mark all notifications as read for a specific user.
     * Validates Requirement 9.5: Allow users to mark all notifications as read.
     *
     * @param userId the user identifier
     * @return count of notifications marked as read
     */
    @Override
    public int markAllAsRead(UUID userId) {
        logger.debug("Marking all notifications as read for user {}", userId);
        
        // Get all unread notifications for the user
        List<Notification> unreadNotifications = notificationRepository.findUnreadByUserId(userId);
        
        // Mark each as read
        unreadNotifications.forEach(Notification::markAsRead);
        
        // Save all
        notificationRepository.saveAll(unreadNotifications);
        
        int count = unreadNotifications.size();
        logger.info("Marked {} notifications as read for user {}", count, userId);
        
        return count;
    }
    
    /**
     * Get the count of unread notifications for a specific user.
     * Used for notification badge counts in the UI.
     * Validates Requirement 9.7: Count unread notifications.
     *
     * @param userId the user identifier
     * @return count of unread notifications
     */
    @Override
    @Transactional(readOnly = true)
    public long getUnreadCount(UUID userId) {
        logger.debug("Counting unread notifications for user {}", userId);
        
        long count = notificationRepository.countUnreadByUserId(userId);
        
        logger.debug("User {} has {} unread notifications", userId, count);
        
        return count;
    }
    
    /**
     * Generate a notification message based on ticket and status.
     * Includes ticket details and appropriate messaging for each status.
     *
     * @param ticket the ticket
     * @param status the new status
     * @return the notification message
     */
    private String generateNotificationMessage(Ticket ticket, TicketStatus status) {
        String ticketNumber = ticket.getTicketNumber();
        String assetName = ticket.getAssetName();
        
        switch (status) {
            case APPROVED:
                return String.format("Your ticket %s for asset '%s' has been approved. " +
                                   "The request will be processed shortly.", 
                                   ticketNumber, assetName);
            
            case REJECTED:
                String rejectionReason = ticket.getRejectionReason();
                if (rejectionReason != null && !rejectionReason.isBlank()) {
                    return String.format("Your ticket %s for asset '%s' has been rejected. " +
                                       "Reason: %s", 
                                       ticketNumber, assetName, rejectionReason);
                } else {
                    return String.format("Your ticket %s for asset '%s' has been rejected.", 
                                       ticketNumber, assetName);
                }
            
            case COMPLETED:
                return String.format("Your ticket %s for asset '%s' has been completed. " +
                                   "The asset action has been executed successfully.", 
                                   ticketNumber, assetName);
            
            case CANCELLED:
                return String.format("Your ticket %s for asset '%s' has been cancelled.", 
                                   ticketNumber, assetName);
            
            default:
                return String.format("Your ticket %s for asset '%s' status has been updated to %s.", 
                                   ticketNumber, assetName, status);
        }
    }
    
    /**
     * Map Notification entity to NotificationDTO.
     *
     * @param notification the notification entity
     * @return the notification DTO
     */
    private NotificationDTO mapToDTO(Notification notification) {
        NotificationDTO dto = new NotificationDTO();
        dto.setId(notification.getId());
        dto.setUserId(notification.getUserId());
        dto.setTicketId(notification.getTicketId());
        dto.setNotificationType(notification.getNotificationType());
        dto.setMessage(notification.getMessage());
        dto.setIsRead(notification.getIsRead());
        dto.setCreatedAt(notification.getCreatedAt());
        
        // If ticket relationship is loaded, include ticket details
        if (notification.getTicket() != null) {
            dto.setTicketNumber(notification.getTicket().getTicketNumber());
            dto.setAssetName(notification.getTicket().getAssetName());
        }
        
        return dto;
    }
    
    /**
     * Map Notification entity to NotificationDTO with ticket details.
     *
     * @param notification the notification entity
     * @param ticket the ticket entity
     * @return the notification DTO
     */
    private NotificationDTO mapToDTO(Notification notification, Ticket ticket) {
        NotificationDTO dto = mapToDTO(notification);
        dto.setTicketNumber(ticket.getTicketNumber());
        dto.setAssetName(ticket.getAssetName());
        return dto;
    }
}
