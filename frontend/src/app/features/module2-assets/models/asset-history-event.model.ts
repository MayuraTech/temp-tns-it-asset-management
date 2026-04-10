/**
 * Asset History Event Model
 * 
 * Represents a single event in an asset's history timeline.
 * Used for tracking all changes and events that occur to an asset over time.
 */
export interface AssetHistoryEvent {
  /** Unique identifier for the history event */
  id: string;
  
  /** Timestamp when the event occurred */
  timestamp: string;
  
  /** Type of event that occurred */
  eventType: AssetHistoryEventType;
  
  /** ID of the user who performed the action */
  userId: string;
  
  /** Name of the user who performed the action */
  userName: string;
  
  /** Human-readable description of the event */
  description: string;
  
  /** List of field changes (for update events) */
  fieldChanges?: FieldChange[];
  
  /** Additional metadata about the event */
  metadata?: Record<string, any>;
}

/**
 * Types of events that can occur in asset history
 */
export enum AssetHistoryEventType {
  CREATED = 'CREATED',
  UPDATED = 'UPDATED',
  STATUS_CHANGED = 'STATUS_CHANGED',
  ASSIGNED = 'ASSIGNED',
  UNASSIGNED = 'UNASSIGNED',
  LOCATION_CHANGED = 'LOCATION_CHANGED',
  DELETED = 'DELETED'
}

/**
 * Represents a change to a specific field
 */
export interface FieldChange {
  /** Name of the field that changed */
  fieldName: string;
  
  /** Previous value of the field */
  oldValue: any;
  
  /** New value of the field */
  newValue: any;
  
  /** Display name for the field (user-friendly) */
  displayName?: string;
}

/**
 * Query parameters for filtering asset history
 */
export interface AssetHistoryQuery {
  /** Filter by event type */
  eventType?: AssetHistoryEventType;
  
  /** Filter events from this date (ISO string) */
  dateFrom?: string;
  
  /** Filter events to this date (ISO string) */
  dateTo?: string;
  
  /** Page number for pagination (zero-based) */
  page?: number;
  
  /** Number of items per page */
  size?: number;
}