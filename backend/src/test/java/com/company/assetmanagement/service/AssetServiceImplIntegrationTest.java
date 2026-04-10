package com.company.assetmanagement.service;

import com.company.assetmanagement.dto.AssetDTO;
import com.company.assetmanagement.dto.AssetRequest;
import com.company.assetmanagement.exception.DuplicateSerialNumberException;
import com.company.assetmanagement.exception.ValidationException;
import com.company.assetmanagement.model.Asset;
import com.company.assetmanagement.model.AssetType;
import com.company.assetmanagement.model.AuditLog;
import com.company.assetmanagement.model.LifecycleStatus;
import com.company.assetmanagement.repository.AssetRepository;
import com.company.assetmanagement.repository.AuditLogRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

/**
 * Integration tests for AssetServiceImpl.
 * Tests the complete flow with real database and all dependencies.
 * 
 * Test Coverage:
 * - Database persistence
 * - Transaction management
 * - Audit log creation
 * - Serial number uniqueness constraint
 * - Complete end-to-end flows
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("AssetServiceImpl Integration Tests")
class AssetServiceImplIntegrationTest {
    
    @Autowired
    private AssetService assetService;
    
    @Autowired
    private AssetRepository assetRepository;
    
    @Autowired
    private AuditLogRepository auditLogRepository;
    
    private String testUserId;
    
    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID().toString();
        // Clean up any existing test data
        assetRepository.deleteAll();
        auditLogRepository.deleteAll();
    }
    
    // ========== Successful Creation and Persistence Tests ==========
    
    @Test
    @DisplayName("Should persist asset to database")
    void shouldPersistAssetToDatabase() {
        // Given
        AssetRequest request = createValidAssetRequest("SRV-INT-001");
        
        // When
        AssetDTO result = assetService.createAsset(testUserId, request);
        
        // Then
        Optional<Asset> savedAsset = assetRepository.findById(UUID.fromString(result.getId()));
        assertThat(savedAsset).isPresent();
        assertThat(savedAsset.get().getName()).isEqualTo(request.getName());
        assertThat(savedAsset.get().getSerialNumber()).isEqualTo(request.getSerialNumber());
        assertThat(savedAsset.get().getAssetType()).isEqualTo(request.getAssetType());
        assertThat(savedAsset.get().getStatus()).isEqualTo(request.getStatus());
    }
    
    @Test
    @DisplayName("Should generate unique UUID for asset")
    void shouldGenerateUniqueUUID() {
        // Given
        AssetRequest request1 = createValidAssetRequest("SRV-INT-002");
        AssetRequest request2 = createValidAssetRequest("SRV-INT-003");
        
        // When
        AssetDTO result1 = assetService.createAsset(testUserId, request1);
        AssetDTO result2 = assetService.createAsset(testUserId, request2);
        
        // Then
        assertThat(result1.getId()).isNotNull();
        assertThat(result2.getId()).isNotNull();
        assertThat(result1.getId()).isNotEqualTo(result2.getId());
    }
    
    @Test
    @DisplayName("Should set audit timestamps on creation")
    void shouldSetAuditTimestamps() {
        // Given
        AssetRequest request = createValidAssetRequest("SRV-INT-004");
        
        // When
        AssetDTO result = assetService.createAsset(testUserId, request);
        
        // Then
        Optional<Asset> savedAsset = assetRepository.findById(UUID.fromString(result.getId()));
        assertThat(savedAsset).isPresent();
        assertThat(savedAsset.get().getCreatedAt()).isNotNull();
        assertThat(savedAsset.get().getUpdatedAt()).isNotNull();
        assertThat(savedAsset.get().getCreatedBy()).isEqualTo(UUID.fromString(testUserId));
        assertThat(savedAsset.get().getUpdatedBy()).isEqualTo(UUID.fromString(testUserId));
    }
    
    @Test
    @DisplayName("Should persist all asset fields correctly")
    void shouldPersistAllFieldsCorrectly() {
        // Given
        AssetRequest request = AssetRequest.builder()
            .assetType(AssetType.LAPTOP)
            .name("Integration Test Laptop")
            .serialNumber("LAP-INT-001")
            .acquisitionDate(LocalDate.of(2024, 1, 15))
            .status(LifecycleStatus.ORDERED)
            .location("Building A, Floor 3")
            .assignedUser("john.doe")
            .assignedUserEmail("john.doe@example.com")
            .notes("Test notes for integration")
            .customFields("{\"warranty\": \"3 years\", \"vendor\": \"Dell\"}")
            .build();
        
        // When
        AssetDTO result = assetService.createAsset(testUserId, request);
        
        // Then
        Optional<Asset> savedAsset = assetRepository.findById(UUID.fromString(result.getId()));
        assertThat(savedAsset).isPresent();
        Asset asset = savedAsset.get();
        assertThat(asset.getAssetType()).isEqualTo(AssetType.LAPTOP);
        assertThat(asset.getName()).isEqualTo("Integration Test Laptop");
        assertThat(asset.getSerialNumber()).isEqualTo("LAP-INT-001");
        assertThat(asset.getAcquisitionDate()).isEqualTo(LocalDate.of(2024, 1, 15));
        assertThat(asset.getStatus()).isEqualTo(LifecycleStatus.ORDERED);
        assertThat(asset.getLocation()).isEqualTo("Building A, Floor 3");
        assertThat(asset.getAssignedUser()).isEqualTo("john.doe");
        assertThat(asset.getAssignedUserEmail()).isEqualTo("john.doe@example.com");
        assertThat(asset.getNotes()).isEqualTo("Test notes for integration");
        assertThat(asset.getCustomFields()).isEqualTo("{\"warranty\": \"3 years\", \"vendor\": \"Dell\"}");
        assertThat(asset.isReadOnly()).isFalse();
    }
    
    // ========== Serial Number Uniqueness Tests ==========
    
    @Test
    @DisplayName("Should enforce serial number uniqueness at database level")
    void shouldEnforceSerialNumberUniqueness() {
        // Given
        AssetRequest request1 = createValidAssetRequest("SRV-UNIQUE-001");
        AssetRequest request2 = createValidAssetRequest("SRV-UNIQUE-001");
        
        // When
        assetService.createAsset(testUserId, request1);
        
        // Then
        assertThatThrownBy(() -> assetService.createAsset(testUserId, request2))
            .isInstanceOf(DuplicateSerialNumberException.class)
            .hasMessageContaining("SRV-UNIQUE-001");
    }
    
    @Test
    @DisplayName("Should allow different serial numbers")
    void shouldAllowDifferentSerialNumbers() {
        // Given
        AssetRequest request1 = createValidAssetRequest("SRV-DIFF-001");
        AssetRequest request2 = createValidAssetRequest("SRV-DIFF-002");
        
        // When/Then - should not throw exception
        assertThatCode(() -> {
            assetService.createAsset(testUserId, request1);
            assetService.createAsset(testUserId, request2);
        }).doesNotThrowAnyException();
        
        // Verify both assets exist
        assertThat(assetRepository.findBySerialNumber("SRV-DIFF-001")).isPresent();
        assertThat(assetRepository.findBySerialNumber("SRV-DIFF-002")).isPresent();
    }
    
    @Test
    @DisplayName("Should check serial number case-sensitively")
    void shouldCheckSerialNumberCaseSensitively() {
        // Given
        AssetRequest request1 = createValidAssetRequest("srv-case-001");
        AssetRequest request2 = createValidAssetRequest("SRV-CASE-001");
        
        // When/Then - should allow both (case-sensitive)
        assertThatCode(() -> {
            assetService.createAsset(testUserId, request1);
            assetService.createAsset(testUserId, request2);
        }).doesNotThrowAnyException();
        
        // Verify both assets exist
        assertThat(assetRepository.findBySerialNumber("srv-case-001")).isPresent();
        assertThat(assetRepository.findBySerialNumber("SRV-CASE-001")).isPresent();
    }
    
    // ========== Audit Logging Integration Tests ==========
    
    @Test
    @DisplayName("Should create audit log entry on asset creation")
    void shouldCreateAuditLogEntry() {
        // Given
        AssetRequest request = createValidAssetRequest("SRV-AUDIT-001");
        
        // When
        AssetDTO result = assetService.createAsset(testUserId, request);
        
        // Then
        List<AuditLog> auditLogs = auditLogRepository.findByResourceId(result.getId());
        assertThat(auditLogs).isNotEmpty();
        assertThat(auditLogs).anySatisfy(log -> {
            assertThat(log.getUserId()).isEqualTo(UUID.fromString(testUserId));
            assertThat(log.getResourceType()).isEqualTo("ASSET");
            assertThat(log.getResourceId()).isEqualTo(result.getId());
        });
    }
    
    @Test
    @DisplayName("Should include correct action type in audit log")
    void shouldIncludeCorrectActionTypeInAuditLog() {
        // Given
        AssetRequest request = createValidAssetRequest("SRV-AUDIT-002");
        
        // When
        AssetDTO result = assetService.createAsset(testUserId, request);
        
        // Then
        List<AuditLog> auditLogs = auditLogRepository.findByResourceId(result.getId());
        assertThat(auditLogs).anySatisfy(log -> {
            assertThat(log.getActionType().name()).isEqualTo("CREATE_ASSET");
        });
    }
    
    // ========== Validation Integration Tests ==========
    
    @Test
    @DisplayName("Should reject asset with missing required fields")
    void shouldRejectAssetWithMissingFields() {
        // Given
        AssetRequest invalidRequest = new AssetRequest();
        invalidRequest.setAssetType(null);
        invalidRequest.setName(null);
        invalidRequest.setSerialNumber(null);
        invalidRequest.setAcquisitionDate(null);
        invalidRequest.setStatus(null);
        
        // When/Then
        assertThatThrownBy(() -> assetService.createAsset(testUserId, invalidRequest))
            .isInstanceOf(ValidationException.class);
        
        // Verify no asset was created
        assertThat(assetRepository.count()).isZero();
    }
    
    @Test
    @DisplayName("Should reject asset with invalid field lengths")
    void shouldRejectAssetWithInvalidFieldLengths() {
        // Given
        AssetRequest invalidRequest = createValidAssetRequest("SHORT");
        invalidRequest.setSerialNumber("1234"); // Too short (min 5)
        
        // When/Then
        assertThatThrownBy(() -> assetService.createAsset(testUserId, invalidRequest))
            .isInstanceOf(ValidationException.class);
        
        // Verify no asset was created
        assertThat(assetRepository.count()).isZero();
    }
    
    @Test
    @DisplayName("Should reject asset with future acquisition date")
    void shouldRejectAssetWithFutureAcquisitionDate() {
        // Given
        AssetRequest invalidRequest = createValidAssetRequest("SRV-FUTURE-001");
        invalidRequest.setAcquisitionDate(LocalDate.now().plusDays(1));
        
        // When/Then
        assertThatThrownBy(() -> assetService.createAsset(testUserId, invalidRequest))
            .isInstanceOf(ValidationException.class);
        
        // Verify no asset was created
        assertThat(assetRepository.count()).isZero();
    }
    
    @Test
    @DisplayName("Should reject asset with invalid email format")
    void shouldRejectAssetWithInvalidEmail() {
        // Given
        AssetRequest invalidRequest = createValidAssetRequest("SRV-EMAIL-001");
        invalidRequest.setAssignedUserEmail("invalid-email-format");
        
        // When/Then
        assertThatThrownBy(() -> assetService.createAsset(testUserId, invalidRequest))
            .isInstanceOf(ValidationException.class);
        
        // Verify no asset was created
        assertThat(assetRepository.count()).isZero();
    }
    
    // ========== Transaction Management Tests ==========
    
    @Test
    @DisplayName("Should rollback transaction on validation failure")
    void shouldRollbackTransactionOnValidationFailure() {
        // Given
        AssetRequest validRequest = createValidAssetRequest("SRV-ROLLBACK-001");
        AssetRequest invalidRequest = createValidAssetRequest("SRV-ROLLBACK-002");
        invalidRequest.setName(null); // Invalid
        
        // When
        assetService.createAsset(testUserId, validRequest);
        long countBefore = assetRepository.count();
        
        try {
            assetService.createAsset(testUserId, invalidRequest);
        } catch (ValidationException e) {
            // Expected
        }
        
        // Then
        long countAfter = assetRepository.count();
        assertThat(countAfter).isEqualTo(countBefore);
    }
    
    @Test
    @DisplayName("Should rollback transaction on duplicate serial number")
    void shouldRollbackTransactionOnDuplicateSerialNumber() {
        // Given
        AssetRequest request1 = createValidAssetRequest("SRV-DUP-001");
        AssetRequest request2 = createValidAssetRequest("SRV-DUP-001");
        
        // When
        assetService.createAsset(testUserId, request1);
        long countBefore = assetRepository.count();
        
        try {
            assetService.createAsset(testUserId, request2);
        } catch (DuplicateSerialNumberException e) {
            // Expected
        }
        
        // Then
        long countAfter = assetRepository.count();
        assertThat(countAfter).isEqualTo(countBefore);
    }
    
    // ========== Multiple Asset Creation Tests ==========
    
    @Test
    @DisplayName("Should create multiple assets successfully")
    void shouldCreateMultipleAssets() {
        // Given
        int numberOfAssets = 5;
        
        // When
        for (int i = 1; i <= numberOfAssets; i++) {
            AssetRequest request = createValidAssetRequest("SRV-MULTI-" + String.format("%03d", i));
            assetService.createAsset(testUserId, request);
        }
        
        // Then
        long count = assetRepository.count();
        assertThat(count).isEqualTo(numberOfAssets);
    }
    
    @Test
    @DisplayName("Should create assets with different types")
    void shouldCreateAssetsWithDifferentTypes() {
        // Given
        AssetRequest serverRequest = createValidAssetRequest("SRV-TYPE-001");
        serverRequest.setAssetType(AssetType.SERVER);
        
        AssetRequest laptopRequest = createValidAssetRequest("LAP-TYPE-001");
        laptopRequest.setAssetType(AssetType.LAPTOP);
        
        AssetRequest networkRequest = createValidAssetRequest("NET-TYPE-001");
        networkRequest.setAssetType(AssetType.NETWORK_DEVICE);
        
        // When
        assetService.createAsset(testUserId, serverRequest);
        assetService.createAsset(testUserId, laptopRequest);
        assetService.createAsset(testUserId, networkRequest);
        
        // Then
        assertThat(assetRepository.count()).isEqualTo(3);
        assertThat(assetRepository.findBySerialNumber("SRV-TYPE-001")).isPresent();
        assertThat(assetRepository.findBySerialNumber("LAP-TYPE-001")).isPresent();
        assertThat(assetRepository.findBySerialNumber("NET-TYPE-001")).isPresent();
    }
    
    // ========== Repository Query Tests ==========
    
    @Test
    @DisplayName("Should find asset by serial number after creation")
    void shouldFindAssetBySerialNumber() {
        // Given
        AssetRequest request = createValidAssetRequest("SRV-FIND-001");
        
        // When
        assetService.createAsset(testUserId, request);
        
        // Then
        Optional<Asset> foundAsset = assetRepository.findBySerialNumber("SRV-FIND-001");
        assertThat(foundAsset).isPresent();
        assertThat(foundAsset.get().getName()).isEqualTo(request.getName());
    }
    
    @Test
    @DisplayName("Should find asset by ID after creation")
    void shouldFindAssetById() {
        // Given
        AssetRequest request = createValidAssetRequest("SRV-FIND-002");
        
        // When
        AssetDTO result = assetService.createAsset(testUserId, request);
        
        // Then
        Optional<Asset> foundAsset = assetRepository.findById(UUID.fromString(result.getId()));
        assertThat(foundAsset).isPresent();
        assertThat(foundAsset.get().getSerialNumber()).isEqualTo("SRV-FIND-002");
    }
    
    @Test
    @DisplayName("Should verify serial number exists after creation")
    void shouldVerifySerialNumberExists() {
        // Given
        AssetRequest request = createValidAssetRequest("SRV-EXISTS-001");
        
        // When
        assetService.createAsset(testUserId, request);
        
        // Then
        boolean exists = assetRepository.existsBySerialNumber("SRV-EXISTS-001");
        assertThat(exists).isTrue();
    }
    
    // ========== Edge Case Tests ==========
    
    @Test
    @DisplayName("Should handle minimum valid serial number length")
    void shouldHandleMinimumSerialNumberLength() {
        // Given
        AssetRequest request = createValidAssetRequest("12345"); // Exactly 5 characters
        
        // When/Then
        assertThatCode(() -> assetService.createAsset(testUserId, request))
            .doesNotThrowAnyException();
        
        Optional<Asset> savedAsset = assetRepository.findBySerialNumber("12345");
        assertThat(savedAsset).isPresent();
    }
    
    @Test
    @DisplayName("Should handle maximum valid serial number length")
    void shouldHandleMaximumSerialNumberLength() {
        // Given
        String maxLengthSerial = "A".repeat(100); // Exactly 100 characters
        AssetRequest request = createValidAssetRequest(maxLengthSerial);
        
        // When/Then
        assertThatCode(() -> assetService.createAsset(testUserId, request))
            .doesNotThrowAnyException();
        
        Optional<Asset> savedAsset = assetRepository.findBySerialNumber(maxLengthSerial);
        assertThat(savedAsset).isPresent();
    }
    
    @Test
    @DisplayName("Should handle acquisition date as today")
    void shouldHandleAcquisitionDateAsToday() {
        // Given
        AssetRequest request = createValidAssetRequest("SRV-TODAY-001");
        request.setAcquisitionDate(LocalDate.now());
        
        // When/Then
        assertThatCode(() -> assetService.createAsset(testUserId, request))
            .doesNotThrowAnyException();
        
        Optional<Asset> savedAsset = assetRepository.findBySerialNumber("SRV-TODAY-001");
        assertThat(savedAsset).isPresent();
        assertThat(savedAsset.get().getAcquisitionDate()).isEqualTo(LocalDate.now());
    }
    
    @Test
    @DisplayName("Should handle null optional fields")
    void shouldHandleNullOptionalFields() {
        // Given
        AssetRequest request = createValidAssetRequest("SRV-NULL-001");
        request.setLocation(null);
        request.setAssignedUser(null);
        request.setAssignedUserEmail(null);
        request.setNotes(null);
        request.setCustomFields(null);
        
        // When/Then
        assertThatCode(() -> assetService.createAsset(testUserId, request))
            .doesNotThrowAnyException();
        
        Optional<Asset> savedAsset = assetRepository.findBySerialNumber("SRV-NULL-001");
        assertThat(savedAsset).isPresent();
        assertThat(savedAsset.get().getLocation()).isNull();
        assertThat(savedAsset.get().getAssignedUser()).isNull();
        assertThat(savedAsset.get().getAssignedUserEmail()).isNull();
        assertThat(savedAsset.get().getNotes()).isNull();
        assertThat(savedAsset.get().getCustomFields()).isNull();
    }
    
    // ========== Asset Retrieval Integration Tests (getAsset) ==========
    
    @Test
    @DisplayName("Should retrieve asset from database by ID")
    void shouldRetrieveAssetFromDatabase() {
        // Given
        AssetRequest request = createValidAssetRequest("SRV-RETRIEVE-001");
        AssetDTO createdAsset = assetService.createAsset(testUserId, request);
        UUID assetId = UUID.fromString(createdAsset.getId());
        
        // When
        Optional<AssetDTO> result = assetService.getAsset(assetId);
        
        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(createdAsset.getId());
        assertThat(result.get().getName()).isEqualTo(request.getName());
        assertThat(result.get().getSerialNumber()).isEqualTo(request.getSerialNumber());
        assertThat(result.get().getAssetType()).isEqualTo(request.getAssetType());
        assertThat(result.get().getStatus()).isEqualTo(request.getStatus());
    }
    
    @Test
    @DisplayName("Should return empty Optional for non-existent asset ID")
    void shouldReturnEmptyOptionalForNonExistentAsset() {
        // Given
        UUID nonExistentId = UUID.randomUUID();
        
        // When
        Optional<AssetDTO> result = assetService.getAsset(nonExistentId);
        
        // Then
        assertThat(result).isEmpty();
    }
    
    @Test
    @DisplayName("Should retrieve asset with all fields populated")
    void shouldRetrieveAssetWithAllFields() {
        // Given
        AssetRequest request = AssetRequest.builder()
            .assetType(AssetType.LAPTOP)
            .name("Full Fields Laptop")
            .serialNumber("LAP-FULL-001")
            .acquisitionDate(LocalDate.of(2024, 1, 15))
            .status(LifecycleStatus.ORDERED)
            .location("Building B, Floor 2")
            .assignedUser("jane.smith")
            .assignedUserEmail("jane.smith@example.com")
            .notes("Complete test asset")
            .customFields("{\"warranty\": \"2 years\"}")
            .build();
        
        AssetDTO createdAsset = assetService.createAsset(testUserId, request);
        UUID assetId = UUID.fromString(createdAsset.getId());
        
        // When
        Optional<AssetDTO> result = assetService.getAsset(assetId);
        
        // Then
        assertThat(result).isPresent();
        AssetDTO dto = result.get();
        assertThat(dto.getAssetType()).isEqualTo(AssetType.LAPTOP);
        assertThat(dto.getName()).isEqualTo("Full Fields Laptop");
        assertThat(dto.getSerialNumber()).isEqualTo("LAP-FULL-001");
        assertThat(dto.getAcquisitionDate()).isEqualTo(LocalDate.of(2024, 1, 15));
        assertThat(dto.getStatus()).isEqualTo(LifecycleStatus.ORDERED);
        assertThat(dto.getLocation()).isEqualTo("Building B, Floor 2");
        assertThat(dto.getAssignedUser()).isEqualTo("jane.smith");
        assertThat(dto.getAssignedUserEmail()).isEqualTo("jane.smith@example.com");
        assertThat(dto.getNotes()).isEqualTo("Complete test asset");
        assertThat(dto.getCustomFields()).isEqualTo("{\"warranty\": \"2 years\"}");
        assertThat(dto.isReadOnly()).isFalse();
    }
    
    @Test
    @DisplayName("Should retrieve asset with null optional fields")
    void shouldRetrieveAssetWithNullOptionalFields() {
        // Given
        AssetRequest request = createValidAssetRequest("SRV-NULL-RETRIEVE-001");
        request.setLocation(null);
        request.setAssignedUser(null);
        request.setAssignedUserEmail(null);
        request.setNotes(null);
        request.setCustomFields(null);
        
        AssetDTO createdAsset = assetService.createAsset(testUserId, request);
        UUID assetId = UUID.fromString(createdAsset.getId());
        
        // When
        Optional<AssetDTO> result = assetService.getAsset(assetId);
        
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
    @DisplayName("Should retrieve asset with correct audit fields")
    void shouldRetrieveAssetWithCorrectAuditFields() {
        // Given
        AssetRequest request = createValidAssetRequest("SRV-AUDIT-RETRIEVE-001");
        AssetDTO createdAsset = assetService.createAsset(testUserId, request);
        UUID assetId = UUID.fromString(createdAsset.getId());
        
        // When
        Optional<AssetDTO> result = assetService.getAsset(assetId);
        
        // Then
        assertThat(result).isPresent();
        AssetDTO dto = result.get();
        assertThat(dto.getCreatedBy()).isEqualTo(testUserId);
        assertThat(dto.getUpdatedBy()).isEqualTo(testUserId);
        assertThat(dto.getCreatedAt()).isNotNull();
        assertThat(dto.getUpdatedAt()).isNotNull();
    }
    
    @Test
    @DisplayName("Should retrieve multiple different assets correctly")
    void shouldRetrieveMultipleDifferentAssets() {
        // Given
        AssetRequest request1 = createValidAssetRequest("SRV-MULTI-RETRIEVE-001");
        AssetRequest request2 = createValidAssetRequest("LAP-MULTI-RETRIEVE-002");
        request2.setAssetType(AssetType.LAPTOP);
        request2.setName("Test Laptop");
        
        AssetDTO createdAsset1 = assetService.createAsset(testUserId, request1);
        AssetDTO createdAsset2 = assetService.createAsset(testUserId, request2);
        
        // When
        Optional<AssetDTO> result1 = assetService.getAsset(UUID.fromString(createdAsset1.getId()));
        Optional<AssetDTO> result2 = assetService.getAsset(UUID.fromString(createdAsset2.getId()));
        
        // Then
        assertThat(result1).isPresent();
        assertThat(result2).isPresent();
        assertThat(result1.get().getSerialNumber()).isEqualTo("SRV-MULTI-RETRIEVE-001");
        assertThat(result2.get().getSerialNumber()).isEqualTo("LAP-MULTI-RETRIEVE-002");
        assertThat(result1.get().getAssetType()).isEqualTo(AssetType.SERVER);
        assertThat(result2.get().getAssetType()).isEqualTo(AssetType.LAPTOP);
    }
    
    @Test
    @DisplayName("Should retrieve asset with different lifecycle statuses")
    void shouldRetrieveAssetWithDifferentStatuses() {
        // Given
        AssetRequest orderedRequest = createValidAssetRequest("SRV-STATUS-ORDERED");
        orderedRequest.setStatus(LifecycleStatus.ORDERED);
        
        AssetRequest receivedRequest = createValidAssetRequest("SRV-STATUS-RECEIVED");
        receivedRequest.setStatus(LifecycleStatus.RECEIVED);
        
        AssetDTO orderedAsset = assetService.createAsset(testUserId, orderedRequest);
        AssetDTO receivedAsset = assetService.createAsset(testUserId, receivedRequest);
        
        // When
        Optional<AssetDTO> orderedResult = assetService.getAsset(UUID.fromString(orderedAsset.getId()));
        Optional<AssetDTO> receivedResult = assetService.getAsset(UUID.fromString(receivedAsset.getId()));
        
        // Then
        assertThat(orderedResult).isPresent();
        assertThat(receivedResult).isPresent();
        assertThat(orderedResult.get().getStatus()).isEqualTo(LifecycleStatus.ORDERED);
        assertThat(receivedResult.get().getStatus()).isEqualTo(LifecycleStatus.RECEIVED);
    }
    
    @Test
    @DisplayName("Should retrieve asset with different asset types")
    void shouldRetrieveAssetWithDifferentTypes() {
        // Given
        AssetRequest serverRequest = createValidAssetRequest("SRV-TYPE-RETRIEVE-001");
        serverRequest.setAssetType(AssetType.SERVER);
        
        AssetRequest laptopRequest = createValidAssetRequest("LAP-TYPE-RETRIEVE-001");
        laptopRequest.setAssetType(AssetType.LAPTOP);
        
        AssetRequest networkRequest = createValidAssetRequest("NET-TYPE-RETRIEVE-001");
        networkRequest.setAssetType(AssetType.NETWORK_DEVICE);
        
        AssetDTO serverAsset = assetService.createAsset(testUserId, serverRequest);
        AssetDTO laptopAsset = assetService.createAsset(testUserId, laptopRequest);
        AssetDTO networkAsset = assetService.createAsset(testUserId, networkRequest);
        
        // When
        Optional<AssetDTO> serverResult = assetService.getAsset(UUID.fromString(serverAsset.getId()));
        Optional<AssetDTO> laptopResult = assetService.getAsset(UUID.fromString(laptopAsset.getId()));
        Optional<AssetDTO> networkResult = assetService.getAsset(UUID.fromString(networkAsset.getId()));
        
        // Then
        assertThat(serverResult).isPresent();
        assertThat(laptopResult).isPresent();
        assertThat(networkResult).isPresent();
        assertThat(serverResult.get().getAssetType()).isEqualTo(AssetType.SERVER);
        assertThat(laptopResult.get().getAssetType()).isEqualTo(AssetType.LAPTOP);
        assertThat(networkResult.get().getAssetType()).isEqualTo(AssetType.NETWORK_DEVICE);
    }
    
    @Test
    @DisplayName("Should verify DTO mapping is consistent with entity")
    void shouldVerifyDTOMappingConsistency() {
        // Given
        AssetRequest request = createValidAssetRequest("SRV-MAPPING-001");
        AssetDTO createdAsset = assetService.createAsset(testUserId, request);
        UUID assetId = UUID.fromString(createdAsset.getId());
        
        // When
        Optional<AssetDTO> retrievedAsset = assetService.getAsset(assetId);
        Optional<Asset> entityFromRepo = assetRepository.findById(assetId);
        
        // Then
        assertThat(retrievedAsset).isPresent();
        assertThat(entityFromRepo).isPresent();
        
        AssetDTO dto = retrievedAsset.get();
        Asset entity = entityFromRepo.get();
        
        assertThat(dto.getId()).isEqualTo(entity.getId().toString());
        assertThat(dto.getAssetType()).isEqualTo(entity.getAssetType());
        assertThat(dto.getName()).isEqualTo(entity.getName());
        assertThat(dto.getSerialNumber()).isEqualTo(entity.getSerialNumber());
        assertThat(dto.getAcquisitionDate()).isEqualTo(entity.getAcquisitionDate());
        assertThat(dto.getStatus()).isEqualTo(entity.getStatus());
        assertThat(dto.isReadOnly()).isEqualTo(entity.isReadOnly());
    }
    
    @Test
    @DisplayName("Should handle retrieval immediately after creation")
    void shouldHandleRetrievalImmediatelyAfterCreation() {
        // Given
        AssetRequest request = createValidAssetRequest("SRV-IMMEDIATE-001");
        
        // When
        AssetDTO createdAsset = assetService.createAsset(testUserId, request);
        Optional<AssetDTO> retrievedAsset = assetService.getAsset(UUID.fromString(createdAsset.getId()));
        
        // Then
        assertThat(retrievedAsset).isPresent();
        assertThat(retrievedAsset.get().getId()).isEqualTo(createdAsset.getId());
        assertThat(retrievedAsset.get().getSerialNumber()).isEqualTo(createdAsset.getSerialNumber());
    }
    
    // ========== Asset Update Integration Tests ==========
    
    @Test
    @DisplayName("Should update asset and persist changes to database")
    void shouldUpdateAssetAndPersistChanges() {
        // Given
        AssetRequest createRequest = createValidAssetRequest("SRV-UPDATE-001");
        AssetDTO createdAsset = assetService.createAsset(testUserId, createRequest);
        UUID assetId = UUID.fromString(createdAsset.getId());
        
        AssetRequest updateRequest = AssetRequest.builder()
            .assetType(AssetType.SERVER)
            .name("Updated Server Name")
            .serialNumber("SRV-UPDATE-001")
            .acquisitionDate(LocalDate.now())
            .status(LifecycleStatus.RECEIVED)
            .location("Updated Location")
            .build();
        
        // When
        AssetDTO updatedAsset = assetService.updateAsset(testUserId, assetId, updateRequest);
        
        // Then
        Optional<Asset> savedAsset = assetRepository.findById(assetId);
        assertThat(savedAsset).isPresent();
        assertThat(savedAsset.get().getName()).isEqualTo("Updated Server Name");
        assertThat(savedAsset.get().getStatus()).isEqualTo(LifecycleStatus.RECEIVED);
        assertThat(savedAsset.get().getLocation()).isEqualTo("Updated Location");
    }
    
    @Test
    @DisplayName("Should create audit log entry on asset update")
    void shouldCreateAuditLogEntryOnUpdate() {
        // Given
        AssetRequest createRequest = createValidAssetRequest("SRV-UPDATE-AUDIT-001");
        AssetDTO createdAsset = assetService.createAsset(testUserId, createRequest);
        UUID assetId = UUID.fromString(createdAsset.getId());
        
        AssetRequest updateRequest = createValidAssetRequest("SRV-UPDATE-AUDIT-001");
        updateRequest.setName("Updated Name");
        
        // When
        assetService.updateAsset(testUserId, assetId, updateRequest);
        
        // Then
        List<AuditLog> auditLogs = auditLogRepository.findByResourceId(createdAsset.getId());
        assertThat(auditLogs).hasSizeGreaterThan(1); // CREATE + UPDATE
        assertThat(auditLogs).anySatisfy(log -> {
            assertThat(log.getActionType().name()).isEqualTo("UPDATE_ASSET");
        });
    }
    
    @Test
    @DisplayName("Should not update immutable fields in database")
    void shouldNotUpdateImmutableFieldsInDatabase() {
        // Given
        AssetRequest createRequest = createValidAssetRequest("ORIGINAL-SERIAL");
        AssetDTO createdAsset = assetService.createAsset(testUserId, createRequest);
        UUID assetId = UUID.fromString(createdAsset.getId());
        UUID originalCreatedBy = UUID.fromString(createdAsset.getCreatedBy());
        
        AssetRequest updateRequest = createValidAssetRequest("NEW-SERIAL");
        updateRequest.setName("Updated Name");
        
        // When
        assetService.updateAsset(testUserId, assetId, updateRequest);
        
        // Then
        Optional<Asset> savedAsset = assetRepository.findById(assetId);
        assertThat(savedAsset).isPresent();
        assertThat(savedAsset.get().getSerialNumber()).isEqualTo("ORIGINAL-SERIAL");
        assertThat(savedAsset.get().getCreatedBy()).isEqualTo(originalCreatedBy);
        assertThat(savedAsset.get().getId()).isEqualTo(assetId);
    }
    
    @Test
    @DisplayName("Should update updatedBy and updatedAt fields")
    void shouldUpdateAuditFieldsOnUpdate() {
        // Given
        AssetRequest createRequest = createValidAssetRequest("SRV-UPDATE-AUDIT-002");
        AssetDTO createdAsset = assetService.createAsset(testUserId, createRequest);
        UUID assetId = UUID.fromString(createdAsset.getId());
        
        String differentUserId = UUID.randomUUID().toString();
        AssetRequest updateRequest = createValidAssetRequest("SRV-UPDATE-AUDIT-002");
        updateRequest.setName("Updated Name");
        
        // When
        AssetDTO updatedAsset = assetService.updateAsset(differentUserId, assetId, updateRequest);
        
        // Then
        Optional<Asset> savedAsset = assetRepository.findById(assetId);
        assertThat(savedAsset).isPresent();
        assertThat(savedAsset.get().getUpdatedBy()).isEqualTo(UUID.fromString(differentUserId));
        assertThat(savedAsset.get().getUpdatedAt()).isNotNull();
        assertThat(savedAsset.get().getCreatedBy()).isEqualTo(UUID.fromString(testUserId));
    }
    
    @Test
    @DisplayName("Should reject update for retired asset except notes")
    void shouldRejectUpdateForRetiredAsset() {
        // Given
        AssetRequest createRequest = createValidAssetRequest("SRV-RETIRED-001");
        AssetDTO createdAsset = assetService.createAsset(testUserId, createRequest);
        UUID assetId = UUID.fromString(createdAsset.getId());
        
        // Set asset to retired
        Asset asset = assetRepository.findById(assetId).get();
        asset.setStatus(LifecycleStatus.RETIRED);
        asset.setReadOnly(true);
        assetRepository.save(asset);
        
        AssetRequest updateRequest = createValidAssetRequest("SRV-RETIRED-001");
        updateRequest.setName("Trying to update retired asset");
        
        // When/Then
        assertThatThrownBy(() -> assetService.updateAsset(testUserId, assetId, updateRequest))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("read-only");
    }
    
    @Test
    @DisplayName("Should allow notes update for retired asset")
    void shouldAllowNotesUpdateForRetiredAsset() {
        // Given
        AssetRequest createRequest = createValidAssetRequest("SRV-RETIRED-NOTES-001");
        AssetDTO createdAsset = assetService.createAsset(testUserId, createRequest);
        UUID assetId = UUID.fromString(createdAsset.getId());
        
        // Set asset to retired
        Asset asset = assetRepository.findById(assetId).get();
        asset.setStatus(LifecycleStatus.RETIRED);
        asset.setReadOnly(true);
        asset.setNotes("Old notes");
        assetRepository.save(asset);
        
        AssetRequest updateRequest = AssetRequest.builder()
            .assetType(asset.getAssetType())
            .name(asset.getName())
            .serialNumber(asset.getSerialNumber())
            .acquisitionDate(asset.getAcquisitionDate())
            .status(asset.getStatus())
            .notes("Updated notes for retired asset")
            .build();
        
        // When
        AssetDTO updatedAsset = assetService.updateAsset(testUserId, assetId, updateRequest);
        
        // Then
        Optional<Asset> savedAsset = assetRepository.findById(assetId);
        assertThat(savedAsset).isPresent();
        assertThat(savedAsset.get().getNotes()).isEqualTo("Updated notes for retired asset");
    }
    
    @Test
    @DisplayName("Should update location and set locationUpdateDate")
    void shouldUpdateLocationAndSetLocationUpdateDate() {
        // Given
        AssetRequest createRequest = createValidAssetRequest("SRV-LOCATION-001");
        createRequest.setLocation("Old Location");
        AssetDTO createdAsset = assetService.createAsset(testUserId, createRequest);
        UUID assetId = UUID.fromString(createdAsset.getId());
        
        AssetRequest updateRequest = createValidAssetRequest("SRV-LOCATION-001");
        updateRequest.setLocation("New Location");
        
        // When
        assetService.updateAsset(testUserId, assetId, updateRequest);
        
        // Then
        Optional<Asset> savedAsset = assetRepository.findById(assetId);
        assertThat(savedAsset).isPresent();
        assertThat(savedAsset.get().getLocation()).isEqualTo("New Location");
        assertThat(savedAsset.get().getLocationUpdateDate()).isNotNull();
    }
    
    @Test
    @DisplayName("Should update assignedUser and set assignmentDate")
    void shouldUpdateAssignedUserAndSetAssignmentDate() {
        // Given
        AssetRequest createRequest = createValidAssetRequest("SRV-ASSIGN-001");
        createRequest.setAssignedUser("old.user");
        AssetDTO createdAsset = assetService.createAsset(testUserId, createRequest);
        UUID assetId = UUID.fromString(createdAsset.getId());
        
        AssetRequest updateRequest = createValidAssetRequest("SRV-ASSIGN-001");
        updateRequest.setAssignedUser("new.user");
        updateRequest.setAssignedUserEmail("new.user@example.com");
        
        // When
        assetService.updateAsset(testUserId, assetId, updateRequest);
        
        // Then
        Optional<Asset> savedAsset = assetRepository.findById(assetId);
        assertThat(savedAsset).isPresent();
        assertThat(savedAsset.get().getAssignedUser()).isEqualTo("new.user");
        assertThat(savedAsset.get().getAssignmentDate()).isNotNull();
    }
    
    @Test
    @DisplayName("Should rollback transaction on update validation failure")
    void shouldRollbackTransactionOnUpdateValidationFailure() {
        // Given
        AssetRequest createRequest = createValidAssetRequest("SRV-ROLLBACK-UPDATE-001");
        createRequest.setName("Original Name");
        AssetDTO createdAsset = assetService.createAsset(testUserId, createRequest);
        UUID assetId = UUID.fromString(createdAsset.getId());
        
        AssetRequest invalidUpdateRequest = createValidAssetRequest("SRV-ROLLBACK-UPDATE-001");
        invalidUpdateRequest.setName(null); // Invalid
        
        // When
        try {
            assetService.updateAsset(testUserId, assetId, invalidUpdateRequest);
        } catch (ValidationException e) {
            // Expected
        }
        
        // Then - original name should be preserved
        Optional<Asset> savedAsset = assetRepository.findById(assetId);
        assertThat(savedAsset).isPresent();
        assertThat(savedAsset.get().getName()).isEqualTo("Original Name");
    }
    
    @Test
    @DisplayName("Should update multiple fields in single transaction")
    void shouldUpdateMultipleFieldsInSingleTransaction() {
        // Given
        AssetRequest createRequest = createValidAssetRequest("SRV-MULTI-UPDATE-001");
        AssetDTO createdAsset = assetService.createAsset(testUserId, createRequest);
        UUID assetId = UUID.fromString(createdAsset.getId());
        
        AssetRequest updateRequest = AssetRequest.builder()
            .assetType(AssetType.SERVER)
            .name("Updated Name")
            .serialNumber("SRV-MULTI-UPDATE-001")
            .acquisitionDate(LocalDate.now())
            .status(LifecycleStatus.DEPLOYED)
            .location("Updated Location")
            .assignedUser("updated.user")
            .assignedUserEmail("updated.user@example.com")
            .notes("Updated notes")
            .customFields("{\"updated\": \"true\"}")
            .build();
        
        // When
        assetService.updateAsset(testUserId, assetId, updateRequest);
        
        // Then
        Optional<Asset> savedAsset = assetRepository.findById(assetId);
        assertThat(savedAsset).isPresent();
        Asset asset = savedAsset.get();
        assertThat(asset.getName()).isEqualTo("Updated Name");
        assertThat(asset.getStatus()).isEqualTo(LifecycleStatus.DEPLOYED);
        assertThat(asset.getLocation()).isEqualTo("Updated Location");
        assertThat(asset.getAssignedUser()).isEqualTo("updated.user");
        assertThat(asset.getAssignedUserEmail()).isEqualTo("updated.user@example.com");
        assertThat(asset.getNotes()).isEqualTo("Updated notes");
        assertThat(asset.getCustomFields()).isEqualTo("{\"updated\": \"true\"}");
    }
    
    @Test
    @DisplayName("Should throw ResourceNotFoundException for non-existent asset")
    void shouldThrowResourceNotFoundExceptionForNonExistentAsset() {
        // Given
        UUID nonExistentId = UUID.randomUUID();
        AssetRequest updateRequest = createValidAssetRequest("SRV-NOT-FOUND-001");
        
        // When/Then
        assertThatThrownBy(() -> assetService.updateAsset(testUserId, nonExistentId, updateRequest))
            .isInstanceOf(com.company.assetmanagement.exception.ResourceNotFoundException.class)
            .hasMessageContaining(nonExistentId.toString());
    }
    
    @Test
    @DisplayName("Should handle concurrent updates correctly")
    void shouldHandleConcurrentUpdatesCorrectly() {
        // Given
        AssetRequest createRequest = createValidAssetRequest("SRV-CONCURRENT-001");
        AssetDTO createdAsset = assetService.createAsset(testUserId, createRequest);
        UUID assetId = UUID.fromString(createdAsset.getId());
        
        AssetRequest updateRequest1 = createValidAssetRequest("SRV-CONCURRENT-001");
        updateRequest1.setName("Update 1");
        
        AssetRequest updateRequest2 = createValidAssetRequest("SRV-CONCURRENT-001");
        updateRequest2.setName("Update 2");
        
        // When
        assetService.updateAsset(testUserId, assetId, updateRequest1);
        assetService.updateAsset(testUserId, assetId, updateRequest2);
        
        // Then - last update should win
        Optional<Asset> savedAsset = assetRepository.findById(assetId);
        assertThat(savedAsset).isPresent();
        assertThat(savedAsset.get().getName()).isEqualTo("Update 2");
    }
    
    // ========== Status Management Integration Tests (updateStatus) ==========
    
    @Test
    @DisplayName("Should update status and persist to database")
    void shouldUpdateStatusAndPersistToDatabase() {
        // Given
        AssetRequest createRequest = createValidAssetRequest("SRV-STATUS-UPDATE-001");
        createRequest.setStatus(LifecycleStatus.ORDERED);
        AssetDTO createdAsset = assetService.createAsset(testUserId, createRequest);
        UUID assetId = UUID.fromString(createdAsset.getId());
        
        // When
        AssetDTO updatedAsset = assetService.updateStatus(testUserId, assetId, LifecycleStatus.RECEIVED);
        
        // Then
        Optional<Asset> savedAsset = assetRepository.findById(assetId);
        assertThat(savedAsset).isPresent();
        assertThat(savedAsset.get().getStatus()).isEqualTo(LifecycleStatus.RECEIVED);
        assertThat(updatedAsset.getStatus()).isEqualTo(LifecycleStatus.RECEIVED);
    }
    
    @Test
    @DisplayName("Should set readOnly flag when status becomes RETIRED")
    void shouldSetReadOnlyFlagWhenRetired() {
        // Given
        AssetRequest createRequest = createValidAssetRequest("SRV-RETIRE-001");
        createRequest.setStatus(LifecycleStatus.IN_USE);
        AssetDTO createdAsset = assetService.createAsset(testUserId, createRequest);
        UUID assetId = UUID.fromString(createdAsset.getId());
        
        // When
        AssetDTO updatedAsset = assetService.updateStatus(testUserId, assetId, LifecycleStatus.RETIRED);
        
        // Then
        Optional<Asset> savedAsset = assetRepository.findById(assetId);
        assertThat(savedAsset).isPresent();
        assertThat(savedAsset.get().isReadOnly()).isTrue();
        assertThat(updatedAsset.isReadOnly()).isTrue();
    }
    
    @Test
    @DisplayName("Should create audit log entry on status update")
    void shouldCreateAuditLogEntryOnStatusUpdate() {
        // Given
        AssetRequest createRequest = createValidAssetRequest("SRV-STATUS-AUDIT-001");
        createRequest.setStatus(LifecycleStatus.ORDERED);
        AssetDTO createdAsset = assetService.createAsset(testUserId, createRequest);
        UUID assetId = UUID.fromString(createdAsset.getId());
        
        // When
        assetService.updateStatus(testUserId, assetId, LifecycleStatus.RECEIVED);
        
        // Then
        List<AuditLog> auditLogs = auditLogRepository.findByResourceId(createdAsset.getId());
        assertThat(auditLogs).hasSizeGreaterThan(1); // CREATE + STATUS_UPDATE
        assertThat(auditLogs).anySatisfy(log -> {
            assertThat(log.getActionType().name()).isEqualTo("UPDATE_ASSET");
        });
    }
    
    @Test
    @DisplayName("Should reject invalid status transition")
    void shouldRejectInvalidStatusTransition() {
        // Given
        AssetRequest createRequest = createValidAssetRequest("SRV-INVALID-TRANSITION-001");
        createRequest.setStatus(LifecycleStatus.ORDERED);
        AssetDTO createdAsset = assetService.createAsset(testUserId, createRequest);
        UUID assetId = UUID.fromString(createdAsset.getId());
        
        // When/Then - ORDERED cannot transition to IN_USE directly
        assertThatThrownBy(() -> assetService.updateStatus(testUserId, assetId, LifecycleStatus.IN_USE))
            .isInstanceOf(com.company.assetmanagement.exception.InvalidStatusTransitionException.class)
            .hasMessageContaining("ORDERED")
            .hasMessageContaining("IN_USE");
        
        // Verify status was not changed
        Optional<Asset> savedAsset = assetRepository.findById(assetId);
        assertThat(savedAsset).isPresent();
        assertThat(savedAsset.get().getStatus()).isEqualTo(LifecycleStatus.ORDERED);
    }
    
    @Test
    @DisplayName("Should not allow transitions from RETIRED status")
    void shouldNotAllowTransitionsFromRetired() {
        // Given
        AssetRequest createRequest = createValidAssetRequest("SRV-RETIRED-TRANSITION-001");
        createRequest.setStatus(LifecycleStatus.IN_USE);
        AssetDTO createdAsset = assetService.createAsset(testUserId, createRequest);
        UUID assetId = UUID.fromString(createdAsset.getId());
        
        // Retire the asset
        assetService.updateStatus(testUserId, assetId, LifecycleStatus.RETIRED);
        
        // When/Then - RETIRED cannot transition to any status
        assertThatThrownBy(() -> assetService.updateStatus(testUserId, assetId, LifecycleStatus.STORAGE))
            .isInstanceOf(com.company.assetmanagement.exception.InvalidStatusTransitionException.class)
            .hasMessageContaining("RETIRED");
        
        // Verify status remains RETIRED
        Optional<Asset> savedAsset = assetRepository.findById(assetId);
        assertThat(savedAsset).isPresent();
        assertThat(savedAsset.get().getStatus()).isEqualTo(LifecycleStatus.RETIRED);
    }
    
    @Test
    @DisplayName("Should allow complete lifecycle progression")
    void shouldAllowCompleteLifecycleProgression() {
        // Given
        AssetRequest createRequest = createValidAssetRequest("SRV-LIFECYCLE-001");
        createRequest.setStatus(LifecycleStatus.ORDERED);
        AssetDTO createdAsset = assetService.createAsset(testUserId, createRequest);
        UUID assetId = UUID.fromString(createdAsset.getId());
        
        // When - Progress through lifecycle
        assetService.updateStatus(testUserId, assetId, LifecycleStatus.RECEIVED);
        assetService.updateStatus(testUserId, assetId, LifecycleStatus.DEPLOYED);
        assetService.updateStatus(testUserId, assetId, LifecycleStatus.IN_USE);
        assetService.updateStatus(testUserId, assetId, LifecycleStatus.STORAGE);
        assetService.updateStatus(testUserId, assetId, LifecycleStatus.RETIRED);
        
        // Then
        Optional<Asset> savedAsset = assetRepository.findById(assetId);
        assertThat(savedAsset).isPresent();
        assertThat(savedAsset.get().getStatus()).isEqualTo(LifecycleStatus.RETIRED);
        assertThat(savedAsset.get().isReadOnly()).isTrue();
    }
    
    @Test
    @DisplayName("Should allow transition to MAINTENANCE from any status")
    void shouldAllowTransitionToMaintenanceFromAnyStatus() {
        // Test from ORDERED
        AssetRequest orderedRequest = createValidAssetRequest("SRV-MAINT-ORDERED");
        orderedRequest.setStatus(LifecycleStatus.ORDERED);
        AssetDTO orderedAsset = assetService.createAsset(testUserId, orderedRequest);
        UUID orderedId = UUID.fromString(orderedAsset.getId());
        
        assetService.updateStatus(testUserId, orderedId, LifecycleStatus.MAINTENANCE);
        Optional<Asset> orderedSaved = assetRepository.findById(orderedId);
        assertThat(orderedSaved).isPresent();
        assertThat(orderedSaved.get().getStatus()).isEqualTo(LifecycleStatus.MAINTENANCE);
        
        // Test from IN_USE
        AssetRequest inUseRequest = createValidAssetRequest("SRV-MAINT-INUSE");
        inUseRequest.setStatus(LifecycleStatus.IN_USE);
        AssetDTO inUseAsset = assetService.createAsset(testUserId, inUseRequest);
        UUID inUseId = UUID.fromString(inUseAsset.getId());
        
        assetService.updateStatus(testUserId, inUseId, LifecycleStatus.MAINTENANCE);
        Optional<Asset> inUseSaved = assetRepository.findById(inUseId);
        assertThat(inUseSaved).isPresent();
        assertThat(inUseSaved.get().getStatus()).isEqualTo(LifecycleStatus.MAINTENANCE);
    }
    
    @Test
    @DisplayName("Should allow transition from MAINTENANCE to any status except RETIRED")
    void shouldAllowTransitionFromMaintenanceExceptRetired() {
        // Given
        AssetRequest createRequest = createValidAssetRequest("SRV-MAINT-RETURN-001");
        createRequest.setStatus(LifecycleStatus.IN_USE);
        AssetDTO createdAsset = assetService.createAsset(testUserId, createRequest);
        UUID assetId = UUID.fromString(createdAsset.getId());
        
        // Move to MAINTENANCE
        assetService.updateStatus(testUserId, assetId, LifecycleStatus.MAINTENANCE);
        
        // When - Return to DEPLOYED
        assetService.updateStatus(testUserId, assetId, LifecycleStatus.DEPLOYED);
        
        // Then
        Optional<Asset> savedAsset = assetRepository.findById(assetId);
        assertThat(savedAsset).isPresent();
        assertThat(savedAsset.get().getStatus()).isEqualTo(LifecycleStatus.DEPLOYED);
    }
    
    @Test
    @DisplayName("Should not allow transition from MAINTENANCE to RETIRED")
    void shouldNotAllowMaintenanceToRetired() {
        // Given
        AssetRequest createRequest = createValidAssetRequest("SRV-MAINT-RETIRE-001");
        createRequest.setStatus(LifecycleStatus.MAINTENANCE);
        AssetDTO createdAsset = assetService.createAsset(testUserId, createRequest);
        UUID assetId = UUID.fromString(createdAsset.getId());
        
        // When/Then
        assertThatThrownBy(() -> assetService.updateStatus(testUserId, assetId, LifecycleStatus.RETIRED))
            .isInstanceOf(com.company.assetmanagement.exception.InvalidStatusTransitionException.class)
            .hasMessageContaining("MAINTENANCE")
            .hasMessageContaining("RETIRED");
    }
    
    @Test
    @DisplayName("Should update updatedBy field on status change")
    void shouldUpdateUpdatedByFieldOnStatusChange() {
        // Given
        AssetRequest createRequest = createValidAssetRequest("SRV-STATUS-UPDATEDBY-001");
        createRequest.setStatus(LifecycleStatus.ORDERED);
        AssetDTO createdAsset = assetService.createAsset(testUserId, createRequest);
        UUID assetId = UUID.fromString(createdAsset.getId());
        
        String differentUserId = UUID.randomUUID().toString();
        
        // When
        assetService.updateStatus(differentUserId, assetId, LifecycleStatus.RECEIVED);
        
        // Then
        Optional<Asset> savedAsset = assetRepository.findById(assetId);
        assertThat(savedAsset).isPresent();
        assertThat(savedAsset.get().getUpdatedBy()).isEqualTo(UUID.fromString(differentUserId));
    }
    
    @Test
    @DisplayName("Should rollback transaction on invalid status transition")
    void shouldRollbackTransactionOnInvalidTransition() {
        // Given
        AssetRequest createRequest = createValidAssetRequest("SRV-ROLLBACK-STATUS-001");
        createRequest.setStatus(LifecycleStatus.ORDERED);
        AssetDTO createdAsset = assetService.createAsset(testUserId, createRequest);
        UUID assetId = UUID.fromString(createdAsset.getId());
        
        // When
        try {
            assetService.updateStatus(testUserId, assetId, LifecycleStatus.IN_USE);
        } catch (com.company.assetmanagement.exception.InvalidStatusTransitionException e) {
            // Expected
        }
        
        // Then - status should remain ORDERED
        Optional<Asset> savedAsset = assetRepository.findById(assetId);
        assertThat(savedAsset).isPresent();
        assertThat(savedAsset.get().getStatus()).isEqualTo(LifecycleStatus.ORDERED);
        assertThat(savedAsset.get().isReadOnly()).isFalse();
    }
    
    @Test
    @DisplayName("Should handle multiple status updates in sequence")
    void shouldHandleMultipleStatusUpdatesInSequence() {
        // Given
        AssetRequest createRequest = createValidAssetRequest("SRV-MULTI-STATUS-001");
        createRequest.setStatus(LifecycleStatus.ORDERED);
        AssetDTO createdAsset = assetService.createAsset(testUserId, createRequest);
        UUID assetId = UUID.fromString(createdAsset.getId());
        
        // When
        assetService.updateStatus(testUserId, assetId, LifecycleStatus.RECEIVED);
        assetService.updateStatus(testUserId, assetId, LifecycleStatus.DEPLOYED);
        assetService.updateStatus(testUserId, assetId, LifecycleStatus.STORAGE);
        
        // Then
        Optional<Asset> savedAsset = assetRepository.findById(assetId);
        assertThat(savedAsset).isPresent();
        assertThat(savedAsset.get().getStatus()).isEqualTo(LifecycleStatus.STORAGE);
        
        // Verify audit logs for all transitions
        List<AuditLog> auditLogs = auditLogRepository.findByResourceId(createdAsset.getId());
        assertThat(auditLogs).hasSizeGreaterThanOrEqualTo(4); // CREATE + 3 STATUS_UPDATES
    }
    
    @Test
    @DisplayName("Should throw ResourceNotFoundException for non-existent asset")
    void shouldThrowResourceNotFoundExceptionForStatusUpdate() {
        // Given
        UUID nonExistentId = UUID.randomUUID();
        
        // When/Then
        assertThatThrownBy(() -> assetService.updateStatus(testUserId, nonExistentId, LifecycleStatus.RECEIVED))
            .isInstanceOf(com.company.assetmanagement.exception.ResourceNotFoundException.class)
            .hasMessageContaining(nonExistentId.toString());
    }
    
    @Test
    @DisplayName("Should verify readOnly flag persists after retirement")
    void shouldVerifyReadOnlyFlagPersistsAfterRetirement() {
        // Given
        AssetRequest createRequest = createValidAssetRequest("SRV-READONLY-PERSIST-001");
        createRequest.setStatus(LifecycleStatus.IN_USE);
        AssetDTO createdAsset = assetService.createAsset(testUserId, createRequest);
        UUID assetId = UUID.fromString(createdAsset.getId());
        
        // When
        assetService.updateStatus(testUserId, assetId, LifecycleStatus.RETIRED);
        
        // Then - Verify readOnly persists across retrieval
        Optional<AssetDTO> retrievedAsset = assetService.getAsset(assetId);
        assertThat(retrievedAsset).isPresent();
        assertThat(retrievedAsset.get().isReadOnly()).isTrue();
        
        Optional<Asset> savedAsset = assetRepository.findById(assetId);
        assertThat(savedAsset).isPresent();
        assertThat(savedAsset.get().isReadOnly()).isTrue();
    }
    
    @Test
    @DisplayName("Should not set readOnly for non-RETIRED status transitions")
    void shouldNotSetReadOnlyForNonRetiredTransitions() {
        // Given
        AssetRequest createRequest = createValidAssetRequest("SRV-NOT-READONLY-001");
        createRequest.setStatus(LifecycleStatus.ORDERED);
        AssetDTO createdAsset = assetService.createAsset(testUserId, createRequest);
        UUID assetId = UUID.fromString(createdAsset.getId());
        
        // When
        assetService.updateStatus(testUserId, assetId, LifecycleStatus.RECEIVED);
        assetService.updateStatus(testUserId, assetId, LifecycleStatus.DEPLOYED);
        assetService.updateStatus(testUserId, assetId, LifecycleStatus.IN_USE);
        
        // Then
        Optional<Asset> savedAsset = assetRepository.findById(assetId);
        assertThat(savedAsset).isPresent();
        assertThat(savedAsset.get().isReadOnly()).isFalse();
    }
    
    // ========== Delete Asset Integration Tests ==========
    
    @Test
    @DisplayName("Should delete asset from database")
    void shouldDeleteAssetFromDatabase() {
        // Given
        AssetRequest createRequest = createValidAssetRequest("SRV-DELETE-001");
        AssetDTO createdAsset = assetService.createAsset(testUserId, createRequest);
        UUID assetId = UUID.fromString(createdAsset.getId());
        
        // Verify asset exists
        assertThat(assetRepository.findById(assetId)).isPresent();
        
        // When
        assetService.deleteAsset(testUserId, assetId);
        
        // Then
        Optional<Asset> deletedAsset = assetRepository.findById(assetId);
        assertThat(deletedAsset).isEmpty();
    }
    
    @Test
    @DisplayName("Should create audit log entry when deleting asset")
    void shouldCreateAuditLogEntryWhenDeletingAsset() {
        // Given
        AssetRequest createRequest = createValidAssetRequest("SRV-DELETE-AUDIT-001");
        AssetDTO createdAsset = assetService.createAsset(testUserId, createRequest);
        UUID assetId = UUID.fromString(createdAsset.getId());
        
        // Clear audit logs from creation
        auditLogRepository.deleteAll();
        
        // When
        assetService.deleteAsset(testUserId, assetId);
        
        // Then
        List<AuditLog> auditLogs = auditLogRepository.findAll();
        assertThat(auditLogs).hasSize(1);
        
        AuditLog auditLog = auditLogs.get(0);
        assertThat(auditLog.getUserId()).isEqualTo(UUID.fromString(testUserId));
        assertThat(auditLog.getActionType()).isEqualTo(com.company.assetmanagement.model.Action.DELETE_ASSET);
        assertThat(auditLog.getResourceType()).isEqualTo("ASSET");
        assertThat(auditLog.getResourceId()).isEqualTo(assetId.toString());
    }
    
    @Test
    @DisplayName("Should throw ResourceNotFoundException when deleting non-existent asset")
    void shouldThrowResourceNotFoundExceptionWhenDeletingNonExistentAsset() {
        // Given
        UUID nonExistentAssetId = UUID.randomUUID();
        
        // When/Then
        assertThatThrownBy(() -> assetService.deleteAsset(testUserId, nonExistentAssetId))
            .isInstanceOf(com.company.assetmanagement.exception.ResourceNotFoundException.class)
            .hasMessageContaining("Asset")
            .hasMessageContaining(nonExistentAssetId.toString());
    }
    
    @Test
    @DisplayName("Should allow deleting asset in any lifecycle status")
    void shouldAllowDeletingAssetInAnyLifecycleStatus() {
        // Given - Create asset in RETIRED status
        AssetRequest createRequest = createValidAssetRequest("SRV-DELETE-RETIRED-001");
        createRequest.setStatus(LifecycleStatus.ORDERED);
        AssetDTO createdAsset = assetService.createAsset(testUserId, createRequest);
        UUID assetId = UUID.fromString(createdAsset.getId());
        
        // Transition to RETIRED
        assetService.updateStatus(testUserId, assetId, LifecycleStatus.RECEIVED);
        assetService.updateStatus(testUserId, assetId, LifecycleStatus.DEPLOYED);
        assetService.updateStatus(testUserId, assetId, LifecycleStatus.IN_USE);
        assetService.updateStatus(testUserId, assetId, LifecycleStatus.RETIRED);
        
        // When - Delete retired asset
        assertThatCode(() -> assetService.deleteAsset(testUserId, assetId))
            .doesNotThrowAnyException();
        
        // Then
        Optional<Asset> deletedAsset = assetRepository.findById(assetId);
        assertThat(deletedAsset).isEmpty();
    }
    
    @Test
    @DisplayName("Should allow deleting asset with assigned user")
    void shouldAllowDeletingAssetWithAssignedUser() {
        // Given - Create asset with assigned user
        AssetRequest createRequest = createValidAssetRequest("SRV-DELETE-ASSIGNED-001");
        createRequest.setAssignedUser("John Doe");
        createRequest.setAssignedUserEmail("john.doe@example.com");
        AssetDTO createdAsset = assetService.createAsset(testUserId, createRequest);
        UUID assetId = UUID.fromString(createdAsset.getId());
        
        // When
        assertThatCode(() -> assetService.deleteAsset(testUserId, assetId))
            .doesNotThrowAnyException();
        
        // Then
        Optional<Asset> deletedAsset = assetRepository.findById(assetId);
        assertThat(deletedAsset).isEmpty();
    }
    
    @Test
    @DisplayName("Should handle transaction rollback on deletion failure")
    void shouldHandleTransactionRollbackOnDeletionFailure() {
        // Given
        AssetRequest createRequest = createValidAssetRequest("SRV-DELETE-ROLLBACK-001");
        AssetDTO createdAsset = assetService.createAsset(testUserId, createRequest);
        UUID assetId = UUID.fromString(createdAsset.getId());
        
        // When - Try to delete with invalid user ID (should fail validation)
        assertThatThrownBy(() -> assetService.deleteAsset(null, assetId))
            .isInstanceOf(IllegalArgumentException.class);
        
        // Then - Asset should still exist (transaction rolled back)
        Optional<Asset> asset = assetRepository.findById(assetId);
        assertThat(asset).isPresent();
    }
    
    @Test
    @DisplayName("Should delete multiple assets independently")
    void shouldDeleteMultipleAssetsIndependently() {
        // Given - Create multiple assets
        AssetRequest request1 = createValidAssetRequest("SRV-DELETE-MULTI-001");
        AssetRequest request2 = createValidAssetRequest("SRV-DELETE-MULTI-002");
        AssetRequest request3 = createValidAssetRequest("SRV-DELETE-MULTI-003");
        
        AssetDTO asset1 = assetService.createAsset(testUserId, request1);
        AssetDTO asset2 = assetService.createAsset(testUserId, request2);
        AssetDTO asset3 = assetService.createAsset(testUserId, request3);
        
        UUID assetId1 = UUID.fromString(asset1.getId());
        UUID assetId2 = UUID.fromString(asset2.getId());
        UUID assetId3 = UUID.fromString(asset3.getId());
        
        // When - Delete first and third assets
        assetService.deleteAsset(testUserId, assetId1);
        assetService.deleteAsset(testUserId, assetId3);
        
        // Then - First and third should be deleted, second should remain
        assertThat(assetRepository.findById(assetId1)).isEmpty();
        assertThat(assetRepository.findById(assetId2)).isPresent();
        assertThat(assetRepository.findById(assetId3)).isEmpty();
    }
    
    // ========== Helper Methods ==========
    
    /**
     * Creates a valid asset request for testing with the specified serial number.
     */
    private AssetRequest createValidAssetRequest(String serialNumber) {
        return AssetRequest.builder()
            .assetType(AssetType.SERVER)
            .name("Integration Test Server")
            .serialNumber(serialNumber)
            .acquisitionDate(LocalDate.now())
            .status(LifecycleStatus.ORDERED)
            .build();
    }
}
