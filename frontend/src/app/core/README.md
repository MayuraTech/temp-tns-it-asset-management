# Core Module

This module contains singleton services, guards, and interceptors that are used throughout the application.

## Services

### AuthService

Handles user authentication, session management, and token operations.

**Key Features:**
- User login/logout
- JWT token management
- Automatic token refresh
- Session persistence
- Current user state management

**Usage:**
```typescript
constructor(private authService: AuthService) {}

login() {
  this.authService.login(username, password).subscribe({
    next: (response) => {
      // Login successful
      this.router.navigate(['/dashboard']);
    },
    error: (error) => {
      // Handle login error
    }
  });
}

logout() {
  this.authService.logout().subscribe(() => {
    this.router.navigate(['/login']);
  });
}

// Check authentication status
if (this.authService.isAuthenticated) {
  // User is authenticated
}

// Get current user
const user = this.authService.currentUserValue;
```

### LoadingService

Manages global loading state for HTTP requests.

**Key Features:**
- Automatic loading indicator management
- Request count tracking
- Observable loading state

**Usage:**
```typescript
constructor(private loadingService: LoadingService) {}

ngOnInit() {
  this.loadingService.loading$.subscribe(isLoading => {
    // Update UI based on loading state
  });
}
```

## Guards

### AuthGuard

Protects routes that require authentication.

**Usage:**
```typescript
// In app.routes.ts
{
  path: 'assets',
  component: AssetListComponent,
  canActivate: [authGuard]
}
```

### RoleGuard

Protects routes based on user roles.

**Usage:**
```typescript
// In app.routes.ts
import { Role } from './core/models/auth.model';

{
  path: 'admin',
  component: AdminComponent,
  canActivate: [roleGuard],
  data: { roles: [Role.ADMINISTRATOR] }
}

{
  path: 'assets/create',
  component: AssetCreateComponent,
  canActivate: [roleGuard],
  data: { roles: [Role.ADMINISTRATOR, Role.ASSET_MANAGER] }
}
```

## Interceptors

### JwtInterceptor

Automatically adds JWT token to outgoing HTTP requests.

**Features:**
- Adds Authorization header with Bearer token
- Skips authentication endpoints
- Automatically applied to all HTTP requests

### ErrorInterceptor

Handles HTTP errors globally.

**Features:**
- Centralized error handling
- Automatic token refresh on 401 errors
- User-friendly error messages
- Automatic navigation on specific errors (403, 401)

### LoadingInterceptor

Manages loading state for HTTP requests.

**Features:**
- Automatically shows/hides loading indicator
- Tracks concurrent requests
- Can be skipped for specific requests using `X-Skip-Loading` header

**Skip loading for specific request:**
```typescript
this.http.get('/api/v1/assets', {
  headers: { 'X-Skip-Loading': 'true' }
})
```

## Models

### Auth Models

- `LoginRequest`: Login credentials
- `LoginResponse`: Authentication response with tokens
- `User`: User information and roles
- `Role`: User role enumeration
- `Action`: Permission action enumeration

### Error Models

- `ErrorResponse`: Standardized error response
- `ValidationError`: Validation error details

## Configuration

The interceptors are registered in `app.config.ts` in the following order:

1. **JwtInterceptor** - Adds authentication token
2. **LoadingInterceptor** - Manages loading state
3. **ErrorInterceptor** - Handles errors

This order ensures that:
- Requests are authenticated before being sent
- Loading state is tracked for all requests
- Errors are handled consistently

## Best Practices

1. **Authentication State**: Always check `authService.isAuthenticated` before accessing protected resources
2. **Current User**: Subscribe to `authService.currentUser$` for reactive user state updates
3. **Error Handling**: Let the ErrorInterceptor handle common errors, only handle specific errors in components
4. **Loading State**: Use LoadingService for global loading, component-level loading for specific operations
5. **Route Protection**: Always use guards for protected routes, never rely on UI-only protection
6. **Token Management**: The AuthService handles token refresh automatically, no manual intervention needed

## Security Considerations

1. **Token Storage**: Tokens are stored in localStorage (consider HttpOnly cookies for production)
2. **Token Expiration**: Tokens are automatically refreshed 1 minute before expiration
3. **Session Cleanup**: Session is cleared on logout or authentication failure
4. **Authorization**: Always verify permissions on the backend, frontend guards are for UX only
