package com.company.assetmanagement.model;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for the Asset entity.
 * Tests validation annotations, business logic methods, equals/hashCode, and toString.
 */
@DisplayName("Asset Entity Tests")
class AssetTest {
    
    private Validator validator;
    
    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }
    
    @Test
    @DisplayName("Should create asset with valid required fields")
    void shouldCreateAssetWithValidRequiredFields() {
        // Given
        Asset asset = new Asset(
            AssetType.SERVER,
            "Test Server",
            "SRV-001",
            LocalDate.now(),
            LifecycleStatus.ORDERED
        );
        asset.setCreatedBy(UUID.randomUUID());
        asset.setUpdatedBy(UUID.randomUUID());
        
        // When
        Set<ConstraintViolation<Asset>> violations = validator.validate(asset);
        
        // Then
        assertThat(violations).isEmpty();
        assertThat(asset.getAssetType()).isEqualTo(AssetType.SERVER);
        assertThat(asset.getName()).isEqualTo("Test Server");
        assertThat(asset.getSerialNumber()).isEqualTo("SRV-001");
        assertThat(asset.getStatus()).isEqualTo(LifecycleStatus.ORDERED);
        assertThat(asset.isReadOnly()).isFalse();
    }
    
    @Test
    @DisplayName("Should fail validation when asset type is null")
    void shouldFailValidationWhenAssetTypeIsNull() {
        // Given
        Asset asset = new Asset();
        asset.setAssetType(null);
        asset.setName("Test Server");
        asset.setSerialNumber("SRV-001");
        asset.setAcquisitionDate(LocalDate.now());
        asset.setStatus(LifecycleStatus.ORDERED);
        asset.setCreatedBy(UUID.randomUUID());
        asset.setUpdatedBy(UUID.randomUUID());
        
        // When
        Set<ConstraintViolation<Asset>> violations = validator.validate(asset);
        
        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
            .isEqualTo("Asset type is required");
    }
    
    @Test
    @DisplayName("Should fail validation when name is blank")
    void shouldFailValidationWhenNameIsBlank() {
        // Given
        Asset asset = new Asset();
        asset.setAssetType(AssetType.SERVER);
        asset.setName("");
        asset.setSerialNumber("SRV-001");
        asset.setAcquisitionDate(LocalDate.now());
        asset.setStatus(LifecycleStatus.ORDERED);
        asset.setCreatedBy(UUID.randomUUID());
        asset.setUpdatedBy(UUID.randomUUID());
        
        // When
        Set<ConstraintViolation<Asset>> violations = validator.validate(asset);
        
        // Then
        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> 
            v.getMessage().contains("Name is required") || 
            v.getMessage().contains("Name must be between 1 and 255 characters")
        );
    }
    
    @Test
    @DisplayName("Should fail validation when name exceeds 255 characters")
    void shouldFailValidationWhenNameExceedsMaxLength() {
        // Given
        Asset asset = new Asset();
        asset.setAssetType(AssetType.SERVER);
        asset.setName("A".repeat(256));
        asset.setSerialNumber("SRV-001");
        asset.setAcquisitionDate(LocalDate.now());
        asset.setStatus(LifecycleStatus.ORDERED);
        asset.setCreatedBy(UUID.randomUUID());
        asset.setUpdatedBy(UUID.randomUUID());
        
        // When
        Set<ConstraintViolation<Asset>> violations = validator.validate(asset);
        
        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
            .contains("Name must be between 1 and 255 characters");
    }
    
    @Test
    @DisplayName("Should fail validation when serial number is too short")
    void shouldFailValidationWhenSerialNumberIsTooShort() {
        // Given
        Asset asset = new Asset();
        asset.setAssetType(AssetType.SERVER);
        asset.setName("Test Server");
        asset.setSerialNumber("SRV");
        asset.setAcquisitionDate(LocalDate.now());
        asset.setStatus(LifecycleStatus.ORDERED);
        asset.setCreatedBy(UUID.randomUUID());
        asset.setUpdatedBy(UUID.randomUUID());
        
        // When
        Set<ConstraintViolation<Asset>> violations = validator.validate(asset);
        
        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
            .contains("Serial number must be between 5 and 100 characters");
    }
    
    @Test
    @DisplayName("Should fail validation when serial number exceeds 100 characters")
    void shouldFailValidationWhenSerialNumberExceedsMaxLength() {
        // Given
        Asset asset = new Asset();
        asset.setAssetType(AssetType.SERVER);
        asset.setName("Test Server");
        asset.setSerialNumber("A".repeat(101));
        asset.setAcquisitionDate(LocalDate.now());
        asset.setStatus(LifecycleStatus.ORDERED);
        asset.setCreatedBy(UUID.randomUUID());
        asset.setUpdatedBy(UUID.randomUUID());
        
        // When
        Set<ConstraintViolation<Asset>> violations = validator.validate(asset);
        
        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
            .contains("Serial number must be between 5 and 100 characters");
    }
    
    @Test
    @DisplayName("Should fail validation when acquisition date is in the future")
    void shouldFailValidationWhenAcquisitionDateIsInFuture() {
        // Given
        Asset asset = new Asset();
        asset.setAssetType(AssetType.SERVER);
        asset.setName("Test Server");
        asset.setSerialNumber("SRV-001");
        asset.setAcquisitionDate(LocalDate.now().plusDays(1));
        asset.setStatus(LifecycleStatus.ORDERED);
        asset.setCreatedBy(UUID.randomUUID());
        asset.setUpdatedBy(UUID.randomUUID());
        
        // When
        Set<ConstraintViolation<Asset>> violations = validator.validate(asset);
        
        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
            .isEqualTo("Acquisition date cannot be in the future");
    }
    
    @Test
    @DisplayName("Should fail validation when email format is invalid")
    void shouldFailValidationWhenEmailFormatIsInvalid() {
        // Given
        Asset asset = new Asset();
        asset.setAssetType(AssetType.SERVER);
        asset.setName("Test Server");
        asset.setSerialNumber("SRV-001");
        asset.setAcquisitionDate(LocalDate.now());
        asset.setStatus(LifecycleStatus.ORDERED);
        asset.setAssignedUserEmail("invalid-email");
        asset.setCreatedBy(UUID.randomUUID());
        asset.setUpdatedBy(UUID.randomUUID());
        
        // When
        Set<ConstraintViolation<Asset>> violations = validator.validate(asset);
        
        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
            .isEqualTo("Invalid email format");
    }
    
    @Test
    @DisplayName("Should pass validation with valid email")
    void shouldPassValidationWithValidEmail() {
        // Given
        Asset asset = new Asset();
        asset.setAssetType(AssetType.SERVER);
        asset.setName("Test Server");
        asset.setSerialNumber("SRV-001");
        asset.setAcquisitionDate(LocalDate.now());
        asset.setStatus(LifecycleStatus.ORDERED);
        asset.setAssignedUserEmail("user@example.com");
        asset.setCreatedBy(UUID.randomUUID());
        asset.setUpdatedBy(UUID.randomUUID());
        
        // When
        Set<ConstraintViolation<Asset>> violations = validator.validate(asset);
        
        // Then
        assertThat(violations).isEmpty();
    }
    
    @Test
    @DisplayName("Should return true when asset is assigned")
    void shouldReturnTrueWhenAssetIsAssigned() {
        // Given
        Asset asset = new Asset();
        asset.setAssignedUser("John Doe");
        
        // When
        boolean isAssigned = asset.isAssigned();
        
        // Then
        assertThat(isAssigned).isTrue();
    }
    
    @Test
    @DisplayName("Should return false when asset is not assigned")
    void shouldReturnFalseWhenAssetIsNotAssigned() {
        // Given
        Asset asset = new Asset();
        asset.setAssignedUser(null);
        
        // When
        boolean isAssigned = asset.isAssigned();
        
        // Then
        assertThat(isAssigned).isFalse();
    }
    
    @Test
    @DisplayName("Should return false when assigned user is blank")
    void shouldReturnFalseWhenAssignedUserIsBlank() {
        // Given
        Asset asset = new Asset();
        asset.setAssignedUser("   ");
        
        // When
        boolean isAssigned = asset.isAssigned();
        
        // Then
        assertThat(isAssigned).isFalse();
    }
    
    @Test
    @DisplayName("Should return true when asset is retired")
    void shouldReturnTrueWhenAssetIsRetired() {
        // Given
        Asset asset = new Asset();
        asset.setStatus(LifecycleStatus.RETIRED);
        
        // When
        boolean isRetired = asset.isRetired();
        
        // Then
        assertThat(isRetired).isTrue();
    }
    
    @Test
    @DisplayName("Should return false when asset is not retired")
    void shouldReturnFalseWhenAssetIsNotRetired() {
        // Given
        Asset asset = new Asset();
        asset.setStatus(LifecycleStatus.IN_USE);
        
        // When
        boolean isRetired = asset.isRetired();
        
        // Then
        assertThat(isRetired).isFalse();
    }
    
    @Test
    @DisplayName("Should return true when asset can be modified")
    void shouldReturnTrueWhenAssetCanBeModified() {
        // Given
        Asset asset = new Asset();
        asset.setReadOnly(false);
        
        // When
        boolean canBeModified = asset.canBeModified();
        
        // Then
        assertThat(canBeModified).isTrue();
    }
    
    @Test
    @DisplayName("Should return false when asset is read-only")
    void shouldReturnFalseWhenAssetIsReadOnly() {
        // Given
        Asset asset = new Asset();
        asset.setReadOnly(true);
        
        // When
        boolean canBeModified = asset.canBeModified();
        
        // Then
        assertThat(canBeModified).isFalse();
    }
    
    @Test
    @DisplayName("Should be equal when serial numbers match")
    void shouldBeEqualWhenSerialNumbersMatch() {
        // Given
        Asset asset1 = new Asset();
        asset1.setSerialNumber("SRV-001");
        
        Asset asset2 = new Asset();
        asset2.setSerialNumber("SRV-001");
        
        // When & Then
        assertThat(asset1).isEqualTo(asset2);
        assertThat(asset1.hashCode()).isEqualTo(asset2.hashCode());
    }
    
    @Test
    @DisplayName("Should not be equal when serial numbers differ")
    void shouldNotBeEqualWhenSerialNumbersDiffer() {
        // Given
        Asset asset1 = new Asset();
        asset1.setSerialNumber("SRV-001");
        
        Asset asset2 = new Asset();
        asset2.setSerialNumber("SRV-002");
        
        // When & Then
        assertThat(asset1).isNotEqualTo(asset2);
    }
    
    @Test
    @DisplayName("Should be equal to itself")
    void shouldBeEqualToItself() {
        // Given
        Asset asset = new Asset();
        asset.setSerialNumber("SRV-001");
        
        // When & Then
        assertThat(asset).isEqualTo(asset);
    }
    
    @Test
    @DisplayName("Should not be equal to null")
    void shouldNotBeEqualToNull() {
        // Given
        Asset asset = new Asset();
        asset.setSerialNumber("SRV-001");
        
        // When & Then
        assertThat(asset).isNotEqualTo(null);
    }
    
    @Test
    @DisplayName("Should not be equal to different class")
    void shouldNotBeEqualToDifferentClass() {
        // Given
        Asset asset = new Asset();
        asset.setSerialNumber("SRV-001");
        String differentClass = "SRV-001";
        
        // When & Then
        assertThat(asset).isNotEqualTo(differentClass);
    }
    
    @Test
    @DisplayName("Should generate toString with all fields")
    void shouldGenerateToStringWithAllFields() {
        // Given
        UUID id = UUID.randomUUID();
        UUID createdBy = UUID.randomUUID();
        UUID updatedBy = UUID.randomUUID();
        LocalDate acquisitionDate = LocalDate.of(2024, 1, 15);
        LocalDateTime createdAt = LocalDateTime.of(2024, 1, 15, 10, 30);
        LocalDateTime updatedAt = LocalDateTime.of(2024, 1, 15, 11, 30);
        LocalDateTime assignmentDate = LocalDateTime.of(2024, 1, 16, 9, 0);
        LocalDateTime locationUpdateDate = LocalDateTime.of(2024, 1, 16, 10, 0);
        
        Asset asset = new Asset();
        asset.setId(id);
        asset.setAssetType(AssetType.SERVER);
        asset.setName("Test Server");
        asset.setSerialNumber("SRV-001");
        asset.setAcquisitionDate(acquisitionDate);
        asset.setStatus(LifecycleStatus.IN_USE);
        asset.setLocation("Data Center A");
        asset.setAssignedUser("John Doe");
        asset.setAssignedUserEmail("john.doe@example.com");
        asset.setAssignmentDate(assignmentDate);
        asset.setLocationUpdateDate(locationUpdateDate);
        asset.setCreatedAt(createdAt);
        asset.setCreatedBy(createdBy);
        asset.setUpdatedAt(updatedAt);
        asset.setUpdatedBy(updatedBy);
        asset.setReadOnly(false);
        
        // When
        String toString = asset.toString();
        
        // Then
        assertThat(toString).contains("Asset{");
        assertThat(toString).contains("id=" + id);
        assertThat(toString).contains("assetType=SERVER");
        assertThat(toString).contains("name='Test Server'");
        assertThat(toString).contains("serialNumber='SRV-001'");
        assertThat(toString).contains("status=IN_USE");
        assertThat(toString).contains("location='Data Center A'");
        assertThat(toString).contains("assignedUser='John Doe'");
        assertThat(toString).contains("assignedUserEmail='john.doe@example.com'");
        assertThat(toString).contains("readOnly=false");
    }
    
    @Test
    @DisplayName("Should set all optional fields correctly")
    void shouldSetAllOptionalFieldsCorrectly() {
        // Given
        Asset asset = new Asset();
        LocalDateTime now = LocalDateTime.now();
        
        // When
        asset.setLocation("Data Center A");
        asset.setAssignedUser("John Doe");
        asset.setAssignedUserEmail("john.doe@example.com");
        asset.setAssignmentDate(now);
        asset.setLocationUpdateDate(now);
        asset.setNotes("Test notes");
        asset.setCustomFields("{\"key\":\"value\"}");
        
        // Then
        assertThat(asset.getLocation()).isEqualTo("Data Center A");
        assertThat(asset.getAssignedUser()).isEqualTo("John Doe");
        assertThat(asset.getAssignedUserEmail()).isEqualTo("john.doe@example.com");
        assertThat(asset.getAssignmentDate()).isEqualTo(now);
        assertThat(asset.getLocationUpdateDate()).isEqualTo(now);
        assertThat(asset.getNotes()).isEqualTo("Test notes");
        assertThat(asset.getCustomFields()).isEqualTo("{\"key\":\"value\"}");
    }
    
    @Test
    @DisplayName("Should initialize readOnly to false by default")
    void shouldInitializeReadOnlyToFalseByDefault() {
        // Given & When
        Asset asset = new Asset();
        
        // Then
        assertThat(asset.isReadOnly()).isFalse();
    }
}
