package com.company.assetmanagement.model;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for AssignmentHistory entity validation.
 * Tests validation annotations and entity behavior.
 */
@DisplayName("AssignmentHistory Entity Tests")
class AssignmentHistoryTest {
    
    private static Validator validator;
    
    @BeforeAll
    static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }
    
    @Test
    @DisplayName("Should create valid assignment history with all required fields")
    void shouldCreateValidAssignmentHistory() {
        // Given
        UUID assetId = UUID.randomUUID();
        UUID assignedBy = UUID.randomUUID();
        AssignmentHistory assignment = new AssignmentHistory(
            assetId,
            AssignmentType.USER,
            "John Doe",
            assignedBy
        );
        
        // When
        Set<ConstraintViolation<AssignmentHistory>> violations = validator.validate(assignment);
        
        // Then
        assertThat(violations).isEmpty();
        assertThat(assignment.getAssetId()).isEqualTo(assetId);
        assertThat(assignment.getAssignmentType()).isEqualTo(AssignmentType.USER);
        assertThat(assignment.getAssignedTo()).isEqualTo("John Doe");
        assertThat(assignment.getAssignedBy()).isEqualTo(assignedBy);
        assertThat(assignment.getAssignedAt()).isNotNull();
        assertThat(assignment.getUnassignedAt()).isNull();
        assertThat(assignment.isActive()).isTrue();
    }
    
    @Test
    @DisplayName("Should fail validation when asset ID is null")
    void shouldFailValidationWhenAssetIdIsNull() {
        // Given
        AssignmentHistory assignment = new AssignmentHistory();
        assignment.setAssetId(null);
        assignment.setAssignmentType(AssignmentType.USER);
        assignment.setAssignedTo("John Doe");
        assignment.setAssignedBy(UUID.randomUUID());
        assignment.setAssignedAt(LocalDateTime.now());
        
        // When
        Set<ConstraintViolation<AssignmentHistory>> violations = validator.validate(assignment);
        
        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
            .isEqualTo("Asset ID is required");
    }
    
    @Test
    @DisplayName("Should fail validation when assignment type is null")
    void shouldFailValidationWhenAssignmentTypeIsNull() {
        // Given
        AssignmentHistory assignment = new AssignmentHistory();
        assignment.setAssetId(UUID.randomUUID());
        assignment.setAssignmentType(null);
        assignment.setAssignedTo("John Doe");
        assignment.setAssignedBy(UUID.randomUUID());
        assignment.setAssignedAt(LocalDateTime.now());
        
        // When
        Set<ConstraintViolation<AssignmentHistory>> violations = validator.validate(assignment);
        
        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
            .isEqualTo("Assignment type is required");
    }
    
    @Test
    @DisplayName("Should fail validation when assigned to is null")
    void shouldFailValidationWhenAssignedToIsNull() {
        // Given
        AssignmentHistory assignment = new AssignmentHistory();
        assignment.setAssetId(UUID.randomUUID());
        assignment.setAssignmentType(AssignmentType.USER);
        assignment.setAssignedTo(null);
        assignment.setAssignedBy(UUID.randomUUID());
        assignment.setAssignedAt(LocalDateTime.now());
        
        // When
        Set<ConstraintViolation<AssignmentHistory>> violations = validator.validate(assignment);
        
        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
            .isEqualTo("Assigned to is required");
    }
    
    @Test
    @DisplayName("Should fail validation when assigned to is blank")
    void shouldFailValidationWhenAssignedToIsBlank() {
        // Given
        AssignmentHistory assignment = new AssignmentHistory();
        assignment.setAssetId(UUID.randomUUID());
        assignment.setAssignmentType(AssignmentType.USER);
        assignment.setAssignedTo("   ");
        assignment.setAssignedBy(UUID.randomUUID());
        assignment.setAssignedAt(LocalDateTime.now());
        
        // When
        Set<ConstraintViolation<AssignmentHistory>> violations = validator.validate(assignment);
        
        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
            .isEqualTo("Assigned to is required");
    }
    
    @Test
    @DisplayName("Should fail validation when assigned to exceeds 255 characters")
    void shouldFailValidationWhenAssignedToExceedsMaxLength() {
        // Given
        String longName = "A".repeat(256);
        AssignmentHistory assignment = new AssignmentHistory();
        assignment.setAssetId(UUID.randomUUID());
        assignment.setAssignmentType(AssignmentType.USER);
        assignment.setAssignedTo(longName);
        assignment.setAssignedBy(UUID.randomUUID());
        assignment.setAssignedAt(LocalDateTime.now());
        
        // When
        Set<ConstraintViolation<AssignmentHistory>> violations = validator.validate(assignment);
        
        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
            .isEqualTo("Assigned to must not exceed 255 characters");
    }
    
    @Test
    @DisplayName("Should fail validation when assigned by is null")
    void shouldFailValidationWhenAssignedByIsNull() {
        // Given
        AssignmentHistory assignment = new AssignmentHistory();
        assignment.setAssetId(UUID.randomUUID());
        assignment.setAssignmentType(AssignmentType.USER);
        assignment.setAssignedTo("John Doe");
        assignment.setAssignedBy(null);
        assignment.setAssignedAt(LocalDateTime.now());
        
        // When
        Set<ConstraintViolation<AssignmentHistory>> violations = validator.validate(assignment);
        
        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
            .isEqualTo("Assigned by is required");
    }
    
    @Test
    @DisplayName("Should fail validation when assigned at is null")
    void shouldFailValidationWhenAssignedAtIsNull() {
        // Given
        AssignmentHistory assignment = new AssignmentHistory();
        assignment.setAssetId(UUID.randomUUID());
        assignment.setAssignmentType(AssignmentType.USER);
        assignment.setAssignedTo("John Doe");
        assignment.setAssignedBy(UUID.randomUUID());
        assignment.setAssignedAt(null);
        
        // When
        Set<ConstraintViolation<AssignmentHistory>> violations = validator.validate(assignment);
        
        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
            .isEqualTo("Assigned at timestamp is required");
    }
    
    @Test
    @DisplayName("Should pass validation with all fields at maximum valid length")
    void shouldPassValidationWithMaximumValidLength() {
        // Given
        String maxLengthName = "A".repeat(255);
        AssignmentHistory assignment = new AssignmentHistory();
        assignment.setAssetId(UUID.randomUUID());
        assignment.setAssignmentType(AssignmentType.LOCATION);
        assignment.setAssignedTo(maxLengthName);
        assignment.setAssignedBy(UUID.randomUUID());
        assignment.setAssignedAt(LocalDateTime.now());
        
        // When
        Set<ConstraintViolation<AssignmentHistory>> violations = validator.validate(assignment);
        
        // Then
        assertThat(violations).isEmpty();
    }
    
    @Test
    @DisplayName("Should report multiple validation errors when multiple fields are invalid")
    void shouldReportMultipleValidationErrors() {
        // Given
        AssignmentHistory assignment = new AssignmentHistory();
        assignment.setAssetId(null);
        assignment.setAssignmentType(null);
        assignment.setAssignedTo("");
        assignment.setAssignedBy(null);
        assignment.setAssignedAt(null);
        
        // When
        Set<ConstraintViolation<AssignmentHistory>> violations = validator.validate(assignment);
        
        // Then
        assertThat(violations).hasSize(5);
    }
    
    @Test
    @DisplayName("Should correctly identify active assignment when unassigned at is null")
    void shouldIdentifyActiveAssignment() {
        // Given
        AssignmentHistory assignment = new AssignmentHistory(
            UUID.randomUUID(),
            AssignmentType.USER,
            "John Doe",
            UUID.randomUUID()
        );
        assignment.setUnassignedAt(null);
        
        // When
        boolean isActive = assignment.isActive();
        
        // Then
        assertThat(isActive).isTrue();
    }
    
    @Test
    @DisplayName("Should correctly identify inactive assignment when unassigned at is set")
    void shouldIdentifyInactiveAssignment() {
        // Given
        AssignmentHistory assignment = new AssignmentHistory(
            UUID.randomUUID(),
            AssignmentType.USER,
            "John Doe",
            UUID.randomUUID()
        );
        assignment.setUnassignedAt(LocalDateTime.now());
        
        // When
        boolean isActive = assignment.isActive();
        
        // Then
        assertThat(isActive).isFalse();
    }
    
    @Test
    @DisplayName("Should implement equals correctly based on ID")
    void shouldImplementEqualsCorrectly() {
        // Given
        UUID id = UUID.randomUUID();
        AssignmentHistory assignment1 = new AssignmentHistory(
            UUID.randomUUID(),
            AssignmentType.USER,
            "John Doe",
            UUID.randomUUID()
        );
        assignment1.setId(id);
        
        AssignmentHistory assignment2 = new AssignmentHistory(
            UUID.randomUUID(),
            AssignmentType.LOCATION,
            "Data Center A",
            UUID.randomUUID()
        );
        assignment2.setId(id);
        
        AssignmentHistory assignment3 = new AssignmentHistory(
            UUID.randomUUID(),
            AssignmentType.USER,
            "Jane Smith",
            UUID.randomUUID()
        );
        assignment3.setId(UUID.randomUUID());
        
        // Then
        assertThat(assignment1).isEqualTo(assignment2);
        assertThat(assignment1).isNotEqualTo(assignment3);
        assertThat(assignment1).isEqualTo(assignment1);
        assertThat(assignment1).isNotEqualTo(null);
        assertThat(assignment1).isNotEqualTo(new Object());
    }
    
    @Test
    @DisplayName("Should implement hashCode correctly based on ID")
    void shouldImplementHashCodeCorrectly() {
        // Given
        UUID id = UUID.randomUUID();
        AssignmentHistory assignment1 = new AssignmentHistory(
            UUID.randomUUID(),
            AssignmentType.USER,
            "John Doe",
            UUID.randomUUID()
        );
        assignment1.setId(id);
        
        AssignmentHistory assignment2 = new AssignmentHistory(
            UUID.randomUUID(),
            AssignmentType.LOCATION,
            "Data Center A",
            UUID.randomUUID()
        );
        assignment2.setId(id);
        
        // Then
        assertThat(assignment1.hashCode()).isEqualTo(assignment2.hashCode());
    }
    
    @Test
    @DisplayName("Should generate meaningful toString representation")
    void shouldGenerateMeaningfulToString() {
        // Given
        UUID assetId = UUID.randomUUID();
        UUID assignedBy = UUID.randomUUID();
        AssignmentHistory assignment = new AssignmentHistory(
            assetId,
            AssignmentType.USER,
            "John Doe",
            assignedBy
        );
        
        // When
        String toString = assignment.toString();
        
        // Then
        assertThat(toString).contains("AssignmentHistory");
        assertThat(toString).contains("assetId=" + assetId);
        assertThat(toString).contains("assignmentType=USER");
        assertThat(toString).contains("assignedTo='John Doe'");
        assertThat(toString).contains("assignedBy=" + assignedBy);
        assertThat(toString).contains("active=true");
    }
    
    @Test
    @DisplayName("Should initialize assigned at timestamp in default constructor")
    void shouldInitializeAssignedAtInDefaultConstructor() {
        // Given
        LocalDateTime before = LocalDateTime.now().minusSeconds(1);
        
        // When
        AssignmentHistory assignment = new AssignmentHistory();
        
        // Then
        LocalDateTime after = LocalDateTime.now().plusSeconds(1);
        assertThat(assignment.getAssignedAt()).isNotNull();
        assertThat(assignment.getAssignedAt()).isAfter(before);
        assertThat(assignment.getAssignedAt()).isBefore(after);
    }
    
    @Test
    @DisplayName("Should initialize assigned at timestamp in parameterized constructor")
    void shouldInitializeAssignedAtInParameterizedConstructor() {
        // Given
        LocalDateTime before = LocalDateTime.now().minusSeconds(1);
        
        // When
        AssignmentHistory assignment = new AssignmentHistory(
            UUID.randomUUID(),
            AssignmentType.USER,
            "John Doe",
            UUID.randomUUID()
        );
        
        // Then
        LocalDateTime after = LocalDateTime.now().plusSeconds(1);
        assertThat(assignment.getAssignedAt()).isNotNull();
        assertThat(assignment.getAssignedAt()).isAfter(before);
        assertThat(assignment.getAssignedAt()).isBefore(after);
    }
    
    @Test
    @DisplayName("Should support USER assignment type")
    void shouldSupportUserAssignmentType() {
        // Given
        AssignmentHistory assignment = new AssignmentHistory(
            UUID.randomUUID(),
            AssignmentType.USER,
            "John Doe",
            UUID.randomUUID()
        );
        
        // When
        Set<ConstraintViolation<AssignmentHistory>> violations = validator.validate(assignment);
        
        // Then
        assertThat(violations).isEmpty();
        assertThat(assignment.getAssignmentType()).isEqualTo(AssignmentType.USER);
    }
    
    @Test
    @DisplayName("Should support LOCATION assignment type")
    void shouldSupportLocationAssignmentType() {
        // Given
        AssignmentHistory assignment = new AssignmentHistory(
            UUID.randomUUID(),
            AssignmentType.LOCATION,
            "Data Center A",
            UUID.randomUUID()
        );
        
        // When
        Set<ConstraintViolation<AssignmentHistory>> violations = validator.validate(assignment);
        
        // Then
        assertThat(violations).isEmpty();
        assertThat(assignment.getAssignmentType()).isEqualTo(AssignmentType.LOCATION);
    }
}
