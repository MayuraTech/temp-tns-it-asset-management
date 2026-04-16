import { Component, OnInit, ChangeDetectionStrategy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { AllocationService } from '../../../../core/services/allocation.service';
import { AssignmentType } from '../../../../shared/models/allocation.model';

/**
 * Allocation Form Component
 * 
 * Provides a reactive form for assigning assets to users or locations.
 * Implements conditional validation based on assignment type.
 * 
 * Features:
 * - Reactive form with FormBuilder
 * - Conditional email validation for USER assignments
 * - Material Design UI components
 * - Editorial Geometry styling
 * - OnPush change detection for performance
 */
@Component({
  selector: 'app-allocation-form',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatButtonModule,
    MatSnackBarModule,
    MatProgressSpinnerModule
  ],
  templateUrl: './allocation-form.component.html',
  styleUrls: ['./allocation-form.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class AllocationFormComponent implements OnInit {
  allocationForm: FormGroup;
  assignmentTypes = [
    { value: AssignmentType.USER, label: 'User' },
    { value: AssignmentType.LOCATION, label: 'Location' }
  ];
  isSubmitting = false;
  assetId: string | null = null;

  constructor(
    private fb: FormBuilder,
    private allocationService: AllocationService,
    private route: ActivatedRoute,
    private router: Router,
    private snackBar: MatSnackBar
  ) {
    this.allocationForm = this.fb.group({
      assignmentType: [AssignmentType.USER, Validators.required],
      assignedTo: ['', [Validators.required, Validators.maxLength(255)]],
      assignedUserEmail: ['']
    });
  }

  ngOnInit(): void {
    // Get asset ID from route parameters
    this.assetId = this.route.snapshot.paramMap.get('id');

    if (!this.assetId) {
      this.snackBar.open('Asset ID is required', 'Close', { duration: 3000 });
      this.router.navigate(['/assets']);
      return;
    }

    // Watch assignment type changes to conditionally require email
    this.allocationForm.get('assignmentType')?.valueChanges.subscribe(type => {
      this.updateEmailValidation(type);
    });

    // Initialize email validation based on default type
    this.updateEmailValidation(this.allocationForm.get('assignmentType')?.value);
  }

  /**
   * Updates email field validation based on assignment type
   * Email is required for USER assignments, optional for LOCATION
   */
  private updateEmailValidation(type: AssignmentType): void {
    const emailControl = this.allocationForm.get('assignedUserEmail');
    
    if (type === AssignmentType.USER) {
      emailControl?.setValidators([Validators.required, Validators.email]);
    } else {
      emailControl?.clearValidators();
    }
    
    emailControl?.updateValueAndValidity();
  }

  /**
   * Handles form submission
   * Validates form and calls appropriate service method based on assignment type
   */
  onSubmit(): void {
    if (this.allocationForm.invalid) {
      this.allocationForm.markAllAsTouched();
      return;
    }

    if (!this.assetId) {
      this.snackBar.open('Asset ID is missing', 'Close', { duration: 3000 });
      return;
    }

    this.isSubmitting = true;
    const formValue = this.allocationForm.value;
    const request = {
      assignmentType: formValue.assignmentType,
      assignedTo: formValue.assignedTo,
      assignedUserEmail: formValue.assignedUserEmail
    };

    const operation = formValue.assignmentType === AssignmentType.USER
      ? this.allocationService.assignToUser(this.assetId, request)
      : this.allocationService.assignToLocation(this.assetId, request);

    operation.subscribe({
      next: () => {
        this.snackBar.open('Asset assigned successfully', 'Close', { duration: 3000 });
        this.router.navigate(['/assets', this.assetId]);
      },
      error: (error) => {
        this.isSubmitting = false;
        this.snackBar.open(error.message || 'Failed to assign asset', 'Close', { duration: 5000 });
      },
      complete: () => {
        this.isSubmitting = false;
      }
    });
  }

  /**
   * Handles cancel button click
   * Navigates back to asset detail page
   */
  onCancel(): void {
    if (this.assetId) {
      this.router.navigate(['/assets', this.assetId]);
    } else {
      this.router.navigate(['/assets']);
    }
  }

  /**
   * Gets error message for a form field
   */
  getErrorMessage(fieldName: string): string {
    const control = this.allocationForm.get(fieldName);
    
    if (!control || !control.errors || !control.touched) {
      return '';
    }

    if (control.errors['required']) {
      return `${this.getFieldLabel(fieldName)} is required`;
    }

    if (control.errors['email']) {
      return 'Please enter a valid email address';
    }

    if (control.errors['maxlength']) {
      const maxLength = control.errors['maxlength'].requiredLength;
      return `${this.getFieldLabel(fieldName)} must not exceed ${maxLength} characters`;
    }

    return 'Invalid value';
  }

  /**
   * Gets user-friendly label for form field
   */
  private getFieldLabel(fieldName: string): string {
    const labels: { [key: string]: string } = {
      assignmentType: 'Assignment type',
      assignedTo: 'Assigned to',
      assignedUserEmail: 'Email address'
    };
    return labels[fieldName] || fieldName;
  }

  /**
   * Checks if a form field has an error and has been touched
   */
  hasError(fieldName: string): boolean {
    const control = this.allocationForm.get(fieldName);
    return !!(control && control.invalid && control.touched);
  }
}
