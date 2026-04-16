import { Component, Input, Output, EventEmitter, OnInit, OnDestroy, ChangeDetectionStrategy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AuthError } from '../../../core/models/error.model';

/**
 * Error Message Component
 * 
 * Displays authentication error messages with appropriate styling and dismissal options.
 * Implements auto-dismiss after 10 seconds and manual dismiss via X button.
 * Maps error types to user-friendly messages.
 * 
 * Requirements: 4.1, 4.2, 4.3, 4.4, 4.5
 */
@Component({
  selector: 'app-error-message',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './error-message.component.html',
  styleUrls: ['./error-message.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class ErrorMessageComponent implements OnInit, OnDestroy {
  @Input() error: AuthError | null = null;
  @Output() dismiss = new EventEmitter<void>();

  private autoDismissTimer: any;

  /**
   * Get user-friendly error message based on error type
   * Requirements: 4.1, 4.2, 4.3
   */
  get displayMessage(): string {
    if (!this.error) {
      return '';
    }

    // Map error types to user-friendly messages
    switch (this.error.type) {
      case 'invalid_credentials':
        // Requirement 4.1 - Invalid credentials message
        return 'Invalid username or password. Please try again.';
      
      case 'account_locked':
        // Requirement 4.2 - Account lockout message
        return 'Account locked due to multiple failed attempts. Please try again in 15 minutes.';
      
      case 'network_error':
        // Requirement 4.3 - Network error message
        return 'Unable to connect to server. Please check your connection and try again.';
      
      case 'unknown':
      default:
        // Fallback for unknown errors - use the provided message or a generic one
        return this.error.message || 'An unexpected error occurred. Please try again.';
    }
  }

  ngOnInit(): void {
    // Requirement 4.5 - Auto-dismiss after 10 seconds
    if (this.error) {
      this.startAutoDismissTimer();
    }
  }

  ngOnDestroy(): void {
    this.clearAutoDismissTimer();
  }

  /**
   * Start the auto-dismiss timer
   * Requirement 4.5 - Automatically dismiss after 10 seconds
   */
  private startAutoDismissTimer(): void {
    this.autoDismissTimer = setTimeout(() => {
      this.onDismiss();
    }, 10000); // 10 seconds
  }

  /**
   * Clear the auto-dismiss timer
   */
  private clearAutoDismissTimer(): void {
    if (this.autoDismissTimer) {
      clearTimeout(this.autoDismissTimer);
      this.autoDismissTimer = null;
    }
  }

  /**
   * Handle dismiss button click
   * Requirement 4.5 - Dismissible by clicking X icon
   */
  onDismiss(): void {
    this.clearAutoDismissTimer();
    this.dismiss.emit();
  }
}
