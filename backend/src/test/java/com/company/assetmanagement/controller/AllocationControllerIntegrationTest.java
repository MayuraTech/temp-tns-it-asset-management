package com.company.assetmanagement.controller;

import com.company.assetmanagement.dto.AssignmentRequest;
import com.company.assetmanagement.model.*;
import com.company.assetmanagement.repository.AssetRepository;
import com.company.assetmanagement.repository.AssignmentHistoryRepository;
import com.company.assetmanagement.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for AllocationController.
 * 
 * Tests all allocation management endpoints with real database interactions.
 * Uses @Transactional to rollback changes after each test.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
class AllocationControllerIntegrationTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Autowired
    private AssetRepository assetRepository;
    
    @Autowired
    private AssignmentHistoryRepository assignmentHistoryRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    private Asset testAsset;
    private User testUser;
    
    @BeforeEach
    void setUp() {
        // Create test user
        testUser = new User();
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser = userRepository.save(testUser);
        
        // Create test asset
        testAsset = new Asset();
        testAsset.setAssetType(AssetType.SERVER);
        testAsset.setName("Test Server");
        testAsset.setSerialNumber("TEST-SRV-" + UUID.randomUUID().toString().substring(0, 8));
        testAsset.setAcquisitionDate(LocalDate.now());
        testAsset.setStatus(LifecycleStatus.IN_USE);
        testAsset = assetRepository.save(testAsset);
    }
    
    // ========== Assignment Creation Tests ==========
    
    @Test
    @WithMockUser(username = "testuser", roles = {"Administrator"})
    @DisplayName("Should create user assignment when authorized and asset is available")
    void shouldCreateUserAssignment() throws Exception {
        AssignmentRequest request = new AssignmentRequest();
        request.setAssignmentType(AssignmentType.USER);
        request.setAssignedTo("John Doe");
        request.setAssignedUserEmail("john.doe@example.com");
        
        mockMvc.perform(post("/api/v1/assets/{id}/assignments", testAsset.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").exists())
            .andExpect(jsonPath("$.assetId").value(testAsset.getId().toString()))
            .andExpect(jsonPath("$.assignmentType").value("USER"))
            .andExpect(jsonPath("$.assignedTo").value("John Doe"))
            .andExpect(jsonPath("$.active").value(true));
    }
    
    @Test
    @WithMockUser(username = "testuser", roles = {"Asset_Manager"})
    @DisplayName("Should create location assignment when authorized")
    void shouldCreateLocationAssignment() throws Exception {
        AssignmentRequest request = new AssignmentRequest();
        request.setAssignmentType(AssignmentType.LOCATION);
        request.setAssignedTo("Data Center A");
        
        mockMvc.perform(post("/api/v1/assets/{id}/assignments", testAsset.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.assignmentType").value("LOCATION"))
            .andExpect(jsonPath("$.assignedTo").value("Data Center A"));
    }
    
    @Test
    @WithMockUser(username = "testuser", roles = {"Viewer"})
    @DisplayName("Should return 403 when user lacks permission to create assignment")
    void shouldReturn403WhenUnauthorizedToCreateAssignment() throws Exception {
        AssignmentRequest request = new AssignmentRequest();
        request.setAssignmentType(AssignmentType.USER);
        request.setAssignedTo("John Doe");
        request.setAssignedUserEmail("john.doe@example.com");
        
        mockMvc.perform(post("/api/v1/assets/{id}/assignments", testAsset.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isForbidden());
    }
    
    @Test
    @WithMockUser(username = "testuser", roles = {"Administrator"})
    @DisplayName("Should return 400 when assignment request is invalid")
    void shouldReturn400WhenRequestInvalid() throws Exception {
        AssignmentRequest request = new AssignmentRequest();
        request.setAssignmentType(AssignmentType.USER);
        // Missing assignedTo and assignedUserEmail
        
        mockMvc.perform(post("/api/v1/assets/{id}/assignments", testAsset.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error.type").value("VALIDATION_ERROR"));
    }
    
    @Test
    @WithMockUser(username = "testuser", roles = {"Administrator"})
    @DisplayName("Should return 404 when asset does not exist")
    void shouldReturn404WhenAssetNotFound() throws Exception {
        AssignmentRequest request = new AssignmentRequest();
        request.setAssignmentType(AssignmentType.USER);
        request.setAssignedTo("John Doe");
        request.setAssignedUserEmail("john.doe@example.com");
        
        UUID nonExistentId = UUID.randomUUID();
        
        mockMvc.perform(post("/api/v1/assets/{id}/assignments", nonExistentId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.error.type").value("RESOURCE_NOT_FOUND"));
    }
    
    // ========== Deallocation Tests ==========
    
    @Test
    @WithMockUser(username = "testuser", roles = {"Administrator"})
    @DisplayName("Should deallocate asset when it has active assignment")
    void shouldDeallocateAsset() throws Exception {
        // First create an assignment
        AssignmentHistory assignment = new AssignmentHistory();
        assignment.setAssetId(testAsset.getId());
        assignment.setAssignmentType(AssignmentType.USER);
        assignment.setAssignedTo("John Doe");
        assignment.setAssignedBy(testUser.getId());
        assignmentHistoryRepository.save(assignment);
        
        mockMvc.perform(delete("/api/v1/assets/{id}/assignments", testAsset.getId()))
            .andExpect(status().isNoContent());
    }
    
    @Test
    @WithMockUser(username = "testuser", roles = {"Viewer"})
    @DisplayName("Should return 403 when user lacks permission to deallocate")
    void shouldReturn403WhenUnauthorizedToDeallocate() throws Exception {
        mockMvc.perform(delete("/api/v1/assets/{id}/assignments", testAsset.getId()))
            .andExpect(status().isForbidden());
    }
    
    // ========== Assignment History Tests ==========
    
    @Test
    @WithMockUser(username = "testuser", roles = {"Viewer"})
    @DisplayName("Should retrieve assignment history for asset")
    void shouldRetrieveAssignmentHistory() throws Exception {
        // Create some assignment history
        AssignmentHistory assignment1 = new AssignmentHistory();
        assignment1.setAssetId(testAsset.getId());
        assignment1.setAssignmentType(AssignmentType.USER);
        assignment1.setAssignedTo("User 1");
        assignment1.setAssignedBy(testUser.getId());
        assignmentHistoryRepository.save(assignment1);
        
        AssignmentHistory assignment2 = new AssignmentHistory();
        assignment2.setAssetId(testAsset.getId());
        assignment2.setAssignmentType(AssignmentType.LOCATION);
        assignment2.setAssignedTo("Location 1");
        assignment2.setAssignedBy(testUser.getId());
        assignmentHistoryRepository.save(assignment2);
        
        mockMvc.perform(get("/api/v1/assets/{id}/assignment-history", testAsset.getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content").isArray())
            .andExpect(jsonPath("$.content", hasSize(greaterThanOrEqualTo(2))))
            .andExpect(jsonPath("$.page.totalElements").value(greaterThanOrEqualTo(2)));
    }
    
    @Test
    @WithMockUser(username = "testuser", roles = {"Viewer"})
    @DisplayName("Should support pagination for assignment history")
    void shouldSupportPaginationForHistory() throws Exception {
        mockMvc.perform(get("/api/v1/assets/{id}/assignment-history", testAsset.getId())
                .param("page", "0")
                .param("size", "10"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.page.size").value(10))
            .andExpect(jsonPath("$.page.number").value(0));
    }
    
    // ========== Query by User Tests ==========
    
    @Test
    @WithMockUser(username = "testuser", roles = {"Viewer"})
    @DisplayName("Should query assets by user")
    void shouldQueryAssetsByUser() throws Exception {
        // Assign asset to user
        testAsset.setAssignedUser("John Doe");
        testAsset.setAssignedUserEmail("john.doe@example.com");
        assetRepository.save(testAsset);
        
        mockMvc.perform(get("/api/v1/assignments/user/{userName}", "John Doe"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content").isArray());
    }
    
    // ========== Query by Location Tests ==========
    
    @Test
    @WithMockUser(username = "testuser", roles = {"Viewer"})
    @DisplayName("Should query assets by location")
    void shouldQueryAssetsByLocation() throws Exception {
        // Assign asset to location
        testAsset.setLocation("Data Center A");
        assetRepository.save(testAsset);
        
        mockMvc.perform(get("/api/v1/assignments/location/{location}", "Data Center A"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content").isArray());
    }
    
    // ========== Statistics Tests ==========
    
    @Test
    @WithMockUser(username = "testuser", roles = {"Administrator"})
    @DisplayName("Should retrieve assignment statistics")
    void shouldRetrieveStatistics() throws Exception {
        mockMvc.perform(get("/api/v1/assignments/statistics"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalAssignedAssets").exists())
            .andExpect(jsonPath("$.userAssignments").exists())
            .andExpect(jsonPath("$.locationAssignments").exists())
            .andExpect(jsonPath("$.availableAssetsByStatus").exists());
    }
    
    @Test
    @WithMockUser(username = "testuser", roles = {"Viewer"})
    @DisplayName("Should return 403 when viewer tries to access statistics")
    void shouldReturn403WhenViewerAccessesStatistics() throws Exception {
        mockMvc.perform(get("/api/v1/assignments/statistics"))
            .andExpect(status().isForbidden());
    }
    
    // ========== Export Tests ==========
    
    @Test
    @WithMockUser(username = "testuser", roles = {"Administrator"})
    @DisplayName("Should export assignment data as CSV")
    void shouldExportAssignmentData() throws Exception {
        mockMvc.perform(get("/api/v1/assignments/export"))
            .andExpect(status().isOk())
            .andExpect(header().string("Content-Type", containsString("text/csv")))
            .andExpect(header().exists("Content-Disposition"));
    }
    
    // ========== Bulk Deallocate Tests ==========
    
    @Test
    @WithMockUser(username = "testuser", roles = {"Administrator"})
    @DisplayName("Should bulk deallocate multiple assets")
    void shouldBulkDeallocateAssets() throws Exception {
        // Create and assign multiple assets
        Asset asset1 = createAndAssignAsset("Asset 1");
        Asset asset2 = createAndAssignAsset("Asset 2");
        
        List<UUID> assetIds = List.of(asset1.getId(), asset2.getId());
        
        mockMvc.perform(post("/api/v1/assignments/bulk-deallocate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(assetIds)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalRequested").value(2))
            .andExpect(jsonPath("$.successCount").value(2))
            .andExpect(jsonPath("$.failureCount").value(0))
            .andExpect(jsonPath("$.successfulDeallocations").isArray())
            .andExpect(jsonPath("$.successfulDeallocations.length()").value(2))
            .andExpect(jsonPath("$.failedDeallocations").isArray())
            .andExpect(jsonPath("$.failedDeallocations.length()").value(0));
    }
    
    // ========== Case-Insensitive Query Tests ==========
    
    @Test
    @WithMockUser(username = "testuser", roles = {"Viewer"})
    @DisplayName("Should query assets by location with case-insensitive matching")
    void shouldQueryAssetsByLocationCaseInsensitive() throws Exception {
        // Given: Assets with various location names
        Asset asset1 = new Asset();
        asset1.setAssetType(AssetType.SERVER);
        asset1.setName("Server 1");
        asset1.setSerialNumber("SRV-" + UUID.randomUUID().toString().substring(0, 8));
        asset1.setAcquisitionDate(LocalDate.now());
        asset1.setStatus(LifecycleStatus.IN_USE);
        asset1.setLocation("Data Center A");
        asset1 = assetRepository.save(asset1);
        
        Asset asset2 = new Asset();
        asset2.setAssetType(AssetType.SERVER);
        asset2.setName("Server 2");
        asset2.setSerialNumber("SRV-" + UUID.randomUUID().toString().substring(0, 8));
        asset2.setAcquisitionDate(LocalDate.now());
        asset2.setStatus(LifecycleStatus.DEPLOYED);
        asset2.setLocation("DATA CENTER B");
        asset2 = assetRepository.save(asset2);
        
        Asset asset3 = new Asset();
        asset3.setAssetType(AssetType.WORKSTATION);
        asset3.setName("Workstation 1");
        asset3.setSerialNumber("WS-" + UUID.randomUUID().toString().substring(0, 8));
        asset3.setAcquisitionDate(LocalDate.now());
        asset3.setStatus(LifecycleStatus.IN_USE);
        asset3.setLocation("Office Building");
        asset3 = assetRepository.save(asset3);
        
        // When/Then: Search with lowercase "data center" should match both uppercase and mixed case
        mockMvc.perform(get("/api/v1/assignments/location/{location}", "data center")
                .param("page", "0")
                .param("size", "20"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content").isArray())
            .andExpect(jsonPath("$.content.length()").value(2))
            .andExpect(jsonPath("$.content[*].location", 
                containsInAnyOrder("Data Center A", "DATA CENTER B")));
    }
    
    @Test
    @WithMockUser(username = "testuser", roles = {"Viewer"})
    @DisplayName("Should query assets by user with case-insensitive matching")
    void shouldQueryAssetsByUserCaseInsensitive() throws Exception {
        // Given: Assets assigned to users with various name cases
        Asset asset1 = new Asset();
        asset1.setAssetType(AssetType.LAPTOP);
        asset1.setName("Laptop 1");
        asset1.setSerialNumber("LAP-" + UUID.randomUUID().toString().substring(0, 8));
        asset1.setAcquisitionDate(LocalDate.now());
        asset1.setStatus(LifecycleStatus.IN_USE);
        asset1.setAssignedUser("John Doe");
        asset1.setAssignedUserEmail("john.doe@example.com");
        asset1 = assetRepository.save(asset1);
        
        Asset asset2 = new Asset();
        asset2.setAssetType(AssetType.LAPTOP);
        asset2.setName("Laptop 2");
        asset2.setSerialNumber("LAP-" + UUID.randomUUID().toString().substring(0, 8));
        asset2.setAcquisitionDate(LocalDate.now());
        asset2.setStatus(LifecycleStatus.IN_USE);
        asset2.setAssignedUser("JOHN SMITH");
        asset2.setAssignedUserEmail("john.smith@example.com");
        asset2 = assetRepository.save(asset2);
        
        Asset asset3 = new Asset();
        asset3.setAssetType(AssetType.LAPTOP);
        asset3.setName("Laptop 3");
        asset3.setSerialNumber("LAP-" + UUID.randomUUID().toString().substring(0, 8));
        asset3.setAcquisitionDate(LocalDate.now());
        asset3.setStatus(LifecycleStatus.IN_USE);
        asset3.setAssignedUser("Jane Smith");
        asset3.setAssignedUserEmail("jane.smith@example.com");
        asset3 = assetRepository.save(asset3);
        
        // When/Then: Search with lowercase "john" should match both "John Doe" and "JOHN SMITH"
        mockMvc.perform(get("/api/v1/assignments/user/{userName}", "john")
                .param("page", "0")
                .param("size", "20"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content").isArray())
            .andExpect(jsonPath("$.content.length()").value(2))
            .andExpect(jsonPath("$.content[*].assignedUser", 
                containsInAnyOrder("John Doe", "JOHN SMITH")));
    }
    
    @Test
    @WithMockUser(username = "testuser", roles = {"Viewer"})
    @DisplayName("Should support partial location name matching with case-insensitivity")
    void shouldSupportPartialLocationNameMatching() throws Exception {
        // Given: Assets with locations containing "center"
        Asset asset1 = new Asset();
        asset1.setAssetType(AssetType.SERVER);
        asset1.setName("Server 1");
        asset1.setSerialNumber("SRV-" + UUID.randomUUID().toString().substring(0, 8));
        asset1.setAcquisitionDate(LocalDate.now());
        asset1.setStatus(LifecycleStatus.IN_USE);
        asset1.setLocation("Data Center A");
        asset1 = assetRepository.save(asset1);
        
        Asset asset2 = new Asset();
        asset2.setAssetType(AssetType.NETWORK_DEVICE);
        asset2.setName("Router 1");
        asset2.setSerialNumber("RTR-" + UUID.randomUUID().toString().substring(0, 8));
        asset2.setAcquisitionDate(LocalDate.now());
        asset2.setStatus(LifecycleStatus.DEPLOYED);
        asset2.setLocation("Distribution Center");
        asset2 = assetRepository.save(asset2);
        
        Asset asset3 = new Asset();
        asset3.setAssetType(AssetType.WORKSTATION);
        asset3.setName("Workstation 1");
        asset3.setSerialNumber("WS-" + UUID.randomUUID().toString().substring(0, 8));
        asset3.setAcquisitionDate(LocalDate.now());
        asset3.setStatus(LifecycleStatus.IN_USE);
        asset3.setLocation("Office Building");
        asset3 = assetRepository.save(asset3);
        
        // When/Then: Search with "center" should match both "Data Center A" and "Distribution Center"
        mockMvc.perform(get("/api/v1/assignments/location/{location}", "center")
                .param("page", "0")
                .param("size", "20"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content").isArray())
            .andExpect(jsonPath("$.content.length()").value(2))
            .andExpect(jsonPath("$.content[*].location", 
                containsInAnyOrder("Data Center A", "Distribution Center")));
    }
    
    // ========== Helper Methods ==========
    
    private Asset createAndAssignAsset(String name) {
        Asset asset = new Asset();
        asset.setAssetType(AssetType.SERVER);
        asset.setName(name);
        asset.setSerialNumber("TEST-" + UUID.randomUUID().toString().substring(0, 8));
        asset.setAcquisitionDate(LocalDate.now());
        asset.setStatus(LifecycleStatus.IN_USE);
        asset = assetRepository.save(asset);
        
        // Create assignment
        AssignmentHistory assignment = new AssignmentHistory();
        assignment.setAssetId(asset.getId());
        assignment.setAssignmentType(AssignmentType.USER);
        assignment.setAssignedTo("Test User");
        assignment.setAssignedBy(testUser.getId());
        assignmentHistoryRepository.save(assignment);
        
        return asset;
    }
}
