package com.company.assetmanagement.controller;

import com.company.assetmanagement.dto.NotificationDTO;
import com.company.assetmanagement.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST controller for notification management operations.
 * Provides endpoints for retrieving, marking as read, and managing user notifications
 * related to ticket status changes.
 * 
 * Notifications inform users about important ticket events:
 * - TICKET_APPROVED: Ticket has been approved by an approver
 * - TICKET_REJECTED: Ticket has been rejected with a reason
 * - TICKET_COMPLETED: Ticket has been completed and asset action executed
 * - TICKET_CANCELLED: Ticket has been cancelled
 * 
 * All notification operations are restricted to the authenticated user's own notifications.
 * 
 * Validates Requirements: 9.1-9.7
 */
@RestController
@RequestMapping("/api/v1/notifications")
@Tag(name = "Notifications", description = "Notification management endpoints for ticket status change alerts")
public class NotificationController {
    
    private final NotificationService notificationService;
    
    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }
    
    /**
     * Get all notifications for the authenticated user.
     * Returns notifications ordered by creation date (newest first).
     * Optionally filter by read status.
     * 
     * Validates Requirements: 9.6, 9.7
     *
     * @param isRead optional filter for read status (true for read, false for unread, null for all)
     * @param authentication the authenticated user
     * @return list of notification DTOs for the user
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(
        summary = "Get user notifications",
        description = "Retrieve all notifications for the authenticated user. " +
                     "Optionally filter by read status. Returns notifications ordered by creation date (newest first)."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Notifications retrieved successfully",
            content = @Content(schema = @Schema(implementation = List.class))
        ),
        @ApiResponse(responseCode = "401", description = "Not authenticated")
    })
    public ResponseEntity<List<NotificationDTO>> getNotifications(
            @Parameter(description = "Filter by read status (true for read, false for unread, omit for all)")
            @RequestParam(required = false) Boolean isRead,
            
            Authentication authentication) {
        
        UUID userId = UUID.fromString(authentication.getName());
        
        List<NotificationDTO> notifications;
        if (isRead == null) {
            notifications = notificationService.getNotifications(userId);
        } else {
            notifications = notificationService.getNotifications(userId, isRead);
        }
        
        return ResponseEntity.ok(notifications);
    }
    
    /**
     * Get the count of unread notifications for the authenticated user.
     * Used for notification badge counts in the UI.
     * 
     * Validates Requirement 9.7
     *
     * @param authentication the authenticated user
     * @return count of unread notifications
     */
    @GetMapping("/unread-count")
    @PreAuthorize("isAuthenticated()")
    @Operation(
        summary = "Get unread notification count",
        description = "Retrieve the count of unread notifications for the authenticated user. " +
                     "Used for notification badge counts in the UI."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Unread count retrieved successfully",
            content = @Content(schema = @Schema(implementation = Long.class))
        ),
        @ApiResponse(responseCode = "401", description = "Not authenticated")
    })
    public ResponseEntity<Long> getUnreadCount(Authentication authentication) {
        UUID userId = UUID.fromString(authentication.getName());
        long count = notificationService.getUnreadCount(userId);
        return ResponseEntity.ok(count);
    }
    
    /**
     * Mark a specific notification as read.
     * Users can only mark their own notifications as read.
     * 
     * Validates Requirement 9.5
     *
     * @param id the notification identifier
     * @param authentication the authenticated user
     * @return the updated notification DTO
     */
    @PatchMapping("/{id}/read")
    @PreAuthorize("isAuthenticated()")
    @Operation(
        summary = "Mark notification as read",
        description = "Mark a specific notification as read. " +
                     "Users can only mark their own notifications as read."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Notification marked as read successfully",
            content = @Content(schema = @Schema(implementation = NotificationDTO.class))
        ),
        @ApiResponse(responseCode = "401", description = "Not authenticated"),
        @ApiResponse(responseCode = "403", description = "Insufficient permissions - can only mark own notifications as read"),
        @ApiResponse(responseCode = "404", description = "Notification not found")
    })
    public ResponseEntity<NotificationDTO> markAsRead(
            @Parameter(description = "Notification ID")
            @PathVariable UUID id,
            
            Authentication authentication) {
        
        UUID userId = UUID.fromString(authentication.getName());
        NotificationDTO notification = notificationService.markAsRead(id, userId);
        return ResponseEntity.ok(notification);
    }
    
    /**
     * Mark all notifications as read for the authenticated user.
     * 
     * Validates Requirement 9.5
     *
     * @param authentication the authenticated user
     * @return count of notifications marked as read
     */
    @PatchMapping("/mark-all-read")
    @PreAuthorize("isAuthenticated()")
    @Operation(
        summary = "Mark all notifications as read",
        description = "Mark all notifications as read for the authenticated user. " +
                     "Returns the count of notifications that were marked as read."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "All notifications marked as read successfully",
            content = @Content(schema = @Schema(implementation = Integer.class))
        ),
        @ApiResponse(responseCode = "401", description = "Not authenticated")
    })
    public ResponseEntity<Integer> markAllAsRead(Authentication authentication) {
        UUID userId = UUID.fromString(authentication.getName());
        int count = notificationService.markAllAsRead(userId);
        return ResponseEntity.ok(count);
    }
}
