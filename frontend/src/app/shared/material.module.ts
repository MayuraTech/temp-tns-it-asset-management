/**
 * Material Module
 * 
 * Centralized module for importing and exporting Angular Material components.
 * This module should be imported in feature modules that need Material components.
 * 
 * Requirements: 17.1, 17.2, 17.3, 17.4, 17.5
 */

import { NgModule } from '@angular/core';

// Material Components
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatSidenavModule } from '@angular/material/sidenav';
import { MatListModule } from '@angular/material/list';
import { MatCardModule } from '@angular/material/card';
import { MatTableModule } from '@angular/material/table';
import { MatPaginatorModule } from '@angular/material/paginator';
import { MatSortModule } from '@angular/material/sort';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatNativeDateModule } from '@angular/material/core';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatRadioModule } from '@angular/material/radio';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { MatDialogModule } from '@angular/material/dialog';
import { MatSnackBarModule } from '@angular/material/snack-bar';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatMenuModule } from '@angular/material/menu';
import { MatChipsModule } from '@angular/material/chips';
import { MatBadgeModule } from '@angular/material/badge';
import { MatTabsModule } from '@angular/material/tabs';
import { MatExpansionModule } from '@angular/material/expansion';
import { MatStepperModule } from '@angular/material/stepper';
import { MatAutocompleteModule } from '@angular/material/autocomplete';
import { MatSliderModule } from '@angular/material/slider';
import { MatButtonToggleModule } from '@angular/material/button-toggle';
import { MatDividerModule } from '@angular/material/divider';
import { MatGridListModule } from '@angular/material/grid-list';
import { MatRippleModule } from '@angular/material/core';

const materialModules = [
  // Layout
  MatToolbarModule,
  MatSidenavModule,
  MatListModule,
  MatCardModule,
  MatDividerModule,
  MatGridListModule,
  
  // Tables
  MatTableModule,
  MatPaginatorModule,
  MatSortModule,
  
  // Forms
  MatFormFieldModule,
  MatInputModule,
  MatSelectModule,
  MatDatepickerModule,
  MatNativeDateModule,
  MatCheckboxModule,
  MatRadioModule,
  MatSlideToggleModule,
  MatAutocompleteModule,
  MatSliderModule,
  MatButtonToggleModule,
  
  // Buttons & Indicators
  MatButtonModule,
  MatIconModule,
  MatBadgeModule,
  MatChipsModule,
  MatProgressSpinnerModule,
  MatProgressBarModule,
  MatRippleModule,
  
  // Popups & Modals
  MatDialogModule,
  MatSnackBarModule,
  MatTooltipModule,
  MatMenuModule,
  
  // Navigation
  MatTabsModule,
  MatExpansionModule,
  MatStepperModule,
];

@NgModule({
  imports: materialModules,
  exports: materialModules
})
export class MaterialModule { }
