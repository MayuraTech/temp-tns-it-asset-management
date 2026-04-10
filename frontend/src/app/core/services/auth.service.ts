import { Injectable } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { BehaviorSubject, Observable, throwError, timer, of } from 'rxjs';
import { tap, catchError, switchMap, map } from 'rxjs/operators';
import { LoginRequest, LoginResponse, User } from '../models/auth.model';
import { AuthError, AuthErrorType } from '../models/error.model';
import { environment } from '../../../environments/environment';
import { StorageService } from './storage.service';

/**
 * Authentication service for handling user login, logout, and session management
 * 
 * Requirements: 3.1, 3.3, 8.5, 10.2, 10.3, 10.4, 11.2, 11.3, 11.4
 */
@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private readonly apiUrl = `${environment.apiUrl}/auth`;
  private currentUserSubject = new BehaviorSubject<User | null>(null);
  private isAuthenticatedSubject = new BehaviorSubject<boolean>(false);
  private tokenExpirationTimer: any;
  private loadingStartTime: number = 0;
  private readonly MIN_LOADING_DURATION = 500; // Requirement 8.5

  public currentUser$ = this.currentUserSubject.asObservable();
  public isAuthenticated$ = this.isAuthenticatedSubject.asObservable();

  constructor(
    private http: HttpClient,
    private storageService: StorageService
  ) {
    this.initializeAuth();
  }

  /**
   * Get the current authenticated user
   */
  get currentUserValue(): User | null {
    return this.currentUserSubject.value;
  }

  /**
   * Get the current authenticated user (alias for currentUserValue)
   */
  getCurrentUser(): User | null {
    return this.currentUserSubject.value;
  }

  /**
   * Check if user is authenticated
   */
  get isAuthenticated(): boolean {
    return this.isAuthenticatedSubject.value;
  }

  /**
   * Initialize authentication state on service creation
   * 
   * Requirements:
   * - 11.3: Check for valid persistent token on load
   * - 11.4: Require re-authentication if token expired
   */
  private initializeAuth(): void {
    // Check for valid token in storage
    const accessToken = this.storageService.getItem('access_token');
    
    if (accessToken && this.storageService.hasValidToken('access_token')) {
      // Valid token found, load user data
      this.loadUserFromStorage();
      this.isAuthenticatedSubject.next(true);
    } else {
      // No valid token, user needs to authenticate
      this.isAuthenticatedSubject.next(false);
    }
  }

  /**
   * Check if there's a valid persistent token for automatic authentication
   * 
   * Requirements:
   * - 11.3: Auto-authenticate and redirect if token valid
   * - 11.4: Require re-authentication if token expired
   * 
   * @returns Observable that emits true if auto-authentication is possible
   */
  canAutoAuthenticate(): Observable<boolean> {
    const accessToken = this.storageService.getItem('access_token');
    
    if (!accessToken || !this.storageService.hasValidToken('access_token')) {
      return of(false);
    }

    // Token exists and is valid, verify with server
    return this.http.get<User>(`${environment.apiUrl}/users/me`)
      .pipe(
        map(user => {
          this.currentUserSubject.next(user);
          this.isAuthenticatedSubject.next(true);
          return true;
        }),
        catchError(() => {
          // Token invalid or expired, clear storage
          this.clearSession();
          return of(false);
        })
      );
  }

  /**
   * Authenticate user with credentials
   * 
   * Requirements:
   * - 3.1: Send authentication request to Authentication_Service
   * - 3.3: Store Session_Token and navigate to dashboard on success
   * - 8.5: Ensure loading state persists for minimum 500ms
   * - 10.2: Use HTTPS for all authentication requests
   * - 10.3: Do not store passwords in browser storage
   * - 10.4: Clear sensitive form data from memory after successful authentication
   * - 11.2: Store persistent Session_Token when Remember Me is checked
   * 
   * @param username User's username
   * @param password User's password
   * @param rememberMe Whether to persist session token
   * @returns Observable that emits LoginResponse on success or AuthError on failure
   */
  login(username: string, password: string, rememberMe: boolean = false): Observable<LoginResponse> {
    const request: LoginRequest = { username, password };
    this.loadingStartTime = Date.now();

    return this.http.post<LoginResponse>(`${this.apiUrl}/login`, request)
      .pipe(
        // Ensure minimum loading duration (Requirement 8.5)
        switchMap(response => {
          const elapsed = Date.now() - this.loadingStartTime;
          const remaining = Math.max(0, this.MIN_LOADING_DURATION - elapsed);
          
          // Wait for remaining time if needed, then emit response
          return timer(remaining).pipe(map(() => response));
        }),
        tap(response => {
          // Store session token using StorageService (Requirements 3.3, 11.2)
          this.storeTokens(response, rememberMe);
          this.startTokenExpirationTimer(response.expiresIn);
          this.loadCurrentUser();
          this.isAuthenticatedSubject.next(true);
        }),
        catchError(error => this.handleAuthError(error))
      );
  }

  /**
   * Handle authentication errors and map to appropriate error types
   * 
   * Requirements:
   * - 4.1: Display "Invalid username or password" for invalid credentials
   * - 4.2: Display account lockout message for locked accounts
   * - 4.3: Display network error message for connection issues
   * 
   * @param error HTTP error response
   * @returns Observable that throws AuthError
   */
  private handleAuthError(error: HttpErrorResponse): Observable<never> {
    let authError: AuthError;

    if (error.status === 0) {
      // Network error (Requirement 4.3)
      authError = {
        type: 'network_error',
        message: 'Unable to connect to server. Please check your connection and try again.',
        timestamp: new Date()
      };
    } else if (error.status === 401) {
      // Invalid credentials (Requirement 4.1)
      authError = {
        type: 'invalid_credentials',
        message: 'Invalid username or password. Please try again.',
        timestamp: new Date()
      };
    } else if (error.status === 423) {
      // Account locked (Requirement 4.2)
      authError = {
        type: 'account_locked',
        message: 'Account locked due to multiple failed attempts. Please try again in 15 minutes.',
        timestamp: new Date()
      };
    } else {
      // Unknown error
      authError = {
        type: 'unknown',
        message: error.error?.message || 'An unexpected error occurred. Please try again later.',
        timestamp: new Date()
      };
    }

    return throwError(() => authError);
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
          const isPersistent = this.storageService.isPersistent('access_token');
          this.storeTokens(response, isPersistent);
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
    return this.storageService.getItem('access_token');
  }

  /**
   * Get refresh token from storage
   */
  getRefreshToken(): string | null {
    return this.storageService.getItem('refresh_token');
  }

  /**
   * Store authentication tokens using StorageService
   * 
   * Requirements:
   * - 11.2: Store persistent Session_Token when Remember Me is checked
   * - 11.4: Handle token expiration (30 days)
   * - 10.3: Do not store passwords in browser storage
   * 
   * @param response Login response containing tokens
   * @param persistent Whether to persist session (Remember Me)
   */
  private storeTokens(response: LoginResponse, persistent: boolean = false): void {
    this.storageService.setItem('access_token', response.accessToken, persistent);
    this.storageService.setItem('refresh_token', response.refreshToken, persistent);
    this.storageService.setItem('token_type', response.tokenType, persistent);
  }

  /**
   * Load current user information
   */
  private loadCurrentUser(): void {
    this.http.get<User>(`${environment.apiUrl}/users/me`)
      .subscribe({
        next: user => {
          this.currentUserSubject.next(user);
          const isPersistent = this.storageService.isPersistent('access_token');
          this.storageService.setItem('current_user', JSON.stringify(user), isPersistent);
        },
        error: error => {
          console.error('Failed to load user:', error);
          this.clearSession();
        }
      });
  }

  /**
   * Load user from storage on service initialization
   * 
   * Requirements:
   * - 11.3: Check for valid persistent token on load
   */
  private loadUserFromStorage(): void {
    const userJson = this.storageService.getItem('current_user');
    if (userJson) {
      try {
        const user = JSON.parse(userJson);
        this.currentUserSubject.next(user);
      } catch (error) {
        console.error('Failed to parse stored user:', error);
        this.storageService.removeItem('current_user');
      }
    }
  }

  /**
   * Clear session data
   * 
   * Requirement 10.4: Clear sensitive form data from memory after successful authentication
   */
  private clearSession(): void {
    this.storageService.clear();
    this.currentUserSubject.next(null);
    this.isAuthenticatedSubject.next(false);
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
