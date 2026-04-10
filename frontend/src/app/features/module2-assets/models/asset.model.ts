import { AssetType } from './asset-type.enum';
import { LifecycleStatus } from './lifecycle-status.enum';

/**
 * Asset model representing complete asset information.
 * Used for displaying asset data from the backend.
 * 
 * Matches backend AssetDTO structure exactly.
 */
export interface Asset {
  /** Unique identifier of the asset (UUID format) */
  id: string;
  
  /** Type of the asset */
  assetType: AssetType;
  
  /** Name of the asset */
  name: string;
  
  /** Unique serial number of the asset (immutable after creation) */
  serialNumber: string;
  
  /** Date when the asset was acquired (ISO 8601 format: YYYY-MM-DD) */
  acquisitionDate: string;
  
  /** Current lifecycle status of the asset */
  status: LifecycleStatus;
  
  /** Physical location of the asset */
  location?: string;
  
  /** Name of the user to whom the asset is assigned */
  assignedUser?: string;
  
  /** Email address of the assigned user */
  assignedUserEmail?: string;
  
  /** Date and time when the asset was assigned to the current user (ISO 8601 format) */
  assignmentDate?: string;
  
  /** Date and time when the asset location was last updated (ISO 8601 format) */
  locationUpdateDate?: string;
  
  /** Additional notes or comments about the asset */
  notes?: string;
  
  /** Custom fields in JSON format for extensibility */
  customFields?: string;
  
  /** URL of the asset image */
  imageUrl?: string;
  
  /** Original filename of the uploaded image */
  imageFilename?: string;
  
  /** Size of the image file in bytes */
  imageSize?: number;
  
  /** MIME type of the image */
  imageContentType?: string;
  
  /** Timestamp when the asset was created (ISO 8601 format) */
  createdAt: string;
  
  /** User ID who created the asset */
  createdBy: string;
  
  /** Timestamp when the asset was last updated (ISO 8601 format) */
  updatedAt: string;
  
  /** User ID who last updated the asset */
  updatedBy: string;
  
  /** Indicates if the asset is read-only (e.g., retired assets) */
  readOnly: boolean;
}
