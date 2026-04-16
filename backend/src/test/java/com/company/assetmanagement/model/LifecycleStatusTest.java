package com.company.assetmanagement.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for LifecycleStatus enum.
 * Tests status transition validation logic according to Requirement 4.
 */
@DisplayName("LifecycleStatus Tests")
class LifecycleStatusTest {

    @Test
    @DisplayName("Should have 7 lifecycle statuses")
    void shouldHaveSevenStatuses() {
        assertThat(LifecycleStatus.values()).hasSize(7);
    }

    @Test
    @DisplayName("Should get correct string values for all statuses")
    void shouldGetCorrectStringValues() {
        assertThat(LifecycleStatus.ORDERED.getValue()).isEqualTo("ordered");
        assertThat(LifecycleStatus.RECEIVED.getValue()).isEqualTo("received");
        assertThat(LifecycleStatus.DEPLOYED.getValue()).isEqualTo("deployed");
        assertThat(LifecycleStatus.IN_USE.getValue()).isEqualTo("in_use");
        assertThat(LifecycleStatus.MAINTENANCE.getValue()).isEqualTo("maintenance");
        assertThat(LifecycleStatus.STORAGE.getValue()).isEqualTo("storage");
        assertThat(LifecycleStatus.RETIRED.getValue()).isEqualTo("retired");
    }

    @Test
    @DisplayName("Should convert from string value to enum")
    void shouldConvertFromStringValue() {
        assertThat(LifecycleStatus.fromValue("ordered")).isEqualTo(LifecycleStatus.ORDERED);
        assertThat(LifecycleStatus.fromValue("received")).isEqualTo(LifecycleStatus.RECEIVED);
        assertThat(LifecycleStatus.fromValue("deployed")).isEqualTo(LifecycleStatus.DEPLOYED);
        assertThat(LifecycleStatus.fromValue("in_use")).isEqualTo(LifecycleStatus.IN_USE);
        assertThat(LifecycleStatus.fromValue("maintenance")).isEqualTo(LifecycleStatus.MAINTENANCE);
        assertThat(LifecycleStatus.fromValue("storage")).isEqualTo(LifecycleStatus.STORAGE);
        assertThat(LifecycleStatus.fromValue("retired")).isEqualTo(LifecycleStatus.RETIRED);
    }

    @Test
    @DisplayName("Should throw exception for invalid string value")
    void shouldThrowExceptionForInvalidValue() {
        assertThatThrownBy(() -> LifecycleStatus.fromValue("invalid"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Unknown lifecycle status: invalid");
    }

    // Valid Transition Tests

    @Test
    @DisplayName("ORDERED should transition to RECEIVED")
    void orderedShouldTransitionToReceived() {
        assertThat(LifecycleStatus.ORDERED.canTransitionTo(LifecycleStatus.RECEIVED)).isTrue();
    }

    @Test
    @DisplayName("RECEIVED should transition to DEPLOYED")
    void receivedShouldTransitionToDeployed() {
        assertThat(LifecycleStatus.RECEIVED.canTransitionTo(LifecycleStatus.DEPLOYED)).isTrue();
    }

    @Test
    @DisplayName("DEPLOYED should transition to IN_USE")
    void deployedShouldTransitionToInUse() {
        assertThat(LifecycleStatus.DEPLOYED.canTransitionTo(LifecycleStatus.IN_USE)).isTrue();
    }

    @Test
    @DisplayName("DEPLOYED should transition to STORAGE")
    void deployedShouldTransitionToStorage() {
        assertThat(LifecycleStatus.DEPLOYED.canTransitionTo(LifecycleStatus.STORAGE)).isTrue();
    }

    @Test
    @DisplayName("IN_USE should transition to STORAGE")
    void inUseShouldTransitionToStorage() {
        assertThat(LifecycleStatus.IN_USE.canTransitionTo(LifecycleStatus.STORAGE)).isTrue();
    }

    @Test
    @DisplayName("IN_USE should transition to RETIRED")
    void inUseShouldTransitionToRetired() {
        assertThat(LifecycleStatus.IN_USE.canTransitionTo(LifecycleStatus.RETIRED)).isTrue();
    }

    @Test
    @DisplayName("STORAGE should transition to DEPLOYED")
    void storageShouldTransitionToDeployed() {
        assertThat(LifecycleStatus.STORAGE.canTransitionTo(LifecycleStatus.DEPLOYED)).isTrue();
    }

    @Test
    @DisplayName("STORAGE should transition to RETIRED")
    void storageShouldTransitionToRetired() {
        assertThat(LifecycleStatus.STORAGE.canTransitionTo(LifecycleStatus.RETIRED)).isTrue();
    }

    @ParameterizedTest
    @EnumSource(value = LifecycleStatus.class, names = {"ORDERED", "RECEIVED", "DEPLOYED", "IN_USE", "STORAGE"})
    @DisplayName("Any status (except RETIRED) should transition to MAINTENANCE")
    void anyStatusShouldTransitionToMaintenance(LifecycleStatus status) {
        assertThat(status.canTransitionTo(LifecycleStatus.MAINTENANCE)).isTrue();
    }

    @ParameterizedTest
    @EnumSource(value = LifecycleStatus.class, names = {"ORDERED", "RECEIVED", "DEPLOYED", "IN_USE", "STORAGE"})
    @DisplayName("MAINTENANCE should transition back to any status except RETIRED")
    void maintenanceShouldTransitionToAnyStatusExceptRetired(LifecycleStatus targetStatus) {
        assertThat(LifecycleStatus.MAINTENANCE.canTransitionTo(targetStatus)).isTrue();
    }

    // Invalid Transition Tests

    @Test
    @DisplayName("RETIRED should not transition to any status")
    void retiredShouldNotTransitionToAnyStatus() {
        assertThat(LifecycleStatus.RETIRED.canTransitionTo(LifecycleStatus.ORDERED)).isFalse();
        assertThat(LifecycleStatus.RETIRED.canTransitionTo(LifecycleStatus.RECEIVED)).isFalse();
        assertThat(LifecycleStatus.RETIRED.canTransitionTo(LifecycleStatus.DEPLOYED)).isFalse();
        assertThat(LifecycleStatus.RETIRED.canTransitionTo(LifecycleStatus.IN_USE)).isFalse();
        assertThat(LifecycleStatus.RETIRED.canTransitionTo(LifecycleStatus.MAINTENANCE)).isFalse();
        assertThat(LifecycleStatus.RETIRED.canTransitionTo(LifecycleStatus.STORAGE)).isFalse();
        assertThat(LifecycleStatus.RETIRED.canTransitionTo(LifecycleStatus.RETIRED)).isFalse();
    }

    @Test
    @DisplayName("MAINTENANCE should not transition to RETIRED")
    void maintenanceShouldNotTransitionToRetired() {
        assertThat(LifecycleStatus.MAINTENANCE.canTransitionTo(LifecycleStatus.RETIRED)).isFalse();
    }

    @Test
    @DisplayName("ORDERED should not transition to DEPLOYED")
    void orderedShouldNotTransitionToDeployed() {
        assertThat(LifecycleStatus.ORDERED.canTransitionTo(LifecycleStatus.DEPLOYED)).isFalse();
    }

    @Test
    @DisplayName("ORDERED should not transition to IN_USE")
    void orderedShouldNotTransitionToInUse() {
        assertThat(LifecycleStatus.ORDERED.canTransitionTo(LifecycleStatus.IN_USE)).isFalse();
    }

    @Test
    @DisplayName("ORDERED should not transition to STORAGE")
    void orderedShouldNotTransitionToStorage() {
        assertThat(LifecycleStatus.ORDERED.canTransitionTo(LifecycleStatus.STORAGE)).isFalse();
    }

    @Test
    @DisplayName("ORDERED should not transition to RETIRED")
    void orderedShouldNotTransitionToRetired() {
        assertThat(LifecycleStatus.ORDERED.canTransitionTo(LifecycleStatus.RETIRED)).isFalse();
    }

    @Test
    @DisplayName("RECEIVED should not transition to IN_USE")
    void receivedShouldNotTransitionToInUse() {
        assertThat(LifecycleStatus.RECEIVED.canTransitionTo(LifecycleStatus.IN_USE)).isFalse();
    }

    @Test
    @DisplayName("RECEIVED should not transition to STORAGE")
    void receivedShouldNotTransitionToStorage() {
        assertThat(LifecycleStatus.RECEIVED.canTransitionTo(LifecycleStatus.STORAGE)).isFalse();
    }

    @Test
    @DisplayName("RECEIVED should not transition to RETIRED")
    void receivedShouldNotTransitionToRetired() {
        assertThat(LifecycleStatus.RECEIVED.canTransitionTo(LifecycleStatus.RETIRED)).isFalse();
    }

    @Test
    @DisplayName("DEPLOYED should not transition to RECEIVED")
    void deployedShouldNotTransitionToReceived() {
        assertThat(LifecycleStatus.DEPLOYED.canTransitionTo(LifecycleStatus.RECEIVED)).isFalse();
    }

    @Test
    @DisplayName("DEPLOYED should not transition to RETIRED")
    void deployedShouldNotTransitionToRetired() {
        assertThat(LifecycleStatus.DEPLOYED.canTransitionTo(LifecycleStatus.RETIRED)).isFalse();
    }

    @Test
    @DisplayName("IN_USE should not transition to ORDERED")
    void inUseShouldNotTransitionToOrdered() {
        assertThat(LifecycleStatus.IN_USE.canTransitionTo(LifecycleStatus.ORDERED)).isFalse();
    }

    @Test
    @DisplayName("IN_USE should not transition to RECEIVED")
    void inUseShouldNotTransitionToReceived() {
        assertThat(LifecycleStatus.IN_USE.canTransitionTo(LifecycleStatus.RECEIVED)).isFalse();
    }

    @Test
    @DisplayName("IN_USE should not transition to DEPLOYED")
    void inUseShouldNotTransitionToDeployed() {
        assertThat(LifecycleStatus.IN_USE.canTransitionTo(LifecycleStatus.DEPLOYED)).isFalse();
    }

    @Test
    @DisplayName("STORAGE should not transition to ORDERED")
    void storageShouldNotTransitionToOrdered() {
        assertThat(LifecycleStatus.STORAGE.canTransitionTo(LifecycleStatus.ORDERED)).isFalse();
    }

    @Test
    @DisplayName("STORAGE should not transition to RECEIVED")
    void storageShouldNotTransitionToReceived() {
        assertThat(LifecycleStatus.STORAGE.canTransitionTo(LifecycleStatus.RECEIVED)).isFalse();
    }

    @Test
    @DisplayName("STORAGE should not transition to IN_USE")
    void storageShouldNotTransitionToInUse() {
        assertThat(LifecycleStatus.STORAGE.canTransitionTo(LifecycleStatus.IN_USE)).isFalse();
    }

    // Comprehensive transition matrix test
    @ParameterizedTest
    @CsvSource({
        "ORDERED, RECEIVED, true",
        "ORDERED, DEPLOYED, false",
        "ORDERED, IN_USE, false",
        "ORDERED, MAINTENANCE, true",
        "ORDERED, STORAGE, false",
        "ORDERED, RETIRED, false",
        "RECEIVED, ORDERED, false",
        "RECEIVED, DEPLOYED, true",
        "RECEIVED, IN_USE, false",
        "RECEIVED, MAINTENANCE, true",
        "RECEIVED, STORAGE, false",
        "RECEIVED, RETIRED, false",
        "DEPLOYED, ORDERED, false",
        "DEPLOYED, RECEIVED, false",
        "DEPLOYED, IN_USE, true",
        "DEPLOYED, MAINTENANCE, true",
        "DEPLOYED, STORAGE, true",
        "DEPLOYED, RETIRED, false",
        "IN_USE, ORDERED, false",
        "IN_USE, RECEIVED, false",
        "IN_USE, DEPLOYED, false",
        "IN_USE, MAINTENANCE, true",
        "IN_USE, STORAGE, true",
        "IN_USE, RETIRED, true",
        "MAINTENANCE, ORDERED, true",
        "MAINTENANCE, RECEIVED, true",
        "MAINTENANCE, DEPLOYED, true",
        "MAINTENANCE, IN_USE, true",
        "MAINTENANCE, STORAGE, true",
        "MAINTENANCE, RETIRED, false",
        "STORAGE, ORDERED, false",
        "STORAGE, RECEIVED, false",
        "STORAGE, DEPLOYED, true",
        "STORAGE, IN_USE, false",
        "STORAGE, MAINTENANCE, true",
        "STORAGE, RETIRED, true",
        "RETIRED, ORDERED, false",
        "RETIRED, RECEIVED, false",
        "RETIRED, DEPLOYED, false",
        "RETIRED, IN_USE, false",
        "RETIRED, MAINTENANCE, false",
        "RETIRED, STORAGE, false",
        "RETIRED, RETIRED, false"
    })
    @DisplayName("Should validate all status transitions according to transition matrix")
    void shouldValidateAllStatusTransitions(LifecycleStatus from, LifecycleStatus to, boolean expected) {
        assertThat(from.canTransitionTo(to))
            .as("Transition from %s to %s should be %s", from, to, expected ? "allowed" : "forbidden")
            .isEqualTo(expected);
    }
}
