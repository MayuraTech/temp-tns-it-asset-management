/**
 * Navigation Configuration for Editorial Geometry Dashboard
 * 
 * Defines the navigation structure for the AssetIntel application
 * following Editorial Geometry principles with geometric accents
 * and tonal layering.
 */

export interface NavigationItem {
  id: string;
  label: string;
  route: string;
  icon: string;
  type: 'primary' | 'secondary';
  order: number;
}

export interface SecondaryNavItem {
  id: string;
  label: string;
  route: string;
  active: boolean;
}

export interface NavigationConfig {
  primary: NavigationItem[];
  secondary: NavigationItem[];
  topNav: SecondaryNavItem[];
}

/**
 * Primary Navigation Items - Main application sections
 * Displayed in the sidebar with geometric accent indicators
 */
export const PRIMARY_NAVIGATION: NavigationItem[] = [
  {
    id: 'assets',
    label: 'Assets',
    route: '/assets',
    icon: 'assets-icon',
    type: 'primary',
    order: 1
  },
  {
    id: 'software',
    label: 'Software',
    route: '/software',
    icon: 'software-icon',
    type: 'primary',
    order: 2
  },
  {
    id: 'licenses',
    label: 'Licenses',
    route: '/licenses',
    icon: 'licenses-icon',
    type: 'primary',
    order: 3
  },
  {
    id: 'network',
    label: 'Network',
    route: '/network',
    icon: 'network-icon',
    type: 'primary',
    order: 4
  },
  {
    id: 'users',
    label: 'Users',
    route: '/users',
    icon: 'users-icon',
    type: 'primary',
    order: 5
  }
];

/**
 * Secondary Navigation Items - Supporting sections
 * Displayed below primary navigation with subtle styling
 */
export const SECONDARY_NAVIGATION: NavigationItem[] = [
  {
    id: 'audit-logs',
    label: 'Audit Logs',
    route: '/audit-logs',
    icon: 'audit-icon',
    type: 'secondary',
    order: 1
  },
  {
    id: 'archived',
    label: 'Archived',
    route: '/archived',
    icon: 'archive-icon',
    type: 'secondary',
    order: 2
  }
];

/**
 * Top Navigation Items - Secondary navigation in top bar
 * Displayed with editorial typography and geometric hover effects
 */
export const TOP_NAVIGATION: SecondaryNavItem[] = [
  {
    id: 'dashboard',
    label: 'Dashboard',
    route: '/dashboard',
    active: false
  },
  {
    id: 'inventory',
    label: 'Inventory',
    route: '/inventory',
    active: false
  },
  {
    id: 'reports',
    label: 'Reports',
    route: '/reports',
    active: false
  },
  {
    id: 'settings',
    label: 'Settings',
    route: '/settings',
    active: false
  }
];

/**
 * Complete Navigation Configuration
 * Combines all navigation sections for the dashboard layout
 */
export const NAVIGATION_CONFIG: NavigationConfig = {
  primary: PRIMARY_NAVIGATION,
  secondary: SECONDARY_NAVIGATION,
  topNav: TOP_NAVIGATION
};

/**
 * User Control Actions - Top navigation user controls
 */
export interface UserAction {
  type: 'notification' | 'settings' | 'profile';
  icon?: string;
}

export const USER_ACTIONS: UserAction[] = [
  {
    type: 'notification',
    icon: 'notification-icon'
  },
  {
    type: 'settings',
    icon: 'settings-icon'
  },
  {
    type: 'profile'
    // Profile uses avatar image, no icon
  }
];