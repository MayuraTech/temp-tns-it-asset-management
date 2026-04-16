package com.company.assetmanagement.repository;

import com.company.assetmanagement.model.AssignmentHistory;
import com.company.assetmanagement.model.AssignmentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for AssignmentHistoryRepository.
 * Tests all custom query methods with actual database operations.
 * 
 * <p>These tests verify:
 * <ul>
 *   <li>Query methods return correct results</li>
 *   <li>Pagination works correctly</li>
 *   <li>Sorting is applied properly</li>
 *   <li>Case-insensitive searches work</li>
 *   <li>Aggregation queries return accurate statistics</li>
 * </ul>
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("AssignmentHistoryRepository Integration Tests")
class AssignmentHistoryRepositoryTest {
    
    @Autowired
    private AssignmentHistoryRepository assignmentHistoryRepository;
    
    private UUID testAssetId1;
    private UUID testAssetId2;
    private UUID testUserId1;
    private UUID testUserId2;
    
    @BeforeEach
    void setUp() {
        // Clean up before each test
        assignmentHistoryRepository.deleteAll();
        
        // Initialize test data
        testAssetId1 = UUID.randomUUID();
        testAssetId2 = UUID.randomUUID();
        testUserId1 = UUID.randomUUID();
        testUserId2 = UUID.randomUUID();
    }
    
    @Test
    @DisplayName("Should find assignment history by asset ID ordered by assigned at descending")
    void shouldFindByAssetIdOrderByAssignedAtDesc() {
        // Given: Multiple assignments for the same asset at different times
        LocalDateTime now = LocalDateTime.now();
        
        AssignmentHistory assignment1 = createAssignment(
            testAssetId1, AssignmentType.USER, "John Doe", testUserId1, now.minusDays(3)
        );
        assignment1.setUnassignedAt(now.minusDays(2));
        assignmentHistoryRepository.save(assignment1);
        
        AssignmentHistory assignment2 = createAssignment(
            testAssetId1, AssignmentType.LOCATION, "Data Center A", testUserId1, now.minusDays(1)
        );
        assignment2.setUnassignedAt(now.minusHours(1));
        assignmentHistoryRepository.save(assignment2);
        
        AssignmentHistory assignment3 = createAssignment(
            testAssetId1, AssignmentType.USER, "Jane Smith", testUserId2, now
        );
        assignmentHistoryRepository.save(assignment3);
        
        // Create assignment for different asset (should not be included)
        createAndSaveAssignment(testAssetId2, AssignmentType.USER, "Other User", testUserId1);
        
        // When
        Pageable pageable = PageRequest.of(0, 20);
        Page<AssignmentHistory> results = assignmentHistoryRepository
            .findByAssetIdOrderByAssignedAtDesc(testAssetId1, pageable);
        
        // Then
        assertThat(results.getContent()).hasSize(3);
        assertThat(results.getTotalElements()).isEqualTo(3);
        
        // Verify chronological order (most recent first)
        List<AssignmentHistory> assignments = results.getContent();
        assertThat(assignments.get(0).getAssignedTo()).isEqualTo("Jane Smith");
        assertThat(assignments.get(1).getAssignedTo()).isEqualTo("Data Center A");
        assertThat(assignments.get(2).getAssignedTo()).isEqualTo("John Doe");
        
        // Verify timestamps are in descending order
        assertThat(assignments.get(0).getAssignedAt()).isAfter(assignments.get(1).getAssignedAt());
        assertThat(assignments.get(1).getAssignedAt()).isAfter(assignments.get(2).getAssignedAt());
    }
    
    @Test
    @DisplayName("Should find assignments by assigned to containing ignore case")
    void shouldFindByAssignedToContainingIgnoreCase() {
        // Given: Assignments with various names
        createAndSaveAssignment(testAssetId1, AssignmentType.USER, "John Doe", testUserId1);
        createAndSaveAssignment(testAssetId2, AssignmentType.USER, "Jane Doe", testUserId1);
        createAndSaveAssignment(UUID.randomUUID(), AssignmentType.USER, "Bob Smith", testUserId2);
        createAndSaveAssignment(UUID.randomUUID(), AssignmentType.LOCATION, "Data Center", testUserId1);
        
        // When: Search for "doe" (case-insensitive)
        Pageable pageable = PageRequest.of(0, 20);
        Page<AssignmentHistory> results = assignmentHistoryRepository
            .findByAssignedToContainingIgnoreCase("doe", pageable);
        
        // Then
        assertThat(results.getContent()).hasSize(2);
        assertThat(results.getContent())
            .extracting(AssignmentHistory::getAssignedTo)
            .containsExactlyInAnyOrder("John Doe", "Jane Doe");
    }
    
    @Test
    @DisplayName("Should find assignments by assigned to with different case")
    void shouldFindByAssignedToWithDifferentCase() {
        // Given
        createAndSaveAssignment(testAssetId1, AssignmentType.USER, "John DOE", testUserId1);
        createAndSaveAssignment(testAssetId2, AssignmentType.USER, "jane doe", testUserId1);
        
        // When: Search with lowercase
        Pageable pageable = PageRequest.of(0, 20);
        Page<AssignmentHistory> results = assignmentHistoryRepository
            .findByAssignedToContainingIgnoreCase("doe", pageable);
        
        // Then: Should find both regardless of case
        assertThat(results.getContent()).hasSize(2);
    }
    
    @Test
    @DisplayName("Should find active assignments by asset ID")
    void shouldFindActiveAssignmentsByAssetId() {
        // Given: Mix of active and historical assignments
        LocalDateTime now = LocalDateTime.now();
        
        // Historical assignment (closed)
        AssignmentHistory historical = createAssignment(
            testAssetId1, AssignmentType.USER, "John Doe", testUserId1, now.minusDays(5)
        );
        historical.setUnassignedAt(now.minusDays(1));
        assignmentHistoryRepository.save(historical);
        
        // Active assignment (not closed)
        AssignmentHistory active = createAssignment(
            testAssetId1, AssignmentType.LOCATION, "Data Center A", testUserId1, now
        );
        assignmentHistoryRepository.save(active);
        
        // Active assignment for different asset
        createAndSaveAssignment(testAssetId2, AssignmentType.USER, "Jane Smith", testUserId2);
        
        // When
        List<AssignmentHistory> results = assignmentHistoryRepository
            .findActiveAssignmentsByAssetId(testAssetId1);
        
        // Then: Should only return the active assignment for testAssetId1
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getAssignedTo()).isEqualTo("Data Center A");
        assertThat(results.get(0).getUnassignedAt()).isNull();
        assertThat(results.get(0).isActive()).isTrue();
    }
    
    @Test
    @DisplayName("Should return empty list when no active assignments exist")
    void shouldReturnEmptyListWhenNoActiveAssignments() {
        // Given: Only historical assignments
        LocalDateTime now = LocalDateTime.now();
        AssignmentHistory historical = createAssignment(
            testAssetId1, AssignmentType.USER, "John Doe", testUserId1, now.minusDays(5)
        );
        historical.setUnassignedAt(now.minusDays(1));
        assignmentHistoryRepository.save(historical);
        
        // When
        List<AssignmentHistory> results = assignmentHistoryRepository
            .findActiveAssignmentsByAssetId(testAssetId1);
        
        // Then
        assertThat(results).isEmpty();
    }
    
    @Test
    @DisplayName("Should get assignment statistics grouped by type")
    void shouldGetAssignmentStatistics() {
        // Given: Mix of user and location assignments
        createAndSaveAssignment(testAssetId1, AssignmentType.USER, "John Doe", testUserId1);
        createAndSaveAssignment(testAssetId2, AssignmentType.USER, "Jane Smith", testUserId1);
        createAndSaveAssignment(UUID.randomUUID(), AssignmentType.USER, "Bob Johnson", testUserId2);
        createAndSaveAssignment(UUID.randomUUID(), AssignmentType.LOCATION, "Data Center A", testUserId1);
        createAndSaveAssignment(UUID.randomUUID(), AssignmentType.LOCATION, "Data Center B", testUserId1);
        
        // Create some historical assignments (should not be counted)
        LocalDateTime now = LocalDateTime.now();
        AssignmentHistory historical = createAssignment(
            UUID.randomUUID(), AssignmentType.USER, "Historical User", testUserId1, now.minusDays(5)
        );
        historical.setUnassignedAt(now.minusDays(1));
        assignmentHistoryRepository.save(historical);
        
        // When
        List<Object[]> statistics = assignmentHistoryRepository.getAssignmentStatistics();
        
        // Then
        assertThat(statistics).hasSize(2);
        
        // Verify counts by type
        boolean foundUserStats = false;
        boolean foundLocationStats = false;
        
        for (Object[] stat : statistics) {
            AssignmentType type = (AssignmentType) stat[0];
            Long count = (Long) stat[1];
            
            if (type == AssignmentType.USER) {
                assertThat(count).isEqualTo(3L);
                foundUserStats = true;
            } else if (type == AssignmentType.LOCATION) {
                assertThat(count).isEqualTo(2L);
                foundLocationStats = true;
            }
        }
        
        assertThat(foundUserStats).isTrue();
        assertThat(foundLocationStats).isTrue();
    }
    
    @Test
    @DisplayName("Should get top assignments by type")
    void shouldGetTopAssignmentsByType() {
        // Given: Multiple assignments to different users
        createAndSaveAssignment(UUID.randomUUID(), AssignmentType.USER, "John Doe", testUserId1);
        createAndSaveAssignment(UUID.randomUUID(), AssignmentType.USER, "John Doe", testUserId1);
        createAndSaveAssignment(UUID.randomUUID(), AssignmentType.USER, "John Doe", testUserId1);
        createAndSaveAssignment(UUID.randomUUID(), AssignmentType.USER, "Jane Smith", testUserId1);
        createAndSaveAssignment(UUID.randomUUID(), AssignmentType.USER, "Jane Smith", testUserId1);
        createAndSaveAssignment(UUID.randomUUID(), AssignmentType.USER, "Bob Johnson", testUserId2);
        
        // When: Get top 2 users
        Pageable pageable = PageRequest.of(0, 2);
        List<Object[]> topUsers = assignmentHistoryRepository
            .getTopAssignmentsByType(AssignmentType.USER, pageable);
        
        // Then
        assertThat(topUsers).hasSize(2);
        
        // Verify order (highest count first)
        String topUser = (String) topUsers.get(0)[0];
        Long topCount = (Long) topUsers.get(0)[1];
        assertThat(topUser).isEqualTo("John Doe");
        assertThat(topCount).isEqualTo(3L);
        
        String secondUser = (String) topUsers.get(1)[0];
        Long secondCount = (Long) topUsers.get(1)[1];
        assertThat(secondUser).isEqualTo("Jane Smith");
        assertThat(secondCount).isEqualTo(2L);
    }
    
    @Test
    @DisplayName("Should get top locations by assignment count")
    void shouldGetTopLocationsByAssignmentCount() {
        // Given: Multiple assignments to different locations
        createAndSaveAssignment(UUID.randomUUID(), AssignmentType.LOCATION, "Data Center A", testUserId1);
        createAndSaveAssignment(UUID.randomUUID(), AssignmentType.LOCATION, "Data Center A", testUserId1);
        createAndSaveAssignment(UUID.randomUUID(), AssignmentType.LOCATION, "Data Center B", testUserId1);
        createAndSaveAssignment(UUID.randomUUID(), AssignmentType.LOCATION, "Office Floor 3", testUserId2);
        
        // When: Get top 3 locations
        Pageable pageable = PageRequest.of(0, 3);
        List<Object[]> topLocations = assignmentHistoryRepository
            .getTopAssignmentsByType(AssignmentType.LOCATION, pageable);
        
        // Then
        assertThat(topLocations).hasSize(3);
        
        // Verify top location
        String topLocation = (String) topLocations.get(0)[0];
        Long topCount = (Long) topLocations.get(0)[1];
        assertThat(topLocation).isEqualTo("Data Center A");
        assertThat(topCount).isEqualTo(2L);
    }
    
    @Test
    @DisplayName("Should count active assignments")
    void shouldCountActiveAssignments() {
        // Given: Mix of active and historical assignments
        LocalDateTime now = LocalDateTime.now();
        
        // Active assignments
        createAndSaveAssignment(testAssetId1, AssignmentType.USER, "John Doe", testUserId1);
        createAndSaveAssignment(testAssetId2, AssignmentType.USER, "Jane Smith", testUserId1);
        createAndSaveAssignment(UUID.randomUUID(), AssignmentType.LOCATION, "Data Center A", testUserId1);
        
        // Historical assignments
        AssignmentHistory historical1 = createAssignment(
            UUID.randomUUID(), AssignmentType.USER, "Old User 1", testUserId1, now.minusDays(10)
        );
        historical1.setUnassignedAt(now.minusDays(5));
        assignmentHistoryRepository.save(historical1);
        
        AssignmentHistory historical2 = createAssignment(
            UUID.randomUUID(), AssignmentType.USER, "Old User 2", testUserId2, now.minusDays(8)
        );
        historical2.setUnassignedAt(now.minusDays(3));
        assignmentHistoryRepository.save(historical2);
        
        // When
        long count = assignmentHistoryRepository.countActiveAssignments();
        
        // Then: Should only count active assignments
        assertThat(count).isEqualTo(3L);
    }
    
    @Test
    @DisplayName("Should find assignments by assignment type")
    void shouldFindByAssignmentType() {
        // Given
        createAndSaveAssignment(testAssetId1, AssignmentType.USER, "John Doe", testUserId1);
        createAndSaveAssignment(testAssetId2, AssignmentType.USER, "Jane Smith", testUserId1);
        createAndSaveAssignment(UUID.randomUUID(), AssignmentType.LOCATION, "Data Center A", testUserId1);
        createAndSaveAssignment(UUID.randomUUID(), AssignmentType.LOCATION, "Data Center B", testUserId2);
        
        // When
        Pageable pageable = PageRequest.of(0, 20);
        Page<AssignmentHistory> userAssignments = assignmentHistoryRepository
            .findByAssignmentType(AssignmentType.USER, pageable);
        Page<AssignmentHistory> locationAssignments = assignmentHistoryRepository
            .findByAssignmentType(AssignmentType.LOCATION, pageable);
        
        // Then
        assertThat(userAssignments.getContent()).hasSize(2);
        assertThat(userAssignments.getContent())
            .allMatch(a -> a.getAssignmentType() == AssignmentType.USER);
        
        assertThat(locationAssignments.getContent()).hasSize(2);
        assertThat(locationAssignments.getContent())
            .allMatch(a -> a.getAssignmentType() == AssignmentType.LOCATION);
    }
    
    @Test
    @DisplayName("Should find active assignments by type")
    void shouldFindActiveAssignmentsByType() {
        // Given: Mix of active and historical assignments
        LocalDateTime now = LocalDateTime.now();
        
        // Active user assignments
        createAndSaveAssignment(testAssetId1, AssignmentType.USER, "John Doe", testUserId1);
        createAndSaveAssignment(testAssetId2, AssignmentType.USER, "Jane Smith", testUserId1);
        
        // Active location assignment
        createAndSaveAssignment(UUID.randomUUID(), AssignmentType.LOCATION, "Data Center A", testUserId1);
        
        // Historical user assignment
        AssignmentHistory historical = createAssignment(
            UUID.randomUUID(), AssignmentType.USER, "Old User", testUserId1, now.minusDays(10)
        );
        historical.setUnassignedAt(now.minusDays(5));
        assignmentHistoryRepository.save(historical);
        
        // When
        List<AssignmentHistory> activeUserAssignments = assignmentHistoryRepository
            .findActiveAssignmentsByType(AssignmentType.USER);
        List<AssignmentHistory> activeLocationAssignments = assignmentHistoryRepository
            .findActiveAssignmentsByType(AssignmentType.LOCATION);
        
        // Then
        assertThat(activeUserAssignments).hasSize(2);
        assertThat(activeUserAssignments)
            .allMatch(a -> a.getAssignmentType() == AssignmentType.USER && a.isActive());
        
        assertThat(activeLocationAssignments).hasSize(1);
        assertThat(activeLocationAssignments)
            .allMatch(a -> a.getAssignmentType() == AssignmentType.LOCATION && a.isActive());
    }
    
    @Test
    @DisplayName("Should find assignments by assigned by user")
    void shouldFindByAssignedBy() {
        // Given: Assignments created by different users
        createAndSaveAssignment(testAssetId1, AssignmentType.USER, "John Doe", testUserId1);
        createAndSaveAssignment(testAssetId2, AssignmentType.USER, "Jane Smith", testUserId1);
        createAndSaveAssignment(UUID.randomUUID(), AssignmentType.LOCATION, "Data Center A", testUserId1);
        createAndSaveAssignment(UUID.randomUUID(), AssignmentType.USER, "Bob Johnson", testUserId2);
        
        // When
        Pageable pageable = PageRequest.of(0, 20);
        Page<AssignmentHistory> user1Assignments = assignmentHistoryRepository
            .findByAssignedBy(testUserId1, pageable);
        Page<AssignmentHistory> user2Assignments = assignmentHistoryRepository
            .findByAssignedBy(testUserId2, pageable);
        
        // Then
        assertThat(user1Assignments.getContent()).hasSize(3);
        assertThat(user1Assignments.getContent())
            .allMatch(a -> a.getAssignedBy().equals(testUserId1));
        
        assertThat(user2Assignments.getContent()).hasSize(1);
        assertThat(user2Assignments.getContent())
            .allMatch(a -> a.getAssignedBy().equals(testUserId2));
    }
    
    @Test
    @DisplayName("Should handle pagination correctly")
    void shouldHandlePaginationCorrectly() {
        // Given: Create 25 assignments
        for (int i = 0; i < 25; i++) {
            createAndSaveAssignment(
                UUID.randomUUID(),
                AssignmentType.USER,
                "User " + i,
                testUserId1
            );
        }
        
        // When: Request first page with size 10
        Pageable firstPage = PageRequest.of(0, 10);
        Page<AssignmentHistory> results = assignmentHistoryRepository
            .findByAssignedBy(testUserId1, firstPage);
        
        // Then
        assertThat(results.getContent()).hasSize(10);
        assertThat(results.getTotalElements()).isEqualTo(25);
        assertThat(results.getTotalPages()).isEqualTo(3);
        assertThat(results.hasNext()).isTrue();
        assertThat(results.hasPrevious()).isFalse();
        
        // When: Request second page
        Pageable secondPage = PageRequest.of(1, 10);
        Page<AssignmentHistory> secondResults = assignmentHistoryRepository
            .findByAssignedBy(testUserId1, secondPage);
        
        // Then
        assertThat(secondResults.getContent()).hasSize(10);
        assertThat(secondResults.hasNext()).isTrue();
        assertThat(secondResults.hasPrevious()).isTrue();
        
        // When: Request last page
        Pageable lastPage = PageRequest.of(2, 10);
        Page<AssignmentHistory> lastResults = assignmentHistoryRepository
            .findByAssignedBy(testUserId1, lastPage);
        
        // Then
        assertThat(lastResults.getContent()).hasSize(5);
        assertThat(lastResults.hasNext()).isFalse();
        assertThat(lastResults.hasPrevious()).isTrue();
    }
    
    @Test
    @DisplayName("Should persist and retrieve all assignment fields correctly")
    void shouldPersistAndRetrieveAllFieldsCorrectly() {
        // Given
        LocalDateTime assignedAt = LocalDateTime.now().minusDays(1);
        AssignmentHistory assignment = createAssignment(
            testAssetId1,
            AssignmentType.USER,
            "John Doe",
            testUserId1,
            assignedAt
        );
        
        // When
        AssignmentHistory saved = assignmentHistoryRepository.save(assignment);
        AssignmentHistory retrieved = assignmentHistoryRepository.findById(saved.getId()).orElseThrow();
        
        // Then
        assertThat(retrieved.getId()).isNotNull();
        assertThat(retrieved.getAssetId()).isEqualTo(testAssetId1);
        assertThat(retrieved.getAssignmentType()).isEqualTo(AssignmentType.USER);
        assertThat(retrieved.getAssignedTo()).isEqualTo("John Doe");
        assertThat(retrieved.getAssignedBy()).isEqualTo(testUserId1);
        assertThat(retrieved.getAssignedAt()).isEqualToIgnoringNanos(assignedAt);
        assertThat(retrieved.getUnassignedAt()).isNull();
        assertThat(retrieved.isActive()).isTrue();
    }
    
    // Helper methods
    
    private AssignmentHistory createAssignment(
            UUID assetId,
            AssignmentType type,
            String assignedTo,
            UUID assignedBy,
            LocalDateTime assignedAt) {
        
        AssignmentHistory assignment = new AssignmentHistory();
        assignment.setAssetId(assetId);
        assignment.setAssignmentType(type);
        assignment.setAssignedTo(assignedTo);
        assignment.setAssignedBy(assignedBy);
        assignment.setAssignedAt(assignedAt);
        return assignment;
    }
    
    private AssignmentHistory createAndSaveAssignment(
            UUID assetId,
            AssignmentType type,
            String assignedTo,
            UUID assignedBy) {
        
        AssignmentHistory assignment = new AssignmentHistory(assetId, type, assignedTo, assignedBy);
        return assignmentHistoryRepository.save(assignment);
    }
}
