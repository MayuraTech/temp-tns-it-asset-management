import { Component, OnInit, OnDestroy, ChangeDetectionStrategy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { BehaviorSubject, Subject, interval, throwError } from 'rxjs';
import { takeUntil, switchMap, startWith, catchError, tap } from 'rxjs/operators';
import { AssetService, AssetStats } from '../../services/asset.service';

/**
 * Dashboard Stats Widget Component
 * 
 * Displays quick statistics about assets including:
 * - Total Assets
 * - Assets In Use  
 * - Assets Available
 * 
 * Features:
 * - Real-time updates every 30 seconds
 * - Loading states
 * - Error handling
 * - Editorial Geometry design system compliance
 */
@Component({
  selector: 'app-dashboard-stats',
  standalone: true,
  imports: [
    CommonModule,
    MatCardModule,
    MatIconModule,
    MatProgressSpinnerModule
  ],
  templateUrl: './dashboard-stats.component.html',
  styleUrls: ['./dashboard-stats.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class DashboardStatsComponent implements OnInit, OnDestroy {
  
  // Observable streams for reactive data management
  stats$ = new BehaviorSubject<AssetStats | null>(null);
  loading$ = new BehaviorSubject<boolean>(false);
  error$ = new BehaviorSubject<string | null>(null);
  
  private destroy$ = new Subject<void>();
  private readonly REFRESH_INTERVAL = 30000; // 30 seconds

  constructor(private assetService: AssetService) {}

  ngOnInit(): void {
    this.startStatsUpdates();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  /**
   * Starts the real-time stats update cycle
   * Refreshes stats immediately and then every 30 seconds
   */
  private startStatsUpdates(): void {
    interval(this.REFRESH_INTERVAL)
      .pipe(
        startWith(0), // Emit immediately on subscription
        switchMap(() => this.loadStats()),
        takeUntil(this.destroy$)
      )
      .subscribe();
  }

  /**
   * Loads asset statistics from the service
   * Handles loading states and error conditions
   */
  private loadStats() {
    this.loading$.next(true);
    this.error$.next(null);

    return this.assetService.getAssetStats()
      .pipe(
        takeUntil(this.destroy$),
        catchError((error) => {
          console.error('Failed to load asset stats:', error);
          this.error$.next('Failed to load statistics');
          this.loading$.next(false);
          return throwError(() => error);
        }),
        tap({
          next: (stats) => {
            this.stats$.next(stats);
            this.loading$.next(false);
          },
          error: () => {
            this.loading$.next(false);
          }
        })
      );
  }

  /**
   * Manually refresh stats (called by user action)
   */
  refreshStats(): void {
    this.loadStats().subscribe();
  }

  /**
   * Calculate available assets (Total - In Use)
   */
  getAvailableAssets(stats: AssetStats): number {
    return stats.totalAssets - stats.assetsInUse;
  }

  /**
   * Calculate percentage of assets in use
   */
  getUsagePercentage(stats: AssetStats): number {
    if (stats.totalAssets === 0) return 0;
    return Math.round((stats.assetsInUse / stats.totalAssets) * 100);
  }
}