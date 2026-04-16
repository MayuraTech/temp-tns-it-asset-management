package com.company.assetmanagement.repository;

import com.company.assetmanagement.model.Ticket;
import com.company.assetmanagement.model.TicketPriority;
import com.company.assetmanagement.model.TicketStatus;
import com.company.assetmanagement.model.TicketType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for Ticket entities.
 * Provides query methods for searching, filtering, and managing asset allocation/de-allocation tickets.
 * 
 * Supports filtering by:
 * - Status (PENDING, APPROVED, REJECTED, COMPLETED, CANCELLED)
 * - Type (ALLOCATION, DEALLOCATION)
 * - Priority (LOW, STANDARD, URGENT)
 * - Requester ID
 * - Approver ID
 * - Asset ID
 * - Date ranges
 * 
 * Validates Requirements: 8.1-8.10, 15.2, 15.3
 */
@Repository
public interface TicketRepository extends JpaRepository<Ticket, UUID> {
    
    /**
     * Find ticket by ticket number.
     * Ticket numbers are unique human-readable identifiers (e.g., TKT-2024-00001).
     *
     * @param ticketNumber the ticket number
     * @return optional containing the ticket if found
     */
    Optional<Ticket> findByTicketNumber(String ticketNumber);
    
    /**
     * Find all tickets by status.
     * Validates Requirement 8.1.
     *
     * @param status the ticket status
     * @param pageable pagination information
     * @return page of tickets with the specified status
     */
    Page<Ticket> findByStatus(TicketStatus status, Pageable pageable);
    
    /**
     * Find all tickets by type.
     * Validates Requirement 8.2.
     *
     * @param type the ticket type (ALLOCATION or DEALLOCATION)
     * @param pageable pagination information
     * @return page of tickets with the specified type
     */
    Page<Ticket> findByType(TicketType type, Pageable pageable);
    
    /**
     * Find all tickets by priority.
     * Validates Requirement 8.3.
     *
     * @param priority the ticket priority
     * @param pageable pagination information
     * @return page of tickets with the specified priority
     */
    Page<Ticket> findByPriority(TicketPriority priority, Pageable pageable);
    
    /**
     * Find all tickets created by a specific requester.
     * Validates Requirement 8.4.
     *
     * @param requesterId the requester user ID
     * @param pageable pagination information
     * @return page of tickets created by the requester
     */
    Page<Ticket> findByRequesterId(UUID requesterId, Pageable pageable);
    
    /**
     * Find all tickets approved by a specific approver.
     * Validates Requirement 8.5.
     *
     * @param approverId the approver user ID
     * @param pageable pagination information
     * @return page of tickets approved by the approver
     */
    Page<Ticket> findByApproverId(UUID approverId, Pageable pageable);
    
    /**
     * Find all tickets related to a specific asset.
     * Validates Requirement 8.6.
     *
     * @param assetId the asset ID
     * @param pageable pagination information
     * @return page of tickets related to the asset
     */
    Page<Ticket> findByAssetId(UUID assetId, Pageable pageable);
    
    /**
     * Find all tickets created within a date range.
     * Validates Requirement 8.7.
     *
     * @param startDate the start date (inclusive)
     * @param endDate the end date (inclusive)
     * @param pageable pagination information
     * @return page of tickets created within the date range
     */
    Page<Ticket> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);
    
    /**
     * Find all pending tickets for approval workflow.
     * Convenience method for finding tickets with PENDING status.
     * Validates Requirement 8.10.
     *
     * @param pageable pagination information
     * @return page of pending tickets
     */
    default Page<Ticket> findPendingApprovals(Pageable pageable) {
        return findByStatus(TicketStatus.PENDING, pageable);
    }
    
    /**
     * Search tickets with multiple filter criteria.
     * Supports combining filters using AND logic.
     * Validates Requirements 8.1-8.8.
     *
     * @param status optional status filter
     * @param type optional type filter
     * @param priority optional priority filter
     * @param requesterId optional requester ID filter
     * @param approverId optional approver ID filter
     * @param assetId optional asset ID filter
     * @param createdFrom optional start date filter
     * @param createdTo optional end date filter
     * @param pageable pagination information
     * @return page of tickets matching all specified filters
     */
    @Query("SELECT t FROM Ticket t WHERE " +
           "(:status IS NULL OR t.status = :status) AND " +
           "(:type IS NULL OR t.type = :type) AND " +
           "(:priority IS NULL OR t.priority = :priority) AND " +
           "(:requesterId IS NULL OR t.requesterId = :requesterId) AND " +
           "(:approverId IS NULL OR t.approverId = :approverId) AND " +
           "(:assetId IS NULL OR t.assetId = :assetId) AND " +
           "(:createdFrom IS NULL OR t.createdAt >= :createdFrom) AND " +
           "(:createdTo IS NULL OR t.createdAt <= :createdTo) " +
           "ORDER BY t.createdAt DESC")
    Page<Ticket> searchTickets(
        @Param("status") TicketStatus status,
        @Param("type") TicketType type,
        @Param("priority") TicketPriority priority,
        @Param("requesterId") UUID requesterId,
        @Param("approverId") UUID approverId,
        @Param("assetId") UUID assetId,
        @Param("createdFrom") LocalDateTime createdFrom,
        @Param("createdTo") LocalDateTime createdTo,
        Pageable pageable
    );
    
    /**
     * Count tickets by status.
     * Used for dashboard metrics and analytics.
     *
     * @param status the ticket status
     * @return count of tickets with the specified status
     */
    long countByStatus(TicketStatus status);
    
    /**
     * Count tickets by type.
     * Used for dashboard metrics and analytics.
     *
     * @param type the ticket type
     * @return count of tickets with the specified type
     */
    long countByType(TicketType type);
    
    /**
     * Count tickets by priority.
     * Used for dashboard metrics and analytics.
     *
     * @param priority the ticket priority
     * @return count of tickets with the specified priority
     */
    long countByPriority(TicketPriority priority);
    
    /**
     * Find tickets by status and requester.
     * Used for user-specific ticket views.
     *
     * @param status the ticket status
     * @param requesterId the requester user ID
     * @param pageable pagination information
     * @return page of tickets matching status and requester
     */
    Page<Ticket> findByStatusAndRequesterId(TicketStatus status, UUID requesterId, Pageable pageable);
    
    /**
     * Find tickets by type and status.
     * Used for filtered ticket views.
     *
     * @param type the ticket type
     * @param status the ticket status
     * @param pageable pagination information
     * @return page of tickets matching type and status
     */
    Page<Ticket> findByTypeAndStatus(TicketType type, TicketStatus status, Pageable pageable);
    
    /**
     * Find tickets by priority and status.
     * Used for priority-based ticket management.
     *
     * @param priority the ticket priority
     * @param status the ticket status
     * @param pageable pagination information
     * @return page of tickets matching priority and status
     */
    Page<Ticket> findByPriorityAndStatus(TicketPriority priority, TicketStatus status, Pageable pageable);
    
    /**
     * Find all tickets for a specific asset ordered by creation date.
     * Used for asset history tracking.
     *
     * @param assetId the asset ID
     * @return list of tickets related to the asset, ordered by creation date descending
     */
    List<Ticket> findByAssetIdOrderByCreatedAtDesc(UUID assetId);
    
    /**
     * Check if a ticket exists by ticket number.
     * Used for validation during ticket creation.
     *
     * @param ticketNumber the ticket number
     * @return true if ticket exists, false otherwise
     */
    boolean existsByTicketNumber(String ticketNumber);
    
    /**
     * Find tickets created within a date range with specific status.
     * Used for reporting and analytics.
     *
     * @param startDate the start date (inclusive)
     * @param endDate the end date (inclusive)
     * @param status the ticket status
     * @return list of tickets matching the criteria
     */
    List<Ticket> findByCreatedAtBetweenAndStatus(
        LocalDateTime startDate, 
        LocalDateTime endDate, 
        TicketStatus status
    );
    
    /**
     * Calculate average approval time in hours.
     * Used for metrics and analytics (Requirement 10.2).
     *
     * @param startDate optional start date filter
     * @param endDate optional end date filter
     * @return average approval time in hours, or null if no approved tickets
     */
    @Query("SELECT AVG(TIMESTAMPDIFF(HOUR, t.createdAt, t.approvedAt)) " +
           "FROM Ticket t WHERE t.status = 'APPROVED' AND " +
           "(:startDate IS NULL OR t.createdAt >= :startDate) AND " +
           "(:endDate IS NULL OR t.createdAt <= :endDate)")
    Double calculateAverageApprovalTimeHours(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );
    
    /**
     * Calculate average completion time in hours.
     * Used for metrics and analytics (Requirement 10.3).
     *
     * @param startDate optional start date filter
     * @param endDate optional end date filter
     * @return average completion time in hours, or null if no completed tickets
     */
    @Query("SELECT AVG(TIMESTAMPDIFF(HOUR, t.createdAt, t.completedAt)) " +
           "FROM Ticket t WHERE t.status = 'COMPLETED' AND " +
           "(:startDate IS NULL OR t.createdAt >= :startDate) AND " +
           "(:endDate IS NULL OR t.createdAt <= :endDate)")
    Double calculateAverageCompletionTimeHours(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );
}
