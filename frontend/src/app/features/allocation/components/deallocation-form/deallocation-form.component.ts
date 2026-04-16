import { Component, OnInit, ChangeDetectionStrategy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { AllocationService } from '../../../../core/services/allocation.service';
import { ConfirmationDialogComponent } from './confirmation-dialog.component';

/**
 * Deallocation Form Component
 * 
 * Provides interface for deallocating assets with confirmation dialog.
 * Shows current assignment information before deallocation.
 * 
 * Features:
 * - Confirmation dialog before deallocation
 * - Current assignment display
 * - Success/error messaging
 * - Editorial Geometry styling
 * - OnPush change detection for performance
 */
@Component({
  selector: 'app-deallocation-form',
  standalone: true,
  imports: [
    CommonModule,
    MatDialogModule,
    MatButtonModule,
    MatCardModule,
    MatSnackBarModule,
    MatProgressSpinnerModule
  ],
  templateUrl: './deallocation-form.component.html',
  styleUrls: ['./deallocation-form.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class DeallocationFormComponent implements OnInit {
  assetId: string | null = null;
  assetName: string = 'Asset';
  currentAssignment: string = 'Unknown';
  isProcessing = false;

  constructor(
    private allocationService: AllocationService,
    private route: ActivatedRoute,
    private router: Router,
    private dialog: MatDialog,
    private snackBar: MatSnackBar
  ) {}

  ngOnInit(): void {
    // Get asset ID from route parameters
    this.assetId = this.route.snapshot.paramMap.get('id');
    
    // Get asset details from route data if available
    const assetData = this.route.snapshot.data['asset'];
    if (assetData) {
      this.assetName = assetData.name || 'Asset';
      this.currentAssignment = assetData.assignedUser || assetData.location || 'Unknown';
    }

    if (!this.assetId) {
      this.snackBar.open('Asset ID is required', 'Close', { duration: 3000 });
      this.router.navigate(['/assets']);
    }
  }

  /**
   * Opens confirmation dialog and deallocates asset on confirmation
   */
  deallocate(): void {
    const dialogRef = this.dialog.open(ConfirmationDialogComponent, {
      width: '500px',
      data: {
        title: 'Confirm Deallocation',
        message: `Are you sure you want to deallocate this asset from "${this.currentAssignment}"? This action will remove the current assignment.`,
        confirmText: 'Deallocate',
        cancelText: 'Cancel'
      }
    });

    dialogRef.afterClosed().subscribe(confirmed => {
      if (confirmed && this.assetId) {
        this.performDeallocation();
      }
    });
  }

  /**
   * Performs the deallocation operation
   */
  private performDeallocation(): void {
    if (!this.assetId) {
      return;
    }

    this.isProcessing = true;

    this.allocationService.deallocate(this.assetId).subscribe({
      next: () => {
        this.snackBar.open('Asset deallocated successfully', 'Close', { duration: 3000 });
        this.router.navigate(['/assets', this.assetId]);
      },
      error: (error) => {
        this.isProcessing = false;
        this.snackBar.open(
          error.message || 'Failed to deallocate asset',
          'Close',
          { duration: 5000 }
        );
      },
      complete: () => {
        this.isProcessing = false;
      }
    });
  }

  /**
   * Handles cancel button click
   */
  onCancel(): void {
    if (this.assetId) {
      this.router.navigate(['/assets', this.assetId]);
    } else {
      this.router.navigate(['/assets']);
    }
  }
}
