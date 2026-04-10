# API Usage Guide for Frontend Developers

## Table of Contents

1. [Introduction](#introduction)
2. [Getting Started](#getting-started)
3. [Authentication Implementation](#authentication-implementation)
4. [Angular Service Examples](#angular-service-examples)
5. [Error Handling](#error-handling)
6. [Common Patterns](#common-patterns)
7. [TypeScript Interfaces](#typescript-interfaces)
8. [Best Practices](#best-practices)

## Introduction

This guide provides practical examples and best practices for integrating the User Management API into your Angular frontend application. All examples use Angular 17+ with TypeScript and RxJS.

### Prerequisites

- Angular 17+
- TypeScript 5+
- RxJS 7+
- HttpClient module

### Base Configuration

```typescript
// environment.ts
export const environment = {
  production: false,
  apiUrl: 'http://localhost:8080/api/v1'
};
```

## Getting Started

### 1. Import HttpClientModule

```typescript
// app.config.ts
import { ApplicationConfig } from '@angular/core';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { authInterceptor } from './core/interceptors/auth.interceptor';

export const appConfig: ApplicationConfig = {
  providers: [
    provideHttpClient(
      withInterceptors([authInterceptor])
    )
  ]
};
```

### 2. Create HTTP Interceptor for Authentication

```typescript
// core/interceptors/auth.interceptor.ts
import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { AuthService } from '../services/auth.service';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const authService = inject(AuthService);
  const token = authService.getAccessToken();
  
  if (token) {
    req = req.clone({
      setHeaders: {
        Authorization: `Bearer ${token}`
      }
    });
  }
  
  return next(req);
};
```

## Authentication Implementation

### AuthService

```typescript
// core/services/auth.service.ts
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, BehaviorSubject, tap } from 'rxjs';
import { Router } from '@angular/router';
import { environment } from '../../../environments/environment';

export interface LoginRequest {
  username: string;
  password: string;
}

export interface TokenResponse {
  accessToken: string;
  refreshToken: string;
  tokenType: string;
  expiresIn: number;
}

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private readonly apiUrl = `${environment.apiUrl}/auth`;
  private readonly ACCESS_TOKEN_KEY = 'access_token';
  private readonly REFRESH_TOKEN_KEY = 'refresh_token';
  
  private currentUserSubject = new BehaviorSubject<any>(null);
  public currentUser$ = this.currentUserSubject.asObservable();
  
  constructor(
    private http: HttpClient,
    private router: Router
  ) {
    this.loadCurrentUser();
  }
  
  /**
   * Authenticates user with username and password.
   * Stores tokens in localStorage and loads user profile.
   */
  login(credentials: LoginRequest): Observable<TokenResponse> {
    return this.http.post<TokenResponse>(`${this.apiUrl}/login`, credentials)
      .pipe(
        tap(response => {
          this.storeTokens(response);
          this.loadCurrentUser();
        })
      );
  }
  
  /**
   * Logs out current user and clears stored tokens.
   */
  logout(): Observable<void> {
    return this.http.post<void>(`${this.apiUrl}/logout`, {})
      .pipe(
        tap(() => {
          this.clearTokens();
          this.currentUserSubject.next(null);
          this.router.navigate(['/login']);
        })
      );
  }
  
  /**
   * Refreshes access token using refresh token.
   */
  refreshToken(): Observable<TokenResponse> {
    const refreshToken = this.getRefreshToken();
    
    if (!refreshToken) {
      throw new Error('No refresh token available');
    }
    
    return this.http.post<TokenResponse>(`${this.apiUrl}/refresh`, { refreshToken })
      .pipe(
        tap(response => {
          this.storeTokens(response);
        })
      );
  }
  
  /**
   * Gets stored access token.
   */
  getAccessToken(): string | null {
    return localStorage.getItem(this.ACCESS_TOKEN_KEY);
  }
  
  /**
   * Gets stored refresh token.
   */
  getRefreshToken(): string | null {
    return localStorage.getItem(this.REFRESH_TOKEN_KEY);
  }
  
  /**
   * Checks if user is authenticated.
   */
  isAuthenticated(): boolean {
    return !!this.getAccessToken();
  }
  
  /**
   * Stores tokens in localStorage.
   */
  private storeTokens(response: TokenResponse): void {
    localStorage.setItem(this.ACCESS_TOKEN_KEY, response.accessToken);
    localStorage.setItem(this.REFRESH_TOKEN_KEY, response.refreshToken);
  }
  
  /**
   * Clears stored tokens.
   */
  private clearTokens(): void {
    localStorage.removeItem(this.ACCESS_TOKEN_KEY);
    localStorage.removeItem(this.REFRESH_TOKEN_KEY);
  }
  
  /**
   * Loads current user profile.
   */
  private loadCurrentUser(): void {
    if (this.isAuthenticated()) {
      // Load user profile from /api/v1/profile
      this.http.get(`${environment.apiUrl}/profile`)
        .subscribe({
          next: (user) => this.currentUserSubject.next(user),
          error: () => this.clearTokens()
        });
    }
  }
}
```

### Login Component

```typescript
// features/auth/login/login.component.ts
import { Component } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html'
})
export class LoginComponent {
  loginForm: FormGroup;
  loading = false;
  error: string | null = null;
  
  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private router: Router
  ) {
    this.loginForm = this.fb.group({
      username: ['', Validators.required],
      password: ['', Validators.required]
    });
  }
  
  onSubmit(): void {
    if (this.loginForm.valid) {
      this.loading = true;
      this.error = null;
      
      this.authService.login(this.loginForm.value)
        .subscribe({
          next: () => {
            this.router.navigate(['/dashboard']);
          },
          error: (error) => {
            this.loading = false;
            this.error = this.getErrorMessage(error);
          }
        });
    }
  }
  
  private getErrorMessage(error: any): string {
    if (error.error?.error?.type === 'ACCOUNT_LOCKED') {
      const lockUntil = error.error.error.details?.lockUntil;
      return `Account is locked until ${new Date(lockUntil).toLocaleString()}`;
    }
    
    return error.error?.error?.message || 'Login failed. Please try again.';
  }
}
```

## Angular Service Examples

### UserService

```typescript
// features/users/services/user.service.ts
import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';

export interface User {
  id: string;
  username: string;
  email: string;
  isActive: boolean;
  accountLocked: boolean;
  lockUntil: string | null;
  lastLoginAt: string | null;
  roles: Role[];
  createdAt: string;
  updatedAt: string;
  createdBy?: string;
  updatedBy?: string;
}

export interface UserRequest {
  username: string;
  email: string;
  password: string;
  roles: Role[];
}

export interface UserUpdateRequest {
  username?: string;
  email?: string;
}

export enum Role {
  ADMINISTRATOR = 'ADMINISTRATOR',
  ASSET_MANAGER = 'ASSET_MANAGER',
  VIEWER = 'VIEWER'
}

export interface PageResponse<T> {
  content: T[];
  pageable: {
    pageNumber: number;
    pageSize: number;
    sort: {
      sorted: boolean;
      unsorted: boolean;
      empty: boolean;
    };
  };
  totalElements: number;
  totalPages: number;
  last: boolean;
  first: boolean;
  size: number;
  number: number;
}

@Injectable({
  providedIn: 'root'
})
export class UserService {
  private readonly apiUrl = `${environment.apiUrl}/users`;
  
  constructor(private http: HttpClient) {}
  
  /**
   * Creates a new user.
   */
  createUser(request: UserRequest): Observable<User> {
    return this.http.post<User>(this.apiUrl, request);
  }
  
  /**
   * Retrieves all users with pagination and optional role filter.
   */
  getUsers(
    role?: Role,
    page: number = 0,
    size: number = 20,
    sort: string = 'createdAt,desc'
  ): Observable<PageResponse<User>> {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString())
      .set('sort', sort);
    
    if (role) {
      params = params.set('role', role);
    }
    
    return this.http.get<PageResponse<User>>(this.apiUrl, { params });
  }
  
  /**
   * Retrieves a specific user by ID.
   */
  getUser(id: string): Observable<User> {
    return this.http.get<User>(`${this.apiUrl}/${id}`);
  }
  
  /**
   * Updates a user.
   */
  updateUser(id: string, request: UserUpdateRequest): Observable<User> {
    return this.http.put<User>(`${this.apiUrl}/${id}`, request);
  }
  
  /**
   * Deletes a user.
   */
  deleteUser(id: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
  
  /**
   * Enables a user account.
   */
  enableUser(id: string): Observable<void> {
    return this.http.patch<void>(`${this.apiUrl}/${id}/enable`, {});
  }
  
  /**
   * Disables a user account.
   */
  disableUser(id: string): Observable<void> {
    return this.http.patch<void>(`${this.apiUrl}/${id}/disable`, {});
  }
  
  /**
   * Assigns a role to a user.
   */
  assignRole(id: string, role: Role): Observable<void> {
    return this.http.post<void>(`${this.apiUrl}/${id}/roles`, { role });
  }
  
  /**
   * Revokes a role from a user.
   */
  revokeRole(id: string, role: Role): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}/roles/${role}`);
  }
}
```

### ProfileService

```typescript
// features/profile/services/profile.service.ts
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { User } from '../../users/services/user.service';

export interface ProfileUpdateRequest {
  email: string;
}

export interface ChangePasswordRequest {
  currentPassword: string;
  newPassword: string;
}

@Injectable({
  providedIn: 'root'
})
export class ProfileService {
  private readonly apiUrl = `${environment.apiUrl}/profile`;
  
  constructor(private http: HttpClient) {}
  
  /**
   * Gets current user profile.
   */
  getProfile(): Observable<User> {
    return this.http.get<User>(this.apiUrl);
  }
  
  /**
   * Updates current user profile.
   */
  updateProfile(request: ProfileUpdateRequest): Observable<User> {
    return this.http.put<User>(this.apiUrl, request);
  }
  
  /**
   * Changes current user password.
   */
  changePassword(request: ChangePasswordRequest): Observable<void> {
    return this.http.post<void>(`${this.apiUrl}/change-password`, request);
  }
}
```

## Error Handling

### Error Interceptor

```typescript
// core/interceptors/error.interceptor.ts
import { HttpInterceptorFn, HttpErrorResponse } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, throwError } from 'rxjs';
import { Router } from '@angular/router';
import { AuthService } from '../services/auth.service';

export const errorInterceptor: HttpInterceptorFn = (req, next) => {
  const router = inject(Router);
  const authService = inject(AuthService);
  
  return next(req).pipe(
    catchError((error: HttpErrorResponse) => {
      if (error.status === 401) {
        // Unauthorized - redirect to login
        authService.logout().subscribe();
        router.navigate(['/login']);
      }
      
      return throwError(() => error);
    })
  );
};
```

### Error Service

```typescript
// core/services/error.service.ts
import { Injectable } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';

export interface ApiError {
  type: string;
  message: string;
  details?: any;
  timestamp: string;
  requestId: string;
}

@Injectable({
  providedIn: 'root'
})
export class ErrorService {
  
  /**
   * Extracts user-friendly error message from HTTP error response.
   */
  getErrorMessage(error: HttpErrorResponse): string {
    if (error.error?.error) {
      const apiError: ApiError = error.error.error;
      
      switch (apiError.type) {
        case 'VALIDATION_ERROR':
          return this.formatValidationErrors(apiError.details);
        case 'ACCOUNT_LOCKED':
          return `Account is locked until ${new Date(apiError.details.lockUntil).toLocaleString()}`;
        case 'ACCOUNT_DISABLED':
          return 'Your account has been disabled. Please contact support.';
        case 'DUPLICATE_USERNAME':
          return 'Username already exists. Please choose a different username.';
        case 'DUPLICATE_EMAIL':
          return 'Email already exists. Please use a different email address.';
        case 'INSUFFICIENT_PERMISSIONS':
          return 'You do not have permission to perform this action.';
        default:
          return apiError.message || 'An error occurred. Please try again.';
      }
    }
    
    return 'An unexpected error occurred. Please try again.';
  }
  
  /**
   * Formats validation errors into a readable message.
   */
  private formatValidationErrors(details: any[]): string {
    if (Array.isArray(details)) {
      return details.map(d => d.message).join('. ');
    }
    return 'Validation failed. Please check your input.';
  }
}
```

## Common Patterns

### User List Component

```typescript
// features/users/components/user-list/user-list.component.ts
import { Component, OnInit } from '@angular/core';
import { UserService, User, Role, PageResponse } from '../../services/user.service';
import { ErrorService } from '../../../../core/services/error.service';

@Component({
  selector: 'app-user-list',
  templateUrl: './user-list.component.html'
})
export class UserListComponent implements OnInit {
  users: User[] = [];
  totalElements = 0;
  totalPages = 0;
  currentPage = 0;
  pageSize = 20;
  loading = false;
  error: string | null = null;
  
  roleFilter: Role | undefined;
  roles = Object.values(Role);
  
  constructor(
    private userService: UserService,
    private errorService: ErrorService
  ) {}
  
  ngOnInit(): void {
    this.loadUsers();
  }
  
  loadUsers(): void {
    this.loading = true;
    this.error = null;
    
    this.userService.getUsers(this.roleFilter, this.currentPage, this.pageSize)
      .subscribe({
        next: (response: PageResponse<User>) => {
          this.users = response.content;
          this.totalElements = response.totalElements;
          this.totalPages = response.totalPages;
          this.loading = false;
        },
        error: (error) => {
          this.error = this.errorService.getErrorMessage(error);
          this.loading = false;
        }
      });
  }
  
  onPageChange(page: number): void {
    this.currentPage = page;
    this.loadUsers();
  }
  
  onRoleFilterChange(role: Role | undefined): void {
    this.roleFilter = role;
    this.currentPage = 0;
    this.loadUsers();
  }
  
  onToggleStatus(user: User): void {
    const action = user.isActive 
      ? this.userService.disableUser(user.id)
      : this.userService.enableUser(user.id);
    
    action.subscribe({
      next: () => this.loadUsers(),
      error: (error) => {
        this.error = this.errorService.getErrorMessage(error);
      }
    });
  }
  
  onDeleteUser(user: User): void {
    if (confirm(`Are you sure you want to delete user ${user.username}?`)) {
      this.userService.deleteUser(user.id)
        .subscribe({
          next: () => this.loadUsers(),
          error: (error) => {
            this.error = this.errorService.getErrorMessage(error);
          }
        });
    }
  }
}
```

### Password Change Component

```typescript
// features/profile/components/change-password/change-password.component.ts
import { Component } from '@angular/core';
import { FormBuilder, FormGroup, Validators, AbstractControl } from '@angular/forms';
import { ProfileService } from '../../services/profile.service';
import { ErrorService } from '../../../../core/services/error.service';
import { Router } from '@angular/router';

@Component({
  selector: 'app-change-password',
  templateUrl: './change-password.component.html'
})
export class ChangePasswordComponent {
  passwordForm: FormGroup;
  loading = false;
  error: string | null = null;
  success = false;
  
  constructor(
    private fb: FormBuilder,
    private profileService: ProfileService,
    private errorService: ErrorService,
    private router: Router
  ) {
    this.passwordForm = this.fb.group({
      currentPassword: ['', Validators.required],
      newPassword: ['', [
        Validators.required,
        Validators.minLength(8),
        this.passwordComplexityValidator
      ]],
      confirmPassword: ['', Validators.required]
    }, {
      validators: this.passwordMatchValidator
    });
  }
  
  onSubmit(): void {
    if (this.passwordForm.valid) {
      this.loading = true;
      this.error = null;
      
      const { currentPassword, newPassword } = this.passwordForm.value;
      
      this.profileService.changePassword({ currentPassword, newPassword })
        .subscribe({
          next: () => {
            this.success = true;
            this.loading = false;
            // Redirect to login after 2 seconds (session invalidated)
            setTimeout(() => {
              this.router.navigate(['/login']);
            }, 2000);
          },
          error: (error) => {
            this.error = this.errorService.getErrorMessage(error);
            this.loading = false;
          }
        });
    }
  }
  
  private passwordComplexityValidator(control: AbstractControl): { [key: string]: any } | null {
    const value = control.value;
    
    if (!value) {
      return null;
    }
    
    const hasUpperCase = /[A-Z]/.test(value);
    const hasLowerCase = /[a-z]/.test(value);
    const hasDigit = /\d/.test(value);
    const hasSpecialChar = /[@$!%*?&]/.test(value);
    
    const valid = hasUpperCase && hasLowerCase && hasDigit && hasSpecialChar;
    
    return valid ? null : { passwordComplexity: true };
  }
  
  private passwordMatchValidator(group: AbstractControl): { [key: string]: any } | null {
    const newPassword = group.get('newPassword')?.value;
    const confirmPassword = group.get('confirmPassword')?.value;
    
    return newPassword === confirmPassword ? null : { passwordMismatch: true };
  }
}
```

## TypeScript Interfaces

### Complete Interface Definitions

```typescript
// shared/models/user.model.ts

export enum Role {
  ADMINISTRATOR = 'ADMINISTRATOR',
  ASSET_MANAGER = 'ASSET_MANAGER',
  VIEWER = 'VIEWER'
}

export interface User {
  id: string;
  username: string;
  email: string;
  isActive: boolean;
  accountLocked: boolean;
  lockUntil: string | null;
  lastLoginAt: string | null;
  roles: Role[];
  createdAt: string;
  updatedAt: string;
  createdBy?: string;
  updatedBy?: string;
}

export interface UserRequest {
  username: string;
  email: string;
  password: string;
  roles: Role[];
}

export interface UserUpdateRequest {
  username?: string;
  email?: string;
}

export interface ProfileUpdateRequest {
  email: string;
}

export interface ChangePasswordRequest {
  currentPassword: string;
  newPassword: string;
}

export interface LoginRequest {
  username: string;
  password: string;
}

export interface TokenResponse {
  accessToken: string;
  refreshToken: string;
  tokenType: string;
  expiresIn: number;
}

export interface RefreshTokenRequest {
  refreshToken: string;
}

export interface RoleAssignmentRequest {
  role: Role;
}

export interface PageResponse<T> {
  content: T[];
  pageable: {
    pageNumber: number;
    pageSize: number;
    sort: {
      sorted: boolean;
      unsorted: boolean;
      empty: boolean;
    };
  };
  totalElements: number;
  totalPages: number;
  last: boolean;
  first: boolean;
  size: number;
  number: number;
}

export interface ApiError {
  type: string;
  message: string;
  details?: any;
  timestamp: string;
  requestId: string;
}

export interface ErrorResponse {
  error: ApiError;
}
```

## Best Practices

### 1. Token Management

```typescript
// Use secure storage for tokens
// For web apps, consider HttpOnly cookies instead of localStorage
// For mobile apps, use secure storage mechanisms

// Implement automatic token refresh
export class TokenRefreshService {
  constructor(private authService: AuthService) {
    // Refresh token 5 minutes before expiration
    this.scheduleTokenRefresh();
  }
  
  private scheduleTokenRefresh(): void {
    const expiresIn = 1800; // 30 minutes
    const refreshTime = (expiresIn - 300) * 1000; // 25 minutes
    
    setTimeout(() => {
      this.authService.refreshToken().subscribe({
        next: () => this.scheduleTokenRefresh(),
        error: () => this.authService.logout()
      });
    }, refreshTime);
  }
}
```

### 2. Error Handling

```typescript
// Always handle errors gracefully
this.userService.createUser(request)
  .subscribe({
    next: (user) => {
      // Success handling
      this.showSuccess('User created successfully');
    },
    error: (error) => {
      // Error handling
      const message = this.errorService.getErrorMessage(error);
      this.showError(message);
    }
  });
```

### 3. Loading States

```typescript
// Always show loading indicators
loading = false;

loadData(): void {
  this.loading = true;
  
  this.service.getData()
    .pipe(finalize(() => this.loading = false))
    .subscribe({
      next: (data) => this.data = data,
      error: (error) => this.error = error
    });
}
```

### 4. Form Validation

```typescript
// Implement comprehensive client-side validation
this.userForm = this.fb.group({
  username: ['', [
    Validators.required,
    Validators.minLength(3),
    Validators.maxLength(100),
    Validators.pattern(/^[a-zA-Z0-9_]+$/)
  ]],
  email: ['', [
    Validators.required,
    Validators.email,
    Validators.minLength(5),
    Validators.maxLength(255)
  ]],
  password: ['', [
    Validators.required,
    Validators.minLength(8),
    this.passwordComplexityValidator
  ]]
});
```

### 5. Unsubscribe from Observables

```typescript
// Use takeUntil pattern to prevent memory leaks
private destroy$ = new Subject<void>();

ngOnInit(): void {
  this.service.getData()
    .pipe(takeUntil(this.destroy$))
    .subscribe(data => this.data = data);
}

ngOnDestroy(): void {
  this.destroy$.next();
  this.destroy$.complete();
}
```

### 6. Route Guards

```typescript
// Implement authentication guard
export const authGuard: CanActivateFn = (route, state) => {
  const authService = inject(AuthService);
  const router = inject(Router);
  
  if (authService.isAuthenticated()) {
    return true;
  }
  
  router.navigate(['/login'], {
    queryParams: { returnUrl: state.url }
  });
  return false;
};

// Implement role guard
export const roleGuard = (allowedRoles: Role[]): CanActivateFn => {
  return (route, state) => {
    const authService = inject(AuthService);
    const router = inject(Router);
    
    const user = authService.currentUserSubject.value;
    
    if (user && user.roles.some((role: Role) => allowedRoles.includes(role))) {
      return true;
    }
    
    router.navigate(['/unauthorized']);
    return false;
  };
};
```

### 7. Environment-Specific Configuration

```typescript
// environment.development.ts
export const environment = {
  production: false,
  apiUrl: 'http://localhost:8080/api/v1',
  enableDebugLogging: true
};

// environment.production.ts
export const environment = {
  production: true,
  apiUrl: 'https://api.example.com/api/v1',
  enableDebugLogging: false
};
```

---

## Quick Reference

### Common API Calls

```typescript
// Login
authService.login({ username: 'admin', password: 'Admin@123456' })

// Get all users
userService.getUsers()

// Get users by role
userService.getUsers(Role.ADMINISTRATOR)

// Create user
userService.createUser({
  username: 'jdoe',
  email: 'jdoe@example.com',
  password: 'SecurePass123!',
  roles: [Role.ASSET_MANAGER]
})

// Update user
userService.updateUser(userId, { email: 'newemail@example.com' })

// Delete user
userService.deleteUser(userId)

// Enable/Disable user
userService.enableUser(userId)
userService.disableUser(userId)

// Assign/Revoke role
userService.assignRole(userId, Role.ADMINISTRATOR)
userService.revokeRole(userId, Role.VIEWER)

// Get profile
profileService.getProfile()

// Update profile
profileService.updateProfile({ email: 'newemail@example.com' })

// Change password
profileService.changePassword({
  currentPassword: 'OldPass123!',
  newPassword: 'NewSecurePass456!'
})
```

---

## Support

For additional support or questions:
- **API Documentation**: http://localhost:8080/swagger-ui.html
- **Support Email**: support@example.com

---

**Last Updated**: 2024-01-15  
**Guide Version**: 1.0.0
