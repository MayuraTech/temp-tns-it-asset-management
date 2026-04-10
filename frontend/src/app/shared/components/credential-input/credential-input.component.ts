/**
 * CredentialInputComponent - Reusable Input Component for Login Credentials
 * 
 * Implements minimalist bottom-border styling per Editorial Geometry design system.
 * Supports text and password input types with validation and error display.
 * 
 * Requirements: 1.1, 9.2, 9.3
 */

import { Component, Input, Output, EventEmitter, ChangeDetectionStrategy, forwardRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ControlValueAccessor, NG_VALUE_ACCESSOR } from '@angular/forms';

@Component({
  selector: 'app-credential-input',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './credential-input.component.html',
  styleUrls: ['./credential-input.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  providers: [
    {
      provide: NG_VALUE_ACCESSOR,
      useExisting: forwardRef(() => CredentialInputComponent),
      multi: true
    }
  ]
})
export class CredentialInputComponent implements ControlValueAccessor {
  
  /**
   * Input type - text or password
   * Requirement 1.1
   */
  @Input() type: 'text' | 'password' = 'text';
  
  /**
   * Input label
   * Requirement 1.1
   */
  @Input() label: string = '';
  
  /**
   * Input value
   */
  @Input() value: string = '';
  
  /**
   * Validation error message
   * Requirement 1.1
   */
  @Input() error: string | null = null;
  
  /**
   * Whether the input is disabled
   */
  @Input() disabled: boolean = false;
  
  /**
   * Autocomplete attribute for password managers
   */
  @Input() autocomplete: string = '';
  
  /**
   * Whether to show password visibility toggle
   */
  @Input() showVisibilityToggle: boolean = false;
  
  /**
   * Whether password is currently visible
   */
  @Input() isPasswordVisible: boolean = false;
  
  /**
   * Input ID for accessibility
   */
  @Input() inputId: string = '';
  
  /**
   * Placeholder text
   */
  @Input() placeholder: string = '';
  
  /**
   * Whether to autofocus this input
   * Requirement 6.1
   */
  @Input() autofocus: boolean = false;
  
  /**
   * Event emitted when value changes
   */
  @Output() valueChange = new EventEmitter<string>();
  
  /**
   * Event emitted when input loses focus
   */
  @Output() blur = new EventEmitter<void>();
  
  /**
   * Event emitted when visibility toggle is clicked
   */
  @Output() visibilityToggle = new EventEmitter<void>();
  
  /**
   * Event emitted on keypress
   */
  @Output() keypress = new EventEmitter<KeyboardEvent>();
  
  // ControlValueAccessor implementation
  private onChange: (value: string) => void = () => {};
  private onTouched: () => void = () => {};
  
  /**
   * Handle input value change
   */
  onInputChange(event: Event): void {
    const target = event.target as HTMLInputElement;
    this.value = target.value;
    this.valueChange.emit(this.value);
    this.onChange(this.value);
  }
  
  /**
   * Handle input blur event
   */
  onInputBlur(): void {
    this.blur.emit();
    this.onTouched();
  }
  
  /**
   * Handle visibility toggle click
   */
  onVisibilityToggleClick(): void {
    this.visibilityToggle.emit();
  }
  
  /**
   * Handle keypress event
   */
  onKeyPress(event: KeyboardEvent): void {
    this.keypress.emit(event);
  }
  
  /**
   * Get the current input type (handles password visibility)
   */
  getCurrentInputType(): string {
    if (this.type === 'password' && this.isPasswordVisible) {
      return 'text';
    }
    return this.type;
  }
  
  // ControlValueAccessor methods
  writeValue(value: string): void {
    this.value = value || '';
  }
  
  registerOnChange(fn: (value: string) => void): void {
    this.onChange = fn;
  }
  
  registerOnTouched(fn: () => void): void {
    this.onTouched = fn;
  }
  
  setDisabledState(isDisabled: boolean): void {
    this.disabled = isDisabled;
  }
}
