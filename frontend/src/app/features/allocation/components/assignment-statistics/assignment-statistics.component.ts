import { Component, OnInit, ChangeDetectionStrategy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { BehaviorSubject } from 'rxjs';
import { MatCardModule } from '@angular/material/card';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatListModule } from '@angular/material/list';
import { MatDividerModule } from '@angular/material/divider';
import { AllocationService } from '../../../../core/services/allocation.service';
import { AssignmentStatistics } from '../../../../shared/models/allocation.model';

/**
 * Assignment Statistics Component
 * 
 * Displays comprehensive assignment statistics including:
 * - Total assigned assets
 * - User vs location breakdown
 * - Available assets by status
 * - Top 10 users by assignment count
 * - Top 10 locations by assignment count
 * 
 * Features:
 * - Asymmetrical layout with geometric accents
 * - Large display numbers for key metrics
 * - Top assignees lists
 * - Editorial Geometry styling
 * - OnPush change detection for performance
 */
@Component({
  selector: 'app-assignment-statistics',
  standalone: true,
  imports: [
    CommonModule,
    MatCardModule,
    MatProgressSpinnerModule,
    MatListModule,
    MatDividerModule
  ],
  templateUrl: './assignment-statistics.component.html',
  styleUrls: ['./assignment-statistics.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class AssignmentStatisticsComponent implements OnInit {
  // Observable state management
  statistics$ = new BehaviorSubject<AssignmentStatistics | null>(null);
  loading$ = new BehaviorSubject<boolean>(false);
  error$ = new BehaviorSubject<string | null>(null);

  constructor(private allocationService: AllocationService) {}

  ngOnInit(): void {
    this.loadStatistics();
  }

  /**
   * Loads assignment statistics from the service
   */
  loadStatistics(): void {
    this.loading$.next(true);
    this.error$.next(null);

    this.allocationService.getStatistics().subscribe({
      next: (statistics) => {
        this.statistics$.next(statistics);
        this.loading$.next(false);
      },
      error: (error) => {
        this.error$.next(error.message || 'Failed to load statistics');
        this.loading$.next(false);
      }
    });
  }

  /**
   * Calculates percentage for visual representation
   */
  calculatePercentage(value: number, total: number): number {
    if (total === 0) {
      return 0;
    }
    return Math.round((value / total) * 100);
  }

  /**
   * Gets the maximum count from top assignees for bar chart scaling
   */
  getMaxCount(assignees: { name: string; count: number }[]): number {
    if (!assignees || assignees.length === 0) {
      return 1;
    }
    return Math.max(...assignees.map(a => a.count));
  }

  /**
   * Calculates bar width percentage for visual representation
   */
  getBarWidth(count: number, maxCount: number): number {
    if (maxCount === 0) {
      return 0;
    }
    return Math.round((count / maxCount) * 100);
  }
}
