/**
 * Assignment History Entry Model
 * 
 * Represents a single assignment period in an asset's assignment history.
 * Tracks who had the asset, when they got it, and for how long.
 */
export interface AssignmentHistoryEntry {
  /** Unique identifier for the assignment entry */
  id: string;
  
  /** Name of the user who was assigned the asset */
  assignedUser: string;
  
  /** Email of the user who was assigned the asset */
  assignedUserEmail: string;
  
  /** Date when the assignment started */
  assignmentDate: string;
  
  /** Date when the assignment ended (null for current assignment) */
  unassignmentDate?: string;
  
  /** Duration of the assignment in human-readable format */
  duration: string;
  
  /** Duration in days (for sorting and calculations) */
  durationInDays: number;
  
  /** Status of the assignment */
  status: AssignmentStatus;
  
  /** Location where the asset was during this assignment */
  location?: string;
  
  /** Additional notes about the assignment */
  notes?: string;
  
  /** User who created this assignment */
  assignedBy?: string;
  
  /** Reason for unassignment (if applicable) */
  unassignmentReason?: string;
}

/**
 * Status of an assignment
 */
export enum AssignmentStatus {
  /** Currently assigned to this user */
  CURRENT = 'CURRENT',
  
  /** Previously assigned to this user */
  PAST = 'PAST',
  
  /** Assignment was cancelled/revoked */
  CANCELLED = 'CANCELLED'
}

/**
 * Summary statistics for assignment history
 */
export interface AssignmentHistorySummary {
  /** Total number of assignments */
  totalAssignments: number;
  
  /** Current assignment (if any) */
  currentAssignment?: AssignmentHistoryEntry;
  
  /** Average assignment duration in days */
  averageDurationDays: number;
  
  /** Longest assignment duration in days */
  longestDurationDays: number;
  
  /** Total time asset has been assigned (in days) */
  totalAssignedDays: number;
  
  /** Most frequent assignee */
  mostFrequentAssignee?: string;
}

/**
 * Query parameters for filtering assignment history
 */
export interface AssignmentHistoryQuery {
  /** Filter by assignment status */
  status?: AssignmentStatus;
  
  /** Filter assignments from this date (ISO string) */
  dateFrom?: string;
  
  /** Filter assignments to this date (ISO string) */
  dateTo?: string;
  
  /** Filter by assigned user */
  assignedUser?: string;
  
  /** Page number for pagination (zero-based) */
  page?: number;
  
  /** Number of items per page */
  size?: number;
}