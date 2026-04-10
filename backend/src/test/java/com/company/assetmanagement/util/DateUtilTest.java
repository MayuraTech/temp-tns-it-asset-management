package com.company.assetmanagement.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;

import static org.assertj.core.api.Assertions.*;

@DisplayName("DateUtil Tests")
class DateUtilTest {
    
    @Test
    @DisplayName("Should validate date is not in future")
    void shouldValidateDateNotInFuture() {
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);
        LocalDate tomorrow = today.plusDays(1);
        
        assertThat(DateUtil.isNotInFuture(today)).isTrue();
        assertThat(DateUtil.isNotInFuture(yesterday)).isTrue();
        assertThat(DateUtil.isNotInFuture(tomorrow)).isFalse();
        assertThat(DateUtil.isNotInFuture(null)).isFalse();
    }
    
    @Test
    @DisplayName("Should validate date is in future")
    void shouldValidateDateInFuture() {
        LocalDate today = LocalDate.now();
        LocalDate tomorrow = today.plusDays(1);
        LocalDate yesterday = today.minusDays(1);
        
        assertThat(DateUtil.isInFuture(tomorrow)).isTrue();
        assertThat(DateUtil.isInFuture(today)).isFalse();
        assertThat(DateUtil.isInFuture(yesterday)).isFalse();
        assertThat(DateUtil.isInFuture(null)).isFalse();
    }
    
    @Test
    @DisplayName("Should validate date is in past")
    void shouldValidateDateInPast() {
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);
        LocalDate tomorrow = today.plusDays(1);
        
        assertThat(DateUtil.isInPast(yesterday)).isTrue();
        assertThat(DateUtil.isInPast(today)).isFalse();
        assertThat(DateUtil.isInPast(tomorrow)).isFalse();
        assertThat(DateUtil.isInPast(null)).isFalse();
    }
    
    @Test
    @DisplayName("Should format date to ISO format")
    void shouldFormatDateToISO() {
        LocalDate date = LocalDate.of(2024, 1, 15);
        
        String formatted = DateUtil.formatDate(date);
        
        assertThat(formatted).isEqualTo("2024-01-15");
        assertThat(DateUtil.formatDate(null)).isNull();
    }
    
    @Test
    @DisplayName("Should format datetime to ISO format")
    void shouldFormatDateTimeToISO() {
        LocalDateTime dateTime = LocalDateTime.of(2024, 1, 15, 10, 30, 45);
        
        String formatted = DateUtil.formatDateTime(dateTime);
        
        assertThat(formatted).isEqualTo("2024-01-15T10:30:45");
        assertThat(DateUtil.formatDateTime(null)).isNull();
    }
    
    @Test
    @DisplayName("Should parse date from ISO format")
    void shouldParseDateFromISO() {
        String dateString = "2024-01-15";
        
        LocalDate parsed = DateUtil.parseDate(dateString);
        
        assertThat(parsed).isEqualTo(LocalDate.of(2024, 1, 15));
        assertThat(DateUtil.parseDate(null)).isNull();
        assertThat(DateUtil.parseDate("")).isNull();
        assertThat(DateUtil.parseDate("   ")).isNull();
    }
    
    @Test
    @DisplayName("Should throw exception for invalid date format")
    void shouldThrowExceptionForInvalidDateFormat() {
        assertThatThrownBy(() -> DateUtil.parseDate("invalid-date"))
            .isInstanceOf(DateTimeParseException.class);
    }
    
    @Test
    @DisplayName("Should parse datetime from ISO format")
    void shouldParseDateTimeFromISO() {
        String dateTimeString = "2024-01-15T10:30:45";
        
        LocalDateTime parsed = DateUtil.parseDateTime(dateTimeString);
        
        assertThat(parsed).isEqualTo(LocalDateTime.of(2024, 1, 15, 10, 30, 45));
        assertThat(DateUtil.parseDateTime(null)).isNull();
        assertThat(DateUtil.parseDateTime("")).isNull();
    }
    
    @Test
    @DisplayName("Should validate date string format")
    void shouldValidateDateStringFormat() {
        assertThat(DateUtil.isValidDate("2024-01-15")).isTrue();
        assertThat(DateUtil.isValidDate("invalid-date")).isFalse();
        assertThat(DateUtil.isValidDate(null)).isFalse();
        assertThat(DateUtil.isValidDate("")).isFalse();
        assertThat(DateUtil.isValidDate("   ")).isFalse();
    }
    
    @Test
    @DisplayName("Should validate datetime string format")
    void shouldValidateDateTimeStringFormat() {
        assertThat(DateUtil.isValidDateTime("2024-01-15T10:30:45")).isTrue();
        assertThat(DateUtil.isValidDateTime("invalid-datetime")).isFalse();
        assertThat(DateUtil.isValidDateTime(null)).isFalse();
        assertThat(DateUtil.isValidDateTime("")).isFalse();
    }
    
    @Test
    @DisplayName("Should format date for display")
    void shouldFormatDateForDisplay() {
        LocalDate date = LocalDate.of(2024, 1, 15);
        
        String formatted = DateUtil.formatDateForDisplay(date);
        
        assertThat(formatted).isEqualTo("2024-01-15");
    }
    
    @Test
    @DisplayName("Should format datetime for display")
    void shouldFormatDateTimeForDisplay() {
        LocalDateTime dateTime = LocalDateTime.of(2024, 1, 15, 10, 30, 45);
        
        String formatted = DateUtil.formatDateTimeForDisplay(dateTime);
        
        assertThat(formatted).isEqualTo("2024-01-15 10:30:45");
    }
}
