import { Component, OnInit, OnDestroy, ChangeDetectionStrategy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { BehaviorSubject, Subject } from 'rxjs';
import { takeUntil, finalize } from 'rxjs/operators';
import { Ticket, TicketStatus, TicketPriority } from '../../../../shared/models/ticket.model';
import { TicketService } from '../../services/ticket.service';
import { PageResponse } from '../../../../shared/models/page-response.model';

/**
 * Component for displaying ticket list (My Requests Dashboard)
 */
@Component({
  selector: 'app-ticket-list',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './ticket-list.component.html',
  styleUrls: ['./ticket-list.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class TicketListComponent implements OnInit, OnDestroy {
  tickets$ = new BehaviorSubject<Ticket[]>([]);
  loading$ = new BehaviorSubject<boolean>(false);
  error$ = new BehaviorSubject<string | null>(null);
  
  // Filter state
  selectedFilter: 'all' | 'pending' | 'approved' | 'completed' = 'all';
  
  // Summary counts
  inProgressCount = 0;
  approvedCount = 0;
  highPriorityCount = 0;
  
  // Pagination
  currentPage = 0;
  pageSize = 20;
  totalElements = 0;
  
  // Bulk operations
  selectedTicketIds = new Set<string>();
  bulkOperationInProgress$ = new BehaviorSubject<boolean>(false);
  
  private destroy$ = new Subject<void>();
  
  // Enum references for template
  TicketStatus = TicketStatus;
  TicketPriority = TicketPriority;

  constructor(
    private ticketService: TicketService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loadTickets();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  /**
   * Load tickets based on current filter
   */
  loadTickets(): void {
    this.loading$.next(true);
    this.error$.next(null);

    const query = this.buildQuery();

    this.ticketService.getMyTickets(this.currentPage, this.pageSize)
      .pipe(
        takeUntil(this.destroy$),
        finalize(() => this.loading$.next(false))
      )
      .subscribe({
        next: (response: PageResponse<Ticket>) => {
          this.tickets$.next(response.content);
          this.totalElements = response.page.totalElements;
          this.calculateSummaryCounts(response.content);
        },
        error: (error) => {
          this.error$.next('Failed to load tickets');
          console.error('Error loading tickets:', error);
        }
      });
  }

  /**
   * Build query based on selected filter
   */
  private buildQuery(): any {
    switch (this.selectedFilter) {
      case 'pending':
        return { statuses: [TicketStatus.PENDING] };
      case 'approved':
        return { statuses: [TicketStatus.APPROVED] };
      case 'completed':
        return { statuses: [TicketStatus.COMPLETED] };
      default:
        return {};
    }
  }

  /**
   * Calculate summary counts
   */
  private calculateSummaryCounts(tickets: Ticket[]): void {
    this.inProgressCount = tickets.filter(t => t.status === TicketStatus.PENDING).length;
    this.approvedCount = tickets.filter(t => t.status === TicketStatus.APPROVED).length;
    this.highPriorityCount = tickets.filter(t => t.priority === TicketPriority.URGENT).length;
  }

  /**
   * Handle filter change
   */
  onFilterChange(filter: 'all' | 'pending' | 'approved' | 'completed'): void {
    this.selectedFilter = filter;
    this.currentPage = 0;
    this.loadTickets();
  }

  /**
   * Navigate to ticket details
   */
  viewTicketDetails(ticket: Ticket): void {
    this.router.navigate(['/tickets', ticket.id]);
  }

  /**
   * Navigate to create ticket page
   */
  createEmergencyTicket(): void {
    this.router.navigate(['/tickets/create']);
  }

  /**
   * Handle pagination
   */
  onPageChange(page: number): void {
    this.currentPage = page;
    this.loadTickets();
  }

  /**
   * Get status badge class
   */
  getStatusBadgeClass(status: TicketStatus): string {
    switch (status) {
      case TicketStatus.PENDING:
        return 'badge-pending';
      case TicketStatus.APPROVED:
        return 'badge-approved';
      case TicketStatus.COMPLETED:
        return 'badge-completed';
      case TicketStatus.REJECTED:
        return 'badge-rejected';
      case TicketStatus.CANCELLED:
        return 'badge-cancelled';
      default:
        return '';
    }
  }

  /**
   * Get priority indicator class
   */
  getPriorityClass(priority: TicketPriority): string {
    switch (priority) {
      case TicketPriority.URGENT:
        return 'priority-high';
      case TicketPriority.STANDARD:
        return 'priority-medium';
      case TicketPriority.LOW:
        return 'priority-low';
      default:
        return '';
    }
  }

  /**
   * Toggle ticket selection for bulk operations
   */
  toggleTicketSelection(ticketId: string): void {
    if (this.selectedTicketIds.has(ticketId)) {
      this.selectedTicketIds.delete(ticketId);
    } else {
      this.selectedTicketIds.add(ticketId);
    }
  }

  /**
   * Check if ticket is selected
   */
  isTicketSelected(ticketId: string): boolean {
    return this.selectedTicketIds.has(ticketId);
  }

  /**
   * Select all tickets
   */
  selectAllTickets(): void {
    const tickets = this.tickets$.value;
    tickets.forEach(ticket => this.selectedTicketIds.add(ticket.id));
  }

  /**
   * Deselect all tickets
   */
  deselectAllTickets(): void {
    this.selectedTicketIds.clear();
  }

  /**
   * Bulk approve selected tickets
   */
  bulkApprove(): void {
    if (this.selectedTicketIds.size === 0) return;
    
    if (!confirm(`Are you sure you want to approve ${this.selectedTicketIds.size} ticket(s)?`)) {
      return;
    }

    this.bulkOperationInProgress$.next(true);
    const ticketIds = Array.from(this.selectedTicketIds);

    this.ticketService.bulkApprove(ticketIds)
      .pipe(
        takeUntil(this.destroy$),
        finalize(() => this.bulkOperationInProgress$.next(false))
      )
      .subscribe({
        next: (result) => {
          alert(`Bulk approve completed: ${result.successCount} succeeded, ${result.failureCount} failed`);
          this.selectedTicketIds.clear();
          this.loadTickets();
        },
        error: (error) => {
          this.error$.next('Bulk approve operation failed');
          console.error('Error in bulk approve:', error);
        }
      });
  }

  /**
   * Bulk reject selected tickets
   */
  bulkReject(): void {
    if (this.selectedTicketIds.size === 0) return;
    
    const reason = prompt('Please provide a reason for rejection:');
    if (!reason) return;

    if (!confirm(`Are you sure you want to reject ${this.selectedTicketIds.size} ticket(s)?`)) {
      return;
    }

    this.bulkOperationInProgress$.next(true);
    const ticketIds = Array.from(this.selectedTicketIds);

    this.ticketService.bulkReject(ticketIds, reason)
      .pipe(
        takeUntil(this.destroy$),
        finalize(() => this.bulkOperationInProgress$.next(false))
      )
      .subscribe({
        next: (result) => {
          alert(`Bulk reject completed: ${result.successCount} succeeded, ${result.failureCount} failed`);
          this.selectedTicketIds.clear();
          this.loadTickets();
        },
        error: (error) => {
          this.error$.next('Bulk reject operation failed');
          console.error('Error in bulk reject:', error);
        }
      });
  }
}
