/**
 * PrimaryActionButtonComponent - Primary Action Button with Glassmorphism
 * 
 * Implements "Add New Asset" button with Editorial Geometry styling,
 * blue-tinted shadow effects, and glassmorphism for elevated states.
 * 
 * Requirements: 10.1, 10.2, 10.3, 10.4, 10.5, 10.6, 10.7, 10.8, 22.5
 */

import { Component, Output, EventEmitter, Input, ChangeDetectionStrategy } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-primary-action-button',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './primary-action-button.component.html',
  styleUrls: ['./primary-action-button.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class PrimaryActionButtonComponent {
  
  /**
   * Button text label
   * Requirement 10.2
   */
  @Input() label: string = 'Add New Asset';
  
  /**
   * Button icon (optional)
   */
  @Input() icon: string = 'add';
  
  /**
   * Whether the button is disabled
   */
  @Input() disabled: boolean = false;
  
  /**
   * Whether the button is in loading state
   */
  @Input() loading: boolean = false;
  
  /**
   * Button size variant
   */
  @Input() size: 'small' | 'medium' | 'large' = 'medium';
  
  /**
   * Whether to show the button at full width
   * Requirement 10.7
   */
  @Input() fullWidth: boolean = false;
  
  /**
   * Event emitted when button is clicked
   * Requirement 10.8
   */
  @Output() buttonClick = new EventEmitter<void>();
  
  /**
   * Handle button click with geometric feedback
   * Requirement 10.8
   */
  onClick(): void {
    if (!this.disabled && !this.loading) {
      this.buttonClick.emit();
    }
  }
}
