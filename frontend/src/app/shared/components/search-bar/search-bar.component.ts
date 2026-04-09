/**
 * SearchBarComponent - Ghost Border Search Input
 * 
 * Implements search input with ghost border styling, editorial typography,
 * and geometric hover effects following Editorial Geometry principles.
 * 
 * Requirements: 5.1, 5.2, 5.3, 5.4, 5.5, 5.6, 5.7, 5.8, 25.5, 25.6
 */

import { Component, Output, EventEmitter, Input, ChangeDetectionStrategy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-search-bar',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './search-bar.component.html',
  styleUrls: ['./search-bar.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class SearchBarComponent {
  
  /**
   * Placeholder text for the search input
   * Requirement 5.1
   */
  @Input() placeholder: string = 'Search infrastructure...';
  
  /**
   * Current search query value
   */
  @Input() value: string = '';
  
  /**
   * Event emitted when search input changes
   * Requirement 5.6
   */
  @Output() searchChange = new EventEmitter<string>();
  
  /**
   * Event emitted when search is submitted (Enter key)
   */
  @Output() searchSubmit = new EventEmitter<string>();
  
  /**
   * Handle input changes with validation
   * Requirement 5.6
   */
  onInputChange(value: string): void {
    // Validate and sanitize input
    const sanitizedValue = this.sanitizeInput(value);
    this.searchChange.emit(sanitizedValue);
  }
  
  /**
   * Handle Enter key press for search submission
   */
  onKeyPress(event: KeyboardEvent): void {
    if (event.key === 'Enter') {
      this.searchSubmit.emit(this.value);
    }
  }
  
  /**
   * Sanitize search input to prevent XSS
   */
  private sanitizeInput(value: string): string {
    // Remove potentially dangerous characters
    return value.replace(/<[^>]*>/g, '').trim();
  }
}
