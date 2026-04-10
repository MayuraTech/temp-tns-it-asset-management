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
