package com.company.assetmanagement.util;

import com.company.assetmanagement.dto.NotificationDTO;
import com.company.assetmanagement.model.Notification;
import com.company.assetmanagement.model.Ticket;

/**
 * Utility class for mapping between Notification entity and NotificationDTO.
 * Provides static methods for entity-to-DTO conversions.
 */
public class NotificationMapper {
    
    private NotificationMapper() {
        // Private constructor to prevent instantiation
    }
    
    /**
     * Maps a Notification entity to a NotificationDTO.
     * 
     * @param notification the notification entity to map
     * @return the mapped NotificationDTO, or null if input is null
     */
    public static NotificationDTO toDTO(Notification notification) {
        if (notification == null) {
            return null;
        }
        
        NotificationDTO dto = new NotificationDTO();
        dto.setId(notification.getId());
        dto.setUserId(notification.getUserId());
        dto.setTicketId(notification.getTicketId());
        dto.setNotificationType(notification.getNotificationType());
        dto.setMessage(notification.getMessage());
        dto.setIsRead(notification.getIsRead());
        dto.setCreatedAt(notification.getCreatedAt());
        
        return dto;
    }
    
    /**
     * Maps a Notification entity to a NotificationDTO with ticket details.
     * 
     * @param notification the notification entity to map
     * @param ticket the associated ticket entity
     * @return the mapped NotificationDTO with ticket details, or null if notification is null
     */
    public static NotificationDTO toDTO(Notification notification, Ticket ticket) {
        if (notification == null) {
            return null;
        }
        
        NotificationDTO dto = toDTO(notification);
        
        if (ticket != null) {
            dto.setTicketNumber(ticket.getTicketNumber());
            dto.setAssetName(ticket.getAssetName());
        }
        
        return dto;
    }
    
    /**
     * Maps a Notification entity to a NotificationDTO with ticket details.
     * 
     * @param notification the notification entity to map
     * @param ticketNumber the ticket number
     * @param assetName the asset name
     * @return the mapped NotificationDTO with ticket details, or null if notification is null
     */
    public static NotificationDTO toDTO(Notification notification, String ticketNumber, String assetName) {
        if (notification == null) {
            return null;
        }
        
        NotificationDTO dto = toDTO(notification);
        dto.setTicketNumber(ticketNumber);
        dto.setAssetName(assetName);
        
        return dto;
    }
}
