package com.company.assetmanagement.service;

import com.company.assetmanagement.dto.AssetRequest;
import com.company.assetmanagement.exception.InvalidStatusTransitionException;
import com.company.assetmanagement.exception.ValidationException;
import com.company.assetmanagement.model.AssetType;
import com.company.assetmanagement.model.LifecycleStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for AssetValidationService.
 * Tests all validation scenarios for asset data and status transitions.
 */
@DisplayName("AssetValidationService Tests")
class AssetValidationServiceTest {
    
    private AssetValidationService validationService;
    
    @BeforeEach
    void setUp() {
        validationService = new AssetValidationService();
    }
    
    // ========== Valid Request Tests ==========
    
    @Test
    @DisplayName("Should pass validation for valid asset request")
    void shouldPassValidationForValidRequest() {
        // Given
        AssetRequest request = createValidAssetRequest();
        
        // When/Then - should not throw exception
        assertThatCode(() -> validationService.validateAssetRequest(request))
            .doesNotThrowAnyException();
    }
    
    // ========== Required Field Validation Tests ==========
    
    @Test
    @DisplayName("Should fail validation when asset type is null")
    void shouldFailValidationWhenAssetTypeIsNull() {
        // Given
        AssetRequest request = createValidAssetRequest();
        request.setAssetType(null);
        
        // When/Then
        assertThatThrownBy(() -> validationService.validateAssetRequest(request))
            .isInstanceOf(ValidationException.class)
            .satisfies(ex -> {
                ValidationException vex = (ValidationException) ex;
                assertThat(vex.getErrors()).hasSize(1);
                assertThat(vex.getErrors().get(0).getField()).isEqualTo("assetType");
                assertThat(vex.getErrors().get(0).getMessage()).contains("required");
            });
    }
    
    @Test
    @DisplayName("Should fail validation when name is null")
    void shouldFailValidationWhenNameIsNull() {
        // Given
        AssetRequest request = createValidAssetRequest();
        request.setName(null);
        
        // When/Then
        assertThatThrownBy(() -> validationService.validateAssetRequest(request))
            .isInstanceOf(ValidationException.class)
            .satisfies(ex -> {
                ValidationException vex = (ValidationException) ex;
                assertThat(vex.getErrors()).anySatisfy(error -> {
                    assertThat(error.getField()).isEqualTo("name");
                    assertThat(error.getMessage()).contains("required");
                });
            });
    }
    
    @Test
    @DisplayName("Should fail validation when name is empty")
    void shouldFailValidationWhenNameIsEmpty() {
        // Given
        AssetRequest request = createValidAssetRequest();
        request.setName("");
        
        // When/Then
        assertThatThrownBy(() -> validationService.validateAssetRequest(request))
            .isInstanceOf(ValidationException.class)
            .satisfies(ex -> {
                ValidationException vex = (ValidationException) ex;
                assertThat(vex.getErrors()).anySatisfy(error -> {
                    assertThat(error.getField()).isEqualTo("name");
                });
            });
    }
    
    @Test
    @DisplayName("Should fail validation when name is blank")
    void shouldFailValidationWhenNameIsBlank() {
        // Given
        AssetRequest request = createValidAssetRequest();
        request.setName("   ");
        
        // When/Then
        assertThatThrownBy(() -> validationService.validateAssetRequest(request))
            .isInstanceOf(ValidationException.class)
            .satisfies(ex -> {
                ValidationException vex = (ValidationException) ex;
                assertThat(vex.getErrors()).anySatisfy(error -> {
                    assertThat(error.getField()).isEqualTo("name");
                });
            });
    }
    
    @Test
    @DisplayName("Should fail validation when serial number is null")
    void shouldFailValidationWhenSerialNumberIsNull() {
        // Given
        AssetRequest request = createValidAssetRequest();
        request.setSerialNumber(null);
        
        // When/Then
        assertThatThrownBy(() -> validationService.validateAssetRequest(request))
            .isInstanceOf(ValidationException.class)
            .satisfies(ex -> {
                ValidationException vex = (ValidationException) ex;
                assertThat(vex.getErrors()).anySatisfy(error -> {
                    assertThat(error.getField()).isEqualTo("serialNumber");
                    assertThat(error.getMessage()).contains("required");
                });
            });
    }
    
    @Test
    @DisplayName("Should fail validation when acquisition date is null")
    void shouldFailValidationWhenAcquisitionDateIsNull() {
        // Given
        AssetRequest request = createValidAssetRequest();
        request.setAcquisitionDate(null);
        
        // When/Then
        assertThatThrownBy(() -> validationService.validateAssetRequest(request))
            .isInstanceOf(ValidationException.class)
            .satisfies(ex -> {
                ValidationException vex = (ValidationException) ex;
                assertThat(vex.getErrors()).anySatisfy(error -> {
                    assertThat(error.getField()).isEqualTo("acquisitionDate");
                    assertThat(error.getMessage()).contains("required");
                });
            });
    }
    
    @Test
    @DisplayName("Should fail validation when status is null")
    void shouldFailValidationWhenStatusIsNull() {
        // Given
        AssetRequest request = createValidAssetRequest();
        request.setStatus(null);
        
        // When/Then
        assertThatThrownBy(() -> validationService.validateAssetRequest(request))
            .isInstanceOf(ValidationException.class)
            .satisfies(ex -> {
                ValidationException vex = (ValidationException) ex;
                assertThat(vex.getErrors()).anySatisfy(error -> {
                    assertThat(error.getField()).isEqualTo("status");
                    assertThat(error.getMessage()).contains("required");
                });
            });
    }
    
    @Test
    @DisplayName("Should collect all validation errors when multiple fields are invalid")
    void shouldCollectAllValidationErrors() {
        // Given
        AssetRequest request = new AssetRequest();
        request.setAssetType(null);
        request.setName(null);
        request.setSerialNumber(null);
        request.setAcquisitionDate(null);
        request.setStatus(null);
        
        // When/Then
        assertThatThrownBy(() -> validationService.validateAssetRequest(request))
            .isInstanceOf(ValidationException.class)
            .satisfies(ex -> {
                ValidationException vex = (ValidationException) ex;
                assertThat(vex.getErrors()).hasSize(5);
                assertThat(vex.getErrors()).extracting("field")
                    .containsExactlyInAnyOrder("assetType", "name", "serialNumber", 
                        "acquisitionDate", "status");
            });
    }
    
    // ========== Field Length Validation Tests ==========
    
    @Test
    @DisplayName("Should fail validation when name exceeds 255 characters")
    void shouldFailValidationWhenNameTooLong() {
        // Given
        AssetRequest request = createValidAssetRequest();
        request.setName("A".repeat(256));
        
        // When/Then
        assertThatThrownBy(() -> validationService.validateAssetRequest(request))
            .isInstanceOf(ValidationException.class)
            .satisfies(ex -> {
                ValidationException vex = (ValidationException) ex;
                assertThat(vex.getErrors()).anySatisfy(error -> {
                    assertThat(error.getField()).isEqualTo("name");
                    assertThat(error.getMessage()).contains("255");
                });
            });
    }
    
    @Test
    @DisplayName("Should pass validation when name is exactly 255 characters")
    void shouldPassValidationWhenNameExactly255Characters() {
        // Given
        AssetRequest request = createValidAssetRequest();
        request.setName("A".repeat(255));
        
        // When/Then
        assertThatCode(() -> validationService.validateAssetRequest(request))
            .doesNotThrowAnyException();
    }
    
    @Test
    @DisplayName("Should fail validation when serial number is less than 5 characters")
    void shouldFailValidationWhenSerialNumberTooShort() {
        // Given
        AssetRequest request = createValidAssetRequest();
        request.setSerialNumber("1234");
        
        // When/Then
        assertThatThrownBy(() -> validationService.validateAssetRequest(request))
            .isInstanceOf(ValidationException.class)
            .satisfies(ex -> {
                ValidationException vex = (ValidationException) ex;
                assertThat(vex.getErrors()).anySatisfy(error -> {
                    assertThat(error.getField()).isEqualTo("serialNumber");
                    assertThat(error.getMessage()).contains("5");
                });
            });
    }
    
    @Test
    @DisplayName("Should pass validation when serial number is exactly 5 characters")
    void shouldPassValidationWhenSerialNumberExactly5Characters() {
        // Given
        AssetRequest request = createValidAssetRequest();
        request.setSerialNumber("12345");
        
        // When/Then
        assertThatCode(() -> validationService.validateAssetRequest(request))
            .doesNotThrowAnyException();
    }
    
    @Test
    @DisplayName("Should fail validation when serial number exceeds 100 characters")
    void shouldFailValidationWhenSerialNumberTooLong() {
        // Given
        AssetRequest request = createValidAssetRequest();
        request.setSerialNumber("A".repeat(101));
        
        // When/Then
        assertThatThrownBy(() -> validationService.validateAssetRequest(request))
            .isInstanceOf(ValidationException.class)
            .satisfies(ex -> {
                ValidationException vex = (ValidationException) ex;
                assertThat(vex.getErrors()).anySatisfy(error -> {
                    assertThat(error.getField()).isEqualTo("serialNumber");
                    assertThat(error.getMessage()).contains("100");
                });
            });
    }
    
    @Test
    @DisplayName("Should pass validation when serial number is exactly 100 characters")
    void shouldPassValidationWhenSerialNumberExactly100Characters() {
        // Given
        AssetRequest request = createValidAssetRequest();
        request.setSerialNumber("A".repeat(100));
        
        // When/Then
        assertThatCode(() -> validationService.validateAssetRequest(request))
            .doesNotThrowAnyException();
    }
    
    @Test
    @DisplayName("Should fail validation when location exceeds 255 characters")
    void shouldFailValidationWhenLocationTooLong() {
        // Given
        AssetRequest request = createValidAssetRequest();
        request.setLocation("A".repeat(256));
        
        // When/Then
        assertThatThrownBy(() -> validationService.validateAssetRequest(request))
            .isInstanceOf(ValidationException.class)
            .satisfies(ex -> {
                ValidationException vex = (ValidationException) ex;
                assertThat(vex.getErrors()).anySatisfy(error -> {
                    assertThat(error.getField()).isEqualTo("location");
                    assertThat(error.getMessage()).contains("255");
                });
            });
    }
    
    @Test
    @DisplayName("Should pass validation when location is null")
    void shouldPassValidationWhenLocationIsNull() {
        // Given
        AssetRequest request = createValidAssetRequest();
        request.setLocation(null);
        
        // When/Then
        assertThatCode(() -> validationService.validateAssetRequest(request))
            .doesNotThrowAnyException();
    }
    
    // ========== Acquisition Date Validation Tests ==========
    
    @Test
    @DisplayName("Should fail validation when acquisition date is in the future")
    void shouldFailValidationWhenAcquisitionDateInFuture() {
        // Given
        AssetRequest request = createValidAssetRequest();
        request.setAcquisitionDate(LocalDate.now().plusDays(1));
        
        // When/Then
        assertThatThrownBy(() -> validationService.validateAssetRequest(request))
            .isInstanceOf(ValidationException.class)
            .satisfies(ex -> {
                ValidationException vex = (ValidationException) ex;
                assertThat(vex.getErrors()).anySatisfy(error -> {
                    assertThat(error.getField()).isEqualTo("acquisitionDate");
                    assertThat(error.getMessage()).contains("future");
                });
            });
    }
    
    @Test
    @DisplayName("Should pass validation when acquisition date is today")
    void shouldPassValidationWhenAcquisitionDateIsToday() {
        // Given
        AssetRequest request = createValidAssetRequest();
        request.setAcquisitionDate(LocalDate.now());
        
        // When/Then
        assertThatCode(() -> validationService.validateAssetRequest(request))
            .doesNotThrowAnyException();
    }
    
    @Test
    @DisplayName("Should pass validation when acquisition date is in the past")
    void shouldPassValidationWhenAcquisitionDateInPast() {
        // Given
        AssetRequest request = createValidAssetRequest();
        request.setAcquisitionDate(LocalDate.now().minusDays(1));
        
        // When/Then
        assertThatCode(() -> validationService.validateAssetRequest(request))
            .doesNotThrowAnyException();
    }
    
    // ========== Email Validation Tests ==========
    
    @Test
    @DisplayName("Should fail validation when email format is invalid")
    void shouldFailValidationWhenEmailFormatInvalid() {
        // Given
        AssetRequest request = createValidAssetRequest();
        request.setAssignedUserEmail("invalid-email");
        
        // When/Then
        assertThatThrownBy(() -> validationService.validateAssetRequest(request))
            .isInstanceOf(ValidationException.class)
            .satisfies(ex -> {
                ValidationException vex = (ValidationException) ex;
                assertThat(vex.getErrors()).anySatisfy(error -> {
                    assertThat(error.getField()).isEqualTo("assignedUserEmail");
                    assertThat(error.getMessage()).containsIgnoringCase("email");
                });
            });
    }
    
    @Test
    @DisplayName("Should pass validation when email format is valid")
    void shouldPassValidationWhenEmailFormatValid() {
        // Given
        AssetRequest request = createValidAssetRequest();
        request.setAssignedUserEmail("user@example.com");
        
        // When/Then
        assertThatCode(() -> validationService.validateAssetRequest(request))
            .doesNotThrowAnyException();
    }
    
    @Test
    @DisplayName("Should pass validation when email is null")
    void shouldPassValidationWhenEmailIsNull() {
        // Given
        AssetRequest request = createValidAssetRequest();
        request.setAssignedUserEmail(null);
        
        // When/Then
        assertThatCode(() -> validationService.validateAssetRequest(request))
            .doesNotThrowAnyException();
    }
    
    @Test
    @DisplayName("Should pass validation when email is empty")
    void shouldPassValidationWhenEmailIsEmpty() {
        // Given
        AssetRequest request = createValidAssetRequest();
        request.setAssignedUserEmail("");
        
        // When/Then
        assertThatCode(() -> validationService.validateAssetRequest(request))
            .doesNotThrowAnyException();
    }
    
    // ========== Status Transition Validation Tests ==========
    
    @Test
    @DisplayName("Should allow valid transition from ORDERED to RECEIVED")
    void shouldAllowTransitionFromOrderedToReceived() {
        // When/Then
        assertThatCode(() -> validationService.validateStatusTransition(
            LifecycleStatus.ORDERED, LifecycleStatus.RECEIVED))
            .doesNotThrowAnyException();
    }
    
    @Test
    @DisplayName("Should allow valid transition from RECEIVED to DEPLOYED")
    void shouldAllowTransitionFromReceivedToDeployed() {
        // When/Then
        assertThatCode(() -> validationService.validateStatusTransition(
            LifecycleStatus.RECEIVED, LifecycleStatus.DEPLOYED))
            .doesNotThrowAnyException();
    }
    
    @Test
    @DisplayName("Should allow valid transition from DEPLOYED to IN_USE")
    void shouldAllowTransitionFromDeployedToInUse() {
        // When/Then
        assertThatCode(() -> validationService.validateStatusTransition(
            LifecycleStatus.DEPLOYED, LifecycleStatus.IN_USE))
            .doesNotThrowAnyException();
    }
    
    @Test
    @DisplayName("Should allow valid transition from DEPLOYED to STORAGE")
    void shouldAllowTransitionFromDeployedToStorage() {
        // When/Then
        assertThatCode(() -> validationService.validateStatusTransition(
            LifecycleStatus.DEPLOYED, LifecycleStatus.STORAGE))
            .doesNotThrowAnyException();
    }
    
    @Test
    @DisplayName("Should allow valid transition from IN_USE to STORAGE")
    void shouldAllowTransitionFromInUseToStorage() {
        // When/Then
        assertThatCode(() -> validationService.validateStatusTransition(
            LifecycleStatus.IN_USE, LifecycleStatus.STORAGE))
            .doesNotThrowAnyException();
    }
    
    @Test
    @DisplayName("Should allow valid transition from IN_USE to RETIRED")
    void shouldAllowTransitionFromInUseToRetired() {
        // When/Then
        assertThatCode(() -> validationService.validateStatusTransition(
            LifecycleStatus.IN_USE, LifecycleStatus.RETIRED))
            .doesNotThrowAnyException();
    }
    
    @Test
    @DisplayName("Should allow valid transition from STORAGE to DEPLOYED")
    void shouldAllowTransitionFromStorageToDeployed() {
        // When/Then
        assertThatCode(() -> validationService.validateStatusTransition(
            LifecycleStatus.STORAGE, LifecycleStatus.DEPLOYED))
            .doesNotThrowAnyException();
    }
    
    @Test
    @DisplayName("Should allow valid transition from STORAGE to RETIRED")
    void shouldAllowTransitionFromStorageToRetired() {
        // When/Then
        assertThatCode(() -> validationService.validateStatusTransition(
            LifecycleStatus.STORAGE, LifecycleStatus.RETIRED))
            .doesNotThrowAnyException();
    }
    
    @Test
    @DisplayName("Should allow transition to MAINTENANCE from any status")
    void shouldAllowTransitionToMaintenanceFromAnyStatus() {
        // When/Then
        assertThatCode(() -> validationService.validateStatusTransition(
            LifecycleStatus.ORDERED, LifecycleStatus.MAINTENANCE))
            .doesNotThrowAnyException();
        
        assertThatCode(() -> validationService.validateStatusTransition(
            LifecycleStatus.RECEIVED, LifecycleStatus.MAINTENANCE))
            .doesNotThrowAnyException();
        
        assertThatCode(() -> validationService.validateStatusTransition(
            LifecycleStatus.DEPLOYED, LifecycleStatus.MAINTENANCE))
            .doesNotThrowAnyException();
        
        assertThatCode(() -> validationService.validateStatusTransition(
            LifecycleStatus.IN_USE, LifecycleStatus.MAINTENANCE))
            .doesNotThrowAnyException();
        
        assertThatCode(() -> validationService.validateStatusTransition(
            LifecycleStatus.STORAGE, LifecycleStatus.MAINTENANCE))
            .doesNotThrowAnyException();
    }
    
    @Test
    @DisplayName("Should allow transition from MAINTENANCE to any status except RETIRED")
    void shouldAllowTransitionFromMaintenanceToAnyStatusExceptRetired() {
        // When/Then
        assertThatCode(() -> validationService.validateStatusTransition(
            LifecycleStatus.MAINTENANCE, LifecycleStatus.ORDERED))
            .doesNotThrowAnyException();
        
        assertThatCode(() -> validationService.validateStatusTransition(
            LifecycleStatus.MAINTENANCE, LifecycleStatus.RECEIVED))
            .doesNotThrowAnyException();
        
        assertThatCode(() -> validationService.validateStatusTransition(
            LifecycleStatus.MAINTENANCE, LifecycleStatus.DEPLOYED))
            .doesNotThrowAnyException();
        
        assertThatCode(() -> validationService.validateStatusTransition(
            LifecycleStatus.MAINTENANCE, LifecycleStatus.IN_USE))
            .doesNotThrowAnyException();
        
        assertThatCode(() -> validationService.validateStatusTransition(
            LifecycleStatus.MAINTENANCE, LifecycleStatus.STORAGE))
            .doesNotThrowAnyException();
    }
    
    @Test
    @DisplayName("Should reject invalid transition from ORDERED to DEPLOYED")
    void shouldRejectTransitionFromOrderedToDeployed() {
        // When/Then
        assertThatThrownBy(() -> validationService.validateStatusTransition(
            LifecycleStatus.ORDERED, LifecycleStatus.DEPLOYED))
            .isInstanceOf(InvalidStatusTransitionException.class)
            .hasMessageContaining("ORDERED")
            .hasMessageContaining("DEPLOYED");
    }
    
    @Test
    @DisplayName("Should reject invalid transition from RECEIVED to IN_USE")
    void shouldRejectTransitionFromReceivedToInUse() {
        // When/Then
        assertThatThrownBy(() -> validationService.validateStatusTransition(
            LifecycleStatus.RECEIVED, LifecycleStatus.IN_USE))
            .isInstanceOf(InvalidStatusTransitionException.class)
            .hasMessageContaining("RECEIVED")
            .hasMessageContaining("IN_USE");
    }
    
    @Test
    @DisplayName("Should reject any transition from RETIRED status")
    void shouldRejectAnyTransitionFromRetired() {
        // When/Then
        assertThatThrownBy(() -> validationService.validateStatusTransition(
            LifecycleStatus.RETIRED, LifecycleStatus.STORAGE))
            .isInstanceOf(InvalidStatusTransitionException.class)
            .hasMessageContaining("RETIRED");
        
        assertThatThrownBy(() -> validationService.validateStatusTransition(
            LifecycleStatus.RETIRED, LifecycleStatus.MAINTENANCE))
            .isInstanceOf(InvalidStatusTransitionException.class)
            .hasMessageContaining("RETIRED");
    }
    
    @Test
    @DisplayName("Should reject transition from MAINTENANCE to RETIRED")
    void shouldRejectTransitionFromMaintenanceToRetired() {
        // When/Then
        assertThatThrownBy(() -> validationService.validateStatusTransition(
            LifecycleStatus.MAINTENANCE, LifecycleStatus.RETIRED))
            .isInstanceOf(InvalidStatusTransitionException.class)
            .hasMessageContaining("MAINTENANCE")
            .hasMessageContaining("RETIRED");
    }
    
    @Test
    @DisplayName("Should throw exception when current status is null")
    void shouldThrowExceptionWhenCurrentStatusIsNull() {
        // When/Then
        assertThatThrownBy(() -> validationService.validateStatusTransition(
            null, LifecycleStatus.RECEIVED))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("null");
    }
    
    @Test
    @DisplayName("Should throw exception when new status is null")
    void shouldThrowExceptionWhenNewStatusIsNull() {
        // When/Then
        assertThatThrownBy(() -> validationService.validateStatusTransition(
            LifecycleStatus.ORDERED, null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("null");
    }
    
    // ========== Helper Methods ==========
    
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
}
