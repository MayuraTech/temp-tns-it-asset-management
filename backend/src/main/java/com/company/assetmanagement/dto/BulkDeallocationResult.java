package com.company.assetmanagement.dto;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * DTO representing the result of a bulk deallocation operation.
 * 
 * Contains lists of successfully deallocated assets and failed deallocations
 * with error details. This allows clients to see which operations succeeded
 * and which failed in a bulk operation.
 */
public class BulkDeallocationResult {
    
    private List<DeallocationSuccess> successfulDeallocations;
    private List<DeallocationFailure> failedDeallocations;
    private int totalRequested;
    private int successCount;
    private int failureCount;
    
    public BulkDeallocationResult() {
        this.successfulDeallocations = new ArrayList<>();
        this.failedDeallocations = new ArrayList<>();
        this.totalRequested = 0;
        this.successCount = 0;
        this.failureCount = 0;
    }
    
    /**
     * Represents a successful deallocation in a bulk operation.
     */
    public static class DeallocationSuccess {
        private UUID assetId;
        private AssignmentDTO closedAssignment;
        
        public DeallocationSuccess() {}
        
        public DeallocationSuccess(UUID assetId, AssignmentDTO closedAssignment) {
            this.assetId = assetId;
            this.closedAssignment = closedAssignment;
        }
        
        public UUID getAssetId() {
            return assetId;
        }
        
        public void setAssetId(UUID assetId) {
            this.assetId = assetId;
        }
        
        public AssignmentDTO getClosedAssignment() {
            return closedAssignment;
        }
        
        public void setClosedAssignment(AssignmentDTO closedAssignment) {
            this.closedAssignment = closedAssignment;
        }
    }
    
    /**
     * Represents a failed deallocation in a bulk operation.
     */
    public static class DeallocationFailure {
        private UUID assetId;
        private String errorType;
        private String errorMessage;
        
        public DeallocationFailure() {}
        
        public DeallocationFailure(UUID assetId, String errorType, String errorMessage) {
            this.assetId = assetId;
            this.errorType = errorType;
            this.errorMessage = errorMessage;
        }
        
        public UUID getAssetId() {
            return assetId;
        }
        
        public void setAssetId(UUID assetId) {
            this.assetId = assetId;
        }
        
        public String getErrorType() {
            return errorType;
        }
        
        public void setErrorType(String errorType) {
            this.errorType = errorType;
        }
        
        public String getErrorMessage() {
            return errorMessage;
        }
        
        public void setErrorMessage(String errorMessage) {
            this.errorMessage = errorMessage;
        }
    }
    
    // Getters and setters
    
    public List<DeallocationSuccess> getSuccessfulDeallocations() {
        return successfulDeallocations;
    }
    
    public void setSuccessfulDeallocations(List<DeallocationSuccess> successfulDeallocations) {
        this.successfulDeallocations = successfulDeallocations;
    }
    
    public List<DeallocationFailure> getFailedDeallocations() {
        return failedDeallocations;
    }
    
    public void setFailedDeallocations(List<DeallocationFailure> failedDeallocations) {
        this.failedDeallocations = failedDeallocations;
    }
    
    public int getTotalRequested() {
        return totalRequested;
    }
    
    public void setTotalRequested(int totalRequested) {
        this.totalRequested = totalRequested;
    }
    
    public int getSuccessCount() {
        return successCount;
    }
    
    public void setSuccessCount(int successCount) {
        this.successCount = successCount;
    }
    
    public int getFailureCount() {
        return failureCount;
    }
    
    public void setFailureCount(int failureCount) {
        this.failureCount = failureCount;
    }
    
    /**
     * Adds a successful deallocation to the result.
     *
     * @param assetId the ID of the deallocated asset
     * @param closedAssignment the closed assignment DTO
     */
    public void addSuccess(UUID assetId, AssignmentDTO closedAssignment) {
        this.successfulDeallocations.add(new DeallocationSuccess(assetId, closedAssignment));
        this.successCount++;
    }
    
    /**
     * Adds a failed deallocation to the result.
     *
     * @param assetId the ID of the asset that failed to deallocate
     * @param errorType the type of error that occurred
     * @param errorMessage the error message
     */
    public void addFailure(UUID assetId, String errorType, String errorMessage) {
        this.failedDeallocations.add(new DeallocationFailure(assetId, errorType, errorMessage));
        this.failureCount++;
    }
}
