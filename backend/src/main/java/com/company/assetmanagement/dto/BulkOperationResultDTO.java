package com.company.assetmanagement.dto;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Data Transfer Object for bulk operation results.
 * Used to return the results of bulk ticket operations (approve, reject, etc.).
 * 
 * Provides:
 * - Total number of tickets processed
 * - Number of successful operations
 * - Number of failed operations
 * - Details of any failures (ticket ID and error message)
 * 
 * Supports Requirement 18.4: Return success and failure counts with details of any failures.
 */
public class BulkOperationResultDTO {
    
    private Integer totalProcessed;
    private Integer successCount;
    private Integer failureCount;
    private List<FailureDetail> failures;
    
    // Constructors
    public BulkOperationResultDTO() {
        this.totalProcessed = 0;
        this.successCount = 0;
        this.failureCount = 0;
        this.failures = new ArrayList<>();
    }
    
    public BulkOperationResultDTO(Integer totalProcessed, Integer successCount, Integer failureCount) {
        this.totalProcessed = totalProcessed;
        this.successCount = successCount;
        this.failureCount = failureCount;
        this.failures = new ArrayList<>();
    }
    
    // Getters and Setters
    public Integer getTotalProcessed() {
        return totalProcessed;
    }
    
    public void setTotalProcessed(Integer totalProcessed) {
        this.totalProcessed = totalProcessed;
    }
    
    public Integer getSuccessCount() {
        return successCount;
    }
    
    public void setSuccessCount(Integer successCount) {
        this.successCount = successCount;
    }
    
    public Integer getFailureCount() {
        return failureCount;
    }
    
    public void setFailureCount(Integer failureCount) {
        this.failureCount = failureCount;
    }
    
    public List<FailureDetail> getFailures() {
        return failures;
    }
    
    public void setFailures(List<FailureDetail> failures) {
        this.failures = failures;
    }
    
    // Helper methods
    
    /**
     * Add a failure detail to the result.
     * 
     * @param ticketId the ID of the ticket that failed
     * @param ticketNumber the ticket number for display
     * @param errorMessage the error message explaining the failure
     */
    public void addFailure(UUID ticketId, String ticketNumber, String errorMessage) {
        this.failures.add(new FailureDetail(ticketId, ticketNumber, errorMessage));
        this.failureCount++;
    }
    
    /**
     * Increment the success count.
     */
    public void incrementSuccess() {
        this.successCount++;
    }
    
    /**
     * Check if all operations were successful.
     * 
     * @return true if no failures occurred
     */
    public boolean isAllSuccessful() {
        return failureCount == 0;
    }
    
    /**
     * Check if any operations failed.
     * 
     * @return true if at least one failure occurred
     */
    public boolean hasFailures() {
        return failureCount > 0;
    }
    
    /**
     * Inner class representing a single failure detail.
     */
    public static class FailureDetail {
        private UUID ticketId;
        private String ticketNumber;
        private String errorMessage;
        
        public FailureDetail() {
        }
        
        public FailureDetail(UUID ticketId, String ticketNumber, String errorMessage) {
            this.ticketId = ticketId;
            this.ticketNumber = ticketNumber;
            this.errorMessage = errorMessage;
        }
        
        public UUID getTicketId() {
            return ticketId;
        }
        
        public void setTicketId(UUID ticketId) {
            this.ticketId = ticketId;
        }
        
        public String getTicketNumber() {
            return ticketNumber;
        }
        
        public void setTicketNumber(String ticketNumber) {
            this.ticketNumber = ticketNumber;
        }
        
        public String getErrorMessage() {
            return errorMessage;
        }
        
        public void setErrorMessage(String errorMessage) {
            this.errorMessage = errorMessage;
        }
    }
}
