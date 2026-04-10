import { Injectable } from '@angular/core';
import { HttpClient, HttpParams, HttpErrorResponse } from '@angular/common/http';
import { Observable, throwError, interval, BehaviorSubject } from 'rxjs';
import { catchError, switchMap, tap } from 'rxjs/operators';
import { Notification } from '../../../shared/models/ticket.model';

/**
 * Service for managing ticket notifications
 */
@Injectable({
  providedIn: 'root'
})
export class NotificationService {
  private readonly apiUrl = '/api/v1/notifications';
  private readonly pollingInterval = 30000; // 30 seconds

  // Observable for unread count
  private unreadCountSubject = new BehaviorSubject<number>(0);
  public unreadCount$ = this.unreadCountSubject.asObservable();

  constructor(private http: HttpClient) {}

  /**
   * Get notifications for the current user
   */
  getNotifications(page: number = 0, size: number = 20, unreadOnly: boolean = false): Observable<Notification[]> {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());

    if (unreadOnly) {
      params = params.set('unreadOnly', 'true');
    }

    return this.http.get<Notification[]>(this.apiUrl, { params })
      .pipe(catchError(this.handleError));
  }

  /**
   * Get unread notification count
   */
  getUnreadCount(): Observable<number> {
    return this.http.get<{ count: number }>(`${this.apiUrl}/unread-count`)
      .pipe(
        tap(response => this.unreadCountSubject.next(response.count)),
        catchError(this.handleError)
      );
  }

  /**
   * Mark a notification as read
   */
  markAsRead(notificationId: string): Observable<void> {
    return this.http.patch<void>(`${this.apiUrl}/${notificationId}/read`, {})
      .pipe(
        tap(() => {
          // Decrement unread count
          const currentCount = this.unreadCountSubject.value;
          this.unreadCountSubject.next(Math.max(0, currentCount - 1));
        }),
        catchError(this.handleError)
      );
  }

  /**
   * Mark all notifications as read
   */
  markAllAsRead(): Observable<void> {
    return this.http.patch<void>(`${this.apiUrl}/mark-all-read`, {})
      .pipe(
        tap(() => this.unreadCountSubject.next(0)),
        catchError(this.handleError)
      );
  }

  /**
   * Start polling for new notifications
   * Returns an observable that emits the unread count at regular intervals
   */
  startPolling(): Observable<number> {
    return interval(this.pollingInterval).pipe(
      switchMap(() => this.getUnreadCount())
    );
  }

  /**
   * Refresh unread count immediately
   */
  refreshUnreadCount(): void {
    this.getUnreadCount().subscribe();
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
