package com.company.assetmanagement.service;

import com.company.assetmanagement.dto.AuditEventDTO;
import com.company.assetmanagement.dto.AuditLogDTO;
import com.company.assetmanagement.dto.FieldChangeDTO;
import com.company.assetmanagement.model.Action;
import com.company.assetmanagement.repository.AuditLogRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for AuditService.
 * Tests the complete audit logging workflow with database persistence.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class AuditServiceIntegrationTest {
    
    @Autowired
    private AuditService auditService;
    
    @Autowired
    private AuditLogRepository auditLogRepository;
    
    private UUID testUserId;
    private String testUsername;
    
    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID();
        testUsername = "integrationtestuser";
        auditLogRepository.deleteAll();
    }
    
    @Test
    @DisplayName("Should persist audit event to database")
    void shouldPersistAuditEventToDatabase() {
        // Given
        String resourceId = UUID.randomUUID().toString();
        
        Map<String, FieldChangeDTO> changes = new HashMap<>();
        changes.put("status", new FieldChangeDTO("status", "ordered", "received"));
        changes.put("location", new FieldChangeDTO("location", null, "Warehouse A"));
        
        AuditEventDTO event = AuditEventDTO.builder()
            .userId(testUserId)
            .username(testUsername)
            .actionType(Action.UPDATE)
            .resourceType("ASSET")
            .resourceId(resourceId)
            .changes(changes)
            .ipAddress("192.168.1.100")
            .build();
        
        // When
        auditService.logEvent(event);
        
        // Then
        List<AuditLogDTO> auditTrail = auditService.getResourceAuditTrail(resourceId);
        assertThat(auditTrail).hasSize(1);
        
        AuditLogDTO savedLog = auditTrail.get(0);
        assertThat(savedLog.getUserId()).isEqualTo(testUserId);
        assertThat(savedLog.getUsername()).isEqualTo(testUsername);
        assertThat(savedLog.getActionType()).isEqualTo(Action.UPDATE);
        assertThat(savedLog.getResourceType()).isEqualTo("ASSET");
        assertThat(savedLog.getResourceId()).isEqualTo(resourceId);
        assertThat(savedLog.getIpAddress()).isEqualTo("192.168.1.100");
        assertThat(savedLog.getChanges()).hasSize(2);
        assertThat(savedLog.getChanges().get("status").getOldValue()).isEqualTo("ordered");
        assertThat(savedLog.getChanges().get("status").getNewValue()).isEqualTo("received");
    }
    
    @Test
    @DisplayName("Should search audit logs by user ID")
    void shouldSearchAuditLogsByUserId() {
        // Given
        createTestAuditEvent(testUserId, Action.CREATE, "ASSET", "asset-1");
        createTestAuditEvent(testUserId, Action.UPDATE, "ASSET", "asset-1");
        createTestAuditEvent(UUID.randomUUID(), Action.CREATE, "ASSET", "asset-2");
        
        // When
        Page<AuditLogDTO> results = auditService.searchAuditLog(
            testUserId, null, null, null, null, null, PageRequest.of(0, 20)
        );
        
        // Then
        assertThat(results.getContent()).hasSize(2);
        assertThat(results.getContent()).allMatch(log -> log.getUserId().equals(testUserId));
    }
    
    @Test
    @DisplayName("Should search audit logs by action type")
    void shouldSearchAuditLogsByActionType() {
        // Given
        createTestAuditEvent(testUserId, Action.CREATE, "ASSET", "asset-1");
        createTestAuditEvent(testUserId, Action.UPDATE, "ASSET", "asset-1");
        createTestAuditEvent(testUserId, Action.UPDATE, "ASSET", "asset-2");
        
        // When
        Page<AuditLogDTO> results = auditService.searchAuditLog(
            null, Action.UPDATE, null, null, null, null, PageRequest.of(0, 20)
        );
        
        // Then
        assertThat(results.getContent()).hasSize(2);
        assertThat(results.getContent()).allMatch(log -> log.getActionType() == Action.UPDATE);
    }
    
    @Test
    @DisplayName("Should search audit logs by resource type")
    void shouldSearchAuditLogsByResourceType() {
        // Given
        createTestAuditEvent(testUserId, Action.CREATE, "ASSET", "asset-1");
        createTestAuditEvent(testUserId, Action.CREATE, "USER", "user-1");
        createTestAuditEvent(testUserId, Action.CREATE, "ASSET", "asset-2");
        
        // When
        Page<AuditLogDTO> results = auditService.searchAuditLog(
            null, null, "ASSET", null, null, null, PageRequest.of(0, 20)
        );
        
        // Then
        assertThat(results.getContent()).hasSize(2);
        assertThat(results.getContent()).allMatch(log -> log.getResourceType().equals("ASSET"));
    }
    
    @Test
    @DisplayName("Should search audit logs by date range")
    void shouldSearchAuditLogsByDateRange() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime yesterday = now.minusDays(1);
        LocalDateTime twoDaysAgo = now.minusDays(2);
        
        createTestAuditEvent(testUserId, Action.CREATE, "ASSET", "asset-1");
        
        // When
        Page<AuditLogDTO> results = auditService.searchAuditLog(
            null, null, null, null, yesterday, now.plusHours(1), PageRequest.of(0, 20)
        );
        
        // Then
        assertThat(results.getContent()).hasSize(1);
        assertThat(results.getContent().get(0).getTimestamp()).isAfter(yesterday);
    }
    
    @Test
    @DisplayName("Should search audit logs with multiple filters")
    void shouldSearchAuditLogsWithMultipleFilters() {
        // Given
        String resourceId = "asset-specific";
        createTestAuditEvent(testUserId, Action.UPDATE, "ASSET", resourceId);
        createTestAuditEvent(testUserId, Action.CREATE, "ASSET", "asset-other");
        createTestAuditEvent(UUID.randomUUID(), Action.UPDATE, "ASSET", resourceId);
        
        // When
        Page<AuditLogDTO> results = auditService.searchAuditLog(
            testUserId, Action.UPDATE, "ASSET", resourceId, null, null, PageRequest.of(0, 20)
        );
        
        // Then
        assertThat(results.getContent()).hasSize(1);
        AuditLogDTO log = results.getContent().get(0);
        assertThat(log.getUserId()).isEqualTo(testUserId);
        assertThat(log.getActionType()).isEqualTo(Action.UPDATE);
        assertThat(log.getResourceType()).isEqualTo("ASSET");
        assertThat(log.getResourceId()).isEqualTo(resourceId);
    }
    
    @Test
    @DisplayName("Should get complete audit trail for resource")
    void shouldGetCompleteAuditTrailForResource() {
        // Given
        String resourceId = "asset-with-history";
        createTestAuditEvent(testUserId, Action.CREATE, "ASSET", resourceId);
        createTestAuditEvent(testUserId, Action.UPDATE, "ASSET", resourceId);
        createTestAuditEvent(testUserId, Action.STATUS_CHANGE, "ASSET", resourceId);
        createTestAuditEvent(testUserId, Action.DELETE, "ASSET", resourceId);
        
        // When
        List<AuditLogDTO> auditTrail = auditService.getResourceAuditTrail(resourceId);
        
        // Then
        assertThat(auditTrail).hasSize(4);
        // Verify chronological order (most recent first)
        assertThat(auditTrail.get(0).getActionType()).isEqualTo(Action.DELETE);
        assertThat(auditTrail.get(1).getActionType()).isEqualTo(Action.STATUS_CHANGE);
        assertThat(auditTrail.get(2).getActionType()).isEqualTo(Action.UPDATE);
        assertThat(auditTrail.get(3).getActionType()).isEqualTo(Action.CREATE);
    }
    
    @Test
    @DisplayName("Should handle audit events with metadata")
    void shouldHandleAuditEventsWithMetadata() {
        // Given
        String resourceId = UUID.randomUUID().toString();
        
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("reason", "Scheduled maintenance");
        metadata.put("duration", 120);
        metadata.put("approved", true);
        
        AuditEventDTO event = AuditEventDTO.builder()
            .userId(testUserId)
            .username(testUsername)
            .actionType(Action.STATUS_CHANGE)
            .resourceType("ASSET")
            .resourceId(resourceId)
            .metadata(metadata)
            .build();
        
        // When
        auditService.logEvent(event);
        
        // Then
        List<AuditLogDTO> auditTrail = auditService.getResourceAuditTrail(resourceId);
        assertThat(auditTrail).hasSize(1);
        
        AuditLogDTO savedLog = auditTrail.get(0);
        assertThat(savedLog.getMetadata()).isNotNull();
        assertThat(savedLog.getMetadata()).containsEntry("reason", "Scheduled maintenance");
        assertThat(savedLog.getMetadata()).containsEntry("duration", 120);
        assertThat(savedLog.getMetadata()).containsEntry("approved", true);
    }
    
    private void createTestAuditEvent(UUID userId, Action action, String resourceType, String resourceId) {
        AuditEventDTO event = AuditEventDTO.builder()
            .userId(userId)
            .username("testuser-" + userId.toString().substring(0, 8))
            .actionType(action)
            .resourceType(resourceType)
            .resourceId(resourceId)
            .ipAddress("192.168.1.100")
            .build();
        
        auditService.logEvent(event);
    }
}
