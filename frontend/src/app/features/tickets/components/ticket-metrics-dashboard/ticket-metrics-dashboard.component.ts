import { Component, OnInit, OnDestroy, ChangeDetectionStrategy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule } from '@angular/forms';
import { BehaviorSubject, Subject } from 'rxjs';
import { takeUntil, finalize } from 'rxjs/operators';
import { TicketMetrics } from '../../../../shared/models/ticket.model';
import { TicketMetricsService } from '../../services/ticket-metrics.service';

@Component({
  selector: 'app-ticket-metrics-dashboard',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './ticket-metrics-dashboard.component.html',
  styleUrls: ['./ticket-metrics-dashboard.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class TicketMetricsDashboardComponent implements OnInit, OnDestroy {
  metrics$ = new BehaviorSubject<TicketMetrics | null>(null);
  loading$ = new BehaviorSubject<boolean>(false);
  error$ = new BehaviorSubject<string | null>(null);
  
  dateRangeForm!: FormGroup;
  
  private destroy$ = new Subject<void>();

  constructor(
    private metricsService: TicketMetricsService,
    private fb: FormBuilder
  ) {}

  ngOnInit(): void {
    this.initializeForm();
    this.loadMetrics();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  initializeForm(): void {
    const today = new Date();
    const thirtyDaysAgo = new Date(today.getTime() - 30 * 24 * 60 * 60 * 1000);
    
    this.dateRangeForm = this.fb.group({
      fromDate: [thirtyDaysAgo.toISOString().split('T')[0]],
      toDate: [today.toISOString().split('T')[0]]
    });
  }

  loadMetrics(): void {
    this.loading$.next(true);
    this.error$.next(null);

    const { fromDate, toDate } = this.dateRangeForm.value;

    this.metricsService.getMetrics(fromDate, toDate)
      .pipe(
        takeUntil(this.destroy$),
        finalize(() => this.loading$.next(false))
      )
      .subscribe({
        next: (metrics) => this.metrics$.next(metrics),
        error: (error) => {
          this.error$.next('Failed to load metrics');
          console.error('Error loading metrics:', error);
        }
      });
  }

  onDateRangeChange(): void {
    this.loadMetrics();
  }

  getStatusEntries(metrics: TicketMetrics): Array<{ key: string; value: number }> {
    return Object.entries(metrics.ticketsByStatus).map(([key, value]) => ({ key, value }));
  }

  getTypeEntries(metrics: TicketMetrics): Array<{ key: string; value: number }> {
    return Object.entries(metrics.ticketsByType).map(([key, value]) => ({ key, value }));
  }

  getPriorityEntries(metrics: TicketMetrics): Array<{ key: string; value: number }> {
    return Object.entries(metrics.ticketsByPriority).map(([key, value]) => ({ key, value }));
  }

  getPercentage(value: number, total: number): number {
    return total > 0 ? Math.round((value / total) * 100) : 0;
  }
}
