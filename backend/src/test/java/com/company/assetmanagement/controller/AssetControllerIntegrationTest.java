package com.company.assetmanagement.controller;

import com.company.assetmanagement.dto.*;
import com.company.assetmanagement.model.AssetType;
import com.company.assetmanagement.model.LifecycleStatus;
import com.company.assetmanagement.repository.AssetRepository;
import com.company.assetmanagement.service.AssetService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for AssetController.
 * Tests all REST endpoints with actual Spring context and database.
 * 
 * <p>Tests cover:
 * <ul>
 *   <li>GET /api/v1/assets - List assets with pagination</li>
 *   <li>GET /api/v1/assets/{id} - Get single asset</li>
 *   <li>POST /api/v1/assets - Create asset</li>
 *   <li>PUT /api/v1/assets/{id} - Update asset</li>
 *   <li>PATCH /api/v1/assets/{id} - Partial update</li>
 *   <li>DELETE /api/v1/assets/{id} - Delete asset</li>
 *   <li>PATCH /api/v1/assets/{id}/status - Update status</li>
 *   <li>GET /api/v1/assets/search - Search assets</li>
 *   <li>GET /api/v1/assets/export - Export assets</li>
 *   <li>POST /api/v1/assets/import - Import assets</li>
 * </ul>
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AssetControllerIntegrationTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Autowired
    private AssetService assetService;
    
    @Autowired
    private AssetRepository assetRepository;
    
    private AssetRequest validAssetRequest;
    private String testUserId;
    
    @BeforeEach
    void setUp() {
        // Clean up test data
        assetRepository.deleteAll();
        
        testUserId = "test-user-123";
        
        validAssetRequest = AssetRequest.builder()
                .assetType(AssetType.SERVER)
                .name("Test Server")
                .serialNumber("TEST-SRV-001")
                .acquisitionDate(LocalDate.now().minusDays(30))
                .status(LifecycleStatus.ORDERED)
                .location("Data Center A")
                .notes("Test server for integration testing")
                .build();
    }
    
    // ========== GET /api/v1/assets - List Assets ==========
    
    @Test
    @DisplayName("GET /api/v1/assets - Should return paginated list of assets")
    @WithMockUser(username = "testuser", roles = {"ASSET_MANAGER"})
    void shouldReturnPaginatedAssets() throws Exception {
        // Given: Create test assets
        assetService.createAsset(testUserId, validAssetRequest);
        
        AssetRequest request2 = AssetRequest.builder()
                .assetType(AssetType.WORKSTATION)
                .name("Test Workstation")
                .serialNumber("TEST-WS-001")
                .acquisitionDate(LocalDate.now().minusDays(20))
                .status(LifecycleStatus.IN_USE)
                .build();
        assetService.createAsset(testUserId, request2);
        
        // When/Then: Request assets list
        mockMvc.perform(get("/api/v1/assets")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[0].name", notNullValue()))
                .andExpect(jsonPath("$.content[0].serialNumber", notNullValue()))
                .andExpect(jsonPath("$.totalElements", is(2)))
                .andExpect(jsonPath("$.totalPages", is(1)));
    }
    
    @Test
    @DisplayName("GET /api/v1/assets - Should filter by asset type")
    @WithMockUser(username = "testuser", roles = {"ASSET_MANAGER"})
    void shouldFilterByAssetType() throws Exception {
        // Given: Create assets of different types
        assetService.createAsset(testUserId, validAssetRequest);
        
        AssetRequest laptopRequest = AssetRequest.builder()
                .assetType(AssetType.LAPTOP)
                .name("Test Laptop")
                .serialNumber("TEST-LAP-001")
                .acquisitionDate(LocalDate.now())
                .status(LifecycleStatus.IN_USE)
                .build();
        assetService.createAsset(testUserId, laptopRequest);
        
        // When/Then: Filter by SERVER type
        mockMvc.perform(get("/api/v1/assets")
                        .param("assetTypes", "SERVER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].assetType", is("SERVER")));
    }
    
    @Test
    @DisplayName("GET /api/v1/assets - Should require authentication")
    void shouldRequireAuthenticationForList() throws Exception {
        mockMvc.perform(get("/api/v1/assets"))
                .andExpect(status().isUnauthorized());
    }
    
    // ========== GET /api/v1/assets/{id} - Get Single Asset ==========
    
    @Test
    @DisplayName("GET /api/v1/assets/{id} - Should return asset by ID")
    @WithMockUser(username = "testuser", roles = {"VIEWER"})
    void shouldReturnAssetById() throws Exception {
        // Given: Create an asset
        AssetDTO created = assetService.createAsset(testUserId, validAssetRequest);
        
        // When/Then: Get asset by ID
        mockMvc.perform(get("/api/v1/assets/{id}", created.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(created.getId())))
                .andExpect(jsonPath("$.name", is("Test Server")))
                .andExpect(jsonPath("$.serialNumber", is("TEST-SRV-001")))
                .andExpect(jsonPath("$.assetType", is("SERVER")))
                .andExpect(jsonPath("$.status", is("ORDERED")));
    }
    
    @Test
    @DisplayName("GET /api/v1/assets/{id} - Should return 404 for non-existent asset")
    @WithMockUser(username = "testuser", roles = {"VIEWER"})
    void shouldReturn404ForNonExistentAsset() throws Exception {
        UUID nonExistentId = UUID.randomUUID();
        
        mockMvc.perform(get("/api/v1/assets/{id}", nonExistentId))
                .andExpect(status().isNotFound());
    }
    
    // ========== POST /api/v1/assets - Create Asset ==========
    
    @Test
    @DisplayName("POST /api/v1/assets - Should create new asset")
    @WithMockUser(username = "testuser", roles = {"ASSET_MANAGER"})
    void shouldCreateAsset() throws Exception {
        String requestJson = objectMapper.writeValueAsString(validAssetRequest);
        
        mockMvc.perform(post("/api/v1/assets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.name", is("Test Server")))
                .andExpect(jsonPath("$.serialNumber", is("TEST-SRV-001")))
                .andExpect(jsonPath("$.assetType", is("SERVER")))
                .andExpect(jsonPath("$.status", is("ORDERED")))
                .andExpect(jsonPath("$.createdAt", notNullValue()))
                .andExpect(jsonPath("$.createdBy", notNullValue()));
    }
    
    @Test
    @DisplayName("POST /api/v1/assets - Should reject invalid request")
    @WithMockUser(username = "testuser", roles = {"ASSET_MANAGER"})
    void shouldRejectInvalidAssetRequest() throws Exception {
        AssetRequest invalidRequest = AssetRequest.builder()
                .assetType(AssetType.SERVER)
                // Missing required fields: name, serialNumber, acquisitionDate, status
                .build();
        
        String requestJson = objectMapper.writeValueAsString(invalidRequest);
        
        mockMvc.perform(post("/api/v1/assets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest());
    }
    
    @Test
    @DisplayName("POST /api/v1/assets - Should reject duplicate serial number")
    @WithMockUser(username = "testuser", roles = {"ASSET_MANAGER"})
    void shouldRejectDuplicateSerialNumber() throws Exception {
        // Given: Create first asset
        assetService.createAsset(testUserId, validAssetRequest);
        
        // When/Then: Try to create asset with same serial number
        String requestJson = objectMapper.writeValueAsString(validAssetRequest);
        
        mockMvc.perform(post("/api/v1/assets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isConflict());
    }
    
    @Test
    @DisplayName("POST /api/v1/assets - Should require ASSET_MANAGER role")
    @WithMockUser(username = "testuser", roles = {"VIEWER"})
    void shouldRequireAssetManagerRoleForCreate() throws Exception {
        String requestJson = objectMapper.writeValueAsString(validAssetRequest);
        
        mockMvc.perform(post("/api/v1/assets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isForbidden());
    }
    
    // ========== PUT /api/v1/assets/{id} - Update Asset ==========
    
    @Test
    @DisplayName("PUT /api/v1/assets/{id} - Should update asset")
    @WithMockUser(username = "testuser", roles = {"ASSET_MANAGER"})
    void shouldUpdateAsset() throws Exception {
        // Given: Create an asset
        AssetDTO created = assetService.createAsset(testUserId, validAssetRequest);
        
        // When: Update the asset
        AssetRequest updateRequest = AssetRequest.builder()
                .assetType(AssetType.SERVER)
                .name("Updated Server Name")
                .serialNumber("TEST-SRV-001") // Same serial number
                .acquisitionDate(LocalDate.now().minusDays(30))
                .status(LifecycleStatus.RECEIVED)
                .location("Data Center B")
                .notes("Updated notes")
                .build();
        
        String requestJson = objectMapper.writeValueAsString(updateRequest);
        
        // Then: Verify update
        mockMvc.perform(put("/api/v1/assets/{id}", created.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(created.getId())))
                .andExpect(jsonPath("$.name", is("Updated Server Name")))
                .andExpect(jsonPath("$.status", is("RECEIVED")))
                .andExpect(jsonPath("$.location", is("Data Center B")))
                .andExpect(jsonPath("$.notes", is("Updated notes")));
    }
    
    // ========== PATCH /api/v1/assets/{id} - Partial Update ==========
    
    @Test
    @DisplayName("PATCH /api/v1/assets/{id} - Should partially update asset")
    @WithMockUser(username = "testuser", roles = {"ASSET_MANAGER"})
    void shouldPartiallyUpdateAsset() throws Exception {
        // Given: Create an asset
        AssetDTO created = assetService.createAsset(testUserId, validAssetRequest);
        
        // When: Partial update (only name and location)
        Map<String, Object> updates = new HashMap<>();
        updates.put("name", "Partially Updated Server");
        updates.put("location", "Data Center C");
        
        String requestJson = objectMapper.writeValueAsString(updates);
        
        // Then: Verify partial update
        mockMvc.perform(patch("/api/v1/assets/{id}", created.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(created.getId())))
                .andExpect(jsonPath("$.name", is("Partially Updated Server")))
                .andExpect(jsonPath("$.location", is("Data Center C")))
                .andExpect(jsonPath("$.serialNumber", is("TEST-SRV-001"))) // Unchanged
                .andExpect(jsonPath("$.status", is("ORDERED"))); // Unchanged
    }
    
    // ========== DELETE /api/v1/assets/{id} - Delete Asset ==========
    
    @Test
    @DisplayName("DELETE /api/v1/assets/{id} - Should delete asset")
    @WithMockUser(username = "testuser", roles = {"ADMINISTRATOR"})
    void shouldDeleteAsset() throws Exception {
        // Given: Create an asset
        AssetDTO created = assetService.createAsset(testUserId, validAssetRequest);
        
        // When: Delete the asset
        mockMvc.perform(delete("/api/v1/assets/{id}", created.getId()))
                .andExpect(status().isNoContent());
        
        // Then: Verify asset is deleted
        mockMvc.perform(get("/api/v1/assets/{id}", created.getId()))
                .andExpect(status().isNotFound());
    }
    
    @Test
    @DisplayName("DELETE /api/v1/assets/{id} - Should require ADMINISTRATOR role")
    @WithMockUser(username = "testuser", roles = {"ASSET_MANAGER"})
    void shouldRequireAdministratorRoleForDelete() throws Exception {
        AssetDTO created = assetService.createAsset(testUserId, validAssetRequest);
        
        mockMvc.perform(delete("/api/v1/assets/{id}", created.getId()))
                .andExpect(status().isForbidden());
    }
    
    // ========== PATCH /api/v1/assets/{id}/status - Update Status ==========
    
    @Test
    @DisplayName("PATCH /api/v1/assets/{id}/status - Should update asset status")
    @WithMockUser(username = "testuser", roles = {"ASSET_MANAGER"})
    void shouldUpdateAssetStatus() throws Exception {
        // Given: Create an asset with ORDERED status
        AssetDTO created = assetService.createAsset(testUserId, validAssetRequest);
        
        // When: Update status to RECEIVED
        StatusUpdateRequest statusUpdate = StatusUpdateRequest.builder()
                .newStatus(LifecycleStatus.RECEIVED)
                .reason("Asset received from vendor")
                .build();
        
        String requestJson = objectMapper.writeValueAsString(statusUpdate);
        
        // Then: Verify status update
        mockMvc.perform(patch("/api/v1/assets/{id}/status", created.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(created.getId())))
                .andExpect(jsonPath("$.status", is("RECEIVED")));
    }
    
    @Test
    @DisplayName("PATCH /api/v1/assets/{id}/status - Should reject invalid status transition")
    @WithMockUser(username = "testuser", roles = {"ASSET_MANAGER"})
    void shouldRejectInvalidStatusTransition() throws Exception {
        // Given: Create an asset with ORDERED status
        AssetDTO created = assetService.createAsset(testUserId, validAssetRequest);
        
        // When: Try invalid transition ORDERED -> IN_USE (should go through RECEIVED, DEPLOYED first)
        StatusUpdateRequest statusUpdate = StatusUpdateRequest.builder()
                .newStatus(LifecycleStatus.IN_USE)
                .build();
        
        String requestJson = objectMapper.writeValueAsString(statusUpdate);
        
        // Then: Expect 422 Unprocessable Entity
        mockMvc.perform(patch("/api/v1/assets/{id}/status", created.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isUnprocessableEntity());
    }
    
    // ========== GET /api/v1/assets/search - Search Assets ==========
    
    @Test
    @DisplayName("GET /api/v1/assets/search - Should search assets by text")
    @WithMockUser(username = "testuser", roles = {"VIEWER"})
    void shouldSearchAssetsByText() throws Exception {
        // Given: Create multiple assets
        assetService.createAsset(testUserId, validAssetRequest);
        
        AssetRequest laptopRequest = AssetRequest.builder()
                .assetType(AssetType.LAPTOP)
                .name("Dell Laptop")
                .serialNumber("TEST-LAP-001")
                .acquisitionDate(LocalDate.now())
                .status(LifecycleStatus.IN_USE)
                .build();
        assetService.createAsset(testUserId, laptopRequest);
        
        // When/Then: Search for "Server"
        mockMvc.perform(get("/api/v1/assets/search")
                        .param("text", "Server"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].name", containsString("Server")));
    }
    
    @Test
    @DisplayName("GET /api/v1/assets/search - Should filter by status")
    @WithMockUser(username = "testuser", roles = {"VIEWER"})
    void shouldSearchAssetsByStatus() throws Exception {
        // Given: Create assets with different statuses
        assetService.createAsset(testUserId, validAssetRequest); // ORDERED
        
        AssetRequest inUseRequest = AssetRequest.builder()
                .assetType(AssetType.LAPTOP)
                .name("Laptop In Use")
                .serialNumber("TEST-LAP-002")
                .acquisitionDate(LocalDate.now())
                .status(LifecycleStatus.IN_USE)
                .build();
        assetService.createAsset(testUserId, inUseRequest);
        
        // When/Then: Filter by IN_USE status
        mockMvc.perform(get("/api/v1/assets/search")
                        .param("statuses", "IN_USE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].status", is("IN_USE")));
    }
    
    // ========== GET /api/v1/assets/export - Export Assets ==========
    
    @Test
    @DisplayName("GET /api/v1/assets/export - Should export assets to CSV")
    @WithMockUser(username = "testuser", roles = {"ASSET_MANAGER"})
    void shouldExportAssetsToCSV() throws Exception {
        // Given: Create test assets
        assetService.createAsset(testUserId, validAssetRequest);
        
        // When/Then: Export to CSV
        mockMvc.perform(get("/api/v1/assets/export")
                        .param("format", "CSV"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", containsString("text/csv")))
                .andExpect(header().exists("Content-Disposition"))
                .andExpect(content().string(notNullValue()));
    }
    
    @Test
    @DisplayName("GET /api/v1/assets/export - Should export assets to JSON")
    @WithMockUser(username = "testuser", roles = {"ASSET_MANAGER"})
    void shouldExportAssetsToJSON() throws Exception {
        // Given: Create test assets
        assetService.createAsset(testUserId, validAssetRequest);
        
        // When/Then: Export to JSON
        mockMvc.perform(get("/api/v1/assets/export")
                        .param("format", "JSON"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", containsString("application/json")))
                .andExpect(header().exists("Content-Disposition"))
                .andExpect(content().string(notNullValue()));
    }
    
    @Test
    @DisplayName("GET /api/v1/assets/export - Should require ASSET_MANAGER role")
    @WithMockUser(username = "testuser", roles = {"VIEWER"})
    void shouldRequireAssetManagerRoleForExport() throws Exception {
        mockMvc.perform(get("/api/v1/assets/export")
                        .param("format", "CSV"))
                .andExpect(status().isForbidden());
    }
    
    // ========== POST /api/v1/assets/import - Import Assets ==========
    
    @Test
    @DisplayName("POST /api/v1/assets/import - Should import assets from CSV")
    @WithMockUser(username = "testuser", roles = {"ASSET_MANAGER"})
    void shouldImportAssetsFromCSV() throws Exception {
        // Given: CSV file content
        String csvContent = "assetType,name,serialNumber,acquisitionDate,status\n" +
                "SERVER,Import Server 1,IMP-SRV-001,2024-01-15,ORDERED\n" +
                "LAPTOP,Import Laptop 1,IMP-LAP-001,2024-01-16,IN_USE\n";
        
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "assets.csv",
                "text/csv",
                csvContent.getBytes()
        );
        
        // When/Then: Import CSV
        mockMvc.perform(multipart("/api/v1/assets/import")
                        .file(file)
                        .param("format", "CSV"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.successCount", greaterThan(0)))
                .andExpect(jsonPath("$.totalRecords", greaterThan(0)));
    }
    
    @Test
    @DisplayName("POST /api/v1/assets/import - Should require ASSET_MANAGER role")
    @WithMockUser(username = "testuser", roles = {"VIEWER"})
    void shouldRequireAssetManagerRoleForImport() throws Exception {
        String csvContent = "assetType,name,serialNumber,acquisitionDate,status\n";
        
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "assets.csv",
                "text/csv",
                csvContent.getBytes()
        );
        
        mockMvc.perform(multipart("/api/v1/assets/import")
                        .file(file)
                        .param("format", "CSV"))
                .andExpect(status().isForbidden());
    }
}
