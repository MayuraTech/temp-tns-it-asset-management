/**
 * Icon Service - Centralized Icon Registry
 *
 * Provides a centralized registry of icon names for consistent
 * icon management across the Editorial Geometry design system.
 *
 * Requirements: 18.3
 */

import { Injectable } from '@angular/core';

export type IconName =
  | 'assets-icon'
  | 'software-icon'
  | 'licenses-icon'
  | 'network-icon'
  | 'users-icon'
  | 'audit-icon'
  | 'archive-icon'
  | 'search-icon'
  | 'notification-icon'
  | 'settings-icon'
  | 'logo-icon'
  | 'geometric-triangle-accent'
  | 'geometric-triangle-large'
  | 'geometric-triangle-small';

@Injectable({
  providedIn: 'root'
})
export class IconService {
  private readonly iconRegistry: ReadonlySet<IconName> = new Set<IconName>([
    'assets-icon',
    'software-icon',
    'licenses-icon',
    'network-icon',
    'users-icon',
    'audit-icon',
    'archive-icon',
    'search-icon',
    'notification-icon',
    'settings-icon',
    'logo-icon',
    'geometric-triangle-accent',
    'geometric-triangle-large',
    'geometric-triangle-small'
  ]);

  /**
   * Returns all registered icon names.
   */
  getIconNames(): IconName[] {
    return Array.from(this.iconRegistry);
  }

  /**
   * Checks whether the given icon name exists in the registry.
   * @param name - The icon name to check
   */
  hasIcon(name: string): name is IconName {
    return this.iconRegistry.has(name as IconName);
  }
}
