import { Injectable } from '@angular/core';
import { HttpClient, HttpParams, HttpErrorResponse } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError, map } from 'rxjs/operators';
import {
  Ticket,
  AllocationTicketRequest,
  DeallocationTicketRequest,
  TicketSearchQuery,
  TicketStatusHistory,
  TicketPriority
} from '../../../shared/models/ticket.model';
import { PageResponse, SpringPagePayload, mapSpringToPageResponse } from '../../../shared/models/page-response.model';

/**
 * Service for managing tickets (allocation and de-allocation requests)
 */
@Injectable({
  providedIn: 'root'
})
export class TicketService {
  private readonly apiUrl = '/api/v1/tickets';

  constructor(private http: HttpClient) {}

  /**
   * Get tickets with optional filtering and pagination
   */
  getTickets(query?: TicketSearchQuery, page: number = 0, size: number = 20): Observable<PageResponse<Ticket>> {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());

    if (query) {
      params = this.buildQueryParams(params, query);
    }

    return this.http.get<PageResponse<Ticket>>(this.apiUrl, { params })
      .pipe(
        map(body => mapSpringToPageResponse(body as SpringPagePayload<Ticket>)),
        catchError(this.handleError)
      );
  }

  /**
   * Get a single ticket by ID
   */
  getTicket(id: string): Observable<Ticket> {
    return this.http.get<Ticket>(`${this.apiUrl}/${id}`)
      .pipe(catchError(this.handleError));
  }

  /**
   * Create an allocation ticket
   */
  createAllocationTicket(request: AllocationTicketRequest): Observable<Ticket> {
    return this.http.post<Ticket>(`${this.apiUrl}/allocation`, request)
      .pipe(catchError(this.handleError));
  }

  /**
   * Create a de-allocation ticket
   */
  createDeallocationTicket(request: DeallocationTicketRequest): Observable<Ticket> {
    return this.http.post<Ticket>(`${this.apiUrl}/deallocation`, request)
      .pipe(catchError(this.handleError));
  }

  /**
   * Get tickets created by the current user
   */
  getMyTickets(page: number = 0, size: number = 20): Observable<PageResponse<Ticket>> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());

    return this.http.get<PageResponse<Ticket>>(`${this.apiUrl}/my-requests`, { params })
      .pipe(
        map(body => mapSpringToPageResponse(body as SpringPagePayload<Ticket>)),
        catchError(this.handleError)
      );
  }

  /**
   * Get tickets pending approval (for approvers)
   */
  getPendingApprovals(page: number = 0, size: number = 20): Observable<PageResponse<Ticket>> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());

    return this.http.get<PageResponse<Ticket>>(`${this.apiUrl}/pending-approvals`, { params })
      .pipe(
        map(body => mapSpringToPageResponse(body as SpringPagePayload<Ticket>)),
        catchError(this.handleError)
      );
  }

  /**
   * Get status history for a ticket
   */
  getStatusHistory(ticketId: string): Observable<TicketStatusHistory[]> {
    return this.http.get<TicketStatusHistory[]>(`${this.apiUrl}/${ticketId}/status-history`)
      .pipe(catchError(this.handleError));
  }

  /**
   * Approve a ticket
   */
  approveTicket(ticketId: string, comments?: string): Observable<Ticket> {
    return this.http.post<Ticket>(`${this.apiUrl}/${ticketId}/approve`, { comments })
      .pipe(catchError(this.handleError));
  }

  /**
   * Reject a ticket
   */
  rejectTicket(ticketId: string, reason: string): Observable<Ticket> {
    return this.http.post<Ticket>(`${this.apiUrl}/${ticketId}/reject`, { reason })
      .pipe(catchError(this.handleError));
  }

  /**
   * Complete a ticket
   */
  completeTicket(ticketId: string): Observable<Ticket> {
    return this.http.post<Ticket>(`${this.apiUrl}/${ticketId}/complete`, {})
      .pipe(catchError(this.handleError));
  }

  /**
   * Cancel a ticket
   */
  cancelTicket(ticketId: string, reason?: string): Observable<Ticket> {
    return this.http.post<Ticket>(`${this.apiUrl}/${ticketId}/cancel`, { reason })
      .pipe(catchError(this.handleError));
  }

  /**
   * Update ticket priority
   */
  updatePriority(ticketId: string, priority: TicketPriority): Observable<Ticket> {
    return this.http.patch<Ticket>(`${this.apiUrl}/${ticketId}/priority`, { priority })
      .pipe(catchError(this.handleError));
  }

  /**
   * Bulk approve tickets
   */
  bulkApprove(ticketIds: string[], comments?: string): Observable<any> {
    return this.http.post(`${this.apiUrl}/bulk-approve`, { ticketIds, comments })
      .pipe(catchError(this.handleError));
  }

  /**
   * Bulk reject tickets
   */
  bulkReject(ticketIds: string[], reason: string): Observable<any> {
    return this.http.post(`${this.apiUrl}/bulk-reject`, { ticketIds, reason })
      .pipe(catchError(this.handleError));
  }

  /**
   * Build query parameters from search query
   */
  private buildQueryParams(params: HttpParams, query: TicketSearchQuery): HttpParams {
    if (query.statuses?.length) {
      query.statuses.forEach(status => {
        params = params.append('statuses', status);
      });
    }

    if (query.types?.length) {
      query.types.forEach(type => {
        params = params.append('types', type);
      });
    }

    if (query.priorities?.length) {
      query.priorities.forEach(priority => {
        params = params.append('priorities', priority);
      });
    }

    if (query.requesterId) {
      params = params.set('requesterId', query.requesterId);
    }

    if (query.approverId) {
      params = params.set('approverId', query.approverId);
    }

    if (query.assetId) {
      params = params.set('assetId', query.assetId);
    }

    if (query.createdFrom) {
      params = params.set('createdFrom', query.createdFrom);
    }

    if (query.createdTo) {
      params = params.set('createdTo', query.createdTo);
    }

    return params;
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
