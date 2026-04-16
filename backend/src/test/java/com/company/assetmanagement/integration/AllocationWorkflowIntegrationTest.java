package com.company.assetmanagement.integration;

import com.company.assetmanagement.dto.AssignmentDTO;
import com.company.assetmanagement.dto.AssignmentHistoryDTO;
import com.company.assetmanagement.dto.AssignmentRequest;
import com.company.assetmanagement.exception.AssetAlreadyAssignedException;
import com.company.assetmanagement.model.*;
import com.company.assetmanagement.repository.AssetRepository;
import com.company.assetmanagement.repository.AssignmentHistoryRepository;
import com.company.assetmanagement.repository.AuditLogRepository;
import com.company.assetmanagement.repository.UserRepository;
import com.company.assetmanagement.service.AllocationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Integration tests for complete allocation workflows.
 * 
 * **Validates: Phase 11.1 - Backend Integration Tests**
 * 
 * Tests complete workflows with real database interactions:
 * - Assignment workflow (create asset, assign, verify)
 * - Deallocation workflow (assign, deallocate, verify)
 * - Reassignment workflow (assign, reassign, verify)
 * - Concurrent assignment handling
 */
@SpringBootTest
@Transactional
@ActiveProfiles("test")
class AllocationWorkflowIntegrationTest {
    
    @Autowired
    private AllocationService allocationService;
    
    @Autowired
    private AssetRepository assetRepository;
    
    @Autowired
    private AssignmentHistoryRepository assignmentHistoryRepository;
    
    @Autowired
    private AuditLogRepository auditLogRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    private User testUser;
    private String userId;
    
    @BeforeEach
    void setUp() {
        // Create test user
        testUser = new User();
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPasswordHash("hashed");
        testUser = userRepository.save(testUser);
        userId = testUser.getId().toString();
    }
    
    /**
     * Test complete assignment workflow:
     * 1. Create asset
     * 2. Assign to user
     * 3. Verify assignment record created
     * 4. Verify asset fields updated
     * 5. Verify audit log entry
     */
    @Test
    @DisplayName("Complete assignment workflow - create asset, assign to user, verify all changes")
    void completeAssignmentWorkflow() {
        // Step 1: Create asset
        Asset asset = new Asset();
        asset.setAssetType(AssetType.SERVER);
        asset.setName("Integration Test Server");
        asset.setSerialNumber("INT-SRV-" + UUID.randomUUID().toString().substring(0, 8));
        asset.setAcquisitionDate(LocalDate.now());
        asset.setStatus(LifecycleStatus.IN_USE);
        asset = assetRepository.save(asset);
        
        // Step 2: Assign to user
        AssignmentRequest request = new AssignmentRequest();
        request.setAssignmentType(AssignmentType.USER);
        request.setAssignedTo("John Doe");
        request.setAssignedUserEmail("john.doe@example.com");
        
        AssignmentDTO assignment = allocationService.assignToUser(userId, asset.getId(), request);
        
        // Step 3: Verify assignment record created
        assertThat(assignment).isNotNull();
        assertThat(assignment.getId()).isNotNull();
        assertThat(assignment.getAssetId()).isEqualTo(asset.getId());
        assertThat(assignment.getAssignmentType()).isEqualTo(AssignmentType.USER);
        assertThat(assignment.getAssignedTo()).isEqualTo("John Doe");
        assertThat(assignment.getAssignedBy()).isEqualTo(testUser.getId());
        assertThat(assignment.getAssignedAt()).isNotNull();
        assertThat(assignment.getUnassignedAt()).isNull();
        assertThat(assignment.isActive()).isTrue();
        
        // Verify assignment exists in database
        AssignmentHistory savedAssignment = assignmentHistoryRepository.findById(assignment.getId())
            .orElseThrow(() -> new AssertionError("Assignment not found in database"));
        assertThat(savedAssignment.getAssetId()).isEqualTo(asset.getId());
        assertThat(savedAssignment.getAssignmentType()).isEqualTo(AssignmentType.USER);
        assertThat(savedAssignment.getAssignedTo()).isEqualTo("John Doe");
        
        // Step 4: Verify asset fields updated
        Asset updatedAsset = assetRepository.findById(asset.getId())
            .orElseThrow(() -> new AssertionError("Asset not found"));
        assertThat(updatedAsset.getAssignedUser()).isEqualTo("John Doe");
        assertThat(updatedAsset.getAssignedUserEmail()).isEqualTo("john.doe@example.com");
        assertThat(updatedAsset.getAssignmentDate()).isNotNull();
        assertThat(updatedAsset.getLocation()).isNull();
        assertThat(updatedAsset.getLocationUpdateDate()).isNull();
        
        // Step 5: Verify audit log entry
        List<AuditLog> auditLogs = auditLogRepository.findByResourceId(assignment.getId().toString());
        assertThat(auditLogs).isNotEmpty();
        
        AuditLog auditLog = auditLogs.get(0);
        assertThat(auditLog.getActionType()).isEqualTo(Action.CREATE_ASSET);
        assertThat(auditLog.getResourceType()).isEqualTo("ASSIGNMENT");
        assertThat(auditLog.getResourceId()).isEqualTo(assignment.getId().toString());
        assertThat(auditLog.getUserId()).isEqualTo(testUser.getId());
    }
    
    /**
     * Test complete deallocation workflow:
     * 1. Assign asset
     * 2. Deallocate asset
     * 3. Verify assignment closed
     * 4. Verify asset fields cleared
     * 
     * **Validates: Requirement 3 - Deallocate Asset**
     * **Validates: Property 20 - Deallocation Completeness**
     * 
     * Ensures that deallocation properly closes the assignment record and clears
     * all asset assignment fields (AssignedUser, AssignedUserEmail, Location,
     * AssignmentDate, LocationUpdateDate).
     */
    @Test
    @DisplayName("Complete deallocation workflow - assign, deallocate, verify all changes")
    void completeDeallocationWorkflow() {
        // Step 1: Create and assign asset
        Asset asset = createTestAsset();
        
        AssignmentRequest request = new AssignmentRequest();
        request.setAssignmentType(AssignmentType.USER);
        request.setAssignedTo("Jane Smith");
        request.setAssignedUserEmail("jane.smith@example.com");
        
        AssignmentDTO assignment = allocationService.assignToUser(userId, asset.getId(), request);
        UUID assignmentId = assignment.getId();
        
        // Verify asset is assigned before deallocation
        Asset assignedAsset = assetRepository.findById(asset.getId())
            .orElseThrow(() -> new AssertionError("Asset not found"));
        assertThat(assignedAsset.getAssignedUser()).isEqualTo("Jane Smith");
        assertThat(assignedAsset.getAssignedUserEmail()).isEqualTo("jane.smith@example.com");
        assertThat(assignedAsset.getAssignmentDate()).isNotNull();
        
        // Step 2: Deallocate asset
        allocationService.deallocate(userId, asset.getId());
        
        // Step 3: Verify assignment closed
        AssignmentHistory closedAssignment = assignmentHistoryRepository.findById(assignmentId)
            .orElseThrow(() -> new AssertionError("Assignment not found"));
        assertThat(closedAssignment.getUnassignedAt()).isNotNull();
        assertThat(closedAssignment.getUnassignedAt()).isAfter(closedAssignment.getAssignedAt());
        
        // Step 4: Verify asset fields cleared
        // **This validates Requirement 3.2 and 3.3 - all assignment fields must be cleared**
        Asset deallocatedAsset = assetRepository.findById(asset.getId())
            .orElseThrow(() -> new AssertionError("Asset not found"));
        
        // Verify user assignment fields are cleared
        assertThat(deallocatedAsset.getAssignedUser())
            .as("AssignedUser field should be cleared after deallocation")
            .isNull();
        assertThat(deallocatedAsset.getAssignedUserEmail())
            .as("AssignedUserEmail field should be cleared after deallocation")
            .isNull();
        assertThat(deallocatedAsset.getAssignmentDate())
            .as("AssignmentDate field should be cleared after deallocation")
            .isNull();
        
        // Verify location assignment fields are cleared
        assertThat(deallocatedAsset.getLocation())
            .as("Location field should be cleared after deallocation")
            .isNull();
        assertThat(deallocatedAsset.getLocationUpdateDate())
            .as("LocationUpdateDate field should be cleared after deallocation")
            .isNull();
        
        // Verify audit log entry for deallocation
        List<AuditLog> auditLogs = auditLogRepository.findByResourceId(assignmentId.toString());
        assertThat(auditLogs).hasSizeGreaterThanOrEqualTo(2); // CREATE and DELETE
        
        // Find the DELETE audit log
        boolean hasDeleteLog = auditLogs.stream()
            .anyMatch(log -> log.getActionType() == Action.DELETE_ASSET);
        assertThat(hasDeleteLog)
            .as("Deallocation should be logged to audit service")
            .isTrue();
    }
    
    /**
     * Test complete reassignment workflow:
     * 1. Assign asset to user A
     * 2. Reassign to user B
     * 3. Verify old assignment closed
     * 4. Verify new assignment created
     */
    @Test
    @DisplayName("Complete reassignment workflow - assign to A, reassign to B, verify both records")
    void completeReassignmentWorkflow() {
        // Step 1: Create and assign asset to user A
        Asset asset = createTestAsset();
        
        AssignmentRequest requestA = new AssignmentRequest();
        requestA.setAssignmentType(AssignmentType.USER);
        requestA.setAssignedTo("User A");
        requestA.setAssignedUserEmail("usera@example.com");
        
        AssignmentDTO assignmentA = allocationService.assignToUser(userId, asset.getId(), requestA);
        UUID assignmentAId = assignmentA.getId();
        
        // Step 2: Reassign to user B
        AssignmentRequest requestB = new AssignmentRequest();
        requestB.setAssignmentType(AssignmentType.USER);
        requestB.setAssignedTo("User B");
        requestB.setAssignedUserEmail("userb@example.com");
        
        AssignmentDTO assignmentB = allocationService.reassign(userId, asset.getId(), requestB);
        
        // Step 3: Verify old assignment closed
        AssignmentHistory closedAssignment = assignmentHistoryRepository.findById(assignmentAId)
            .orElseThrow(() -> new AssertionError("Old assignment not found"));
        assertThat(closedAssignment.getUnassignedAt()).isNotNull();
        assertThat(closedAssignment.getAssignedTo()).isEqualTo("User A");
        
        // Step 4: Verify new assignment created
        assertThat(assignmentB.getId()).isNotEqualTo(assignmentAId);
        assertThat(assignmentB.getAssignedTo()).isEqualTo("User B");
        assertThat(assignmentB.getUnassignedAt()).isNull();
        assertThat(assignmentB.isActive()).isTrue();
        
        // Verify asset fields updated to new assignment
        Asset updatedAsset = assetRepository.findById(asset.getId())
            .orElseThrow(() -> new AssertionError("Asset not found"));
        assertThat(updatedAsset.getAssignedUser()).isEqualTo("User B");
        assertThat(updatedAsset.getAssignedUserEmail()).isEqualTo("userb@example.com");
        
        // Verify assignment history shows both assignments
        Page<AssignmentHistoryDTO> history = allocationService.getAssignmentHistory(
            userId, asset.getId(), PageRequest.of(0, 10)
        );
        assertThat(history.getContent()).hasSize(2);
        
        // Most recent should be first (User B)
        assertThat(history.getContent().get(0).getAssignedTo()).isEqualTo("User B");
        assertThat(history.getContent().get(0).getUnassignedAt()).isNull();
        
        // Older assignment should be second (User A)
        assertThat(history.getContent().get(1).getAssignedTo()).isEqualTo("User A");
        assertThat(history.getContent().get(1).getUnassignedAt()).isNotNull();
    }
    
    /**
     * Test location deallocation workflow to ensure location fields are cleared
     */
    @Test
    @DisplayName("Location deallocation workflow - verify location fields cleared")
    void locationDeallocationWorkflow() {
        // Create and assign asset to location
        Asset asset = createTestAsset();
        
        AssignmentRequest request = new AssignmentRequest();
        request.setAssignmentType(AssignmentType.LOCATION);
        request.setAssignedTo("Warehouse C");
        
        AssignmentDTO assignment = allocationService.assignToLocation(userId, asset.getId(), request);
        UUID assignmentId = assignment.getId();
        
        // Verify location is assigned
        Asset assignedAsset = assetRepository.findById(asset.getId())
            .orElseThrow(() -> new AssertionError("Asset not found"));
        assertThat(assignedAsset.getLocation()).isEqualTo("Warehouse C");
        assertThat(assignedAsset.getLocationUpdateDate()).isNotNull();
        
        // Deallocate asset
        allocationService.deallocate(userId, asset.getId());
        
        // Verify assignment closed
        AssignmentHistory closedAssignment = assignmentHistoryRepository.findById(assignmentId)
            .orElseThrow(() -> new AssertionError("Assignment not found"));
        assertThat(closedAssignment.getUnassignedAt()).isNotNull();
        
        // Verify location fields cleared
        Asset deallocatedAsset = assetRepository.findById(asset.getId())
            .orElseThrow(() -> new AssertionError("Asset not found"));
        assertThat(deallocatedAsset.getLocation())
            .as("Location field should be cleared after deallocation")
            .isNull();
        assertThat(deallocatedAsset.getLocationUpdateDate())
            .as("LocationUpdateDate field should be cleared after deallocation")
            .isNull();
    }
    
    /**
     * Test location assignment workflow
     */
    @Test
    @DisplayName("Complete location assignment workflow")
    void completeLocationAssignmentWorkflow() {
        // Create asset
        Asset asset = createTestAsset();
        
        // Assign to location
        AssignmentRequest request = new AssignmentRequest();
        request.setAssignmentType(AssignmentType.LOCATION);
        request.setAssignedTo("Data Center A");
        
        AssignmentDTO assignment = allocationService.assignToLocation(userId, asset.getId(), request);
        
        // Verify assignment record
        assertThat(assignment.getAssignmentType()).isEqualTo(AssignmentType.LOCATION);
        assertThat(assignment.getAssignedTo()).isEqualTo("Data Center A");
        
        // Verify asset fields
        Asset updatedAsset = assetRepository.findById(asset.getId())
            .orElseThrow(() -> new AssertionError("Asset not found"));
        assertThat(updatedAsset.getLocation()).isEqualTo("Data Center A");
        assertThat(updatedAsset.getLocationUpdateDate()).isNotNull();
        assertThat(updatedAsset.getAssignedUser()).isNull();
        assertThat(updatedAsset.getAssignedUserEmail()).isNull();
    }
    
    /**
     * Test reassignment from user to location
     */
    @Test
    @DisplayName("Reassignment from user to location")
    void reassignmentFromUserToLocation() {
        // Create and assign to user
        Asset asset = createTestAsset();
        
        AssignmentRequest userRequest = new AssignmentRequest();
        userRequest.setAssignmentType(AssignmentType.USER);
        userRequest.setAssignedTo("John Doe");
        userRequest.setAssignedUserEmail("john@example.com");
        
        allocationService.assignToUser(userId, asset.getId(), userRequest);
        
        // Reassign to location
        AssignmentRequest locationRequest = new AssignmentRequest();
        locationRequest.setAssignmentType(AssignmentType.LOCATION);
        locationRequest.setAssignedTo("Storage Room B");
        
        AssignmentDTO newAssignment = allocationService.reassign(userId, asset.getId(), locationRequest);
        
        // Verify new assignment is location type
        assertThat(newAssignment.getAssignmentType()).isEqualTo(AssignmentType.LOCATION);
        assertThat(newAssignment.getAssignedTo()).isEqualTo("Storage Room B");
        
        // Verify asset fields updated correctly
        Asset updatedAsset = assetRepository.findById(asset.getId())
            .orElseThrow(() -> new AssertionError("Asset not found"));
        assertThat(updatedAsset.getLocation()).isEqualTo("Storage Room B");
        assertThat(updatedAsset.getLocationUpdateDate()).isNotNull();
        assertThat(updatedAsset.getAssignedUser()).isNull();
        assertThat(updatedAsset.getAssignedUserEmail()).isNull();
        assertThat(updatedAsset.getAssignmentDate()).isNull();
    }
    
    /**
     * Test concurrent assignment handling:
     * 1. Simulate concurrent requests for the same asset
     * 2. Verify only one succeeds
     * 3. Verify others receive AssetAlreadyAssignedException
     * 
     * **Validates: Requirement 11 - Handle Concurrent Assignment Requests**
     * 
     * Uses pessimistic locking to ensure only one assignment succeeds when
     * multiple concurrent requests are made for the same asset.
     */
    @Test
    @DisplayName("Concurrent assignment handling - only one succeeds, others fail with conflict error")
    void concurrentAssignmentHandling() throws InterruptedException, ExecutionException {
        // Create test asset
        Asset asset = createTestAsset();
        UUID assetId = asset.getId();
        
        // Create multiple assignment requests
        int concurrentRequests = 5;
        ExecutorService executorService = Executors.newFixedThreadPool(concurrentRequests);
        List<Future<AssignmentResult>> futures = new ArrayList<>();
        
        // Submit concurrent assignment requests
        for (int i = 0; i < concurrentRequests; i++) {
            final int requestNumber = i;
            Future<AssignmentResult> future = executorService.submit(() -> {
                try {
                    AssignmentRequest request = new AssignmentRequest();
                    request.setAssignmentType(AssignmentType.USER);
                    request.setAssignedTo("User " + requestNumber);
                    request.setAssignedUserEmail("user" + requestNumber + "@example.com");
                    
                    AssignmentDTO assignment = allocationService.assignToUser(userId, assetId, request);
                    return new AssignmentResult(true, assignment, null);
                } catch (AssetAlreadyAssignedException e) {
                    return new AssignmentResult(false, null, e);
                } catch (Exception e) {
                    return new AssignmentResult(false, null, e);
                }
            });
            futures.add(future);
        }
        
        // Wait for all requests to complete
        executorService.shutdown();
        executorService.awaitTermination(10, TimeUnit.SECONDS);
        
        // Collect results
        int successCount = 0;
        int conflictCount = 0;
        AssignmentDTO successfulAssignment = null;
        
        for (Future<AssignmentResult> future : futures) {
            AssignmentResult result = future.get();
            if (result.success) {
                successCount++;
                successfulAssignment = result.assignment;
            } else if (result.exception instanceof AssetAlreadyAssignedException) {
                conflictCount++;
            }
        }
        
        // Verify only one assignment succeeded
        assertThat(successCount).isEqualTo(1);
        assertThat(conflictCount).isEqualTo(concurrentRequests - 1);
        assertThat(successfulAssignment).isNotNull();
        
        // Verify the successful assignment is persisted
        AssignmentHistory savedAssignment = assignmentHistoryRepository.findById(successfulAssignment.getId())
            .orElseThrow(() -> new AssertionError("Successful assignment not found in database"));
        assertThat(savedAssignment.getAssetId()).isEqualTo(assetId);
        assertThat(savedAssignment.getUnassignedAt()).isNull();
        
        // Verify asset is assigned
        Asset assignedAsset = assetRepository.findById(assetId)
            .orElseThrow(() -> new AssertionError("Asset not found"));
        assertThat(assignedAsset.getAssignedUser()).isNotNull();
        assertThat(assignedAsset.getAssignedUserEmail()).isNotNull();
        
        // Verify only one assignment record exists for this asset
        Page<AssignmentHistoryDTO> history = allocationService.getAssignmentHistory(
            userId, assetId, PageRequest.of(0, 10)
        );
        assertThat(history.getContent()).hasSize(1);
        assertThat(history.getContent().get(0).getId()).isEqualTo(successfulAssignment.getId());
    }
    
    /**
     * Helper class to capture assignment results from concurrent operations
     */
    private static class AssignmentResult {
        final boolean success;
        final AssignmentDTO assignment;
        final Exception exception;
        
        AssignmentResult(boolean success, AssignmentDTO assignment, Exception exception) {
            this.success = success;
            this.assignment = assignment;
            this.exception = exception;
        }
    }
    
    // Helper methods
    
    private Asset createTestAsset() {
        Asset asset = new Asset();
        asset.setAssetType(AssetType.SERVER);
        asset.setName("Test Asset");
        asset.setSerialNumber("TEST-" + UUID.randomUUID().toString().substring(0, 8));
        asset.setAcquisitionDate(LocalDate.now());
        asset.setStatus(LifecycleStatus.IN_USE);
        return assetRepository.save(asset);
    }
}
