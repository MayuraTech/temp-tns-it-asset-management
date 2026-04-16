import { Component, ChangeDetectionStrategy, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { BehaviorSubject, Subject, finalize, takeUntil } from 'rxjs';
import { ProfileService } from '../../services/profile.service';
import { UserDTO, ProfileUpdateRequest, ChangePasswordRequest } from '../../models';

/**
 * User Profile Component
 * 
 * Displays and allows editing of the current user's profile including:
 * - Personal information
 * - Email updates with validation
 * - Password change functionality with current password verification
 * - Account activity history
 * - Proper validation and error handling
 */
@Component({
  selector: 'app-user-profile',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './user-profile.component.html',
  styleUrls: ['./user-profile.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class UserProfileComponent implements OnInit, OnDestroy {
  profile$ = new BehaviorSubject<UserDTO | null>(null);
  loading$ = new BehaviorSubject<boolean>(false);
  error$ = new BehaviorSubject<string | null>(null);
  successMessage$ = new BehaviorSubject<string | null>(null);

  profileForm!: FormGroup;
  passwordForm!: FormGroup;

  isEditingProfile = false;
  isChangingPassword = false;

  private destroy$ = new Subject<void>();

  constructor(
    private profileService: ProfileService,
    private fb: FormBuilder
  ) {
    this.initializeForms();
  }

  ngOnInit(): void {
    this.loadProfile();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  /**
   * Initializes the profile and password forms
   */
  private initializeForms(): void {
    this.profileForm = this.fb.group({
      email: ['', [Validators.required, Validators.email, Validators.maxLength(255)]]
    });

    this.passwordForm = this.fb.group({
      currentPassword: ['', [Validators.required]],
      newPassword: ['', [
        Validators.required,
        Validators.minLength(8),
        Validators.pattern(/^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&])[A-Za-z\d@$!%*?&]{8,}$/)
      ]],
      confirmPassword: ['', [Validators.required]]
    }, { validators: this.passwordMatchValidator });
  }

  /**
   * Custom validator to check if passwords match
   */
  private passwordMatchValidator(group: FormGroup): { [key: string]: boolean } | null {
    const newPassword = group.get('newPassword')?.value;
    const confirmPassword = group.get('confirmPassword')?.value;
    
    if (newPassword && confirmPassword && newPassword !== confirmPassword) {
      return { passwordMismatch: true };
    }
    
    return null;
  }

  /**
   * Loads the current user's profile
   */
  loadProfile(): void {
    this.loading$.next(true);
    this.error$.next(null);

    this.profileService.getProfile()
      .pipe(
        takeUntil(this.destroy$),
        finalize(() => this.loading$.next(false))
      )
      .subscribe({
        next: (profile) => {
          this.profile$.next(profile);
          this.profileForm.patchValue({
            email: profile.email
          });
        },
        error: (error) => {
          this.error$.next('Failed to load profile. Please try again.');
          console.error('Error loading profile:', error);
        }
      });
  }

  /**
   * Enables profile editing mode
   */
  startEditingProfile(): void {
    this.isEditingProfile = true;
    this.successMessage$.next(null);
    this.error$.next(null);
  }

  /**
   * Cancels profile editing and resets form
   */
  cancelEditingProfile(): void {
    this.isEditingProfile = false;
    const profile = this.profile$.value;
    if (profile) {
      this.profileForm.patchValue({
        email: profile.email
      });
    }
    this.error$.next(null);
  }

  /**
   * Saves profile updates
   */
  saveProfile(): void {
    if (this.profileForm.invalid) {
      this.profileForm.markAllAsTouched();
      return;
    }

    this.loading$.next(true);
    this.error$.next(null);
    this.successMessage$.next(null);

    const request: ProfileUpdateRequest = {
      email: this.profileForm.value.email
    };

    this.profileService.updateProfile(request)
      .pipe(
        takeUntil(this.destroy$),
        finalize(() => this.loading$.next(false))
      )
      .subscribe({
        next: (updatedProfile) => {
          this.profile$.next(updatedProfile);
          this.isEditingProfile = false;
          this.successMessage$.next('Profile updated successfully');
          setTimeout(() => this.successMessage$.next(null), 5000);
        },
        error: (error) => {
          const errorMessage = error.error?.message || 'Failed to update profile. Please try again.';
          this.error$.next(errorMessage);
          console.error('Error updating profile:', error);
        }
      });
  }

  /**
   * Toggles password change form visibility
   */
  togglePasswordChange(): void {
    this.isChangingPassword = !this.isChangingPassword;
    if (!this.isChangingPassword) {
      this.passwordForm.reset();
    }
    this.error$.next(null);
    this.successMessage$.next(null);
  }

  /**
   * Changes the user's password
   */
  changePassword(): void {
    if (this.passwordForm.invalid) {
      this.passwordForm.markAllAsTouched();
      return;
    }

    this.loading$.next(true);
    this.error$.next(null);
    this.successMessage$.next(null);

    const request: ChangePasswordRequest = {
      currentPassword: this.passwordForm.value.currentPassword,
      newPassword: this.passwordForm.value.newPassword
    };

    this.profileService.changePassword(request)
      .pipe(
        takeUntil(this.destroy$),
        finalize(() => this.loading$.next(false))
      )
      .subscribe({
        next: () => {
          this.passwordForm.reset();
          this.isChangingPassword = false;
          this.successMessage$.next('Password changed successfully. You will need to log in again.');
          setTimeout(() => this.successMessage$.next(null), 5000);
        },
        error: (error) => {
          const errorMessage = error.error?.message || 'Failed to change password. Please check your current password and try again.';
          this.error$.next(errorMessage);
          console.error('Error changing password:', error);
        }
      });
  }

  /**
   * Gets validation error message for a form field
   */
  getErrorMessage(formGroup: FormGroup, fieldName: string): string {
    const control = formGroup.get(fieldName);
    
    if (!control || !control.errors || !control.touched) {
      return '';
    }

    if (control.errors['required']) {
      return `${this.getFieldLabel(fieldName)} is required`;
    }

    if (control.errors['email']) {
      return 'Please enter a valid email address';
    }

    if (control.errors['minlength']) {
      return `${this.getFieldLabel(fieldName)} must be at least ${control.errors['minlength'].requiredLength} characters`;
    }

    if (control.errors['maxlength']) {
      return `${this.getFieldLabel(fieldName)} must not exceed ${control.errors['maxlength'].requiredLength} characters`;
    }

    if (control.errors['pattern'] && fieldName === 'newPassword') {
      return 'Password must contain at least 8 characters, one uppercase letter, one lowercase letter, one digit, and one special character';
    }

    return 'Invalid value';
  }

  /**
   * Gets form-level error message
   */
  getFormErrorMessage(formGroup: FormGroup): string {
    if (formGroup.errors?.['passwordMismatch']) {
      return 'Passwords do not match';
    }
    return '';
  }

  /**
   * Gets human-readable field label
   */
  private getFieldLabel(fieldName: string): string {
    const labels: { [key: string]: string } = {
      email: 'Email',
      currentPassword: 'Current password',
      newPassword: 'New password',
      confirmPassword: 'Confirm password'
    };
    return labels[fieldName] || fieldName;
  }

  /**
   * Formats date for display
   */
  formatDate(date: Date | undefined): string {
    if (!date) return 'Never';
    return new Date(date).toLocaleString();
  }

  /**
   * Formats roles for display
   */
  formatRoles(roles: string[]): string {
    return roles.map(role => role.replace('_', ' ')).join(', ');
  }
}
