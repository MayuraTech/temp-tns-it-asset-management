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
import { RouterModule, Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';

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
  
  constructor(
    private navigationService: NavigationService,
    private router: Router,
    private authService: AuthService
  ) {
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
    
    // Load current user information
    this.loadUserInfo();
  }
  
  /**
   * Load current user information from AuthService
   */
  private loadUserInfo(): void {
    this.authService.currentUser$.subscribe(user => {
      if (user) {
        this.userInfo = {
          name: user.username || 'User',
          email: user.email || 'user@example.com',
          initials: this.getInitials(user.username || 'User')
        };
      }
    });
  }
  
  /**
   * Get user initials from username
   */
  private getInitials(username: string): string {
    const nameParts = username.split(' ');
    if (nameParts.length >= 2) {
      return `${nameParts[0][0]}${nameParts[1][0]}`.toUpperCase();
    }
    return username.substring(0, 2).toUpperCase();
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
    switch (action) {
      case 'notification':
        // Open notification panel
        break;
      case 'settings':
        // Navigate to settings
        this.navigationService.navigateTo('/settings');
        break;
      case 'profile':
        // Navigate to user profile
        this.navigationService.navigateTo('/users/profile');
        break;
      case 'logout':
        // Logout and redirect to login page
        this.authService.logout().subscribe({
          complete: () => {
            this.router.navigate(['/login']);
          },
          error: (error) => {
            console.error('Logout error:', error);
            // Navigate to login even if logout request fails
            this.router.navigate(['/login']);
          }
        });
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