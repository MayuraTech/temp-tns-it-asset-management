import { Injectable } from '@angular/core';
import { AuthError, AuthErrorType } from '../models/error.model';

/**
 * Error Message Service
 * 
 * Maps error types to user-friendly messages.
 * Provides centralized error message handling for authentication errors.
 * 
 * Requirements: 4.1, 4.2, 4.3
 */
@Injectable({
  providedIn: 'root'
})
export class ErrorMessageService {
  
  /**
   * Map error type to user-friendly message
   * Requirements: 4.1, 4.2, 4.3
   */
  getErrorMessage(errorType: AuthErrorType): string {
    const errorMessages: Record<AuthErrorType, string> = {
      // Requirement 4.1 - Invalid credentials message
      'invalid_credentials': 'Invalid username or password. Please try again.',
      
      // Requirement 4.2 - Account lockout message
      'account_locked': 'Account locked due to multiple failed attempts. Please try again in 15 minutes.',
      
      // Requirement 4.3 - Network error message
      'network_error': 'Unable to connect to server. Please check your connection and try again.',
      
      // Default unknown error message
      'unknown': 'An unexpected error occurred. Please try again later.'
    };

    return errorMessages[errorType] || errorMessages['unknown'];
  }

  /**
   * Create an AuthError object from error type
   */
  createAuthError(errorType: AuthErrorType): AuthError {
    return {
      type: errorType,
      message: this.getErrorMessage(errorType),
      timestamp: new Date()
    };
  }

  /**
   * Parse error response and create AuthError
   * Handles various error response formats from the backend
   */
  parseErrorResponse(error: any): AuthError {
    // Check for specific error types from backend
    if (error?.error?.type) {
      const errorType = this.mapBackendErrorType(error.error.type);
      return this.createAuthError(errorType);
    }

    // Check HTTP status codes
    if (error?.status === 401) {
      return this.createAuthError('invalid_credentials');
    }

    if (error?.status === 423) {
      return this.createAuthError('account_locked');
    }

    if (error?.status === 0 || error?.status === 504) {
      return this.createAuthError('network_error');
    }

    // Default to unknown error
    return this.createAuthError('unknown');
  }

  /**
   * Map backend error type strings to AuthErrorType
   */
  private mapBackendErrorType(backendType: string): AuthErrorType {
    const typeMap: Record<string, AuthErrorType> = {
      'INVALID_CREDENTIALS': 'invalid_credentials',
      'UNAUTHORIZED': 'invalid_credentials',
      'ACCOUNT_LOCKED': 'account_locked',
      'LOCKED': 'account_locked',
      'NETWORK_ERROR': 'network_error',
      'CONNECTION_ERROR': 'network_error',
      'TIMEOUT': 'network_error'
    };

    return typeMap[backendType.toUpperCase()] || 'unknown';
  }
}
