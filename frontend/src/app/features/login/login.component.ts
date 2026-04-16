import { Component, ChangeDetectionStrategy, ChangeDetectorRef, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, ActivatedRoute } from '@angular/router';
import { CredentialInputComponent } from '../../shared/components/credential-input/credential-input.component';
import { GeometricTriangleComponent } from '../../shared/components/geometric-triangle/geometric-triangle.component';
import { ErrorMessageComponent } from '../../shared/components/error-message/error-message.component';
import { ValidationService } from '../../core/services/validation.service';
import { AuthService } from '../../core/services/auth.service';
import { AuthError } from '../../core/models/error.model';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';

/**
 * Login Component
 * 
 * Main login component that orchestrates the authentication flow.
 * Composes all child components (form inputs, error messages, geometric accents).
 * Implements state management for credentials, validation, loading, and errors.
 * Coordinates authentication flow with AuthService and navigation.
 * Applies Editorial Geometry background and layout.
 * 
 * Requirements: 1.2, 1.5, 1.1, 1.4, 2.1, 2.4, 3.1, 3.2, 3.3, 5.4, 6.1, 6.2, 6.3, 6.4, 8.1, 8.2, 8.3, 8.4, 9.5, 11.3
 */
@Component({
  selector: 'app-login',
  standalone: true,
  imports: [
    CommonModule, 
    CredentialInputComponent, 
    GeometricTriangleComponent,
    ErrorMessageComponent
  ],
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class LoginComponent implements OnInit, OnDestroy {
  // Subscription management for cleanup
  private destroy$ = new Subject<void>();
  
  // Return URL for redirect after login
  private returnUrl: string = '/dashboard';
  
  // Form state
  username: string = '';
  password: string = '';
  showPassword: boolean = false;
  rememberMe: boolean = false;
  isLoading: boolean = false;
  
  // Validation errors
  usernameError: string | null = null;
  passwordError: string | null = null;
  
  // Authentication error
  authError: AuthError | null = null;
  
  // Track if fields have been touched (blurred)
  usernameTouched: boolean = false;
  passwordTouched: boolean = false;

  constructor(
    private validationService: ValidationService,
    private authService: AuthService,
    private router: Router,
    private route: ActivatedRoute,
    private cdr: ChangeDetectorRef
  ) {}

  /**
   * Initialize component and check for automatic authentication
   * 
   * Requirements:
   * - 3.3: Implement redirect after login
   * - 6.1: Auto-focus username field on init (handled in template with autofocus attribute)
   * - 11.3: Check for valid persistent token on load
   * - 11.3: Auto-authenticate and redirect if token valid
   * - 11.4: Require re-authentication if token expired
   */
  ngOnInit(): void {
    // Get return URL from query params (Requirement 3.3)
    this.returnUrl = this.route.snapshot.queryParams['returnUrl'] || '/dashboard';
    
    // Check if user can be automatically authenticated
    this.authService.canAutoAuthenticate()
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (canAutoAuth) => {
          if (canAutoAuth) {
            // Valid token found, redirect to return URL or dashboard
            this.router.navigateByUrl(this.returnUrl);
          }
          // If canAutoAuth is false, user stays on login page
        },
        error: (error) => {
          // Auto-authentication failed, user needs to login manually
          console.log('Auto-authentication not available');
        }
      });
  }

  /**
   * Component cleanup
   * 
   * Requirements:
   * - 5.4: Reset password visibility on reload
   * - Clean up subscriptions to prevent memory leaks
   */
  ngOnDestroy(): void {
    // Reset password visibility state
    this.showPassword = false;
    
    // Complete all subscriptions
    this.destroy$.next();
    this.destroy$.complete();
  }

  /**
   * Handle username input change
   * Requirement 2.4 - Clear errors when valid input is provided
   */
  onUsernameChange(value: string): void {
    this.username = value;
    
    // Clear error if field has been touched and now has valid input
    if (this.usernameTouched) {
      this.validateUsername();
    }
  }

  /**
   * Handle password input change
   * Requirement 2.4 - Clear errors when valid input is provided
   */
  onPasswordChange(value: string): void {
    this.password = value;
    
    // Clear error if field has been touched and now has valid input
    if (this.passwordTouched) {
      this.validatePassword();
    }
  }

  /**
   * Handle username field blur
   * Requirement 2.1 - Display validation error when field loses focus with empty content
   */
  onUsernameBlur(): void {
    this.usernameTouched = true;
    this.validateUsername();
  }

  /**
   * Handle password field blur
   * Requirement 2.1 - Display validation error when field loses focus with empty content
   */
  onPasswordBlur(): void {
    this.passwordTouched = true;
    this.validatePassword();
  }

  /**
   * Validate username field
   * Requirement 2.2 - Display "Username is required" when empty
   */
  private validateUsername(): void {
    this.usernameError = this.validationService.validateUsername(this.username);
  }

  /**
   * Validate password field
   * Requirement 2.3 - Display "Password is required" when empty
   */
  private validatePassword(): void {
    this.passwordError = this.validationService.validatePassword(this.password);
  }

  /**
   * Handle password visibility toggle
   */
  onPasswordVisibilityToggle(): void {
    this.showPassword = !this.showPassword;
  }

  /**
   * Handle remember me checkbox change
   * Requirement 11.1 - Display "Remember me" checkbox
   */
  onRememberMeChange(event: Event): void {
    const target = event.target as HTMLInputElement;
    this.rememberMe = target.checked;
  }

  /**
   * Handle forgot password link click
   * 
   * Requirements:
   * - 12.2: Navigate to password reset route on click
   * - 12.4: Ensure keyboard accessibility
   */
  onForgotPasswordClick(): void {
    this.router.navigate(['/password-reset']);
  }

  /**
   * Handle Enter key press in input fields
   * Requirement 6.4 - Submit form on Enter key press
   */
  onInputKeyPress(event: KeyboardEvent): void {
    if (event.key === 'Enter' && this.isFormValid() && !this.isLoading) {
      event.preventDefault();
      this.onSubmit();
    }
  }

  /**
   * Check if form is valid
   * Requirement 2.5 - Submit button should be disabled when fields are empty
   */
  isFormValid(): boolean {
    return this.validationService.isFormValid(this.username, this.password);
  }

  /**
   * Check if submit button should be disabled
   * Requirements: 2.5, 3.2, 8.1
   */
  isSubmitDisabled(): boolean {
    return !this.isFormValid() || this.isLoading;
  }

  /**
   * Handle form submission
   * 
   * Requirements:
   * - 1.3: Submit authentication credentials
   * - 3.1: Send authentication request to Authentication_Service
   * - 3.2: Display loading indicator during authentication
   * - 3.3: Store Session_Token and navigate to dashboard on success
   * - 3.4: Display error message on failure
   * - 3.5: Clear password field after failed authentication
   * - 8.1: Disable form inputs during authentication
   * - 8.2: Show loading indicator on submit button
   * - 8.3: Prevent form resubmission
   * - 8.4: Restore form state after completion
   * - 11.2: Store persistent Session_Token when Remember Me is checked
   */
  onSubmit(): void {
    // Mark all fields as touched
    this.usernameTouched = true;
    this.passwordTouched = true;
    
    // Validate all fields
    this.validateUsername();
    this.validatePassword();
    
    // Only proceed if form is valid
    if (this.isFormValid() && !this.isLoading) {
      // Clear any previous authentication errors
      this.authError = null;
      
      // Set loading state (Requirements 3.2, 8.1, 8.2, 8.3)
      this.isLoading = true;
      this.cdr.markForCheck();
      
      // Send authentication request (Requirement 3.1)
      this.authService.login(this.username, this.password, this.rememberMe)
        .pipe(takeUntil(this.destroy$))
        .subscribe({
          next: (response) => {
            // Authentication successful (Requirement 3.3)
            // Clear sensitive data from memory (Requirement 10.4)
            const tempPassword = this.password;
            this.password = '';
            
            // Navigate to return URL or dashboard (Requirement 3.3)
            this.router.navigateByUrl(this.returnUrl);
            
            // Restore form state (Requirement 8.4)
            this.isLoading = false;
            this.cdr.markForCheck();
          },
          error: (error: AuthError) => {
            // Authentication failed (Requirements 3.4, 3.5)
            this.authError = error;
            
            // Clear password field (Requirement 3.5)
            this.password = '';
            
            // Restore form state (Requirement 8.4)
            this.isLoading = false;
            this.cdr.markForCheck();
          }
        });
    }
  }

  /**
   * Dismiss authentication error
   * Requirement 4.5 - Error should be dismissible
   */
  onErrorDismiss(): void {
    this.authError = null;
    this.cdr.markForCheck();
  }
}
