import { Injectable } from '@angular/core';

/**
 * ValidationService
 * 
 * Provides validation logic for form fields and generates user-friendly error messages.
 * Supports field-level and form-level validation.
 * 
 * Requirements: 2.1, 2.2, 2.3
 */
@Injectable({
  providedIn: 'root'
})
export class ValidationService {

  /**
   * Validates if a field is required and not empty
   * 
   * @param value - The field value to validate
   * @returns true if valid, false if empty
   */
  isRequired(value: string | null | undefined): boolean {
    return value !== null && value !== undefined && value.trim().length > 0;
  }

  /**
   * Validates username field
   * 
   * @param username - The username value
   * @returns Validation error message or null if valid
   */
  validateUsername(username: string | null | undefined): string | null {
    if (!this.isRequired(username)) {
      return 'Username is required';
    }
    return null;
  }

  /**
   * Validates password field
   * 
   * @param password - The password value
   * @returns Validation error message or null if valid
   */
  validatePassword(password: string | null | undefined): string | null {
    if (!this.isRequired(password)) {
      return 'Password is required';
    }
    return null;
  }

  /**
   * Validates all login form fields
   * 
   * @param username - The username value
   * @param password - The password value
   * @returns Object containing validation errors for each field
   */
  validateLoginForm(username: string | null | undefined, password: string | null | undefined): {
    username: string | null;
    password: string | null;
  } {
    return {
      username: this.validateUsername(username),
      password: this.validatePassword(password)
    };
  }

  /**
   * Checks if the form is valid (no validation errors)
   * 
   * @param username - The username value
   * @param password - The password value
   * @returns true if form is valid, false otherwise
   */
  isFormValid(username: string | null | undefined, password: string | null | undefined): boolean {
    const errors = this.validateLoginForm(username, password);
    return errors.username === null && errors.password === null;
  }

  /**
   * Generates a user-friendly error message for a field
   * 
   * @param fieldName - The name of the field
   * @param errorType - The type of validation error
   * @returns User-friendly error message
   */
  getErrorMessage(fieldName: string, errorType: string): string {
    const messages: { [key: string]: { [key: string]: string } } = {
      username: {
        required: 'Username is required'
      },
      password: {
        required: 'Password is required'
      }
    };

    return messages[fieldName]?.[errorType] || 'Invalid input';
  }
}
