import { Routes } from '@angular/router';
import { authGuard } from '../../core/guards/auth.guard';
import { roleGuard } from '../../core/guards/role.guard';
import { Role } from '../../core/models/auth.model';

/**
 * User Management Feature Routes
 * 
 * Defines routing for user management functionality including:
 * - User list view
 * - User detail view
 * - User creation
 * - User editing
 * - Profile management
 */
export const userManagementRoutes: Routes = [
  {
    path: '',
    canActivate: [authGuard],
    children: [
      {
        path: '',
        loadComponent: () => import('./components/user-list/user-list.component')
          .then(m => m.UserListComponent),
        data: { 
          title: 'Users',
          requiredRoles: [Role.ADMINISTRATOR, Role.ASSET_MANAGER, Role.VIEWER]
        }
      },
      {
        path: 'create',
        loadComponent: () => import('./components/user-create/user-create.component')
          .then(m => m.UserCreateComponent),
        canActivate: [roleGuard],
        data: { 
          title: 'Create User',
          requiredRoles: [Role.ADMINISTRATOR]
        }
      },
      {
        path: 'profile',
        loadComponent: () => import('./components/user-profile/user-profile.component')
          .then(m => m.UserProfileComponent),
        data: { 
          title: 'My Profile'
        }
      },
      {
        path: ':id',
        loadComponent: () => import('./components/user-detail/user-detail.component')
          .then(m => m.UserDetailComponent),
        data: { 
          title: 'User Details',
          requiredRoles: [Role.ADMINISTRATOR, Role.ASSET_MANAGER]
        }
      },
      {
        path: ':id/edit',
        loadComponent: () => import('./components/user-edit/user-edit.component')
          .then(m => m.UserEditComponent),
        canActivate: [roleGuard],
        data: { 
          title: 'Edit User',
          requiredRoles: [Role.ADMINISTRATOR]
        }
      }
    ]
  }
];
