import { inject } from '@angular/core';
import { Router, CanActivateFn, ActivatedRouteSnapshot } from '@angular/router';
import { AuthService } from '../services/auth.service';
import { Role } from '../models/auth.model';

/**
 * Route guard to protect routes based on user roles
 * 
 * Usage in routes:
 * {
 *   path: 'admin',
 *   component: AdminComponent,
 *   canActivate: [roleGuard],
 *   data: { roles: [Role.ADMINISTRATOR] }
 * }
 */
export const roleGuard: CanActivateFn = (route: ActivatedRouteSnapshot) => {
  const authService = inject(AuthService);
  const router = inject(Router);

  const currentUser = authService.currentUserValue;
  
  if (!currentUser) {
    router.navigate(['/login']);
    return false;
  }

  const requiredRoles = route.data['roles'] as Role[];
  
  if (!requiredRoles || requiredRoles.length === 0) {
    return true;
  }

  const hasRequiredRole = requiredRoles.some(role => 
    currentUser.roles.includes(role)
  );

  if (hasRequiredRole) {
    return true;
  }

  // User doesn't have required role, redirect to unauthorized page
  router.navigate(['/unauthorized']);
  return false;
};
