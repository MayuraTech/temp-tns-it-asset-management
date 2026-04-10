/**
 * Asset models and enums
 */

/**
 * Asset type enumeration - 15 standard types for IT infrastructure
 */
export enum AssetType {
  SERVER = 'SERVER',
  WORKSTATION = 'WORKSTATION',
  LAPTOP = 'LAPTOP',
  TABLET = 'TABLET',
  MOBILE_DEVICE = 'MOBILE_DEVICE',
  PRINTER = 'PRINTER',
  SCANNER = 'SCANNER',
  NETWORK_DEVICE = 'NETWORK_DEVICE',
  STORAGE_DEVICE = 'STORAGE_DEVICE',
  MONITOR = 'MONITOR',
  PROJECTOR = 'PROJECTOR',
  PHONE_SYSTEM = 'PHONE_SYSTEM',
  UPS = 'UPS',
  RACK = 'RACK',
  OTHER = 'OTHER'
}

/**
 * Asset lifecycle status enumeration - 7 stages from acquisition to retirement
 */
export enum LifecycleStatus {
  ORDERED = 'ORDERED',
  RECEIVED = 'RECEIVED',
  IN_USE = 'IN_USE',
  IN_MAINTENANCE = 'IN_MAINTENANCE',
  IN_STORAGE = 'IN_STORAGE',
  RETIRED = 'RETIRED',
  DISPOSED = 'DISPOSED'
}

/**
 * Asset interface representing an IT infrastructure asset
 */
export interface Asset {
  id: string;
  assetType: AssetType;
  name: string;
  serialNumber: string;
  acquisitionDate: Date | string;
  status: LifecycleStatus;
  location?: string;
  assignedUser?: string;
  assignedUserEmail?: string;
  assignmentDate?: Date | string;
  locationUpdateDate?: Date | string;
  notes?: string;
  customFields?: Record<string, any>;
  createdAt: Date | string;
  createdBy: string;
  updatedAt: Date | string;
  updatedBy: string;
  readOnly: boolean;
}

/**
 * Asset creation/update request interface
 */
export interface AssetRequest {
  assetType: AssetType;
  name: string;
  serialNumber: string;
  acquisitionDate: Date | string;
  status: LifecycleStatus;
  location?: string;
  assignedUser?: string;
  assignedUserEmail?: string;
  notes?: string;
  customFields?: Record<string, any>;
}

/**
 * Assignment history entry interface
 */
export interface AssignmentHistory {
  id: string;
  assetId: string;
  assignmentType: 'USER' | 'LOCATION';
  assignedTo: string;
  assignedBy: string;
  assignedAt: Date | string;
  unassignedAt?: Date | string;
}
