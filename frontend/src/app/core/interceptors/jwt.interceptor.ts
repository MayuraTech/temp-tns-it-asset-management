import { HttpInterceptorFn, HttpErrorResponse, HttpEvent, HttpRequest, HttpHandlerFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, switchMap, throwError, BehaviorSubject, filter, take, Observable } from 'rxjs';
import { AuthService } from '../services/auth.service';

/**
 * Flag to track if token refresh is in progress
 */
let isRefreshing = false;

/**
 * Subject to queue requests while token is being refreshed
 */
const refreshTokenSubject = new BehaviorSubject<string | null>(null);

/**
 * HTTP interceptor to add JWT token to outgoing requests and handle token refresh
 * 
 * Features:
 * - Automatically attaches JWT access token to all outgoing requests
 * - Handles token refresh when access token expires (401 response)
 * - Queues requests during token refresh to prevent multiple refresh calls
 * - Redirects to login on authentication failures
 * 
 * @param req - The outgoing HTTP request
 * @param next - The next handler in the interceptor chain
 * @returns Observable of the HTTP response
 */
export const jwtInterceptor: HttpInterceptorFn = (req, next) => {
  const authService = inject(AuthService);
  const router = inject(Router);

  // Skip adding token for authentication endpoints
  const isAuthEndpoint = req.url.includes('/auth/login') || 
                         req.url.includes('/auth/refresh');

  // Clone request and add authorization header if token exists
  const token = authService.getAccessToken();
  if (token && !isAuthEndpoint) {
    req = addAuthorizationHeader(req, token);
  }

  // Handle the request and catch authentication errors
  return next(req).pipe(
    catchError((error: unknown) => {
      // Handle 401 Unauthorized errors (token expired or invalid)
      if (error instanceof HttpErrorResponse && error.status === 401 && !isAuthEndpoint) {
        return handle401Error(req, next, authService, router);
      }

      // For other errors, pass them through
      return throwError(() => error);
    })
  );
};

/**
 * Add Authorization header to the request
 */
function addAuthorizationHeader(req: HttpRequest<unknown>, token: string): HttpRequest<unknown> {
  return req.clone({
    setHeaders: {
      Authorization: `Bearer ${token}`
    }
  });
}

/**
 * Handle 401 Unauthorized errors by attempting to refresh the token
 * 
 * This function implements a queuing mechanism to prevent multiple simultaneous
 * token refresh requests. When a 401 error occurs:
 * 1. If no refresh is in progress, initiate token refresh
 * 2. If refresh is in progress, queue the request until refresh completes
 * 3. Retry the original request with the new token
 * 4. If refresh fails, clear session and redirect to login
 */
function handle401Error(
  req: HttpRequest<unknown>, 
  next: HttpHandlerFn, 
  authService: AuthService, 
  router: Router
): Observable<HttpEvent<unknown>> {
  if (!isRefreshing) {
    // Start token refresh process
    isRefreshing = true;
    refreshTokenSubject.next(null);

    const refreshToken = authService.getRefreshToken();

    // If no refresh token available, redirect to login
    if (!refreshToken) {
      isRefreshing = false;
      authService.logout().subscribe({
        complete: () => router.navigate(['/login'])
      });
      return throwError(() => new Error('No refresh token available'));
    }

    // Attempt to refresh the token
    return authService.refreshToken().pipe(
      switchMap((response) => {
        // Token refresh successful
        isRefreshing = false;
        refreshTokenSubject.next(response.access_token);

        // Retry the original request with new token
        return next(addAuthorizationHeader(req, response.access_token));
      }),
      catchError((error) => {
        // Token refresh failed - clear session and redirect to login
        isRefreshing = false;
        refreshTokenSubject.next(null);
        
        authService.logout().subscribe({
          complete: () => router.navigate(['/login'], {
            queryParams: { returnUrl: router.url, reason: 'session-expired' }
          })
        });

        return throwError(() => new Error('Token refresh failed'));
      })
    );
  } else {
    // Token refresh is already in progress - queue this request
    return refreshTokenSubject.pipe(
      filter(token => token !== null), // Wait for refresh to complete
      take(1), // Take only the first emission
      switchMap(token => {
        // Retry the original request with the new token
        return next(addAuthorizationHeader(req, token!));
      })
    );
  }
}
