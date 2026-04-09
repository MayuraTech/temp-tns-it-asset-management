/**
 * SidebarComponent - Editorial Geometry Navigation Panel
 * 
 * Implements the fixed left navigation panel with brand identity,
 * primary navigation, and geometric triangle accents following
 * Editorial Geometry design principles.
 * 
 * Requirements: 2.1, 2.2, 2.6, 2.7, 16.2, 21.6, 24.2, 2.3, 2.4, 2.5, 2.8, 16.5
 */

import { Component, ChangeDetectionStrategy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { Observable } from 'rxjs';
import { GeometricTriangleComponent } from '../../../shared/components/geometric-triangle/geometric-triangle.component';
import { PrimaryActionButtonComponent } from '../../../shared/components/primary-action-button/primary-action-button.component';
import { NavigationItem } from '../../../shared/constants/navigation.config';
import { NavigationService } from '../../services/navigation.service';

@Component({
  selector: 'app-sidebar',
  standalone: true,
  imports: [CommonModule, RouterModule, GeometricTriangleComponent, PrimaryActionButtonComponent],
  templateUrl: './sidebar.component.html',
  styleUrls: ['./sidebar.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class SidebarComponent {
  
  /**
   * Primary navigation items (Assets, Software, Licenses, Network, Users)
   */
  primaryNavItems: NavigationItem[];
  
  /**
   * Secondary navigation items (Audit Logs, Archived)
   */
  secondaryNavItems: NavigationItem[];
  
  /**
   * Current route observable for active state detection
   */
  currentRoute$: Observable<string>;
  
  constructor(private navigationService: NavigationService) {
    // Get navigation items from service
    this.primaryNavItems = this.navigationService.primaryNavigation;
    this.secondaryNavItems = this.navigationService.secondaryNavigation;
    
    // Subscribe to current route for active state detection
    this.currentRoute$ = this.navigationService.currentRoute$;
  }
  
  /**
   * Handles navigation item click
   * @param item Navigation item to navigate to
   */
  onNavigationClick(item: NavigationItem): void {
    this.navigationService.navigateTo(item.route);
  }
  
  /**
   * Handles action button click (Add New Asset)
   */
  onActionButtonClick(): void {
    // Navigate to asset creation page
    this.navigationService.navigateTo('/assets/create');
  }
  
  /**
   * Checks if a route is currently active
   * @param route Route to check
   * @returns True if route is active
   */
  isActiveRoute(route: string): boolean {
    return this.navigationService.isActiveRoute(route);
  }
}