import { Component, Input, Output, EventEmitter, ChangeDetectionStrategy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatMenuModule } from '@angular/material/menu';
import { MatTooltipModule } from '@angular/material/tooltip';
import { LifecycleStatus } from '../../../models';

/**
 * Quick Actions Component
 * 
 * Provides quick action buttons for common asset operations.
 * Used in asset detail view for easy access to primary actions.
 * 
 * Features:
 * - Primary action buttons (Edit, Change Status, Generate Report)
 * - Status change dropdown menu
 * - Conditional button states based on asset status and permissions
 * - Responsive design with Editorial Geometry styling
 * - Accessible with proper ARIA labels and keyboard navigation
 */
@Component({
  selector: 'app-quick-actions',
  standalone: true,
  imports: [
    CommonModule,
    MatButtonModule,
    MatIconModule,
    MatMenuModule,
    MatTooltipModule
  ],
  template: `
    <div class="quick-actions-container">
      <h3 class="actions-title">Quick Actions</h3>
      
      <div class="actions-grid">
        <!-- Edit Asset Button -->
        <button 
          mat-flat-button
          color="primary"
          class="action-button primary-action"
          (click)="onEditClick()"
          [disabled]="readOnly"
          matTooltip="Edit asset details"
          aria-label="Edit asset">
          <mat-icon>edit</mat-icon>
          <span>Edit Asset</span>
        </button>

        <!-- Change Status Button with Menu -->
        <button 
          mat-stroked-button
          class="action-button"
          [matMenuTriggerFor]="statusMenu"
          [disabled]="readOnly || currentStatus === 'RETIRED'"
          matTooltip="Change asset status"
          aria-label="Change asset status">
          <mat-icon>swap_vert</mat-icon>
          <span>Change Status</span>
        </button>

        <!-- Status Change Menu -->
        <mat-menu #statusMenu="matMenu" class="status-menu">
          <button 
            mat-menu-item
            *ngFor="let status of getAvailableStatuses()"
            (click)="onStatusChange(status)"
            [disabled]="status === currentStatus">
            <mat-icon [class]="getStatusIconClass(status)">
              {{ getStatusIcon(status) }}
            </mat-icon>
            <span>{{ formatStatus(status) }}</span>
          </button>
        </mat-menu>

        <!-- Generate Report Button -->
        <button 
          mat-stroked-button
          class="action-button"
          (click)="onGenerateReportClick()"
          matTooltip="Generate asset report"
          aria-label="Generate report">
          <mat-icon>description</mat-icon>
          <span>Generate Report</span>
        </button>

        <!-- Assign/Reassign Button -->
        <button 
          mat-stroked-button
          class="action-button"
          (click)="onAssignmentClick()"
          [disabled]="readOnly"
          [matTooltip]="isAssigned ? 'Reassign asset to another user' : 'Assign asset to a user'"
          [attr.aria-label]="isAssigned ? 'Reassign asset' : 'Assign asset'">
          <mat-icon>{{ isAssigned ? 'swap_horiz' : 'person_add' }}</mat-icon>
          <span>{{ isAssigned ? 'Reassign' : 'Assign' }}</span>
        </button>

        <!-- Delete Asset Button -->
        <button 
          mat-stroked-button
          class="action-button danger-action"
          (click)="onDeleteClick()"
          [disabled]="readOnly"
          matTooltip="Delete asset permanently"
          aria-label="Delete asset">
          <mat-icon>delete</mat-icon>
          <span>Delete</span>
        </button>

        <!-- Export Asset Button -->
        <button 
          mat-stroked-button
          class="action-button"
          (click)="onExportClick()"
          matTooltip="Export asset data"
          aria-label="Export asset">
          <mat-icon>download</mat-icon>
          <span>Export</span>
        </button>
      </div>

      <!-- Secondary Actions -->
      <div class="secondary-actions" *ngIf="showSecondaryActions">
        <h4 class="secondary-title">More Actions</h4>
        
        <div class="secondary-buttons">
          <button 
            mat-button
            class="secondary-button"
            (click)="onDuplicateClick()"
            [disabled]="readOnly"
            matTooltip="Create a copy of this asset"
            aria-label="Duplicate asset">
            <mat-icon>content_copy</mat-icon>
            Duplicate
          </button>

          <button 
            mat-button
            class="secondary-button"
            (click)="onHistoryClick()"
            matTooltip="View full asset history"
            aria-label="View history">
            <mat-icon>history</mat-icon>
            View History
          </button>

          <button 
            mat-button
            class="secondary-button"
            (click)="onPrintClick()"
            matTooltip="Print asset details"
            aria-label="Print asset">
            <mat-icon>print</mat-icon>
            Print
          </button>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .quick-actions-container {
      background: var(--surface-container-lowest, #ffffff);
      border-radius: 8px;
      padding: 24px;
      box-shadow: 0 4px 12px rgba(20, 59, 125, 0.08);
    }

    .actions-title {
      font-family: 'Manrope', sans-serif;
      font-size: 18px;
      font-weight: 600;
      color: var(--on-surface, #1a1b20);
      margin: 0 0 20px 0;
      display: flex;
      align-items: center;
      gap: 8px;
    }

    .actions-title::before {
      content: '';
      width: 4px;
      height: 20px;
      background: var(--secondary, #a9371d);
      border-radius: 2px;
    }

    .actions-grid {
      display: grid;
      grid-template-columns: 1fr;
      gap: 12px;
    }

    .action-button {
      display: flex;
      align-items: center;
      justify-content: flex-start;
      gap: 12px;
      padding: 12px 16px;
      height: 48px;
      font-family: 'Inter', sans-serif;
      font-weight: 600;
      font-size: 14px;
      border-radius: 8px;
      transition: all 0.2s ease;
      text-align: left;
      width: 100%;
    }

    .action-button mat-icon {
      font-size: 20px;
      width: 20px;
      height: 20px;
    }

    .primary-action {
      background: linear-gradient(135deg, var(--primary, #143b7d) 0%, var(--primary-container, #315396) 100%);
      color: white;
      box-shadow: 0 4px 12px rgba(20, 59, 125, 0.3);
    }

    .primary-action:hover {
      box-shadow: 0 6px 16px rgba(20, 59, 125, 0.4);
      transform: translateY(-1px);
    }

    .action-button:not(.primary-action) {
      color: var(--primary, #143b7d);
      border-color: var(--outline-variant, #c4c6d2);
    }

    .action-button:not(.primary-action):hover {
      background: rgba(20, 59, 125, 0.04);
      border-color: var(--primary, #143b7d);
    }

    .danger-action {
      color: #dc3545 !important;
      border-color: rgba(220, 53, 69, 0.3) !important;
    }

    .danger-action:hover {
      background: rgba(220, 53, 69, 0.04) !important;
      border-color: #dc3545 !important;
    }

    .action-button:disabled {
      opacity: 0.5;
      cursor: not-allowed;
    }

    /* Status Menu Styling */
    .status-menu {
      margin-top: 8px;
    }

    .status-menu .mat-mdc-menu-item {
      display: flex;
      align-items: center;
      gap: 12px;
      font-family: 'Inter', sans-serif;
    }

    .status-menu mat-icon {
      font-size: 18px;
      width: 18px;
      height: 18px;
    }

    /* Status-specific icon colors */
    .status-icon-ordered { color: #ffc107; }
    .status-icon-received { color: #0d6efd; }
    .status-icon-deployed { color: #198754; }
    .status-icon-in-use { color: #28a745; }
    .status-icon-maintenance { color: #fd7e14; }
    .status-icon-storage { color: #6c757d; }
    .status-icon-retired { color: #dc3545; }

    /* Secondary Actions */
    .secondary-actions {
      margin-top: 24px;
      padding-top: 20px;
      border-top: 1px solid var(--outline-variant, #c4c6d2);
    }

    .secondary-title {
      font-family: 'Inter', sans-serif;
      font-size: 14px;
      font-weight: 600;
      color: var(--on-surface-variant, #434750);
      margin: 0 0 12px 0;
    }

    .secondary-buttons {
      display: flex;
      flex-direction: column;
      gap: 4px;
    }

    .secondary-button {
      display: flex;
      align-items: center;
      justify-content: flex-start;
      gap: 12px;
      padding: 8px 12px;
      height: 40px;
      font-family: 'Inter', sans-serif;
      font-size: 13px;
      color: var(--on-surface-variant, #434750);
      border-radius: 6px;
      text-align: left;
      width: 100%;
    }

    .secondary-button mat-icon {
      font-size: 18px;
      width: 18px;
      height: 18px;
    }

    .secondary-button:hover {
      background: var(--surface-container-low, #f4f3f9);
      color: var(--on-surface, #1a1b20);
    }

    /* Responsive Design */
    @media (min-width: 768px) {
      .actions-grid {
        grid-template-columns: 1fr 1fr;
        gap: 16px;
      }
    }

    @media (min-width: 1024px) {
      .actions-grid {
        grid-template-columns: 1fr;
        gap: 12px;
      }
    }

    @media (max-width: 480px) {
      .quick-actions-container {
        padding: 16px;
      }

      .actions-title {
        font-size: 16px;
        margin-bottom: 16px;
      }

      .action-button {
        padding: 10px 12px;
        height: 44px;
        font-size: 13px;
      }

      .action-button mat-icon {
        font-size: 18px;
        width: 18px;
        height: 18px;
      }
    }

    /* High Contrast Mode */
    @media (prefers-contrast: high) {
      .quick-actions-container {
        border: 2px solid var(--outline, #747782);
      }

      .action-button {
        border-width: 2px;
      }

      .primary-action {
        border: 2px solid var(--primary, #143b7d);
      }
    }

    /* Reduced Motion */
    @media (prefers-reduced-motion: reduce) {
      .action-button {
        transition: none;
      }

      .primary-action:hover {
        transform: none;
      }
    }
  `],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class QuickActionsComponent {
  @Input() currentStatus?: LifecycleStatus;
  @Input() isAssigned: boolean = false;
  @Input() readOnly: boolean = false;
  @Input() showSecondaryActions: boolean = true;

  @Output() editClick = new EventEmitter<void>();
  @Output() statusChange = new EventEmitter<LifecycleStatus>();
  @Output() generateReportClick = new EventEmitter<void>();
  @Output() assignmentClick = new EventEmitter<void>();
  @Output() deleteClick = new EventEmitter<void>();
  @Output() exportClick = new EventEmitter<void>();
  @Output() duplicateClick = new EventEmitter<void>();
  @Output() historyClick = new EventEmitter<void>();
  @Output() printClick = new EventEmitter<void>();

  /**
   * Handle edit button click
   */
  onEditClick(): void {
    this.editClick.emit();
  }

  /**
   * Handle status change selection
   */
  onStatusChange(status: LifecycleStatus): void {
    this.statusChange.emit(status);
  }

  /**
   * Handle generate report button click
   */
  onGenerateReportClick(): void {
    this.generateReportClick.emit();
  }

  /**
   * Handle assignment button click
   */
  onAssignmentClick(): void {
    this.assignmentClick.emit();
  }

  /**
   * Handle delete button click
   */
  onDeleteClick(): void {
    this.deleteClick.emit();
  }

  /**
   * Handle export button click
   */
  onExportClick(): void {
    this.exportClick.emit();
  }

  /**
   * Handle duplicate button click
   */
  onDuplicateClick(): void {
    this.duplicateClick.emit();
  }

  /**
   * Handle history button click
   */
  onHistoryClick(): void {
    this.historyClick.emit();
  }

  /**
   * Handle print button click
   */
  onPrintClick(): void {
    this.printClick.emit();
  }

  /**
   * Get available status transitions based on current status
   */
  getAvailableStatuses(): LifecycleStatus[] {
    if (!this.currentStatus) {
      return Object.values(LifecycleStatus);
    }

    const transitions: Record<LifecycleStatus, LifecycleStatus[]> = {
      [LifecycleStatus.ORDERED]: [LifecycleStatus.RECEIVED, LifecycleStatus.MAINTENANCE],
      [LifecycleStatus.RECEIVED]: [LifecycleStatus.DEPLOYED, LifecycleStatus.MAINTENANCE],
      [LifecycleStatus.DEPLOYED]: [LifecycleStatus.IN_USE, LifecycleStatus.STORAGE, LifecycleStatus.MAINTENANCE],
      [LifecycleStatus.IN_USE]: [LifecycleStatus.STORAGE, LifecycleStatus.RETIRED, LifecycleStatus.MAINTENANCE],
      [LifecycleStatus.MAINTENANCE]: Object.values(LifecycleStatus).filter(s => s !== LifecycleStatus.MAINTENANCE),
      [LifecycleStatus.STORAGE]: [LifecycleStatus.DEPLOYED, LifecycleStatus.RETIRED, LifecycleStatus.MAINTENANCE],
      [LifecycleStatus.RETIRED]: [] // No transitions from retired
    };

    return transitions[this.currentStatus] || [];
  }

  /**
   * Get icon for lifecycle status
   */
  getStatusIcon(status: LifecycleStatus): string {
    const iconMap: Record<LifecycleStatus, string> = {
      [LifecycleStatus.ORDERED]: 'shopping_cart',
      [LifecycleStatus.RECEIVED]: 'inventory',
      [LifecycleStatus.DEPLOYED]: 'rocket_launch',
      [LifecycleStatus.IN_USE]: 'play_circle',
      [LifecycleStatus.MAINTENANCE]: 'build',
      [LifecycleStatus.STORAGE]: 'archive',
      [LifecycleStatus.RETIRED]: 'delete_forever'
    };
    return iconMap[status] || 'help';
  }

  /**
   * Get CSS class for status icon
   */
  getStatusIconClass(status: LifecycleStatus): string {
    const classMap: Record<LifecycleStatus, string> = {
      [LifecycleStatus.ORDERED]: 'status-icon-ordered',
      [LifecycleStatus.RECEIVED]: 'status-icon-received',
      [LifecycleStatus.DEPLOYED]: 'status-icon-deployed',
      [LifecycleStatus.IN_USE]: 'status-icon-in-use',
      [LifecycleStatus.MAINTENANCE]: 'status-icon-maintenance',
      [LifecycleStatus.STORAGE]: 'status-icon-storage',
      [LifecycleStatus.RETIRED]: 'status-icon-retired'
    };
    return classMap[status] || '';
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