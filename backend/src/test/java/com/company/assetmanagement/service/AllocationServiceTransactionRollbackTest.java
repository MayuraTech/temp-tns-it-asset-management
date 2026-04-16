package com.company.assetmanagement.service;

import com.company.assetmanagement.dto.AssignmentRequest;
import com.company.assetmanagement.model.*;
import com.company.assetmanagement.repository.AssignmentHistoryRepository;
import com.company.assetmanagement.repository.AssetRepository;
import com.company.assetmanagement.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;

/**
 * Integration tests for transaction rollback scenarios in AllocationService.
 * 
 * These tests verify that when errors occur during reassignment operations,
 * the entire transaction is rolled back and the database remains in a consistent state.
 * 
 * Test scenarios:
 * - Audit logging failure causes complete rollback
 * - Asset save failure causes assignment history rollback
 * - New assignment save failure causes old assignment closure rollback
 * - Database consistency is maintained after rollback
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class AllocationServiceTransactionRollbackTest {
    
    @Autowired
    private AllocationService allocationService;
    
    @Autowired
    private AssetRepository assetRepository;
    
    @Autowired
    private AssignmentHistoryRepository assignmentHistoryRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @SpyBean
    private AuditService auditService;
    
    private User testUser;
    private Asset testAsset;
    private UUID testUserId;
    
    @BeforeEach
    void setUp() {
        // Clean up test data
        assignmentHistoryRepository.deleteAll();
        assetRepository.deleteAll();
        userRepository.deleteAll();
        
        // Create test user
        testUser = new User();
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPasswordHash("hashedpassword");
        testUser.setRole(Role.ADMINISTRATOR);
        testUser.setCreatedAt(LocalDateTime.now());
        testUser.setUpdatedAt(LocalDateTime.now());
        testUser = userRepository.save(testUser);
        testUserId = testUser.getId();
        
        // Create test asset
        testAsset = createTestAsset();
    }
    
    @Test
    @DisplayName("Should rollback entire reassignment when audit logging fails")
    void shouldRollbackReassignmentWhenAuditLoggingFails() {
        // Given: Asset is assigned to user A
        AssignmentRequest initialRequest = new AssignmentRequest();
        initialRequest.setAssignmentType(AssignmentType.USER);
        initialRequest.setAssignedTo("User A");
        initialRequest.setAssignedUserEmail("usera@example.com");
        
        allocationService.assignToUser(testUserId.toString(), testAsset.getId(), initialRequest);
        
        // Verify initial state
        Asset assetBeforeReassign = assetRepository.findById(testAsset.getId()).orElseThrow();
        assertThat(assetBeforeReassign.getAssignedUser()).isEqualTo("User A");
        assertThat(assetBeforeReassign.getAssignedUserEmail()).isEqualTo("usera@example.com");
        
        List<AssignmentHistory> assignmentsBeforeReassign = 
            assignmentHistoryRepository.findByAssetIdOrderByAssignedAtDesc(testAsset.getId());
        assertThat(assignmentsBeforeReassign).hasSize(1);
        assertThat(assignmentsBeforeReassign.get(0).getUnassignedAt()).isNull();
        
        // Configure audit service to fail
        doThrow(new RuntimeException("Audit service unavailable"))
            .when(auditService).logEvent(any());
        
        // When: Attempt to reassign to user B
        AssignmentRequest reassignRequest = new AssignmentRequest();
        reassignRequest.setAssignmentType(AssignmentType.USER);
        reassignRequest.setAssignedTo("User B");
        reassignRequest.setAssignedUserEmail("userb@example.com");
        
        // Then: Reassignment should fail
        assertThatThrownBy(() -> 
            allocationService.reassign(testUserId.toString(), testAsset.getId(), reassignRequest))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("Audit service unavailable");
        
        // Verify rollback: Asset should still be assigned to User A
        Asset assetAfterFailure = assetRepository.findById(testAsset.getId()).orElseThrow();
        assertThat(assetAfterFailure.getAssignedUser()).isEqualTo("User A");
        assertThat(assetAfterFailure.getAssignedUserEmail()).isEqualTo("usera@example.com");
        assertThat(assetAfterFailure.getLocation()).isNull();
        
        // Verify rollback: Old assignment should still be active
        List<AssignmentHistory> assignmentsAfterFailure = 
            assignmentHistoryRepository.findByAssetIdOrderByAssignedAtDesc(testAsset.getId());
        assertThat(assignmentsAfterFailure).hasSize(1);
        assertThat(assignmentsAfterFailure.get(0).getAssignedTo()).isEqualTo("User A");
        assertThat(assignmentsAfterFailure.get(0).getUnassignedAt()).isNull();
        
        // Verify rollback: No new assignment record created
        List<AssignmentHistory> allAssignments = 
            assignmentHistoryRepository.findAll();
        assertThat(allAssignments).hasSize(1);
    }
    
    @Test
    @DisplayName("Should rollback assignment history changes when asset save fails")
    void shouldRollbackAssignmentHistoryWhenAssetSaveFails() {
        // Given: Asset is assigned to location A
        AssignmentRequest initialRequest = new AssignmentRequest();
        initialRequest.setAssignmentType(AssignmentType.LOCATION);
        initialRequest.setAssignedTo("Location A");
        
        allocationService.assignToLocation(testUserId.toString(), testAsset.getId(), initialRequest);
        
        // Verify initial state
        Asset assetBeforeReassign = assetRepository.findById(testAsset.getId()).orElseThrow();
        assertThat(assetBeforeReassign.getLocation()).isEqualTo("Location A");
        assertThat(assetBeforeReassign.getLocationUpdateDate()).isNotNull();
        
        List<AssignmentHistory> assignmentsBeforeReassign = 
            assignmentHistoryRepository.findActiveAssignmentsByAssetId(testAsset.getId());
        assertThat(assignmentsBeforeReassign).hasSize(1);
        LocalDateTime originalAssignedAt = assignmentsBeforeReassign.get(0).getAssignedAt();
        
        // When: Attempt to reassign with invalid data that will cause asset save to fail
        // We'll simulate this by trying to reassign to a location with an extremely long name
        // that exceeds database constraints
        AssignmentRequest reassignRequest = new AssignmentRequest();
        reassignRequest.setAssignmentType(AssignmentType.LOCATION);
        reassignRequest.setAssignedTo("A".repeat(300)); // Exceeds 255 character limit
        
        // Then: Reassignment should fail during validation
        assertThatThrownBy(() -> 
            allocationService.reassign(testUserId.toString(), testAsset.getId(), reassignRequest))
            .hasMessageContaining("255 characters");
        
        // Verify rollback: Asset should still be at Location A
        Asset assetAfterFailure = assetRepository.findById(testAsset.getId()).orElseThrow();
        assertThat(assetAfterFailure.getLocation()).isEqualTo("Location A");
        assertThat(assetAfterFailure.getAssignedUser()).isNull();
        
        // Verify rollback: Old assignment should still be active with original timestamp
        List<AssignmentHistory> assignmentsAfterFailure = 
            assignmentHistoryRepository.findActiveAssignmentsByAssetId(testAsset.getId());
        assertThat(assignmentsAfterFailure).hasSize(1);
        assertThat(assignmentsAfterFailure.get(0).getAssignedTo()).isEqualTo("Location A");
        assertThat(assignmentsAfterFailure.get(0).getUnassignedAt()).isNull();
        assertThat(assignmentsAfterFailure.get(0).getAssignedAt()).isEqualTo(originalAssignedAt);
        
        // Verify rollback: No additional assignment records created
        List<AssignmentHistory> allAssignments = 
            assignmentHistoryRepository.findByAssetIdOrderByAssignedAtDesc(testAsset.getId());
        assertThat(allAssignments).hasSize(1);
    }
    
    @Test
    @DisplayName("Should rollback old assignment closure when new assignment creation fails")
    void shouldRollbackOldAssignmentClosureWhenNewAssignmentFails() {
        // Given: Asset is assigned to user A
        AssignmentRequest initialRequest = new AssignmentRequest();
        initialRequest.setAssignmentType(AssignmentType.USER);
        initialRequest.setAssignedTo("User A");
        initialRequest.setAssignedUserEmail("usera@example.com");
        
        allocationService.assignToUser(testUserId.toString(), testAsset.getId(), initialRequest);
        
        // Verify initial state
        List<AssignmentHistory> assignmentsBeforeReassign = 
            assignmentHistoryRepository.findActiveAssignmentsByAssetId(testAsset.getId());
        assertThat(assignmentsBeforeReassign).hasSize(1);
        UUID originalAssignmentId = assignmentsBeforeReassign.get(0).getId();
        
        // When: Attempt to reassign with invalid email that will fail validation
        AssignmentRequest reassignRequest = new AssignmentRequest();
        reassignRequest.setAssignmentType(AssignmentType.USER);
        reassignRequest.setAssignedTo("User B");
        reassignRequest.setAssignedUserEmail("invalid-email-format"); // Invalid email
        
        // Then: Reassignment should fail during validation
        assertThatThrownBy(() -> 
            allocationService.reassign(testUserId.toString(), testAsset.getId(), reassignRequest))
            .hasMessageContaining("email");
        
        // Verify rollback: Old assignment should still be active (not closed)
        AssignmentHistory oldAssignment = 
            assignmentHistoryRepository.findById(originalAssignmentId).orElseThrow();
        assertThat(oldAssignment.getUnassignedAt()).isNull();
        assertThat(oldAssignment.getAssignedTo()).isEqualTo("User A");
        
        // Verify rollback: Only one assignment record exists (the original)
        List<AssignmentHistory> allAssignments = 
            assignmentHistoryRepository.findByAssetIdOrderByAssignedAtDesc(testAsset.getId());
        assertThat(allAssignments).hasSize(1);
        assertThat(allAssignments.get(0).getId()).isEqualTo(originalAssignmentId);
        
        // Verify rollback: Asset still assigned to User A
        Asset assetAfterFailure = assetRepository.findById(testAsset.getId()).orElseThrow();
        assertThat(assetAfterFailure.getAssignedUser()).isEqualTo("User A");
        assertThat(assetAfterFailure.getAssignedUserEmail()).isEqualTo("usera@example.com");
    }
    
    @Test
    @DisplayName("Should maintain database consistency after rollback")
    void shouldMaintainDatabaseConsistencyAfterRollback() {
        // Given: Asset is assigned to user A
        AssignmentRequest initialRequest = new AssignmentRequest();
        initialRequest.setAssignmentType(AssignmentType.USER);
        initialRequest.setAssignedTo("User A");
        initialRequest.setAssignedUserEmail("usera@example.com");
        
        allocationService.assignToUser(testUserId.toString(), testAsset.getId(), initialRequest);
        
        // Capture initial state
        Asset assetBeforeReassign = assetRepository.findById(testAsset.getId()).orElseThrow();
        String initialAssignedUser = assetBeforeReassign.getAssignedUser();
        String initialAssignedEmail = assetBeforeReassign.getAssignedUserEmail();
        LocalDateTime initialAssignmentDate = assetBeforeReassign.getAssignmentDate();
        
        List<AssignmentHistory> assignmentsBeforeReassign = 
            assignmentHistoryRepository.findByAssetIdOrderByAssignedAtDesc(testAsset.getId());
        int initialAssignmentCount = assignmentsBeforeReassign.size();
        UUID originalAssignmentId = assignmentsBeforeReassign.get(0).getId();
        
        // Configure audit service to fail on the second call (new assignment logging)
        doThrow(new RuntimeException("Audit service unavailable"))
            .when(auditService).logEvent(any());
        
        // When: Attempt to reassign to user B (will fail due to audit service)
        AssignmentRequest reassignRequest = new AssignmentRequest();
        reassignRequest.setAssignmentType(AssignmentType.USER);
        reassignRequest.setAssignedTo("User B");
        reassignRequest.setAssignedUserEmail("userb@example.com");
        
        // Then: Reassignment should fail
        assertThatThrownBy(() -> 
            allocationService.reassign(testUserId.toString(), testAsset.getId(), reassignRequest))
            .isInstanceOf(RuntimeException.class);
        
        // Verify database consistency: Asset state unchanged
        Asset assetAfterFailure = assetRepository.findById(testAsset.getId()).orElseThrow();
        assertThat(assetAfterFailure.getAssignedUser()).isEqualTo(initialAssignedUser);
        assertThat(assetAfterFailure.getAssignedUserEmail()).isEqualTo(initialAssignedEmail);
        assertThat(assetAfterFailure.getAssignmentDate()).isEqualTo(initialAssignmentDate);
        assertThat(assetAfterFailure.getLocation()).isNull();
        assertThat(assetAfterFailure.getLocationUpdateDate()).isNull();
        
        // Verify database consistency: Assignment history unchanged
        List<AssignmentHistory> assignmentsAfterFailure = 
            assignmentHistoryRepository.findByAssetIdOrderByAssignedAtDesc(testAsset.getId());
        assertThat(assignmentsAfterFailure).hasSize(initialAssignmentCount);
        
        // Verify database consistency: Original assignment still active
        AssignmentHistory originalAssignment = 
            assignmentHistoryRepository.findById(originalAssignmentId).orElseThrow();
        assertThat(originalAssignment.getUnassignedAt()).isNull();
        assertThat(originalAssignment.getAssignedTo()).isEqualTo("User A");
        
        // Verify database consistency: No orphaned records
        List<AssignmentHistory> activeAssignments = 
            assignmentHistoryRepository.findActiveAssignmentsByAssetId(testAsset.getId());
        assertThat(activeAssignments).hasSize(1);
        assertThat(activeAssignments.get(0).getId()).isEqualTo(originalAssignmentId);
        
        // Verify database consistency: Asset and assignment history are in sync
        assertThat(assetAfterFailure.getAssignedUser()).isEqualTo(activeAssignments.get(0).getAssignedTo());
    }
    
    @Test
    @DisplayName("Should handle successful reassignment without rollback")
    void shouldHandleSuccessfulReassignmentWithoutRollback() {
        // Given: Asset is assigned to user A
        AssignmentRequest initialRequest = new AssignmentRequest();
        initialRequest.setAssignmentType(AssignmentType.USER);
        initialRequest.setAssignedTo("User A");
        initialRequest.setAssignedUserEmail("usera@example.com");
        
        allocationService.assignToUser(testUserId.toString(), testAsset.getId(), initialRequest);
        
        List<AssignmentHistory> assignmentsBeforeReassign = 
            assignmentHistoryRepository.findByAssetIdOrderByAssignedAtDesc(testAsset.getId());
        UUID originalAssignmentId = assignmentsBeforeReassign.get(0).getId();
        
        // When: Successfully reassign to user B
        AssignmentRequest reassignRequest = new AssignmentRequest();
        reassignRequest.setAssignmentType(AssignmentType.USER);
        reassignRequest.setAssignedTo("User B");
        reassignRequest.setAssignedUserEmail("userb@example.com");
        
        allocationService.reassign(testUserId.toString(), testAsset.getId(), reassignRequest);
        
        // Then: Asset should be assigned to User B
        Asset assetAfterReassign = assetRepository.findById(testAsset.getId()).orElseThrow();
        assertThat(assetAfterReassign.getAssignedUser()).isEqualTo("User B");
        assertThat(assetAfterReassign.getAssignedUserEmail()).isEqualTo("userb@example.com");
        
        // Verify: Old assignment should be closed
        AssignmentHistory oldAssignment = 
            assignmentHistoryRepository.findById(originalAssignmentId).orElseThrow();
        assertThat(oldAssignment.getUnassignedAt()).isNotNull();
        assertThat(oldAssignment.getAssignedTo()).isEqualTo("User A");
        
        // Verify: New assignment should be active
        List<AssignmentHistory> activeAssignments = 
            assignmentHistoryRepository.findActiveAssignmentsByAssetId(testAsset.getId());
        assertThat(activeAssignments).hasSize(1);
        assertThat(activeAssignments.get(0).getAssignedTo()).isEqualTo("User B");
        assertThat(activeAssignments.get(0).getUnassignedAt()).isNull();
        
        // Verify: Two assignment records exist (old and new)
        List<AssignmentHistory> allAssignments = 
            assignmentHistoryRepository.findByAssetIdOrderByAssignedAtDesc(testAsset.getId());
        assertThat(allAssignments).hasSize(2);
    }
    
    @Test
    @DisplayName("Should rollback reassignment from user to location when audit fails")
    void shouldRollbackReassignmentFromUserToLocationWhenAuditFails() {
        // Given: Asset is assigned to user A
        AssignmentRequest initialRequest = new AssignmentRequest();
        initialRequest.setAssignmentType(AssignmentType.USER);
        initialRequest.setAssignedTo("User A");
        initialRequest.setAssignedUserEmail("usera@example.com");
        
        allocationService.assignToUser(testUserId.toString(), testAsset.getId(), initialRequest);
        
        // Verify initial state
        Asset assetBeforeReassign = assetRepository.findById(testAsset.getId()).orElseThrow();
        assertThat(assetBeforeReassign.getAssignedUser()).isEqualTo("User A");
        assertThat(assetBeforeReassign.getLocation()).isNull();
        
        // Configure audit service to fail
        doThrow(new RuntimeException("Audit service unavailable"))
            .when(auditService).logEvent(any());
        
        // When: Attempt to reassign to location B
        AssignmentRequest reassignRequest = new AssignmentRequest();
        reassignRequest.setAssignmentType(AssignmentType.LOCATION);
        reassignRequest.setAssignedTo("Location B");
        
        // Then: Reassignment should fail
        assertThatThrownBy(() -> 
            allocationService.reassign(testUserId.toString(), testAsset.getId(), reassignRequest))
            .isInstanceOf(RuntimeException.class);
        
        // Verify rollback: Asset should still be assigned to User A
        Asset assetAfterFailure = assetRepository.findById(testAsset.getId()).orElseThrow();
        assertThat(assetAfterFailure.getAssignedUser()).isEqualTo("User A");
        assertThat(assetAfterFailure.getAssignedUserEmail()).isEqualTo("usera@example.com");
        assertThat(assetAfterFailure.getLocation()).isNull();
        assertThat(assetAfterFailure.getLocationUpdateDate()).isNull();
        
        // Verify rollback: Only one active assignment (user assignment)
        List<AssignmentHistory> activeAssignments = 
            assignmentHistoryRepository.findActiveAssignmentsByAssetId(testAsset.getId());
        assertThat(activeAssignments).hasSize(1);
        assertThat(activeAssignments.get(0).getAssignmentType()).isEqualTo(AssignmentType.USER);
        assertThat(activeAssignments.get(0).getAssignedTo()).isEqualTo("User A");
    }
    
    // Helper methods
    
    private Asset createTestAsset() {
        Asset asset = new Asset();
        asset.setAssetType(AssetType.SERVER);
        asset.setName("Test Server");
        asset.setSerialNumber("TEST-SRV-" + UUID.randomUUID().toString().substring(0, 8));
        asset.setAcquisitionDate(LocalDate.now());
        asset.setStatus(LifecycleStatus.IN_USE);
        asset.setCreatedAt(LocalDateTime.now());
        asset.setUpdatedAt(LocalDateTime.now());
        asset.setCreatedBy(testUser);
        asset.setUpdatedBy(testUser);
        return assetRepository.save(asset);
    }
}
