/**
 * Enumeration of asset types supported by the system.
 * Defines the 15 standard asset types for IT infrastructure management.
 * 
 * Must match backend AssetType enum exactly.
 */
export enum AssetType {
  SERVER = 'SERVER',
  WORKSTATION = 'WORKSTATION',
  NETWORK_DEVICE = 'NETWORK_DEVICE',
  STORAGE_DEVICE = 'STORAGE_DEVICE',
  SOFTWARE_LICENSE = 'SOFTWARE_LICENSE',
  PERIPHERAL = 'PERIPHERAL',
  KEYBOARD = 'KEYBOARD',
  MOUSE = 'MOUSE',
  LAPTOP = 'LAPTOP',
  MONITOR = 'MONITOR',
  HEADSET = 'HEADSET',
  LAPTOP_CHARGER = 'LAPTOP_CHARGER',
  HDMI_CABLE = 'HDMI_CABLE',
  NETWORK_CABLE = 'NETWORK_CABLE',
  ACCESS_CARD = 'ACCESS_CARD'
}
