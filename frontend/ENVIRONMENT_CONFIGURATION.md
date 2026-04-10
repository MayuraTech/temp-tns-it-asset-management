# Frontend Environment Configuration

This document describes the environment configuration for the IT Infrastructure Asset Management frontend application.

## Overview

The application uses Angular's environment configuration system to manage settings across different deployment environments (development, production, etc.). All environment configurations are type-safe using TypeScript interfaces.

## Environment Files

### Location
- Development: `src/environments/environment.ts`
- Production: `src/environments/environment.prod.ts`

### Type Definitions
- Interface: `src/app/core/models/environment.model.ts`

## Configuration Structure

### API Configuration

```typescript
apiUrl: string;        // Base URL for backend API
apiTimeout: number;    // Request timeout in milliseconds (30000 = 30 seconds)
```

**Development:**
- `apiUrl`: `http://localhost:8080/api/v1` (connects to local backend)

**Production:**
- `apiUrl`: `/api/v1` (relative URL, assumes same domain)

### Authentication Configuration

```typescript
auth: {
  tokenKey: string;                  // LocalStorage key for access token
  refreshTokenKey: string;           // LocalStorage key for refresh token
  tokenExpirationKey: string;        // LocalStorage key for token expiration
  userKey: string;                   // LocalStorage key for user data
  sessionTimeout: number;            // Session timeout in ms (1800000 = 30 min)
  refreshTokenExpiration: number;    // Refresh token expiration (86400000 = 24 hours)
  loginUrl: string;                  // Login endpoint path
  logoutUrl: string;                 // Logout endpoint path
  refreshUrl: string;                // Token refresh endpoint path
  changePasswordUrl: string;         // Change password endpoint path
}
```

**Key Points:**
- Session timeout matches backend configuration (30 minutes)
- Refresh token expiration matches backend (24 hours)
- All authentication endpoints are relative to `apiUrl`

### Feature Flags

```typescript
features: {
  enableDebugMode: boolean;           // Enable debug logging and tools
  enableConsoleLogging: boolean;      // Enable console.log statements
  enablePerformanceMonitoring: boolean; // Enable performance tracking
  enableMockData: boolean;            // Use mock data instead of API calls
}
```

**Development:**
- Debug mode and console logging enabled
- Performance monitoring disabled (to avoid overhead)

**Production:**
- Debug mode and console logging disabled
- Performance monitoring enabled

### Pagination Configuration

```typescript
pagination: {
  defaultPageSize: number;      // Default items per page (20)
  pageSizeOptions: number[];    // Available page size options [10, 20, 50, 100]
  maxPageSize: number;          // Maximum allowed page size (100)
}
```

**Matches backend configuration:**
- Default page size: 20
- Maximum page size: 100

### File Upload Configuration

```typescript
fileUpload: {
  maxFileSize: number;          // Maximum file size in bytes (10485760 = 10MB)
  maxRequestSize: number;       // Maximum request size in bytes (10485760 = 10MB)
  allowedFormats: string[];     // Allowed file extensions ['csv', 'json']
  allowedMimeTypes: string[];   // Allowed MIME types
}
```

**Matches backend configuration:**
- Maximum file size: 10MB
- Supported formats: CSV and JSON

### UI Configuration

```typescript
ui: {
  defaultTheme: 'light' | 'dark';  // Default theme
  enableAnimations: boolean;        // Enable UI animations
  toastDuration: number;            // Toast notification duration (3000 = 3 sec)
  debounceTime: number;             // Input debounce time (300 = 300ms)
  autoRefreshInterval: number;      // Auto-refresh interval (60000 = 1 min)
}
```

**Usage:**
- `toastDuration`: How long success/error messages display
- `debounceTime`: Delay before triggering search on user input
- `autoRefreshInterval`: How often dashboard data refreshes

### Validation Configuration

```typescript
validation: {
  passwordMinLength: number;           // Minimum password length (12)
  passwordRequireUppercase: boolean;   // Require uppercase letters
  passwordRequireLowercase: boolean;   // Require lowercase letters
  passwordRequireNumbers: boolean;     // Require numbers
  passwordRequireSpecialChars: boolean; // Require special characters
  serialNumberMinLength: number;       // Minimum serial number length (5)
  serialNumberMaxLength: number;       // Maximum serial number length (100)
  nameMaxLength: number;               // Maximum name length (255)
  notesMaxLength: number;              // Maximum notes length (4000)
}
```

**Matches backend validation rules:**
- Password complexity requirements (Requirement 1.3)
- Serial number constraints (Requirement 3.2)
- Field length limits

### Error Handling Configuration

```typescript
errorHandling: {
  showDetailedErrors: boolean;  // Show detailed error messages
  retryAttempts: number;        // Number of retry attempts (3)
  retryDelay: number;           // Delay between retries in ms (1000 = 1 sec)
}
```

**Development:**
- Detailed errors shown for debugging

**Production:**
- Detailed errors hidden for security

## Usage in Application

### Importing Environment

```typescript
import { environment } from '../environments/environment';

// Access configuration
const apiUrl = environment.apiUrl;
const sessionTimeout = environment.auth.sessionTimeout;
```

### Type Safety

All environment objects are typed with the `Environment` interface:

```typescript
import { Environment } from './app/core/models/environment.model';

export const environment: Environment = {
  // Configuration must match interface
};
```

### Example: Using in Services

```typescript
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class AssetService {
  private apiUrl = environment.apiUrl;
  
  constructor(private http: HttpClient) {}
  
  getAssets() {
    return this.http.get(`${this.apiUrl}/assets`, {
      timeout: environment.apiTimeout
    });
  }
}
```

### Example: Using in Components

```typescript
import { Component } from '@angular/core';
import { environment } from '../../environments/environment';

@Component({
  selector: 'app-dashboard',
  templateUrl: './dashboard.component.html'
})
export class DashboardComponent {
  private refreshInterval = environment.ui.autoRefreshInterval;
  
  ngOnInit() {
    // Refresh dashboard every minute
    setInterval(() => this.loadData(), this.refreshInterval);
  }
}
```

## Environment-Specific Behavior

### Development Environment
- Connects to `http://localhost:8080`
- Shows detailed error messages
- Enables debug logging
- Displays SQL queries in backend logs

### Production Environment
- Uses relative URLs (same domain as frontend)
- Hides detailed error messages
- Disables debug logging
- Enables performance monitoring
- Requires HTTPS
- Enables security headers

## Configuration Best Practices

1. **Never commit secrets**: Use environment variables for sensitive data
2. **Match backend settings**: Keep timeouts and limits synchronized
3. **Use type safety**: Always use the `Environment` interface
4. **Document changes**: Update this file when adding new configuration
5. **Test both environments**: Verify settings work in dev and prod

## Deployment Considerations

### Development Deployment
```bash
ng serve
# Uses environment.ts by default
```

### Production Build
```bash
ng build --configuration production
# Uses environment.prod.ts
```

### Docker Deployment
The production build is served by Nginx with the following configuration:
- API requests proxied to backend
- Static files served with caching
- HTTPS enforced
- Security headers applied

## Troubleshooting

### CORS Issues in Development
If you encounter CORS errors:
1. Verify backend `cors.allowed-origins` includes `http://localhost:4200`
2. Check `application-dev.properties` in backend

### API Connection Issues
If frontend cannot connect to backend:
1. Verify backend is running on port 8080
2. Check `apiUrl` in `environment.ts`
3. Verify no firewall blocking localhost connections

### Authentication Issues
If authentication fails:
1. Verify JWT secret is configured in backend
2. Check token expiration times match between frontend and backend
3. Verify localStorage keys are correct

## Related Documentation

- Backend Configuration: `backend/src/main/resources/APPLICATION_PROPERTIES_README.md`
- API Design: `.kiro/steering/it-asset-management-api-design.md`
- Security Implementation: `backend/SECURITY_IMPLEMENTATION.md`
- Deployment Guide: `.kiro/steering/it-asset-management-deployment.md`
