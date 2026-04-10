/**
 * Production Environment Configuration
 * 
 * This file contains configuration for the production environment.
 * It uses relative URLs to connect to the backend API on the same domain.
 */
import { Environment } from '../app/core/models/environment.model';

export const environment: Environment = {
  production: true,
  
  // API Configuration
  apiUrl: '/api/v1',
  apiTimeout: 30000, // 30 seconds
  
  // Authentication Configuration
  auth: {
    tokenKey: 'auth_token',
    refreshTokenKey: 'refresh_token',
    tokenExpirationKey: 'token_expiration',
    userKey: 'current_user',
    sessionTimeout: 1800000, // 30 minutes (matches backend)
    refreshTokenExpiration: 86400000, // 24 hours (matches backend)
    loginUrl: '/auth/login',
    logoutUrl: '/auth/logout',
    refreshUrl: '/auth/refresh',
    changePasswordUrl: '/auth/change-password'
  },
  
  // Feature Flags
  features: {
    enableDebugMode: false,
    enableConsoleLogging: false,
    enablePerformanceMonitoring: true,
    enableMockData: false
  },
  
  // Pagination Configuration
  pagination: {
    defaultPageSize: 20,
    pageSizeOptions: [10, 20, 50, 100],
    maxPageSize: 100
  },
  
  // File Upload Configuration
  fileUpload: {
    maxFileSize: 10485760, // 10MB (matches backend)
    maxRequestSize: 10485760, // 10MB
    allowedFormats: ['csv', 'json'],
    allowedMimeTypes: ['text/csv', 'application/json']
  },
  
  // UI Configuration
  ui: {
    defaultTheme: 'light',
    enableAnimations: true,
    toastDuration: 3000, // 3 seconds
    debounceTime: 300, // 300ms for search inputs
    autoRefreshInterval: 60000 // 1 minute for dashboard
  },
  
  // Validation Configuration
  validation: {
    passwordMinLength: 12,
    passwordRequireUppercase: true,
    passwordRequireLowercase: true,
    passwordRequireNumbers: true,
    passwordRequireSpecialChars: true,
    serialNumberMinLength: 5,
    serialNumberMaxLength: 100,
    nameMaxLength: 255,
    notesMaxLength: 4000
  },
  
  // Error Handling Configuration
  errorHandling: {
    showDetailedErrors: false,
    retryAttempts: 3,
    retryDelay: 1000 // 1 second
  }
};
