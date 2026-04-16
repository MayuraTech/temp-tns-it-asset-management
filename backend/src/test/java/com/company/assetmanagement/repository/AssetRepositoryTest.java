package com.company.assetmanagement.repository;

import com.company.assetmanagement.model.Asset;
import com.company.assetmanagement.model.AssetType;
import com.company.assetmanagement.model.LifecycleStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for AssetRepository.
 * Tests all custom query methods against an in-memory H2 database.
 * 
 * @see AssetRepository
 */
@DataJpaTest
@ActiveProfiles("test")
@DisplayName("AssetRepository Integration Tests")
class AssetRepositoryTest {
    
    @Autowired
    private TestEntityManager entityManager;
    
    @Autowired
    private AssetRepository assetRepository;
    
    private Asset testAsset1;
    private Asset testAsset2;
    private Asset testAsset3;
    private UUID testUserId;
    
    @BeforeEach
    void setUp() {
        // Clear any existing data
        assetRepository.deleteAll();
        entityManager.flush();
        entityManager.clear();
        
        // Create test user ID
        testUserId = UUID.randomUUID();
        
        // Create test assets
        testAsset1 = createAsset(
            AssetType.SERVER,
            "Production Server 01",
            "SRV-PROD-001",
            LocalDate.of(2024, 1, 15),
            LifecycleStatus.IN_USE,
            "Data Center A",
            "john.doe"
        );
        
        testAsset2 = createAsset(
            AssetType.LAPTOP,
            "Developer Laptop",
            "LAP-DEV-001",
            LocalDate.of(2024, 2, 20),
            LifecycleStatus.DEPLOYED,
            "Office Building B",
            "jane.smith"
        );
        
        testAsset3 = createAsset(
            AssetType.SERVER,
            "Backup Server",
            "SRV-BACKUP-001",
            LocalDate.of(2023, 12, 10),
            LifecycleStatus.STORAGE,
            "Data Center A",
            null
        );
        
        // Persist test assets
        entityManager.persist(testAsset1);
        entityManager.persist(testAsset2);
        entityManager.persist(testAsset3);
        entityManager.flush();
    }
    
    // ========== Sub-task 4.2: existsBySerialNumber() tests ==========
    
    @Test
    @DisplayName("Should return true when asset with serial number exists")
    void existsBySerialNumber_WhenExists_ReturnsTrue() {
        // When
        boolean exists = assetRepository.existsBySerialNumber("SRV-PROD-001");
        
        // Then
        assertThat(exists).isTrue();
    }
    
    @Test
    @DisplayName("Should return false when asset with serial number does not exist")
    void existsBySerialNumber_WhenNotExists_ReturnsFalse() {
        // When
        boolean exists = assetRepository.existsBySerialNumber("NON-EXISTENT-001");
        
        // Then
        assertThat(exists).isFalse();
    }
    
    @Test
    @DisplayName("Should be case-sensitive for serial number existence check")
    void existsBySerialNumber_IsCaseSensitive() {
        // When
        boolean exists = assetRepository.existsBySerialNumber("srv-prod-001");
        
        // Then
        assertThat(exists).isFalse();
    }
    
    // ========== Sub-task 4.3: findBySerialNumber() tests ==========
    
    @Test
    @DisplayName("Should find asset by serial number when exists")
    void findBySerialNumber_WhenExists_ReturnsAsset() {
        // When
        Optional<Asset> result = assetRepository.findBySerialNumber("SRV-PROD-001");
        
        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("Production Server 01");
        assertThat(result.get().getAssetType()).isEqualTo(AssetType.SERVER);
    }
    
    @Test
    @DisplayName("Should return empty when serial number not found")
    void findBySerialNumber_WhenNotExists_ReturnsEmpty() {
        // When
        Optional<Asset> result = assetRepository.findBySerialNumber("NON-EXISTENT-001");
        
        // Then
        assertThat(result).isEmpty();
    }
    
    // ========== Sub-task 4.4: searchAssets() tests ==========
    
    @Test
    @DisplayName("Should return all assets when no filters applied")
    void searchAssets_NoFilters_ReturnsAllAssets() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        
        // When
        Page<Asset> result = assetRepository.searchAssets(
            null, null, null, null, null, null, pageable
        );
        
        // Then
        assertThat(result.getContent()).hasSize(3);
        assertThat(result.getTotalElements()).isEqualTo(3);
    }
    
    @Test
    @DisplayName("Should search by text in asset name")
    void searchAssets_ByTextInName_ReturnsMatchingAssets() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        
        // When
        Page<Asset> result = assetRepository.searchAssets(
            "server", null, null, null, null, null, pageable
        );
        
        // Then
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent())
            .extracting(Asset::getName)
            .containsExactlyInAnyOrder("Production Server 01", "Backup Server");
    }
    
    @Test
    @DisplayName("Should search by text in serial number")
    void searchAssets_ByTextInSerialNumber_ReturnsMatchingAssets() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        
        // When
        Page<Asset> result = assetRepository.searchAssets(
            "LAP-DEV", null, null, null, null, null, pageable
        );
        
        // Then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getSerialNumber()).isEqualTo("LAP-DEV-001");
    }
    
    @Test
    @DisplayName("Should search by text in location")
    void searchAssets_ByTextInLocation_ReturnsMatchingAssets() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        
        // When
        Page<Asset> result = assetRepository.searchAssets(
            "Data Center", null, null, null, null, null, pageable
        );
        
        // Then
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent())
            .extracting(Asset::getLocation)
            .containsOnly("Data Center A");
    }
    
    @Test
    @DisplayName("Should filter by single asset type")
    void searchAssets_BySingleAssetType_ReturnsMatchingAssets() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        List<AssetType> types = Arrays.asList(AssetType.SERVER);
        
        // When
        Page<Asset> result = assetRepository.searchAssets(
            null, types, null, null, null, null, pageable
        );
        
        // Then
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent())
            .extracting(Asset::getAssetType)
            .containsOnly(AssetType.SERVER);
    }
    
    @Test
    @DisplayName("Should filter by multiple asset types")
    void searchAssets_ByMultipleAssetTypes_ReturnsMatchingAssets() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        List<AssetType> types = Arrays.asList(AssetType.SERVER, AssetType.LAPTOP);
        
        // When
        Page<Asset> result = assetRepository.searchAssets(
            null, types, null, null, null, null, pageable
        );
        
        // Then
        assertThat(result.getContent()).hasSize(3);
    }
    
    @Test
    @DisplayName("Should filter by single status")
    void searchAssets_BySingleStatus_ReturnsMatchingAssets() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        List<LifecycleStatus> statuses = Arrays.asList(LifecycleStatus.IN_USE);
        
        // When
        Page<Asset> result = assetRepository.searchAssets(
            null, null, statuses, null, null, null, pageable
        );
        
        // Then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getStatus()).isEqualTo(LifecycleStatus.IN_USE);
    }
    
    @Test
    @DisplayName("Should filter by multiple statuses")
    void searchAssets_ByMultipleStatuses_ReturnsMatchingAssets() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        List<LifecycleStatus> statuses = Arrays.asList(
            LifecycleStatus.IN_USE, 
            LifecycleStatus.DEPLOYED
        );
        
        // When
        Page<Asset> result = assetRepository.searchAssets(
            null, null, statuses, null, null, null, pageable
        );
        
        // Then
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent())
            .extracting(Asset::getStatus)
            .containsExactlyInAnyOrder(LifecycleStatus.IN_USE, LifecycleStatus.DEPLOYED);
    }
    
    @Test
    @DisplayName("Should filter by exact location match")
    void searchAssets_ByExactLocation_ReturnsMatchingAssets() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        
        // When
        Page<Asset> result = assetRepository.searchAssets(
            null, null, null, "Data Center A", null, null, pageable
        );
        
        // Then
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent())
            .extracting(Asset::getLocation)
            .containsOnly("Data Center A");
    }
    
    @Test
    @DisplayName("Should filter by acquisition date from")
    void searchAssets_ByDateFrom_ReturnsMatchingAssets() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        LocalDate dateFrom = LocalDate.of(2024, 1, 1);
        
        // When
        Page<Asset> result = assetRepository.searchAssets(
            null, null, null, null, dateFrom, null, pageable
        );
        
        // Then
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent())
            .extracting(Asset::getAcquisitionDate)
            .allMatch(date -> !date.isBefore(dateFrom));
    }
    
    @Test
    @DisplayName("Should filter by acquisition date to")
    void searchAssets_ByDateTo_ReturnsMatchingAssets() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        LocalDate dateTo = LocalDate.of(2024, 1, 31);
        
        // When
        Page<Asset> result = assetRepository.searchAssets(
            null, null, null, null, null, dateTo, pageable
        );
        
        // Then
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent())
            .extracting(Asset::getAcquisitionDate)
            .allMatch(date -> !date.isAfter(dateTo));
    }
    
    @Test
    @DisplayName("Should filter by acquisition date range")
    void searchAssets_ByDateRange_ReturnsMatchingAssets() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        LocalDate dateFrom = LocalDate.of(2024, 1, 1);
        LocalDate dateTo = LocalDate.of(2024, 2, 28);
        
        // When
        Page<Asset> result = assetRepository.searchAssets(
            null, null, null, null, dateFrom, dateTo, pageable
        );
        
        // Then
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent())
            .extracting(Asset::getAcquisitionDate)
            .allMatch(date -> !date.isBefore(dateFrom) && !date.isAfter(dateTo));
    }
    
    @Test
    @DisplayName("Should combine multiple filters with AND logic")
    void searchAssets_WithMultipleFilters_ReturnsMatchingAssets() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        List<AssetType> types = Arrays.asList(AssetType.SERVER);
        List<LifecycleStatus> statuses = Arrays.asList(LifecycleStatus.IN_USE);
        
        // When
        Page<Asset> result = assetRepository.searchAssets(
            "Production", types, statuses, "Data Center A", null, null, pageable
        );
        
        // Then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getName()).isEqualTo("Production Server 01");
    }
    
    @Test
    @DisplayName("Should return empty page when no matches found")
    void searchAssets_NoMatches_ReturnsEmptyPage() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        
        // When
        Page<Asset> result = assetRepository.searchAssets(
            "NonExistent", null, null, null, null, null, pageable
        );
        
        // Then
        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isZero();
    }
    
    @Test
    @DisplayName("Should support pagination")
    void searchAssets_WithPagination_ReturnsCorrectPage() {
        // Given
        Pageable pageable = PageRequest.of(0, 2);
        
        // When
        Page<Asset> result = assetRepository.searchAssets(
            null, null, null, null, null, null, pageable
        );
        
        // Then
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getTotalElements()).isEqualTo(3);
        assertThat(result.getTotalPages()).isEqualTo(2);
        assertThat(result.hasNext()).isTrue();
    }
    
    @Test
    @DisplayName("Should perform case-insensitive text search")
    void searchAssets_CaseInsensitiveText_ReturnsMatchingAssets() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        
        // When
        Page<Asset> result = assetRepository.searchAssets(
            "PRODUCTION", null, null, null, null, null, pageable
        );
        
        // Then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getName()).isEqualTo("Production Server 01");
    }
    
    // ========== Sub-task 4.5: findByAssignedUser() tests ==========
    
    @Test
    @DisplayName("Should find assets by assigned user")
    void findByAssignedUser_WhenAssetsExist_ReturnsAssets() {
        // When
        List<Asset> result = assetRepository.findByAssignedUser("john.doe");
        
        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getAssignedUser()).isEqualTo("john.doe");
        assertThat(result.get(0).getName()).isEqualTo("Production Server 01");
    }
    
    @Test
    @DisplayName("Should return empty list when no assets assigned to user")
    void findByAssignedUser_WhenNoAssets_ReturnsEmptyList() {
        // When
        List<Asset> result = assetRepository.findByAssignedUser("nonexistent.user");
        
        // Then
        assertThat(result).isEmpty();
    }
    
    @Test
    @DisplayName("Should return empty list for null assigned user")
    void findByAssignedUser_WithNull_ReturnsEmptyList() {
        // When
        List<Asset> result = assetRepository.findByAssignedUser(null);
        
        // Then
        assertThat(result).isEmpty();
    }
    
    // ========== Sub-task 4.6: findByLocation() tests ==========
    
    @Test
    @DisplayName("Should find assets by location")
    void findByLocation_WhenAssetsExist_ReturnsAssets() {
        // When
        List<Asset> result = assetRepository.findByLocation("Data Center A");
        
        // Then
        assertThat(result).hasSize(2);
        assertThat(result)
            .extracting(Asset::getLocation)
            .containsOnly("Data Center A");
    }
    
    @Test
    @DisplayName("Should return empty list when no assets at location")
    void findByLocation_WhenNoAssets_ReturnsEmptyList() {
        // When
        List<Asset> result = assetRepository.findByLocation("Nonexistent Location");
        
        // Then
        assertThat(result).isEmpty();
    }
    
    @Test
    @DisplayName("Should be case-sensitive for location search")
    void findByLocation_IsCaseSensitive() {
        // When
        List<Asset> result = assetRepository.findByLocation("data center a");
        
        // Then
        assertThat(result).isEmpty();
    }
    
    // ========== Sub-task 4.7: countByAssetType() tests ==========
    
    @Test
    @DisplayName("Should count assets grouped by asset type")
    void countByAssetType_ReturnsCorrectCounts() {
        // When
        List<Object[]> result = assetRepository.countByAssetType();
        
        // Then
        assertThat(result).hasSize(2);
        
        // Verify SERVER count
        Object[] serverCount = result.stream()
            .filter(row -> row[0] == AssetType.SERVER)
            .findFirst()
            .orElseThrow();
        assertThat(serverCount[1]).isEqualTo(2L);
        
        // Verify LAPTOP count
        Object[] laptopCount = result.stream()
            .filter(row -> row[0] == AssetType.LAPTOP)
            .findFirst()
            .orElseThrow();
        assertThat(laptopCount[1]).isEqualTo(1L);
    }
    
    @Test
    @DisplayName("Should return empty list when no assets exist")
    void countByAssetType_WhenNoAssets_ReturnsEmptyList() {
        // Given
        assetRepository.deleteAll();
        entityManager.flush();
        
        // When
        List<Object[]> result = assetRepository.countByAssetType();
        
        // Then
        assertThat(result).isEmpty();
    }
    
    // ========== Sub-task 4.8: countByStatus() tests ==========
    
    @Test
    @DisplayName("Should count assets grouped by status")
    void countByStatus_ReturnsCorrectCounts() {
        // When
        List<Object[]> result = assetRepository.countByStatus();
        
        // Then
        assertThat(result).hasSize(3);
        
        // Verify IN_USE count
        Object[] inUseCount = result.stream()
            .filter(row -> row[0] == LifecycleStatus.IN_USE)
            .findFirst()
            .orElseThrow();
        assertThat(inUseCount[1]).isEqualTo(1L);
        
        // Verify DEPLOYED count
        Object[] deployedCount = result.stream()
            .filter(row -> row[0] == LifecycleStatus.DEPLOYED)
            .findFirst()
            .orElseThrow();
        assertThat(deployedCount[1]).isEqualTo(1L);
        
        // Verify STORAGE count
        Object[] storageCount = result.stream()
            .filter(row -> row[0] == LifecycleStatus.STORAGE)
            .findFirst()
            .orElseThrow();
        assertThat(storageCount[1]).isEqualTo(1L);
    }
    
    @Test
    @DisplayName("Should return empty list when no assets exist")
    void countByStatus_WhenNoAssets_ReturnsEmptyList() {
        // Given
        assetRepository.deleteAll();
        entityManager.flush();
        
        // When
        List<Object[]> result = assetRepository.countByStatus();
        
        // Then
        assertThat(result).isEmpty();
    }
    
    // ========== Helper Methods ==========
    
    private Asset createAsset(
            AssetType assetType,
            String name,
            String serialNumber,
            LocalDate acquisitionDate,
            LifecycleStatus status,
            String location,
            String assignedUser) {
        
        Asset asset = new Asset();
        asset.setAssetType(assetType);
        asset.setName(name);
        asset.setSerialNumber(serialNumber);
        asset.setAcquisitionDate(acquisitionDate);
        asset.setStatus(status);
        asset.setLocation(location);
        asset.setAssignedUser(assignedUser);
        asset.setCreatedBy(testUserId);
        asset.setUpdatedBy(testUserId);
        asset.setCreatedAt(LocalDateTime.now());
        asset.setUpdatedAt(LocalDateTime.now());
        asset.setReadOnly(false);
        
        return asset;
    }
}
