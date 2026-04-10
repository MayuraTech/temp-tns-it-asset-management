/**
 * Environment Configuration Interface
 * 
 * Defines the structure of environment configuration objects
 * to ensure type safety across the application.
 */

export interface Environment {
  production: boolean;
  apiUrl: string;
  apiTimeout: number;
  auth: AuthConfig;
  features: FeatureFlags;
  pagination: PaginationConfig;
  fileUpload: FileUploadConfig;
  ui: UIConfig;
  validation: ValidationConfig;
  errorHandling: ErrorHandlingConfig;
}

export interface AuthConfig {
  tokenKey: string;
  refreshTokenKey: string;
  tokenExpirationKey: string;
  userKey: string;
  sessionTimeout: number;
  refreshTokenExpiration: number;
  loginUrl: string;
  logoutUrl: string;
  refreshUrl: string;
  changePasswordUrl: string;
}

export interface FeatureFlags {
  enableDebugMode: boolean;
  enableConsoleLogging: boolean;
  enablePerformanceMonitoring: boolean;
  enableMockData: boolean;
}

export interface PaginationConfig {
  defaultPageSize: number;
  pageSizeOptions: number[];
  maxPageSize: number;
}

export interface FileUploadConfig {
  maxFileSize: number;
  maxRequestSize: number;
  allowedFormats: string[];
  allowedMimeTypes: string[];
}

export interface UIConfig {
  defaultTheme: 'light' | 'dark';
  enableAnimations: boolean;
  toastDuration: number;
  debounceTime: number;
  autoRefreshInterval: number;
}

export interface ValidationConfig {
  passwordMinLength: number;
  passwordRequireUppercase: boolean;
  passwordRequireLowercase: boolean;
  passwordRequireNumbers: boolean;
  passwordRequireSpecialChars: boolean;
  serialNumberMinLength: number;
  serialNumberMaxLength: number;
  nameMaxLength: number;
  notesMaxLength: number;
}

export interface ErrorHandlingConfig {
  showDetailedErrors: boolean;
  retryAttempts: number;
  retryDelay: number;
}
