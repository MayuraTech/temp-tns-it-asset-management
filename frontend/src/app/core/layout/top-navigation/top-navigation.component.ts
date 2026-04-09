/**
 * TopNavigationComponent - Glassmorphism Navigation Bar
 * 
 * Implements the horizontal navigation bar with glassmorphism effects,
 * search functionality, secondary navigation, and user controls following
 * Editorial Geometry design principles.
 * 
 * Requirements: 4.1, 4.2, 4.3, 4.4, 4.5, 4.6, 4.7, 4.8, 16.3, 22.1, 22.2, 22.3, 6.1-6.7
 */

import { Component, ChangeDetectionStrategy } from '@angular/core';
import { Observable, BehaviorSubject } from 'rxjs';
import { SecondaryNavItem } from '../../../shared/constants/navigation.config';
import { NavigationService } from '../../services/navigation.service';
import { SearchBarComponent } from '../../../shared/components/search-bar/search-bar.component';
import { UserControlsComponent, UserControlAction, UserInfo } from '../../../shared/components/user-controls/user-controls.component';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';

@Component({
  selector: 'app-top-navigation',
  standalone: true,
  imports: [CommonModule, RouterModule, SearchBarComponent, UserControlsComponent],
  templateUrl: './top-navigation.component.html',
  styleUrls: ['./top-navigation.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class TopNavigationComponent {
  
  /**
   * Current route observable for active state detection
   */
  currentRoute$: Observable<string>;
  
  /**
   * Secondary navigation items with active state
   */
  topNavItems$: Observable<SecondaryNavItem[]>;
  
  /**
   * Search query subject for reactive search
   */
  searchQuery$ = new BehaviorSubject<string>('');
  
  /**
   * User information for avatar display
   */
  userInfo: UserInfo = {
    name: 'Admin User',
    email: 'admin@assetintel.com',
    initials: 'AU'
  };
  
  /**
   * Notification count
   */
  notificationCount = 3;
  
  constructor(private navigationService: NavigationService) {
    // Subscribe to current route for active state detection
    this.currentRoute$ = this.navigationService.currentRoute$;
    
    // Get top navigation items as observable
    this.topNavItems$ = new BehaviorSubject(this.navigationService.topNavigation);
    
    // Update top nav items when route changes
    this.currentRoute$.subscribe(() => {
      (this.topNavItems$ as BehaviorSubject<SecondaryNavItem[]>).next(
        this.navigationService.topNavigation
      );
    });
  }
  
  /**
   * Handle search input changes
   */
  onSearchInput(query: string): void {
    this.searchQuery$.next(query);
    // TODO: Implement search functionality
  }
  
  /**
   * Handle secondary navigation click
   */
  onSecondaryNavClick(item: SecondaryNavItem): void {
    this.navigationService.navigateTo(item.route);
  }
  
  /**
   * Check if a route is currently active
   */
  isActiveRoute(route: string): boolean {
    return this.navigationService.isActiveRoute(route);
  }
  
  /**
   * Handle user control actions
   */
  onUserControlClick(action: UserControlAction): void {
    console.log(`User control clicked: ${action}`);
    // TODO: Implement user control actions
    switch (action) {
      case 'notification':
        // Open notification panel
        break;
      case 'settings':
        // Navigate to settings
        this.navigationService.navigateTo('/settings');
        break;
      case 'profile':
        // Open user profile menu
        break;
    }
  }
  
  /**
   * Handle search submission
   */
  onSearchSubmit(query: string): void {
    console.log(`Search submitted: ${query}`);
    // TODO: Implement search submission
  }
}