import { Injectable } from '@angular/core';
import { HttpClient, HttpParams, HttpErrorResponse } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { TicketMetrics } from '../../../shared/models/ticket.model';

/**
 * Service for ticket metrics and analytics
 */
@Injectable({
  providedIn: 'root'
})
export class TicketMetricsService {
  private readonly apiUrl = '/api/v1/tickets/metrics';

  constructor(private http: HttpClient) {}

  /**
   * Get comprehensive ticket metrics
   */
  getMetrics(fromDate?: string, toDate?: string): Observable<TicketMetrics> {
    let params = new HttpParams();

    if (fromDate) {
      params = params.set('from', fromDate);
    }

    if (toDate) {
      params = params.set('to', toDate);
    }

    return this.http.get<TicketMetrics>(this.apiUrl, { params })
      .pipe(catchError(this.handleError));
  }

  /**
   * Get ticket counts by status
   */
  getTicketsByStatus(fromDate?: string, toDate?: string): Observable<Record<string, number>> {
    let params = new HttpParams();

    if (fromDate) {
      params = params.set('from', fromDate);
    }

    if (toDate) {
      params = params.set('to', toDate);
    }

    return this.http.get<Record<string, number>>(`${this.apiUrl}/by-status`, { params })
      .pipe(catchError(this.handleError));
  }

  /**
   * Get ticket counts by type
   */
  getTicketsByType(fromDate?: string, toDate?: string): Observable<Record<string, number>> {
    let params = new HttpParams();

    if (fromDate) {
      params = params.set('from', fromDate);
    }

    if (toDate) {
      params = params.set('to', toDate);
    }

    return this.http.get<Record<string, number>>(`${this.apiUrl}/by-type`, { params })
      .pipe(catchError(this.handleError));
  }

  /**
   * Get ticket counts by priority
   */
  getTicketsByPriority(fromDate?: string, toDate?: string): Observable<Record<string, number>> {
    let params = new HttpParams();

    if (fromDate) {
      params = params.set('from', fromDate);
    }

    if (toDate) {
      params = params.set('to', toDate);
    }

    return this.http.get<Record<string, number>>(`${this.apiUrl}/by-priority`, { params })
      .pipe(catchError(this.handleError));
  }

  /**
   * Handle HTTP errors
   */
  private handleError(error: HttpErrorResponse): Observable<never> {
    let errorMessage = 'An error occurred';

    if (error.error instanceof ErrorEvent) {
      // Client-side error
      errorMessage = error.error.message;
    } else {
      // Server-side error
      errorMessage = error.error?.message || `Error Code: ${error.status}`;
    }

    return throwError(() => new Error(errorMessage));
  }
}
