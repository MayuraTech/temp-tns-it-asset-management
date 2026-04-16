package com.company.assetmanagement.service;

import com.company.assetmanagement.dto.TicketDTO;
import com.company.assetmanagement.dto.TicketMetricsDTO;
import com.company.assetmanagement.model.Ticket;
import com.company.assetmanagement.model.TicketPriority;
import com.company.assetmanagement.model.TicketStatus;
import com.company.assetmanagement.model.TicketType;
import com.company.assetmanagement.repository.TicketRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for TicketMetricsServiceImpl.
 * 
 * Tests all metrics calculation methods, filtering operations, and performance requirements.
 * Validates Requirements: 10.1-10.7, 15.5
 */
@ExtendWith(MockitoExtension.class)
class TicketMetricsServiceImplTest {
    
    @Mock
    private TicketRepository ticketRepository;
    
    @InjectMocks
    private TicketMetricsServiceImpl ticketMetricsService;
    
    private LocalDate testFromDate;
    private LocalDate testToDate;
    private LocalDateTime testFromDateTime;
    private LocalDateTime testToDateTime;
    
    @BeforeEach
    void setUp() {
        testFromDate = LocalDate.of(2024, 1, 1);
        testToDate = LocalDate.of(2024, 12, 31);
        testFromDateTime = testFromDate.atStartOfDay();
        testToDateTime = testToDate.atTime(23, 59, 59);
    }
    
    @Test
    @DisplayName("Should generate comprehensive metrics without date filtering")
    void shouldGenerateMetricsWithoutDateFiltering() {
        // Given
        when(ticketRepository.count()).thenReturn(100L);
        
        // Mock status counts
        when(ticketRepository.countByStatus(TicketStatus.PENDING)).thenReturn(20L);
        when(ticketRepository.countByStatus(TicketStatus.APPROVED)).thenReturn(15L);
        when(ticketRepository.countByStatus(TicketStatus.REJECTED)).thenReturn(10L);
        when(ticketRepository.countByStatus(TicketStatus.COMPLETED)).thenReturn(50L);
        when(ticketRepository.countByStatus(TicketStatus.CANCELLED)).thenReturn(5L);
        
        // Mock type counts
        when(ticketRepository.countByType(TicketType.ALLOCATION)).thenReturn(60L);
        when(ticketRepository.countByType(TicketType.DEALLOCATION)).thenReturn(40L);
        
        // Mock priority counts
        when(ticketRepository.countByPriority(TicketPriority.LOW)).thenReturn(30L);
        when(ticketRepository.countByPriority(TicketPriority.STANDARD)).thenReturn(50L);
        when(ticketRepository.countByPriority(TicketPriority.URGENT)).thenReturn(20L);
        
        // Mock average times
        when(ticketRepository.calculateAverageApprovalTimeHours(null, null)).thenReturn(24.5);
        when(ticketRepository.calculateAverageCompletionTimeHours(null, null)).thenReturn(48.0);
        
        // When
        TicketMetricsDTO metrics = ticketMetricsService.generateMetrics(null, null);
        
        // Then
        assertThat(metrics).isNotNull();
        assertThat(metrics.getTotalTickets()).isEqualTo(100L);
        
        // Verify status counts
        assertThat(metrics.getTicketsByStatus()).containsEntry("pending", 20L);
        assertThat(metrics.getTicketsByStatus()).containsEntry("approved", 15L);
        assertThat(metrics.getTicketsByStatus()).containsEntry("rejected", 10L);
        assertThat(metrics.getTicketsByStatus()).containsEntry("completed", 50L);
        assertThat(metrics.getTicketsByStatus()).containsEntry("cancelled", 5L);
        
        // Verify type counts
        assertThat(metrics.getTicketsByType()).containsEntry("allocation", 60L);
        assertThat(metrics.getTicketsByType()).containsEntry("deallocation", 40L);
        
        // Verify priority counts
        assertThat(metrics.getTicketsByPriority()).containsEntry("low", 30L);
        assertThat(metrics.getTicketsByPriority()).containsEntry("standard", 50L);
        assertThat(metrics.getTicketsByPriority()).containsEntry("urgent", 20L);
        
        // Verify average times
        assertThat(metrics.getAverageApprovalTimeHours()).isEqualTo(24.5);
        assertThat(metrics.getAverageCompletionTimeHours()).isEqualTo(48.0);
        
        // Verify rates (approved + completed = 65, rejected = 10)
        assertThat(metrics.getApprovalRate()).isEqualTo(65.0); // (15 + 50) / 100 * 100
        assertThat(metrics.getRejectionRate()).isEqualTo(10.0); // 10 / 100 * 100
        
        verify(ticketRepository).count();
        verify(ticketRepository, times(5)).countByStatus(any(TicketStatus.class));
        verify(ticketRepository, times(2)).countByType(any(TicketType.class));
        verify(ticketRepository, times(3)).countByPriority(any(TicketPriority.class));
    }
    
    @Test
    @DisplayName("Should generate metrics with date range filtering")
    void shouldGenerateMetricsWithDateRangeFiltering() {
        // Given
        Page<Ticket> emptyPage = new PageImpl<>(List.of());
        when(ticketRepository.searchTickets(
            any(), any(), any(), any(), any(), any(), 
            eq(testFromDateTime), eq(testToDateTime), 
            eq(Pageable.unpaged())
        )).thenReturn(emptyPage);
        
        when(ticketRepository.calculateAverageApprovalTimeHours(testFromDateTime, testToDateTime))
            .thenReturn(20.0);
        when(ticketRepository.calculateAverageCompletionTimeHours(testFromDateTime, testToDateTime))
            .thenReturn(40.0);
        
        // When
        TicketMetricsDTO metrics = ticketMetricsService.generateMetrics(testFromDate, testToDate);
        
        // Then
        assertThat(metrics).isNotNull();
        assertThat(metrics.getAverageApprovalTimeHours()).isEqualTo(20.0);
        assertThat(metrics.getAverageCompletionTimeHours()).isEqualTo(40.0);
        
        verify(ticketRepository).calculateAverageApprovalTimeHours(testFromDateTime, testToDateTime);
        verify(ticketRepository).calculateAverageCompletionTimeHours(testFromDateTime, testToDateTime);
    }
    
    @Test
    @DisplayName("Should handle zero tickets gracefully")
    void shouldHandleZeroTicketsGracefully() {
        // Given
        when(ticketRepository.count()).thenReturn(0L);
        
        when(ticketRepository.countByStatus(any(TicketStatus.class))).thenReturn(0L);
        when(ticketRepository.countByType(any(TicketType.class))).thenReturn(0L);
        when(ticketRepository.countByPriority(any(TicketPriority.class))).thenReturn(0L);
        
        when(ticketRepository.calculateAverageApprovalTimeHours(null, null)).thenReturn(null);
        when(ticketRepository.calculateAverageCompletionTimeHours(null, null)).thenReturn(null);
        
        // When
        TicketMetricsDTO metrics = ticketMetricsService.generateMetrics(null, null);
        
        // Then
        assertThat(metrics).isNotNull();
        assertThat(metrics.getTotalTickets()).isEqualTo(0L);
        assertThat(metrics.getAverageApprovalTimeHours()).isEqualTo(0.0);
        assertThat(metrics.getAverageCompletionTimeHours()).isEqualTo(0.0);
        assertThat(metrics.getApprovalRate()).isEqualTo(0.0);
        assertThat(metrics.getRejectionRate()).isEqualTo(0.0);
    }
    
    @Test
    @DisplayName("Should get tickets by status without date filtering")
    void shouldGetTicketsByStatusWithoutDateFiltering() {
        // Given
        Ticket ticket = createTestTicket(TicketStatus.PENDING);
        Page<Ticket> ticketPage = new PageImpl<>(List.of(ticket));
        Pageable pageable = PageRequest.of(0, 20);
        
        when(ticketRepository.findByStatus(TicketStatus.PENDING, pageable)).thenReturn(ticketPage);
        
        // When
        Page<TicketDTO> result = ticketMetricsService.getTicketsByStatus(
            TicketStatus.PENDING, null, null, pageable
        );
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getStatus()).isEqualTo(TicketStatus.PENDING);
        
        verify(ticketRepository).findByStatus(TicketStatus.PENDING, pageable);
        verify(ticketRepository, never()).searchTickets(any(), any(), any(), any(), any(), any(), any(), any(), any());
    }
    
    @Test
    @DisplayName("Should get tickets by status with date filtering")
    void shouldGetTicketsByStatusWithDateFiltering() {
        // Given
        Ticket ticket = createTestTicket(TicketStatus.APPROVED);
        Page<Ticket> ticketPage = new PageImpl<>(List.of(ticket));
        Pageable pageable = PageRequest.of(0, 20);
        
        when(ticketRepository.searchTickets(
            eq(TicketStatus.APPROVED), any(), any(), any(), any(), any(),
            eq(testFromDateTime), eq(testToDateTime), eq(pageable)
        )).thenReturn(ticketPage);
        
        // When
        Page<TicketDTO> result = ticketMetricsService.getTicketsByStatus(
            TicketStatus.APPROVED, testFromDate, testToDate, pageable
        );
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        
        verify(ticketRepository).searchTickets(
            eq(TicketStatus.APPROVED), any(), any(), any(), any(), any(),
            eq(testFromDateTime), eq(testToDateTime), eq(pageable)
        );
    }
    
    @Test
    @DisplayName("Should get tickets by type without date filtering")
    void shouldGetTicketsByTypeWithoutDateFiltering() {
        // Given
        Ticket ticket = createTestTicket(TicketStatus.PENDING);
        ticket.setType(TicketType.ALLOCATION);
        Page<Ticket> ticketPage = new PageImpl<>(List.of(ticket));
        Pageable pageable = PageRequest.of(0, 20);
        
        when(ticketRepository.findByType(TicketType.ALLOCATION, pageable)).thenReturn(ticketPage);
        
        // When
        Page<TicketDTO> result = ticketMetricsService.getTicketsByType(
            TicketType.ALLOCATION, null, null, pageable
        );
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getType()).isEqualTo(TicketType.ALLOCATION);
        
        verify(ticketRepository).findByType(TicketType.ALLOCATION, pageable);
    }
    
    @Test
    @DisplayName("Should get tickets by type with date filtering")
    void shouldGetTicketsByTypeWithDateFiltering() {
        // Given
        Ticket ticket = createTestTicket(TicketStatus.PENDING);
        ticket.setType(TicketType.DEALLOCATION);
        Page<Ticket> ticketPage = new PageImpl<>(List.of(ticket));
        Pageable pageable = PageRequest.of(0, 20);
        
        when(ticketRepository.searchTickets(
            any(), eq(TicketType.DEALLOCATION), any(), any(), any(), any(),
            eq(testFromDateTime), eq(testToDateTime), eq(pageable)
        )).thenReturn(ticketPage);
        
        // When
        Page<TicketDTO> result = ticketMetricsService.getTicketsByType(
            TicketType.DEALLOCATION, testFromDate, testToDate, pageable
        );
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        
        verify(ticketRepository).searchTickets(
            any(), eq(TicketType.DEALLOCATION), any(), any(), any(), any(),
            eq(testFromDateTime), eq(testToDateTime), eq(pageable)
        );
    }
    
    @Test
    @DisplayName("Should get tickets by priority without date filtering")
    void shouldGetTicketsByPriorityWithoutDateFiltering() {
        // Given
        Ticket ticket = createTestTicket(TicketStatus.PENDING);
        ticket.setPriority(TicketPriority.URGENT);
        Page<Ticket> ticketPage = new PageImpl<>(List.of(ticket));
        Pageable pageable = PageRequest.of(0, 20);
        
        when(ticketRepository.findByPriority(TicketPriority.URGENT, pageable)).thenReturn(ticketPage);
        
        // When
        Page<TicketDTO> result = ticketMetricsService.getTicketsByPriority(
            TicketPriority.URGENT, null, null, pageable
        );
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getPriority()).isEqualTo(TicketPriority.URGENT);
        
        verify(ticketRepository).findByPriority(TicketPriority.URGENT, pageable);
    }
    
    @Test
    @DisplayName("Should get tickets by priority with date filtering")
    void shouldGetTicketsByPriorityWithDateFiltering() {
        // Given
        Ticket ticket = createTestTicket(TicketStatus.PENDING);
        ticket.setPriority(TicketPriority.LOW);
        Page<Ticket> ticketPage = new PageImpl<>(List.of(ticket));
        Pageable pageable = PageRequest.of(0, 20);
        
        when(ticketRepository.searchTickets(
            any(), any(), eq(TicketPriority.LOW), any(), any(), any(),
            eq(testFromDateTime), eq(testToDateTime), eq(pageable)
        )).thenReturn(ticketPage);
        
        // When
        Page<TicketDTO> result = ticketMetricsService.getTicketsByPriority(
            TicketPriority.LOW, testFromDate, testToDate, pageable
        );
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        
        verify(ticketRepository).searchTickets(
            any(), any(), eq(TicketPriority.LOW), any(), any(), any(),
            eq(testFromDateTime), eq(testToDateTime), eq(pageable)
        );
    }
    
    @Test
    @DisplayName("Should calculate average approval time")
    void shouldCalculateAverageApprovalTime() {
        // Given
        when(ticketRepository.calculateAverageApprovalTimeHours(null, null)).thenReturn(36.5);
        
        // When
        Double result = ticketMetricsService.calculateAverageApprovalTime(null, null);
        
        // Then
        assertThat(result).isEqualTo(36.5);
        verify(ticketRepository).calculateAverageApprovalTimeHours(null, null);
    }
    
    @Test
    @DisplayName("Should return 0.0 when no approved tickets exist")
    void shouldReturnZeroWhenNoApprovedTicketsExist() {
        // Given
        when(ticketRepository.calculateAverageApprovalTimeHours(null, null)).thenReturn(null);
        
        // When
        Double result = ticketMetricsService.calculateAverageApprovalTime(null, null);
        
        // Then
        assertThat(result).isEqualTo(0.0);
    }
    
    @Test
    @DisplayName("Should calculate average completion time")
    void shouldCalculateAverageCompletionTime() {
        // Given
        when(ticketRepository.calculateAverageCompletionTimeHours(null, null)).thenReturn(72.0);
        
        // When
        Double result = ticketMetricsService.calculateAverageCompletionTime(null, null);
        
        // Then
        assertThat(result).isEqualTo(72.0);
        verify(ticketRepository).calculateAverageCompletionTimeHours(null, null);
    }
    
    @Test
    @DisplayName("Should return 0.0 when no completed tickets exist")
    void shouldReturnZeroWhenNoCompletedTicketsExist() {
        // Given
        when(ticketRepository.calculateAverageCompletionTimeHours(null, null)).thenReturn(null);
        
        // When
        Double result = ticketMetricsService.calculateAverageCompletionTime(null, null);
        
        // Then
        assertThat(result).isEqualTo(0.0);
    }
    
    @Test
    @DisplayName("Should calculate approval rate correctly")
    void shouldCalculateApprovalRateCorrectly() {
        // Given - 40 approved, 30 completed out of 100 total = 70% approval rate
        when(ticketRepository.count()).thenReturn(100L);
        when(ticketRepository.countByStatus(TicketStatus.APPROVED)).thenReturn(40L);
        when(ticketRepository.countByStatus(TicketStatus.COMPLETED)).thenReturn(30L);
        when(ticketRepository.countByStatus(TicketStatus.REJECTED)).thenReturn(20L);
        when(ticketRepository.countByStatus(TicketStatus.PENDING)).thenReturn(5L);
        when(ticketRepository.countByStatus(TicketStatus.CANCELLED)).thenReturn(5L);
        
        when(ticketRepository.countByType(any())).thenReturn(50L);
        when(ticketRepository.countByPriority(any())).thenReturn(33L);
        when(ticketRepository.calculateAverageApprovalTimeHours(null, null)).thenReturn(24.0);
        when(ticketRepository.calculateAverageCompletionTimeHours(null, null)).thenReturn(48.0);
        
        // When
        TicketMetricsDTO metrics = ticketMetricsService.generateMetrics(null, null);
        
        // Then
        assertThat(metrics.getApprovalRate()).isEqualTo(70.0);
        assertThat(metrics.getRejectionRate()).isEqualTo(20.0);
    }
    
    @Test
    @DisplayName("Should complete metrics calculation within performance requirement")
    void shouldCompleteMetricsCalculationWithinPerformanceRequirement() {
        // Given
        when(ticketRepository.count()).thenReturn(10000L);
        when(ticketRepository.countByStatus(any())).thenReturn(2000L);
        when(ticketRepository.countByType(any())).thenReturn(5000L);
        when(ticketRepository.countByPriority(any())).thenReturn(3333L);
        when(ticketRepository.calculateAverageApprovalTimeHours(null, null)).thenReturn(24.0);
        when(ticketRepository.calculateAverageCompletionTimeHours(null, null)).thenReturn(48.0);
        
        // When
        long startTime = System.currentTimeMillis();
        TicketMetricsDTO metrics = ticketMetricsService.generateMetrics(null, null);
        long duration = System.currentTimeMillis() - startTime;
        
        // Then
        assertThat(metrics).isNotNull();
        // Performance requirement: complete within 5 seconds (Requirement 15.5)
        assertThat(duration).isLessThan(5000);
    }
    
    private Ticket createTestTicket(TicketStatus status) {
        Ticket ticket = new Ticket();
        ticket.setId(UUID.randomUUID());
        ticket.setTicketNumber("TKT-2024-00001");
        ticket.setType(TicketType.ALLOCATION);
        ticket.setStatus(status);
        ticket.setPriority(TicketPriority.STANDARD);
        ticket.setAssetId(UUID.randomUUID());
        ticket.setAssetName("Test Asset");
        ticket.setAssetSerialNumber("SN-001");
        ticket.setRequesterId(UUID.randomUUID());
        ticket.setRequesterName("Test User");
        ticket.setRequesterEmail("test@example.com");
        ticket.setRequestReason("Test reason for allocation");
        ticket.setCreatedAt(LocalDateTime.now());
        ticket.setUpdatedAt(LocalDateTime.now());
        return ticket;
    }
}
