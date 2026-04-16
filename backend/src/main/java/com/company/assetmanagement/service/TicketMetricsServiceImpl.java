package com.company.assetmanagement.service;

import com.company.assetmanagement.dto.TicketDTO;
import com.company.assetmanagement.dto.TicketMetricsDTO;
import com.company.assetmanagement.model.Ticket;
import com.company.assetmanagement.model.TicketPriority;
import com.company.assetmanagement.model.TicketStatus;
import com.company.assetmanagement.model.TicketType;
import com.company.assetmanagement.repository.TicketRepository;
import com.company.assetmanagement.util.TicketMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Implementation of TicketMetricsService.
 * 
 * Provides ticket metrics and analytics with optimized queries for large datasets.
 * All aggregation operations are performed at the database level for performance.
 * Supports optional date range filtering for time-based analysis.
 * 
 * Validates Requirements: 10.1-10.7, 15.5
 */
@Service
@Transactional(readOnly = true)
public class TicketMetricsServiceImpl implements TicketMetricsService {
    
    private static final Logger logger = LoggerFactory.getLogger(TicketMetricsServiceImpl.class);
    
    private final TicketRepository ticketRepository;
    
    public TicketMetricsServiceImpl(TicketRepository ticketRepository) {
        this.ticketRepository = ticketRepository;
    }
    
    @Override
    public TicketMetricsDTO generateMetrics(LocalDate fromDate, LocalDate toDate) {
        logger.debug("Generating ticket metrics from {} to {}", fromDate, toDate);
        
        long startTime = System.currentTimeMillis();
        
        // Convert LocalDate to LocalDateTime for database queries
        LocalDateTime startDateTime = fromDate != null ? fromDate.atStartOfDay() : null;
        LocalDateTime endDateTime = toDate != null ? toDate.atTime(23, 59, 59) : null;
        
        // 1. Calculate total tickets (Requirement 10.1)
        Long totalTickets = calculateTotalTickets(startDateTime, endDateTime);
        
        // 2. Calculate tickets by status (Requirement 10.1)
        Map<String, Long> ticketsByStatus = calculateTicketsByStatus(startDateTime, endDateTime);
        
        // 3. Calculate tickets by type (Requirement 10.1)
        Map<String, Long> ticketsByType = calculateTicketsByType(startDateTime, endDateTime);
        
        // 4. Calculate tickets by priority (Requirement 10.1)
        Map<String, Long> ticketsByPriority = calculateTicketsByPriority(startDateTime, endDateTime);
        
        // 5. Calculate average approval time (Requirement 10.2)
        Double averageApprovalTimeHours = calculateAverageApprovalTime(fromDate, toDate);
        
        // 6. Calculate average completion time (Requirement 10.3)
        Double averageCompletionTimeHours = calculateAverageCompletionTime(fromDate, toDate);
        
        // 7. Calculate approval rate (Requirement 10.4)
        Double approvalRate = calculateApprovalRate(totalTickets, ticketsByStatus);
        
        // 8. Calculate rejection rate (Requirement 10.5)
        Double rejectionRate = calculateRejectionRate(totalTickets, ticketsByStatus);
        
        long duration = System.currentTimeMillis() - startTime;
        logger.info("Ticket metrics generated in {} ms - Total tickets: {}", duration, totalTickets);
        
        // Validate performance requirement (Requirement 15.5)
        if (duration > 5000) {
            logger.warn("Metrics calculation took {} ms, exceeding 5 second target", duration);
        }
        
        return new TicketMetricsDTO(
            totalTickets,
            ticketsByStatus,
            ticketsByType,
            ticketsByPriority,
            averageApprovalTimeHours,
            averageCompletionTimeHours,
            approvalRate,
            rejectionRate
        );
    }
    
    @Override
    public Page<TicketDTO> getTicketsByStatus(TicketStatus status, LocalDate fromDate, LocalDate toDate, Pageable pageable) {
        logger.debug("Getting tickets by status: {} from {} to {}", status, fromDate, toDate);
        
        LocalDateTime startDateTime = fromDate != null ? fromDate.atStartOfDay() : null;
        LocalDateTime endDateTime = toDate != null ? toDate.atTime(23, 59, 59) : null;
        
        Page<Ticket> tickets;
        
        if (startDateTime != null || endDateTime != null) {
            // Use search query with date range filtering
            tickets = ticketRepository.searchTickets(
                status,
                null,  // type
                null,  // priority
                null,  // requesterId
                null,  // approverId
                null,  // assetId
                startDateTime,
                endDateTime,
                pageable
            );
        } else {
            // Use simple status query without date filtering
            tickets = ticketRepository.findByStatus(status, pageable);
        }
        
        return tickets.map(TicketMapper::toDTO);
    }
    
    @Override
    public Page<TicketDTO> getTicketsByType(TicketType type, LocalDate fromDate, LocalDate toDate, Pageable pageable) {
        logger.debug("Getting tickets by type: {} from {} to {}", type, fromDate, toDate);
        
        LocalDateTime startDateTime = fromDate != null ? fromDate.atStartOfDay() : null;
        LocalDateTime endDateTime = toDate != null ? toDate.atTime(23, 59, 59) : null;
        
        Page<Ticket> tickets;
        
        if (startDateTime != null || endDateTime != null) {
            // Use search query with date range filtering
            tickets = ticketRepository.searchTickets(
                null,  // status
                type,
                null,  // priority
                null,  // requesterId
                null,  // approverId
                null,  // assetId
                startDateTime,
                endDateTime,
                pageable
            );
        } else {
            // Use simple type query without date filtering
            tickets = ticketRepository.findByType(type, pageable);
        }
        
        return tickets.map(TicketMapper::toDTO);
    }
    
    @Override
    public Page<TicketDTO> getTicketsByPriority(TicketPriority priority, LocalDate fromDate, LocalDate toDate, Pageable pageable) {
        logger.debug("Getting tickets by priority: {} from {} to {}", priority, fromDate, toDate);
        
        LocalDateTime startDateTime = fromDate != null ? fromDate.atStartOfDay() : null;
        LocalDateTime endDateTime = toDate != null ? toDate.atTime(23, 59, 59) : null;
        
        Page<Ticket> tickets;
        
        if (startDateTime != null || endDateTime != null) {
            // Use search query with date range filtering
            tickets = ticketRepository.searchTickets(
                null,  // status
                null,  // type
                priority,
                null,  // requesterId
                null,  // approverId
                null,  // assetId
                startDateTime,
                endDateTime,
                pageable
            );
        } else {
            // Use simple priority query without date filtering
            tickets = ticketRepository.findByPriority(priority, pageable);
        }
        
        return tickets.map(TicketMapper::toDTO);
    }
    
    @Override
    public Double calculateAverageApprovalTime(LocalDate fromDate, LocalDate toDate) {
        logger.debug("Calculating average approval time from {} to {}", fromDate, toDate);
        
        LocalDateTime startDateTime = fromDate != null ? fromDate.atStartOfDay() : null;
        LocalDateTime endDateTime = toDate != null ? toDate.atTime(23, 59, 59) : null;
        
        Double averageHours = ticketRepository.calculateAverageApprovalTimeHours(startDateTime, endDateTime);
        
        // Return 0.0 if no approved tickets exist (Requirement 10.2)
        return averageHours != null ? averageHours : 0.0;
    }
    
    @Override
    public Double calculateAverageCompletionTime(LocalDate fromDate, LocalDate toDate) {
        logger.debug("Calculating average completion time from {} to {}", fromDate, toDate);
        
        LocalDateTime startDateTime = fromDate != null ? fromDate.atStartOfDay() : null;
        LocalDateTime endDateTime = toDate != null ? toDate.atTime(23, 59, 59) : null;
        
        Double averageHours = ticketRepository.calculateAverageCompletionTimeHours(startDateTime, endDateTime);
        
        // Return 0.0 if no completed tickets exist (Requirement 10.3)
        return averageHours != null ? averageHours : 0.0;
    }
    
    // Private helper methods
    
    /**
     * Calculate total ticket count with optional date range filtering.
     *
     * @param startDateTime optional start date/time
     * @param endDateTime optional end date/time
     * @return total ticket count
     */
    private Long calculateTotalTickets(LocalDateTime startDateTime, LocalDateTime endDateTime) {
        if (startDateTime != null || endDateTime != null) {
            // Count tickets within date range
            return ticketRepository.searchTickets(
                null, null, null, null, null, null,
                startDateTime, endDateTime,
                Pageable.unpaged()
            ).getTotalElements();
        } else {
            // Count all tickets
            return ticketRepository.count();
        }
    }
    
    /**
     * Calculate tickets grouped by status with optional date range filtering.
     *
     * @param startDateTime optional start date/time
     * @param endDateTime optional end date/time
     * @return map of status to ticket count
     */
    private Map<String, Long> calculateTicketsByStatus(LocalDateTime startDateTime, LocalDateTime endDateTime) {
        Map<String, Long> statusCounts = new HashMap<>();
        
        for (TicketStatus status : TicketStatus.values()) {
            long count;
            
            if (startDateTime != null || endDateTime != null) {
                // Count tickets with status within date range
                count = ticketRepository.searchTickets(
                    status, null, null, null, null, null,
                    startDateTime, endDateTime,
                    Pageable.unpaged()
                ).getTotalElements();
            } else {
                // Count all tickets with status
                count = ticketRepository.countByStatus(status);
            }
            
            statusCounts.put(status.getValue(), count);
        }
        
        return statusCounts;
    }
    
    /**
     * Calculate tickets grouped by type with optional date range filtering.
     *
     * @param startDateTime optional start date/time
     * @param endDateTime optional end date/time
     * @return map of type to ticket count
     */
    private Map<String, Long> calculateTicketsByType(LocalDateTime startDateTime, LocalDateTime endDateTime) {
        Map<String, Long> typeCounts = new HashMap<>();
        
        for (TicketType type : TicketType.values()) {
            long count;
            
            if (startDateTime != null || endDateTime != null) {
                // Count tickets with type within date range
                count = ticketRepository.searchTickets(
                    null, type, null, null, null, null,
                    startDateTime, endDateTime,
                    Pageable.unpaged()
                ).getTotalElements();
            } else {
                // Count all tickets with type
                count = ticketRepository.countByType(type);
            }
            
            typeCounts.put(type.getValue(), count);
        }
        
        return typeCounts;
    }
    
    /**
     * Calculate tickets grouped by priority with optional date range filtering.
     *
     * @param startDateTime optional start date/time
     * @param endDateTime optional end date/time
     * @return map of priority to ticket count
     */
    private Map<String, Long> calculateTicketsByPriority(LocalDateTime startDateTime, LocalDateTime endDateTime) {
        Map<String, Long> priorityCounts = new HashMap<>();
        
        for (TicketPriority priority : TicketPriority.values()) {
            long count;
            
            if (startDateTime != null || endDateTime != null) {
                // Count tickets with priority within date range
                count = ticketRepository.searchTickets(
                    null, null, priority, null, null, null,
                    startDateTime, endDateTime,
                    Pageable.unpaged()
                ).getTotalElements();
            } else {
                // Count all tickets with priority
                count = ticketRepository.countByPriority(priority);
            }
            
            priorityCounts.put(priority.getValue(), count);
        }
        
        return priorityCounts;
    }
    
    /**
     * Calculate approval rate as percentage of approved tickets.
     * Validates Requirement 10.4.
     *
     * @param totalTickets total ticket count
     * @param ticketsByStatus map of status to ticket count
     * @return approval rate percentage (0.0 to 100.0)
     */
    private Double calculateApprovalRate(Long totalTickets, Map<String, Long> ticketsByStatus) {
        if (totalTickets == 0) {
            return 0.0;
        }
        
        Long approvedCount = ticketsByStatus.getOrDefault(TicketStatus.APPROVED.getValue(), 0L);
        Long completedCount = ticketsByStatus.getOrDefault(TicketStatus.COMPLETED.getValue(), 0L);
        
        // Include both APPROVED and COMPLETED tickets in approval rate
        Long totalApproved = approvedCount + completedCount;
        
        return (totalApproved.doubleValue() / totalTickets.doubleValue()) * 100.0;
    }
    
    /**
     * Calculate rejection rate as percentage of rejected tickets.
     * Validates Requirement 10.5.
     *
     * @param totalTickets total ticket count
     * @param ticketsByStatus map of status to ticket count
     * @return rejection rate percentage (0.0 to 100.0)
     */
    private Double calculateRejectionRate(Long totalTickets, Map<String, Long> ticketsByStatus) {
        if (totalTickets == 0) {
            return 0.0;
        }
        
        Long rejectedCount = ticketsByStatus.getOrDefault(TicketStatus.REJECTED.getValue(), 0L);
        
        return (rejectedCount.doubleValue() / totalTickets.doubleValue()) * 100.0;
    }
}
