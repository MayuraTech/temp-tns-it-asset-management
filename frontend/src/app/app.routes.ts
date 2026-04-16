import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth.guard';
import { roleGuard } from './core/guards/role.guard';
import { Role } from './core/models/auth.model';
import { loginGuard } from './core/guards/login.guard';

/**
 * Application Routes - Editorial Geometry Dashboard
 * 
 * Defines the routing structure for the TnS Assets application
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
    loadComponent: () => import('./features/login/login.component').then(m => m.LoginComponent),
    canActivate: [loginGuard]
  },
  {
    path: 'password-reset',
    loadComponent: () => import('./features/login/password-reset/password-reset.component').then(m => m.PasswordResetComponent),
    canActivate: [loginGuard]
  },
  {
    path: 'unauthorized',
    loadComponent: () => import('./features/auth/unauthorized/unauthorized.component').then(m => m.UnauthorizedComponent),
    canActivate: [authGuard]
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
    path: 'assets/new',
    loadComponent: () => import('./features/module2-assets/components/asset-form/asset-form.component').then(m => m.AssetFormComponent),
    canActivate: [authGuard, roleGuard],
    data: {
      title: 'Create Asset',
      roles: [Role.ADMINISTRATOR, Role.ASSET_MANAGER]
    }
  },
  {
    path: 'assets/create',
    redirectTo: '/assets/new',
    pathMatch: 'full'
  },
  {
    path: 'assets/:id/edit',
    loadComponent: () => import('./features/module2-assets/components/asset-form/asset-form.component').then(m => m.AssetFormComponent),
    canActivate: [authGuard, roleGuard],
    data: {
      title: 'Edit Asset',
      roles: [Role.ADMINISTRATOR, Role.ASSET_MANAGER]
    }
  },
  {
    path: 'assets/:id',
    loadComponent: () => import('./features/module2-assets/components/asset-detail/asset-detail.component').then(m => m.AssetDetailComponent),
    canActivate: [authGuard],
    data: {
      title: 'Asset Details'
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
