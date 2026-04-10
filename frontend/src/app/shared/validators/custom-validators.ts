import { AbstractControl, ValidationErrors, ValidatorFn } from '@angular/forms';

/**
 * Custom validators for form validation
 * Provides reusable ValidatorFn functions for common validation scenarios
 */

/**
 * Email validator - validates email format
 * More comprehensive than Angular's built-in email validator
 * @returns ValidatorFn that returns ValidationErrors or null
 */
export function emailValidator(): ValidatorFn {
  return (control: AbstractControl): ValidationErrors | null => {
    if (!control.value) {
      return null; // Don't validate empty values (use Validators.required for that)
    }

    const emailRegex = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/;
    const valid = emailRegex.test(control.value);

    return valid ? null : { email: { value: control.value } };
  };
}

/**
 * Password complexity validator
 * Validates password meets complexity requirements:
 * - Minimum 8 characters
 * - At least one uppercase letter
 * - At least one lowercase letter
 * - At least one number
 * - At least one special character
 * @returns ValidatorFn that returns ValidationErrors or null
 */
export function passwordComplexityValidator(): ValidatorFn {
  return (control: AbstractControl): ValidationErrors | null => {
    if (!control.value) {
      return null; // Don't validate empty values
    }

    const password = control.value;
    const errors: ValidationErrors = {};

    // Check minimum length
    if (password.length < 8) {
      errors['minLength'] = { requiredLength: 8, actualLength: password.length };
    }

    // Check for uppercase letter
    if (!/[A-Z]/.test(password)) {
      errors['uppercase'] = { message: 'Password must contain at least one uppercase letter' };
    }

    // Check for lowercase letter
    if (!/[a-z]/.test(password)) {
      errors['lowercase'] = { message: 'Password must contain at least one lowercase letter' };
    }

    // Check for number
    if (!/[0-9]/.test(password)) {
      errors['number'] = { message: 'Password must contain at least one number' };
    }

    // Check for special character
    if (!/[!@#$%^&*()_+\-=\[\]{};':"\\|,.<>\/?]/.test(password)) {
      errors['specialChar'] = { message: 'Password must contain at least one special character' };
    }

    return Object.keys(errors).length > 0 ? { passwordComplexity: errors } : null;
  };
}

/**
 * Date not in future validator
 * Ensures the date is not in the future (today or earlier)
 * Useful for acquisition dates, birth dates, etc.
 * @returns ValidatorFn that returns ValidationErrors or null
 */
export function dateNotInFutureValidator(): ValidatorFn {
  return (control: AbstractControl): ValidationErrors | null => {
    if (!control.value) {
      return null; // Don't validate empty values
    }

    const inputDate = new Date(control.value);
    
    // Check if date is valid
    if (isNaN(inputDate.getTime())) {
      return { invalidDate: { value: control.value } };
    }

    const today = new Date();
    today.setHours(0, 0, 0, 0); // Reset time to start of day
    
    const inputDateOnly = new Date(inputDate);
    inputDateOnly.setHours(0, 0, 0, 0); // Reset time to start of day

    if (inputDateOnly > today) {
      return { 
        futureDate: { 
          value: control.value,
          message: 'Date cannot be in the future'
        } 
      };
    }

    return null;
  };
}
