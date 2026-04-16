import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { HttpClientModule } from '@angular/common/http';

// Angular Material Modules
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatNativeDateModule } from '@angular/material/core';
import { MatTableModule } from '@angular/material/table';
import { MatPaginatorModule } from '@angular/material/paginator';
import { MatSortModule } from '@angular/material/sort';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatDialogModule } from '@angular/material/dialog';
import { MatSnackBarModule } from '@angular/material/snack-bar';
import { MatCardModule } from '@angular/material/card';
import { MatChipsModule } from '@angular/material/chips';
import { MatMenuModule } from '@angular/material/menu';

// Shared Components
import { LoadingSpinnerComponent } from '../../shared/components/loading-spinner/loading-spinner.component';
import { StatusBadgeComponent } from '../../shared/components/status-badge/status-badge.component';
import { IconComponent } from '../../shared/components/icon/icon.component';
import { GeometricTriangleComponent } from '../../shared/components/geometric-triangle/geometric-triangle.component';
import { SearchBarComponent } from '../../shared/components/search-bar/search-bar.component';
import { PrimaryActionButtonComponent } from '../../shared/components/primary-action-button/primary-action-button.component';
import { ConfirmationDialogComponent } from '../../shared/components/confirmation-dialog/confirmation-dialog.component';

// Asset Management Components
import { AssetInventoryComponent } from './components/asset-inventory/asset-inventory.component';
import { AssetFormComponent } from './components/asset-form/asset-form.component';
import { AssetDetailComponent } from './components/asset-detail/asset-detail.component';

// Services
import { AssetService } from './services/asset.service';

// Routes
import { ASSET_ROUTES } from './assets.routes';

/**
 * Assets Module
 * 
 * Angular module for the Asset Management feature (Module 2).
 * Provides comprehensive asset lifecycle management functionality including:
 * - Asset inventory with search and filtering
 * - Asset creation and editing
 * - Asset detail view with history
 * - Import/Export capabilities
 * 
 * This module follows Angular 17+ patterns with standalone components
 * and lazy loading for optimal performance.
 * 
 * Design System: Editorial Geometry UI Standards
 * - Asymmetrical layouts with geometric accents
 * - Glassmorphism effects for elevated elements
 * - Premium editorial typography (Manrope + Inter)
 * - Surface hierarchy with tonal layering
 */
@NgModule({
  declarations: [
    // Asset components are standalone, so they're not declared here
    // This module serves as a configuration and provider module
  ],
  imports: [
    // Angular Core Modules
    CommonModule,
    ReactiveFormsModule,
    HttpClientModule,
    RouterModule.forChild(ASSET_ROUTES),
    
    // Angular Material Modules
    MatIconModule,
    MatButtonModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatDatepickerModule,
    MatNativeDateModule,
    MatTableModule,
    MatPaginatorModule,
    MatSortModule,
    MatProgressSpinnerModule,
    MatTooltipModule,
    MatDialogModule,
    MatSnackBarModule,
    MatCardModule,
    MatChipsModule,
    MatMenuModule,
    
    // Shared Components (Standalone)
    LoadingSpinnerComponent,
    StatusBadgeComponent,
    IconComponent,
    GeometricTriangleComponent,
    SearchBarComponent,
    PrimaryActionButtonComponent,
    ConfirmationDialogComponent,
    
    // Asset Components (Standalone)
    AssetInventoryComponent,
    AssetFormComponent,
    AssetDetailComponent
  ],
  providers: [
    // Asset Service is provided at root level via @Injectable({ providedIn: 'root' })
    // but we can also provide it here for module-specific configuration if needed
    AssetService
  ],
  exports: [
    // Export components for use in other modules if needed
    AssetInventoryComponent,
    AssetFormComponent,
    AssetDetailComponent
  ]
})
export class AssetsModule {
  constructor() {
    console.log('AssetsModule initialized - Module 2: Asset Management');
  }
}
