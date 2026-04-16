package com.company.assetmanagement.dto;

import java.util.Map;

/**
 * DTO for ticket metrics and analytics.
 * Provides aggregated statistics about ticket processing and performance.
 * 
 * Validates Requirements: 10.1-10.7
 */
public class TicketMetricsDTO {
    
    private Long totalTickets;
    private Map<String, Long> ticketsByStatus;
    private Map<String, Long> ticketsByType;
    private Map<String, Long> ticketsByPriority;
    private Double averageApprovalTimeHours;
    private Double averageCompletionTimeHours;
    private Double approvalRate;
    private Double rejectionRate;
    
    // Constructors
    public TicketMetricsDTO() {
    }
    
    public TicketMetricsDTO(Long totalTickets, 
                           Map<String, Long> ticketsByStatus,
                           Map<String, Long> ticketsByType,
                           Map<String, Long> ticketsByPriority,
                           Double averageApprovalTimeHours,
                           Double averageCompletionTimeHours,
                           Double approvalRate,
                           Double rejectionRate) {
        this.totalTickets = totalTickets;
        this.ticketsByStatus = ticketsByStatus;
        this.ticketsByType = ticketsByType;
        this.ticketsByPriority = ticketsByPriority;
        this.averageApprovalTimeHours = averageApprovalTimeHours;
        this.averageCompletionTimeHours = averageCompletionTimeHours;
        this.approvalRate = approvalRate;
        this.rejectionRate = rejectionRate;
    }
    
    // Getters and Setters
    public Long getTotalTickets() {
        return totalTickets;
    }
    
    public void setTotalTickets(Long totalTickets) {
        this.totalTickets = totalTickets;
    }
    
    public Map<String, Long> getTicketsByStatus() {
        return ticketsByStatus;
    }
    
    public void setTicketsByStatus(Map<String, Long> ticketsByStatus) {
        this.ticketsByStatus = ticketsByStatus;
    }
    
    public Map<String, Long> getTicketsByType() {
        return ticketsByType;
    }
    
    public void setTicketsByType(Map<String, Long> ticketsByType) {
        this.ticketsByType = ticketsByType;
    }
    
    public Map<String, Long> getTicketsByPriority() {
        return ticketsByPriority;
    }
    
    public void setTicketsByPriority(Map<String, Long> ticketsByPriority) {
        this.ticketsByPriority = ticketsByPriority;
    }
    
    public Double getAverageApprovalTimeHours() {
        return averageApprovalTimeHours;
    }
    
    public void setAverageApprovalTimeHours(Double averageApprovalTimeHours) {
        this.averageApprovalTimeHours = averageApprovalTimeHours;
    }
    
    public Double getAverageCompletionTimeHours() {
        return averageCompletionTimeHours;
    }
    
    public void setAverageCompletionTimeHours(Double averageCompletionTimeHours) {
        this.averageCompletionTimeHours = averageCompletionTimeHours;
    }
    
    public Double getApprovalRate() {
        return approvalRate;
    }
    
    public void setApprovalRate(Double approvalRate) {
        this.approvalRate = approvalRate;
    }
    
    public Double getRejectionRate() {
        return rejectionRate;
    }
    
    public void setRejectionRate(Double rejectionRate) {
        this.rejectionRate = rejectionRate;
    }
    
    @Override
    public String toString() {
        return "TicketMetricsDTO{" +
                "totalTickets=" + totalTickets +
                ", ticketsByStatus=" + ticketsByStatus +
                ", ticketsByType=" + ticketsByType +
                ", ticketsByPriority=" + ticketsByPriority +
                ", averageApprovalTimeHours=" + averageApprovalTimeHours +
                ", averageCompletionTimeHours=" + averageCompletionTimeHours +
                ", approvalRate=" + approvalRate +
                ", rejectionRate=" + rejectionRate +
                '}';
    }
}
