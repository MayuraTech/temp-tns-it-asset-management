/**
 * Module 2 Assets - Models Index
 * 
 * Central export point for all asset-related models, enums, and interfaces.
 * This allows for clean imports throughout the application:
 * 
 * Example:
 * import { Asset, AssetType, LifecycleStatus, AssetRequest } from '@features/module2-assets/models';
 */

// Enums
export { AssetType } from './asset-type.enum';
export { LifecycleStatus } from './lifecycle-status.enum';
export { AssetHistoryEventType } from './asset-history-event.model';
export { AssignmentStatus } from './assignment-history-entry.model';

// Models
export { Asset } from './asset.model';
export { AssetRequest } from './asset-request.model';
export { AssetSearchQuery } from './asset-search-query.model';
export { Page, PageInfo, SpringPagePayload, mapSpringPageToAppPage } from './page.model';

// History Models
export { 
  AssetHistoryEvent, 
  FieldChange, 
  AssetHistoryQuery 
} from './asset-history-event.model';

export { 
  AssignmentHistoryEntry, 
  AssignmentHistoryQuery, 
  AssignmentHistorySummary 
} from './assignment-history-entry.model';
