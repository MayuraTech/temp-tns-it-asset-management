import { HttpInterceptorFn, HttpErrorResponse } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';

/**
 * HTTP interceptor for global error handling
 * 
 * Handles HTTP errors and provides user-friendly error messages.
 * Authentication errors (401) are handled by the JWT interceptor.
 */
export const errorInterceptor: HttpInterceptorFn = (req, next) => {
  const router = inject(Router);

  return next(req).pipe(
    catchError((error: HttpErrorResponse) => {
      let errorMessage = 'An error occurred';

      if (error.error instanceof ErrorEvent) {
        // Client-side error
        errorMessage = `Error: ${error.error.message}`;
      } else {
        // Server-side error
        switch (error.status) {
          case 401:
            // Unauthorized - handled by JWT interceptor
            // This case should rarely be reached as JWT interceptor handles token refresh
            errorMessage = 'Authentication failed. Please log in again.';
            break;

          case 403:
            // Forbidden - insufficient permissions
            errorMessage = 'You do not have permission to perform this action.';
            router.navigate(['/unauthorized']);
            break;

          case 404:
            // Not found
            errorMessage = 'The requested resource was not found.';
            break;

          case 409:
            // Conflict (e.g., duplicate serial number)
            errorMessage = error.error?.error?.message || 'A conflict occurred.';
            break;

          case 422:
            // Unprocessable entity (e.g., invalid state transition)
            errorMessage = error.error?.error?.message || 'Invalid operation.';
            break;

          case 429:
            // Too many requests
            errorMessage = 'Too many requests. Please try again later.';
            break;

          case 500:
            // Internal server error
            errorMessage = 'An internal server error occurred. Please try again later.';
            break;

          case 503:
            // Service unavailable
            errorMessage = 'The service is temporarily unavailable. Please try again later.';
            break;

          default:
            errorMessage = error.error?.error?.message || 
                          `Error Code: ${error.status}\nMessage: ${error.message}`;
        }
      }

      console.error('HTTP Error:', {
        status: error.status,
        message: errorMessage,
        error: error.error
      });

      return throwError(() => new Error(errorMessage));
    })
  );
};
