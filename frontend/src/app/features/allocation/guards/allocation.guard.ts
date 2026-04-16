import { inject } from '@angular/core';
import { Router, CanActivateFn } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { Role } from '../../../core/models/auth.model';

/**
 * Route guard for allocation write operations (assign, deallocate)
 * 
 * Protects routes that require ADMINISTRATOR or ASSET_MANAGER role.
 * Redirects unauthorized users to the unauthorized page.
 * 
 * Usage in routes:
 * {
 *   path: 'assign/:id',
 *   component: AllocationFormComponent,
 *   canActivate: [allocationGuard]
 * }
 */
export const allocationGuard: CanActivateFn = (route, state) => {
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

  // Check if user has ADMINISTRATOR or ASSET_MANAGER role
  const hasPermission = currentUser.roles.some(role => 
    role === Role.ADMINISTRATOR || role === Role.ASSET_MANAGER
  );

  if (hasPermission) {
    return true;
  }

  // User doesn't have required role, redirect to unauthorized page
  router.navigate(['/unauthorized']);
  return false;
};
