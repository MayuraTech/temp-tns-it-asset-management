/**
 * Ticket models and enums
 */

/**
 * Ticket type enumeration
 */
export enum TicketType {
  ALLOCATION = 'ALLOCATION',
  DEALLOCATION = 'DEALLOCATION'
}

/**
 * Ticket status enumeration - 6 statuses for ticket workflow
 */
export enum TicketStatus {
  PENDING = 'PENDING',
  APPROVED = 'APPROVED',
  REJECTED = 'REJECTED',
  COMPLETED = 'COMPLETED',
  CANCELLED = 'CANCELLED'
}

/**
 * Ticket priority enumeration - 4 priority levels
 */
export enum TicketPriority {
  LOW = 'LOW',
  MEDIUM = 'MEDIUM',
  HIGH = 'HIGH',
  URGENT = 'URGENT'
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
  assetId: string;
  assetName: string;
  assetSerialNumber: string;
  requesterId: string;
  requesterName: string;
  assignToUser?: string;
  assignToUserEmail?: string;
  assignToLocation?: string;
  requestReason?: string;
  deallocationReason?: string;
  approverId?: string;
  approverName?: string;
  approvalComments?: string;
  rejectionReason?: string;
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
  assignToUser?: string;
  assignToUserEmail?: string;
  assignToLocation?: string;
  requestReason: string;
  priority: TicketPriority;
}

/**
 * De-allocation ticket creation request interface
 */
export interface DeallocationTicketRequest {
  assetId: string;
  deallocationReason: string;
  priority: TicketPriority;
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
  changedAt: Date | string;
  comments?: string;
}

/**
 * Ticket metrics interface
 */
export interface TicketMetrics {
  totalTickets: number;
  ticketsByStatus: Record<TicketStatus, number>;
  ticketsByType: Record<TicketType, number>;
  ticketsByPriority: Record<TicketPriority, number>;
  averageApprovalTimeHours: number;
  averageCompletionTimeHours: number;
  approvalRate: number;
  rejectionRate: number;
}
