/**
 * Allocation management models and interfaces
 */

/**
 * Assignment type enumeration
 */
export enum AssignmentType {
  USER = 'USER',
  LOCATION = 'LOCATION'
}

/**
 * Assignment interface representing an asset assignment
 */
export interface Assignment {
  id: string;
  assetId: string;
  assignmentType: AssignmentType;
  assignedTo: string;
  assignedBy: string;
  assignedAt: Date | string;
  unassignedAt?: Date | string;
}

/**
 * Assignment request interface for creating assignments
 */
export interface AssignmentRequest {
  assignmentType: AssignmentType;
  assignedTo: string;
  assignedUserEmail?: string; // Required for USER assignments
}

/**
 * Assignment history DTO interface
 */
export interface AssignmentHistoryDTO {
  id: string;
  assetId: string;
  assignmentType: AssignmentType;
  assignedTo: string;
  assignedBy: string;
  assignedByUsername?: string;
  assignedAt: Date | string;
  unassignedAt?: Date | string;
}

/**
 * Assignment statistics interface
 */
export interface AssignmentStatistics {
  totalAssigned: number;
  userAssignments: number;
  locationAssignments: number;
  availableAssets: AvailableAssetsByStatus;
  topUsers: TopAssignee[];
  topLocations: TopAssignee[];
}

/**
 * Available assets by status interface
 */
export interface AvailableAssetsByStatus {
  inUse: number;
  deployed: number;
  storage: number;
}

/**
 * Top assignee interface (user or location)
 */
export interface TopAssignee {
  name: string;
  count: number;
}

/**
 * Bulk deallocation result interface
 */
export interface BulkDeallocationResult {
  successful: Assignment[];
  failed: BulkDeallocationError[];
  successCount: number;
  failureCount: number;
}

/**
 * Bulk deallocation error interface
 */
export interface BulkDeallocationError {
  assetId: string;
  error: string;
}

/**
 * Export filters interface
 */
export interface ExportFilters {
  assignmentType?: AssignmentType;
  dateFrom?: string;
  dateTo?: string;
  assignedBy?: string;
}
