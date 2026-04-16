package com.company.assetmanagement.service;

import com.company.assetmanagement.dto.AssetDTO;
import com.company.assetmanagement.dto.AssetRequest;
import com.company.assetmanagement.dto.AuditEventDTO;
import com.company.assetmanagement.dto.ValidationError;
import com.company.assetmanagement.exception.DuplicateSerialNumberException;
import com.company.assetmanagement.exception.ValidationException;
import com.company.assetmanagement.model.Action;
import com.company.assetmanagement.model.Asset;
import com.company.assetmanagement.model.AssetType;
import com.company.assetmanagement.model.LifecycleStatus;
import com.company.assetmanagement.repository.AssetRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AssetServiceImpl.
 * Tests the createAsset() method with mocked dependencies.
 * 
 * Test Coverage:
 * - Successful asset creation
 * - Authorization checks
 * - Validation scenarios
 * - Serial number uniqueness enforcement
 * - Audit logging integration
 * - Error handling
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AssetServiceImpl Unit Tests")
class AssetServiceImplTest {
    
    @Mock
    private AssetRepository assetRepository;
    
    @Mock
    private AuditService auditService;
    
    @Mock
    private AssetValidationService validationService;

    @Mock
    private AuthorizationService authorizationService;
    
    @InjectMocks
    private AssetServiceImpl assetService;
    
    private String testUserId;
    private AssetRequest validRequest;
    
    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID().toString();
        validRequest = createValidAssetRequest();
        lenient().when(authorizationService.hasPermission(anyString(), any(Action.class))).thenReturn(true);
        lenient().when(authorizationService.resolveActorUuid(anyString()))
            .thenAnswer(invocation -> UUID.fromString(invocation.getArgument(0, String.class)));
    }
    
    // ========== Successful Creation Tests ==========
    
    @Test
    @DisplayName("Should create asset successfully with valid data")
    void shouldCreateAssetSuccessfully() {
        // Given
        when(assetRepository.existsBySerialNumber(validRequest.getSerialNumber()))
            .thenReturn(false);
        when(assetRepository.save(any(Asset.class)))
            .thenAnswer(invocation -> {
                Asset asset = invocation.getArgument(0);
                asset.setId(UUID.randomUUID());
                return asset;
            });
        
        // When
        AssetDTO result = assetService.createAsset(testUserId, validRequest);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isNotNull();
        assertThat(result.getAssetType()).isEqualTo(validRequest.getAssetType());
        assertThat(result.getName()).isEqualTo(validRequest.getName());
        assertThat(result.getSerialNumber()).isEqualTo(validRequest.getSerialNumber());
        assertThat(result.getAcquisitionDate()).isEqualTo(validRequest.getAcquisitionDate());
        assertThat(result.getStatus()).isEqualTo(validRequest.getStatus());
        assertThat(result.isReadOnly()).isFalse();
        
        verify(validationService).validateAssetRequest(validRequest);
        verify(assetRepository).existsBySerialNumber(validRequest.getSerialNumber());
        verify(assetRepository).save(any(Asset.class));
        verify(auditService).logEvent(any(AuditEventDTO.class));
    }
    
    @Test
    @DisplayName("Should set createdBy and updatedBy fields to user ID")
    void shouldSetAuditFieldsToUserId() {
        // Given
        when(assetRepository.existsBySerialNumber(anyString())).thenReturn(false);
        
        ArgumentCaptor<Asset> assetCaptor = ArgumentCaptor.forClass(Asset.class);
        when(assetRepository.save(assetCaptor.capture()))
            .thenAnswer(invocation -> {
                Asset asset = invocation.getArgument(0);
                asset.setId(UUID.randomUUID());
                return asset;
            });
        
        // When
        assetService.createAsset(testUserId, validRequest);
        
        // Then
        Asset savedAsset = assetCaptor.getValue();
        assertThat(savedAsset.getCreatedBy()).isEqualTo(UUID.fromString(testUserId));
        assertThat(savedAsset.getUpdatedBy()).isEqualTo(UUID.fromString(testUserId));
    }
    
    @Test
    @DisplayName("Should set readOnly to false for new assets")
    void shouldSetReadOnlyToFalse() {
        // Given
        when(assetRepository.existsBySerialNumber(anyString())).thenReturn(false);
        
        ArgumentCaptor<Asset> assetCaptor = ArgumentCaptor.forClass(Asset.class);
        when(assetRepository.save(assetCaptor.capture()))
            .thenAnswer(invocation -> {
                Asset asset = invocation.getArgument(0);
                asset.setId(UUID.randomUUID());
                return asset;
            });
        
        // When
        assetService.createAsset(testUserId, validRequest);
        
        // Then
        Asset savedAsset = assetCaptor.getValue();
        assertThat(savedAsset.isReadOnly()).isFalse();
    }
    
    @Test
    @DisplayName("Should create asset with optional fields")
    void shouldCreateAssetWithOptionalFields() {
        // Given
        AssetRequest requestWithOptionalFields = AssetRequest.builder()
            .assetType(AssetType.LAPTOP)
            .name("Test Laptop")
            .serialNumber("LAP-TEST-001")
            .acquisitionDate(LocalDate.now())
            .status(LifecycleStatus.ORDERED)
            .location("Office Building A")
            .assignedUser("john.doe")
            .assignedUserEmail("john.doe@example.com")
            .notes("Test notes")
            .customFields("{\"warranty\": \"3 years\"}")
            .build();
        
        when(assetRepository.existsBySerialNumber(anyString())).thenReturn(false);
        when(assetRepository.save(any(Asset.class)))
            .thenAnswer(invocation -> {
                Asset asset = invocation.getArgument(0);
                asset.setId(UUID.randomUUID());
                return asset;
            });
        
        // When
        AssetDTO result = assetService.createAsset(testUserId, requestWithOptionalFields);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.getLocation()).isEqualTo("Office Building A");
        assertThat(result.getAssignedUser()).isEqualTo("john.doe");
        assertThat(result.getAssignedUserEmail()).isEqualTo("john.doe@example.com");
        assertThat(result.getNotes()).isEqualTo("Test notes");
        assertThat(result.getCustomFields()).isEqualTo("{\"warranty\": \"3 years\"}");
    }
    
    // ========== Validation Tests ==========
    
    @Test
    @DisplayName("Should call validation service before creating asset")
    void shouldCallValidationService() {
        // Given
        when(assetRepository.existsBySerialNumber(anyString())).thenReturn(false);
        when(assetRepository.save(any(Asset.class)))
            .thenAnswer(invocation -> {
                Asset asset = invocation.getArgument(0);
                asset.setId(UUID.randomUUID());
                return asset;
            });
        
        // When
        assetService.createAsset(testUserId, validRequest);
        
        // Then
        verify(validationService).validateAssetRequest(validRequest);
    }
    
    @Test
    @DisplayName("Should throw ValidationException when validation fails")
    void shouldThrowValidationExceptionWhenValidationFails() {
        // Given
        doThrow(new ValidationException("Validation failed"))
            .when(validationService).validateAssetRequest(any());
        
        // When/Then
        assertThatThrownBy(() -> assetService.createAsset(testUserId, validRequest))
            .isInstanceOf(ValidationException.class)
            .hasMessageContaining("Validation failed");
        
        verify(validationService).validateAssetRequest(validRequest);
        verify(assetRepository, never()).existsBySerialNumber(anyString());
        verify(assetRepository, never()).save(any());
        verify(auditService, never()).logEvent(any());
    }
    
    @Test
    @DisplayName("Should throw IllegalArgumentException when userId is null")
    void shouldThrowExceptionWhenUserIdIsNull() {
        // When/Then
        assertThatThrownBy(() -> assetService.createAsset(null, validRequest))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("User ID");
        
        verify(validationService, never()).validateAssetRequest(any());
        verify(assetRepository, never()).save(any());
    }
    
    @Test
    @DisplayName("Should throw IllegalArgumentException when userId is empty")
    void shouldThrowExceptionWhenUserIdIsEmpty() {
        // When/Then
        assertThatThrownBy(() -> assetService.createAsset("", validRequest))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("User ID");
        
        verify(validationService, never()).validateAssetRequest(any());
        verify(assetRepository, never()).save(any());
    }
    
    @Test
    @DisplayName("Should throw IllegalArgumentException when userId is blank")
    void shouldThrowExceptionWhenUserIdIsBlank() {
        // When/Then
        assertThatThrownBy(() -> assetService.createAsset("   ", validRequest))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("User ID");
        
        verify(validationService, never()).validateAssetRequest(any());
        verify(assetRepository, never()).save(any());
    }
    
    @Test
    @DisplayName("Should throw IllegalArgumentException when request is null")
    void shouldThrowExceptionWhenRequestIsNull() {
        // When/Then
        assertThatThrownBy(() -> assetService.createAsset(testUserId, null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("request");
        
        verify(validationService, never()).validateAssetRequest(any());
        verify(assetRepository, never()).save(any());
    }
    
    // ========== Serial Number Uniqueness Tests ==========
    
    @Test
    @DisplayName("Should check serial number uniqueness before creating asset")
    void shouldCheckSerialNumberUniqueness() {
        // Given
        when(assetRepository.existsBySerialNumber(validRequest.getSerialNumber()))
            .thenReturn(false);
        when(assetRepository.save(any(Asset.class)))
            .thenAnswer(invocation -> {
                Asset asset = invocation.getArgument(0);
                asset.setId(UUID.randomUUID());
                return asset;
            });
        
        // When
        assetService.createAsset(testUserId, validRequest);
        
        // Then
        verify(assetRepository).existsBySerialNumber(validRequest.getSerialNumber());
    }
    
    @Test
    @DisplayName("Should throw DuplicateSerialNumberException when serial number exists")
    void shouldThrowExceptionWhenSerialNumberExists() {
        // Given
        when(assetRepository.existsBySerialNumber(validRequest.getSerialNumber()))
            .thenReturn(true);
        
        // When/Then
        assertThatThrownBy(() -> assetService.createAsset(testUserId, validRequest))
            .isInstanceOf(DuplicateSerialNumberException.class)
            .hasMessageContaining(validRequest.getSerialNumber());
        
        verify(validationService).validateAssetRequest(validRequest);
        verify(assetRepository).existsBySerialNumber(validRequest.getSerialNumber());
        verify(assetRepository, never()).save(any());
        verify(auditService, never()).logEvent(any());
    }
    
    @Test
    @DisplayName("Should not save asset when serial number is duplicate")
    void shouldNotSaveAssetWhenSerialNumberDuplicate() {
        // Given
        when(assetRepository.existsBySerialNumber(anyString())).thenReturn(true);
        
        // When/Then
        assertThatThrownBy(() -> assetService.createAsset(testUserId, validRequest))
            .isInstanceOf(DuplicateSerialNumberException.class);
        
        verify(assetRepository, never()).save(any());
    }
    
    // ========== Audit Logging Tests ==========
    
    @Test
    @DisplayName("Should log audit event after successful creation")
    void shouldLogAuditEventAfterCreation() {
        // Given
        UUID assetId = UUID.randomUUID();
        when(assetRepository.existsBySerialNumber(anyString())).thenReturn(false);
        when(assetRepository.save(any(Asset.class)))
            .thenAnswer(invocation -> {
                Asset asset = invocation.getArgument(0);
                asset.setId(assetId);
                return asset;
            });
        
        ArgumentCaptor<AuditEventDTO> eventCaptor = ArgumentCaptor.forClass(AuditEventDTO.class);
        
        // When
        assetService.createAsset(testUserId, validRequest);
        
        // Then
        verify(auditService).logEvent(eventCaptor.capture());
        
        AuditEventDTO capturedEvent = eventCaptor.getValue();
        assertThat(capturedEvent.getUserId()).isEqualTo(UUID.fromString(testUserId));
        assertThat(capturedEvent.getActionType()).isEqualTo(Action.CREATE_ASSET);
        assertThat(capturedEvent.getResourceType()).isEqualTo("ASSET");
        assertThat(capturedEvent.getResourceId()).isEqualTo(assetId.toString());
    }
    
    @Test
    @DisplayName("Should not fail asset creation when audit logging fails")
    void shouldNotFailCreationWhenAuditLoggingFails() {
        // Given
        when(assetRepository.existsBySerialNumber(anyString())).thenReturn(false);
        when(assetRepository.save(any(Asset.class)))
            .thenAnswer(invocation -> {
                Asset asset = invocation.getArgument(0);
                asset.setId(UUID.randomUUID());
                return asset;
            });
        
        doThrow(new RuntimeException("Audit service unavailable"))
            .when(auditService).logEvent(any());
        
        // When/Then - should not throw exception
        assertThatCode(() -> assetService.createAsset(testUserId, validRequest))
            .doesNotThrowAnyException();
        
        verify(assetRepository).save(any(Asset.class));
    }
    
    // ========== Integration with Repository Tests ==========
    
    @Test
    @DisplayName("Should save asset to repository")
    void shouldSaveAssetToRepository() {
        // Given
        when(assetRepository.existsBySerialNumber(anyString())).thenReturn(false);
        when(assetRepository.save(any(Asset.class)))
            .thenAnswer(invocation -> {
                Asset asset = invocation.getArgument(0);
                asset.setId(UUID.randomUUID());
                return asset;
            });
        
        // When
        assetService.createAsset(testUserId, validRequest);
        
        // Then
        verify(assetRepository).save(any(Asset.class));
    }
    
    @Test
    @DisplayName("Should map request fields to entity correctly")
    void shouldMapRequestFieldsToEntity() {
        // Given
        when(assetRepository.existsBySerialNumber(anyString())).thenReturn(false);
        
        ArgumentCaptor<Asset> assetCaptor = ArgumentCaptor.forClass(Asset.class);
        when(assetRepository.save(assetCaptor.capture()))
            .thenAnswer(invocation -> {
                Asset asset = invocation.getArgument(0);
                asset.setId(UUID.randomUUID());
                return asset;
            });
        
        // When
        assetService.createAsset(testUserId, validRequest);
        
        // Then
        Asset savedAsset = assetCaptor.getValue();
        assertThat(savedAsset.getAssetType()).isEqualTo(validRequest.getAssetType());
        assertThat(savedAsset.getName()).isEqualTo(validRequest.getName());
        assertThat(savedAsset.getSerialNumber()).isEqualTo(validRequest.getSerialNumber());
        assertThat(savedAsset.getAcquisitionDate()).isEqualTo(validRequest.getAcquisitionDate());
        assertThat(savedAsset.getStatus()).isEqualTo(validRequest.getStatus());
    }
    
    @Test
    @DisplayName("Should return DTO with all fields from saved entity")
    void shouldReturnDTOWithAllFields() {
        // Given
        UUID assetId = UUID.randomUUID();
        when(assetRepository.existsBySerialNumber(anyString())).thenReturn(false);
        when(assetRepository.save(any(Asset.class)))
            .thenAnswer(invocation -> {
                Asset asset = invocation.getArgument(0);
                asset.setId(assetId);
                return asset;
            });
        
        // When
        AssetDTO result = assetService.createAsset(testUserId, validRequest);
        
        // Then
        assertThat(result.getId()).isEqualTo(assetId.toString());
        assertThat(result.getAssetType()).isEqualTo(validRequest.getAssetType());
        assertThat(result.getName()).isEqualTo(validRequest.getName());
        assertThat(result.getSerialNumber()).isEqualTo(validRequest.getSerialNumber());
        assertThat(result.getAcquisitionDate()).isEqualTo(validRequest.getAcquisitionDate());
        assertThat(result.getStatus()).isEqualTo(validRequest.getStatus());
        assertThat(result.getCreatedBy()).isEqualTo(testUserId);
        assertThat(result.getUpdatedBy()).isEqualTo(testUserId);
    }
    
    // ========== Different Asset Types Tests ==========
    
    @Test
    @DisplayName("Should create SERVER asset type")
    void shouldCreateServerAsset() {
        // Given
        validRequest.setAssetType(AssetType.SERVER);
        when(assetRepository.existsBySerialNumber(anyString())).thenReturn(false);
        when(assetRepository.save(any(Asset.class)))
            .thenAnswer(invocation -> {
                Asset asset = invocation.getArgument(0);
                asset.setId(UUID.randomUUID());
                return asset;
            });
        
        // When
        AssetDTO result = assetService.createAsset(testUserId, validRequest);
        
        // Then
        assertThat(result.getAssetType()).isEqualTo(AssetType.SERVER);
    }
    
    @Test
    @DisplayName("Should create LAPTOP asset type")
    void shouldCreateLaptopAsset() {
        // Given
        validRequest.setAssetType(AssetType.LAPTOP);
        validRequest.setSerialNumber("LAP-001");
        when(assetRepository.existsBySerialNumber(anyString())).thenReturn(false);
        when(assetRepository.save(any(Asset.class)))
            .thenAnswer(invocation -> {
                Asset asset = invocation.getArgument(0);
                asset.setId(UUID.randomUUID());
                return asset;
            });
        
        // When
        AssetDTO result = assetService.createAsset(testUserId, validRequest);
        
        // Then
        assertThat(result.getAssetType()).isEqualTo(AssetType.LAPTOP);
    }
    
    @Test
    @DisplayName("Should create NETWORK_DEVICE asset type")
    void shouldCreateNetworkDeviceAsset() {
        // Given
        validRequest.setAssetType(AssetType.NETWORK_DEVICE);
        validRequest.setSerialNumber("NET-001");
        when(assetRepository.existsBySerialNumber(anyString())).thenReturn(false);
        when(assetRepository.save(any(Asset.class)))
            .thenAnswer(invocation -> {
                Asset asset = invocation.getArgument(0);
                asset.setId(UUID.randomUUID());
                return asset;
            });
        
        // When
        AssetDTO result = assetService.createAsset(testUserId, validRequest);
        
        // Then
        assertThat(result.getAssetType()).isEqualTo(AssetType.NETWORK_DEVICE);
    }
    
    // ========== Different Lifecycle Status Tests ==========
    
    @Test
    @DisplayName("Should create asset with ORDERED status")
    void shouldCreateAssetWithOrderedStatus() {
        // Given
        validRequest.setStatus(LifecycleStatus.ORDERED);
        when(assetRepository.existsBySerialNumber(anyString())).thenReturn(false);
        when(assetRepository.save(any(Asset.class)))
            .thenAnswer(invocation -> {
                Asset asset = invocation.getArgument(0);
                asset.setId(UUID.randomUUID());
                return asset;
            });
        
        // When
        AssetDTO result = assetService.createAsset(testUserId, validRequest);
        
        // Then
        assertThat(result.getStatus()).isEqualTo(LifecycleStatus.ORDERED);
    }
    
    @Test
    @DisplayName("Should create asset with RECEIVED status")
    void shouldCreateAssetWithReceivedStatus() {
        // Given
        validRequest.setStatus(LifecycleStatus.RECEIVED);
        when(assetRepository.existsBySerialNumber(anyString())).thenReturn(false);
        when(assetRepository.save(any(Asset.class)))
            .thenAnswer(invocation -> {
                Asset asset = invocation.getArgument(0);
                asset.setId(UUID.randomUUID());
                return asset;
            });
        
        // When
        AssetDTO result = assetService.createAsset(testUserId, validRequest);
        
        // Then
        assertThat(result.getStatus()).isEqualTo(LifecycleStatus.RECEIVED);
    }
    
    // ========== Asset Retrieval Tests (getAsset) ==========
    
    @Test
    @DisplayName("Should retrieve asset successfully by ID")
    void shouldRetrieveAssetSuccessfully() {
        // Given
        UUID assetId = UUID.randomUUID();
        Asset asset = createTestAsset(assetId);
        
        when(assetRepository.findById(assetId)).thenReturn(java.util.Optional.of(asset));
        
        // When
        java.util.Optional<AssetDTO> result = assetService.getAsset(assetId);
        
        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(assetId.toString());
        assertThat(result.get().getName()).isEqualTo(asset.getName());
        assertThat(result.get().getSerialNumber()).isEqualTo(asset.getSerialNumber());
        assertThat(result.get().getAssetType()).isEqualTo(asset.getAssetType());
        assertThat(result.get().getStatus()).isEqualTo(asset.getStatus());
        
        verify(assetRepository).findById(assetId);
    }
    
    @Test
    @DisplayName("Should return empty Optional when asset not found")
    void shouldReturnEmptyOptionalWhenAssetNotFound() {
        // Given
        UUID assetId = UUID.randomUUID();
        when(assetRepository.findById(assetId)).thenReturn(java.util.Optional.empty());
        
        // When
        java.util.Optional<AssetDTO> result = assetService.getAsset(assetId);
        
        // Then
        assertThat(result).isEmpty();
        verify(assetRepository).findById(assetId);
    }
    
    @Test
    @DisplayName("Should throw IllegalArgumentException when asset ID is null")
    void shouldThrowExceptionWhenAssetIdIsNull() {
        // When/Then
        assertThatThrownBy(() -> assetService.getAsset(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Asset ID");
        
        verify(assetRepository, never()).findById(any());
    }
    
    @Test
    @DisplayName("Should map all asset fields to DTO correctly")
    void shouldMapAllAssetFieldsToDTO() {
        // Given
        UUID assetId = UUID.randomUUID();
        UUID createdByUserId = UUID.randomUUID();
        UUID updatedByUserId = UUID.randomUUID();
        
        Asset asset = createTestAsset(assetId);
        asset.setLocation("Data Center A");
        asset.setAssignedUser("john.doe");
        asset.setAssignedUserEmail("john.doe@example.com");
        asset.setNotes("Test notes");
        asset.setCustomFields("{\"warranty\": \"3 years\"}");
        asset.setCreatedBy(createdByUserId);
        asset.setUpdatedBy(updatedByUserId);
        asset.setReadOnly(false);
        
        when(assetRepository.findById(assetId)).thenReturn(java.util.Optional.of(asset));
        
        // When
        java.util.Optional<AssetDTO> result = assetService.getAsset(assetId);
        
        // Then
        assertThat(result).isPresent();
        AssetDTO dto = result.get();
        assertThat(dto.getId()).isEqualTo(assetId.toString());
        assertThat(dto.getAssetType()).isEqualTo(asset.getAssetType());
        assertThat(dto.getName()).isEqualTo(asset.getName());
        assertThat(dto.getSerialNumber()).isEqualTo(asset.getSerialNumber());
        assertThat(dto.getAcquisitionDate()).isEqualTo(asset.getAcquisitionDate());
        assertThat(dto.getStatus()).isEqualTo(asset.getStatus());
        assertThat(dto.getLocation()).isEqualTo(asset.getLocation());
        assertThat(dto.getAssignedUser()).isEqualTo(asset.getAssignedUser());
        assertThat(dto.getAssignedUserEmail()).isEqualTo(asset.getAssignedUserEmail());
        assertThat(dto.getNotes()).isEqualTo(asset.getNotes());
        assertThat(dto.getCustomFields()).isEqualTo(asset.getCustomFields());
        assertThat(dto.getCreatedBy()).isEqualTo(createdByUserId.toString());
        assertThat(dto.getUpdatedBy()).isEqualTo(updatedByUserId.toString());
        assertThat(dto.isReadOnly()).isEqualTo(asset.isReadOnly());
    }
    
    @Test
    @DisplayName("Should retrieve asset with null optional fields")
    void shouldRetrieveAssetWithNullOptionalFields() {
        // Given
        UUID assetId = UUID.randomUUID();
        Asset asset = createTestAsset(assetId);
        asset.setLocation(null);
        asset.setAssignedUser(null);
        asset.setAssignedUserEmail(null);
        asset.setNotes(null);
        asset.setCustomFields(null);
        
        when(assetRepository.findById(assetId)).thenReturn(java.util.Optional.of(asset));
        
        // When
        java.util.Optional<AssetDTO> result = assetService.getAsset(assetId);
        
        // Then
        assertThat(result).isPresent();
        AssetDTO dto = result.get();
        assertThat(dto.getLocation()).isNull();
        assertThat(dto.getAssignedUser()).isNull();
        assertThat(dto.getAssignedUserEmail()).isNull();
        assertThat(dto.getNotes()).isNull();
        assertThat(dto.getCustomFields()).isNull();
    }
    
    @Test
    @DisplayName("Should retrieve asset with readOnly flag set to true")
    void shouldRetrieveAssetWithReadOnlyTrue() {
        // Given
        UUID assetId = UUID.randomUUID();
        Asset asset = createTestAsset(assetId);
        asset.setStatus(LifecycleStatus.RETIRED);
        asset.setReadOnly(true);
        
        when(assetRepository.findById(assetId)).thenReturn(java.util.Optional.of(asset));
        
        // When
        java.util.Optional<AssetDTO> result = assetService.getAsset(assetId);
        
        // Then
        assertThat(result).isPresent();
        assertThat(result.get().isReadOnly()).isTrue();
        assertThat(result.get().getStatus()).isEqualTo(LifecycleStatus.RETIRED);
    }
    
    @Test
    @DisplayName("Should retrieve different asset types correctly")
    void shouldRetrieveDifferentAssetTypes() {
        // Test SERVER
        UUID serverId = UUID.randomUUID();
        Asset serverAsset = createTestAsset(serverId);
        serverAsset.setAssetType(AssetType.SERVER);
        when(assetRepository.findById(serverId)).thenReturn(java.util.Optional.of(serverAsset));
        
        java.util.Optional<AssetDTO> serverResult = assetService.getAsset(serverId);
        assertThat(serverResult).isPresent();
        assertThat(serverResult.get().getAssetType()).isEqualTo(AssetType.SERVER);
        
        // Test LAPTOP
        UUID laptopId = UUID.randomUUID();
        Asset laptopAsset = createTestAsset(laptopId);
        laptopAsset.setAssetType(AssetType.LAPTOP);
        when(assetRepository.findById(laptopId)).thenReturn(java.util.Optional.of(laptopAsset));
        
        java.util.Optional<AssetDTO> laptopResult = assetService.getAsset(laptopId);
        assertThat(laptopResult).isPresent();
        assertThat(laptopResult.get().getAssetType()).isEqualTo(AssetType.LAPTOP);
    }
    
    @Test
    @DisplayName("Should retrieve assets with different lifecycle statuses")
    void shouldRetrieveAssetsWithDifferentStatuses() {
        // Test ORDERED status
        UUID orderedId = UUID.randomUUID();
        Asset orderedAsset = createTestAsset(orderedId);
        orderedAsset.setStatus(LifecycleStatus.ORDERED);
        when(assetRepository.findById(orderedId)).thenReturn(java.util.Optional.of(orderedAsset));
        
        java.util.Optional<AssetDTO> orderedResult = assetService.getAsset(orderedId);
        assertThat(orderedResult).isPresent();
        assertThat(orderedResult.get().getStatus()).isEqualTo(LifecycleStatus.ORDERED);
        
        // Test IN_USE status
        UUID inUseId = UUID.randomUUID();
        Asset inUseAsset = createTestAsset(inUseId);
        inUseAsset.setStatus(LifecycleStatus.IN_USE);
        when(assetRepository.findById(inUseId)).thenReturn(java.util.Optional.of(inUseAsset));
        
        java.util.Optional<AssetDTO> inUseResult = assetService.getAsset(inUseId);
        assertThat(inUseResult).isPresent();
        assertThat(inUseResult.get().getStatus()).isEqualTo(LifecycleStatus.IN_USE);
    }
    
    // ========== Asset Update Tests ==========
    
    @Test
    @DisplayName("Should update asset successfully with valid data")
    void shouldUpdateAssetSuccessfully() {
        // Given
        UUID assetId = UUID.randomUUID();
        Asset existingAsset = createTestAsset(assetId);
        existingAsset.setName("Old Name");
        existingAsset.setLocation("Old Location");
        
        AssetRequest updateRequest = AssetRequest.builder()
            .assetType(AssetType.SERVER)
            .name("Updated Name")
            .serialNumber("SRV-TEST-001")
            .acquisitionDate(LocalDate.now())
            .status(LifecycleStatus.ORDERED)
            .location("Updated Location")
            .build();
        
        when(assetRepository.findById(assetId)).thenReturn(java.util.Optional.of(existingAsset));
        when(assetRepository.save(any(Asset.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        // When
        AssetDTO result = assetService.updateAsset(testUserId, assetId, updateRequest);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Updated Name");
        assertThat(result.getLocation()).isEqualTo("Updated Location");
        
        verify(validationService).validateAssetRequest(updateRequest);
        verify(assetRepository).findById(assetId);
        verify(assetRepository).save(any(Asset.class));
        verify(auditService).logEvent(any(AuditEventDTO.class));
    }
    
    @Test
    @DisplayName("Should throw ResourceNotFoundException when asset not found")
    void shouldThrowResourceNotFoundExceptionWhenAssetNotFound() {
        // Given
        UUID assetId = UUID.randomUUID();
        AssetRequest updateRequest = createValidAssetRequest();
        
        when(assetRepository.findById(assetId)).thenReturn(java.util.Optional.empty());
        
        // When/Then
        assertThatThrownBy(() -> assetService.updateAsset(testUserId, assetId, updateRequest))
            .isInstanceOf(com.company.assetmanagement.exception.ResourceNotFoundException.class)
            .hasMessageContaining(assetId.toString());
        
        verify(assetRepository).findById(assetId);
        verify(assetRepository, never()).save(any());
        verify(auditService, never()).logEvent(any());
    }
    
    @Test
    @DisplayName("Should throw IllegalStateException when updating retired asset with non-notes fields")
    void shouldThrowExceptionWhenUpdatingRetiredAsset() {
        // Given
        UUID assetId = UUID.randomUUID();
        Asset retiredAsset = createTestAsset(assetId);
        retiredAsset.setStatus(LifecycleStatus.RETIRED);
        retiredAsset.setReadOnly(true);
        
        AssetRequest updateRequest = AssetRequest.builder()
            .assetType(AssetType.SERVER)
            .name("Updated Name")  // Trying to update non-notes field
            .serialNumber("SRV-TEST-001")
            .acquisitionDate(LocalDate.now())
            .status(LifecycleStatus.RETIRED)
            .build();
        
        when(assetRepository.findById(assetId)).thenReturn(java.util.Optional.of(retiredAsset));
        
        // When/Then
        assertThatThrownBy(() -> assetService.updateAsset(testUserId, assetId, updateRequest))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("read-only");
        
        verify(assetRepository).findById(assetId);
        verify(assetRepository, never()).save(any());
    }
    
    @Test
    @DisplayName("Should allow notes update for retired asset")
    void shouldAllowNotesUpdateForRetiredAsset() {
        // Given
        UUID assetId = UUID.randomUUID();
        Asset retiredAsset = createTestAsset(assetId);
        retiredAsset.setStatus(LifecycleStatus.RETIRED);
        retiredAsset.setReadOnly(true);
        retiredAsset.setNotes("Old notes");
        
        AssetRequest updateRequest = AssetRequest.builder()
            .assetType(retiredAsset.getAssetType())
            .name(retiredAsset.getName())
            .serialNumber(retiredAsset.getSerialNumber())
            .acquisitionDate(retiredAsset.getAcquisitionDate())
            .status(retiredAsset.getStatus())
            .notes("Updated notes")  // Only updating notes
            .build();
        
        when(assetRepository.findById(assetId)).thenReturn(java.util.Optional.of(retiredAsset));
        when(assetRepository.save(any(Asset.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        // When
        AssetDTO result = assetService.updateAsset(testUserId, assetId, updateRequest);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.getNotes()).isEqualTo("Updated notes");
        
        verify(assetRepository).save(any(Asset.class));
    }
    
    @Test
    @DisplayName("Should not update immutable fields")
    void shouldNotUpdateImmutableFields() {
        // Given
        UUID assetId = UUID.randomUUID();
        UUID originalCreatedBy = UUID.randomUUID();
        Asset existingAsset = createTestAsset(assetId);
        existingAsset.setCreatedBy(originalCreatedBy);
        existingAsset.setSerialNumber("ORIGINAL-SERIAL");
        
        AssetRequest updateRequest = AssetRequest.builder()
            .assetType(AssetType.SERVER)
            .name("Updated Name")
            .serialNumber("NEW-SERIAL")  // Attempting to change immutable field
            .acquisitionDate(LocalDate.now())
            .status(LifecycleStatus.ORDERED)
            .build();
        
        when(assetRepository.findById(assetId)).thenReturn(java.util.Optional.of(existingAsset));
        when(assetRepository.save(any(Asset.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        ArgumentCaptor<Asset> assetCaptor = ArgumentCaptor.forClass(Asset.class);
        
        // When
        assetService.updateAsset(testUserId, assetId, updateRequest);
        
        // Then
        verify(assetRepository).save(assetCaptor.capture());
        Asset savedAsset = assetCaptor.getValue();
        
        // Immutable fields should not change
        assertThat(savedAsset.getSerialNumber()).isEqualTo("ORIGINAL-SERIAL");
        assertThat(savedAsset.getCreatedBy()).isEqualTo(originalCreatedBy);
        assertThat(savedAsset.getId()).isEqualTo(assetId);
    }
    
    @Test
    @DisplayName("Should track field changes for audit log")
    void shouldTrackFieldChangesForAuditLog() {
        // Given
        UUID assetId = UUID.randomUUID();
        Asset existingAsset = createTestAsset(assetId);
        existingAsset.setName("Old Name");
        existingAsset.setLocation("Old Location");
        existingAsset.setStatus(LifecycleStatus.ORDERED);
        
        AssetRequest updateRequest = AssetRequest.builder()
            .assetType(AssetType.SERVER)
            .name("New Name")
            .serialNumber("SRV-TEST-001")
            .acquisitionDate(LocalDate.now())
            .status(LifecycleStatus.RECEIVED)
            .location("New Location")
            .build();
        
        when(assetRepository.findById(assetId)).thenReturn(java.util.Optional.of(existingAsset));
        when(assetRepository.save(any(Asset.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        ArgumentCaptor<AuditEventDTO> eventCaptor = ArgumentCaptor.forClass(AuditEventDTO.class);
        
        // When
        assetService.updateAsset(testUserId, assetId, updateRequest);
        
        // Then
        verify(auditService).logEvent(eventCaptor.capture());
        AuditEventDTO capturedEvent = eventCaptor.getValue();
        
        assertThat(capturedEvent.getActionType()).isEqualTo(Action.UPDATE_ASSET);
        assertThat(capturedEvent.getResourceId()).isEqualTo(assetId.toString());
        assertThat(capturedEvent.getChanges()).isNotNull();
        assertThat(capturedEvent.getChanges()).containsKeys("name", "location", "status");
    }
    
    @Test
    @DisplayName("Should set updatedBy field to current user")
    void shouldSetUpdatedByFieldToCurrentUser() {
        // Given
        UUID assetId = UUID.randomUUID();
        Asset existingAsset = createTestAsset(assetId);
        AssetRequest updateRequest = createValidAssetRequest();
        updateRequest.setName("Updated Name");
        
        when(assetRepository.findById(assetId)).thenReturn(java.util.Optional.of(existingAsset));
        when(assetRepository.save(any(Asset.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        ArgumentCaptor<Asset> assetCaptor = ArgumentCaptor.forClass(Asset.class);
        
        // When
        assetService.updateAsset(testUserId, assetId, updateRequest);
        
        // Then
        verify(assetRepository).save(assetCaptor.capture());
        Asset savedAsset = assetCaptor.getValue();
        assertThat(savedAsset.getUpdatedBy()).isEqualTo(UUID.fromString(testUserId));
    }
    
    @Test
    @DisplayName("Should throw IllegalArgumentException when userId is null for update")
    void shouldThrowExceptionWhenUserIdIsNullForUpdate() {
        // Given
        UUID assetId = UUID.randomUUID();
        AssetRequest updateRequest = createValidAssetRequest();
        
        // When/Then
        assertThatThrownBy(() -> assetService.updateAsset(null, assetId, updateRequest))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("User ID");
        
        verify(assetRepository, never()).findById(any());
        verify(assetRepository, never()).save(any());
    }
    
    @Test
    @DisplayName("Should throw IllegalArgumentException when assetId is null for update")
    void shouldThrowExceptionWhenAssetIdIsNullForUpdate() {
        // Given
        AssetRequest updateRequest = createValidAssetRequest();
        
        // When/Then
        assertThatThrownBy(() -> assetService.updateAsset(testUserId, null, updateRequest))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Asset ID");
        
        verify(assetRepository, never()).findById(any());
        verify(assetRepository, never()).save(any());
    }
    
    @Test
    @DisplayName("Should throw IllegalArgumentException when request is null for update")
    void shouldThrowExceptionWhenRequestIsNullForUpdate() {
        // Given
        UUID assetId = UUID.randomUUID();
        
        // When/Then
        assertThatThrownBy(() -> assetService.updateAsset(testUserId, assetId, null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("request");
        
        verify(assetRepository, never()).findById(any());
        verify(assetRepository, never()).save(any());
    }
    
    @Test
    @DisplayName("Should call validation service before updating asset")
    void shouldCallValidationServiceBeforeUpdate() {
        // Given
        UUID assetId = UUID.randomUUID();
        Asset existingAsset = createTestAsset(assetId);
        AssetRequest updateRequest = createValidAssetRequest();
        
        when(assetRepository.findById(assetId)).thenReturn(java.util.Optional.of(existingAsset));
        when(assetRepository.save(any(Asset.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        // When
        assetService.updateAsset(testUserId, assetId, updateRequest);
        
        // Then
        verify(validationService).validateAssetRequest(updateRequest);
    }
    
    @Test
    @DisplayName("Should throw ValidationException when update validation fails")
    void shouldThrowValidationExceptionWhenUpdateValidationFails() {
        // Given
        UUID assetId = UUID.randomUUID();
        Asset existingAsset = createTestAsset(assetId);
        AssetRequest updateRequest = createValidAssetRequest();
        
        when(assetRepository.findById(assetId)).thenReturn(java.util.Optional.of(existingAsset));
        doThrow(new ValidationException("Validation failed"))
            .when(validationService).validateAssetRequest(any());
        
        // When/Then
        assertThatThrownBy(() -> assetService.updateAsset(testUserId, assetId, updateRequest))
            .isInstanceOf(ValidationException.class)
            .hasMessageContaining("Validation failed");
        
        verify(assetRepository).findById(assetId);
        verify(validationService).validateAssetRequest(updateRequest);
        verify(assetRepository, never()).save(any());
    }
    
    @Test
    @DisplayName("Should update location and set locationUpdateDate")
    void shouldUpdateLocationAndSetLocationUpdateDate() {
        // Given
        UUID assetId = UUID.randomUUID();
        Asset existingAsset = createTestAsset(assetId);
        existingAsset.setLocation("Old Location");
        
        AssetRequest updateRequest = createValidAssetRequest();
        updateRequest.setLocation("New Location");
        
        when(assetRepository.findById(assetId)).thenReturn(java.util.Optional.of(existingAsset));
        when(assetRepository.save(any(Asset.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        ArgumentCaptor<Asset> assetCaptor = ArgumentCaptor.forClass(Asset.class);
        
        // When
        assetService.updateAsset(testUserId, assetId, updateRequest);
        
        // Then
        verify(assetRepository).save(assetCaptor.capture());
        Asset savedAsset = assetCaptor.getValue();
        assertThat(savedAsset.getLocation()).isEqualTo("New Location");
        assertThat(savedAsset.getLocationUpdateDate()).isNotNull();
    }
    
    @Test
    @DisplayName("Should update assignedUser and set assignmentDate")
    void shouldUpdateAssignedUserAndSetAssignmentDate() {
        // Given
        UUID assetId = UUID.randomUUID();
        Asset existingAsset = createTestAsset(assetId);
        existingAsset.setAssignedUser("old.user");
        
        AssetRequest updateRequest = createValidAssetRequest();
        updateRequest.setAssignedUser("new.user");
        updateRequest.setAssignedUserEmail("new.user@example.com");
        
        when(assetRepository.findById(assetId)).thenReturn(java.util.Optional.of(existingAsset));
        when(assetRepository.save(any(Asset.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        ArgumentCaptor<Asset> assetCaptor = ArgumentCaptor.forClass(Asset.class);
        
        // When
        assetService.updateAsset(testUserId, assetId, updateRequest);
        
        // Then
        verify(assetRepository).save(assetCaptor.capture());
        Asset savedAsset = assetCaptor.getValue();
        assertThat(savedAsset.getAssignedUser()).isEqualTo("new.user");
        assertThat(savedAsset.getAssignmentDate()).isNotNull();
    }
    
    @Test
    @DisplayName("Should not fail update when audit logging fails")
    void shouldNotFailUpdateWhenAuditLoggingFails() {
        // Given
        UUID assetId = UUID.randomUUID();
        Asset existingAsset = createTestAsset(assetId);
        AssetRequest updateRequest = createValidAssetRequest();
        updateRequest.setName("Updated Name");
        
        when(assetRepository.findById(assetId)).thenReturn(java.util.Optional.of(existingAsset));
        when(assetRepository.save(any(Asset.class))).thenAnswer(invocation -> invocation.getArgument(0));
        doThrow(new RuntimeException("Audit service unavailable"))
            .when(auditService).logEvent(any());
        
        // When/Then - should not throw exception
        assertThatCode(() -> assetService.updateAsset(testUserId, assetId, updateRequest))
            .doesNotThrowAnyException();
        
        verify(assetRepository).save(any(Asset.class));
    }
    
    // ========== Status Management Tests (updateStatus) ==========
    
    @Test
    @DisplayName("Should update status successfully with valid transition")
    void shouldUpdateStatusSuccessfully() {
        // Given
        UUID assetId = UUID.randomUUID();
        Asset existingAsset = createTestAsset(assetId);
        existingAsset.setStatus(LifecycleStatus.ORDERED);
        
        when(assetRepository.findById(assetId)).thenReturn(java.util.Optional.of(existingAsset));
        when(assetRepository.save(any(Asset.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        // When
        AssetDTO result = assetService.updateStatus(testUserId, assetId, LifecycleStatus.RECEIVED);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(LifecycleStatus.RECEIVED);
        
        verify(assetRepository).findById(assetId);
        verify(assetRepository).save(any(Asset.class));
        verify(auditService).logEvent(any(AuditEventDTO.class));
    }
    
    @Test
    @DisplayName("Should throw InvalidStatusTransitionException for invalid transition")
    void shouldThrowInvalidStatusTransitionException() {
        // Given
        UUID assetId = UUID.randomUUID();
        Asset existingAsset = createTestAsset(assetId);
        existingAsset.setStatus(LifecycleStatus.ORDERED);
        
        when(assetRepository.findById(assetId)).thenReturn(java.util.Optional.of(existingAsset));
        
        // When/Then - ORDERED cannot transition to IN_USE directly
        assertThatThrownBy(() -> assetService.updateStatus(testUserId, assetId, LifecycleStatus.IN_USE))
            .isInstanceOf(com.company.assetmanagement.exception.InvalidStatusTransitionException.class)
            .hasMessageContaining("ORDERED")
            .hasMessageContaining("IN_USE");
        
        verify(assetRepository).findById(assetId);
        verify(assetRepository, never()).save(any());
        verify(auditService, never()).logEvent(any());
    }
    
    @Test
    @DisplayName("Should set readOnly to true when status becomes RETIRED")
    void shouldSetReadOnlyWhenStatusBecomesRetired() {
        // Given
        UUID assetId = UUID.randomUUID();
        Asset existingAsset = createTestAsset(assetId);
        existingAsset.setStatus(LifecycleStatus.IN_USE);
        existingAsset.setReadOnly(false);
        
        when(assetRepository.findById(assetId)).thenReturn(java.util.Optional.of(existingAsset));
        
        ArgumentCaptor<Asset> assetCaptor = ArgumentCaptor.forClass(Asset.class);
        when(assetRepository.save(assetCaptor.capture())).thenAnswer(invocation -> invocation.getArgument(0));
        
        // When
        AssetDTO result = assetService.updateStatus(testUserId, assetId, LifecycleStatus.RETIRED);
        
        // Then
        assertThat(result.isReadOnly()).isTrue();
        Asset savedAsset = assetCaptor.getValue();
        assertThat(savedAsset.isReadOnly()).isTrue();
    }
    
    @Test
    @DisplayName("Should not allow transitions from RETIRED status")
    void shouldNotAllowTransitionsFromRetired() {
        // Given
        UUID assetId = UUID.randomUUID();
        Asset existingAsset = createTestAsset(assetId);
        existingAsset.setStatus(LifecycleStatus.RETIRED);
        existingAsset.setReadOnly(true);
        
        when(assetRepository.findById(assetId)).thenReturn(java.util.Optional.of(existingAsset));
        
        // When/Then - RETIRED cannot transition to any status
        assertThatThrownBy(() -> assetService.updateStatus(testUserId, assetId, LifecycleStatus.STORAGE))
            .isInstanceOf(com.company.assetmanagement.exception.InvalidStatusTransitionException.class)
            .hasMessageContaining("RETIRED");
        
        verify(assetRepository, never()).save(any());
    }
    
    @Test
    @DisplayName("Should allow transition from ORDERED to RECEIVED")
    void shouldAllowOrderedToReceived() {
        // Given
        UUID assetId = UUID.randomUUID();
        Asset existingAsset = createTestAsset(assetId);
        existingAsset.setStatus(LifecycleStatus.ORDERED);
        
        when(assetRepository.findById(assetId)).thenReturn(java.util.Optional.of(existingAsset));
        when(assetRepository.save(any(Asset.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        // When
        AssetDTO result = assetService.updateStatus(testUserId, assetId, LifecycleStatus.RECEIVED);
        
        // Then
        assertThat(result.getStatus()).isEqualTo(LifecycleStatus.RECEIVED);
        verify(assetRepository).save(any(Asset.class));
    }
    
    @Test
    @DisplayName("Should allow transition from RECEIVED to DEPLOYED")
    void shouldAllowReceivedToDeployed() {
        // Given
        UUID assetId = UUID.randomUUID();
        Asset existingAsset = createTestAsset(assetId);
        existingAsset.setStatus(LifecycleStatus.RECEIVED);
        
        when(assetRepository.findById(assetId)).thenReturn(java.util.Optional.of(existingAsset));
        when(assetRepository.save(any(Asset.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        // When
        AssetDTO result = assetService.updateStatus(testUserId, assetId, LifecycleStatus.DEPLOYED);
        
        // Then
        assertThat(result.getStatus()).isEqualTo(LifecycleStatus.DEPLOYED);
        verify(assetRepository).save(any(Asset.class));
    }
    
    @Test
    @DisplayName("Should allow transition from DEPLOYED to IN_USE")
    void shouldAllowDeployedToInUse() {
        // Given
        UUID assetId = UUID.randomUUID();
        Asset existingAsset = createTestAsset(assetId);
        existingAsset.setStatus(LifecycleStatus.DEPLOYED);
        
        when(assetRepository.findById(assetId)).thenReturn(java.util.Optional.of(existingAsset));
        when(assetRepository.save(any(Asset.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        // When
        AssetDTO result = assetService.updateStatus(testUserId, assetId, LifecycleStatus.IN_USE);
        
        // Then
        assertThat(result.getStatus()).isEqualTo(LifecycleStatus.IN_USE);
        verify(assetRepository).save(any(Asset.class));
    }
    
    @Test
    @DisplayName("Should allow transition from DEPLOYED to STORAGE")
    void shouldAllowDeployedToStorage() {
        // Given
        UUID assetId = UUID.randomUUID();
        Asset existingAsset = createTestAsset(assetId);
        existingAsset.setStatus(LifecycleStatus.DEPLOYED);
        
        when(assetRepository.findById(assetId)).thenReturn(java.util.Optional.of(existingAsset));
        when(assetRepository.save(any(Asset.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        // When
        AssetDTO result = assetService.updateStatus(testUserId, assetId, LifecycleStatus.STORAGE);
        
        // Then
        assertThat(result.getStatus()).isEqualTo(LifecycleStatus.STORAGE);
        verify(assetRepository).save(any(Asset.class));
    }
    
    @Test
    @DisplayName("Should allow transition from IN_USE to STORAGE")
    void shouldAllowInUseToStorage() {
        // Given
        UUID assetId = UUID.randomUUID();
        Asset existingAsset = createTestAsset(assetId);
        existingAsset.setStatus(LifecycleStatus.IN_USE);
        
        when(assetRepository.findById(assetId)).thenReturn(java.util.Optional.of(existingAsset));
        when(assetRepository.save(any(Asset.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        // When
        AssetDTO result = assetService.updateStatus(testUserId, assetId, LifecycleStatus.STORAGE);
        
        // Then
        assertThat(result.getStatus()).isEqualTo(LifecycleStatus.STORAGE);
        verify(assetRepository).save(any(Asset.class));
    }
    
    @Test
    @DisplayName("Should allow transition from IN_USE to RETIRED")
    void shouldAllowInUseToRetired() {
        // Given
        UUID assetId = UUID.randomUUID();
        Asset existingAsset = createTestAsset(assetId);
        existingAsset.setStatus(LifecycleStatus.IN_USE);
        
        when(assetRepository.findById(assetId)).thenReturn(java.util.Optional.of(existingAsset));
        when(assetRepository.save(any(Asset.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        // When
        AssetDTO result = assetService.updateStatus(testUserId, assetId, LifecycleStatus.RETIRED);
        
        // Then
        assertThat(result.getStatus()).isEqualTo(LifecycleStatus.RETIRED);
        assertThat(result.isReadOnly()).isTrue();
        verify(assetRepository).save(any(Asset.class));
    }
    
    @Test
    @DisplayName("Should allow transition from STORAGE to DEPLOYED")
    void shouldAllowStorageToDeployed() {
        // Given
        UUID assetId = UUID.randomUUID();
        Asset existingAsset = createTestAsset(assetId);
        existingAsset.setStatus(LifecycleStatus.STORAGE);
        
        when(assetRepository.findById(assetId)).thenReturn(java.util.Optional.of(existingAsset));
        when(assetRepository.save(any(Asset.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        // When
        AssetDTO result = assetService.updateStatus(testUserId, assetId, LifecycleStatus.DEPLOYED);
        
        // Then
        assertThat(result.getStatus()).isEqualTo(LifecycleStatus.DEPLOYED);
        verify(assetRepository).save(any(Asset.class));
    }
    
    @Test
    @DisplayName("Should allow transition from STORAGE to RETIRED")
    void shouldAllowStorageToRetired() {
        // Given
        UUID assetId = UUID.randomUUID();
        Asset existingAsset = createTestAsset(assetId);
        existingAsset.setStatus(LifecycleStatus.STORAGE);
        
        when(assetRepository.findById(assetId)).thenReturn(java.util.Optional.of(existingAsset));
        when(assetRepository.save(any(Asset.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        // When
        AssetDTO result = assetService.updateStatus(testUserId, assetId, LifecycleStatus.RETIRED);
        
        // Then
        assertThat(result.getStatus()).isEqualTo(LifecycleStatus.RETIRED);
        assertThat(result.isReadOnly()).isTrue();
        verify(assetRepository).save(any(Asset.class));
    }
    
    @Test
    @DisplayName("Should allow transition from any status to MAINTENANCE")
    void shouldAllowAnyStatusToMaintenance() {
        // Test from ORDERED
        UUID orderedId = UUID.randomUUID();
        Asset orderedAsset = createTestAsset(orderedId);
        orderedAsset.setStatus(LifecycleStatus.ORDERED);
        when(assetRepository.findById(orderedId)).thenReturn(java.util.Optional.of(orderedAsset));
        when(assetRepository.save(any(Asset.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        AssetDTO result1 = assetService.updateStatus(testUserId, orderedId, LifecycleStatus.MAINTENANCE);
        assertThat(result1.getStatus()).isEqualTo(LifecycleStatus.MAINTENANCE);
        
        // Test from IN_USE
        UUID inUseId = UUID.randomUUID();
        Asset inUseAsset = createTestAsset(inUseId);
        inUseAsset.setStatus(LifecycleStatus.IN_USE);
        when(assetRepository.findById(inUseId)).thenReturn(java.util.Optional.of(inUseAsset));
        
        AssetDTO result2 = assetService.updateStatus(testUserId, inUseId, LifecycleStatus.MAINTENANCE);
        assertThat(result2.getStatus()).isEqualTo(LifecycleStatus.MAINTENANCE);
    }
    
    @Test
    @DisplayName("Should allow transition from MAINTENANCE to any status except RETIRED")
    void shouldAllowMaintenanceToAnyStatusExceptRetired() {
        // Test MAINTENANCE to DEPLOYED
        UUID maintenanceId = UUID.randomUUID();
        Asset maintenanceAsset = createTestAsset(maintenanceId);
        maintenanceAsset.setStatus(LifecycleStatus.MAINTENANCE);
        when(assetRepository.findById(maintenanceId)).thenReturn(java.util.Optional.of(maintenanceAsset));
        when(assetRepository.save(any(Asset.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        AssetDTO result = assetService.updateStatus(testUserId, maintenanceId, LifecycleStatus.DEPLOYED);
        assertThat(result.getStatus()).isEqualTo(LifecycleStatus.DEPLOYED);
    }
    
    @Test
    @DisplayName("Should not allow transition from MAINTENANCE to RETIRED")
    void shouldNotAllowMaintenanceToRetired() {
        // Given
        UUID assetId = UUID.randomUUID();
        Asset existingAsset = createTestAsset(assetId);
        existingAsset.setStatus(LifecycleStatus.MAINTENANCE);
        
        when(assetRepository.findById(assetId)).thenReturn(java.util.Optional.of(existingAsset));
        
        // When/Then
        assertThatThrownBy(() -> assetService.updateStatus(testUserId, assetId, LifecycleStatus.RETIRED))
            .isInstanceOf(com.company.assetmanagement.exception.InvalidStatusTransitionException.class)
            .hasMessageContaining("MAINTENANCE")
            .hasMessageContaining("RETIRED");
        
        verify(assetRepository, never()).save(any());
    }
    
    @Test
    @DisplayName("Should log audit event with status change details")
    void shouldLogAuditEventWithStatusChange() {
        // Given
        UUID assetId = UUID.randomUUID();
        Asset existingAsset = createTestAsset(assetId);
        existingAsset.setStatus(LifecycleStatus.ORDERED);
        
        when(assetRepository.findById(assetId)).thenReturn(java.util.Optional.of(existingAsset));
        when(assetRepository.save(any(Asset.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        ArgumentCaptor<AuditEventDTO> eventCaptor = ArgumentCaptor.forClass(AuditEventDTO.class);
        
        // When
        assetService.updateStatus(testUserId, assetId, LifecycleStatus.RECEIVED);
        
        // Then
        verify(auditService).logEvent(eventCaptor.capture());
        AuditEventDTO capturedEvent = eventCaptor.getValue();
        
        assertThat(capturedEvent.getUserId()).isEqualTo(UUID.fromString(testUserId));
        assertThat(capturedEvent.getActionType()).isEqualTo(Action.UPDATE_ASSET);
        assertThat(capturedEvent.getResourceType()).isEqualTo("ASSET");
        assertThat(capturedEvent.getResourceId()).isEqualTo(assetId.toString());
        assertThat(capturedEvent.getChanges()).isNotNull();
        assertThat(capturedEvent.getChanges()).containsKey("status");
    }
    
    @Test
    @DisplayName("Should set updatedBy field when updating status")
    void shouldSetUpdatedByWhenUpdatingStatus() {
        // Given
        UUID assetId = UUID.randomUUID();
        Asset existingAsset = createTestAsset(assetId);
        existingAsset.setStatus(LifecycleStatus.ORDERED);
        
        when(assetRepository.findById(assetId)).thenReturn(java.util.Optional.of(existingAsset));
        
        ArgumentCaptor<Asset> assetCaptor = ArgumentCaptor.forClass(Asset.class);
        when(assetRepository.save(assetCaptor.capture())).thenAnswer(invocation -> invocation.getArgument(0));
        
        // When
        assetService.updateStatus(testUserId, assetId, LifecycleStatus.RECEIVED);
        
        // Then
        Asset savedAsset = assetCaptor.getValue();
        assertThat(savedAsset.getUpdatedBy()).isEqualTo(UUID.fromString(testUserId));
    }
    
    @Test
    @DisplayName("Should throw ResourceNotFoundException when asset not found for status update")
    void shouldThrowResourceNotFoundExceptionForStatusUpdate() {
        // Given
        UUID assetId = UUID.randomUUID();
        when(assetRepository.findById(assetId)).thenReturn(java.util.Optional.empty());
        
        // When/Then
        assertThatThrownBy(() -> assetService.updateStatus(testUserId, assetId, LifecycleStatus.RECEIVED))
            .isInstanceOf(com.company.assetmanagement.exception.ResourceNotFoundException.class)
            .hasMessageContaining(assetId.toString());
        
        verify(assetRepository).findById(assetId);
        verify(assetRepository, never()).save(any());
    }
    
    @Test
    @DisplayName("Should throw IllegalArgumentException when userId is null for status update")
    void shouldThrowExceptionWhenUserIdIsNullForStatusUpdate() {
        // Given
        UUID assetId = UUID.randomUUID();
        
        // When/Then
        assertThatThrownBy(() -> assetService.updateStatus(null, assetId, LifecycleStatus.RECEIVED))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("User ID");
        
        verify(assetRepository, never()).findById(any());
    }
    
    @Test
    @DisplayName("Should throw IllegalArgumentException when assetId is null for status update")
    void shouldThrowExceptionWhenAssetIdIsNullForStatusUpdate() {
        // When/Then
        assertThatThrownBy(() -> assetService.updateStatus(testUserId, null, LifecycleStatus.RECEIVED))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Asset ID");
        
        verify(assetRepository, never()).findById(any());
    }
    
    @Test
    @DisplayName("Should throw IllegalArgumentException when newStatus is null")
    void shouldThrowExceptionWhenNewStatusIsNull() {
        // Given
        UUID assetId = UUID.randomUUID();
        
        // When/Then
        assertThatThrownBy(() -> assetService.updateStatus(testUserId, assetId, null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("status");
        
        verify(assetRepository, never()).findById(any());
    }
    
    @Test
    @DisplayName("Should not fail status update when audit logging fails")
    void shouldNotFailStatusUpdateWhenAuditLoggingFails() {
        // Given
        UUID assetId = UUID.randomUUID();
        Asset existingAsset = createTestAsset(assetId);
        existingAsset.setStatus(LifecycleStatus.ORDERED);
        
        when(assetRepository.findById(assetId)).thenReturn(java.util.Optional.of(existingAsset));
        when(assetRepository.save(any(Asset.class))).thenAnswer(invocation -> invocation.getArgument(0));
        doThrow(new RuntimeException("Audit service unavailable"))
            .when(auditService).logEvent(any());
        
        // When/Then - should not throw exception
        assertThatCode(() -> assetService.updateStatus(testUserId, assetId, LifecycleStatus.RECEIVED))
            .doesNotThrowAnyException();
        
        verify(assetRepository).save(any(Asset.class));
    }
    
    @Test
    @DisplayName("Should not set readOnly for non-RETIRED status transitions")
    void shouldNotSetReadOnlyForNonRetiredTransitions() {
        // Given
        UUID assetId = UUID.randomUUID();
        Asset existingAsset = createTestAsset(assetId);
        existingAsset.setStatus(LifecycleStatus.ORDERED);
        existingAsset.setReadOnly(false);
        
        when(assetRepository.findById(assetId)).thenReturn(java.util.Optional.of(existingAsset));
        
        ArgumentCaptor<Asset> assetCaptor = ArgumentCaptor.forClass(Asset.class);
        when(assetRepository.save(assetCaptor.capture())).thenAnswer(invocation -> invocation.getArgument(0));
        
        // When
        AssetDTO result = assetService.updateStatus(testUserId, assetId, LifecycleStatus.RECEIVED);
        
        // Then
        assertThat(result.isReadOnly()).isFalse();
        Asset savedAsset = assetCaptor.getValue();
        assertThat(savedAsset.isReadOnly()).isFalse();
    }
    
    // ========== Asset Search Tests (searchAssets) ==========
    
    @Test
    @DisplayName("Should search assets with text query")
    void shouldSearchAssetsWithTextQuery() {
        // Given
        com.company.assetmanagement.dto.AssetSearchQuery query = 
            new com.company.assetmanagement.dto.AssetSearchQuery.Builder()
                .text("server")
                .build();
        
        org.springframework.data.domain.Pageable pageable = 
            org.springframework.data.domain.PageRequest.of(0, 20);
        
        java.util.List<Asset> assets = java.util.List.of(
            createTestAsset(UUID.randomUUID()),
            createTestAsset(UUID.randomUUID())
        );
        
        org.springframework.data.domain.Page<Asset> assetPage = 
            new org.springframework.data.domain.PageImpl<>(assets, pageable, assets.size());
        
        when(assetRepository.searchAssets(
            eq("server"), 
            isNull(), 
            isNull(), 
            isNull(), 
            isNull(), 
            isNull(), 
            eq(pageable)
        )).thenReturn(assetPage);
        
        // When
        org.springframework.data.domain.Page<AssetDTO> result = 
            assetService.searchAssets(query, pageable);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getTotalElements()).isEqualTo(2);
        
        verify(assetRepository).searchAssets(
            eq("server"), 
            isNull(), 
            isNull(), 
            isNull(), 
            isNull(), 
            isNull(), 
            eq(pageable)
        );
    }
    
    @Test
    @DisplayName("Should search assets with asset type filter")
    void shouldSearchAssetsWithAssetTypeFilter() {
        // Given
        java.util.List<AssetType> assetTypes = java.util.List.of(AssetType.SERVER, AssetType.LAPTOP);
        
        com.company.assetmanagement.dto.AssetSearchQuery query = 
            new com.company.assetmanagement.dto.AssetSearchQuery.Builder()
                .assetTypes(assetTypes)
                .build();
        
        org.springframework.data.domain.Pageable pageable = 
            org.springframework.data.domain.PageRequest.of(0, 20);
        
        java.util.List<Asset> assets = java.util.List.of(createTestAsset(UUID.randomUUID()));
        org.springframework.data.domain.Page<Asset> assetPage = 
            new org.springframework.data.domain.PageImpl<>(assets, pageable, assets.size());
        
        when(assetRepository.searchAssets(
            isNull(), 
            eq(assetTypes), 
            isNull(), 
            isNull(), 
            isNull(), 
            isNull(), 
            eq(pageable)
        )).thenReturn(assetPage);
        
        // When
        org.springframework.data.domain.Page<AssetDTO> result = 
            assetService.searchAssets(query, pageable);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        
        verify(assetRepository).searchAssets(
            isNull(), 
            eq(assetTypes), 
            isNull(), 
            isNull(), 
            isNull(), 
            isNull(), 
            eq(pageable)
        );
    }
    
    @Test
    @DisplayName("Should search assets with status filter")
    void shouldSearchAssetsWithStatusFilter() {
        // Given
        java.util.List<LifecycleStatus> statuses = 
            java.util.List.of(LifecycleStatus.IN_USE, LifecycleStatus.DEPLOYED);
        
        com.company.assetmanagement.dto.AssetSearchQuery query = 
            new com.company.assetmanagement.dto.AssetSearchQuery.Builder()
                .statuses(statuses)
                .build();
        
        org.springframework.data.domain.Pageable pageable = 
            org.springframework.data.domain.PageRequest.of(0, 20);
        
        java.util.List<Asset> assets = java.util.List.of(createTestAsset(UUID.randomUUID()));
        org.springframework.data.domain.Page<Asset> assetPage = 
            new org.springframework.data.domain.PageImpl<>(assets, pageable, assets.size());
        
        when(assetRepository.searchAssets(
            isNull(), 
            isNull(), 
            eq(statuses), 
            isNull(), 
            isNull(), 
            isNull(), 
            eq(pageable)
        )).thenReturn(assetPage);
        
        // When
        org.springframework.data.domain.Page<AssetDTO> result = 
            assetService.searchAssets(query, pageable);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        
        verify(assetRepository).searchAssets(
            isNull(), 
            isNull(), 
            eq(statuses), 
            isNull(), 
            isNull(), 
            isNull(), 
            eq(pageable)
        );
    }
    
    @Test
    @DisplayName("Should search assets with location filter")
    void shouldSearchAssetsWithLocationFilter() {
        // Given
        com.company.assetmanagement.dto.AssetSearchQuery query = 
            new com.company.assetmanagement.dto.AssetSearchQuery.Builder()
                .location("Data Center A")
                .build();
        
        org.springframework.data.domain.Pageable pageable = 
            org.springframework.data.domain.PageRequest.of(0, 20);
        
        java.util.List<Asset> assets = java.util.List.of(createTestAsset(UUID.randomUUID()));
        org.springframework.data.domain.Page<Asset> assetPage = 
            new org.springframework.data.domain.PageImpl<>(assets, pageable, assets.size());
        
        when(assetRepository.searchAssets(
            isNull(), 
            isNull(), 
            isNull(), 
            eq("Data Center A"), 
            isNull(), 
            isNull(), 
            eq(pageable)
        )).thenReturn(assetPage);
        
        // When
        org.springframework.data.domain.Page<AssetDTO> result = 
            assetService.searchAssets(query, pageable);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        
        verify(assetRepository).searchAssets(
            isNull(), 
            isNull(), 
            isNull(), 
            eq("Data Center A"), 
            isNull(), 
            isNull(), 
            eq(pageable)
        );
    }
    
    @Test
    @DisplayName("Should search assets with date range filter")
    void shouldSearchAssetsWithDateRangeFilter() {
        // Given
        java.time.LocalDate dateFrom = java.time.LocalDate.of(2024, 1, 1);
        java.time.LocalDate dateTo = java.time.LocalDate.of(2024, 12, 31);
        
        com.company.assetmanagement.dto.AssetSearchQuery query = 
            new com.company.assetmanagement.dto.AssetSearchQuery.Builder()
                .acquisitionDateFrom(dateFrom)
                .acquisitionDateTo(dateTo)
                .build();
        
        org.springframework.data.domain.Pageable pageable = 
            org.springframework.data.domain.PageRequest.of(0, 20);
        
        java.util.List<Asset> assets = java.util.List.of(createTestAsset(UUID.randomUUID()));
        org.springframework.data.domain.Page<Asset> assetPage = 
            new org.springframework.data.domain.PageImpl<>(assets, pageable, assets.size());
        
        when(assetRepository.searchAssets(
            isNull(), 
            isNull(), 
            isNull(), 
            isNull(), 
            eq(dateFrom), 
            eq(dateTo), 
            eq(pageable)
        )).thenReturn(assetPage);
        
        // When
        org.springframework.data.domain.Page<AssetDTO> result = 
            assetService.searchAssets(query, pageable);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        
        verify(assetRepository).searchAssets(
            isNull(), 
            isNull(), 
            isNull(), 
            isNull(), 
            eq(dateFrom), 
            eq(dateTo), 
            eq(pageable)
        );
    }
    
    @Test
    @DisplayName("Should search assets with multiple filters combined")
    void shouldSearchAssetsWithMultipleFilters() {
        // Given
        java.util.List<AssetType> assetTypes = java.util.List.of(AssetType.SERVER);
        java.util.List<LifecycleStatus> statuses = java.util.List.of(LifecycleStatus.IN_USE);
        java.time.LocalDate dateFrom = java.time.LocalDate.of(2024, 1, 1);
        
        com.company.assetmanagement.dto.AssetSearchQuery query = 
            new com.company.assetmanagement.dto.AssetSearchQuery.Builder()
                .text("server")
                .assetTypes(assetTypes)
                .statuses(statuses)
                .location("Data Center A")
                .acquisitionDateFrom(dateFrom)
                .build();
        
        org.springframework.data.domain.Pageable pageable = 
            org.springframework.data.domain.PageRequest.of(0, 20);
        
        java.util.List<Asset> assets = java.util.List.of(createTestAsset(UUID.randomUUID()));
        org.springframework.data.domain.Page<Asset> assetPage = 
            new org.springframework.data.domain.PageImpl<>(assets, pageable, assets.size());
        
        when(assetRepository.searchAssets(
            eq("server"), 
            eq(assetTypes), 
            eq(statuses), 
            eq("Data Center A"), 
            eq(dateFrom), 
            isNull(), 
            eq(pageable)
        )).thenReturn(assetPage);
        
        // When
        org.springframework.data.domain.Page<AssetDTO> result = 
            assetService.searchAssets(query, pageable);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        
        verify(assetRepository).searchAssets(
            eq("server"), 
            eq(assetTypes), 
            eq(statuses), 
            eq("Data Center A"), 
            eq(dateFrom), 
            isNull(), 
            eq(pageable)
        );
    }
    
    @Test
    @DisplayName("Should search assets with null query (unfiltered)")
    void shouldSearchAssetsWithNullQuery() {
        // Given
        org.springframework.data.domain.Pageable pageable = 
            org.springframework.data.domain.PageRequest.of(0, 20);
        
        java.util.List<Asset> assets = java.util.List.of(
            createTestAsset(UUID.randomUUID()),
            createTestAsset(UUID.randomUUID()),
            createTestAsset(UUID.randomUUID())
        );
        
        org.springframework.data.domain.Page<Asset> assetPage = 
            new org.springframework.data.domain.PageImpl<>(assets, pageable, assets.size());
        
        when(assetRepository.searchAssets(
            isNull(), 
            isNull(), 
            isNull(), 
            isNull(), 
            isNull(), 
            isNull(), 
            eq(pageable)
        )).thenReturn(assetPage);
        
        // When
        org.springframework.data.domain.Page<AssetDTO> result = 
            assetService.searchAssets(null, pageable);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(3);
        assertThat(result.getTotalElements()).isEqualTo(3);
        
        verify(assetRepository).searchAssets(
            isNull(), 
            isNull(), 
            isNull(), 
            isNull(), 
            isNull(), 
            isNull(), 
            eq(pageable)
        );
    }
    
    @Test
    @DisplayName("Should return empty page when no assets match search criteria")
    void shouldReturnEmptyPageWhenNoAssetsMatch() {
        // Given
        com.company.assetmanagement.dto.AssetSearchQuery query = 
            new com.company.assetmanagement.dto.AssetSearchQuery.Builder()
                .text("nonexistent")
                .build();
        
        org.springframework.data.domain.Pageable pageable = 
            org.springframework.data.domain.PageRequest.of(0, 20);
        
        org.springframework.data.domain.Page<Asset> emptyPage = 
            new org.springframework.data.domain.PageImpl<>(
                java.util.Collections.emptyList(), 
                pageable, 
                0
            );
        
        when(assetRepository.searchAssets(
            eq("nonexistent"), 
            isNull(), 
            isNull(), 
            isNull(), 
            isNull(), 
            isNull(), 
            eq(pageable)
        )).thenReturn(emptyPage);
        
        // When
        org.springframework.data.domain.Page<AssetDTO> result = 
            assetService.searchAssets(query, pageable);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isEqualTo(0);
        
        verify(assetRepository).searchAssets(
            eq("nonexistent"), 
            isNull(), 
            isNull(), 
            isNull(), 
            isNull(), 
            isNull(), 
            eq(pageable)
        );
    }
    
    @Test
    @DisplayName("Should map all asset fields to DTOs in search results")
    void shouldMapAllAssetFieldsInSearchResults() {
        // Given
        UUID assetId = UUID.randomUUID();
        Asset asset = createTestAsset(assetId);
        asset.setLocation("Data Center A");
        asset.setAssignedUser("john.doe");
        asset.setNotes("Test notes");
        
        com.company.assetmanagement.dto.AssetSearchQuery query = 
            new com.company.assetmanagement.dto.AssetSearchQuery.Builder()
                .text("server")
                .build();
        
        org.springframework.data.domain.Pageable pageable = 
            org.springframework.data.domain.PageRequest.of(0, 20);
        
        org.springframework.data.domain.Page<Asset> assetPage = 
            new org.springframework.data.domain.PageImpl<>(
                java.util.List.of(asset), 
                pageable, 
                1
            );
        
        when(assetRepository.searchAssets(
            eq("server"), 
            isNull(), 
            isNull(), 
            isNull(), 
            isNull(), 
            isNull(), 
            eq(pageable)
        )).thenReturn(assetPage);
        
        // When
        org.springframework.data.domain.Page<AssetDTO> result = 
            assetService.searchAssets(query, pageable);
        
        // Then
        assertThat(result.getContent()).hasSize(1);
        AssetDTO dto = result.getContent().get(0);
        assertThat(dto.getId()).isEqualTo(assetId.toString());
        assertThat(dto.getName()).isEqualTo(asset.getName());
        assertThat(dto.getSerialNumber()).isEqualTo(asset.getSerialNumber());
        assertThat(dto.getAssetType()).isEqualTo(asset.getAssetType());
        assertThat(dto.getStatus()).isEqualTo(asset.getStatus());
        assertThat(dto.getLocation()).isEqualTo(asset.getLocation());
        assertThat(dto.getAssignedUser()).isEqualTo(asset.getAssignedUser());
        assertThat(dto.getNotes()).isEqualTo(asset.getNotes());
    }
    
    @Test
    @DisplayName("Should support pagination with different page sizes")
    void shouldSupportPaginationWithDifferentPageSizes() {
        // Given
        com.company.assetmanagement.dto.AssetSearchQuery query = 
            new com.company.assetmanagement.dto.AssetSearchQuery.Builder()
                .text("server")
                .build();
        
        org.springframework.data.domain.Pageable pageable = 
            org.springframework.data.domain.PageRequest.of(1, 10);
        
        java.util.List<Asset> assets = java.util.List.of(
            createTestAsset(UUID.randomUUID()),
            createTestAsset(UUID.randomUUID())
        );
        
        org.springframework.data.domain.Page<Asset> assetPage = 
            new org.springframework.data.domain.PageImpl<>(assets, pageable, 25);
        
        when(assetRepository.searchAssets(
            eq("server"), 
            isNull(), 
            isNull(), 
            isNull(), 
            isNull(), 
            isNull(), 
            eq(pageable)
        )).thenReturn(assetPage);
        
        // When
        org.springframework.data.domain.Page<AssetDTO> result = 
            assetService.searchAssets(query, pageable);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.getNumber()).isEqualTo(1);
        assertThat(result.getSize()).isEqualTo(10);
        assertThat(result.getTotalElements()).isEqualTo(25);
        assertThat(result.getTotalPages()).isEqualTo(3);
        
        verify(assetRepository).searchAssets(
            eq("server"), 
            isNull(), 
            isNull(), 
            isNull(), 
            isNull(), 
            isNull(), 
            eq(pageable)
        );
    }
    
    @Test
    @DisplayName("Should throw IllegalArgumentException when pageable is null")
    void shouldThrowExceptionWhenPageableIsNull() {
        // Given
        com.company.assetmanagement.dto.AssetSearchQuery query = 
            new com.company.assetmanagement.dto.AssetSearchQuery.Builder()
                .text("server")
                .build();
        
        // When/Then
        assertThatThrownBy(() -> assetService.searchAssets(query, null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Pageable");
        
        verify(assetRepository, never()).searchAssets(
            any(), any(), any(), any(), any(), any(), any()
        );
    }
    
    @Test
    @DisplayName("Should support sorting in search results")
    void shouldSupportSortingInSearchResults() {
        // Given
        com.company.assetmanagement.dto.AssetSearchQuery query = 
            new com.company.assetmanagement.dto.AssetSearchQuery.Builder()
                .text("server")
                .build();
        
        org.springframework.data.domain.Pageable pageable = 
            org.springframework.data.domain.PageRequest.of(
                0, 
                20, 
                org.springframework.data.domain.Sort.by("name").ascending()
            );
        
        java.util.List<Asset> assets = java.util.List.of(createTestAsset(UUID.randomUUID()));
        org.springframework.data.domain.Page<Asset> assetPage = 
            new org.springframework.data.domain.PageImpl<>(assets, pageable, assets.size());
        
        when(assetRepository.searchAssets(
            eq("server"), 
            isNull(), 
            isNull(), 
            isNull(), 
            isNull(), 
            isNull(), 
            eq(pageable)
        )).thenReturn(assetPage);
        
        // When
        org.springframework.data.domain.Page<AssetDTO> result = 
            assetService.searchAssets(query, pageable);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.getSort()).isEqualTo(pageable.getSort());
        
        verify(assetRepository).searchAssets(
            eq("server"), 
            isNull(), 
            isNull(), 
            isNull(), 
            isNull(), 
            isNull(), 
            eq(pageable)
        );
    }
    
    // ========== Delete Asset Tests ==========
    
    @Test
    @DisplayName("Should delete asset successfully when asset exists")
    void shouldDeleteAssetSuccessfully() {
        // Given
        UUID assetId = UUID.randomUUID();
        Asset existingAsset = createTestAsset(assetId);
        
        when(assetRepository.findById(assetId)).thenReturn(java.util.Optional.of(existingAsset));
        doNothing().when(assetRepository).delete(existingAsset);
        doNothing().when(auditService).logEvent(any(AuditEventDTO.class));
        
        // When
        assetService.deleteAsset(testUserId, assetId);
        
        // Then
        verify(assetRepository).findById(assetId);
        verify(assetRepository).delete(existingAsset);
        verify(auditService).logEvent(any(AuditEventDTO.class));
    }
    
    @Test
    @DisplayName("Should log audit event with correct details when deleting asset")
    void shouldLogAuditEventWhenDeletingAsset() {
        // Given
        UUID assetId = UUID.randomUUID();
        Asset existingAsset = createTestAsset(assetId);
        
        when(assetRepository.findById(assetId)).thenReturn(java.util.Optional.of(existingAsset));
        doNothing().when(assetRepository).delete(existingAsset);
        
        ArgumentCaptor<AuditEventDTO> auditCaptor = ArgumentCaptor.forClass(AuditEventDTO.class);
        doNothing().when(auditService).logEvent(auditCaptor.capture());
        
        // When
        assetService.deleteAsset(testUserId, assetId);
        
        // Then
        AuditEventDTO capturedEvent = auditCaptor.getValue();
        assertThat(capturedEvent.getUserId()).isEqualTo(UUID.fromString(testUserId));
        assertThat(capturedEvent.getActionType()).isEqualTo(Action.DELETE_ASSET);
        assertThat(capturedEvent.getResourceType()).isEqualTo("ASSET");
        assertThat(capturedEvent.getResourceId()).isEqualTo(assetId.toString());
    }
    
    @Test
    @DisplayName("Should throw ResourceNotFoundException when asset does not exist")
    void shouldThrowResourceNotFoundExceptionWhenAssetDoesNotExist() {
        // Given
        UUID assetId = UUID.randomUUID();
        
        when(assetRepository.findById(assetId)).thenReturn(java.util.Optional.empty());
        
        // When/Then
        assertThatThrownBy(() -> assetService.deleteAsset(testUserId, assetId))
            .isInstanceOf(com.company.assetmanagement.exception.ResourceNotFoundException.class)
            .hasMessageContaining("Asset")
            .hasMessageContaining(assetId.toString());
        
        verify(assetRepository).findById(assetId);
        verify(assetRepository, never()).delete(any(Asset.class));
        verify(auditService, never()).logEvent(any(AuditEventDTO.class));
    }
    
    @Test
    @DisplayName("Should throw IllegalArgumentException when userId is null")
    void shouldThrowIllegalArgumentExceptionWhenUserIdIsNull() {
        // Given
        UUID assetId = UUID.randomUUID();
        
        // When/Then
        assertThatThrownBy(() -> assetService.deleteAsset(null, assetId))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("User ID cannot be null or empty");
        
        verify(assetRepository, never()).findById(any());
        verify(assetRepository, never()).delete(any());
        verify(auditService, never()).logEvent(any());
    }
    
    @Test
    @DisplayName("Should throw IllegalArgumentException when userId is empty")
    void shouldThrowIllegalArgumentExceptionWhenUserIdIsEmpty() {
        // Given
        UUID assetId = UUID.randomUUID();
        
        // When/Then
        assertThatThrownBy(() -> assetService.deleteAsset("", assetId))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("User ID cannot be null or empty");
        
        verify(assetRepository, never()).findById(any());
        verify(assetRepository, never()).delete(any());
        verify(auditService, never()).logEvent(any());
    }
    
    @Test
    @DisplayName("Should throw IllegalArgumentException when assetId is null")
    void shouldThrowIllegalArgumentExceptionWhenAssetIdIsNull() {
        // When/Then
        assertThatThrownBy(() -> assetService.deleteAsset(testUserId, null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Asset ID cannot be null");
        
        verify(assetRepository, never()).findById(any());
        verify(assetRepository, never()).delete(any());
        verify(auditService, never()).logEvent(any());
    }
    
    @Test
    @DisplayName("Should not fail deletion when audit logging fails")
    void shouldNotFailDeletionWhenAuditLoggingFails() {
        // Given
        UUID assetId = UUID.randomUUID();
        Asset existingAsset = createTestAsset(assetId);
        
        when(assetRepository.findById(assetId)).thenReturn(java.util.Optional.of(existingAsset));
        doNothing().when(assetRepository).delete(existingAsset);
        doThrow(new RuntimeException("Audit service unavailable"))
            .when(auditService).logEvent(any(AuditEventDTO.class));
        
        // When - Should not throw exception
        assertThatCode(() -> assetService.deleteAsset(testUserId, assetId))
            .doesNotThrowAnyException();
        
        // Then - Asset should still be deleted
        verify(assetRepository).findById(assetId);
        verify(assetRepository).delete(existingAsset);
        verify(auditService).logEvent(any(AuditEventDTO.class));
    }
    
    // ========== Export Functionality Tests ==========
    
    @Test
    @DisplayName("Should export assets to CSV format successfully")
    void shouldExportAssetsToCsvSuccessfully() {
        // Given
        com.company.assetmanagement.dto.ExportFormat format = 
            com.company.assetmanagement.dto.ExportFormat.CSV;
        com.company.assetmanagement.dto.AssetSearchQuery query = null;
        
        java.util.List<Asset> assets = java.util.Arrays.asList(
            createTestAsset(UUID.randomUUID()),
            createTestAsset(UUID.randomUUID())
        );
        
        org.springframework.data.domain.Page<Asset> assetPage = 
            new org.springframework.data.domain.PageImpl<>(assets);
        
        when(assetRepository.searchAssets(any(), any(), any(), any(), any(), any(), any()))
            .thenReturn(assetPage);
        
        // When
        com.company.assetmanagement.dto.ExportResult result = 
            assetService.exportAssets(format, query);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.getFileName()).contains("assets_export_");
        assertThat(result.getFileName()).endsWith(".csv");
        assertThat(result.getContentType()).isEqualTo("text/csv");
        assertThat(result.getRecordCount()).isEqualTo(2);
        assertThat(result.getData()).isNotNull();
        assertThat(result.getData().length).isGreaterThan(0);
        assertThat(result.getMessage()).contains("Successfully exported 2 asset(s)");
        
        // Verify CSV content
        String csvContent = new String(result.getData(), java.nio.charset.StandardCharsets.UTF_8);
        assertThat(csvContent).contains("ID,Asset Type,Name,Serial Number");
        assertThat(csvContent).contains("Test Server");
        assertThat(csvContent).contains("SRV-TEST-001");
    }
    
    @Test
    @DisplayName("Should export assets to JSON format successfully")
    void shouldExportAssetsToJsonSuccessfully() {
        // Given
        com.company.assetmanagement.dto.ExportFormat format = 
            com.company.assetmanagement.dto.ExportFormat.JSON;
        com.company.assetmanagement.dto.AssetSearchQuery query = null;
        
        java.util.List<Asset> assets = java.util.Arrays.asList(
            createTestAsset(UUID.randomUUID()),
            createTestAsset(UUID.randomUUID())
        );
        
        org.springframework.data.domain.Page<Asset> assetPage = 
            new org.springframework.data.domain.PageImpl<>(assets);
        
        when(assetRepository.searchAssets(any(), any(), any(), any(), any(), any(), any()))
            .thenReturn(assetPage);
        
        // When
        com.company.assetmanagement.dto.ExportResult result = 
            assetService.exportAssets(format, query);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.getFileName()).contains("assets_export_");
        assertThat(result.getFileName()).endsWith(".json");
        assertThat(result.getContentType()).isEqualTo("application/json");
        assertThat(result.getRecordCount()).isEqualTo(2);
        assertThat(result.getData()).isNotNull();
        assertThat(result.getData().length).isGreaterThan(0);
        
        // Verify JSON content
        String jsonContent = new String(result.getData(), java.nio.charset.StandardCharsets.UTF_8);
        assertThat(jsonContent).contains("\"name\" : \"Test Server\"");
        assertThat(jsonContent).contains("\"serialNumber\" : \"SRV-TEST-001\"");
        assertThat(jsonContent).startsWith("[");
        assertThat(jsonContent).endsWith("]");
    }
    
    @Test
    @DisplayName("Should export filtered assets based on search query")
    void shouldExportFilteredAssets() {
        // Given
        com.company.assetmanagement.dto.ExportFormat format = 
            com.company.assetmanagement.dto.ExportFormat.CSV;
        com.company.assetmanagement.dto.AssetSearchQuery query = 
            com.company.assetmanagement.dto.AssetSearchQuery.builder()
                .text("server")
                .assetTypes(java.util.Arrays.asList(AssetType.SERVER))
                .build();
        
        java.util.List<Asset> assets = java.util.Arrays.asList(
            createTestAsset(UUID.randomUUID())
        );
        
        org.springframework.data.domain.Page<Asset> assetPage = 
            new org.springframework.data.domain.PageImpl<>(assets);
        
        when(assetRepository.searchAssets(eq("server"), any(), any(), any(), any(), any(), any()))
            .thenReturn(assetPage);
        
        // When
        com.company.assetmanagement.dto.ExportResult result = 
            assetService.exportAssets(format, query);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.getRecordCount()).isEqualTo(1);
        verify(assetRepository).searchAssets(eq("server"), any(), any(), any(), any(), any(), any());
    }
    
    @Test
    @DisplayName("Should export empty result when no assets match")
    void shouldExportEmptyResultWhenNoAssetsMatch() {
        // Given
        com.company.assetmanagement.dto.ExportFormat format = 
            com.company.assetmanagement.dto.ExportFormat.CSV;
        com.company.assetmanagement.dto.AssetSearchQuery query = null;
        
        java.util.List<Asset> assets = java.util.Collections.emptyList();
        org.springframework.data.domain.Page<Asset> assetPage = 
            new org.springframework.data.domain.PageImpl<>(assets);
        
        when(assetRepository.searchAssets(any(), any(), any(), any(), any(), any(), any()))
            .thenReturn(assetPage);
        
        // When
        com.company.assetmanagement.dto.ExportResult result = 
            assetService.exportAssets(format, query);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.getRecordCount()).isEqualTo(0);
        assertThat(result.getData()).isNotNull();
        assertThat(result.getMessage()).contains("Successfully exported 0 asset(s)");
    }
    
    @Test
    @DisplayName("Should throw IllegalArgumentException when export format is null")
    void shouldThrowExceptionWhenExportFormatIsNull() {
        // When/Then
        assertThatThrownBy(() -> assetService.exportAssets(null, null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Export format");
        
        verify(assetRepository, never()).searchAssets(any(), any(), any(), any(), any(), any(), any());
    }
    
    @Test
    @DisplayName("Should include all asset fields in CSV export")
    void shouldIncludeAllFieldsInCsvExport() {
        // Given
        com.company.assetmanagement.dto.ExportFormat format = 
            com.company.assetmanagement.dto.ExportFormat.CSV;
        
        UUID assetId = UUID.randomUUID();
        Asset asset = createTestAsset(assetId);
        asset.setLocation("Data Center A");
        asset.setAssignedUser("john.doe");
        asset.setAssignedUserEmail("john.doe@example.com");
        asset.setNotes("Test notes");
        asset.setCustomFields("{\"warranty\": \"3 years\"}");
        
        java.util.List<Asset> assets = java.util.Arrays.asList(asset);
        org.springframework.data.domain.Page<Asset> assetPage = 
            new org.springframework.data.domain.PageImpl<>(assets);
        
        when(assetRepository.searchAssets(any(), any(), any(), any(), any(), any(), any()))
            .thenReturn(assetPage);
        
        // When
        com.company.assetmanagement.dto.ExportResult result = 
            assetService.exportAssets(format, null);
        
        // Then
        String csvContent = new String(result.getData(), java.nio.charset.StandardCharsets.UTF_8);
        assertThat(csvContent).contains(assetId.toString());
        assertThat(csvContent).contains("Data Center A");
        assertThat(csvContent).contains("john.doe");
        assertThat(csvContent).contains("john.doe@example.com");
        assertThat(csvContent).contains("Test notes");
        assertThat(csvContent).contains("{\"warranty\": \"3 years\"}");
    }
    
    @Test
    @DisplayName("Should include all asset fields in JSON export")
    void shouldIncludeAllFieldsInJsonExport() {
        // Given
        com.company.assetmanagement.dto.ExportFormat format = 
            com.company.assetmanagement.dto.ExportFormat.JSON;
        
        UUID assetId = UUID.randomUUID();
        Asset asset = createTestAsset(assetId);
        asset.setLocation("Data Center A");
        asset.setAssignedUser("john.doe");
        asset.setAssignedUserEmail("john.doe@example.com");
        asset.setNotes("Test notes");
        
        java.util.List<Asset> assets = java.util.Arrays.asList(asset);
        org.springframework.data.domain.Page<Asset> assetPage = 
            new org.springframework.data.domain.PageImpl<>(assets);
        
        when(assetRepository.searchAssets(any(), any(), any(), any(), any(), any(), any()))
            .thenReturn(assetPage);
        
        // When
        com.company.assetmanagement.dto.ExportResult result = 
            assetService.exportAssets(format, null);
        
        // Then
        String jsonContent = new String(result.getData(), java.nio.charset.StandardCharsets.UTF_8);
        assertThat(jsonContent).contains("\"id\"");
        assertThat(jsonContent).contains("\"assetType\"");
        assertThat(jsonContent).contains("\"name\"");
        assertThat(jsonContent).contains("\"serialNumber\"");
        assertThat(jsonContent).contains("\"location\" : \"Data Center A\"");
        assertThat(jsonContent).contains("\"assignedUser\" : \"john.doe\"");
        assertThat(jsonContent).contains("\"assignedUserEmail\" : \"john.doe@example.com\"");
    }
    
    @Test
    @DisplayName("Should handle CSV special characters correctly")
    void shouldHandleCsvSpecialCharactersCorrectly() {
        // Given
        com.company.assetmanagement.dto.ExportFormat format = 
            com.company.assetmanagement.dto.ExportFormat.CSV;
        
        Asset asset = createTestAsset(UUID.randomUUID());
        asset.setName("Server, with comma");
        asset.setNotes("Notes with \"quotes\" and, commas");
        
        java.util.List<Asset> assets = java.util.Arrays.asList(asset);
        org.springframework.data.domain.Page<Asset> assetPage = 
            new org.springframework.data.domain.PageImpl<>(assets);
        
        when(assetRepository.searchAssets(any(), any(), any(), any(), any(), any(), any()))
            .thenReturn(assetPage);
        
        // When
        com.company.assetmanagement.dto.ExportResult result = 
            assetService.exportAssets(format, null);
        
        // Then
        String csvContent = new String(result.getData(), java.nio.charset.StandardCharsets.UTF_8);
        assertThat(csvContent).contains("\"Server, with comma\"");
        assertThat(csvContent).contains("\"Notes with \"\"quotes\"\" and, commas\"");
    }
    
    @Test
    @DisplayName("Should handle null values in CSV export")
    void shouldHandleNullValuesInCsvExport() {
        // Given
        com.company.assetmanagement.dto.ExportFormat format = 
            com.company.assetmanagement.dto.ExportFormat.CSV;
        
        Asset asset = createTestAsset(UUID.randomUUID());
        asset.setLocation(null);
        asset.setAssignedUser(null);
        asset.setAssignedUserEmail(null);
        asset.setNotes(null);
        asset.setCustomFields(null);
        
        java.util.List<Asset> assets = java.util.Arrays.asList(asset);
        org.springframework.data.domain.Page<Asset> assetPage = 
            new org.springframework.data.domain.PageImpl<>(assets);
        
        when(assetRepository.searchAssets(any(), any(), any(), any(), any(), any(), any()))
            .thenReturn(assetPage);
        
        // When
        com.company.assetmanagement.dto.ExportResult result = 
            assetService.exportAssets(format, null);
        
        // Then
        assertThat(result).isNotNull();
        String csvContent = new String(result.getData(), java.nio.charset.StandardCharsets.UTF_8);
        // Should have empty values for null fields
        assertThat(csvContent).contains(",,"); // Adjacent commas for null values
    }
    
    @Test
    @DisplayName("Should generate unique filename with timestamp")
    void shouldGenerateUniqueFilenameWithTimestamp() {
        // Given
        com.company.assetmanagement.dto.ExportFormat format = 
            com.company.assetmanagement.dto.ExportFormat.CSV;
        
        java.util.List<Asset> assets = java.util.Arrays.asList(createTestAsset(UUID.randomUUID()));
        org.springframework.data.domain.Page<Asset> assetPage = 
            new org.springframework.data.domain.PageImpl<>(assets);
        
        when(assetRepository.searchAssets(any(), any(), any(), any(), any(), any(), any()))
            .thenReturn(assetPage);
        
        // When
        com.company.assetmanagement.dto.ExportResult result1 = 
            assetService.exportAssets(format, null);
        
        // Wait a moment to ensure different timestamp
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        com.company.assetmanagement.dto.ExportResult result2 = 
            assetService.exportAssets(format, null);
        
        // Then
        assertThat(result1.getFileName()).isNotEqualTo(result2.getFileName());
        assertThat(result1.getFileName()).matches("assets_export_\\d{8}_\\d{6}\\.csv");
        assertThat(result2.getFileName()).matches("assets_export_\\d{8}_\\d{6}\\.csv");
    }
    
    @Test
    @DisplayName("Should set correct timestamp in export result")
    void shouldSetCorrectTimestampInExportResult() {
        // Given
        com.company.assetmanagement.dto.ExportFormat format = 
            com.company.assetmanagement.dto.ExportFormat.CSV;
        
        java.util.List<Asset> assets = java.util.Arrays.asList(createTestAsset(UUID.randomUUID()));
        org.springframework.data.domain.Page<Asset> assetPage = 
            new org.springframework.data.domain.PageImpl<>(assets);
        
        when(assetRepository.searchAssets(any(), any(), any(), any(), any(), any(), any()))
            .thenReturn(assetPage);
        
        java.time.LocalDateTime before = java.time.LocalDateTime.now();
        
        // When
        com.company.assetmanagement.dto.ExportResult result = 
            assetService.exportAssets(format, null);
        
        java.time.LocalDateTime after = java.time.LocalDateTime.now();
        
        // Then
        assertThat(result.getTimestamp()).isNotNull();
        assertThat(result.getTimestamp()).isAfterOrEqualTo(before);
        assertThat(result.getTimestamp()).isBeforeOrEqualTo(after);
    }
    
    // ========== Helper Methods ==========
    // ========== Import Functionality Tests ==========
    
    @Test
    @DisplayName("Should import assets from CSV successfully")
    void shouldImportAssetsFromCSVSuccessfully() {
        // Given
        String csvData = "Asset Type,Name,Serial Number,Acquisition Date,Status,Location,Assigned User,Assigned User Email,Notes\n" +
                        "SERVER,Test Server 1,SRV-IMP-001,2024-01-15,ORDERED,Data Center,,,\n" +
                        "LAPTOP,Test Laptop 1,LAP-IMP-001,2024-01-16,RECEIVED,Office,john.doe,john@example.com,Test notes";
        byte[] data = csvData.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        
        when(assetRepository.existsBySerialNumber(anyString())).thenReturn(false);
        when(assetRepository.saveAll(anyList()))
            .thenAnswer(invocation -> {
                List<Asset> assets = invocation.getArgument(0);
                assets.forEach(asset -> asset.setId(UUID.randomUUID()));
                return assets;
            });
        
        // When
        com.company.assetmanagement.dto.ImportResult result = 
            assetService.importAssets(testUserId, com.company.assetmanagement.dto.ImportFormat.CSV, data);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.getTotalRecords()).isEqualTo(2);
        assertThat(result.getSuccessCount()).isEqualTo(2);
        assertThat(result.getFailureCount()).isEqualTo(0);
        assertThat(result.getErrors()).isEmpty();
        assertThat(result.isSuccess()).isTrue();
        
        verify(assetRepository, atLeastOnce()).existsBySerialNumber(anyString());
        verify(assetRepository, atLeastOnce()).saveAll(anyList());
    }
    
    @Test
    @DisplayName("Should import assets from JSON successfully")
    void shouldImportAssetsFromJSONSuccessfully() {
        // Given
        String jsonData = "[" +
                "{\"assetType\":\"SERVER\",\"name\":\"Test Server 1\",\"serialNumber\":\"SRV-JSON-001\"," +
                "\"acquisitionDate\":\"2024-01-15\",\"status\":\"ORDERED\"}," +
                "{\"assetType\":\"LAPTOP\",\"name\":\"Test Laptop 1\",\"serialNumber\":\"LAP-JSON-001\"," +
                "\"acquisitionDate\":\"2024-01-16\",\"status\":\"RECEIVED\"}" +
                "]";
        byte[] data = jsonData.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        
        when(assetRepository.existsBySerialNumber(anyString())).thenReturn(false);
        when(assetRepository.saveAll(anyList()))
            .thenAnswer(invocation -> {
                List<Asset> assets = invocation.getArgument(0);
                assets.forEach(asset -> asset.setId(UUID.randomUUID()));
                return assets;
            });
        
        // When
        com.company.assetmanagement.dto.ImportResult result = 
            assetService.importAssets(testUserId, com.company.assetmanagement.dto.ImportFormat.JSON, data);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.getTotalRecords()).isEqualTo(2);
        assertThat(result.getSuccessCount()).isEqualTo(2);
        assertThat(result.getFailureCount()).isEqualTo(0);
        assertThat(result.getErrors()).isEmpty();
        assertThat(result.isSuccess()).isTrue();
    }
    
    @Test
    @DisplayName("Should collect validation errors with line numbers")
    void shouldCollectValidationErrorsWithLineNumbers() {
        // Given - CSV with invalid data (missing required fields)
        String csvData = "Asset Type,Name,Serial Number,Acquisition Date,Status,Location,Assigned User,Assigned User Email,Notes\n" +
                        "SERVER,Test Server 1,SRV-001,2024-01-15,ORDERED,,,\n" +
                        ",Missing Type,SRV-002,2024-01-16,ORDERED,,,\n" +  // Missing asset type
                        "LAPTOP,,LAP-001,2024-01-17,RECEIVED,,,";  // Missing name
        byte[] data = csvData.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        
        when(assetRepository.existsBySerialNumber("SRV-001")).thenReturn(false);
        when(assetRepository.saveAll(anyList()))
            .thenAnswer(invocation -> {
                List<Asset> assets = invocation.getArgument(0);
                assets.forEach(asset -> asset.setId(UUID.randomUUID()));
                return assets;
            });
        
        // Mock validation to throw exception for invalid records
        doNothing().when(validationService).validateAssetRequest(argThat(req -> 
            "SRV-001".equals(req.getSerialNumber())));
        doThrow(new ValidationException(List.of(
            new ValidationError("assetType", "Asset type is required"))))
            .when(validationService).validateAssetRequest(argThat(req -> 
                "SRV-002".equals(req.getSerialNumber())));
        doThrow(new ValidationException(List.of(
            new ValidationError("name", "Name is required"))))
            .when(validationService).validateAssetRequest(argThat(req -> 
                "LAP-001".equals(req.getSerialNumber())));
        
        // When
        com.company.assetmanagement.dto.ImportResult result = 
            assetService.importAssets(testUserId, com.company.assetmanagement.dto.ImportFormat.CSV, data);
        
        // Then
        assertThat(result.getTotalRecords()).isEqualTo(3);
        assertThat(result.getSuccessCount()).isEqualTo(1);
        assertThat(result.getFailureCount()).isEqualTo(2);
        assertThat(result.getErrors()).hasSize(2);
        assertThat(result.isSuccess()).isFalse();
        
        // Verify error details include line numbers
        assertThat(result.getErrors()).anySatisfy(error -> {
            assertThat(error.getLineNumber()).isEqualTo(3);
            assertThat(error.getErrorMessage()).contains("assetType");
        });
        assertThat(result.getErrors()).anySatisfy(error -> {
            assertThat(error.getLineNumber()).isEqualTo(4);
            assertThat(error.getErrorMessage()).contains("name");
        });
    }
    
    @Test
    @DisplayName("Should check for duplicate serial numbers during import")
    void shouldCheckForDuplicateSerialNumbersDuringImport() {
        // Given
        String csvData = "Asset Type,Name,Serial Number,Acquisition Date,Status,Location,Assigned User,Assigned User Email,Notes\n" +
                        "SERVER,Test Server 1,SRV-DUP-001,2024-01-15,ORDERED,,,\n" +
                        "LAPTOP,Test Laptop 1,LAP-DUP-001,2024-01-16,RECEIVED,,,";
        byte[] data = csvData.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        
        // First serial number is duplicate, second is not
        when(assetRepository.existsBySerialNumber("SRV-DUP-001")).thenReturn(true);
        when(assetRepository.existsBySerialNumber("LAP-DUP-001")).thenReturn(false);
        when(assetRepository.saveAll(anyList()))
            .thenAnswer(invocation -> {
                List<Asset> assets = invocation.getArgument(0);
                assets.forEach(asset -> asset.setId(UUID.randomUUID()));
                return assets;
            });
        
        // When
        com.company.assetmanagement.dto.ImportResult result = 
            assetService.importAssets(testUserId, com.company.assetmanagement.dto.ImportFormat.CSV, data);
        
        // Then
        assertThat(result.getTotalRecords()).isEqualTo(2);
        assertThat(result.getSuccessCount()).isEqualTo(1);
        assertThat(result.getFailureCount()).isEqualTo(1);
        assertThat(result.getErrors()).hasSize(1);
        
        // Verify duplicate error
        assertThat(result.getErrors().get(0).getErrorMessage())
            .contains("Duplicate serial number")
            .contains("SRV-DUP-001");
        assertThat(result.getErrors().get(0).getSerialNumber()).isEqualTo("SRV-DUP-001");
    }
    
    @Test
    @DisplayName("Should process imports in batches")
    void shouldProcessImportsInBatches() {
        // Given - Create 250 records to test batch processing (batch size is 100)
        StringBuilder csvBuilder = new StringBuilder();
        csvBuilder.append("Asset Type,Name,Serial Number,Acquisition Date,Status,Location,Assigned User,Assigned User Email,Notes\n");
        for (int i = 1; i <= 250; i++) {
            csvBuilder.append(String.format("SERVER,Test Server %d,SRV-BATCH-%03d,2024-01-15,ORDERED,,,\n", i, i));
        }
        byte[] data = csvBuilder.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8);
        
        when(assetRepository.existsBySerialNumber(anyString())).thenReturn(false);
        when(assetRepository.saveAll(anyList()))
            .thenAnswer(invocation -> {
                List<Asset> assets = invocation.getArgument(0);
                assets.forEach(asset -> asset.setId(UUID.randomUUID()));
                return assets;
            });
        
        // When
        com.company.assetmanagement.dto.ImportResult result = 
            assetService.importAssets(testUserId, com.company.assetmanagement.dto.ImportFormat.CSV, data);
        
        // Then
        assertThat(result.getTotalRecords()).isEqualTo(250);
        assertThat(result.getSuccessCount()).isEqualTo(250);
        assertThat(result.getFailureCount()).isEqualTo(0);
        
        // Verify saveAll was called multiple times (for batches)
        verify(assetRepository, atLeast(2)).saveAll(anyList());
    }
    
    @Test
    @DisplayName("Should throw exception when file size exceeds 10MB")
    void shouldThrowExceptionWhenFileSizeExceeds10MB() {
        // Given - Create data larger than 10MB
        byte[] largeData = new byte[11 * 1024 * 1024]; // 11MB
        
        // When/Then
        assertThatThrownBy(() -> 
            assetService.importAssets(testUserId, com.company.assetmanagement.dto.ImportFormat.CSV, largeData))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("File size exceeds maximum allowed size of 10MB");
        
        verify(assetRepository, never()).saveAll(anyList());
    }
    
    @Test
    @DisplayName("Should throw exception when record count exceeds 10,000")
    void shouldThrowExceptionWhenRecordCountExceeds10000() {
        // Given - Create CSV with more than 10,000 records
        StringBuilder csvBuilder = new StringBuilder();
        csvBuilder.append("Asset Type,Name,Serial Number,Acquisition Date,Status,Location,Assigned User,Assigned User Email,Notes\n");
        for (int i = 1; i <= 10001; i++) {
            csvBuilder.append(String.format("SERVER,Server %d,SRV-%05d,2024-01-15,ORDERED,,,\n", i, i));
        }
        byte[] data = csvBuilder.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8);
        
        // When/Then
        assertThatThrownBy(() -> 
            assetService.importAssets(testUserId, com.company.assetmanagement.dto.ImportFormat.CSV, data))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("more than maximum allowed 10000 records");
        
        verify(assetRepository, never()).saveAll(anyList());
    }
    
    @Test
    @DisplayName("Should throw exception when userId is null")
    void shouldThrowExceptionWhenUserIdIsNullForImport() {
        // Given
        byte[] data = "test".getBytes();
        
        // When/Then
        assertThatThrownBy(() -> 
            assetService.importAssets(null, com.company.assetmanagement.dto.ImportFormat.CSV, data))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("User ID");
    }
    
    @Test
    @DisplayName("Should throw exception when format is null")
    void shouldThrowExceptionWhenFormatIsNull() {
        // Given
        byte[] data = "test".getBytes();
        
        // When/Then
        assertThatThrownBy(() -> 
            assetService.importAssets(testUserId, null, data))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("format");
    }
    
    @Test
    @DisplayName("Should throw exception when data is null")
    void shouldThrowExceptionWhenDataIsNull() {
        // When/Then
        assertThatThrownBy(() -> 
            assetService.importAssets(testUserId, com.company.assetmanagement.dto.ImportFormat.CSV, null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("data");
    }
    
    @Test
    @DisplayName("Should throw exception when data is empty")
    void shouldThrowExceptionWhenDataIsEmpty() {
        // Given
        byte[] emptyData = new byte[0];
        
        // When/Then
        assertThatThrownBy(() -> 
            assetService.importAssets(testUserId, com.company.assetmanagement.dto.ImportFormat.CSV, emptyData))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("data");
    }
    
    @Test
    @DisplayName("Should handle CSV with quoted values containing commas")
    void shouldHandleCSVWithQuotedValues() {
        // Given
        String csvData = "Asset Type,Name,Serial Number,Acquisition Date,Status,Location,Assigned User,Assigned User Email,Notes\n" +
                        "SERVER,\"Test Server, Production\",SRV-QUOTE-001,2024-01-15,ORDERED,\"Data Center, Building A\",,,\"Notes with, commas\"";
        byte[] data = csvData.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        
        when(assetRepository.existsBySerialNumber(anyString())).thenReturn(false);
        when(assetRepository.saveAll(anyList()))
            .thenAnswer(invocation -> {
                List<Asset> assets = invocation.getArgument(0);
                assets.forEach(asset -> asset.setId(UUID.randomUUID()));
                return assets;
            });
        
        // When
        com.company.assetmanagement.dto.ImportResult result = 
            assetService.importAssets(testUserId, com.company.assetmanagement.dto.ImportFormat.CSV, data);
        
        // Then
        assertThat(result.getSuccessCount()).isEqualTo(1);
        assertThat(result.getFailureCount()).isEqualTo(0);
    }
    
    @Test
    @DisplayName("Should skip empty lines in CSV")
    void shouldSkipEmptyLinesInCSV() {
        // Given
        String csvData = "Asset Type,Name,Serial Number,Acquisition Date,Status,Location,Assigned User,Assigned User Email,Notes\n" +
                        "SERVER,Test Server 1,SRV-EMPTY-001,2024-01-15,ORDERED,,,\n" +
                        "\n" +  // Empty line
                        "LAPTOP,Test Laptop 1,LAP-EMPTY-001,2024-01-16,RECEIVED,,,";
        byte[] data = csvData.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        
        when(assetRepository.existsBySerialNumber(anyString())).thenReturn(false);
        when(assetRepository.saveAll(anyList()))
            .thenAnswer(invocation -> {
                List<Asset> assets = invocation.getArgument(0);
                assets.forEach(asset -> asset.setId(UUID.randomUUID()));
                return assets;
            });
        
        // When
        com.company.assetmanagement.dto.ImportResult result = 
            assetService.importAssets(testUserId, com.company.assetmanagement.dto.ImportFormat.CSV, data);
        
        // Then
        assertThat(result.getTotalRecords()).isEqualTo(2);
        assertThat(result.getSuccessCount()).isEqualTo(2);
    }
    
    @Test
    @DisplayName("Should log audit events for imported assets")
    void shouldLogAuditEventsForImportedAssets() {
        // Given
        String csvData = "Asset Type,Name,Serial Number,Acquisition Date,Status,Location,Assigned User,Assigned User Email,Notes\n" +
                        "SERVER,Test Server 1,SRV-AUDIT-001,2024-01-15,ORDERED,,,";
        byte[] data = csvData.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        
        when(assetRepository.existsBySerialNumber(anyString())).thenReturn(false);
        when(assetRepository.saveAll(anyList()))
            .thenAnswer(invocation -> {
                List<Asset> assets = invocation.getArgument(0);
                assets.forEach(asset -> asset.setId(UUID.randomUUID()));
                return assets;
            });
        
        // When
        assetService.importAssets(testUserId, com.company.assetmanagement.dto.ImportFormat.CSV, data);
        
        // Then
        verify(auditService, atLeastOnce()).logEvent(any(AuditEventDTO.class));
    }
    
    @Test
    @DisplayName("Should return appropriate message for successful import")
    void shouldReturnAppropriateMessageForSuccessfulImport() {
        // Given
        String csvData = "Asset Type,Name,Serial Number,Acquisition Date,Status,Location,Assigned User,Assigned User Email,Notes\n" +
                        "SERVER,Test Server 1,SRV-MSG-001,2024-01-15,ORDERED,,,";
        byte[] data = csvData.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        
        when(assetRepository.existsBySerialNumber(anyString())).thenReturn(false);
        when(assetRepository.saveAll(anyList()))
            .thenAnswer(invocation -> {
                List<Asset> assets = invocation.getArgument(0);
                assets.forEach(asset -> asset.setId(UUID.randomUUID()));
                return assets;
            });
        
        // When
        com.company.assetmanagement.dto.ImportResult result = 
            assetService.importAssets(testUserId, com.company.assetmanagement.dto.ImportFormat.CSV, data);
        
        // Then
        assertThat(result.getMessage()).contains("Successfully imported all 1 asset(s)");
    }
    
    @Test
    @DisplayName("Should return appropriate message for partial import")
    void shouldReturnAppropriateMessageForPartialImport() {
        // Given
        String csvData = "Asset Type,Name,Serial Number,Acquisition Date,Status,Location,Assigned User,Assigned User Email,Notes\n" +
                        "SERVER,Test Server 1,SRV-PART-001,2024-01-15,ORDERED,,,\n" +
                        ",Missing Type,SRV-PART-002,2024-01-16,ORDERED,,,";
        byte[] data = csvData.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        
        when(assetRepository.existsBySerialNumber("SRV-PART-001")).thenReturn(false);
        when(assetRepository.saveAll(anyList()))
            .thenAnswer(invocation -> {
                List<Asset> assets = invocation.getArgument(0);
                assets.forEach(asset -> asset.setId(UUID.randomUUID()));
                return assets;
            });
        
        doNothing().when(validationService).validateAssetRequest(argThat(req -> 
            "SRV-PART-001".equals(req.getSerialNumber())));
        doThrow(new ValidationException(List.of(
            new ValidationError("assetType", "Asset type is required"))))
            .when(validationService).validateAssetRequest(argThat(req -> 
                "SRV-PART-002".equals(req.getSerialNumber())));
        
        // When
        com.company.assetmanagement.dto.ImportResult result = 
            assetService.importAssets(testUserId, com.company.assetmanagement.dto.ImportFormat.CSV, data);
        
        // Then
        assertThat(result.getMessage()).contains("Imported 1 asset(s) with 1 failure(s)");
    }
    
    @Test
    @DisplayName("Should return appropriate message for failed import")
    void shouldReturnAppropriateMessageForFailedImport() {
        // Given
        String csvData = "Asset Type,Name,Serial Number,Acquisition Date,Status,Location,Assigned User,Assigned User Email,Notes\n" +
                        ",Missing Type,SRV-FAIL-001,2024-01-16,ORDERED,,,";
        byte[] data = csvData.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        
        doThrow(new ValidationException(List.of(
            new ValidationError("assetType", "Asset type is required"))))
            .when(validationService).validateAssetRequest(any());
        
        // When
        com.company.assetmanagement.dto.ImportResult result = 
            assetService.importAssets(testUserId, com.company.assetmanagement.dto.ImportFormat.CSV, data);
        
        // Then
        assertThat(result.getMessage()).contains("Import failed. No assets were imported");
    }
    
    /**
     * Creates a valid asset request for testing.
     */
    private AssetRequest createValidAssetRequest() {
        return AssetRequest.builder()
            .assetType(AssetType.SERVER)
            .name("Test Server")
            .serialNumber("SRV-TEST-001")
            .acquisitionDate(LocalDate.now())
            .status(LifecycleStatus.ORDERED)
            .build();
    }
    
    /**
     * Creates a test asset entity with the given ID.
     */
    private Asset createTestAsset(UUID assetId) {
        Asset asset = new Asset();
        asset.setId(assetId);
        asset.setAssetType(AssetType.SERVER);
        asset.setName("Test Server");
        asset.setSerialNumber("SRV-TEST-001");
        asset.setAcquisitionDate(LocalDate.now());
        asset.setStatus(LifecycleStatus.ORDERED);
        asset.setCreatedBy(UUID.randomUUID());
        asset.setUpdatedBy(UUID.randomUUID());
        asset.setReadOnly(false);
        return asset;
    }
}
