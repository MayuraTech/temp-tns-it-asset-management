/**
 * Error response models
 */

export interface ErrorResponse {
  error: {
    type: string;
    message: string;
    details?: any;
    timestamp: string;
    requestId?: string;
  };
}

export interface ValidationError {
  field: string;
  message: string;
  value?: any;
}

/**
 * Authentication error types
 */
export type AuthErrorType = 'invalid_credentials' | 'account_locked' | 'network_error' | 'unknown';

/**
 * Authentication error model
 */
export interface AuthError {
  type: AuthErrorType;
  message: string;
  timestamp: Date;
}

