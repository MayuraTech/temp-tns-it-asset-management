import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatDialogModule } from '@angular/material/dialog';
import { MatSelectModule } from '@angular/material/select';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatCardModule } from '@angular/material/card';
import { Subject, takeUntil, finalize } from 'rxjs';
import { AssetService } from '../../services/asset.service';
import { Asset, AssetType, LifecycleStatus } from '../../models';

/**
 * Asset Detail Component
 * 
 * Displays comprehensive asset information in a 3-column bento grid layout.
 * Based on Figma Asset Detail View screen design.
 * 
 * Layout:
 * - Left Column (40%): General details and asset image
 * - Middle Column (30%): Current assignment and lifecycle history
 * - Right Column (30%): Quick actions and assignment history
 * 
 * Features:
 * - Breadcrumb navigation with back button
 * - Asset header with icon, name, and status badge
 * - Edit asset action button
 * - Status change dialog
 * - Delete confirmation dialog
 * - Loading states and error handling
 * - Responsive design (stacks columns on mobile)
 */
@Component({
  selector: 'app-asset-detail',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    MatButtonModule,
    MatIconModule,
    MatProgressSpinnerModule,
    MatDialogModule,
    MatSelectModule,
    MatTooltipModule,
    MatCardModule
  ],
  templateUrl: './asset-detail.component.html',
  styleUrls: ['./asset-detail.component.scss']
})
export class AssetDetailComponent implements OnInit, OnDestroy {
  asset: Asset | null = null;
  loading = false;
  error: string | null = null;
  assetId: string | null = null;
  
  // Dialog states
  showStatusDialog = false;
  showDeleteDialog = false;
  selectedStatus: LifecycleStatus | null = null;
  
  // Lifecycle history mock data (will be replaced with API call)
  lifecycleHistory: LifecycleEvent[] = [];
  
  // Assignment history mock data (will be replaced with API call)
  assignmentHistory: AssignmentHistoryEntry[] = [];
  
  // Available lifecycle statuses for status change
  availableStatuses = Object.values(LifecycleStatus);
  
  private destroy$ = new Subject<void>();

  constructor(
    private assetService: AssetService,
    private route: ActivatedRoute,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.route.paramMap
      .pipe(takeUntil(this.destroy$))
      .subscribe(params => {
        this.assetId = params.get('id');
        if (this.assetId) {
          this.loadAsset(this.assetId);
        }
      });
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  /**
   * Loads asset details by ID from the backend
   */
  loadAsset(id: string): void {
    this.loading = true;
    this.error = null;

    this.assetService.getAsset(id)
      .pipe(
        takeUntil(this.destroy$),
        finalize(() => this.loading = false)
      )
      .subscribe({
        next: (asset) => {
          this.asset = asset;
          this.generateLifecycleHistory(asset);
          this.generateAssignmentHistory(asset);
        },
        error: (error) => {
          this.error = error.message || 'Failed to load asset details';
          console.error('Error loading asset:', error);
        }
      });
  }

  /**
   * Navigates back to the asset inventory page
   */
  goBack(): void {
    this.router.navigate(['/assets']);
  }

  /**
   * Navigates to the asset edit page
   */
  editAsset(): void {
    if (this.assetId) {
      this.router.navigate(['/assets', this.assetId, 'edit']);
    }
  }

  /**
   * Opens the status change dialog
   */
  openStatusDialog(): void {
    this.showStatusDialog = true;
    this.selectedStatus = this.asset?.status || null;
  }

  /**
   * Closes the status change dialog
   */
  closeStatusDialog(): void {
    this.showStatusDialog = false;
    this.selectedStatus = null;
  }

  /**
   * Confirms and applies the status change
   */
  confirmStatusChange(): void {
    if (!this.assetId || !this.selectedStatus) {
      return;
    }

    this.loading = true;
    this.assetService.updateStatus(this.assetId, this.selectedStatus)
      .pipe(
        takeUntil(this.destroy$),
        finalize(() => this.loading = false)
      )
      .subscribe({
        next: (updatedAsset) => {
          this.asset = updatedAsset;
          this.closeStatusDialog();
          this.generateLifecycleHistory(updatedAsset);
        },
        error: (error) => {
          this.error = error.message || 'Failed to update asset status';
          console.error('Error updating status:', error);
        }
      });
  }

  /**
   * Opens the delete confirmation dialog
   */
  openDeleteDialog(): void {
    this.showDeleteDialog = true;
  }

  /**
   * Closes the delete confirmation dialog
   */
  closeDeleteDialog(): void {
    this.showDeleteDialog = false;
  }

  /**
   * Confirms and deletes the asset
   */
  confirmDelete(): void {
    if (!this.assetId) {
      return;
    }

    this.loading = true;
    this.assetService.deleteAsset(this.assetId)
      .pipe(
        takeUntil(this.destroy$),
        finalize(() => this.loading = false)
      )
      .subscribe({
        next: () => {
          this.router.navigate(['/assets']);
        },
        error: (error) => {
          this.error = error.message || 'Failed to delete asset';
          console.error('Error deleting asset:', error);
          this.closeDeleteDialog();
        }
      });
  }

  /**
   * Generates a report for the asset (placeholder)
   */
  generateReport(): void {
    console.log('Generate report for asset:', this.assetId);
    // TODO: Implement report generation
  }

  /**
   * Navigates to reassign asset page (placeholder)
   */
  reassignAsset(): void {
    console.log('Reassign asset:', this.assetId);
    // TODO: Implement asset reassignment
  }

  /**
   * Views full assignment history (placeholder)
   */
  viewFullHistory(): void {
    console.log('View full history for asset:', this.assetId);
    // TODO: Implement full history view
  }

  /**
   * Gets the display label for an asset type
   */
  getAssetTypeLabel(type: AssetType): string {
    return type.replace(/_/g, ' ');
  }

  /**
   * Gets the display label for a lifecycle status
   */
  getStatusLabel(status: LifecycleStatus): string {
    return status.replace(/_/g, ' ');
  }

  /**
   * Gets the CSS class for a status badge
   */
  getStatusClass(status: LifecycleStatus): string {
    const statusMap: Record<LifecycleStatus, string> = {
      [LifecycleStatus.ORDERED]: 'status-ordered',
      [LifecycleStatus.RECEIVED]: 'status-received',
      [LifecycleStatus.DEPLOYED]: 'status-deployed',
      [LifecycleStatus.IN_USE]: 'status-in-use',
      [LifecycleStatus.MAINTENANCE]: 'status-maintenance',
      [LifecycleStatus.STORAGE]: 'status-storage',
      [LifecycleStatus.RETIRED]: 'status-retired'
    };
    return statusMap[status] || '';
  }

  /**
   * Gets the icon class for an asset type
   */
  getAssetIcon(type: AssetType): string {
    const iconMap: Record<AssetType, string> = {
      [AssetType.SERVER]: 'server',
      [AssetType.WORKSTATION]: 'desktop_windows',
      [AssetType.NETWORK_DEVICE]: 'router',
      [AssetType.STORAGE_DEVICE]: 'storage',
      [AssetType.SOFTWARE_LICENSE]: 'key',
      [AssetType.PERIPHERAL]: 'devices',
      [AssetType.KEYBOARD]: 'keyboard',
      [AssetType.MOUSE]: 'mouse',
      [AssetType.LAPTOP]: 'laptop',
      [AssetType.MONITOR]: 'monitor',
      [AssetType.HEADSET]: 'headset',
      [AssetType.LAPTOP_CHARGER]: 'power',
      [AssetType.HDMI_CABLE]: 'cable',
      [AssetType.NETWORK_CABLE]: 'cable',
      [AssetType.ACCESS_CARD]: 'badge'
    };
    return iconMap[type] || 'devices';
  }

  /**
   * Formats a date string for display
   */
  formatDate(dateString: string | undefined): string {
    if (!dateString) {
      return 'N/A';
    }
    const date = new Date(dateString);
    return date.toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'short',
      day: 'numeric'
    });
  }

  /**
   * Formats a datetime string for display
   */
  formatDateTime(dateString: string | undefined): string {
    if (!dateString) {
      return 'N/A';
    }
    const date = new Date(dateString);
    return date.toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  }

  /**
   * Generates lifecycle history from asset data
   * TODO: Replace with actual API call to get history
   */
  private generateLifecycleHistory(asset: Asset): void {
    this.lifecycleHistory = [
      {
        status: asset.status,
        date: asset.updatedAt,
        description: `Asset status changed to ${this.getStatusLabel(asset.status)}`,
        icon: 'update'
      },
      {
        status: LifecycleStatus.RECEIVED,
        date: asset.createdAt,
        description: 'Asset created in system',
        icon: 'add_circle'
      }
    ];
  }

  /**
   * Generates assignment history from asset data
   * TODO: Replace with actual API call to get assignment history
   */
  private generateAssignmentHistory(asset: Asset): void {
    if (asset.assignedUser) {
      this.assignmentHistory = [
        {
          userName: asset.assignedUser,
          userEmail: asset.assignedUserEmail || '',
          startDate: asset.assignmentDate || asset.createdAt,
          endDate: null,
          duration: this.calculateDuration(asset.assignmentDate || asset.createdAt),
          isCurrent: true
        }
      ];
    } else {
      this.assignmentHistory = [];
    }
  }

  /**
   * Calculates duration from a start date to now
   */
  private calculateDuration(startDate: string): string {
    const start = new Date(startDate);
    const now = new Date();
    const diffMs = now.getTime() - start.getTime();
    const diffDays = Math.floor(diffMs / (1000 * 60 * 60 * 24));
    
    if (diffDays < 30) {
      return `${diffDays} days`;
    } else if (diffDays < 365) {
      const months = Math.floor(diffDays / 30);
      return `${months} month${months > 1 ? 's' : ''}`;
    } else {
      const years = Math.floor(diffDays / 365);
      return `${years} year${years > 1 ? 's' : ''}`;
    }
  }
}

/**
 * Interface for lifecycle history events
 */
interface LifecycleEvent {
  status: LifecycleStatus;
  date: string;
  description: string;
  icon: string;
}

/**
 * Interface for assignment history entries
 */
interface AssignmentHistoryEntry {
  userName: string;
  userEmail: string;
  startDate: string;
  endDate: string | null;
  duration: string;
  isCurrent: boolean;
}
