import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth.guard';
import { loginGuard } from './core/guards/login.guard';

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
    path: 'login',
    loadComponent: () => import('./features/login/login.component').then(m => m.LoginComponent),
    canActivate: [loginGuard]
  },
  {
    path: 'password-reset',
    loadComponent: () => import('./features/login/password-reset/password-reset.component').then(m => m.PasswordResetComponent),
    canActivate: [loginGuard]
  },
  {
    path: 'dashboard',
    loadComponent: () => import('./features/dashboard/dashboard.component').then(m => m.DashboardComponent),
    canActivate: [authGuard]
  },
  {
    path: 'assets',
    loadComponent: () => import('./features/assets/assets.component').then(m => m.AssetsComponent),
    canActivate: [authGuard]
  },
  {
    path: 'assets/create',
    loadComponent: () => import('./features/assets/asset-create/asset-create.component').then(m => m.AssetCreateComponent),
    canActivate: [authGuard]
  },
  {
    path: 'software',
    loadComponent: () => import('./features/software/software.component').then(m => m.SoftwareComponent),
    canActivate: [authGuard]
  },
  {
    path: 'licenses',
    loadComponent: () => import('./features/licenses/licenses.component').then(m => m.LicensesComponent),
    canActivate: [authGuard]
  },
  {
    path: 'network',
    loadComponent: () => import('./features/network/network.component').then(m => m.NetworkComponent),
    canActivate: [authGuard]
  },
  {
    path: 'users',
    loadComponent: () => import('./features/users/users.component').then(m => m.UsersComponent),
    canActivate: [authGuard]
  },
  {
    path: 'audit-logs',
    loadComponent: () => import('./features/audit-logs/audit-logs.component').then(m => m.AuditLogsComponent),
    canActivate: [authGuard]
  },
  {
    path: 'archived',
    loadComponent: () => import('./features/archived/archived.component').then(m => m.ArchivedComponent),
    canActivate: [authGuard]
  },
  {
    path: 'inventory',
    loadComponent: () => import('./features/inventory/inventory.component').then(m => m.InventoryComponent),
    canActivate: [authGuard]
  },
  {
    path: 'reports',
    loadComponent: () => import('./features/reports/reports.component').then(m => m.ReportsComponent),
    canActivate: [authGuard]
  },
  {
    path: 'settings',
    loadComponent: () => import('./features/settings/settings.component').then(m => m.SettingsComponent),
    canActivate: [authGuard]
  },
  {
    path: '**',
    redirectTo: '/dashboard'
  }
];
