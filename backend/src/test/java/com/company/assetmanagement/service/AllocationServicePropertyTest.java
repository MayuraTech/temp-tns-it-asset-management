package com.company.assetmanagement.service;

import com.company.assetmanagement.dto.AssignmentDTO;
import com.company.assetmanagement.dto.AssignmentHistoryDTO;
import com.company.assetmanagement.dto.AssignmentRequest;
import com.company.assetmanagement.model.*;
import com.company.assetmanagement.repository.AssignmentHistoryRepository;
import com.company.assetmanagement.repository.AssetRepository;
import com.company.assetmanagement.repository.UserRepository;
import net.jqwik.api.*;
import net.jqwik.api.constraints.AlphaChars;
import net.jqwik.api.constraints.StringLength;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Property-based tests for AllocationService.
 * 
 * **Validates: Requirements 1.1, 1.2, 2.1, 3.1, 3.2, 5.1, 5.2**
 * 
 * Tests correctness properties across randomized inputs to ensure
 * allocation operations maintain invariants under all conditions.
 */
@Group
@Label("Feature: allocation-management")
class AllocationServicePropertyTest {
    
    /**
     * Property 18: Assignment creation generates unique identifier and persists all fields.
     * 
     * **Validates: Requirements 1.1, 1.2, 2.1, 2.2**
     * 
     * For any valid assignment request (both USER and LOCATION types), the system SHALL 
     * generate a unique identifier and persist all assignment fields correctly including:
     * - Unique UUID identifier
     * - Asset ID reference
     * - Assignment type (USER or LOCATION)
     * - Assigned to value (user name or location name)
     * - Assigned by user ID
     * - Assigned at timestamp
     * - Active status (unassignedAt is null)
     */
    @Property(tries = 100)
    @Label("Property 18: Assignment creation generates unique identifier for USER assignments")
    void assignmentCreationGeneratesUniqueIdentifierForUserAssignments(
            @ForAll("validUserAssignmentRequests") AssignmentRequest request) {
        
        // Given: A valid USER assignment request and mocked dependencies
        AllocationService service = createMockedAllocationService();
        UUID assetId = UUID.randomUUID();
        String userId = UUID.randomUUID().toString();
        
        // When: Creating the USER assignment
        AssignmentDTO result = service.assignToUser(userId, assetId, request);
        
        // Then: Assignment has unique ID and all fields are persisted correctly
        assertThat(result.getId())
            .as("Assignment ID should be generated and not null")
            .isNotNull();
        
        assertThat(result.getAssetId())
            .as("Asset ID should match the requested asset")
            .isEqualTo(assetId);
        
        assertThat(result.getAssignmentType())
            .as("Assignment type should be USER")
            .isEqualTo(AssignmentType.USER);
        
        assertThat(result.getAssignedTo())
            .as("Assigned to should match the request")
            .isEqualTo(request.getAssignedTo());
        
        assertThat(result.getAssignedBy())
            .as("Assigned by should be set to the user performing the assignment")
            .isNotNull()
            .isEqualTo(UUID.fromString(userId));
        
        assertThat(result.getAssignedAt())
            .as("Assigned at timestamp should be set")
            .isNotNull()
            .isBeforeOrEqualTo(LocalDateTime.now());
        
        assertThat(result.getUnassignedAt())
            .as("Unassigned at should be null for new assignments")
            .isNull();
        
        assertThat(result.isActive())
            .as("Assignment should be active")
            .isTrue();
    }
    
    /**
     * Property 18b: Assignment creation generates unique identifier for LOCATION assignments.
     * 
     * **Validates: Requirements 2.1, 2.2**
     * 
     * For any valid LOCATION assignment request, the system SHALL generate a unique 
     * identifier and persist all assignment fields correctly.
     */
    @Property(tries = 100)
    @Label("Property 18: Assignment creation generates unique identifier for LOCATION assignments")
    void assignmentCreationGeneratesUniqueIdentifierForLocationAssignments(
            @ForAll("validLocationAssignmentRequests") AssignmentRequest request) {
        
        // Given: A valid LOCATION assignment request and mocked dependencies
        AllocationService service = createMockedAllocationService();
        UUID assetId = UUID.randomUUID();
        String userId = UUID.randomUUID().toString();
        
        // When: Creating the LOCATION assignment
        AssignmentDTO result = service.assignToLocation(userId, assetId, request);
        
        // Then: Assignment has unique ID and all fields are persisted correctly
        assertThat(result.getId())
            .as("Assignment ID should be generated and not null")
            .isNotNull();
        
        assertThat(result.getAssetId())
            .as("Asset ID should match the requested asset")
            .isEqualTo(assetId);
        
        assertThat(result.getAssignmentType())
            .as("Assignment type should be LOCATION")
            .isEqualTo(AssignmentType.LOCATION);
        
        assertThat(result.getAssignedTo())
            .as("Assigned to should match the location name from request")
            .isEqualTo(request.getAssignedTo());
        
        assertThat(result.getAssignedBy())
            .as("Assigned by should be set to the user performing the assignment")
            .isNotNull()
            .isEqualTo(UUID.fromString(userId));
        
        assertThat(result.getAssignedAt())
            .as("Assigned at timestamp should be set")
            .isNotNull()
            .isBeforeOrEqualTo(LocalDateTime.now());
        
        assertThat(result.getUnassignedAt())
            .as("Unassigned at should be null for new assignments")
            .isNull();
        
        assertThat(result.isActive())
            .as("Assignment should be active")
            .isTrue();
    }
    
    /**
     * Property 18c: Multiple assignments generate unique identifiers.
     * 
     * **Validates: Requirements 1.1, 2.1**
     * 
     * For any sequence of assignment requests, each assignment SHALL receive a 
     * unique identifier that is different from all other assignments.
     */
    @Property(tries = 50)
    @Label("Property 18: Multiple assignments generate unique identifiers")
    void multipleAssignmentsGenerateUniqueIdentifiers(
            @ForAll("assignmentSequence") List<AssignmentRequest> requests) {
        
        Assume.that(!requests.isEmpty());
        
        // Given: Multiple assignment requests
        AllocationService service = createMockedAllocationService();
        String userId = UUID.randomUUID().toString();
        Set<UUID> generatedIds = new HashSet<>();
        
        // When: Creating multiple assignments (each to a different asset)
        for (AssignmentRequest request : requests) {
            UUID assetId = UUID.randomUUID(); // Different asset for each assignment
            
            AssignmentDTO result;
            if (request.getAssignmentType() == AssignmentType.USER) {
                result = service.assignToUser(userId, assetId, request);
            } else {
                result = service.assignToLocation(userId, assetId, request);
            }
            
            // Then: Each assignment has a unique ID
            assertThat(result.getId())
                .as("Assignment ID should be unique")
                .isNotNull()
                .isNotIn(generatedIds);
            
            generatedIds.add(result.getId());
        }
        
        // Verify all IDs are unique
        assertThat(generatedIds)
            .as("All generated IDs should be unique")
            .hasSize(requests.size());
    }
    
    /**
     * Property 19: Assignment history maintains chronological order.
     * 
     * **Validates: Requirements 5.1, 5.2**
     * 
     * For any sequence of assignments and deallocations, the assignment history
     * SHALL maintain chronological order with most recent first (descending by AssignedAt).
     * 
     * This test generates 1-20 assignments for a single asset, creates them through
     * the service with chronologically ordered timestamps, then verifies that
     * getAssignmentHistory() returns them in descending order (most recent first).
     */
    @Property(tries = 100)
    @Label("Property 19: Assignment history maintains chronological order")
    void assignmentHistoryMaintainsChronologicalOrder(
            @ForAll("assignmentSequenceForHistory") List<AssignmentRequest> requests) {
        
        Assume.that(!requests.isEmpty());
        Assume.that(requests.size() <= 20);
        
        // Given: A sequence of assignment requests for a single asset
        UUID assetId = UUID.randomUUID();
        String userId = UUID.randomUUID().toString();
        
        // Setup mocked repositories with realistic behavior
        AssignmentHistoryRepository mockHistoryRepo = mock(AssignmentHistoryRepository.class);
        AssetRepository mockAssetRepo = mock(AssetRepository.class);
        UserRepository mockUserRepo = mock(UserRepository.class);
        AuditService mockAuditService = mock(AuditService.class);
        AuthorizationService mockAuthService = mock(AuthorizationService.class);
        
        // Track all created assignments with their timestamps
        List<AssignmentHistory> allAssignments = new ArrayList<>();
        LocalDateTime baseTime = LocalDateTime.now().minusDays(requests.size());
        
        // Setup asset that can be assigned and deallocated
        Asset asset = createTestAsset(assetId);
        when(mockAssetRepo.findById(assetId)).thenReturn(Optional.of(asset));
        when(mockAssetRepo.existsById(assetId)).thenReturn(true);
        
        // Setup authorization
        when(mockAuthService.hasPermission(anyString(), any(Action.class))).thenReturn(true);
        
        // Setup user repository
        when(mockUserRepo.findById(any())).thenAnswer(invocation -> {
            UUID id = invocation.getArgument(0);
            User user = new User();
            user.setId(id);
            user.setUsername("testuser");
            user.setEmail("test@example.com");
            return Optional.of(user);
        });
        
        // Mock assignment history repository to track assignments
        when(mockHistoryRepo.save(any(AssignmentHistory.class)))
            .thenAnswer(invocation -> {
                AssignmentHistory assignment = invocation.getArgument(0);
                if (assignment.getId() == null) {
                    assignment.setId(UUID.randomUUID());
                }
                allAssignments.add(assignment);
                return assignment;
            });
        
        // Mock finding active assignments for deallocation
        when(mockHistoryRepo.findActiveAssignmentsByAssetId(assetId))
            .thenAnswer(invocation -> {
                return allAssignments.stream()
                    .filter(a -> a.getUnassignedAt() == null)
                    .collect(Collectors.toList());
            });
        
        when(mockAssetRepo.save(any(Asset.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));
        
        AllocationService service = new AllocationServiceImpl(
            mockHistoryRepo,
            mockAssetRepo,
            mockUserRepo,
            mockAuditService,
            mockAuthService
        );
        
        // When: Creating a sequence of assignments with chronologically ordered timestamps
        for (int i = 0; i < requests.size(); i++) {
            AssignmentRequest request = requests.get(i);
            
            // Create assignment
            if (request.getAssignmentType() == AssignmentType.USER) {
                service.assignToUser(userId, assetId, request);
            } else {
                service.assignToLocation(userId, assetId, request);
            }
            
            // Deallocate to allow next assignment (except for the last one)
            if (i < requests.size() - 1) {
                service.deallocate(userId, assetId);
            }
        }
        
        // Mock the history query to return assignments sorted by assignedAt descending
        List<AssignmentHistory> sortedHistory = allAssignments.stream()
            .sorted(Comparator.comparing(AssignmentHistory::getAssignedAt).reversed())
            .collect(Collectors.toList());
        
        Page<AssignmentHistory> historyPage = new PageImpl<>(sortedHistory);
        when(mockHistoryRepo.findByAssetIdOrderByAssignedAtDesc(eq(assetId), any(Pageable.class)))
            .thenReturn(historyPage);
        
        // When: Retrieving assignment history
        Page<AssignmentHistoryDTO> history = service.getAssignmentHistory(
            userId, assetId, PageRequest.of(0, 100)
        );
        
        // Then: History is ordered by assignedAt descending (most recent first)
        List<LocalDateTime> timestamps = history.getContent().stream()
            .map(AssignmentHistoryDTO::getAssignedAt)
            .collect(Collectors.toList());
        
        assertThat(timestamps)
            .as("Assignment history should contain all assignments")
            .hasSize(allAssignments.size());
        
        // Verify chronological order (descending - most recent first)
        for (int i = 0; i < timestamps.size() - 1; i++) {
            assertThat(timestamps.get(i))
                .as("Assignment at index %d (timestamp: %s) should be after or equal to assignment at index %d (timestamp: %s)", 
                    i, timestamps.get(i), i + 1, timestamps.get(i + 1))
                .isAfterOrEqualTo(timestamps.get(i + 1));
        }
        
        // Verify that the most recent assignment is first
        if (!timestamps.isEmpty()) {
            LocalDateTime mostRecent = allAssignments.stream()
                .map(AssignmentHistory::getAssignedAt)
                .max(Comparator.naturalOrder())
                .orElse(null);
            
            assertThat(timestamps.get(0))
                .as("First timestamp in history should be the most recent")
                .isEqualTo(mostRecent);
        }
    }
    
    /**
     * Property 20: Deallocation completeness.
     * 
     * **Validates: Requirements 3.1, 3.2, 3.3**
     * 
     * For any deallocation operation on a USER assignment, the system SHALL properly 
     * close the assignment record and clear all asset assignment fields.
     */
    @Property(tries = 100)
    @Label("Property 20: Deallocation completeness for USER assignments")
    void deallocationProperlyClosesUserAssignmentAndClearsFields(
            @ForAll("validUserAssignmentRequests") AssignmentRequest request) {
        
        // Given: Mocked repositories
        AssignmentHistoryRepository mockHistoryRepo = mock(AssignmentHistoryRepository.class);
        AssetRepository mockAssetRepo = mock(AssetRepository.class);
        UserRepository mockUserRepo = mock(UserRepository.class);
        AuditService mockAuditService = mock(AuditService.class);
        AuthorizationService mockAuthService = mock(AuthorizationService.class);
        
        UUID assetId = UUID.randomUUID();
        String userId = UUID.randomUUID().toString();
        
        // Setup asset with USER assignment
        Asset asset = createTestAsset(assetId);
        asset.setAssignedUser(request.getAssignedTo());
        asset.setAssignedUserEmail(request.getAssignedUserEmail());
        asset.setAssignmentDate(LocalDateTime.now());
        
        // Setup active assignment record
        AssignmentHistory activeAssignment = new AssignmentHistory();
        activeAssignment.setId(UUID.randomUUID());
        activeAssignment.setAssetId(assetId);
        activeAssignment.setAssignmentType(AssignmentType.USER);
        activeAssignment.setAssignedTo(request.getAssignedTo());
        activeAssignment.setAssignedBy(UUID.fromString(userId));
        activeAssignment.setAssignedAt(LocalDateTime.now());
        
        // Setup mocks
        when(mockAuthService.hasPermission(userId, Action.DEALLOCATE_ASSET)).thenReturn(true);
        when(mockAssetRepo.findById(assetId)).thenReturn(Optional.of(asset));
        when(mockHistoryRepo.findActiveAssignmentsByAssetId(assetId))
            .thenReturn(Collections.singletonList(activeAssignment));
        when(mockHistoryRepo.save(any(AssignmentHistory.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));
        when(mockAssetRepo.save(any(Asset.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));
        when(mockUserRepo.findById(any())).thenAnswer(invocation -> {
            UUID id = invocation.getArgument(0);
            User user = new User();
            user.setId(id);
            user.setUsername("testuser");
            user.setEmail("test@example.com");
            return Optional.of(user);
        });
        
        AllocationService service = new AllocationServiceImpl(
            mockHistoryRepo,
            mockAssetRepo,
            mockUserRepo,
            mockAuditService,
            mockAuthService
        );
        
        // When: Deallocating the asset
        service.deallocate(userId, assetId);
        
        // Then: Assignment record is closed (unassignedAt is set)
        verify(mockHistoryRepo).save(argThat(assignment -> 
            assignment.getId().equals(activeAssignment.getId()) &&
            assignment.getUnassignedAt() != null &&
            assignment.getUnassignedAt().isBefore(LocalDateTime.now().plusSeconds(1))
        ));
        
        // Then: All asset assignment fields are cleared
        verify(mockAssetRepo).save(argThat(savedAsset -> 
            savedAsset.getId().equals(assetId) &&
            savedAsset.getAssignedUser() == null &&
            savedAsset.getAssignedUserEmail() == null &&
            savedAsset.getLocation() == null &&
            savedAsset.getAssignmentDate() == null &&
            savedAsset.getLocationUpdateDate() == null
        ));
        
        // Then: Audit event is logged
        verify(mockAuditService).logEvent(argThat(event ->
            event.getActionType() == Action.DELETE_ASSET &&
            event.getResourceType().equals("ASSIGNMENT") &&
            event.getResourceId().equals(activeAssignment.getId().toString())
        ));
    }
    
    /**
     * Property 20: Deallocation completeness for LOCATION assignments.
     * 
     * **Validates: Requirements 3.1, 3.2, 3.3**
     * 
     * For any deallocation operation on a LOCATION assignment, the system SHALL properly 
     * close the assignment record and clear all asset assignment fields.
     */
    @Property(tries = 100)
    @Label("Property 20: Deallocation completeness for LOCATION assignments")
    void deallocationProperlyClosesLocationAssignmentAndClearsFields(
            @ForAll("validLocationAssignmentRequests") AssignmentRequest request) {
        
        // Given: Mocked repositories
        AssignmentHistoryRepository mockHistoryRepo = mock(AssignmentHistoryRepository.class);
        AssetRepository mockAssetRepo = mock(AssetRepository.class);
        UserRepository mockUserRepo = mock(UserRepository.class);
        AuditService mockAuditService = mock(AuditService.class);
        AuthorizationService mockAuthService = mock(AuthorizationService.class);
        
        UUID assetId = UUID.randomUUID();
        String userId = UUID.randomUUID().toString();
        
        // Setup asset with LOCATION assignment
        Asset asset = createTestAsset(assetId);
        asset.setLocation(request.getAssignedTo());
        asset.setLocationUpdateDate(LocalDateTime.now());
        
        // Setup active assignment record
        AssignmentHistory activeAssignment = new AssignmentHistory();
        activeAssignment.setId(UUID.randomUUID());
        activeAssignment.setAssetId(assetId);
        activeAssignment.setAssignmentType(AssignmentType.LOCATION);
        activeAssignment.setAssignedTo(request.getAssignedTo());
        activeAssignment.setAssignedBy(UUID.fromString(userId));
        activeAssignment.setAssignedAt(LocalDateTime.now());
        
        // Setup mocks
        when(mockAuthService.hasPermission(userId, Action.DEALLOCATE_ASSET)).thenReturn(true);
        when(mockAssetRepo.findById(assetId)).thenReturn(Optional.of(asset));
        when(mockHistoryRepo.findActiveAssignmentsByAssetId(assetId))
            .thenReturn(Collections.singletonList(activeAssignment));
        when(mockHistoryRepo.save(any(AssignmentHistory.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));
        when(mockAssetRepo.save(any(Asset.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));
        when(mockUserRepo.findById(any())).thenAnswer(invocation -> {
            UUID id = invocation.getArgument(0);
            User user = new User();
            user.setId(id);
            user.setUsername("testuser");
            user.setEmail("test@example.com");
            return Optional.of(user);
        });
        
        AllocationService service = new AllocationServiceImpl(
            mockHistoryRepo,
            mockAssetRepo,
            mockUserRepo,
            mockAuditService,
            mockAuthService
        );
        
        // When: Deallocating the asset
        service.deallocate(userId, assetId);
        
        // Then: Assignment record is closed (unassignedAt is set)
        verify(mockHistoryRepo).save(argThat(assignment -> 
            assignment.getId().equals(activeAssignment.getId()) &&
            assignment.getUnassignedAt() != null &&
            assignment.getUnassignedAt().isBefore(LocalDateTime.now().plusSeconds(1))
        ));
        
        // Then: All asset assignment fields are cleared (including location fields)
        verify(mockAssetRepo).save(argThat(savedAsset -> 
            savedAsset.getId().equals(assetId) &&
            savedAsset.getAssignedUser() == null &&
            savedAsset.getAssignedUserEmail() == null &&
            savedAsset.getLocation() == null &&
            savedAsset.getAssignmentDate() == null &&
            savedAsset.getLocationUpdateDate() == null
        ));
        
        // Then: Audit event is logged
        verify(mockAuditService).logEvent(argThat(event ->
            event.getActionType() == Action.DELETE_ASSET &&
            event.getResourceType().equals("ASSIGNMENT") &&
            event.getResourceId().equals(activeAssignment.getId().toString())
        ));
    }
    
    // Generators
    
    @Provide
    Arbitrary<AssignmentRequest> validUserAssignmentRequests() {
        return Combinators.combine(
            Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(100),
            Arbitraries.strings().alpha().ofMinLength(5).ofMaxLength(50)
                .map(s -> s + "@example.com")
        ).as((name, email) -> {
            AssignmentRequest request = new AssignmentRequest();
            request.setAssignmentType(AssignmentType.USER);
            request.setAssignedTo(name);
            request.setAssignedUserEmail(email);
            return request;
        });
    }
    
    @Provide
    Arbitrary<AssignmentRequest> validLocationAssignmentRequests() {
        return Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(100)
            .map(location -> {
                AssignmentRequest request = new AssignmentRequest();
                request.setAssignmentType(AssignmentType.LOCATION);
                request.setAssignedTo(location);
                return request;
            });
    }
    
    @Provide
    Arbitrary<List<AssignmentRequest>> assignmentSequence() {
        return Arbitraries.oneOf(
            validUserAssignmentRequests(),
            validLocationAssignmentRequests()
        ).list().ofMinSize(2).ofMaxSize(10);
    }
    
    @Provide
    Arbitrary<List<AssignmentRequest>> assignmentSequenceForHistory() {
        return Arbitraries.oneOf(
            validUserAssignmentRequests(),
            validLocationAssignmentRequests()
        ).list().ofMinSize(1).ofMaxSize(20);
    }
    
    // Helper methods
    
    private AllocationService createMockedAllocationService() {
        AssignmentHistoryRepository mockHistoryRepo = mock(AssignmentHistoryRepository.class);
        AssetRepository mockAssetRepo = mock(AssetRepository.class);
        UserRepository mockUserRepo = mock(UserRepository.class);
        AuditService mockAuditService = mock(AuditService.class);
        AuthorizationService mockAuthService = mock(AuthorizationService.class);
        
        // Setup default mocks
        when(mockAuthService.hasPermission(any(), any())).thenReturn(true);
        when(mockAssetRepo.findById(any())).thenAnswer(invocation -> {
            UUID id = invocation.getArgument(0);
            return Optional.of(createTestAsset(id));
        });
        when(mockHistoryRepo.findActiveAssignmentsByAssetId(any()))
            .thenReturn(Collections.emptyList());
        when(mockHistoryRepo.save(any(AssignmentHistory.class)))
            .thenAnswer(invocation -> {
                AssignmentHistory assignment = invocation.getArgument(0);
                if (assignment.getId() == null) {
                    assignment.setId(UUID.randomUUID());
                }
                return assignment;
            });
        when(mockUserRepo.findById(any())).thenAnswer(invocation -> {
            UUID id = invocation.getArgument(0);
            User user = new User();
            user.setId(id);
            user.setUsername("testuser");
            user.setEmail("test@example.com");
            return Optional.of(user);
        });
        
        return new AllocationServiceImpl(
            mockHistoryRepo,
            mockAssetRepo,
            mockUserRepo,
            mockAuditService,
            mockAuthService
        );
    }
    
    private Asset createTestAsset(UUID id) {
        Asset asset = new Asset();
        asset.setId(id);
        asset.setAssetType(AssetType.SERVER);
        asset.setName("Test Asset");
        asset.setSerialNumber("SRV-" + id.toString().substring(0, 8));
        asset.setAcquisitionDate(LocalDate.now());
        asset.setStatus(LifecycleStatus.IN_USE);
        return asset;
    }
}
