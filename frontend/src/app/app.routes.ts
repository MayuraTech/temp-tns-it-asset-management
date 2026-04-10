import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth.guard';
import { roleGuard } from './core/guards/role.guard';
import { Role } from './core/models/auth.model';

/**
 * Application Routes - Editorial Geometry Dashboard
 * 
 * Defines the routing structure for the AssetIntel application
 * with support for all navigation items defined in navigation.config.ts
 * 
 * All routes are protected by authGuard to ensure authentication.
 * Role-specific routes use roleGuard with required roles in route data.
 */
export const routes: Routes = [
  {
    path: '',
    redirectTo: '/dashboard',
    pathMatch: 'full'
  },
  {
    path: 'login',
    loadComponent: () => import('./features/auth/login/login.component').then(m => m.LoginComponent)
  },
  {
    path: 'unauthorized',
    loadComponent: () => import('./features/auth/unauthorized/unauthorized.component').then(m => m.UnauthorizedComponent)
  },
  {
    path: 'dashboard',
    loadComponent: () => import('./features/dashboard/dashboard.component').then(m => m.DashboardComponent),
    canActivate: [authGuard],
    data: { 
      title: 'Dashboard'
    }
  },
  {
    path: 'assets',
    loadComponent: () => import('./features/assets/assets.component').then(m => m.AssetsComponent),
    canActivate: [authGuard],
    data: { 
      title: 'Assets'
    }
  },
  {
    path: 'assets/create',
    loadComponent: () => import('./features/assets/asset-create/asset-create.component').then(m => m.AssetCreateComponent),
    canActivate: [authGuard, roleGuard],
    data: { 
      title: 'Create Asset',
      roles: [Role.ADMINISTRATOR, Role.ASSET_MANAGER]
    }
  },
  {
    path: 'software',
    loadComponent: () => import('./features/software/software.component').then(m => m.SoftwareComponent),
    canActivate: [authGuard],
    data: { 
      title: 'Software'
    }
  },
  {
    path: 'licenses',
    loadComponent: () => import('./features/licenses/licenses.component').then(m => m.LicensesComponent),
    canActivate: [authGuard],
    data: { 
      title: 'Licenses'
    }
  },
  {
    path: 'network',
    loadComponent: () => import('./features/network/network.component').then(m => m.NetworkComponent),
    canActivate: [authGuard],
    data: { 
      title: 'Network'
    }
  },
  {
    path: 'users',
    loadChildren: () => import('./features/user-management/user-management.routes')
      .then(m => m.userManagementRoutes)
  },
  {
    path: 'audit-logs',
    loadComponent: () => import('./features/audit-logs/audit-logs.component').then(m => m.AuditLogsComponent),
    canActivate: [authGuard, roleGuard],
    data: { 
      title: 'Audit Logs',
      roles: [Role.ADMINISTRATOR]
    }
  },
  {
    path: 'archived',
    loadComponent: () => import('./features/archived/archived.component').then(m => m.ArchivedComponent),
    canActivate: [authGuard],
    data: { 
      title: 'Archived'
    }
  },
  {
    path: 'inventory',
    loadComponent: () => import('./features/inventory/inventory.component').then(m => m.InventoryComponent),
    canActivate: [authGuard],
    data: { 
      title: 'Inventory'
    }
  },
  {
    path: 'reports',
    loadComponent: () => import('./features/reports/reports.component').then(m => m.ReportsComponent),
    canActivate: [authGuard],
    data: { 
      title: 'Reports'
    }
  },
  {
    path: 'settings',
    loadComponent: () => import('./features/settings/settings.component').then(m => m.SettingsComponent),
    canActivate: [authGuard, roleGuard],
    data: { 
      title: 'Settings',
      roles: [Role.ADMINISTRATOR]
    }
  },
  {
    path: '**',
    redirectTo: '/dashboard'
  }
];
