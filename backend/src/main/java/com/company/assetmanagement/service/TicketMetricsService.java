package com.company.assetmanagement.service;

import com.company.assetmanagement.dto.TicketDTO;
import com.company.assetmanagement.dto.TicketMetricsDTO;
import com.company.assetmanagement.model.TicketPriority;
import com.company.assetmanagement.model.TicketStatus;
import com.company.assetmanagement.model.TicketType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

/**
 * Service interface for ticket metrics and analytics operations.
 * 
 * Provides methods for generating aggregated statistics about ticket processing,
 * performance metrics, and filtered ticket retrieval by various criteria.
 * 
 * All metrics operations support optional date range filtering for time-based analysis.
 * Optimized for large datasets with efficient query execution.
 * 
 * Validates Requirements: 10.1-10.7, 15.5
 */
public interface TicketMetricsService {
    
    /**
     * Generate comprehensive ticket metrics with optional date range filtering.
     * 
     * Calculates:
     * - Total ticket count
     * - Tickets grouped by status (PENDING, APPROVED, REJECTED, COMPLETED, CANCELLED)
     * - Tickets grouped by type (ALLOCATION, DEALLOCATION)
     * - Tickets grouped by priority (LOW, STANDARD, URGENT)
     * - Average approval time in hours
     * - Average completion time in hours
     * - Approval rate (percentage of tickets approved)
     * - Rejection rate (percentage of tickets rejected)
     * 
     * Validates Requirements: 10.1-10.7, 15.5
     *
     * @param fromDate optional start date for filtering (inclusive)
     * @param toDate optional end date for filtering (inclusive)
     * @return ticket metrics DTO with aggregated statistics
     */
    TicketMetricsDTO generateMetrics(LocalDate fromDate, LocalDate toDate);
    
    /**
     * Get tickets filtered by status with pagination.
     * 
     * Supports optional date range filtering for time-based analysis.
     * Optimized with database indexes for fast retrieval.
     * 
     * Validates Requirement 10.1
     *
     * @param status the ticket status to filter by
     * @param fromDate optional start date for filtering (inclusive)
     * @param toDate optional end date for filtering (inclusive)
     * @param pageable pagination information
     * @return page of tickets with the specified status
     */
    Page<TicketDTO> getTicketsByStatus(TicketStatus status, LocalDate fromDate, LocalDate toDate, Pageable pageable);
    
    /**
     * Get tickets filtered by type with pagination.
     * 
     * Supports optional date range filtering for time-based analysis.
     * Optimized with database indexes for fast retrieval.
     * 
     * Validates Requirement 10.1
     *
     * @param type the ticket type to filter by (ALLOCATION or DEALLOCATION)
     * @param fromDate optional start date for filtering (inclusive)
     * @param toDate optional end date for filtering (inclusive)
     * @param pageable pagination information
     * @return page of tickets with the specified type
     */
    Page<TicketDTO> getTicketsByType(TicketType type, LocalDate fromDate, LocalDate toDate, Pageable pageable);
    
    /**
     * Get tickets filtered by priority with pagination.
     * 
     * Supports optional date range filtering for time-based analysis.
     * Optimized with database indexes for fast retrieval.
     * 
     * Validates Requirement 10.1
     *
     * @param priority the ticket priority to filter by (LOW, STANDARD, URGENT)
     * @param fromDate optional start date for filtering (inclusive)
     * @param toDate optional end date for filtering (inclusive)
     * @param pageable pagination information
     * @return page of tickets with the specified priority
     */
    Page<TicketDTO> getTicketsByPriority(TicketPriority priority, LocalDate fromDate, LocalDate toDate, Pageable pageable);
    
    /**
     * Calculate average approval time in hours.
     * 
     * Computes the mean time between ticket creation and approval for all approved tickets.
     * Supports optional date range filtering for time-based analysis.
     * 
     * Validates Requirement 10.2
     *
     * @param fromDate optional start date for filtering (inclusive)
     * @param toDate optional end date for filtering (inclusive)
     * @return average approval time in hours, or 0.0 if no approved tickets exist
     */
    Double calculateAverageApprovalTime(LocalDate fromDate, LocalDate toDate);
    
    /**
     * Calculate average completion time in hours.
     * 
     * Computes the mean time between ticket creation and completion for all completed tickets.
     * Supports optional date range filtering for time-based analysis.
     * 
     * Validates Requirement 10.3
     *
     * @param fromDate optional start date for filtering (inclusive)
     * @param toDate optional end date for filtering (inclusive)
     * @return average completion time in hours, or 0.0 if no completed tickets exist
     */
    Double calculateAverageCompletionTime(LocalDate fromDate, LocalDate toDate);
}
