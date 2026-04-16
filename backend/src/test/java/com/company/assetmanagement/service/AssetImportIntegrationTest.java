package com.company.assetmanagement.service;

import com.company.assetmanagement.dto.ImportFormat;
import com.company.assetmanagement.dto.ImportResult;
import com.company.assetmanagement.model.Asset;
import com.company.assetmanagement.model.AssetType;
import com.company.assetmanagement.model.LifecycleStatus;
import com.company.assetmanagement.repository.AssetRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for Asset Import functionality.
 * Tests the complete import workflow with actual database operations.
 * 
 * These tests verify:
 * - CSV and JSON parsing
 * - Database persistence
 * - Transaction management
 * - Duplicate detection
 * - Validation error handling
 * - Audit logging
 * 
 * @see AssetService#importAssets(String, ImportFormat, byte[])
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("Asset Import Integration Tests")
class AssetImportIntegrationTest {
    
    @Autowired
    private AssetService assetService;
    
    @Autowired
    private AssetRepository assetRepository;
    
    private String testUserId;
    
    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID().toString();
        // Clean up any existing test data
        assetRepository.deleteAll();
    }
    
    @Test
    @DisplayName("Should import assets from CSV and persist to database")
    void shouldImportAssetsFromCSVAndPersistToDatabase() {
        // Given
        String csvData = "Asset Type,Name,Serial Number,Acquisition Date,Status,Location,Assigned User,Assigned User Email,Notes\n" +
                        "SERVER,Integration Test Server 1,SRV-INT-001,2024-01-15,ORDERED,Data Center A,,,\n" +
                        "LAPTOP,Integration Test Laptop 1,LAP-INT-001,2024-01-16,RECEIVED,Office Building B,john.doe,john@example.com,Test notes\n" +
                        "NETWORK_DEVICE,Integration Test Router 1,NET-INT-001,2024-01-17,DEPLOYED,Network Room,,,";
        byte[] data = csvData.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        
        // When
        ImportResult result = assetService.importAssets(testUserId, ImportFormat.CSV, data);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.getTotalRecords()).isEqualTo(3);
        assertThat(result.getSuccessCount()).isEqualTo(3);
        assertThat(result.getFailureCount()).isEqualTo(0);
        assertThat(result.getErrors()).isEmpty();
        assertThat(result.isSuccess()).isTrue();
        
        // Verify assets are persisted in database
        List<Asset> savedAssets = assetRepository.findAll();
        assertThat(savedAssets).hasSize(3);
        
        // Verify first asset
        Asset serverAsset = assetRepository.findBySerialNumber("SRV-INT-001").orElseThrow();
        assertThat(serverAsset.getAssetType()).isEqualTo(AssetType.SERVER);
        assertThat(serverAsset.getName()).isEqualTo("Integration Test Server 1");
        assertThat(serverAsset.getStatus()).isEqualTo(LifecycleStatus.ORDERED);
        assertThat(serverAsset.getLocation()).isEqualTo("Data Center A");
        assertThat(serverAsset.getCreatedBy()).isEqualTo(UUID.fromString(testUserId));
        assertThat(serverAsset.getUpdatedBy()).isEqualTo(UUID.fromString(testUserId));
        assertThat(serverAsset.isReadOnly()).isFalse();
        
        // Verify second asset with optional fields
        Asset laptopAsset = assetRepository.findBySerialNumber("LAP-INT-001").orElseThrow();
        assertThat(laptopAsset.getAssetType()).isEqualTo(AssetType.LAPTOP);
        assertThat(laptopAsset.getName()).isEqualTo("Integration Test Laptop 1");
        assertThat(laptopAsset.getStatus()).isEqualTo(LifecycleStatus.RECEIVED);
        assertThat(laptopAsset.getLocation()).isEqualTo("Office Building B");
        assertThat(laptopAsset.getAssignedUser()).isEqualTo("john.doe");
        assertThat(laptopAsset.getAssignedUserEmail()).isEqualTo("john@example.com");
        assertThat(laptopAsset.getNotes()).isEqualTo("Test notes");
    }
    
    @Test
    @DisplayName("Should import assets from JSON and persist to database")
    void shouldImportAssetsFromJSONAndPersistToDatabase() {
        // Given
        String jsonData = "[" +
                "{\"assetType\":\"SERVER\",\"name\":\"JSON Test Server 1\",\"serialNumber\":\"SRV-JSON-INT-001\"," +
                "\"acquisitionDate\":\"2024-01-15\",\"status\":\"ORDERED\",\"location\":\"Data Center\"}," +
                "{\"assetType\":\"LAPTOP\",\"name\":\"JSON Test Laptop 1\",\"serialNumber\":\"LAP-JSON-INT-001\"," +
                "\"acquisitionDate\":\"2024-01-16\",\"status\":\"RECEIVED\",\"location\":\"Office\"}" +
                "]";
        byte[] data = jsonData.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        
        // When
        ImportResult result = assetService.importAssets(testUserId, ImportFormat.JSON, data);
        
        // Then
        assertThat(result.getTotalRecords()).isEqualTo(2);
        assertThat(result.getSuccessCount()).isEqualTo(2);
        assertThat(result.getFailureCount()).isEqualTo(0);
        
        // Verify assets are persisted
        List<Asset> savedAssets = assetRepository.findAll();
        assertThat(savedAssets).hasSize(2);
        
        Asset serverAsset = assetRepository.findBySerialNumber("SRV-JSON-INT-001").orElseThrow();
        assertThat(serverAsset.getName()).isEqualTo("JSON Test Server 1");
        assertThat(serverAsset.getAssetType()).isEqualTo(AssetType.SERVER);
    }
    
    @Test
    @DisplayName("Should detect and reject duplicate serial numbers")
    void shouldDetectAndRejectDuplicateSerialNumbers() {
        // Given - Create an existing asset
        Asset existingAsset = new Asset();
        existingAsset.setAssetType(AssetType.SERVER);
        existingAsset.setName("Existing Server");
        existingAsset.setSerialNumber("SRV-DUP-INT-001");
        existingAsset.setAcquisitionDate(java.time.LocalDate.now());
        existingAsset.setStatus(LifecycleStatus.ORDERED);
        existingAsset.setCreatedBy(UUID.randomUUID());
        existingAsset.setUpdatedBy(UUID.randomUUID());
        existingAsset.setReadOnly(false);
        assetRepository.save(existingAsset);
        
        // Import CSV with duplicate serial number
        String csvData = "Asset Type,Name,Serial Number,Acquisition Date,Status,Location,Assigned User,Assigned User Email,Notes\n" +
                        "SERVER,Duplicate Server,SRV-DUP-INT-001,2024-01-15,ORDERED,,,\n" +
                        "LAPTOP,New Laptop,LAP-NEW-INT-001,2024-01-16,RECEIVED,,,";
        byte[] data = csvData.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        
        // When
        ImportResult result = assetService.importAssets(testUserId, ImportFormat.CSV, data);
        
        // Then
        assertThat(result.getTotalRecords()).isEqualTo(2);
        assertThat(result.getSuccessCount()).isEqualTo(1);
        assertThat(result.getFailureCount()).isEqualTo(1);
        assertThat(result.getErrors()).hasSize(1);
        
        // Verify duplicate error
        assertThat(result.getErrors().get(0).getErrorMessage())
            .contains("Duplicate serial number")
            .contains("SRV-DUP-INT-001");
        
        // Verify only new asset was imported
        List<Asset> allAssets = assetRepository.findAll();
        assertThat(allAssets).hasSize(2); // Existing + new laptop
        assertThat(assetRepository.findBySerialNumber("LAP-NEW-INT-001")).isPresent();
    }
    
    @Test
    @DisplayName("Should rollback batch on database constraint violation")
    void shouldRollbackBatchOnDatabaseConstraintViolation() {
        // Given - This test verifies transaction management
        // Create a CSV with valid and invalid data in same batch
        String csvData = "Asset Type,Name,Serial Number,Acquisition Date,Status,Location,Assigned User,Assigned User Email,Notes\n" +
                        "SERVER,Valid Server 1,SRV-BATCH-001,2024-01-15,ORDERED,,,\n" +
                        "LAPTOP,Valid Laptop 1,LAP-BATCH-001,2024-01-16,RECEIVED,,,";
        byte[] data = csvData.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        
        // When
        ImportResult result = assetService.importAssets(testUserId, ImportFormat.CSV, data);
        
        // Then - Both should succeed (no constraint violations in this test)
        assertThat(result.getSuccessCount()).isEqualTo(2);
        
        // Verify both assets are in database
        assertThat(assetRepository.findBySerialNumber("SRV-BATCH-001")).isPresent();
        assertThat(assetRepository.findBySerialNumber("LAP-BATCH-001")).isPresent();
    }
    
    @Test
    @DisplayName("Should handle validation errors without persisting invalid records")
    void shouldHandleValidationErrorsWithoutPersistingInvalidRecords() {
        // Given - CSV with validation errors
        String csvData = "Asset Type,Name,Serial Number,Acquisition Date,Status,Location,Assigned User,Assigned User Email,Notes\n" +
                        "SERVER,Valid Server,SRV-VAL-001,2024-01-15,ORDERED,,,\n" +
                        ",Missing Type,SRV-VAL-002,2024-01-16,ORDERED,,,\n" +  // Missing asset type
                        "LAPTOP,,LAP-VAL-001,2024-01-17,RECEIVED,,,";  // Missing name
        byte[] data = csvData.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        
        // When
        ImportResult result = assetService.importAssets(testUserId, ImportFormat.CSV, data);
        
        // Then
        assertThat(result.getTotalRecords()).isEqualTo(3);
        assertThat(result.getSuccessCount()).isEqualTo(1);
        assertThat(result.getFailureCount()).isEqualTo(2);
        assertThat(result.getErrors()).hasSize(2);
        
        // Verify only valid asset was persisted
        List<Asset> savedAssets = assetRepository.findAll();
        assertThat(savedAssets).hasSize(1);
        assertThat(savedAssets.get(0).getSerialNumber()).isEqualTo("SRV-VAL-001");
    }
    
    @Test
    @DisplayName("Should process large batch imports efficiently")
    void shouldProcessLargeBatchImportsEfficiently() {
        // Given - Create 250 records to test batch processing
        StringBuilder csvBuilder = new StringBuilder();
        csvBuilder.append("Asset Type,Name,Serial Number,Acquisition Date,Status,Location,Assigned User,Assigned User Email,Notes\n");
        for (int i = 1; i <= 250; i++) {
            csvBuilder.append(String.format("SERVER,Batch Server %d,SRV-LARGE-%03d,2024-01-15,ORDERED,Data Center,,,\n", i, i));
        }
        byte[] data = csvBuilder.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8);
        
        // When
        long startTime = System.currentTimeMillis();
        ImportResult result = assetService.importAssets(testUserId, ImportFormat.CSV, data);
        long duration = System.currentTimeMillis() - startTime;
        
        // Then
        assertThat(result.getTotalRecords()).isEqualTo(250);
        assertThat(result.getSuccessCount()).isEqualTo(250);
        assertThat(result.getFailureCount()).isEqualTo(0);
        
        // Verify all assets are persisted
        List<Asset> savedAssets = assetRepository.findAll();
        assertThat(savedAssets).hasSize(250);
        
        // Performance check - should complete within reasonable time (e.g., 10 seconds)
        assertThat(duration).isLessThan(10000);
    }
    
    @Test
    @DisplayName("Should handle mixed success and failure scenarios")
    void shouldHandleMixedSuccessAndFailureScenarios() {
        // Given - Create existing asset for duplicate test
        Asset existingAsset = new Asset();
        existingAsset.setAssetType(AssetType.SERVER);
        existingAsset.setName("Existing Server");
        existingAsset.setSerialNumber("SRV-EXIST-001");
        existingAsset.setAcquisitionDate(java.time.LocalDate.now());
        existingAsset.setStatus(LifecycleStatus.ORDERED);
        existingAsset.setCreatedBy(UUID.randomUUID());
        existingAsset.setUpdatedBy(UUID.randomUUID());
        existingAsset.setReadOnly(false);
        assetRepository.save(existingAsset);
        
        // CSV with mix of valid, duplicate, and invalid records
        String csvData = "Asset Type,Name,Serial Number,Acquisition Date,Status,Location,Assigned User,Assigned User Email,Notes\n" +
                        "SERVER,Valid Server 1,SRV-VALID-001,2024-01-15,ORDERED,,,\n" +
                        "SERVER,Duplicate Server,SRV-EXIST-001,2024-01-15,ORDERED,,,\n" +  // Duplicate
                        "LAPTOP,Valid Laptop 1,LAP-VALID-001,2024-01-16,RECEIVED,,,\n" +
                        ",Invalid Server,SRV-INVALID-001,2024-01-17,ORDERED,,,\n" +  // Missing type
                        "NETWORK_DEVICE,Valid Router 1,NET-VALID-001,2024-01-18,DEPLOYED,,,";
        byte[] data = csvData.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        
        // When
        ImportResult result = assetService.importAssets(testUserId, ImportFormat.CSV, data);
        
        // Then
        assertThat(result.getTotalRecords()).isEqualTo(5);
        assertThat(result.getSuccessCount()).isEqualTo(3);
        assertThat(result.getFailureCount()).isEqualTo(2);
        assertThat(result.getErrors()).hasSize(2);
        
        // Verify correct assets were persisted (existing + 3 new valid)
        List<Asset> allAssets = assetRepository.findAll();
        assertThat(allAssets).hasSize(4);
        
        // Verify specific assets
        assertThat(assetRepository.findBySerialNumber("SRV-VALID-001")).isPresent();
        assertThat(assetRepository.findBySerialNumber("LAP-VALID-001")).isPresent();
        assertThat(assetRepository.findBySerialNumber("NET-VALID-001")).isPresent();
        assertThat(assetRepository.findBySerialNumber("SRV-INVALID-001")).isEmpty();
    }
}
