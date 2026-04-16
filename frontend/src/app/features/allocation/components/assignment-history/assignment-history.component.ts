import { Component, OnInit, ChangeDetectionStrategy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute } from '@angular/router';
import { BehaviorSubject } from 'rxjs';
import { MatTableModule } from '@angular/material/table';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatCardModule } from '@angular/material/card';
import { MatChipsModule } from '@angular/material/chips';
import { AllocationService } from '../../../../core/services/allocation.service';
import { AssignmentHistoryDTO, AssignmentType } from '../../../../shared/models/allocation.model';
import { PageResponse } from '../../../../shared/models/page-response.model';

/**
 * Assignment History Component
 * 
 * Displays the complete assignment history for an asset in a paginated table.
 * Shows both active and historical assignments with assignment details.
 * 
 * Features:
 * - Material table with pagination
 * - Loading and empty states
 * - Assignment type badges
 * - Date formatting
 * - Editorial Geometry styling
 * - OnPush change detection for performance
 */
@Component({
  selector: 'app-assignment-history',
  standalone: true,
  imports: [
    CommonModule,
    MatTableModule,
    MatPaginatorModule,
    MatProgressSpinnerModule,
    MatCardModule,
    MatChipsModule
  ],
  templateUrl: './assignment-history.component.html',
  styleUrls: ['./assignment-history.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class AssignmentHistoryComponent implements OnInit {
  // Observable state management
  history$ = new BehaviorSubject<AssignmentHistoryDTO[]>([]);
  loading$ = new BehaviorSubject<boolean>(false);
  error$ = new BehaviorSubject<string | null>(null);
  
  // Pagination state
  totalElements = 0;
  pageSize = 20;
  pageIndex = 0;
  
  // Table configuration
  displayedColumns: string[] = [
    'assignmentType',
    'assignedTo',
    'assignedBy',
    'assignedAt',
    'unassignedAt',
    'status'
  ];
  
  assetId: string | null = null;

  constructor(
    private allocationService: AllocationService,
    private route: ActivatedRoute
  ) {}

  ngOnInit(): void {
    // Get asset ID from route parameters
    this.assetId = this.route.snapshot.paramMap.get('id');
    
    if (this.assetId) {
      this.loadHistory();
    } else {
      this.error$.next('Asset ID is required');
    }
  }

  /**
   * Loads assignment history for the asset
   */
  loadHistory(): void {
    if (!this.assetId) {
      return;
    }

    this.loading$.next(true);
    this.error$.next(null);

    this.allocationService.getAssignmentHistory(this.assetId, this.pageIndex, this.pageSize)
      .subscribe({
        next: (response: PageResponse<AssignmentHistoryDTO>) => {
          this.history$.next(response.content);
          this.totalElements = response.page.totalElements;
          this.loading$.next(false);
        },
        error: (error) => {
          this.error$.next(error.message || 'Failed to load assignment history');
          this.loading$.next(false);
        }
      });
  }

  /**
   * Handles pagination events
   */
  onPageChange(event: PageEvent): void {
    this.pageIndex = event.pageIndex;
    this.pageSize = event.pageSize;
    this.loadHistory();
  }

  /**
   * Formats date for display
   */
  formatDate(date: Date | string | undefined): string {
    if (!date) {
      return 'N/A';
    }

    const dateObj = typeof date === 'string' ? new Date(date) : date;
    return dateObj.toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  }

  /**
   * Gets assignment type label
   */
  getAssignmentTypeLabel(type: AssignmentType): string {
    return type === AssignmentType.USER ? 'User' : 'Location';
  }

  /**
   * Gets assignment type chip color
   */
  getAssignmentTypeColor(type: AssignmentType): string {
    return type === AssignmentType.USER ? 'primary' : 'accent';
  }

  /**
   * Determines if assignment is currently active
   */
  isActive(assignment: AssignmentHistoryDTO): boolean {
    return !assignment.unassignedAt;
  }

  /**
   * Gets status label for assignment
   */
  getStatusLabel(assignment: AssignmentHistoryDTO): string {
    return this.isActive(assignment) ? 'Active' : 'Historical';
  }

  /**
   * Gets status chip color
   */
  getStatusColor(assignment: AssignmentHistoryDTO): string {
    return this.isActive(assignment) ? 'primary' : 'default';
  }
}
