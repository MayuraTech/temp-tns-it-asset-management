import { Component, Input, Output, EventEmitter, ChangeDetectionStrategy, ViewChild, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatTableModule, MatTableDataSource } from '@angular/material/table';
import { MatSortModule, MatSort, Sort } from '@angular/material/sort';
import { MatPaginatorModule, MatPaginator, PageEvent } from '@angular/material/paginator';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatMenuModule } from '@angular/material/menu';
import { SelectionModel } from '@angular/cdk/collections';
import { Subject, takeUntil } from 'rxjs';
import { Asset } from '../../../models';
import { AssetStatusBadgeComponent } from '../asset-status-badge/asset-status-badge.component';
import { AssetIconComponent } from '../asset-icon/asset-icon.component';

/**
 * Interface for table action events
 */
export interface AssetTableAction {
  action: 'view' | 'edit' | 'delete' | 'assign' | 'export';
  asset: Asset;
}

/**
 * Interface for bulk action events
 */
export interface BulkAction {
  action: 'delete' | 'export' | 'assign' | 'status-change';
  assets: Asset[];
  data?: any;
}

/**
 * Asset Table Component
 * 
 * Reusable data table for displaying assets with sorting, pagination, and actions.
 * Used across asset inventory, search results, and selection dialogs.
 * 
 * Features:
 * - Sortable columns with Material Design sort indicators
 * - Pagination with configurable page sizes
 * - Row selection with bulk actions
 * - Row-level action buttons (View, Edit, Delete)
 * - Responsive design with column hiding on mobile
 * - Loading and empty states
 * - Editorial Geometry styling with proper spacing
 */
@Component({
  selector: 'app-asset-table',
  standalone: true,
  imports: [
    CommonModule,
    MatTableModule,
    MatSortModule,
    MatPaginatorModule,
    MatButtonModule,
    MatIconModule,
    MatTooltipModule,
    MatCheckboxModule,
    MatMenuModule,
    AssetStatusBadgeComponent,
    AssetIconComponent
  ],
  template: `
    <div class="table-container">
      <!-- Bulk Actions Bar -->
      <div class="bulk-actions-bar" *ngIf="selection.hasValue() && showBulkActions">
        <div class="selection-info">
          <span class="selection-count">{{ selection.selected.length }} selected</span>
          <button 
            mat-button
            class="clear-selection"
            (click)="clearSelection()"
            aria-label="Clear selection">
            Clear
          </button>
        </div>
        
        <div class="bulk-actions">
          <button 
            mat-stroked-button
            class="bulk-action-btn"
            (click)="onBulkAction('export')"
            matTooltip="Export selected assets">
            <mat-icon>download</mat-icon>
            Export
          </button>
          
          <button 
            mat-stroked-button
            class="bulk-action-btn"
            (click)="onBulkAction('assign')"
            matTooltip="Assign selected assets">
            <mat-icon>person_add</mat-icon>
            Assign
          </button>
          
          <button 
            mat-stroked-button
            class="bulk-action-btn danger"
            (click)="onBulkAction('delete')"
            matTooltip="Delete selected assets">
            <mat-icon>delete</mat-icon>
            Delete
          </button>
        </div>
      </div>

      <!-- Table -->
      <div class="table-wrapper">
        <table 
          mat-table 
          [dataSource]="dataSource" 
          matSort
          class="asset-table"
          [class.loading]="loading"
          role="table"
          aria-label="Assets table">
          
          <!-- Selection Column -->
          <ng-container matColumnDef="select" *ngIf="showSelection">
            <th mat-header-cell *matHeaderCellDef class="select-column">
              <mat-checkbox
                (change)="$event ? toggleAllRows() : null"
                [checked]="selection.hasValue() && isAllSelected()"
                [indeterminate]="selection.hasValue() && !isAllSelected()"
                [aria-label]="checkboxLabel()"
                color="primary">
              </mat-checkbox>
            </th>
            <td mat-cell *matCellDef="let asset" class="select-column">
              <mat-checkbox
                (click)="$event.stopPropagation()"
                (change)="$event ? selection.toggle(asset) : null"
                [checked]="selection.isSelected(asset)"
                [aria-label]="checkboxLabel(asset)"
                color="primary">
              </mat-checkbox>
            </td>
          </ng-container>

          <!-- Name Column -->
          <ng-container matColumnDef="name">
            <th mat-header-cell *matHeaderCellDef mat-sort-header class="name-column">
              Name
            </th>
            <td mat-cell *matCellDef="let asset" class="name-column">
              <div class="name-cell">
                <app-asset-icon 
                  [assetType]="asset.assetType" 
                  size="small"
                  class="asset-icon">
                </app-asset-icon>
                <span class="asset-name" [title]="asset.name">{{ asset.name }}</span>
              </div>
            </td>
          </ng-container>

          <!-- Type Column -->
          <ng-container matColumnDef="type">
            <th mat-header-cell *matHeaderCellDef mat-sort-header class="type-column">
              Type
            </th>
            <td mat-cell *matCellDef="let asset" class="type-column">
              <span class="asset-type">{{ formatAssetType(asset.assetType) }}</span>
            </td>
          </ng-container>

          <!-- Serial Number Column -->
          <ng-container matColumnDef="serialNumber">
            <th mat-header-cell *matHeaderCellDef mat-sort-header class="serial-column">
              Serial Number
            </th>
            <td mat-cell *matCellDef="let asset" class="serial-column">
              <code class="serial-number">{{ asset.serialNumber }}</code>
            </td>
          </ng-container>

          <!-- Status Column -->
          <ng-container matColumnDef="status">
            <th mat-header-cell *matHeaderCellDef mat-sort-header class="status-column">
              Status
            </th>
            <td mat-cell *matCellDef="let asset" class="status-column">
              <app-asset-status-badge [status]="asset.status"></app-asset-status-badge>
            </td>
          </ng-container>

          <!-- Acquisition Date Column -->
          <ng-container matColumnDef="acquisitionDate">
            <th mat-header-cell *matHeaderCellDef mat-sort-header class="date-column">
              Acquired
            </th>
            <td mat-cell *matCellDef="let asset" class="date-column">
              <time [attr.datetime]="asset.acquisitionDate">
                {{ formatDate(asset.acquisitionDate) }}
              </time>
            </td>
          </ng-container>

          <!-- Location Column -->
          <ng-container matColumnDef="location">
            <th mat-header-cell *matHeaderCellDef mat-sort-header class="location-column">
              Location
            </th>
            <td mat-cell *matCellDef="let asset" class="location-column">
              <span class="location-text" [title]="asset.location || 'Not specified'">
                {{ asset.location || 'Not specified' }}
              </span>
            </td>
          </ng-container>

          <!-- Assigned User Column -->
          <ng-container matColumnDef="assignedUser">
            <th mat-header-cell *matHeaderCellDef mat-sort-header class="user-column">
              Assigned User
            </th>
            <td mat-cell *matCellDef="let asset" class="user-column">
              <span class="user-text" [title]="asset.assignedUser || 'Unassigned'">
                {{ asset.assignedUser || 'Unassigned' }}
              </span>
            </td>
          </ng-container>

          <!-- Actions Column -->
          <ng-container matColumnDef="actions" *ngIf="showActions">
            <th mat-header-cell *matHeaderCellDef class="actions-column">
              Actions
            </th>
            <td mat-cell *matCellDef="let asset" class="actions-column">
              <div class="action-buttons">
                <button 
                  mat-icon-button
                  class="action-btn view-btn"
                  (click)="onAction('view', asset); $event.stopPropagation()"
                  matTooltip="View asset details"
                  aria-label="View asset">
                  <mat-icon>visibility</mat-icon>
                </button>
                
                <button 
                  mat-icon-button
                  class="action-btn edit-btn"
                  (click)="onAction('edit', asset); $event.stopPropagation()"
                  [disabled]="asset.readOnly"
                  matTooltip="Edit asset"
                  aria-label="Edit asset">
                  <mat-icon>edit</mat-icon>
                </button>
                
                <button 
                  mat-icon-button
                  class="action-btn delete-btn"
                  (click)="onAction('delete', asset); $event.stopPropagation()"
                  [disabled]="asset.readOnly"
                  matTooltip="Delete asset"
                  aria-label="Delete asset">
                  <mat-icon>delete</mat-icon>
                </button>
              </div>
            </td>
          </ng-container>

          <!-- Header Row -->
          <tr mat-header-row *matHeaderRowDef="displayedColumns; sticky: stickyHeader"></tr>
          
          <!-- Data Rows -->
          <tr 
            mat-row 
            *matRowDef="let asset; columns: displayedColumns;"
            class="data-row"
            [class.selected]="selection.isSelected(asset)"
            (click)="onRowClick(asset)"
            role="button"
            [attr.aria-label]="'Asset: ' + asset.name">
          </tr>

          <!-- No Data Row -->
          <tr class="mat-row no-data-row" *matNoDataRow>
            <td class="mat-cell no-data-cell" [attr.colspan]="displayedColumns.length">
              <div class="no-data-content">
                <mat-icon class="no-data-icon">inventory_2</mat-icon>
                <h3 class="no-data-title">{{ loading ? 'Loading assets...' : 'No assets found' }}</h3>
                <p class="no-data-description" *ngIf="!loading">
                  {{ emptyMessage || 'Try adjusting your search criteria or add new assets.' }}
                </p>
              </div>
            </td>
          </tr>
        </table>
      </div>

      <!-- Paginator -->
      <mat-paginator
        *ngIf="showPagination && !loading"
        [length]="totalElements"
        [pageSize]="pageSize"
        [pageIndex]="pageIndex"
        [pageSizeOptions]="pageSizeOptions"
        [showFirstLastButtons]="true"
        (page)="onPageChange($event)"
        aria-label="Select page of assets">
      </mat-paginator>
    </div>
  `,
  styles: [`
    .table-container {
      background: var(--surface-container-lowest, #ffffff);
      border-radius: 8px;
      overflow: hidden;
      box-shadow: 0 4px 12px rgba(20, 59, 125, 0.08);
    }

    /* Bulk Actions Bar */
    .bulk-actions-bar {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 12px 16px;
      background: var(--primary-container, #315396);
      color: white;
      border-bottom: 1px solid var(--outline-variant, #c4c6d2);
    }

    .selection-info {
      display: flex;
      align-items: center;
      gap: 12px;
    }

    .selection-count {
      font-family: 'Inter', sans-serif;
      font-size: 14px;
      font-weight: 600;
    }

    .clear-selection {
      color: white;
      font-size: 12px;
      min-width: auto;
      padding: 4px 8px;
    }

    .bulk-actions {
      display: flex;
      gap: 8px;
    }

    .bulk-action-btn {
      color: white;
      border-color: rgba(255, 255, 255, 0.3);
      font-size: 12px;
      height: 32px;
    }

    .bulk-action-btn.danger {
      border-color: rgba(220, 53, 69, 0.5);
      color: #ffcccb;
    }

    .bulk-action-btn mat-icon {
      font-size: 16px;
      width: 16px;
      height: 16px;
    }

    /* Table Wrapper */
    .table-wrapper {
      overflow-x: auto;
      max-height: 600px;
    }

    .asset-table {
      width: 100%;
      background: var(--surface-container-lowest, #ffffff);
    }

    .asset-table.loading {
      opacity: 0.6;
      pointer-events: none;
    }

    /* Header Styles */
    .mat-mdc-header-cell {
      font-family: 'Inter', sans-serif;
      font-size: 12px;
      font-weight: 600;
      color: var(--on-surface-variant, #434750);
      text-transform: uppercase;
      letter-spacing: 0.5px;
      background: var(--surface-container-low, #f4f3f9);
      border-bottom: 2px solid var(--outline-variant, #c4c6d2);
      padding: 12px 16px;
    }

    /* Cell Styles */
    .mat-mdc-cell {
      font-family: 'Inter', sans-serif;
      font-size: 14px;
      color: var(--on-surface, #1a1b20);
      padding: 12px 16px;
      border-bottom: 1px solid var(--outline-variant, #c4c6d2);
    }

    /* Column-specific styles */
    .select-column {
      width: 48px;
      padding: 8px 12px;
    }

    .name-column {
      min-width: 200px;
    }

    .type-column {
      min-width: 120px;
    }

    .serial-column {
      min-width: 140px;
    }

    .status-column {
      min-width: 100px;
    }

    .date-column {
      min-width: 100px;
    }

    .location-column {
      min-width: 120px;
    }

    .user-column {
      min-width: 140px;
    }

    .actions-column {
      width: 120px;
      text-align: right;
    }

    /* Name Cell */
    .name-cell {
      display: flex;
      align-items: center;
      gap: 12px;
    }

    .asset-name {
      font-weight: 500;
      color: var(--on-surface, #1a1b20);
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }

    /* Asset Type */
    .asset-type {
      font-size: 13px;
      color: var(--on-surface-variant, #434750);
    }

    /* Serial Number */
    .serial-number {
      font-family: 'Courier New', monospace;
      font-size: 12px;
      background: var(--surface-container-low, #f4f3f9);
      padding: 2px 6px;
      border-radius: 4px;
      color: var(--primary, #143b7d);
    }

    /* Location and User Text */
    .location-text,
    .user-text {
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
      display: block;
    }

    /* Action Buttons */
    .action-buttons {
      display: flex;
      gap: 4px;
      justify-content: flex-end;
    }

    .action-btn {
      width: 32px;
      height: 32px;
      color: var(--on-surface-variant, #434750);
    }

    .action-btn mat-icon {
      font-size: 18px;
      width: 18px;
      height: 18px;
    }

    .view-btn:hover {
      color: var(--primary, #143b7d);
      background: rgba(20, 59, 125, 0.04);
    }

    .edit-btn:hover {
      color: #28a745;
      background: rgba(40, 167, 69, 0.04);
    }

    .delete-btn:hover {
      color: #dc3545;
      background: rgba(220, 53, 69, 0.04);
    }

    .action-btn:disabled {
      opacity: 0.3;
      cursor: not-allowed;
    }

    /* Row States */
    .data-row {
      cursor: pointer;
      transition: background-color 0.2s ease;
    }

    .data-row:hover {
      background: var(--surface-container-low, #f4f3f9);
    }

    .data-row.selected {
      background: rgba(20, 59, 125, 0.08);
    }

    /* No Data State */
    .no-data-row {
      height: 200px;
    }

    .no-data-cell {
      text-align: center;
      border-bottom: none;
    }

    .no-data-content {
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      padding: 40px 20px;
    }

    .no-data-icon {
      font-size: 48px;
      width: 48px;
      height: 48px;
      color: var(--on-surface-variant, #434750);
      opacity: 0.6;
      margin-bottom: 16px;
    }

    .no-data-title {
      font-family: 'Manrope', sans-serif;
      font-size: 18px;
      font-weight: 600;
      color: var(--on-surface, #1a1b20);
      margin: 0 0 8px 0;
    }

    .no-data-description {
      font-family: 'Inter', sans-serif;
      font-size: 14px;
      color: var(--on-surface-variant, #434750);
      margin: 0;
      max-width: 400px;
      line-height: 1.4;
    }

    /* Paginator */
    .mat-mdc-paginator {
      background: var(--surface-container-low, #f4f3f9);
      border-top: 1px solid var(--outline-variant, #c4c6d2);
    }

    /* Responsive Design */
    @media (max-width: 768px) {
      .bulk-actions-bar {
        flex-direction: column;
        gap: 12px;
        align-items: stretch;
      }

      .bulk-actions {
        justify-content: center;
      }

      .table-wrapper {
        max-height: 400px;
      }

      /* Hide less important columns on mobile */
      .type-column,
      .location-column,
      .date-column {
        display: none;
      }

      .mat-mdc-cell,
      .mat-mdc-header-cell {
        padding: 8px 12px;
      }

      .name-column {
        min-width: 150px;
      }

      .action-buttons {
        flex-direction: column;
        gap: 2px;
      }

      .actions-column {
        width: 40px;
      }
    }

    @media (max-width: 480px) {
      .user-column {
        display: none;
      }

      .name-cell {
        gap: 8px;
      }

      .asset-name {
        font-size: 13px;
      }

      .serial-number {
        font-size: 11px;
      }
    }

    /* High Contrast Mode */
    @media (prefers-contrast: high) {
      .table-container {
        border: 2px solid var(--outline, #747782);
      }

      .mat-mdc-header-cell {
        border-bottom: 3px solid var(--outline, #747782);
      }

      .mat-mdc-cell {
        border-bottom: 1px solid var(--outline, #747782);
      }

      .data-row.selected {
        background: rgba(20, 59, 125, 0.2);
        border: 2px solid var(--primary, #143b7d);
      }
    }

    /* Print Styles */
    @media print {
      .table-container {
        box-shadow: none;
        border: 1px solid #ccc;
      }

      .bulk-actions-bar,
      .actions-column,
      .select-column {
        display: none;
      }

      .asset-table {
        font-size: 12px;
      }
    }
  `],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class AssetTableComponent implements OnInit, OnDestroy {
  @ViewChild(MatSort) sort!: MatSort;
  @ViewChild(MatPaginator) paginator!: MatPaginator;

  @Input() assets: Asset[] = [];
  @Input() loading: boolean = false;
  @Input() showSelection: boolean = false;
  @Input() showActions: boolean = true;
  @Input() showBulkActions: boolean = true;
  @Input() showPagination: boolean = true;
  @Input() stickyHeader: boolean = false;
  @Input() emptyMessage?: string;
  
  // Pagination inputs
  @Input() totalElements: number = 0;
  @Input() pageSize: number = 20;
  @Input() pageIndex: number = 0;
  @Input() pageSizeOptions: number[] = [10, 20, 50, 100];

  // Column configuration
  @Input() columns: string[] = ['name', 'type', 'serialNumber', 'status', 'acquisitionDate', 'location', 'assignedUser'];

  @Output() actionClick = new EventEmitter<AssetTableAction>();
  @Output() bulkActionClick = new EventEmitter<BulkAction>();
  @Output() rowClick = new EventEmitter<Asset>();
  @Output() sortChange = new EventEmitter<Sort>();
  @Output() pageChange = new EventEmitter<PageEvent>();

  dataSource = new MatTableDataSource<Asset>();
  selection = new SelectionModel<Asset>(true, []);
  
  private destroy$ = new Subject<void>();

  get displayedColumns(): string[] {
    const cols = [];
    
    if (this.showSelection) {
      cols.push('select');
    }
    
    cols.push(...this.columns);
    
    if (this.showActions) {
      cols.push('actions');
    }
    
    return cols;
  }

  ngOnInit(): void {
    this.dataSource.data = this.assets;
    
    // Listen for selection changes
    this.selection.changed
      .pipe(takeUntil(this.destroy$))
      .subscribe(() => {
        // Handle selection changes if needed
      });
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  ngOnChanges(): void {
    if (this.dataSource) {
      this.dataSource.data = this.assets;
    }
  }

  ngAfterViewInit(): void {
    if (this.sort) {
      this.dataSource.sort = this.sort;
      this.sort.sortChange
        .pipe(takeUntil(this.destroy$))
        .subscribe((sort: Sort) => {
          this.sortChange.emit(sort);
        });
    }
  }

  /**
   * Handle row click events
   */
  onRowClick(asset: Asset): void {
    this.rowClick.emit(asset);
  }

  /**
   * Handle action button clicks
   */
  onAction(action: 'view' | 'edit' | 'delete' | 'assign' | 'export', asset: Asset): void {
    this.actionClick.emit({ action, asset });
  }

  /**
   * Handle bulk action clicks
   */
  onBulkAction(action: 'delete' | 'export' | 'assign' | 'status-change', data?: any): void {
    this.bulkActionClick.emit({
      action,
      assets: this.selection.selected,
      data
    });
  }

  /**
   * Handle page change events
   */
  onPageChange(event: PageEvent): void {
    this.pageChange.emit(event);
  }

  /**
   * Toggle all rows selection
   */
  toggleAllRows(): void {
    if (this.isAllSelected()) {
      this.selection.clear();
    } else {
      this.dataSource.data.forEach(asset => this.selection.select(asset));
    }
  }

  /**
   * Check if all rows are selected
   */
  isAllSelected(): boolean {
    const numSelected = this.selection.selected.length;
    const numRows = this.dataSource.data.length;
    return numSelected === numRows && numRows > 0;
  }

  /**
   * Clear selection
   */
  clearSelection(): void {
    this.selection.clear();
  }

  /**
   * Get checkbox label for accessibility
   */
  checkboxLabel(asset?: Asset): string {
    if (!asset) {
      return `${this.isAllSelected() ? 'deselect' : 'select'} all`;
    }
    return `${this.selection.isSelected(asset) ? 'deselect' : 'select'} asset ${asset.name}`;
  }

  /**
   * Format asset type for display
   */
  formatAssetType(type: string): string {
    return type.replace(/_/g, ' ').toLowerCase()
      .split(' ')
      .map(word => word.charAt(0).toUpperCase() + word.slice(1))
      .join(' ');
  }

  /**
   * Format date for display
   */
  formatDate(dateString: string): string {
    const date = new Date(dateString);
    return date.toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'short',
      day: 'numeric'
    });
  }
}