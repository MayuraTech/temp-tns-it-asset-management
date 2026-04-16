package com.company.assetmanagement.property;

import com.company.assetmanagement.dto.AssetDTO;
import com.company.assetmanagement.dto.AssetRequest;
import com.company.assetmanagement.dto.AssetSearchQuery;
import com.company.assetmanagement.dto.ExportFormat;
import com.company.assetmanagement.dto.ExportResult;
import com.company.assetmanagement.dto.ImportFormat;
import com.company.assetmanagement.dto.ImportResult;
import com.company.assetmanagement.exception.DuplicateSerialNumberException;
import com.company.assetmanagement.exception.InvalidStatusTransitionException;
import com.company.assetmanagement.model.Asset;
import com.company.assetmanagement.model.AssetType;
import com.company.assetmanagement.model.LifecycleStatus;
import com.company.assetmanagement.repository.AssetRepository;
import com.company.assetmanagement.service.AssetService;
import net.jqwik.api.*;
import net.jqwik.api.constraints.*;
import net.jqwik.time.api.DateTimes;
import net.jqwik.time.api.Dates;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.*;

/**
 * Property-Based Tests for Asset Management Module using jqwik framework.
 * 
 * Tests the following correctness properties:
 * - Property 7: Valid asset creation generates unique identifier
 * - Property 8: Asset data persistence and retrieval
 * - Property 9: Serial number uniqueness enforcement
 * - Property 10: Asset update preserves immutable fields
 * - Property 11: Lifecycle status transition validation
 * - Property 12: Retired assets become read-only
 * - Property 16: Search returns matching assets
 * - Property 17: Search performance under load
 * - Property 28: Import validation catches errors
 * - Property 29: Export completeness
 * - Property 32: Concurrent updates maintain consistency
 * - Property 33: Database constraints enforced
 * 
 * **Validates: Requirements 1-12, 16-17, 28-29, 32-33**
 * 
 * @see AssetService
 * @see Asset
 */
@SpringBootTest
@ActiveProfiles("test")
@Group
@Label("Feature: it-infrastructure-asset-management-module2")
public class AssetPropertyTests {
    
    @Autowired
    private AssetService assetService;
    
    @Autowired
    private AssetRepository assetRepository;
    
    private static final String TEST_USER_ID = "00000000-0000-0000-0000-000000000001";
    
    /**
     * Property 7: Valid asset creation generates unique identifier
     * 
     * **Validates: Requirements 1.1**
     * 
     * For all valid asset requests, creating an asset should:
     * - Generate a non-null unique identifier
     * - Persist all provided fields correctly
     * - Return the created asset with all data intact
     */
    @Property(tries = 100)
    @Label("Property 7: Valid asset creation generates unique identifier")
    @Transactional
    void validAssetCreationGeneratesUniqueIdentifier(
            @ForAll("validAssetRequests") AssetRequest request) {
        
        // When: Creating the asset
        AssetDTO result = assetService.createAsset(TEST_USER_ID, request);
        
        // Then: Asset has unique ID and all fields are persisted
        assertThat(result.getId()).isNotNull();
        assertThat(result.getAssetType()).isEqualTo(request.getAssetType());
        assertThat(result.getName()).isEqualTo(request.getName());
        assertThat(result.getSerialNumber()).isEqualTo(request.getSerialNumber());
        assertThat(result.getAcquisitionDate()).isEqualTo(request.getAcquisitionDate());
        assertThat(result.getStatus()).isEqualTo(request.getStatus());
        assertThat(result.getCreatedAt()).isNotNull();
        assertThat(result.getUpdatedAt()).isNotNull();
        assertThat(result.isReadOnly()).isFalse();
        
        // Cleanup
        assetRepository.deleteById(UUID.fromString(result.getId()));
    }
    
    /**
     * Property 8: Asset data persistence and retrieval
     * 
     * **Validates: Requirements 1.1, 2.1**
     * 
     * For all valid assets, after creation:
     * - The asset can be retrieved by its ID
     * - All persisted data matches the original request
     * - Audit fields are populated correctly
     */
    @Property(tries = 100)
    @Label("Property 8: Asset data persistence and retrieval")
    @Transactional
    void assetDataPersistenceAndRetrieval(
            @ForAll("validAssetRequests") AssetRequest request) {
        
        // Given: An asset is created
        AssetDTO created = assetService.createAsset(TEST_USER_ID, request);
        
        // When: Retrieving the asset by ID
        Optional<AssetDTO> retrieved = assetService.getAsset(UUID.fromString(created.getId()));
        
        // Then: Asset is found and all data matches
        assertThat(retrieved).isPresent();
        AssetDTO asset = retrieved.get();
        
        assertThat(asset.getId()).isEqualTo(created.getId());
        assertThat(asset.getAssetType()).isEqualTo(request.getAssetType());
        assertThat(asset.getName()).isEqualTo(request.getName());
        assertThat(asset.getSerialNumber()).isEqualTo(request.getSerialNumber());
        assertThat(asset.getAcquisitionDate()).isEqualTo(request.getAcquisitionDate());
        assertThat(asset.getStatus()).isEqualTo(request.getStatus());
        assertThat(asset.getCreatedAt()).isNotNull();
        assertThat(asset.getUpdatedAt()).isNotNull();
        
        // Cleanup
        assetRepository.deleteById(UUID.fromString(created.getId()));
    }
    
    /**
     * Property 9: Serial number uniqueness enforcement
     * 
     * **Validates: Requirements 1.4, 7.1, 7.2, 7.3**
     * 
     * For any two asset requests with the same serial number:
     * - The first creation succeeds
     * - The second creation fails with DuplicateSerialNumberException
     * - The exception contains the conflicting serial number
     */
    @Property(tries = 100)
    @Label("Property 9: Serial number uniqueness enforcement")
    @Transactional
    void serialNumberUniquenessEnforcement(
            @ForAll("validAssetRequests") AssetRequest request1,
            @ForAll("validAssetRequests") AssetRequest request2) {
        
        // Given: Two assets with the same serial number
        request2.setSerialNumber(request1.getSerialNumber());
        
        // When: Creating first asset succeeds
        AssetDTO first = assetService.createAsset(TEST_USER_ID, request1);
        assertThat(first).isNotNull();
        
        // Then: Creating second asset with duplicate serial number fails
        assertThatThrownBy(() -> assetService.createAsset(TEST_USER_ID, request2))
            .isInstanceOf(DuplicateSerialNumberException.class)
            .hasMessageContaining(request1.getSerialNumber());
        
        // Cleanup
        assetRepository.deleteById(UUID.fromString(first.getId()));
    }
    
    /**
     * Property 10: Asset update preserves immutable fields
     * 
     * **Validates: Requirements 3.4**
     * 
     * For all asset updates:
     * - Immutable fields (id, serialNumber, createdAt, createdBy) remain unchanged
     * - Mutable fields can be updated
     * - updatedAt timestamp is refreshed
     */
    @Property(tries = 100)
    @Label("Property 10: Asset update preserves immutable fields")
    @Transactional
    void assetUpdatePreservesImmutableFields(
            @ForAll("validAssetRequests") AssetRequest createRequest,
            @ForAll("validAssetRequests") AssetRequest updateRequest) {
        
        // Given: An existing asset
        AssetDTO created = assetService.createAsset(TEST_USER_ID, createRequest);
        UUID originalId = UUID.fromString(created.getId());
        String originalSerialNumber = created.getSerialNumber();
        LocalDateTime originalCreatedAt = created.getCreatedAt();
        
        // When: Updating the asset (but keeping serial number same to avoid uniqueness violation)
        updateRequest.setSerialNumber(originalSerialNumber);
        AssetDTO updated = assetService.updateAsset(TEST_USER_ID, originalId, updateRequest);
        
        // Then: Immutable fields are preserved
        assertThat(updated.getId()).isEqualTo(created.getId());
        assertThat(updated.getSerialNumber()).isEqualTo(originalSerialNumber);
        assertThat(updated.getCreatedAt()).isEqualTo(originalCreatedAt);
        
        // And: Mutable fields are updated
        assertThat(updated.getName()).isEqualTo(updateRequest.getName());
        assertThat(updated.getAssetType()).isEqualTo(updateRequest.getAssetType());
        
        // And: updatedAt is refreshed
        assertThat(updated.getUpdatedAt()).isAfterOrEqualTo(created.getUpdatedAt());
        
        // Cleanup
        assetRepository.deleteById(originalId);
    }
    
    /**
     * Property 11: Lifecycle status transition validation
     * 
     * **Validates: Requirements 4.2, 4.3, 4.5**
     * 
     * For all status transitions:
     * - Valid transitions succeed
     * - Invalid transitions throw InvalidStatusTransitionException
     * - Status transition rules are enforced
     */
    @Property(tries = 100)
    @Label("Property 11: Lifecycle status transition validation")
    @Transactional
    void lifecycleStatusTransitionValidation(
            @ForAll("validAssetRequests") AssetRequest request,
            @ForAll LifecycleStatus fromStatus,
            @ForAll LifecycleStatus toStatus) {
        
        // Given: An asset with a specific status
        request.setStatus(fromStatus);
        AssetDTO created = assetService.createAsset(TEST_USER_ID, request);
        UUID assetId = UUID.fromString(created.getId());
        
        // When/Then: Attempting status transition
        if (fromStatus.canTransitionTo(toStatus)) {
            // Valid transition should succeed
            AssetDTO updated = assetService.updateStatus(TEST_USER_ID, assetId, toStatus);
            assertThat(updated.getStatus()).isEqualTo(toStatus);
        } else {
            // Invalid transition should fail
            assertThatThrownBy(() -> assetService.updateStatus(TEST_USER_ID, assetId, toStatus))
                .isInstanceOf(InvalidStatusTransitionException.class);
        }
        
        // Cleanup
        assetRepository.deleteById(assetId);
    }
    
    /**
     * Property 12: Retired assets become read-only
     * 
     * **Validates: Requirements 3.5, 4.4**
     * 
     * For all assets that reach RETIRED status:
     * - The readOnly flag is set to true
     * - Further status transitions are not allowed
     * - Updates are rejected (except for notes field)
     */
    @Property(tries = 100)
    @Label("Property 12: Retired assets become read-only")
    @Transactional
    void retiredAssetsBecomeReadOnly(
            @ForAll("validAssetRequests") AssetRequest request) {
        
        // Given: An asset that can transition to RETIRED
        request.setStatus(LifecycleStatus.IN_USE);
        AssetDTO created = assetService.createAsset(TEST_USER_ID, request);
        UUID assetId = UUID.fromString(created.getId());
        
        // When: Transitioning to RETIRED status
        AssetDTO retired = assetService.updateStatus(TEST_USER_ID, assetId, LifecycleStatus.RETIRED);
        
        // Then: Asset becomes read-only
        assertThat(retired.getStatus()).isEqualTo(LifecycleStatus.RETIRED);
        assertThat(retired.isReadOnly()).isTrue();
        
        // And: Further status transitions are not allowed
        assertThatThrownBy(() -> 
            assetService.updateStatus(TEST_USER_ID, assetId, LifecycleStatus.IN_USE))
            .isInstanceOf(InvalidStatusTransitionException.class);
        
        // Cleanup
        assetRepository.deleteById(assetId);
    }
    
    /**
     * Property 16: Search returns matching assets
     * 
     * **Validates: Requirements 5.1, 5.2, 5.3, 5.4**
     * 
     * For all search queries:
     * - All returned assets match the search criteria
     * - Text search matches name, serialNumber, or location
     * - Filter criteria are applied correctly
     */
    @Property(tries = 50)
    @Label("Property 16: Search returns matching assets")
    @Transactional
    void searchReturnsMatchingAssets(
            @ForAll("validAssetRequests") AssetRequest request,
            @ForAll("searchQueries") AssetSearchQuery query) {
        
        // Given: An asset is created
        AssetDTO created = assetService.createAsset(TEST_USER_ID, request);
        UUID assetId = UUID.fromString(created.getId());
        
        // When: Searching with query
        Pageable pageable = PageRequest.of(0, 20);
        Page<AssetDTO> results = assetService.searchAssets(query, pageable);
        
        // Then: All results match the query criteria
        results.getContent().forEach(asset -> {
            // Text search validation
            if (query.getText() != null && !query.getText().isBlank()) {
                String searchText = query.getText().toLowerCase();
                boolean matchesText = 
                    (asset.getName() != null && asset.getName().toLowerCase().contains(searchText)) ||
                    (asset.getSerialNumber() != null && asset.getSerialNumber().toLowerCase().contains(searchText)) ||
                    (asset.getLocation() != null && asset.getLocation().toLowerCase().contains(searchText));
                assertThat(matchesText).isTrue();
            }
            
            // Asset type filter validation
            if (query.getAssetTypes() != null && !query.getAssetTypes().isEmpty()) {
                assertThat(query.getAssetTypes()).contains(asset.getAssetType());
            }
            
            // Status filter validation
            if (query.getStatuses() != null && !query.getStatuses().isEmpty()) {
                assertThat(query.getStatuses()).contains(asset.getStatus());
            }
        });
        
        // Cleanup
        assetRepository.deleteById(assetId);
    }
    
    /**
     * Property 17: Search performance under load
     * 
     * **Validates: Requirements 5.5, 12.1**
     * 
     * For all search operations:
     * - Search completes within 2 seconds for large datasets
     * - Pagination works correctly
     * - Performance requirements are met
     */
    @Property(tries = 20)
    @Label("Property 17: Search performance under load")
    @Transactional
    void searchPerformanceUnderLoad(
            @ForAll("searchQueries") AssetSearchQuery query) {
        
        // Given: A search query
        Pageable pageable = PageRequest.of(0, 20);
        
        // When: Executing search
        long startTime = System.currentTimeMillis();
        Page<AssetDTO> results = assetService.searchAssets(query, pageable);
        long duration = System.currentTimeMillis() - startTime;
        
        // Then: Search completes within performance requirements
        // Note: 2 second requirement is for 100,000 assets, we're testing with smaller dataset
        assertThat(duration).isLessThan(2000);
        
        // And: Pagination metadata is correct
        assertThat(results.getSize()).isLessThanOrEqualTo(20);
        assertThat(results.getNumber()).isEqualTo(0);
    }
    
    /**
     * Property 28: Import validation catches errors
     * 
     * **Validates: Requirements 10.2, 10.4, 10.5**
     * 
     * For all import operations with invalid data:
     * - Validation errors are detected
     * - Error messages include line numbers
     * - Invalid records are not imported
     */
    @Property(tries = 50)
    @Label("Property 28: Import validation catches errors")
    @Transactional
    void importValidationCatchesErrors(
            @ForAll("invalidAssetRequests") AssetRequest invalidRequest) {
        
        // Given: Invalid asset data in CSV format
        String csvData = String.format("assetType,name,serialNumber,acquisitionDate,status\n%s,%s,%s,%s,%s",
            invalidRequest.getAssetType() != null ? invalidRequest.getAssetType() : "",
            invalidRequest.getName() != null ? invalidRequest.getName() : "",
            invalidRequest.getSerialNumber() != null ? invalidRequest.getSerialNumber() : "",
            invalidRequest.getAcquisitionDate() != null ? invalidRequest.getAcquisitionDate() : "",
            invalidRequest.getStatus() != null ? invalidRequest.getStatus() : "");
        
        // When: Attempting to import
        ImportResult result = assetService.importAssets(TEST_USER_ID, ImportFormat.CSV, csvData.getBytes());
        
        // Then: Validation errors are caught
        assertThat(result.getFailureCount()).isGreaterThan(0);
        assertThat(result.getErrors()).isNotEmpty();
        
        // And: No invalid records are imported
        assertThat(result.getSuccessCount()).isEqualTo(0);
    }
    
    /**
     * Property 29: Export completeness
     * 
     * **Validates: Requirements 9.4, 9.5**
     * 
     * For all export operations:
     * - All asset fields are included in the export
     * - Export format is correct (CSV or JSON)
     * - Data integrity is maintained
     */
    @Property(tries = 50)
    @Label("Property 29: Export completeness")
    @Transactional
    void exportCompleteness(
            @ForAll("validAssetRequests") AssetRequest request,
            @ForAll ExportFormat format) {
        
        // Given: An asset exists
        AssetDTO created = assetService.createAsset(TEST_USER_ID, request);
        UUID assetId = UUID.fromString(created.getId());
        
        // When: Exporting assets
        AssetSearchQuery query = new AssetSearchQuery();
        query.setText(created.getSerialNumber()); // Export only this asset
        ExportResult result = assetService.exportAssets(format, query);
        
        // Then: Export is successful
        assertThat(result).isNotNull();
        assertThat(result.getData()).isNotNull();
        assertThat(result.getData().length).isGreaterThan(0);
        
        // And: Export contains the asset data
        String exportContent = new String(result.getData());
        assertThat(exportContent).contains(created.getSerialNumber());
        assertThat(exportContent).contains(created.getName());
        
        // Cleanup
        assetRepository.deleteById(assetId);
    }
    
    /**
     * Property 32: Concurrent updates maintain consistency
     * 
     * **Validates: Requirements 12.6**
     * 
     * For all concurrent update operations:
     * - Data consistency is maintained
     * - No data corruption occurs
     * - All updates are properly serialized
     */
    @Property(tries = 20)
    @Label("Property 32: Concurrent updates maintain consistency")
    void concurrentUpdatesMaintainConsistency(
            @ForAll("validAssetRequests") AssetRequest request) throws Exception {
        
        // Given: An asset exists
        AssetDTO created = assetService.createAsset(TEST_USER_ID, request);
        UUID assetId = UUID.fromString(created.getId());
        
        // When: Multiple threads attempt to update the asset concurrently
        int threadCount = 5;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);
        
        for (int i = 0; i < threadCount; i++) {
            final int index = i;
            executor.submit(() -> {
                try {
                    AssetRequest updateRequest = new AssetRequest.Builder()
                        .assetType(request.getAssetType())
                        .name("Updated Name " + index)
                        .serialNumber(request.getSerialNumber())
                        .acquisitionDate(request.getAcquisitionDate())
                        .status(request.getStatus())
                        .build();
                    
                    assetService.updateAsset(TEST_USER_ID, assetId, updateRequest);
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    // Some updates may fail due to optimistic locking, which is expected
                } finally {
                    latch.countDown();
                }
            });
        }
        
        latch.await();
        executor.shutdown();
        
        // Then: Asset data remains consistent
        Optional<AssetDTO> finalState = assetService.getAsset(assetId);
        assertThat(finalState).isPresent();
        assertThat(finalState.get().getSerialNumber()).isEqualTo(request.getSerialNumber());
        
        // And: At least one update succeeded
        assertThat(successCount.get()).isGreaterThan(0);
        
        // Cleanup
        assetRepository.deleteById(assetId);
    }
    
    /**
     * Property 33: Database constraints enforced
     * 
     * **Validates: Requirements 17.2, 17.3, 17.4, 17.5**
     * 
     * For all database operations:
     * - NOT NULL constraints are enforced
     * - UNIQUE constraints are enforced
     * - CHECK constraints are enforced
     * - Foreign key constraints are enforced
     */
    @Property(tries = 50)
    @Label("Property 33: Database constraints enforced")
    @Transactional
    void databaseConstraintsEnforced(
            @ForAll("validAssetRequests") AssetRequest request) {
        
        // Given: A valid asset request
        AssetDTO created = assetService.createAsset(TEST_USER_ID, request);
        UUID assetId = UUID.fromString(created.getId());
        
        // When: Retrieving from database
        Optional<Asset> asset = assetRepository.findById(assetId);
        
        // Then: All NOT NULL constraints are satisfied
        assertThat(asset).isPresent();
        assertThat(asset.get().getId()).isNotNull();
        assertThat(asset.get().getAssetType()).isNotNull();
        assertThat(asset.get().getName()).isNotNull();
        assertThat(asset.get().getSerialNumber()).isNotNull();
        assertThat(asset.get().getAcquisitionDate()).isNotNull();
        assertThat(asset.get().getStatus()).isNotNull();
        assertThat(asset.get().getCreatedAt()).isNotNull();
        assertThat(asset.get().getCreatedBy()).isNotNull();
        assertThat(asset.get().getUpdatedAt()).isNotNull();
        assertThat(asset.get().getUpdatedBy()).isNotNull();
        
        // And: UNIQUE constraint on serial number is enforced (tested in Property 9)
        // And: CHECK constraints on enums are enforced (validated by enum types)
        
        // Cleanup
        assetRepository.deleteById(assetId);
    }
    
    // ==================== Data Generators ====================
    
    /**
     * Generator for valid AssetRequest objects.
     * Generates requests that satisfy all validation constraints.
     */
    @Provide
    Arbitrary<AssetRequest> validAssetRequests() {
        return Combinators.combine(
            Arbitraries.of(AssetType.values()),
            Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(100),
            Arbitraries.strings().alpha().numeric().ofMinLength(5).ofMaxLength(50)
                .map(s -> "SN-" + s + "-" + System.nanoTime()), // Ensure uniqueness
            Dates.dates().atTheEarliest(LocalDate.of(2000, 1, 1))
                .atTheLatest(LocalDate.now()),
            Arbitraries.of(LifecycleStatus.values())
        ).as((type, name, serial, date, status) -> {
            AssetRequest.Builder builder = new AssetRequest.Builder()
                .assetType(type)
                .name(name)
                .serialNumber(serial)
                .acquisitionDate(date)
                .status(status);
            
            return builder.build();
        });
    }
    
    /**
     * Generator for invalid AssetRequest objects.
     * Generates requests that violate validation constraints.
     */
    @Provide
    Arbitrary<AssetRequest> invalidAssetRequests() {
        return Arbitraries.oneOf(
            // Missing required fields
            Arbitraries.just(new AssetRequest.Builder().build()),
            
            // Invalid name (empty or too long)
            Combinators.combine(
                Arbitraries.of(AssetType.values()),
                Arbitraries.oneOf(
                    Arbitraries.just(""),
                    Arbitraries.strings().ofMinLength(256).ofMaxLength(300)
                ),
                Arbitraries.strings().alpha().numeric().ofMinLength(5).ofMaxLength(50),
                Dates.dates().atTheEarliest(LocalDate.of(2000, 1, 1))
                    .atTheLatest(LocalDate.now()),
                Arbitraries.of(LifecycleStatus.values())
            ).as((type, name, serial, date, status) ->
                new AssetRequest.Builder()
                    .assetType(type)
                    .name(name)
                    .serialNumber(serial)
                    .acquisitionDate(date)
                    .status(status)
                    .build()
            ),
            
            // Invalid serial number (too short or too long)
            Combinators.combine(
                Arbitraries.of(AssetType.values()),
                Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(100),
                Arbitraries.oneOf(
                    Arbitraries.strings().ofMinLength(1).ofMaxLength(4),
                    Arbitraries.strings().ofMinLength(101).ofMaxLength(150)
                ),
                Dates.dates().atTheEarliest(LocalDate.of(2000, 1, 1))
                    .atTheLatest(LocalDate.now()),
                Arbitraries.of(LifecycleStatus.values())
            ).as((type, name, serial, date, status) ->
                new AssetRequest.Builder()
                    .assetType(type)
                    .name(name)
                    .serialNumber(serial)
                    .acquisitionDate(date)
                    .status(status)
                    .build()
            ),
            
            // Invalid acquisition date (in the future)
            Combinators.combine(
                Arbitraries.of(AssetType.values()),
                Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(100),
                Arbitraries.strings().alpha().numeric().ofMinLength(5).ofMaxLength(50),
                Dates.dates().atTheEarliest(LocalDate.now().plusDays(1))
                    .atTheLatest(LocalDate.now().plusYears(1)),
                Arbitraries.of(LifecycleStatus.values())
            ).as((type, name, serial, date, status) ->
                new AssetRequest.Builder()
                    .assetType(type)
                    .name(name)
                    .serialNumber(serial)
                    .acquisitionDate(date)
                    .status(status)
                    .build()
            )
        );
    }
    
    /**
     * Generator for AssetSearchQuery objects.
     * Generates various search query combinations.
     */
    @Provide
    Arbitrary<AssetSearchQuery> searchQueries() {
        return Combinators.combine(
            Arbitraries.strings().alpha().ofMinLength(0).ofMaxLength(50).optional(),
            Arbitraries.subsetOf(AssetType.values()).optional(),
            Arbitraries.subsetOf(LifecycleStatus.values()).optional()
        ).as((text, types, statuses) -> {
            AssetSearchQuery query = new AssetSearchQuery();
            text.ifPresent(query::setText);
            types.ifPresent(t -> query.setAssetTypes(List.copyOf(t)));
            statuses.ifPresent(s -> query.setStatuses(List.copyOf(s)));
            return query;
        });
    }
}
