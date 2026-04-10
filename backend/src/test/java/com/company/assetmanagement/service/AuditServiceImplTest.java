package com.company.assetmanagement.service;

import com.company.assetmanagement.dto.AuditEventDTO;
import com.company.assetmanagement.dto.AuditLogDTO;
import com.company.assetmanagement.dto.FieldChangeDTO;
import com.company.assetmanagement.model.Action;
import com.company.assetmanagement.model.AuditLog;
import com.company.assetmanagement.repository.AuditLogRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
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

import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuditServiceImplTest {
    
    @Mock
    private AuditLogRepository auditLogRepository;
    
    @Mock
    private ObjectMapper objectMapper;
    
    @InjectMocks
    private AuditServiceImpl auditService;
    
    private UUID testUserId;
    private String testUsername;
    private String testResourceId;
    
    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID();
        testUsername = "testuser";
        testResourceId = UUID.randomUUID().toString();
    }
    
    @Test
    @DisplayName("Should log audit event with all fields")
    void shouldLogAuditEventWithAllFields() throws Exception {
        // Given
        Map<String, FieldChangeDTO> changes = new HashMap<>();
        changes.put("status", new FieldChangeDTO("status", "ordered", "received"));
        
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("reason", "Asset received from vendor");
        
        AuditEventDTO event = AuditEventDTO.builder()
            .userId(testUserId)
            .username(testUsername)
            .actionType(Action.UPDATE)
            .resourceType("ASSET")
            .resourceId(testResourceId)
            .changes(changes)
            .metadata(metadata)
            .ipAddress("192.168.1.100")
            .build();
        
        when(objectMapper.writeValueAsString(changes)).thenReturn("{\"status\":{\"field\":\"status\",\"oldValue\":\"ordered\",\"newValue\":\"received\"}}");
        when(objectMapper.writeValueAsString(metadata)).thenReturn("{\"reason\":\"Asset received from vendor\"}");
        when(auditLogRepository.save(any(AuditLog.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        // When
        auditService.logEvent(event);
        
        // Then
        ArgumentCaptor<AuditLog> auditLogCaptor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository).save(auditLogCaptor.capture());
        
        AuditLog savedLog = auditLogCaptor.getValue();
        assertThat(savedLog.getUserId()).isEqualTo(testUserId);
        assertThat(savedLog.getUsername()).isEqualTo(testUsername);
        assertThat(savedLog.getActionType()).isEqualTo(Action.UPDATE);
        assertThat(savedLog.getResourceType()).isEqualTo("ASSET");
        assertThat(savedLog.getResourceId()).isEqualTo(testResourceId);
        assertThat(savedLog.getIpAddress()).isEqualTo("192.168.1.100");
        assertThat(savedLog.getTimestamp()).isNotNull();
    }
    
    @Test
    @DisplayName("Should log audit event without changes or metadata")
    void shouldLogAuditEventWithoutChangesOrMetadata() {
        // Given
        AuditEventDTO event = AuditEventDTO.builder()
            .userId(testUserId)
            .username(testUsername)
            .actionType(Action.CREATE)
            .resourceType("ASSET")
            .resourceId(testResourceId)
            .build();
        
        when(auditLogRepository.save(any(AuditLog.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        // When
        auditService.logEvent(event);
        
        // Then
        ArgumentCaptor<AuditLog> auditLogCaptor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository).save(auditLogCaptor.capture());
        
        AuditLog savedLog = auditLogCaptor.getValue();
        assertThat(savedLog.getUserId()).isEqualTo(testUserId);
        assertThat(savedLog.getActionType()).isEqualTo(Action.CREATE);
        assertThat(savedLog.getChanges()).isNull();
        assertThat(savedLog.getMetadata()).isNull();
    }
    
    @Test
    @DisplayName("Should not throw exception when audit logging fails")
    void shouldNotThrowExceptionWhenAuditLoggingFails() {
        // Given
        AuditEventDTO event = AuditEventDTO.builder()
            .userId(testUserId)
            .username(testUsername)
            .actionType(Action.CREATE)
            .resourceType("ASSET")
            .resourceId(testResourceId)
            .build();
        
        when(auditLogRepository.save(any(AuditLog.class))).thenThrow(new RuntimeException("Database error"));
        
        // When/Then - should not throw exception
        auditService.logEvent(event);
        
        verify(auditLogRepository).save(any(AuditLog.class));
    }
    
    @Test
    @DisplayName("Should search audit log with all filters")
    void shouldSearchAuditLogWithAllFilters() {
        // Given
        LocalDateTime startDate = LocalDateTime.now().minusDays(7);
        LocalDateTime endDate = LocalDateTime.now();
        Pageable pageable = PageRequest.of(0, 20);
        
        AuditLog auditLog = createTestAuditLog();
        Page<AuditLog> auditLogPage = new PageImpl<>(List.of(auditLog));
        
        when(auditLogRepository.searchAuditLog(
            testUserId, Action.UPDATE, "ASSET", testResourceId, startDate, endDate, pageable
        )).thenReturn(auditLogPage);
        
        // When
        Page<AuditLogDTO> result = auditService.searchAuditLog(
            testUserId, Action.UPDATE, "ASSET", testResourceId, startDate, endDate, pageable
        );
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getUserId()).isEqualTo(testUserId);
        assertThat(result.getContent().get(0).getActionType()).isEqualTo(Action.UPDATE);
        
        verify(auditLogRepository).searchAuditLog(
            testUserId, Action.UPDATE, "ASSET", testResourceId, startDate, endDate, pageable
        );
    }
    
    @Test
    @DisplayName("Should search audit log with no filters")
    void shouldSearchAuditLogWithNoFilters() {
        // Given
        Pageable pageable = PageRequest.of(0, 20);
        
        AuditLog auditLog = createTestAuditLog();
        Page<AuditLog> auditLogPage = new PageImpl<>(List.of(auditLog));
        
        when(auditLogRepository.searchAuditLog(
            null, null, null, null, null, null, pageable
        )).thenReturn(auditLogPage);
        
        // When
        Page<AuditLogDTO> result = auditService.searchAuditLog(
            null, null, null, null, null, null, pageable
        );
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        
        verify(auditLogRepository).searchAuditLog(
            null, null, null, null, null, null, pageable
        );
    }
    
    @Test
    @DisplayName("Should get resource audit trail")
    void shouldGetResourceAuditTrail() {
        // Given
        AuditLog auditLog1 = createTestAuditLog();
        auditLog1.setActionType(Action.CREATE);
        
        AuditLog auditLog2 = createTestAuditLog();
        auditLog2.setActionType(Action.UPDATE);
        
        when(auditLogRepository.findByResourceIdOrderByTimestampDesc(testResourceId))
            .thenReturn(List.of(auditLog2, auditLog1));
        
        // When
        List<AuditLogDTO> result = auditService.getResourceAuditTrail(testResourceId);
        
        // Then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getActionType()).isEqualTo(Action.UPDATE);
        assertThat(result.get(1).getActionType()).isEqualTo(Action.CREATE);
        
        verify(auditLogRepository).findByResourceIdOrderByTimestampDesc(testResourceId);
    }
    
    @Test
    @DisplayName("Should get audit log by ID")
    void shouldGetAuditLogById() {
        // Given
        UUID auditLogId = UUID.randomUUID();
        AuditLog auditLog = createTestAuditLog();
        auditLog.setId(auditLogId);
        
        when(auditLogRepository.findById(auditLogId)).thenReturn(Optional.of(auditLog));
        
        // When
        AuditLogDTO result = auditService.getAuditLogById(auditLogId);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(auditLogId);
        assertThat(result.getUserId()).isEqualTo(testUserId);
        
        verify(auditLogRepository).findById(auditLogId);
    }
    
    @Test
    @DisplayName("Should return null when audit log not found")
    void shouldReturnNullWhenAuditLogNotFound() {
        // Given
        UUID auditLogId = UUID.randomUUID();
        when(auditLogRepository.findById(auditLogId)).thenReturn(Optional.empty());
        
        // When
        AuditLogDTO result = auditService.getAuditLogById(auditLogId);
        
        // Then
        assertThat(result).isNull();
        
        verify(auditLogRepository).findById(auditLogId);
    }
    
    private AuditLog createTestAuditLog() {
        AuditLog auditLog = new AuditLog();
        auditLog.setId(UUID.randomUUID());
        auditLog.setTimestamp(LocalDateTime.now());
        auditLog.setUserId(testUserId);
        auditLog.setUsername(testUsername);
        auditLog.setActionType(Action.UPDATE);
        auditLog.setResourceType("ASSET");
        auditLog.setResourceId(testResourceId);
        auditLog.setIpAddress("192.168.1.100");
        return auditLog;
    }
}
