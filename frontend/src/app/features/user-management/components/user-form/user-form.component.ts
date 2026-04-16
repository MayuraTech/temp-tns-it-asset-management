import { Component, ChangeDetectionStrategy, OnInit, OnDestroy, signal, computed, input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, ActivatedRoute, RouterModule } from '@angular/router';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule, AbstractControl, ValidationErrors } from '@angular/forms';
import { Subject, takeUntil, finalize } from 'rxjs';

// Material imports
import { MaterialModule } from '../../../../shared/material.module';
import { MatSnackBar } from '@angular/material/snack-bar';

// Services and models
import { UserService } from '../../services/user.service';
import { UserDTO, UserRequest, UserUpdateRequest } from '../../models/user.model';
import { Role } from '../../../../core/models/auth.model';

// Validators
import { passwordComplexityValidator } from '../../../../shared/validators/custom-validators';

/**
 * User Form Component
 * 
 * Implements UserFormComponent for creating and editing users.
 * Features:
 * - Comprehensive form validation with real-time feedback
 * - Password strength indicator
 * - Role selection with multi-select
 * - Proper error handling and success notifications
 * - Editorial Geometry UI standards
 * 
 * Requirements: 4.1, 4.2, 4.3, 4.4, 4.5, 4.6, 4.7, 4.8, 6.1, 6.2, 6.3, 6.4, 6.5, 6.6
 */
@Component({
  selector: 'app-user-form',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    ReactiveFormsModule,
    MaterialModule
  ],
  templateUrl: './user-form.component.html',
  styleUrls: ['./user-form.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class UserFormComponent implements OnInit, OnDestroy {
  // Signals for reactive state management
  loading = signal<boolean>(false);
  isEditMode = signal<boolean>(false);
  userId = signal<string | null>(null);
  existingUser = signal<UserDTO | null>(null);
  
  // Form
  userForm!: FormGroup;
  
  // Password strength
  passwordStrength = signal<'weak' | 'medium' | 'strong' | null>(null);
  passwordStrengthText = computed(() => {
    const strength = this.passwordStrength();
    if (!strength) return '';
    
    switch (strength) {
      case 'weak':
        return 'Weak - Add more complexity';
      case 'medium':
        return 'Medium - Consider adding more characters';
      case 'strong':
        return 'Strong - Good password!';
      default:
        return '';
    }
  });
  
  // Available roles
  availableRoles = [
    { value: Role.ADMINISTRATOR, label: 'Administrator', description: 'Full system access and user management' },
    { value: Role.ASSET_MANAGER, label: 'Asset Manager', description: 'Manage assets, tickets, and assignments' },
    { value: Role.VIEWER, label: 'Viewer', description: 'Read-only access to system data' }
  ];
  
  // Role enum for template
  Role = Role;
  
  // Page title
  pageTitle = computed(() => this.isEditMode() ? 'Edit User' : 'Create User');
  submitButtonText = computed(() => this.isEditMode() ? 'Save Changes' : 'Create User');
  
  // Password visibility toggles
  hidePassword = true;
  hideConfirmPassword = true;
  
  private destroy$ = new Subject<void>();
  
  constructor(
    private fb: FormBuilder,
    private userService: UserService,
    private router: Router,
    private route: ActivatedRoute,
    private snackBar: MatSnackBar
  ) {}
  
  ngOnInit(): void {
    this.initializeForm();
    this.checkEditMode();
    this.setupPasswordStrengthListener();
  }
  
  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }
  
  /**
   * Initialize the form with validators
   */
  private initializeForm(): void {
    this.userForm = this.fb.group({
      username: ['', [
        Validators.required,
        Validators.minLength(3),
        Validators.maxLength(100),
        Validators.pattern(/^[a-zA-Z0-9_]+$/)
      ]],
      email: ['', [
        Validators.required,
        Validators.email,
        Validators.minLength(5),
        Validators.maxLength(255)
      ]],
      password: ['', [
        Validators.required,
        Validators.minLength(8),
        passwordComplexityValidator()
      ]],
      confirmPassword: ['', [Validators.required]],
      roles: [[], [Validators.required, this.atLeastOneRoleValidator()]]
    }, {
      validators: this.passwordMatchValidator()
    });
  }
  
  /**
   * Check if we're in edit mode and load user data
   */
  private checkEditMode(): void {
    const id = this.route.snapshot.paramMap.get('id');
    
    if (id) {
      this.isEditMode.set(true);
      this.userId.set(id);
      this.loadUserData(id);
      
      // In edit mode, password is optional
      this.userForm.get('password')?.clearValidators();
      this.userForm.get('password')?.setValidators([
        Validators.minLength(8),
        passwordComplexityValidator()
      ]);
      this.userForm.get('confirmPassword')?.clearValidators();
      this.userForm.get('password')?.updateValueAndValidity();
      this.userForm.get('confirmPassword')?.updateValueAndValidity();
    }
  }
  
  /**
   * Load existing user data for editing
   */
  private loadUserData(id: string): void {
    this.loading.set(true);
    
    this.userService.getUser(id)
      .pipe(
        takeUntil(this.destroy$),
        finalize(() => this.loading.set(false))
      )
      .subscribe({
        next: (user: UserDTO) => {
          this.existingUser.set(user);
          this.userForm.patchValue({
            username: user.username,
            email: user.email,
            roles: user.roles
          });
        },
        error: (error) => {
          console.error('Error loading user:', error);
          this.snackBar.open('Failed to load user data', 'Close', {
            duration: 3000,
            panelClass: ['error-snackbar']
          });
          this.router.navigate(['/users']);
        }
      });
  }
  
  /**
   * Setup password strength listener
   */
  private setupPasswordStrengthListener(): void {
    this.userForm.get('password')?.valueChanges
      .pipe(takeUntil(this.destroy$))
      .subscribe((password: string) => {
        if (!password) {
          this.passwordStrength.set(null);
          return;
        }
        
        this.passwordStrength.set(this.calculatePasswordStrength(password));
      });
  }
  
  /**
   * Calculate password strength
   */
  private calculatePasswordStrength(password: string): 'weak' | 'medium' | 'strong' {
    let strength = 0;
    
    // Length
    if (password.length >= 8) strength++;
    if (password.length >= 12) strength++;
    if (password.length >= 16) strength++;
    
    // Character types
    if (/[a-z]/.test(password)) strength++;
    if (/[A-Z]/.test(password)) strength++;
    if (/[0-9]/.test(password)) strength++;
    if (/[!@#$%^&*()_+\-=\[\]{};':"\\|,.<>\/?]/.test(password)) strength++;
    
    // Variety
    const uniqueChars = new Set(password.split('')).size;
    if (uniqueChars >= password.length * 0.7) strength++;
    
    if (strength <= 3) return 'weak';
    if (strength <= 6) return 'medium';
    return 'strong';
  }
  
  /**
   * Custom validator to ensure passwords match
   */
  private passwordMatchValidator() {
    return (formGroup: AbstractControl): ValidationErrors | null => {
      const password = formGroup.get('password')?.value;
      const confirmPassword = formGroup.get('confirmPassword')?.value;
      
      if (!password || !confirmPassword) {
        return null;
      }
      
      return password === confirmPassword ? null : { passwordMismatch: true };
    };
  }
  
  /**
   * Custom validator to ensure at least one role is selected
   */
  private atLeastOneRoleValidator() {
    return (control: AbstractControl): ValidationErrors | null => {
      const roles = control.value;
      return roles && roles.length > 0 ? null : { atLeastOneRole: true };
    };
  }
  
  /**
   * Check if a role is selected
   */
  isRoleSelected(role: Role): boolean {
    const roles = this.userForm.get('roles')?.value || [];
    return roles.includes(role);
  }
  
  /**
   * Toggle role selection
   */
  toggleRole(role: Role): void {
    const rolesControl = this.userForm.get('roles');
    const currentRoles = rolesControl?.value || [];
    
    const index = currentRoles.indexOf(role);
    if (index > -1) {
      // Remove role
      currentRoles.splice(index, 1);
    } else {
      // Add role
      currentRoles.push(role);
    }
    
    rolesControl?.setValue([...currentRoles]);
    rolesControl?.markAsTouched();
  }
  
  /**
   * Get error message for a form field
   */
  getErrorMessage(fieldName: string): string {
    const control = this.userForm.get(fieldName);
    
    if (!control || !control.errors || !control.touched) {
      return '';
    }
    
    const errors = control.errors;
    
    // Username errors
    if (fieldName === 'username') {
      if (errors['required']) return 'Username is required';
      if (errors['minlength']) return `Username must be at least ${errors['minlength'].requiredLength} characters`;
      if (errors['maxlength']) return `Username must not exceed ${errors['maxlength'].requiredLength} characters`;
      if (errors['pattern']) return 'Username can only contain letters, numbers, and underscores';
    }
    
    // Email errors
    if (fieldName === 'email') {
      if (errors['required']) return 'Email is required';
      if (errors['email']) return 'Please enter a valid email address';
      if (errors['minlength']) return `Email must be at least ${errors['minlength'].requiredLength} characters`;
      if (errors['maxlength']) return `Email must not exceed ${errors['maxlength'].requiredLength} characters`;
    }
    
    // Password errors
    if (fieldName === 'password') {
      if (errors['required']) return 'Password is required';
      if (errors['minlength']) return `Password must be at least ${errors['minlength'].requiredLength} characters`;
      if (errors['passwordComplexity']) {
        const complexityErrors = errors['passwordComplexity'];
        if (complexityErrors['uppercase']) return 'Password must contain at least one uppercase letter';
        if (complexityErrors['lowercase']) return 'Password must contain at least one lowercase letter';
        if (complexityErrors['number']) return 'Password must contain at least one number';
        if (complexityErrors['specialChar']) return 'Password must contain at least one special character';
      }
    }
    
    // Confirm password errors
    if (fieldName === 'confirmPassword') {
      if (errors['required']) return 'Please confirm your password';
      if (this.userForm.errors?.['passwordMismatch']) return 'Passwords do not match';
    }
    
    // Roles errors
    if (fieldName === 'roles') {
      if (errors['required'] || errors['atLeastOneRole']) return 'At least one role must be selected';
    }
    
    return 'Invalid value';
  }
  
  /**
   * Check if a field has an error
   */
  hasError(fieldName: string): boolean {
    const control = this.userForm.get(fieldName);
    return !!(control && control.errors && control.touched);
  }
  
  /**
   * Submit the form
   */
  onSubmit(): void {
    if (this.userForm.invalid) {
      this.userForm.markAllAsTouched();
      this.snackBar.open('Please fix the form errors before submitting', 'Close', {
        duration: 3000,
        panelClass: ['error-snackbar']
      });
      return;
    }
    
    if (this.isEditMode()) {
      this.updateUser();
    } else {
      this.createUser();
    }
  }
  
  /**
   * Create new user
   */
  private createUser(): void {
    this.loading.set(true);
    
    const formValue = this.userForm.value;
    const request: UserRequest = {
      username: formValue.username,
      email: formValue.email,
      password: formValue.password,
      roles: formValue.roles
    };
    
    this.userService.createUser(request)
      .pipe(
        takeUntil(this.destroy$),
        finalize(() => this.loading.set(false))
      )
      .subscribe({
        next: (user: UserDTO) => {
          this.snackBar.open('User created successfully', 'Close', {
            duration: 3000,
            panelClass: ['success-snackbar']
          });
          this.router.navigate(['/users', user.id]);
        },
        error: (error) => {
          console.error('Error creating user:', error);
          
          let errorMessage = 'Failed to create user';
          if (error.error?.message) {
            errorMessage = error.error.message;
          } else if (error.error?.type === 'DUPLICATE_USERNAME') {
            errorMessage = 'Username already exists';
          } else if (error.error?.type === 'DUPLICATE_EMAIL') {
            errorMessage = 'Email already exists';
          }
          
          this.snackBar.open(errorMessage, 'Close', {
            duration: 5000,
            panelClass: ['error-snackbar']
          });
        }
      });
  }
  
  /**
   * Update existing user
   */
  private updateUser(): void {
    const id = this.userId();
    if (!id) return;
    
    this.loading.set(true);
    
    const formValue = this.userForm.value;
    const request: UserUpdateRequest = {
      username: formValue.username,
      email: formValue.email
    };
    
    this.userService.updateUser(id, request)
      .pipe(
        takeUntil(this.destroy$),
        finalize(() => this.loading.set(false))
      )
      .subscribe({
        next: (user: UserDTO) => {
          // Update roles if changed
          this.updateRolesIfNeeded(id, formValue.roles, user.roles);
          
          this.snackBar.open('User updated successfully', 'Close', {
            duration: 3000,
            panelClass: ['success-snackbar']
          });
          this.router.navigate(['/users', user.id]);
        },
        error: (error) => {
          console.error('Error updating user:', error);
          
          let errorMessage = 'Failed to update user';
          if (error.error?.message) {
            errorMessage = error.error.message;
          } else if (error.error?.type === 'DUPLICATE_USERNAME') {
            errorMessage = 'Username already exists';
          } else if (error.error?.type === 'DUPLICATE_EMAIL') {
            errorMessage = 'Email already exists';
          }
          
          this.snackBar.open(errorMessage, 'Close', {
            duration: 5000,
            panelClass: ['error-snackbar']
          });
        }
      });
  }
  
  /**
   * Update roles if they have changed
   */
  private updateRolesIfNeeded(userId: string, newRoles: Role[], currentRoles: Role[]): void {
    // Find roles to add
    const rolesToAdd = newRoles.filter(role => !currentRoles.includes(role));
    
    // Find roles to remove
    const rolesToRemove = currentRoles.filter(role => !newRoles.includes(role));
    
    // Add new roles
    rolesToAdd.forEach(role => {
      this.userService.assignRole(userId, role)
        .pipe(takeUntil(this.destroy$))
        .subscribe({
          error: (error) => {
            console.error(`Error assigning role ${role}:`, error);
          }
        });
    });
    
    // Remove old roles
    rolesToRemove.forEach(role => {
      this.userService.revokeRole(userId, role)
        .pipe(takeUntil(this.destroy$))
        .subscribe({
          error: (error) => {
            console.error(`Error revoking role ${role}:`, error);
          }
        });
    });
  }
  
  /**
   * Cancel and navigate back
   */
  onCancel(): void {
    if (this.userForm.dirty) {
      const confirmed = confirm('You have unsaved changes. Are you sure you want to cancel?');
      if (!confirmed) {
        return;
      }
    }
    
    this.router.navigate(['/users']);
  }
  
  /**
   * Get password strength class for styling
   */
  getPasswordStrengthClass(): string {
    const strength = this.passwordStrength();
    if (!strength) return '';
    
    return `password-strength-${strength}`;
  }
}
