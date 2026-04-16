/**
 * Ticket models and enums for Module 4 - Ticket Management
 */

/**
 * Ticket type enumeration
 */
export enum TicketType {
  ALLOCATION = 'ALLOCATION',
  DEALLOCATION = 'DEALLOCATION'
}

/**
 * Ticket status enumeration - 5 statuses for ticket workflow
 */
export enum TicketStatus {
  PENDING = 'PENDING',
  APPROVED = 'APPROVED',
  REJECTED = 'REJECTED',
  COMPLETED = 'COMPLETED',
  CANCELLED = 'CANCELLED'
}

/**
 * Ticket priority enumeration - 3 priority levels
 * STANDARD maps to MEDIUM internally for backward compatibility
 */
export enum TicketPriority {
  LOW = 'LOW',
  STANDARD = 'STANDARD',
  URGENT = 'URGENT'
}

/**
 * Notification type enumeration
 */
export enum NotificationType {
  TICKET_APPROVED = 'TICKET_APPROVED',
  TICKET_REJECTED = 'TICKET_REJECTED',
  TICKET_COMPLETED = 'TICKET_COMPLETED',
  TICKET_CANCELLED = 'TICKET_CANCELLED'
}

/**
 * Ticket interface representing an asset allocation/de-allocation request
 */
export interface Ticket {
  id: string;
  ticketNumber: string;
  type: TicketType;
  status: TicketStatus;
  priority: TicketPriority;
  
  // Asset Information
  assetId: string;
  assetName: string;
  assetSerialNumber: string;
  
  // Requester Information
  requesterId: string;
  requesterName: string;
  requesterEmail: string;
  
  // Assignment Information (for allocation tickets)
  assignToUser?: string;
  assignToUserEmail?: string;
  assignToLocation?: string;
  
  // Request Details
  requestReason?: string;
  deallocationReason?: string;
  
  // Approver Information
  approverId?: string;
  approverName?: string;
  approvalComments?: string;
  rejectionReason?: string;
  
  // Audit Fields
  createdAt: Date | string;
  updatedAt: Date | string;
  approvedAt?: Date | string;
  rejectedAt?: Date | string;
  completedAt?: Date | string;
  cancelledAt?: Date | string;
}

/**
 * Allocation ticket creation request interface
 */
export interface AllocationTicketRequest {
  assetId: string;
  priority: TicketPriority;
  requestReason: string;
  assignToUser?: string;
  assignToUserEmail?: string;
  assignToLocation?: string;
}

/**
 * De-allocation ticket creation request interface
 */
export interface DeallocationTicketRequest {
  assetId: string;
  priority: TicketPriority;
  deallocationReason: string;
}

/**
 * Ticket search query interface for filtering tickets
 */
export interface TicketSearchQuery {
  statuses?: TicketStatus[];
  types?: TicketType[];
  priorities?: TicketPriority[];
  requesterId?: string;
  approverId?: string;
  assetId?: string;
  createdFrom?: string;
  createdTo?: string;
}

/**
 * Ticket status history entry interface
 */
export interface TicketStatusHistory {
  id: string;
  ticketId: string;
  fromStatus: TicketStatus | null;
  toStatus: TicketStatus;
  changedBy: string;
  changedByName: string;
  changedAt: Date | string;
  comments?: string;
}

/**
 * Notification interface for ticket status changes
 */
export interface Notification {
  id: string;
  userId: string;
  ticketId: string;
  ticketNumber: string;
  assetName: string;
  notificationType: NotificationType;
  message: string;
  isRead: boolean;
  createdAt: Date | string;
}

/**
 * Ticket metrics interface for analytics
 */
export interface TicketMetrics {
  totalTickets: number;
  ticketsByStatus: Record<string, number>;
  ticketsByType: Record<string, number>;
  ticketsByPriority: Record<string, number>;
  averageApprovalTimeHours: number;
  averageCompletionTimeHours: number;
  approvalRate: number;
  rejectionRate: number;
}

/**
 * Bulk operation result interface
 */
export interface BulkOperationResult {
  totalProcessed: number;
  successCount: number;
  failureCount: number;
  failures: FailureDetail[];
}

/**
 * Failure detail interface for bulk operations
 */
export interface FailureDetail {
  ticketId: string;
  ticketNumber: string;
  errorMessage: string;
}
