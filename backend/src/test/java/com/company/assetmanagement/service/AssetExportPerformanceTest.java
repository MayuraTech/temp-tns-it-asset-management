package com.company.assetmanagement.service;

import com.company.assetmanagement.dto.ExportFormat;
import com.company.assetmanagement.dto.ExportResult;
import com.company.assetmanagement.dto.AssetSearchQuery;
import com.company.assetmanagement.model.Asset;
import com.company.assetmanagement.model.AssetType;
import com.company.assetmanagement.model.LifecycleStatus;
import com.company.assetmanagement.repository.AssetRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Performance tests for asset export functionality.
 * 
 * <p>Tests verify that export operations meet performance requirements:
 * <ul>
 *   <li>Export must complete within 30 seconds for 100,000 assets</li>
 *   <li>Memory usage should be reasonable for large datasets</li>
 *   <li>Both CSV and JSON formats should meet performance targets</li>
 * </ul>
 * 
 * <p><strong>Note:</strong> These tests use mocked data to simulate large datasets
 * without requiring actual database setup. For true integration performance testing,
 * use a separate integration test suite with a populated database.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Asset Export Performance Tests")
class AssetExportPerformanceTest {
    
    @Mock
    private AssetRepository assetRepository;
    
    @Mock
    private AuditService auditService;
    
    @Mock
    private AssetValidationService validationService;
    
    @InjectMocks
    private AssetServiceImpl assetService;
    
    private static final int LARGE_DATASET_SIZE = 100_000;
    private static final int PERFORMANCE_THRESHOLD_MS = 30_000; // 30 seconds
    
    @BeforeEach
    void setUp() {
        // No specific setup needed for performance tests
    }
    
    @Test
    @DisplayName("Should export 100,000 assets to CSV within 30 seconds")
    void shouldExportLargeDatasetToCsvWithinTimeLimit() {
        // Given
        ExportFormat format = ExportFormat.CSV;
        AssetSearchQuery query = null;
        
        List<Asset> largeDataset = createLargeAssetDataset(LARGE_DATASET_SIZE);
        Page<Asset> assetPage = new PageImpl<>(largeDataset);
        
        when(assetRepository.searchAssets(any(), any(), any(), any(), any(), any(), any()))
            .thenReturn(assetPage);
        
        // When
        long startTime = System.currentTimeMillis();
        ExportResult result = assetService.exportAssets(format, query);
        long duration = System.currentTimeMillis() - startTime;
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.getRecordCount()).isEqualTo(LARGE_DATASET_SIZE);
        assertThat(result.getData()).isNotNull();
        assertThat(duration).isLessThan(PERFORMANCE_THRESHOLD_MS);
        
        System.out.println("CSV Export Performance: " + LARGE_DATASET_SIZE + 
                         " assets exported in " + duration + "ms");
    }
    
    @Test
    @DisplayName("Should export 100,000 assets to JSON within 30 seconds")
    void shouldExportLargeDatasetToJsonWithinTimeLimit() {
        // Given
        ExportFormat format = ExportFormat.JSON;
        AssetSearchQuery query = null;
        
        List<Asset> largeDataset = createLargeAssetDataset(LARGE_DATASET_SIZE);
        Page<Asset> assetPage = new PageImpl<>(largeDataset);
        
        when(assetRepository.searchAssets(any(), any(), any(), any(), any(), any(), any()))
            .thenReturn(assetPage);
        
        // When
        long startTime = System.currentTimeMillis();
        ExportResult result = assetService.exportAssets(format, query);
        long duration = System.currentTimeMillis() - startTime;
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.getRecordCount()).isEqualTo(LARGE_DATASET_SIZE);
        assertThat(result.getData()).isNotNull();
        assertThat(duration).isLessThan(PERFORMANCE_THRESHOLD_MS);
        
        System.out.println("JSON Export Performance: " + LARGE_DATASET_SIZE + 
                         " assets exported in " + duration + "ms");
    }
    
    @Test
    @DisplayName("Should export 10,000 assets to CSV within 3 seconds")
    void shouldExportMediumDatasetToCsvQuickly() {
        // Given
        int mediumDatasetSize = 10_000;
        int mediumThresholdMs = 3_000; // 3 seconds
        
        ExportFormat format = ExportFormat.CSV;
        List<Asset> mediumDataset = createLargeAssetDataset(mediumDatasetSize);
        Page<Asset> assetPage = new PageImpl<>(mediumDataset);
        
        when(assetRepository.searchAssets(any(), any(), any(), any(), any(), any(), any()))
            .thenReturn(assetPage);
        
        // When
        long startTime = System.currentTimeMillis();
        ExportResult result = assetService.exportAssets(format, null);
        long duration = System.currentTimeMillis() - startTime;
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.getRecordCount()).isEqualTo(mediumDatasetSize);
        assertThat(duration).isLessThan(mediumThresholdMs);
        
        System.out.println("Medium CSV Export Performance: " + mediumDatasetSize + 
                         " assets exported in " + duration + "ms");
    }
    
    @Test
    @DisplayName("Should export 10,000 assets to JSON within 3 seconds")
    void shouldExportMediumDatasetToJsonQuickly() {
        // Given
        int mediumDatasetSize = 10_000;
        int mediumThresholdMs = 3_000; // 3 seconds
        
        ExportFormat format = ExportFormat.JSON;
        List<Asset> mediumDataset = createLargeAssetDataset(mediumDatasetSize);
        Page<Asset> assetPage = new PageImpl<>(mediumDataset);
        
        when(assetRepository.searchAssets(any(), any(), any(), any(), any(), any(), any()))
            .thenReturn(assetPage);
        
        // When
        long startTime = System.currentTimeMillis();
        ExportResult result = assetService.exportAssets(format, null);
        long duration = System.currentTimeMillis() - startTime;
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.getRecordCount()).isEqualTo(mediumDatasetSize);
        assertThat(duration).isLessThan(mediumThresholdMs);
        
        System.out.println("Medium JSON Export Performance: " + mediumDatasetSize + 
                         " assets exported in " + duration + "ms");
    }
    
    @Test
    @DisplayName("Should export 1,000 assets to CSV within 500ms")
    void shouldExportSmallDatasetToCsvVeryQuickly() {
        // Given
        int smallDatasetSize = 1_000;
        int smallThresholdMs = 500; // 500 milliseconds
        
        ExportFormat format = ExportFormat.CSV;
        List<Asset> smallDataset = createLargeAssetDataset(smallDatasetSize);
        Page<Asset> assetPage = new PageImpl<>(smallDataset);
        
        when(assetRepository.searchAssets(any(), any(), any(), any(), any(), any(), any()))
            .thenReturn(assetPage);
        
        // When
        long startTime = System.currentTimeMillis();
        ExportResult result = assetService.exportAssets(format, null);
        long duration = System.currentTimeMillis() - startTime;
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.getRecordCount()).isEqualTo(smallDatasetSize);
        assertThat(duration).isLessThan(smallThresholdMs);
        
        System.out.println("Small CSV Export Performance: " + smallDatasetSize + 
                         " assets exported in " + duration + "ms");
    }
    
    @Test
    @DisplayName("Should handle assets with all optional fields populated efficiently")
    void shouldHandleCompleteAssetsEfficiently() {
        // Given
        int datasetSize = 10_000;
        ExportFormat format = ExportFormat.CSV;
        
        List<Asset> completeAssets = createCompleteAssetDataset(datasetSize);
        Page<Asset> assetPage = new PageImpl<>(completeAssets);
        
        when(assetRepository.searchAssets(any(), any(), any(), any(), any(), any(), any()))
            .thenReturn(assetPage);
        
        // When
        long startTime = System.currentTimeMillis();
        ExportResult result = assetService.exportAssets(format, null);
        long duration = System.currentTimeMillis() - startTime;
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.getRecordCount()).isEqualTo(datasetSize);
        assertThat(duration).isLessThan(5_000); // Should complete within 5 seconds
        
        System.out.println("Complete Assets Export Performance: " + datasetSize + 
                         " assets with all fields exported in " + duration + "ms");
    }
    
    @Test
    @DisplayName("Should handle assets with special characters efficiently")
    void shouldHandleSpecialCharactersEfficiently() {
        // Given
        int datasetSize = 5_000;
        ExportFormat format = ExportFormat.CSV;
        
        List<Asset> assetsWithSpecialChars = createAssetsWithSpecialCharacters(datasetSize);
        Page<Asset> assetPage = new PageImpl<>(assetsWithSpecialChars);
        
        when(assetRepository.searchAssets(any(), any(), any(), any(), any(), any(), any()))
            .thenReturn(assetPage);
        
        // When
        long startTime = System.currentTimeMillis();
        ExportResult result = assetService.exportAssets(format, null);
        long duration = System.currentTimeMillis() - startTime;
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.getRecordCount()).isEqualTo(datasetSize);
        assertThat(duration).isLessThan(3_000); // Should complete within 3 seconds
        
        System.out.println("Special Characters Export Performance: " + datasetSize + 
                         " assets with special characters exported in " + duration + "ms");
    }
    
    // ========== Helper Methods ==========
    
    /**
     * Creates a large dataset of assets for performance testing.
     * Assets have minimal fields populated to simulate typical data.
     * 
     * @param size the number of assets to create
     * @return list of assets
     */
    private List<Asset> createLargeAssetDataset(int size) {
        List<Asset> assets = new ArrayList<>(size);
        AssetType[] assetTypes = AssetType.values();
        LifecycleStatus[] statuses = LifecycleStatus.values();
        
        for (int i = 0; i < size; i++) {
            Asset asset = new Asset();
            asset.setId(UUID.randomUUID());
            asset.setAssetType(assetTypes[i % assetTypes.length]);
            asset.setName("Asset " + i);
            asset.setSerialNumber("SN-" + String.format("%08d", i));
            asset.setAcquisitionDate(LocalDate.now().minusDays(i % 365));
            asset.setStatus(statuses[i % statuses.length]);
            asset.setCreatedBy(UUID.randomUUID());
            asset.setUpdatedBy(UUID.randomUUID());
            asset.setReadOnly(false);
            
            assets.add(asset);
        }
        
        return assets;
    }
    
    /**
     * Creates assets with all optional fields populated.
     * 
     * @param size the number of assets to create
     * @return list of complete assets
     */
    private List<Asset> createCompleteAssetDataset(int size) {
        List<Asset> assets = new ArrayList<>(size);
        
        for (int i = 0; i < size; i++) {
            Asset asset = new Asset();
            asset.setId(UUID.randomUUID());
            asset.setAssetType(AssetType.SERVER);
            asset.setName("Complete Asset " + i);
            asset.setSerialNumber("COMPLETE-" + String.format("%08d", i));
            asset.setAcquisitionDate(LocalDate.now().minusDays(i % 365));
            asset.setStatus(LifecycleStatus.IN_USE);
            asset.setLocation("Data Center " + (i % 10));
            asset.setAssignedUser("user" + (i % 100));
            asset.setAssignedUserEmail("user" + (i % 100) + "@example.com");
            asset.setNotes("Notes for asset " + i);
            asset.setCustomFields("{\"warranty\": \"3 years\", \"vendor\": \"Vendor " + (i % 5) + "\"}");
            asset.setCreatedBy(UUID.randomUUID());
            asset.setUpdatedBy(UUID.randomUUID());
            asset.setReadOnly(false);
            
            assets.add(asset);
        }
        
        return assets;
    }
    
    /**
     * Creates assets with special characters that require CSV escaping.
     * 
     * @param size the number of assets to create
     * @return list of assets with special characters
     */
    private List<Asset> createAssetsWithSpecialCharacters(int size) {
        List<Asset> assets = new ArrayList<>(size);
        String[] specialNames = {
            "Asset, with comma",
            "Asset \"with quotes\"",
            "Asset\nwith newline",
            "Asset with, comma and \"quotes\"",
            "Normal Asset"
        };
        
        for (int i = 0; i < size; i++) {
            Asset asset = new Asset();
            asset.setId(UUID.randomUUID());
            asset.setAssetType(AssetType.LAPTOP);
            asset.setName(specialNames[i % specialNames.length] + " " + i);
            asset.setSerialNumber("SPECIAL-" + String.format("%08d", i));
            asset.setAcquisitionDate(LocalDate.now().minusDays(i % 365));
            asset.setStatus(LifecycleStatus.IN_USE);
            asset.setNotes("Notes with, commas and \"quotes\" for asset " + i);
            asset.setCreatedBy(UUID.randomUUID());
            asset.setUpdatedBy(UUID.randomUUID());
            asset.setReadOnly(false);
            
            assets.add(asset);
        }
        
        return assets;
    }
}
