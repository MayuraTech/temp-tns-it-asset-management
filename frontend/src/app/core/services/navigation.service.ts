/**
 * Navigation Service - Editorial Geometry Navigation State Management
 * 
 * Manages navigation state for the AssetIntel dashboard layout
 * with reactive updates for active states and geometric accent indicators.
 */

import { Injectable } from '@angular/core';
import { Router, NavigationEnd } from '@angular/router';
import { BehaviorSubject, Observable } from 'rxjs';
import { filter, map } from 'rxjs/operators';

import { 
  NAVIGATION_CONFIG, 
  NavigationItem, 
  SecondaryNavItem 
} from '../../shared/constants/navigation.config';

@Injectable({
  providedIn: 'root'
})
export class NavigationService {
  private currentRouteSubject = new BehaviorSubject<string>('/dashboard');
  
  constructor(private router: Router) {
    // Listen to router events to update current route
    this.router.events
      .pipe(
        filter(event => event instanceof NavigationEnd),
        map(event => (event as NavigationEnd).url)
      )
      .subscribe(url => {
        this.currentRouteSubject.next(url);
      });
  }

  /**
   * Get the current route as an observable
   */
  get currentRoute$(): Observable<string> {
    return this.currentRouteSubject.asObservable();
  }

  /**
   * Get the current route value
   */
  get currentRoute(): string {
    return this.currentRouteSubject.value;
  }

  /**
   * Get primary navigation items
   */
  get primaryNavigation(): NavigationItem[] {
    return NAVIGATION_CONFIG.primary;
  }

  /**
   * Get secondary navigation items
   */
  get secondaryNavigation(): NavigationItem[] {
    return NAVIGATION_CONFIG.secondary;
  }

  /**
   * Get top navigation items with active state
   */
  get topNavigation(): SecondaryNavItem[] {
    const currentRoute = this.currentRoute;
    return NAVIGATION_CONFIG.topNav.map(item => ({
      ...item,
      active: currentRoute.startsWith(item.route)
    }));
  }

  /**
   * Check if a route is currently active
   */
  isActiveRoute(route: string): boolean {
    return this.currentRoute.startsWith(route);
  }

  /**
   * Navigate to a specific route
   */
  navigateTo(route: string): void {
    this.router.navigate([route]);
  }

  /**
   * Get navigation item by route
   */
  getNavigationItemByRoute(route: string): NavigationItem | undefined {
    const allItems = [...NAVIGATION_CONFIG.primary, ...NAVIGATION_CONFIG.secondary];
    return allItems.find(item => item.route === route);
  }

  /**
   * Get active navigation item
   */
  getActiveNavigationItem(): NavigationItem | undefined {
    return this.getNavigationItemByRoute(this.currentRoute);
  }
}