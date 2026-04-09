/**
 * Layout Module - Editorial Geometry Dashboard Layout
 * 
 * Provides the core layout components for the AssetIntel application
 * following Editorial Geometry design principles with geometric accents,
 * tonal layering, and glassmorphism effects.
 */

import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';

// Layout Components
import { AppShellComponent } from './app-shell/app-shell.component';
import { SidebarComponent } from './sidebar/sidebar.component';
import { TopNavigationComponent } from './top-navigation/top-navigation.component';
import { MainContentComponent } from './main-content/main-content.component';

@NgModule({
  declarations: [
    AppShellComponent
  ],
  imports: [
    CommonModule,
    RouterModule,
    FormsModule,
    // Standalone components must be imported, not declared
    SidebarComponent,
    TopNavigationComponent,
    MainContentComponent
  ],
  exports: [
    AppShellComponent,
    SidebarComponent,
    TopNavigationComponent,
    MainContentComponent
  ]
})
export class LayoutModule { }