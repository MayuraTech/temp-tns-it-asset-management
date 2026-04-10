/**
 * Search query models
 */

import { AssetType, LifecycleStatus } from './asset.model';
import { TicketType, TicketStatus, TicketPriority } from './ticket.model';

/**
 * Asset search query interface
 */
export interface SearchQuery {
  text?: string;
  filters?: SearchFilters;
  sortBy?: string;
  sortOrder?: 'ASC' | 'DESC';
  page?: number;
  size?: number;
}

/**
 * Search filters for assets
 */
export interface SearchFilters {
  assetTypes?: AssetType[];
  statuses?: LifecycleStatus[];
  locations?: string[];
  acquisitionDateFrom?: Date | string;
  acquisitionDateTo?: Date | string;
  assignedUser?: string;
}

/**
 * Ticket search query interface
 */
export interface TicketSearchQuery {
  status?: TicketStatus[];
  type?: TicketType[];
  priority?: TicketPriority[];
  requesterId?: string;
  approverId?: string;
  assetId?: string;
  createdFrom?: Date | string;
  createdTo?: Date | string;
  sortBy?: string;
  sortOrder?: 'ASC' | 'DESC';
  page?: number;
  size?: number;
}

/**
 * Audit log search query interface
 */
export interface AuditSearchQuery {
  userId?: string;
  actionType?: string[];
  resourceType?: string[];
  resourceId?: string;
  dateFrom?: Date | string;
  dateTo?: Date | string;
  sortBy?: string;
  sortOrder?: 'ASC' | 'DESC';
  page?: number;
  size?: number;
}
