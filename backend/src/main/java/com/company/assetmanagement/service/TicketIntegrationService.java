package com.company.assetmanagement.service;

import com.company.assetmanagement.model.Ticket;

/**
 * Service interface for integrating ticket operations with Module 3 (Allocation Management).
 * This service acts as a bridge between the Ticket Management module and the Allocation Management module.
 * 
 * Responsibilities:
 * - Execute asset allocation operations for approved allocation tickets
 * - Execute asset de-allocation operations for approved de-allocation tickets
 * - Validate asset availability before allocation
 * - Handle integration errors gracefully
 * - Map ticket data to allocation service requests
 * 
 * Requirements: 4.1-4.5, 14.1-14.5
 */
public interface TicketIntegrationService {
    
    /**
     * Execute the allocation operation for an approved allocation ticket.
     * This method calls the AllocationService to assign the asset to the specified user or location.
     * 
     * Requirement 4.1: When completing an APPROVED Allocation_Ticket, invoke Allocation_Service to assign the Asset.
     * Requirement 14.1: Invoke Allocation_Service.assignAsset with the Asset and assignment details.
     * Requirement 14.4: Pass the Approver identifier to the Allocation_Service as the User performing the allocation.
     * 
     * @param ticket the allocation ticket to execute
     * @param userId the ID of the user completing the ticket (typically the approver)
     * @throws IllegalArgumentException if ticket is not an allocation ticket or not in APPROVED status
     * @throws AllocationFailedException if the allocation operation fails
     */
    void executeAllocation(Ticket ticket, String userId);
    
    /**
     * Execute the de-allocation operation for an approved de-allocation ticket.
     * This method calls the AllocationService to remove the asset assignment.
     * 
     * Requirement 4.2: When completing an APPROVED Deallocation_Ticket, invoke Allocation_Service to remove the Asset assignment.
     * Requirement 14.2: Invoke Allocation_Service.deallocateAsset with the Asset identifier.
     * Requirement 14.4: Pass the Approver identifier to the Allocation_Service as the User performing the de-allocation.
     * 
     * @param ticket the de-allocation ticket to execute
     * @param userId the ID of the user completing the ticket (typically the approver)
     * @throws IllegalArgumentException if ticket is not a de-allocation ticket or not in APPROVED status
     * @throws AllocationFailedException if the de-allocation operation fails
     */
    void executeDeallocation(Ticket ticket, String userId);
    
    /**
     * Validate that an asset is available for allocation.
     * This method checks if the asset exists and is in a state that allows allocation.
     * 
     * Requirement 1.3: Validate the referenced Asset exists and is available for allocation.
     * Requirement 1.4: Prevent creation of Allocation_Tickets for Assets with status RETIRED.
     * 
     * @param assetId the ID of the asset to validate
     * @return true if the asset is available for allocation, false otherwise
     */
    boolean validateAssetAvailability(String assetId);
    
    /**
     * Validate that an asset is currently assigned and can be de-allocated.
     * This method checks if the asset exists and has an active assignment.
     * 
     * Requirement 2.3: Validate the referenced Asset exists and is currently assigned.
     * Requirement 2.4: Prevent creation of Deallocation_Tickets for Assets that are not currently assigned.
     * 
     * @param assetId the ID of the asset to validate
     * @return true if the asset is currently assigned and can be de-allocated, false otherwise
     */
    boolean validateAssetAssignment(String assetId);
}
