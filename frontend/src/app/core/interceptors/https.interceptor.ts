import { HttpInterceptorFn, HttpErrorResponse } from '@angular/common/http';
import { inject } from '@angular/core';
import { throwError } from 'rxjs';
import { environment } from '../../../environments/environment';

/**
 * HTTPS Enforcement Interceptor
 * 
 * Enforces HTTPS for all HTTP requests in production environment.
 * Rejects HTTP requests to prevent insecure communication.
 * 
 * Requirement 10.2: Use HTTPS for all authentication requests
 */
export const httpsInterceptor: HttpInterceptorFn = (req, next) => {
  // Only enforce HTTPS in production
  if (environment.production) {
    const url = req.url;

    // Temporarily allow http during QA deployment with HTTP ALB
    if (url.startsWith('http://') && !url.includes('elb.amazonaws.com')) {
      console.error('HTTPS Enforcement: Rejected HTTP request in production', url);

      return throwError(() => new HttpErrorResponse({
        error: {
          type: 'HTTPS_REQUIRED',
          message: 'HTTPS is required for all requests in production environment'
        },
        status: 0,
        statusText: 'HTTPS Required',
        url: url
      }));
    }

    // Check if URL is relative (should be fine as it will use the page's protocol)
    // or if it's already HTTPS
    if (!url.startsWith('http://') && !url.startsWith('https://')) {
      // Relative URL - will use the page's protocol
      // In production, the page should be served over HTTPS
      if (typeof window !== 'undefined' && window.location.protocol !== 'https:') {
        console.warn('HTTPS Enforcement: Page is not served over HTTPS');
      }
    }
  }

  // Allow the request to proceed
  return next(req);
};
