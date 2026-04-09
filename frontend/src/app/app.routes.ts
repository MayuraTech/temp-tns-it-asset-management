import { Routes } from '@angular/router';

/**
 * Application Routes - Editorial Geometry Dashboard
 * 
 * Defines the routing structure for the AssetIntel application
 * with support for all navigation items defined in navigation.config.ts
 */
export const routes: Routes = [
  {
    path: '',
    redirectTo: '/dashboard',
    pathMatch: 'full'
  },
  {
    path: 'dashboard',
    loadComponent: () => import('./features/dashboard/dashboard.component').then(m => m.DashboardComponent)
  },
  {
    path: 'assets',
    loadComponent: () => import('./features/assets/assets.component').then(m => m.AssetsComponent)
  },
  {
    path: 'assets/create',
    loadComponent: () => import('./features/assets/asset-create/asset-create.component').then(m => m.AssetCreateComponent)
  },
  {
    path: 'software',
    loadComponent: () => import('./features/software/software.component').then(m => m.SoftwareComponent)
  },
  {
    path: 'licenses',
    loadComponent: () => import('./features/licenses/licenses.component').then(m => m.LicensesComponent)
  },
  {
    path: 'network',
    loadComponent: () => import('./features/network/network.component').then(m => m.NetworkComponent)
  },
  {
    path: 'users',
    loadComponent: () => import('./features/users/users.component').then(m => m.UsersComponent)
  },
  {
    path: 'audit-logs',
    loadComponent: () => import('./features/audit-logs/audit-logs.component').then(m => m.AuditLogsComponent)
  },
  {
    path: 'archived',
    loadComponent: () => import('./features/archived/archived.component').then(m => m.ArchivedComponent)
  },
  {
    path: 'inventory',
    loadComponent: () => import('./features/inventory/inventory.component').then(m => m.InventoryComponent)
  },
  {
    path: 'reports',
    loadComponent: () => import('./features/reports/reports.component').then(m => m.ReportsComponent)
  },
  {
    path: 'settings',
    loadComponent: () => import('./features/settings/settings.component').then(m => m.SettingsComponent)
  },
  {
    path: '**',
    redirectTo: '/dashboard'
  }
];
