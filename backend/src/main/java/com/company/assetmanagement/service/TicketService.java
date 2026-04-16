package com.company.assetmanagement.service;

import com.company.assetmanagement.dto.AllocationTicketRequest;
import com.company.assetmanagement.dto.DeallocationTicketRequest;
import com.company.assetmanagement.dto.TicketDTO;
import com.company.assetmanagement.dto.TicketSearchQuery;
import com.company.assetmanagement.dto.TicketStatusHistoryDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service interface for ticket management operations.
 * 
 * Provides methods for creating, retrieving, and searching tickets.
 * Handles allocation and de-allocation ticket workflows with validation and authorization.
 * 
 * All ticket operations are logged to the audit service for compliance.
 * Validates Requirements: 1.1-1.6, 2.1-2.5, 8.1-8.10, 11.1-11.7, 12.1-12.7, 15.1-15.3, 16.1-16.7
 */
public interface TicketService {
    
    /**
     * Create an allocation ticket.
     * Validates the asset exists and is available for allocation.
     * Generates a unique ticket number in format TKT-YYYY-NNNNN.
     * 
     * Validates Requirements: 1.1-1.6, 11.1-11.7, 12.1, 15.1, 16.1
     *
     * @param userId the ID of the user creating the ticket
     * @param request the allocation ticket request
     * @return the created ticket DTO
     * @throws ResourceNotFoundException if asset is not found
     * @throws ValidationException if request data is invalid
     * @throws InsufficientPermissionsException if user lacks CREATE_TICKET permission
     */
    TicketDTO createAllocationTicket(String userId, AllocationTicketRequest request);
    
    /**
     * Create a de-allocation ticket.
     * Validates the asset exists and is currently assigned.
     * Generates a unique ticket number in format TKT-YYYY-NNNNN.
     * 
     * Validates Requirements: 2.1-2.5, 11.1-11.7, 12.1, 15.1, 16.1
     *
     * @param userId the ID of the user creating the ticket
     * @param request the de-allocation ticket request
     * @return the created ticket DTO
     * @throws ResourceNotFoundException if asset is not found
     * @throws ValidationException if request data is invalid or asset is not assigned
     * @throws InsufficientPermissionsException if user lacks CREATE_TICKET permission
     */
    TicketDTO createDeallocationTicket(String userId, DeallocationTicketRequest request);
    
    /**
     * Get a ticket by ID.
     * Enforces authorization: users can only view their own tickets unless they have
     * Asset_Manager or Administrator role.
     * 
     * Validates Requirements: 12.2, 12.3, 15.2
     *
     * @param userId the ID of the user requesting the ticket
     * @param ticketId the ticket identifier
     * @return optional containing the ticket DTO if found and authorized
     * @throws InsufficientPermissionsException if user lacks permission to view the ticket
     */
    Optional<TicketDTO> getTicket(String userId, String ticketId);
    
    /**
     * Search tickets with filtering and pagination.
     * Supports filtering by status, type, priority, requester, approver, asset, and date range.
     * 
     * Validates Requirements: 8.1-8.10, 15.3
     *
     * @param userId the ID of the user performing the search
     * @param query the search query with filter criteria
     * @param pageable pagination information
     * @return page of tickets matching the search criteria
     */
    Page<TicketDTO> searchTickets(String userId, TicketSearchQuery query, Pageable pageable);
    
    /**
     * Get tickets created by the authenticated user (My Requests view).
     * Returns only tickets where the user is the requester.
     * 
     * Validates Requirements: 8.4, 8.9, 12.2
     *
     * @param userId the ID of the user
     * @param pageable pagination information
     * @return page of tickets created by the user
     */
    Page<TicketDTO> getMyTickets(String userId, Pageable pageable);
    
    /**
     * Get pending tickets for approval (Pending Approvals view).
     * Returns all tickets with PENDING status.
     * Requires Asset_Manager or Administrator role.
     * 
     * Validates Requirements: 8.10, 12.3, 12.4
     *
     * @param userId the ID of the user requesting pending approvals
     * @param pageable pagination information
     * @return page of pending tickets
     * @throws InsufficientPermissionsException if user lacks APPROVE_TICKET permission
     */
    Page<TicketDTO> getPendingApprovals(String userId, Pageable pageable);
    
    /**
     * Get status history for a ticket.
     * Returns all status transitions in chronological order.
     * 
     * Validates Requirements: 6.1, 6.2, 6.3
     *
     * @param userId the ID of the user requesting the history
     * @param ticketId the ticket identifier
     * @return list of status history entries in chronological order
     * @throws ResourceNotFoundException if ticket is not found
     * @throws InsufficientPermissionsException if user lacks permission to view the ticket
     */
    List<TicketStatusHistoryDTO> getStatusHistory(String userId, String ticketId);
}
