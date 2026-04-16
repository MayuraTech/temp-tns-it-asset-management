package com.company.assetmanagement.service;

import com.company.assetmanagement.dto.*;
import com.company.assetmanagement.exception.*;
import com.company.assetmanagement.model.*;
import com.company.assetmanagement.repository.AssignmentHistoryRepository;
import com.company.assetmanagement.repository.AssetRepository;
import com.company.assetmanagement.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AllocationServiceImpl.
 * 
 * Tests all allocation operations including assignment, deallocation,
 * reassignment, and query operations.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AllocationServiceImpl Unit Tests")
class AllocationServiceImplTest {
    
    @Mock
    private AssignmentHistoryRepository assignmentHistoryRepository;
    
    @Mock
    private AssetRepository assetRepository;
    
    @Mock
    private UserRepository userRepository;
    
    @Mock
    private AuditService auditService;
    
    @Mock
    private AuthorizationService authorizationService;
    
    @InjectMocks
    private AllocationServiceImpl allocationService;
    
    private UUID assetId;
    private UUID userId;
    private Asset testAsset;
    private User testUser;
    private AssignmentRequest userAssignmentRequest;
    private AssignmentRequest locationAssignmentRequest;
    
    @BeforeEach
    void setUp() {
        assetId = UUID.randomUUID();
        userId = UUID.randomUUID();
        
        // Create test asset
        testAsset = new Asset();
        testAsset.setId(assetId);
        testAsset.setAssetType(AssetType.SERVER);
        testAsset.setName("Test Server");
        testAsset.setSerialNumber("SRV-001");
        testAsset.setAcquisitionDate(LocalDate.now());
        testAsset.setStatus(LifecycleStatus.IN_USE);
        
        // Create test user
        testUser = new User();
        testUser.setId(userId);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        
        // Create test requests
        userAssignmentRequest = new AssignmentRequest();
        userAssignmentRequest.setAssignmentType(AssignmentType.USER);
        userAssignmentRequest.setAssignedTo("John Doe");
        userAssignmentRequest.setAssignedUserEmail("john.doe@example.com");
        
        locationAssignmentRequest = new AssignmentRequest();
        locationAssignmentRequest.setAssignmentType(AssignmentType.LOCATION);
        locationAssignmentRequest.setAssignedTo("Data Center A");
    }
    
    // Task 3.2: Assignment Operations Tests
    
    @Test
    @DisplayName("Should assign asset to user when authorized and valid")
    void shouldAssignAssetToUserWhenAuthorizedAndValid() {
        // Given
        when(authorizationService.hasPermission(userId.toString(), Action.ALLOCATE_ASSET))
            .thenReturn(true);
        when(assetRepository.findById(assetId)).thenReturn(Optional.of(testAsset));
        when(assignmentHistoryRepository.findActiveAssignmentsByAssetId(assetId))
            .thenReturn(Collections.emptyList());
        when(assignmentHistoryRepository.save(any(AssignmentHistory.class)))
            .thenAnswer(invocation -> {
                AssignmentHistory assignment = invocation.getArgument(0);
                assignment.setId(UUID.randomUUID());
                return assignment;
            });
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        
        // When
        AssignmentDTO result = allocationService.assignToUser(userId.toString(), assetId, userAssignmentRequest);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isNotNull();
        assertThat(result.getAssetId()).isEqualTo(assetId);
        assertThat(result.getAssignmentType()).isEqualTo(AssignmentType.USER);
        assertThat(result.getAssignedTo()).isEqualTo("John Doe");
        assertThat(result.isActive()).isTrue();
        
        // Verify assignment history was saved
        ArgumentCaptor<AssignmentHistory> assignmentCaptor = ArgumentCaptor.forClass(AssignmentHistory.class);
        verify(assignmentHistoryRepository).save(assignmentCaptor.capture());
        AssignmentHistory savedAssignment = assignmentCaptor.getValue();
        assertThat(savedAssignment.getAssetId()).isEqualTo(assetId);
        assertThat(savedAssignment.getAssignmentType()).isEqualTo(AssignmentType.USER);
        assertThat(savedAssignment.getAssignedTo()).isEqualTo("John Doe");
        
        // Verify asset was updated
        ArgumentCaptor<Asset> assetCaptor = ArgumentCaptor.forClass(Asset.class);
        verify(assetRepository).save(assetCaptor.capture());
        Asset savedAsset = assetCaptor.getValue();
        assertThat(savedAsset.getAssignedUser()).isEqualTo("John Doe");
        assertThat(savedAsset.getAssignedUserEmail()).isEqualTo("john.doe@example.com");
        assertThat(savedAsset.getAssignmentDate()).isNotNull();
        
        // Verify audit log was created
        verify(auditService).logEvent(any(AuditEventDTO.class));
    }
    
    @Test
    @DisplayName("Should assign asset to location when authorized and valid")
    void shouldAssignAssetToLocationWhenAuthorizedAndValid() {
        // Given
        when(authorizationService.hasPermission(userId.toString(), Action.ALLOCATE_ASSET))
            .thenReturn(true);
        when(assetRepository.findById(assetId)).thenReturn(Optional.of(testAsset));
        when(assignmentHistoryRepository.findActiveAssignmentsByAssetId(assetId))
            .thenReturn(Collections.emptyList());
        when(assignmentHistoryRepository.save(any(AssignmentHistory.class)))
            .thenAnswer(invocation -> {
                AssignmentHistory assignment = invocation.getArgument(0);
                assignment.setId(UUID.randomUUID());
                return assignment;
            });
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        
        // When
        AssignmentDTO result = allocationService.assignToLocation(userId.toString(), assetId, locationAssignmentRequest);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.getAssignmentType()).isEqualTo(AssignmentType.LOCATION);
        assertThat(result.getAssignedTo()).isEqualTo("Data Center A");
        
        // Verify asset location was updated
        ArgumentCaptor<Asset> assetCaptor = ArgumentCaptor.forClass(Asset.class);
        verify(assetRepository).save(assetCaptor.capture());
        Asset savedAsset = assetCaptor.getValue();
        assertThat(savedAsset.getLocation()).isEqualTo("Data Center A");
        assertThat(savedAsset.getLocationUpdateDate()).isNotNull();
    }
    
    @Test
    @DisplayName("Should throw InsufficientPermissionsException when user lacks permission")
    void shouldThrowExceptionWhenUserLacksPermission() {
        // Given
        when(authorizationService.hasPermission(userId.toString(), Action.ALLOCATE_ASSET))
            .thenReturn(false);
        
        // When/Then
        assertThatThrownBy(() -> 
            allocationService.assignToUser(userId.toString(), assetId, userAssignmentRequest))
            .isInstanceOf(InsufficientPermissionsException.class);
        
        verify(assetRepository, never()).save(any());
        verify(assignmentHistoryRepository, never()).save(any());
    }
    
    @Test
    @DisplayName("Should throw ResourceNotFoundException when asset does not exist")
    void shouldThrowExceptionWhenAssetNotFound() {
        // Given
        when(authorizationService.hasPermission(userId.toString(), Action.ALLOCATE_ASSET))
            .thenReturn(true);
        when(assetRepository.findById(assetId)).thenReturn(Optional.empty());
        
        // When/Then
        assertThatThrownBy(() -> 
            allocationService.assignToUser(userId.toString(), assetId, userAssignmentRequest))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining(assetId.toString());
    }
    
    @Test
    @DisplayName("Should throw AssetAlreadyAssignedException when asset is already assigned")
    void shouldThrowExceptionWhenAssetAlreadyAssigned() {
        // Given
        when(authorizationService.hasPermission(userId.toString(), Action.ALLOCATE_ASSET))
            .thenReturn(true);
        when(assetRepository.findById(assetId)).thenReturn(Optional.of(testAsset));
        
        AssignmentHistory existingAssignment = new AssignmentHistory();
        existingAssignment.setAssignedTo("Existing User");
        when(assignmentHistoryRepository.findActiveAssignmentsByAssetId(assetId))
            .thenReturn(Collections.singletonList(existingAssignment));
        
        // When/Then
        assertThatThrownBy(() -> 
            allocationService.assignToUser(userId.toString(), assetId, userAssignmentRequest))
            .isInstanceOf(AssetAlreadyAssignedException.class)
            .hasMessageContaining("already assigned");
    }
    
    @Test
    @DisplayName("Should throw AssetNotAssignableException when asset status is not assignable")
    void shouldThrowExceptionWhenAssetNotAssignable() {
        // Given
        testAsset.setStatus(LifecycleStatus.ORDERED);
        
        when(authorizationService.hasPermission(userId.toString(), Action.ALLOCATE_ASSET))
            .thenReturn(true);
        when(assetRepository.findById(assetId)).thenReturn(Optional.of(testAsset));
        when(assignmentHistoryRepository.findActiveAssignmentsByAssetId(assetId))
            .thenReturn(Collections.emptyList());
        
        // When/Then
        assertThatThrownBy(() -> 
            allocationService.assignToUser(userId.toString(), assetId, userAssignmentRequest))
            .isInstanceOf(AssetNotAssignableException.class)
            .hasMessageContaining("ORDERED");
    }
    
    @Test
    @DisplayName("Should throw ValidationException when assigned to is blank")
    void shouldThrowValidationExceptionWhenAssignedToBlank() {
        // Given
        userAssignmentRequest.setAssignedTo("");
        
        when(authorizationService.hasPermission(userId.toString(), Action.ALLOCATE_ASSET))
            .thenReturn(true);
        
        // When/Then
        assertThatThrownBy(() -> 
            allocationService.assignToUser(userId.toString(), assetId, userAssignmentRequest))
            .isInstanceOf(ValidationException.class);
    }
    
    @Test
    @DisplayName("Should throw ValidationException when email format is invalid")
    void shouldThrowValidationExceptionWhenEmailFormatInvalid() {
        // Given
        userAssignmentRequest.setAssignedUserEmail("invalid-email");
        
        when(authorizationService.hasPermission(userId.toString(), Action.ALLOCATE_ASSET))
            .thenReturn(true);
        
        // When/Then
        assertThatThrownBy(() -> 
            allocationService.assignToUser(userId.toString(), assetId, userAssignmentRequest))
            .isInstanceOf(ValidationException.class)
            .hasMessageContaining("Invalid email format");
    }
    
    @Test
    @DisplayName("Should throw ValidationException when email is missing for user assignment")
    void shouldThrowValidationExceptionWhenEmailMissing() {
        // Given
        userAssignmentRequest.setAssignedUserEmail(null);
        
        when(authorizationService.hasPermission(userId.toString(), Action.ALLOCATE_ASSET))
            .thenReturn(true);
        
        // When/Then
        assertThatThrownBy(() -> 
            allocationService.assignToUser(userId.toString(), assetId, userAssignmentRequest))
            .isInstanceOf(ValidationException.class)
            .hasMessageContaining("email is required");
    }
    
    @Test
    @DisplayName("Should throw AssetAlreadyAssignedException when asset has assigned user field set")
    void shouldThrowExceptionWhenAssetHasAssignedUserField() {
        // Given
        testAsset.setAssignedUser("Existing User");
        
        when(authorizationService.hasPermission(userId.toString(), Action.ALLOCATE_ASSET))
            .thenReturn(true);
        when(assetRepository.findById(assetId)).thenReturn(Optional.of(testAsset));
        
        // When/Then
        assertThatThrownBy(() -> 
            allocationService.assignToUser(userId.toString(), assetId, userAssignmentRequest))
            .isInstanceOf(AssetAlreadyAssignedException.class)
            .hasMessageContaining("already assigned");
    }
    
    @Test
    @DisplayName("Should throw AssetAlreadyAssignedException when asset has location field set")
    void shouldThrowExceptionWhenAssetHasLocationField() {
        // Given
        testAsset.setLocation("Data Center A");
        
        when(authorizationService.hasPermission(userId.toString(), Action.ALLOCATE_ASSET))
            .thenReturn(true);
        when(assetRepository.findById(assetId)).thenReturn(Optional.of(testAsset));
        
        // When/Then
        assertThatThrownBy(() -> 
            allocationService.assignToUser(userId.toString(), assetId, userAssignmentRequest))
            .isInstanceOf(AssetAlreadyAssignedException.class)
            .hasMessageContaining("already assigned");
    }
    
    @Test
    @DisplayName("Should throw ValidationException when assignment type mismatch for user assignment")
    void shouldThrowValidationExceptionWhenAssignmentTypeMismatch() {
        // Given
        userAssignmentRequest.setAssignmentType(AssignmentType.LOCATION);
        
        when(authorizationService.hasPermission(userId.toString(), Action.ALLOCATE_ASSET))
            .thenReturn(true);
        
        // When/Then
        assertThatThrownBy(() -> 
            allocationService.assignToUser(userId.toString(), assetId, userAssignmentRequest))
            .isInstanceOf(ValidationException.class)
            .hasMessageContaining("Assignment type must be USER");
    }
    
    @Test
    @DisplayName("Should throw ValidationException when assigned to exceeds max length")
    void shouldThrowValidationExceptionWhenAssignedToTooLong() {
        // Given
        String longName = "A".repeat(256);
        userAssignmentRequest.setAssignedTo(longName);
        
        when(authorizationService.hasPermission(userId.toString(), Action.ALLOCATE_ASSET))
            .thenReturn(true);
        
        // When/Then
        assertThatThrownBy(() -> 
            allocationService.assignToUser(userId.toString(), assetId, userAssignmentRequest))
            .isInstanceOf(ValidationException.class)
            .hasMessageContaining("must not exceed 255 characters");
    }
    
    @Test
    @DisplayName("Should verify audit log contains correct metadata for user assignment")
    void shouldVerifyAuditLogMetadataForUserAssignment() {
        // Given
        when(authorizationService.hasPermission(userId.toString(), Action.ALLOCATE_ASSET))
            .thenReturn(true);
        when(assetRepository.findById(assetId)).thenReturn(Optional.of(testAsset));
        when(assignmentHistoryRepository.findActiveAssignmentsByAssetId(assetId))
            .thenReturn(Collections.emptyList());
        when(assignmentHistoryRepository.save(any(AssignmentHistory.class)))
            .thenAnswer(invocation -> {
                AssignmentHistory assignment = invocation.getArgument(0);
                assignment.setId(UUID.randomUUID());
                return assignment;
            });
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        
        // When
        allocationService.assignToUser(userId.toString(), assetId, userAssignmentRequest);
        
        // Then
        ArgumentCaptor<AuditEventDTO> auditCaptor = ArgumentCaptor.forClass(AuditEventDTO.class);
        verify(auditService).logEvent(auditCaptor.capture());
        AuditEventDTO auditEvent = auditCaptor.getValue();
        
        assertThat(auditEvent.getUserId()).isEqualTo(userId);
        assertThat(auditEvent.getActionType()).isEqualTo(Action.CREATE_ASSET);
        assertThat(auditEvent.getResourceType()).isEqualTo("ASSIGNMENT");
        assertThat(auditEvent.getMetadata()).containsEntry("assetId", assetId.toString());
        assertThat(auditEvent.getMetadata()).containsEntry("assignmentType", "USER");
        assertThat(auditEvent.getMetadata()).containsEntry("assignedTo", "John Doe");
        assertThat(auditEvent.getMetadata()).containsEntry("assignedUserEmail", "john.doe@example.com");
    }
    
    @Test
    @DisplayName("Should verify audit log contains correct metadata for location assignment")
    void shouldVerifyAuditLogMetadataForLocationAssignment() {
        // Given
        when(authorizationService.hasPermission(userId.toString(), Action.ALLOCATE_ASSET))
            .thenReturn(true);
        when(assetRepository.findById(assetId)).thenReturn(Optional.of(testAsset));
        when(assignmentHistoryRepository.findActiveAssignmentsByAssetId(assetId))
            .thenReturn(Collections.emptyList());
        when(assignmentHistoryRepository.save(any(AssignmentHistory.class)))
            .thenAnswer(invocation -> {
                AssignmentHistory assignment = invocation.getArgument(0);
                assignment.setId(UUID.randomUUID());
                return assignment;
            });
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        
        // When
        allocationService.assignToLocation(userId.toString(), assetId, locationAssignmentRequest);
        
        // Then
        ArgumentCaptor<AuditEventDTO> auditCaptor = ArgumentCaptor.forClass(AuditEventDTO.class);
        verify(auditService).logEvent(auditCaptor.capture());
        AuditEventDTO auditEvent = auditCaptor.getValue();
        
        assertThat(auditEvent.getMetadata()).containsEntry("assignmentType", "LOCATION");
        assertThat(auditEvent.getMetadata()).containsEntry("assignedTo", "Data Center A");
        assertThat(auditEvent.getMetadata()).doesNotContainKey("assignedUserEmail");
    }
    
    @Test
    @DisplayName("Should handle all assignable statuses correctly")
    void shouldHandleAllAssignableStatusesCorrectly() {
        // Given
        List<LifecycleStatus> assignableStatuses = Arrays.asList(
            LifecycleStatus.IN_USE,
            LifecycleStatus.DEPLOYED,
            LifecycleStatus.STORAGE
        );
        
        when(authorizationService.hasPermission(userId.toString(), Action.ALLOCATE_ASSET))
            .thenReturn(true);
        when(assignmentHistoryRepository.findActiveAssignmentsByAssetId(assetId))
            .thenReturn(Collections.emptyList());
        when(assignmentHistoryRepository.save(any(AssignmentHistory.class)))
            .thenAnswer(invocation -> {
                AssignmentHistory assignment = invocation.getArgument(0);
                assignment.setId(UUID.randomUUID());
                return assignment;
            });
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        
        // When/Then - Test each assignable status
        for (LifecycleStatus status : assignableStatuses) {
            testAsset.setStatus(status);
            testAsset.setAssignedUser(null);
            testAsset.setLocation(null);
            
            when(assetRepository.findById(assetId)).thenReturn(Optional.of(testAsset));
            
            assertThatCode(() -> 
                allocationService.assignToUser(userId.toString(), assetId, userAssignmentRequest))
                .doesNotThrowAnyException();
        }
    }
    
    @Test
    @DisplayName("Should reject all non-assignable statuses")
    void shouldRejectAllNonAssignableStatuses() {
        // Given
        List<LifecycleStatus> nonAssignableStatuses = Arrays.asList(
            LifecycleStatus.ORDERED,
            LifecycleStatus.RECEIVED,
            LifecycleStatus.MAINTENANCE,
            LifecycleStatus.RETIRED
        );
        
        when(authorizationService.hasPermission(userId.toString(), Action.ALLOCATE_ASSET))
            .thenReturn(true);
        when(assetRepository.findById(assetId)).thenReturn(Optional.of(testAsset));
        when(assignmentHistoryRepository.findActiveAssignmentsByAssetId(assetId))
            .thenReturn(Collections.emptyList());
        
        // When/Then - Test each non-assignable status
        for (LifecycleStatus status : nonAssignableStatuses) {
            testAsset.setStatus(status);
            
            assertThatThrownBy(() -> 
                allocationService.assignToUser(userId.toString(), assetId, userAssignmentRequest))
                .isInstanceOf(AssetNotAssignableException.class)
                .hasMessageContaining(status.toString());
        }
    }
    
    @Test
    @DisplayName("Should set assignment date when assigning to user")
    void shouldSetAssignmentDateWhenAssigningToUser() {
        // Given
        when(authorizationService.hasPermission(userId.toString(), Action.ALLOCATE_ASSET))
            .thenReturn(true);
        when(assetRepository.findById(assetId)).thenReturn(Optional.of(testAsset));
        when(assignmentHistoryRepository.findActiveAssignmentsByAssetId(assetId))
            .thenReturn(Collections.emptyList());
        when(assignmentHistoryRepository.save(any(AssignmentHistory.class)))
            .thenAnswer(invocation -> {
                AssignmentHistory assignment = invocation.getArgument(0);
                assignment.setId(UUID.randomUUID());
                return assignment;
            });
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        
        LocalDateTime beforeAssignment = LocalDateTime.now();
        
        // When
        allocationService.assignToUser(userId.toString(), assetId, userAssignmentRequest);
        
        LocalDateTime afterAssignment = LocalDateTime.now();
        
        // Then
        ArgumentCaptor<Asset> assetCaptor = ArgumentCaptor.forClass(Asset.class);
        verify(assetRepository).save(assetCaptor.capture());
        Asset savedAsset = assetCaptor.getValue();
        
        assertThat(savedAsset.getAssignmentDate()).isNotNull();
        assertThat(savedAsset.getAssignmentDate()).isBetween(beforeAssignment, afterAssignment);
        assertThat(savedAsset.getLocationUpdateDate()).isNull();
    }
    
    @Test
    @DisplayName("Should set location update date when assigning to location")
    void shouldSetLocationUpdateDateWhenAssigningToLocation() {
        // Given
        when(authorizationService.hasPermission(userId.toString(), Action.ALLOCATE_ASSET))
            .thenReturn(true);
        when(assetRepository.findById(assetId)).thenReturn(Optional.of(testAsset));
        when(assignmentHistoryRepository.findActiveAssignmentsByAssetId(assetId))
            .thenReturn(Collections.emptyList());
        when(assignmentHistoryRepository.save(any(AssignmentHistory.class)))
            .thenAnswer(invocation -> {
                AssignmentHistory assignment = invocation.getArgument(0);
                assignment.setId(UUID.randomUUID());
                return assignment;
            });
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        
        LocalDateTime beforeAssignment = LocalDateTime.now();
        
        // When
        allocationService.assignToLocation(userId.toString(), assetId, locationAssignmentRequest);
        
        LocalDateTime afterAssignment = LocalDateTime.now();
        
        // Then
        ArgumentCaptor<Asset> assetCaptor = ArgumentCaptor.forClass(Asset.class);
        verify(assetRepository).save(assetCaptor.capture());
        Asset savedAsset = assetCaptor.getValue();
        
        assertThat(savedAsset.getLocationUpdateDate()).isNotNull();
        assertThat(savedAsset.getLocationUpdateDate()).isBetween(beforeAssignment, afterAssignment);
        assertThat(savedAsset.getAssignmentDate()).isNull();
    }
    
    @Test
    @DisplayName("Should not update asset fields when authorization fails")
    void shouldNotUpdateAssetFieldsWhenAuthorizationFails() {
        // Given
        when(authorizationService.hasPermission(userId.toString(), Action.ALLOCATE_ASSET))
            .thenReturn(false);
        
        // When/Then
        assertThatThrownBy(() -> 
            allocationService.assignToUser(userId.toString(), assetId, userAssignmentRequest))
            .isInstanceOf(InsufficientPermissionsException.class);
        
        verify(assetRepository, never()).findById(any());
        verify(assetRepository, never()).save(any());
        verify(assignmentHistoryRepository, never()).save(any());
        verify(auditService, never()).logEvent(any());
    }
    
    @Test
    @DisplayName("Should not update asset fields when validation fails")
    void shouldNotUpdateAssetFieldsWhenValidationFails() {
        // Given
        userAssignmentRequest.setAssignedTo(null);
        
        when(authorizationService.hasPermission(userId.toString(), Action.ALLOCATE_ASSET))
            .thenReturn(true);
        
        // When/Then
        assertThatThrownBy(() -> 
            allocationService.assignToUser(userId.toString(), assetId, userAssignmentRequest))
            .isInstanceOf(ValidationException.class);
        
        verify(assetRepository, never()).findById(any());
        verify(assetRepository, never()).save(any());
        verify(assignmentHistoryRepository, never()).save(any());
        verify(auditService, never()).logEvent(any());
    }
    
    @Test
    @DisplayName("Should include assigned by username in assignment DTO")
    void shouldIncludeAssignedByUsernameInAssignmentDTO() {
        // Given
        when(authorizationService.hasPermission(userId.toString(), Action.ALLOCATE_ASSET))
            .thenReturn(true);
        when(assetRepository.findById(assetId)).thenReturn(Optional.of(testAsset));
        when(assignmentHistoryRepository.findActiveAssignmentsByAssetId(assetId))
            .thenReturn(Collections.emptyList());
        when(assignmentHistoryRepository.save(any(AssignmentHistory.class)))
            .thenAnswer(invocation -> {
                AssignmentHistory assignment = invocation.getArgument(0);
                assignment.setId(UUID.randomUUID());
                return assignment;
            });
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        
        // When
        AssignmentDTO result = allocationService.assignToUser(userId.toString(), assetId, userAssignmentRequest);
        
        // Then
        assertThat(result.getAssignedByUsername()).isEqualTo("testuser");
    }
    
    @Test
    @DisplayName("Should handle missing user gracefully in DTO mapping")
    void shouldHandleMissingUserGracefullyInDTOMapping() {
        // Given
        when(authorizationService.hasPermission(userId.toString(), Action.ALLOCATE_ASSET))
            .thenReturn(true);
        when(assetRepository.findById(assetId)).thenReturn(Optional.of(testAsset));
        when(assignmentHistoryRepository.findActiveAssignmentsByAssetId(assetId))
            .thenReturn(Collections.emptyList());
        when(assignmentHistoryRepository.save(any(AssignmentHistory.class)))
            .thenAnswer(invocation -> {
                AssignmentHistory assignment = invocation.getArgument(0);
                assignment.setId(UUID.randomUUID());
                return assignment;
            });
        when(userRepository.findById(userId)).thenReturn(Optional.empty());
        
        // When
        AssignmentDTO result = allocationService.assignToUser(userId.toString(), assetId, userAssignmentRequest);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.getAssignedByUsername()).isNull();
    }
    
    @Test
    @DisplayName("Should verify assignment history record has correct assigned by field")
    void shouldVerifyAssignmentHistoryHasCorrectAssignedByField() {
        // Given
        when(authorizationService.hasPermission(userId.toString(), Action.ALLOCATE_ASSET))
            .thenReturn(true);
        when(assetRepository.findById(assetId)).thenReturn(Optional.of(testAsset));
        when(assignmentHistoryRepository.findActiveAssignmentsByAssetId(assetId))
            .thenReturn(Collections.emptyList());
        when(assignmentHistoryRepository.save(any(AssignmentHistory.class)))
            .thenAnswer(invocation -> {
                AssignmentHistory assignment = invocation.getArgument(0);
                assignment.setId(UUID.randomUUID());
                return assignment;
            });
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        
        // When
        allocationService.assignToUser(userId.toString(), assetId, userAssignmentRequest);
        
        // Then
        ArgumentCaptor<AssignmentHistory> assignmentCaptor = ArgumentCaptor.forClass(AssignmentHistory.class);
        verify(assignmentHistoryRepository).save(assignmentCaptor.capture());
        AssignmentHistory savedAssignment = assignmentCaptor.getValue();
        
        assertThat(savedAssignment.getAssignedBy()).isEqualTo(userId);
        assertThat(savedAssignment.getAssignedAt()).isNotNull();
        assertThat(savedAssignment.getUnassignedAt()).isNull();
    }
    
    @Test
    @DisplayName("Should update asset updatedAt timestamp on assignment")
    void shouldUpdateAssetUpdatedAtTimestampOnAssignment() {
        // Given
        when(authorizationService.hasPermission(userId.toString(), Action.ALLOCATE_ASSET))
            .thenReturn(true);
        when(assetRepository.findById(assetId)).thenReturn(Optional.of(testAsset));
        when(assignmentHistoryRepository.findActiveAssignmentsByAssetId(assetId))
            .thenReturn(Collections.emptyList());
        when(assignmentHistoryRepository.save(any(AssignmentHistory.class)))
            .thenAnswer(invocation -> {
                AssignmentHistory assignment = invocation.getArgument(0);
                assignment.setId(UUID.randomUUID());
                return assignment;
            });
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        
        LocalDateTime beforeAssignment = LocalDateTime.now();
        
        // When
        allocationService.assignToUser(userId.toString(), assetId, userAssignmentRequest);
        
        LocalDateTime afterAssignment = LocalDateTime.now();
        
        // Then
        ArgumentCaptor<Asset> assetCaptor = ArgumentCaptor.forClass(Asset.class);
        verify(assetRepository).save(assetCaptor.capture());
        Asset savedAsset = assetCaptor.getValue();
        
        assertThat(savedAsset.getUpdatedAt()).isNotNull();
        assertThat(savedAsset.getUpdatedAt()).isBetween(beforeAssignment, afterAssignment);
    }
    
    // Task 3.3: Deallocation Operations Tests
    
    @Test
    @DisplayName("Should deallocate asset when authorized and assigned")
    void shouldDeallocateAssetWhenAuthorizedAndAssigned() {
        // Given
        AssignmentHistory activeAssignment = new AssignmentHistory();
        activeAssignment.setId(UUID.randomUUID());
        activeAssignment.setAssetId(assetId);
        activeAssignment.setAssignmentType(AssignmentType.USER);
        activeAssignment.setAssignedTo("John Doe");
        activeAssignment.setAssignedBy(userId);
        activeAssignment.setAssignedAt(LocalDateTime.now());
        
        testAsset.setAssignedUser("John Doe");
        testAsset.setAssignedUserEmail("john.doe@example.com");
        
        when(authorizationService.hasPermission(userId.toString(), Action.DEALLOCATE_ASSET))
            .thenReturn(true);
        when(assetRepository.findById(assetId)).thenReturn(Optional.of(testAsset));
        when(assignmentHistoryRepository.findActiveAssignmentsByAssetId(assetId))
            .thenReturn(Collections.singletonList(activeAssignment));
        
        // When
        allocationService.deallocate(userId.toString(), assetId);
        
        // Then
        // Verify assignment was closed
        ArgumentCaptor<AssignmentHistory> assignmentCaptor = ArgumentCaptor.forClass(AssignmentHistory.class);
        verify(assignmentHistoryRepository).save(assignmentCaptor.capture());
        AssignmentHistory closedAssignment = assignmentCaptor.getValue();
        assertThat(closedAssignment.getUnassignedAt()).isNotNull();
        
        // Verify asset fields were cleared
        ArgumentCaptor<Asset> assetCaptor = ArgumentCaptor.forClass(Asset.class);
        verify(assetRepository).save(assetCaptor.capture());
        Asset savedAsset = assetCaptor.getValue();
        assertThat(savedAsset.getAssignedUser()).isNull();
        assertThat(savedAsset.getAssignedUserEmail()).isNull();
        assertThat(savedAsset.getLocation()).isNull();
        assertThat(savedAsset.getAssignmentDate()).isNull();
        assertThat(savedAsset.getLocationUpdateDate()).isNull();
        
        // Verify audit log was created
        verify(auditService).logEvent(any(AuditEventDTO.class));
    }
    
    @Test
    @DisplayName("Should throw AssetNotAssignedException when asset is not assigned")
    void shouldThrowExceptionWhenAssetNotAssigned() {
        // Given
        when(authorizationService.hasPermission(userId.toString(), Action.DEALLOCATE_ASSET))
            .thenReturn(true);
        when(assetRepository.findById(assetId)).thenReturn(Optional.of(testAsset));
        when(assignmentHistoryRepository.findActiveAssignmentsByAssetId(assetId))
            .thenReturn(Collections.emptyList());
        
        // When/Then
        assertThatThrownBy(() -> 
            allocationService.deallocate(userId.toString(), assetId))
            .isInstanceOf(AssetNotAssignedException.class)
            .hasMessageContaining("not currently assigned");
    }
    
    @Test
    @DisplayName("Should throw InsufficientPermissionsException when user lacks deallocation permission")
    void shouldThrowExceptionWhenUserLacksDeallocationPermission() {
        // Given
        when(authorizationService.hasPermission(userId.toString(), Action.DEALLOCATE_ASSET))
            .thenReturn(false);
        
        // When/Then
        assertThatThrownBy(() -> 
            allocationService.deallocate(userId.toString(), assetId))
            .isInstanceOf(InsufficientPermissionsException.class);
        
        verify(assetRepository, never()).findById(any());
        verify(assetRepository, never()).save(any());
        verify(assignmentHistoryRepository, never()).save(any());
        verify(auditService, never()).logEvent(any());
    }
    
    @Test
    @DisplayName("Should throw ResourceNotFoundException when asset does not exist for deallocation")
    void shouldThrowExceptionWhenAssetNotFoundForDeallocation() {
        // Given
        when(authorizationService.hasPermission(userId.toString(), Action.DEALLOCATE_ASSET))
            .thenReturn(true);
        when(assetRepository.findById(assetId)).thenReturn(Optional.empty());
        
        // When/Then
        assertThatThrownBy(() -> 
            allocationService.deallocate(userId.toString(), assetId))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining(assetId.toString());
        
        verify(assignmentHistoryRepository, never()).save(any());
        verify(auditService, never()).logEvent(any());
    }
    
    @Test
    @DisplayName("Should set UnassignedAt timestamp when deallocating")
    void shouldSetUnassignedAtTimestampWhenDeallocating() {
        // Given
        AssignmentHistory activeAssignment = new AssignmentHistory();
        activeAssignment.setId(UUID.randomUUID());
        activeAssignment.setAssetId(assetId);
        activeAssignment.setAssignmentType(AssignmentType.USER);
        activeAssignment.setAssignedTo("John Doe");
        activeAssignment.setAssignedBy(userId);
        activeAssignment.setAssignedAt(LocalDateTime.now().minusDays(1));
        
        testAsset.setAssignedUser("John Doe");
        
        when(authorizationService.hasPermission(userId.toString(), Action.DEALLOCATE_ASSET))
            .thenReturn(true);
        when(assetRepository.findById(assetId)).thenReturn(Optional.of(testAsset));
        when(assignmentHistoryRepository.findActiveAssignmentsByAssetId(assetId))
            .thenReturn(Collections.singletonList(activeAssignment));
        
        LocalDateTime beforeDeallocation = LocalDateTime.now();
        
        // When
        allocationService.deallocate(userId.toString(), assetId);
        
        LocalDateTime afterDeallocation = LocalDateTime.now();
        
        // Then
        ArgumentCaptor<AssignmentHistory> assignmentCaptor = ArgumentCaptor.forClass(AssignmentHistory.class);
        verify(assignmentHistoryRepository).save(assignmentCaptor.capture());
        AssignmentHistory closedAssignment = assignmentCaptor.getValue();
        
        assertThat(closedAssignment.getUnassignedAt()).isNotNull();
        assertThat(closedAssignment.getUnassignedAt()).isBetween(beforeDeallocation, afterDeallocation);
    }
    
    @Test
    @DisplayName("Should clear all asset assignment fields when deallocating user assignment")
    void shouldClearAllAssetFieldsWhenDeallocatingUserAssignment() {
        // Given
        AssignmentHistory activeAssignment = new AssignmentHistory();
        activeAssignment.setId(UUID.randomUUID());
        activeAssignment.setAssetId(assetId);
        activeAssignment.setAssignmentType(AssignmentType.USER);
        activeAssignment.setAssignedTo("John Doe");
        activeAssignment.setAssignedBy(userId);
        activeAssignment.setAssignedAt(LocalDateTime.now());
        
        testAsset.setAssignedUser("John Doe");
        testAsset.setAssignedUserEmail("john.doe@example.com");
        testAsset.setAssignmentDate(LocalDateTime.now());
        
        when(authorizationService.hasPermission(userId.toString(), Action.DEALLOCATE_ASSET))
            .thenReturn(true);
        when(assetRepository.findById(assetId)).thenReturn(Optional.of(testAsset));
        when(assignmentHistoryRepository.findActiveAssignmentsByAssetId(assetId))
            .thenReturn(Collections.singletonList(activeAssignment));
        
        // When
        allocationService.deallocate(userId.toString(), assetId);
        
        // Then
        ArgumentCaptor<Asset> assetCaptor = ArgumentCaptor.forClass(Asset.class);
        verify(assetRepository).save(assetCaptor.capture());
        Asset savedAsset = assetCaptor.getValue();
        
        assertThat(savedAsset.getAssignedUser()).isNull();
        assertThat(savedAsset.getAssignedUserEmail()).isNull();
        assertThat(savedAsset.getAssignmentDate()).isNull();
    }
    
    @Test
    @DisplayName("Should clear all asset location fields when deallocating location assignment")
    void shouldClearAllAssetLocationFieldsWhenDeallocatingLocationAssignment() {
        // Given
        AssignmentHistory activeAssignment = new AssignmentHistory();
        activeAssignment.setId(UUID.randomUUID());
        activeAssignment.setAssetId(assetId);
        activeAssignment.setAssignmentType(AssignmentType.LOCATION);
        activeAssignment.setAssignedTo("Data Center A");
        activeAssignment.setAssignedBy(userId);
        activeAssignment.setAssignedAt(LocalDateTime.now());
        
        testAsset.setLocation("Data Center A");
        testAsset.setLocationUpdateDate(LocalDateTime.now());
        
        when(authorizationService.hasPermission(userId.toString(), Action.DEALLOCATE_ASSET))
            .thenReturn(true);
        when(assetRepository.findById(assetId)).thenReturn(Optional.of(testAsset));
        when(assignmentHistoryRepository.findActiveAssignmentsByAssetId(assetId))
            .thenReturn(Collections.singletonList(activeAssignment));
        
        // When
        allocationService.deallocate(userId.toString(), assetId);
        
        // Then
        ArgumentCaptor<Asset> assetCaptor = ArgumentCaptor.forClass(Asset.class);
        verify(assetRepository).save(assetCaptor.capture());
        Asset savedAsset = assetCaptor.getValue();
        
        assertThat(savedAsset.getLocation()).isNull();
        assertThat(savedAsset.getLocationUpdateDate()).isNull();
    }
    
    @Test
    @DisplayName("Should update asset updatedAt timestamp when deallocating")
    void shouldUpdateAssetUpdatedAtTimestampWhenDeallocating() {
        // Given
        AssignmentHistory activeAssignment = new AssignmentHistory();
        activeAssignment.setId(UUID.randomUUID());
        activeAssignment.setAssetId(assetId);
        activeAssignment.setAssignmentType(AssignmentType.USER);
        activeAssignment.setAssignedTo("John Doe");
        activeAssignment.setAssignedBy(userId);
        activeAssignment.setAssignedAt(LocalDateTime.now());
        
        testAsset.setAssignedUser("John Doe");
        
        when(authorizationService.hasPermission(userId.toString(), Action.DEALLOCATE_ASSET))
            .thenReturn(true);
        when(assetRepository.findById(assetId)).thenReturn(Optional.of(testAsset));
        when(assignmentHistoryRepository.findActiveAssignmentsByAssetId(assetId))
            .thenReturn(Collections.singletonList(activeAssignment));
        
        LocalDateTime beforeDeallocation = LocalDateTime.now();
        
        // When
        allocationService.deallocate(userId.toString(), assetId);
        
        LocalDateTime afterDeallocation = LocalDateTime.now();
        
        // Then
        ArgumentCaptor<Asset> assetCaptor = ArgumentCaptor.forClass(Asset.class);
        verify(assetRepository).save(assetCaptor.capture());
        Asset savedAsset = assetCaptor.getValue();
        
        assertThat(savedAsset.getUpdatedAt()).isNotNull();
        assertThat(savedAsset.getUpdatedAt()).isBetween(beforeDeallocation, afterDeallocation);
    }
    
    @Test
    @DisplayName("Should log deallocation to audit service with correct metadata")
    void shouldLogDeallocationToAuditServiceWithCorrectMetadata() {
        // Given
        UUID assignmentId = UUID.randomUUID();
        AssignmentHistory activeAssignment = new AssignmentHistory();
        activeAssignment.setId(assignmentId);
        activeAssignment.setAssetId(assetId);
        activeAssignment.setAssignmentType(AssignmentType.USER);
        activeAssignment.setAssignedTo("John Doe");
        activeAssignment.setAssignedBy(userId);
        activeAssignment.setAssignedAt(LocalDateTime.now());
        
        testAsset.setAssignedUser("John Doe");
        
        when(authorizationService.hasPermission(userId.toString(), Action.DEALLOCATE_ASSET))
            .thenReturn(true);
        when(assetRepository.findById(assetId)).thenReturn(Optional.of(testAsset));
        when(assignmentHistoryRepository.findActiveAssignmentsByAssetId(assetId))
            .thenReturn(Collections.singletonList(activeAssignment));
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        
        // When
        allocationService.deallocate(userId.toString(), assetId);
        
        // Then
        ArgumentCaptor<AuditEventDTO> auditCaptor = ArgumentCaptor.forClass(AuditEventDTO.class);
        verify(auditService).logEvent(auditCaptor.capture());
        AuditEventDTO auditEvent = auditCaptor.getValue();
        
        assertThat(auditEvent.getUserId()).isEqualTo(userId);
        assertThat(auditEvent.getActionType()).isEqualTo(Action.DELETE_ASSET);
        assertThat(auditEvent.getResourceType()).isEqualTo("ASSIGNMENT");
        assertThat(auditEvent.getResourceId()).isEqualTo(assignmentId.toString());
        assertThat(auditEvent.getMetadata()).containsEntry("assetId", assetId.toString());
        assertThat(auditEvent.getMetadata()).containsEntry("assignmentType", "USER");
        assertThat(auditEvent.getMetadata()).containsEntry("assignedTo", "John Doe");
    }
    
    @Test
    @DisplayName("Should handle deallocation of location assignment correctly")
    void shouldHandleDeallocationOfLocationAssignmentCorrectly() {
        // Given
        AssignmentHistory activeAssignment = new AssignmentHistory();
        activeAssignment.setId(UUID.randomUUID());
        activeAssignment.setAssetId(assetId);
        activeAssignment.setAssignmentType(AssignmentType.LOCATION);
        activeAssignment.setAssignedTo("Data Center A");
        activeAssignment.setAssignedBy(userId);
        activeAssignment.setAssignedAt(LocalDateTime.now());
        
        testAsset.setLocation("Data Center A");
        testAsset.setLocationUpdateDate(LocalDateTime.now());
        
        when(authorizationService.hasPermission(userId.toString(), Action.DEALLOCATE_ASSET))
            .thenReturn(true);
        when(assetRepository.findById(assetId)).thenReturn(Optional.of(testAsset));
        when(assignmentHistoryRepository.findActiveAssignmentsByAssetId(assetId))
            .thenReturn(Collections.singletonList(activeAssignment));
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        
        // When
        allocationService.deallocate(userId.toString(), assetId);
        
        // Then
        ArgumentCaptor<AuditEventDTO> auditCaptor = ArgumentCaptor.forClass(AuditEventDTO.class);
        verify(auditService).logEvent(auditCaptor.capture());
        AuditEventDTO auditEvent = auditCaptor.getValue();
        
        assertThat(auditEvent.getMetadata()).containsEntry("assignmentType", "LOCATION");
        assertThat(auditEvent.getMetadata()).containsEntry("assignedTo", "Data Center A");
    }
    
    @Test
    @DisplayName("Should process bulk deallocate independently")
    void shouldProcessBulkDeallocateIndependently() {
        // Given
        UUID assetId1 = UUID.randomUUID();
        UUID assetId2 = UUID.randomUUID();
        UUID assetId3 = UUID.randomUUID();
        
        Asset asset1 = createTestAsset(assetId1);
        Asset asset2 = createTestAsset(assetId2);
        Asset asset3 = createTestAsset(assetId3);
        
        AssignmentHistory assignment1 = createTestAssignment(assetId1);
        AssignmentHistory assignment2 = createTestAssignment(assetId2);
        
        when(authorizationService.hasPermission(userId.toString(), Action.DEALLOCATE_ASSET))
            .thenReturn(true);
        
        // Asset 1: Success
        when(assetRepository.findById(assetId1)).thenReturn(Optional.of(asset1));
        when(assignmentHistoryRepository.findActiveAssignmentsByAssetId(assetId1))
            .thenReturn(Collections.singletonList(assignment1));
        when(assignmentHistoryRepository.findByAssetIdOrderByAssignedAtDesc(eq(assetId1), any(Pageable.class)))
            .thenReturn(new PageImpl<>(Collections.singletonList(assignment1)));
        
        // Asset 2: Success
        when(assetRepository.findById(assetId2)).thenReturn(Optional.of(asset2));
        when(assignmentHistoryRepository.findActiveAssignmentsByAssetId(assetId2))
            .thenReturn(Collections.singletonList(assignment2));
        when(assignmentHistoryRepository.findByAssetIdOrderByAssignedAtDesc(eq(assetId2), any(Pageable.class)))
            .thenReturn(new PageImpl<>(Collections.singletonList(assignment2)));
        
        // Asset 3: Failure (not assigned)
        when(assetRepository.findById(assetId3)).thenReturn(Optional.of(asset3));
        when(assignmentHistoryRepository.findActiveAssignmentsByAssetId(assetId3))
            .thenReturn(Collections.emptyList());
        
        // When
        BulkDeallocationResult result = allocationService.bulkDeallocate(
            userId.toString(), 
            Arrays.asList(assetId1, assetId2, assetId3)
        );
        
        // Then
        assertThat(result.getTotalRequested()).isEqualTo(3);
        assertThat(result.getSuccessCount()).isEqualTo(2);
        assertThat(result.getFailureCount()).isEqualTo(1);
        assertThat(result.getSuccessfulDeallocations()).hasSize(2);
        assertThat(result.getFailedDeallocations()).hasSize(1);
        
        // Verify successful deallocations
        assertThat(result.getSuccessfulDeallocations())
            .extracting(BulkDeallocationResult.DeallocationSuccess::getAssetId)
            .containsExactlyInAnyOrder(assetId1, assetId2);
        
        // Verify failed deallocation
        assertThat(result.getFailedDeallocations())
            .extracting(BulkDeallocationResult.DeallocationFailure::getAssetId)
            .containsExactly(assetId3);
        assertThat(result.getFailedDeallocations().get(0).getErrorType())
            .isEqualTo("ASSET_NOT_ASSIGNED");
    }
    
    @Test
    @DisplayName("Should throw ValidationException when bulk deallocation exceeds maximum size")
    void shouldThrowValidationExceptionWhenBulkDeallocateExceedsMaxSize() {
        // Given
        List<UUID> tooManyAssets = new ArrayList<>();
        for (int i = 0; i < 51; i++) {
            tooManyAssets.add(UUID.randomUUID());
        }
        
        when(authorizationService.hasPermission(userId.toString(), Action.DEALLOCATE_ASSET))
            .thenReturn(true);
        
        // When/Then
        assertThatThrownBy(() -> 
            allocationService.bulkDeallocate(userId.toString(), tooManyAssets))
            .isInstanceOf(ValidationException.class)
            .hasMessageContaining("limited to 50 assets");
        
        verify(assetRepository, never()).findById(any());
        verify(assignmentHistoryRepository, never()).save(any());
    }
    
    @Test
    @DisplayName("Should throw InsufficientPermissionsException for bulk deallocation when user lacks permission")
    void shouldThrowExceptionForBulkDeallocateWhenUserLacksPermission() {
        // Given
        List<UUID> assetIds = Arrays.asList(UUID.randomUUID(), UUID.randomUUID());
        
        when(authorizationService.hasPermission(userId.toString(), Action.DEALLOCATE_ASSET))
            .thenReturn(false);
        
        // When/Then
        assertThatThrownBy(() -> 
            allocationService.bulkDeallocate(userId.toString(), assetIds))
            .isInstanceOf(InsufficientPermissionsException.class);
        
        verify(assetRepository, never()).findById(any());
        verify(assignmentHistoryRepository, never()).save(any());
    }
    
    @Test
    @DisplayName("Should collect all failures in bulk deallocation result")
    void shouldCollectAllFailuresInBulkDeallocationResult() {
        // Given
        UUID assetId1 = UUID.randomUUID();
        UUID assetId2 = UUID.randomUUID();
        UUID assetId3 = UUID.randomUUID();
        
        Asset asset1 = createTestAsset(assetId1);
        Asset asset2 = createTestAsset(assetId2);
        
        when(authorizationService.hasPermission(userId.toString(), Action.DEALLOCATE_ASSET))
            .thenReturn(true);
        
        // Asset 1: Failure (not found)
        when(assetRepository.findById(assetId1)).thenReturn(Optional.empty());
        
        // Asset 2: Failure (not assigned)
        when(assetRepository.findById(assetId2)).thenReturn(Optional.of(asset1));
        when(assignmentHistoryRepository.findActiveAssignmentsByAssetId(assetId2))
            .thenReturn(Collections.emptyList());
        
        // Asset 3: Failure (not found)
        when(assetRepository.findById(assetId3)).thenReturn(Optional.empty());
        
        // When
        BulkDeallocationResult result = allocationService.bulkDeallocate(
            userId.toString(), 
            Arrays.asList(assetId1, assetId2, assetId3)
        );
        
        // Then
        assertThat(result.getTotalRequested()).isEqualTo(3);
        assertThat(result.getSuccessCount()).isEqualTo(0);
        assertThat(result.getFailureCount()).isEqualTo(3);
        assertThat(result.getFailedDeallocations()).hasSize(3);
        
        // Verify error types
        assertThat(result.getFailedDeallocations())
            .extracting(BulkDeallocationResult.DeallocationFailure::getErrorType)
            .contains("ASSET_NOT_FOUND", "ASSET_NOT_ASSIGNED");
    }
    
    @Test
    @DisplayName("Should continue processing remaining assets after failure in bulk deallocation")
    void shouldContinueProcessingAfterFailureInBulkDeallocation() {
        // Given
        UUID assetId1 = UUID.randomUUID();
        UUID assetId2 = UUID.randomUUID();
        UUID assetId3 = UUID.randomUUID();
        
        Asset asset1 = createTestAsset(assetId1);
        Asset asset3 = createTestAsset(assetId3);
        
        AssignmentHistory assignment1 = createTestAssignment(assetId1);
        AssignmentHistory assignment3 = createTestAssignment(assetId3);
        
        when(authorizationService.hasPermission(userId.toString(), Action.DEALLOCATE_ASSET))
            .thenReturn(true);
        
        // Asset 1: Success
        when(assetRepository.findById(assetId1)).thenReturn(Optional.of(asset1));
        when(assignmentHistoryRepository.findActiveAssignmentsByAssetId(assetId1))
            .thenReturn(Collections.singletonList(assignment1));
        when(assignmentHistoryRepository.findByAssetIdOrderByAssignedAtDesc(eq(assetId1), any(Pageable.class)))
            .thenReturn(new PageImpl<>(Collections.singletonList(assignment1)));
        
        // Asset 2: Failure (not found)
        when(assetRepository.findById(assetId2)).thenReturn(Optional.empty());
        
        // Asset 3: Success
        when(assetRepository.findById(assetId3)).thenReturn(Optional.of(asset3));
        when(assignmentHistoryRepository.findActiveAssignmentsByAssetId(assetId3))
            .thenReturn(Collections.singletonList(assignment3));
        when(assignmentHistoryRepository.findByAssetIdOrderByAssignedAtDesc(eq(assetId3), any(Pageable.class)))
            .thenReturn(new PageImpl<>(Collections.singletonList(assignment3)));
        
        // When
        BulkDeallocationResult result = allocationService.bulkDeallocate(
            userId.toString(), 
            Arrays.asList(assetId1, assetId2, assetId3)
        );
        
        // Then
        assertThat(result.getSuccessCount()).isEqualTo(2);
        assertThat(result.getFailureCount()).isEqualTo(1);
        
        // Verify both successful deallocations were processed
        assertThat(result.getSuccessfulDeallocations())
            .extracting(BulkDeallocationResult.DeallocationSuccess::getAssetId)
            .containsExactlyInAnyOrder(assetId1, assetId3);
    }
    
    @Test
    @DisplayName("Should log each deallocation separately in bulk operation")
    void shouldLogEachDeallocationSeparatelyInBulkOperation() {
        // Given
        UUID assetId1 = UUID.randomUUID();
        UUID assetId2 = UUID.randomUUID();
        
        Asset asset1 = createTestAsset(assetId1);
        Asset asset2 = createTestAsset(assetId2);
        
        AssignmentHistory assignment1 = createTestAssignment(assetId1);
        AssignmentHistory assignment2 = createTestAssignment(assetId2);
        
        when(authorizationService.hasPermission(userId.toString(), Action.DEALLOCATE_ASSET))
            .thenReturn(true);
        
        when(assetRepository.findById(assetId1)).thenReturn(Optional.of(asset1));
        when(assignmentHistoryRepository.findActiveAssignmentsByAssetId(assetId1))
            .thenReturn(Collections.singletonList(assignment1));
        when(assignmentHistoryRepository.findByAssetIdOrderByAssignedAtDesc(eq(assetId1), any(Pageable.class)))
            .thenReturn(new PageImpl<>(Collections.singletonList(assignment1)));
        
        when(assetRepository.findById(assetId2)).thenReturn(Optional.of(asset2));
        when(assignmentHistoryRepository.findActiveAssignmentsByAssetId(assetId2))
            .thenReturn(Collections.singletonList(assignment2));
        when(assignmentHistoryRepository.findByAssetIdOrderByAssignedAtDesc(eq(assetId2), any(Pageable.class)))
            .thenReturn(new PageImpl<>(Collections.singletonList(assignment2)));
        
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        
        // When
        allocationService.bulkDeallocate(userId.toString(), Arrays.asList(assetId1, assetId2));
        
        // Then
        // Verify audit log was called twice (once for each deallocation)
        verify(auditService, times(2)).logEvent(any(AuditEventDTO.class));
    }
    
    @Test
    @DisplayName("Should return correct success and failure counts in bulk deallocation result")
    void shouldReturnCorrectCountsInBulkDeallocationResult() {
        // Given
        UUID assetId1 = UUID.randomUUID();
        UUID assetId2 = UUID.randomUUID();
        UUID assetId3 = UUID.randomUUID();
        UUID assetId4 = UUID.randomUUID();
        
        Asset asset1 = createTestAsset(assetId1);
        Asset asset2 = createTestAsset(assetId2);
        Asset asset3 = createTestAsset(assetId3);
        
        AssignmentHistory assignment1 = createTestAssignment(assetId1);
        AssignmentHistory assignment3 = createTestAssignment(assetId3);
        
        when(authorizationService.hasPermission(userId.toString(), Action.DEALLOCATE_ASSET))
            .thenReturn(true);
        
        // Asset 1: Success
        when(assetRepository.findById(assetId1)).thenReturn(Optional.of(asset1));
        when(assignmentHistoryRepository.findActiveAssignmentsByAssetId(assetId1))
            .thenReturn(Collections.singletonList(assignment1));
        when(assignmentHistoryRepository.findByAssetIdOrderByAssignedAtDesc(eq(assetId1), any(Pageable.class)))
            .thenReturn(new PageImpl<>(Collections.singletonList(assignment1)));
        
        // Asset 2: Failure (not assigned)
        when(assetRepository.findById(assetId2)).thenReturn(Optional.of(asset2));
        when(assignmentHistoryRepository.findActiveAssignmentsByAssetId(assetId2))
            .thenReturn(Collections.emptyList());
        
        // Asset 3: Success
        when(assetRepository.findById(assetId3)).thenReturn(Optional.of(asset3));
        when(assignmentHistoryRepository.findActiveAssignmentsByAssetId(assetId3))
            .thenReturn(Collections.singletonList(assignment3));
        when(assignmentHistoryRepository.findByAssetIdOrderByAssignedAtDesc(eq(assetId3), any(Pageable.class)))
            .thenReturn(new PageImpl<>(Collections.singletonList(assignment3)));
        
        // Asset 4: Failure (not found)
        when(assetRepository.findById(assetId4)).thenReturn(Optional.empty());
        
        // When
        BulkDeallocationResult result = allocationService.bulkDeallocate(
            userId.toString(), 
            Arrays.asList(assetId1, assetId2, assetId3, assetId4)
        );
        
        // Then
        assertThat(result.getTotalRequested()).isEqualTo(4);
        assertThat(result.getSuccessCount()).isEqualTo(2);
        assertThat(result.getFailureCount()).isEqualTo(2);
        assertThat(result.getSuccessfulDeallocations()).hasSize(2);
        assertThat(result.getFailedDeallocations()).hasSize(2);
    }
    
    @Test
    @DisplayName("Should include assignment DTO in successful bulk deallocation results")
    void shouldIncludeAssignmentDTOInSuccessfulBulkDeallocationResults() {
        // Given
        UUID assetId1 = UUID.randomUUID();
        Asset asset1 = createTestAsset(assetId1);
        AssignmentHistory assignment1 = createTestAssignment(assetId1);
        
        when(authorizationService.hasPermission(userId.toString(), Action.DEALLOCATE_ASSET))
            .thenReturn(true);
        when(assetRepository.findById(assetId1)).thenReturn(Optional.of(asset1));
        when(assignmentHistoryRepository.findActiveAssignmentsByAssetId(assetId1))
            .thenReturn(Collections.singletonList(assignment1));
        when(assignmentHistoryRepository.findByAssetIdOrderByAssignedAtDesc(eq(assetId1), any(Pageable.class)))
            .thenReturn(new PageImpl<>(Collections.singletonList(assignment1)));
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        
        // When
        BulkDeallocationResult result = allocationService.bulkDeallocate(
            userId.toString(), 
            Collections.singletonList(assetId1)
        );
        
        // Then
        assertThat(result.getSuccessfulDeallocations()).hasSize(1);
        BulkDeallocationResult.DeallocationSuccess success = result.getSuccessfulDeallocations().get(0);
        assertThat(success.getAssetId()).isEqualTo(assetId1);
        assertThat(success.getClosedAssignment()).isNotNull();
        assertThat(success.getClosedAssignment().getAssetId()).isEqualTo(assetId1);
    }
    
    @Test
    @DisplayName("Should include error message in failed bulk deallocation results")
    void shouldIncludeErrorMessageInFailedBulkDeallocationResults() {
        // Given
        UUID assetId1 = UUID.randomUUID();
        
        when(authorizationService.hasPermission(userId.toString(), Action.DEALLOCATE_ASSET))
            .thenReturn(true);
        when(assetRepository.findById(assetId1)).thenReturn(Optional.empty());
        
        // When
        BulkDeallocationResult result = allocationService.bulkDeallocate(
            userId.toString(), 
            Collections.singletonList(assetId1)
        );
        
        // Then
        assertThat(result.getFailedDeallocations()).hasSize(1);
        BulkDeallocationResult.DeallocationFailure failure = result.getFailedDeallocations().get(0);
        assertThat(failure.getAssetId()).isEqualTo(assetId1);
        assertThat(failure.getErrorType()).isEqualTo("ASSET_NOT_FOUND");
        assertThat(failure.getErrorMessage()).isNotNull();
        assertThat(failure.getErrorMessage()).contains(assetId1.toString());
    }
    
    // Task 3.4: Reassignment Operations Tests
    
    @Test
    @DisplayName("Should reassign asset from user to user when authorized and valid")
    void shouldReassignAssetFromUserToUserWhenAuthorizedAndValid() {
        // Given
        AssignmentHistory oldAssignment = new AssignmentHistory();
        oldAssignment.setId(UUID.randomUUID());
        oldAssignment.setAssetId(assetId);
        oldAssignment.setAssignmentType(AssignmentType.USER);
        oldAssignment.setAssignedTo("Old User");
        oldAssignment.setAssignedBy(userId);
        oldAssignment.setAssignedAt(LocalDateTime.now().minusDays(1));
        
        testAsset.setAssignedUser("Old User");
        testAsset.setAssignedUserEmail("old.user@example.com");
        testAsset.setAssignmentDate(LocalDateTime.now().minusDays(1));
        
        when(authorizationService.hasPermission(userId.toString(), Action.ALLOCATE_ASSET))
            .thenReturn(true);
        when(assetRepository.findById(assetId)).thenReturn(Optional.of(testAsset));
        when(assignmentHistoryRepository.findActiveAssignmentsByAssetId(assetId))
            .thenReturn(Collections.singletonList(oldAssignment));
        when(assignmentHistoryRepository.save(any(AssignmentHistory.class)))
            .thenAnswer(invocation -> {
                AssignmentHistory assignment = invocation.getArgument(0);
                if (assignment.getId() == null) {
                    assignment.setId(UUID.randomUUID());
                }
                return assignment;
            });
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        
        // When
        AssignmentDTO result = allocationService.reassign(userId.toString(), assetId, userAssignmentRequest);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.getAssignedTo()).isEqualTo("John Doe");
        assertThat(result.getAssignmentType()).isEqualTo(AssignmentType.USER);
        assertThat(result.isActive()).isTrue();
        
        // Verify old assignment was closed
        ArgumentCaptor<AssignmentHistory> assignmentCaptor = ArgumentCaptor.forClass(AssignmentHistory.class);
        verify(assignmentHistoryRepository, times(2)).save(assignmentCaptor.capture());
        
        List<AssignmentHistory> savedAssignments = assignmentCaptor.getAllValues();
        AssignmentHistory closedAssignment = savedAssignments.get(0);
        assertThat(closedAssignment.getUnassignedAt()).isNotNull();
        
        // Verify new assignment was created
        AssignmentHistory newAssignment = savedAssignments.get(1);
        assertThat(newAssignment.getAssetId()).isEqualTo(assetId);
        assertThat(newAssignment.getAssignmentType()).isEqualTo(AssignmentType.USER);
        assertThat(newAssignment.getAssignedTo()).isEqualTo("John Doe");
        assertThat(newAssignment.getAssignedBy()).isEqualTo(userId);
        assertThat(newAssignment.getUnassignedAt()).isNull();
        
        // Verify asset fields were updated
        ArgumentCaptor<Asset> assetCaptor = ArgumentCaptor.forClass(Asset.class);
        verify(assetRepository).save(assetCaptor.capture());
        Asset savedAsset = assetCaptor.getValue();
        assertThat(savedAsset.getAssignedUser()).isEqualTo("John Doe");
        assertThat(savedAsset.getAssignedUserEmail()).isEqualTo("john.doe@example.com");
        assertThat(savedAsset.getAssignmentDate()).isNotNull();
        assertThat(savedAsset.getLocation()).isNull();
        assertThat(savedAsset.getLocationUpdateDate()).isNull();
        
        // Verify both operations were logged
        verify(auditService, times(2)).logEvent(any(AuditEventDTO.class));
    }
    
    @Test
    @DisplayName("Should reassign asset from user to location")
    void shouldReassignAssetFromUserToLocation() {
        // Given
        AssignmentHistory oldAssignment = new AssignmentHistory();
        oldAssignment.setId(UUID.randomUUID());
        oldAssignment.setAssetId(assetId);
        oldAssignment.setAssignmentType(AssignmentType.USER);
        oldAssignment.setAssignedTo("Old User");
        oldAssignment.setAssignedBy(userId);
        oldAssignment.setAssignedAt(LocalDateTime.now().minusDays(1));
        
        testAsset.setAssignedUser("Old User");
        testAsset.setAssignedUserEmail("old.user@example.com");
        testAsset.setAssignmentDate(LocalDateTime.now().minusDays(1));
        
        when(authorizationService.hasPermission(userId.toString(), Action.ALLOCATE_ASSET))
            .thenReturn(true);
        when(assetRepository.findById(assetId)).thenReturn(Optional.of(testAsset));
        when(assignmentHistoryRepository.findActiveAssignmentsByAssetId(assetId))
            .thenReturn(Collections.singletonList(oldAssignment));
        when(assignmentHistoryRepository.save(any(AssignmentHistory.class)))
            .thenAnswer(invocation -> {
                AssignmentHistory assignment = invocation.getArgument(0);
                if (assignment.getId() == null) {
                    assignment.setId(UUID.randomUUID());
                }
                return assignment;
            });
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        
        // When
        AssignmentDTO result = allocationService.reassign(userId.toString(), assetId, locationAssignmentRequest);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.getAssignedTo()).isEqualTo("Data Center A");
        assertThat(result.getAssignmentType()).isEqualTo(AssignmentType.LOCATION);
        
        // Verify asset fields were updated correctly
        ArgumentCaptor<Asset> assetCaptor = ArgumentCaptor.forClass(Asset.class);
        verify(assetRepository).save(assetCaptor.capture());
        Asset savedAsset = assetCaptor.getValue();
        assertThat(savedAsset.getLocation()).isEqualTo("Data Center A");
        assertThat(savedAsset.getLocationUpdateDate()).isNotNull();
        assertThat(savedAsset.getAssignedUser()).isNull();
        assertThat(savedAsset.getAssignedUserEmail()).isNull();
        assertThat(savedAsset.getAssignmentDate()).isNull();
    }
    
    @Test
    @DisplayName("Should reassign asset from location to user")
    void shouldReassignAssetFromLocationToUser() {
        // Given
        AssignmentHistory oldAssignment = new AssignmentHistory();
        oldAssignment.setId(UUID.randomUUID());
        oldAssignment.setAssetId(assetId);
        oldAssignment.setAssignmentType(AssignmentType.LOCATION);
        oldAssignment.setAssignedTo("Old Location");
        oldAssignment.setAssignedBy(userId);
        oldAssignment.setAssignedAt(LocalDateTime.now().minusDays(1));
        
        testAsset.setLocation("Old Location");
        testAsset.setLocationUpdateDate(LocalDateTime.now().minusDays(1));
        
        when(authorizationService.hasPermission(userId.toString(), Action.ALLOCATE_ASSET))
            .thenReturn(true);
        when(assetRepository.findById(assetId)).thenReturn(Optional.of(testAsset));
        when(assignmentHistoryRepository.findActiveAssignmentsByAssetId(assetId))
            .thenReturn(Collections.singletonList(oldAssignment));
        when(assignmentHistoryRepository.save(any(AssignmentHistory.class)))
            .thenAnswer(invocation -> {
                AssignmentHistory assignment = invocation.getArgument(0);
                if (assignment.getId() == null) {
                    assignment.setId(UUID.randomUUID());
                }
                return assignment;
            });
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        
        // When
        AssignmentDTO result = allocationService.reassign(userId.toString(), assetId, userAssignmentRequest);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.getAssignedTo()).isEqualTo("John Doe");
        assertThat(result.getAssignmentType()).isEqualTo(AssignmentType.USER);
        
        // Verify asset fields were updated correctly
        ArgumentCaptor<Asset> assetCaptor = ArgumentCaptor.forClass(Asset.class);
        verify(assetRepository).save(assetCaptor.capture());
        Asset savedAsset = assetCaptor.getValue();
        assertThat(savedAsset.getAssignedUser()).isEqualTo("John Doe");
        assertThat(savedAsset.getAssignedUserEmail()).isEqualTo("john.doe@example.com");
        assertThat(savedAsset.getAssignmentDate()).isNotNull();
        assertThat(savedAsset.getLocation()).isNull();
        assertThat(savedAsset.getLocationUpdateDate()).isNull();
    }
    
    @Test
    @DisplayName("Should throw InsufficientPermissionsException when user lacks permission for reassignment")
    void shouldThrowExceptionWhenUserLacksPermissionForReassignment() {
        // Given
        when(authorizationService.hasPermission(userId.toString(), Action.ALLOCATE_ASSET))
            .thenReturn(false);
        
        // When/Then
        assertThatThrownBy(() -> 
            allocationService.reassign(userId.toString(), assetId, userAssignmentRequest))
            .isInstanceOf(InsufficientPermissionsException.class);
        
        verify(assetRepository, never()).findById(any());
        verify(assignmentHistoryRepository, never()).save(any());
        verify(auditService, never()).logEvent(any());
    }
    
    @Test
    @DisplayName("Should throw ResourceNotFoundException when asset does not exist for reassignment")
    void shouldThrowExceptionWhenAssetNotFoundForReassignment() {
        // Given
        when(authorizationService.hasPermission(userId.toString(), Action.ALLOCATE_ASSET))
            .thenReturn(true);
        when(assetRepository.findById(assetId)).thenReturn(Optional.empty());
        
        // When/Then
        assertThatThrownBy(() -> 
            allocationService.reassign(userId.toString(), assetId, userAssignmentRequest))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining(assetId.toString());
        
        verify(assignmentHistoryRepository, never()).save(any());
        verify(auditService, never()).logEvent(any());
    }
    
    @Test
    @DisplayName("Should throw AssetNotAssignedException when asset is not currently assigned")
    void shouldThrowExceptionWhenAssetNotCurrentlyAssigned() {
        // Given
        when(authorizationService.hasPermission(userId.toString(), Action.ALLOCATE_ASSET))
            .thenReturn(true);
        when(assetRepository.findById(assetId)).thenReturn(Optional.of(testAsset));
        when(assignmentHistoryRepository.findActiveAssignmentsByAssetId(assetId))
            .thenReturn(Collections.emptyList());
        
        // When/Then
        assertThatThrownBy(() -> 
            allocationService.reassign(userId.toString(), assetId, userAssignmentRequest))
            .isInstanceOf(AssetNotAssignedException.class)
            .hasMessageContaining(assetId.toString());
        
        verify(assignmentHistoryRepository, never()).save(any());
        verify(auditService, never()).logEvent(any());
    }
    
    @Test
    @DisplayName("Should throw AssetNotAssignableException when asset status is not assignable for reassignment")
    void shouldThrowExceptionWhenAssetNotAssignableForReassignment() {
        // Given
        testAsset.setStatus(LifecycleStatus.MAINTENANCE);
        
        when(authorizationService.hasPermission(userId.toString(), Action.ALLOCATE_ASSET))
            .thenReturn(true);
        when(assetRepository.findById(assetId)).thenReturn(Optional.of(testAsset));
        
        // When/Then
        assertThatThrownBy(() -> 
            allocationService.reassign(userId.toString(), assetId, userAssignmentRequest))
            .isInstanceOf(AssetNotAssignableException.class)
            .hasMessageContaining("MAINTENANCE");
        
        verify(assignmentHistoryRepository, never()).save(any());
        verify(auditService, never()).logEvent(any());
    }
    
    @Test
    @DisplayName("Should throw ValidationException when reassignment request is invalid")
    void shouldThrowValidationExceptionWhenReassignmentRequestInvalid() {
        // Given
        userAssignmentRequest.setAssignedTo("");
        
        when(authorizationService.hasPermission(userId.toString(), Action.ALLOCATE_ASSET))
            .thenReturn(true);
        
        // When/Then
        assertThatThrownBy(() -> 
            allocationService.reassign(userId.toString(), assetId, userAssignmentRequest))
            .isInstanceOf(ValidationException.class);
        
        verify(assetRepository, never()).findById(any());
        verify(assignmentHistoryRepository, never()).save(any());
        verify(auditService, never()).logEvent(any());
    }
    
    @Test
    @DisplayName("Should verify old assignment is closed with UnassignedAt timestamp")
    void shouldVerifyOldAssignmentIsClosedWithTimestamp() {
        // Given
        AssignmentHistory oldAssignment = new AssignmentHistory();
        oldAssignment.setId(UUID.randomUUID());
        oldAssignment.setAssetId(assetId);
        oldAssignment.setAssignmentType(AssignmentType.USER);
        oldAssignment.setAssignedTo("Old User");
        oldAssignment.setAssignedBy(userId);
        oldAssignment.setAssignedAt(LocalDateTime.now().minusDays(1));
        
        testAsset.setAssignedUser("Old User");
        
        when(authorizationService.hasPermission(userId.toString(), Action.ALLOCATE_ASSET))
            .thenReturn(true);
        when(assetRepository.findById(assetId)).thenReturn(Optional.of(testAsset));
        when(assignmentHistoryRepository.findActiveAssignmentsByAssetId(assetId))
            .thenReturn(Collections.singletonList(oldAssignment));
        when(assignmentHistoryRepository.save(any(AssignmentHistory.class)))
            .thenAnswer(invocation -> {
                AssignmentHistory assignment = invocation.getArgument(0);
                if (assignment.getId() == null) {
                    assignment.setId(UUID.randomUUID());
                }
                return assignment;
            });
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        
        LocalDateTime beforeReassignment = LocalDateTime.now();
        
        // When
        allocationService.reassign(userId.toString(), assetId, userAssignmentRequest);
        
        LocalDateTime afterReassignment = LocalDateTime.now();
        
        // Then
        ArgumentCaptor<AssignmentHistory> assignmentCaptor = ArgumentCaptor.forClass(AssignmentHistory.class);
        verify(assignmentHistoryRepository, times(2)).save(assignmentCaptor.capture());
        
        AssignmentHistory closedAssignment = assignmentCaptor.getAllValues().get(0);
        assertThat(closedAssignment.getUnassignedAt()).isNotNull();
        assertThat(closedAssignment.getUnassignedAt()).isBetween(beforeReassignment, afterReassignment);
    }
    
    @Test
    @DisplayName("Should verify both deallocation and allocation are logged to audit service")
    void shouldVerifyBothOperationsLoggedToAuditService() {
        // Given
        AssignmentHistory oldAssignment = new AssignmentHistory();
        oldAssignment.setId(UUID.randomUUID());
        oldAssignment.setAssetId(assetId);
        oldAssignment.setAssignmentType(AssignmentType.USER);
        oldAssignment.setAssignedTo("Old User");
        oldAssignment.setAssignedBy(userId);
        oldAssignment.setAssignedAt(LocalDateTime.now().minusDays(1));
        
        testAsset.setAssignedUser("Old User");
        
        when(authorizationService.hasPermission(userId.toString(), Action.ALLOCATE_ASSET))
            .thenReturn(true);
        when(assetRepository.findById(assetId)).thenReturn(Optional.of(testAsset));
        when(assignmentHistoryRepository.findActiveAssignmentsByAssetId(assetId))
            .thenReturn(Collections.singletonList(oldAssignment));
        when(assignmentHistoryRepository.save(any(AssignmentHistory.class)))
            .thenAnswer(invocation -> {
                AssignmentHistory assignment = invocation.getArgument(0);
                if (assignment.getId() == null) {
                    assignment.setId(UUID.randomUUID());
                }
                return assignment;
            });
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        
        // When
        allocationService.reassign(userId.toString(), assetId, userAssignmentRequest);
        
        // Then
        ArgumentCaptor<AuditEventDTO> auditCaptor = ArgumentCaptor.forClass(AuditEventDTO.class);
        verify(auditService, times(2)).logEvent(auditCaptor.capture());
        
        List<AuditEventDTO> auditEvents = auditCaptor.getAllValues();
        
        // Verify first audit log is for closing old assignment (DELETE)
        AuditEventDTO closeEvent = auditEvents.get(0);
        assertThat(closeEvent.getUserId()).isEqualTo(userId);
        assertThat(closeEvent.getActionType()).isEqualTo(Action.DELETE_ASSET);
        assertThat(closeEvent.getResourceType()).isEqualTo("ASSIGNMENT");
        assertThat(closeEvent.getMetadata()).containsEntry("operation", "reassignment_close");
        assertThat(closeEvent.getMetadata()).containsEntry("oldAssignedTo", "Old User");
        
        // Verify second audit log is for creating new assignment (CREATE)
        AuditEventDTO createEvent = auditEvents.get(1);
        assertThat(createEvent.getUserId()).isEqualTo(userId);
        assertThat(createEvent.getActionType()).isEqualTo(Action.CREATE_ASSET);
        assertThat(createEvent.getResourceType()).isEqualTo("ASSIGNMENT");
        assertThat(createEvent.getMetadata()).containsEntry("operation", "reassignment_create");
        assertThat(createEvent.getMetadata()).containsEntry("assignedTo", "John Doe");
    }
    
    @Test
    @DisplayName("Should verify reassignment is atomic - both operations succeed or both fail")
    void shouldVerifyReassignmentIsAtomic() {
        // Given
        AssignmentHistory oldAssignment = new AssignmentHistory();
        oldAssignment.setId(UUID.randomUUID());
        oldAssignment.setAssetId(assetId);
        oldAssignment.setAssignmentType(AssignmentType.USER);
        oldAssignment.setAssignedTo("Old User");
        oldAssignment.setAssignedBy(userId);
        oldAssignment.setAssignedAt(LocalDateTime.now().minusDays(1));
        
        testAsset.setAssignedUser("Old User");
        
        when(authorizationService.hasPermission(userId.toString(), Action.ALLOCATE_ASSET))
            .thenReturn(true);
        when(assetRepository.findById(assetId)).thenReturn(Optional.of(testAsset));
        when(assignmentHistoryRepository.findActiveAssignmentsByAssetId(assetId))
            .thenReturn(Collections.singletonList(oldAssignment));
        when(assignmentHistoryRepository.save(any(AssignmentHistory.class)))
            .thenAnswer(invocation -> {
                AssignmentHistory assignment = invocation.getArgument(0);
                if (assignment.getId() == null) {
                    assignment.setId(UUID.randomUUID());
                }
                return assignment;
            });
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        
        // When
        allocationService.reassign(userId.toString(), assetId, userAssignmentRequest);
        
        // Then - Verify both assignment history saves occurred
        verify(assignmentHistoryRepository, times(2)).save(any(AssignmentHistory.class));
        
        // Verify asset was saved once with updated fields
        verify(assetRepository, times(1)).save(any(Asset.class));
        
        // Verify both audit logs were created
        verify(auditService, times(2)).logEvent(any(AuditEventDTO.class));
    }
    
    @Test
    @DisplayName("Should update asset updatedAt timestamp during reassignment")
    void shouldUpdateAssetUpdatedAtTimestampDuringReassignment() {
        // Given
        AssignmentHistory oldAssignment = new AssignmentHistory();
        oldAssignment.setId(UUID.randomUUID());
        oldAssignment.setAssetId(assetId);
        oldAssignment.setAssignmentType(AssignmentType.USER);
        oldAssignment.setAssignedTo("Old User");
        oldAssignment.setAssignedBy(userId);
        oldAssignment.setAssignedAt(LocalDateTime.now().minusDays(1));
        
        testAsset.setAssignedUser("Old User");
        
        when(authorizationService.hasPermission(userId.toString(), Action.ALLOCATE_ASSET))
            .thenReturn(true);
        when(assetRepository.findById(assetId)).thenReturn(Optional.of(testAsset));
        when(assignmentHistoryRepository.findActiveAssignmentsByAssetId(assetId))
            .thenReturn(Collections.singletonList(oldAssignment));
        when(assignmentHistoryRepository.save(any(AssignmentHistory.class)))
            .thenAnswer(invocation -> {
                AssignmentHistory assignment = invocation.getArgument(0);
                if (assignment.getId() == null) {
                    assignment.setId(UUID.randomUUID());
                }
                return assignment;
            });
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        
        LocalDateTime beforeReassignment = LocalDateTime.now();
        
        // When
        allocationService.reassign(userId.toString(), assetId, userAssignmentRequest);
        
        LocalDateTime afterReassignment = LocalDateTime.now();
        
        // Then
        ArgumentCaptor<Asset> assetCaptor = ArgumentCaptor.forClass(Asset.class);
        verify(assetRepository).save(assetCaptor.capture());
        Asset savedAsset = assetCaptor.getValue();
        
        assertThat(savedAsset.getUpdatedAt()).isNotNull();
        assertThat(savedAsset.getUpdatedAt()).isBetween(beforeReassignment, afterReassignment);
    }
    
    @Test
    @DisplayName("Should handle reassignment with validation error for new assignment")
    void shouldHandleReassignmentWithValidationErrorForNewAssignment() {
        // Given
        userAssignmentRequest.setAssignedUserEmail("invalid-email");
        
        when(authorizationService.hasPermission(userId.toString(), Action.ALLOCATE_ASSET))
            .thenReturn(true);
        
        // When/Then
        assertThatThrownBy(() -> 
            allocationService.reassign(userId.toString(), assetId, userAssignmentRequest))
            .isInstanceOf(ValidationException.class)
            .hasMessageContaining("Invalid email format");
        
        // Verify no database operations occurred
        verify(assetRepository, never()).findById(any());
        verify(assignmentHistoryRepository, never()).save(any());
        verify(auditService, never()).logEvent(any());
    }
    
    @Test
    @DisplayName("Should include new assignment ID in result DTO")
    void shouldIncludeNewAssignmentIdInResultDTO() {
        // Given
        AssignmentHistory oldAssignment = new AssignmentHistory();
        oldAssignment.setId(UUID.randomUUID());
        oldAssignment.setAssetId(assetId);
        oldAssignment.setAssignmentType(AssignmentType.USER);
        oldAssignment.setAssignedTo("Old User");
        oldAssignment.setAssignedBy(userId);
        oldAssignment.setAssignedAt(LocalDateTime.now().minusDays(1));
        
        testAsset.setAssignedUser("Old User");
        
        UUID newAssignmentId = UUID.randomUUID();
        
        when(authorizationService.hasPermission(userId.toString(), Action.ALLOCATE_ASSET))
            .thenReturn(true);
        when(assetRepository.findById(assetId)).thenReturn(Optional.of(testAsset));
        when(assignmentHistoryRepository.findActiveAssignmentsByAssetId(assetId))
            .thenReturn(Collections.singletonList(oldAssignment));
        when(assignmentHistoryRepository.save(any(AssignmentHistory.class)))
            .thenAnswer(invocation -> {
                AssignmentHistory assignment = invocation.getArgument(0);
                if (assignment.getId() == null) {
                    assignment.setId(newAssignmentId);
                }
                return assignment;
            });
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        
        // When
        AssignmentDTO result = allocationService.reassign(userId.toString(), assetId, userAssignmentRequest);
        
        // Then
        assertThat(result.getId()).isEqualTo(newAssignmentId);
    }
    
    // Task 3.5: Query Operations Tests
    
    @Test
    @DisplayName("Should retrieve assignment history with pagination")
    void shouldRetrieveAssignmentHistoryWithPagination() {
        // Given
        AssignmentHistory assignment1 = createTestAssignment(assetId);
        assignment1.setAssignedAt(LocalDateTime.now());
        
        AssignmentHistory assignment2 = createTestAssignment(assetId);
        assignment2.setAssignedAt(LocalDateTime.now().minusDays(1));
        assignment2.setUnassignedAt(LocalDateTime.now().minusHours(1));
        
        Page<AssignmentHistory> page = new PageImpl<>(
            Arrays.asList(assignment1, assignment2),
            PageRequest.of(0, 20),
            2
        );
        
        when(authorizationService.hasPermission(TEST_USER_ID, Action.VIEW_ASSET)).thenReturn(true);
        when(assetRepository.existsById(assetId)).thenReturn(true);
        when(assignmentHistoryRepository.findByAssetIdOrderByAssignedAtDesc(eq(assetId), any(Pageable.class)))
            .thenReturn(page);
        when(userRepository.findById(any(UUID.class))).thenReturn(Optional.of(testUser));
        
        // When
        Page<AssignmentHistoryDTO> result = allocationService.getAssignmentHistory(
            TEST_USER_ID,
            assetId, 
            PageRequest.of(0, 20)
        );
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent().get(0).isActive()).isTrue();
        assertThat(result.getContent().get(1).isActive()).isFalse();
    }
    
    @Test
    @DisplayName("Should query assets by user with case-insensitive matching")
    void shouldQueryAssetsByUserCaseInsensitive() {
        // Given
        Asset asset1 = createTestAsset(UUID.randomUUID());
        asset1.setAssignedUser("John Doe");
        
        Asset asset2 = createTestAsset(UUID.randomUUID());
        asset2.setAssignedUser("john doe");
        
        Page<Asset> page = new PageImpl<>(
            Arrays.asList(asset1, asset2),
            PageRequest.of(0, 20),
            2
        );
        
        when(assetRepository.findByAssignedUserContainingIgnoreCase(eq("john"), any(Pageable.class)))
            .thenReturn(page);
        
        // When
        Page<AssetDTO> result = allocationService.getAssetsByUser("john", PageRequest.of(0, 20));
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(2);
    }
    
    @Test
    @DisplayName("Should query assets by location with case-insensitive matching")
    void shouldQueryAssetsByLocationCaseInsensitive() {
        // Given
        Asset asset1 = createTestAsset(UUID.randomUUID());
        asset1.setLocation("Data Center A");
        
        Page<Asset> page = new PageImpl<>(
            Collections.singletonList(asset1),
            PageRequest.of(0, 20),
            1
        );
        
        when(assetRepository.findByLocationContainingIgnoreCase(eq("data center"), any(Pageable.class)))
            .thenReturn(page);
        
        // When
        Page<AssetDTO> result = allocationService.getAssetsByLocation("data center", PageRequest.of(0, 20));
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getLocation()).isEqualTo("Data Center A");
    }
    
    // Task 3.6: Statistics and Export Tests
    
    @Test
    @DisplayName("Should generate assignment statistics")
    void shouldGenerateAssignmentStatistics() {
        // Given
        when(authorizationService.hasPermission(userId.toString(), Action.VIEW_ASSET)).thenReturn(true);
        when(assetRepository.countAssignedAssets()).thenReturn(100L);
        when(assignmentHistoryRepository.getAssignmentStatistics())
            .thenReturn(Arrays.asList(
                new Object[]{AssignmentType.USER, 60L},
                new Object[]{AssignmentType.LOCATION, 40L}
            ));
        when(assetRepository.countByStatusAndUnassigned(LifecycleStatus.IN_USE)).thenReturn(20L);
        when(assetRepository.countByStatusAndUnassigned(LifecycleStatus.DEPLOYED)).thenReturn(15L);
        when(assetRepository.countByStatusAndUnassigned(LifecycleStatus.STORAGE)).thenReturn(10L);
        when(assignmentHistoryRepository.getTopAssignmentsByType(eq(AssignmentType.USER), any(Pageable.class)))
            .thenReturn(Arrays.asList(
                new Object[]{"John Doe", 10L},
                new Object[]{"Jane Smith", 8L}
            ));
        when(assignmentHistoryRepository.getTopAssignmentsByType(eq(AssignmentType.LOCATION), any(Pageable.class)))
            .thenReturn(Arrays.asList(
                new Object[]{"Data Center A", 25L},
                new Object[]{"Data Center B", 15L}
            ));
        
        // When
        AssignmentStatisticsDTO stats = allocationService.getStatistics(userId.toString());
        
        // Then
        assertThat(stats).isNotNull();
        assertThat(stats.getTotalAssignedAssets()).isEqualTo(100L);
        assertThat(stats.getUserAssignments()).isEqualTo(60L);
        assertThat(stats.getLocationAssignments()).isEqualTo(40L);
        assertThat(stats.getAvailableAssetsByStatus()).containsEntry("IN_USE", 20L);
        assertThat(stats.getTopUsersByAssignments()).hasSize(2);
        assertThat(stats.getTopLocationsByAssignments()).hasSize(2);
        
        verify(authorizationService).hasPermission(userId.toString(), Action.VIEW_ASSET);
    }
    
    @Test
    @DisplayName("Should throw InsufficientPermissionsException when user lacks permission to view statistics")
    void shouldThrowExceptionWhenUserLacksPermissionForStatistics() {
        // Given
        when(authorizationService.hasPermission(userId.toString(), Action.VIEW_ASSET)).thenReturn(false);
        
        // When/Then
        assertThatThrownBy(() -> allocationService.getStatistics(userId.toString()))
            .isInstanceOf(InsufficientPermissionsException.class)
            .hasMessageContaining("User does not have permission to view assignment statistics");
        
        verify(authorizationService).hasPermission(userId.toString(), Action.VIEW_ASSET);
        verify(assetRepository, never()).countAssignedAssets();
    }
    
    @Test
    @DisplayName("Should export assignments to CSV")
    void shouldExportAssignmentsToCsv() {
        // Given
        AssignmentHistory assignment = createTestAssignment(assetId);
        Asset asset = createTestAsset(assetId);
        
        when(assignmentHistoryRepository.findActiveAssignmentsWithFilters(null, null, null, null))
            .thenReturn(Collections.singletonList(assignment));
        when(assetRepository.findById(assetId)).thenReturn(Optional.of(asset));
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        
        // When
        byte[] csv = allocationService.exportAssignments(userId.toString(), Collections.emptyMap());
        
        // Then
        assertThat(csv).isNotNull();
        assertThat(csv.length).isGreaterThan(0);
        
        String csvContent = new String(csv);
        assertThat(csvContent).contains("Asset ID");
        assertThat(csvContent).contains("Asset Name");
        assertThat(csvContent).contains(asset.getName());
        
        verify(auditService).logEvent(any(AuditEventDTO.class));
    }
    
    @Test
    @DisplayName("Should export CSV with all required columns")
    void shouldExportCsvWithAllRequiredColumns() {
        // Given
        AssignmentHistory assignment = createTestAssignment(assetId);
        Asset asset = createTestAsset(assetId);
        
        when(assignmentHistoryRepository.findActiveAssignmentsByType(null))
            .thenReturn(Collections.singletonList(assignment));
        when(assetRepository.findById(assetId)).thenReturn(Optional.of(asset));
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        
        // When
        byte[] csv = allocationService.exportAssignments(Collections.emptyMap());
        
        // Then
        String csvContent = new String(csv);
        String[] lines = csvContent.split("\n");
        
        // Verify header contains all required columns
        String header = lines[0];
        assertThat(header).contains("Asset ID");
        assertThat(header).contains("Asset Name");
        assertThat(header).contains("Serial Number");
        assertThat(header).contains("Asset Type");
        assertThat(header).contains("Assignment Type");
        assertThat(header).contains("Assigned To");
        assertThat(header).contains("Assigned By");
        assertThat(header).contains("Assigned At");
        
        // Verify data row contains expected values
        assertThat(lines.length).isGreaterThan(1);
        String dataRow = lines[1];
        assertThat(dataRow).contains(asset.getId().toString());
        assertThat(dataRow).contains(asset.getName());
        assertThat(dataRow).contains(asset.getSerialNumber());
        assertThat(dataRow).contains(asset.getAssetType().toString());
        assertThat(dataRow).contains(assignment.getAssignmentType().toString());
        assertThat(dataRow).contains(assignment.getAssignedTo());
        assertThat(dataRow).contains(testUser.getUsername());
    }
    
    @Test
    @DisplayName("Should properly escape CSV special characters")
    void shouldProperlyEscapeCsvSpecialCharacters() {
        // Given
        AssignmentHistory assignment = createTestAssignment(assetId);
        Asset asset = createTestAsset(assetId);
        asset.setName("Server, \"Production\"");  // Contains comma and quotes
        asset.setSerialNumber("SRV-001\nBackup"); // Contains newline
        assignment.setAssignedTo("John \"Johnny\" Doe"); // Contains quotes
        
        when(assignmentHistoryRepository.findActiveAssignmentsByType(null))
            .thenReturn(Collections.singletonList(assignment));
        when(assetRepository.findById(assetId)).thenReturn(Optional.of(asset));
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        
        // When
        byte[] csv = allocationService.exportAssignments(Collections.emptyMap());
        
        // Then
        String csvContent = new String(csv);
        
        // Verify special characters are properly escaped
        assertThat(csvContent).contains("\"Server, \"\"Production\"\"\""); // Comma and quotes escaped
        assertThat(csvContent).contains("\"SRV-001\nBackup\""); // Newline escaped
        assertThat(csvContent).contains("\"John \"\"Johnny\"\" Doe\""); // Quotes escaped
    }
    
    @Test
    @DisplayName("Should throw ValidationException when export exceeds size limit")
    void shouldThrowValidationExceptionWhenExportExceedsSizeLimit() {
        // Given
        List<AssignmentHistory> largeList = new ArrayList<>();
        for (int i = 0; i < 10001; i++) {
            largeList.add(createTestAssignment(UUID.randomUUID()));
        }
        
        when(assignmentHistoryRepository.findActiveAssignmentsByType(null))
            .thenReturn(largeList);
        
        // When/Then
        assertThatThrownBy(() -> allocationService.exportAssignments(Collections.emptyMap()))
            .isInstanceOf(ValidationException.class)
            .hasMessageContaining("Export limited to 10000 records");
    }
    
    // Helper methods
    
    private Asset createTestAsset(UUID id) {
        Asset asset = new Asset();
        asset.setId(id);
        asset.setAssetType(AssetType.SERVER);
        asset.setName("Test Asset " + id);
        asset.setSerialNumber("SRV-" + id.toString().substring(0, 8));
        asset.setAcquisitionDate(LocalDate.now());
        asset.setStatus(LifecycleStatus.IN_USE);
        return asset;
    }
    
    private AssignmentHistory createTestAssignment(UUID assetId) {
        AssignmentHistory assignment = new AssignmentHistory();
        assignment.setId(UUID.randomUUID());
        assignment.setAssetId(assetId);
        assignment.setAssignmentType(AssignmentType.USER);
        assignment.setAssignedTo("Test User");
        assignment.setAssignedBy(userId);
        assignment.setAssignedAt(LocalDateTime.now());
        return assignment;
    }
}

    // Task 3.4: Query Operations Tests
    
    // Tests for getAssignmentHistory
    
    @Test
    @DisplayName("Should retrieve assignment history with pagination when authorized")
    void shouldRetrieveAssignmentHistoryWithPaginationWhenAuthorized() {
        // Given
        AssignmentHistory history1 = new AssignmentHistory();
        history1.setId(UUID.randomUUID());
        history1.setAssetId(assetId);
        history1.setAssignmentType(AssignmentType.USER);
        history1.setAssignedTo("John Doe");
        history1.setAssignedBy(userId);
        history1.setAssignedAt(LocalDateTime.now().minusDays(2));
        history1.setUnassignedAt(LocalDateTime.now().minusDays(1));
        
        AssignmentHistory history2 = new AssignmentHistory();
        history2.setId(UUID.randomUUID());
        history2.setAssetId(assetId);
        history2.setAssignmentType(AssignmentType.LOCATION);
        history2.setAssignedTo("Data Center A");
        history2.setAssignedBy(userId);
        history2.setAssignedAt(LocalDateTime.now());
        
        Pageable pageable = PageRequest.of(0, 20);
        Page<AssignmentHistory> historyPage = new PageImpl<>(
            Arrays.asList(history2, history1), 
            pageable, 
            2
        );
        
        when(authorizationService.hasPermission(userId.toString(), Action.VIEW_ASSET))
            .thenReturn(true);
        when(assetRepository.existsById(assetId)).thenReturn(true);
        when(assignmentHistoryRepository.findByAssetIdOrderByAssignedAtDesc(assetId, pageable))
            .thenReturn(historyPage);
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        
        // When
        Page<AssignmentHistoryDTO> result = allocationService.getAssignmentHistory(
            userId.toString(), assetId, pageable
        );
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getTotalElements()).isEqualTo(2);
        
        // Verify ordering (most recent first)
        assertThat(result.getContent().get(0).getAssignedTo()).isEqualTo("Data Center A");
        assertThat(result.getContent().get(1).getAssignedTo()).isEqualTo("John Doe");
        
        verify(assignmentHistoryRepository).findByAssetIdOrderByAssignedAtDesc(assetId, pageable);
    }
    
    @Test
    @DisplayName("Should throw InsufficientPermissionsException when user lacks VIEW_ASSET permission")
    void shouldThrowExceptionWhenUserLacksViewAssetPermission() {
        // Given
        Pageable pageable = PageRequest.of(0, 20);
        
        when(authorizationService.hasPermission(userId.toString(), Action.VIEW_ASSET))
            .thenReturn(false);
        
        // When/Then
        assertThatThrownBy(() -> 
            allocationService.getAssignmentHistory(userId.toString(), assetId, pageable))
            .isInstanceOf(InsufficientPermissionsException.class)
            .hasMessageContaining("view assignment history");
        
        verify(assetRepository, never()).existsById(any());
        verify(assignmentHistoryRepository, never()).findByAssetIdOrderByAssignedAtDesc(any(), any());
    }
    
    @Test
    @DisplayName("Should throw ResourceNotFoundException when asset does not exist for history query")
    void shouldThrowExceptionWhenAssetNotFoundForHistoryQuery() {
        // Given
        Pageable pageable = PageRequest.of(0, 20);
        
        when(authorizationService.hasPermission(userId.toString(), Action.VIEW_ASSET))
            .thenReturn(true);
        when(assetRepository.existsById(assetId)).thenReturn(false);
        
        // When/Then
        assertThatThrownBy(() -> 
            allocationService.getAssignmentHistory(userId.toString(), assetId, pageable))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining(assetId.toString());
        
        verify(assignmentHistoryRepository, never()).findByAssetIdOrderByAssignedAtDesc(any(), any());
    }
    
    @Test
    @DisplayName("Should return results ordered by AssignedAt descending")
    void shouldReturnResultsOrderedByAssignedAtDescending() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        
        AssignmentHistory history1 = new AssignmentHistory();
        history1.setId(UUID.randomUUID());
        history1.setAssetId(assetId);
        history1.setAssignmentType(AssignmentType.USER);
        history1.setAssignedTo("User 1");
        history1.setAssignedBy(userId);
        history1.setAssignedAt(now.minusDays(3));
        history1.setUnassignedAt(now.minusDays(2));
        
        AssignmentHistory history2 = new AssignmentHistory();
        history2.setId(UUID.randomUUID());
        history2.setAssetId(assetId);
        history2.setAssignmentType(AssignmentType.USER);
        history2.setAssignedTo("User 2");
        history2.setAssignedBy(userId);
        history2.setAssignedAt(now.minusDays(1));
        history2.setUnassignedAt(now);
        
        AssignmentHistory history3 = new AssignmentHistory();
        history3.setId(UUID.randomUUID());
        history3.setAssetId(assetId);
        history3.setAssignmentType(AssignmentType.LOCATION);
        history3.setAssignedTo("Location A");
        history3.setAssignedBy(userId);
        history3.setAssignedAt(now);
        
        Pageable pageable = PageRequest.of(0, 20);
        Page<AssignmentHistory> historyPage = new PageImpl<>(
            Arrays.asList(history3, history2, history1), 
            pageable, 
            3
        );
        
        when(authorizationService.hasPermission(userId.toString(), Action.VIEW_ASSET))
            .thenReturn(true);
        when(assetRepository.existsById(assetId)).thenReturn(true);
        when(assignmentHistoryRepository.findByAssetIdOrderByAssignedAtDesc(assetId, pageable))
            .thenReturn(historyPage);
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        
        // When
        Page<AssignmentHistoryDTO> result = allocationService.getAssignmentHistory(
            userId.toString(), assetId, pageable
        );
        
        // Then
        assertThat(result.getContent()).hasSize(3);
        
        // Verify ordering - most recent first
        assertThat(result.getContent().get(0).getAssignedAt()).isEqualTo(now);
        assertThat(result.getContent().get(1).getAssignedAt()).isEqualTo(now.minusDays(1));
        assertThat(result.getContent().get(2).getAssignedAt()).isEqualTo(now.minusDays(3));
    }
    
    @Test
    @DisplayName("Should include username in assignment history DTO")
    void shouldIncludeUsernameInAssignmentHistoryDTO() {
        // Given
        AssignmentHistory history = new AssignmentHistory();
        history.setId(UUID.randomUUID());
        history.setAssetId(assetId);
        history.setAssignmentType(AssignmentType.USER);
        history.setAssignedTo("John Doe");
        history.setAssignedBy(userId);
        history.setAssignedAt(LocalDateTime.now());
        
        Pageable pageable = PageRequest.of(0, 20);
        Page<AssignmentHistory> historyPage = new PageImpl<>(
            Collections.singletonList(history), 
            pageable, 
            1
        );
        
        when(authorizationService.hasPermission(userId.toString(), Action.VIEW_ASSET))
            .thenReturn(true);
        when(assetRepository.existsById(assetId)).thenReturn(true);
        when(assignmentHistoryRepository.findByAssetIdOrderByAssignedAtDesc(assetId, pageable))
            .thenReturn(historyPage);
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        
        // When
        Page<AssignmentHistoryDTO> result = allocationService.getAssignmentHistory(
            userId.toString(), assetId, pageable
        );
        
        // Then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getAssignedByUsername()).isEqualTo("testuser");
    }
    
    @Test
    @DisplayName("Should return empty page when no assignment history exists")
    void shouldReturnEmptyPageWhenNoAssignmentHistoryExists() {
        // Given
        Pageable pageable = PageRequest.of(0, 20);
        Page<AssignmentHistory> emptyPage = new PageImpl<>(
            Collections.emptyList(), 
            pageable, 
            0
        );
        
        when(authorizationService.hasPermission(userId.toString(), Action.VIEW_ASSET))
            .thenReturn(true);
        when(assetRepository.existsById(assetId)).thenReturn(true);
        when(assignmentHistoryRepository.findByAssetIdOrderByAssignedAtDesc(assetId, pageable))
            .thenReturn(emptyPage);
        
        // When
        Page<AssignmentHistoryDTO> result = allocationService.getAssignmentHistory(
            userId.toString(), assetId, pageable
        );
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isEqualTo(0);
    }
    
    // Tests for getAssetsByUser
    
    @Test
    @DisplayName("Should retrieve assets by user with case-insensitive matching")
    void shouldRetrieveAssetsByUserWithCaseInsensitiveMatching() {
        // Given
        String userName = "john";
        Pageable pageable = PageRequest.of(0, 20);
        
        Asset asset1 = new Asset();
        asset1.setId(UUID.randomUUID());
        asset1.setName("Server 1");
        asset1.setSerialNumber("SRV-001");
        asset1.setAssetType(AssetType.SERVER);
        asset1.setStatus(LifecycleStatus.IN_USE);
        asset1.setAssignedUser("John Doe");
        asset1.setAssignmentDate(LocalDateTime.now());
        
        Asset asset2 = new Asset();
        asset2.setId(UUID.randomUUID());
        asset2.setName("Laptop 1");
        asset2.setSerialNumber("LAP-001");
        asset2.setAssetType(AssetType.WORKSTATION);
        asset2.setStatus(LifecycleStatus.DEPLOYED);
        asset2.setAssignedUser("Johnny Smith");
        asset2.setAssignmentDate(LocalDateTime.now());
        
        Page<Asset> assetsPage = new PageImpl<>(
            Arrays.asList(asset1, asset2), 
            pageable, 
            2
        );
        
        when(assetRepository.findByAssignedUserContainingIgnoreCase(userName, pageable))
            .thenReturn(assetsPage);
        
        // When
        Page<AssetDTO> result = allocationService.getAssetsByUser(userName, pageable);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getTotalElements()).isEqualTo(2);
        
        // Verify case-insensitive matching was used
        verify(assetRepository).findByAssignedUserContainingIgnoreCase(userName, pageable);
    }
    
    @Test
    @DisplayName("Should support pagination for assets by user query")
    void shouldSupportPaginationForAssetsByUserQuery() {
        // Given
        String userName = "john";
        Pageable pageable = PageRequest.of(1, 10); // Page 1, size 10
        
        List<Asset> assets = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            Asset asset = new Asset();
            asset.setId(UUID.randomUUID());
            asset.setName("Asset " + i);
            asset.setSerialNumber("SRV-" + i);
            asset.setAssetType(AssetType.SERVER);
            asset.setStatus(LifecycleStatus.IN_USE);
            asset.setAssignedUser("John Doe");
            assets.add(asset);
        }
        
        Page<Asset> assetsPage = new PageImpl<>(assets, pageable, 25);
        
        when(assetRepository.findByAssignedUserContainingIgnoreCase(userName, pageable))
            .thenReturn(assetsPage);
        
        // When
        Page<AssetDTO> result = allocationService.getAssetsByUser(userName, pageable);
        
        // Then
        assertThat(result.getNumber()).isEqualTo(1);
        assertThat(result.getSize()).isEqualTo(10);
        assertThat(result.getTotalElements()).isEqualTo(25);
        assertThat(result.getTotalPages()).isEqualTo(3);
    }
    
    @Test
    @DisplayName("Should return empty page when no assets assigned to user")
    void shouldReturnEmptyPageWhenNoAssetsAssignedToUser() {
        // Given
        String userName = "nonexistent";
        Pageable pageable = PageRequest.of(0, 20);
        Page<Asset> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);
        
        when(assetRepository.findByAssignedUserContainingIgnoreCase(userName, pageable))
            .thenReturn(emptyPage);
        
        // When
        Page<AssetDTO> result = allocationService.getAssetsByUser(userName, pageable);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isEqualTo(0);
    }
    
    @Test
    @DisplayName("Should support partial name matching for user query")
    void shouldSupportPartialNameMatchingForUserQuery() {
        // Given
        String partialName = "doe";
        Pageable pageable = PageRequest.of(0, 20);
        
        Asset asset1 = new Asset();
        asset1.setId(UUID.randomUUID());
        asset1.setName("Server 1");
        asset1.setAssetType(AssetType.SERVER);
        asset1.setStatus(LifecycleStatus.IN_USE);
        asset1.setAssignedUser("John Doe");
        
        Asset asset2 = new Asset();
        asset2.setId(UUID.randomUUID());
        asset2.setName("Server 2");
        asset2.setAssetType(AssetType.SERVER);
        asset2.setStatus(LifecycleStatus.IN_USE);
        asset2.setAssignedUser("Jane Doeson");
        
        Page<Asset> assetsPage = new PageImpl<>(
            Arrays.asList(asset1, asset2), 
            pageable, 
            2
        );
        
        when(assetRepository.findByAssignedUserContainingIgnoreCase(partialName, pageable))
            .thenReturn(assetsPage);
        
        // When
        Page<AssetDTO> result = allocationService.getAssetsByUser(partialName, pageable);
        
        // Then
        assertThat(result.getContent()).hasSize(2);
        
        // Verify partial matching was used (ContainingIgnoreCase)
        verify(assetRepository).findByAssignedUserContainingIgnoreCase(partialName, pageable);
    }
    
    // Tests for getAssetsByLocation
    
    @Test
    @DisplayName("Should retrieve assets by location with case-insensitive matching")
    void shouldRetrieveAssetsByLocationWithCaseInsensitiveMatching() {
        // Given
        String location = "data center";
        Pageable pageable = PageRequest.of(0, 20);
        
        Asset asset1 = new Asset();
        asset1.setId(UUID.randomUUID());
        asset1.setName("Server 1");
        asset1.setSerialNumber("SRV-001");
        asset1.setAssetType(AssetType.SERVER);
        asset1.setStatus(LifecycleStatus.IN_USE);
        asset1.setLocation("Data Center A");
        asset1.setLocationUpdateDate(LocalDateTime.now());
        
        Asset asset2 = new Asset();
        asset2.setId(UUID.randomUUID());
        asset2.setName("Server 2");
        asset2.setSerialNumber("SRV-002");
        asset2.setAssetType(AssetType.SERVER);
        asset2.setStatus(LifecycleStatus.DEPLOYED);
        asset2.setLocation("Data Center B");
        asset2.setLocationUpdateDate(LocalDateTime.now());
        
        Page<Asset> assetsPage = new PageImpl<>(
            Arrays.asList(asset1, asset2), 
            pageable, 
            2
        );
        
        when(assetRepository.findByLocationContainingIgnoreCase(location, pageable))
            .thenReturn(assetsPage);
        
        // When
        Page<AssetDTO> result = allocationService.getAssetsByLocation(location, pageable);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getTotalElements()).isEqualTo(2);
        
        // Verify case-insensitive matching was used
        verify(assetRepository).findByLocationContainingIgnoreCase(location, pageable);
    }
    
    @Test
    @DisplayName("Should support pagination for assets by location query")
    void shouldSupportPaginationForAssetsByLocationQuery() {
        // Given
        String location = "warehouse";
        Pageable pageable = PageRequest.of(2, 15); // Page 2, size 15
        
        List<Asset> assets = new ArrayList<>();
        for (int i = 0; i < 15; i++) {
            Asset asset = new Asset();
            asset.setId(UUID.randomUUID());
            asset.setName("Asset " + i);
            asset.setSerialNumber("AST-" + i);
            asset.setAssetType(AssetType.NETWORK_DEVICE);
            asset.setStatus(LifecycleStatus.STORAGE);
            asset.setLocation("Warehouse 1");
            assets.add(asset);
        }
        
        Page<Asset> assetsPage = new PageImpl<>(assets, pageable, 50);
        
        when(assetRepository.findByLocationContainingIgnoreCase(location, pageable))
            .thenReturn(assetsPage);
        
        // When
        Page<AssetDTO> result = allocationService.getAssetsByLocation(location, pageable);
        
        // Then
        assertThat(result.getNumber()).isEqualTo(2);
        assertThat(result.getSize()).isEqualTo(15);
        assertThat(result.getTotalElements()).isEqualTo(50);
        assertThat(result.getTotalPages()).isEqualTo(4);
    }
    
    @Test
    @DisplayName("Should return empty page when no assets at location")
    void shouldReturnEmptyPageWhenNoAssetsAtLocation() {
        // Given
        String location = "nonexistent location";
        Pageable pageable = PageRequest.of(0, 20);
        Page<Asset> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);
        
        when(assetRepository.findByLocationContainingIgnoreCase(location, pageable))
            .thenReturn(emptyPage);
        
        // When
        Page<AssetDTO> result = allocationService.getAssetsByLocation(location, pageable);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isEqualTo(0);
    }
    
    @Test
    @DisplayName("Should support partial name matching for location query")
    void shouldSupportPartialNameMatchingForLocationQuery() {
        // Given
        String partialLocation = "center";
        Pageable pageable = PageRequest.of(0, 20);
        
        Asset asset1 = new Asset();
        asset1.setId(UUID.randomUUID());
        asset1.setName("Server 1");
        asset1.setAssetType(AssetType.SERVER);
        asset1.setStatus(LifecycleStatus.IN_USE);
        asset1.setLocation("Data Center A");
        
        Asset asset2 = new Asset();
        asset2.setId(UUID.randomUUID());
        asset2.setName("Server 2");
        asset2.setAssetType(AssetType.SERVER);
        asset2.setStatus(LifecycleStatus.IN_USE);
        asset2.setLocation("Distribution Center");
        
        Page<Asset> assetsPage = new PageImpl<>(
            Arrays.asList(asset1, asset2), 
            pageable, 
            2
        );
        
        when(assetRepository.findByLocationContainingIgnoreCase(partialLocation, pageable))
            .thenReturn(assetsPage);
        
        // When
        Page<AssetDTO> result = allocationService.getAssetsByLocation(partialLocation, pageable);
        
        // Then
        assertThat(result.getContent()).hasSize(2);
        
        // Verify partial matching was used (ContainingIgnoreCase)
        verify(assetRepository).findByLocationContainingIgnoreCase(partialLocation, pageable);
    }
    
    @Test
    @DisplayName("Should include asset details in query results")
    void shouldIncludeAssetDetailsInQueryResults() {
        // Given
        String location = "office";
        Pageable pageable = PageRequest.of(0, 20);
        
        Asset asset = new Asset();
        asset.setId(UUID.randomUUID());
        asset.setName("Workstation 1");
        asset.setSerialNumber("WS-001");
        asset.setAssetType(AssetType.WORKSTATION);
        asset.setStatus(LifecycleStatus.IN_USE);
        asset.setLocation("Office Building A");
        asset.setLocationUpdateDate(LocalDateTime.now());
        asset.setAcquisitionDate(LocalDate.now().minusMonths(6));
        
        Page<Asset> assetsPage = new PageImpl<>(
            Collections.singletonList(asset), 
            pageable, 
            1
        );
        
        when(assetRepository.findByLocationContainingIgnoreCase(location, pageable))
            .thenReturn(assetsPage);
        
        // When
        Page<AssetDTO> result = allocationService.getAssetsByLocation(location, pageable);
        
        // Then
        assertThat(result.getContent()).hasSize(1);
        AssetDTO dto = result.getContent().get(0);
        
        assertThat(dto.getId()).isEqualTo(asset.getId());
        assertThat(dto.getName()).isEqualTo("Workstation 1");
        assertThat(dto.getSerialNumber()).isEqualTo("WS-001");
        assertThat(dto.getAssetType()).isEqualTo(AssetType.WORKSTATION);
        assertThat(dto.getStatus()).isEqualTo(LifecycleStatus.IN_USE);
        assertThat(dto.getLocation()).isEqualTo("Office Building A");
        assertThat(dto.getLocationUpdateDate()).isNotNull();
    }
    
    // Task 3.6: Export with Filtering Tests
    
    @Test
    @DisplayName("Should export assignments filtered by assignment type")
    void shouldExportAssignmentsFilteredByAssignmentType() {
        // Given
        AssignmentHistory userAssignment = createTestAssignment(assetId);
        userAssignment.setAssignmentType(AssignmentType.USER);
        
        AssignmentHistory locationAssignment = createTestAssignment(UUID.randomUUID());
        locationAssignment.setAssignmentType(AssignmentType.LOCATION);
        
        Asset asset = createTestAsset(assetId);
        
        Map<String, Object> filters = new HashMap<>();
        filters.put("assignmentType", "USER");
        
        when(assignmentHistoryRepository.findActiveAssignmentsWithFilters(
            eq(AssignmentType.USER), eq(null), eq(null), eq(null)))
            .thenReturn(Collections.singletonList(userAssignment));
        when(assetRepository.findById(assetId)).thenReturn(Optional.of(asset));
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        
        // When
        byte[] csv = allocationService.exportAssignments(filters);
        
        // Then
        assertThat(csv).isNotNull();
        String csvContent = new String(csv);
        assertThat(csvContent).contains("USER");
        assertThat(csvContent).doesNotContain("LOCATION");
        
        verify(assignmentHistoryRepository).findActiveAssignmentsWithFilters(
            eq(AssignmentType.USER), eq(null), eq(null), eq(null));
    }
    
    @Test
    @DisplayName("Should export assignments filtered by date range")
    void shouldExportAssignmentsFilteredByDateRange() {
        // Given
        LocalDateTime dateFrom = LocalDateTime.of(2024, 1, 1, 0, 0);
        LocalDateTime dateTo = LocalDateTime.of(2024, 12, 31, 23, 59);
        
        AssignmentHistory assignment = createTestAssignment(assetId);
        assignment.setAssignedAt(LocalDateTime.of(2024, 6, 15, 10, 30));
        
        Asset asset = createTestAsset(assetId);
        
        Map<String, Object> filters = new HashMap<>();
        filters.put("dateFrom", dateFrom.toString());
        filters.put("dateTo", dateTo.toString());
        
        when(assignmentHistoryRepository.findActiveAssignmentsWithFilters(
            eq(null), eq(dateFrom), eq(dateTo), eq(null)))
            .thenReturn(Collections.singletonList(assignment));
        when(assetRepository.findById(assetId)).thenReturn(Optional.of(asset));
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        
        // When
        byte[] csv = allocationService.exportAssignments(filters);
        
        // Then
        assertThat(csv).isNotNull();
        String csvContent = new String(csv);
        assertThat(csvContent).contains(asset.getName());
        
        verify(assignmentHistoryRepository).findActiveAssignmentsWithFilters(
            eq(null), eq(dateFrom), eq(dateTo), eq(null));
    }
    
    @Test
    @DisplayName("Should export assignments filtered by assigned by user")
    void shouldExportAssignmentsFilteredByAssignedByUser() {
        // Given
        UUID specificUserId = UUID.randomUUID();
        AssignmentHistory assignment = createTestAssignment(assetId);
        assignment.setAssignedBy(specificUserId);
        
        Asset asset = createTestAsset(assetId);
        
        User specificUser = new User();
        specificUser.setId(specificUserId);
        specificUser.setUsername("specificuser");
        
        Map<String, Object> filters = new HashMap<>();
        filters.put("assignedBy", specificUserId.toString());
        
        when(assignmentHistoryRepository.findActiveAssignmentsWithFilters(
            eq(null), eq(null), eq(null), eq(specificUserId)))
            .thenReturn(Collections.singletonList(assignment));
        when(assetRepository.findById(assetId)).thenReturn(Optional.of(asset));
        when(userRepository.findById(specificUserId)).thenReturn(Optional.of(specificUser));
        
        // When
        byte[] csv = allocationService.exportAssignments(filters);
        
        // Then
        assertThat(csv).isNotNull();
        String csvContent = new String(csv);
        assertThat(csvContent).contains("specificuser");
        
        verify(assignmentHistoryRepository).findActiveAssignmentsWithFilters(
            eq(null), eq(null), eq(null), eq(specificUserId));
    }
    
    @Test
    @DisplayName("Should export assignments with multiple filters combined")
    void shouldExportAssignmentsWithMultipleFiltersCombined() {
        // Given
        LocalDateTime dateFrom = LocalDateTime.of(2024, 1, 1, 0, 0);
        LocalDateTime dateTo = LocalDateTime.of(2024, 12, 31, 23, 59);
        UUID specificUserId = UUID.randomUUID();
        
        AssignmentHistory assignment = createTestAssignment(assetId);
        assignment.setAssignmentType(AssignmentType.USER);
        assignment.setAssignedBy(specificUserId);
        assignment.setAssignedAt(LocalDateTime.of(2024, 6, 15, 10, 30));
        
        Asset asset = createTestAsset(assetId);
        
        User specificUser = new User();
        specificUser.setId(specificUserId);
        specificUser.setUsername("specificuser");
        
        Map<String, Object> filters = new HashMap<>();
        filters.put("assignmentType", "USER");
        filters.put("dateFrom", dateFrom.toString());
        filters.put("dateTo", dateTo.toString());
        filters.put("assignedBy", specificUserId.toString());
        
        when(assignmentHistoryRepository.findActiveAssignmentsWithFilters(
            eq(AssignmentType.USER), eq(dateFrom), eq(dateTo), eq(specificUserId)))
            .thenReturn(Collections.singletonList(assignment));
        when(assetRepository.findById(assetId)).thenReturn(Optional.of(asset));
        when(userRepository.findById(specificUserId)).thenReturn(Optional.of(specificUser));
        
        // When
        byte[] csv = allocationService.exportAssignments(filters);
        
        // Then
        assertThat(csv).isNotNull();
        String csvContent = new String(csv);
        assertThat(csvContent).contains("USER");
        assertThat(csvContent).contains("specificuser");
        
        verify(assignmentHistoryRepository).findActiveAssignmentsWithFilters(
            eq(AssignmentType.USER), eq(dateFrom), eq(dateTo), eq(specificUserId));
    }
    
    @Test
    @DisplayName("Should handle null filters map gracefully")
    void shouldHandleNullFiltersMapGracefully() {
        // Given
        AssignmentHistory assignment = createTestAssignment(assetId);
        Asset asset = createTestAsset(assetId);
        
        when(assignmentHistoryRepository.findActiveAssignmentsWithFilters(
            eq(null), eq(null), eq(null), eq(null)))
            .thenReturn(Collections.singletonList(assignment));
        when(assetRepository.findById(assetId)).thenReturn(Optional.of(asset));
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        
        // When
        byte[] csv = allocationService.exportAssignments(null);
        
        // Then
        assertThat(csv).isNotNull();
        verify(assignmentHistoryRepository).findActiveAssignmentsWithFilters(
            eq(null), eq(null), eq(null), eq(null));
    }
    
    @Test
    @DisplayName("Should handle empty filters map gracefully")
    void shouldHandleEmptyFiltersMapGracefully() {
        // Given
        AssignmentHistory assignment = createTestAssignment(assetId);
        Asset asset = createTestAsset(assetId);
        
        when(assignmentHistoryRepository.findActiveAssignmentsWithFilters(
            eq(null), eq(null), eq(null), eq(null)))
            .thenReturn(Collections.singletonList(assignment));
        when(assetRepository.findById(assetId)).thenReturn(Optional.of(asset));
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        
        // When
        byte[] csv = allocationService.exportAssignments(Collections.emptyMap());
        
        // Then
        assertThat(csv).isNotNull();
        verify(assignmentHistoryRepository).findActiveAssignmentsWithFilters(
            eq(null), eq(null), eq(null), eq(null));
    }
    
    @Test
    @DisplayName("Should handle invalid assignment type filter gracefully")
    void shouldHandleInvalidAssignmentTypeFilterGracefully() {
        // Given
        AssignmentHistory assignment = createTestAssignment(assetId);
        Asset asset = createTestAsset(assetId);
        
        Map<String, Object> filters = new HashMap<>();
        filters.put("assignmentType", "INVALID_TYPE");
        
        when(assignmentHistoryRepository.findActiveAssignmentsWithFilters(
            eq(null), eq(null), eq(null), eq(null)))
            .thenReturn(Collections.singletonList(assignment));
        when(assetRepository.findById(assetId)).thenReturn(Optional.of(asset));
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        
        // When
        byte[] csv = allocationService.exportAssignments(filters);
        
        // Then
        assertThat(csv).isNotNull();
        // Should ignore invalid filter and proceed with null
        verify(assignmentHistoryRepository).findActiveAssignmentsWithFilters(
            eq(null), eq(null), eq(null), eq(null));
    }
    
    @Test
    @DisplayName("Should handle invalid date filter gracefully")
    void shouldHandleInvalidDateFilterGracefully() {
        // Given
        AssignmentHistory assignment = createTestAssignment(assetId);
        Asset asset = createTestAsset(assetId);
        
        Map<String, Object> filters = new HashMap<>();
        filters.put("dateFrom", "invalid-date");
        
        when(assignmentHistoryRepository.findActiveAssignmentsWithFilters(
            eq(null), eq(null), eq(null), eq(null)))
            .thenReturn(Collections.singletonList(assignment));
        when(assetRepository.findById(assetId)).thenReturn(Optional.of(asset));
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        
        // When
        byte[] csv = allocationService.exportAssignments(filters);
        
        // Then
        assertThat(csv).isNotNull();
        // Should ignore invalid filter and proceed with null
        verify(assignmentHistoryRepository).findActiveAssignmentsWithFilters(
            eq(null), eq(null), eq(null), eq(null));
    }
    
    @Test
    @DisplayName("Should handle invalid UUID filter gracefully")
    void shouldHandleInvalidUuidFilterGracefully() {
        // Given
        AssignmentHistory assignment = createTestAssignment(assetId);
        Asset asset = createTestAsset(assetId);
        
        Map<String, Object> filters = new HashMap<>();
        filters.put("assignedBy", "invalid-uuid");
        
        when(assignmentHistoryRepository.findActiveAssignmentsWithFilters(
            eq(null), eq(null), eq(null), eq(null)))
            .thenReturn(Collections.singletonList(assignment));
        when(assetRepository.findById(assetId)).thenReturn(Optional.of(asset));
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        
        // When
        byte[] csv = allocationService.exportAssignments(filters);
        
        // Then
        assertThat(csv).isNotNull();
        // Should ignore invalid filter and proceed with null
        verify(assignmentHistoryRepository).findActiveAssignmentsWithFilters(
            eq(null), eq(null), eq(null), eq(null));
    }
    
    @Test
    @DisplayName("Should include filters in audit log metadata")
    void shouldIncludeFiltersInAuditLogMetadata() {
        // Given
        AssignmentHistory assignment = createTestAssignment(assetId);
        Asset asset = createTestAsset(assetId);
        
        Map<String, Object> filters = new HashMap<>();
        filters.put("assignmentType", "USER");
        filters.put("dateFrom", "2024-01-01T00:00:00");
        
        when(assignmentHistoryRepository.findActiveAssignmentsWithFilters(
            any(), any(), any(), any()))
            .thenReturn(Collections.singletonList(assignment));
        when(assetRepository.findById(assetId)).thenReturn(Optional.of(asset));
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        
        // When
        allocationService.exportAssignments(filters);
        
        // Then
        ArgumentCaptor<AuditEventDTO> auditCaptor = ArgumentCaptor.forClass(AuditEventDTO.class);
        verify(auditService).logEvent(auditCaptor.capture());
        AuditEventDTO auditEvent = auditCaptor.getValue();
        
        assertThat(auditEvent.getActionType()).isEqualTo(Action.EXPORT_DATA);
        assertThat(auditEvent.getResourceType()).isEqualTo("ASSIGNMENT");
        assertThat(auditEvent.getMetadata()).containsKey("recordCount");
        assertThat(auditEvent.getMetadata()).containsKey("filters");
    }
    
    @Test
    @DisplayName("Should respect 10000 record limit with filters")
    void shouldRespectRecordLimitWithFilters() {
        // Given
        List<AssignmentHistory> largeList = new ArrayList<>();
        for (int i = 0; i < 10001; i++) {
            largeList.add(createTestAssignment(UUID.randomUUID()));
        }
        
        Map<String, Object> filters = new HashMap<>();
        filters.put("assignmentType", "USER");
        
        when(assignmentHistoryRepository.findActiveAssignmentsWithFilters(
            eq(AssignmentType.USER), eq(null), eq(null), eq(null)))
            .thenReturn(largeList);
        
        // When/Then
        assertThatThrownBy(() -> allocationService.exportAssignments(filters))
            .isInstanceOf(ValidationException.class)
            .hasMessageContaining("Export limited to 10000 records");
    }
    
    @Test
    @DisplayName("Should accept AssignmentType enum directly in filters")
    void shouldAcceptAssignmentTypeEnumDirectlyInFilters() {
        // Given
        AssignmentHistory assignment = createTestAssignment(assetId);
        Asset asset = createTestAsset(assetId);
        
        Map<String, Object> filters = new HashMap<>();
        filters.put("assignmentType", AssignmentType.USER);
        
        when(assignmentHistoryRepository.findActiveAssignmentsWithFilters(
            eq(AssignmentType.USER), eq(null), eq(null), eq(null)))
            .thenReturn(Collections.singletonList(assignment));
        when(assetRepository.findById(assetId)).thenReturn(Optional.of(asset));
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        
        // When
        byte[] csv = allocationService.exportAssignments(filters);
        
        // Then
        assertThat(csv).isNotNull();
        verify(assignmentHistoryRepository).findActiveAssignmentsWithFilters(
            eq(AssignmentType.USER), eq(null), eq(null), eq(null));
    }
    
    @Test
    @DisplayName("Should accept LocalDateTime directly in filters")
    void shouldAcceptLocalDateTimeDirectlyInFilters() {
        // Given
        LocalDateTime dateFrom = LocalDateTime.of(2024, 1, 1, 0, 0);
        AssignmentHistory assignment = createTestAssignment(assetId);
        Asset asset = createTestAsset(assetId);
        
        Map<String, Object> filters = new HashMap<>();
        filters.put("dateFrom", dateFrom);
        
        when(assignmentHistoryRepository.findActiveAssignmentsWithFilters(
            eq(null), eq(dateFrom), eq(null), eq(null)))
            .thenReturn(Collections.singletonList(assignment));
        when(assetRepository.findById(assetId)).thenReturn(Optional.of(asset));
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        
        // When
        byte[] csv = allocationService.exportAssignments(filters);
        
        // Then
        assertThat(csv).isNotNull();
        verify(assignmentHistoryRepository).findActiveAssignmentsWithFilters(
            eq(null), eq(dateFrom), eq(null), eq(null));
    }
    
    @Test
    @DisplayName("Should accept UUID directly in filters")
    void shouldAcceptUuidDirectlyInFilters() {
        // Given
        UUID specificUserId = UUID.randomUUID();
        AssignmentHistory assignment = createTestAssignment(assetId);
        Asset asset = createTestAsset(assetId);
        
        User specificUser = new User();
        specificUser.setId(specificUserId);
        specificUser.setUsername("specificuser");
        
        Map<String, Object> filters = new HashMap<>();
        filters.put("assignedBy", specificUserId);
        
        when(assignmentHistoryRepository.findActiveAssignmentsWithFilters(
            eq(null), eq(null), eq(null), eq(specificUserId)))
            .thenReturn(Collections.singletonList(assignment));
        when(assetRepository.findById(assetId)).thenReturn(Optional.of(asset));
        when(userRepository.findById(specificUserId)).thenReturn(Optional.of(specificUser));
        
        // When
        byte[] csv = allocationService.exportAssignments(filters);
        
        // Then
        assertThat(csv).isNotNull();
        verify(assignmentHistoryRepository).findActiveAssignmentsWithFilters(
            eq(null), eq(null), eq(null), eq(specificUserId));
    }
}

