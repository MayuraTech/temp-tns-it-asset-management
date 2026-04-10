package com.company.assetmanagement.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("ValidationUtil Tests")
class ValidationUtilTest {
    
    @Test
    @DisplayName("Should validate null or empty strings")
    void shouldValidateNullOrEmpty() {
        assertThat(ValidationUtil.isNullOrEmpty(null)).isTrue();
        assertThat(ValidationUtil.isNullOrEmpty("")).isTrue();
        assertThat(ValidationUtil.isNullOrEmpty("   ")).isTrue();
        assertThat(ValidationUtil.isNullOrEmpty("test")).isFalse();
    }
    
    @Test
    @DisplayName("Should validate not null or empty strings")
    void shouldValidateNotNullOrEmpty() {
        assertThat(ValidationUtil.isNotNullOrEmpty("test")).isTrue();
        assertThat(ValidationUtil.isNotNullOrEmpty(null)).isFalse();
        assertThat(ValidationUtil.isNotNullOrEmpty("")).isFalse();
        assertThat(ValidationUtil.isNotNullOrEmpty("   ")).isFalse();
    }
    
    @Test
    @DisplayName("Should validate email format")
    void shouldValidateEmailFormat() {
        // Valid emails
        assertThat(ValidationUtil.isValidEmail("user@example.com")).isTrue();
        assertThat(ValidationUtil.isValidEmail("test.user@example.com")).isTrue();
        assertThat(ValidationUtil.isValidEmail("user+tag@example.co.uk")).isTrue();
        
        // Invalid emails
        assertThat(ValidationUtil.isValidEmail("invalid")).isFalse();
        assertThat(ValidationUtil.isValidEmail("@example.com")).isFalse();
        assertThat(ValidationUtil.isValidEmail("user@")).isFalse();
        assertThat(ValidationUtil.isValidEmail("user@.com")).isFalse();
        assertThat(ValidationUtil.isValidEmail(null)).isFalse();
        assertThat(ValidationUtil.isValidEmail("")).isFalse();
    }
    
    @Test
    @DisplayName("Should validate password complexity")
    void shouldValidatePasswordComplexity() {
        // Valid passwords
        assertThat(ValidationUtil.isValidPassword("Admin@123456")).isTrue();
        assertThat(ValidationUtil.isValidPassword("SecurePass123!")).isTrue();
        assertThat(ValidationUtil.isValidPassword("MyP@ssw0rd123")).isTrue();
        
        // Invalid passwords - too short
        assertThat(ValidationUtil.isValidPassword("Short1!")).isFalse();
        
        // Invalid passwords - missing uppercase
        assertThat(ValidationUtil.isValidPassword("lowercase123!")).isFalse();
        
        // Invalid passwords - missing lowercase
        assertThat(ValidationUtil.isValidPassword("UPPERCASE123!")).isFalse();
        
        // Invalid passwords - missing digit
        assertThat(ValidationUtil.isValidPassword("NoDigits!@#")).isFalse();
        
        // Invalid passwords - missing special character
        assertThat(ValidationUtil.isValidPassword("NoSpecial123")).isFalse();
        
        assertThat(ValidationUtil.isValidPassword(null)).isFalse();
        assertThat(ValidationUtil.isValidPassword("")).isFalse();
    }
    
    @Test
    @DisplayName("Should validate alphanumeric strings")
    void shouldValidateAlphanumeric() {
        assertThat(ValidationUtil.isAlphanumeric("ABC123")).isTrue();
        assertThat(ValidationUtil.isAlphanumeric("test")).isTrue();
        assertThat(ValidationUtil.isAlphanumeric("123")).isTrue();
        
        assertThat(ValidationUtil.isAlphanumeric("test-123")).isFalse();
        assertThat(ValidationUtil.isAlphanumeric("test 123")).isFalse();
        assertThat(ValidationUtil.isAlphanumeric("test@123")).isFalse();
        assertThat(ValidationUtil.isAlphanumeric(null)).isFalse();
        assertThat(ValidationUtil.isAlphanumeric("")).isFalse();
    }
    
    @Test
    @DisplayName("Should validate string length in range")
    void shouldValidateLengthInRange() {
        assertThat(ValidationUtil.isLengthInRange("test", 1, 10)).isTrue();
        assertThat(ValidationUtil.isLengthInRange("test", 4, 4)).isTrue();
        assertThat(ValidationUtil.isLengthInRange("test", 5, 10)).isFalse();
        assertThat(ValidationUtil.isLengthInRange("test", 1, 3)).isFalse();
        assertThat(ValidationUtil.isLengthInRange(null, 1, 10)).isFalse();
    }
    
    @Test
    @DisplayName("Should validate pattern matching")
    void shouldValidatePatternMatching() {
        assertThat(ValidationUtil.matchesPattern("ABC123", "^[A-Z0-9]+$")).isTrue();
        assertThat(ValidationUtil.matchesPattern("test", "^[a-z]+$")).isTrue();
        assertThat(ValidationUtil.matchesPattern("test123", "^[a-z]+$")).isFalse();
        assertThat(ValidationUtil.matchesPattern(null, "^[a-z]+$")).isFalse();
        assertThat(ValidationUtil.matchesPattern("test", null)).isFalse();
    }
    
    @Test
    @DisplayName("Should validate serial number format")
    void shouldValidateSerialNumberFormat() {
        // Valid serial numbers
        assertThat(ValidationUtil.isValidSerialNumberFormat("SRV-001", "SERVER")).isTrue();
        assertThat(ValidationUtil.isValidSerialNumberFormat("WKS-12345", "WORKSTATION")).isTrue();
        assertThat(ValidationUtil.isValidSerialNumberFormat("ABC123DEF", "SERVER")).isTrue();
        
        // Invalid - too short
        assertThat(ValidationUtil.isValidSerialNumberFormat("ABC", "SERVER")).isFalse();
        
        // Invalid - too long (over 100 chars)
        String longSerial = "A".repeat(101);
        assertThat(ValidationUtil.isValidSerialNumberFormat(longSerial, "SERVER")).isFalse();
        
        // Invalid - contains special characters
        assertThat(ValidationUtil.isValidSerialNumberFormat("SRV@001", "SERVER")).isFalse();
        assertThat(ValidationUtil.isValidSerialNumberFormat("SRV 001", "SERVER")).isFalse();
        
        // Invalid - null or empty
        assertThat(ValidationUtil.isValidSerialNumberFormat(null, "SERVER")).isFalse();
        assertThat(ValidationUtil.isValidSerialNumberFormat("", "SERVER")).isFalse();
        assertThat(ValidationUtil.isValidSerialNumberFormat("SRV-001", null)).isFalse();
    }
    
    @Test
    @DisplayName("Should validate numeric range")
    void shouldValidateNumericRange() {
        assertThat(ValidationUtil.isInRange(5, 1, 10)).isTrue();
        assertThat(ValidationUtil.isInRange(1, 1, 10)).isTrue();
        assertThat(ValidationUtil.isInRange(10, 1, 10)).isTrue();
        assertThat(ValidationUtil.isInRange(0, 1, 10)).isFalse();
        assertThat(ValidationUtil.isInRange(11, 1, 10)).isFalse();
    }
    
    @Test
    @DisplayName("Should validate positive numbers")
    void shouldValidatePositiveNumbers() {
        assertThat(ValidationUtil.isPositive(1)).isTrue();
        assertThat(ValidationUtil.isPositive(100)).isTrue();
        assertThat(ValidationUtil.isPositive(0)).isFalse();
        assertThat(ValidationUtil.isPositive(-1)).isFalse();
    }
    
    @Test
    @DisplayName("Should validate non-negative numbers")
    void shouldValidateNonNegativeNumbers() {
        assertThat(ValidationUtil.isNonNegative(0)).isTrue();
        assertThat(ValidationUtil.isNonNegative(1)).isTrue();
        assertThat(ValidationUtil.isNonNegative(100)).isTrue();
        assertThat(ValidationUtil.isNonNegative(-1)).isFalse();
    }
}
