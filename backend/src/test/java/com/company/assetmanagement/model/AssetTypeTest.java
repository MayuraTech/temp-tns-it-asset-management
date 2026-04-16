package com.company.assetmanagement.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for AssetType enum.
 * Tests asset type enumeration according to Requirement 1.
 */
@DisplayName("AssetType Tests")
class AssetTypeTest {

    @Test
    @DisplayName("Should have 15 asset types")
    void shouldHaveFifteenAssetTypes() {
        assertThat(AssetType.values()).hasSize(15);
    }

    @Test
    @DisplayName("Should get correct string values for all asset types")
    void shouldGetCorrectStringValues() {
        assertThat(AssetType.SERVER.getValue()).isEqualTo("server");
        assertThat(AssetType.WORKSTATION.getValue()).isEqualTo("workstation");
        assertThat(AssetType.NETWORK_DEVICE.getValue()).isEqualTo("network_device");
        assertThat(AssetType.STORAGE_DEVICE.getValue()).isEqualTo("storage_device");
        assertThat(AssetType.SOFTWARE_LICENSE.getValue()).isEqualTo("software_license");
        assertThat(AssetType.PERIPHERAL.getValue()).isEqualTo("peripheral");
        assertThat(AssetType.KEYBOARD.getValue()).isEqualTo("keyboard");
        assertThat(AssetType.MOUSE.getValue()).isEqualTo("mouse");
        assertThat(AssetType.LAPTOP.getValue()).isEqualTo("laptop");
        assertThat(AssetType.MONITOR.getValue()).isEqualTo("monitor");
        assertThat(AssetType.HEADSET.getValue()).isEqualTo("headset");
        assertThat(AssetType.LAPTOP_CHARGER.getValue()).isEqualTo("laptop_charger");
        assertThat(AssetType.HDMI_CABLE.getValue()).isEqualTo("hdmi_cable");
        assertThat(AssetType.NETWORK_CABLE.getValue()).isEqualTo("network_cable");
        assertThat(AssetType.ACCESS_CARD.getValue()).isEqualTo("access_card");
    }

    @Test
    @DisplayName("Should convert from string value to enum")
    void shouldConvertFromStringValue() {
        assertThat(AssetType.fromValue("server")).isEqualTo(AssetType.SERVER);
        assertThat(AssetType.fromValue("workstation")).isEqualTo(AssetType.WORKSTATION);
        assertThat(AssetType.fromValue("network_device")).isEqualTo(AssetType.NETWORK_DEVICE);
        assertThat(AssetType.fromValue("storage_device")).isEqualTo(AssetType.STORAGE_DEVICE);
        assertThat(AssetType.fromValue("software_license")).isEqualTo(AssetType.SOFTWARE_LICENSE);
        assertThat(AssetType.fromValue("peripheral")).isEqualTo(AssetType.PERIPHERAL);
        assertThat(AssetType.fromValue("keyboard")).isEqualTo(AssetType.KEYBOARD);
        assertThat(AssetType.fromValue("mouse")).isEqualTo(AssetType.MOUSE);
        assertThat(AssetType.fromValue("laptop")).isEqualTo(AssetType.LAPTOP);
        assertThat(AssetType.fromValue("monitor")).isEqualTo(AssetType.MONITOR);
        assertThat(AssetType.fromValue("headset")).isEqualTo(AssetType.HEADSET);
        assertThat(AssetType.fromValue("laptop_charger")).isEqualTo(AssetType.LAPTOP_CHARGER);
        assertThat(AssetType.fromValue("hdmi_cable")).isEqualTo(AssetType.HDMI_CABLE);
        assertThat(AssetType.fromValue("network_cable")).isEqualTo(AssetType.NETWORK_CABLE);
        assertThat(AssetType.fromValue("access_card")).isEqualTo(AssetType.ACCESS_CARD);
    }

    @Test
    @DisplayName("Should throw exception for invalid string value")
    void shouldThrowExceptionForInvalidValue() {
        assertThatThrownBy(() -> AssetType.fromValue("invalid_type"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Unknown asset type: invalid_type");
    }

    @Test
    @DisplayName("Should throw exception for null value")
    void shouldThrowExceptionForNullValue() {
        assertThatThrownBy(() -> AssetType.fromValue(null))
            .isInstanceOf(NullPointerException.class);
    }

    @Test
    @DisplayName("Should be case-sensitive when converting from string")
    void shouldBeCaseSensitiveWhenConverting() {
        assertThatThrownBy(() -> AssetType.fromValue("SERVER"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Unknown asset type: SERVER");
    }

    @Test
    @DisplayName("Should have all required asset types for IT infrastructure")
    void shouldHaveAllRequiredAssetTypes() {
        // Verify all 15 required types exist
        assertThat(AssetType.values())
            .containsExactlyInAnyOrder(
                AssetType.SERVER,
                AssetType.WORKSTATION,
                AssetType.NETWORK_DEVICE,
                AssetType.STORAGE_DEVICE,
                AssetType.SOFTWARE_LICENSE,
                AssetType.PERIPHERAL,
                AssetType.KEYBOARD,
                AssetType.MOUSE,
                AssetType.LAPTOP,
                AssetType.MONITOR,
                AssetType.HEADSET,
                AssetType.LAPTOP_CHARGER,
                AssetType.HDMI_CABLE,
                AssetType.NETWORK_CABLE,
                AssetType.ACCESS_CARD
            );
    }
}
