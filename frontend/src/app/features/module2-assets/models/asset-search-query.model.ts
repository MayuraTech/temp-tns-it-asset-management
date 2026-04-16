import { AssetType } from './asset-type.enum';
import { LifecycleStatus } from './lifecycle-status.enum';

/**
 * Asset search query model for filtering and searching assets.
 * Supports filtering by multiple criteria for advanced search functionality.
 * 
 * Matches backend AssetSearchQuery structure exactly.
 */
export interface AssetSearchQuery {
  /** Full-text search across name, serial number, and location */
  text?: string;
  
  /** Filter by asset types (multiple values supported) */
  assetTypes?: AssetType[];
  
  /** Filter by lifecycle statuses (multiple values supported) */
  statuses?: LifecycleStatus[];
  
  /** Filter by exact location */
  location?: string;
  
  /** Filter by acquisition date from (ISO 8601 format: YYYY-MM-DD) */
  acquisitionDateFrom?: string;
  
  /** Filter by acquisition date to (ISO 8601 format: YYYY-MM-DD) */
  acquisitionDateTo?: string;
  
  /** Filter by assigned user name */
  assignedUser?: string;
}
