import { Component, OnInit, OnDestroy, ChangeDetectionStrategy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { BehaviorSubject, Subject } from 'rxjs';
import { takeUntil, finalize } from 'rxjs/operators';
import { Ticket, TicketStatus, TicketStatusHistory } from '../../../../shared/models/ticket.model';
import { TicketService } from '../../services/ticket.service';

@Component({
  selector: 'app-ticket-detail',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './ticket-detail.component.html',
  styleUrls: ['./ticket-detail.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class TicketDetailComponent implements OnInit, OnDestroy {
  ticket$ = new BehaviorSubject<Ticket | null>(null);
  statusHistory$ = new BehaviorSubject<TicketStatusHistory[]>([]);
  loading$ = new BehaviorSubject<boolean>(false);
  error$ = new BehaviorSubject<string | null>(null);
  
  // Action states
  actionInProgress$ = new BehaviorSubject<boolean>(false);
  
  private ticketId: string = '';
  private destroy$ = new Subject<void>();
  
  TicketStatus = TicketStatus;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private ticketService: TicketService
  ) {}

  ngOnInit(): void {
    this.route.params.pipe(takeUntil(this.destroy$)).subscribe(params => {
      this.ticketId = params['id'];
      if (this.ticketId) {
        this.loadTicket();
        this.loadStatusHistory();
      }
    });
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  loadTicket(): void {
    this.loading$.next(true);
    this.error$.next(null);

    this.ticketService.getTicket(this.ticketId)
      .pipe(
        takeUntil(this.destroy$),
        finalize(() => this.loading$.next(false))
      )
      .subscribe({
        next: (ticket) => this.ticket$.next(ticket),
        error: (error) => {
          this.error$.next('Failed to load ticket');
          console.error('Error loading ticket:', error);
        }
      });
  }

  loadStatusHistory(): void {
    this.ticketService.getStatusHistory(this.ticketId)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (history) => this.statusHistory$.next(history),
        error: (error) => console.error('Error loading status history:', error)
      });
  }

  approveTicket(): void {
    if (!this.ticketId) return;
    
    this.actionInProgress$.next(true);
    this.ticketService.approveTicket(this.ticketId)
      .pipe(
        takeUntil(this.destroy$),
        finalize(() => this.actionInProgress$.next(false))
      )
      .subscribe({
        next: (ticket) => {
          this.ticket$.next(ticket);
          this.loadStatusHistory();
        },
        error: (error) => {
          this.error$.next('Failed to approve ticket');
          console.error('Error approving ticket:', error);
        }
      });
  }

  rejectTicket(): void {
    const reason = prompt('Please provide a reason for rejection:');
    if (!reason || !this.ticketId) return;
    
    this.actionInProgress$.next(true);
    this.ticketService.rejectTicket(this.ticketId, reason)
      .pipe(
        takeUntil(this.destroy$),
        finalize(() => this.actionInProgress$.next(false))
      )
      .subscribe({
        next: (ticket) => {
          this.ticket$.next(ticket);
          this.loadStatusHistory();
        },
        error: (error) => {
          this.error$.next('Failed to reject ticket');
          console.error('Error rejecting ticket:', error);
        }
      });
  }

  completeTicket(): void {
    if (!this.ticketId) return;
    
    this.actionInProgress$.next(true);
    this.ticketService.completeTicket(this.ticketId)
      .pipe(
        takeUntil(this.destroy$),
        finalize(() => this.actionInProgress$.next(false))
      )
      .subscribe({
        next: (ticket) => {
          this.ticket$.next(ticket);
          this.loadStatusHistory();
        },
        error: (error) => {
          this.error$.next('Failed to complete ticket');
          console.error('Error completing ticket:', error);
        }
      });
  }

  cancelTicket(): void {
    const reason = prompt('Please provide a reason for cancellation:');
    if (!this.ticketId) return;
    
    this.actionInProgress$.next(true);
    this.ticketService.cancelTicket(this.ticketId, reason)
      .pipe(
        takeUntil(this.destroy$),
        finalize(() => this.actionInProgress$.next(false))
      )
      .subscribe({
        next: (ticket) => {
          this.ticket$.next(ticket);
          this.loadStatusHistory();
        },
        error: (error) => {
          this.error$.next('Failed to cancel ticket');
          console.error('Error cancelling ticket:', error);
        }
      });
  }

  canApprove(ticket: Ticket): boolean {
    return ticket.status === TicketStatus.PENDING;
  }

  canReject(ticket: Ticket): boolean {
    return ticket.status === TicketStatus.PENDING;
  }

  canComplete(ticket: Ticket): boolean {
    return ticket.status === TicketStatus.APPROVED;
  }

  canCancel(ticket: Ticket): boolean {
    return ticket.status === TicketStatus.PENDING || ticket.status === TicketStatus.APPROVED;
  }

  goBack(): void {
    this.router.navigate(['/tickets']);
  }
}
