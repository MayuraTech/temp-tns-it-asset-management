import { inject } from '@angular/core';
import { Router, CanActivateFn } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { Role } from '../../../core/models/auth.model';

/**
 * Route guard for allocation read operations (history, statistics)
 * 
 * Protects routes that require ADMINISTRATOR, ASSET_MANAGER, or VIEWER role.
 * Redirects unauthorized users to the unauthorized page.
 * 
 * Usage in routes:
 * {
 *   path: 'history/:id',
 *   component: AssignmentHistoryComponent,
 *   canActivate: [viewerGuard]
 * }
 */
export const viewerGuard: CanActivateFn = (route, state) => {
  const authService = inject(AuthService);
  const router = inject(Router);

  const currentUser = authService.currentUserValue;
  
  // Check if user is authenticated
  if (!currentUser) {
    router.navigate(['/login'], {
      queryParams: { returnUrl: state.url }
    });
    return false;
  }

  // Check if user has ADMINISTRATOR, ASSET_MANAGER, or VIEWER role
  const hasPermission = currentUser.roles.some(role => 
    role === Role.ADMINISTRATOR || 
    role === Role.ASSET_MANAGER || 
    role === Role.VIEWER
  );

  if (hasPermission) {
    return true;
  }

  // User doesn't have required role, redirect to unauthorized page
  router.navigate(['/unauthorized']);
  return false;
};
