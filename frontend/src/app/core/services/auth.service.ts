import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable, throwError } from 'rxjs';
import { tap, catchError, map } from 'rxjs/operators';
import { LoginRequest, LoginResponse, User } from '../models/auth.model';
import { environment } from '../../../environments/environment';

/**
 * Authentication service for handling user login, logout, and session management
 */
@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private readonly apiUrl = `${environment.apiUrl}/auth`;
  private currentUserSubject = new BehaviorSubject<User | null>(null);
  private tokenExpirationTimer: any;

  public currentUser$ = this.currentUserSubject.asObservable();

  constructor(private http: HttpClient) {
    this.loadUserFromStorage();
  }

  /**
   * Get the current authenticated user
   */
  get currentUserValue(): User | null {
    return this.currentUserSubject.value;
  }

  /**
   * Check if user is authenticated
   */
  get isAuthenticated(): boolean {
    return !!this.currentUserValue && !!this.getAccessToken();
  }

  /**
   * Authenticate user with credentials
   */
  login(username: string, password: string): Observable<LoginResponse> {
    const request: LoginRequest = { username, password };

    return this.http.post<LoginResponse>(`${this.apiUrl}/login`, request)
      .pipe(
        tap(response => {
          this.storeTokens(response);
          this.startTokenExpirationTimer(response.expiresIn);
          this.loadCurrentUser();
        }),
        catchError(error => {
          console.error('Login failed:', error);
          return throwError(() => error);
        })
      );
  }

  /**
   * Terminate user session
   */
  logout(): Observable<void> {
    return this.http.post<void>(`${this.apiUrl}/logout`, {})
      .pipe(
        tap(() => this.clearSession()),
        catchError(error => {
          // Clear session even if logout request fails
          this.clearSession();
          return throwError(() => error);
        })
      );
  }

  /**
   * Refresh access token using refresh token
   */
  refreshToken(): Observable<LoginResponse> {
    const refreshToken = this.getRefreshToken();
    
    if (!refreshToken) {
      return throwError(() => new Error('No refresh token available'));
    }

    return this.http.post<LoginResponse>(`${this.apiUrl}/refresh`, { refreshToken })
      .pipe(
        tap(response => {
          this.storeTokens(response);
          this.startTokenExpirationTimer(response.expiresIn);
        }),
        catchError(error => {
          this.clearSession();
          return throwError(() => error);
        })
      );
  }

  /**
   * Change user password
   */
  changePassword(currentPassword: string, newPassword: string): Observable<void> {
    return this.http.post<void>(`${this.apiUrl}/change-password`, {
      currentPassword,
      newPassword
    });
  }

  /**
   * Get access token from storage
   */
  getAccessToken(): string | null {
    return localStorage.getItem('access_token');
  }

  /**
   * Get refresh token from storage
   */
  getRefreshToken(): string | null {
    return localStorage.getItem('refresh_token');
  }

  /**
   * Store authentication tokens
   */
  private storeTokens(response: LoginResponse): void {
    localStorage.setItem('access_token', response.accessToken);
    localStorage.setItem('refresh_token', response.refreshToken);
    localStorage.setItem('token_type', response.tokenType);
  }

  /**
   * Load current user information
   */
  private loadCurrentUser(): void {
    this.http.get<User>(`${environment.apiUrl}/users/me`)
      .subscribe({
        next: user => {
          this.currentUserSubject.next(user);
          localStorage.setItem('current_user', JSON.stringify(user));
        },
        error: error => {
          console.error('Failed to load user:', error);
          this.clearSession();
        }
      });
  }

  /**
   * Load user from local storage on service initialization
   */
  private loadUserFromStorage(): void {
    const userJson = localStorage.getItem('current_user');
    if (userJson) {
      try {
        const user = JSON.parse(userJson);
        this.currentUserSubject.next(user);
      } catch (error) {
        console.error('Failed to parse stored user:', error);
        localStorage.removeItem('current_user');
      }
    }
  }

  /**
   * Clear session data
   */
  private clearSession(): void {
    localStorage.removeItem('access_token');
    localStorage.removeItem('refresh_token');
    localStorage.removeItem('token_type');
    localStorage.removeItem('current_user');
    this.currentUserSubject.next(null);
    this.stopTokenExpirationTimer();
  }

  /**
   * Start timer to refresh token before expiration
   */
  private startTokenExpirationTimer(expiresIn: number): void {
    this.stopTokenExpirationTimer();
    
    // Refresh token 1 minute before expiration
    const refreshTime = (expiresIn - 60) * 1000;
    
    this.tokenExpirationTimer = setTimeout(() => {
      this.refreshToken().subscribe({
        error: () => this.clearSession()
      });
    }, refreshTime);
  }

  /**
   * Stop token expiration timer
   */
  private stopTokenExpirationTimer(): void {
    if (this.tokenExpirationTimer) {
      clearTimeout(this.tokenExpirationTimer);
      this.tokenExpirationTimer = null;
    }
  }
}
