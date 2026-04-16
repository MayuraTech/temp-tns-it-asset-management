import { inject } from '@angular/core';
import { Router, CanActivateFn } from '@angular/router';
import { AuthService } from '../services/auth.service';

/**
 * Login Guard
 * 
 * Prevents authenticated users from accessing the login page.
 * Redirects authenticated users to the dashboard or their intended destination.
 * 
 * Requirements: 3.3, 11.3
 */
export const loginGuard: CanActivateFn = (route, state) => {
  const authService = inject(AuthService);
  const router = inject(Router);

  // If user is already authenticated, redirect to dashboard
  if (authService.isAuthenticated) {
    // Check if there's a return URL in query params
    const returnUrl = route.queryParams['returnUrl'];
    
    if (returnUrl) {
      router.navigateByUrl(returnUrl);
    } else {
      router.navigate(['/dashboard']);
    }
    
    return false;
  }

  // Allow access to login page for unauthenticated users
  return true;
};
