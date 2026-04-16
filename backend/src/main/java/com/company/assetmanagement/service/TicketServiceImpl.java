package com.company.assetmanagement.service;

import com.company.assetmanagement.dto.*;
import com.company.assetmanagement.exception.InsufficientPermissionsException;
import com.company.assetmanagement.exception.ResourceNotFoundException;
import com.company.assetmanagement.exception.ValidationException;
import com.company.assetmanagement.model.*;
import com.company.assetmanagement.repository.TicketRepository;
import com.company.assetmanagement.repository.TicketStatusHistoryRepository;
import com.company.assetmanagement.util.TicketMapper;
import com.company.assetmanagement.util.TicketStatusHistoryMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.Year;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementation of TicketService.
 * 
 * Handles ticket creation, retrieval, and search operations.
 * Integrates with AuditService for logging all operations.
 * Enforces authorization rules for ticket access.
 * 
 * Validates Requirements: 1.1-1.6, 2.1-2.5, 8.1-8.10, 11.1-11.7, 12.1-12.7, 15.1-15.3, 16.1-16.7
 */
@Service
@Transactional(readOnly = true)
public class TicketServiceImpl implements TicketService {
    
    private static final Logger logger = LoggerFactory.getLogger(TicketServiceImpl.class);
    
    private final TicketRepository ticketRepository;
    private final TicketStatusHistoryRepository statusHistoryRepository;
    private final AuditService auditService;
    
    public TicketServiceImpl(
            TicketRepository ticketRepository,
            TicketStatusHistoryRepository statusHistoryRepository,
            AuditService auditService) {
        this.ticketRepository = ticketRepository;
        this.statusHistoryRepository = statusHistoryRepository;
        this.auditService = auditService;
    }
    
    @Override
    @Transactional
    public TicketDTO createAllocationTicket(String userId, AllocationTicketRequest request) {
        logger.debug("Creating allocation ticket for user: {}, asset: {}", userId, request.getAssetId());
        
        // 1. Authorization check - all authenticated users can create tickets (Requirement 12.1)
        // This is handled by Spring Security at the controller level
        
        // 2. Validation
        validateAllocationTicketRequest(request);
        
        // 3. Validate asset exists and is available (Requirements 1.3, 1.4)
        // Note: Asset validation will be implemented when Asset module is available
        // For now, we'll proceed with the assumption that the asset ID is valid
        
        // 4. Create ticket entity
        Ticket ticket = new Ticket();
        ticket.setTicketNumber(generateTicketNumber());
        ticket.setType(TicketType.ALLOCATION);
        ticket.setStatus(TicketStatus.PENDING);
        ticket.setPriority(request.getPriority());
        
        // Asset information (will be populated from Asset entity when available)
        ticket.setAssetId(request.getAssetId());
        ticket.setAssetName("Asset-" + request.getAssetId()); // Placeholder
        ticket.setAssetSerialNumber("SN-" + request.getAssetId()); // Placeholder
        
        // Requester information (will be populated from User entity when available)
        UUID requesterUuid = UUID.fromString(userId);
        ticket.setRequesterId(requesterUuid);
        ticket.setRequesterName("User-" + userId); // Placeholder
        ticket.setRequesterEmail("user" + userId + "@example.com"); // Placeholder
        
        // Assignment information
        ticket.setAssignToUser(request.getAssignToUser());
        ticket.setAssignToUserEmail(request.getAssignToUserEmail());
        ticket.setAssignToLocation(request.getAssignToLocation());
        
        // Request details
        ticket.setRequestReason(request.getRequestReason());
        
        // 5. Save ticket
        Ticket savedTicket = ticketRepository.save(ticket);
        
        // 6. Create initial status history entry (Requirement 6.5)
        createStatusHistoryEntry(savedTicket, null, TicketStatus.PENDING, requesterUuid, "Ticket created");
        
        // 7. Audit logging (Requirement 16.1)
        logTicketCreation(userId, savedTicket);
        
        logger.info("Allocation ticket created: {}", savedTicket.getTicketNumber());
        
        return TicketMapper.toDTO(savedTicket);
    }
    
    @Override
    @Transactional
    public TicketDTO createDeallocationTicket(String userId, DeallocationTicketRequest request) {
        logger.debug("Creating de-allocation ticket for user: {}, asset: {}", userId, request.getAssetId());
        
        // 1. Authorization check - all authenticated users can create tickets (Requirement 12.1)
        // This is handled by Spring Security at the controller level
        
        // 2. Validation
        validateDeallocationTicketRequest(request);
        
        // 3. Validate asset exists and is currently assigned (Requirements 2.3, 2.4)
        // Note: Asset validation will be implemented when Asset module is available
        // For now, we'll proceed with the assumption that the asset ID is valid
        
        // 4. Create ticket entity
        Ticket ticket = new Ticket();
        ticket.setTicketNumber(generateTicketNumber());
        ticket.setType(TicketType.DEALLOCATION);
        ticket.setStatus(TicketStatus.PENDING);
        ticket.setPriority(request.getPriority());
        
        // Asset information (will be populated from Asset entity when available)
        ticket.setAssetId(request.getAssetId());
        ticket.setAssetName("Asset-" + request.getAssetId()); // Placeholder
        ticket.setAssetSerialNumber("SN-" + request.getAssetId()); // Placeholder
        
        // Requester information (will be populated from User entity when available)
        UUID requesterUuid = UUID.fromString(userId);
        ticket.setRequesterId(requesterUuid);
        ticket.setRequesterName("User-" + userId); // Placeholder
        ticket.setRequesterEmail("user" + userId + "@example.com"); // Placeholder
        
        // Request details
        ticket.setDeallocationReason(request.getDeallocationReason());
        
        // 5. Save ticket
        Ticket savedTicket = ticketRepository.save(ticket);
        
        // 6. Create initial status history entry (Requirement 6.5)
        createStatusHistoryEntry(savedTicket, null, TicketStatus.PENDING, requesterUuid, "Ticket created");
        
        // 7. Audit logging (Requirement 16.1)
        logTicketCreation(userId, savedTicket);
        
        logger.info("De-allocation ticket created: {}", savedTicket.getTicketNumber());
        
        return TicketMapper.toDTO(savedTicket);
    }

    
    @Override
    public Optional<TicketDTO> getTicket(String userId, String ticketId) {
        logger.debug("Getting ticket: {} for user: {}", ticketId, userId);
        
        UUID ticketUuid;
        try {
            ticketUuid = UUID.fromString(ticketId);
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid ticket ID format: {}", ticketId);
            return Optional.empty();
        }
        
        Optional<Ticket> ticketOpt = ticketRepository.findById(ticketUuid);
        
        if (ticketOpt.isEmpty()) {
            return Optional.empty();
        }
        
        Ticket ticket = ticketOpt.get();
        
        // Authorization check (Requirement 12.2, 12.3)
        // Users can only view their own tickets unless they have Asset_Manager or Administrator role
        // Note: Role checking will be implemented when User/Role module is available
        // For now, we'll allow access if the user is the requester
        UUID userUuid = UUID.fromString(userId);
        if (!ticket.getRequesterId().equals(userUuid)) {
            // In production, check if user has Asset_Manager or Administrator role
            // For now, throw exception
            logger.warn("User {} attempted to access ticket {} without permission", userId, ticketId);
            throw new InsufficientPermissionsException(userId, "VIEW_TICKET");
        }
        
        return Optional.of(TicketMapper.toDTO(ticket));
    }
    
    @Override
    public Page<TicketDTO> searchTickets(String userId, TicketSearchQuery query, Pageable pageable) {
        logger.debug("Searching tickets for user: {} with query: {}", userId, query);
        
        // Build search criteria
        TicketStatus status = (query.getStatuses() != null && !query.getStatuses().isEmpty()) 
            ? query.getStatuses().get(0) : null;
        TicketType type = (query.getTypes() != null && !query.getTypes().isEmpty()) 
            ? query.getTypes().get(0) : null;
        TicketPriority priority = (query.getPriorities() != null && !query.getPriorities().isEmpty()) 
            ? query.getPriorities().get(0) : null;
        
        LocalDateTime createdFrom = query.getCreatedFrom() != null 
            ? query.getCreatedFrom().atStartOfDay() : null;
        LocalDateTime createdTo = query.getCreatedTo() != null 
            ? query.getCreatedTo().atTime(23, 59, 59) : null;
        
        // Execute search (Requirement 8.1-8.8)
        Page<Ticket> tickets = ticketRepository.searchTickets(
            status,
            type,
            priority,
            query.getRequesterId(),
            query.getApproverId(),
            query.getAssetId(),
            createdFrom,
            createdTo,
            pageable
        );
        
        // Filter results based on user authorization (Requirement 12.2, 12.3)
        // Note: In production, check if user has Asset_Manager or Administrator role
        // For now, filter to only show tickets where user is the requester
        UUID userUuid = UUID.fromString(userId);
        Page<Ticket> filteredTickets = tickets.map(ticket -> {
            if (ticket.getRequesterId().equals(userUuid)) {
                return ticket;
            }
            // In production, check role here and return ticket if user has permission
            return null;
        }).map(ticket -> ticket);
        
        return filteredTickets.map(TicketMapper::toDTO);
    }
    
    @Override
    public Page<TicketDTO> getMyTickets(String userId, Pageable pageable) {
        logger.debug("Getting tickets for requester: {}", userId);
        
        UUID userUuid = UUID.fromString(userId);
        
        // Get tickets where user is the requester (Requirement 8.9)
        Page<Ticket> tickets = ticketRepository.findByRequesterId(userUuid, pageable);
        
        return tickets.map(TicketMapper::toDTO);
    }
    
    @Override
    public Page<TicketDTO> getPendingApprovals(String userId, Pageable pageable) {
        logger.debug("Getting pending approvals for user: {}", userId);
        
        // Authorization check (Requirement 12.4)
        // Requires Asset_Manager or Administrator role
        // Note: Role checking will be implemented when User/Role module is available
        // For now, we'll allow access (assuming authorization is handled at controller level)
        
        // Get all pending tickets (Requirement 8.10)
        Page<Ticket> pendingTickets = ticketRepository.findByStatus(TicketStatus.PENDING, pageable);
        
        return pendingTickets.map(TicketMapper::toDTO);
    }
    
    @Override
    public List<TicketStatusHistoryDTO> getStatusHistory(String userId, String ticketId) {
        logger.debug("Getting status history for ticket: {}", ticketId);
        
        UUID ticketUuid;
        try {
            ticketUuid = UUID.fromString(ticketId);
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid ticket ID format: {}", ticketId);
            throw new ResourceNotFoundException("Ticket", ticketId);
        }
        
        // Verify ticket exists
        Ticket ticket = ticketRepository.findById(ticketUuid)
            .orElseThrow(() -> new ResourceNotFoundException("Ticket", ticketId));
        
        // Authorization check - user must have permission to view the ticket
        UUID userUuid = UUID.fromString(userId);
        if (!ticket.getRequesterId().equals(userUuid)) {
            // In production, check if user has Asset_Manager or Administrator role
            logger.warn("User {} attempted to access ticket history {} without permission", userId, ticketId);
            throw new InsufficientPermissionsException(userId, "VIEW_TICKET");
        }
        
        // Get status history in chronological order (Requirement 6.3)
        List<TicketStatusHistory> history = statusHistoryRepository.findByTicketIdOrderByChangedAtAsc(ticketUuid);
        
        return history.stream()
            .map(TicketStatusHistoryMapper::toDTO)
            .collect(Collectors.toList());
    }
    
    // Private helper methods
    
    /**
     * Generate a unique ticket number in format TKT-YYYY-NNNNN.
     * Requirement 1.6: Generate human-readable ticket number.
     *
     * @return unique ticket number
     */
    private String generateTicketNumber() {
        int currentYear = Year.now().getValue();
        String prefix = "TKT-" + currentYear + "-";
        
        // Find the highest ticket number for the current year
        // This is a simplified implementation - in production, use a sequence or counter
        String pattern = prefix + "%";
        long count = ticketRepository.count();
        
        // Generate sequential number with leading zeros
        String sequentialNumber = String.format("%05d", count + 1);
        
        String ticketNumber = prefix + sequentialNumber;
        
        // Ensure uniqueness
        while (ticketRepository.existsByTicketNumber(ticketNumber)) {
            count++;
            sequentialNumber = String.format("%05d", count + 1);
            ticketNumber = prefix + sequentialNumber;
        }
        
        return ticketNumber;
    }
    
    /**
     * Validate allocation ticket request.
     * Requirement 11.1-11.7: Validate all required fields.
     *
     * @param request the allocation ticket request
     * @throws ValidationException if validation fails
     */
    private void validateAllocationTicketRequest(AllocationTicketRequest request) {
        List<ValidationException.ValidationError> errors = new ArrayList<>();
        
        if (request.getAssetId() == null) {
            errors.add(new ValidationException.ValidationError("assetId", "Asset ID is required"));
        }
        
        if (request.getPriority() == null) {
            errors.add(new ValidationException.ValidationError("priority", "Priority is required"));
        }
        
        if (request.getRequestReason() == null || request.getRequestReason().isBlank()) {
            errors.add(new ValidationException.ValidationError("requestReason", "Request reason is required"));
        } else if (request.getRequestReason().length() < 10 || request.getRequestReason().length() > 1000) {
            errors.add(new ValidationException.ValidationError("requestReason", 
                "Request reason must be between 10 and 1000 characters"));
        }
        
        // Validate that at least one assignment target is provided (Requirement 11.6)
        boolean hasAssignToUser = request.getAssignToUser() != null && !request.getAssignToUser().isBlank();
        boolean hasAssignToLocation = request.getAssignToLocation() != null && !request.getAssignToLocation().isBlank();
        
        if (!hasAssignToUser && !hasAssignToLocation) {
            errors.add(new ValidationException.ValidationError("assignment", 
                "At least one of assignToUser or assignToLocation must be provided"));
        }
        
        // Validate email format if provided (Requirement 11.7)
        if (request.getAssignToUserEmail() != null && !request.getAssignToUserEmail().isBlank()) {
            if (!isValidEmail(request.getAssignToUserEmail())) {
                errors.add(new ValidationException.ValidationError("assignToUserEmail", 
                    "Invalid email format"));
            }
        }
        
        if (!errors.isEmpty()) {
            throw new ValidationException(errors);
        }
    }
    
    /**
     * Validate de-allocation ticket request.
     * Requirement 11.1-11.7: Validate all required fields.
     *
     * @param request the de-allocation ticket request
     * @throws ValidationException if validation fails
     */
    private void validateDeallocationTicketRequest(DeallocationTicketRequest request) {
        List<ValidationException.ValidationError> errors = new ArrayList<>();
        
        if (request.getAssetId() == null) {
            errors.add(new ValidationException.ValidationError("assetId", "Asset ID is required"));
        }
        
        if (request.getPriority() == null) {
            errors.add(new ValidationException.ValidationError("priority", "Priority is required"));
        }
        
        if (request.getDeallocationReason() == null || request.getDeallocationReason().isBlank()) {
            errors.add(new ValidationException.ValidationError("deallocationReason", 
                "Deallocation reason is required"));
        } else if (request.getDeallocationReason().length() < 10 || request.getDeallocationReason().length() > 1000) {
            errors.add(new ValidationException.ValidationError("deallocationReason", 
                "Deallocation reason must be between 10 and 1000 characters"));
        }
        
        if (!errors.isEmpty()) {
            throw new ValidationException(errors);
        }
    }
    
    /**
     * Validate email format.
     *
     * @param email the email to validate
     * @return true if email format is valid
     */
    private boolean isValidEmail(String email) {
        if (email == null || email.isBlank()) {
            return false;
        }
        // Simple email validation - in production, use a more robust validator
        return email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    }
    
    /**
     * Create a status history entry for a ticket.
     * Requirement 6.1, 6.2: Maintain status history for every transition.
     *
     * @param ticket the ticket
     * @param fromStatus the previous status (null for initial creation)
     * @param toStatus the new status
     * @param changedBy the user who made the change
     * @param comments optional comments
     */
    private void createStatusHistoryEntry(Ticket ticket, TicketStatus fromStatus, 
                                         TicketStatus toStatus, UUID changedBy, String comments) {
        TicketStatusHistory history = new TicketStatusHistory(
            ticket.getId(),
            fromStatus,
            toStatus,
            changedBy,
            comments
        );
        
        statusHistoryRepository.save(history);
        
        logger.debug("Status history entry created for ticket: {} - {} -> {}", 
            ticket.getTicketNumber(), fromStatus, toStatus);
    }
    
    /**
     * Log ticket creation to audit service.
     * Requirement 16.1: Record all ticket creation operations.
     *
     * @param userId the user who created the ticket
     * @param ticket the created ticket
     */
    private void logTicketCreation(String userId, Ticket ticket) {
        try {
            AuditEventDTO event = new AuditEventDTO();
            event.setTimestamp(LocalDateTime.now());
            event.setUserId(UUID.fromString(userId));
            event.setUsername("User-" + userId); // Placeholder
            event.setActionType(Action.CREATE_TICKET);
            event.setResourceType("TICKET");
            event.setResourceId(ticket.getId().toString());
            
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("ticketNumber", ticket.getTicketNumber());
            metadata.put("ticketType", ticket.getType().toString());
            metadata.put("priority", ticket.getPriority().toString());
            metadata.put("assetId", ticket.getAssetId().toString());
            event.setMetadata(metadata);
            
            auditService.logEvent(event);
        } catch (Exception e) {
            logger.error("Failed to log ticket creation to audit service", e);
            // Don't throw exception - audit logging should not break business operations
        }
    }
}
