import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { RouterModule, Routes } from '@angular/router';
import { HttpClientModule } from '@angular/common/http';

// Material modules
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatSelectModule } from '@angular/material/select';
import { MatTableModule } from '@angular/material/table';
import { MatPaginatorModule } from '@angular/material/paginator';
import { MatCardModule } from '@angular/material/card';
import { MatChipsModule } from '@angular/material/chips';
import { MatDialogModule } from '@angular/material/dialog';
import { MatSnackBarModule } from '@angular/material/snack-bar';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatListModule } from '@angular/material/list';
import { MatDividerModule } from '@angular/material/divider';

// Guards
import { allocationGuard } from './guards/allocation.guard';
import { viewerGuard } from './guards/viewer.guard';

// Components are standalone, so we just need to define routes
const routes: Routes = [
  {
    path: 'assign/:id',
    loadComponent: () => import('./components/allocation-form/allocation-form.component')
      .then(m => m.AllocationFormComponent),
    canActivate: [allocationGuard],
    data: { title: 'Assign Asset' }
  },
  {
    path: 'history/:id',
    loadComponent: () => import('./components/assignment-history/assignment-history.component')
      .then(m => m.AssignmentHistoryComponent),
    canActivate: [viewerGuard],
    data: { title: 'Assignment History' }
  },
  {
    path: 'deallocate/:id',
    loadComponent: () => import('./components/deallocation-form/deallocation-form.component')
      .then(m => m.DeallocationFormComponent),
    canActivate: [allocationGuard],
    data: { title: 'Deallocate Asset' }
  },
  {
    path: 'statistics',
    loadComponent: () => import('./components/assignment-statistics/assignment-statistics.component')
      .then(m => m.AssignmentStatisticsComponent),
    canActivate: [allocationGuard],
    data: { title: 'Assignment Statistics' }
  }
];

/**
 * Allocation Module
 * 
 * Feature module for allocation management functionality.
 * All components are standalone and lazy-loaded for optimal performance.
 * 
 * This module provides the routing configuration and imports common dependencies
 * that may be needed by the allocation feature components.
 * 
 * Routes:
 * - /allocation/assign/:id - Assign asset to user or location (requires ADMINISTRATOR or ASSET_MANAGER)
 * - /allocation/history/:id - View assignment history for asset (requires ADMINISTRATOR, ASSET_MANAGER, or VIEWER)
 * - /allocation/deallocate/:id - Deallocate asset (requires ADMINISTRATOR or ASSET_MANAGER)
 * - /allocation/statistics - View assignment statistics (requires ADMINISTRATOR or ASSET_MANAGER)
 * 
 * Route Guards:
 * - allocationGuard: Protects write operations (assign, deallocate, statistics)
 * - viewerGuard: Protects read operations (history)
 * 
 * Note: Since all components are standalone, they import their own dependencies directly.
 * This module primarily serves as a routing configuration container.
 */
@NgModule({
  imports: [
    // Angular core modules
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    HttpClientModule,
    
    // Material modules (available for any non-standalone components if needed)
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatSelectModule,
    MatTableModule,
    MatPaginatorModule,
    MatCardModule,
    MatChipsModule,
    MatDialogModule,
    MatSnackBarModule,
    MatProgressSpinnerModule,
    MatListModule,
    MatDividerModule,
    
    // Routing
    RouterModule.forChild(routes)
  ],
  exports: [
    // Export RouterModule so routes are available when this module is imported
    RouterModule
  ]
})
export class AllocationModule { }
