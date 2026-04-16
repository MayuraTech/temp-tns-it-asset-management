import { Component, Input, Output, EventEmitter, ChangeDetectionStrategy, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatSelectModule } from '@angular/material/select';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatChipsModule } from '@angular/material/chips';
import { AssetType, LifecycleStatus } from '../../../models';

/**
 * Asset Filters Component
 * 
 * Advanced filter bar for asset search and filtering.
 * Implements Editorial Geometry design with glassmorphism effects.
 * 
 * Features:
 * - Multi-select dropdowns for Asset Type and Status
 * - Location filter with autocomplete
 * - Active filter chips display
 * - Reset filters functionality
 * - Responsive design for mobile/tablet
 */
@Component({
  selector: 'app-asset-filters',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatSelectModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatIconModule,
    MatChipsModule
  ],
  template: `
    <div class="filters-container">
      <!-- Filter Controls -->
      <div class="filter-controls">
        <!-- Asset Type Filter -->
        <mat-form-field appearance="outline" class="filter-field">
          <mat-label>Asset Type</mat-label>
          <mat-select 
            multiple
            [value]="selectedAssetTypes"
            (selectionChange)="onAssetTypeChange($event.value)"
            placeholder="All Types">
            <mat-option *ngFor="let type of assetTypes" [value]="type">
              {{ formatAssetType(type) }}
            </mat-option>
          </mat-select>
        </mat-form-field>

        <!-- Status Filter -->
        <mat-form-field appearance="outline" class="filter-field">
          <mat-label>Status</mat-label>
          <mat-select 
            multiple
            [value]="selectedStatuses"
            (selectionChange)="onStatusChange($event.value)"
            placeholder="All Statuses">
            <mat-option *ngFor="let status of lifecycleStatuses" [value]="status">
              {{ formatStatus(status) }}
            </mat-option>
          </mat-select>
        </mat-form-field>

        <!-- Location Filter -->
        <mat-form-field appearance="outline" class="filter-field">
          <mat-label>Location</mat-label>
          <mat-select 
            [value]="selectedLocation"
            (selectionChange)="onLocationChange($event.value)"
            placeholder="All Locations">
            <mat-option value="">All Locations</mat-option>
            <mat-option *ngFor="let location of locations" [value]="location">
              {{ location }}
            </mat-option>
          </mat-select>
        </mat-form-field>

        <!-- Reset Button -->
        <button 
          mat-stroked-button
          class="reset-button"
          (click)="resetFilters()"
          [disabled]="!hasActiveFilters()"
          aria-label="Reset all filters">
          <mat-icon>clear_all</mat-icon>
          Reset
        </button>
      </div>

      <!-- Active Filters Display -->
      <div class="active-filters" *ngIf="hasActiveFilters()">
        <span class="active-filters-label">Active Filters:</span>
        
        <!-- Asset Type Chips -->
        <mat-chip-set *ngIf="selectedAssetTypes.length > 0">
          <mat-chip 
            *ngFor="let type of selectedAssetTypes"
            (removed)="removeAssetTypeFilter(type)"
            removable>
            {{ formatAssetType(type) }}
            <mat-icon matChipRemove>cancel</mat-icon>
          </mat-chip>
        </mat-chip-set>

        <!-- Status Chips -->
        <mat-chip-set *ngIf="selectedStatuses.length > 0">
          <mat-chip 
            *ngFor="let status of selectedStatuses"
            (removed)="removeStatusFilter(status)"
            removable>
            {{ formatStatus(status) }}
            <mat-icon matChipRemove>cancel</mat-icon>
          </mat-chip>
        </mat-chip-set>

        <!-- Location Chip -->
        <mat-chip-set *ngIf="selectedLocation">
          <mat-chip 
            (removed)="removeLocationFilter()"
            removable>
            Location: {{ selectedLocation }}
            <mat-icon matChipRemove>cancel</mat-icon>
          </mat-chip>
        </mat-chip-set>
      </div>
    </div>
  `,
  styles: [`
    .filters-container {
      background: rgba(250, 249, 255, 0.7);
      backdrop-filter: blur(12px);
      -webkit-backdrop-filter: blur(12px);
      border-radius: 8px;
      padding: 24px;
      margin-bottom: 24px;
      box-shadow: 0 4px 12px rgba(20, 59, 125, 0.08);
    }

    /* Fallback for browsers without backdrop-filter support */
    @supports not (backdrop-filter: blur(12px)) {
      .filters-container {
        background: rgba(250, 249, 255, 0.95);
      }
    }

    .filter-controls {
      display: flex;
      flex-wrap: wrap;
      gap: 16px;
      align-items: flex-end;
      margin-bottom: 16px;
    }

    .filter-field {
      min-width: 200px;
      flex: 1;
    }

    .reset-button {
      height: 56px;
      color: var(--primary, #143b7d);
      border-color: var(--outline-variant, #c4c6d2);
    }

    .reset-button:disabled {
      opacity: 0.5;
    }

    .active-filters {
      display: flex;
      flex-wrap: wrap;
      align-items: center;
      gap: 8px;
      padding-top: 16px;
      border-top: 1px solid var(--outline-variant, #c4c6d2);
    }

    .active-filters-label {
      font-family: 'Inter', sans-serif;
      font-size: 14px;
      font-weight: 600;
      color: var(--on-surface-variant, #434750);
      margin-right: 8px;
    }

    mat-chip-set {
      display: flex;
      flex-wrap: wrap;
      gap: 4px;
    }

    mat-chip {
      background: var(--primary-container, #315396);
      color: var(--on-primary-container, #ffffff);
      font-size: 12px;
    }

    /* Responsive design */
    @media (max-width: 768px) {
      .filter-controls {
        flex-direction: column;
        align-items: stretch;
      }

      .filter-field {
        min-width: unset;
        width: 100%;
      }

      .reset-button {
        width: 100%;
      }

      .active-filters {
        flex-direction: column;
        align-items: flex-start;
      }
    }

    /* High contrast mode support */
    @media (prefers-contrast: high) {
      .filters-container {
        border: 2px solid var(--outline, #747782);
      }
      
      mat-chip {
        border: 1px solid currentColor;
      }
    }
  `],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class AssetFiltersComponent implements OnInit {
  @Input() selectedAssetTypes: AssetType[] = [];
  @Input() selectedStatuses: LifecycleStatus[] = [];
  @Input() selectedLocation: string = '';
  @Input() locations: string[] = [];

  @Output() assetTypesChange = new EventEmitter<AssetType[]>();
  @Output() statusesChange = new EventEmitter<LifecycleStatus[]>();
  @Output() locationChange = new EventEmitter<string>();
  @Output() filtersReset = new EventEmitter<void>();

  assetTypes = Object.values(AssetType);
  lifecycleStatuses = Object.values(LifecycleStatus);

  ngOnInit(): void {
    // Component initialization
  }

  /**
   * Handle asset type filter changes
   */
  onAssetTypeChange(types: AssetType[]): void {
    this.assetTypesChange.emit(types);
  }

  /**
   * Handle status filter changes
   */
  onStatusChange(statuses: LifecycleStatus[]): void {
    this.statusesChange.emit(statuses);
  }

  /**
   * Handle location filter changes
   */
  onLocationChange(location: string): void {
    this.locationChange.emit(location);
  }

  /**
   * Reset all filters
   */
  resetFilters(): void {
    this.filtersReset.emit();
  }

  /**
   * Remove specific asset type filter
   */
  removeAssetTypeFilter(type: AssetType): void {
    const updatedTypes = this.selectedAssetTypes.filter(t => t !== type);
    this.assetTypesChange.emit(updatedTypes);
  }

  /**
   * Remove specific status filter
   */
  removeStatusFilter(status: LifecycleStatus): void {
    const updatedStatuses = this.selectedStatuses.filter(s => s !== status);
    this.statusesChange.emit(updatedStatuses);
  }

  /**
   * Remove location filter
   */
  removeLocationFilter(): void {
    this.locationChange.emit('');
  }

  /**
   * Check if any filters are active
   */
  hasActiveFilters(): boolean {
    return this.selectedAssetTypes.length > 0 || 
           this.selectedStatuses.length > 0 || 
           !!this.selectedLocation;
  }

  /**
   * Format asset type for display
   */
  formatAssetType(type: AssetType): string {
    return type.replace(/_/g, ' ').toLowerCase()
      .split(' ')
      .map(word => word.charAt(0).toUpperCase() + word.slice(1))
      .join(' ');
  }

  /**
   * Format status for display
   */
  formatStatus(status: LifecycleStatus): string {
    return status.replace(/_/g, ' ').toLowerCase()
      .split(' ')
      .map(word => word.charAt(0).toUpperCase() + word.slice(1))
      .join(' ');
  }
}