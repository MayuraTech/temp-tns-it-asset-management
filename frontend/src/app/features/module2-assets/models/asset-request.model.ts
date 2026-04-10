import { AssetType } from './asset-type.enum';
import { LifecycleStatus } from './lifecycle-status.enum';

/**
 * Asset request model for creating or updating assets.
 * Used when sending asset data to the backend.
 * 
 * Matches backend AssetRequest structure exactly.
 */
export interface AssetRequest {
  /** Type of the asset (required) */
  assetType: AssetType;
  
  /** Name of the asset (required, 1-255 characters) */
  name: string;
  
  /** Unique serial number of the asset (required, 5-100 characters) */
  serialNumber: string;
  
  /** Date when the asset was acquired (required, cannot be in the future, ISO 8601 format: YYYY-MM-DD) */
  acquisitionDate: string;
  
  /** Initial lifecycle status of the asset (required) */
  status: LifecycleStatus;
  
  /** Physical location of the asset (optional, max 255 characters) */
  location?: string;
  
  /** Name of the user to whom the asset is assigned (optional, max 255 characters) */
  assignedUser?: string;
  
  /** Email address of the assigned user (optional, must be valid email format, max 255 characters) */
  assignedUserEmail?: string;
  
  /** Date and time when the asset was assigned to the current user (optional, ISO 8601 format) */
  assignmentDate?: string;
  
  /** Date and time when the asset location was last updated (optional, ISO 8601 format) */
  locationUpdateDate?: string;
  
  /** Additional notes or comments about the asset (optional) */
  notes?: string;
  
  /** Custom fields in JSON format for extensibility (optional) */
  customFields?: string;
}
