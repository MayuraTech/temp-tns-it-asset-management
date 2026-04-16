package com.company.assetmanagement.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for Notification entity.
 */
@DisplayName("Notification Entity Tests")
class NotificationTest {
    
    @Test
    @DisplayName("Should create notification with default values")
    void shouldCreateNotificationWithDefaults() {
        // When
        Notification notification = new Notification();
        
        // Then
        assertThat(notification.getIsRead()).isFalse();
        assertThat(notification.getCreatedAt()).isNotNull();
        assertThat(notification.getCreatedAt()).isBeforeOrEqualTo(LocalDateTime.now());
    }
    
    @Test
    @DisplayName("Should set and get all fields correctly")
    void shouldSetAndGetAllFields() {
        // Given
        UUID id = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID ticketId = UUID.randomUUID();
        NotificationType type = NotificationType.TICKET_APPROVED;
        String message = "Your ticket has been approved";
        LocalDateTime createdAt = LocalDateTime.now();
        
        // When
        Notification notification = new Notification();
        notification.setId(id);
        notification.setUserId(userId);
        notification.setTicketId(ticketId);
        notification.setNotificationType(type);
        notification.setMessage(message);
        notification.setIsRead(true);
        notification.setCreatedAt(createdAt);
        
        // Then
        assertThat(notification.getId()).isEqualTo(id);
        assertThat(notification.getUserId()).isEqualTo(userId);
        assertThat(notification.getTicketId()).isEqualTo(ticketId);
        assertThat(notification.getNotificationType()).isEqualTo(type);
        assertThat(notification.getMessage()).isEqualTo(message);
        assertThat(notification.getIsRead()).isTrue();
        assertThat(notification.getCreatedAt()).isEqualTo(createdAt);
    }
    
    @Test
    @DisplayName("Should mark notification as read")
    void shouldMarkAsRead() {
        // Given
        Notification notification = new Notification();
        assertThat(notification.getIsRead()).isFalse();
        
        // When
        notification.markAsRead();
        
        // Then
        assertThat(notification.getIsRead()).isTrue();
    }
    
    @Test
    @DisplayName("Should mark notification as unread")
    void shouldMarkAsUnread() {
        // Given
        Notification notification = new Notification();
        notification.setIsRead(true);
        
        // When
        notification.markAsUnread();
        
        // Then
        assertThat(notification.getIsRead()).isFalse();
    }
    
    @Test
    @DisplayName("Should check if notification is unread")
    void shouldCheckIfUnread() {
        // Given
        Notification notification = new Notification();
        
        // Then
        assertThat(notification.isUnread()).isTrue();
        
        // When
        notification.markAsRead();
        
        // Then
        assertThat(notification.isUnread()).isFalse();
    }
    
    @Test
    @DisplayName("Should implement equals correctly")
    void shouldImplementEquals() {
        // Given
        UUID id = UUID.randomUUID();
        
        Notification notification1 = new Notification();
        notification1.setId(id);
        
        Notification notification2 = new Notification();
        notification2.setId(id);
        
        Notification notification3 = new Notification();
        notification3.setId(UUID.randomUUID());
        
        // Then
        assertThat(notification1).isEqualTo(notification2);
        assertThat(notification1).isNotEqualTo(notification3);
        assertThat(notification1).isEqualTo(notification1);
    }
    
    @Test
    @DisplayName("Should implement hashCode correctly")
    void shouldImplementHashCode() {
        // Given
        UUID id = UUID.randomUUID();
        
        Notification notification1 = new Notification();
        notification1.setId(id);
        
        Notification notification2 = new Notification();
        notification2.setId(id);
        
        // Then
        assertThat(notification1.hashCode()).isEqualTo(notification2.hashCode());
    }
    
    @Test
    @DisplayName("Should generate toString with key fields")
    void shouldGenerateToString() {
        // Given
        UUID userId = UUID.randomUUID();
        UUID ticketId = UUID.randomUUID();
        
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setTicketId(ticketId);
        notification.setNotificationType(NotificationType.TICKET_APPROVED);
        
        // When
        String toString = notification.toString();
        
        // Then
        assertThat(toString).contains("Notification");
        assertThat(toString).contains(userId.toString());
        assertThat(toString).contains(ticketId.toString());
        assertThat(toString).contains("TICKET_APPROVED");
    }
}
